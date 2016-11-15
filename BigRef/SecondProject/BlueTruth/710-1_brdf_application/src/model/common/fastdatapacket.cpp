#include "stdafx.h"
#include "fastdatapacket.h"

#include <sstream>
#include <cassert>

// Alignment must be at least 4 but we are using 128 because of cache alignment.
#define DEFAULT_ALIGNMENT (0x80u)

#ifdef _DEBUG
#define INITIALISE_WITH_ZERO
#endif

namespace
{
    const size_t MAX_LENGTH = 4u * 1024u * 1024u;
    const size_t DEFAULT_PACKET_SIZE_INT = 0x10; //16
    const size_t DEFAULT_PACKET_SIZE_CHAR = DEFAULT_PACKET_SIZE_INT * sizeof(unsigned int); //64
}


FastDataPacket::FastDataPacket()
:
m_dataPtr(0),
m_length(0),
m_size(DEFAULT_PACKET_SIZE_CHAR)
{
    unsigned int* __restrict ptr = new unsigned int[DEFAULT_PACKET_SIZE_INT];
    m_dataPtr = reinterpret_cast<unsigned char*>(ptr);

#ifdef INITIALISE_WITH_ZERO
    for (size_t i=DEFAULT_PACKET_SIZE_INT; i!=0; --i)
    {
        *ptr++ = 0;
    }
#endif
}

FastDataPacket::FastDataPacket(const size_t reservedPacketSize)
:
m_dataPtr(0),
m_length(0),
m_size(calculateRoundedLength(reservedPacketSize))
{
    unsigned int* __restrict ptr = new unsigned int[m_size >> 2];
    m_dataPtr = reinterpret_cast<unsigned char*>(ptr);

#ifdef INITIALISE_WITH_ZERO
    size_t numberOfWords = (m_size >> 2);
    for (size_t i=numberOfWords; i!=0; --i)
    {
        *ptr++ = 0;
    }
#endif
}

FastDataPacket::FastDataPacket(const std::string& s)
:
m_dataPtr(0),
m_length(0),
m_size(DEFAULT_PACKET_SIZE_CHAR)
{
    //Specify new size
    m_size = calculateRoundedLength(s.size());

    m_dataPtr = reinterpret_cast<unsigned char*>(new unsigned int[(m_size >> 2)]);

    append(reinterpret_cast<const unsigned char*>(s.c_str()), s.size());
}

FastDataPacket::FastDataPacket(const FastDataPacket &pkt)
:
m_dataPtr(0),
m_length(0),
m_size(DEFAULT_PACKET_SIZE_CHAR)
{
    //Specify new size
    if (pkt.length() > DEFAULT_PACKET_SIZE_CHAR)
    {
        m_size = calculateRoundedLength(pkt.length());
    }
    //else do nothing

    m_dataPtr = reinterpret_cast<unsigned char*>(new unsigned int[(m_size >> 2)]);

    //Copy data
    //append(pkt.m_dataPtr, pkt.m_length);
    unsigned int* __restrict tmpDataPtr = reinterpret_cast<unsigned int*>(m_dataPtr);
    const unsigned int* __restrict tmpSourceDataPtr = reinterpret_cast<unsigned int*>(pkt.m_dataPtr);
    size_t numberOfWords = (pkt.m_length >> 2);
    if ((numberOfWords << 2) != pkt.m_length)
    {
        ++numberOfWords;
    }
    //else do nothing

    for (size_t i=numberOfWords; i!=0; --i)
    {
        *tmpDataPtr++ = *tmpSourceDataPtr++;
    }

    m_length = pkt.m_length;
}

FastDataPacket::FastDataPacket(const unsigned char* szFastDataPacket, const size_t nLength)
:
m_dataPtr(0),
m_length(0),
m_size(DEFAULT_PACKET_SIZE_CHAR)
{
    //Specify new size
    m_size = calculateRoundedLength(nLength);

    m_dataPtr = reinterpret_cast<unsigned char*>(new unsigned int[(m_size >> 2)]);

    append(szFastDataPacket, nLength);
}

FastDataPacket::~FastDataPacket()
{
    if (m_dataPtr==0)
    {
        assert(false);
    }
    //else do nothing

    delete[] m_dataPtr;
    m_dataPtr = 0;
}

