/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/


#ifndef _UTILS_H_
#define _UTILS_H_

// HICPP Violation: Rule 13.4 - Do not use compiler specific language or preprocessor extensions
// Justification: this warning is disabled as it only appears because of a bug in MVC++ 6 when using certain STL componenents (std::map), and additionally it only surfaces during DEBUG mode
#ifdef _WIN32
#pragma warning (disable: 4786)
#endif

#include <string>
#include <vector>

#include "unicode_types.h"
#include "types.h"


namespace {
    const tchar sz_FORWARD_SLASH[] = _T("/");
    const tchar sz_COLON[] = _T(":");
}


class FastDataPacket;
class Clock;

//! ---------------------------------------------------------------------------
//!                                  Utils
//! ---------------------------------------------------------------------------
class Utils
{
    public:

        static const unsigned int SHIFT_ONE_BYTE;
        static const unsigned int SHIFT_TWO_BYTES;
        static const unsigned int SHIFT_THREE_BYTES;
        static const unsigned int SHIFT_FOUR_BYTES;
        static const unsigned int SHIFT_FIVE_BYTES;
        static const unsigned int SHIFT_SIX_BYTES;
        static const unsigned int SHIFT_SEVEN_BYTES;

        /**
         * Functions to read integers from a buffer.
         * Implementations do not assume that intN_t/uintN_t comprise precisely N bits.
         */
        static uint8_t  readUInt8 (const uint8_t* const dataBuffer, const size_t offset = 0u);
        static uint16_t readUInt16(const uint8_t* const dataBuffer, const size_t offset = 0u);
        static uint32_t readUInt24(const uint8_t* const dataBuffer, const size_t offset = 0u);
        static uint32_t readUInt32(const uint8_t* const dataBuffer, const size_t offset = 0u);
        static uint64_t readUInt64(const uint8_t* const dataBuffer, const size_t offset = 0u);

        /**
         * Functions to read integers from a data packet.
         * Implementations do not assume that intN_t/uintN_t comprise precisely N bits.
         */
        static uint8_t  readUInt8 (const FastDataPacket& dataPacket, const size_t offset = 0u);
        static uint16_t readUInt16(const FastDataPacket& dataPacket, const size_t offset = 0u);
        static uint32_t readUInt32(const FastDataPacket& dataPacket, const size_t offset = 0u);


        //! converts a single 2-byte unsigned integer into two 1-byte
        //! values, and stores them in LITTLE ENDIAN format
        static void shortToCharPtrLE(unsigned short param, BYTE* szResult);

        //! converts two 1-byte values stored in LITTLE ENDIAN format
        //! into a single 2-byte unsigned integer
        static unsigned short charPtrToShortLE(const BYTE* szParam);

        //! converts a single 4-byte unsigned integer into four 1-byte
        //! values, and stores them in LITTLE ENDIAN format
        static void longToCharPtrLE(unsigned long param, BYTE* szResult);

        //! converts four 1-byte values stored in LITTLE_ENDIAN format
        //! into a single 4-byte unsigned integer
        static unsigned long charPtrToLongLE(const BYTE* szParam);


        //!
        //! converts a 2-byte unsigned into to a string
        static const char* shortToStringTwoDigits(const unsigned short x);
        static const wchar_t* shortToWStringTwoDigits(const unsigned short x);

        //!
        //! converts a 2-byte unsigned into to a string
        static std::string shortToStringThreeDigits(unsigned short param);
        static std::wstring shortToWStringThreeDigits(unsigned short param);

        //!
        //! converts the 4-byte unsigned integer to a string
        //static std::string longToString(unsigned long value);
        //static std::tstring longToTString(unsigned long value);

        //!
        //! converts the 4-byte unsigned integer to a string
        static std::string intToString(const int value);
        static std::wstring intToWString(const int value);

        enum ConversionType {
            BIN = 2,
            OCT = 8,
            DEC = 10,
            HEX = 16
        };

