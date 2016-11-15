#include "stdafx.h"
#include <gtest/gtest.h>

#include "clock.h"
#include "gsmmodem/test_gsmmodemsignallevelprocessor.h"
#include "instation/instationhttpclient.h"
#include "instation/instationreporter.h"
#include "instation/test_httpclient.h"
#include "instation/test_reversesshconnector.h"
#include "configuration/test_coreconfiguration.h"
#include "configuration/test_seedconfiguration.h"
#include "configuration/iniconfiguration.h"
#include "queuedetector.h"
#include "remotedevicerecord.h"
#include "test_observer.h"
#include "test_signaturegenerator.h"
//#include "test_tcpclient.h"
#include "utils.h"


using InStation::InStationHTTPClient;
using InStation::InStationReporter;
using Model::DataContainer;
using Model::EBinType;
using Model::IniConfiguration;
using QueueDetection::QueueDetector;
using Testing::TestCoreConfiguration;
using Testing::TestGSMModemSignalLevelProcessor;
using Testing::TestHTTPClient;
using Testing::TestSeedConfiguration;
using Testing::TestObserver;
using Testing::TestReverseSSHConnector;
using Testing::TestSignatureGenerator;
//using Testing::TestTCPClient;

#define _CRLF "\x0d\x0a"


TEST(InStationReporter, notifyObservers_eSEND_STATUS_REPORT)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSSLSerialNumber("3412");
    TestSeedConfiguration seedConfiguration;
    seedConfiguration.setId(777);
    TestReverseSSHConnector reverseSSHConnector;
    TestReverseSSHConnector::TConnectionParameters parameters;
    reverseSSHConnector.setConnectionParameters(
        false, parameters);
    boost::shared_ptr<TestGSMModemSignalLevelProcessor> pSignalLevelProcessor =
        boost::shared_ptr<TestGSMModemSignalLevelProcessor>(new TestGSMModemSignalLevelProcessor());
    pSignalLevelProcessor->setup(1);
    pSignalLevelProcessor->setSignalLevel(-123);

    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());

    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;

    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    boost::shared_ptr<QueueDetector> pQueueDetector(
        new QueueDetector(deviceCollection, deviceCollectionMutex, &clock));

    boost::shared_ptr<TestHTTPClient> pRequestConfigurationClient;
    boost::shared_ptr<TestHTTPClient> pCongestionReportingClient;
    boost::shared_ptr<TestHTTPClient> pRawDeviceDetectionClient;
    boost::shared_ptr<TestHTTPClient> pAlertAndStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatusReportingClient(
        new TestHTTPClient());
    boost::shared_ptr<TestHTTPClient> pFaultReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatisticsReportingClient;

    InStationReporter instationReporter(
        coreConfiguration,
        seedConfiguration,
        reverseSSHConnector,
        pSignalLevelProcessor,
        pDataContainer,
        pQueueDetector,
        pRequestConfigurationClient,
        pCongestionReportingClient,
        pRawDeviceDetectionClient,
        pAlertAndStatusReportingClient,
        pStatusReportingClient,
        pFaultReportingClient,
        pStatisticsReportingClient,
        &clock);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
                "obfuscatingFunction=1\n"
            )));
    instationReporter.setup(pIniConfiguration);

    pStatusReportingClient->addObserver(&instationReporter);
    pStatusReportingClient->notifyObservers(TestHTTPClient::eSEND_STATUS_REPORT);


    TestHTTPClient::TStatusReportCollection statusReport(
        pStatusReportingClient->getStatusReportCollection());
    ASSERT_EQ(9, statusReport.size());
    size_t i=0;
    EXPECT_STREQ("boot", statusReport[i].name.c_str());
    EXPECT_STREQ("0", statusReport[i].value.c_str());

    EXPECT_STREQ("fv", statusReport[++i].name.c_str());
    //Do not check firmware version. It may change
    //EXPECT_STREQ("xxxx", statusReport[i].value.c_str());

    EXPECT_STREQ("sn", statusReport[++i].name.c_str());
    EXPECT_STREQ("3412", statusReport[i].value.c_str());

    EXPECT_STREQ("cv", statusReport[++i].name.c_str());
    EXPECT_STREQ("1367a74550f41c57a4d945d86b31e28", statusReport[i].value.c_str());

    EXPECT_STREQ("sl", statusReport[++i].name.c_str());
    EXPECT_STREQ("-123", statusReport[i].value.c_str());

    EXPECT_STREQ("of", statusReport[++i].name.c_str());
    EXPECT_STREQ("1", statusReport[i].value.c_str());

    EXPECT_STREQ("seed", statusReport[++i].name.c_str());
    EXPECT_STREQ("777", statusReport[i].value.c_str());

    EXPECT_STREQ("ssh", statusReport[++i].name.c_str());
    EXPECT_STREQ("closed", statusReport[i].value.c_str());

    EXPECT_STREQ("up", statusReport[++i].name.c_str());
    EXPECT_STREQ("0", statusReport[i].value.c_str());
}

