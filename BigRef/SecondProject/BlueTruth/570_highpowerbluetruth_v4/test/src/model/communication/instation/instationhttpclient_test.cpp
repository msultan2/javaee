#include "stdafx.h"
#include <gtest/gtest.h>

#include "instation/instationhttpclient.h"

#include "clock.h"
#include "fault.h"
#include "test_connectionproducerclient.h"
#include "test_observer.h"
#include "test_signaturegenerator.h"
#include "configuration/iniconfiguration.h"
#include "configuration/test_coreconfiguration.h"
#include "configuration/test_seedconfiguration.h"
#include "instation/test_instationreporter.h"


using InStation::IHTTPClient;
using InStation::InStationHTTPClient;
using Model::Fault;
using Model::IConnectionProducerClient;
using Model::IniConfiguration;
using Testing::TestConnectionProducerClient;
using Testing::TestCoreConfiguration;
using Testing::TestInStationReporter;
using Testing::TestObserver;
using Testing::TestSeedConfiguration;
using Testing::TestSignatureGenerator;

template <class S, class T>
void maskCharactersToIgnoreDueToRandFunction(S& s1, T& s2, const size_t macAddressPosition)
{
    s1[macAddressPosition+4]  = s2[macAddressPosition+4] = '?';
    s1[macAddressPosition+5]  = s2[macAddressPosition+5] = '?';
    s1[macAddressPosition+10] = s2[macAddressPosition+10] = '?';
    s1[macAddressPosition+11] = s2[macAddressPosition+11] = '?';
}

template <class S, class T>
void displayDifferingCharacters(const S& s1, const size_t s1Size, const T& s2, const size_t s2Size)
{
    for (size_t i=0; (i<s1Size) && (s1Size == s2Size); ++i)
    {
        if (s1[i] != s2[i])
            std::cout << i << ": " << s1[i] << "-" << s2[i] << std::endl;
    }
}

#define _CRLF "\x0d\x0a"


TEST(InStationHTTPClient, constructor)
{
    TestCoreConfiguration coreConfiguration;
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    //Do not know what to check...
}

TEST(InStationHTTPClient, sendRawDeviceDetection)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(3);
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    { //Nothing should be sent if rawDeviceDetectionCollection is empty
        ::TTime_t startTime;
        InStationHTTPClient::TRawDeviceDetectionCollection rawDeviceDetectionCollection;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendRawDeviceDetection(
            startTime,
            rawDeviceDetectionCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 0);
    }

    { //m_rawDeviceDetectionPath is empty. nothing should be sent
        ::TTime_t startTime;
        InStationHTTPClient::TRawDeviceDetectionCollection rawDeviceDetectionCollection;
        InStationHTTPClient::TRawDeviceDetection record1(1234);
        rawDeviceDetectionCollection.push_back(record1);
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendRawDeviceDetection(
            startTime,
            rawDeviceDetectionCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 0);
    }

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
            )));
    std::string rawDeviceDetectionPath;
    pIniConfiguration->getValueString(
                Model::eURL_JOURNEY_TIMES_REPORTING, rawDeviceDetectionPath);
    EXPECT_STREQ("http://localhost/x", rawDeviceDetectionPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);

    {
        ::TTime_t startTime(ZERO_TIME);
        InStationHTTPClient::TRawDeviceDetectionCollection rawDeviceDetectionCollection;
        InStationHTTPClient::TRawDeviceDetection record1(0x0004d2);
        rawDeviceDetectionCollection.push_back(record1);
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendRawDeviceDetection(
            startTime,
            rawDeviceDetectionCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        ASSERT_EQ(1, inStationHTTPClient.getAllocatedSendRequestList().size());
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: application/x-www-form-urlencoded" _CRLF
            "Content-Length: 70" _CRLF
            "" _CRLF
            "outstationID=&startTime=1970-01-01 00:00:00&devCount=1&d1=0000000004d2";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION, tuple.type);

        inStationHTTPClient.shutdown();
    }

    {
        ::TTime_t startTime(ZERO_TIME);
        InStationHTTPClient::TRawDeviceDetectionCollection rawDeviceDetectionCollection;
        InStationHTTPClient::TRawDeviceDetection record1(0xc8df7cc81be7);
        rawDeviceDetectionCollection.push_back(record1);
        InStationHTTPClient::TRawDeviceDetection record2(0x1235);
        rawDeviceDetectionCollection.push_back(record2);
        bool useHttpVersion1_1 = false;
        bool shouldCloseConnectionAfterSending = true;
        inStationHTTPClient.sendRawDeviceDetection(
            startTime,
            rawDeviceDetectionCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x HTTP/1.0" _CRLF
            "Host: :0" _CRLF
            "Content-Type: application/x-www-form-urlencoded" _CRLF
            "Content-Length: 86" _CRLF
            "" _CRLF
            "outstationID=&startTime=1970-01-01 00:00:00&devCount=2&d1=c8df7cc81be7&d2=000000001235";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION, tuple.type);

        inStationHTTPClient.shutdown();
    }
}

