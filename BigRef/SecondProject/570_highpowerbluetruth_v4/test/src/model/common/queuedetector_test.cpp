#include "stdafx.h"
#include <gtest/gtest.h>

#include "queuedetector.h"

#include "clock.h"
#include "datacontainer.h"


using namespace QueueDetection;
using QueueDetection::QueueDetector;
using QueueDetection::TCongestionReport;
using Model::DataContainer;
using Model::EBinType;


TEST(Model, getBinTypeName)
{
    //These tests exist only to bypass coverage statistics
    Model::getBinTypeName(Model::eBIN_TYPE_UNDEFINED);
    Model::getBinTypeName(Model::eBIN_TYPE_FREE_FLOW);
    Model::getBinTypeName(Model::eBIN_TYPE_MODERATE_FLOW);
    Model::getBinTypeName(Model::eBIN_TYPE_SLOW_FLOW);
    Model::getBinTypeName(Model::eBIN_TYPE_VERY_SLOW_FLOW);
    Model::getBinTypeName(Model::eBIN_TYPE_STATIC_FLOW);
}

TEST(QueueDetector, setup)
{
    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(deviceCollection, deviceCollectionMutex, &clock);

    const unsigned int inquiryScanDurationInSeconds = 1;
    const unsigned int dropOutScanCycleThresholdInSeconds = 1;
    const unsigned int freeFlowThresholdInSeconds = 5;
    const unsigned int moderateFlowThresholdInSeconds = 10;
    const unsigned int slowFlowThresholdInSeconds = 20;
    const unsigned int veryslowFlowThresholdInSeconds = 30;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_FREE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 5;
    const unsigned int queueClearanceThresholdDetectionNumber = 1;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());
}

TEST(QueueDetector, updateDevicesFreeFlow)
{
    //Setup queue detector.
    //Report first one device, then four devices (including the first one) multiple times.
    //Check how the detections propagate through the bins.
    //Start reporting no devices and check how this fact afects the bins

    //1) Setup
    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(deviceCollection, deviceCollectionMutex, &clock);

    const unsigned int inquiryScanDurationInSeconds = 1;
    const unsigned int dropOutScanCycleThresholdInSeconds = 2;
    const unsigned int freeFlowThresholdInSeconds = 1;
    const unsigned int moderateFlowThresholdInSeconds = 1;
    const unsigned int slowFlowThresholdInSeconds = 1;
    const unsigned int veryslowFlowThresholdInSeconds = 1;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_FREE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 2;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());

    Model::TRemoteDeviceRecord record1(1,0);
    Model::TRemoteDeviceRecord record2(2,0);
    Model::TRemoteDeviceRecord record3(3,0);
    Model::TRemoteDeviceRecord record4(4,0);

    { //Report empty collection. Check that nothing changes
        deviceCollection.clear();
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report the first device. Only one bin should be affected
        deviceCollection.clear();
        deviceCollection[record1.address] = record1;
        deviceCollection[record1.address].numberOfScans++;
        queueDetector.updateDevices();
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        //Report other devices multiple times and check how this update propagates through the bins
        deviceCollection[record2.address] = record2;
        deviceCollection[record3.address] = record3;
        deviceCollection[record4.address] = record4;
        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;

        queueDetector.updateDevices();
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

       //Report empty collection and see how the queue clearance is detected
        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        deviceCollection.clear();
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report other devices multiple times and check how this affects the report.
        deviceCollection.clear();
        deviceCollection[record1.address] = record1;
        deviceCollection[record2.address] = record2;
        deviceCollection[record3.address] = record3;
        deviceCollection[record4.address] = record4;

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        for (int i=0; i<100; ++i)
        {
            queueDetector.updateDevices();
            EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
        }
    }
}

