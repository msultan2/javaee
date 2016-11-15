#include "stdafx.h"
#include "utils.h"

#include "clock.h"
#include "fastdatapacket.h"
#include "os_utilities.h"

#include <iostream>
#include <sstream>
#include <algorithm>
#include <cassert>
#include <cctype>
#include <fstream>
#include <climits>
#include <new>
#include <string.h>

#ifdef _WIN32
//#define _WINSOCKAPI_
#include <windows.h>
#else
#include <ctime>
#endif


// TODO: these macros should be replaced by inline functions.
#define SHL(x,y) ((x) <<= (y))
#define SHR(x,y) ((x) >>= (y))


#define INCREASE_LOG_TIME_ACCURACY

namespace
{
    const unsigned long MAX_LONG = 0xFFFFFFFFL;
    const unsigned long MAX_BYTE = 0xFFL;

    const unsigned int LOWER_BYTE_MASK = 0x00FFU;
    const unsigned int LOWER_BYTE_MASK_L = 0x000000FFUL;

    const int CONS_LSB      = 0x0f;
    const int INDEX_ZERO =  0;
    const int INDEX_ONE =   1;
    const int INDEX_TWO =   2;
    const int INDEX_THREE = 3;
    const int CONS_ONE =    1;
    const int CONS_TWO =    2;
    const int CONS_THREE =  3;
    const int CONS_FOUR =   4;
    const int CONS_SEVEN =  7;
    const int CONS_NINE =   9;
    const int CONS_EIGHT =  8;
    const int CONS_TEN =    10;
    const int CONS_FIFTEEN = 15;
    const int CONS_SIXTEEN = 16;
    const int CONS_EIGHTEEN = 18;
    const int CONS_TWENTY_ONE = 21;
    const int CONS_TWENTY_FOUR =24;
    const int CONS_FIVE_ONE_ONE = 511;
    const int CONS_DIGIT_SIZE = 4;
    const int CONS_DIGIT_OF_A_BYTE = 0x0f;
    const unsigned int LETTER_ZERO   =   '0';
    const unsigned int LETTER_ONE   =    '1';
    const unsigned int LETTER_SEVEN  =   '7';
    const unsigned int LETTER_NINE   =   '9';
    const unsigned int LETTER_A      =   'A';
    const unsigned int LETTER_F      =   'F';
    const unsigned int LETTER_SA     =   'a';
    const unsigned int LETTER_SF     =   'f';
    const unsigned int SPACE         =   ' ';
    const unsigned int TAB           =   '\t';

    const char CARRIAGE_RETURN = '\r'; //0x0D
    const char LINE_FEED = '\n'; //0x0A

    const char HEX_CHARACTERS[] = "0123456789ABCDEF";


    const int HUNDRED = 100;
    const int NINETEEN_HUNDRED = 1900;
    const int NINETEEN_HUNDRED_SEVENTY = 1970;

    const uint8_t JANUARY   = 0x00;
    const uint8_t FEBRUARY  = 0x01;
    const uint8_t MARCH     = 0x02;
    const uint8_t APRIL     = 0x03;
    const uint8_t MAY       = 0x04;
    const uint8_t JUNE      = 0x05;
    const uint8_t JULY      = 0x06;
    const uint8_t AUGUST    = 0x07;
    const uint8_t SEPTEMBER = 0x08;
    const uint8_t OCTOBER   = 0x09;
    const uint8_t NOVEMBER  = 0x0a;
    const uint8_t DECEMBER  = 0x0b;

    const uint64_t NUMBER_OF_MICROSECONDS_PER_MILLISECOND = 1000ull;
    const uint64_t NUMBER_OF_MILLISECONDS_PER_SECOND = 1000ull;
    const uint64_t NUMBER_OF_SECONDS_PER_MINUTE = 60ull;
    const uint64_t NUMBER_OF_MINUTES_PER_HOUR = 60ull;
    const uint64_t NUMBER_OF_HOURS_PER_DAY = 24ull;
    const uint64_t NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER = 31ull;
    const uint64_t NUMBER_OF_DAYS_IN_APRIL_JUNE_SEPTEMBER_NOVEMBER = 30ull;
    const uint64_t NUMBER_OF_DAYS_IN_NORMAL_YEAR = 365ull;
    const uint64_t NUMBER_OF_DAYS_IN_LEAP_YEAR = 366ull;
    const uint64_t NUMBER_OF_DAYS_IN_FEBRUARY_IN_NORMAL_YEAR = 28ull;
    const uint64_t NUMBER_OF_DAYS_IN_FEBRUARY_IN_LEAP_YEAR = 29ull;

    const uint64_t NUMBER_OF_MICROSECONDS_PER_SECOND =
        NUMBER_OF_MICROSECONDS_PER_MILLISECOND *
        NUMBER_OF_MILLISECONDS_PER_SECOND;
    const uint64_t NUMBER_OF_SECONDS_PER_HOUR =
        NUMBER_OF_MINUTES_PER_HOUR *
        NUMBER_OF_SECONDS_PER_MINUTE;
    const uint64_t NUMBER_OF_SECONDS_PER_DAY =
        NUMBER_OF_HOURS_PER_DAY *
        NUMBER_OF_SECONDS_PER_HOUR;

    const uint64_t FOUR_HUNDRED = 400ull;
    const uint64_t NUMBER_OF_DAYS_PER_FOUR_HUNDRED_YEARS = (303ull * NUMBER_OF_DAYS_IN_NORMAL_YEAR) +
                                                           (97ull  * NUMBER_OF_DAYS_IN_LEAP_YEAR);

    const char MODULE_NAME [] = "Utils";
} // namespace


const unsigned int Utils::SHIFT_ONE_BYTE    = 8u;
const unsigned int Utils::SHIFT_TWO_BYTES   = 16u;
const unsigned int Utils::SHIFT_THREE_BYTES = 24u;
const unsigned int Utils::SHIFT_FOUR_BYTES  = 32u;
const unsigned int Utils::SHIFT_FIVE_BYTES  = 40u;
const unsigned int Utils::SHIFT_SIX_BYTES   = 48u;
const unsigned int Utils::SHIFT_SEVEN_BYTES = 56u;


::Clock* Utils::m_pClock = 0;

uint8_t Utils::readUInt8 (const uint8_t* const dataBuffer, const size_t offset)
{
    return dataBuffer[offset];
}

uint16_t Utils::readUInt16(const uint8_t* const dataBuffer, const size_t offset)
{
    uint16_t result = 0u;

    result += static_cast<uint16_t>(dataBuffer[offset     ]);
    result += static_cast<uint16_t>(dataBuffer[offset + 1u] << SHIFT_ONE_BYTE);

    return result;
}

uint32_t Utils::readUInt24(const uint8_t* const dataBuffer, const size_t offset)
{
    uint32_t result = 0u;

    result += static_cast<uint32_t>(dataBuffer[offset     ]);
    result += static_cast<uint32_t>(dataBuffer[offset + 1u] << SHIFT_ONE_BYTE  );
    result += static_cast<uint32_t>(dataBuffer[offset + 2u] << SHIFT_TWO_BYTES );

    return result;
}

uint32_t Utils::readUInt32(const uint8_t* const dataBuffer, const size_t offset)
{
    uint32_t result = 0u;

    result += static_cast<uint32_t>(dataBuffer[offset     ]);
    result += static_cast<uint32_t>(dataBuffer[offset + 1u] << SHIFT_ONE_BYTE  );
    result += static_cast<uint32_t>(dataBuffer[offset + 2u] << SHIFT_TWO_BYTES );
    result += static_cast<uint32_t>(dataBuffer[offset + 3u] << SHIFT_THREE_BYTES);

    return result;
}

