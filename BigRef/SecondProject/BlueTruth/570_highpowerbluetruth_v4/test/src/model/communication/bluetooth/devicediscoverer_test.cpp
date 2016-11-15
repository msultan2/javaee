#include "stdafx.h"
#include <gtest/gtest.h>

#include "bluetooth/devicediscoverer.h"
#include "bluetooth/test_bluetoothdriver_rawhci.h"
#include "instation/test_instationreporter.h"

#include "datacontainer.h"
#include "lock.h"

using BlueTooth::DeviceDiscoverer;
using BlueTooth::TestRawHCIDriver;
using Model::DataContainer;
using Model::TLocalDeviceRecord;
using Model::TLocalDeviceRecord_shared_ptr;
using Model::TLocalDeviceConfiguration;
using Testing::TestInStationReporter;

TEST(DeviceDiscoverer, constructor)
{
    {
        boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
        DeviceDiscoverer deviceDiscoverer(pDataContainer, false, "/tmp/raw_scan_results.csv");

        //Mark getMutex() method as tested
        ::Lock lock(deviceDiscoverer.getMutex());

        EXPECT_TRUE(deviceDiscoverer.isRecordFileOpen());
    }

    {
        boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
        DeviceDiscoverer deviceDiscoverer(pDataContainer, false);

        EXPECT_FALSE(deviceDiscoverer.isRecordFileOpen());
    }

    {
        boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
        DeviceDiscoverer deviceDiscoverer(pDataContainer, false, "");

        EXPECT_FALSE(deviceDiscoverer.isRecordFileOpen());
    }

    {
        boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
        DeviceDiscoverer deviceDiscoverer(pDataContainer, false, "/proc/?!""£$%^&*");

        EXPECT_FALSE(deviceDiscoverer.isRecordFileOpen());
    }
}

TEST(DeviceDiscoverer, getLocalRadioInfo)
{
    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
    pDataContainer->getLocalDeviceConfiguration().deviceDriver
        = Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI;
    DeviceDiscoverer deviceDiscoverer(pDataContainer, false, "/tmp/raw_scan_results.csv");
    TestRawHCIDriver::initialise();

    boost::shared_ptr<QueueDetection::QueueDetector> pQueueDetector;
    boost::shared_ptr<TestInStationReporter> pInStationReporter(new TestInStationReporter());
    deviceDiscoverer.setup(pQueueDetector, pInStationReporter);

    TLocalDeviceRecord_shared_ptr pLocalDeviceRecord(new TLocalDeviceRecord());

    EXPECT_FALSE(deviceDiscoverer.getLocalRadioInfo(0, pLocalDeviceRecord));

    {
        DataContainer::TLocalDeviceRecordCollection localDeviceRecordCollection;
        localDeviceRecordCollection[1] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(1, 100, "device1"));
        localDeviceRecordCollection[2] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(2, 200, "device2"));
        localDeviceRecordCollection[3] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(3, 300, "device3"));
        TestRawHCIDriver::setLocalDeviceCollection(localDeviceRecordCollection);
        EXPECT_EQ(3U, localDeviceRecordCollection.size());

        //find the first item
        ASSERT_TRUE(deviceDiscoverer.getLocalRadioInfo(0, pLocalDeviceRecord));
        EXPECT_EQ(1, pLocalDeviceRecord->address);
        EXPECT_EQ(100, pLocalDeviceRecord->deviceClass);
        EXPECT_STREQ("device1", pLocalDeviceRecord->name.c_str());

        ASSERT_TRUE(deviceDiscoverer.getLocalRadioInfo(1, pLocalDeviceRecord));
        EXPECT_EQ(1, pLocalDeviceRecord->address);
        EXPECT_EQ(100, pLocalDeviceRecord->deviceClass);
        EXPECT_STREQ("device1", pLocalDeviceRecord->name.c_str());

        ASSERT_TRUE(deviceDiscoverer.getLocalRadioInfo(2, pLocalDeviceRecord));
        EXPECT_EQ(2, pLocalDeviceRecord->address);
        EXPECT_EQ(200, pLocalDeviceRecord->deviceClass);
        EXPECT_STREQ("device2", pLocalDeviceRecord->name.c_str());

        ASSERT_TRUE(deviceDiscoverer.getLocalRadioInfo(3, pLocalDeviceRecord));
        EXPECT_EQ(3, pLocalDeviceRecord->address);
        EXPECT_EQ(300, pLocalDeviceRecord->deviceClass);
        EXPECT_STREQ("device3", pLocalDeviceRecord->name.c_str());

        ASSERT_FALSE(deviceDiscoverer.getLocalRadioInfo(4, pLocalDeviceRecord));
    }
}

