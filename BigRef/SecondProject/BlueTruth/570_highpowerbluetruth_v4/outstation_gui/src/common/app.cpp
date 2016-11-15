#include "stdafx.h"
#include "app.h"

#include "applicationconfiguration.h"
#include "outstationconfigurationparameters.h"
#include "view.h"
#include "controller.h"
#include "model.h"
#include "logger.h"
#include "outstationmainframe.h"
#include "applicationconfiguration.h"
#include "os_utilities.h"
#include "utils.h"

#ifdef _WIN32
//#include "vld.h"
#else
#include <pwd.h>
#endif


#include "wx/msgdlg.h"
#include "wx/busyinfo.h"


//#define USE_BOOST_LOG
#ifdef USE_BOOST_LOG
#include "boostlogsink.h"
#else
#include "legacyfilelogsink.h"
#include "legacyconsolelogsink.h"
#endif

#include "viewlogsink.h"


#include <cassert>
#include <signal.h>
#include <stdlib.h>


//RG: This variable may be accessed as extern from other modules (OutStationConfigurationParameters)
//so they must exist outside of the unnamed namespace
const tchar APPLICATION_NAME[] = _T("BlueTruth OutStation");
const tchar APPLICATION_CONFIG_FILE_NAME[] = _T("outstation_gui.cfg");
const tchar VENDOR_NAME[] = _T("Simulation Systems Ltd");

namespace
{
    volatile bool loopForEver = true;
    volatile int programReturnCode = ePROGRAM_RETURN_OK;

    const bool ASSIGN_ITSELF_TO_UTILS = true;
    const char MODULE_NAME[] = "MainApp";
}

IMPLEMENT_APP(MainApp)

using BlueTruth::ApplicationConfiguration;

MainApp::MainApp(void)
:
wxApp(),
m_clock(ASSIGN_ITSELF_TO_UTILS),
m_initialiser(ApplicationConfiguration::getSetOfAllDirectoriesForInitialisation()),
m_logHandler(),
m_pBoostLogSink(0),
m_pLegacyConsoleLogSink(0),
m_pLegacyFileLogSink(0),
m_pViewLogSink(0)
{
#ifdef USE_BOOST_LOG
    m_pBoostLogSink = new BoostLogSink();
    m_logHandler.addFileLogSink(m_pBoostLogSink);
#else
    m_pLegacyConsoleLogSink = new LegacyConsoleLogSink();
    m_logHandler.addConsoleLogSink(m_pLegacyConsoleLogSink);
    m_pLegacyFileLogSink = new LegacyFileLogSink();
    m_logHandler.addFileLogSink(m_pLegacyFileLogSink);
#endif

    SetVendorName(VENDOR_NAME);
    SetAppName(APPLICATION_NAME);

    Logger::log(LOG_LEVEL_NOTICE, "Program started");
}

MainApp::~MainApp(void)
{
    try
    {
        Controller::Controller::destruct();
        Model::Model::destruct();
        //Disconnect view logging first
        m_logHandler.delConsoleLogSink(m_pViewLogSink);
        delete m_pViewLogSink;

        OS_Utilities::sleep(1000);
        View::View::destruct();

        //Then destroy view
        ApplicationConfiguration::destruct();
        View::OutStationConfigurationParameters::destruct();

        Logger::logAction("Program shutdown");

#ifdef USE_BOOST_LOG
        delete m_pBoostLogSink;
#else
        m_logHandler.clearLogSinks();
        delete m_pLegacyConsoleLogSink;
        delete m_pLegacyFileLogSink;
#endif
    }
    catch (...)
    {
        //do nothing
    }
}

