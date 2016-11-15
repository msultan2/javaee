/*
    WARNING:
    The implementation contained in this module is GPL licensed.
    This module interacts with bluez library (www.bluez.org) which is GPL
    licensed. The source contains some code directly copied from the
    library source or associated tools source (hcidump in particular).
    To overcome the licencing issues and use this module some techniques
    have to be applied - see http://www.gnu.org/licenses/gpl-faq.html#NFUseGPLPlugins.
 */

#include "bluetoothdriver_rawhci.h"

#include "applicationconfiguration.h"
#include "datacontainer.h"
#include "lock.h"
#include "logger.h"
#include "os_utilities.h"
#include "tools/hcidump.h"
#include "tools/parser/hci_parser.h"
#include "utils.h"

#include <errno.h>
#include <stdio.h>

#include <poll.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/socket.h>

#include "bluetooth/lib/bluetooth.h"
#include "bluetooth/lib/hci.h"
#include "bluetooth/lib/hci_lib.h"
#include "bluetooth/tools/parser/parser.h"


// Enable SAVE_RAW_RSSI_SCAN_RESULTS_TO_FILE variable to start saving intermediate inquiry results
//#define SAVE_RAW_RSSI_SCAN_RESULTS_TO_FILE


namespace
{
    const char OUTPUT_FILE_NAME[] = "raw_rssi_scan_results.csv";
}


