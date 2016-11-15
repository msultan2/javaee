#ifndef _BOOTLOADER_API_H_
#define _BOOTLOADER_API_H_


#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>


/** @file */

#ifdef __XC16__
#define SPACE_ATTRIBUTE __attribute__((space(prog)))
#else

//The __prog__ is very specific to XC16
#define __prog__

#define SPACE_ATTRIBUTE

#endif

#ifdef	__cplusplus
extern "C" {
#endif


/** Main program activity */
typedef void (*T_bootloader__initialise)(void);
extern const __prog__ T_bootloader__initialise p_bootloader__initialise SPACE_ATTRIBUTE;

typedef void (*T_bootloader__process)(void);
extern const __prog__ T_bootloader__process p_bootloader__process SPACE_ATTRIBUTE;


typedef enum
{
    eFIRMWARE_TYPE_BOOTLOADER = 1,
    eFIRMWARE_TYPE_GSM = 2,
    eFIRMWARE_TYPE_APPLICATION = 3,
} EFirmwareType;


#define FIRMWARE_DESCRIPTOR__MAGIC_NUMBER 0xB00710AD

#define FIRMWARE_DESCRIPTOR_SIZE 256
#define FIRMWARE_DESCRIPTOR__MAGIC_NUMBER_SIZE 4
#define FIRMWARE_DESCRIPTOR__FIRMWARE_TYPE_SIZE 4
#define FIRMWARE_DESCRIPTOR__MAJOR_VERSION_SIZE 4
#define FIRMWARE_DESCRIPTOR__MINOR_VERSION_SIZE 4
#define FIRMWARE_DESCRIPTOR__DESCRIPTION_SIZE 128
#define FIRMWARE_DESCRIPTOR__COMPILATION_DATE_SIZE 16
#define FIRMWARE_DESCRIPTOR__COMPILATION_TIME_SIZE 16
#define FIRMWARE_DESCRIPTOR__RESERVED_SIZE 80 //to match size of 256

#if ( \
    FIRMWARE_DESCRIPTOR__MAGIC_NUMBER_SIZE + \
    FIRMWARE_DESCRIPTOR__FIRMWARE_TYPE_SIZE + \
    FIRMWARE_DESCRIPTOR__MAJOR_VERSION_SIZE + \
    FIRMWARE_DESCRIPTOR__MINOR_VERSION_SIZE + \
    FIRMWARE_DESCRIPTOR__DESCRIPTION_SIZE + \
    FIRMWARE_DESCRIPTOR__COMPILATION_DATE_SIZE + \
    FIRMWARE_DESCRIPTOR__COMPILATION_TIME_SIZE + \
    FIRMWARE_DESCRIPTOR__RESERVED_SIZE != \
    FIRMWARE_DESCRIPTOR_SIZE \
)
#error Total firmware descriptor size mismatch. The total size of TFirmwareDescriptor should be FIRMWARE_DESCRIPTOR_SIZE
#endif

typedef struct TFirmwareDescriptor
{
    uint32_t magicNumber;
    uint32_t firmwareType;
    uint32_t majorVersion;
    uint32_t minorVersion;
    char description[FIRMWARE_DESCRIPTOR__DESCRIPTION_SIZE];
    char compilationDate[FIRMWARE_DESCRIPTOR__COMPILATION_DATE_SIZE];
    char compilationTime[FIRMWARE_DESCRIPTOR__COMPILATION_TIME_SIZE];
    uint8_t RESERVED[FIRMWARE_DESCRIPTOR__RESERVED_SIZE];
} TFirmwareDescriptor;

typedef bool (*T_version__read_firmware_descriptor)(
    const int firmwareType,
    TFirmwareDescriptor* pDescriptor);
extern const __prog__ T_version__read_firmware_descriptor p_version__read_firmware_descriptor SPACE_ATTRIBUTE;

typedef void (*T_populate_bootloader_firmware_descriptor)(struct TFirmwareDescriptor* pDescriptor);
extern const __prog__ T_populate_bootloader_firmware_descriptor p_populate_bootloader_firmware_descriptor SPACE_ATTRIBUTE;


#define PRODUCTION_DATA__MAGIC_NUMBER 0xB00810AE
#define PRODUCTION_DATA__MAGIC_NUMBER_SIZE 4
///Production data must fit into the entire program memory page and will be
///populated after production as a part of PAT
typedef struct TProductionData
{
    uint32_t magicNumber;
    uint32_t serialNumber;
    char picBoardVersion[32];
    char productionDate[16];
    uint8_t RESERVED[0x400 - 4 - 4 - 32 - 16];
} TProductionData;

typedef void (*T_production_data__populate)(TProductionData* pData);
extern const __prog__ T_production_data__populate p_production_data__populate SPACE_ATTRIBUTE;


/* Timer related functions */
typedef struct TTimer1Context
{
    uint32_t tick_1ms;
    uint32_t uptimeInSeconds;
    uint32_t tick;
} TTimer1Context;

