#include "stdafx.h"
#include <gtest/gtest.h>

#include "fastdatapacket.h"

TEST(FastDataPacket, calculateRoundedLength)
{
    //e.g. 0x405,0x406,0x407->0x800, 0x800->0x800, 0x801->0x1000, etc.
    EXPECT_EQ(0x400u,  FastDataPacket::calculateRoundedLength(0x400));
    EXPECT_EQ(0x800u,  FastDataPacket::calculateRoundedLength(0x401));
    EXPECT_EQ(0x800u,  FastDataPacket::calculateRoundedLength(0x402));
    EXPECT_EQ(0x800u,  FastDataPacket::calculateRoundedLength(0x403));
    EXPECT_EQ(0x800u,  FastDataPacket::calculateRoundedLength(0x404));
    EXPECT_EQ(0x800u,  FastDataPacket::calculateRoundedLength(0x405));
    EXPECT_EQ(0x800u,  FastDataPacket::calculateRoundedLength(0x406));
    EXPECT_EQ(0x800u,  FastDataPacket::calculateRoundedLength(0x407));
    EXPECT_EQ(0x800u,  FastDataPacket::calculateRoundedLength(0x800));
    EXPECT_EQ(0x1000u, FastDataPacket::calculateRoundedLength(0x801));
}

TEST(FastDataPacket, constructor)
{
    {
        FastDataPacket packet;
        packet.append('a');
        packet.append('a');
        packet.append('a');
        packet.append('a');
        packet.append('a');
        EXPECT_EQ('a', packet[0]);
        EXPECT_EQ('a', packet[1]);
        EXPECT_EQ('a', packet[2]);
        EXPECT_EQ('a', packet[3]);
        EXPECT_EQ('a', packet[4]);

        // copy constructor
        FastDataPacket copiedDataPacket(packet);
        EXPECT_TRUE(copiedDataPacket.equals(packet));

        // assignment operator
        FastDataPacket assignedDataPacket = copiedDataPacket;
        EXPECT_TRUE(assignedDataPacket.equals(packet));
    }

    {
        // constructor (unsigned char*, size_t)
        std::string text("WORLD2");
        FastDataPacket packet((unsigned char*) text.c_str(), text.size());
        EXPECT_EQ('W', packet[0]);
        EXPECT_EQ('O', packet[1]);
        EXPECT_EQ('R', packet[2]);
        EXPECT_EQ('L', packet[3]);
        EXPECT_EQ('D', packet[4]);
        EXPECT_EQ('2', packet[5]);
        EXPECT_EQ(6U, packet.size());
    }

    {
        // constructor (unsigned char*, size_t)
        std::string text("WORLD3");
        FastDataPacket packet(text);
        EXPECT_EQ('W', packet[0]);
        EXPECT_EQ('O', packet[1]);
        EXPECT_EQ('R', packet[2]);
        EXPECT_EQ('L', packet[3]);
        EXPECT_EQ('D', packet[4]);
        EXPECT_EQ('3', packet[5]);
        EXPECT_EQ(6U, packet.size());
    }

    { //copy constructor and assignment operator
        FastDataPacket packet;
        ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());

        for (size_t i=0; i<1025u; ++i)
        {
            packet.append( static_cast<unsigned char>(i & 0xFF));
        }

        ASSERT_EQ(2u * 1024u, packet.getMemorySize());
        ASSERT_EQ(1025u, packet.length());
        for (size_t i=0; i<packet.length(); ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), packet[i]);
        }

        FastDataPacket copyPacket(packet);
        EXPECT_EQ(2u * 1024u, copyPacket.getMemorySize());
        EXPECT_EQ(1025u, copyPacket.length());
        for (size_t i=0; i<1025u; ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), copyPacket[i]);
        }

        FastDataPacket assignedPacket;
        assignedPacket = packet;
        EXPECT_EQ(2u * 1024u, assignedPacket.getMemorySize());
        EXPECT_EQ(1025u, assignedPacket.length());
        for (size_t i=0; i<1025u; ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), assignedPacket[i]);
        }
    }
}

