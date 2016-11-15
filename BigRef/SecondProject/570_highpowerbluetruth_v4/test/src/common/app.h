/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    15/11/2013  RG      001         V1.00 First Issue
*/

#ifndef _APP_H_
#define _APP_H_


enum EProgramReturnCode
{
    ePROGRAM_RETURN_OK = 0,
    //Errors:
    ePROGRAM_FATAL_ERROR = 1,
    //Other exit codes:
    ePROGRAM_RESTART_REQUIRED = 16,
    eSYSTEM_RESTART_REQUIRED = 17,
};
void processReceivedSignal(const int sig, const EProgramReturnCode _programReturnCode = ePROGRAM_RETURN_OK);
EProgramReturnCode getProgramReturnCode();

#endif // _APP_H_
