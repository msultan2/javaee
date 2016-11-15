#include "bluetoothdriver_parani.h"

#include "lock.h"
#include "logger.h"
#include "paranioutputparser.h"
#include "utils.h"

#include <errno.h>
#include <fcntl.h>
#include <sstream>
#include <string.h>
#include <termios.h>
#include <unistd.h>


namespace
{
    const char COMMAND_CHECK_CONNECTION[] = "AT";
    const char COMMAND_SOFTWARE_RESET[] = "ATZ";
    const char COMMAND_HARDWARE_RESET[] = "AT&F";
    const char COMMAND_CHECK_BAUD_RATE_SET_BY_DIP_SWITCH[] = "AT+USEDIP?";
    const char COMMAND_DISPLAY_BLUETOOTH_SETTINGS[] = "AT+BTINFO?";
    const char COMMAND_DISPLAY_FIRMWARE_VERSION[] = "AT+BTVER?";
    const char COMMAND_INQUIRE[] = "AT+BTINQ?";
    const char COMMAND_CANCEL[] = "AT+BTCANCEL";
    const char COMMAND_DISPLAY_ALL_REGISTERS[] = "AT&V";

    const char CRLF[] = "\x0d\x0a";

    const unsigned int DEFAULT_COMMS_RESPONSE_TIMEOUT_IN_MILLISECONDS = 100;
}

static int set_interface_attribs (int fd, int speed)
{
    struct ::termios tty;
    memset(&tty, 0, sizeof(tty));

    if (::tcgetattr (fd, &tty) != 0)
    {
        std::ostringstream ss;
        ss << "tcgetattr(): " << strerror(errno);
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

        return -1;
    }
    //else continue

    ::cfsetospeed (&tty, speed);
    ::cfsetispeed (&tty, speed);

    /*
        BAUDRATE: Set bps rate. You could also use cfsetispeed and cfsetospeed.
        CRTSCTS : output hardware flow control (only used if the cable has
                all necessary lines. See sect. 7 of Serial-HOWTO)
        CS8     : 8n1 (8bit,no parity,1 stopbit)
        CLOCAL  : local connection, no modem contol
        CREAD   : enable receiving characters
    */
    tty.c_cflag = speed | CRTSCTS | CS8 | CLOCAL | CREAD;

    /*
        ICANON  : enable canonical input
        disable all echo functionality, and don't send signals to calling program
    */
    tty.c_lflag = ICANON;

    /*
        IGNPAR  : ignore bytes with parity errors
        ICRNL   : map CR to NL (otherwise a CR input on the other computer
                will not terminate input)
        otherwise make device raw (no other input processing)
    */
     tty.c_iflag = IGNPAR | ICRNL;

    // Chose raw (not processed) output
    tty.c_oflag = 0;


    tty.c_cc[VMIN]  = 1; //should_block
    tty.c_cc[VTIME] = 5; // 0.5 seconds read timeout


    ::tcflush(fd, TCIFLUSH);
    if (::tcsetattr(fd, TCSANOW, &tty) != 0)
    {
        std::ostringstream ss;
        ss << "tcsetattr(): " << strerror(errno);
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

        return -1;
    }
    //else do nothing

    return 0;
}


/**
 * @brief send a command over serial port
 *
 * @param portName Name of the port to be used, e.g. /dev/ttyUSB0. The user running this function must have
 * the access right to write to this port (e.g. be in the dialup group)
 * @param bitRate The current bit rate to be used when communicating with the device e.g. 9600b/s
 * @param command AT command to be sent to the device
 * @param commandSize The length of the command to be sent to the device
 * @param timeoutInMilliSeconds The time to wait after the AT command has been sent. If the bit rate of the device
 * and this host do not match a timeout should occur
 * @param deviceResponse A response from the device to the command
 */