TEST(FastDataPacket, equals)
{
	{ //empty packet
		FastDataPacket lhs;
		FastDataPacket rhs;
		EXPECT_TRUE(lhs.equals(rhs));

		lhs.append((unsigned char)1);
		EXPECT_FALSE(lhs.equals(rhs));

		rhs.append((unsigned char)1);
		EXPECT_TRUE(lhs.equals(rhs));
	}

    { //compare packets of not-equal lengths
        FastDataPacket packet1;
        packet1.append('a');
        packet1.append('b');
        EXPECT_EQ('a', packet1[0]);
        EXPECT_EQ('b', packet1[1]);

        FastDataPacket packet2;
        packet2.append('a');
        EXPECT_EQ('a', packet2[0]);
        EXPECT_FALSE(packet2.equals(packet1));

        packet2.append('b');
        EXPECT_EQ('b', packet2[1]);
        EXPECT_TRUE(packet2.equals(packet1));
	}

    { //compare packets of equal lengths
        FastDataPacket packet1;
        packet1.append('a');
        packet1.append('b');
        packet1.append('c');
        packet1.append('d');
        packet1.append('e');
        packet1.append('f');
        packet1.append('g');
        EXPECT_EQ('a', packet1[0]);
        EXPECT_EQ('b', packet1[1]);
        EXPECT_EQ('c', packet1[2]);
        EXPECT_EQ('d', packet1[3]);
        EXPECT_EQ('e', packet1[4]);
        EXPECT_EQ('f', packet1[5]);
        EXPECT_EQ('g', packet1[6]);

        FastDataPacket packet2;
        packet2.append('a');
        EXPECT_EQ('a', packet2[0]);
        EXPECT_FALSE(packet2.equals(packet1));
        EXPECT_FALSE(packet2 == packet1);

        packet2.append('b');
        packet2.append('c');
        packet2.append('d');
        packet2.append('e');
        packet2.append('f');
        EXPECT_EQ('b', packet2[1]);
        EXPECT_EQ('c', packet2[2]);
        EXPECT_EQ('d', packet2[3]);
        EXPECT_EQ('e', packet2[4]);
        EXPECT_EQ('f', packet2[5]);
        EXPECT_FALSE(packet2.equals(packet1));
        EXPECT_FALSE(packet2 == packet1);

        packet2.append('g');
        EXPECT_EQ('g', packet2[6]);
        EXPECT_TRUE(packet2.equals(packet1));
        EXPECT_FALSE(packet2 != packet1);
	}

    { //compare packets of equal lengths
        FastDataPacket packet1;
        packet1.append('a');
        packet1.append('b');
        packet1.append('c');
        packet1.append('d');
        packet1.append('e');
        packet1.append('f');
        packet1.append('g');
        EXPECT_EQ('a', packet1[0]);
        EXPECT_EQ('b', packet1[1]);
        EXPECT_EQ('c', packet1[2]);
        EXPECT_EQ('d', packet1[3]);
        EXPECT_EQ('e', packet1[4]);
        EXPECT_EQ('f', packet1[5]);
        EXPECT_EQ('g', packet1[6]);

        FastDataPacket packet2;
        packet2.append('a');
        packet2.append('X');
        packet2.append('c');
        packet2.append('d');
        packet2.append('e');
        packet2.append('f');
        packet2.append('g');
        EXPECT_EQ('a', packet2[0]);
        EXPECT_EQ('X', packet2[1]);
        EXPECT_EQ('c', packet2[2]);
        EXPECT_EQ('d', packet2[3]);
        EXPECT_EQ('e', packet2[4]);
        EXPECT_EQ('f', packet2[5]);
        EXPECT_EQ('g', packet2[6]);

        EXPECT_FALSE(packet2.equals(packet1));
        EXPECT_TRUE(packet2 != packet1);
	}

    { //compare packets of equal lengths
        FastDataPacket packet1;
        packet1.append('a');
        packet1.append('b');
        packet1.append('c');
        packet1.append('d');
        packet1.append('e');
        packet1.append('f');
        packet1.append('g');
        EXPECT_EQ('a', packet1[0]);
        EXPECT_EQ('b', packet1[1]);
        EXPECT_EQ('c', packet1[2]);
        EXPECT_EQ('d', packet1[3]);
        EXPECT_EQ('e', packet1[4]);
        EXPECT_EQ('f', packet1[5]);
        EXPECT_EQ('g', packet1[6]);

        FastDataPacket packet2;
        packet2.append('a');
        packet2.append('b');
        packet2.append('c');
        packet2.append('d');
        packet2.append('e');
        packet2.append('f');
        packet2.append('x');
        EXPECT_EQ('b', packet2[1]);
        EXPECT_EQ('c', packet2[2]);
        EXPECT_EQ('d', packet2[3]);
        EXPECT_EQ('e', packet2[4]);
        EXPECT_EQ('f', packet2[5]);
        EXPECT_EQ('x', packet2[6]);

        EXPECT_FALSE(packet2.equals(packet1));
        EXPECT_TRUE(packet2 != packet1);
	}
}