TEST(QueueDetector, updateDevicesModerateFlow)
{
    //Setup queue detector.
    //Report first one device, then four devices (including the first one) multiple times.
    //Check how the detections propagate through the bins.
    //Start reporting no devices and check how this fact afects the bins

    //1) Setup
    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(deviceCollection, deviceCollectionMutex, &clock);

    const unsigned int inquiryScanDurationInSeconds = 1;
    const unsigned int dropOutScanCycleThresholdInSeconds = 1;
    const unsigned int freeFlowThresholdInSeconds = 1;
    const unsigned int moderateFlowThresholdInSeconds = 1;
    const unsigned int slowFlowThresholdInSeconds = 1;
    const unsigned int veryslowFlowThresholdInSeconds = 1;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_MODERATE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 1;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());

    Model::TRemoteDeviceRecord record1(1,0);
    Model::TRemoteDeviceRecord record2(2,0);
    Model::TRemoteDeviceRecord record3(3,0);
    Model::TRemoteDeviceRecord record4(4,0);

    { //Report empty collection. Check that nothing changes
        deviceCollection.clear();
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report the first device. Only one bin should be affected
        deviceCollection[record1.address] = record1;
        ++deviceCollection[record1.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

      //Report other devices multiple times and check how this update propagates through the bins
        deviceCollection[record2.address] = record2;
        deviceCollection[record3.address] = record3;
        deviceCollection[record4.address] = record4;
        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
    }

    { //Report empty collection and see how the queue clearance is detected
        deviceCollection.clear();

        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }
}

TEST(QueueDetector, updateDevicesSlowFlow)
{
    //Setup queue detector.
    //Report first one device, then four devices (including the first one) multiple times.
    //Check how the detections propagate through the bins.
    //Start reporting no devices and check how this fact afects the bins

    //1) Setup
    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(deviceCollection, deviceCollectionMutex, &clock);

    const unsigned int inquiryScanDurationInSeconds = 1;
    const unsigned int dropOutScanCycleThresholdInSeconds = 1;
    const unsigned int freeFlowThresholdInSeconds = 1;
    const unsigned int moderateFlowThresholdInSeconds = 1;
    const unsigned int slowFlowThresholdInSeconds = 1;
    const unsigned int veryslowFlowThresholdInSeconds = 1;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_SLOW_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 1;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());

    Model::TRemoteDeviceRecord record1(1,0);
    Model::TRemoteDeviceRecord record2(2,0);
    Model::TRemoteDeviceRecord record3(3,0);
    Model::TRemoteDeviceRecord record4(4,0);

    { //Report empty collection. Check that nothing changes
        deviceCollection.clear();
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report the first device. Only one bin should be affected
        deviceCollection[record1.address] = record1;
        ++deviceCollection[record1.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

      //Report other devices multiple times and check how this update propagates through the bins
        deviceCollection[record2.address] = record2;
        deviceCollection[record3.address] = record3;
        deviceCollection[record4.address] = record4;
        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;

        queueDetector.updateDevices();
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
    }

    { //Report empty collection and see how the queue clearance is detected
        deviceCollection.clear();

        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }
}

TEST(QueueDetector, updateDevicesVerySlowFlow)
{
    //Setup queue detector.
    //Report first one device, then four devices (including the first one) multiple times.
    //Check how the detections propagate through the bins.
    //Start reporting no devices and check how this fact afects the bins

    //1) Setup
    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(deviceCollection, deviceCollectionMutex, &clock);

    const unsigned int inquiryScanDurationInSeconds = 1;
    const unsigned int dropOutScanCycleThresholdInSeconds = 1;
    const unsigned int freeFlowThresholdInSeconds = 1;
    const unsigned int moderateFlowThresholdInSeconds = 1;
    const unsigned int slowFlowThresholdInSeconds = 1;
    const unsigned int veryslowFlowThresholdInSeconds = 1;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_VERY_SLOW_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 1;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());

    Model::TRemoteDeviceRecord record1(1,0);
    Model::TRemoteDeviceRecord record2(2,0);
    Model::TRemoteDeviceRecord record3(3,0);
    Model::TRemoteDeviceRecord record4(4,0);

    { //Report empty collection. Check that nothing changes
        deviceCollection.clear();
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report the first device. Only one bin should be affected
        deviceCollection[record1.address] = record1;
        ++deviceCollection[record1.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

      //Report other devices multiple times and check how this update propagates through the bins
        deviceCollection[record2.address] = record2;
        deviceCollection[record3.address] = record3;
        deviceCollection[record4.address] = record4;
        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
    }

    { //Report empty collection and see how the queue clearance is detected
        deviceCollection.clear();

        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }
}