bool MainApp::OnInit()
{
    bool result = true;

    if (wxApp::OnInit())
    {
        try
        {
            ApplicationConfiguration::construct();
        }
        catch (std::bad_alloc& badAlloc)
        {
            Logger::logMemoryAllocationError(MODULE_NAME, "OnInit", badAlloc.what(), "ApplicationConfiguration::construct()");
            result = false;
        }
        catch (...)
        {
            Logger::logSoftwareException(MODULE_NAME, "OnInit", "Exception in ApplicationConfiguration::construct()");
            result = false;
        }

        try
        {
            if (result)
            {
                result = View::OutStationConfigurationParameters::construct();
            }
            //else do nothing - result already false

            if (result)
            {
                View::OutStationConfigurationParameters::getInstancePtr()->readAllParametersFromFile();
                result = !View::OutStationConfigurationParameters::getInstancePtr()->isConfigurationErrorReported();

                //Read logging level from the configuration file and set it for the application
                m_logHandler.setFileLogLevel(static_cast<LoggingLevel> (View::OutStationConfigurationParameters::getInstancePtr()->getFileLogLevel()));
                m_logHandler.setConsoleLogLevel(static_cast<LoggingLevel> (View::OutStationConfigurationParameters::getInstancePtr()->getConsoleLogLevel()));

            //Delete old log files
            {
                wxBusyInfo* busyInfoPtr = new wxBusyInfo(_T("Deleting old log files..."), NULL);

#ifdef USE_BOOST_LOG
                m_pBoostLogSink->deleteOldLogFiles(View::OutStationConfigurationParameters::getInstancePtr()->getMaximumLogFileAgeInSeconds());
#else
                m_pLegacyFileLogSink->deleteOldLogFiles(View::OutStationConfigurationParameters::getInstancePtr()->getMaximumLogFileAgeInSeconds());
#endif

                delete busyInfoPtr;
            }

#ifdef USE_BOOST_LOG
#else
                m_pLegacyFileLogSink->setFileLogMaxNumberOfEntries(static_cast<LoggingLevel>(
                    View::OutStationConfigurationParameters::getInstancePtr()->getLogMaxNumberOfEntriesPerFile()));
#endif
            }
        }
        catch (std::bad_alloc& badAlloc)
        {
            Logger::logMemoryAllocationError(MODULE_NAME, "OnInit", badAlloc.what(), "View::ConfigurationParameters::construct()");
            result = false;
        }
        catch (...)
        {
            Logger::logSoftwareException(MODULE_NAME, "OnInit", "Exception in View::ConfigurationParameters::construct()");
            result = false;
        }

        try
        {
            if (result)
            {
                result = View::View::construct();
            }
            //else do nothing - result already false
        }
        catch (std::bad_alloc& badAlloc)
        {
            Logger::logMemoryAllocationError(MODULE_NAME, "OnInit", badAlloc.what(), "View::View::construct()");
            result = false;
        }
        catch (...)
        {
            Logger::logSoftwareException(MODULE_NAME, "OnInit", "Exception in View::View::construct()");
            result = false;
        }

        try
        {
            if (result)
            {
                result = Model::Model::construct();
            }
            //else do nothing - result already false
        }
        catch (std::bad_alloc& badAlloc)
        {
            Logger::logMemoryAllocationError(MODULE_NAME, "OnInit", badAlloc.what(), "Model::Model::construct()");
            result = false;
        }
        catch (...)
        {
            Logger::logSoftwareException(MODULE_NAME, "OnInit", "Exception in Model::Model::construct()");
            result = false;
        }

        try
        {
            if (result)
            {
                Controller::Controller::construct();
            }
            //else do nothing - result already false
        }
        catch (std::bad_alloc& badAlloc)
        {
            Logger::logMemoryAllocationError(MODULE_NAME, "OnInit", badAlloc.what(), "Controller::Controller::construct()");
            result = false;
        }
        catch (...)
        {
            Logger::logSoftwareException(MODULE_NAME, "OnInit", "Exception in Controller::Controller::construct()");
            result = false;
        }
    }
    else
    {
        result = false;
    }

    if (result)
    {
        SetTopWindow(View::View::getMainFrame());

        //The view has been created. Now bind it to logHandler
        m_pViewLogSink = new ViewLogSink();
        m_logHandler.addConsoleLogSink(m_pViewLogSink);

        View::View::setup();

        Controller::Controller::startStopRetrieveConfigurationClient();
        Controller::Controller::startStopGSMModemMonitor();
        Controller::Controller::startStopInstationClient();
        Controller::Controller::startStopBlueToothDeviceDiscovery();

        signal(SIGABRT, &_sighandler);
        signal(SIGTERM, &_sighandler);
        signal(SIGINT,  &_sighandler);
#ifdef __linux__
        signal(SIGUSR1, &_sighandler);
        signal(SIGUSR2, &_sighandler);
#endif
    }
    else
    {
        wxMessageBox(
            _T("A serious error has been encountered and the program is unable to start.\nPlease check the log file for further information."),
            APPLICATION_NAME,
            wxOK | wxICON_ERROR
            );
    }

    if (!result)
        programReturnCode = ePROGRAM_FATAL_ERROR;

    return result;
}

//The first chance exception being raised is the CTRL-C event which is trapped
//by the debugging environment. This is expected behaviour.
//One can choose to ignore this: go to Debug menu/Exceptions/Win32 Exceptions
//and take out the CONTROL-C check from the "Thrown" column menu.
//This will ensure that the debugger only breaks on CONTROL-C when it is user-unhandled.
//For details see: http://stackoverflow.com/questions/13206911/why-getting-first-chace-exception-in-c

void sighandler(const int sig, const EProgramReturnCode _programReturnCode)
{
    programReturnCode = _programReturnCode;
    _sighandler(sig);
}

void _sighandler(int sig)
{
    std::cout << "Signal " << sig << " caught..." << std::endl;

    switch (sig)
    {
        case SIGABRT:
        case SIGTERM:
        case SIGINT:
#ifdef __linux
        case SIGQUIT:
#endif
        {
//#define TESTING
#ifdef TESTING
            std::cout << "loopForEver=false" << std::endl;
#endif
            loopForEver = false;
            break;
        }

        default:
        {
            //Do nothing
            break;
        }
    }
}