TEST(InStationHTTPClient, sendCongestionReport_v3)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(3);
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    { //m_congestionReportingPath is empty. nothing should be sent
        ::TTime_t reportTime(ZERO_TIME);
        QueueDetection::CongestionReport congestionReport;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_EQ(0U, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlCongestionReporting=http://localhost/x/y.html\n"
            )));
    std::string congestionReportPath;
    pIniConfiguration->getValueString(
                Model::eURL_CONGESTION_REPORTING, congestionReportPath);
    EXPECT_STREQ("http://localhost/x/y.html", congestionReportPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);


    { //report time is empty. nothing should be sent
        ::TTime_t reportTime;
        QueueDetection::CongestionReport congestionReport;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_EQ(0U, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    {
        ::TTime_t reportTime(ZERO_TIME);
        QueueDetection::CongestionReport congestionReport;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.0" _CRLF
            "Host: :0" _CRLF
            "Content-Type: application/x-www-form-urlencoded" _CRLF
            "Content-Length: 47" _CRLF
            "" _CRLF
            "id=&t=1970-01-01 00:00:00&f=0&m=0&s=0&vs=0&st=0";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }

    {
        ::TTime_t reportTime(ZERO_TIME);
        QueueDetection::CongestionReport congestionReport;
        congestionReport.numberOfDevicesInFreeFlowBin = 1;
        congestionReport.numberOfDevicesInModerateFlowBin = 2;
        congestionReport.numberOfDevicesInSlowFlowBin = 3;
        congestionReport.numberOfDevicesInVerySlowFlowBin = 4;
        congestionReport.numberOfDevicesInStaticFlowBin = 5;
        congestionReport.queuePresenceState = QueueDetection::eQUEUE_PRESENCE_STATE_NOT_READY;
        congestionReport.queueStartTime = pt::time_from_string("2013-01-01 00:00:00.000");
        congestionReport.queueEndTime = pt::time_from_string("2014-01-31 00:00:00.000");
        bool useHttpVersion1_1 = false;
        bool shouldCloseConnectionAfterSending = true;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.0" _CRLF
            "Host: :0" _CRLF
            "Content-Type: application/x-www-form-urlencoded" _CRLF
            "Content-Length: 93" _CRLF
            "" _CRLF
            "id=&t=1970-01-01 00:00:00&f=1&m=2&s=3&vs=4&st=5&qs=2013-01-01 00:00:00&qe=2014-01-31 00:00:00";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }
}

TEST(InStationHTTPClient, sendCongestionReport_v4)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("12345");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x1234);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    { //m_congestionReportingPath is empty. nothing should be sent
        ::TTime_t reportTime(pt::time_from_string("1970-01-01 00:00:01.000"));
        QueueDetection::CongestionReport congestionReport;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_EQ(0U, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlCongestionReports=http://localhost/x/y.html\n"
            )));
    std::string congestionReportPath;
    pIniConfiguration->getValueString(
                Model::eURL_CONGESTION_REPORTS, congestionReportPath);
    EXPECT_STREQ("http://localhost/x/y.html", congestionReportPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);


    {
        ::TTime_t reportTime(pt::time_from_string("1970-01-01 00:00:01.000"));
        QueueDetection::CongestionReport congestionReport;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 32" _CRLF
            "" _CRLF
            "12345,00000001,0:0:0:0:0,fe,1234";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }

    {
        ::TTime_t reportTime(ZERO_TIME);
        QueueDetection::CongestionReport congestionReport;
        congestionReport.numberOfDevicesInFreeFlowBin = 1;
        congestionReport.numberOfDevicesInModerateFlowBin = 2;
        congestionReport.numberOfDevicesInSlowFlowBin = 3;
        congestionReport.numberOfDevicesInVerySlowFlowBin = 4;
        congestionReport.numberOfDevicesInStaticFlowBin = 5;
        congestionReport.queuePresenceState = QueueDetection::eQUEUE_PRESENCE_STATE_NO_QUEUE;
        congestionReport.queueStartTime = pt::time_from_string("2013-01-01 00:00:00.000");
        congestionReport.queueEndTime = pt::time_from_string("2014-01-31 00:00:00.000");
        bool useHttpVersion1_1 = false;
        bool shouldCloseConnectionAfterSending = true;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.0" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 31" _CRLF
            "" _CRLF
            "12345,00000000,1:2:3:4:5,0,1234";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }

    {
        ::TTime_t reportTime(ZERO_TIME);
        QueueDetection::CongestionReport congestionReport;
        congestionReport.numberOfDevicesInFreeFlowBin = 1;
        congestionReport.numberOfDevicesInModerateFlowBin = 2;
        congestionReport.numberOfDevicesInSlowFlowBin = 3;
        congestionReport.numberOfDevicesInVerySlowFlowBin = 4;
        congestionReport.numberOfDevicesInStaticFlowBin = 5;
        congestionReport.queuePresenceState = QueueDetection::eQUEUE_PRESENCE_STATE_QUEUE_PRESENT;
        congestionReport.queueStartTime = pt::time_from_string("2013-01-01 00:00:00.000");
        congestionReport.queueEndTime = pt::time_from_string("2014-01-31 00:00:00.000");
        bool useHttpVersion1_1 = false;
        bool shouldCloseConnectionAfterSending = true;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.0" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 31" _CRLF
            "" _CRLF
            "12345,00000000,1:2:3:4:5,9,1234";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }

    {
        ::TTime_t reportTime(ZERO_TIME);
        QueueDetection::CongestionReport congestionReport;
        congestionReport.numberOfDevicesInFreeFlowBin = 1;
        congestionReport.numberOfDevicesInModerateFlowBin = 2;
        congestionReport.numberOfDevicesInSlowFlowBin = 3;
        congestionReport.numberOfDevicesInVerySlowFlowBin = 4;
        congestionReport.numberOfDevicesInStaticFlowBin = 5;
        congestionReport.queuePresenceState = QueueDetection::eQUEUE_PRESENCE_STATE_NOT_READY;
        congestionReport.queueStartTime = pt::time_from_string("2013-01-01 00:00:00.000");
        congestionReport.queueEndTime = pt::time_from_string("2014-01-31 00:00:00.000");
        bool useHttpVersion1_1 = false;
        bool shouldCloseConnectionAfterSending = true;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.0" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 32" _CRLF
            "" _CRLF
            "12345,00000000,1:2:3:4:5,fe,1234";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }

    {
        ::TTime_t reportTime(ZERO_TIME);
        QueueDetection::CongestionReport congestionReport;
        congestionReport.numberOfDevicesInFreeFlowBin = 1;
        congestionReport.numberOfDevicesInModerateFlowBin = 2;
        congestionReport.numberOfDevicesInSlowFlowBin = 3;
        congestionReport.numberOfDevicesInVerySlowFlowBin = 4;
        congestionReport.numberOfDevicesInStaticFlowBin = 5;
        congestionReport.queuePresenceState = QueueDetection::eQUEUE_PRESENCE_STATE_FAULT;
        congestionReport.queueStartTime = pt::time_from_string("2013-01-01 00:00:00.000");
        congestionReport.queueEndTime = pt::time_from_string("2014-01-31 00:00:00.000");
        bool useHttpVersion1_1 = false;
        bool shouldCloseConnectionAfterSending = true;
        inStationHTTPClient.sendCongestionReport(
            reportTime,
            congestionReport,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.0" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 32" _CRLF
            "" _CRLF
            "12345,00000000,1:2:3:4:5,ff,1234";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }
}

TEST(InStationHTTPClient, sendConfigurationRequest)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(3);
    coreConfiguration.setSiteIdentifier("12347");
    TestSeedConfiguration seedConfiguration;

    TestConnectionProducerClient testConnectionProducerClient;
    testConnectionProducerClient.setRemoteAddress("127.0.0.1");
    testConnectionProducerClient.setRemotePortNumber(80);
    TestSignatureGenerator testSignatureGenerator;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    { //urlConfigurationPath is empty. nothing should be sent
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendConfigurationRequest(
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 0);
    }

    coreConfiguration.setConfigurationURL("http://37.152.43.178:80/DetectorConfigurationDownload/1_50");
    coreConfiguration.setConfigurationURL_filePrefix("");
    coreConfiguration.setConfigurationURL_fileSuffix("_ini.txt");

    {
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendConfigurationRequest(
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "GET /DetectorConfigurationDownload/1_50/12347_ini.txt HTTP/1.1" _CRLF
            "Host: 127.0.0.1:80" _CRLF
            "" _CRLF;
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_GET_CONFIGURATION, tuple.type);

        inStationHTTPClient.shutdown();
    }
}

TEST(InStationHTTPClient, sendAlertAndStatusReport)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(3);
    coreConfiguration.setSiteIdentifier("1234");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    { //m_alertAndStatusReportsPath is empty. nothing should be sent
        ::TTime_t reportTime(ZERO_TIME);
        InStationHTTPClient::TAlertAndStatusReportCollection alertAndStatusReportCollection;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendAlertAndStatusReport(
            reportTime,
            alertAndStatusReportCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 0);
    }

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlAlertAndStatusReports=http://localhost/x/y.html\n"
            )));
    std::string alertAndStatusReportsPath;
    pIniConfiguration->getValueString(
                Model::eURL_ALERT_AND_STATUS_REPORTS, alertAndStatusReportsPath);
    EXPECT_STREQ("http://localhost/x/y.html", alertAndStatusReportsPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);

    {
        ::TTime_t reportTime(ZERO_TIME);
        InStationHTTPClient::TAlertAndStatusReportCollection alertAndStatusReportCollection;
        InStationHTTPClient::TAlertAndStatusReport report1("100",5);
        alertAndStatusReportCollection.push_back(report1);
        InStationHTTPClient::TAlertAndStatusReport report2("101",4);
        alertAndStatusReportCollection.push_back(report2);

        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendAlertAndStatusReport(
            reportTime,
            alertAndStatusReportCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        ASSERT_EQ(1, inStationHTTPClient.getAllocatedSendRequestList().size());
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: application/x-www-form-urlencoded" _CRLF
            "Content-Length: 48" _CRLF
            "" _CRLF
            "id=1234&dt=1970-01-01 00:00:00&m=3&s=100:5,101:4";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_ALARM_AND_STATUS_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }
}

TEST(InStationHTTPClient, sendStatusReport)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("1234");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x3456);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    ASSERT_EQ(0, inStationHTTPClient.getAllocatedSendRequestList().size());

    //Start testing

    { //m_statusReportsPath is empty. nothing should be sent
        ::TTime_t reportTime(ZERO_TIME);
        InStationHTTPClient::TStatusReportCollection statusReportCollection;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendStatusReport(
            reportTime,
            statusReportCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 0);
    }

    { //Setup ini configuration
        boost::shared_ptr<IniConfiguration> pIniConfiguration =
            boost::shared_ptr<IniConfiguration>(
                new IniConfiguration(std::string(
                    "urlStatusReports=http://localhost/x/z.html\n"
                )));
        std::string statusReportsPath;
        pIniConfiguration->getValueString(
                    Model::eURL_STATUS_REPORTS, statusReportsPath);
        EXPECT_STREQ("http://localhost/x/z.html", statusReportsPath.c_str());

        inStationHTTPClient.setup(pIniConfiguration);
    }

    { //statusReportCollection is empty. nothing should be sent
        ::TTime_t reportTime(ZERO_TIME);
        InStationHTTPClient::TStatusReportCollection statusReportCollection;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendStatusReport(
            reportTime,
            statusReportCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 0);
    }

    {
        ::TTime_t reportTime(pt::time_from_string("1971-04-02 00:00:00.000"));
        InStationHTTPClient::TStatusReportCollection statusReportCollection;
        InStationHTTPClient::TStatusReport report1("firmware_version","0.99");
        statusReportCollection.push_back(report1);
        InStationHTTPClient::TStatusReport report2("config_version","1.25");
        statusReportCollection.push_back(report2);

        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendStatusReport(
            reportTime,
            statusReportCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        ASSERT_EQ(1, inStationHTTPClient.getAllocatedSendRequestList().size());
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/z.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 60" _CRLF
            "" _CRLF
            "1234,02592c00,firmware_version=0.99,config_version=1.25,3456";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_STATUS_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }
}