TEST(QueueDetector, updateDevicesStaticFlow)
{
    //Setup queue detector.
    //Report first one device, then four devices (including the first one) multiple times.
    //Check how the detections propagate through the bins.
    //Start reporting no devices and check how this fact afects the bins

    //1) Setup
    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(deviceCollection, deviceCollectionMutex, &clock);

    const unsigned int inquiryScanDurationInSeconds = 1;
    const unsigned int dropOutScanCycleThresholdInSeconds = 1;
    const unsigned int freeFlowThresholdInSeconds = 1;
    const unsigned int moderateFlowThresholdInSeconds = 1;
    const unsigned int slowFlowThresholdInSeconds = 1;
    const unsigned int veryslowFlowThresholdInSeconds = 1;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_STATIC_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 2;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());

    Model::TRemoteDeviceRecord record1(1,0);
    Model::TRemoteDeviceRecord record2(2,0);
    Model::TRemoteDeviceRecord record3(3,0);
    Model::TRemoteDeviceRecord record4(4,0);

    { //Report empty collection. Check that nothing changes
        deviceCollection.clear();
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report the first device. Only one bin should be affected
        deviceCollection[record1.address] = record1;
        ++deviceCollection[record1.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

      //Report other devices multiple times and check how this update propagates through the bins
        deviceCollection[record2.address] = record2;
        deviceCollection[record3.address] = record3;
        deviceCollection[record4.address] = record4;
        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
    }

    { //Report empty collection and see how the queue clearance is detected
        deviceCollection.clear();

        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }
}

TEST(QueueDetector, updateDevicesFromRawTimeFreeFlow)
{
    //Setup queue detector.
    //Report first one device, then four devices (including the first one) multiple times.
    //Check how the detections propagate through the bins.
    //Start reporting no devices and check how this fact afects the bins

    //1) Setup
    DataContainer dataContainer;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(
        dataContainer.getRemoteDeviceCollection(),
        deviceCollectionMutex,
        &clock);

    const unsigned int inquiryScanDurationInSeconds = 10;
    const unsigned int dropOutScanCycleThresholdInSeconds = 20;
    const unsigned int freeFlowThresholdInSeconds = 10;
    const unsigned int moderateFlowThresholdInSeconds = 20;
    const unsigned int slowFlowThresholdInSeconds = 30;
    const unsigned int veryslowFlowThresholdInSeconds = 40;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_FREE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 2;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());

    { //Report empty collection. Check that nothing changes
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report the first device. Only one bin should be affected
        Model::TRemoteDeviceRecord record(1,0);
        record.firstObservationTime = 1;
        record.lastObservationTime = 9;
        dataContainer.updateRemoteDeviceRecord(record);
        clock += 10;
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report other devices multiple times and check how this update propagates through the bins
        Model::TRemoteDeviceRecord record1(1,0);
        record1.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record1.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record1);

        Model::TRemoteDeviceRecord record2(2,0);
        record2.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record2.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record2);

        Model::TRemoteDeviceRecord record3(3,0);
        record3.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record3.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record3);

        Model::TRemoteDeviceRecord record4(4,0);
        record4.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record4.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record4);

        clock += 10;
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        //Now one should go into UNDEFINED bin (above STATIC bin)
        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(9, queueDetector.getQueuePresenceState());
    }

    { //Report empty collection and see how the queue clearance is detected
        QueueDetector::TRemoteDeviceRecordCollection collection;

        for (int i=0; i<10; ++i)
        {
            queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
            EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
            EXPECT_EQ(9, queueDetector.getQueuePresenceState());
        }

        clock += 10;
        dataContainer.removeNonPresentRemoteDeviceRecords(
            clock.getUniversalTimeSinceEpochInSeconds(),
            dropOutScanCycleThresholdInSeconds,
            true);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        dataContainer.removeNonPresentRemoteDeviceRecords(
            clock.getUniversalTimeSinceEpochInSeconds(),
            dropOutScanCycleThresholdInSeconds,
            true);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report other devices multiple times and check how this affects the report.
        Model::TRemoteDeviceRecord record1(1,0);
        record1.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record1.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        Model::TRemoteDeviceRecord record2(2,0);
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record2.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record2);
        Model::TRemoteDeviceRecord record3(3,0);
        record3.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record3.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record3);
        Model::TRemoteDeviceRecord record4(4,0);
        record4.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record4.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record4);

        for(int i=0; i<1000; ++i)
        {
            clock += 10;
            queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
            EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
            EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
        }
    }
}