FastDataPacket& FastDataPacket::operator=(const FastDataPacket &pkt)
{
    if (this != &pkt)
    {
        //Specify new size
        if (pkt.length() > DEFAULT_PACKET_SIZE_CHAR)
        {
            m_size = calculateRoundedLength(pkt.length());

            delete[] m_dataPtr;
            const size_t NEW_SIZE_INT = m_size >> 2;
            m_dataPtr = reinterpret_cast<unsigned char*>(new unsigned int[NEW_SIZE_INT]);
        }
        //else do nothing

        //Copy data
        //append(pkt.m_dataPtr, pkt.m_length);
        unsigned int* __restrict tmpDataPtr = reinterpret_cast<unsigned int*>(m_dataPtr);
        const unsigned int* __restrict tmpSourceDataPtr = reinterpret_cast<unsigned int*>(pkt.m_dataPtr);
        size_t numberOfWords = (pkt.m_length >> 2);
        if ((numberOfWords << 2) != pkt.m_length)
        {
            ++numberOfWords;
        }
        //else do nothing

        for (size_t i=numberOfWords; i!=0; --i)
        {
            *tmpDataPtr++ = *tmpSourceDataPtr++;
        }

        m_length = pkt.m_length;
    }
    //else do nothing

    return *this;
}

unsigned char FastDataPacket::operator[](const size_t index) const
{
    assert (index < m_length);

    const unsigned char RESULT = *(m_dataPtr + index);

    return RESULT;
}

void FastDataPacket::reserve(const size_t nLength)
{
    const size_t NEW_SIZE = calculateRoundedLength(nLength);

    if (NEW_SIZE != m_size)
    {
        //Allocate bigger memory space for this FastDataPacket
        unsigned char* __restrict oldPktDataPtr = m_dataPtr;
        m_dataPtr = reinterpret_cast<unsigned char*>(new unsigned int[NEW_SIZE >> 2]);

        //Copy old data
        unsigned int* __restrict tmpDataPtr = reinterpret_cast<unsigned int*>(m_dataPtr);
        const unsigned int* __restrict tmpSourceDataPtr = reinterpret_cast<unsigned int*>(oldPktDataPtr);
        size_t numberOfWords = (m_length >> 2);
        if ((numberOfWords << 2) != m_length)
        {
            ++numberOfWords;
        }
        //else do nothing

        for (size_t i=numberOfWords; i!=0; --i)
        {
            *tmpDataPtr++ = *tmpSourceDataPtr++;
        }

        m_size = NEW_SIZE;

        //Delete old data object
        delete[] oldPktDataPtr;
    }
    //else do nothing
}

void FastDataPacket::addSizeForRawBufferAccess(const size_t size)
{
    if (m_length + size > m_size)
    {
        reserve(m_length + size);
    }
    //else do nothing

    m_length += size;
}

void FastDataPacket::fullErase()
{
    //First zero all the fields
    unsigned int* __restrict tmpDataPtr = reinterpret_cast<unsigned int*>(m_dataPtr);
    size_t numberOfWords = (m_size >> 2);
    if ((numberOfWords << 2) != m_size)
    {
        ++numberOfWords;
    }
    //else do nothing

    for (size_t i=numberOfWords; i!=0; --i)
    {
        *tmpDataPtr++ = 0;
    }

    //Adjust lentgh
    erase();
}

void FastDataPacket::setLength(const size_t length)
{
    if (length > m_size)
    {
        /* Only call reserve if we need to allocate more. */
        reserve(length);
    }
    //else do nothing

    m_length = length;
}

FastDataPacket& FastDataPacket::append(const unsigned char b)
{
    // is there space for the character?
    if (m_length + 1 > m_size)
    {
        reserve(m_length + 1);
    }
    //else do nothing

    *(m_dataPtr + (m_length++)) = b;

    return *this;
}

FastDataPacket& FastDataPacket::append(const char b)
{
    // is there space for the character?
    if (m_length + 1 > m_size)
    {
        reserve(m_length + 1);
    }
    //else do nothing

    *(m_dataPtr + (m_length++)) = *reinterpret_cast<const unsigned char *>(&b);

    return *this;
}

FastDataPacket& FastDataPacket::append(const bool value)
{
    // is there space for the character?
    if (m_length + 1 > m_size)
    {
        reserve(m_length + 1);
    }
    //else do nothing

    if (value)
    {
        *(m_dataPtr + (m_length++)) = 1;
    }
    else
    {
        *(m_dataPtr + (m_length++)) = 0;
    }

    return *this;
}


FastDataPacket& FastDataPacket::append(
                const unsigned char* __restrict src,
                const size_t charCount)
{
    if (m_length + charCount > m_size)
    {
        reserve(m_length + charCount);
    }
    //else do nothing

    unsigned char* __restrict tmpDataPtr = m_dataPtr + m_length;
    const unsigned char* __restrict tmpSourceDataPtr = src;
    for (size_t i=charCount; i!=0; --i)
    {
        *tmpDataPtr++ = *tmpSourceDataPtr++;
    }

    m_length += charCount;

    return *this;
}