uint64_t Utils::readUInt64(const uint8_t* const dataBuffer, const size_t offset)
{
    uint64_t result = 0u;

    result += static_cast<uint64_t>(dataBuffer[offset     ]);
    result += static_cast<uint64_t>(dataBuffer[offset + 1u]) << SHIFT_ONE_BYTE;
    result += static_cast<uint64_t>(dataBuffer[offset + 2u]) << SHIFT_TWO_BYTES;
    result += static_cast<uint64_t>(dataBuffer[offset + 3u]) << SHIFT_THREE_BYTES;
    result += static_cast<uint64_t>(dataBuffer[offset + 4u]) << SHIFT_FOUR_BYTES;
    result += static_cast<uint64_t>(dataBuffer[offset + 5u]) << SHIFT_FIVE_BYTES;
    result += static_cast<uint64_t>(dataBuffer[offset + 6u]) << SHIFT_SIX_BYTES;
    result += static_cast<uint64_t>(dataBuffer[offset + 7u]) << SHIFT_SEVEN_BYTES;

    return result;
}

uint8_t Utils::readUInt8(const FastDataPacket& dataPacket, const size_t offset)
{
    return readUInt8(dataPacket.data(), offset);
}

uint16_t Utils::readUInt16(const FastDataPacket& dataPacket, const size_t offset)
{
    return readUInt16(dataPacket.data(), offset);
}

uint32_t Utils::readUInt32(const FastDataPacket& dataPacket, const size_t offset)
{
    return readUInt32(dataPacket.data(), offset);
}


void Utils::shortToCharPtrLE(unsigned short param, BYTE* szResult)
{
    unsigned int temp = 0;
    szResult[0] = static_cast<BYTE>(param & LOWER_BYTE_MASK);
    temp = static_cast<unsigned int>(param);
    temp = temp & 0xff00U;
    //temp SHR 8U;
    SHR(temp, 8U);
    szResult[1] = static_cast<BYTE>(temp);
}

unsigned short Utils::charPtrToShortLE(const BYTE* szParam)
{
    unsigned short result = 0;
    unsigned short temp = 0;

    result = result + BYTE_CAST(szParam[0]);

    temp = BYTE_CAST(szParam[1]);
    //temp SHL 8U;
    SHL(temp, 8U);
    result = result + (temp);

    return result;
}

void Utils::longToCharPtrLE(unsigned long param, BYTE* szResult)
{
    unsigned long temp = 0;

    szResult[0] = static_cast<BYTE>(param & LOWER_BYTE_MASK_L);

    temp = param &0x0000ff00U;
    //temp SHR 8U;
    SHR(temp, 8U);
    szResult[1] = static_cast<BYTE>(temp);

    temp = param & 0x00ff0000U;
    //temp SHR 16U;
    SHR(temp, 16U);
    szResult[2] = static_cast<BYTE>(temp);

    temp = param & 0xff000000U;
    //temp SHR 24U;
    SHR(temp, 24U);
    szResult[3] = static_cast<BYTE>(temp);
}

unsigned long Utils::charPtrToLongLE(const BYTE* szParam)
{
    unsigned long result = 0;
    unsigned long tempLong = 0;

    result += BYTE_CAST(szParam[0]);

    tempLong = BYTE_CAST(szParam[1]);
    //tempLong SHL 8U;
    SHL(tempLong, 8U);
    result += tempLong;

    tempLong = BYTE_CAST(szParam[2]);
    //tempLong SHL 16U;
    SHL(tempLong, 16U);
    result += tempLong;

    tempLong = BYTE_CAST(szParam[3]);
    //tempLong SHL 24U;
    SHL(tempLong, 24U);
    result += tempLong;

    return result;
}

const char* Utils::shortToStringTwoDigits(const unsigned short x)
{
    //This function is mainly used for rendering of time and dates
    //so optimisation goes towards the use case
    static const size_t TABLE_SIZE = 100;
    static const char* RESULT[TABLE_SIZE] = {
        "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59",
        "60", "61", "62", "63", "64", "65", "66", "67", "68", "69",
        "70", "71", "72", "73", "74", "75", "76", "77", "78", "79",
        "80", "81", "82", "83", "84", "85", "86", "87", "88", "89",
        "90", "91", "92", "93", "94", "95", "96", "97", "98", "99",
    };

    if (x <= TABLE_SIZE)
    {
        return RESULT[static_cast<size_t>(x)];
    }
    else
    {
        return "??";
    }
}

const wchar_t* Utils::shortToWStringTwoDigits(const unsigned short x)
{
    //This function is mainly used for rendering of time and dates
    //so optimisation goes towards the use case
    static const size_t TABLE_SIZE = 100;
    static const wchar_t* RESULT[TABLE_SIZE] = {
        L"00", L"01", L"02", L"03", L"04", L"05", L"06", L"07", L"08", L"09",
        L"10", L"11", L"12", L"13", L"14", L"15", L"16", L"17", L"18", L"19",
        L"20", L"21", L"22", L"23", L"24", L"25", L"26", L"27", L"28", L"29",
        L"30", L"31", L"32", L"33", L"34", L"35", L"36", L"37", L"38", L"39",
        L"40", L"41", L"42", L"43", L"44", L"45", L"46", L"47", L"48", L"49",
        L"50", L"51", L"52", L"53", L"54", L"55", L"56", L"57", L"58", L"59",
        L"60", L"61", L"62", L"63", L"64", L"65", L"66", L"67", L"68", L"69",
        L"70", L"71", L"72", L"73", L"74", L"75", L"76", L"77", L"78", L"79",
        L"80", L"81", L"82", L"83", L"84", L"85", L"86", L"87", L"88", L"89",
        L"90", L"91", L"92", L"93", L"94", L"95", L"96", L"97", L"98", L"99"
    };

    if (x <= TABLE_SIZE)
    {
        return RESULT[static_cast<size_t>(x)];
    }
    else
    {
        return L"??";
    }
}

std::string Utils::shortToStringThreeDigits(unsigned short param)
{
    std::ostringstream resultStream;
    resultStream.width(3);
    resultStream.fill('0');
    resultStream << param;
    return resultStream.str();
}

std::wstring Utils::shortToWStringThreeDigits(unsigned short param)
{
    std::wostringstream resultStream;
    resultStream.width(3);
    resultStream.fill(L'0');
    resultStream << param;
    return resultStream.str();
}

//std::string Utils::longToString(unsigned long value)
//{
//    std::ostringstream result;
//    result << std::dec << value;
//    return result.str();
//}
//
//std::tstring Utils::longToTString(unsigned long value)
//{
//    std::tostringstream result;
//    result << std::dec << value;
//    return result.str();
//}
//
std::string Utils::intToString(const int value)
{
    //std::ostringstream result;
    //result << std::dec << value;
    //return result.str();
    return int64ToString(static_cast<int64_t>(value));
}

std::wstring Utils::intToWString(const int value)
{
    //std::tostringstream result;
    //result << std::dec << value;
    //return result.str();
    return int64ToWString(static_cast<int64_t>(value));
}

std::string Utils::int64ToString(
    const int64_t value,
    const ConversionType conversionBase,
    const unsigned int width,
    const char fillCharacter)
{
    std::string result;
    uint64_t absValue = 0;

    if (value < 0)
    {
        result += '-';
        absValue = static_cast<uint64_t>(-value);
    }
    else
    {
        absValue = static_cast<uint64_t>(value);
    }

    result += uint64ToString(
        absValue,
        conversionBase,
        width,
        fillCharacter);

    return result;
}

