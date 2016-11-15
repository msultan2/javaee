/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef TEST_INSTATION_REPORTER_H_
#define TEST_INSTATION_REPORTER_H_

#include "instation/iinstationreporter.h"


namespace Testing
{

/**
 * @brief This is a class that provides an interface of IInStationReporter
 * that can be used for testing of other classes
 */
class TestInStationReporter : public InStation::IInStationReporter
{
public:

    //! default constructor
    TestInStationReporter();
    //! destructor
    virtual ~TestInStationReporter();

    virtual void sendRawDeviceDetection();
    virtual void sendCongestionReport();
    virtual void sendFullStatusReport();

    virtual void sendStatusReport(const InStation::IHTTPClient::TStatusReportCollection& statusReportCollection);
    const InStation::IHTTPClient::TStatusReportCollection& getLastStatusReportCollection() const;
    bool wasLastStatusReportCollectionSet() const { return m_lastStatusReportCollectionSetFlag; }
    void resetLastStatusReportCollection() { m_lastStatusReportCollectionSetFlag = false; }

    virtual void sendStatisticsReport();
    virtual void sendConfigurationRequest();
    virtual void reportFault();


    bool wasRawDeviceDetectionSent() { return m_rawDeviceDetectionSent; }
    void clearRawDeviceDetectionSent() { m_rawDeviceDetectionSent = false; }

    bool wasCongestionReportSent() { return m_congestionReportSent; }
    void clearCongestionReportSent() { m_congestionReportSent = false; }

    bool wasStatusReportSent() { return m_statusReportSent; }
    void clearStatusReportSent() { m_statusReportSent = false; }

    bool wasStatisticsReportSent() { return m_statisticsReportSent; }
    void clearStatisticsReportSent() { m_statisticsReportSent = false; }

    bool wasFaultReported() { return m_faultReported; }
    void clearFaultReported() { m_faultReported = false; }

protected:

    //! copy constructor
    TestInStationReporter(const TestInStationReporter& ) = delete;
    //! assignment operator
    TestInStationReporter& operator=(const TestInStationReporter& ) = delete;

private:
    InStation::IHTTPClient::TStatusReportCollection m_lastStatusReportCollection;
    bool m_lastStatusReportCollectionSetFlag;

    bool m_rawDeviceDetectionSent;
    bool m_congestionReportSent;
    bool m_statusReportSent;
    bool m_statisticsReportSent;
    bool m_faultReported;
};

}
//namespace

#endif //TEST_INSTATION_REPORTER_H_
