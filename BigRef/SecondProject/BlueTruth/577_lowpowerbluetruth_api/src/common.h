#ifndef _COMMON_H_
#define _COMMON_H_

/** @file */

//#include "base_64_encoder.h"
#include "bootloader_api.h"
//#include "rfc1321.h"
#include <stddef.h>
#include <stdint.h>
#ifdef __XC__
#include <xc.h>
#endif


#ifdef __cplusplus
extern "C" {
#endif


//#define AUTO_PSV __attribute__((auto_psv))


/**
 @brief Wrapper for printf function.

 In the case of PIC microcontroller all output will be redirected to UART4.
 In other cases it will be redirected to stdout.
 */
#ifndef USE_BOOTLOADER
void print(const char* text);
#else
#define print(text) (*p_print)(text)
#endif

/**
 @brief Wrapper for printf function.

 In the case of PIC microcontroller all output will be redirected to UART4.
 In other cases it will be redirected to stdout.
 */
#ifndef USE_BOOTLOADER
void printc(const char c);
#else
#define printc(c) (*p_printc)(c)
#endif

/**
  @brief Wrapper for fflush function.

  Wait until all outstanding data is written to the screen (UART4)
 */
#ifndef USE_BOOTLOADER
void flush_print(void);
#else
#define flush_print() (*p_flush_print)()
#endif



/**
  @brief Wrapper for report_error function.

  Update PIC board LED error status
 */
#ifndef USE_BOOTLOADER
void report_error(const EErrorSignallingSignal _signal, const bool value);
#else
#define report_error(_signal, value) (*p_report_error)(_signal, value)
#endif



enum {
    BIN = 2,
    OCT = 8,
    DEC = 10,
    HEX = 16
};

/**
 * Convert the 4-byte unsigned integer to a string
 *
 * @param value value to be converted
 * @param conversionBase binary/decimal or hexadecimal
 * @param pResult array where to put result
 * @return number of written characters
 */
#ifndef USE_BOOTLOADER
size_t uint32_to_string(
    const uint32_t value,
    const int conversionBase,
    char *pResult);
#else
#define uint32_to_string(value, conversionBase, pResult) \
            (*p_uint32_to_string)(value, conversionBase, pResult)
#endif

/**
 * Convert the 4-byte signed integer to a string using decimal conversion
 *
 * @param value value to be converted
 * @param pResult array where to put result
 * @return number of written characters
 */
#ifndef USE_BOOTLOADER
size_t int32_to_string(
    const int32_t value,
    char *pResult);
#else
#define int32_to_string(value, pResult) \
            (*p_int32_to_string)(value, pResult)
#endif

/**
 * Convert the 1-byte unsigned integer to a string
 *
 * @param value value to be converted
 * @param conversionBase binary/decimal or hexadecimal
 * @param pResult array where to put result
 * @return number of written characters
 */
#ifndef USE_BOOTLOADER
size_t uint8_to_string(
    const uint8_t value,
    const int conversionBase,
    char *pResult);
#else
#define uint8_to_string(value, conversionBase, pResult) \
            (*p_uint8_to_string)(value, conversionBase, pResult)
#endif


/**
    @brief Convert ascii hex nibble to hex nibble value

    @param[in] Nibble = ascii char
    @return hex nibble value (0x0-0xF)
*/
#ifndef USE_BOOTLOADER
uint8_t hexChar_to_nibble(const uint8_t nibble);
#else
#define hexChar_to_nibble(nibble) \
            (*p_hexChar_to_nibble)(nibble)
#endif



typedef enum {
    eBIN = 2,
    eOCT = 8,
    eDEC = 10,
    eHEX = 16
} EConversionType;

/**
Checks if the character is one of the allowed for this base

@param character - the character to be analysed
@param conversionBase - the base of conversion. Only 2, 8, 10 and 16 values are allowed
@return - true if allowed, false otherwise
*/
bool is_this_an_allowed_character(
    const int character,
    const EConversionType base);


/**
    Convert the value contained in a string in decimal into a unsigned int64 (8 bytes)
    Checks are performed to verify that the string is a valid number.
    @param input - the string to be converted
    @param inputSize - the size of string to be converted
    @param pNumber - reference parameter into which the converted value will be stored
    @return true if the string is a valid number
  */
#ifndef USE_BOOTLOADER
bool string_to_uint64(
    const char* pInput,
    const size_t inputSize,
    const int conversionBase,
    uint64_t* pNumber);
#else
#define string_to_uint64(pInput, inputSize, conversionBase, pNumber) \
    p_string_to_uint64(pInput, inputSize, conversionBase, pNumber)
#endif

/**
    Convert the value contained in a string in decimal into a unsigned int32 (4 bytes)
    Checks are performed to verify that the string is a valid number.
    @param input - the string to be converted
    @param inputSize - the size of string to be converted
    @param pNumber - reference parameter into which the converted value will be stored
    @return true if the string is a valid number
  */
#ifndef USE_BOOTLOADER
bool string_to_uint32(
    const char* pInput,
    const size_t inputSize,
    const int conversionBase,
    uint32_t* pNumber);
#else
#define string_to_uint32(pInput, inputSize, conversionBase, pNumber) \
    p_string_to_uint32(pInput, inputSize, conversionBase, pNumber)
#endif


/**
    Convert the value contained in a string in decimal into a signed int64 (8 bytes)
    Checks are performed to verify that the string is a valid number.
    @param input - the string to be converted
    @param inputSize - the size of string to be converted
    @param pNumber - reference parameter into which the converted value will be stored
    @return true if the string is a valid number
  */
#ifndef USE_BOOTLOADER
bool string_to_int64(
    const char* pInput,
    const size_t inputSize,
    int64_t* pNumber);
#else
#define string_to_int64(pInput, inputSize, pNumber) \
    p_string_to_int64(pInput, inputSize, pNumber)
#endif

/**
    Convert the value contained in a string in decimal into a signed int32 (4 bytes)
    Checks are performed to verify that the string is a valid number.
    @param input - the string to be converted
    @param inputSize - the size of string to be converted
    @param pNumber - reference parameter into which the converted value will be stored
    @return true if the string is a valid number
  */
#ifndef USE_BOOTLOADER
bool string_to_int32(
    const char* pInput,
    const size_t inputSize,
    int32_t* pNumber);
#else
#define string_to_int32(pInput, inputSize, pNumber) \
    p_string_to_int32(pInput, inputSize, pNumber)
#endif

/**
    Convert the uint array to char array
    Checks are performed to verify that the string is a valid number.
    @param input - the array to be converted
    @param inputSize - the size of array to be converted
    @param pNumber - reference parameter into which the converted value will be stored
    @return true if the string is a valid number
  */
#ifndef USE_BOOTLOADER
void uint8_aray_to_char_array(
    const uint8_t* pInput,
    const size_t inputSize,
    char* pOuput);
#else
#define uint8_aray_to_char_array(pInput, inputSize, pOuput) \
    p_uint8_aray_to_char_array(pInput, inputSize, pOuput)
#endif

/**
    Convert the char array to unit array
    Checks are performed to verify that the string is a valid number.
    @param input - the array to be converted
    @param inputSize - the size of array to be converted
    @param pNumber - reference parameter into which the converted value will be stored
    @return true if the string is a valid number
  */
#ifndef USE_BOOTLOADER
void char_array_to_uint8_array(
    const char* pInput,
    const size_t inputSize,
    uint8_t* pOuput);
#else
#define char_array_to_uint8_array(pInput, inputSize, pOuput) \
    p_char_array_to_uint8_array(pInput, inputSize, pOuput)
#endif

/**
    Convert the value contained in a string to Md5hashed value (4 bytes)
    Checks are performed to verify that the string is a valid number.
    @param input - the string to be converted
    @param inputSize - the size of string to be converted
    @param pNumber - reference parameter into which the converted value will be stored
    @return true if the string is a valid number
  */
#ifndef USE_BOOTLOADER
bool string_to_Md5(
    const uint8_t* pInput,
    const size_t inputSize,
    uint8_t* pOuput);
#else
#define string_to_Md5(pInput, inputSize, pNumber) \
    p_string_to_Md5(pInput, inputSize, pNumber)
#endif


/**
    Convert the string to a base64 string
    Checks are performed to verify that the string is a valid number.
    @param input - the string to be converted
    @param inputSize - the size of string to be converted
    @param pNumber - reference parameter into which the converted value will be stored
    @return true if the string is a valid number
  */
#ifndef USE_BOOTLOADER
bool string_to_base64(
    const char* pInput,
    const size_t inputSize,
    char* pOuput);
#else
#define string_to_base64(pInput, inputSize, pNumber) \
    p_string_to_base64(pInput, inputSize, pNumber)
#endif


/**
 * Copy string from ppDest to pSrc and control the number of
 * available characters not to exceed the buffer size.
 *
 * After call the value pointed by ppDest pointer will be shifted by the
 * number of copied characters.
 *
 * @param[inout] ppDest pointer of the destination
 * @param[in] pSrc pointer of the source
 * @param[inout] pCounter the number of characters that have already been copied to the buffer
 * @param[in] maxCounter the maximum allowed number of characters that can be fit into an arraye pinted by ppDest
 * @return number of characters that have been copied
 */
#ifndef USE_BOOTLOADER
size_t strncpy_safe(char** ppDest, const char* pSrc, size_t* pCounter, const size_t maxCounter);
#else
#define strncpy_safe(ppDest, pSrc, pCounter, maxCounter) \
            (*p_strncpy_safe)(ppDest, pSrc, pCounter, maxCounter)
#endif


/**
 * @brief Copy string from pDest to pSrc.
 * The last character copied is zero and pSrc must be ended with zero.
 * This function should usually be used in the sequence of appendices to the string.
 *
 * @param[inout] pDest pointer of the destination
 * @param[in] pSrc pointer of the source
 *
 * @return the pointer to the last character different from zero in the pDest.
 */
#ifndef USE_BOOTLOADER
char* strcpy_returning_offset(char* pDest, const char* pSrc);
#else
#define strcpy_returning_offset(pDestination, pSource) \
            (*p_strcpy_returning_offset)(pDestination, pSource)
#endif


#define EOL "\x0D"
#define EOL_CHAR '\x0D'
#define CTRL_D '\x04'
#define CTRL_Z '\x1A'

#define NO_SIZE (size_t)-1

#define UNUSED(x) (void)(x)

/**
  This macro disables interrupts by increasing the current priority level to 7,
  performing the desired statement and then restoring the previous priority level.
*/
#define INTERRUPT_PROTECT(expression) { \
    char saved_ipl; \
    \
    SET_AND_SAVE_CPU_IPL(saved_ipl,7); \
    expression; \
    RESTORE_CPU_IPL(saved_ipl); } (void) 0;

