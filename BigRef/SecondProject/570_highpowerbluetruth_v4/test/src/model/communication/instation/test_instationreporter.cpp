#include "stdafx.h"
#include "test_instationreporter.h"

using InStation::IInStationReporter;
using InStation::IHTTPClient;


namespace Testing
{

TestInStationReporter::TestInStationReporter()
:
IInStationReporter(),
m_lastStatusReportCollection(),
m_lastStatusReportCollectionSetFlag(false),
m_rawDeviceDetectionSent(false),
m_congestionReportSent(false),
m_statusReportSent(false),
m_statisticsReportSent(false),
m_faultReported(false)
{
    //do nothing
}

TestInStationReporter::~TestInStationReporter()
{
    //do nothing
}

void TestInStationReporter::sendRawDeviceDetection()
{
    m_rawDeviceDetectionSent = true;
}

void TestInStationReporter::sendCongestionReport()
{
    m_congestionReportSent = true;
}

void TestInStationReporter::sendFullStatusReport()
{
    m_statusReportSent = true;
}

void TestInStationReporter::sendStatusReport(const IHTTPClient::TStatusReportCollection& statusReportCollection)
{
    m_lastStatusReportCollection = statusReportCollection;
    m_lastStatusReportCollectionSetFlag = true;
}

const IHTTPClient::TStatusReportCollection& TestInStationReporter::getLastStatusReportCollection() const
{
    return m_lastStatusReportCollection;
}

void TestInStationReporter::sendStatisticsReport()
{
    m_statisticsReportSent = true;
}

void TestInStationReporter::sendConfigurationRequest()
{
    //do nothing
}

void TestInStationReporter::reportFault()
{
    m_faultReported = true;
}

} //namespace