TEST(DeviceDiscoverer, setupLocalRadio)
{
    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
    pDataContainer->getLocalDeviceConfiguration().deviceDriver
        = Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI;
    DeviceDiscoverer deviceDiscoverer(pDataContainer, false, "/tmp/raw_scan_results.csv");

    TLocalDeviceRecord_shared_ptr result;
    EXPECT_FALSE(deviceDiscoverer.setupLocalRadio(result));

    result = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord());
    EXPECT_TRUE(deviceDiscoverer.setupLocalRadio(result));
}

TEST(DeviceDiscoverer, checkForLocalRadio)
{
    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
    pDataContainer->getLocalDeviceConfiguration().deviceDriver
        = Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI;
    DeviceDiscoverer deviceDiscoverer(pDataContainer, false, "/tmp/raw_scan_results.csv");
    TestRawHCIDriver::initialise();

    boost::shared_ptr<QueueDetection::QueueDetector> pQueueDetector;
    boost::shared_ptr<TestInStationReporter> pInStationReporter(new TestInStationReporter());
    deviceDiscoverer.setup(pQueueDetector, pInStationReporter);

    { //getLocalRadioInfo() returns false
        EXPECT_FALSE(pDataContainer->getBluetoothDeviceFault().get());
        EXPECT_FALSE(pInStationReporter->wasFaultReported());

        EXPECT_FALSE(deviceDiscoverer.checkForLocalRadio());

        EXPECT_TRUE(pDataContainer->getBluetoothDeviceFault().get());
        EXPECT_TRUE(pInStationReporter->wasFaultReported());

        pInStationReporter->clearFaultReported();
    }

    { //getLocalRadioInfo() returns false
        EXPECT_TRUE(pDataContainer->getBluetoothDeviceFault().get());
        EXPECT_FALSE(pInStationReporter->wasFaultReported());

        EXPECT_FALSE(deviceDiscoverer.checkForLocalRadio());

        EXPECT_TRUE(pDataContainer->getBluetoothDeviceFault().get());
        EXPECT_TRUE(pInStationReporter->wasFaultReported());

        pInStationReporter->clearFaultReported();
        pDataContainer->getBluetoothDeviceFault().setPending();
        pDataContainer->getBluetoothDeviceFault().setWasReported();
    }

    {
        DataContainer::TLocalDeviceRecordCollection localDeviceRecordCollection;
        localDeviceRecordCollection[1] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(1, 100, "device1"));
        localDeviceRecordCollection[2] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(2, 200, "device2"));
        localDeviceRecordCollection[3] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(3, 300, "device3"));
        TestRawHCIDriver::setLocalDeviceCollection(localDeviceRecordCollection);
        EXPECT_EQ(3U, localDeviceRecordCollection.size());

        EXPECT_TRUE(pDataContainer->getBluetoothDeviceFault().get());

        EXPECT_TRUE(deviceDiscoverer.checkForLocalRadio());

        EXPECT_FALSE(pDataContainer->getBluetoothDeviceFault().get());
    }
}

