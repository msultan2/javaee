#include "stdafx.h"
#include <gtest/gtest.h>

#include "datacontainer.h"

#include <algorithm> //std::find


using Model::DataContainer;
using Model::Fault;
using Model::TLocalDeviceConfiguration;
using Model::TLocalDeviceRecord;
using Model::TLocalDeviceRecord_shared_ptr;
using Model::RemoteDeviceRecord;


TEST(DataContainer, faults)
{
    {
        DataContainer dataContainer;
        Fault& fault = dataContainer.getBluetoothDeviceFault();

        EXPECT_FALSE(fault.get());
        EXPECT_TRUE(fault.wasReported());

        fault.set();
        EXPECT_TRUE(fault.get());
        EXPECT_FALSE(fault.wasReported());
    }

    {
        DataContainer dataContainer;
        Fault& fault = dataContainer.getRetrieveConfigurationClientCommunicationFault();

        EXPECT_FALSE(fault.get());
        EXPECT_TRUE(fault.wasReported());

        fault.set();
        EXPECT_TRUE(fault.get());
        EXPECT_FALSE(fault.wasReported());
    }

    {
        DataContainer dataContainer;
        Fault& fault = dataContainer.getCongestionReportingClientCommunicationFault();

        EXPECT_FALSE(fault.get());
        EXPECT_TRUE(fault.wasReported());

        fault.set();
        EXPECT_TRUE(fault.get());
        EXPECT_FALSE(fault.wasReported());
    }

    {
        DataContainer dataContainer;
        Fault& fault = dataContainer.getRawDeviceDetectionClientCommunicationFault();

        EXPECT_FALSE(fault.get());
        EXPECT_TRUE(fault.wasReported());

        fault.set();
        EXPECT_TRUE(fault.get());
        EXPECT_FALSE(fault.wasReported());
    }

    {
        DataContainer dataContainer;
        Fault& fault = dataContainer.getAlertAndStatusReportingClientCommunicationFault();

        EXPECT_FALSE(fault.get());
        EXPECT_TRUE(fault.wasReported());

        fault.set();
        EXPECT_TRUE(fault.get());
        EXPECT_FALSE(fault.wasReported());
    }

    {
        DataContainer dataContainer;
        Fault& fault = dataContainer.getStatusReportingClientCommunicationFault();

        EXPECT_FALSE(fault.get());
        EXPECT_TRUE(fault.wasReported());

        fault.set();
        EXPECT_TRUE(fault.get());
        EXPECT_FALSE(fault.wasReported());
    }

    {
        DataContainer dataContainer;
        Fault& fault = dataContainer.getFaultReportingClientCommunicationFault();

        EXPECT_FALSE(fault.get());
        EXPECT_TRUE(fault.wasReported());

        fault.set();
        EXPECT_TRUE(fault.get());
        EXPECT_FALSE(fault.wasReported());
    }

    {
        DataContainer dataContainer;
        Fault& fault = dataContainer.getStatisticsReportingClientCommunicationFault();

        EXPECT_FALSE(fault.get());
        EXPECT_TRUE(fault.wasReported());

        fault.set();
        EXPECT_TRUE(fault.get());
        EXPECT_FALSE(fault.wasReported());
    }
}

TEST(DataContainer, updateAndResetRemoteDeviceRecord)
{
    //First update twice with the same record and then with another record.
    //Finally reset the collection.
    DataContainer dataContainer;
    EXPECT_EQ(0, dataContainer.getRemoteDeviceCollection().size());

    const uint64_t TEST_ADDRESS1 = 1657;
    RemoteDeviceRecord record1(TEST_ADDRESS1, 0);
    const uint64_t TEST_ADDRESS2 = 1658;
    RemoteDeviceRecord record2(TEST_ADDRESS2, 0);

    dataContainer.updateRemoteDeviceRecord(record1);
    EXPECT_EQ(1, dataContainer.getRemoteDeviceCollection().size());
    EXPECT_EQ(1, dataContainer.getRemoteDeviceCollection()[TEST_ADDRESS1].visibilityCounter);

    dataContainer.updateRemoteDeviceRecord(record1);
    EXPECT_EQ(1, dataContainer.getRemoteDeviceCollection().size());
    EXPECT_EQ(2, dataContainer.getRemoteDeviceCollection()[TEST_ADDRESS1].visibilityCounter);

    dataContainer.updateRemoteDeviceRecord(record2);
    EXPECT_EQ(2, dataContainer.getRemoteDeviceCollection().size());
    EXPECT_EQ(2, dataContainer.getRemoteDeviceCollection()[TEST_ADDRESS1].visibilityCounter);
    EXPECT_EQ(1, dataContainer.getRemoteDeviceCollection()[TEST_ADDRESS2].visibilityCounter);

    dataContainer.resetRemoteDeviceRecords();
    EXPECT_EQ(0, dataContainer.getRemoteDeviceCollection().size());
}