TEST(InStationHTTPClient, sendFaultReport)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("1234");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x2345);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    { //m_faultReportsPath is empty. nothing should be sent
        ::TTime_t reportTime(ZERO_TIME);
        InStationHTTPClient::TFaultReportCollection faultReportCollection;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendFaultReport(
            reportTime,
            faultReportCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 0);
    }

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlFaultReports=http://localhost/x/z.html\n"
            )));
    std::string statusReportsPath;
    pIniConfiguration->getValueString(
                Model::eURL_FAULT_REPORTS, statusReportsPath);
    EXPECT_STREQ("http://localhost/x/z.html", statusReportsPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);

    { //faultReportCollection is empty. nothing should be sent
        ::TTime_t reportTime(ZERO_TIME);
        InStationHTTPClient::TFaultReportCollection faultReportCollection;
        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendFaultReport(
            reportTime,
            faultReportCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 0);
    }

    {
        ::TTime_t reportTime(pt::time_from_string("1971-04-02 00:00:00.000"));
        InStationHTTPClient::TFaultReportCollection faultReportCollection;
        InStationHTTPClient::FaultReport report1(101, pt::time_from_string("2000-01-01 00:00:00.000"), 1);
        faultReportCollection.push_back(report1);
        InStationHTTPClient::FaultReport report2(102, pt::time_from_string("2000-01-01 00:00:10.000"), 0);
        faultReportCollection.push_back(report2);

        bool useHttpVersion1_1 = true;
        bool shouldCloseConnectionAfterSending = false;
        inStationHTTPClient.sendFaultReport(
            reportTime,
            faultReportCollection,
            useHttpVersion1_1,
            shouldCloseConnectionAfterSending);

        ASSERT_EQ(1, inStationHTTPClient.getAllocatedSendRequestList().size());
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/z.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 48" _CRLF
            "" _CRLF
            "1234,02592c00,101:386d4380:1,102:386d438a:0,2345";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_FAULT_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
    }
}

TEST(InStationHTTPClient, sendStatisticsReport)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("1234");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x7654);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    { //m_statisticsReportsPath is empty. nothing should be sent
        ::TTime_t reportStartTime(ZERO_TIME);
        ::TTime_t reportEndTime(pt::time_from_string("1970-01-01 01:00:00.000"));
        InStationHTTPClient::TStatisticsReportCollection statisticsReportCollection;
        inStationHTTPClient.sendStatisticsReport(
            reportStartTime,
            reportEndTime,
            statisticsReportCollection);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 0);
    }

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlStatisticsReports=http://localhost/x/z.html\n"
            )));
    std::string statusReportsPath;
    pIniConfiguration->getValueString(
                Model::eURL_STATISTICS_REPORTS, statusReportsPath);
    EXPECT_STREQ("http://localhost/x/z.html", statusReportsPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);

    { //statisticsReportCollection is empty. Empty STATISTICS REPORT should be sent
        ::TTime_t reportStartTime(ZERO_TIME);
        ::TTime_t reportEndTime(pt::time_from_string("1970-01-01 01:00:00.000"));
        InStationHTTPClient::TStatisticsReportCollection statisticsReportCollection;
        inStationHTTPClient.sendStatisticsReport(
            reportStartTime,
            reportEndTime,
            statisticsReportCollection);

        ASSERT_EQ(1, inStationHTTPClient.getAllocatedSendRequestList().size());
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/z.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 22" _CRLF
            "" _CRLF
            "1234,00000000,e10"
            ",7654";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_STATISTICS_REPORT, tuple.type);
    }

    {
        Model::RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_NONE;
        ::TTime_t reportStartTime(ZERO_TIME);
        ::TTime_t reportEndTime(pt::time_from_string("1970-01-01 01:00:00.000"));
        InStationHTTPClient::TStatisticsReportCollection statisticsReportCollection;
        InStationHTTPClient::StatisticsReport record1(0xABCDEF012345, (const uint8_t*)"\xAB\xCD\xEF\x01\x23\x45", 0x123, 0xa, 0x14, 0x1e);
        statisticsReportCollection.push_back(record1);
        InStationHTTPClient::StatisticsReport record2(0xABCDEF543210, (const uint8_t*)"\xAB\xCD\xEF\x54\x32\x10", 0x123, 0xa, 0x14, 0x28);
        statisticsReportCollection.push_back(record2);
        inStationHTTPClient.sendStatisticsReport(
            reportStartTime,
            reportEndTime,
            statisticsReportCollection);

        ASSERT_EQ(2, inStationHTTPClient.getAllocatedSendRequestList().size());
        InStationHTTPClient::TRequestTypeTupleList::const_iterator it(inStationHTTPClient.getAllocatedSendRequestList().begin());
        {
            it++;
            InStationHTTPClient::RequestTypeTuple tuple(*it);

            const char EXPECTED_MESSAGE[] =
                "POST /x/z.html HTTP/1.1" _CRLF
                "Host: :0" _CRLF
                "Content-Type: text/plain" _CRLF
                "Content-Length: 86" _CRLF
                "" _CRLF
                "1234,00000000,e10"
                ",abcdef012345:123:0000000a:14:1e"
                ",abcdef543210:123:0000000a:14:28"
                ",7654";
            const std::string RESULTING_MESSAGE(
                (const char*)tuple.pPacket->data(), tuple.pPacket->size());

            EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
            EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
            EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_STATISTICS_REPORT, tuple.type);
        }

        inStationHTTPClient.shutdown();
    }

    { //The same but using hashing function
        pIniConfiguration->setValueInt64(Model::eHASHING_FUNCTION, (int64_t)1);
        Model::RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_RAND1;

        ::TTime_t reportStartTime(ZERO_TIME);
        ::TTime_t reportEndTime(pt::time_from_string("1970-01-01 01:00:00.000"));
        InStationHTTPClient::TStatisticsReportCollection statisticsReportCollection;
        InStationHTTPClient::StatisticsReport record1(0xABCDEF012345, (const uint8_t*)"\xEF\x23\x00\xAB\x45\x00\xCD\x01", 0x123, 10, 20, 30);
        statisticsReportCollection.push_back(record1);
        InStationHTTPClient::StatisticsReport record2(0xABCDEF543210, (const uint8_t*)"\xEF\x32\x00\xAB\x10\x00\xCD\x54", 0x123, 10, 20, 40);
        statisticsReportCollection.push_back(record2);
        inStationHTTPClient.sendStatisticsReport(
            reportStartTime,
            reportEndTime,
            statisticsReportCollection);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        char EXPECTED_MESSAGE[] =
            "POST /x/z.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 94" _CRLF
            "" _CRLF
            "1234,00000000,e10"
            ",ef237bab4532cd01:123:0000000a:14:1e"
            ",ef323cab1064cd54:123:0000000a:14:28"
            ",7654";
        std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        maskCharactersToIgnoreDueToRandFunction(RESULTING_MESSAGE, EXPECTED_MESSAGE, 101);
        maskCharactersToIgnoreDueToRandFunction(RESULTING_MESSAGE, EXPECTED_MESSAGE, 137);
        displayDifferingCharacters(RESULTING_MESSAGE, RESULTING_MESSAGE.size(), EXPECTED_MESSAGE, sizeof(EXPECTED_MESSAGE)-1);

        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_STATISTICS_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
        Model::RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_NONE;
    }

    { //The same but using hashing function
        pIniConfiguration->setValueInt64(Model::eHASHING_FUNCTION, (int64_t)2);
        Model::RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_SHA256;

        char sha256Value[LENGTH_OF_SHA256_HASH_IN_BYTES];
        memset(sha256Value, 0, sizeof(sha256Value));

        ::TTime_t reportStartTime(ZERO_TIME);
        ::TTime_t reportEndTime(pt::time_from_string("1970-01-01 01:00:00.000"));
        InStationHTTPClient::TStatisticsReportCollection statisticsReportCollection;
        strcpy(sha256Value, "\xAB\xCD\xEF\x01\x23\x45\x67\x89");
        InStationHTTPClient::StatisticsReport record1(0xABCDEF012345, (const uint8_t*)sha256Value, 0x123, 10, 20, 30);
        statisticsReportCollection.push_back(record1);
        strcpy(sha256Value, "\xAB\xCD\xEF\x01\x32\x45\x67\x89");
        InStationHTTPClient::StatisticsReport record2(0xABCDEF543210, (const uint8_t*)sha256Value, 0x123, 10, 20, 40);
        statisticsReportCollection.push_back(record2);
        inStationHTTPClient.sendStatisticsReport(
            reportStartTime,
            reportEndTime,
            statisticsReportCollection);

        EXPECT_TRUE(inStationHTTPClient.getAllocatedSendRequestList().size() == 1);
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        char EXPECTED_MESSAGE[] =
            "POST /x/z.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 190" _CRLF
            "" _CRLF
            "1234,00000000,e10"
            ",abcdef0123456789000000000000000000000000000000000000000000000000:123:0000000a:14:1e"
            ",abcdef0132456789000000000000000000000000000000000000000000000000:123:0000000a:14:28"
            ",7654";
        std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        displayDifferingCharacters(RESULTING_MESSAGE, RESULTING_MESSAGE.size(), EXPECTED_MESSAGE, sizeof(EXPECTED_MESSAGE)-1);

        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_STATISTICS_REPORT, tuple.type);

        inStationHTTPClient.shutdown();
        Model::RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_NONE;
    }
}

