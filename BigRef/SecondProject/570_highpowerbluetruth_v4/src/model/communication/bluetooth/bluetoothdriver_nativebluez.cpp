#include "bluetoothdriver_nativebluez.h"

#include "lock.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"

#include <errno.h>
#include <sstream>
#include <sys/ioctl.h>

#include "bluetooth/lib/bluetooth.h"
#include "bluetooth/lib/hci.h"
#include "bluetooth/lib/hci_lib.h"
#include "bluetooth/tools/parser/parser.h"


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

static int addDeviceInfo(int s, int dev_id, long arg)
{
	struct ::hci_dev_info device_info;
        device_info.dev_id = dev_id;

	if (::ioctl(s, HCIGETDEVINFO, (void *) &device_info))
		return 0;

    Model::TLocalDeviceRecord_shared_ptr pLocalDeviceRecord(new Model::TLocalDeviceRecord());
    pLocalDeviceRecord->address = convert_bdaddr_to_uint64(device_info.bdaddr);
    pLocalDeviceRecord->deviceClass = 0;
    pLocalDeviceRecord->name = device_info.name;
    pLocalDeviceRecord->hciRoute = ::hci_get_route(&device_info.bdaddr);

    Model::DataContainer::TLocalDeviceRecordCollection* pLocalDeviceCollection =
        (Model::DataContainer::TLocalDeviceRecordCollection*)arg;
    if (pLocalDeviceCollection != 0)
    {
        pLocalDeviceCollection->operator[](pLocalDeviceRecord->address) = pLocalDeviceRecord;
    }
    //else do nothing

	return 0;
}


