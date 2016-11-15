#include "stdafx.h"
#include <gtest/gtest.h>

#include "types.h"
#include "fastdatapacket.h"
#include "md5_rfc1321.h"

//unsigned char* calculateMD5FromString(const unsigned char* data, const unsigned int len);

TEST(RFC1321, calculateMD5FromString)
{
    // TEST DATA
    const uint8_t TEST_DATA[] = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 
                                  0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16};
    const uint8_t EXPECTED_MD5_SUM[] = { 0xe8, 0x00, 0x34, 0xe8, 0x86, 0x21, 0x2a, 0x5d,
                                         0x7d, 0xa9, 0xef, 0x09, 0x63, 0x33, 0x65, 0x5c };

    // TEST
    Model::DiggestType md5Hash = {0};
    Model::calculateMD5FromString(TEST_DATA, sizeof(TEST_DATA), &md5Hash);

    const FastDataPacket EXPECTED_MD5_SUM_DATA_PACKET(EXPECTED_MD5_SUM, sizeof(EXPECTED_MD5_SUM));
    const FastDataPacket CALCULATED_MD5_SUM_DATA_PACKET(md5Hash, sizeof(md5Hash));

    EXPECT_EQ(EXPECTED_MD5_SUM_DATA_PACKET.dumpData(), CALCULATED_MD5_SUM_DATA_PACKET.dumpData());
}