TEST(InStationHTTPClient, identifiers)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("1234");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        10,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    EXPECT_EQ(1u, inStationHTTPClient.getNumberOfIdentifiers());
    EXPECT_TRUE(inStationHTTPClient.isOfIdentifier(10));

    inStationHTTPClient.addIdentifier(100, 0, 0, 0);
    EXPECT_EQ(2u, inStationHTTPClient.getNumberOfIdentifiers());
    EXPECT_TRUE(inStationHTTPClient.isOfIdentifier(10));
    EXPECT_TRUE(inStationHTTPClient.isOfIdentifier(100));

    inStationHTTPClient.addIdentifier(200, 0, 0, 0);
    EXPECT_EQ(3u, inStationHTTPClient.getNumberOfIdentifiers());
    EXPECT_TRUE(inStationHTTPClient.isOfIdentifier(10));
    EXPECT_TRUE(inStationHTTPClient.isOfIdentifier(100));
    EXPECT_TRUE(inStationHTTPClient.isOfIdentifier(200));

    inStationHTTPClient.removeIdentifier(100);
    EXPECT_EQ(2u, inStationHTTPClient.getNumberOfIdentifiers());
    EXPECT_TRUE(inStationHTTPClient.isOfIdentifier(10));
    EXPECT_TRUE(inStationHTTPClient.isOfIdentifier(200));

    inStationHTTPClient.removeIdentifier(200);
    EXPECT_EQ(1u, inStationHTTPClient.getNumberOfIdentifiers());
    EXPECT_TRUE(inStationHTTPClient.isOfIdentifier(10));
}