TEST(DataContainer, removeNonPresentRemoteDeviceRecords)
{
    { //store three records with various observation times. Call removeNonPresentRemoteDeviceRecords
      //and check that only some records have been moved to non-present device collection
        DataContainer dataContainer;
        EXPECT_EQ(0, dataContainer.getRemoteDeviceCollection().size());

        const uint64_t TEST_ADDRESS1 = 1657;
        RemoteDeviceRecord record1;
        record1.address = TEST_ADDRESS1;
        record1.lastObservationTime = 100;
        const uint64_t TEST_ADDRESS2 = 1658;
        RemoteDeviceRecord record2;
        record2.address = TEST_ADDRESS2;
        record2.lastObservationTime = 110;
        const uint64_t TEST_ADDRESS3 = 1659;
        RemoteDeviceRecord record3;
        record3.address = TEST_ADDRESS3;
        record3.lastObservationTime = 120;
        const uint64_t TEST_ADDRESS4 = 1660;
        RemoteDeviceRecord record4;
        record4.address = TEST_ADDRESS4;
        record4.lastObservationTime = 201;

        dataContainer.updateRemoteDeviceRecord(record1);
        dataContainer.updateRemoteDeviceRecord(record2);
        dataContainer.updateRemoteDeviceRecord(record3);
        dataContainer.updateRemoteDeviceRecord(record4);

        DataContainer::TRemoteDeviceRecordCollection& deviceCollection =
            dataContainer.getRemoteDeviceCollection();
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS1));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS3));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS4));
        DataContainer::TNonPendingRemoteDeviceRecordCollection& nonPresentDeviceCollection =
            dataContainer.getNonPendingRemoteDeviceCollection();
        EXPECT_EQ(0u, nonPresentDeviceCollection.size());


        //last observation time is : 100, 110, 120. Current time is 200
        //TEST_ADDRESS1 is expected to be removed
        dataContainer.removeNonPresentRemoteDeviceRecords(
            200, //current time
            100, //drop out scan cycle threshold in seconds
            true //move to non-present collection for reporting
            );


        EXPECT_TRUE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS1));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS3));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS4));

        EXPECT_EQ(1u, nonPresentDeviceCollection.size());
        if (nonPresentDeviceCollection.size() > 0)
            EXPECT_EQ(TEST_ADDRESS1, nonPresentDeviceCollection[0].address);
    }

    { //the same but do not move to non-present devices collection
        DataContainer dataContainer;
        EXPECT_EQ(0, dataContainer.getRemoteDeviceCollection().size());

        const uint64_t TEST_ADDRESS1 = 1657;
        RemoteDeviceRecord record1;
        record1.address = TEST_ADDRESS1;
        record1.lastObservationTime = 100;
        const uint64_t TEST_ADDRESS2 = 1658;
        RemoteDeviceRecord record2;
        record2.address = TEST_ADDRESS2;
        record2.lastObservationTime = 110;
        const uint64_t TEST_ADDRESS3 = 1659;
        RemoteDeviceRecord record3;
        record3.address = TEST_ADDRESS3;
        record3.lastObservationTime = 120;

        dataContainer.updateRemoteDeviceRecord(record1);
        dataContainer.updateRemoteDeviceRecord(record2);
        dataContainer.updateRemoteDeviceRecord(record3);

        DataContainer::TRemoteDeviceRecordCollection& deviceCollection =
            dataContainer.getRemoteDeviceCollection();
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS1));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS3));
        DataContainer::TNonPendingRemoteDeviceRecordCollection& nonPresentDeviceCollection =
            dataContainer.getNonPendingRemoteDeviceCollection();
        EXPECT_EQ(0u, nonPresentDeviceCollection.size());


        //last observation time is : 100, 110, 120. Current time is 200
        //TEST_ADDRESS1 is expected to be removed
        dataContainer.removeNonPresentRemoteDeviceRecords(
            200, //current time
            100, //drop out scan cycle threshold in seconds
            false //do not move to non-present collection for reporting
            );


        EXPECT_TRUE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS1));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));
        EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS3));

        EXPECT_EQ(0u, nonPresentDeviceCollection.size());
    }
}