TEST(InStationReporter, notifyObservers_eSEND_STATUS_REPORT_sshOpen)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    coreConfiguration.setSSLSerialNumber("5678");
    TestSeedConfiguration seedConfiguration;
    seedConfiguration.setId(777);
    TestReverseSSHConnector reverseSSHConnector;
    TestReverseSSHConnector::TConnectionParameters parameters;
    parameters.address = "localhost";
    parameters.remotePortNumber = 50000;
    reverseSSHConnector.setConnectionParameters(
        true, parameters);
    boost::shared_ptr<TestGSMModemSignalLevelProcessor> pSignalLevelProcessor =
        boost::shared_ptr<TestGSMModemSignalLevelProcessor>(new TestGSMModemSignalLevelProcessor());

    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());

    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;

    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    boost::shared_ptr<QueueDetector> pQueueDetector(
        new QueueDetector(deviceCollection, deviceCollectionMutex, &clock));

    boost::shared_ptr<TestHTTPClient> pRequestConfigurationClient;
    boost::shared_ptr<TestHTTPClient> pCongestionReportingClient;
    boost::shared_ptr<TestHTTPClient> pRawDeviceDetectionClient;
    boost::shared_ptr<TestHTTPClient> pAlertAndStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatusReportingClient(
        new TestHTTPClient());
    boost::shared_ptr<TestHTTPClient> pFaultReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatisticsReportingClient;

    InStationReporter instationReporter(
        coreConfiguration,
        seedConfiguration,
        reverseSSHConnector,
        pSignalLevelProcessor,
        pDataContainer,
        pQueueDetector,
        pRequestConfigurationClient,
        pCongestionReportingClient,
        pRawDeviceDetectionClient,
        pAlertAndStatusReportingClient,
        pStatusReportingClient,
        pFaultReportingClient,
        pStatisticsReportingClient,
        &clock);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
            )));
    instationReporter.setup(pIniConfiguration);

    pStatusReportingClient->addObserver(&instationReporter);
    pStatusReportingClient->notifyObservers(TestHTTPClient::eSEND_STATUS_REPORT);


    TestHTTPClient::TStatusReportCollection statusReport(
        pStatusReportingClient->getStatusReportCollection());
    ASSERT_EQ(9, statusReport.size());
    size_t i=0;
    EXPECT_STREQ("boot", statusReport[i].name.c_str());
    EXPECT_STREQ("0", statusReport[i].value.c_str());

    EXPECT_STREQ("fv", statusReport[++i].name.c_str());
    //Do not check firmware version. It may change
    //EXPECT_STREQ("xxxx", statusReport[i].value.c_str());

    EXPECT_STREQ("sn", statusReport[++i].name.c_str());
    EXPECT_STREQ("5678", statusReport[i].value.c_str());

    EXPECT_STREQ("cv", statusReport[++i].name.c_str());
    EXPECT_STREQ("24047bf65f3b76ce9735ce32d1be875", statusReport[i].value.c_str());

    EXPECT_STREQ("sl", statusReport[++i].name.c_str());
    EXPECT_STREQ("0", statusReport[i].value.c_str());

    EXPECT_STREQ("of", statusReport[++i].name.c_str());
    EXPECT_STREQ("0", statusReport[i].value.c_str());

    EXPECT_STREQ("seed", statusReport[++i].name.c_str());
    EXPECT_STREQ("777", statusReport[i].value.c_str());

    EXPECT_STREQ("ssh", statusReport[++i].name.c_str());
    EXPECT_STREQ("open localhost 50000", statusReport[i].value.c_str());

    EXPECT_STREQ("up", statusReport[++i].name.c_str());
    EXPECT_STREQ("0", statusReport[i].value.c_str());
}