TEST(FastDataPacket, append)
{
    {
        // default constructor, empty
        FastDataPacket pkt;
        EXPECT_TRUE(pkt.empty());
        EXPECT_EQ(0U, pkt.length());
        EXPECT_EQ(0U, pkt.size());
        EXPECT_TRUE(pkt.equals(pkt));
        EXPECT_TRUE(pkt.equals(FastDataPacket()));

        // append(const unsigned char)
        FastDataPacket res(pkt.append((unsigned char)0));
        EXPECT_EQ(0, pkt[0]);
        EXPECT_EQ(0, res[0]);
        EXPECT_FALSE(pkt.empty());
        EXPECT_EQ(1U, pkt.size());

        res = pkt.append((unsigned char)1);
        EXPECT_EQ(1, pkt[1]);
        EXPECT_EQ(1, res[1]);
        EXPECT_EQ(2U, pkt.size());
        EXPECT_EQ(2U, res.size());

        res = pkt.append((unsigned char)2);
        EXPECT_EQ(2, pkt[2]);
        EXPECT_EQ(2, res[2]);
        EXPECT_EQ(3U, pkt.size());
        EXPECT_EQ(3U, res.size());
    }

    {
        // append (unsigned char*, unsigned long)
        FastDataPacket pkt;
        unsigned char data[8];
        data[0] = 1;
        data[1] = 2;
        data[2] = 0;
        data[3] = 1;
        data[4] = 2;
        data[5] = 0;
        data[6] = 1;
        data[7] = 2;
        pkt.append(data, 8);

        EXPECT_EQ(1, pkt[0]);
        EXPECT_EQ(2, pkt[1]);
        EXPECT_EQ(0, pkt[2]);
        EXPECT_EQ(1, pkt[3]);
        EXPECT_EQ(2, pkt[4]);
        EXPECT_EQ(0, pkt[5]);
        EXPECT_EQ(1, pkt[6]);
        EXPECT_EQ(2, pkt[7]);

        // append(FastDataPacket)
        FastDataPacket pkt2;
        pkt2.append(pkt);
        EXPECT_TRUE(pkt.equals(pkt2));
        EXPECT_EQ(1, pkt2[0]);
        EXPECT_EQ(2, pkt2[1]);
        EXPECT_EQ(0, pkt2[2]);
        EXPECT_EQ(1, pkt2[3]);
        EXPECT_EQ(2, pkt2[4]);
        EXPECT_EQ(0, pkt2[5]);
        EXPECT_EQ(1, pkt2[6]);
        EXPECT_EQ(2, pkt2[7]);

    }

    {
        // append (const bool)
        FastDataPacket pkt;
        pkt.append(true);
        pkt.append(false);
        pkt.append(true);
        pkt.append(true);
        pkt.append(false);
        for (int i=0; i<1000; ++i)
        {
            pkt.append(true);
        }

        EXPECT_EQ(1005, pkt.size());
        if (pkt.size() >=1005)
        {
            EXPECT_EQ(1, pkt[0]);
            EXPECT_EQ(0, pkt[1]);
            EXPECT_EQ(1, pkt[2]);
            EXPECT_EQ(1, pkt[3]);
            EXPECT_EQ(0, pkt[4]);

            for (int i=0; i<1000; ++i)
            {
                EXPECT_EQ(1, pkt[i+5]);
            }
        }
        //else do nothing
    }

	{
		FastDataPacket packet;
		ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());

		for (size_t i=0; i<packet.getMemorySize(); ++i)
		{
			packet.append( static_cast<unsigned char>(i & 0xFF));
		}

		ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());
		ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.length());
		for (size_t i=0; i<packet.length(); ++i)
		{
			ASSERT_EQ(static_cast<int>(i & 0xFF), static_cast<int>(packet[i]));
		}

		packet.append('X');
		ASSERT_EQ(2U * FastDataPacket::getDefaultSize(), packet.getMemorySize());
		ASSERT_EQ(FastDataPacket::getDefaultSize() + 1u, packet.length());
		for (size_t i=0; i<FastDataPacket::getDefaultSize(); ++i)
		{
			ASSERT_EQ(static_cast<int>(i & 0xFF), static_cast<int>(packet[i]));
		}
		ASSERT_EQ('X', packet[FastDataPacket::getDefaultSize()]);
	}

	{
		FastDataPacket packet;
		ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());

		for (size_t i=0; i<packet.getMemorySize(); ++i)
		{
			packet.append( static_cast<unsigned char>(i & 0xFF));
		}

		ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());
		ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.length());
		for (size_t i=0; i<packet.length(); ++i)
		{
			ASSERT_EQ(static_cast<int>(i & 0xFF), static_cast<int>(packet[i]));
		}

		const unsigned char A_STRING[] = "AB";
		packet.append(A_STRING, 2);
		ASSERT_EQ(2U * FastDataPacket::getDefaultSize(), packet.getMemorySize());
		ASSERT_EQ(FastDataPacket::getDefaultSize() + 2u, packet.length());
		for (size_t i=0; i<FastDataPacket::getDefaultSize(); ++i)
		{
			ASSERT_EQ(static_cast<int>(i & 0xFF), static_cast<int>(packet[i]));
		}
		ASSERT_EQ('A', packet[FastDataPacket::getDefaultSize()]);
		ASSERT_EQ('B', packet[FastDataPacket::getDefaultSize() + 1u]);
	}
}

