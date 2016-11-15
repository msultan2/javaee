#include "stdafx.h"
#include <gtest/gtest.h>

#include "datacontainer.h"
#include "remotedevicerecord.h"

using Model::RemoteDeviceRecord;


TEST(RemoteDeviceRecord, constructor)
{
    { //default constructor
        RemoteDeviceRecord record;
        EXPECT_EQ(0, record.address);
        EXPECT_EQ(0, record.deviceClass);
        EXPECT_TRUE(record.name.empty());
        EXPECT_EQ(0, record.firstObservationTime);
        EXPECT_EQ(0, record.referencePointObservationTime);
        EXPECT_EQ(0, record.lastObservationTime);
        EXPECT_EQ(0, record.visibilityCounter);
    }

    { //constructor with two parameters
        const uint64_t TEST_ADDRESS = 1657;
        const uint32_t TEST_COD = 101;
        RemoteDeviceRecord record(TEST_ADDRESS, TEST_COD);
        EXPECT_EQ(TEST_ADDRESS, record.address);
        EXPECT_EQ(TEST_COD, record.deviceClass);
        EXPECT_TRUE(record.name.empty());
        EXPECT_NE(0, record.firstObservationTime);
        EXPECT_NE(0, record.referencePointObservationTime);
        EXPECT_NE(0, record.lastObservationTime);
        EXPECT_EQ(1, record.visibilityCounter);
    }

    { //constructor with three parameters
        const uint64_t TEST_ADDRESS = 657;
        const uint32_t TEST_COD = 101;
        const char TEST_NAME[] = "test_name";
        RemoteDeviceRecord record(TEST_ADDRESS, TEST_COD, TEST_NAME);
        EXPECT_EQ(TEST_ADDRESS, record.address);
        EXPECT_EQ(TEST_COD, record.deviceClass);
        EXPECT_STREQ(TEST_NAME, record.name.c_str());
        EXPECT_NE(0, record.firstObservationTime);
        EXPECT_NE(0, record.referencePointObservationTime);
        EXPECT_NE(0, record.lastObservationTime);
        EXPECT_EQ(1, record.visibilityCounter);
    }
}

TEST(RemoteDeviceRecord, reset)
{
    { //reset
        const uint64_t TEST_ADDRESS = 657;
        const uint32_t TEST_COD = 101;
        const char TEST_NAME[] = "test_name";
        RemoteDeviceRecord record(TEST_ADDRESS, TEST_COD, TEST_NAME);
        record.reset();
        EXPECT_EQ(0, record.address);
        EXPECT_FALSE(record.deviceIdentifierHashCalculated);
        EXPECT_EQ(0, record.deviceClass);
        EXPECT_TRUE(record.name.empty());
        EXPECT_EQ(0, record.firstObservationTime);
        EXPECT_EQ(0, record.referencePointObservationTime);
        EXPECT_EQ(0, record.lastObservationTime);
        EXPECT_EQ(0, record.visibilityCounter);

        record.calculateHashIfRequired();
        EXPECT_EQ(0, record.address);
        EXPECT_TRUE(record.deviceIdentifierHashCalculated);
        EXPECT_EQ(0, record.deviceClass);
        EXPECT_TRUE(record.name.empty());
        EXPECT_EQ(0, record.firstObservationTime);
        EXPECT_EQ(0, record.referencePointObservationTime);
        EXPECT_EQ(0, record.lastObservationTime);
        EXPECT_EQ(0, record.visibilityCounter);
    }
}

TEST(RemoteDeviceRecord, recalculateHash)
{
    {
        RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_NONE;

        const uint64_t TEST_ADDRESS = 0x01234567890AB;
        const uint32_t TEST_COD = 101;
        const char TEST_NAME[] = "test_name";
        RemoteDeviceRecord record(TEST_ADDRESS, TEST_COD, TEST_NAME);

        EXPECT_EQ(TEST_ADDRESS, record.address);
        EXPECT_TRUE(record.deviceIdentifierHashCalculated);

        size_t i=0;
        EXPECT_EQ(0x12, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0x34, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0x56, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0x78, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0x90, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0xab, record.deviceIdentifierHash[i++]);

        EXPECT_EQ(TEST_COD, record.deviceClass);
        EXPECT_EQ(1, record.visibilityCounter);
    }

    {
        RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_RAND1;

        const uint64_t TEST_ADDRESS = 0x01234567890AB;
        const uint32_t TEST_COD = 101;
        const char TEST_NAME[] = "test_name";
        RemoteDeviceRecord record(TEST_ADDRESS, TEST_COD, TEST_NAME);

        EXPECT_EQ(TEST_ADDRESS, record.address);
        EXPECT_TRUE(record.deviceIdentifierHashCalculated);

        size_t i=0;
        EXPECT_EQ(0x56, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0x90, record.deviceIdentifierHash[i++]);
        i++;
        EXPECT_EQ(0x12, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0xAB, record.deviceIdentifierHash[i++]);
        i++;
        EXPECT_EQ(0x34, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0x78, record.deviceIdentifierHash[i++]);

        EXPECT_EQ(TEST_COD, record.deviceClass);
        EXPECT_EQ(1, record.visibilityCounter);
    }

    {
        RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_SHA256;
        const char PRESEED[] = "Hello world!";
        RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeedSize =
            std::min(sizeof(PRESEED) - 1, sizeof(RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeed));
        memcpy(RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeed, PRESEED, RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeedSize);
        const char POSTSEED[] = "Simulation System Ltd.";
        RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeedSize =
            std::min(sizeof(POSTSEED) - 1, sizeof(RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeed));
        memcpy(RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeed, POSTSEED, RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeedSize);


        const uint64_t TEST_ADDRESS = 0x01234567890AB;
        const uint32_t TEST_COD = 101;
        const char TEST_NAME[] = "test_name";
        RemoteDeviceRecord record(TEST_ADDRESS, TEST_COD, TEST_NAME);

        EXPECT_EQ(TEST_ADDRESS, record.address);
        EXPECT_TRUE(record.deviceIdentifierHashCalculated);

        size_t i=0;
        EXPECT_EQ(0xE3, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0xD0, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0x5B, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0xAD, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0xBB, record.deviceIdentifierHash[i++]);
        EXPECT_EQ(0x70, record.deviceIdentifierHash[i++]);
        //etc. We are checking only the first 6 bytes

        EXPECT_EQ(TEST_COD, record.deviceClass);
        EXPECT_EQ(1, record.visibilityCounter);
    }

    RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_NONE;
}
