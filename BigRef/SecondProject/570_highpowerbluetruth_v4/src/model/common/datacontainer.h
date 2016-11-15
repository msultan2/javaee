/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef DATA_CONTAINER_H_
#define DATA_CONTAINER_H_

#include "instation/iinstationdatacontainer.h"
#include "iobservable.h"
#include "types.h"

#include "fault.h"
#include "losslessdatareporter.h"
#include "localdevicerecord.h"
#include "localdeviceconfiguration.h"
#include "remotedevicerecord.h"

#include <boost/any.hpp>
#include <map>
#include <vector>


namespace Model
{

class DataContainer :
    public InStation::IInStationDataContainer,
    public ::IObservable
{

public:

    //! default constructor
    DataContainer();
    //! destructor
    virtual ~DataContainer();


    typedef std::map<uint64_t, TLocalDeviceRecord_shared_ptr> TLocalDeviceRecordCollection;
    typedef std::map<uint64_t, TRemoteDeviceRecord> TRemoteDeviceRecordCollection;
    typedef std::vector<TRemoteDeviceRecord> TNonPendingRemoteDeviceRecordCollection;

    void setLastInquiryStartTime(const ::TTime_t& startTime);
    const ::TTime_t& getLastInquiryStartTime() const;

    void updateRemoteDeviceRecord(const TRemoteDeviceRecord& remoteDeviceRecord);
    void resetRemoteDeviceRecords();

    // Note: All background devices are analysed using UTC clock rather than steady clock.
    // This is due to the fact that the time of background devices is stored accress reboots

    /**
     * @brief Remove all non-present devices from the present devices map
     *
     * @param currentTimeSinceZeroInSecondsUTC current time as number of seconds since 1970/1/1 0:00
     * @param absenceThresholdInSeconds absence threshold in seconds
     * @param moveToNonPresentCollectionForReporting This parameter must be
     * set to false if there is no reporting of statistic to the instation. If set
     * to true all the memory will be consumed by non-present devices tags after
     * a while
     */
    void removeNonPresentRemoteDeviceRecords(
        const uint64_t currentTimeSinceZeroInSecondsUTC,
        const unsigned int absenceThresholdInSeconds,
        const bool moveToNonPresentCollectionForReporting);

    /**
     * @brief Review all devices against background criteria.
     * Mark as background those that are visible for too long.
     * Remove those that exceed background leave threshold
     *
     * @param currentTimeSinceZeroInSecondsUTC current time as number of seconds since 1970/1/1 0:00
     * @param backgroundPresenceThresholdInSeconds time interval a device must be visible to become background
     * @param backgroundAbsenceThresholdInSeconds time interval a device must be not visible to be removed from background set
     */
    void reviewRemoteDevicesAgainstBackgroundCriteria(
        const uint64_t currentTimeSinceZeroInSecondsUTC,
        const unsigned int backgroundPresenceThresholdInSeconds,
        const unsigned int backgroundAbsenceThresholdInSeconds);

    /**
     * @brief Review all devices against background criteria.
     * Mark as background those that are visible for too long.
     */
    void latchBackgroundDevices(
        const uint64_t currentTimeSinceZeroInSecondsUTC,
        const unsigned int backgroundPresenceThresholdInSeconds);


    /**
     * @brief Remove all devices from the background bin
     */
    void flushBackgroundDevices();

    /**
     * @brief Provide a collection of all devices that are perceived as background
     *
     * @param[out] result Collection of all devices that belong to the background bin
     */
    void getBackgroundDevicesCollection(std::vector<TRemoteDeviceRecord>& result) const;

    /**
     * @brief Move all devices to non-existing devices collection to be reported as faulty
     * due to local bluetooth device failure. Only devices that belong to background bin
     * will stay unchanged
     */
    void processDevicesDueToLocalBlueToothDeviceFault();

    TRemoteDeviceRecordCollection& getRemoteDeviceCollection();
    TNonPendingRemoteDeviceRecordCollection& getNonPendingRemoteDeviceCollection();
    ::Mutex& getRemoteDeviceCollectionMutex();

    void setLocalDeviceRecord(TLocalDeviceRecord_shared_ptr pDeviceRecord);
    const TLocalDeviceRecord_shared_ptr getLocalDeviceRecord() const;
    TLocalDeviceRecord_shared_ptr getLocalDeviceRecord();

    enum EFaultNumber
    {
        eFAULT_FUNCTIONAL_CONFIGURATION_SYNTAX = 51,
        eFAULT_FUNCTIONAL_CONFIGURATION_PARAMETER_ERROR = 52,

        eFAULT_SEED_FILE = 60,
        eFAULT_NUMBER_BLUETOOTH_DEVICE = 100,

        eFAULT_NUMBER_RETRIEVE_CONFIGURATION_COMMUNICATION = 201,
        eFAULT_NUMBER_CONGESTION_REPORTING_COMMUNICATION = 202,
        eFAULT_NUMBER_RAW_DEVICE_DETECTION_REPORTING_COMMUNICATION = 203,
        eFAULT_NUMBER_ALERT_AND_STATUS_REPORTING_COMMUNICATION = 204,
        eFAULT_NUMBER_STATUS_REPORTING_COMMUNICATION = 205,
        eFAULT_NUMBER_FAULT_REPORTING_COMMUNICATION = 206,
        eFAULT_NUMBER_STATISTICS_REPORTING_COMMUNICATION = 207,

        eFAULT_NUMBER_RETRIEVE_CONFIGURATION_RESPONSE_NOT_OK = 211,
        eFAULT_NUMBER_CONGESTION_REPORTING_RESPONSE_NOT_OK = 212,
        eFAULT_NUMBER_RAW_DEVICE_DETECTION_REPORTING_RESPONSE_NOT_OK = 213,
        eFAULT_NUMBER_ALERT_AND_STATUS_REPORTING_RESPONSE_NOT_OK = 214,
        eFAULT_NUMBER_STATUS_REPORTING_RESPONSE_NOT_OK = 215,
        eFAULT_NUMBER_FAULT_REPORTING_RESPONSE_NOT_OK = 216,
        eFAULT_NUMBER_STATISTICS_REPORTING_RESPONSE_NOT_OK = 217,

        eFAULT_NUMBER_RETRIEVE_CONFIGURATION_RESPONSE_MESSAGE_BODY_ERROR = 221,
        eFAULT_NUMBER_CONGESTION_REPORTING_RESPONSE_MESSAGE_BODY_ERROR = 222,
        eFAULT_NUMBER_RAW_DEVICE_DETECTION_REPORTING_RESPONSE_MESSAGE_BODY_ERROR = 223,
        eFAULT_NUMBER_ALERT_AND_STATUS_REPORTING_RESPONSE_MESSAGE_BODY_ERROR = 224,
        eFAULT_NUMBER_STATUS_REPORTING_RESPONSE_MESSAGE_BODY_ERROR = 225,
        eFAULT_NUMBER_FAULT_REPORTING_RESPONSE_MESSAGE_BODY_ERROR = 226,
        eFAULT_NUMBER_STATISTICS_REPORTING_RESPONSE_MESSAGE_BODY_ERROR = 227,

        eFAULT_INSTATION_SSH_UNABLE_TO_CONNECT = 300,

        eFAULT_GSM_MODEM_UNABLE_TO_CONNECT = 400,
    };

    //Legacy fault strings
    static const char FAULT_STR_FUNCTIONAL_CONFIGURATION_SYNTAX[];
    static const char FAULT_STR_FUNCTIONAL_CONFIGURATION_PARAMETER_ERROR[];
    static const char FAULT_STR_NUMBER_RETRIEVE_CONFIGURATION_RESPONSE_NOT_OK[];
    static const char FAULT_STR_NUMBER_CONGESTION_REPORTING_RESPONSE_NOT_OK[];
    static const char FAULT_STR_NUMBER_RAW_DEVICE_DETECTION_REPORTING_RESPONSE_NOT_OK[];
    static const char FAULT_STR_NUMBER_ALERT_AND_STATUS_REPORTING_RESPONSE_NOT_OK[];
    static const char FAULT_STR_NUMBER_RETRIEVE_CONFIGURATION_COMMUNICATION[];
    static const char FAULT_STR_NUMBER_RAW_DEVICE_DETECTION_REPORTING_COMMUNICATION[];
    static const char FAULT_STR_NUMBER_BLUETOOTH_DEVICE[];


    Fault& getBluetoothDeviceFault();
    Fault& getRetrieveConfigurationClientCommunicationFault();
    Fault& getRetrieveConfigurationClientResponseNotOkFault();
    Fault& getRetrieveConfigurationClientResponseMessageBodyErrorFault();
    Fault& getCongestionReportingClientCommunicationFault();
    Fault& getCongestionReportingClientResponseNotOkFault();
    Fault& getCongestionReportingClientResponseMessageBodyErrorFault();
    Fault& getRawDeviceDetectionClientCommunicationFault();
    Fault& getRawDeviceDetectionClientResponseNotOkFault();
    Fault& getRawDeviceDetectionClientResponseMessageBodyErrorFault();
    Fault& getAlertAndStatusReportingClientCommunicationFault();
    Fault& getAlertAndStatusReportingClientResponseNotOkFault();
    Fault& getAlertAndStatusReportingClientResponseMessageBodyErrorFault();
    Fault& getStatusReportingClientCommunicationFault();
    Fault& getStatusReportingClientResponseNotOkFault();
    Fault& getStatusReportingClientResponseMessageBodyErrorFault();
    Fault& getFaultReportingClientCommunicationFault();
    Fault& getFaultReportingClientResponseNotOkFault();
    Fault& getFaultReportingClientResponseMessageBodyErrorFault();
    Fault& getStatisticsReportingClientCommunicationFault();
    Fault& getStatisticsReportingClientResponseNotOkFault();
    Fault& getStatisticsReportingClientResponseMessageBodyErrorFault();
    Fault& getFunctionalConfigurationSyntaxFault();
    Fault& getFunctionalConfigurationParameterValueFault();
    Fault& getSeedFileFault();
    Fault& getInStationSSHUnableToConnectFault();
    Fault& getGSMModemUnableToConnectFault();


    const LocalDeviceConfiguration& getLocalDeviceConfiguration() const;
    LocalDeviceConfiguration& getLocalDeviceConfiguration();
    void setLocalDeviceConfiguration(const LocalDeviceConfiguration& value);

    enum
    {
        eLOCAL_DEVICE_HAS_BEEN_CHANGED = 1,
        eREMOTE_DEVICE_COLLECTION_HAS_BEEN_CHANGED
    };

private:

    //! copy constructor. Not implemented
    DataContainer(const DataContainer& );
    //! assignment operator. Not implemented
    DataContainer& operator=(const DataContainer& );

    //Device identifiers for Raw Journey Time reporting
    //LosslessDataReporter<TRemoteDeviceRecordCollection> m_remoteDeviceCollection;
    TRemoteDeviceRecordCollection m_remoteDeviceCollection;
    TNonPendingRemoteDeviceRecordCollection m_nonPendingRemoteDeviceCollection;
    mutable ::Mutex m_remoteDeviceCollectionMutex;
    ::TTime_t m_lastInquiryStartTime;

    TLocalDeviceRecord_shared_ptr m_pLocalDeviceRecord;
    LocalDeviceConfiguration m_localDeviceConfiguration;

    Fault m_bluetoothDeviceFault;
    Fault m_retrieveConfigurationClientCommunicationFault;
    Fault m_retrieveConfigurationClientResponseNotOkFault;
    Fault m_retrieveConfigurationClientResponseMessageBodyErrorFault;
    Fault m_congestionReportingClientCommunicationFault;
    Fault m_congestionReportingClientResponseNotOkFault;
    Fault m_congestionReportingClientResponseMessageBodyErrorFault;
    Fault m_rawDeviceDetectionClientCommunicationFault;
    Fault m_rawDeviceDetectionClientResponseNotOkFault;
    Fault m_rawDeviceDetectionClientResponseMessageBodyErrorFault;
    Fault m_alertAndStatusReportingClientCommunicationFault;
    Fault m_alertAndStatusReportingClientResponseNotOkFault;
    Fault m_alertAndStatusReportingClientResponseMessageBodyErrorFault;
    Fault m_statusReportingClientCommunicationFault;
    Fault m_statusReportingClientResponseNotOkFault;
    Fault m_statusReportingClientResponseMessageBodyErrorFault;
    Fault m_faultReportingClientCommunicationFault;
    Fault m_faultReportingClientResponseNotOkFault;
    Fault m_faultReportingClientResponseMessageBodyErrorFault;
    Fault m_statisticsReportingClientCommunicationFault;
    Fault m_statisticsReportingClientResponseNotOkFault;
    Fault m_statisticsReportingClientResponseMessageBodyErrorFault;
    Fault m_functionalConfigurationSyntaxFault;
    Fault m_functionalConfigurationParameterValueFault;
    Fault m_seedFileFault;
    Fault m_inStationSSHUnableToConnectFault;
    Fault m_GSMModemUnableToConnectFault;
};

}

#endif //DATA_CONTAINER_H_