namespace BlueTooth
{

static int addDeviceInfo(int dd, int dev_id, long arg);



/* Default options */
static int  snap_len = SNAP_LEN;

static uint64_t convert_bdaddr_to_uint64(const bdaddr_t& value)
{
    uint64_t result =
     (uint64_t)value.b[0]        +
    ((uint64_t)value.b[1] <<  8) +
    ((uint64_t)value.b[2] << 16) +
    ((uint64_t)value.b[3] << 24) +
    ((uint64_t)value.b[4] << 32) +
    ((uint64_t)value.b[5] << 40);

    return result;
}

static bdaddr_t convert_uint64_to_bdaddr(const uint64_t& value)
{
	::bdaddr_t result = {{0}};;
    result.b[0] =  value & 0xFFULL;
    result.b[1] = (value & 0xFF00ULL) >> 8;
    result.b[2] = (value & 0xFF0000ULL) >> 16;
    result.b[3] = (value & 0xFF000000ULL) >> 24;
    result.b[4] = (value & 0xFF00000000ULL) >> 32;
    result.b[5] = (value & 0xFF0000000000ULL) >> 40;

    return result;
}

struct TMonitorEventsData
{
    int hciRoute;
    Model::DataContainer::TRemoteDeviceRecordCollection* pRemoteDeviceCollection;
    ::Mutex& remoteDeviceCollectionMutex;
    std::ofstream* pOutputFile;
    bool result;
    volatile bool finish;
};

//This is a function which is called as a separate thread to monitor for incoming bluetooth events
static void* monitorEvents(void* arg)
{
    if (arg == 0)
    {
        ::pthread_exit(arg);
        return arg;
    }
    //else continue

    TMonitorEventsData& data = *(TMonitorEventsData*)arg;

    //Prepare for incomming events
    struct ::frame frm;
    int hdr_size = BTSNOOP_PKT_SIZE;
    char* pBuffer = (char*)malloc(snap_len + hdr_size);
    if (pBuffer == 0)
    {
        perror("Can't allocate data buffer");

        ::pthread_exit(arg);
        return arg;
    }
    //else continue

    frm.data = pBuffer + hdr_size;

    char* pCtrl = (char*)malloc(100);
    if (pCtrl == 0)
    {
        free(pBuffer);
        perror("Can't allocate control buffer");

        ::pthread_exit(arg);
        return arg;
    }
    //else continue

    struct ::iovec  iv;

    struct ::cmsghdr *cmsg;
    struct ::msghdr msg;
    memset(&msg, 0, sizeof(msg));

    struct ::pollfd fds[2];
    ::nfds_t nfds = 0;
    int sock = open_socket(data.hciRoute);
    if (sock < 0)
    {
        free(pBuffer);
        free(pCtrl);

        std::ostringstream ss;
        ss << "open_socket() failed: " << strerror(errno);
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

        ::pthread_exit(arg);
        return arg;
    }
    //else continue

    fds[nfds].fd = sock; //file descriptor
    fds[nfds].events = POLLIN; //requested events
    fds[nfds].revents = 0; //returned events
    ++nfds;

    //Wait for events of type EVT_INQUIRY_RESULT and EVT_INQUIRY_COMPLETE
    int ready = 0;
    while (!data.finish)
    {
        ready = ::poll(fds, nfds, 100); //the third argument is timeout in ms
        if (ready <= 0)
            continue;

        if (fds[0].revents & (POLLHUP | POLLERR | POLLNVAL))
        {
            if (fds[0].fd == sock)
                printf("device: disconnected\n");
            else
                printf("client: disconnect\n");

            data.finish = false;
            data.result = false;
            break;
        }
        //else do nothing

        iv.iov_base = frm.data;
        iv.iov_len  = snap_len;

        msg.msg_iov = &iv;
        msg.msg_iovlen = 1;
        msg.msg_control = pCtrl;
        msg.msg_controllen = 100;

        int len = ::recvmsg(sock, &msg, MSG_DONTWAIT);
        if (len < 0)
        {
            if ((errno == EAGAIN) || (errno == EINTR))
                continue;

            std::ostringstream ss;
            ss << "recvmsg(): " << strerror(errno);
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

            data.finish = false;
            data.result = false;
            break;
        }

        /* Process control message */
        frm.data_len = len;
        frm.dev_id = data.hciRoute;
        frm.in = 0;

        cmsg = CMSG_FIRSTHDR(&msg);
        while (cmsg) {
            int dir;
            switch (cmsg->cmsg_type) {
            case HCI_CMSG_DIR:
                memcpy(&dir, CMSG_DATA(cmsg), sizeof(int));
                frm.in = (uint8_t) dir;
                break;
            case HCI_CMSG_TSTAMP:
                memcpy(&frm.ts, CMSG_DATA(cmsg),
                        sizeof(struct timeval));
                break;
            }
            cmsg = CMSG_NXTHDR(&msg, cmsg);
        }

        frm.ptr = frm.data;
        frm.len = frm.data_len;

        if (frm.ptr == 0)
        {
            data.finish = false;
            data.result = false;
            break;
        }
        //else do nothing

        //from tools/parser/hci.c Line 4091 hci_dump(...)
        int type = static_cast<int>(*reinterpret_cast<uint8_t*>(frm.ptr));
        frm.ptr = static_cast<uint8_t*>(frm.ptr) + 1;
        frm.len--;

        hci_event_hdr* hdr = reinterpret_cast<hci_event_hdr*>(frm.ptr);
        uint8_t eventType = hdr->evt;

        switch (type)
        {
            case HCI_EVENT_PKT:
            {
                //based on tools/parser/hci.c Line 3775 event_dump(...)
                if (
                    (eventType <= EVENT_NUM) &&
                    (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
                    )
                {
                    std::ostringstream ss;
                    ss << "HCI Event: " << event_str[eventType] << " (0x" << std::hex << static_cast<unsigned int>(hdr->evt) << ")";
                    Logger::log(LOG_LEVEL_DEBUG3, ss.str().c_str());
                }
                //else do nothing

                frm.ptr = static_cast<uint8_t*>(frm.ptr) + HCI_EVENT_HDR_SIZE;
                frm.len -= HCI_EVENT_HDR_SIZE;

                switch (eventType)
                {
                    case EVT_INQUIRY_COMPLETE:
                    case EVT_CMD_COMPLETE:
                    {
                        //status_response_dump(level + 1, frm);
                        uint8_t status = *reinterpret_cast<uint8_t*>(frm.ptr);
                        frm.ptr = static_cast<uint8_t*>(frm.ptr) + 1;
                        frm.len--;

                        if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
                        {
                            std::ostringstream ss;
                            ss << "HCI Inquiry/Command Exit Status: 0x" << std::hex << static_cast<unsigned int>(status);
                            Logger::log(LOG_LEVEL_DEBUG3, ss.str().c_str());
                        }
                        //else do nothing

                        data.finish = true;
                        break;
                    }

                    case EVT_INQUIRY_RESULT:
                    case EVT_INQUIRY_RESULT_WITH_RSSI:
                    case EVT_EXTENDED_INQUIRY_RESULT:
                    {
                        //based on tools/parser/hci.c Line 3029 inq_result_dump(...)
                        uint8_t num = *reinterpret_cast<uint8_t*>(frm.ptr);
                        frm.ptr = static_cast<uint8_t*>(frm.ptr) + 1;
                        frm.len--;

                        int i;

                        for (i = 0; i < num; i++)
                        {
                            uint64_t remoteDeviceAddress = 0;
                            uint32_t remoteDeviceClass = 0;
                            int remoteDeviceRssi = 0;

                            if (eventType == EVT_INQUIRY_RESULT)
                            {
                                inquiry_info* info = static_cast<inquiry_info*>(frm.ptr);

                                remoteDeviceAddress = convert_bdaddr_to_uint64(info->bdaddr);
                                remoteDeviceClass =
                                     (uint32_t)info->dev_class[0]        +
                                    ((uint32_t)info->dev_class[1] <<  8) +
                                    ((uint32_t)info->dev_class[2] << 16);
                            }
                            else if (eventType == EVT_INQUIRY_RESULT_WITH_RSSI)
                            {
                                inquiry_info_with_rssi* info = static_cast<inquiry_info_with_rssi*>(frm.ptr);

                                remoteDeviceAddress = convert_bdaddr_to_uint64(info->bdaddr);
                                remoteDeviceClass =
                                     (uint32_t)info->dev_class[0]        +
                                    ((uint32_t)info->dev_class[1] <<  8) +
                                    ((uint32_t)info->dev_class[2] << 16);
                                remoteDeviceRssi = info->rssi;
                            }
                            else if (eventType == EVT_EXTENDED_INQUIRY_RESULT)
                            {
                                extended_inquiry_info* info = static_cast<extended_inquiry_info*>(frm.ptr);

                                remoteDeviceAddress = convert_bdaddr_to_uint64(info->bdaddr);
                                remoteDeviceClass =
                                     (uint32_t)info->dev_class[0]        +
                                    ((uint32_t)info->dev_class[1] <<  8) +
                                    ((uint32_t)info->dev_class[2] << 16);
                                remoteDeviceRssi = info->rssi;
                            }
                            else
                            {
                                //do nothing
                            }

                            const TTime_t CURRENT_TIME_UTC(pt::microsec_clock::universal_time());
                            const TTimeDiff_t TIME_SINCE_ZERO_UTC = CURRENT_TIME_UTC - ZERO_TIME_UTC;

                            const TSteadyTimePoint CURRENT_TIME_STEADY(bc::steady_clock::now());
                            const TSteadyTimeDuration TIME_SINCE_ZERO_STEADY(CURRENT_TIME_STEADY - ZERO_TIME_STEADY);
                            const uint64_t TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY = bc::duration_cast<bc::seconds>(TIME_SINCE_ZERO_STEADY).count();

                            {
                                ::Lock lock(data.remoteDeviceCollectionMutex);

                                Model::DataContainer::TRemoteDeviceRecordCollection::iterator iter(
                                    data.pRemoteDeviceCollection->find(remoteDeviceAddress));

                                if (iter != data.pRemoteDeviceCollection->end())
                                {
                                    //Update existing record
                                    iter->second.lastObservationTimeUTC = TIME_SINCE_ZERO_UTC.total_seconds();
                                    iter->second.lastObservationTimeSteady = TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
                                    iter->second.presentInTheLastInquiry = true;
                                    ++iter->second.visibilityCounter;
                                }
                                else
                                {
                                    //Add as a new record
                                    (*data.pRemoteDeviceCollection)[remoteDeviceAddress] =
                                        Model::TRemoteDeviceRecord(
                                            remoteDeviceAddress,
                                            remoteDeviceClass);
                                }
                            }

                            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG1))
                            {
                                std::ostringstream ss;
                                ss << "Found Device: "
                                    << Utils::convertMACAddressToString(remoteDeviceAddress)
                                    << ", CoD=0x" << std::hex << remoteDeviceClass;
                                if (
                                    (eventType == EVT_INQUIRY_RESULT_WITH_RSSI) ||
                                    (eventType == EVT_EXTENDED_INQUIRY_RESULT)
                                    )
                                {
                                    ss << ", RSSI=" << std::dec << remoteDeviceRssi;
                                }
                                //else do nothing

                                Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
                            }
                            //else do nothing

                            frm.ptr = static_cast<uint8_t*>(frm.ptr) + INQUIRY_INFO_SIZE;
                            frm.len -= INQUIRY_INFO_SIZE;

                            //Add an entry to the raw RSSI result file
                            if (data.pOutputFile != 0)
                            {
                                *data.pOutputFile
                                    << TIME_SINCE_ZERO_UTC.total_milliseconds() << ","
                                    << std::hex
                                        << remoteDeviceAddress << ","
                                        << remoteDeviceClass << ","
                                    << std::dec << remoteDeviceRssi << "\n";
                            }
                            //else do nothing

                        } //for

                        break;
                    }

                    case EVT_CMD_STATUS:
                    {
                        evt_cmd_status* pEvt = reinterpret_cast<evt_cmd_status*>(frm.ptr);
                        uint16_t opcode = btohs(pEvt->opcode);
                        uint16_t ogf = cmd_opcode_ogf(opcode);
                        uint16_t ocf = cmd_opcode_ocf(opcode);

                        if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
                        {
                            std::ostringstream ss;
                            ss << opcode2str(opcode) <<std::hex
                                << " (0x" << ogf << "|0x" << ocf << ") "
                                   "status 0x" << static_cast<unsigned int>(pEvt->status)
                                << " ncmd " << std::dec << static_cast<unsigned int>(pEvt->ncmd);
                            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
                        }
                        //else do nothing

                        if (
                            (pEvt->status > 0) &&
                            (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
                            )
                        {
                            std::ostringstream ss;
                            ss << "Error: " << status2str(pEvt->status);
                            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
                        }
                        //else do nothing

                        break;
                    }

                    default:
                    {
                        break;
                    }
                } //switch

                break;
            }
            default:
            {
                //Ignore all other events
                break;
            }
        } //switch (type)
    } //while ! finish

    free(pBuffer);
    free(pCtrl);
    ::close(sock);

    data.result = true;
    ::pthread_exit(arg);
    return arg;
}


bool RawHCIDriver::scanForRemoteDevices(
    const Model::TLocalDeviceRecord& localDeviceRecord,
    const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
    Model::DataContainer::TRemoteDeviceRecordCollection* pRemoteDeviceCollection,
    ::Mutex& remoteDeviceCollectionMutex,
    ::TTime_t& inquiryStartTime)
{
    assert(pRemoteDeviceCollection != 0);

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_INFO))
    {
        std::ostringstream ss;
        ss << "Inquiring BlueTooth devices ... "
            "(duration=" << localDeviceConfiguration.inquiryDurationInSeconds << "s, "
            "using " << localDeviceRecord.name << ")";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
    }
    //else do nothing

    ::inquiry_cp cp;
    memset(&cp, 0, sizeof(cp));
    cp.lap[0] = 0x33;
    cp.lap[1] = 0x8B;
    cp.lap[2] = 0x9E;
    int numberOfScanCycles = (localDeviceConfiguration.inquiryDurationInSeconds * 100) >> 7; //equivalent to division by 1.28
    cp.length = numberOfScanCycles;
    cp.num_rsp = localDeviceConfiguration.deviceDiscoveryMaxDevices;

    std::ofstream* pOutputFile = 0;

