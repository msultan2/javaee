#ifndef _APPLICATION_API_H_
#define _APPLICATION_API_H_

#include "bootloader_api.h"
#include "gsm_api.h"

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#ifndef __XC16__
//The __prog__ is very specific to XC16
#define __prog__
#endif

struct TProcessInstationCommandContext;


/** Main program activity */
typedef void (*T_application__main)(void);
extern const __prog__ T_application__main p_application__main SPACE_ATTRIBUTE;

typedef void (*T_application__configure_pins)(void);
extern const __prog__ T_application__configure_pins p_application__configure_pins SPACE_ATTRIBUTE;

typedef void (*T_application__initialise)(void);
extern const __prog__ T_application__initialise p_application__initialise SPACE_ATTRIBUTE;

typedef void (*T_populate_application_firmware_descriptor)(struct TFirmwareDescriptor* pDescriptor);
extern const __prog__ T_populate_application_firmware_descriptor p_populate_application_firmware_descriptor SPACE_ATTRIBUTE;

typedef void (*T_set_application_sms_message)(const struct TGsmSmsData* pSmsData);
extern const __prog__ T_set_application_sms_message p_set_application_sms_message SPACE_ATTRIBUTE;

typedef bool (*T_rtcc_control__update_time_from_sms)(const char* text, const size_t textSize, const bool overwriteTime);
extern const __prog__ T_rtcc_control__update_time_from_sms p_rtcc_control__update_time_from_sms SPACE_ATTRIBUTE;


typedef bool (*T_process_instation_command)(const char* data, const size_t dataSize, void* );
extern const __prog__ T_process_instation_command p_process_instation_command SPACE_ATTRIBUTE;

typedef struct TProcessInstationCommandContext* (*T_getProcessInstationCommandContext)(void);
extern const __prog__ T_getProcessInstationCommandContext pGetProcessInstationCommandContext SPACE_ATTRIBUTE;

enum
{
    eERROR_URL_NOT_DEFINED = 200,
    eERROR_DEVICE_ID_NOT_DEFINED,
    eERROR_INVALID_PERIOD_VALUE,
    eERROR_BUSY,
    eERROR_GPRS_POST_INITIALISATION_FAILED
};

#endif //_APPLICATION_API_H_