TEST(DeviceDiscoverer, inquireDevices)
{
    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
    pDataContainer->getLocalDeviceConfiguration().deviceDriver
        = Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI;
    DeviceDiscoverer deviceDiscoverer(pDataContainer, false, "/tmp/raw_scan_results.csv");
    TestRawHCIDriver::initialise();

    boost::shared_ptr<QueueDetection::QueueDetector> pQueueDetector;
    boost::shared_ptr<TestInStationReporter> pInStationReporter(new TestInStationReporter());
    deviceDiscoverer.setup(pQueueDetector, pInStationReporter);

    //No local radio is present so the function should immediately return false and do nothing
    {
        EXPECT_FALSE(pDataContainer->getBluetoothDeviceFault().get());
        EXPECT_FALSE(pInStationReporter->wasFaultReported());

        EXPECT_FALSE(deviceDiscoverer.inquireDevices());

        EXPECT_TRUE(pDataContainer->getBluetoothDeviceFault().get());
        EXPECT_TRUE(pInStationReporter->wasFaultReported());

        pInStationReporter->clearFaultReported();
    }

    TestRawHCIDriver::setScanForRemoteDevicesResult(false);

    { //scanForRemoteDevices() returns false - an error should be reported
        DataContainer::TLocalDeviceRecordCollection localDeviceRecordCollection;
        localDeviceRecordCollection[1] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(1, 100, "ldevice1"));
        localDeviceRecordCollection[2] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(2, 200, "ldevice2"));
        localDeviceRecordCollection[3] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(3, 300, "ldevice3"));
        TestRawHCIDriver::setLocalDeviceCollection(localDeviceRecordCollection);
        EXPECT_EQ(3U, localDeviceRecordCollection.size());

        EXPECT_FALSE(deviceDiscoverer.inquireDevices());
    }

    TestRawHCIDriver::setScanForRemoteDevicesResult(true);

    {
        DataContainer::TRemoteDeviceRecordCollection remoteDeviceRecordCollection;
        remoteDeviceRecordCollection[10] = Model::TRemoteDeviceRecord(10, 1000, "rdevice1");
        remoteDeviceRecordCollection[20] = Model::TRemoteDeviceRecord(20, 2000, "rdevice2");
        TestRawHCIDriver::setRemoteDeviceCollection(remoteDeviceRecordCollection);
        EXPECT_EQ(2U, remoteDeviceRecordCollection.size());

        EXPECT_TRUE(deviceDiscoverer.inquireDevices());

        EXPECT_EQ(2U, pDataContainer->getRemoteDeviceCollection().size());
    }

    {
        DataContainer::TRemoteDeviceRecordCollection remoteDeviceRecordCollection;
        TestRawHCIDriver::setRemoteDeviceCollection(remoteDeviceRecordCollection);
        EXPECT_EQ(0U, remoteDeviceRecordCollection.size());

        TestRawHCIDriver::setScanForRemoteDevicesResult(true);
        EXPECT_TRUE(deviceDiscoverer.inquireDevices());

        EXPECT_EQ(0U, pDataContainer->getRemoteDeviceCollection().size());
    }
}

TEST(DeviceDiscoverer, inquireDevicesInLegacyMode)
{
    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
    pDataContainer->getLocalDeviceConfiguration().deviceDriver
        = Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI;
    DeviceDiscoverer deviceDiscoverer(pDataContainer, true, "/tmp/raw_scan_results.csv"); // true for legacy mode
    TestRawHCIDriver::initialise();

    boost::shared_ptr<QueueDetection::QueueDetector> pQueueDetector;
    boost::shared_ptr<TestInStationReporter> pInStationReporter(new TestInStationReporter());
    deviceDiscoverer.setup(pQueueDetector, pInStationReporter);

    //No local radio is present so the function should immediately return false and do nothing
    {
        EXPECT_FALSE(pDataContainer->getBluetoothDeviceFault().get());
        EXPECT_FALSE(pInStationReporter->wasFaultReported());

        EXPECT_FALSE(deviceDiscoverer.inquireDevices());

        EXPECT_TRUE(pDataContainer->getBluetoothDeviceFault().get());
        EXPECT_TRUE(pInStationReporter->wasFaultReported());

        pInStationReporter->clearFaultReported();
    }

    TestRawHCIDriver::setScanForRemoteDevicesResult(false);

    { //scanForRemoteDevices() returns false - an error should be reported
        DataContainer::TLocalDeviceRecordCollection localDeviceRecordCollection;
        localDeviceRecordCollection[1] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(1, 100, "ldevice1"));
        localDeviceRecordCollection[2] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(2, 200, "ldevice2"));
        localDeviceRecordCollection[3] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(3, 300, "ldevice3"));
        TestRawHCIDriver::setLocalDeviceCollection(localDeviceRecordCollection);
        EXPECT_EQ(3U, localDeviceRecordCollection.size());

        EXPECT_FALSE(pInStationReporter->wasRawDeviceDetectionSent());
        EXPECT_FALSE(pInStationReporter->wasCongestionReportSent());

        EXPECT_FALSE(deviceDiscoverer.inquireDevices());

        EXPECT_TRUE(pInStationReporter->wasRawDeviceDetectionSent());
        pInStationReporter->clearRawDeviceDetectionSent();
        EXPECT_TRUE(pInStationReporter->wasCongestionReportSent());
        pInStationReporter->clearCongestionReportSent();
    }

    TestRawHCIDriver::setScanForRemoteDevicesResult(true);

    {
        DataContainer::TRemoteDeviceRecordCollection remoteDeviceRecordCollection;
        remoteDeviceRecordCollection[10] = Model::TRemoteDeviceRecord(10, 1000, "rdevice1");
        remoteDeviceRecordCollection[20] = Model::TRemoteDeviceRecord(20, 2000, "rdevice2");
        TestRawHCIDriver::setRemoteDeviceCollection(remoteDeviceRecordCollection);
        EXPECT_EQ(2U, remoteDeviceRecordCollection.size());

        EXPECT_FALSE(pInStationReporter->wasRawDeviceDetectionSent());
        EXPECT_FALSE(pInStationReporter->wasCongestionReportSent());

        EXPECT_TRUE(deviceDiscoverer.inquireDevices());

        EXPECT_TRUE(pInStationReporter->wasRawDeviceDetectionSent());
        pInStationReporter->clearRawDeviceDetectionSent();
        EXPECT_TRUE(pInStationReporter->wasCongestionReportSent());
        pInStationReporter->clearCongestionReportSent();
        EXPECT_EQ(2U, pDataContainer->getRemoteDeviceCollection().size());
    }

    {
        DataContainer::TRemoteDeviceRecordCollection remoteDeviceRecordCollection;
        TestRawHCIDriver::setRemoteDeviceCollection(remoteDeviceRecordCollection);
        EXPECT_EQ(0U, remoteDeviceRecordCollection.size());

        EXPECT_FALSE(pInStationReporter->wasRawDeviceDetectionSent());
        EXPECT_FALSE(pInStationReporter->wasCongestionReportSent());

        TestRawHCIDriver::setScanForRemoteDevicesResult(true);
        EXPECT_TRUE(deviceDiscoverer.inquireDevices());

        EXPECT_TRUE(pInStationReporter->wasRawDeviceDetectionSent());
        pInStationReporter->clearRawDeviceDetectionSent();
        EXPECT_TRUE(pInStationReporter->wasCongestionReportSent());
        pInStationReporter->clearCongestionReportSent();
        EXPECT_EQ(0U, pDataContainer->getRemoteDeviceCollection().size());
    }
}