/**
 * Define type of ring buffer of particular size and assign type name to it.
 * This type is thread safe as long each thread (interrupt) uses either index or
 * tail to point to the buffer.
 * To get number of characters in buffer define the relevant function with the
 * implementation like this:
 * size_t get_number_of_characters_UART1(TUART1Context* pContext)
 * {
 *     int size = pContext->bufferHead - pContext->bufferTail;
 *     if (size < 0)
 *         size += _size;
 *     return (size_t)size;
 * }
 */
#define DEF_RING_BUFFER(_size) \
    struct \
    { \
        size_t bufferHead; \
        size_t bufferTail; \
        bool transmissionPending; \
        bool overflow; \
        char buffer[_size]; \
    }

#ifdef __cplusplus
}
#endif


#ifndef max
#define max(a,b) \
   ({ __typeof__ (a) _a = (a); \
       __typeof__ (b) _b = (b); \
     _a > _b ? _a : _b; })
#endif

#ifndef min
#define min(a,b) \
   ({ __typeof__ (a) _a = (a); \
       __typeof__ (b) _b = (b); \
     _a < _b ? _a : _b; })
#endif


#define htons(A) ((((uint16_t)(A) & 0xff00) >> 8) | \
(((uint16_t)(A) & 0x00ff) << 8))
#define htonl(A) ((((uint32_t)(A) & 0xff000000) >> 24) | \
(((uint32_t)(A) & 0x00ff0000) >> 8) | \
(((uint32_t)(A) & 0x0000ff00) << 8) | \
(((uint32_t)(A) & 0x000000ff) << 24))
#define htonl64(A) (\
(((uint64_t)(A) & (uint64_t)0xff00000000000000) >> 56) | \
(((uint64_t)(A) & (uint64_t)0x00ff000000000000) >> 40) | \
(((uint64_t)(A) & (uint64_t)0x0000ff0000000000) >> 24) | \
(((uint64_t)(A) & (uint64_t)0x000000ff00000000) >> 8) | \
(((uint64_t)(A) & (uint64_t)0x00000000ff000000) << 8) | \
(((uint64_t)(A) & (uint64_t)0x0000000000ff0000) << 24) | \
(((uint64_t)(A) & (uint64_t)0x000000000000ff00) << 40) | \
(((uint64_t)(A) & (uint64_t)0x00000000000000ff) << 56))

#define ntohs htons
#define ntohl htonl
#define ntohl64 htonl64


#endif //_MENU_H_