TEST(QueueDetector, updateDevicesFromRawTimeFreeFlowWithStartupInterval)
{
    //Setup queue detector.
    //Report first one device, then four devices (including the first one) multiple times.
    //Check how the detections propagate through the bins.
    //Check that no queue is reported until startup time passes by
    //Start reporting no devices and check how this fact afects the bins

    //1) Setup
    DataContainer dataContainer;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(
        dataContainer.getRemoteDeviceCollection(),
        deviceCollectionMutex,
        &clock);

    const unsigned int inquiryScanDurationInSeconds = 10;
    const unsigned int dropOutScanCycleThresholdInSeconds = 20;
    const unsigned int freeFlowThresholdInSeconds = 10;
    const unsigned int moderateFlowThresholdInSeconds = 20;
    const unsigned int slowFlowThresholdInSeconds = 30;
    const unsigned int veryslowFlowThresholdInSeconds = 40;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_FREE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 2;
    const unsigned int queueDetectionStartupIntervalInSeconds = 30;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());

    { //Report empty collection. Check that nothing changes
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NOT_READY, queueDetector.getQueuePresenceState());
    }

    { //Report the first device. Only one bin should be affected
        Model::TRemoteDeviceRecord record(1,0);
        record.firstObservationTime = 1;
        record.lastObservationTime = 9;
        dataContainer.updateRemoteDeviceRecord(record);
        clock += 10;
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NOT_READY, queueDetector.getQueuePresenceState());
    }

    { //Report other devices multiple times and check how this update propagates through the bins
        Model::TRemoteDeviceRecord record1(1,0);
        record1.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record1.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record1);

        Model::TRemoteDeviceRecord record2(2,0);
        record2.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record2.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record2);

        Model::TRemoteDeviceRecord record3(3,0);
        record3.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record3.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record3);

        Model::TRemoteDeviceRecord record4(4,0);
        record4.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record4.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record4);

        clock += 10;
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NOT_READY, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(3, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(1, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        //Now one should go into UNDEFINED bin (above STATIC bin)
        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(9, queueDetector.getQueuePresenceState());
    }

    { //Report empty collection and see how the queue clearance is detected
        QueueDetector::TRemoteDeviceRecordCollection collection;

        for (int i=0; i<10; ++i)
        {
            dataContainer.removeNonPresentRemoteDeviceRecords(
                clock.getUniversalTimeSinceEpochInSeconds(),
                dropOutScanCycleThresholdInSeconds,
                true);
            queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
            EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
            EXPECT_EQ(9, queueDetector.getQueuePresenceState());
        }

        clock += 10;
        dataContainer.removeNonPresentRemoteDeviceRecords(
            clock.getUniversalTimeSinceEpochInSeconds(),
            dropOutScanCycleThresholdInSeconds,
            true);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());


        clock += 10;
        dataContainer.removeNonPresentRemoteDeviceRecords(
            clock.getUniversalTimeSinceEpochInSeconds(),
            dropOutScanCycleThresholdInSeconds,
            true);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());
    }

    { //Report other devices multiple times and check how this affects the report.
        Model::TRemoteDeviceRecord record1(1,0);
        record1.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record1.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        Model::TRemoteDeviceRecord record2(2,0);
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record2.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record2);
        Model::TRemoteDeviceRecord record3(3,0);
        record3.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record3.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record3);
        Model::TRemoteDeviceRecord record4(4,0);
        record4.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record4.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record4);

        for (int i=0; i<100; ++i)
        {
            clock += 10;
            queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
            EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
            EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
            EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
        }
    }
}