        //!
        //! converts the 4-byte integer to a string
        static std::string int64ToString(
            const int64_t valueconst,
            ConversionType conversionBase = DEC,
            const unsigned int width = 0,
            const char fillCharacter = '0');

        //!
        //! converts the 4-byte unsigned integer to a string
        static std::string uint64ToString(
            const uint64_t valueconst,
            ConversionType conversionBase = DEC,
            const unsigned int width = 0,
            const char fillCharacter = '0');

        //!
        //! converts the 4-byte integer to a string
        static std::wstring int64ToWString(
            const int64_t valueconst,
            ConversionType conversionBase = DEC,
            const unsigned int width = 0,
            const wchar_t fillCharacter = L'0');

        //!
        //! converts the 4-byte unsigned integer to a string
        static std::wstring uint64ToWString(
            const uint64_t valueconst,
            ConversionType conversionBase = DEC,
            const unsigned int width = 0,
            const wchar_t fillCharacter = L'0');

        //!
        //! converts the 4-byte unsigned integer to a string
        static std::string floatToString(const float value);

        //!
        //! converts a 1-byte unsigned integer to a string
        static std::string byteToString(const unsigned char param);

        //! attempts to convert the value contained in a string into a 4-byte unsigned integer.
        //! checks are performed to verify that the string is a valid number
        //! @param string - the string to be converted
        //! @return the converted number, 0 if it fails
        static unsigned long stringToUnsignedLong(const std::string& string);
        static unsigned short stringToUnsignedShort(const std::string& string);

        //! Checks if the character is one of the allowed for this base
        //! @param character - the character to be analysed
        //! @param conversionBase - the base of conversion. Only 2, 8, 10 and 16 values are allowed
        //! return - true if allowed, false otherwise
        static bool isThisAnAllowedCharacter(
            const unsigned int character,
            const unsigned long base);

        //! Convert the value contained in a string in decimal into a unsigned int64 (8 bytes)
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @return true if the string is a valid number
        static bool stringToUInt64(
            const std::string& input,
            uint64_t& number,
            const ConversionType conversionBase = DEC);

        //! Convert the value contained in a string in decimal into a int64 (8 bytes)
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @return true if the string is a valid number
        static bool stringToInt64(
            const std::string& input,
            int64_t& number);

        //! Convert the value contained in a string in decimal into a unsigned int (4 byte)
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @return true if the string is a valid number
        static bool stringToUInt(
            const std::string& input,
            unsigned int& number,
            const ConversionType conversionBase = DEC);

        //! Convert the value contained in a string in decimal into a signed int (4 byte)
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @return true if the string is a valid number
        static bool stringToInt(
            const std::string& input,
            int& number,
            const ConversionType conversionBase = DEC);

        //! Convert the value contained in a string in decimal format into an unsigned int (4-byte).
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @param removeTrailings - try to remove a pair or /r/n characters at the end of string
        //! @return true if the string is a valid number
        static bool decToInt(
            const std::string& input,
            unsigned int& number,
            const bool removeTrailings = false);

        //! Convert the value contained in a string in decimal format into an unsigned char (1-byte)
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @param removeTrailings - try to remove a pair or /r/n characters at the end of string
        //! @return true if the string is a valid number
        static bool decToByte(
            const std::string& input,
            unsigned char& number,
            const bool removeTrailings = false);
        //! Convert the value contained in a string in octal format into an unsigned int (4-byte).
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @return true if the string is a valid number
        static bool octalToInt(const std::string& input, unsigned int& number);

        //! Convert the value contained in a string in octal format into an unsigned short (2-byte).
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @return true if the string is a valid number
        static bool octalToShort(const std::string& input, unsigned short& number);

        //! Convert the value contained in a string in octal format into an unsigned char (1-byte)
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @return true if the string is a valid number
        static bool octalToByte(const std::string& input, unsigned char& number);