TEST(InStationHTTPClient, receive)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("1234");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);
    TestObserver testObserver;
    std::vector<int>& testObserverIndexCollection = testObserver.getIndexCollection();
    inStationHTTPClient.addObserver(&testObserver);

    {
        FastDataPacket_shared_ptr packet;
        inStationHTTPClient.onReceive(packet);
        inStationHTTPClient.test_processReceivedPacket(packet);

        //TODO What can be tested now?
    }

    {
        const char RESPONSE[] = "Hello" _CRLF;
        FastDataPacket_shared_ptr packet(new FastDataPacket(RESPONSE));
        inStationHTTPClient.onReceive(packet);
        inStationHTTPClient.test_processReceivedPacket(packet);

        EXPECT_FALSE(inStationHTTPClient.getFullResponseDataPacket() == 0);
        for (size_t i=0; i<sizeof(RESPONSE) - 1; ++i)
            EXPECT_EQ(RESPONSE[i], (*inStationHTTPClient.getFullResponseDataPacket())[i]);
    }

    {
        const char RESPONSE[] = "Hello" _CRLF _CRLF;
        FastDataPacket_shared_ptr packet(new FastDataPacket(RESPONSE));
        inStationHTTPClient.onReceive(packet);
        inStationHTTPClient.test_processReceivedPacket(packet);

        EXPECT_TRUE(inStationHTTPClient.getFullResponseDataPacket() == 0);
    }

    {
        const char RESPONSE[] = _CRLF
"HTTP/1.1 500 Internal Server Error" _CRLF
"Date: Sat, 05 Oct 2013 21:21:12 GMT" _CRLF
"Content-Type: text/html;charset=utf-8" _CRLF
"Content-Length: 58" _CRLF
"Connection: close" _CRLF
 _CRLF
"<html><head><title>Apache Tomcat/6.0.35 - Error report ...";
        FastDataPacket_shared_ptr packet(new FastDataPacket(RESPONSE));
        inStationHTTPClient.onReceive(packet);
        inStationHTTPClient.test_processReceivedPacket(packet);

        EXPECT_TRUE(inStationHTTPClient.getFullResponseDataPacket() == 0);
    }

    {
        InStationHTTPClient::RequestTypeTuple tuple;
        tuple.pPacket = FastDataPacket_shared_ptr(new FastDataPacket());
        tuple.type = InStationHTTPClient::eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION,
        tuple.usePersistentConnection = true;
        tuple.shouldCloseConnectionAfterSending = true;
        //tuple.numberOfDispatches = 1;
        inStationHTTPClient.getAllocatedSendRequestList().push_back(tuple);

        const char RESPONSE[] = _CRLF
"HTTP/1.1 200 OK" _CRLF
"Date: Sat, 05 Oct 2013 21:21:12 GMT" _CRLF
"Content-Type: text/html;charset=utf-8" _CRLF
"Content-Length: 0" _CRLF
"Connection: close" _CRLF
 _CRLF;
        FastDataPacket_shared_ptr packet(new FastDataPacket(RESPONSE));
        inStationHTTPClient.onReceive(packet);
        inStationHTTPClient.test_processReceivedPacket(packet);

        EXPECT_TRUE(inStationHTTPClient.getFullResponseDataPacket() == 0);

        //Cleanup
        testObserver.getIndexCollection().clear();
        EXPECT_EQ(0, testObserver.getIndexCollection().size());
        inStationHTTPClient.getAllocatedSendRequestList().clear();
        EXPECT_EQ(0, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    { //Test openSSHConnection request
        InStationHTTPClient::RequestTypeTuple tuple;
        tuple.pPacket = FastDataPacket_shared_ptr(new FastDataPacket());
        tuple.type = InStationHTTPClient::eREQUEST_TYPE_POST_ALARM_AND_STATUS_REPORT,
        tuple.usePersistentConnection = true;
        tuple.shouldCloseConnectionAfterSending = true;
        //tuple.numberOfDispatches = 1;
        inStationHTTPClient.getAllocatedSendRequestList().push_back(tuple);

        const char RESPONSE[] = _CRLF
"HTTP/1.1 200 OK" _CRLF
"Date: Sat, 05 Oct 2013 21:21:12 GMT" _CRLF
"Content-Type: text/plain" _CRLF
"Content-Length: 23" _CRLF
"Connection: close" _CRLF
 _CRLF
"openSSHConnection:50000";
        FastDataPacket_shared_ptr packet(new FastDataPacket(RESPONSE));
        inStationHTTPClient.onReceive(packet);
        inStationHTTPClient.test_processReceivedPacket(packet);

        //EXPECT_FALSE(testConnectionProducerClient.requestToRestartReceived());
        EXPECT_TRUE(inStationHTTPClient.getFullResponseDataPacket() == 0);

        EXPECT_TRUE(std::find(
                testObserverIndexCollection.begin(),
                testObserverIndexCollection.end(),
                InStationHTTPClient::eOPEN_SSH_CONNECTION) != testObserverIndexCollection.end());
        EXPECT_EQ(50000, inStationHTTPClient.getRemoteSSHPortNumber());

        //Cleanup
        testObserver.getIndexCollection().clear();
        EXPECT_EQ(0, testObserver.getIndexCollection().size());
        inStationHTTPClient.getAllocatedSendRequestList().clear();
        EXPECT_EQ(0, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    { //test closeSSHConnection request
        InStationHTTPClient::RequestTypeTuple tuple;
        tuple.pPacket = FastDataPacket_shared_ptr(new FastDataPacket());
        tuple.type = InStationHTTPClient::eREQUEST_TYPE_POST_STATUS_REPORT,
        tuple.usePersistentConnection = true;
        tuple.shouldCloseConnectionAfterSending = true;
        //tuple.numberOfDispatches = 1;
        inStationHTTPClient.getAllocatedSendRequestList().push_back(tuple);

        const char RESPONSE[] = _CRLF
"HTTP/1.1 200 OK" _CRLF
"Date: Sat, 05 Oct 2013 21:21:12 GMT" _CRLF
"Content-Type: text/plain" _CRLF
"Content-Length: 18" _CRLF
"Connection: close" _CRLF
 _CRLF
"closeSSHConnection";
        FastDataPacket_shared_ptr packet(new FastDataPacket(RESPONSE));
        inStationHTTPClient.onReceive(packet);
        inStationHTTPClient.test_processReceivedPacket(packet);

        //EXPECT_FALSE(testConnectionProducerClient.requestToRestartReceived());
        EXPECT_TRUE(inStationHTTPClient.getFullResponseDataPacket() == 0);

        EXPECT_TRUE(std::find(
                testObserverIndexCollection.begin(),
                testObserverIndexCollection.end(),
                InStationHTTPClient::eCLOSE_SSH_CONNECTION) != testObserverIndexCollection.end());

        //Cleanup
        testObserver.getIndexCollection().clear();
        EXPECT_EQ(0, testObserver.getIndexCollection().size());
        inStationHTTPClient.getAllocatedSendRequestList().clear();
        EXPECT_EQ(0, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    { //test reloadConfiguration request
        InStationHTTPClient::RequestTypeTuple tuple;
        tuple.pPacket = FastDataPacket_shared_ptr(new FastDataPacket());
        tuple.type = InStationHTTPClient::eREQUEST_TYPE_POST_FAULT_REPORT,
        tuple.usePersistentConnection = true;
        tuple.shouldCloseConnectionAfterSending = true;
        //tuple.numberOfDispatches = 1;
        inStationHTTPClient.getAllocatedSendRequestList().push_back(tuple);

        const char RESPONSE[] = _CRLF
"HTTP/1.1 200 OK" _CRLF
"Date: Sat, 05 Oct 2013 21:21:12 GMT" _CRLF
"Content-Type: text/plain" _CRLF
"Content-Length: 19" _CRLF
"Connection: close" _CRLF
 _CRLF
"reloadConfiguration";
        FastDataPacket_shared_ptr packet(new FastDataPacket(RESPONSE));
        inStationHTTPClient.onReceive(packet);
        inStationHTTPClient.test_processReceivedPacket(packet);

        //EXPECT_FALSE(testConnectionProducerClient.requestToRestartReceived());
        EXPECT_TRUE(inStationHTTPClient.getFullResponseDataPacket() == 0);

        EXPECT_TRUE(std::find(
                testObserverIndexCollection.begin(),
                testObserverIndexCollection.end(),
                InStationHTTPClient::eRELOAD_CONFIGURATION) != testObserverIndexCollection.end());

        //Cleanup
        testObserver.getIndexCollection().clear();
        EXPECT_EQ(0, testObserver.getIndexCollection().size());

        EXPECT_NE(0, inStationHTTPClient.getAllocatedSendRequestList().size());
        inStationHTTPClient.clearAllocatedSendRequestList();
        EXPECT_EQ(0, inStationHTTPClient.getAllocatedSendRequestList().size());
    }
}

TEST(InStationHTTPClient, reboot)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("1234");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x4567);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);
    TestObserver testObserver;
    std::vector<int>& testObserverIndexCollection = testObserver.getIndexCollection();
    inStationHTTPClient.addObserver(&testObserver);

    boost::shared_ptr<TestInStationReporter> pInStationReporter(
        new TestInStationReporter());
    inStationHTTPClient.setup(pInStationReporter);

    { //Setup ini configuration
        boost::shared_ptr<IniConfiguration> pIniConfiguration =
            boost::shared_ptr<IniConfiguration>(
                new IniConfiguration(std::string(
                    "urlStatusReports=http://localhost/statusReports\n"
                )));

        std::string urlStatusReports;
        pIniConfiguration->getValueString(Model::eURL_STATUS_REPORTS, urlStatusReports);
        EXPECT_STREQ("http://localhost/statusReports", urlStatusReports.c_str());

        inStationHTTPClient.setup(pIniConfiguration);
        ASSERT_EQ(0, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    { //Pretend a report has been sent
        InStationHTTPClient::RequestTypeTuple tuple;
        tuple.pPacket = FastDataPacket_shared_ptr(new FastDataPacket());
        tuple.type = InStationHTTPClient::eREQUEST_TYPE_POST_FAULT_REPORT,
        tuple.usePersistentConnection = true;
        tuple.shouldCloseConnectionAfterSending = true;
        //tuple.numberOfDispatches = 1;
        inStationHTTPClient.getAllocatedSendRequestList().push_back(tuple);
        ASSERT_EQ(1, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    //Pretend to have received this packet containing changeSeed.
    //Check that new value of id is read from the test class and sent in response in Status Report
    const char RESPONSE[] = _CRLF
"HTTP/1.1 200 OK" _CRLF
"Date: Sat, 05 Oct 2013 21:21:12 GMT" _CRLF
"Content-Type: text/plain" _CRLF
"Content-Length: 6" _CRLF
"Connection: close" _CRLF
 _CRLF
"reboot";
    FastDataPacket_shared_ptr packet(new FastDataPacket(RESPONSE));
    inStationHTTPClient.onReceive(packet);
    inStationHTTPClient.test_processReceivedPacket(packet);

    //EXPECT_FALSE(testConnectionProducerClient.requestToRestartReceived());
    EXPECT_TRUE(inStationHTTPClient.getFullResponseDataPacket() == 0);

    EXPECT_TRUE(std::find(
            testObserverIndexCollection.begin(),
            testObserverIndexCollection.end(),
            InStationHTTPClient::eREBOOT) != testObserverIndexCollection.end());

    ASSERT_EQ(1, pInStationReporter->getLastStatusReportCollection().size());
    ASSERT_TRUE(pInStationReporter->wasLastStatusReportCollectionSet());
    IHTTPClient::TStatusReportCollection statusReportCollection(pInStationReporter->getLastStatusReportCollection());

    //std::cout << RESULTING_MESSAGE << std::endl;
    EXPECT_STREQ("boot", statusReportCollection[0].name.c_str());
    EXPECT_STREQ("1",    statusReportCollection[0].value.c_str());

    //Cleanup
    testObserver.getIndexCollection().clear();
    EXPECT_EQ(0, testObserver.getIndexCollection().size());

    EXPECT_NE(0, inStationHTTPClient.getAllocatedSendRequestList().size());
    inStationHTTPClient.clearAllocatedSendRequestList();
    EXPECT_EQ(0, inStationHTTPClient.getAllocatedSendRequestList().size());
}

TEST(InStationHTTPClient, changeSeed)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("1234");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x4567);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;

    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    boost::shared_ptr<TestInStationReporter> pInStationReporter(
        new TestInStationReporter());
    inStationHTTPClient.setup(pInStationReporter);

    TestObserver testObserver;
    std::vector<int>& testObserverIndexCollection = testObserver.getIndexCollection();
    inStationHTTPClient.addObserver(&testObserver);

    { //Setup ini configuration
        boost::shared_ptr<IniConfiguration> pIniConfiguration =
            boost::shared_ptr<IniConfiguration>(
                new IniConfiguration(std::string(
                    "urlStatusReports=http://localhost/statusReports\n"
                )));

        std::string urlStatusReports;
        pIniConfiguration->getValueString(Model::eURL_STATUS_REPORTS, urlStatusReports);
        EXPECT_STREQ("http://localhost/statusReports", urlStatusReports.c_str());

        inStationHTTPClient.setup(pIniConfiguration);
        ASSERT_EQ(0, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    { //Pretend a report has been sent
        InStationHTTPClient::RequestTypeTuple tuple;
        tuple.pPacket = FastDataPacket_shared_ptr(new FastDataPacket());
        tuple.type = InStationHTTPClient::eREQUEST_TYPE_POST_FAULT_REPORT,
        tuple.usePersistentConnection = true;
        tuple.shouldCloseConnectionAfterSending = true;
        //tuple.numberOfDispatches = 1;
        inStationHTTPClient.getAllocatedSendRequestList().push_back(tuple);
        ASSERT_EQ(1, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    seedConfiguration.setId(12345678);

    //Pretend to have received this packet containing changeSeed.
    //Check that new value of id is read from the test class and sent in response in Status Report
    const char RESPONSE[] = _CRLF
"HTTP/1.1 200 OK" _CRLF
"Date: Sat, 05 Oct 2013 21:21:12 GMT" _CRLF
"Content-Type: text/plain" _CRLF
"Content-Length: 10" _CRLF
"Connection: close" _CRLF
 _CRLF
"changeSeed";
    FastDataPacket_shared_ptr packet(new FastDataPacket(RESPONSE));
    inStationHTTPClient.onReceive(packet);
    inStationHTTPClient.test_processReceivedPacket(packet);

    //EXPECT_FALSE(testConnectionProducerClient.requestToRestartReceived());
    EXPECT_TRUE(inStationHTTPClient.getFullResponseDataPacket() == 0);

    EXPECT_TRUE(std::find(
            testObserverIndexCollection.begin(),
            testObserverIndexCollection.end(),
            InStationHTTPClient::eCHANGE_SEED) != testObserverIndexCollection.end());

    ASSERT_EQ(1, pInStationReporter->getLastStatusReportCollection().size());
    ASSERT_TRUE(pInStationReporter->wasLastStatusReportCollectionSet());
    IHTTPClient::TStatusReportCollection statusReportCollection(pInStationReporter->getLastStatusReportCollection());

    //std::cout << RESULTING_MESSAGE << std::endl;
    EXPECT_STREQ("seed",     statusReportCollection[0].name.c_str());
    EXPECT_STREQ("12345678", statusReportCollection[0].value.c_str());

    //Cleanup
    testObserver.getIndexCollection().clear();
    EXPECT_EQ(0, testObserver.getIndexCollection().size());

    EXPECT_NE(0, inStationHTTPClient.getAllocatedSendRequestList().size());
    inStationHTTPClient.clearAllocatedSendRequestList();
    EXPECT_EQ(0, inStationHTTPClient.getAllocatedSendRequestList().size());
}

TEST(InStationHTTPClient, performNormalSequenceCongestionReport)
{
    //Submit one packet to sent and check if it is passed to IConnectionProducerClient

    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("12345");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x1234);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlCongestionReports=http://localhost/x/y.html\n"
            )));
    std::string congestionReportPath;
    pIniConfiguration->getValueString(
                Model::eURL_CONGESTION_REPORTS, congestionReportPath);
    EXPECT_STREQ("http://localhost/x/y.html", congestionReportPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);
    inStationHTTPClient.initialise();

    for (int i=0; i<5; ++i)
    {
        EXPECT_EQ(InStationHTTPClient::eSTATE_WAITING_FOR_REQUEST,
            inStationHTTPClient.getRequestState());
        inStationHTTPClient.perform();
    }

    ::TTime_t reportTime(pt::time_from_string("1970-01-01 00:00:01.000"));
    QueueDetection::CongestionReport congestionReport;
    bool useHttpVersion1_1 = true;
    bool shouldCloseConnectionAfterSending = false;
    inStationHTTPClient.sendCongestionReport(
        reportTime,
        congestionReport,
        useHttpVersion1_1,
        shouldCloseConnectionAfterSending);

    {
        EXPECT_EQ(0U, testConnectionProducerClient.getDataToBeSentCollection().size());
        EXPECT_EQ(1U, inStationHTTPClient.getAllocatedSendRequestList().size());
        InStationHTTPClient::RequestTypeTuple tuple(*inStationHTTPClient.getAllocatedSendRequestList().begin());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 32" _CRLF
            "" _CRLF
            "12345,00000001,0:0:0:0:0,fe,1234";
        const std::string RESULTING_MESSAGE(
            (const char*)tuple.pPacket->data(), tuple.pPacket->size());

        EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
        EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);
    }

    inStationHTTPClient.perform();

    //Signal success to connect
    inStationHTTPClient.onConnect(true);

    inStationHTTPClient.perform();
    {
        std::vector<IConnectionProducerClient::TSendDataPacket_shared_ptr> data =
            testConnectionProducerClient.getDataToBeSentCollection();
        ASSERT_EQ(1U, data.size());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 32" _CRLF
            "" _CRLF
            "12345,00000001,0:0:0:0:0,fe,1234";
        const std::string RESULTING_MESSAGE(
            (const char*)data[0]->second->data(), data[0]->second->size());

        ASSERT_EQ(sizeof(EXPECTED_MESSAGE) -1, RESULTING_MESSAGE.size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
    }


    {
        const char OK_RESPONSE[] =
            "HTTP/1.0 200 OK" _CRLF
            "" _CRLF;

        FastDataPacket_shared_ptr response(new FastDataPacket(OK_RESPONSE));
        inStationHTTPClient.onReceive(response);
        inStationHTTPClient.test_processReceivedPacket(response);
        inStationHTTPClient.perform();
    }

    inStationHTTPClient.perform();
    {
        EXPECT_EQ(1U, testConnectionProducerClient.getDataToBeSentCollection().size());
        EXPECT_EQ(0U, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    //Signal connection closure
    inStationHTTPClient.onClose();

    inStationHTTPClient.perform();
    {
        EXPECT_EQ(1U, testConnectionProducerClient.getDataToBeSentCollection().size());
        EXPECT_EQ(0U, inStationHTTPClient.getAllocatedSendRequestList().size());
    }

    inStationHTTPClient.stop();
    inStationHTTPClient.perform();

    inStationHTTPClient.shutdown();
}

TEST(InStationHTTPClient, performNormalSequenceCongestionReportTwoPackets)
{
    /*
     * Submit two packets to sent and check if they are passed to IConnectionProducerClient.
     * Note that after the first packet is submitted for sending but before it is actually
     * sent the second packet is submitted, so after the first is sent the second is
     * already in the collection and must be pushed out.
     */

    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("12345");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x1234);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    clock += 1;
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlCongestionReports=http://localhost/x/y.html\n"
            )));
    std::string congestionReportPath;
    pIniConfiguration->getValueString(
                Model::eURL_CONGESTION_REPORTS, congestionReportPath);
    EXPECT_STREQ("http://localhost/x/y.html", congestionReportPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);

    QueueDetection::CongestionReport congestionReport;
    bool useHttpVersion1_1 = true;
    bool shouldCloseConnectionAfterSending = false;
    inStationHTTPClient.sendCongestionReport(
        clock.getUniversalTime(),
        congestionReport,
        useHttpVersion1_1,
        shouldCloseConnectionAfterSending);
    inStationHTTPClient.perform();

    clock += 10;
    inStationHTTPClient.sendCongestionReport(
        clock.getUniversalTime(),
        congestionReport,
        useHttpVersion1_1,
        shouldCloseConnectionAfterSending);
    inStationHTTPClient.perform();

    {
        EXPECT_EQ(0U, testConnectionProducerClient.getDataToBeSentCollection().size());

        InStationHTTPClient::TRequestTypeTupleList allocatedSendRequestList = inStationHTTPClient.getAllocatedSendRequestList();
        ASSERT_EQ(2U, allocatedSendRequestList.size());
        InStationHTTPClient::TRequestTypeTupleList::const_iterator it = allocatedSendRequestList.begin();
        const InStationHTTPClient::RequestTypeTuple tuple1(*it++);
        const InStationHTTPClient::RequestTypeTuple tuple2(*it);

        {
            const InStationHTTPClient::RequestTypeTuple& tuple = tuple1;
            const char EXPECTED_MESSAGE[] =
                "POST /x/y.html HTTP/1.1" _CRLF
                "Host: :0" _CRLF
                "Content-Type: text/plain" _CRLF
                "Content-Length: 32" _CRLF
                "" _CRLF
                "12345,00000001,0:0:0:0:0,fe,1234";
            const std::string RESULTING_MESSAGE(
                (const char*)tuple.pPacket->data(), tuple.pPacket->size());

            EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
            EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
            EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
            EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);
        }

        {
            const InStationHTTPClient::RequestTypeTuple& tuple = tuple2;
            const char EXPECTED_MESSAGE[] =
                "POST /x/y.html HTTP/1.1" _CRLF
                "Host: :0" _CRLF
                "Content-Type: text/plain" _CRLF
                "Content-Length: 32" _CRLF
                "" _CRLF
                "12345,0000000b,0:0:0:0:0,fe,1234";
            const std::string RESULTING_MESSAGE(
                (const char*)tuple.pPacket->data(), tuple.pPacket->size());

            EXPECT_EQ(sizeof(EXPECTED_MESSAGE) -1, tuple.pPacket->size());
            EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
            EXPECT_EQ(shouldCloseConnectionAfterSending, tuple.shouldCloseConnectionAfterSending);
            EXPECT_EQ(InStationHTTPClient::eREQUEST_TYPE_POST_CONGESTION_REPORT, tuple.type);
        }
    }

    inStationHTTPClient.onConnect(true);
    inStationHTTPClient.perform();
    {
        std::vector<IConnectionProducerClient::TSendDataPacket_shared_ptr> data =
            testConnectionProducerClient.getDataToBeSentCollection();
        ASSERT_EQ(1U, data.size());

        const char EXPECTED_MESSAGE[] =
            "POST /x/y.html HTTP/1.1" _CRLF
            "Host: :0" _CRLF
            "Content-Type: text/plain" _CRLF
            "Content-Length: 32" _CRLF
            "" _CRLF
            "12345,00000001,0:0:0:0:0,fe,1234";
        const std::string RESULTING_MESSAGE(
            (const char*)data[0]->second->data(), data[0]->second->size());

        ASSERT_EQ(sizeof(EXPECTED_MESSAGE) -1, RESULTING_MESSAGE.size());
        EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());

        inStationHTTPClient.onSend(true, data[0]);
        inStationHTTPClient.perform();
    }

    {
        const char OK_RESPONSE[] =
            "HTTP/1.0 200 OK" _CRLF
            "" _CRLF;

        FastDataPacket_shared_ptr response(new FastDataPacket(OK_RESPONSE));
        inStationHTTPClient.onReceive(response);
        inStationHTTPClient.perform();
        inStationHTTPClient.perform(); //dummy signal
    }
    inStationHTTPClient.onClose();
    inStationHTTPClient.perform();

    inStationHTTPClient.onConnect(true);
    inStationHTTPClient.perform();

    inStationHTTPClient.perform();
    {
        EXPECT_EQ(1U, inStationHTTPClient.getAllocatedSendRequestList().size());

        std::vector<IConnectionProducerClient::TSendDataPacket_shared_ptr> data =
            testConnectionProducerClient.getDataToBeSentCollection();
        ASSERT_EQ(2U, data.size());
        {
            const char EXPECTED_MESSAGE[] =
                "POST /x/y.html HTTP/1.1" _CRLF
                "Host: :0" _CRLF
                "Content-Type: text/plain" _CRLF
                "Content-Length: 32" _CRLF
                "" _CRLF
                "12345,0000000b,0:0:0:0:0,fe,1234";
            const std::string RESULTING_MESSAGE(
                (const char*)data[1]->second->data(), data[1]->second->size());

            ASSERT_EQ(sizeof(EXPECTED_MESSAGE) -1, RESULTING_MESSAGE.size());
            EXPECT_STREQ(EXPECTED_MESSAGE, RESULTING_MESSAGE.c_str());
        }

        inStationHTTPClient.onSend(true, data[1]);
        inStationHTTPClient.perform();
     }

     {
        const char OK_RESPONSE[] =
            "HTTP/1.0 200 OK" _CRLF
            "" _CRLF;

        FastDataPacket_shared_ptr response(new FastDataPacket(OK_RESPONSE));
        inStationHTTPClient.onReceive(response);
        inStationHTTPClient.perform();
        inStationHTTPClient.perform(); //dummy signal
    }

    {
        InStationHTTPClient::TRequestTypeTupleList allocatedSendRequestList = inStationHTTPClient.getAllocatedSendRequestList();
        ASSERT_EQ(0U, allocatedSendRequestList.size());
        std::vector<IConnectionProducerClient::TSendDataPacket_shared_ptr> data =
            testConnectionProducerClient.getDataToBeSentCollection();
        ASSERT_EQ(2U, data.size());
    }

    inStationHTTPClient.onClose();
    inStationHTTPClient.perform();
    inStationHTTPClient.perform();

    inStationHTTPClient.shutdown();
}

