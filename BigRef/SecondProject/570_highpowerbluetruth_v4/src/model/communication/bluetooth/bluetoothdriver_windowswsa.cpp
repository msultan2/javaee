#include "stdafx.h"
#include "bluetoothdriver_windowswsa.h"

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

bool WindowsWSADriver::scanForRemoteDevices(
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

    INT          iResult = 0;
    ULONG        ulFlags = 0;
    ULONG        ulPQSSize = sizeof(::WSAQUERYSET);
    HANDLE       hLookup = 0;
    ::PWSAQUERYSET pWSAQuerySet = NULL;
    pWSAQuerySet = (::PWSAQUERYSET) ::HeapAlloc(::GetProcessHeap(), HEAP_ZERO_MEMORY, ulPQSSize);
    if ( NULL == pWSAQuerySet )
    {
        result = false;
        Logger::log(LOG_LEVEL_ERROR, "Unable to allocate memory for WSAQUERYSET");

        return result;
    }
    //else do nothing

    // WSALookupService is used for both service search and device inquiry
    // LUP_CONTAINERS is the flag which signals that we're doing a device inquiry.
    ulFlags |= LUP_CONTAINERS;

    // Return the Bluetooth COD (class of device bits) directly in the lpServiceClassId
    // member of the WSAQUERYSET structure. The COD is mapped to the Data1 member of the GUID.
    ulFlags |= LUP_RETURN_TYPE;

    // Return information for the local Bluetooth address. This flag has an effect only
    // if LUP_RETURN_ADDR is also specified.
    //ulFlags |= LUP_RES_SERVICE;

    // Friendly device name (if available) will be returned in lpszServiceInstanceName
    //ulFlags |= LUP_RETURN_NAME;

    // BTH_ADDR will be returned in lpcsaBuffer member of WSAQUERYSET
    ulFlags |= LUP_RETURN_ADDR;

    // Flush the device cache for all inquiries.
    // By setting LUP_FLUSHCACHE flag, we're asking the lookup service to do
    // a fresh lookup instead of pulling the information from device cache.
    ulFlags |= LUP_FLUSHCACHE;

    // Start the lookup service
    //http://msdn.microsoft.com/en-us/library/windows/desktop/aa362913%28v=vs.85%29.aspx
    iResult = 0;
    hLookup = 0;
    BTH_QUERY_DEVICE queryParameters; //http://msdn.microsoft.com/en-us/library/windows/desktop/aa362937%28v=vs.85%29.aspx
    queryParameters.LAP = 0;
    queryParameters.length = static_cast<UCHAR>(_inquiryDurationInSeconds); //requested length of the inquiry, in seconds
    BLOB blob; //http://msdn.microsoft.com/en-us/library/windows/desktop/ms737551%28v=vs.85%29.aspx
    blob.cbSize = sizeof(::BTH_QUERY_DEVICE);
    blob.pBlobData = (BYTE*)&queryParameters;
    ::ZeroMemory(pWSAQuerySet, ulPQSSize);
    pWSAQuerySet->dwNameSpace = NS_BTH;
    pWSAQuerySet->dwSize = sizeof(::WSAQUERYSET);
    pWSAQuerySet->lpBlob = &blob;

    //Inquiry start
    static const TTime_t ZERO_TIME(pt::time_from_string("1970-01-01 00:00:00.000"));
    const TTime_t INQUIRY_START_TIME(pt::second_clock::universal_time());
    const TTimeDiff_t INQUIRY_START_TIME_SINCE_ZERO = INQUIRY_START_TIME - ZERO_TIME;

    iResult = ::WSALookupServiceBegin(pWSAQuerySet, ulFlags, &hLookup);

    const TTime_t INQUIRY_END_TIME(pt::second_clock::universal_time());
    const TTimeDiff_t INQUIRY_END_TIME_SINCE_ZERO = INQUIRY_END_TIME - ZERO_TIME;
    //Inquiry end

    if ( (iResult == NO_ERROR) && (hLookup != NULL) )
    {
        ::Lock lock(remoteDeviceCollectionMutex);

        // Get information about next bluetooth device
        while (true)
        {
            if ( NO_ERROR == ::WSALookupServiceNext(hLookup, ulFlags, &ulPQSSize, pWSAQuerySet) )
            {
                // Found a remote bluetooth device. Get the address of the device and exit the lookup.
                PSOCKADDR_BTH pRemoteBlueToothSocket = reinterpret_cast<PSOCKADDR_BTH>(pWSAQuerySet->lpcsaBuffer->RemoteAddr.lpSockaddr);
                if (pRemoteBlueToothSocket != 0)
                {
                    const uint64_t remoteDeviceAddress = pRemoteBlueToothSocket->btAddr;

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
                        record.deviceClass = (uint32_t)pWSAQuerySet->lpServiceClassId->Data1;
                        record.name = OS_Utilities::StringToAnsi(pWSAQuerySet->lpszServiceInstanceName);
                        record.firstObservationTime = INQUIRY_START_TIME_SINCE_ZERO.total_seconds();
                        record.lastObservationTime = INQUIRY_END_TIME_SINCE_ZERO.total_seconds();
                        record.visibilityCounter = 1;

                        (*pRemoteDeviceCollection)[remoteDeviceAddress] = record;
                    }

                    std::ostringstream ss;
                    ss << "Found Device: " << OS_Utilities::StringToAnsi(pWSAQuerySet->lpszServiceInstanceName)
                       << " (" << Utils::convertMACAddressToString(pRemoteBlueToothSocket->btAddr) << ")";
                    Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
                }
                //else do nothing
            }
            else
            {
                if (WSA_E_NO_MORE == (iResult = ::WSAGetLastError())) //No more data
                {
                    // No more devices found.  Exit the lookup.
                    break;
                }
                else if (iResult == WSAEFAULT)
                {
                    // The buffer for QUERYSET was insufficient.
                    // In such case 3rd parameter "ulPQSSize" of function "WSALookupServiceNext()" receives
                    // the required size.  So we can use this parameter to reallocate memory for QUERYSET.
                    ::HeapFree(::GetProcessHeap(), 0, pWSAQuerySet);
                    pWSAQuerySet = NULL;
                    if ( NULL == ( pWSAQuerySet = (::PWSAQUERYSET) ::HeapAlloc(::GetProcessHeap(), HEAP_ZERO_MEMORY, ulPQSSize) ) )
                    {
                        result = false;
                        Logger::log(LOG_LEVEL_ERROR, "Unable to allocate memory for WSAQERYSET");
                        break;
                    }
                    //else do what ???
                }
                else
                {
                    result = false;
                    std::ostringstream ss;
                    ss << "WSALookupServiceNext() failed with error code " << iResult;
                    Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                    break;
                }
            }
        } //while (true)

        // End the lookup service
        ::WSALookupServiceEnd(hLookup);
    }
    else
    {
        result = false;
        std::ostringstream ss;
        ss << "WSALookupServiceBegin() failed with error code " << iResult << ", WSALastError = " << ::WSAGetLastError();
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }

    if ( NULL != pWSAQuerySet )
    {
        ::HeapFree(::GetProcessHeap(), 0, pWSAQuerySet);
        pWSAQuerySet = NULL;
    }
    //else do nothing

    Logger::log(LOG_LEVEL_INFO, "Inquiring BlueTooth devices finished");

    return result;
}

void WindowsWSADriver::stopScanningForRemoteDevices(const Model::TLocalDeviceRecord& )
{
}

void WindowsWSADriver::getLocalDeviceCollection(Model::DataContainer::TLocalDeviceRecordCollection* pResult)
{
    WindowsBluetoothDriver::getLocalDeviceCollection(pResult);
}

bool WindowsWSADriver::setupLocalDevice(const Model::LocalDeviceConfiguration& ,
        Model::TLocalDeviceRecord* )
{
    return true;
}

void WindowsWSADriver::closeLocalDevice(Model::TLocalDeviceRecord* )
{
    //do nothing
}

};