std::string Utils::uint64ToString(
    const uint64_t value,
    const ConversionType conversionBase,
    const unsigned int width,
    const char fillCharacter)
{
    uint64_t base = 0;
    uint64_t divider = 0;

    switch (conversionBase)
    {
        case OCT:
        {
            base = 8;
            divider = 0x8000000000000000ull;

            break;
        }
        case DEC:
        {
            base = 10;
            divider = 10000000000000000000ull;

            break;
        }
        case HEX:
        {
            base = 16;
            divider = 0x1000000000000000ull;

            break;
        }
        default:
        {
            //Logger::logSoftwareException(
            //    MODULE_NAME,
            //    "intToString",
            //    "function has not been implemented for this base");

            return "";
        }
    }
    assert(divider < UINT64_MAX_VALUE);
    assert(divider > UINT64_MAX_VALUE / base);

    std::string resultWithoutPaddingCharacters;
    uint64_t residuum = value;

    if (residuum == 0)
    {
        resultWithoutPaddingCharacters = "0";
    }
    else
    {
        uint64_t characterASCIICode = 0;

        while (divider > residuum)
        {
            divider /= base;
        }

        while (divider > 0)
        {
            characterASCIICode = *(HEX_CHARACTERS + static_cast<size_t>(residuum / divider));
            resultWithoutPaddingCharacters += static_cast<char>(characterASCIICode);

            residuum %= divider;
            divider  /= base;
        }
    }

    if (width>0)
    {
        std::string resultWithPaddingCharacters;
        resultWithPaddingCharacters.reserve(width);

        const size_t CURRENT_LENGTH = resultWithoutPaddingCharacters.size();
        assert(CURRENT_LENGTH <= width);

        const size_t NUMBER_OF_MISSING_PADDING_CHARACTERS = width - CURRENT_LENGTH;

        for (unsigned int i=0; i<NUMBER_OF_MISSING_PADDING_CHARACTERS; ++i)
        {
            resultWithPaddingCharacters += fillCharacter;
        }

        resultWithPaddingCharacters += resultWithoutPaddingCharacters;

        return resultWithPaddingCharacters;
    }
    else
    {
        return resultWithoutPaddingCharacters;
    }
}

std::wstring Utils::int64ToWString(
    const int64_t value,
    const ConversionType conversionBase,
    const unsigned int width,
    const wchar_t fillCharacter)
{
    std::wstring result;
    uint64_t absValue = 0;

    if (value < 0)
    {
        result += L'-';
        absValue = static_cast<uint64_t>(-value);
    }
    else
    {
        absValue = static_cast<uint64_t>(value);
    }

    result += uint64ToWString(
        absValue,
        conversionBase,
        width,
        fillCharacter);

    return result;
}

std::wstring Utils::uint64ToWString(
    const uint64_t value,
    const ConversionType conversionBase,
    const unsigned int width,
    const wchar_t fillCharacter)
{
    uint64_t base = 0;
    uint64_t divider = 0;

    switch (conversionBase)
    {
        case OCT:
        {
            base = 8;
            divider = 0x8000000000000000ull;

            break;
        }
        case DEC:
        {
            base = 10;
            divider = 10000000000000000000ull;

            break;
        }
        case HEX:
        {
            base = 16;
            divider = 0x1000000000000000ull;

            break;
        }
        default:
        {
            //Logger::logSoftwareException(
            //    MODULE_NAME,
            //    "intToString",
            //    "function has not been implemented for this base");

            return L"";
        }
    }
    assert(divider < UINT64_MAX_VALUE);
    assert(divider > UINT64_MAX_VALUE / base);

    std::wstring resultWithoutPaddingCharacters;
    uint64_t residuum = value;

    if (residuum == 0)
    {
        resultWithoutPaddingCharacters = L"0";
    }
    else
    {
        uint64_t characterASCIICode = 0;

        while (divider > residuum)
        {
            divider /= base;
        }

        while (divider > 0)
        {
            characterASCIICode = *(HEX_CHARACTERS + static_cast<size_t>(residuum / divider));
            resultWithoutPaddingCharacters += static_cast<char>(characterASCIICode);

            residuum %= divider;
            divider  /= base;
        }
    }

    if (width>0)
    {
        std::wstring resultWithPaddingCharacters;
        resultWithPaddingCharacters.reserve(width);

        const size_t CURRENT_LENGTH = resultWithoutPaddingCharacters.size();
        assert(CURRENT_LENGTH <= width);

        const size_t NUMBER_OF_MISSING_PADDING_CHARACTERS = width - CURRENT_LENGTH;

        for (unsigned int i=0; i<NUMBER_OF_MISSING_PADDING_CHARACTERS; ++i)
        {
            resultWithPaddingCharacters += fillCharacter;
        }

        resultWithPaddingCharacters += resultWithoutPaddingCharacters;

        return resultWithPaddingCharacters;
    }
    else
    {
        return resultWithoutPaddingCharacters;
    }
}

std::string Utils::floatToString(const float value)
{
    std::ostringstream result;
    result << value;
    return result.str();
}

std::string Utils::byteToString(const unsigned char param)
{
    return shortToStringTwoDigits(static_cast<unsigned short>(param));
}

unsigned long Utils::stringToUnsignedLong(const std::string& string)
{
    unsigned long result = 0;

    // Using the istringstream to convert a string to a decimal.
    std::istringstream iss(string);

    // Create temp int to receive conversion.
    unsigned long temp = 0;

    // Convert.
    const bool CONVERT_OK = !(iss >> std::dec >> temp).fail();
    if (CONVERT_OK)
    {
        // Conversion ok so assigned to output.
        result = temp;
    }
    else
    {
        // conversion failed
        result = 0;
    }

    return result;
}

unsigned short Utils::stringToUnsignedShort(const std::string& string)
{
    return static_cast<unsigned short>(stringToUnsignedLong(string));
}

//----------------------------------------------------------------------------
bool Utils::isThisAnAllowedCharacter(
    const unsigned int character,
    const unsigned long base)
{
    bool result =
          (character == SPACE) //space
          ||
          (character == TAB) //tabulator
          ||

          ((base == HEX) &&
            (
              ( (character >= LETTER_ZERO) &&
                (character <= LETTER_NINE) ) //0..9
              ||
              ( (character >= LETTER_SA) &&
                (character <= LETTER_SF) ) //a..f
              ||
              ( (character >= LETTER_A) &&
                (character <= LETTER_F) ) //A..F
            )
          )
          ||
          ((base == DEC) &&
              (
                  (character >= LETTER_ZERO) &&
                  (character <= LETTER_NINE) //0..9
              )
          )
          ||
          ((base == OCT) &&
              (
                  (character >= LETTER_ZERO) &&
                  (character <= LETTER_SEVEN) //0..7
              )
          )
          ||
          ((base == BIN) &&
              (
                  (character == LETTER_ZERO) ||
                  (character == LETTER_ONE) //0..1
              )
          );

    return result;
}

//----------------------------------------------------------------------------
bool Utils::stringToInt64(
                          const std::string& input,
                          int64_t& number)
{
    bool result = false;

    if (input.size()>0)
    {
        bool negativeNumber = (input[0] == '-');
        uint64_t unsignedNumber = 0;
        std::string inputWithNoSign;

        if (negativeNumber)
        {
            inputWithNoSign = (input.substr(1, input.size() - 1));
        }
        else
        {
            inputWithNoSign = input;
        }

        result = stringToUInt64(inputWithNoSign, unsignedNumber, DEC);

        if (negativeNumber)
        {
            if (unsignedNumber < static_cast<uint64_t>(-1LL))
            {
                number = static_cast<int64_t>(unsignedNumber) * (-1LL);
            }
            else
            {
                //number too big
                result = false;
            }
        }
        else
        {
            number = unsignedNumber;
        }
    }
    //else do nothing

    return result;
}