TEST(InStationReporter, reportFault_v4)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    TestSeedConfiguration seedConfiguration;
    TestReverseSSHConnector reverseSSHConnector;
    TestReverseSSHConnector::TConnectionParameters parameters;
    boost::shared_ptr<TestGSMModemSignalLevelProcessor> pSignalLevelProcessor =
        boost::shared_ptr<TestGSMModemSignalLevelProcessor>(new TestGSMModemSignalLevelProcessor());

    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());

    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;

    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    boost::shared_ptr<QueueDetector> pQueueDetector(
        new QueueDetector(deviceCollection, deviceCollectionMutex, &clock));

    boost::shared_ptr<TestHTTPClient> pRequestConfigurationClient;
    boost::shared_ptr<TestHTTPClient> pCongestionReportingClient;
    boost::shared_ptr<TestHTTPClient> pRawDeviceDetectionClient;
    boost::shared_ptr<TestHTTPClient> pAlertAndStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pFaultReportingClient(
        new TestHTTPClient());
    boost::shared_ptr<TestHTTPClient> pStatisticsReportingClient;

    InStationReporter instationReporter(
        coreConfiguration,
        seedConfiguration,
        reverseSSHConnector,
        pSignalLevelProcessor,
        pDataContainer,
        pQueueDetector,
        pRequestConfigurationClient,
        pCongestionReportingClient,
        pRawDeviceDetectionClient,
        pAlertAndStatusReportingClient,
        pStatusReportingClient,
        pFaultReportingClient,
        pStatisticsReportingClient,
        &clock);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
                "initialStartupDelay=15\n"
            )));
    instationReporter.setup(pIniConfiguration);
    pFaultReportingClient->addObserver(&instationReporter);


    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(0, faultReport.size());
    }

    //Set all faults
    pDataContainer->getBluetoothDeviceFault().set();
    pDataContainer->getRetrieveConfigurationClientCommunicationFault().set();
    pDataContainer->getCongestionReportingClientCommunicationFault().set();
    pDataContainer->getStatusReportingClientCommunicationFault().set();
    pDataContainer->getFaultReportingClientCommunicationFault().set();
    pDataContainer->getStatisticsReportingClientCommunicationFault().set();

    //These ones are not for version 4 so they should not matter
    pDataContainer->getAlertAndStatusReportingClientCommunicationFault().set();
    pDataContainer->getRawDeviceDetectionClientCommunicationFault().set();

    {
        //This is before start-up delay time, so no fault should be reported
        clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:14.999"));
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(0, faultReport.size());
    }

    {
        //Just after start-up delay time, all latched faults should be reported now
        clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:15.001"));
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(6, faultReport.size());
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_BLUETOOTH_DEVICE, faultReport[0].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_RETRIEVE_CONFIGURATION_COMMUNICATION, faultReport[1].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_CONGESTION_REPORTING_COMMUNICATION, faultReport[2].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_STATUS_REPORTING_COMMUNICATION, faultReport[3].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_FAULT_REPORTING_COMMUNICATION, faultReport[4].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_STATISTICS_REPORTING_COMMUNICATION, faultReport[5].id);
    }

    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(0, faultReport.size());
    }

    pFaultReportingClient->notifyObservers(TestHTTPClient::eLAST_FAULT_REPORT_HAS_FAILED);

    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(6, faultReport.size());
    }

    pFaultReportingClient->notifyObservers(TestHTTPClient::eLAST_FAULT_REPORT_HAS_BEEN_SENT);

    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(0, faultReport.size());
    }

    //Now clear all the faults
    pDataContainer->getBluetoothDeviceFault().clear();
    pDataContainer->getRetrieveConfigurationClientCommunicationFault().clear();
    pDataContainer->getCongestionReportingClientCommunicationFault().clear();
    pDataContainer->getStatusReportingClientCommunicationFault().clear();
    pDataContainer->getFaultReportingClientCommunicationFault().clear();
    pDataContainer->getStatisticsReportingClientCommunicationFault().clear();

    //These ones are not for version 4 so they should not matter
    pDataContainer->getAlertAndStatusReportingClientCommunicationFault().clear();
    pDataContainer->getRawDeviceDetectionClientCommunicationFault().clear();

    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(6, faultReport.size());
    }

    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(0, faultReport.size());
    }

    pFaultReportingClient->notifyObservers(TestHTTPClient::eLAST_FAULT_REPORT_HAS_FAILED);

    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(6, faultReport.size());
    }

    pFaultReportingClient->notifyObservers(TestHTTPClient::eLAST_FAULT_REPORT_HAS_BEEN_SENT);

    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(0, faultReport.size());
    }
}

