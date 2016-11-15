/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    06/10/2013  RG      001         V1.00 First Issue
*/

#ifndef TEST_HTTP_CLIENT_H_
#define TEST_HTTP_CLIENT_H_

#include "instation/ihttpclient.h"


namespace Testing
{

class TestHTTPClient : public InStation::IHTTPClient
{

public:

    //! default constructor
    TestHTTPClient();

    //! destructor
    virtual ~TestHTTPClient();

    virtual unsigned short getRemoteSSHPortNumber() const;
    void setRemoteSSHPortNumber(const unsigned short value);


    virtual void sendConfigurationRequest(
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = false);

    virtual void sendRawDeviceDetection(
        const ::TTime_t& startTime,
        const TRawDeviceDetectionCollection& rawDeviceDetectionCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = false);

    ::TTime_t getRawDeviceDetectionStartTime() const;
    TRawDeviceDetectionCollection getRawDeviceDetectionCollection() const;


    virtual void sendCongestionReport(
        const ::TTime_t& reportTime,
        const struct QueueDetection::CongestionReport& congestionReport,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = false);

    ::TTime_t getCongestionReportTime() const;
    QueueDetection::CongestionReport getCongestionReport() const;
    void clearCongestionReport();


    virtual void sendAlertAndStatusReport(
        const ::TTime_t& reportTime,
        const TAlertAndStatusReportCollection& alertAndStatusReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true);

    ::TTime_t getAlertAndStatusReportTime() const;
    TAlertAndStatusReportCollection getAlertAndStatusReportCollection() const;


    virtual void sendStatusReport(
        const ::TTime_t& reportTime,
        const TStatusReportCollection& statusReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true);

    ::TTime_t getStatusReportTime() const;
    TStatusReportCollection getStatusReportCollection() const;


    virtual void sendFaultReport(
        const ::TTime_t& reportTime,
        const TFaultReportCollection& faultReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true);

    ::TTime_t getFaultReportTime() const;
    TFaultReportCollection getFaultReportCollection() const;
    void clearFaultReportCollection();


    virtual void sendStatisticsReport(
        const ::TTime_t& reportStartTime,
        const ::TTime_t& reportEndTime,
        const TStatisticsReportCollection& statisticsReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = false);

    ::TTime_t getStatisticsReportStartTime() const;
    ::TTime_t getStatisticsReportEndTime() const;
    TStatisticsReportCollection getStatisticsReportCollection() const;
    void clearStatisticsReportCollection();

protected:

    //! copy constructor
    TestHTTPClient(const TestHTTPClient& );
    //! assignment operator
    TestHTTPClient& operator=(const TestHTTPClient& );

    unsigned short m_remoteSSHPortNumber;

    ::TTime_t m_rawDeviceDetectionStartTime;
    TRawDeviceDetectionCollection m_rawDeviceDetectionCollection;

    ::TTime_t m_congestionReportTime;
    QueueDetection::CongestionReport m_congestionReport;

    ::TTime_t m_alertAndStatusReportTime;
    TAlertAndStatusReportCollection m_alertAndStatusReportCollection;

    ::TTime_t m_statusReportTime;
    TStatusReportCollection m_statusReportCollection;

    ::TTime_t m_faultReportTime;
    TFaultReportCollection m_faultReportCollection;

    ::TTime_t m_statisticsReportStartTime;
    ::TTime_t m_statisticsReportEndTime;
    TStatisticsReportCollection m_statisticsReportCollection;
};

}

#endif //TEST_HTTP_CLIENT_H_
