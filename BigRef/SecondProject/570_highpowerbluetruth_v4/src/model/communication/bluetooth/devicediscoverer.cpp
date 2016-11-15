#include "stdafx.h"
#include "devicediscoverer.h"

#include "applicationconfiguration.h"
#include "datacontainer.h"
#include "bluetoothdriver_nativebluez.h"
#include "bluetoothdriver_parani.h"
#include "bluetoothdriver_rawhci.h"
#ifdef _WIN32
#include "bluetoothdriver_windowswsa.h"
#include "bluetoothdriver_windowsbluetooth.h"
#endif
#include "instation/instationreporter.h"
#include "lock.h"
#include "logger.h"
#include "os_utilities.h"
#include "queuedetector.h"
#include "utils.h"

#include <boost/date_time/posix_time/posix_time.hpp>

#ifdef TESTING
#include "bluetooth/test_bluetoothdriver_rawhci.h"
#endif

namespace
{
    //std::string fileName;
    //fileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getCacheDirectory());
    //fileName += "raw_scan_results.csv";
}

using Model::DataContainer;
using Model::LocalDeviceConfiguration;
using Model::LocalDeviceRecord;
using Model::TLocalDeviceRecord_shared_ptr;

namespace BlueTooth
{

DeviceDiscoverer::DeviceDiscoverer(
    boost::shared_ptr<Model::DataContainer>& pDataContainer,
    const bool isInLegacyMode,
    const char* rawScanResultsFileName)
:
::IObservable(),
m_pDataContainer(pDataContainer),
M_IS_IN_LEGACY_MODE(isInLegacyMode),
m_pQueueDetector(),
m_pInStationReporter(),
m_mutex(),
m_rawScanResultsFileName(rawScanResultsFileName ? rawScanResultsFileName : ""),
m_rawScanResultsOutputFile(),
m_lastInquiryDurationInSeconds(0),
m_lastDeviceDriver(Model::eDEVICE_DRIVER_UNDEFINED)
{
    openRecordFile();
}

DeviceDiscoverer::~DeviceDiscoverer()
{
    closeRecordFile();
}

void DeviceDiscoverer::setup(
    boost::shared_ptr<QueueDetection::QueueDetector> pQueueDetector,
    boost::shared_ptr<InStation::IInStationReporter> pInStationReporter)
{
    ::Lock lock(m_mutex);
    m_pQueueDetector = pQueueDetector;
    m_pInStationReporter = pInStationReporter;
}

bool DeviceDiscoverer::openRecordFile()
{
    bool result = false;

    if (m_rawScanResultsFileName.empty())
        return false;

    //Open output file in the user data directory
    m_rawScanResultsOutputFile.open(
        m_rawScanResultsFileName.c_str(),
        std::ofstream::out | std::ofstream::app);

    if (m_rawScanResultsOutputFile.is_open())
    {
        TTime_t currentTime(pt::second_clock::local_time());
        m_rawScanResultsOutputFile
            << "#Starting new session (" << currentTime << ")" << std::endl;
        result = true;
    }
    else
    {
        std::string logString;
        logString.reserve(255);
        logString += "Could not open file \"";
        logString += m_rawScanResultsFileName;
        logString += "\"! Raw data will not be recorded!";
        Logger::log(LOG_LEVEL_ERROR, logString.c_str());
    }

    return result;
}

void DeviceDiscoverer::writeDiscoveredDevicesToRecordFile(const bool lastScanWasOk)
{
    //Log all spotted devices to a "raw scan results" file
    if (m_rawScanResultsOutputFile.is_open())
    {
        const LocalDeviceConfiguration& localDeviceConfiguration = m_pDataContainer->getLocalDeviceConfiguration();

        Mutex& remoteDeviceCollectionMutex = m_pDataContainer->getRemoteDeviceCollectionMutex();
        DataContainer::TRemoteDeviceRecordCollection& remoteDeviceCollection =
            m_pDataContainer->getRemoteDeviceCollection();

        // Print results to a file except for the case of Raw_HCI. In this case
        // The records are written by the class itself and contain additional information
        ::Lock lock2(remoteDeviceCollectionMutex);
        if (
#if defined __linux__
            (localDeviceConfiguration.deviceDriver != Model::eDEVICE_DRIVER_LINUX_RAW_HCI) &&
#if defined TESTING
            (localDeviceConfiguration.deviceDriver == Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI) &&
#endif
#endif
            true
            )
        {
            if (!remoteDeviceCollection.empty())
            {
                for (DataContainer::TRemoteDeviceRecordCollection::const_iterator
                        iter(remoteDeviceCollection.begin()),
                        iterEnd(remoteDeviceCollection.end());
                    iter != iterEnd;
                    ++iter)
                {
                    // Write result to a file
                    m_rawScanResultsOutputFile
                        << iter->second.firstObservationTimeUTC << ","
                        << iter->second.lastObservationTimeUTC << ","
                        << Utils::convertMACAddressToString(iter->second.address) << ","
                        << std::hex << iter->second.deviceClass << std::dec << ",";
                    if (!iter->second.name.empty())
                    {
                        m_rawScanResultsOutputFile << '\"' << iter->second.name << '\"';
                    }
                    //else do nothing
                    m_rawScanResultsOutputFile << "\n";
                }
            }
            else //i.e. remoteDeviceCollection is empty
            {
                //Record the time when inquiry has been completed
                const TTime_t TIME_AFTER_INQUIRY(pt::second_clock::universal_time());
                const TTimeDiff_t TIME_AFTER_INQUIRY_SINCE_ZERO = TIME_AFTER_INQUIRY - ZERO_TIME_UTC;
                const uint64_t TIME_AFTER_INQUIRY_SINCE_ZERO_TOTAL_SECONDS = TIME_AFTER_INQUIRY_SINCE_ZERO.total_seconds();

                if (lastScanWasOk)
                {
                    m_rawScanResultsOutputFile
                        << TIME_AFTER_INQUIRY_SINCE_ZERO_TOTAL_SECONDS - localDeviceConfiguration.inquiryDurationInSeconds << ","
                        << TIME_AFTER_INQUIRY_SINCE_ZERO_TOTAL_SECONDS << ","
                           ",,\n";
                }
                else
                { //error
                    m_rawScanResultsOutputFile
                        << TIME_AFTER_INQUIRY_SINCE_ZERO_TOTAL_SECONDS << ","
                        << TIME_AFTER_INQUIRY_SINCE_ZERO_TOTAL_SECONDS << ","
                           ",000000,\"Error\"\n";
                }
            }

            m_rawScanResultsOutputFile << std::flush;
        }
        else
        {
            //do nothing data will be stored in a different form and sent as STATISTICS REPORT
        }
    }
}

void DeviceDiscoverer::closeRecordFile()
{
    m_rawScanResultsOutputFile.close();
}


bool DeviceDiscoverer::inquireDevices()
{
    assert(m_pDataContainer != 0);

    ::Lock lock(m_mutex);

    TLocalDeviceRecord_shared_ptr pLocalDeviceRecord = m_pDataContainer->getLocalDeviceRecord();

    // Check if local bluetooth device was present. If not found find one and set it up.
    // If no device is found report an error and exit
    if (!pLocalDeviceRecord || pLocalDeviceRecord->isReset())
    {
        if (!checkForLocalRadio())
        {
            m_pDataContainer->resetRemoteDeviceRecords();

            updateBluetoothDeviceFault(true);
            return false;
        }
        //else do nothing
    }
    //else do nothing


    DataContainer::TRemoteDeviceRecordCollection& remoteDeviceCollection = m_pDataContainer->getRemoteDeviceCollection();
    ::Mutex& remoteDeviceCollectionMutex = m_pDataContainer->getRemoteDeviceCollectionMutex();

    bool ok = scanForRemoteDevices();

    notifyObservers(eDEVICE_INQUIRY_END);


#if !defined __linux__
    //TODO Check if in Windows this part is necessary
    {
        ::Lock lock2(remoteDeviceCollectionMutex);
        // Merge the results with the data in the DataContainer
        for (DataContainer::TRemoteDeviceRecordCollection::const_iterator
                iter(remoteDeviceCollection.begin()),
                iterEnd(remoteDeviceCollection.end());
            iter != iterEnd;
            ++iter)
        {
            // Append / Update data container
            m_pDataContainer->updateRemoteDeviceRecord(iter->second);
        }
    }
#endif

    verifyIfInquiryDurationChanged();
    verifyIfDriverChanged();
    writeDiscoveredDevicesToRecordFile(ok);

    updateBluetoothDeviceFault(!ok);


    m_pDataContainer->notifyObservers(DataContainer::eREMOTE_DEVICE_COLLECTION_HAS_BEEN_CHANGED);


    if (M_IS_IN_LEGACY_MODE && (m_pInStationReporter != 0))
    {
        //Update QueueDetector and some of the fields that are used to evaluate congestion
        {
            ::Lock lock(remoteDeviceCollectionMutex);
            for (DataContainer::TRemoteDeviceRecordCollection::iterator
                    iter(remoteDeviceCollection.begin()),
                    iterEnd(remoteDeviceCollection.end());
                iter != iterEnd;
                ++iter)
            {
                ++iter->second.numberOfScans;
                if (iter->second.presentInTheLastInquiry)
                {
                    ++iter->second.numberOfScansPresent;
                }
                else
                {
                    ++iter->second.numberOfScansAbsent;
                }
            }
        }

        m_pInStationReporter->sendRawDeviceDetection();
        m_pInStationReporter->sendCongestionReport();
    }
    //else do nothing. If queueDetector was deployed the queue will be processed
    //by a dedicated task (PeriodicallySendCongestionReportTask)

    return ok;
}

void DeviceDiscoverer::updateBluetoothDeviceFault(const bool faultPresent)
{
    if (faultPresent)
    {
        m_pDataContainer->getBluetoothDeviceFault().set();
    }
    else
    {
        m_pDataContainer->getBluetoothDeviceFault().clear();
    }


    TLocalDeviceRecord_shared_ptr pLocalDeviceRecord = m_pDataContainer->getLocalDeviceRecord();

    // Close local device if fault is present
    if (faultPresent && (pLocalDeviceRecord != 0))
    {
        switch (m_pDataContainer->getLocalDeviceConfiguration().deviceDriver)
        {
#if defined _WIN32
            case Model::eDEVICE_DRIVER_WINDOWS_WSA:
            {
                WindowsWSADriver::closeLocalDevice(pLocalDeviceRecord.get());
                break;
            }

            case Model::eDEVICE_DRIVER_WINDOWS_BLUETOOTH:
            default:
            {
                WindowsBluetoothDriver::closeLocalDevice(pLocalDeviceRecord.get());
                break;
            }

#elif defined __linux__
            case Model::eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ:
            {
                NativeBluezDriver::closeLocalDevice(pLocalDeviceRecord.get());
                break;
            }

            case Model::eDEVICE_DRIVER_LINUX_PARANI:
            {
                ParaniDriver::closeLocalDevice(pLocalDeviceRecord.get());
                break;
            }

            case Model::eDEVICE_DRIVER_LINUX_RAW_HCI:
            default:
            {
                RawHCIDriver::closeLocalDevice(pLocalDeviceRecord.get());
                break;
            }

#if defined TESTING
            case Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI:
            {
                TestRawHCIDriver::closeLocalDevice(pLocalDeviceRecord.get());
                break;
            }
#endif

#else
#error Operating System not supported
#endif
        } //switch
    }
    //else do nothing


    Model::Fault& bluetoothDeviceFault = m_pDataContainer->getBluetoothDeviceFault();
    if (!bluetoothDeviceFault.wasReported() && !bluetoothDeviceFault.isPending())
    {
        if (m_pInStationReporter != 0)
        {
            m_pInStationReporter->reportFault();
        }
        //else do nothing
    }
    //else do nothing

    if (
        bluetoothDeviceFault.isPending() ||
        bluetoothDeviceFault.wasReported()
        )
    {
        m_pDataContainer->notifyObservers(DataContainer::eLOCAL_DEVICE_HAS_BEEN_CHANGED);
    }
    //else do nothing
}


void DeviceDiscoverer::interruptInquireDevices()
{
    TLocalDeviceRecord_shared_ptr pLocalDeviceRecord = m_pDataContainer->getLocalDeviceRecord();
    if (!pLocalDeviceRecord)
        return;

    const LocalDeviceConfiguration& localDeviceConfiguration =
        m_pDataContainer->getLocalDeviceConfiguration();

    switch (localDeviceConfiguration.deviceDriver)
    {
#if defined _WIN32
        case Model::eDEVICE_DRIVER_WINDOWS_WSA:
        {
            WindowsWSADriver::stopScanningForRemoteDevices(*pLocalDeviceRecord);
            break;
        }

        case Model::eDEVICE_DRIVER_WINDOWS_BLUETOOTH:
        default:
        {
            WindowsBluetoothDriver::stopScanningForRemoteDevices(*pLocalDeviceRecord);
            break;
        }

#elif defined __linux__
        case Model::eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ:
        {
            NativeBluezDriver::stopScanningForRemoteDevices(*pLocalDeviceRecord);
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_PARANI:
        {
            ParaniDriver::stopScanningForRemoteDevices(*pLocalDeviceRecord);
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_RAW_HCI:
        default:
        {
            RawHCIDriver::stopScanningForRemoteDevices(*pLocalDeviceRecord);
            break;
        }

#if defined TESTING
        case Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI:
        {
            TestRawHCIDriver::stopScanningForRemoteDevices(*pLocalDeviceRecord);
            break;
        }
#endif

#else
#error Operating System not supported
#endif
    }

}

void DeviceDiscoverer::getLocalDeviceCollection(
    const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
    DataContainer::TLocalDeviceRecordCollection* pResult) const
{
    switch (localDeviceConfiguration.deviceDriver)
    {
#if defined _WIN32
        case Model::eDEVICE_DRIVER_WINDOWS_WSA:
        {
            WindowsWSADriver::getLocalDeviceCollection(pResult);
            break;
        }

        case Model::eDEVICE_DRIVER_WINDOWS_BLUETOOTH:
        default:
        {
            WindowsBluetoothDriver::getLocalDeviceCollection(pResult);
            break;
        }

#elif defined __linux__
        case Model::eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ:
        {
            NativeBluezDriver::getLocalDeviceCollection(pResult);
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_PARANI:
        {
            ParaniDriver::getLocalDeviceCollection(
                localDeviceConfiguration,
                pResult);
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_RAW_HCI:
        default:
        {
            RawHCIDriver::getLocalDeviceCollection(pResult);
            break;
        }

#if defined TESTING
        case Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI:
        {
            TestRawHCIDriver::getLocalDeviceCollection(pResult);
            break;
        }
#endif

#else
#error Operating System not supported
#endif
    }
}

bool DeviceDiscoverer::getLocalRadioInfo(const uint64_t address, TLocalDeviceRecord_shared_ptr& pLocalDeviceRecord) const
{
    bool result = false;

    const LocalDeviceConfiguration& localDeviceConfiguration = m_pDataContainer->getLocalDeviceConfiguration();
    DataContainer::TLocalDeviceRecordCollection localDeviceCollection;
    getLocalDeviceCollection(localDeviceConfiguration, &localDeviceCollection);

    if (!localDeviceCollection.empty())
    {
        if (
            (address == 0)
#if defined __linux__
            ||
            (localDeviceConfiguration.deviceDriver == Model::eDEVICE_DRIVER_LINUX_PARANI)
#endif
            )
        {
            //Take the first item
            pLocalDeviceRecord = localDeviceCollection.begin()->second;
            result = true;
        }
        else
        {
            //Find an item with the matching address
            DataContainer::TLocalDeviceRecordCollection::const_iterator
                iter(localDeviceCollection.find(address));

            if (iter != localDeviceCollection.end())
            {
                pLocalDeviceRecord = iter->second;
                result = true;
            }
            else
            {
                std::ostringstream ss;
                ss << "Local radio " << Utils::convertMACAddressToString(pLocalDeviceRecord->address) << " not found";
                Logger::log(LOG_LEVEL_WARNING, ss.str().c_str());
            }
        }
    }
    //else failure

    return result;
}

bool DeviceDiscoverer::setupLocalRadio(TLocalDeviceRecord_shared_ptr& pLocalDeviceRecord) const
{
    if (!pLocalDeviceRecord)
        return false;

    bool result = false;
    const LocalDeviceConfiguration& localDeviceConfiguration = m_pDataContainer->getLocalDeviceConfiguration();

    switch (localDeviceConfiguration.deviceDriver)
    {
#if defined _WIN32
        case Model::eDEVICE_DRIVER_WINDOWS_WSA:
        {
            result = WindowsWSADriver::setupLocalDevice(localDeviceConfiguration, pLocalDeviceRecord.get());
            break;
        }

        case Model::eDEVICE_DRIVER_WINDOWS_BLUETOOTH:
        default:
        {
            result = WindowsBluetoothDriver::setupLocalDevice(localDeviceConfiguration, pLocalDeviceRecord.get());
            break;
        }

#elif defined __linux__
        case Model::eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ:
        {
            result = NativeBluezDriver::setupLocalDevice(localDeviceConfiguration, pLocalDeviceRecord.get());
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_PARANI:
        {
            result = ParaniDriver::setupLocalDevice(localDeviceConfiguration, pLocalDeviceRecord.get());
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_RAW_HCI:
        default:
        {
            result = RawHCIDriver::setupLocalDevice(localDeviceConfiguration, pLocalDeviceRecord.get());
            break;
        }

#if defined TESTING
        case Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI:
        {
            result = TestRawHCIDriver::setupLocalDevice(localDeviceConfiguration, pLocalDeviceRecord.get());
            break;
        }
#endif

#else
#error Operating System not supported
#endif
    }

    return result;
}

bool DeviceDiscoverer::checkForLocalRadio()
{
    uint64_t address = 0;
    TLocalDeviceRecord_shared_ptr pLocalDeviceRecord = m_pDataContainer->getLocalDeviceRecord();

    if (pLocalDeviceRecord)
        address = pLocalDeviceRecord->address;

    bool ok = getLocalRadioInfo(address, pLocalDeviceRecord);
    if (ok)
    {
        ok = setupLocalRadio(pLocalDeviceRecord);
    }
    //else do nothing

    if (ok)
    {
        m_pDataContainer->setLocalDeviceRecord(pLocalDeviceRecord);
    }
    //else do nothing

    updateBluetoothDeviceFault(!ok);

    return ok;
}

bool DeviceDiscoverer::scanForRemoteDevices()
{
    bool ok = true;

    TLocalDeviceRecord_shared_ptr pLocalDeviceRecord = m_pDataContainer->getLocalDeviceRecord();
    if (!pLocalDeviceRecord || pLocalDeviceRecord->isReset())
        return false;

    const LocalDeviceConfiguration& localDeviceConfiguration = m_pDataContainer->getLocalDeviceConfiguration();

    DataContainer::TRemoteDeviceRecordCollection& remoteDeviceCollection =
        m_pDataContainer->getRemoteDeviceCollection();
    Mutex& remoteDeviceCollectionMutex = m_pDataContainer->getRemoteDeviceCollectionMutex();


    //Update view to start counting down the inquiry time
    notifyObservers(eDEVICE_INQUIRY_START);

    //Clear presentInTheLastInquiry flag
    {
        ::Lock lock(remoteDeviceCollectionMutex);
        for (DataContainer::TRemoteDeviceRecordCollection::iterator
                iter(remoteDeviceCollection.begin()),
                iterEnd(remoteDeviceCollection.end());
            iter != iterEnd;
            ++iter)
        {
            iter->second.presentInTheLastInquiry = false;
        }
    }


    //Start scanning
    ::TTime_t inquiryStartTime(pt::not_a_date_time);

    switch (localDeviceConfiguration.deviceDriver)
    {
#if defined _WIN32
        case Model::eDEVICE_DRIVER_WINDOWS_WSA:
        {
            ok = WindowsWSADriver::scanForRemoteDevices(
                *pLocalDeviceRecord,
                localDeviceConfiguration,
                &remoteDeviceCollection,
                remoteDeviceCollectionMutex);
            break;
        }

        case Model::eDEVICE_DRIVER_WINDOWS_BLUETOOTH:
        default:
        {
            ok = WindowsBluetoothDriver::scanForRemoteDevices(
                *pLocalDeviceRecord,
                localDeviceConfiguration,
                &remoteDeviceCollection,
                remoteDeviceCollectionMutex);
            break;
        }

#elif defined __linux__

        case Model::eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ:
        {
            ok = NativeBluezDriver::scanForRemoteDevices(
                *pLocalDeviceRecord,
                localDeviceConfiguration,
                &remoteDeviceCollection,
                remoteDeviceCollectionMutex,
                inquiryStartTime);
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_PARANI:
        {
            ok = ParaniDriver::scanForRemoteDevices(
                *pLocalDeviceRecord,
                localDeviceConfiguration,
                &remoteDeviceCollection,
                remoteDeviceCollectionMutex,
                inquiryStartTime);
            break;
        }

        case Model::eDEVICE_DRIVER_LINUX_RAW_HCI:
        default:
        {
            ok = RawHCIDriver::scanForRemoteDevices(
                *pLocalDeviceRecord,
                localDeviceConfiguration,
                &remoteDeviceCollection,
                remoteDeviceCollectionMutex,
                inquiryStartTime);
            break;
        }

#if defined TESTING
        case Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI:
        {
            if (M_IS_IN_LEGACY_MODE)
            {
                //Update raw remote device records
                m_pDataContainer->resetRemoteDeviceRecords();
            }
            //else do nothing

            ok = TestRawHCIDriver::scanForRemoteDevices(
                *pLocalDeviceRecord,
                localDeviceConfiguration,
                &remoteDeviceCollection,
                inquiryStartTime);
            break;
        }
#endif

#else
#error Operating System not supported
#endif
    }

    m_pDataContainer->setLastInquiryStartTime(inquiryStartTime);

    return ok;
}

void DeviceDiscoverer::verifyIfInquiryDurationChanged()
{
    const LocalDeviceConfiguration& localDeviceConfiguration = m_pDataContainer->getLocalDeviceConfiguration();

    //Report change of inquiry duration if applicable
    if (m_lastInquiryDurationInSeconds != localDeviceConfiguration.inquiryDurationInSeconds)
    {
        if (m_rawScanResultsOutputFile.is_open())
        {
            m_rawScanResultsOutputFile << "#Inquiry duration: " << localDeviceConfiguration.inquiryDurationInSeconds << " s\n";
        }
        //else do not write - file not open

        m_lastInquiryDurationInSeconds = localDeviceConfiguration.inquiryDurationInSeconds;
    }
    //else do nothing
}

void DeviceDiscoverer::verifyIfDriverChanged()
{
    const LocalDeviceConfiguration& localDeviceConfiguration = m_pDataContainer->getLocalDeviceConfiguration();

    //Report change of device driver if applicable
    if (m_lastDeviceDriver != localDeviceConfiguration.deviceDriver)
    {
        if (m_rawScanResultsOutputFile.is_open())
        {
            switch (localDeviceConfiguration.deviceDriver)
            {
#if defined _WIN32
                case Model::eDEVICE_DRIVER_WINDOWS_WSA:
                {
                    m_rawScanResultsOutputFile << "#Driver WindowsWSA\n";
                    break;
                }

                case Model::eDEVICE_DRIVER_WINDOWS_BLUETOOTH:
                default:
                {
                    m_rawScanResultsOutputFile << "#Driver WindowsBluetooth\n";
                    break;
                }

#elif defined __linux__

                case Model::eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ:
                {
                    m_rawScanResultsOutputFile << "#Driver NativeBluez\n";
                    break;
                }

                case Model::eDEVICE_DRIVER_LINUX_PARANI:
                {
                    m_rawScanResultsOutputFile << "#Driver Parani\n";
                    break;
                }

                case Model::eDEVICE_DRIVER_LINUX_RAW_HCI:
                default:
                {
                    m_rawScanResultsOutputFile << "#Driver RawHCI\n";
                    break;
                }

#if defined TESTING
                case Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI:
                {
                    m_rawScanResultsOutputFile << "#Driver Test_RawHCI\n";
                    break;
                }
#endif

#else
#error Operating System not supported
#endif
            } //switch
        }
        //else do not write - file not open

        m_lastDeviceDriver = localDeviceConfiguration.deviceDriver;
    }
    //else do nothing
}

::Mutex& DeviceDiscoverer::getMutex()
{
    return m_mutex;
}

bool DeviceDiscoverer::isRecordFileOpen() const
{
    return m_rawScanResultsOutputFile.is_open();
}


} //namespace