#ifdef SAVE_RAW_RSSI_SCAN_RESULTS_TO_FILE
    std::ofstream outputFile;
    //Open output file in the user data directory
    std::string fileName;
    fileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getCacheDirectory());
    fileName += OUTPUT_FILE_NAME;
    outputFile.open(
        fileName.c_str(),
        std::ofstream::out | std::ofstream::app);

    if (outputFile.is_open())
    {
        pOutputFile = &outputFile
    }
    else
    {
        std::string logString("Could not open file \"");
        logString += OUTPUT_FILE_NAME;
        logString += "\"! Raw RSSI data will not be recorded!";
        Logger::log(LOG_LEVEL_ERROR, logString.c_str());
    }
#endif

    //Send inquiry in this thread but monitor the results in another thread (function monitorEvents(...))
    ::pthread_t threadId;
    TMonitorEventsData monitorEventsData = {
        localDeviceRecord.hciRoute,
        pRemoteDeviceCollection,
        remoteDeviceCollectionMutex,
        pOutputFile,
        false,
        false };
    if (::pthread_create(&threadId, NULL, &monitorEvents, &monitorEventsData) == 0)
    {
        const TTime_t INQUIRY_START_TIME(pt::second_clock::universal_time());

        //Send a HCI command
        if (::hci_send_cmd(localDeviceRecord.deviceDescriptor, OGF_LINK_CTL, OCF_INQUIRY, INQUIRY_CP_SIZE, &cp) >= 0)
        {
            inquiryStartTime = INQUIRY_START_TIME;

            //Now wait for the thread to complete. Normally this should result in a sequence of events ended with
            //event INQUIRY COMPLETE
            ::pthread_join(threadId, NULL);
        }
        else
        { //failure to send
            std::ostringstream ss;
            ss << "hci_send_cmd(): " << strerror(errno);
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

            ::hci_close_dev(localDeviceRecord.deviceDescriptor);

            monitorEventsData.finish = true;
            ::pthread_join(threadId, NULL);

            return false;
        }
    }
    else
    {
        Logger::log(LOG_LEVEL_ERROR, "Could not create a thread to send inquiry");

        //do not wait for thread finish
    }


    Logger::log(LOG_LEVEL_INFO, "Inquiring BlueTooth devices finished");

    if (pOutputFile != 0)
        pOutputFile->close();

    return monitorEventsData.result;
}