        //! Convert the value contained in a string in hexadecimal format into an unsigned int (4-byte).
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @param removeTrailings - try to remove a pair or /r/n characters at the end of string
        //! @return true if the string is a valid number
        static bool hexToInt(const std::string& input, unsigned int& number, const bool removeTrailings = false);

        //! Convert the value contained in a string in hexadecimal format into an unsigned short (2-byte).
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @return true if the string is a valid number
        static bool hexToShort(const std::string& input, unsigned short& number);

        //! Convert the value contained in a string in hexadecimal format into an unsigned char (1-byte).
        //! Checks are performed to verify that the string is a valid number.
        //! @param input - the string to be converted
        //! @param number - reference parameter into which the converted value will be stored
        //! @param removeTrailings - try to remove a pair or /r/n characters at the end of string
        //! @return true if the string is a valid number
        static bool hexToByte(const std::string& input, unsigned char& number, const bool removeTrailings = false);

        //! Remove the last trailing characters '\r' and '\n' if present
        static std::string removeTrailingLF(const std::string& text);

        // --------------------------------------------------------------------------------
        // Time and/or Date methods.
        // --------------------------------------------------------------------------------

        static void setClock(::Clock* timerPtr);
        static ::Clock* getClock();

        //! helper function to detect leap years.
        static bool isLeapYear(const uint32_t years);

        //! disassemble time stamp
        static void decodeTimeStamp(const uint64_t timestamp,
                                    uint16_t& microseconds,
                                    uint16_t& milliseconds,
                                    uint8_t& seconds,
                                    uint8_t& minutes,
                                    uint8_t& hours,
                                    uint8_t& days,
                                    uint8_t& months,
                                    uint32_t& years);

        //!
        //! @return the current date/time in "DD/MM/YY HH:MM:SS" format
        static std::string getTimeStampString();
        static std::wstring getTimeStampWString();

        //! @parameter timeToBeConverted milliseconds since the start of 1970
        //! @return the date/time in "DD/MM/YY HH:MM:SS" format
        static std::string getTimeStampString(const uint64_t timestamp);
        static std::wstring getTimeStampWString(const uint64_t timestamp);

        //!
        //! @return the current date/time in "DD/MM/YY HH:MM:SS .MS" format
        static std::string getTimeStampStringWithMicroseconds();
        static std::wstring getTimeStampWStringWithMicroseconds();

        //! @parameter timeToBeConverted milliseconds since the start of 1970
        //! @return the date/time in "DD/MM/YY HH:MM:SS" format
        static std::string getTimeStampStringWithMicroseconds(const uint64_t timestamp);
        static std::wstring getTimeStampWStringWithMicroseconds(const uint64_t timestamp);

        //!
        //! @return the current date/time in "DD/MM/YY HH:MM:SS .MS" format
        static std::string getTimeStampStringWithMilliseconds();
        static std::wstring getTimeStampWStringWithMilliseconds();

        //! @parameter timeToBeConverted milliseconds since the start of 1970
        //! @return the date/time in "DD/MM/YY HH:MM:SS" format
        static std::string getTimeStampStringWithMilliseconds(const uint64_t timestamp);
        static std::wstring getTimeStampWStringWithMilliseconds(const uint64_t timestamp);

        //!
        //! sets the current time
        //static bool setCurrentTime(const unsigned char hour,
        //                           const unsigned char minute,
        //                           const unsigned char second);

        //!
        //! @return the date/time in "YYYYMMDD_HHMMSS" format
        static std::string getFileTimeStampString();
        //static std::tstring getFileTimeStampTString();

        //!
        //! @return the current date in "DD/MM/YY" format
        static std::string getCurrentDateString();
        //static std::tstring getCurrentDateTString();

        //!
        //! @return the current time in "HH:MM:SS" format
        static std::string getCurrentTimeString();
        //static std::tstring getCurrentTimeTString();

        //!
        //! @return the current time in "HH:MM" format
        static std::string getCurrentPartialTimeString();