TEST(FastDataPacket, substr)
{
	{
		unsigned char TEST_STRING[] = "1234567890\0";

		FastDataPacket orig(&TEST_STRING[0], sizeof(TEST_STRING));

		// substr(unsigned long, unsigned long) [this function calls the other substr]
		FastDataPacket result(orig.substr(0, 1));
		ASSERT_EQ(1u, result.length());
		EXPECT_EQ('1', result[0]);

		result = orig.substr(0, 9);
		ASSERT_EQ(9u, result.length());
		EXPECT_EQ('1', result[0]);
		EXPECT_EQ('2', result[1]);
		EXPECT_EQ('3', result[2]);
		EXPECT_EQ('4', result[3]);
		EXPECT_EQ('5', result[4]);
		EXPECT_EQ('6', result[5]);
		EXPECT_EQ('7', result[6]);
		EXPECT_EQ('8', result[7]);
		EXPECT_EQ('9', result[8]);

		result = orig.substr(0, 10);
		ASSERT_EQ(10u, result.length());
		EXPECT_EQ('1', result[0]);
		EXPECT_EQ('2', result[1]);
		EXPECT_EQ('3', result[2]);
		EXPECT_EQ('4', result[3]);
		EXPECT_EQ('5', result[4]);
		EXPECT_EQ('6', result[5]);
		EXPECT_EQ('7', result[6]);
		EXPECT_EQ('8', result[7]);
		EXPECT_EQ('9', result[8]);
		EXPECT_EQ('0', result[9]);

		result = orig.substr(0, 11);
		ASSERT_EQ(11u, result.length());
		EXPECT_EQ('1', result[0]);
		EXPECT_EQ('2', result[1]);
		EXPECT_EQ('3', result[2]);
		EXPECT_EQ('4', result[3]);
		EXPECT_EQ('5', result[4]);
		EXPECT_EQ('6', result[5]);
		EXPECT_EQ('7', result[6]);
		EXPECT_EQ('8', result[7]);
		EXPECT_EQ('9', result[8]);
		EXPECT_EQ('0', result[9]);
		EXPECT_EQ(0, result[10]);

		result = orig.substr(2, 5);
		ASSERT_EQ(5u, result.length());
		EXPECT_EQ('3', result[0]);
		EXPECT_EQ('4', result[1]);
		EXPECT_EQ('5', result[2]);
		EXPECT_EQ('6', result[3]);
		EXPECT_EQ('7', result[4]);
	}

	{
		FastDataPacket packet;
		for (size_t i=0; i<2048u; ++i)
		{
			packet.append( static_cast<unsigned char>(i & 0xFF));
		}
		ASSERT_EQ(2048u, packet.getMemorySize());
		ASSERT_EQ(2048u, packet.length());

		FastDataPacket subPacket(packet.substr(3u, 1500u));
        ASSERT_EQ(1500u, subPacket.length());
		for (size_t i=0; i<1500u; ++i)
		{
			packet.append( static_cast<unsigned char>((i+3u) & 0xFF));
		}
	}

}

