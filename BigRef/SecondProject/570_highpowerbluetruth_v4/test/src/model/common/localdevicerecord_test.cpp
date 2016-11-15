#include "stdafx.h"
#include <gtest/gtest.h>

#include "datacontainer.h"
#include "localdevicerecord.h"

using Model::LocalDeviceRecord;


TEST(LocalDeviceRecord, constructor)
{
    { //default constructor
        LocalDeviceRecord record;
        EXPECT_EQ(0, record.address);
        EXPECT_EQ(0, record.deviceClass);
        EXPECT_TRUE(record.name.empty());
        EXPECT_EQ(-1, record.hciRoute);
    }

    { //constructor with one parameter
        const uint64_t TEST_ADDRESS = 564;
        LocalDeviceRecord record(TEST_ADDRESS);
        EXPECT_EQ(TEST_ADDRESS, record.address);
        EXPECT_EQ(0, record.deviceClass);
        EXPECT_TRUE(record.name.empty());
        EXPECT_EQ(-1, record.hciRoute);
    }

    { //constructor with three parameters
        const uint64_t TEST_ADDRESS = 657;
        const uint32_t TEST_COD = 101;
        const char TEST_NAME[] = "test_name";
        LocalDeviceRecord record(TEST_ADDRESS, TEST_COD, TEST_NAME);
        EXPECT_EQ(TEST_ADDRESS, record.address);
        EXPECT_EQ(TEST_COD, record.deviceClass);
        EXPECT_STREQ(TEST_NAME, record.name.c_str());
        EXPECT_EQ(-1, record.hciRoute);
    }
}

TEST(LocalDeviceRecord, reset)
{
    { //reset
        const uint64_t TEST_ADDRESS = 658;
        const uint32_t TEST_COD = 101;
        const char TEST_NAME[] = "test_name";
        LocalDeviceRecord record(TEST_ADDRESS, TEST_COD, TEST_NAME);
        record.reset();
        EXPECT_EQ(0, record.address);
        EXPECT_EQ(0, record.deviceClass);
        EXPECT_TRUE(record.name.empty());
        EXPECT_EQ(-1, record.hciRoute);
    }

    { //partial reset
        const uint64_t TEST_ADDRESS = 659;
        const uint32_t TEST_COD = 101;
        const char TEST_NAME[] = "test_name";
        LocalDeviceRecord record(TEST_ADDRESS, TEST_COD, TEST_NAME);
        record.partialReset();
        EXPECT_EQ(TEST_ADDRESS, record.address);
        EXPECT_EQ(0, record.deviceClass);
        EXPECT_TRUE(record.name.empty());
        EXPECT_EQ(-1, record.hciRoute);
    }
}