void FastDataPacket::substr(FastDataPacket& pkt, const size_t pos, const size_t numberOfChars) const
{
    assert( pos + numberOfChars <= m_length );

    pkt.erase();

    if (pkt.m_size < numberOfChars)
    {
        pkt.reserve(numberOfChars);
    }
    //else do nothing

    unsigned char* __restrict tmpDataPtr = pkt.m_dataPtr;
    const unsigned char* __restrict tmpSourceDataPtr = m_dataPtr + pos;
    for (size_t i=numberOfChars; i!=0; --i)
    {
        *tmpDataPtr++ = *tmpSourceDataPtr++;
    }
    pkt.m_length = numberOfChars;
}

size_t FastDataPacket::maxlength()
{
    return MAX_LENGTH;
}

size_t FastDataPacket::getDefaultSize()
{
    return DEFAULT_PACKET_SIZE_CHAR;
}

const std::string FastDataPacket::dumpData(void) const
{
    std::ostringstream resultStream;

    resultStream << " [";

    static const size_t MAX_NUMBER_OF_DUMPED_BYTES = 1000;
    const size_t NUMBER_OF_BYTES =
        (m_length>MAX_NUMBER_OF_DUMPED_BYTES) ? MAX_NUMBER_OF_DUMPED_BYTES : m_length;
    for (size_t i=0; i<NUMBER_OF_BYTES; ++i)
    {
        resultStream.width(2);
        resultStream.fill('0');
        resultStream << std::hex << static_cast<int>(*(m_dataPtr + i)) << " ";
    }

    if (m_length > MAX_NUMBER_OF_DUMPED_BYTES)
    {
        resultStream << " ... ";
    }
    //else do nothing

    resultStream << "] (" << std::dec << m_length << " BYTES)";

    return resultStream.str();
}

bool FastDataPacket::equals(const FastDataPacket& rhs) const
{
    bool result = (m_length == rhs.m_length);

    if (result)
    {
        //First compare as integers (should be 4 times faster than comparing characters)
        const unsigned int* __restrict tmpDataIntPtr = reinterpret_cast<const unsigned int*>(m_dataPtr);
        const unsigned int* __restrict tmpSourceDataIntPtr = reinterpret_cast<const unsigned int*>(rhs.m_dataPtr);
        size_t numberOfWords = (m_length >> 2);

        for (size_t i=numberOfWords; i!=0; --i)
        {
            if (*tmpDataIntPtr++ != *tmpSourceDataIntPtr++)
            {
                result = false;
                break;
            }
            //else continue
        }

        //Compare the remaining bytes
        const unsigned char* __restrict tmpDataCharPtr = reinterpret_cast<const unsigned char*>(tmpDataIntPtr);
        const unsigned char* __restrict tmpSourceDataCharPtr = reinterpret_cast<const unsigned char*>(tmpSourceDataIntPtr);
        const size_t NUMBER_OF_BYTES = m_length - (numberOfWords << 2);
        for (size_t i=NUMBER_OF_BYTES; i!=0; --i)
        {
            if (*tmpDataCharPtr++ != *tmpSourceDataCharPtr++)
            {
                result = false;
                break;
            }
            //else continue
        }
    }
    //else do nothing

    return result;
}

size_t FastDataPacket::calculateRoundedLength(const size_t nLength)
{
    //Round nLength to the higher or equal value that is a power of 2.
    //e.g. 0x405,0x406,0x407->0x800, 0x800->0x800, 0x801->0x1000, etc.

    size_t result = DEFAULT_PACKET_SIZE_CHAR;

    if (nLength > result)
    {
        unsigned int n = 0;
        size_t tmpLength = nLength;
        while (tmpLength != 0)
        {
            ++n;
            tmpLength >>= 1;
        }

        result = 1 << (n-1);
        if (result != nLength)
        {
            result <<= 1;
        }
        //else do nothing (initial value was a power of 2)
    }
    //else do nothing

    return result;
}

void FastDataPacket::fixForDisplaying()
{
    const size_t packetLength = size();
    setLength(packetLength+1);
    getRawBufferData()[packetLength] = 0;
    setLength(packetLength);
}

bool operator==(const FastDataPacket& lhs, const FastDataPacket& rhs)
{
    return lhs.equals(rhs);
}

bool operator!=(const FastDataPacket& lhs, const FastDataPacket& rhs)
{
    return !lhs.equals(rhs);
}
