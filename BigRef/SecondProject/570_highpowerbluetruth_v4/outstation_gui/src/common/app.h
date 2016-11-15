/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    11/02/2013  RG      001         V1.00 First Issue

*/

#ifndef _APP_H_
#define _APP_H_

//#include <wx/wx.h>
//
//class MyApp : public wxApp
//{
//  public:
//    virtual bool OnInit();
//};

#include "clock.h"
#include "initialiser.h"
#include "loghandler.h"

#include <string>
#include <wx/app.h>

class BoostLogSink;
class LegacyConsoleLogSink;
class LegacyFileLogSink;
class ViewLogSink;


class MainApp : public wxApp {
public:
    MainApp();

    virtual ~MainApp();

    virtual bool OnInit();

private:

    //! copy constructor. Not implemented
    MainApp(const MainApp& rhs);
    //! copy assignment operator. Not implemented
    MainApp & operator=(const MainApp& rhs);

    // Private members:
    ::Clock m_clock;
    Initialiser m_initialiser;
    LogHandler m_logHandler;
    BoostLogSink* m_pBoostLogSink;
    LegacyConsoleLogSink* m_pLegacyConsoleLogSink;
    LegacyFileLogSink* m_pLegacyFileLogSink;
    ViewLogSink* m_pViewLogSink;
};

DECLARE_APP(MainApp)

extern const tchar APPLICATION_NAME[];
extern const tchar VENDOR_NAME[];
extern const tchar APPLICATION_CONFIG_FILE_NAME[];


enum EProgramReturnCode
{
    ePROGRAM_RETURN_OK = 0,
    //Errors:
    ePROGRAM_FATAL_ERROR = 1,
    //Other exit codes:
    ePROGRAM_RESTART_REQUIRED = 16,
    eSYSTEM_RESTART_REQUIRED = 17,
};
void sighandler(const int sig, const EProgramReturnCode _programReturnCode = ePROGRAM_RETURN_OK);
void _sighandler(int sig);

#endif // _APP_H_
