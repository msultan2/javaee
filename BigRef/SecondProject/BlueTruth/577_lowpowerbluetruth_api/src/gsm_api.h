#ifndef _GSM_API_H_
#define _GSM_API_H_

#ifdef __XC16__
#else

//The __prog__ is very specific to XC16
#define __prog__

#endif

#include "bootloader_api.h"

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>


#ifdef __cplusplus
extern "C" {
#endif


struct TGsmBuffer;
struct TGprsSrvProfileContext;
struct TGsmModuleControlContext;
struct TGsmSignalLevel;
struct TGsmSmsData;
struct TTaskGprsCloseConnectionContext;
struct TTaskGprsExchangeRawDataContext;
struct TTaskGprsInitialiseProfileContext;
struct TTaskGprsDownloadFileContext;
struct TTaskGprsOpenConnectionContext;
struct TTaskGprsPostDataContext;
struct TTaskGprsProcessIncomingDataContext;
struct TTaskGsmProcessOutcomingTextContext;
struct TTaskGprsProcessOutgoingDataContext;
struct TTaskGsmProcessSimpleCommandContext;
struct TTaskGsmShutdownContext;
struct TTaskPeriodicallyGetSignalLevelContext;
struct TTaskUpgradeFirmwareContext;


/** Main program activity */
typedef void (*T_gsm__main)(void);
extern const __prog__ T_gsm__main p_gsm__main SPACE_ATTRIBUTE;

typedef void (*T_gsm__configure_pins)(void);
extern const __prog__ T_gsm__configure_pins p_gsm__configure_pins SPACE_ATTRIBUTE;

typedef void (*T_gsm__initialise)(void);
extern const __prog__ T_gsm__initialise p_gsm__initialise SPACE_ATTRIBUTE;

typedef void (*T_gsm__process)(void);
extern const __prog__ T_gsm__process p_gsm__process SPACE_ATTRIBUTE;

typedef bool (*T_gsm__can_use_gsm_modem)(void);
extern const __prog__ T_gsm__can_use_gsm_modem p_gsm__can_use_gsm_modem SPACE_ATTRIBUTE;

typedef void (*T_populate_gsm_firmware_descriptor)(struct TFirmwareDescriptor* pDescriptor);
extern const __prog__ T_populate_gsm_firmware_descriptor p_populate_gsm_firmware_descriptor SPACE_ATTRIBUTE;

typedef bool (*T_gsm_sms__send_message)(
    const char* pPhoneNumber,
    const char* pTextToSend);
extern const __prog__ T_gsm_sms__send_message p_gsm_sms__send_message SPACE_ATTRIBUTE;


#define URL_SIZE 145


/* GSM SMS white list related context */
#define GSM_SMS_WHITE_LIST_SIZE 8
#define GSM_PHONE_NUMBER_SIZE 16
#define GSM_TIME_STRING_SIZE 64
typedef struct
{
    unsigned int numberOfEntries;
    char whiteList[GSM_SMS_WHITE_LIST_SIZE][GSM_PHONE_NUMBER_SIZE];
} TGsmSmsWhitelistContext;

typedef TGsmSmsWhitelistContext* (*T_gsm_sms_whitelist__get_context)(void);
extern const __prog__ T_gsm_sms_whitelist__get_context p_gsm_sms_whitelist__get_context SPACE_ATTRIBUTE;


enum
{
    eMETHOD_UNDEFINED = 0,
    eMETHOD_GET,
    eMETHOD_POST,
    eSOCKET_UDP
};

typedef void (*T_task_gprs_open_connection__init)(
    struct TGprsSrvProfileContext* pGsmGprsSrvProfileContext,
    struct TTaskGprsOpenConnectionContext* pCtx);
extern const __prog__ T_task_gprs_open_connection__init p_gsm_gprs_control__open_connection_init SPACE_ATTRIBUTE;

typedef void (*T_task_gprs_open_connection)(struct TTaskGprsOpenConnectionContext* pCtx);
extern const __prog__ T_task_gprs_open_connection p_gsm_gprs_control__open_connection SPACE_ATTRIBUTE;

typedef void (*T_task_gprs_close_connection__init)(
    struct TGprsSrvProfileContext* pGsmGprsSrvProfileContext,
    struct TTaskGprsCloseConnectionContext* pCtx);
extern const __prog__ T_task_gprs_close_connection__init p_gsm_gprs_control__closeConnection_init SPACE_ATTRIBUTE;

typedef void (*T_task_gprs_close_connection)(struct TTaskGprsCloseConnectionContext* pCtx);
extern const __prog__ T_task_gprs_close_connection p_gsm_gprs_control__closeConnection SPACE_ATTRIBUTE;


typedef struct TTaskGprsProcessOutgoingDataContext
{
    struct TGsmBuffer* pInputBuffer;
    int16_t inputBufferNewLineCounter; ///< rolling counter of new lines
    char command[32]; ///< the command to be sent to the modem
    //size_t commandSize;
    char* dataBuffer; ///< the contents of the message
    int32_t numberOfBytesToSend;
    const char* dataToSend; ///< data that is to be sent over the gprs connection
    bool moreToSend; ///< is there more data to be sent
    size_t dataBufferSize;

    bool (*pf_get_characters_to_send)(char* buffer, const size_t bufferSize, size_t* pCount, void*);
    void* pf_get_characters_to_send_pCtx;

    int state; ///< internal task state
    int numberOfRemainingBytes;
    bool urcReceived;
    bool carretSisrReceived;
    uint32_t lastSavedTick; ///< when sending a command to the GSM modem the attribute storing the time of sending

    struct TTaskGsmProcessSimpleCommandContext* pTaskGsmProcessSimpleCommandContext;
    struct TTaskGprsProcessIncomingDataContext* pTaskGsmGprsProcessIncomingDataContext;
    struct TGprsSrvProfileContext* pGsmGprsSrvProfileContext;
    struct TGsmModuleControlContext* pGsmModuleControlContext;
} TTaskGprsProcessOutgoingDataContext;

typedef void (*T_task_gprs_process_outgoing_data__init)(
    const int32_t numberOfBytesToSend,
    const char* dataToSend,
    struct TGprsSrvProfileContext* pGsmGprsSrvProfileContext,
    struct TTaskGprsProcessOutgoingDataContext* pCtx);
extern const __prog__ T_task_gprs_process_outgoing_data__init p_gsm_gprs_control__process_outgoing_data_init SPACE_ATTRIBUTE;

typedef void (*T_task_gprs_process_outgoing_data)(struct TTaskGprsProcessOutgoingDataContext* pCtx);
extern const __prog__ T_task_gprs_process_outgoing_data p_gsm_gprs_control__process_outgoing_data SPACE_ATTRIBUTE;

typedef TTaskGprsProcessOutgoingDataContext* (*T_task_gprs_process_outgoing_data__get_context)(void);
extern const __prog__ T_task_gprs_process_outgoing_data__get_context p_gsm_gprs_control__getOutgoingGPRSDataContext SPACE_ATTRIBUTE;


typedef bool (*T_task_gprs_initialise_profile__init)(
                    const char* url,
                    const int method,
                    struct TGprsSrvProfileContext* pGsmGprsSrvProfileContext,
                    struct TTaskGprsInitialiseProfileContext* pCtx);
extern const __prog__ T_task_gprs_initialise_profile__init p_gsm_gprs_control__initialise_GPRS_settings_init SPACE_ATTRIBUTE;

typedef void (*T_task_gprs_initialise_profile)(
                    struct TTaskGprsInitialiseProfileContext* pCtx);
extern const __prog__ T_task_gprs_initialise_profile p_gsm_gprs_control__initialise_GPRS_settings SPACE_ATTRIBUTE;


//delay_nop_ms needs to be exposed through API because of the calibration process
// (in order not to maintain another version of this function which may not be calibrated)
typedef void (*T_delay_nop_ms)(unsigned int ms);
extern const __prog__ T_delay_nop_ms p_delay_nop_ms SPACE_ATTRIBUTE;

typedef size_t (*T_strlen)(const char *pStr);
extern const __prog__ T_strlen p_strlen SPACE_ATTRIBUTE;

typedef int (*T_strncmp)(const char* s1, const char* s2, size_t n);
extern const __prog__ T_strncmp p_strncmp SPACE_ATTRIBUTE;


//Menu related functions
typedef void (*T_gsm_menu__run)(const char c);
extern const __prog__ T_gsm_menu__run p_gsm_menu__run SPACE_ATTRIBUTE;


//Function added for testing reasons
typedef bool (*T_gsm_sms_whitelist__remove_all_numbers)(void);
extern const __prog__ T_gsm_sms_whitelist__remove_all_numbers p_gsm_sms_whitelist__remove_all_numbers SPACE_ATTRIBUTE;

typedef void (*T_gsm_sms_whitelist__show_all_numbers)(void);
extern const __prog__ T_gsm_sms_whitelist__show_all_numbers p_gsm_sms_whitelist__show_all_numbers SPACE_ATTRIBUTE;


//Reserve profile numbers so that they are unique throughout the program.
//There are 10 profiles starting from number 0. The unused profiles are reserved
//for future use
#define FIRMWARE_DOWNLOAD_PROFILE (0)
#define CONFIGURATION_DOWNLOAD_PROFILE (1)

#define CONGESTION_REPORT_PROFILE (2)
#define STATISTICS_REPORT_PROFILE (3)
#define FAULT_REPORT_PROFILE (4)
#define STATUS_REPORT_PROFILE (5)

#define NTP_PROFILE (6)

#define DIGITAL_INPUT_REPORT_PROFILE (7)


typedef struct TTaskGprsDownloadFileContext
{
    int state;
    int numberOfRetries;
    bool aborted;
    bool paused;
    bool errorOccurred;

    uint8_t srvProfileId;
    char url[URL_SIZE]; ///< srvParmValue of address
    int method;

    bool (*pf_process_received_characters)(const char* buffer, const size_t bufferSize, void* pCtx);
    void* pf_process_received_characters_pCtx; ///< Context when invoking process received characters function

    void (*pf_on_retry)(void* pCtx);
    void* pf_on_retry_pCtx; ///< Context when invoking on_retry function

    struct TGprsSrvProfileContext* pGsmGprsSrvProfileContext;
    struct TGsmModuleControlContext* pGsmModuleControlContext;
    struct TTaskGprsInitialiseProfileContext* pTaskGprsInitialiseProfileContext;
    struct TTaskGprsOpenConnectionContext* pTaskGsmGprsOpenConnection;
    struct TTaskGprsCloseConnectionContext* pTaskGsmGprsCloseConnection;
    struct TTaskGprsProcessIncomingDataContext* pTaskGsmGprsProcessIncomingDataContext;
} TTaskGprsDownloadFileContext;

typedef int (*T_task_gprs_download_file__init)(
                    const char* url,
                    const size_t urlSize,
                    const uint8_t srvProfileId,
                    bool (*pf_process_received_chars)(const char* data, const size_t dataSize, void* ),
                    void* pf_process_received_characters_pCtx,
                    void (*pf_on_retry)(void*),
                    void* pf_on_retry_pCtx,
                    struct TTaskGprsDownloadFileContext* pCtx);
extern const __prog__ T_task_gprs_download_file__init p_task_gprs_download_file__init SPACE_ATTRIBUTE;

typedef void (*T_task_gprs_download_file)(
                    struct TTaskGprsDownloadFileContext* pCtx);
extern const __prog__ T_task_gprs_download_file p_task_gprs_download_file SPACE_ATTRIBUTE;

typedef struct TTaskGprsDownloadFileContext* (*T_task_gprs_download_file__get_context)(void);
extern const __prog__ T_task_gprs_download_file__get_context p_task_gprs_download_file__get_context SPACE_ATTRIBUTE;


#define MAX_DATA_TO_SEND_SIZE 196
typedef struct TTaskGprsPostDataContext
{
    int state;
    int numberOfRetries;
    bool aborted;
    bool errorOccurred;

    uint8_t srvProfileId;
    char url[URL_SIZE]; ///< srvParmValue of address

    int contentLength;

    char dataToSend[MAX_DATA_TO_SEND_SIZE];
    size_t numberOfBytesToSend;

    bool (*pf_get_characters_to_send)(char* buffer, const size_t bufferSize, size_t* pCount, void* );
    void* pf_get_characters_to_send_pCtx;

    void (*pf_on_retry_clear)(void* pCtx);
    void* pf_on_retry_clear_pCtx;

    struct TGsmModuleControlContext* pGsmModuleControlContext;
    struct TGprsSrvProfileContext* pGsmGprsSrvProfileContext;
    struct TTaskGprsInitialiseProfileContext* pTaskGprsInitialiseProfileContext;
    struct TTaskGprsOpenConnectionContext* pTaskGsmGprsOpenConnection;
    struct TTaskGprsCloseConnectionContext* pTaskGsmGprsCloseConnection;
    struct TTaskGprsProcessOutgoingDataContext* pTaskGsmGprsProcessOutgoingDataContext;
} TTaskGprsPostDataContext;

typedef int (*T_task_gprs_post_data__init)(
                    const char* url,
                    const size_t urlSize,
                    const uint8_t srvProfileId,
                    const char* dataToSend,
                    const size_t numberOfBytesToSend,
                    bool (*pf_get_characters_to_send)(char* buffer, const size_t bufferSize, size_t* pCount, void*),
                    void* pf_get_characters_to_send_pCtx,
                    void (*pf_on_retry_clear)(void*),
                    void* pf_on_retry_clear_pCtx,
                    struct TTaskGprsPostDataContext* pCtx);
extern const __prog__ T_task_gprs_post_data__init p_task_gprs_post_data__init SPACE_ATTRIBUTE;

typedef void (*T_task_gprs_post_data)(
                    struct TTaskGprsPostDataContext* pCtx);
extern const __prog__ T_task_gprs_post_data p_task_gprs_post_data SPACE_ATTRIBUTE;

typedef struct TTaskGprsPostDataContext* (*T_task_gprs_post_data__get_context)(void);
extern const __prog__ T_task_gprs_post_data__get_context p_task_gprs_post_data__get_context SPACE_ATTRIBUTE;


typedef struct TTaskGprsExchangeRawDataContext
{
    int state;
    int numberOfRetries;
    bool aborted;
    bool paused;
    bool errorOccurred;

    uint8_t srvProfileId;
    char url[URL_SIZE]; ///< srvParmValue of address

    bool sendDataOnConnection; ///< if set true after connection is established
    /// the client sends data, otherwise it waits for the message from the server
    int maxNumberOfBytesToReceive;
    
    char dataToSend[MAX_DATA_TO_SEND_SIZE];
    size_t numberOfBytesToSend;

    bool (*pf_get_characters_to_send)(char* buffer, const size_t bufferSize, size_t* pCount, void* );
    void* pf_get_characters_to_send_pCtx;

    bool (*pf_process_received_characters)(const char* buffer, const size_t bufferSize, void* pCtx);
    void* pf_process_received_characters_pCtx; ///< Context when invoking process received characters function

    void (*pf_on_error)(void* pCtx);
    void* pf_on_error_pCtx; ///< Context when invoking on_retry function

    struct TGprsSrvProfileContext* pGsmGprsSrvProfileContext;
    struct TGsmModuleControlContext* pGsmModuleControlContext;
    struct TTaskGsmProcessSimpleCommandContext* pTaskGsmProcessSimpleCommandContext;
    struct TTaskGprsInitialiseProfileContext* pTaskGprsInitialiseProfileContext;
    struct TTaskGprsOpenConnectionContext* pTaskGsmGprsOpenConnection;
    struct TTaskGprsCloseConnectionContext* pTaskGsmGprsCloseConnection;
    struct TTaskGprsProcessIncomingDataContext* pTaskGsmGprsProcessIncomingDataContext;
    struct TTaskGprsProcessOutgoingDataContext* pTaskGsmGprsProcessOutgoingDataContext;
} TTaskGprsExchangeRawDataContext;

typedef int (*T_task_gprs_exchange_raw_data__init)(
                    const char* url,
                    const size_t urlSize,
                    const uint8_t srvProfileId,
                    const bool sendDataOnConnection,
                    const int maxNumberOfBytesToReceive,
                    bool (*pf_get_characters_to_send)(char* buffer, const size_t bufferSize, size_t* pCount, void*),
                    void* pf_get_characters_to_send_pCtx,
                    bool (*pf_process_received_characters)(const char* data, const size_t dataSize, void* ),
                    void* pf_process_received_characters_pCtx,
                    void (*pf_on_error)(void*),
                    void* pf_on_error_pCtx,
                    struct TTaskGprsExchangeRawDataContext* pCtx);
extern const __prog__ T_task_gprs_exchange_raw_data__init p_task_gprs_exchange_raw_data__init SPACE_ATTRIBUTE;

typedef void (*T_task_gprs_exchange_raw_data)(
                    struct TTaskGprsExchangeRawDataContext* pCtx);
extern const __prog__ T_task_gprs_exchange_raw_data p_task_gprs_exchange_raw_data SPACE_ATTRIBUTE;

typedef struct TTaskGprsExchangeRawDataContext* (*T_task_gprs_exchange_raw_data__get_context)(void);
extern const __prog__ T_task_gprs_exchange_raw_data__get_context p_task_gprs_exchange_raw_data__get_context SPACE_ATTRIBUTE;



#define MD5_BINARY_LENGTH 16
#define MD5_TEXTUAL_LENGTH (MD5_BINARY_LENGTH << 1)

typedef struct TTaskUpgradeFirmwareContext
{
    char url[URL_SIZE]; ///< srvParmValue of address

    int state;
    int moduleIndex;
    uint8_t requestedMD5[MD5_BINARY_LENGTH];
    uint8_t calculatedMD5[MD5_BINARY_LENGTH];
    char phoneNumber[GSM_PHONE_NUMBER_SIZE]; ///< phone number to be used for the reply SMS
    bool noMoreDataExpected;
    bool errorOccurred;
    bool doNotSendSMSAfterAll; ///< Used for testing

    struct TTaskGprsDownloadFileContext* pGsmGprsDownloadFileContext;
    struct TTaskGsmProcessOutcomingTextContext* pReplySMSContext;
} TTaskUpgradeFirmwareContext;


typedef int (*T_upgrade_firmware_task__init)(
                    const char* phoneNumber,
                    const char* smsContents,
                    const size_t smsContentsSize,
                    struct TTaskUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware_task__init p_upgrade_firmware_task__init SPACE_ATTRIBUTE;

typedef void (*T_upgrade_firmware_task)(
                    struct TTaskUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware_task p_upgrade_firmware_task SPACE_ATTRIBUTE;

typedef struct TTaskUpgradeFirmwareContext* (*T_upgrade_firmware_task__getContext)(void);
extern const __prog__ T_upgrade_firmware_task__getContext p_upgrade_firmware_task__getContext SPACE_ATTRIBUTE;

typedef void (*T_upgrade_firmware_task__progress_to_verify_state)(struct TTaskUpgradeFirmwareContext* pCtx);
extern const __prog__ T_upgrade_firmware_task__progress_to_verify_state p_upgrade_firmware_task__progress_to_verify_state SPACE_ATTRIBUTE;


typedef struct TGsmSmsData
{
    char* textBuffer; ///< The contents of the message
    size_t textBufferSize;
    char phoneNumber[GSM_PHONE_NUMBER_SIZE]; ///< The phone from which the message has been received
    char timeString[GSM_TIME_STRING_SIZE]; ///< time string to be extracted from the incoming SMS
} TGsmSmsData;

typedef bool (*T_gsm_sms__process_incoming_message_contents)(const struct TGsmSmsData* pSmsData);
extern const __prog__ T_gsm_sms__process_incoming_message_contents p_gsm_sms__process_incoming_message_contents SPACE_ATTRIBUTE;


#define GSM_SIGNAL_LEVEL__NO_SIGNAL (-255)

typedef struct TGsmSignalLevel
{
    int32_t minimumSignal_2G;
    int32_t totalSignal_2G;
    int totalSignalCounter_2G;
    int32_t maximumSignal_2G;

    int32_t minimumSignal_3G;
    int32_t totalSignal_3G;
    int totalSignalCounter_3G;
    int32_t maximumSignal_3G;
} TGsmSignalLevel;

typedef struct TGsmSignalLevelContext
{
    struct TGsmSignalLevel gsmSignalLevel;
    struct TGsmSignalLevel temporaryGsmSignalLevel;
    uint32_t lastGsmSignalLevelReloadTime;
} TGsmSignalLevelContext;

typedef struct TGsmSignalLevel* (*T_gsm__signal_level_getContext)(void);
extern const __prog__ T_gsm__signal_level_getContext p_gsm__signal_level_getContext SPACE_ATTRIBUTE;


/**
 * General enumeration used by all tasks in this module
 */
enum
{
    eIDLE = 0,
    eINITIALISED = 1,
    eCOMPLETED = 2,
    eERROR = 3,
    eBLOCKED = 4,
    eNOT_SUPPORTED,
    GLOBAL_STATE_MACHINE_STATE_STARTING_NUMBER
};

#ifdef __cplusplus
}
#endif

#endif //_GSM_API_H_