TEST(InStationReporter, reportFault_v4_full_report)
{
    //The purpose of this test is to check order of reported faults. All faults
    //should be reported in ID ascending order
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    TestSeedConfiguration seedConfiguration;
    TestReverseSSHConnector reverseSSHConnector;
    TestReverseSSHConnector::TConnectionParameters parameters;
    boost::shared_ptr<TestGSMModemSignalLevelProcessor> pSignalLevelProcessor =
        boost::shared_ptr<TestGSMModemSignalLevelProcessor>(new TestGSMModemSignalLevelProcessor());

    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());

    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;

    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    boost::shared_ptr<QueueDetector> pQueueDetector(
        new QueueDetector(deviceCollection, deviceCollectionMutex, &clock));

    boost::shared_ptr<TestHTTPClient> pRequestConfigurationClient;
    boost::shared_ptr<TestHTTPClient> pCongestionReportingClient;
    boost::shared_ptr<TestHTTPClient> pRawDeviceDetectionClient;
    boost::shared_ptr<TestHTTPClient> pAlertAndStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pFaultReportingClient(
        new TestHTTPClient());
    boost::shared_ptr<TestHTTPClient> pStatisticsReportingClient;

    InStationReporter instationReporter(
        coreConfiguration,
        seedConfiguration,
        reverseSSHConnector,
        pSignalLevelProcessor,
        pDataContainer,
        pQueueDetector,
        pRequestConfigurationClient,
        pCongestionReportingClient,
        pRawDeviceDetectionClient,
        pAlertAndStatusReportingClient,
        pStatusReportingClient,
        pFaultReportingClient,
        pStatisticsReportingClient,
        &clock);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
                "initialStartupDelay=15\n"
            )));
    instationReporter.setup(pIniConfiguration);
    pFaultReportingClient->addObserver(&instationReporter);


    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(0, faultReport.size());
    }

    //Set all faults
    pDataContainer->getBluetoothDeviceFault().set();
    pDataContainer->getRetrieveConfigurationClientCommunicationFault().set();
    pDataContainer->getRetrieveConfigurationClientResponseNotOkFault().set();
    pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault().set();
    pDataContainer->getCongestionReportingClientCommunicationFault().set();
    pDataContainer->getCongestionReportingClientResponseNotOkFault().set();
    pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getRawDeviceDetectionClientCommunicationFault().set();
    pDataContainer->getRawDeviceDetectionClientResponseNotOkFault().set();
    pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault().set();
    pDataContainer->getAlertAndStatusReportingClientCommunicationFault().set();
    pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault().set();
    pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getStatusReportingClientCommunicationFault().set();
    pDataContainer->getStatusReportingClientResponseNotOkFault().set();
    pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getFaultReportingClientCommunicationFault().set();
    pDataContainer->getFaultReportingClientResponseNotOkFault().set();
    pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getStatisticsReportingClientCommunicationFault().set();
    pDataContainer->getStatisticsReportingClientResponseNotOkFault().set();
    pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getFunctionalConfigurationSyntaxFault().set();
    pDataContainer->getFunctionalConfigurationParameterValueFault().set();
    pDataContainer->getSeedFileFault().set();
    pDataContainer->getInStationSSHUnableToConnectFault().set();
    pDataContainer->getGSMModemUnableToConnectFault().set();


    {
        clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:15.001"));
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(21, faultReport.size());

        size_t i = 0;
        EXPECT_EQ(DataContainer::eFAULT_FUNCTIONAL_CONFIGURATION_SYNTAX, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_FUNCTIONAL_CONFIGURATION_PARAMETER_ERROR, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_SEED_FILE, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_BLUETOOTH_DEVICE, faultReport[i++].id);

        EXPECT_EQ(DataContainer::eFAULT_NUMBER_RETRIEVE_CONFIGURATION_COMMUNICATION, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_CONGESTION_REPORTING_COMMUNICATION, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_STATUS_REPORTING_COMMUNICATION, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_FAULT_REPORTING_COMMUNICATION, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_STATISTICS_REPORTING_COMMUNICATION, faultReport[i++].id);

        EXPECT_EQ(DataContainer::eFAULT_NUMBER_RETRIEVE_CONFIGURATION_RESPONSE_NOT_OK, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_CONGESTION_REPORTING_RESPONSE_NOT_OK, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_STATUS_REPORTING_RESPONSE_NOT_OK, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_FAULT_REPORTING_RESPONSE_NOT_OK, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_STATISTICS_REPORTING_RESPONSE_NOT_OK, faultReport[i++].id);

        EXPECT_EQ(DataContainer::eFAULT_NUMBER_RETRIEVE_CONFIGURATION_RESPONSE_MESSAGE_BODY_ERROR, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_CONGESTION_REPORTING_RESPONSE_MESSAGE_BODY_ERROR, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_STATUS_REPORTING_RESPONSE_MESSAGE_BODY_ERROR, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_FAULT_REPORTING_RESPONSE_MESSAGE_BODY_ERROR, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_NUMBER_STATISTICS_REPORTING_RESPONSE_MESSAGE_BODY_ERROR, faultReport[i++].id);

        EXPECT_EQ(DataContainer::eFAULT_INSTATION_SSH_UNABLE_TO_CONNECT, faultReport[i++].id);
        EXPECT_EQ(DataContainer::eFAULT_GSM_MODEM_UNABLE_TO_CONNECT, faultReport[i++].id);
    }

    {
        pFaultReportingClient->clearFaultReportCollection();
        instationReporter.reportFault();
        TestHTTPClient::TFaultReportCollection faultReport(
            pFaultReportingClient->getFaultReportCollection());
        ASSERT_EQ(0, faultReport.size());
    }
}