TEST(FastDataPacket, reserve)
{
    {
        FastDataPacket packet;
        ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());

        for (size_t i=0; i<packet.getMemorySize(); ++i)
        {
            packet.append( static_cast<unsigned char>(i & 0xFF));
        }

        ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());
        ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.length());
        for (size_t i=0; i<packet.length(); ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), packet[i]);
        }

        packet.reserve(5000);
        EXPECT_EQ(0x80u * FastDataPacket::getDefaultSize(), packet.getMemorySize());
        for (size_t i=0; i<FastDataPacket::getDefaultSize(); ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), packet[i]);
        }
    }

    {
        FastDataPacket packet;
        ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());

        for (size_t i=0; i<FastDataPacket::getDefaultSize() + 1u; ++i)
        {
            packet.append( static_cast<unsigned char>(i & 0xFF));
        }

        ASSERT_EQ(2u * FastDataPacket::getDefaultSize(), packet.getMemorySize());
        ASSERT_EQ(FastDataPacket::getDefaultSize() + 1u, packet.length());
        for (size_t i=0; i<packet.length(); ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), packet[i]);
        }

        packet.reserve(5000);
        EXPECT_EQ(0x80u * FastDataPacket::getDefaultSize(), packet.getMemorySize());
        for (size_t i=0; i<FastDataPacket::getDefaultSize() + 1u; ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), packet[i]);
        }
    }

    {
        FastDataPacket packet;
        ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());

        for (size_t i=0; i<FastDataPacket::getDefaultSize() + 2u; ++i)
        {
            packet.append( static_cast<unsigned char>(i & 0xFF));
        }

        ASSERT_EQ(2u * FastDataPacket::getDefaultSize(), packet.getMemorySize());
        ASSERT_EQ(FastDataPacket::getDefaultSize() + 2u, packet.length());
        for (size_t i=0; i<packet.length(); ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), packet[i]);
        }

        packet.reserve(5000);
        EXPECT_EQ(0x80u * FastDataPacket::getDefaultSize(), packet.getMemorySize());
        for (size_t i=0; i<FastDataPacket::getDefaultSize() + 2u; ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), packet[i]);
        }
    }

    {
        FastDataPacket packet;
        ASSERT_EQ(FastDataPacket::getDefaultSize(), packet.getMemorySize());

        for (size_t i=0; i<FastDataPacket::getDefaultSize() + 3u; ++i)
        {
            packet.append( static_cast<unsigned char>(i & 0xFF));
        }

        ASSERT_EQ(2u * FastDataPacket::getDefaultSize(), packet.getMemorySize());
        ASSERT_EQ(FastDataPacket::getDefaultSize() + 3u, packet.length());
        for (size_t i=0; i<packet.length(); ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), packet[i]);
        }

        packet.reserve(5000);
        EXPECT_EQ(0x80u * FastDataPacket::getDefaultSize(), packet.getMemorySize());
        for (size_t i=0; i<FastDataPacket::getDefaultSize() + 3u; ++i)
        {
            ASSERT_EQ(static_cast<unsigned char>(i & 0xFF), packet[i]);
        }
    }
}