namespace BlueTooth
{

bool NativeBluezDriver::scanForRemoteDevices(
    const Model::TLocalDeviceRecord& localDeviceRecord,
    const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
    Model::DataContainer::TRemoteDeviceRecordCollection* pRemoteDeviceCollection,
    ::Mutex& remoteDeviceCollectionMutex,
    ::TTime_t& inquiryStartTime)
{
    assert(pRemoteDeviceCollection != 0);

    bool result = false;

    {
        std::ostringstream ss;
        ss << "Inquiring BlueTooth devices ... "
            "(duration=" << localDeviceConfiguration.inquiryDurationInSeconds << "s, "
            "using " << localDeviceRecord.name << ")";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
    }

    int deviceDescriptor = ::hci_open_dev( localDeviceRecord.hciRoute );
    if (deviceDescriptor >= 0)
	{
        ::inquiry_info *pInquiryInfo = (::inquiry_info*)malloc(
            localDeviceConfiguration.deviceDiscoveryMaxDevices * sizeof(::inquiry_info));

        //int numberOfScanCycles = (inquiryDurationInSeconds / 1.28); //scan length in 1.28*len seconds
        int numberOfScanCycles = (localDeviceConfiguration.inquiryDurationInSeconds * 100) >> 7; //equivalent to division by 1.28
        int flags = IREQ_CACHE_FLUSH;

        //Inquiry start
        const TTime_t INQUIRY_START_TIME_UTC(pt::second_clock::universal_time());
        const TTimeDiff_t INQUIRY_START_TIME_SINCE_ZERO_UTC(INQUIRY_START_TIME_UTC - ZERO_TIME_UTC);

        const TSteadyTimePoint INQUIRY_START_TIME_STEADY(bc::steady_clock::now());
        const TSteadyTimeDuration INQUIRY_START_TIME_SINCE_ZERO_STEADY(INQUIRY_START_TIME_STEADY - ZERO_TIME_STEADY);
        const uint64_t INQUIRY_START_TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY = bc::duration_cast<bc::seconds>(INQUIRY_START_TIME_SINCE_ZERO_STEADY).count();

        int num_rsp = ::hci_inquiry(
            localDeviceRecord.hciRoute,
            numberOfScanCycles,
            static_cast<int>(localDeviceConfiguration.deviceDiscoveryMaxDevices),
            NULL,
            &pInquiryInfo,
            flags);

        const TTime_t INQUIRY_END_TIME_UTC(pt::second_clock::universal_time());
        const TTimeDiff_t INQUIRY_END_TIME_SINCE_ZERO_UTC(INQUIRY_END_TIME_UTC - ZERO_TIME_UTC);

        const TSteadyTimePoint INQUIRY_END_TIME_STEADY(bc::steady_clock::now());
        const TSteadyTimeDuration INQUIRY_END_TIME_SINCE_ZERO_STEADY(INQUIRY_END_TIME_STEADY - ZERO_TIME_STEADY);
        //Inquiry end

        if (num_rsp > 0)
        {
            inquiryStartTime = INQUIRY_START_TIME_UTC;
            //char remoteDeviceName[248];

            ::Lock lock(remoteDeviceCollectionMutex);

            for (int i=0; i<num_rsp; i++)
            {
                ::inquiry_info* pRemoteDeviceInfo = pInquiryInfo+i;
                //Do not ask about device name
                //memset(remoteDeviceName, 0, sizeof(remoteDeviceName));
                //::hci_read_remote_name(
                //    deviceDescriptor,
                //    &pRemoteDeviceInfo->bdaddr,
                //    sizeof(remoteDeviceName),
                //    remoteDeviceName,
                //    0); //assume that if getting the name failed remoteDeviceName is empty

                uint64_t remoteDeviceAddress = convert_bdaddr_to_uint64(pRemoteDeviceInfo->bdaddr);
                //Add as a new record
                uint32_t remoteDeviceClass =
                     (uint32_t)pRemoteDeviceInfo->dev_class[0]        +
                    ((uint32_t)pRemoteDeviceInfo->dev_class[1] <<  8) +
                    ((uint32_t)pRemoteDeviceInfo->dev_class[2] << 16);

                Model::DataContainer::TRemoteDeviceRecordCollection::iterator iter(
                    pRemoteDeviceCollection->find(remoteDeviceAddress));

                if (iter != pRemoteDeviceCollection->end())
                {
                    //Update existing record
                    iter->second.lastObservationTimeUTC = INQUIRY_START_TIME_SINCE_ZERO_UTC.total_seconds();
                    iter->second.lastObservationTimeSteady = INQUIRY_START_TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
                    iter->second.presentInTheLastInquiry = true;
                    ++iter->second.visibilityCounter;
                }
                else
                {
                    Model::TRemoteDeviceRecord record(remoteDeviceAddress, remoteDeviceClass);
                    //record.name = remoteDeviceName;
                    record.firstObservationTimeUTC = INQUIRY_START_TIME_SINCE_ZERO_UTC.total_seconds();
                    record.firstObservationTimeSteady = INQUIRY_START_TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
                    record.lastObservationTimeUTC = INQUIRY_END_TIME_SINCE_ZERO_UTC.total_seconds();
                    record.lastObservationTimeSteady = INQUIRY_START_TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
                    record.presentInTheLastInquiry = true;
                    record.visibilityCounter = 1;

                    (*pRemoteDeviceCollection)[remoteDeviceAddress] = record;
                }

                std::ostringstream ss;
                ss << "Found Device: "
                    << Utils::convertMACAddressToString(remoteDeviceAddress)
                    << ", CoD=0x" << std::hex << remoteDeviceClass;
                Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
            }

            result = true;
        }
        else if (num_rsp == 0)
        {
            inquiryStartTime = INQUIRY_START_TIME_UTC;
            Logger::log(LOG_LEVEL_DEBUG1, "No remote BlueTooth radio has been found");
        }
        else
        {
            std::ostringstream ss;
            ss << "Error inquiring bluetooth devices (errno=" << errno << ")"; //ENODEV=9967
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
            result = false;
        }

        free(pInquiryInfo);
        ::hci_close_dev(deviceDescriptor);
	}
	else
	{
        Logger::log(LOG_LEVEL_ERROR, "hci_open_dev(): Error accessing bluetooth radio");
        result = false;
    }

    Logger::log(LOG_LEVEL_INFO, "Inquiring BlueTooth devices finished");

    return result;
}

void NativeBluezDriver::stopScanningForRemoteDevices(const Model::TLocalDeviceRecord& )
{
}

void NativeBluezDriver::getLocalDeviceCollection(Model::DataContainer::TLocalDeviceRecordCollection* pResult)
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
            for (Model::DataContainer::TLocalDeviceRecordCollection::const_iterator
                    iter(pResult->begin()), iterEnd(pResult->end());
                iter != iterEnd;
                ++iter)
            {
                std::ostringstream ss;
                ss << "Local radio: " << iter->second->name << " (" << Utils::convertMACAddressToString(iter->second->address) << ")";
                Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
            }
        }
    }
}

bool NativeBluezDriver::setupLocalDevice(
    const Model::TLocalDeviceConfiguration& ,
    Model::TLocalDeviceRecord* )
{
    return true;
}

void NativeBluezDriver::closeLocalDevice(Model::TLocalDeviceRecord* )
{
    //do nothing
}

};