//----------------------------------------------------------------------------
bool Utils::stringToUInt64(
                           const std::string& input,
                           uint64_t& number,
                           const ConversionType conversionBase)
{
    // This function is based on hexStringToDecimal by George Chiao.
    // The core of this function has been reused by RG and generalised.

    const unsigned long base = static_cast<unsigned long>(conversionBase); // - the base of the conversion
    bool result = true; // result of the function
    const size_t INPUT_LENGTH = input.size();
    unsigned int character = '\0';
    uint64_t tmpNumber = 0; //temporary value used during calculations
    uint64_t lastTmpNumber = 0; //temporary value used to evaluate overflow
    bool anyDigitFound = false;
    bool endHasBeenReached = false;
    bool spaceAfterNumberHasBeenFound = false;
    bool overflowError = false;
    bool notAllowedCharFound = false;

    if (
        (INPUT_LENGTH == 0)
        ||
        (   (base != static_cast<int>(BIN)) &&
        (base != static_cast<int>(OCT)) &&
        (base != static_cast<int>(DEC)) &&
        (base != static_cast<int>(HEX)) )
        )
    {
        result = false;
    }
    else
    {
        uint64_t maxNotOverflownNumberBeforeBaseMultiplication = UINT64_MAX_VALUE/base;
        unsigned int digit = 0;
        unsigned int i = 0; // - index when iterating over the string
        while ((result) && (!endHasBeenReached))
        {
            //iterate over each character
            character = static_cast<unsigned int>(input[i]);

            //check if the character is an allowed digit
            if (isThisAnAllowedCharacter(character, base))
            {
                //this character is an allowed digit. Calculate the corresponding number for this digit

                if ((character >= LETTER_ZERO) &&
                    (character <= LETTER_NINE) )
                { // 0..9
                    digit = character - LETTER_ZERO;
                    anyDigitFound = true;
                }
                else
                {
                    if ((character >= LETTER_SA) &&
                        (character <= LETTER_SF) )
                    { // a..f
                        digit = character - LETTER_SA + CONS_TEN;
                        anyDigitFound = true;
                    }
                    else
                    {
                        if ((character >= LETTER_A) &&
                            (character <= LETTER_F) )

                        { // A..F
                            digit = character - LETTER_A + CONS_TEN;
                            anyDigitFound = true;
                        }
                        else
                        { //space or tabulator
                            if (anyDigitFound)
                            {
                                spaceAfterNumberHasBeenFound = true;
                            }
                            else
                            {
                                // do nothing. Leading spaces
                            }
                        }
                    }
                }

                // add found digit to the result if conditions apply
                if (anyDigitFound && !spaceAfterNumberHasBeenFound)
                {
                    //Check if overflow can happen during this iteration
                    if (tmpNumber > maxNotOverflownNumberBeforeBaseMultiplication)
                    {
                        overflowError = true;
                    }
                    else
                    {
                        //do nothing. Overflow can not happen yet
                    }

                    //multiply this digit by base and add to the result
                    lastTmpNumber = tmpNumber;
                    tmpNumber = base*tmpNumber + static_cast<uint64_t>(digit);

                    if (tmpNumber < lastTmpNumber)
                    {
                        result = false;

                        break;
                    }
                    //else do nothing
                }
                else
                {
                    //do nothing
                }

                //check if it is time to finish the loop
                i++;
                if ((i>=INPUT_LENGTH) || (anyDigitFound && spaceAfterNumberHasBeenFound))
                {
                    endHasBeenReached = true;
                }
                else
                {
                    //do nothing. Some characters still have to be processed.
                }
            }
            else
            {
                //this is not an allowed digit
                notAllowedCharFound = true;
            }

            result = !(notAllowedCharFound || overflowError);
        } //while
    }

    if (result)
    {
        //no errors during conversion
        number = tmpNumber;
    }
    else
    {
        //error
        number = 0;
    }

    return result;
}

//----------------------------------------------------------------------------

bool Utils::stringToUInt(
                         const std::string& input,
                         unsigned int& number,
                         const ConversionType conversionBase)
{
    bool ok = true;
    uint64_t tmpResult;
    bool tmpOk = stringToUInt64(input, tmpResult, conversionBase);

    if (tmpOk && (tmpResult <= UINT_MAX))
    {
        number = static_cast<unsigned int>(tmpResult);
    }
    else
    {
        ok = false;
    }

    return ok;
}

//----------------------------------------------------------------------------

bool Utils::stringToInt(
                         const std::string& input,
                         int& number,
                         const ConversionType conversionBase)
{
    if (conversionBase != DEC)
    {
        return false;
    }
    //else continue

    bool ok = true;
    int64_t tmpResult;
    bool tmpOk = stringToInt64(input, tmpResult);

    if (tmpOk && (tmpResult <= INT_MAX) && (tmpResult >= INT_MIN))
    {
        number = static_cast<int>(tmpResult);
    }
    else
    {
        ok = false;
    }

    return ok;
}

//----------------------------------------------------------------------------

bool Utils::decToInt(const std::string& input, unsigned int& number, const bool removeTrailings)
{
    unsigned int temp = 0;
    bool result = false;

    if (removeTrailings)
    {
        std::string preprocessedInput(removeTrailingLF(input));
        result = stringToUInt(preprocessedInput, temp, DEC);
    }
    else
    {
        result = stringToUInt(input, temp, DEC);
    }

    //range checking is not necessary as long as "int" and "long" types
    //are 4 byte long on 32 bit systems
    number = static_cast<unsigned int>(temp);

    return result;
}

//----------------------------------------------------------------------------
bool Utils::decToByte(const std::string& input, unsigned char& number, const bool )
{
    unsigned int temp = 0;

    bool result = stringToUInt(input, temp, DEC);

    if (temp <=static_cast<unsigned>(MAX_BYTE))
    {
        number = static_cast<unsigned char>(temp);
    }
    else
    {
        result = false;
        number = 0;
    }

    return result;
}

//----------------------------------------------------------------------------
bool Utils::octalToInt(const std::string& input, unsigned int& number)
{
    unsigned int temp = 0;

    bool result = stringToUInt(input, temp, OCT);

    //range checking is not necessary as long as "int" and "long" types
    //are 4 byte long on 32 bit systems
    number = static_cast<unsigned int>(temp);

    return result;
}

//----------------------------------------------------------------------------
bool Utils::octalToShort(const std::string& input, unsigned short& number)
{
    unsigned int temp = 0;

    bool result = stringToUInt(input, temp, OCT);

    if (result && (temp <=static_cast<unsigned int>(USHRT_MAX)))
    {
        number = static_cast<unsigned short>(temp);
    }
    else
    {
        result = false;
        number = 0;
    }

    return result;
}

//----------------------------------------------------------------------------
bool Utils::octalToByte(const std::string& input, unsigned char& number)
{
    unsigned int temp = 0;

    bool result = stringToUInt(input, temp, OCT);

    if (temp <=static_cast<unsigned>(MAX_BYTE))
    {
        number = static_cast<unsigned char>(temp);
    }
    else
    {
        result = false;
        number = 0;
    }

    return result;
}

//----------------------------------------------------------------------------
bool Utils::hexToInt(const std::string& input, unsigned int& number, const bool removeTrailings)
{
    unsigned int temp = 0;
    bool result = false;

    if (removeTrailings)
    {
        std::string preprocessedInput(removeTrailingLF(input));
        result = stringToUInt(preprocessedInput, temp, HEX);
    }
    else
    {
        result = stringToUInt(input, temp, HEX);
    }

    //range checking is not necessary as long as "int" and "long" types
    //are 4 byte long on 32 bit systems
    number = static_cast<unsigned int>(temp);

    return result;
}

//----------------------------------------------------------------------------
bool Utils::hexToShort(const std::string& input, unsigned short& number)
{
    unsigned int temp = 0;

    bool result = stringToUInt(input, temp, HEX);

    if (result && (temp <=static_cast<unsigned int>(USHRT_MAX)))
    {
        number = static_cast<unsigned short>(temp);
    }
    else
    {
        result = false;
        number = 0;
    }

    return result;
}

//----------------------------------------------------------------------------
bool Utils::hexToByte(const std::string& input, unsigned char& number, const bool removeTrailings)
{
    unsigned int temp = 0;

    bool result = hexToInt(input, temp, removeTrailings);

    if (result && (temp <= static_cast<unsigned char>(MAX_BYTE)))
    {
        number = static_cast<BYTE>(temp);
    }
    else
    {
        result = false;
        number = 0;
    }
    return result;
}
//----------------------------------------------------------------------------
std::string Utils::removeTrailingLF(const std::string& text)
{
    std::string result;

    if (text.size() > 2)
    {
        if ( (static_cast<int>(text[text.size() - 2]) == static_cast<int>(CARRIAGE_RETURN)) &&
             (static_cast<int>(text[text.size() - 1]) == static_cast<int>(LINE_FEED)) )
        {
            result = text.substr(0, text.size() - 2);
        }
        else
        {
            result = text;
        }
    }
    else
    {
        result = text;
    }

    return result;
}