typedef volatile const struct TTimer1Context* (*T_timer1__get_context)(void);
extern const __prog__ T_timer1__get_context p_timer1__get_context SPACE_ATTRIBUTE;

typedef TTimer1Context (*T_timer1__get_context_copy)(void);
extern const __prog__ T_timer1__get_context_copy p_timer1__get_context_copy SPACE_ATTRIBUTE;


/* Error signalling related functions */
typedef enum
{
    eERROR_SIGNAL_NO_BOOTLOADER_DESCRIPTOR = 1,
    eERROR_SIGNAL_GSM_FIRMWARE,
    eERROR_SIGNAL_APPLICATION_FIRMWARE,
    eERROR_SIGNAL_GPS_MODULE_COMMUNICATION,
    eERROR_SIGNAL_GSM_MODULE_COMMUNICATION,
    eERROR_SIGNAL_BLUETOOTH_MODULE_COMMUNICATION,
    eERROR_SIGNAL_SIZE
} EErrorSignallingSignal;

typedef void (*T_report_error)(const EErrorSignallingSignal _signal, const bool value);
extern const __prog__ T_report_error p_report_error SPACE_ATTRIBUTE;


/** Print functions using UART4 */
typedef void (*T_print)(const char* );
extern const __prog__ T_print p_print SPACE_ATTRIBUTE;

typedef void (*T_printc)(const char );
extern const __prog__ T_printc p_printc SPACE_ATTRIBUTE;

typedef void (*T_flush_print)(void);
extern const __prog__ T_flush_print p_flush_print SPACE_ATTRIBUTE;

/** Utility functions */
typedef size_t (*T_uint32_to_string)(
    const uint32_t value,
    const int conversionBase,
    char *pResult);
extern const __prog__ T_uint32_to_string p_uint32_to_string SPACE_ATTRIBUTE;

typedef size_t (*T_int32_to_string)(
    const int32_t value,
    char *pResult);
extern const __prog__ T_int32_to_string p_int32_to_string SPACE_ATTRIBUTE;

typedef size_t (*T_uint8_to_string)(
    const uint8_t value,
    const int conversionBase,
    char *pResult);
extern const __prog__ T_uint8_to_string p_uint8_to_string SPACE_ATTRIBUTE;

typedef bool (*T_string_to_uint64)(
    const char* pInput,
    const size_t inputSize,
    const int conversionBase,
    uint64_t* pNumber);
extern const __prog__ T_string_to_uint64 p_string_to_uint64 SPACE_ATTRIBUTE;

typedef bool (*T_string_to_int64)(
    const char* pInput,
    const size_t inputSize,
    int64_t* pNumber);
extern const __prog__ T_string_to_int64 p_string_to_int64 SPACE_ATTRIBUTE;

typedef bool (*T_string_to_uint32)(
    const char* pInput,
    const size_t inputSize,
    const int conversionBase,
    uint32_t* pNumber);
extern const __prog__ T_string_to_uint32 p_string_to_uint32 SPACE_ATTRIBUTE;

typedef bool (*T_string_to_int32)(
    const char* pInput,
    const size_t inputSize,
    int32_t* pNumber);
extern const __prog__ T_string_to_int32 p_string_to_int32 SPACE_ATTRIBUTE;




typedef void (*T_char_array_to_uint8_array)(
    const char* pInput,
    const size_t inputSize,
    uint8_t* pOuput);
extern const __prog__ T_char_array_to_uint8_array p_char_array_to_uint8_array SPACE_ATTRIBUTE;


typedef void (*T_uint8_aray_to_char_array)(
    const uint8_t* pInput,
    const size_t inputSize,
    char* pOuput);
extern const __prog__ T_uint8_aray_to_char_array p_uint8_aray_to_char_array SPACE_ATTRIBUTE;

typedef bool (*T_string_to_Md5)(
    const uint8_t* pInput,
    const size_t inputSize,
    uint8_t pOuput[16]);
extern const __prog__ T_string_to_Md5 p_string_to_Md5 SPACE_ATTRIBUTE;

typedef bool (*T_string_to_base64)(
    const char* pInput,
    const size_t inputSize,
    char* pOuput);
extern const __prog__ T_string_to_base64 p_string_to_base64 SPACE_ATTRIBUTE;

typedef uint8_t (*T_hexChar_to_nibble)(const uint8_t nibble);
extern const __prog__ T_hexChar_to_nibble p_hexChar_to_nibble SPACE_ATTRIBUTE;


typedef size_t (*T_strncpy_safe)(
    char** ppDest,
    const char* pSrc,
    size_t* pCounter,
    const size_t maxCounter);
extern const __prog__ T_strncpy_safe p_strncpy_safe SPACE_ATTRIBUTE;

typedef char* (*T_strcpy_returning_offset)(char* pDestination, const char* pSource);
extern const __prog__ T_strcpy_returning_offset p_strcpy_returning_offset SPACE_ATTRIBUTE;

/* Flash memory related functions */
#define SPI_PAGE_SIZE 256