TEST(QueueDetector, updateDevicesFromRawTimeFreeFlowWithStartupIntervalAndBlueToothDeviceFault)
{
    //Setup queue detector.
    //Report first one device, then four devices (including the first one) multiple times.
    //Check how the detections propagate through the bins.
    //Check that no queue is reported until startup time passes by
    //Report device fault and clear it. Check that startup interval is respected
    //Start reporting no devices and check how this fact afects the bins

    //1) Setup
    DataContainer dataContainer;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(
        dataContainer.getRemoteDeviceCollection(),
        deviceCollectionMutex,
        &clock);
    dataContainer.addObserver(&queueDetector);

    const unsigned int inquiryScanDurationInSeconds = 10;
    const unsigned int dropOutScanCycleThresholdInSeconds = 20;
    const unsigned int freeFlowThresholdInSeconds = 10;
    const unsigned int moderateFlowThresholdInSeconds = 20;
    const unsigned int slowFlowThresholdInSeconds = 30;
    const unsigned int veryslowFlowThresholdInSeconds = 40;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_FREE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 2;
    const unsigned int queueDetectionStartupIntervalInSeconds = 30;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());

    { //Report all devices multiple times and check how this update propagates through the bins
        Model::TRemoteDeviceRecord record1(1,0);
        record1.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record1.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record1);

        Model::TRemoteDeviceRecord record2(2,0);
        record2.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record2.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record2);

        Model::TRemoteDeviceRecord record3(3,0);
        record3.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record3.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record3);

        Model::TRemoteDeviceRecord record4(4,0);
        record4.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record4.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record4);

        clock += 10;
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NOT_READY, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NOT_READY, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
    }

    //Now report a fault
    {
        dataContainer.getBluetoothDeviceFault().set();
        dataContainer.getBluetoothDeviceFault().setPending();
        dataContainer.getBluetoothDeviceFault().setWasReported();
        //clock.setUniversalTime(ZERO_TIME + ::TTimeDiff_t(0, 0, currentTime));
        dataContainer.notifyObservers(Model::DataContainer::eLOCAL_DEVICE_HAS_BEEN_CHANGED);
    }

    { //Report all devices multiple times and check that if device is faulty no queue is reported
        Model::TRemoteDeviceRecord record1(1,0);
        record1.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record1.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record1);

        Model::TRemoteDeviceRecord record2(2,0);
        record2.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record2.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record2);

        Model::TRemoteDeviceRecord record3(3,0);
        record3.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record3.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record3);

        Model::TRemoteDeviceRecord record4(4,0);
        record4.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record4.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record4);

        clock += 10;
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_FAULT, queueDetector.getQueuePresenceState());
    }

    //Now report clear the fault. Check that startup interval is in force
    {
        dataContainer.getBluetoothDeviceFault().clear();
        dataContainer.getBluetoothDeviceFault().setPending();
        dataContainer.getBluetoothDeviceFault().setWasReported();
        //clock.setUniversalTime(ZERO_TIME + ::TTimeDiff_t(0, 0, currentTime));
        dataContainer.notifyObservers(Model::DataContainer::eLOCAL_DEVICE_HAS_BEEN_CHANGED);
    }

    { //Report all devices multiple times and check how this update propagates through the bins
        Model::TRemoteDeviceRecord record1(1,0);
        record1.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record1.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record1);

        Model::TRemoteDeviceRecord record2(2,0);
        record2.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record2.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record2);

        Model::TRemoteDeviceRecord record3(3,0);
        record3.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record3.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record3);

        Model::TRemoteDeviceRecord record4(4,0);
        record4.firstObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 1;
        record4.lastObservationTime = clock.getUniversalTimeSinceEpochInSeconds() + 9;
        dataContainer.updateRemoteDeviceRecord(record4);

        clock += 10;
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NOT_READY, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NOT_READY, queueDetector.getQueuePresenceState());

        clock += 10;
        record1.firstObservationTime += 10;
        record1.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record1);
        record2.firstObservationTime += 10;
        record2.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record2);
        record3.firstObservationTime += 10;
        record3.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record3);
        record4.firstObservationTime += 10;
        record4.lastObservationTime += 10;
        dataContainer.updateRemoteDeviceRecord(record4);
        queueDetector.updateDevicesFromRawTime(clock.getUniversalTime());
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
    }

}