TEST(InStationHTTPClient, performSendCongestionReportWithFailure)
{
    //Try to send a congestion report but opening of the connection should fail

    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("12345");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x1234);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlCongestionReports=http://localhost/x/y.html\n"
            )));
    std::string congestionReportPath;
    pIniConfiguration->getValueString(
                Model::eURL_CONGESTION_REPORTS, congestionReportPath);
    EXPECT_STREQ("http://localhost/x/y.html", congestionReportPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);

    for (int i=0; i<5; ++i)
    {
        EXPECT_EQ(InStationHTTPClient::eSTATE_WAITING_FOR_REQUEST,
            inStationHTTPClient.getRequestState());
        inStationHTTPClient.perform();
    }

    ::TTime_t reportTime(pt::time_from_string("1970-01-01 00:00:01.000"));
    QueueDetection::CongestionReport congestionReport;
    bool useHttpVersion1_1 = true;
    bool shouldCloseConnectionAfterSending = false;
    inStationHTTPClient.sendCongestionReport(
        reportTime,
        congestionReport,
        useHttpVersion1_1,
        shouldCloseConnectionAfterSending);

    inStationHTTPClient.perform();
    {
        ASSERT_TRUE(testConnectionProducerClient.wasConnectionOpened());
        ASSERT_FALSE(testConnectionProducerClient.wasConnectionClosed());

        InStationHTTPClient::TRequestTypeTupleList allocatedSendRequestList = inStationHTTPClient.getAllocatedSendRequestList();
        ASSERT_EQ(1U, allocatedSendRequestList.size());
        std::vector<IConnectionProducerClient::TSendDataPacket_shared_ptr> data =
            testConnectionProducerClient.getDataToBeSentCollection();
        ASSERT_EQ(0U, data.size());
        testConnectionProducerClient.initialise();
    }

    //Signal failure to connect
    inStationHTTPClient.onConnect(false);
    inStationHTTPClient.perform();
    {
        ASSERT_FALSE(testConnectionProducerClient.wasConnectionOpened());
        ASSERT_FALSE(testConnectionProducerClient.wasConnectionClosed());
        testConnectionProducerClient.initialise();
    }

    inStationHTTPClient.perform();

    {
        InStationHTTPClient::TRequestTypeTupleList allocatedSendRequestList = inStationHTTPClient.getAllocatedSendRequestList();
        ASSERT_EQ(1U, allocatedSendRequestList.size());
        std::vector<IConnectionProducerClient::TSendDataPacket_shared_ptr> data =
            testConnectionProducerClient.getDataToBeSentCollection();
        ASSERT_EQ(0U, data.size());
    }

    inStationHTTPClient.shutdown();
}