//----------------------------------------------------------------------------

void Utils::setClock(::Clock* clockPtr)
{
    m_pClock = clockPtr;
}

::Clock* Utils::getClock()
{
    return m_pClock;
}

bool Utils::isLeapYear(const uint32_t years)
{
    const uint64_t ABSOLUTE_YEAR = 1970ull + static_cast<uint64_t>(years);

    const bool YEAR_DIVISIBLE_BY_4   = ((ABSOLUTE_YEAR % 4ull) == 0ull);
    const bool YEAR_DIVISIBLE_BY_100 = ((ABSOLUTE_YEAR % 100ull) == 0ull);
    const bool YEAR_DIVISIBLE_BY_400 = ((ABSOLUTE_YEAR % 400ull) == 0ull);

    return (YEAR_DIVISIBLE_BY_400 || (YEAR_DIVISIBLE_BY_4 && (!YEAR_DIVISIBLE_BY_100)));
}

void Utils::decodeTimeStamp(const uint64_t timestamp,
                            uint16_t& microseconds,
                            uint16_t& milliseconds,
                            uint8_t& seconds,
                            uint8_t& minutes,
                            uint8_t& hours,
                            uint8_t& days,
                            uint8_t& months,
                            uint32_t& years)
{
    // We can work out the number of milliseconds, seconds, minutes and hours instantaneously.
    uint32_t longYears;
    bool yearsOverflow = false;

    microseconds = static_cast<uint16_t>((timestamp                                    ) % NUMBER_OF_MICROSECONDS_PER_MILLISECOND);
    milliseconds = static_cast<uint16_t>((timestamp / NUMBER_OF_MICROSECONDS_PER_MILLISECOND) % NUMBER_OF_MILLISECONDS_PER_SECOND);
    const uint32_t timestampInSeconds = (uint32_t)(timestamp / NUMBER_OF_MICROSECONDS_PER_SECOND);
    seconds      = static_cast<uint8_t> ( timestampInSeconds                                      % NUMBER_OF_SECONDS_PER_MINUTE);
    minutes      = static_cast<uint8_t> ((timestampInSeconds / NUMBER_OF_SECONDS_PER_MINUTE) % NUMBER_OF_MINUTES_PER_HOUR);
    hours        = static_cast<uint8_t> ((timestampInSeconds / NUMBER_OF_SECONDS_PER_HOUR  ) % NUMBER_OF_HOURS_PER_DAY);

    // The values for days, months and years depends on leap year calculations.
    uint32_t daysToCount = (timestampInSeconds / NUMBER_OF_SECONDS_PER_DAY);

    months = 0u;
    days = 0u;

    longYears = (daysToCount / NUMBER_OF_DAYS_PER_FOUR_HUNDRED_YEARS) * FOUR_HUNDRED;
    daysToCount = (daysToCount % NUMBER_OF_DAYS_PER_FOUR_HUNDRED_YEARS);

    if (longYears >= static_cast<uint64_t>(UINT32_MAX_VALUE - NINETEEN_HUNDRED_SEVENTY))
    {
        yearsOverflow = true;
    }
    else
    {
        years = static_cast<uint32_t>(longYears);

        while (daysToCount > 0ull)
        {
            const bool IS_LEAP_YEAR = isLeapYear(years);

            const uint64_t NUMBER_OF_DAYS_IN_YEAR = (IS_LEAP_YEAR)
                ? (NUMBER_OF_DAYS_IN_LEAP_YEAR)
                : (NUMBER_OF_DAYS_IN_NORMAL_YEAR);

            if (daysToCount >= NUMBER_OF_DAYS_IN_YEAR)
            {
                if (years == UINT32_MAX_VALUE - NINETEEN_HUNDRED_SEVENTY)
                {
                    yearsOverflow = true;
                    break;
                }
                else
                {
                    ++years;
                    daysToCount -= NUMBER_OF_DAYS_IN_YEAR;
                }
            }
            else
            {
                const uint64_t NUMBER_OF_DAYS_IN_FEBRUARY = (IS_LEAP_YEAR)
                    ? (NUMBER_OF_DAYS_IN_FEBRUARY_IN_LEAP_YEAR)
                    : (NUMBER_OF_DAYS_IN_FEBRUARY_IN_NORMAL_YEAR);

                const uint64_t NUMBER_OF_DAYS_INCLUDING_JANUARY   = (1ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_FEBRUARY  = (1ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_MARCH     = (2ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_APRIL     = (2ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_APRIL_JUNE_SEPTEMBER_NOVEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_MAY       = (3ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_APRIL_JUNE_SEPTEMBER_NOVEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_JUNE      = (3ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (2ull * NUMBER_OF_DAYS_IN_APRIL_JUNE_SEPTEMBER_NOVEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_JULY      = (4ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (2ull * NUMBER_OF_DAYS_IN_APRIL_JUNE_SEPTEMBER_NOVEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_AUGUST    = (5ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (2ull * NUMBER_OF_DAYS_IN_APRIL_JUNE_SEPTEMBER_NOVEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_SEPTEMBER = (5ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (3ull * NUMBER_OF_DAYS_IN_APRIL_JUNE_SEPTEMBER_NOVEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_OCTOBER   = (6ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (3ull * NUMBER_OF_DAYS_IN_APRIL_JUNE_SEPTEMBER_NOVEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);
                const uint64_t NUMBER_OF_DAYS_INCLUDING_NOVEMBER  = (6ull * NUMBER_OF_DAYS_IN_JANUARY_MARCH_MAY_JULY_AUGUST_OCTOBER_DECEMBER) +
                                                                    (4ull * NUMBER_OF_DAYS_IN_APRIL_JUNE_SEPTEMBER_NOVEMBER) +
                                                                    (1ull * NUMBER_OF_DAYS_IN_FEBRUARY);

                if (daysToCount < NUMBER_OF_DAYS_INCLUDING_JANUARY)
                {
                    months = JANUARY;
                    days = static_cast<uint8_t>(daysToCount);
                }
                else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_FEBRUARY)
                {
                    months = FEBRUARY;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_JANUARY);
                }
                else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_MARCH)
                {
                    months = MARCH;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_FEBRUARY);
                }
                 else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_APRIL)
                {
                    months = APRIL;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_MARCH);
                }
                else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_MAY)
                {
                    months = MAY;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_APRIL);
                }
                else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_JUNE)
                {
                    months = JUNE;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_MAY);
                }
                else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_JULY)
                {
                    months = JULY;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_JUNE);
                }
                else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_AUGUST)
                {
                    months = AUGUST;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_JULY);
                }
                else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_SEPTEMBER)
                {
                    months = SEPTEMBER;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_AUGUST);
                }
                else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_OCTOBER)
                {
                    months = OCTOBER;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_SEPTEMBER);
                }
                else if (daysToCount < NUMBER_OF_DAYS_INCLUDING_NOVEMBER)
                {
                    months = NOVEMBER;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_OCTOBER);
                }
                else
                {
                    months = DECEMBER;
                    days = static_cast<uint8_t>(daysToCount - NUMBER_OF_DAYS_INCLUDING_NOVEMBER);
                }

                break;
            }
        }
    }

    if (yearsOverflow)
    {
        years = UINT32_MAX_VALUE - NINETEEN_HUNDRED_SEVENTY;
        months = 11u;
        days = 30u;
        hours = 23u;
        minutes = 59u;
        seconds = 59u;
    }
    // else do nothing.
}