TEST(QueueDetector, other)
{
    //Setup queue detector.
    //Report first one device, then update its name.
    //Get congestion report
    //Reset

    //1) Setup
    QueueDetector::TRemoteDeviceRecordCollection deviceCollection;
    ::Mutex deviceCollectionMutex;
    Clock clock;
    clock.setUniversalTime(ZERO_TIME);
    QueueDetector queueDetector(deviceCollection, deviceCollectionMutex, &clock);

    const unsigned int inquiryScanDurationInSeconds = 1;
    const unsigned int dropOutScanCycleThresholdInSeconds = 1;
    const unsigned int freeFlowThresholdInSeconds = 1;
    const unsigned int moderateFlowThresholdInSeconds = 2;
    const unsigned int slowFlowThresholdInSeconds = 3;
    const unsigned int veryslowFlowThresholdInSeconds = 4;
    const EBinType queueAlertThresholdBin = Model::eBIN_TYPE_MODERATE_FLOW;
    const unsigned int queueAlertThresholdDetectionNumber = 4;
    const unsigned int queueClearanceThresholdDetectionNumber = 2;
    const unsigned int queueDetectionStartupIntervalInSeconds = 0;

    queueDetector.setup(
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
    EXPECT_TRUE(queueDetector.isConfigured());

    { //Report empty collection. Check that nothing changes
        deviceCollection.clear();
        Model::TRemoteDeviceRecord record1(1,0);
        Model::TRemoteDeviceRecord record2(2,0);
        Model::TRemoteDeviceRecord record3(3,0);
        Model::TRemoteDeviceRecord record4(4,0);
        deviceCollection[record1.address] = record1;
        deviceCollection[record2.address] = record2;
        deviceCollection[record3.address] = record3;
        deviceCollection[record4.address] = record4;
        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;

        queueDetector.updateDevices();
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_NO_QUEUE, queueDetector.getQueuePresenceState());

        deviceCollection[record4.address].name = "Hello";
        ++deviceCollection[record1.address].numberOfScans;
        ++deviceCollection[record2.address].numberOfScans;
        ++deviceCollection[record3.address].numberOfScans;
        ++deviceCollection[record4.address].numberOfScans;
        queueDetector.updateDevices();
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_FREE_FLOW));
        EXPECT_EQ(4, queueDetector.getDeviceCount(Model::eBIN_TYPE_MODERATE_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_VERY_SLOW_FLOW));
        EXPECT_EQ(0, queueDetector.getDeviceCount(Model::eBIN_TYPE_STATIC_FLOW));
        EXPECT_EQ(eQUEUE_PRESENCE_STATE_QUEUE_PRESENT, queueDetector.getQueuePresenceState());
    }

    {
        TCongestionReport report(queueDetector.getCongestionReport());
        EXPECT_EQ(0, report.numberOfDevicesInFreeFlowBin);
        EXPECT_EQ(4, report.numberOfDevicesInModerateFlowBin);
        EXPECT_EQ(0, report.numberOfDevicesInSlowFlowBin);
        EXPECT_EQ(0, report.numberOfDevicesInVerySlowFlowBin);
        EXPECT_EQ(0, report.numberOfDevicesInStaticFlowBin);
    }

    {
        queueDetector.reset();
        TCongestionReport report(queueDetector.getCongestionReport());
        EXPECT_EQ(0, report.numberOfDevicesInFreeFlowBin);
        EXPECT_EQ(0, report.numberOfDevicesInModerateFlowBin);
        EXPECT_EQ(0, report.numberOfDevicesInSlowFlowBin);
        EXPECT_EQ(0, report.numberOfDevicesInVerySlowFlowBin);
        EXPECT_EQ(0, report.numberOfDevicesInStaticFlowBin);
    }
}