        ////!
        ////! sets the current time
        //static bool setCurrentTime(const unsigned char hour,
        //                           const unsigned char minute,
        //                           const unsigned char second);

        //!
        //! get the current time
        static void getCurrentTime(unsigned char& hour,
                                   unsigned char& minute,
                                   unsigned char& second);
        // --------------------------------------------------------------------------------

        // The fileToString method returns the contents of a file as a string.
        static bool fileToString(std::string& string, const tchar* filePath);

        // --------------------------------------------------------------------------------

        //!
        //! converts the given string to lower-case
        static std::string toLowerCase(const std::string& string);

        //!
        //! converts the given string to upper case
        static std::string toUpperCase(const std::string& string);

        //! searches a string for all occurences of a specific string, and replaces it
        //! with another string
        //! @param input - the string to search
        //! @param output - the string into which the result will be stored
        //! @param find - the text to be replaced
        //! @param replace - the text which will be used to replace the found text
        static bool findAndReplace(const std::string& input,
                                   std::string& output,
                                   const std::string& find,
                                   const std::string& replaceString);

        static bool findAndReplace(const std::wstring& input,
                                   std::wstring& output,
                                   const std::wstring& find,
                                   const std::wstring& replaceString);

        //! searches a string for all occurences of a specific string, and replaces it
        //! with another string
        //! @param input - the string to search
        //! @param find - the text to be replaced
        //! @param replace - the text which will be used to replace the found text
        //! @return the output string
        static std::string findAndReplace(const std::string& input,
                                          const std::string& find,
                                          const std::string& replaceString);

        static std::tstring insert(const std::tstring& input,
                           const std::tstring& find,
                           const std::tstring& replaceString);

        static std::tstring removeNewLineCharacters(const std::tstring& text);

        static const char* getFilenameExtension(const char *pFilename);

        static std::string convertMACAddressToString(uint64_t address);
        static bool convertStringToMACAddress(const std::string& address, uint64_t& result);

    private:

        //!
        //! default constructor, not implemented
        Utils(void);

        //!
        //! destructor, not implemented
        virtual ~Utils();


        //!
        //! copy constructor, not implemented
        Utils(const Utils& rhs);
        //!
        //! copy assignment operator, not implemented
        Utils& operator=(const Utils& rhs);


        //!
        //! inline function, used for MACRO REPLACEMENT
        static unsigned short BYTE_CAST(const unsigned char x) {
            return static_cast<unsigned short>(static_cast<BYTE>(x));
        }

        static ::Clock* m_pClock;
};

template<typename T>
inline T left_trim(const T& src, const T& to_trim)
{
    if (!src.length())
        return src;
    size_t pos = src.find_first_not_of(to_trim);
    if (pos != T::npos)
        return src.substr(pos);
    else
        return "";
}

template<typename T>
inline T right_trim(const T& src, const T& to_trim)
{
    if (!src.length())
        return src;
    size_t pos = src.find_last_not_of(to_trim);
    if (pos != T::npos)
        return src.substr(0, pos + 1);
    else
        return "";
}

template<typename T>
inline T trim(const T& src, const T& to_trim)
{
    return right_trim(left_trim(src, to_trim), to_trim);
}

//The same set but with char*
template<typename T>
inline T left_trim(const T& src, const char* to_trim)
{
    if (!src.length())
        return src;
    size_t pos = src.find_first_not_of(to_trim);
    if (pos != T::npos)
        return src.substr(pos);
    else
        return "";
}

template<typename T>
inline T right_trim(const T& src, const char* to_trim)
{
    if (!src.length())
        return src;
    size_t pos = src.find_last_not_of(to_trim);
    if (pos != T::npos)
        return src.substr(0, pos + 1);
    else
        return "";
}

template<typename T>
inline T trim(const T& src, const char* to_trim)
{
    return right_trim(left_trim(src, to_trim), to_trim);
}

#endif // _UTILS_H_