TEST(DataContainer, reviewRemoteDevicesAgainstBackgroundCriteria)
{
    //store three records with various observation times. Call removeNonPresentRemoteDeviceRecords
    //and check that only some records have been moved to non-present device collection
    DataContainer dataContainer;
    EXPECT_EQ(0, dataContainer.getRemoteDeviceCollection().size());

    const uint64_t TEST_ADDRESS1 = 1657;
    RemoteDeviceRecord record1;
    record1.address = TEST_ADDRESS1;
    record1.firstObservationTime = 0;
    record1.lastObservationTime = 150;
    const uint64_t TEST_ADDRESS2 = 1658;
    RemoteDeviceRecord record2;
    record2.address = TEST_ADDRESS2;
    record2.firstObservationTime = 110;
    record2.lastObservationTime = 160;
    const uint64_t TEST_ADDRESS3 = 1659;
    RemoteDeviceRecord record3;
    record3.address = TEST_ADDRESS3;
    record3.firstObservationTime = 120;
    record3.lastObservationTime = 180;
    const uint64_t TEST_ADDRESS4 = 1660;
    RemoteDeviceRecord record4;
    record4.address = TEST_ADDRESS4;
    record4.firstObservationTime = 198;
    record4.lastObservationTime = 201;

    dataContainer.updateRemoteDeviceRecord(record1);
    dataContainer.updateRemoteDeviceRecord(record2);
    dataContainer.updateRemoteDeviceRecord(record3);
    dataContainer.updateRemoteDeviceRecord(record4);

    DataContainer::TRemoteDeviceRecordCollection& deviceCollection =
        dataContainer.getRemoteDeviceCollection();
    EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS1));
    EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));
    EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS3));
    EXPECT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS4));
    DataContainer::TNonPendingRemoteDeviceRecordCollection& nonPresentDeviceCollection =
        dataContainer.getNonPendingRemoteDeviceCollection();
    EXPECT_EQ(0u, nonPresentDeviceCollection.size());


    //Check if devices are promoted to background after they stay too long in sight
    //last observation time is : 100, 110, 120 and 199. Current time is 200
    //TEST_ADDRESS1, TEST_ADDRESS2 is expected to be moved to background
    dataContainer.reviewRemoteDevicesAgainstBackgroundCriteria(
        200, //current time
        90, //backgroundPresenceThresholdInSeconds
        300 //backgroundAbsenceThresholdInSeconds
        );


    ASSERT_EQ(4u, deviceCollection.size());
    ASSERT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS1));
    EXPECT_EQ(Model::eBIN_TYPE_BACKGROUND, deviceCollection.find(TEST_ADDRESS1)->second.binType);
    ASSERT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));
    EXPECT_EQ(Model::eBIN_TYPE_BACKGROUND, deviceCollection.find(TEST_ADDRESS2)->second.binType);

    ASSERT_EQ(2u, nonPresentDeviceCollection.size());
    EXPECT_EQ(TEST_ADDRESS1, nonPresentDeviceCollection[0].address);
    EXPECT_EQ(TEST_ADDRESS2, nonPresentDeviceCollection[1].address);
    nonPresentDeviceCollection.clear();


    //Check if devices which are not background will be removed from the collection
    //last observation time is : 100, 110, 120. Current time is 300
    //TEST_ADDRESS1 and TEST_ADDRESS2 is expected to stay
    //TEST_ADDRESS3 and TEST_ADDRESS4 is expected to be removed
    dataContainer.removeNonPresentRemoteDeviceRecords(
        301, //current time
        100, //drop out scan cycle threshold in seconds
        false //do not move to non-present collection for reporting
        );

    ASSERT_EQ(2u, deviceCollection.size());
    ASSERT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS1));
    ASSERT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));


    //Check if devices that are background are removed after they are not visible for some time
    //TEST_ADDRESS1 and TEST_ADDRESS2 is expected to stay
    dataContainer.reviewRemoteDevicesAgainstBackgroundCriteria(
        399, //current time
        90, //backgroundPresenceThresholdInSeconds
        250 //backgroundAbsenceThresholdInSeconds
        );

    ASSERT_EQ(2u, deviceCollection.size());
    ASSERT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS1));
    ASSERT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));

    //TEST_ADDRESS2 is expected to stay
    dataContainer.reviewRemoteDevicesAgainstBackgroundCriteria(
        400, //current time
        90, //backgroundPresenceThresholdInSeconds
        250 //backgroundAbsenceThresholdInSeconds
        );

    ASSERT_EQ(1u, deviceCollection.size());
    ASSERT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));
    ASSERT_EQ(0u, nonPresentDeviceCollection.size());

    //TEST_ADDRESS2 is expected to stay
    dataContainer.reviewRemoteDevicesAgainstBackgroundCriteria(
        409, //current time
        90, //backgroundPresenceThresholdInSeconds
        250 //backgroundAbsenceThresholdInSeconds
        );

    ASSERT_EQ(1u, deviceCollection.size());
    ASSERT_FALSE(deviceCollection.end() == deviceCollection.find(TEST_ADDRESS2));
    ASSERT_EQ(0u, nonPresentDeviceCollection.size());

    //TEST_ADDRESS2 is expected to stay
    dataContainer.reviewRemoteDevicesAgainstBackgroundCriteria(
        410, //current time
        90, //backgroundPresenceThresholdInSeconds
        250 //backgroundAbsenceThresholdInSeconds
        );

    ASSERT_EQ(0u, deviceCollection.size());
    ASSERT_EQ(0u, nonPresentDeviceCollection.size());
}