void RawHCIDriver::stopScanningForRemoteDevices(const Model::TLocalDeviceRecord& localDeviceRecord)
{
    if (::hci_send_cmd(localDeviceRecord.deviceDescriptor, OGF_LINK_CTL, OCF_INQUIRY_CANCEL, 0, 0) >= 0)
    {
        //do nothing
    }
    else
    { //failure to send
        std::ostringstream ss;
        ss << "hci_send_cmd(): " << strerror(errno);
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }
}


void RawHCIDriver::getLocalDeviceCollection(Model::DataContainer::TLocalDeviceRecordCollection* pResult)
{
    assert(pResult != 0);

    ::hci_for_each_dev(HCI_UP, addDeviceInfo, (long)pResult);
	if (pResult->empty())
	{
		Logger::log(LOG_LEVEL_ERROR, "No local BlueTooth radio has been found");
	}
	else
    {
        if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG1))
        {
            for(Model::DataContainer::TLocalDeviceRecordCollection::const_iterator
                    iter(pResult->begin()), iterEnd(pResult->end());
                iter != iterEnd;
                ++iter)
            {
                std::ostringstream ss;
                ss << "Local radio: " << iter->second->name
                    << " (" << Utils::convertMACAddressToString(iter->second->address) << ")";
                Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
            }
        }
        //else do nothing
    }
}

