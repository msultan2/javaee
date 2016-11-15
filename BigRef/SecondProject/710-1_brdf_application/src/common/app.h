/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef _APP_H_
#define _APP_H_


#include "clock.h"
#include "initialiser.h"

#include <string>

class BoostLogSink;
class LegacyConsoleLogSink;
class LegacyFileLogSink;
class ViewLogSink;

namespace Model
{
    class BrdfXmlConfiguration;
}


class MainApp
{
public:
    MainApp();

    virtual ~MainApp();

    virtual bool OnInit();

    Model::BrdfXmlConfiguration* getBrdfConfiguration() { return m_pBrdfConfiguration; }

private:

    //! copy constructor. Not implemented
    MainApp(const MainApp& rhs);
    //! copy assignment operator. Not implemented
    MainApp & operator=(const MainApp& rhs);

    // Private members:
    ::Clock m_clock;
    Initialiser m_initialiser;
    //LogHandler m_logHandler;
    //BoostLogSink* m_pBoostLogSink;
    //LegacyConsoleLogSink* m_pLegacyConsoleLogSink;
    //LegacyFileLogSink* m_pLegacyFileLogSink;
    //ViewLogSink* m_pViewLogSink;
    Model::BrdfXmlConfiguration* m_pBrdfConfiguration;
};


extern const tchar APPLICATION_NAME[];
extern const tchar VENDOR_NAME[];
extern const tchar APPLICATION_CONFIG_FILE_NAME[];


void signalHandler(const int sig);

enum EProgramReturnCode
{
    ePROGRAM_RETURN_OK = 0,
    //Errors:
    ePROGRAM_FATAL_ERROR = 1,
    eERROR_IN_COMMAND_LINE = 2,
    eERROR_UNHANDLED_EXCEPTION = 3,
    //Other exit codes:
    ePROGRAM_RESTART_REQUIRED = 16,
    eSYSTEM_RESTART_REQUIRED = 17,
};
void processReceivedSignal(const int sig, const EProgramReturnCode _programReturnCode = ePROGRAM_RETURN_OK);

#endif // _APP_H_
