/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    06/10/2013  RG      001         V1.00 First Issue
*/

#ifndef I_HTTP_CLIENT_H_
#define I_HTTP_CLIENT_H_

#include "iobservable.h"
#include "queuedetector.h"
#include "types.h"

#include <stdint.h>
#include <vector>


namespace InStation
{

class IHTTPClient : public ::IObservable
{

public:

    //! destructor
    virtual ~IHTTPClient();

    enum
    {
        eLAST_ALARM_AND_STATUS_REPORT_HAS_BEEN_SENT = 1000,
        eLAST_ALARM_AND_STATUS_REPORT_HAS_FAILED,
        eLAST_STATUS_REPORT_HAS_BEEN_SENT,
        eLAST_STATUS_REPORT_HAS_FAILED,
        eLAST_FAULT_REPORT_HAS_BEEN_SENT,
        eLAST_FAULT_REPORT_HAS_FAILED,
        eLAST_STATISTICS_REPORT_HAS_BEEN_SENT,
        eLAST_STATISTICS_REPORT_HAS_FAILED,
        eLAST_CONGESTION_REPORT_HAS_BEEN_SENT,
        eLAST_CONGESTION_REPORT_HAS_FAILED,
        eLAST_RAW_DEVICE_DETECTION_HAS_BEEN_SENT,
        eLAST_RAW_DEVICE_DETECTION_HAS_FAILED,
        eLAST_CONFIGURATION_REQUEST_HAS_SUCCEDED,
        eLAST_CONFIGURATION_REQUEST_HAS_FAILED,
    };

    enum
    {
        eOPEN_SSH_CONNECTION = 990,
        eCLOSE_SSH_CONNECTION,
        eRELOAD_CONFIGURATION,
        eSEND_STATUS_REPORT,
        eREBOOT,
        eCHANGE_SEED,
        eLATCH_BACKGROUND,
        eFLUSH_BACKGROUND,
        eSEND_FAULT_REPORT
    };

    virtual unsigned short getRemoteSSHPortNumber() const = 0;


    //-------------------------
    virtual void sendConfigurationRequest(
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true) = 0;

    //-------------------------
    struct RawDeviceDetection
    {
        uint64_t deviceIdentifier;

        explicit RawDeviceDetection(const uint64_t _deviceIdentifier);
    };
    typedef struct RawDeviceDetection TRawDeviceDetection;
    typedef std::vector<TRawDeviceDetection> TRawDeviceDetectionCollection;

    virtual void sendRawDeviceDetection(
        const ::TTime_t& startTime,
        const TRawDeviceDetectionCollection& rawDeviceDetectionCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true) = 0;


    //-------------------------
    virtual void sendCongestionReport(
        const ::TTime_t& reportTime,
        const struct QueueDetection::CongestionReport& congestionReport,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true) = 0;


    //-------------------------
    struct AlertAndStatusReport
    {
        std::string code;
        unsigned int count;

        AlertAndStatusReport(const std::string& _code, const unsigned int _count);
    };
    typedef struct AlertAndStatusReport TAlertAndStatusReport;
    typedef std::vector<TAlertAndStatusReport> TAlertAndStatusReportCollection;

    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into InStationDataContainer
    virtual void sendAlertAndStatusReport(
        const ::TTime_t& reportTime,
        const TAlertAndStatusReportCollection& alertAndStatusReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true) = 0;


    //-------------------------
    struct StatusReport
    {
        std::string name;
        std::string value;

        StatusReport(const char* _name, const char* _value);
    };
    typedef struct StatusReport TStatusReport;
    typedef std::vector<TStatusReport> TStatusReportCollection;

    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into InStationDataContainer
    virtual void sendStatusReport(
        const ::TTime_t& reportTime,
        const TStatusReportCollection& statusReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true) = 0;


    //-------------------------
    struct FaultReport
    {
        unsigned int id;
        ::TTime_t eventTime;
        unsigned int status; //0 - cleared, 1 - set.

        FaultReport(
            const unsigned int _id,
            const ::TTime_t&_eventTime,
            const unsigned int _status);
    };
    typedef struct FaultReport TFaultReport;
    typedef std::vector<TFaultReport> TFaultReportCollection;

    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into InStationDataContainer
    virtual void sendFaultReport(
        const ::TTime_t& reportTime,
        const TFaultReportCollection& faultReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true) = 0;


    //-------------------------
    struct StatisticsReport
    {
        uint64_t deviceIdentifier;
        uint8_t deviceIdentifierHash[LENGTH_OF_HASH_IN_BYTES];
        uint32_t cod;
        uint64_t firstObservationTime;
        uint64_t referencePointObservationTimeDelta;
        uint64_t lastObservationTimeDelta;

        StatisticsReport(
            const uint64_t _deviceIdentifier,
            const uint8_t* _deviceIdentifierHash,
            const uint32_t _cod, //class of device
            const uint64_t _firstObservationTime,
            const uint64_t _referencePointObservationTimeDelta,
            const uint64_t _lastObservationTimeDelta);
    };
    typedef struct StatisticsReport TStatisticsReport;
    typedef std::vector<TStatisticsReport> TStatisticsReportCollection;

    virtual void sendStatisticsReport(
        const ::TTime_t& reportStartTime,
        const ::TTime_t& reportEndTime,
        const TStatisticsReportCollection& statisticsReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true) = 0;

protected:

    //! default constructor
    IHTTPClient();
    //! copy constructor
    IHTTPClient(const IHTTPClient& ) = delete;
    //! assignment operator
    IHTTPClient& operator=(const IHTTPClient& ) = delete;

};

}

#endif //I_HTTP_CLIENT_H_