TEST(DeviceDiscoverer, interruptInquireDevices)
{
    //Dummy test. Currently there is not way to test the result
    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
    pDataContainer->getLocalDeviceConfiguration().deviceDriver
        = Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI;
    DeviceDiscoverer deviceDiscoverer(pDataContainer, false, "/tmp/raw_scan_results.csv");

    deviceDiscoverer.interruptInquireDevices();
    EXPECT_FALSE(TestRawHCIDriver::wasStopScanningForRemoteDevicesCalled());

    DataContainer::TLocalDeviceRecordCollection localDeviceRecordCollection;
    localDeviceRecordCollection[1] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(1, 100, "ldevice1"));
    TestRawHCIDriver::setLocalDeviceCollection(localDeviceRecordCollection);
    EXPECT_EQ(1U, localDeviceRecordCollection.size());
    ASSERT_TRUE(deviceDiscoverer.checkForLocalRadio());

    deviceDiscoverer.interruptInquireDevices();
    EXPECT_TRUE(TestRawHCIDriver::wasStopScanningForRemoteDevicesCalled());
    TestRawHCIDriver::clearStopScanningForRemoteDevicesCalled();
}

TEST(DeviceDiscoverer, getLocalDeviceCollection)
{
    boost::shared_ptr<DataContainer> pDataContainer(new DataContainer());
    pDataContainer->getLocalDeviceConfiguration().deviceDriver
        = Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI;
    DeviceDiscoverer deviceDiscoverer(pDataContainer, false, "/tmp/raw_scan_results.csv");

    { //Empty local device collection
        DataContainer::TLocalDeviceRecordCollection localDeviceRecordCollection;
        TestRawHCIDriver::setLocalDeviceCollection(localDeviceRecordCollection);

        DataContainer::TLocalDeviceRecordCollection result;
        deviceDiscoverer.getLocalDeviceCollection(
            pDataContainer->getLocalDeviceConfiguration(),
            &result);
        EXPECT_TRUE(result.empty());
    }

    { //One local device in the collection
        DataContainer::TLocalDeviceRecordCollection localDeviceRecordCollection;
        localDeviceRecordCollection[1] = TLocalDeviceRecord_shared_ptr(new TLocalDeviceRecord(1));
        Model::LocalDeviceConfiguration localDeviceConfiguration;
        localDeviceConfiguration.deviceDriver = Model::eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI;
        TestRawHCIDriver::setLocalDeviceCollection(localDeviceRecordCollection);
        EXPECT_EQ(1U, localDeviceRecordCollection.size());

        DataContainer::TLocalDeviceRecordCollection result;
        deviceDiscoverer.getLocalDeviceCollection(
            localDeviceConfiguration,
            &result);
        EXPECT_EQ(1U, result.size());
    }
}