std::string Utils::getTimeStampString()
{
    assert(m_pClock != 0);
    const uint64_t CURRENT_TIME = m_pClock->getMillisecondsSinceEpoch();
    return getTimeStampString(CURRENT_TIME);
}

std::wstring Utils::getTimeStampWString()
{
    assert(m_pClock != 0);
    const uint64_t CURRENT_TIME = m_pClock->getMillisecondsSinceEpoch();
    return getTimeStampWString(CURRENT_TIME);
}

std::string Utils::getTimeStampString(const uint64_t timestamp)
{
    uint16_t microseconds, milliseconds;
    uint8_t seconds, minutes, hours, days, months;
    uint32_t years;

    decodeTimeStamp(timestamp,
        microseconds, milliseconds, seconds, minutes, hours, days, months, years);

    std::string ss;

    ss += shortToStringTwoDigits(static_cast<short>(days + 1));
    ss += ("/");
    ss += shortToStringTwoDigits(static_cast<short>(months + 1));
    ss += ("/");
    ss += intToString(years + NINETEEN_HUNDRED_SEVENTY);
    ss += (" ");
    ss += shortToStringTwoDigits(static_cast<short>(hours));
    ss += (":");
    ss += shortToStringTwoDigits(static_cast<short>(minutes));
    ss += (":");
    ss += shortToStringTwoDigits(static_cast<short>(seconds));

    return ss;
}

std::wstring Utils::getTimeStampWString(const uint64_t timestamp)
{
    uint16_t microseconds, milliseconds;
    uint8_t seconds, minutes, hours, days, months;
    uint32_t years;

    decodeTimeStamp(timestamp,
        microseconds, milliseconds, seconds, minutes, hours, days, months, years);

    std::wstring ss;
    ss.reserve(256);

    ss += shortToWStringTwoDigits(static_cast<short>(days + 1));
    ss += L"/";
    ss += shortToWStringTwoDigits(static_cast<short>(months + 1));
    ss += L"/";
    ss += intToWString(years + NINETEEN_HUNDRED_SEVENTY);
    ss += L" ";
    ss += shortToWStringTwoDigits(static_cast<short>(hours));
    ss += L":";
    ss += shortToWStringTwoDigits(static_cast<short>(minutes));
    ss += L":";
    ss += shortToWStringTwoDigits(static_cast<short>(seconds));

    return ss;
}

std::string Utils::getTimeStampStringWithMicroseconds()
{
    assert(m_pClock != 0);
    const uint64_t CURRENT_TIME = m_pClock->getMicrosecondsSinceEpoch();
    return getTimeStampStringWithMicroseconds(CURRENT_TIME);
}

std::wstring Utils::getTimeStampWStringWithMicroseconds()
{
    if (m_pClock != 0)
    {
        const uint64_t CURRENT_TIME = m_pClock->getMicrosecondsSinceEpoch();
        return getTimeStampWStringWithMicroseconds(CURRENT_TIME);
    }
    else
    {
        return getTimeStampWStringWithMicroseconds(0);
    }
}

std::string Utils::getTimeStampStringWithMilliseconds()
{
    if (m_pClock != 0)
    {
        const uint64_t CURRENT_TIME = m_pClock->getMicrosecondsSinceEpoch();
        return getTimeStampStringWithMilliseconds(CURRENT_TIME);
    }
    else
    {
        return getTimeStampStringWithMilliseconds(0);
    }
}

std::wstring Utils::getTimeStampWStringWithMilliseconds()
{
    assert(m_pClock != 0);
    const uint64_t CURRENT_TIME = m_pClock->getMicrosecondsSinceEpoch();
    return getTimeStampWStringWithMilliseconds(CURRENT_TIME);
}

std::string Utils::getTimeStampStringWithMicroseconds(const uint64_t timestamp)
{
    uint16_t microseconds, milliseconds;
    uint8_t seconds, minutes, hours, days, months;
    uint32_t years;

    decodeTimeStamp(timestamp, microseconds, milliseconds, seconds, minutes, hours, days, months, years);
    std::string ss;

    ss += shortToStringTwoDigits(static_cast<short>(days + 1));
    ss += ("/");
    ss += shortToStringTwoDigits(static_cast<short>(months + 1));
    ss += ("/");
    ss += intToString(years + NINETEEN_HUNDRED_SEVENTY);
    ss += (" ");
    ss += shortToStringTwoDigits(static_cast<short>(hours));
    ss += (":");
    ss += shortToStringTwoDigits(static_cast<short>(minutes));
    ss += (":");
    ss += shortToStringTwoDigits(static_cast<short>(seconds));

    ss += (".");
    ss += int64ToString(static_cast<int64_t>(microseconds+1000*milliseconds), DEC, 6, '0');

    return ss;
}

std::wstring Utils::getTimeStampWStringWithMicroseconds(const uint64_t timestamp)
{
    uint16_t microseconds, milliseconds;
    uint8_t seconds, minutes, hours, days, months;
    uint32_t years;

    decodeTimeStamp(timestamp, microseconds, milliseconds, seconds, minutes, hours, days, months, years);
    std::wstring ss;

    ss += shortToWStringTwoDigits(static_cast<short>(days + 1));
    ss += L"/";
    ss += shortToWStringTwoDigits(static_cast<short>(months + 1));
    ss += L"/";
    ss += intToWString(years + NINETEEN_HUNDRED_SEVENTY);
    ss += L" ";
    ss += shortToWStringTwoDigits(static_cast<short>(hours));
    ss += L":";
    ss += shortToWStringTwoDigits(static_cast<short>(minutes));
    ss += L":";
    ss += shortToWStringTwoDigits(static_cast<short>(seconds));

    ss += L".";
    ss += int64ToWString(static_cast<int64_t>(microseconds+1000*milliseconds), DEC, 6, '0');

    return ss;
}

std::string Utils::getTimeStampStringWithMilliseconds(const uint64_t timestamp)
{
    uint16_t microseconds, milliseconds;
    uint8_t seconds, minutes, hours, days, months;
    uint32_t years;

    decodeTimeStamp(timestamp, microseconds, milliseconds, seconds, minutes, hours, days, months, years);

    std::string ss;

    ss += shortToStringTwoDigits(static_cast<short>(days + 1));
    ss += ("/");
    ss += shortToStringTwoDigits(static_cast<short>(months + 1));
    ss += ("/");
    ss += intToString(years + NINETEEN_HUNDRED_SEVENTY);
    ss += (" ");
    ss += shortToStringTwoDigits(static_cast<short>(hours));
    ss += (":");
    ss += shortToStringTwoDigits(static_cast<short>(minutes));
    ss += (":");
    ss += shortToStringTwoDigits(static_cast<short>(seconds));

    ss += (".");
    ss += int64ToString(static_cast<int64_t>(milliseconds), DEC, 3, '0');

    return ss;
}

std::wstring Utils::getTimeStampWStringWithMilliseconds(const uint64_t timestamp)
{
    uint16_t microseconds, milliseconds;
    uint8_t seconds, minutes, hours, days, months;
    uint32_t years;

    decodeTimeStamp(timestamp,
        microseconds, milliseconds, seconds, minutes, hours, days, months, years);

    std::wstring ss;

    ss += shortToWStringTwoDigits(static_cast<short>(days + 1));
    ss += L"/";
    ss += shortToWStringTwoDigits(static_cast<short>(months + 1));
    ss += L"/";
    ss += intToWString(years + NINETEEN_HUNDRED_SEVENTY);
    ss += L" ";
    ss += shortToWStringTwoDigits(static_cast<short>(hours));
    ss += L":";
    ss += shortToWStringTwoDigits(static_cast<short>(minutes));
    ss += L":";
    ss += shortToWStringTwoDigits(static_cast<short>(seconds));

    ss += L".";
    ss += int64ToWString(static_cast<int64_t>(milliseconds), DEC, 3, _T('0'));

    return ss;
}