static int send_command_over_serial_port(
    const char* portName,
    const unsigned int bitRate,
    const char* command,
    const size_t commandSize,
    const unsigned int timeoutInMilliSeconds,
    std::string& deviceResponse)
{
    int fd = ::open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK);
    if (fd < 0)
    {
        std::ostringstream ss;
        ss << "Error to open port \"" << portName << "\" (" << strerror(errno) << ")";
        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

        return -1;
    }

    switch (bitRate)
    {
        case 19200:
        {
            set_interface_attribs (fd, B19200);  // set speed to 19,200 bps, 8n1 (no parity)
            break;
        }
        case 38400:
        {
            set_interface_attribs (fd, B38400);  // set speed to 38,400 bps, 8n1 (no parity)
            break;
        }
        case 57600:
        {
            set_interface_attribs (fd, B57600);  // set speed to 57,600 bps, 8n1 (no parity)
            break;
        }
        case 115200:
        {
            set_interface_attribs (fd, B115200);  // set speed to 115,200 bps, 8n1 (no parity)
            break;
        }
        default:
        case 9600:
        {
            set_interface_attribs (fd, B9600);  // set speed to 9,600 bps, 8n1 (no parity)
            break;
        }
    }
    ::fcntl(fd, F_SETFL, O_NONBLOCK);

    //Check the connection status with host equipment
    int n = -1;

    //Print the command
    Logger::log(LOG_LEVEL_DEBUG2, command);

    n = ::write(fd, command, commandSize);
    if (n < 0)
    {
        std::ostringstream ss;
        ss << "write(): " << strerror(errno);
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }
    //else do nothing

    ::write(fd, &CRLF[0], sizeof(CRLF) - 1);
    ::tcflush(fd, TCIFLUSH);

    char buffer[100] = {0};
    n = 0;
    bool finish = false;
    int returnValue = 0;
    while (!finish) //wait for response
    {
        ::fd_set set;
        FD_ZERO(&set); /* clear the set */
        FD_SET(fd, &set); /* add our file descriptor to the set */
        struct ::timeval timeout;
        timeout.tv_sec = timeoutInMilliSeconds/1000;
        timeout.tv_usec = 1000*(timeoutInMilliSeconds%1000);
        int rv = ::select(fd + 1, &set, NULL, NULL, &timeout);
        if (rv == -1) //error
        {
            std::ostringstream ss;
            ss << "Error occurred when waiting for response on port \"" << portName << "\"";
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

            returnValue = -1;
            break;
        }
        else if (rv == 0) //timeout
        {
            std::ostringstream ss;
            ss << "Timeout occurred when waiting for response on port \"" << portName << "\", bit rate=" << bitRate;
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

            returnValue = -2;
            break;
        }
        else
        {
            //do nothing
        }

        n = ::read(fd, buffer, sizeof(buffer));  // read up to 100 characters if ready to read
        if (n > 0)
        {
            for (int i=0; i<n; ++i)
            {
                deviceResponse += buffer[i];
            }
        }
        //else do nothing

        //Check if the response was not the string "OK"
        size_t responseSize = deviceResponse.size();
        if (responseSize >=4)
        {
            char c4 = deviceResponse[responseSize - 4];
            char c3 = deviceResponse[responseSize - 3];
            char c2 = deviceResponse[responseSize - 2];
            char c1 = deviceResponse[responseSize - 1];
            if (
                (c4 == 'O') &&
                (c3 == 'K') &&
                (c2 == 0xa) &&
                (c1 == 0xa)
                )
            {
                finish = true;
                break;
            }
        }

        //Check if the response was not the string "ERROR"
        if (responseSize >=7)
        {
            char c7 = deviceResponse[responseSize - 7];
            char c6 = deviceResponse[responseSize - 6];
            char c5 = deviceResponse[responseSize - 5];
            char c4 = deviceResponse[responseSize - 4];
            char c3 = deviceResponse[responseSize - 3];
            char c2 = deviceResponse[responseSize - 2];
            char c1 = deviceResponse[responseSize - 1];
            if (
                (c7 == 'E') &&
                (c6 == 'R') &&
                (c5 == 'R') &&
                (c4 == 'O') &&
                (c3 == 'R') &&
                (c2 == 0xa) &&
                (c1 == 0xa)
                )
            {
                finish = true;
                returnValue = 1;
                break;
            }
        }
        else
        {
            //do nothing
        }

    } //while(!finish)

    if (!deviceResponse.empty())
    {
        std::size_t firstNotEOL = deviceResponse.find_first_not_of("\n");
        if (firstNotEOL != std::string::npos)
        {
            std::ostringstream ss;
            ss << deviceResponse.substr(firstNotEOL, std::string::npos);
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
        }
        //else do nothing
    }
    //else do nothing

    ::close(fd);
    return returnValue;
}


