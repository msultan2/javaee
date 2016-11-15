#include "stdafx.h"
#include <gtest/gtest.h>

#include "bluetooth/periodicallyremovenonpresentdevicestask.h"

#include "clock.h"
#include "datacontainer.h"
#include "test_observer.h"

using BlueTooth::PeriodicallyRemoveNonPresentDevicesTask;
using Model::DataContainer;
using Testing::TestObserver;


TEST(PeriodicallyRemoveNonPresentDevicesTask, statisticalProperties)
{
    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);

    unsigned int actionPeriodInSeconds = 10;
    unsigned int absenceThresholdInSeconds = 20;
    bool moveToNonPresentCollectionForReporting = true;


    PeriodicallyRemoveNonPresentDevicesTask task(pDataContainer, &clock);
    TestObserver testObserver;
    std::vector<int>& testObserverIndexCollection = testObserver.getIndexCollection();
    task.addObserver(&testObserver);

    EXPECT_FALSE(task.isRunning());

    task.start(0, 0, 0);
    EXPECT_FALSE(task.isRunning());

    task.initialise();
    task.start(0, 0, 0);
    EXPECT_FALSE(task.isRunning());

    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    EXPECT_EQ(0, testObserverIndexCollection.size());

    //Now start the task
    testObserverIndexCollection.clear();
    task.start(actionPeriodInSeconds, absenceThresholdInSeconds, moveToNonPresentCollectionForReporting);
    ASSERT_EQ(1, testObserverIndexCollection.size());
    EXPECT_TRUE(std::find(
            testObserverIndexCollection.begin(),
            testObserverIndexCollection.end(),
            PeriodicallyRemoveNonPresentDevicesTask::eSTARTING) != testObserverIndexCollection.end());
    EXPECT_TRUE(task.isRunning());

    //Restart the task with the same parameter - nothing changes, so no event
    testObserverIndexCollection.clear();
    task.start(actionPeriodInSeconds, absenceThresholdInSeconds, moveToNonPresentCollectionForReporting);
    EXPECT_EQ(0, testObserverIndexCollection.size());
    EXPECT_TRUE(task.isRunning());

    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    EXPECT_EQ(0, testObserverIndexCollection.size());

    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:09.999"));
    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    EXPECT_EQ(0, testObserverIndexCollection.size());

    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:10.001"));
    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    ASSERT_EQ(1, testObserverIndexCollection.size());
    EXPECT_TRUE(std::find(
            testObserverIndexCollection.begin(),
            testObserverIndexCollection.end(),
            PeriodicallyRemoveNonPresentDevicesTask::eREMOVING_NON_PRESENT_REMOTE_DEVICES) != testObserverIndexCollection.end());
    EXPECT_TRUE(task.isRunning());


    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:19.999"));
    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    EXPECT_EQ(0, testObserverIndexCollection.size());

    clock.setUniversalTime(pt::time_from_string("1970-01-01 00:00:20.001"));
    testObserverIndexCollection.clear();
    EXPECT_EQ(0, testObserverIndexCollection.size());
    task.perform();
    ASSERT_EQ(1, testObserverIndexCollection.size());
    EXPECT_TRUE(std::find(
            testObserverIndexCollection.begin(),
            testObserverIndexCollection.end(),
            PeriodicallyRemoveNonPresentDevicesTask::eREMOVING_NON_PRESENT_REMOTE_DEVICES) != testObserverIndexCollection.end());

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
            PeriodicallyRemoveNonPresentDevicesTask::eSTOPPING) != testObserverIndexCollection.end());

    task.shutdown();
    EXPECT_FALSE(task.isRunning());
}