std::string Utils::getFileTimeStampString()
{
#ifdef _WIN32
    ::SYSTEMTIME currentTime = {0};
    ::GetLocalTime(&currentTime);
    std::string dateString;
    dateString += intToString(currentTime.wYear);
    dateString += shortToStringTwoDigits(currentTime.wMonth);
    dateString += shortToStringTwoDigits(currentTime.wDay);
    dateString += "_";
    dateString += shortToStringTwoDigits(currentTime.wHour);
    dateString += shortToStringTwoDigits(currentTime.wMinute);
    dateString += shortToStringTwoDigits(currentTime.wSecond);
    return dateString;
#else
    time_t rawTime = time(0);
    struct tm* localTime = localtime(&rawTime);

    const unsigned short BUFF_SIZE = 16;
    char buff[BUFF_SIZE];
    strftime(buff, BUFF_SIZE+1, "_%Y%m%d_%H%M%S", localTime);
    std::string dateString(buff);
    return dateString;
#endif
}

//std::tstring Utils::getFileTimeStampTString()
//{
//#ifdef _WIN32
//    SYSTEMTIME currentTime = {0};
//    GetLocalTime(&currentTime);
//    std::tstring dateString;
//    dateString += intToTString(currentTime.wYear);
//    dateString += shortToWStringTwoDigits(currentTime.wMonth);
//    dateString += shortToWStringTwoDigits(currentTime.wDay);
//    dateString += _T("_");
//    dateString += shortToWStringTwoDigits(currentTime.wHour);
//    dateString += shortToWStringTwoDigits(currentTime.wMinute);
//    dateString += shortToWStringTwoDigits(currentTime.wSecond);
//    return dateString;
//#else
//    time_t rawTime = time(0);
//    struct tm* localTime = localtime(&rawTime);
//
//    const unsigned short BUFF_SIZE = 16;
//    char buff[BUFF_SIZE];
//    strftime(buff, BUFF_SIZE+1, "_%Y%m%d_%H%M%S", localTime);
//    std::string dateString(buff);
//    return dateString;
//#endif
//}

std::string Utils::getCurrentDateString()
{
#ifdef _WIN32
    SYSTEMTIME currentTime = {0};
    GetLocalTime(&currentTime);
    std::string dateString;
    dateString += shortToStringTwoDigits(currentTime.wDay);
    dateString += "/";
    dateString += shortToStringTwoDigits(currentTime.wMonth);
    dateString += "/";
    dateString += shortToStringTwoDigits(currentTime.wYear - 2000U);
    return dateString;
#else
    time_t rawTime = time(0);
    struct tm* localTime = localtime(&rawTime);

    const unsigned short BUFF_SIZE = 9;
    char buff[BUFF_SIZE];
    strftime(buff, BUFF_SIZE+1, "%d/%m/%y", localTime);
    std::string dateString(buff);
    return dateString;
#endif
}

//std::tstring Utils::getCurrentDateTString()
//{
//#ifdef _WIN32
//    SYSTEMTIME currentTime = {0};
//    GetLocalTime(&currentTime);
//    std::tstring dateString;
//    dateString += shortToWStringTwoDigits(currentTime.wDay);
//    dateString += _T("/");
//    dateString += shortToWStringTwoDigits(currentTime.wMonth);
//    dateString += _T("/");
//    dateString += shortToWStringTwoDigits(currentTime.wYear - 2000U);
//    return dateString;
//#else
//    time_t rawTime = time(0);
//    struct tm* localTime = localtime(&rawTime);
//
//    const unsigned short BUFF_SIZE = 9;
//    char buff[BUFF_SIZE];
//    strftime(buff, BUFF_SIZE+1, "%d/%m/%y", localTime);
//    std::string dateString(buff);
//    return OS_Utilities::StringToTString(dateString);
//#endif
//}

std::string Utils::getCurrentTimeString()
{
#ifdef _WIN32
    SYSTEMTIME currentTime = {0};
    GetLocalTime(&currentTime);
    std::string timeString;
    timeString += shortToStringTwoDigits(currentTime.wHour);
    timeString += ":";
    timeString += shortToStringTwoDigits(currentTime.wMinute);
    timeString += ":";
    timeString += shortToStringTwoDigits(currentTime.wSecond);

#ifdef INCREASE_LOG_TIME_ACCURACY
    timeString += ".";
    timeString += shortToStringThreeDigits(currentTime.wMilliseconds);
#endif //INCREASE_LOG_TIME_ACCURACY

    return timeString;
#else
    time_t rawTime = time(0);
    struct tm* localTime = localtime(&rawTime);

    const unsigned short BUFF_SIZE = 8;
    char buff[BUFF_SIZE];
    strftime(buff, BUFF_SIZE+1, "%X", localTime);
    std::string timeString(buff);
    return timeString;
#endif
}

//std::tstring Utils::getCurrentTimeTString()
//{
//#ifdef _WIN32
//    SYSTEMTIME currentTime = {0};
//    GetLocalTime(&currentTime);
//    std::tstring timeString;
//    timeString += shortToWStringTwoDigits(currentTime.wHour);
//    timeString += _T(":");
//    timeString += shortToWStringTwoDigits(currentTime.wMinute);
//    timeString += _T(":");
//    timeString += shortToWStringTwoDigits(currentTime.wSecond);
//
//#ifdef INCREASE_LOG_TIME_ACCURACY
//    timeString += _T('.');
//    timeString += shortToWStringThreeDigits(currentTime.wMilliseconds);
//#endif //INCREASE_LOG_TIME_ACCURACY
//
//    return timeString;
//#else
//    time_t rawTime = time(0);
//    struct tm* localTime = localtime(&rawTime);
//
//    const unsigned short BUFF_SIZE = 8;
//    tchar buff[BUFF_SIZE];
//    _tcsftime(buff, BUFF_SIZE+1, "%X", localTime);
//    std::tstring timeString(buff);
//    return timeString;
//#endif
//}

std::string Utils::getCurrentPartialTimeString()
{
    return getCurrentTimeString().substr(0,5);
}

//bool Utils::setCurrentTime(const unsigned char hour,
//                           const unsigned char minute,
//                           const unsigned char second)
//{
//    bool setOk = false;
//
//#ifdef _WIN32
//    // get the current time!
//    SYSTEMTIME currentTime = {0};
//    GetLocalTime(&currentTime);
//
//    // replace the current hour, minute and second
//    currentTime.wHour = hour;
//    currentTime.wMinute = minute;
//    currentTime.wSecond = second;
//
//    // set the current time, SetLocalTime will return non-zero on success
//    setOk = (SetLocalTime(&currentTime) != 0);
//#else
//    //!
//    //! LINUX - TODO
//    //!
//#endif
//
//
//    return setOk;
//}

void Utils::getCurrentTime(unsigned char& hour,
                           unsigned char& minute,
                           unsigned char& second)
{
#ifdef _WIN32
    // get the current time!
    ::SYSTEMTIME currentTime = {0};
    GetLocalTime(&currentTime);

    // replace the current hour, minute and second
    hour = static_cast<unsigned char>(currentTime.wHour);
    minute = static_cast<unsigned char>(currentTime.wMinute);
    second = static_cast<unsigned char>(currentTime.wSecond);
#else
    time_t rawTime = time(0);
    struct tm* localTime = localtime(&rawTime);

    hour = static_cast<unsigned char>(localTime->tm_hour);
    minute = static_cast<unsigned char>(localTime->tm_min);
    second = static_cast<unsigned char>(localTime->tm_sec);
#endif
}