TEST(InStationHTTPClient, performResponseTimeoutCongestionReport)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("12345");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x1234);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlCongestionReports=http://localhost/x/y.html\n"
                "httpResponseTimeOutInSeconds=10\n"
                "httpConnectionTimeOutInSeconds=30\n"
            )));
    std::string congestionReportPath;
    pIniConfiguration->getValueString(
                Model::eURL_CONGESTION_REPORTS, congestionReportPath);
    EXPECT_STREQ("http://localhost/x/y.html", congestionReportPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);

    for (int i=0; i<5; ++i)
    {
        EXPECT_EQ(InStationHTTPClient::eSTATE_WAITING_FOR_REQUEST,
            inStationHTTPClient.getRequestState());
        inStationHTTPClient.perform();
    }

    ::TTime_t reportTime(pt::time_from_string("1970-01-01 00:00:01.000"));
    QueueDetection::CongestionReport congestionReport;
    bool useHttpVersion1_1 = true;
    bool shouldCloseConnectionAfterSending = false;
    inStationHTTPClient.sendCongestionReport(
        reportTime,
        congestionReport,
        useHttpVersion1_1,
        shouldCloseConnectionAfterSending);

    inStationHTTPClient.shutdown();
}

TEST(InStationHTTPClient, performConnectionTimeoutCongestionReport)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("12345");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x1234);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlCongestionReports=http://localhost/x/y.html\n"
                "httpResponseTimeOutInSeconds=10\n"
                "httpConnectionTimeOutInSeconds=30\n"
            )));
    std::string congestionReportPath;
    pIniConfiguration->getValueString(
                Model::eURL_CONGESTION_REPORTS, congestionReportPath);
    EXPECT_STREQ("http://localhost/x/y.html", congestionReportPath.c_str());

    inStationHTTPClient.setup(pIniConfiguration);

    for (int i=0; i<5; ++i)
    {
        EXPECT_EQ(InStationHTTPClient::eSTATE_WAITING_FOR_REQUEST,
            inStationHTTPClient.getRequestState());
        inStationHTTPClient.perform();
    }

    ::TTime_t reportTime(pt::time_from_string("1970-01-01 00:00:01.000"));
    QueueDetection::CongestionReport congestionReport;
    bool useHttpVersion1_1 = true;
    bool shouldCloseConnectionAfterSending = false;
    inStationHTTPClient.sendCongestionReport(
        reportTime,
        congestionReport,
        useHttpVersion1_1,
        shouldCloseConnectionAfterSending);

    inStationHTTPClient.shutdown();
}