TEST(FastDataPacket, setLength)
{
    //Verify methods related to manipulation of memory size and length
    {
        FastDataPacket pkt;
        EXPECT_EQ(64u, pkt.getMemorySize());
        EXPECT_EQ(0u, pkt.size());
        EXPECT_EQ(0u, pkt.length());
    }

    {
        FastDataPacket pkt(65u); //65 should be promoted to the nearest 2^N which is 2^7=128
        EXPECT_EQ(128u, pkt.getMemorySize());
        EXPECT_EQ(0U, pkt.size());
    }

    {
        FastDataPacket pkt;
        EXPECT_EQ(64u, pkt.getMemorySize());
        EXPECT_EQ(0U, pkt.size());

        const size_t SIZE = 245;
        pkt.addSizeForRawBufferAccess(SIZE);
        EXPECT_EQ(256u, pkt.getMemorySize());
        EXPECT_EQ(SIZE, pkt.size());
    }

    {
        FastDataPacket pkt;
        EXPECT_EQ(64u, pkt.getMemorySize());
        EXPECT_EQ(0u, pkt.size());
        EXPECT_EQ(0u, pkt.length());

        const size_t LENGTH = 100;
        pkt.setLength(LENGTH);
        for (unsigned char i=0; i<LENGTH; ++i)
        {
            *(pkt.getRawBufferData() + i) = i;
        }

        EXPECT_EQ((size_t)LENGTH, pkt.size());
        EXPECT_EQ(LENGTH, pkt.length());
        if (pkt.size() >= LENGTH)
        {
            for (unsigned char i=0; i<LENGTH; ++i)
            {
                EXPECT_EQ(i, *(pkt.getRawBufferData() + i));
                EXPECT_EQ(i, *(pkt.data() + i));
            }
        }
        //else do nothing

        pkt.fullErase();
        EXPECT_EQ(128, pkt.getMemorySize());
        EXPECT_EQ(0U, pkt.size());
    }
}

TEST(FastDataPacket, TestOther)
{
    {
        FastDataPacket pkt;
        pkt.append((unsigned char)1);
        pkt.append((unsigned char)2);
        EXPECT_EQ(2U, pkt.size());

        // dumpdata
        EXPECT_EQ(" [01 02 ] (2 BYTES)", pkt.dumpData());

        // erase
        pkt.erase();

        EXPECT_EQ(0U, pkt.size());
        EXPECT_EQ(true, pkt.empty());
        EXPECT_EQ(" [] (0 BYTES)", pkt.dumpData());
    }

    {
        EXPECT_TRUE(FastDataPacket::maxlength()!=0);
        EXPECT_TRUE(FastDataPacket::getDefaultSize()!=0);
    }
}