/**
 * @brief Change bit rate of the Parani device
 *
 * @param portName Name of the port to be used, e.g. /dev/ttyUSB0. The user running this function must have
 * the access right to write to this port (e.g. be in the dialup group)
 * @param currentBitRate The current bit rate to be used when communicating with the device e.g. 9600b/s
 * @param newBitRate The new bit rate to be setup
 */
static int changeBitRate(const char* portName, const unsigned int currentBitRate, const unsigned int newBitRate)
{
    int result = 0;

    {
        std::ostringstream ss;
        ss << "Changing bit rate for local radio: on port " << portName
            << " from " << currentBitRate << " to " << newBitRate;
        Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
    }

    {
        //Change the bit rate
        std::string deviceResponse;
        std::ostringstream ss;
        ss << "AT+UARTCONFIG," << newBitRate << ",N,1";
        int sendResult = send_command_over_serial_port(
            portName,
            currentBitRate,
            ss.str().c_str(),
            ss.str().size(),
            500,
            deviceResponse);
        if (sendResult == 0)
        {
            TParaniOutputContext parsingResult;
            bool parserOk = ParaniOutputParser::parse(deviceResponse, parsingResult);
            if (parserOk && parsingResult.paraniOutput.result)
            {
                //do nothing
            }
            else
            {
                Logger::log(LOG_LEVEL_DEBUG2, "Invalid parsing result");
            }
        }
        else
        {
            std::ostringstream errorString;
            errorString << "Could not set serial parameters on port \"" << portName
                << "\" to " << newBitRate << "N1";
            Logger::log(LOG_LEVEL_ERROR, errorString.str().c_str());
        }
    }

    {
        Logger::log(LOG_LEVEL_DEBUG2, "Performing software reset of Parani adevice. Expecting a timeout after ATZ command...");
        //Perform software reset. Ignore the result because of speed change (should be garbage or nothing)
        std::string deviceResponse;
        send_command_over_serial_port(
            portName,
            currentBitRate,
            &COMMAND_SOFTWARE_RESET[0],
            sizeof(COMMAND_SOFTWARE_RESET) -1,
            2000, //wait a bit longer
            deviceResponse);
    }

    return result;
}


