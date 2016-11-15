#include "stdafx.h"
#include "bluetoothdriver_windowsbluetooth.h"

#include "lock.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"

#include <winsock2.h>
#include <ws2bth.h>
#include <BluetoothAPIs.h>
#pragma comment(lib, "ws2_32.lib")


namespace BlueTooth
{

bool WindowsBluetoothDriver::scanForRemoteDevices(
    const Model::TLocalDeviceRecord& localDeviceRecord,
    const Model::LocalDeviceConfiguration& localDeviceConfiguration,
    Model::DataContainer::TRemoteDeviceRecordCollection* pRemoteDeviceCollection,
    ::Mutex& remoteDeviceCollectionMutex)
{
    bool result = true;

    {
        std::ostringstream ss;
        ss << "Inquiring BlueTooth devices ... "
            "(duration=" << localDeviceConfiguration.inquiryDurationInSeconds << "s, "
            "using " << localDeviceRecord.name << ")";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
    }

    unsigned int _inquiryDurationInSeconds = localDeviceConfiguration.inquiryDurationInSeconds;
    if (_inquiryDurationInSeconds > std::numeric_limits<UCHAR>::max())
    {
        _inquiryDurationInSeconds = std::numeric_limits<UCHAR>::max();
    }
    //else do nothing


    //For details how to get local radio info see http://msdn.microsoft.com/en-us/library/windows/desktop/aa362797%28v=vs.85%29.aspx
    HANDLE hRadio = NULL;
    BLUETOOTH_FIND_RADIO_PARAMS btFindRadio = {sizeof(BLUETOOTH_FIND_RADIO_PARAMS)};
    HBLUETOOTH_RADIO_FIND hFoundRadio = ::BluetoothFindFirstRadio(&btFindRadio, &hRadio);
    if (hFoundRadio == 0)
    {
        DWORD lastError = ::GetLastError();
        switch (lastError)
        {
            case ERROR_NO_MORE_ITEMS:
            {
                Logger::log(LOG_LEVEL_ERROR, "listBluetoothDevices(): No local BlueTooth radio has been found");
                break;
            }

            default:
            {
                std::ostringstream ss;
                ss << "listBluetoothDevices(): BluetoothFindFirstRadio failed with error code " << lastError;
                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                break;
            }
        }

        return false;
    }
    //else do nothing


    const unsigned char cTimeoutMultiplier = static_cast<unsigned char>(
        (_inquiryDurationInSeconds * 100) >> 7); //equivalent to division by 1.28
    BLUETOOTH_DEVICE_SEARCH_PARAMS searchParams = {
        sizeof(BLUETOOTH_DEVICE_SEARCH_PARAMS),
        TRUE, //fReturnAuthenticated, a value that specifies that the search should return authenticated Bluetooth devices.
        TRUE, //fReturnRemembered, a value that specifies that the search should return remembered Bluetooth devices.
        TRUE, //fReturnUnknown, a value that specifies that the search should return unknown Bluetooth devices.
        TRUE, //fReturnConnected, a value that specifies that the search should return connected Bluetooth devices.
        TRUE, //fIssueInquiry, a value that specifies that a new inquiry should be issued.
        cTimeoutMultiplier, //cTimeoutMultiplier, a value that indicates the time out for the inquiry,
            //expressed in increments of 1.28 seconds. For example, an inquiry of
            //12.8 seconds has a cTimeoutMultiplier value of 10. The maximum value
            //for this member is 48. When a value greater than 48 is used, the calling
            //function immediately fails and returns E_INVALIDARG.
        hRadio, //hRadio, a handle for the radio on which to perform the inquiry. Set to NULL to perform the inquiry on all local Bluetooth radios.
    };
    BLUETOOTH_DEVICE_INFO deviceInfo;
    ::ZeroMemory(&deviceInfo, sizeof(BLUETOOTH_DEVICE_INFO));
    deviceInfo.dwSize = sizeof(BLUETOOTH_DEVICE_INFO);

    //Inquiry start
    static const TTime_t ZERO_TIME(pt::time_from_string("1970-01-01 00:00:00.000"));
    const TTime_t INQUIRY_START_TIME(pt::second_clock::universal_time());
    const TTimeDiff_t INQUIRY_START_TIME_SINCE_ZERO = INQUIRY_START_TIME - ZERO_TIME;

    HBLUETOOTH_DEVICE_FIND hFoundDevice = ::BluetoothFindFirstDevice(&searchParams, &deviceInfo);

    const TTime_t INQUIRY_END_TIME(pt::second_clock::universal_time());
    const TTimeDiff_t INQUIRY_END_TIME_SINCE_ZERO = INQUIRY_END_TIME - ZERO_TIME;
    //Inquiry end

    if (hFoundDevice == 0)
    {
        DWORD lastError = ::GetLastError();
        switch (lastError)
        {
            case ERROR_NO_MORE_ITEMS:
            {
                Logger::log(LOG_LEVEL_DEBUG1, "No remote BlueTooth radio has been found");
                break;
            }

            default:
            {
                std::ostringstream ss;
                ss << "listBluetoothDevices(): BluetoothFindFirstDevice failed with error code " << lastError;
                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

                result = false;
                break;
            }
        }

        if ((hFoundRadio != 0) && (::BluetoothFindRadioClose(hFoundRadio) != TRUE))
        {
            std::ostringstream ss;
            ss << "listBluetoothDevices(): BluetoothFindRadioClose failed with error code " << GetLastError();
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
        //else continue
        hFoundRadio = 0;
    }
    //else continue

    do
    {
        if (hFoundDevice != 0)
        {
            const uint64_t remoteDeviceAddress = deviceInfo.Address.ullLong;

            Model::DataContainer::TRemoteDeviceRecordCollection::iterator iter(
                pRemoteDeviceCollection->find(remoteDeviceAddress));

            if (iter != pRemoteDeviceCollection->end())
            {
                //Update existing record
                iter->second.lastObservationTime = INQUIRY_START_TIME_SINCE_ZERO.total_seconds();
                iter->second.presentInTheLastInquiry = true;
                ++iter->second.visibilityCounter;
            }
            else
            {
                Model::TRemoteDeviceRecord record;
                record.address = remoteDeviceAddress;
                record.deviceClass = (uint32_t)deviceInfo.ulClassofDevice;
                record.name = OS_Utilities::StringToAnsi(deviceInfo.szName);
                record.firstObservationTime = INQUIRY_START_TIME_SINCE_ZERO.total_seconds();
                record.lastObservationTime = INQUIRY_END_TIME_SINCE_ZERO.total_seconds();
                record.visibilityCounter = 1;

                (*pRemoteDeviceCollection)[remoteDeviceAddress] = record;
            }

            std::ostringstream ss;
            ss << "Found Device: " << OS_Utilities::StringToAnsi(deviceInfo.szName)
               << " (" << Utils::convertMACAddressToString(deviceInfo.Address.ullLong) << ")";
            Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
        }
        else
        {
            break;
        }

        ::ZeroMemory(&deviceInfo, sizeof(BLUETOOTH_DEVICE_INFO));
        deviceInfo.dwSize = sizeof(BLUETOOTH_DEVICE_INFO);
    }
    while (::BluetoothFindNextDevice(hFoundDevice, &deviceInfo));

    if ((hFoundDevice != 0) && (::BluetoothFindDeviceClose(hFoundDevice) != TRUE))
    {
        std::ostringstream ss;
        ss << "listBluetoothDevices(): BluetoothFindDeviceClose failed with error code " << GetLastError();
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }
    //else continue
    hFoundDevice = 0;

    if ((hFoundRadio != 0) && (::BluetoothFindRadioClose(hFoundRadio) != TRUE))
    {
        std::ostringstream ss;
        ss << "listBluetoothDevices(): BluetoothFindRadioClose failed with error code " << GetLastError();
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }
    //else continue
    hFoundRadio = 0;


    Logger::log(LOG_LEVEL_INFO, "Inquiring BlueTooth devices finished");

    return result;
}

void WindowsBluetoothDriver::stopScanningForRemoteDevices(const Model::TLocalDeviceRecord& )
{
}

void WindowsBluetoothDriver::getLocalDeviceCollection(Model::DataContainer::TLocalDeviceRecordCollection* pResult)
{
    HANDLE hRadio = NULL;
    BLUETOOTH_FIND_RADIO_PARAMS btFindRadio = {sizeof(BLUETOOTH_FIND_RADIO_PARAMS)};
    HBLUETOOTH_RADIO_FIND hFoundRadio = ::BluetoothFindFirstRadio(&btFindRadio, &hRadio);
    if (hFoundRadio == 0)
    {
        DWORD lastError = ::GetLastError();
        switch (lastError)
        {
            case ERROR_NO_MORE_ITEMS:
            {
                Logger::log(LOG_LEVEL_ERROR, "No local BlueTooth radio has been found");
                break;
            }

            default:
            {
                std::ostringstream ss;
                ss << "BluetoothFindFirstRadio failed with error code " << lastError;
                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                break;
            }
        }

        return;
    }
    //else do nothing

    do
    {
        BLUETOOTH_RADIO_INFO radioInfo = {sizeof(BLUETOOTH_RADIO_INFO),0,};
        DWORD radioInfoResult = ::BluetoothGetRadioInfo(
            hRadio,
            &radioInfo);
        if (radioInfoResult != ERROR_SUCCESS)
        {
            std::ostringstream ss;
            ss << "BluetoothGetRadioInfo failed with error code " << radioInfoResult;
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

            if ((hFoundRadio != 0) && (::BluetoothFindRadioClose(hFoundRadio) != TRUE))
            {
                std::ostringstream ss;
                ss << "BluetoothFindRadioClose failed with error code " << GetLastError();
                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
            }
            //else continue
            hFoundRadio = 0;

            return;
        }
        //else continue

        Model::TLocalDeviceRecord localDeviceRecord;
        localDeviceRecord.address = radioInfo.address.ullLong;
        localDeviceRecord.name = OS_Utilities::StringToAnsi(radioInfo.szName);
        localDeviceRecord.deviceClass = radioInfo.ulClassofDevice;
        localDeviceRecord.manufacturerID = radioInfo.manufacturer;
        localDeviceRecord.impSubversion = radioInfo.lmpSubversion;
        localDeviceRecord.hciRoute = 0;
        localDeviceRecord.deviceDescriptor = 0;

        {
            std::ostringstream ss;
            ss << "Local radio: (" << Utils::convertMACAddressToString(localDeviceRecord.address) << ")";
            Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
        }

        (*pResult)[localDeviceRecord.address] = localDeviceRecord;
    }
    while (::BluetoothFindNextRadio(hFoundRadio, &hRadio));

    int lastError = ::GetLastError();
    if (lastError != ERROR_NO_MORE_ITEMS)
    {
        std::ostringstream ss;
        ss << "getLocalDeviceCollection(): BluetoothFindNextRadio failed with error code " << GetLastError();
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }
    //else do nothing

    if ((hFoundRadio != 0) && (::BluetoothFindRadioClose(hFoundRadio) != TRUE))
    {
        std::ostringstream ss;
        ss << "getLocalDeviceCollection(): BluetoothFindRadioClose failed with error code " << GetLastError();
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }
    //else continue
    hFoundRadio = 0;
}

bool WindowsBluetoothDriver::setupLocalDevice(
    const Model::LocalDeviceConfiguration& ,
    Model::TLocalDeviceRecord* )
{
    return true;
}

void WindowsBluetoothDriver::closeLocalDevice(Model::TLocalDeviceRecord* pLocalDeviceRecord)
{
    pLocalDeviceRecord->deviceDescriptor = -1;
}

};