//----------------------------------------------------------------------------
bool Utils::fileToString(std::string& str, const tchar* filePath)
{
    bool toOk = false;
    std::ifstream file(
        OS_Utilities::StringToAnsi(filePath).c_str(),
        std::ios::binary | std::ios::in);

    if (!file)
    {
        // No action needed, error - file could not be opened, return false.
    }
    else
    {
        // Get the length of the file (first set position to end, the get the
        // current position, then return the position to the beginning).
        file.seekg (0, std::ios::end);
        std::streampos length(file.tellg());

        //! @fixme RG: Mind that for very big files this function may not work correctly,
        //! as the type streampos and streamsize do not match!

        file.seekg (0, std::ios::beg);

        // Allocate memory for a temporary buffer.
        char* bufferPtr = new(std::nothrow) char[length];

        if (0 != bufferPtr)
        {
            // Read data from the file and fill buffer.
            file.read(bufferPtr, static_cast<std::streamsize>(length));

            // Transfer buffer data to the output string.
            str.assign(bufferPtr, static_cast<size_t>(length));

            // Destroy temp buffer.
            delete[] bufferPtr;

            toOk = true;
        }
        else
        {
            // No action needed, could not allocate memory, return false.
        }
    }

    return toOk;
}

//----------------------------------------------------------------------------
std::string Utils::toLowerCase(const std::string& string)
{
    std::string result(string);
    std::transform(result.begin(), result.end(), result.begin(), tolower);
    return result;
}

std::string Utils::toUpperCase(const std::string& string)
{
    std::string result(string);
    std::transform(result.begin(), result.end(), result.begin(), toupper);
    return result;

}

bool Utils::findAndReplace(const std::string& input,
                           std::string&       output,
                           const std::string& find,
                           const std::string& replaceString)
{
    bool foundAndReplaced = false;
    output = input;

    // Check the input string and find string are valid.
    if (!find.empty())
    {
        // Assign the output string.
        const size_t SIZEOF_INPUT = input.size();
        const size_t SIZEOF_OUTPUT = replaceString.size();
        const size_t MAX_SIZEOF_OUTPUT = SIZEOF_INPUT * SIZEOF_OUTPUT;
        output.reserve(MAX_SIZEOF_OUTPUT);
        const size_t SIZE_OF_FIND = find.size();
        bool noMoreInstancesFound = false;

        // Loop through the input character searching for the find string, if found
        // replace it with the replaceString string.
        size_t k = 0;
        while (!noMoreInstancesFound)
        {
            // Try to find the find string from the current position.
            size_t positionOfTheFirstOccurrence = output.find(find, k);

            if (std::string::npos != positionOfTheFirstOccurrence)
            {
                // Found an instance of the find string in the output string, replace
                // in with the replaceString string.
                output = output.replace(positionOfTheFirstOccurrence, SIZE_OF_FIND, replaceString);

                // Move to the next character after the current instance position.
                k = positionOfTheFirstOccurrence + 1;

                foundAndReplaced = true;
            }
            else
            {
                // No more instances of the find string found, exit the loop.
                noMoreInstancesFound = true;
            }
        } // End while loop.
    }
    else
    {
        // No action needed, the input string is empty, return false.
    }

    return foundAndReplaced;
}

bool Utils::findAndReplace(const std::wstring& input,
                           std::wstring& output,
                           const std::wstring& find,
                           const std::wstring& replaceString)
{
    bool foundAndReplaced = false;
    output = input;

    // Check the input string and find string are valid.
    if (!find.empty())
    {
        // Assign the output string.
        const size_t SIZEOF_INPUT = input.size();
        const size_t SIZEOF_OUTPUT = replaceString.size();
        const size_t MAX_SIZEOF_OUTPUT = SIZEOF_INPUT * SIZEOF_OUTPUT;
        output.reserve(MAX_SIZEOF_OUTPUT);
        const size_t SIZE_OF_FIND = find.size();
        bool noMoreInstancesFound = false;

        // Loop through the input character searching for the find string, if found
        // replace it with the replaceString string.
        size_t k = 0;
        while (!noMoreInstancesFound)
        {
            // Try to find the find string from the current position.
            size_t positionOfTheFirstOccurrence = output.find(find, k);

            if (std::string::npos != positionOfTheFirstOccurrence)
            {
                // Found an instance of the find string in the output string, replace
                // in with the replaceString string.
                output = output.replace(positionOfTheFirstOccurrence, SIZE_OF_FIND, replaceString);

                // Move to the next character after the current instance position.
                k = positionOfTheFirstOccurrence + 1;

                foundAndReplaced = true;
            }
            else
            {
                // No more instances of the find string found, exit the loop.
                noMoreInstancesFound = true;
            }
        } // End while loop.
    }
    else
    {
        // No action needed, the input string is empty, return false.
    }

    return foundAndReplaced;
}

std::string Utils::findAndReplace(const std::string& input,
                                  const std::string& find,
                                  const std::string& replaceString)
{
    std::string output;
    findAndReplace(input, output, find, replaceString);
    return output;
}

std::tstring Utils::insert(const std::tstring& input,
                           const std::tstring& find,
                           const std::tstring& replaceString)
{
    std::tstring inputString(input);

    std::tstring::size_type pos = 0;

    while(std::tstring::npos != (pos = inputString.find(find, pos)))
    {
         inputString.insert(pos, replaceString);
         pos = pos + replaceString.length()+1;
    }

    return inputString;
}

std::tstring Utils::removeNewLineCharacters(const std::tstring& text)
{
    const unsigned int LENGTH_OF_STRING_TO_BE_REPLACED = 1UL;
    std::tstring result(text);

    size_t i = result.find_first_of(NEW_LINE);
    while (i < result.length())
    {
        result.replace(i, LENGTH_OF_STRING_TO_BE_REPLACED, _T(" "));

        i = result.find_first_of(NEW_LINE);
    }

    return result;
}

const char* Utils::getFilenameExtension(const char *pFilename)
{
    if (pFilename != 0)
    {
        const char *pDot = strrchr(pFilename, '.');
        if((pDot == 0) || (pDot == pFilename))
        {
            return 0;
        }
        else
        {
            return pDot + 1;
        }
    }
    else
    {
        return 0;
    }
}

std::string Utils::convertMACAddressToString(uint64_t address)
{
    std::ostringstream ss;
    ss << std::setfill('0') << std::hex << std::uppercase;
    for (int i=5; i>=0; --i)
    {
        ss << std::setw(2) << ((address >> 8*i)& 0xFF);
        if (i>0)
        {
            ss << ':';
        }
        //else do nothing
    }

    return ss.str();
}

bool Utils::convertStringToMACAddress(const std::string& address, uint64_t& result)
{
    if ( address.size() != (sizeof("XX:XX:XX:XX:XX:XX") - 1) )
        return false;

    if (
        (address[2]  != ':') ||
        (address[5]  != ':') ||
        (address[8]  != ':') ||
        (address[11] != ':') ||
        (address[14] != ':')
        )
    {
        return false;
    }
    //else continue

    bool ok = true;
    std::string tmpString;
    uint64_t tmpResult = 0;

    tmpString += address[0];
    tmpString += address[1];
    ok = ok && stringToUInt64(tmpString, tmpResult, Utils::HEX);
    if (ok)
        result |= tmpResult;

    tmpString.clear();
    tmpString += address[3];
    tmpString += address[4];
    ok = ok && stringToUInt64(tmpString, tmpResult, Utils::HEX);
    if (ok)
        result |= (tmpResult << 8);

    tmpString.clear();
    tmpString += address[6];
    tmpString += address[7];
    ok = ok && stringToUInt64(tmpString, tmpResult, Utils::HEX);
    if (ok)
        result |= (tmpResult << 16);

    tmpString.clear();
    tmpString += address[9];
    tmpString += address[10];
    ok = ok && stringToUInt64(tmpString, tmpResult, Utils::HEX);
    if (ok)
        result |= (tmpResult << 24);

    tmpString.clear();
    tmpString += address[12];
    tmpString += address[13];
    ok = ok && stringToUInt64(tmpString, tmpResult, Utils::HEX);
    if (ok)
        result |= (tmpResult << 32);

    tmpString.clear();
    tmpString += address[15];
    tmpString += address[16];
    ok = ok && stringToUInt64(tmpString, tmpResult, Utils::HEX);
    if (ok)
        result |= (tmpResult << 40);

    return ok;
}