namespace BlueTooth
{

unsigned int ParaniDriver::m_lastInquiryDurationInSeconds = 0;
unsigned int ParaniDriver::m_lastDeviceDiscoveryMaxDevices = 0;
unsigned int ParaniDriver::m_currentBitRate = 0;

bool ParaniDriver::scanForRemoteDevices(
    const Model::TLocalDeviceRecord& localDeviceRecord,
    const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
    Model::DataContainer::TRemoteDeviceRecordCollection* pRemoteDeviceCollection,
    ::Mutex& remoteDeviceCollectionMutex,
    ::TTime_t& inquiryStartTime)
{
    assert(pRemoteDeviceCollection != 0);

    if (localDeviceConfiguration.inquiryDurationInSeconds != m_lastInquiryDurationInSeconds)
    {
        //Setup inquiry timeout
        Logger::log(LOG_LEVEL_DEBUG2, "Setting up inquiry time");
        std::string deviceResponse;

        std::ostringstream ss;
        ss << "ATS33=" << localDeviceConfiguration.inquiryDurationInSeconds;
        int sendResult = send_command_over_serial_port(
            localDeviceConfiguration.paraniPortName.c_str(),
            m_currentBitRate,
            ss.str().c_str(),
            ss.str().size(),
            DEFAULT_COMMS_RESPONSE_TIMEOUT_IN_MILLISECONDS,
            deviceResponse);
        if (sendResult == 0)
        {
            TParaniOutputContext parsingResult;
            bool parserOk = ParaniOutputParser::parse(deviceResponse, parsingResult);
            if (parserOk && parsingResult.paraniOutput.result)
            {
                m_lastInquiryDurationInSeconds = localDeviceConfiguration.inquiryDurationInSeconds;
            }
            else
            {
                Logger::log(LOG_LEVEL_DEBUG2, "Invalid parsing result");
            }
        }
        else
        {
            Logger::log(LOG_LEVEL_ERROR, "Could not find any Parani Bluetooth USB Adapter");
            return false;
        }
    }

    if (
        (localDeviceConfiguration.deviceDiscoveryMaxDevices != m_lastDeviceDiscoveryMaxDevices) &&
        (localDeviceConfiguration.deviceDiscoveryMaxDevices <= 15)
        )
    {
        //Set maximum number of inquiry results
        Logger::log(LOG_LEVEL_DEBUG2, "Changing maximum number of inquiry results");
        std::string deviceResponse;

        std::ostringstream ss;
        ss << "ATS24=" << localDeviceConfiguration.deviceDiscoveryMaxDevices;
        int sendResult = send_command_over_serial_port(
            localDeviceConfiguration.paraniPortName.c_str(),
            m_currentBitRate,
            ss.str().c_str(),
            ss.str().size(),
            DEFAULT_COMMS_RESPONSE_TIMEOUT_IN_MILLISECONDS,
            deviceResponse);
        if (sendResult == 0)
        {
            TParaniOutputContext parsingResult;
            bool parserOk = ParaniOutputParser::parse(deviceResponse, parsingResult);
            if (parserOk && parsingResult.paraniOutput.result)
            {
                m_lastDeviceDiscoveryMaxDevices = localDeviceConfiguration.deviceDiscoveryMaxDevices;
            }
            else
            {
                Logger::log(LOG_LEVEL_DEBUG2, "Invalid parsing result");
            }
        }
        else
        {
            Logger::log(LOG_LEVEL_ERROR, "Could not find any Parani Bluetooth USB Adapter");
            return false;
        }
    }

    {
        {
            std::ostringstream ss;
            ss << "Inquiring BlueTooth devices ... "
                "(duration=" << localDeviceConfiguration.inquiryDurationInSeconds << "s, "
                "using " << localDeviceRecord.name << ")";
            Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
        }

        //Inquire nearby devices
        std::string deviceResponse;

        //Inquiry start
        const TTime_t INQUIRY_START_TIME_UTC(pt::second_clock::universal_time());
        const TTimeDiff_t INQUIRY_START_TIME_SINCE_ZERO_UTC(INQUIRY_START_TIME_UTC - ZERO_TIME_UTC);

        const TSteadyTimePoint INQUIRY_START_TIME_STEADY(bc::steady_clock::now());
        const TSteadyTimeDuration INQUIRY_START_TIME_SINCE_ZERO_STEADY(INQUIRY_START_TIME_STEADY - ZERO_TIME_STEADY);
        const uint64_t INQUIRY_START_TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY = bc::duration_cast<bc::seconds>(INQUIRY_START_TIME_SINCE_ZERO_STEADY).count();


        int sendResult = send_command_over_serial_port(
            localDeviceConfiguration.paraniPortName.c_str(),
            m_currentBitRate,
            &COMMAND_INQUIRE[0],
            sizeof(COMMAND_INQUIRE) -1,
            (localDeviceConfiguration.inquiryDurationInSeconds + 1)*1000,
            deviceResponse);

        const TTime_t INQUIRY_END_TIME_UTC(pt::second_clock::universal_time());
        const TTimeDiff_t INQUIRY_END_TIME_SINCE_ZERO_UTC(INQUIRY_END_TIME_UTC - ZERO_TIME_UTC);

        const TSteadyTimePoint INQUIRY_END_TIME_STEADY(bc::steady_clock::now());
        const TSteadyTimeDuration INQUIRY_END_TIME_SINCE_ZERO_STEADY(INQUIRY_END_TIME_STEADY - ZERO_TIME_STEADY);
        const uint64_t INQUIRY_END_TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY = bc::duration_cast<bc::seconds>(INQUIRY_END_TIME_SINCE_ZERO_STEADY).count();
        //Inquiry end

        if (sendResult == 0)
        {
            TParaniOutputContext parsingResult;
            bool parserOk = ParaniOutputParser::parse(deviceResponse, parsingResult);
            if (parserOk && parsingResult.paraniOutput.result)
            {
                ::Lock lock(remoteDeviceCollectionMutex);
                uint64_t remoteDeviceAddress = 0;
                uint32_t remoteDeviceClass = 0;
                inquiryStartTime = INQUIRY_START_TIME_UTC;

                for(TParaniOutput::TParaniOutputInquiryResultEntryCollection::const_iterator
                        iter(parsingResult.paraniOutput.inquiryResult.begin()),
                        iterEnd(parsingResult.paraniOutput.inquiryResult.end());
                        iter != iterEnd;
                        ++iter
                    )
                {
                    //Convert bdaddress to uint64_t number
                    bool addressConversionOk = Utils::stringToUInt64(
                        iter->second.address,
                        remoteDeviceAddress,
                        Utils::HEX);
                    if (addressConversionOk)
                    { //Convert class of device to uint32_t number
                        bool codConversionOk = Utils::stringToUInt(
                            iter->second.deviceClass,
                            remoteDeviceClass,
                            Utils::HEX);
                        if (!codConversionOk)
                        {
                            std::ostringstream ss;
                            ss << "Invalid conversion of CoD (" << iter->second.deviceClass << ") read from the PARANI device";
                            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                        }
                        //else do nothing. Record this entry anyway

                        Model::DataContainer::TRemoteDeviceRecordCollection::iterator iter(
                            pRemoteDeviceCollection->find(remoteDeviceAddress));

                        if (iter != pRemoteDeviceCollection->end())
                        {
                            //Update existing record
                            iter->second.lastObservationTimeUTC = INQUIRY_END_TIME_SINCE_ZERO_UTC.total_seconds();
                            iter->second.lastObservationTimeSteady = INQUIRY_END_TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
                            iter->second.presentInTheLastInquiry = true;
                            ++iter->second.visibilityCounter;
                        }
                        else
                        {
                            Model::TRemoteDeviceRecord record;
                            record.address = remoteDeviceAddress;
                            record.deviceClass = remoteDeviceClass;
                            //record.name = remoteDeviceName;
                            record.firstObservationTimeUTC = INQUIRY_START_TIME_SINCE_ZERO_UTC.total_seconds();
                            record.firstObservationTimeSteady = INQUIRY_START_TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
                            record.lastObservationTimeUTC = INQUIRY_END_TIME_SINCE_ZERO_UTC.total_seconds();
                            record.lastObservationTimeSteady = INQUIRY_END_TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
                            record.presentInTheLastInquiry = true;
                            record.visibilityCounter = 1;

                            (*pRemoteDeviceCollection)[remoteDeviceAddress] = record;
                        }
                    }
                    else
                    {
                        std::ostringstream ss;
                        ss << "Invalid conversion of BD_ADDRESS (" << iter->second.address << ") read from the PARANI device";
                        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                    }
                    //else do nothing

                    {
                        std::ostringstream ss;
                        ss << "Found Device: "
                            << Utils::convertMACAddressToString(remoteDeviceAddress)
                            << ", CoD=0x" << std::hex << remoteDeviceClass;
                        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
                    }
                } //for
            }
            else
            {
                Logger::log(LOG_LEVEL_DEBUG2, "Invalid parsing result");
            }
        }
        else
        {
            Logger::log(LOG_LEVEL_ERROR, "Could not find any Parani Bluetooth USB Adapter");
            return false;
        }

        Logger::log(LOG_LEVEL_INFO, "Inquiring BlueTooth devices finished");
    }

    return true;
}

void ParaniDriver::stopScanningForRemoteDevices(const Model::TLocalDeviceRecord& )
{
    //Sending of cancel command does not work and for that reason has been removed
}

void ParaniDriver::getLocalDeviceCollection(
    const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
    Model::DataContainer::TLocalDeviceRecordCollection* pResult)
{
    assert(pResult != 0);

    Logger::log(LOG_LEVEL_INFO, "Scanning for Parani device using various bit rates...");

    std::vector<unsigned int> bitRates;
    bitRates.push_back(9600);
    bitRates.push_back(19200);
    bitRates.push_back(38400);
    bitRates.push_back(57600);
    bitRates.push_back(115200);

    bool found = false;

    for (std::vector<unsigned int>::const_iterator iter(bitRates.begin()), iterEnd(bitRates.end());
        iter != iterEnd;
        ++iter)
    {
        //Issue 2 commands but ignore the result of the first. This is because if the previous
        //transmission occurred on invalid bit rate some garbage bits reside in Parani device buffers
        //and the command will very likely be responded with ERROR string.
        std::string deviceResponse;
        int sendResult = send_command_over_serial_port( //the message to be ignored
            localDeviceConfiguration.paraniPortName.c_str(),
            *iter,
            &COMMAND_CHECK_CONNECTION[0],
            sizeof(COMMAND_CHECK_CONNECTION) -1,
            DEFAULT_COMMS_RESPONSE_TIMEOUT_IN_MILLISECONDS,
            deviceResponse);
        deviceResponse.clear();

        sendResult = send_command_over_serial_port( //the intended message
            localDeviceConfiguration.paraniPortName.c_str(),
            *iter,
            &COMMAND_DISPLAY_BLUETOOTH_SETTINGS[0],
            sizeof(COMMAND_DISPLAY_BLUETOOTH_SETTINGS) -1,
            500,
            deviceResponse);
        if (sendResult >= 0)
        {
            TParaniOutputContext parsingResult;
            bool parserOk = ParaniOutputParser::parse(deviceResponse, parsingResult);
            if (parserOk && parsingResult.paraniOutput.result)
            {
                Model::TLocalDeviceRecord_shared_ptr record(new Model::TLocalDeviceRecord());
                bool conversionOk = Utils::stringToUInt64(
                    parsingResult.paraniOutput.bluetoothSettings.bdaddress,
                    record->address,
                    Utils::HEX);
                if (!conversionOk)
                {
                    Logger::log(LOG_LEVEL_ERROR, "Invalid conversion of BD_ADDRESS read from the PARANI device");
                }
                //else do nothing

                record->name = parsingResult.paraniOutput.bluetoothSettings.deviceName;
                record->hciRoute = 0;

                (*pResult)[record->address] = record;

                found = true;

                std::ostringstream ss;
                ss << "Local radio: " << record->name << " (" << Utils::convertMACAddressToString(record->address) << "),"
                    " on port " << localDeviceConfiguration.paraniPortName << "/" << *iter << " found!";
                Logger::log(LOG_LEVEL_INFO, ss.str().c_str());


                m_currentBitRate = *iter;

                break;
            }
        }
    }

    if (!found)
    {
        Logger::log(LOG_LEVEL_ERROR, "Could not find any Parani Bluetooth USB Adapter");
    }
    //else do nothing
}

bool ParaniDriver::setupLocalDevice(
    const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
    Model::TLocalDeviceRecord* )
{
    //Change bit rate
    if (m_currentBitRate != localDeviceConfiguration.paraniBitRate)
    {
        bool dipSwitchSetToSWConfig = false;

        //Check the baud rate set by the dip switch
        std::string deviceResponse;
        int sendResult = send_command_over_serial_port(
            localDeviceConfiguration.paraniPortName.c_str(),
            m_currentBitRate,
            &COMMAND_CHECK_BAUD_RATE_SET_BY_DIP_SWITCH[0],
            sizeof(COMMAND_CHECK_BAUD_RATE_SET_BY_DIP_SWITCH) -1,
            DEFAULT_COMMS_RESPONSE_TIMEOUT_IN_MILLISECONDS,
            deviceResponse);
        if (sendResult == 0)
        {
            TParaniOutputContext parsingResult;
            bool parserOk = ParaniOutputParser::parse(deviceResponse, parsingResult);
            if (parserOk && parsingResult.paraniOutput.result)
            {
                TParaniOutput::TRegisterEntryCollection::const_iterator iter(
                    parsingResult.paraniOutput.registerEntries.find("DIP"));
                if (iter != parsingResult.paraniOutput.registerEntries.end())
                {
                    const std::string dipSwitchValueAsString(iter->second.valueString);
                    int number = 1;
                    Utils::stringToInt(dipSwitchValueAsString, number);

                    //In the case of error assume that we cannot change the bit rate
                    if (number == 0) //=dips switches are set to 'S/'W Config'
                    {
                        dipSwitchSetToSWConfig = true;
                    }
                    //else do nothing
                }
            }
            else
            {
                Logger::log(LOG_LEVEL_DEBUG2, "Invalid parsing result");
            }
        }
        else
        {
            Logger::log(LOG_LEVEL_ERROR, "Could not find any Parani Bluetooth USB Adapter");
            return false;
        }

        if (dipSwitchSetToSWConfig)
        {
            if (changeBitRate(
                localDeviceConfiguration.paraniPortName.c_str(),
                m_currentBitRate,
                localDeviceConfiguration.paraniBitRate) == 0)
            {
                m_currentBitRate = localDeviceConfiguration.paraniBitRate;
            }
            else
            {
                return false;
            }
        }
        else
        {
            Logger::log(LOG_LEVEL_WARNING, "The bit rate cannot be changed. "
                "Check if the DIP switch is in \'S/W Config\' position. Using current speed...");
        }
    }
    //else do nothing

    {
        Logger::log(LOG_LEVEL_DEBUG2, "Disabling remote name query");
        //Disable remote name query
        std::string deviceResponse;
        char COMMAND_SET_REGISTER_VALUE[] = "ATS4=0";
        int sendResult = send_command_over_serial_port(
            localDeviceConfiguration.paraniPortName.c_str(),
            m_currentBitRate,
            &COMMAND_SET_REGISTER_VALUE[0],
            sizeof(COMMAND_SET_REGISTER_VALUE) -1,
            DEFAULT_COMMS_RESPONSE_TIMEOUT_IN_MILLISECONDS,
            deviceResponse);
        if (sendResult == 0)
        {
            TParaniOutputContext parsingResult;
            bool parserOk = ParaniOutputParser::parse(deviceResponse, parsingResult);
            if (parserOk && parsingResult.paraniOutput.result)
            {
                //do nothing
            }
            else
            {
                Logger::log(LOG_LEVEL_DEBUG2, "Invalid parsing result");
            }
        }
        else
        {
            Logger::log(LOG_LEVEL_ERROR, "Could not find any Parani Bluetooth USB Adapter");
            return false;
        }
    }


    m_lastInquiryDurationInSeconds = 0;
    m_lastDeviceDiscoveryMaxDevices = 0;

    return true;
}

void ParaniDriver::closeLocalDevice(Model::TLocalDeviceRecord* pLocalDeviceRecord)
{
    m_currentBitRate = 0;
    pLocalDeviceRecord->deviceDescriptor = -1;
}

};
