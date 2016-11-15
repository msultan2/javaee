/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    13/08/2013  RG      001         V1.00 First Issue

    WARNING:
    The implementation contained in this module is GPL licensed.
    This module interacts with bluez library (www.bluez.org) which is GPL
    licensed. The source contains some code directly copied from the
    library source or associated tools source (hcidump in particular).
    To overcome the licencing issues and use this module some techniques
    have to be applied - see http://www.gnu.org/licenses/gpl-faq.html#NFUseGPLPlugins.
*/


#ifndef HCI_PARSER_H_
#define HCI_PARSER_H_

#ifdef __cplusplus
extern "C"
{
#endif

#define EVENT_NUM 77
extern char* event_str[EVENT_NUM + 1];

#define LE_EV_NUM 5
extern char* ev_le_meta_str[LE_EV_NUM + 1];

#define CMD_LINKCTL_NUM 60
extern char* cmd_linkctl_str[CMD_LINKCTL_NUM + 1];

#define CMD_LINKPOL_NUM 17
extern char* cmd_linkpol_str[CMD_LINKPOL_NUM + 1];

#define CMD_HOSTCTL_NUM 109
extern char* cmd_hostctl_str[CMD_HOSTCTL_NUM + 1];

#define CMD_INFO_NUM 10
extern char* cmd_info_str[CMD_INFO_NUM + 1];

#define CMD_STATUS_NUM 11
extern char* cmd_status_str[CMD_STATUS_NUM + 1] ;

#define CMD_TESTING_NUM 4
extern char* cmd_testing_str[CMD_TESTING_NUM + 1];

#define CMD_LE_NUM 31
extern char* cmd_le_str[CMD_LE_NUM + 1];

#define ERROR_CODE_NUM 63
extern char* error_code_str[ERROR_CODE_NUM + 1];


char *opcode2str(uint16_t opcode);
char *status2str(uint8_t status);

#ifdef __cplusplus
}
#endif

#endif
