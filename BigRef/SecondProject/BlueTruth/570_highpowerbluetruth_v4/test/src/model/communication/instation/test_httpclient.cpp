#include "stdafx.h"
#include "test_httpclient.h"


namespace Testing
{

TestHTTPClient::TestHTTPClient()
:
InStation::IHTTPClient(),
m_remoteSSHPortNumber(0),
m_rawDeviceDetectionStartTime(),
m_rawDeviceDetectionCollection(),
m_congestionReportTime(),
m_congestionReport(),
m_alertAndStatusReportTime(),
m_alertAndStatusReportCollection(),
m_statusReportTime(),
m_statusReportCollection(),
m_faultReportTime(),
m_faultReportCollection(),
m_statisticsReportStartTime(),
m_statisticsReportEndTime(),
m_statisticsReportCollection()
{
    //do nothing
}

TestHTTPClient::~TestHTTPClient()
{
    //do nothing
}

unsigned short TestHTTPClient::getRemoteSSHPortNumber() const
{
    return m_remoteSSHPortNumber;
}

void TestHTTPClient::setRemoteSSHPortNumber(const unsigned short value)
{
    m_remoteSSHPortNumber = value;
}

void TestHTTPClient::sendConfigurationRequest(
    const bool ,
    const bool )
{
    //do nothing
}

void TestHTTPClient::sendRawDeviceDetection(
    const ::TTime_t& startTime,
    const TRawDeviceDetectionCollection& rawDeviceDetectionCollection,
    const bool ,
    const bool )
{
    m_rawDeviceDetectionStartTime = startTime;
    m_rawDeviceDetectionCollection = rawDeviceDetectionCollection;
}

::TTime_t TestHTTPClient::getRawDeviceDetectionStartTime() const
{
    return m_rawDeviceDetectionStartTime;
}

TestHTTPClient::TRawDeviceDetectionCollection TestHTTPClient::getRawDeviceDetectionCollection() const
{
    return m_rawDeviceDetectionCollection;
}

void TestHTTPClient::sendCongestionReport(
    const ::TTime_t& reportTime,
    const struct QueueDetection::CongestionReport& congestionReport,
    const bool ,
    const bool )
{
    m_congestionReportTime = reportTime;
    m_congestionReport = congestionReport;
}

::TTime_t TestHTTPClient::getCongestionReportTime() const
{
    return m_congestionReportTime;
}

QueueDetection::CongestionReport TestHTTPClient::getCongestionReport() const
{
    return m_congestionReport;
}

void TestHTTPClient::clearCongestionReport()
{
    m_congestionReport.reset();
}


void TestHTTPClient::sendAlertAndStatusReport(
    const ::TTime_t& reportTime,
    const TAlertAndStatusReportCollection& alertAndStatusReportCollection,
    const bool ,
    const bool )
{
    m_alertAndStatusReportTime = reportTime;
    m_alertAndStatusReportCollection = alertAndStatusReportCollection;
}

::TTime_t TestHTTPClient::getAlertAndStatusReportTime() const
{
    return m_alertAndStatusReportTime;
}

TestHTTPClient::TAlertAndStatusReportCollection TestHTTPClient::getAlertAndStatusReportCollection() const
{
    return m_alertAndStatusReportCollection;
}

void TestHTTPClient::sendStatusReport(
    const ::TTime_t& reportTime,
    const TStatusReportCollection& statusReportCollection,
    const bool ,
    const bool )
{
    m_statusReportTime = reportTime;
    m_statusReportCollection = statusReportCollection;
}

::TTime_t TestHTTPClient::getStatusReportTime() const
{
    return m_statusReportTime;
}

TestHTTPClient::TStatusReportCollection TestHTTPClient::getStatusReportCollection() const
{
    return m_statusReportCollection;
}


void TestHTTPClient::sendFaultReport(
    const ::TTime_t& reportTime,
    const TFaultReportCollection& faultReportCollection,
    const bool ,
    const bool )
{
    m_faultReportTime = reportTime;
    m_faultReportCollection = faultReportCollection;
}

::TTime_t TestHTTPClient::getFaultReportTime() const
{
    return m_faultReportTime;
}

TestHTTPClient::TFaultReportCollection TestHTTPClient::getFaultReportCollection() const
{
    return m_faultReportCollection;
}

void TestHTTPClient::clearFaultReportCollection()
{
    m_faultReportCollection.clear();
}

void TestHTTPClient::sendStatisticsReport(
    const ::TTime_t& reportStartTime,
    const ::TTime_t& reportEndTime,
    const TStatisticsReportCollection& statisticsReportCollection,
    const bool ,
    const bool )
{
    m_statisticsReportStartTime = reportStartTime;
    m_statisticsReportEndTime = reportEndTime;
    m_statisticsReportCollection = statisticsReportCollection;
}

::TTime_t TestHTTPClient::getStatisticsReportStartTime() const
{
    return m_statisticsReportStartTime;
}

::TTime_t TestHTTPClient::getStatisticsReportEndTime() const
{
    return m_statisticsReportEndTime;
}

TestHTTPClient::TStatisticsReportCollection TestHTTPClient::getStatisticsReportCollection() const
{
    return m_statisticsReportCollection;
}

void TestHTTPClient::clearStatisticsReportCollection()
{
    m_statisticsReportCollection.clear();
}

} //namespace