TEST(DataContainer, setLastInquiryStartTime)
{
    DataContainer dataContainer;
    EXPECT_TRUE(dataContainer.getLastInquiryStartTime() == pt::not_a_date_time);

    TTime_t inqTime(ZERO_TIME);
    dataContainer.setLastInquiryStartTime(inqTime);
    EXPECT_TRUE(dataContainer.getLastInquiryStartTime() == inqTime);
}

TEST(DataContainer, setLocalDeviceRecord)
{
    DataContainer dataContainer;
    EXPECT_FALSE(dataContainer.getLocalDeviceRecord());

    const uint64_t ADDRESS = 123456;
    const uint32_t DEVICE_CLASS = 12345;
    TLocalDeviceRecord_shared_ptr pLocalDeviceRecord(new TLocalDeviceRecord(ADDRESS, DEVICE_CLASS, ""));
    dataContainer.setLocalDeviceRecord(pLocalDeviceRecord);

    EXPECT_EQ(ADDRESS, dataContainer.getLocalDeviceRecord()->address);
    EXPECT_EQ(DEVICE_CLASS, dataContainer.getLocalDeviceRecord()->deviceClass);
}

TEST(DataContainer, setLocalDeviceConfiguration)
{
    DataContainer dataContainer;
    EXPECT_EQ(0u, dataContainer.getLocalDeviceConfiguration().address);
    EXPECT_EQ(Model::eDEVICE_DRIVER_UNDEFINED, dataContainer.getLocalDeviceConfiguration().deviceDriver);

    const uint64_t ADDRESS = 123456;
    const Model::EDeviceDriver DEVICE_DRIVER = Model::eDEVICE_DRIVER_LINUX_PARANI;

    TLocalDeviceConfiguration localDeviceConfiguration;
    localDeviceConfiguration.address = ADDRESS;
    localDeviceConfiguration.deviceDriver = DEVICE_DRIVER;
    dataContainer.setLocalDeviceConfiguration(localDeviceConfiguration);

    EXPECT_EQ(ADDRESS, dataContainer.getLocalDeviceConfiguration().address);
    EXPECT_EQ(DEVICE_DRIVER, dataContainer.getLocalDeviceConfiguration().deviceDriver);
}
