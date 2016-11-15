#include "stdafx.h"
#include <gtest/gtest.h>

#include "instation/periodicallysendcongestionreporttask.h"

#include "clock.h"
#include "datacontainer.h"
#include "configuration/iniconfiguration.h"
#include "configuration/test_coreconfiguration.h"
#include "configuration/test_seedconfiguration.h"
#include "gsmmodem/test_gsmmodemsignallevelprocessor.h"
#include "instation/instationreporter.h"
#include "instation/test_httpclient.h"
#include "instation/test_reversesshconnector.h"
#include "queuedetector.h"
#include "test_observer.h"


using InStation::InStationReporter;
using InStation::PeriodicallySendCongestionReportTask;
using Model::DataContainer;
using Model::IniConfiguration;
using QueueDetection::QueueDetector;
using Testing::TestCoreConfiguration;
using Testing::TestGSMModemSignalLevelProcessor;
using Testing::TestObserver;
using Testing::TestReverseSSHConnector;
using Testing::TestSeedConfiguration;
using Testing::TestHTTPClient;


TEST(PeriodicallySendCongestionReportTask, statisticalProperties)
{
    TestCoreConfiguration coreConfiguration;
    coreConfiguration.setMajorCoreConfigurationVersion(4);
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

    boost::shared_ptr<InStationReporter> pInstationReporter(new InStationReporter(
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
        &clock));

    boost::shared_ptr<IniConfiguration> pIniConfiguration =
        boost::shared_ptr<IniConfiguration>(
            new IniConfiguration(std::string(
                "urlJourneyTimesReporting=http://localhost/x\n"
                "obfuscatingFunction=1\n"
            )));
    pInstationReporter->setup(pIniConfiguration);

    pStatusReportingClient->addObserver(&*pInstationReporter);
    pStatusReportingClient->notifyObservers(TestHTTPClient::eSEND_STATUS_REPORT);

    PeriodicallySendCongestionReportTask task(pInstationReporter, &clock);
    TestObserver testObserver;
    std::vector<int>& testObserverIndexCollection = testObserver.getIndexCollection();
    task.addObserver(&testObserver);

    EXPECT_FALSE(task.isRunning());

    //Start the task with invalid parameters
    task.start(0, 0);
    EXPECT_FALSE(task.isRunning());

    //Start the task with invalid parameters (the same but now the delay is non-zero)
    task.initialise();
    task.start(0, 15);
    EXPECT_FALSE(task.isRunning());

    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    EXPECT_EQ(0, testObserverIndexCollection.size());

    //Now start the task
    testObserverIndexCollection.clear();
    task.start(10, 15);
    ASSERT_EQ(1, testObserverIndexCollection.size());
    EXPECT_TRUE(std::find(
            testObserverIndexCollection.begin(),
            testObserverIndexCollection.end(),
            PeriodicallySendCongestionReportTask::eSTARTING) != testObserverIndexCollection.end());
    EXPECT_TRUE(task.isRunning());

    //Restart the task with the same parameter - nothing changes, so no event
    testObserverIndexCollection.clear();
    task.start(10, 15);
    EXPECT_EQ(0, testObserverIndexCollection.size());
    EXPECT_TRUE(task.isRunning());

    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    EXPECT_EQ(0, testObserverIndexCollection.size());

    //Now advance the timer by a few seconds, no expected events
    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:09.999"));
    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    EXPECT_EQ(0, testObserverIndexCollection.size());

    //Now advance the timer by a few seconds, no expected events
    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:14.999"));
    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    EXPECT_EQ(0, testObserverIndexCollection.size());

    //Now advance the timer by a bit, expecting event
    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:15.001"));
    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    ASSERT_EQ(1, testObserverIndexCollection.size());
    EXPECT_TRUE(std::find(
            testObserverIndexCollection.begin(),
            testObserverIndexCollection.end(),
            PeriodicallySendCongestionReportTask::eSENDING_CONGESTION_REPORT) != testObserverIndexCollection.end());
    EXPECT_TRUE(task.isRunning());

    task.start(10, 20);

    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:24.999"));
    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    EXPECT_EQ(0, testObserverIndexCollection.size());

    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:25.001"));
    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    ASSERT_EQ(1, testObserverIndexCollection.size());
    EXPECT_TRUE(std::find(
            testObserverIndexCollection.begin(),
            testObserverIndexCollection.end(),
            PeriodicallySendCongestionReportTask::eSENDING_CONGESTION_REPORT) != testObserverIndexCollection.end());

    testObserverIndexCollection.clear();
    for (int i=0; i<10; ++i)
    {
        task.perform();
        EXPECT_EQ(0, testObserverIndexCollection.size());
    }

    task.stop();
    EXPECT_FALSE(task.isRunning());
    ASSERT_EQ(1, testObserverIndexCollection.size());
    EXPECT_TRUE(std::find(
            testObserverIndexCollection.begin(),
            testObserverIndexCollection.end(),
            PeriodicallySendCongestionReportTask::eSTOPPING) != testObserverIndexCollection.end());

    task.shutdown();
    EXPECT_FALSE(task.isRunning());
}