typedef bool (*T_spi_flash__read)(
    const bool synchronous,
    const uint32_t address,
    const uint32_t numberOfBytes,
    uint8_t* pBuffer);
extern const __prog__ T_spi_flash__read p_spi_flash__read SPACE_ATTRIBUTE;

typedef bool (*T_spi_flash__write_page)(
    const bool synchronous,
    const uint32_t address,
    const uint32_t numberOfBytes,
    const uint8_t* pBuffer);
extern const __prog__ T_spi_flash__write_page p_spi_flash__write_page SPACE_ATTRIBUTE;

typedef bool (*T_spi_flash__erase)(
    const bool synchronous,
    const uint32_t sectorAddress);
extern const __prog__ T_spi_flash__erase p_spi_flash__erase SPACE_ATTRIBUTE;

typedef bool (*T_spi_flash__put_into_deep_power_down)(const bool synchronous);
extern const __prog__ T_spi_flash__put_into_deep_power_down p_spi_flash__put_into_deep_power_down SPACE_ATTRIBUTE;

typedef bool (*T_spi_flash__release_from_deep_power_down)(const bool synchronous);
extern const __prog__ T_spi_flash__release_from_deep_power_down p_spi_flash__release_from_deep_power_down SPACE_ATTRIBUTE;

typedef bool (*T_spi_flash__check_if_write_is_in_progress)(bool* pWipValue);
extern const __prog__ T_spi_flash__check_if_write_is_in_progress p_spi_flash__check_if_write_is_in_progress SPACE_ATTRIBUTE;

/* Program memory related functions */
typedef bool (*T_program_memory__read_data)(
    const bool synchronous,
    const uint32_t address,
    const uint32_t numberOfBytes,
    uint8_t* pBuffer);
extern const __prog__ T_program_memory__read_data p_program_memory__read_data SPACE_ATTRIBUTE;

struct TUpgradeFirmwareContext;

/* Firmware upgrade related functions */
typedef void (*T_upgrade_firmware__initialise)(struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__initialise p_upgrade_firmware__initialise SPACE_ATTRIBUTE;

typedef int (*T_upgrade_firmware__prepare1)(const int firmwareType, struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__prepare1 p_upgrade_firmware__prepare1 SPACE_ATTRIBUTE;

typedef int (*T_upgrade_firmware__prepare2)(const uint32_t startAddress, const uint32_t endAddress, struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__prepare2 p_upgrade_firmware__prepare2 SPACE_ATTRIBUTE;

typedef bool (*T_upgrade_firmware__writec)(const uint8_t c, bool* pFinish, struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__writec p_upgrade_firmware__writec SPACE_ATTRIBUTE;

typedef void (*T_upgrade_firmware__finalise)(struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__finalise p_upgrade_firmware__finalise SPACE_ATTRIBUTE;

typedef bool (*T_upgrade_firmware__calculate_checksum)(uint8_t pMD5[16], struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__calculate_checksum p_upgrade_firmware__calculate_checksum SPACE_ATTRIBUTE;

typedef void (*T_upgrade_firmware__burn_prepare)(struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__burn_prepare p_upgrade_firmware__burn_prepare SPACE_ATTRIBUTE;

typedef bool (*T_upgrade_firmware__burn)(struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__burn p_upgrade_firmware__burn SPACE_ATTRIBUTE;

typedef void (*T_upgrade_firmware__abort)(struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__abort p_upgrade_firmware__abort SPACE_ATTRIBUTE;

typedef void (*T_upgrade_firmware__restart)(struct TUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware__restart p_upgrade_firmware__restart SPACE_ATTRIBUTE;

typedef struct TUpgradeFirmwareContext* (*T_upgrade_firmware__get_context)(void);
extern const __prog__ T_upgrade_firmware__get_context p_upgrade_firmware__get_context SPACE_ATTRIBUTE;


/**
 * General enumeration related how errors are reported used by all tasks in this module
 */
enum
{
    eQUERY_RECEIVED = -1, //another way to signal query after the received message has been received
    eNO_ERROR = 0,
    eERROR_PREVIOUS_ACTION_NOT_COMPLETED,
    eERROR_INVALID_ARGUMENT,
    eERROR_UNABLE_TO_EXTRACT_PARAMETER,
    eERROR_SPI_FLASH_MEMORY_ACCESS,
    eERROR_PROGRAM_MEMORY_WRITE_ACCESS,
    eERROR_UNDEFINED_APN,
    eERROR_WHITE_LIST_NOT_EMPTY,
    eERROR_WHITE_LIST_NUMBER_NOT_FOUND,
    eERROR_WHITE_LIST_NUMBER_ALREADY_EXISTS,
    eERROR_WHITE_LIST_NO_SPACE_LEFT,
    eERROR_INTERNAL
};

#define NUMBER_OF_SECONDS_PER_MINUTE 60UL
#define NUMBER_OF_MILLISECONDS_PER_SECOND 1000

#ifdef	__cplusplus
}
#endif

#endif //_BOOTLOADER_API_H_
