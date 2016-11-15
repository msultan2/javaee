/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef _FAST_DATA_PACKET_H_
#define _FAST_DATA_PACKET_H_

#ifdef _WIN32
#pragma warning (disable: 4786)
#endif //_WIN32

#include <string>
#include <stdlib.h>
#include <boost/shared_ptr.hpp>

/**
 * @brief This is a fast implementation of the DataPacket class intented to
 * manipulate char array directly, not with use of basic_string class.
 */
class FastDataPacket
{
public:

    /// @brief Default constructor
    FastDataPacket();

    /// @brief Constructor with default size
    explicit FastDataPacket(const size_t reservedPacketSize);

    /// @brief Constructor from string
    explicit FastDataPacket(const std::string& s);

    /// @brief Copy constructor
    FastDataPacket(const FastDataPacket& pkt);

    /// @brief Constructor
    FastDataPacket(const unsigned char* szFastDataPacket, const size_t nLength);

    /// @brief Destructor
    virtual ~FastDataPacket();

    /// @brief Copy assignment operator
    FastDataPacket& operator=(const FastDataPacket &pkt);

    /// @brief Index operator
    unsigned char operator[](const size_t index) const;

    /// @brief Get raw data pointer
    //! @returns the contents of the FastDataPacket as a null-terminated array
    //! of bytes (unsigned char)
    const unsigned char* data() const { return m_dataPtr; }

    /// @brief Get raw data pointer
    //! @returns the contents of the FastDataPacket as a null-terminated array
    //! of bytes (unsigned char)
    const char* c_str() const { return (const char*)m_dataPtr; }

    /// @brief Pretend we are appending data.
    void addSizeForRawBufferAccess(const size_t size);

    /// @brief Get access to the data pointer. This method should be used along with setLength() method
    unsigned char* getRawBufferData() { return m_dataPtr; }

    /// @brief Get const access to the data pointer.
    const unsigned char* getRawBufferData() const { return m_dataPtr; }

    /// @brief Check if data is empty
    /// @return true if the FastDataPacket has not data
    bool empty() const { return (m_length == 0); }

    /// @brief Erases all data in the FastDataPacket
    void erase() { m_length = 0; }

    /// @brief Erases all data in the FastDataPacket
    void fullErase();

    /// @brief Get data size
    //! @return the number of bytes in the FastDataPacket
    size_t size() const { return m_length; }

    //! @brief Get actual memory size occupied by data array
    //! @return the memory size occupied by data array
    //TODO This function should be renamed to size()
    size_t getMemorySize() const { return m_size; }

    /// @brief Get the number of bytes stored
    //! @return number of bytes
    size_t length() const { return m_length; }

    //! @brief Set length of a packet.
    //! This method should be used along with getRawBufferData() method
    //! @param length - the number of bytes in the FastDataPacket
    void setLength(const size_t length);

    //! @brief Requests that the capacity of the allocated storage space in the string be at least nLength.
    void reserve(const size_t nLength);


#if defined _WIN32 || defined STD_CPP_0X
    //Disable implicit casts for append method. This function can be called only with the below specified types
    template <typename T>
    FastDataPacket& append(T) = delete;
#endif

    //! @brief Adds a character to the end of this FastDataPacket
    FastDataPacket& append(const unsigned char b);

    //! @brief Adds a character to the end of this FastDataPacket
    FastDataPacket& append(const char b);

    //! @brief Adds a character to the end of this FastDataPacket
    FastDataPacket& append(const bool value);

    //! @brief Adds an array of bytes (unsigned chars) to the FastDataPacket
    //! @param src - pointer to the start of the array
    //! @param charCount - number of bytes in the array
    FastDataPacket& append(const unsigned char* __restrict src, const size_t charCount);

    //! @brief Adds a FastDataPacket object to the end of this FastDataPacket
    FastDataPacket& append(const FastDataPacket &pkt)
    {
        append(pkt.m_dataPtr, pkt.m_length);

        return *this;
    }

    //! @brief Adds a FastDataPacket object to the end of this FastDataPacket
    FastDataPacket& append(const FastDataPacket* pkt)
    {
        append(pkt->m_dataPtr, pkt->m_length);

        return *this;
    }

    //! @brief Gets a substring of the FastDataPacket
    //! @return a substring of the FastDataPacket.
    //! @param pos - the position at which to begin the substring i.e. the index into the array
    //! @param numberOfChars - the number of characters to get. As long as this value is
    //!                        less than the maximum FastDataPacket size, the returned value will
    //!                        always contain this number of characters, padded with zeroes if
    //!                        the original FastDataPacket was not large enough
    FastDataPacket substr(const size_t pos, const size_t numberOfChars) const
    {
        FastDataPacket result;
        substr(result, pos, numberOfChars);

        return result;
    }

    //! @brief Gets a substring of the FastDataPacket
    //! @param pkt - reference parameter to the FastDataPacket into which the result will be stored
    //! @param pos - the position at which to begin the substring i.e. the index into the array
    //! @param numberOfChars - the number of characters to get. As long as this value is
    //!                        less than the maximum FastDataPacket size, the returned value will
    //!                        always contain this number of characters, padded with zeroes if
    //!                        the original FastDataPacket was not large enough
    void substr(FastDataPacket& pkt,
                const size_t pos,
                const size_t numberOfChars) const;

    //! @brief Get the maximum allowed size of data
    //! @return the maximum allowed size
    static size_t maxlength();

    //! @brief Get the default size of data
    //! @return the default size
    static size_t getDefaultSize();

    //! @brief Convert data into hex so that it can be displayed
    //! @return a string containing a textual representation of the data contained
    //! within the object, for debugging purposes
    const std::string dumpData(void) const;

    //! @brief Check if this packet equals to the other one
    //! @return true if the contents of the two packets are exactly equal
    bool equals(const FastDataPacket& rhs) const;

    static size_t calculateRoundedLength(const size_t nLength);

    //! Append 0 to the packet so that when displayed no extranous chars get displayed
    void fixForDisplaying();

private:

    unsigned char* __restrict m_dataPtr;
    size_t m_length;
    size_t m_size;
};

//! @brief Compare two packets
//! @return true if the contents of the two packets are exactly equal
bool operator==(const FastDataPacket& lhs, const FastDataPacket& rhs);

//! @brief Compare two packets
//! @return true if the contents of the two packets are not exactly equal
bool operator!=(const FastDataPacket& lhs, const FastDataPacket& rhs);


typedef boost::shared_ptr<FastDataPacket> FastDataPacket_shared_ptr;

#endif //_FAST_DATA_PACKET_H_