TEST(InStationHTTPClient, unexpectedResponse)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("12345");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x1234);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    EXPECT_EQ(InStationHTTPClient::eSTATE_WAITING_FOR_REQUEST,
        inStationHTTPClient.getRequestState());
    inStationHTTPClient.perform();

    {
        const char OK_RESPONSE[] =
            "HTTP/1.0 200 OK" _CRLF
            "" _CRLF;

        FastDataPacket_shared_ptr response(new FastDataPacket(OK_RESPONSE));
        inStationHTTPClient.onReceive(response);
        inStationHTTPClient.test_processReceivedPacket(response);
        inStationHTTPClient.perform();
    }

    inStationHTTPClient.perform();
    EXPECT_EQ(InStationHTTPClient::eSTATE_WAITING_FOR_REQUEST,
        inStationHTTPClient.getRequestState());
    inStationHTTPClient.perform();
    EXPECT_EQ(InStationHTTPClient::eSTATE_WAITING_FOR_REQUEST,
        inStationHTTPClient.getRequestState());

    inStationHTTPClient.shutdown();
}

TEST(InStationHTTPClient, sendRequest_listFullAllPacketsNotSent)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("12345");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x1234);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    //Apply new configuration
    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "reportStorageCapacity=100\n"
            )));
    int64_t reportStorageCapacity = 0;
    ASSERT_TRUE(pIniConfiguration->getValueInt64(Model::eREPORT_STORAGE_CAPACITY, reportStorageCapacity));
    ASSERT_EQ(100, reportStorageCapacity);
    inStationHTTPClient.setup(pIniConfiguration);

    int i = 0;

    //Start testing
    EXPECT_EQ(InStationHTTPClient::eSTATE_WAITING_FOR_REQUEST,
        inStationHTTPClient.getRequestState());
    inStationHTTPClient.perform();

    std::ostringstream ss;

    for (i=0; i<reportStorageCapacity; i++)
    {
        ss.str("");
        ss << "Item" << i;
        inStationHTTPClient.sendRequest__testing(
            ss.str(),
            InStationHTTPClient::eREQUEST_TYPE_POST_STATISTICS_REPORT,
            false,
            true);
    }
    ASSERT_EQ(reportStorageCapacity, inStationHTTPClient.getAllocatedSendRequestList().size());

    //Do it once again and check if the list size remains the same
    ss.str("");
    ss << "Item" << i;
    inStationHTTPClient.sendRequest__testing(
        ss.str(),
        InStationHTTPClient::eREQUEST_TYPE_POST_STATISTICS_REPORT,
        false,
        true);
    ASSERT_EQ(reportStorageCapacity, inStationHTTPClient.getAllocatedSendRequestList().size());

    i = 1;
    const InStationHTTPClient::TRequestTypeTupleList& allocatedSendRequestList = inStationHTTPClient.getAllocatedSendRequestList();
    for (
        InStationHTTPClient::TRequestTypeTupleList::const_iterator it(allocatedSendRequestList.begin()), itEnd(allocatedSendRequestList.end());
        it != itEnd;
        ++it
    )
    {
        ss.str("");
        ss << "Item" << i++;
        it->pPacket->fixForDisplaying();
        ASSERT_STREQ(ss.str().c_str(), it->pPacket->c_str()) << i;
    }

    inStationHTTPClient.shutdown();
}

TEST(InStationHTTPClient, sendRequest_listFullOnePacketInTheMiddleOfTransaction)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSiteIdentifier("12345");
    TestSeedConfiguration seedConfiguration;
    TestConnectionProducerClient testConnectionProducerClient;
    TestSignatureGenerator testSignatureGenerator;
    testSignatureGenerator.setNewSignature(0x1234);
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    Fault primaryCommunicationFault;
    Fault primaryResponseNotOKFault;
    Fault primaryResponseMessageBodyErrorFault;
    InStationHTTPClient inStationHTTPClient(
        coreConfiguration,
        &seedConfiguration,
        &testConnectionProducerClient,
        &testSignatureGenerator,
        &clock,
        0,
        &primaryCommunicationFault,
        &primaryResponseNotOKFault,
        &primaryResponseMessageBodyErrorFault);

    //Apply new configuration
    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "reportStorageCapacity=100\n"
            )));
    int64_t reportStorageCapacity = 0;
    ASSERT_TRUE(pIniConfiguration->getValueInt64(Model::eREPORT_STORAGE_CAPACITY, reportStorageCapacity));
    ASSERT_EQ(100, reportStorageCapacity);
    inStationHTTPClient.setup(pIniConfiguration);

    int i = 0;
    std::ostringstream ss;

    for (i=0; i<reportStorageCapacity; i++)
    {
        ss.str("");
        ss << "Item" << i;
        inStationHTTPClient.sendRequest__testing(
            ss.str(),
            InStationHTTPClient::eREQUEST_TYPE_POST_STATISTICS_REPORT,
            false,
            true);
    }
    ASSERT_EQ(reportStorageCapacity, inStationHTTPClient.getAllocatedSendRequestList().size());


    //Push the state machine so that the first packet is being sent
    EXPECT_EQ(InStationHTTPClient::eSTATE_WAITING_FOR_REQUEST,
        inStationHTTPClient.getRequestState());
    inStationHTTPClient.perform();

    EXPECT_EQ(InStationHTTPClient::eSTATE_REQUEST_RECEIVED_AND_WAITING_FOR_CONNECTION,
        inStationHTTPClient.getRequestState());
    inStationHTTPClient.perform();

    inStationHTTPClient.onConnect(true);

    i = 0;
    while (inStationHTTPClient.getRequestState() == InStationHTTPClient::eSTATE_REQUEST_RECEIVED_AND_WAITING_FOR_CONNECTION)
    {
        inStationHTTPClient.perform();
        if (i++ > 1000)
            ASSERT_TRUE(false) << "Iteration number exceeded";
    }

    EXPECT_EQ(InStationHTTPClient::eSTATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE,
        inStationHTTPClient.getRequestState());


    //Do it once again and check if the list size remains the same
    i = reportStorageCapacity;
    ss.str("");
    ss << "Item" << i;
    inStationHTTPClient.sendRequest__testing(
        ss.str(),
        InStationHTTPClient::eREQUEST_TYPE_POST_STATISTICS_REPORT,
        false,
        true);
    ASSERT_EQ(reportStorageCapacity, inStationHTTPClient.getAllocatedSendRequestList().size());



    i = 0;
    const InStationHTTPClient::TRequestTypeTupleList& allocatedSendRequestList = inStationHTTPClient.getAllocatedSendRequestList();
    for (
        InStationHTTPClient::TRequestTypeTupleList::const_iterator it(allocatedSendRequestList.begin()), itEnd(allocatedSendRequestList.end());
        it != itEnd;
        ++it
    )
    {
        ss.str("");
        ss << "Item" << i++;
        it->pPacket->fixForDisplaying();
        ASSERT_STREQ(ss.str().c_str(), it->pPacket->c_str()) << i;

        //Move forward when i==1  - this is the record that was to be removed
        if (i==1)
            i++;
    }

    inStationHTTPClient.shutdown();
}
