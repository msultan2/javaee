#include "stdafx.h"
#include "datacontainer.h"

#include "lock.h"
#include "logger.h"

#include <sstream>


namespace
{
    const uint64_t BLUETOOTH_RADIO_FAULT_TIME_TAG = 0xFFFFFFFF;
}


namespace Model
{

const char DataContainer::FAULT_STR_FUNCTIONAL_CONFIGURATION_SYNTAX[] = "054";
const char DataContainer::FAULT_STR_FUNCTIONAL_CONFIGURATION_PARAMETER_ERROR[] = "055";
const char DataContainer::FAULT_STR_NUMBER_RETRIEVE_CONFIGURATION_RESPONSE_NOT_OK[] = "058a";
const char DataContainer::FAULT_STR_NUMBER_CONGESTION_REPORTING_RESPONSE_NOT_OK[] = "058b";
const char DataContainer::FAULT_STR_NUMBER_RAW_DEVICE_DETECTION_REPORTING_RESPONSE_NOT_OK[] = "058c";
const char DataContainer::FAULT_STR_NUMBER_ALERT_AND_STATUS_REPORTING_RESPONSE_NOT_OK[] = "058d";
const char DataContainer::FAULT_STR_NUMBER_RETRIEVE_CONFIGURATION_COMMUNICATION[] = "059";
const char DataContainer::FAULT_STR_NUMBER_RAW_DEVICE_DETECTION_REPORTING_COMMUNICATION[] = "066";
const char DataContainer::FAULT_STR_NUMBER_BLUETOOTH_DEVICE[] = "082";


DataContainer::DataContainer()
:
InStation::IInStationDataContainer(),
::IObservable(),
m_remoteDeviceCollection(),
m_nonPendingRemoteDeviceCollection(),
m_remoteDeviceCollectionMutex(),
m_lastInquiryStartTime(pt::not_a_date_time),
m_pLocalDeviceRecord(),
m_localDeviceConfiguration(),
m_bluetoothDeviceFault(),
m_retrieveConfigurationClientCommunicationFault(),
m_retrieveConfigurationClientResponseNotOkFault(),
m_retrieveConfigurationClientResponseMessageBodyErrorFault(),
m_congestionReportingClientCommunicationFault(),
m_congestionReportingClientResponseNotOkFault(),
m_congestionReportingClientResponseMessageBodyErrorFault(),
m_rawDeviceDetectionClientCommunicationFault(),
m_rawDeviceDetectionClientResponseNotOkFault(),
m_rawDeviceDetectionClientResponseMessageBodyErrorFault(),
m_alertAndStatusReportingClientCommunicationFault(),
m_alertAndStatusReportingClientResponseNotOkFault(),
m_alertAndStatusReportingClientResponseMessageBodyErrorFault(),
m_statusReportingClientCommunicationFault(),
m_statusReportingClientResponseNotOkFault(),
m_statusReportingClientResponseMessageBodyErrorFault(),
m_faultReportingClientCommunicationFault(),
m_faultReportingClientResponseNotOkFault(),
m_faultReportingClientResponseMessageBodyErrorFault(),
m_statisticsReportingClientCommunicationFault(),
m_statisticsReportingClientResponseNotOkFault(),
m_statisticsReportingClientResponseMessageBodyErrorFault(),
m_functionalConfigurationSyntaxFault(),
m_functionalConfigurationParameterValueFault(),
m_seedFileFault(),
m_inStationSSHUnableToConnectFault(),
m_GSMModemUnableToConnectFault()
{
    //do nothing
}

DataContainer::~DataContainer()
{
    //do nothing
}

void DataContainer::setLastInquiryStartTime(const ::TTime_t& startTime)
{
    m_lastInquiryStartTime = startTime;
}

const ::TTime_t& DataContainer::getLastInquiryStartTime() const
{
    return m_lastInquiryStartTime;
}

void DataContainer::updateRemoteDeviceRecord(const TRemoteDeviceRecord& remoteDeviceRecord)
{
    ::Lock lock(m_remoteDeviceCollectionMutex);

    TRemoteDeviceRecordCollection::iterator iter(
        m_remoteDeviceCollection.find(remoteDeviceRecord.address));
    if (iter != m_remoteDeviceCollection.end())
    {
        //Found!
        iter->second.lastObservationTimeUTC = remoteDeviceRecord.lastObservationTimeUTC;
        iter->second.lastObservationTimeSteady = remoteDeviceRecord.lastObservationTimeSteady;
        iter->second.visibilityCounter += remoteDeviceRecord.visibilityCounter;
    }
    else
    {
        m_remoteDeviceCollection[remoteDeviceRecord.address] = remoteDeviceRecord;
    }
}

void DataContainer::resetRemoteDeviceRecords()
{
    m_remoteDeviceCollection.clear();
}

void DataContainer::removeNonPresentRemoteDeviceRecords(
    const uint64_t currentTimeSinceZeroInSecondsUTC,
    const unsigned int absenceThresholdInSeconds,
    const bool moveToNonPresentCollectionForReporting)
{
    ::Lock lock(m_remoteDeviceCollectionMutex);

    for (TRemoteDeviceRecordCollection::iterator iter(m_remoteDeviceCollection.begin());
        iter != m_remoteDeviceCollection.end(); /* not hoisted */
        /* no increment */)
    {
        //It may happen that a device will get updated after the time for this
        //function is stored, which may result in current time being earlier than
        //the last observation.
        if (currentTimeSinceZeroInSecondsUTC < iter->second.lastObservationTimeUTC)
        {
            ++iter;
            continue;
        }
        //else do nothing

        //Ignore background devices
        if (iter->second.binType == eBIN_TYPE_BACKGROUND)
        {
            ++iter;
            continue;
        }
        //else do nothing

        const uint64_t DEVICE_NOT_OBSERVED_FOR_X_SECONDS =
            currentTimeSinceZeroInSecondsUTC - iter->second.lastObservationTimeUTC;
        if (DEVICE_NOT_OBSERVED_FOR_X_SECONDS >= absenceThresholdInSeconds)
        {
            if (moveToNonPresentCollectionForReporting)
            {
                //TODO In the future add an estimator based on RSSI to evaluate strongest signal position
                iter->second.referencePointObservationTimeUTC =
                    iter->second.firstObservationTimeUTC +
                    ((iter->second.lastObservationTimeUTC - iter->second.firstObservationTimeUTC) >> 1);

                iter->second.referencePointObservationTimeSteady =
                    iter->second.firstObservationTimeSteady +
                    ((iter->second.lastObservationTimeSteady - iter->second.firstObservationTimeSteady) >> 1);

                //move entry from the collection to another collection
                m_nonPendingRemoteDeviceCollection.push_back(iter->second);
            }
            //else do nothing

            m_remoteDeviceCollection.erase(iter++);
        }
        else
        {
            ++iter;
        }
    }
}

void DataContainer::reviewRemoteDevicesAgainstBackgroundCriteria(
    const uint64_t currentTimeSinceZeroInSecondsUTC,
    const unsigned int backgroundPresenceThresholdInSeconds,
    const unsigned int backgroundAbsenceThresholdInSeconds)
{
    ::Lock lock(m_remoteDeviceCollectionMutex);

    for (TRemoteDeviceRecordCollection::iterator iter(m_remoteDeviceCollection.begin());
        iter != m_remoteDeviceCollection.end(); /* not hoisted */
        /* no increment */)
    {
        //It may happen that a device will get updated after the time for this
        //function is stored, which may result in current time being earlier than
        //the last observation.
        if (currentTimeSinceZeroInSecondsUTC < iter->second.lastObservationTimeUTC)
        {
            ++iter;
            continue;
        }
        //else do nothing

        if (iter->second.binType != eBIN_TYPE_BACKGROUND)
        {
            const uint64_t DEVICE_OBSERVED_FOR_X_SECONDS =
                currentTimeSinceZeroInSecondsUTC - iter->second.firstObservationTimeUTC;
            if (DEVICE_OBSERVED_FOR_X_SECONDS >= backgroundPresenceThresholdInSeconds)
            {
                iter->second.binType = eBIN_TYPE_BACKGROUND;

                //move entry from the collection to another collection
                m_nonPendingRemoteDeviceCollection.push_back(iter->second);

                std::ostringstream ss;
                ss << "Device " << std::hex << iter->second.address << " has been ADDED to BACKGROUND BIN";
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            }
            //else do nothing

            ++iter;
        }
        else
        {
            const uint64_t DEVICE_NOT_OBSERVED_FOR_X_SECONDS =
                currentTimeSinceZeroInSecondsUTC - iter->second.lastObservationTimeUTC;
            if (DEVICE_NOT_OBSERVED_FOR_X_SECONDS >= backgroundAbsenceThresholdInSeconds)
            {
                std::ostringstream ss;
                ss << "Device " << std::hex << iter->second.address << " has been REMOVED from BACKGROUND BIN";
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

                m_remoteDeviceCollection.erase(iter++);
            }
            else
            {
                ++iter;
            }
        }
    } //for
}

void DataContainer::latchBackgroundDevices(
    const uint64_t currentTimeSinceZeroInSecondsUTC,
    const unsigned int backgroundPresenceThresholdInSeconds)
{
    ::Lock lock(m_remoteDeviceCollectionMutex);

    for (TRemoteDeviceRecordCollection::iterator iter(m_remoteDeviceCollection.begin());
        iter != m_remoteDeviceCollection.end(); /* not hoisted */
        /* no increment */)
    {
        if (iter->second.binType != eBIN_TYPE_BACKGROUND)
        {
            const uint64_t DEVICE_OBSERVED_FOR_X_SECONDS =
                currentTimeSinceZeroInSecondsUTC - iter->second.firstObservationTimeUTC;
            if (DEVICE_OBSERVED_FOR_X_SECONDS >= backgroundPresenceThresholdInSeconds)
            {
                iter->second.binType = eBIN_TYPE_BACKGROUND;

                //move entry from the collection to another collection
                m_nonPendingRemoteDeviceCollection.push_back(iter->second);

                std::ostringstream ss;
                ss << "Device " << std::hex << iter->second.address << " has been ADDED to BACKGROUND BIN";
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            }
            //else do nothing

            ++iter;
        }
        else
        {
            ++iter;
        }
    } //for
}

void DataContainer::flushBackgroundDevices()
{
    ::Lock lock(m_remoteDeviceCollectionMutex);

    for (TRemoteDeviceRecordCollection::iterator iter(m_remoteDeviceCollection.begin());
        iter != m_remoteDeviceCollection.end(); /* not hoisted */
        /* no increment */)
    {
        if (iter->second.binType == eBIN_TYPE_BACKGROUND)
        {
            std::ostringstream ss;
            ss << "Device " << std::hex << iter->second.address << " has been REMOVED from BACKGROUND BIN";
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

            m_remoteDeviceCollection.erase(iter++);
        }
        else
        {
            ++iter;
        }
    } //for
}

void DataContainer::getBackgroundDevicesCollection(std::vector<TRemoteDeviceRecord>& result) const
{
    result.clear();

    ::Lock lock(m_remoteDeviceCollectionMutex);

    for (TRemoteDeviceRecordCollection::const_iterator
            iter(m_remoteDeviceCollection.begin()), iterEnd(m_remoteDeviceCollection.end());
        iter != iterEnd;
        ++iter)
    {
        if (iter->second.binType == eBIN_TYPE_BACKGROUND)
            result.push_back(iter->second);
    }
}

void DataContainer::processDevicesDueToLocalBlueToothDeviceFault()
{
    ::Lock lock(m_remoteDeviceCollectionMutex);

    for (TRemoteDeviceRecordCollection::iterator iter(m_remoteDeviceCollection.begin());
        iter != m_remoteDeviceCollection.end(); /* not hoisted */
        /* no increment */)
    {
        if (iter->second.binType != eBIN_TYPE_BACKGROUND)
        {
            iter->second.referencePointObservationTimeUTC = 0;
            iter->second.referencePointObservationTimeSteady = 0;
            iter->second.lastObservationTimeUTC = BLUETOOTH_RADIO_FAULT_TIME_TAG;
            iter->second.lastObservationTimeSteady = BLUETOOTH_RADIO_FAULT_TIME_TAG;

            //move entry from the collection to another collection
            m_nonPendingRemoteDeviceCollection.push_back(iter->second);
            m_remoteDeviceCollection.erase(iter++);
        }
        else
        {
            ++iter;
        }
    }
}

DataContainer::TRemoteDeviceRecordCollection& DataContainer::getRemoteDeviceCollection()
{
    return m_remoteDeviceCollection;
}

DataContainer::TNonPendingRemoteDeviceRecordCollection& DataContainer::getNonPendingRemoteDeviceCollection()
{
    return m_nonPendingRemoteDeviceCollection;
}

::Mutex& DataContainer::getRemoteDeviceCollectionMutex()
{
    return m_remoteDeviceCollectionMutex;
}

void DataContainer::setLocalDeviceRecord(TLocalDeviceRecord_shared_ptr pDeviceRecord)
{
    m_pLocalDeviceRecord = pDeviceRecord;
}

const TLocalDeviceRecord_shared_ptr DataContainer::getLocalDeviceRecord() const
{
    return m_pLocalDeviceRecord;
}

TLocalDeviceRecord_shared_ptr DataContainer::getLocalDeviceRecord()
{
    return m_pLocalDeviceRecord;
}

Fault& DataContainer::getBluetoothDeviceFault()
{
    return m_bluetoothDeviceFault;
}

Fault& DataContainer::getRetrieveConfigurationClientCommunicationFault()
{
    return m_retrieveConfigurationClientCommunicationFault;
}

Fault& DataContainer::getRetrieveConfigurationClientResponseNotOkFault()
{
    return m_retrieveConfigurationClientResponseNotOkFault;
}

Fault& DataContainer::getRetrieveConfigurationClientResponseMessageBodyErrorFault()
{
    return m_retrieveConfigurationClientResponseMessageBodyErrorFault;
}

Fault& DataContainer::getCongestionReportingClientCommunicationFault()
{
    return m_congestionReportingClientCommunicationFault;
}

Fault& DataContainer::getCongestionReportingClientResponseNotOkFault()
{
    return m_congestionReportingClientResponseNotOkFault;
}

Fault& DataContainer::getCongestionReportingClientResponseMessageBodyErrorFault()
{
    return m_congestionReportingClientResponseMessageBodyErrorFault;
}

Fault& DataContainer::getRawDeviceDetectionClientCommunicationFault()
{
    return m_rawDeviceDetectionClientCommunicationFault;
}

Fault& DataContainer::getRawDeviceDetectionClientResponseNotOkFault()
{
    return m_rawDeviceDetectionClientResponseNotOkFault;
}

Fault& DataContainer::getRawDeviceDetectionClientResponseMessageBodyErrorFault()
{
    return m_rawDeviceDetectionClientResponseMessageBodyErrorFault;
}

Fault& DataContainer::getAlertAndStatusReportingClientCommunicationFault()
{
    return m_alertAndStatusReportingClientCommunicationFault;
}

Fault& DataContainer::getAlertAndStatusReportingClientResponseNotOkFault()
{
    return m_alertAndStatusReportingClientResponseNotOkFault;
}

Fault& DataContainer::getAlertAndStatusReportingClientResponseMessageBodyErrorFault()
{
    return m_alertAndStatusReportingClientResponseMessageBodyErrorFault;
}

Fault& DataContainer::getStatusReportingClientCommunicationFault()
{
    return m_statusReportingClientCommunicationFault;
}

Fault& DataContainer::getStatusReportingClientResponseNotOkFault()
{
    return m_statusReportingClientResponseNotOkFault;
}

Fault& DataContainer::getStatusReportingClientResponseMessageBodyErrorFault()
{
    return m_statusReportingClientResponseMessageBodyErrorFault;
}

Fault& DataContainer::getFaultReportingClientCommunicationFault()
{
    return m_faultReportingClientCommunicationFault;
}

Fault& DataContainer::getFaultReportingClientResponseNotOkFault()
{
    return m_faultReportingClientResponseNotOkFault;
}

Fault& DataContainer::getFaultReportingClientResponseMessageBodyErrorFault()
{
    return m_faultReportingClientResponseMessageBodyErrorFault;
}

Fault& DataContainer::getStatisticsReportingClientCommunicationFault()
{
    return m_statisticsReportingClientCommunicationFault;
}

Fault& DataContainer::getStatisticsReportingClientResponseNotOkFault()
{
    return m_statisticsReportingClientResponseNotOkFault;
}

Fault& DataContainer::getStatisticsReportingClientResponseMessageBodyErrorFault()
{
    return m_statisticsReportingClientResponseMessageBodyErrorFault;
}

Fault& DataContainer::getFunctionalConfigurationSyntaxFault()
{
    return m_functionalConfigurationSyntaxFault;
}

Fault& DataContainer::getFunctionalConfigurationParameterValueFault()
{
    return m_functionalConfigurationParameterValueFault;
}

Fault& DataContainer::getSeedFileFault()
{
    return m_seedFileFault;
}

Fault& DataContainer::getInStationSSHUnableToConnectFault()
{
    return m_inStationSSHUnableToConnectFault;
}

Fault& DataContainer::getGSMModemUnableToConnectFault()
{
    return m_GSMModemUnableToConnectFault;
}

const LocalDeviceConfiguration& DataContainer::getLocalDeviceConfiguration() const
{
    return m_localDeviceConfiguration;
}

LocalDeviceConfiguration& DataContainer::getLocalDeviceConfiguration()
{
    return m_localDeviceConfiguration;
}

void DataContainer::setLocalDeviceConfiguration(const LocalDeviceConfiguration& value)
{
    m_localDeviceConfiguration = value;
}

} //namespace