static int addDeviceInfo(int dd, int dev_id, long arg)
{
	struct ::hci_dev_info device_info;
    memset((void*)&device_info, 0, sizeof(device_info));
    device_info.dev_id = dev_id;

	if (::ioctl(dd, HCIGETDEVINFO, (void *) &device_info))
		return 0;

    Model::TLocalDeviceRecord_shared_ptr pLocalDeviceRecord(new Model::TLocalDeviceRecord());
    pLocalDeviceRecord->address = convert_bdaddr_to_uint64(device_info.bdaddr);
    pLocalDeviceRecord->deviceClass = 0;
    pLocalDeviceRecord->name = device_info.name;

    Model::DataContainer::TLocalDeviceRecordCollection* pLocalDeviceCollection =
        (Model::DataContainer::TLocalDeviceRecordCollection*)arg;
    if (pLocalDeviceCollection != 0)
    {
        pLocalDeviceCollection->operator[](pLocalDeviceRecord->address) = pLocalDeviceRecord;
    }
    //else do nothing

	return 0;
}


bool RawHCIDriver::setupLocalDevice(
    const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
    Model::TLocalDeviceRecord* pLocalDeviceRecord)
{
    assert(pLocalDeviceRecord != 0);

    //First get route to device
    ::bdaddr_t localDeviceAddress = convert_uint64_to_bdaddr(pLocalDeviceRecord->address);
	pLocalDeviceRecord->hciRoute = ::hci_get_route(&localDeviceAddress);
    if (pLocalDeviceRecord->hciRoute == -1)
    {
        std::ostringstream ss;
        ss << "Local Radio " << Utils::convertMACAddressToString(pLocalDeviceRecord->address)
            << " has not been found";
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

        ss.str("");
        ss << "hci_get_route(): Error accessing bluetooth radio (" << strerror(errno) << ")";
        Logger::log(LOG_LEVEL_DEBUG3, ss.str().c_str());

        return false;
    }
    //else do nothing

    //Try to open the specified device
    pLocalDeviceRecord->deviceDescriptor = ::hci_open_dev(pLocalDeviceRecord->hciRoute);
    if (pLocalDeviceRecord->deviceDescriptor >= 0)
	{
        //Check HCI version and print it
        ::hci_version ver;
        if (::hci_read_local_version(pLocalDeviceRecord->deviceDescriptor, &ver, 0) >=0)
        {
            char* pHciVersionStr = hci_vertostr(ver.hci_ver);
            std::ostringstream ss;
            ss << "HCI version: " << pHciVersionStr;
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            free(pHciVersionStr);
        }
        else
        {
            std::ostringstream ss;
            ss << "hci_read_local_version(): Error (" << strerror(errno) << ")";
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
        }

        ::read_local_commands_rp supportedCommands; //See Bluetooth specification, section 6.26 Supported Commands
        if (::hci_read_local_commands(pLocalDeviceRecord->deviceDescriptor, (uint8_t*)&supportedCommands.commands, 0) >= 0)
        {
            std::ostringstream ss;
            ss << "The following commands are supported by this device ([octet] value):\n";
            for (size_t i=0; i<sizeof(supportedCommands.commands); ++i)
            {
                if (supportedCommands.commands[i] != 0)
                {
                    ss << "[" << std::dec << i << "] " << std::hex << static_cast<unsigned int>(supportedCommands.commands[i]) << "\n";
                }
                //else do nothing
            }
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
        }
        else
        {
            std::ostringstream ss;
            ss << "hci_read_local_commands(): Error (" << strerror(errno) << ")";
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
        }

        uint8_t inquiryMode = 0;

//#define UNSET_RSSI_MODE
#ifdef UNSET_RSSI_MODE
        //First check if the Write Inquiry Mode Command is supported
        if ((supportedCommands.commands[12] & 0x80) != 0) //Octet 12, bit 7 (see Bluetooth specification 6.26 Supported Commands, p410
        { //... is supported!

            //First display current inquiry mode
            if (::hci_read_inquiry_mode(pLocalDeviceRecord->deviceDescriptor, &inquiryMode, 0) >= 0)
            {
                std::ostringstream ss;
                ss << "Current Inquiry Mode: " << static_cast<int>(inquiryMode);
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            }
            else
            {
                std::ostringstream ss;
                ss << "hci_read_inquiry_mode(): Error (" << strerror(errno) << ")";
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            }

            //Change the value of inquiry mode
            //See Bluetooth specification, section 7.3.50 Write Inquiry Mode Command
            uint8_t requestedInquiryMode = 0; //set to standard inquiry result event format
            if (::hci_write_inquiry_mode(pLocalDeviceRecord->deviceDescriptor, requestedInquiryMode, 0) < 0)
            {
                std::ostringstream ss;
                ss << "hci_write_inquiry_mode(): Error (" << strerror(errno) << ")";
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            }
            //else do nothing
        }
        //else do nothing
#endif

        inquiryMode = 0;
        if (::hci_read_inquiry_mode(pLocalDeviceRecord->deviceDescriptor, &inquiryMode, 0) >= 0)
        {
            std::ostringstream ss;
            ss << "Current Inquiry Mode: " << static_cast<int>(inquiryMode);
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
        }
        else
        {
            std::ostringstream ss;
            ss << "hci_read_inquiry_mode(): Error (" << strerror(errno) << ")";
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
        }

        //Write Inquiry Transmit Power Level Command p.745 of v2.1 bluetooth specification (sec 7.3.62)
        //First check if the Write Inquiry Mode Command is supported
        if ((supportedCommands.commands[18] & (1 << 1)) != 0) //Octet 18, bit 1 (see Bluetooth specification v2.1 6.26 Supported Commands, p410
        { //... is supported!
            //Change the value of inquiry transmit power
            int8_t requestedInquiryTransmitPowerLevel = localDeviceConfiguration.inquiryPower;
            if (::hci_write_inquiry_transmit_power_level(
                pLocalDeviceRecord->deviceDescriptor, requestedInquiryTransmitPowerLevel, 0) < 0)
            {
                std::ostringstream ss;
                ss << "hci_write_inquiry_transmit_power_level(): Error (" << strerror(errno) << ")";
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            }
            else
            {
                std::ostringstream ss;
                ss << "Inquiry Transmit Power Level set to " << static_cast<int>(requestedInquiryTransmitPowerLevel);
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            }
        }
        //else do nothing
    }
	else
	{
        std::ostringstream ss;
        ss << "hci_open_dev(): Error accessing bluetooth radio (" << strerror(errno) << ")";
        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
        return false;
    }


    return true;
}

void RawHCIDriver::closeLocalDevice(Model::TLocalDeviceRecord* pLocalDeviceRecord)
{
    ::hci_close_dev(pLocalDeviceRecord->deviceDescriptor);
    pLocalDeviceRecord->partialReset();
}

};