TEST(InStationReporter, reportFault_v4_setAllFaultsAsReported)
{
    //The purpose of this test is to check if there is no fault reporting client
    // e.g. missing URL all faults are discarded (i.e. not reported)
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    TestSeedConfiguration seedConfiguration;
    TestReverseSSHConnector reverseSSHConnector;
    TestReverseSSHConnector::TConnectionParameters parameters;
    boost::shared_ptr<TestGSMModemSignalLevelProcessor> pSignalLevelProcessor =
        boost::shared_ptr<TestGSMModemSignalLevelProcessor>(new TestGSMModemSignalLevelProcessor());

    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());

    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;

    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    boost::shared_ptr<QueueDetector> pQueueDetector(
        new QueueDetector(deviceCollection, deviceCollectionMutex, &clock));

    boost::shared_ptr<TestHTTPClient> pRequestConfigurationClient;
    boost::shared_ptr<TestHTTPClient> pCongestionReportingClient;
    boost::shared_ptr<TestHTTPClient> pRawDeviceDetectionClient;
    boost::shared_ptr<TestHTTPClient> pAlertAndStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pFaultReportingClient; //No fault client
    boost::shared_ptr<TestHTTPClient> pStatisticsReportingClient;

    InStationReporter instationReporter(
        coreConfiguration,
        seedConfiguration,
        reverseSSHConnector,
        pSignalLevelProcessor,
        pDataContainer,
        pQueueDetector,
        pRequestConfigurationClient,
        pCongestionReportingClient,
        pRawDeviceDetectionClient,
        pAlertAndStatusReportingClient,
        pStatusReportingClient,
        pFaultReportingClient,
        pStatisticsReportingClient,
        &clock);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
                "initialStartupDelay=15\n"
            )));
    instationReporter.setup(pIniConfiguration);

    //Set all faults
    pDataContainer->getBluetoothDeviceFault().set();
    pDataContainer->getRetrieveConfigurationClientCommunicationFault().set();
    pDataContainer->getRetrieveConfigurationClientResponseNotOkFault().set();
    pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault().set();
    pDataContainer->getCongestionReportingClientCommunicationFault().set();
    pDataContainer->getCongestionReportingClientResponseNotOkFault().set();
    pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getRawDeviceDetectionClientCommunicationFault().set();
    pDataContainer->getRawDeviceDetectionClientResponseNotOkFault().set();
    pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault().set();
    pDataContainer->getAlertAndStatusReportingClientCommunicationFault().set();
    pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault().set();
    pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getStatusReportingClientCommunicationFault().set();
    pDataContainer->getStatusReportingClientResponseNotOkFault().set();
    pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getFaultReportingClientCommunicationFault().set();
    pDataContainer->getFaultReportingClientResponseNotOkFault().set();
    pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getStatisticsReportingClientCommunicationFault().set();
    pDataContainer->getStatisticsReportingClientResponseNotOkFault().set();
    pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault().set();
    pDataContainer->getFunctionalConfigurationSyntaxFault().set();
    pDataContainer->getFunctionalConfigurationParameterValueFault().set();
    pDataContainer->getSeedFileFault().set();
    pDataContainer->getInStationSSHUnableToConnectFault().set();
    pDataContainer->getGSMModemUnableToConnectFault().set();

    EXPECT_FALSE(pDataContainer->getFunctionalConfigurationParameterValueFault().wasReported());
    EXPECT_FALSE(pDataContainer->getFunctionalConfigurationSyntaxFault().wasReported());
    EXPECT_FALSE(pDataContainer->getSeedFileFault().wasReported());

    EXPECT_FALSE(pDataContainer->getBluetoothDeviceFault().wasReported());

    EXPECT_FALSE(pDataContainer->getRetrieveConfigurationClientCommunicationFault().wasReported());
    EXPECT_FALSE(pDataContainer->getRetrieveConfigurationClientResponseNotOkFault().wasReported());
    EXPECT_FALSE(pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_FALSE(pDataContainer->getCongestionReportingClientCommunicationFault().wasReported());
    EXPECT_FALSE(pDataContainer->getCongestionReportingClientResponseNotOkFault().wasReported());
    EXPECT_FALSE(pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_FALSE(pDataContainer->getRawDeviceDetectionClientCommunicationFault().wasReported());
    EXPECT_FALSE(pDataContainer->getRawDeviceDetectionClientResponseNotOkFault().wasReported());
    EXPECT_FALSE(pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_FALSE(pDataContainer->getAlertAndStatusReportingClientCommunicationFault().wasReported());
    EXPECT_FALSE(pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault().wasReported());
    EXPECT_FALSE(pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_FALSE(pDataContainer->getStatusReportingClientCommunicationFault().wasReported());
    EXPECT_FALSE(pDataContainer->getStatusReportingClientResponseNotOkFault().wasReported());
    EXPECT_FALSE(pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_FALSE(pDataContainer->getFaultReportingClientCommunicationFault().wasReported());
    EXPECT_FALSE(pDataContainer->getFaultReportingClientResponseNotOkFault().wasReported());
    EXPECT_FALSE(pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_FALSE(pDataContainer->getStatisticsReportingClientCommunicationFault().wasReported());
    EXPECT_FALSE(pDataContainer->getStatisticsReportingClientResponseNotOkFault().wasReported());
    EXPECT_FALSE(pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_FALSE(pDataContainer->getInStationSSHUnableToConnectFault().wasReported());
    EXPECT_FALSE(pDataContainer->getGSMModemUnableToConnectFault().wasReported());


    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:15.001"));
    instationReporter.reportFault();

    EXPECT_TRUE(pDataContainer->getFunctionalConfigurationParameterValueFault().wasReported());
    EXPECT_TRUE(pDataContainer->getFunctionalConfigurationSyntaxFault().wasReported());
    EXPECT_TRUE(pDataContainer->getSeedFileFault().wasReported());

    EXPECT_TRUE(pDataContainer->getBluetoothDeviceFault().wasReported());

    EXPECT_TRUE(pDataContainer->getRetrieveConfigurationClientCommunicationFault().wasReported());
    EXPECT_TRUE(pDataContainer->getRetrieveConfigurationClientResponseNotOkFault().wasReported());
    EXPECT_TRUE(pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_TRUE(pDataContainer->getCongestionReportingClientCommunicationFault().wasReported());
    EXPECT_TRUE(pDataContainer->getCongestionReportingClientResponseNotOkFault().wasReported());
    EXPECT_TRUE(pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_TRUE(pDataContainer->getRawDeviceDetectionClientCommunicationFault().wasReported());
    EXPECT_TRUE(pDataContainer->getRawDeviceDetectionClientResponseNotOkFault().wasReported());
    EXPECT_TRUE(pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_TRUE(pDataContainer->getAlertAndStatusReportingClientCommunicationFault().wasReported());
    EXPECT_TRUE(pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault().wasReported());
    EXPECT_TRUE(pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_TRUE(pDataContainer->getStatusReportingClientCommunicationFault().wasReported());
    EXPECT_TRUE(pDataContainer->getStatusReportingClientResponseNotOkFault().wasReported());
    EXPECT_TRUE(pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_TRUE(pDataContainer->getFaultReportingClientCommunicationFault().wasReported());
    EXPECT_TRUE(pDataContainer->getFaultReportingClientResponseNotOkFault().wasReported());
    EXPECT_TRUE(pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_TRUE(pDataContainer->getStatisticsReportingClientCommunicationFault().wasReported());
    EXPECT_TRUE(pDataContainer->getStatisticsReportingClientResponseNotOkFault().wasReported());
    EXPECT_TRUE(pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault().wasReported());

    EXPECT_TRUE(pDataContainer->getInStationSSHUnableToConnectFault().wasReported());
    EXPECT_TRUE(pDataContainer->getGSMModemUnableToConnectFault().wasReported());
}

TEST(InStationReporter, sendCongestionReport_v4)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    TestSeedConfiguration seedConfiguration;
    seedConfiguration.setId(777);
    TestReverseSSHConnector reverseSSHConnector;
    TestReverseSSHConnector::TConnectionParameters parameters;
    boost::shared_ptr<TestGSMModemSignalLevelProcessor> pSignalLevelProcessor =
        boost::shared_ptr<TestGSMModemSignalLevelProcessor>(new TestGSMModemSignalLevelProcessor());

    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());

    //QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    Model::TRemoteDeviceRecord record1(1,0);
    Model::TRemoteDeviceRecord record2(2,0);
    Model::TRemoteDeviceRecord record3(3,0);
    Model::TRemoteDeviceRecord record4(4,0);

    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    boost::shared_ptr<QueueDetector> pQueueDetector(
        new QueueDetector(
            pDataContainer->getRemoteDeviceCollection(),
            pDataContainer->getRemoteDeviceCollectionMutex(),
            &clock));
    const unsigned int inquiryScanDurationInSeconds = 10;
    const unsigned int dropOutScanCycleThresholdInSeconds = 200;
    const unsigned int freeFlowThresholdInSeconds = 10;
    const unsigned int moderateFlowThresholdInSeconds = 20;
    const unsigned int slowFlowThresholdInSeconds = 30;
    const unsigned int veryslowFlowThresholdInSeconds = 40;
    const Model::EBinType queueAlertThresholdBin = Model::eBIN_TYPE_FREE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 2;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    pQueueDetector->setup(
        inquiryScanDurationInSeconds,
        dropOutScanCycleThresholdInSeconds,
        freeFlowThresholdInSeconds,
        moderateFlowThresholdInSeconds,
        slowFlowThresholdInSeconds,
        veryslowFlowThresholdInSeconds,
        queueAlertThresholdBin,
        queueAlertThresholdDetectionNumber,
        queueClearanceThresholdDetectionNumber,
        queueDetectionStartupIntervalInSeconds);
    EXPECT_TRUE(pQueueDetector->isConfigured());


    boost::shared_ptr<TestHTTPClient> pRequestConfigurationClient;
    boost::shared_ptr<TestHTTPClient> pCongestionReportingClient(
        new TestHTTPClient());
    boost::shared_ptr<TestHTTPClient> pRawDeviceDetectionClient;
    boost::shared_ptr<TestHTTPClient> pAlertAndStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pFaultReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatisticsReportingClient;

    InStationReporter instationReporter(
        coreConfiguration,
        seedConfiguration,
        reverseSSHConnector,
        pSignalLevelProcessor,
        pDataContainer,
        pQueueDetector,
        pRequestConfigurationClient,
        pCongestionReportingClient,
        pRawDeviceDetectionClient,
        pAlertAndStatusReportingClient,
        pStatusReportingClient,
        pFaultReportingClient,
        pStatisticsReportingClient,
        &clock);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
            )));
    instationReporter.setup(pIniConfiguration);

    pCongestionReportingClient->addObserver(&instationReporter);

    //Check if components are populated correctly
    {
        pCongestionReportingClient->clearCongestionReport();
        instationReporter.sendCongestionReport();

        QueueDetection::CongestionReport congestionReport(
            pCongestionReportingClient->getCongestionReport());
        EXPECT_EQ(0, congestionReport.numberOfDevicesInFreeFlowBin);
        EXPECT_EQ(0, congestionReport.numberOfDevicesInModerateFlowBin);
        EXPECT_EQ(0, congestionReport.numberOfDevicesInSlowFlowBin);
        EXPECT_EQ(0, congestionReport.numberOfDevicesInVerySlowFlowBin);
        EXPECT_EQ(0, congestionReport.numberOfDevicesInStaticFlowBin);
        EXPECT_EQ(QueueDetection::eQUEUE_PRESENCE_STATE_NO_QUEUE, congestionReport.queuePresenceState);
    }

    {
       //Report the first device. Only one bin should be affected
        Model::TRemoteDeviceRecord record(1,0);
        record.firstObservationTime = 1;
        record.referencePointObservationTime = 5;
        record.lastObservationTime = 9;
        pDataContainer->updateRemoteDeviceRecord(record);
        clock.advanceUniversalTimeBySeconds(10);
        pQueueDetector->updateDevicesFromRawTime(clock.getUniversalTime());

        pCongestionReportingClient->clearCongestionReport();
        instationReporter.sendCongestionReport();

        QueueDetection::CongestionReport congestionReport(
            pCongestionReportingClient->getCongestionReport());
        EXPECT_EQ(1, congestionReport.numberOfDevicesInFreeFlowBin);
        EXPECT_EQ(0, congestionReport.numberOfDevicesInModerateFlowBin);
        EXPECT_EQ(0, congestionReport.numberOfDevicesInSlowFlowBin);
        EXPECT_EQ(0, congestionReport.numberOfDevicesInVerySlowFlowBin);
        EXPECT_EQ(0, congestionReport.numberOfDevicesInStaticFlowBin);
        EXPECT_EQ(QueueDetection::eQUEUE_PRESENCE_STATE_NO_QUEUE, congestionReport.queuePresenceState);
    }

}

TEST(InStationReporter, sendStatisticsReportBrief)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    TestSeedConfiguration seedConfiguration;
    seedConfiguration.setId(777);
    TestReverseSSHConnector reverseSSHConnector;
    TestReverseSSHConnector::TConnectionParameters parameters;
    boost::shared_ptr<TestGSMModemSignalLevelProcessor> pSignalLevelProcessor =
        boost::shared_ptr<TestGSMModemSignalLevelProcessor>(new TestGSMModemSignalLevelProcessor());

    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());

    //QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    Model::TRemoteDeviceRecord record1(1,0);
    Model::TRemoteDeviceRecord record2(2,0);
    Model::TRemoteDeviceRecord record3(3,0);
    Model::TRemoteDeviceRecord record4(4,0);

    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    boost::shared_ptr<QueueDetector> pQueueDetector(
        new QueueDetector(
            pDataContainer->getRemoteDeviceCollection(),
            pDataContainer->getRemoteDeviceCollectionMutex(),
            &clock));
    const unsigned int inquiryScanDurationInSeconds = 10;
    const unsigned int dropOutScanCycleThresholdInSeconds = 200;
    const unsigned int freeFlowThresholdInSeconds = 10;
    const unsigned int moderateFlowThresholdInSeconds = 20;
    const unsigned int slowFlowThresholdInSeconds = 30;
    const unsigned int veryslowFlowThresholdInSeconds = 40;
    const Model::EBinType queueAlertThresholdBin = Model::eBIN_TYPE_FREE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 2;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    pQueueDetector->setup(
        inquiryScanDurationInSeconds,
        dropOutScanCycleThresholdInSeconds,
        freeFlowThresholdInSeconds,
        moderateFlowThresholdInSeconds,
        slowFlowThresholdInSeconds,
        veryslowFlowThresholdInSeconds,
        queueAlertThresholdBin,
        queueAlertThresholdDetectionNumber,
        queueClearanceThresholdDetectionNumber,
        queueDetectionStartupIntervalInSeconds);
    EXPECT_TRUE(pQueueDetector->isConfigured());


    boost::shared_ptr<TestHTTPClient> pRequestConfigurationClient;
    boost::shared_ptr<TestHTTPClient> pCongestionReportingClient;
    boost::shared_ptr<TestHTTPClient> pRawDeviceDetectionClient;
    boost::shared_ptr<TestHTTPClient> pAlertAndStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pFaultReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatisticsReportingClient(
        new TestHTTPClient());

    InStationReporter instationReporter(
        coreConfiguration,
        seedConfiguration,
        reverseSSHConnector,
        pSignalLevelProcessor,
        pDataContainer,
        pQueueDetector,
        pRequestConfigurationClient,
        pCongestionReportingClient,
        pRawDeviceDetectionClient,
        pAlertAndStatusReportingClient,
        pStatusReportingClient,
        pFaultReportingClient,
        pStatisticsReportingClient,
        &clock);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
            )));
    instationReporter.setup(pIniConfiguration);

    pStatisticsReportingClient->addObserver(&instationReporter);


    pDataContainer->getNonPendingRemoteDeviceCollection().push_back(
        Model::TRemoteDeviceRecord(0x123456789012, 5432));
    pDataContainer->getRemoteDeviceCollection()[0x123456789013] =
        Model::TRemoteDeviceRecord(0x123456789013, 5433);

    instationReporter.sendStatisticsReport();

    TestHTTPClient::TStatisticsReportCollection statisticsReport(
        pStatisticsReportingClient->getStatisticsReportCollection());
    ASSERT_EQ(1, statisticsReport.size());
    EXPECT_EQ(0x123456789012, statisticsReport[0].deviceIdentifier);
    EXPECT_EQ(5432, statisticsReport[0].cod);
}

TEST(InStationReporter, sendStatisticsReportFull)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
    TestSeedConfiguration seedConfiguration;
    seedConfiguration.setId(777);
    TestReverseSSHConnector reverseSSHConnector;
    TestReverseSSHConnector::TConnectionParameters parameters;
    boost::shared_ptr<TestGSMModemSignalLevelProcessor> pSignalLevelProcessor =
        boost::shared_ptr<TestGSMModemSignalLevelProcessor>(new TestGSMModemSignalLevelProcessor());

    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());

    //QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    Model::TRemoteDeviceRecord record1(1,0);
    Model::TRemoteDeviceRecord record2(2,0);
    Model::TRemoteDeviceRecord record3(3,0);
    Model::TRemoteDeviceRecord record4(4,0);

    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    boost::shared_ptr<QueueDetector> pQueueDetector(
        new QueueDetector(
            pDataContainer->getRemoteDeviceCollection(),
            pDataContainer->getRemoteDeviceCollectionMutex(),
            &clock));
    const unsigned int inquiryScanDurationInSeconds = 10;
    const unsigned int dropOutScanCycleThresholdInSeconds = 200;
    const unsigned int freeFlowThresholdInSeconds = 10;
    const unsigned int moderateFlowThresholdInSeconds = 20;
    const unsigned int slowFlowThresholdInSeconds = 30;
    const unsigned int veryslowFlowThresholdInSeconds = 40;
    const Model::EBinType queueAlertThresholdBin = Model::eBIN_TYPE_FREE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 2;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    pQueueDetector->setup(
        inquiryScanDurationInSeconds,
        dropOutScanCycleThresholdInSeconds,
        freeFlowThresholdInSeconds,
        moderateFlowThresholdInSeconds,
        slowFlowThresholdInSeconds,
        veryslowFlowThresholdInSeconds,
        queueAlertThresholdBin,
        queueAlertThresholdDetectionNumber,
        queueClearanceThresholdDetectionNumber,
        queueDetectionStartupIntervalInSeconds);
    EXPECT_TRUE(pQueueDetector->isConfigured());


    boost::shared_ptr<TestHTTPClient> pRequestConfigurationClient;
    boost::shared_ptr<TestHTTPClient> pCongestionReportingClient;
    boost::shared_ptr<TestHTTPClient> pRawDeviceDetectionClient;
    boost::shared_ptr<TestHTTPClient> pAlertAndStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatusReportingClient;
    boost::shared_ptr<TestHTTPClient> pFaultReportingClient;
    boost::shared_ptr<TestHTTPClient> pStatisticsReportingClient(
        new TestHTTPClient());

    InStationReporter instationReporter(
        coreConfiguration,
        seedConfiguration,
        reverseSSHConnector,
        pSignalLevelProcessor,
        pDataContainer,
        pQueueDetector,
        pRequestConfigurationClient,
        pCongestionReportingClient,
        pRawDeviceDetectionClient,
        pAlertAndStatusReportingClient,
        pStatusReportingClient,
        pFaultReportingClient,
        pStatisticsReportingClient,
        &clock);

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
                "statisticsReportContents=full\n"
            )));
    instationReporter.setup(pIniConfiguration);

    pStatisticsReportingClient->addObserver(&instationReporter);


    pDataContainer->getNonPendingRemoteDeviceCollection().push_back(
        Model::TRemoteDeviceRecord(0x123456789012, 5432));
    pDataContainer->getRemoteDeviceCollection()[0x123456789013] =
        Model::TRemoteDeviceRecord(0x123456789013, 5433);

    instationReporter.sendStatisticsReport();

    TestHTTPClient::TStatisticsReportCollection statisticsReport(
        pStatisticsReportingClient->getStatisticsReportCollection());
    ASSERT_EQ(2, statisticsReport.size());
    EXPECT_EQ(0x123456789012, statisticsReport[0].deviceIdentifier);
    EXPECT_EQ(5432, statisticsReport[0].cod);
    EXPECT_EQ(0x123456789013, statisticsReport[1].deviceIdentifier);
    EXPECT_EQ(5433, statisticsReport[1].cod);
}
