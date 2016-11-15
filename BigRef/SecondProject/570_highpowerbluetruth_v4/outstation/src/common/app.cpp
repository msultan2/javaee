#include "stdafx.h"
#include "app.h"

#include "atomicvariable.h"
#include "applicationconfiguration.h"
#include "config.h"
#include "coreconfiguration.h"
#include "view.h"
#include "controller.h"
#include "model.h"
#include "logger.h"
#include "applicationconfiguration.h"
#include "os_utilities.h"
#include "utils.h"
#include "version.h"

#ifdef _WIN32
//#include "vld.h"
#else
#include <pwd.h>
#endif

#include <cassert>
#include <signal.h>
#include <stdlib.h>

#include "boost/program_options.hpp"


//RG: This variable may be accessed as extern from other modules (OutStationConfigurationParameters)
//so they must exist outside of the unnamed namespace
const tchar APPLICATION_NAME[] = _T("BlueTruth OutStation");
const tchar VENDOR_NAME[] = _T("Simulation Systems Ltd");
const char COPYRIGHT_NOTE[] = "Copyright (C) 2016 Simulation Systems Ltd";

namespace
{
    ::AtomicVariable<bool> loopForEver(true);
    volatile int programReturnCode = ePROGRAM_RETURN_OK;

    const char MODULE_NAME[] = "MainApp";

    MainApp* pMainApp = 0;
    int gSignal = 0; ///< the variable to store the received signals (e.g. USR1)
    std::string g_statusReportURL;
}


using BlueTruth::ApplicationConfiguration;

MainApp::MainApp(void)
:
m_clock(),
m_initialiser(ApplicationConfiguration::getSetOfAllDirectoriesForInitialisation()),
m_pCoreConfiguration(0)
{
    Utils::setClock(&m_clock);
    Logger::initialise(ApplicationConfiguration::getLogDirectory().c_str());
    Logger::log(LOG_LEVEL_NOTICE, "Program started");
}

MainApp::~MainApp(void)
{
    try
    {
        Logger::log(LOG_LEVEL_NOTICE, "Program shutdown");
        Logger::destruct();

        Controller::Controller::destruct();
        Model::Model::destruct();

        OS_Utilities::sleep(1000);
        View::View::destruct();

        //Then destroy view
        ApplicationConfiguration::destruct();

        delete m_pCoreConfiguration;
    }
    catch (...)
    {
        //do nothing
    }
}

bool MainApp::OnInit()
{
    bool result = true;

    try
    {
        ApplicationConfiguration::construct();
    }
    catch (std::bad_alloc& badAlloc)
    {
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", badAlloc.what(), "ApplicationConfiguration::construct()");
        result = false;
    }
    catch (...)
    {
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", "Exception in ApplicationConfiguration::construct()");
        result = false;
    }

    try
    {
        if (result)
        {
            m_pCoreConfiguration = new Model::CoreConfiguration();
            result = m_pCoreConfiguration->readAllParametersFromFile();
            if (result)
            {
                m_pCoreConfiguration->setCommandLineStatusReportURL(g_statusReportURL);

                //Read logging level from the configuration file and set it for the application
                Logger::setFileLogLevel(static_cast<ESeverityLevel> (m_pCoreConfiguration->getFileLogLevel()));
                Logger::setConsoleLogLevel(static_cast<ESeverityLevel> (m_pCoreConfiguration->getConsoleLogLevel()));

                //Define max number of entries
                Logger::setFileLogMaxNumberOfEntries(static_cast<ESeverityLevel>(
                    m_pCoreConfiguration->getLogMaxNumberOfEntriesPerFile()));
                Logger::setFileLogMaxNumberOfCharacters(static_cast<ESeverityLevel>(
                    m_pCoreConfiguration->getLogMaxNumberOfCharactersPerFile()));
            }
            else
            {
                Logger::log(LOG_LEVEL_FATAL,
                    "Unable to continue. "
                    "The core configuration file could not be read or is invalid");
            }
        }
        //else do nothing
    }
    catch (std::bad_alloc& badAlloc)
    {
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", badAlloc.what(), "View::ConfigurationParameters::construct()");
        result = false;
    }
    catch (...)
    {
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", "Exception in View::ConfigurationParameters::construct()");
        result = false;
    }

    try
    {
        if (result)
        {
            result = View::View::construct(*m_pCoreConfiguration);
        }
        //else do nothing - result already false
    }
    catch (std::bad_alloc& badAlloc)
    {
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", badAlloc.what(), "View::View::construct()");
        result = false;
    }
    catch (...)
    {
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", "Exception in View::View::construct()");
        result = false;
    }

    try
    {
        if (result)
        {
            result = Model::Model::construct(*m_pCoreConfiguration);
        }
        //else do nothing - result already false
    }
    catch (std::bad_alloc& badAlloc)
    {
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", badAlloc.what(), "Model::Model::construct()");
        result = false;
    }
    catch (...)
    {
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", "Exception in Model::Model::construct()");
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
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", badAlloc.what(), "Controller::Controller::construct()");
        result = false;
    }
    catch (...)
    {
        Logger::log(LOG_LEVEL_FATAL, MODULE_NAME, "OnInit", "Exception in Controller::Controller::construct()");
        result = false;
    }

    if (result)
    {
        if (Model::Model::isValid())
        {
            Model::Model::startRetrieveConfigurationClient();
            Model::Model::startGSMModemMonitor();
            Model::Model::startAllInstationClients();
            Model::Model::startBlueToothDeviceDiscovery();
        }
        //else do nothing
    }
    //else do nothing

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

void signalHandler(const int sig)
{
    gSignal = sig;
}

void displaySignalName(const int sig)
{
    std::ostringstream ss;
    ss << "Signal ";

    switch (sig)
    {
        case SIGABRT:
        {
            ss << "SIGABRT";
            break;
        }

        case SIGTERM:
        {
            ss << "SIGTERM";
            break;
        }

        case SIGINT:
        {
            ss << "SIGINT";
            break;
        }

#ifdef __linux
        case SIGQUIT:
        {
            ss << "SIGQUIT";
            break;
        }

        case SIGUSR1:
        {
            ss << "SIGUSR1";
            break;
        }

        case SIGUSR2:
        {
            ss << "SIGUSR2";
            break;
        }
#endif

        default:
        {
            ss << sig;
            break;
        }
    }
    ss << " caught...";
    Logger::log(LOG_LEVEL_NOTICE, ss.str().c_str());
}

void processReceivedSignal(const int sig, const EProgramReturnCode _programReturnCode)
{
    displaySignalName(sig);
    programReturnCode = _programReturnCode;

    switch (sig)
    {
        case SIGABRT:
        case SIGTERM:
        case SIGINT:
#ifdef __linux
        case SIGQUIT:
#endif
        {

            loopForEver.set(false);
            break;
        }

#ifdef __linux
        case SIGUSR1:
        {
            //Reread configuration parameters and apply them
            if (pMainApp == 0)
                return;

            pMainApp->getCoreConfiguration()->readAllParametersFromFile();

            //Read logging level from the configuration file and set it for the application
            {
                int logLevel = pMainApp->getCoreConfiguration()->getFileLogLevel();
                Logger::setFileLogLevel(static_cast<ESeverityLevel>(logLevel));

                std::ostringstream ss;
                ss << "File log level changed to " << logLevel;
                Logger::log(LOG_LEVEL_NOTICE, ss.str().c_str());
            }

            {
                int logLevel = pMainApp->getCoreConfiguration()->getConsoleLogLevel();
                Logger::setConsoleLogLevel(static_cast<ESeverityLevel>(logLevel));

                std::ostringstream ss;
                ss << "Console log level changed to " << logLevel;
                Logger::log(LOG_LEVEL_NOTICE, ss.str().c_str());
            }

            //Read URL of the configuration server and pass it to Model
            {
                Model::Model::setRetrieveConfigurationConnectionParameters();

                std::ostringstream ss;
                ss << "Ini configuration host ip address set to "
                    << Model::Model::getInstancePtr()->getRetrieveConfigurationRemoteAddress()
                    << ':' << Model::Model::getInstancePtr()->getRetrieveConfigurationRemotePortNumber()
                    << " (URL: "
                        << pMainApp->getCoreConfiguration()->getConfigurationURL()
                        << pMainApp->getCoreConfiguration()->getConfigurationURL_filePrefix()
                        << pMainApp->getCoreConfiguration()->getSSLSerialNumber()
                        << pMainApp->getCoreConfiguration()->getConfigurationURL_fileSuffix()
                    << ")";

                Logger::log(LOG_LEVEL_NOTICE, ss.str().c_str());
            }

            //Read BT_ADDR of the device to be used for subsequent inquiries
            {
                Model::TLocalDeviceRecord_shared_ptr pLocalDeviceRecord =
                    Model::Model::getInstancePtr()->getDataContainer()->getLocalDeviceRecord();
                if (pLocalDeviceRecord)
                {
                    pLocalDeviceRecord->reset();
                    pLocalDeviceRecord->address = pMainApp->getCoreConfiguration()->getLastUsedBlueToothDevice();

                    std::ostringstream ss;
                    ss << "Local radio to be used set to " << Utils::convertMACAddressToString(pLocalDeviceRecord->address);

                    Logger::log(LOG_LEVEL_NOTICE, ss.str().c_str());
                }
            }

            break;
        }

        case SIGUSR2:
        {
            break;
        }
#endif

        default:
        {
            //Do nothing
            break;
        }
    }
}

int main(int argc, char** argv)
{
    try
    {
        namespace po = boost::program_options;
        // Declare the supported options.
        po::options_description optionsDescription("Options");
        optionsDescription.add_options()
            ("help,h", "produce help message")
            ("version,v", "display program version")
            ("status_report_URL,s", po::value<std::string>(),
                "use the URL provided for sending STATUS REPORT messages instead of values read from the functional configuration");

        po::variables_map vm;
        try
        {
            po::store(po::parse_command_line(argc, argv, optionsDescription), vm); // can throw

            if (vm.count("help"))
            {
                std::cout << PACKAGE_NAME << " version " << Version::getNumber() << "\n"
                    << COPYRIGHT_NOTE << "\n\n" << optionsDescription << std::endl;
                return ePROGRAM_RETURN_OK;
            }
            else if (vm.count("version"))
            {
                std::cout << PACKAGE_NAME << " version " << Version::getNumber() << std::endl;
                return ePROGRAM_RETURN_OK;
            }
            else if (vm.count("status_report_URL"))
            {
                g_statusReportURL = vm["status_report_URL"].as<std::string>();
                std::cout << "STATUS REPORT URL will be overidden with value " << g_statusReportURL << std::endl;
            }
            else
            {
                //do nothing
            }

            po::notify(vm); // throws on error, so do after help in case
            // there are any problems
        }
        catch (po::error& e)
        {
            std::cerr << "ERROR: " << e.what() << std::endl << std::endl;
            std::cout << PACKAGE_NAME << " version " << Version::getNumber() << "\n"
                << COPYRIGHT_NOTE << "\n\n" << optionsDescription << std::endl;
            return eERROR_IN_COMMAND_LINE;
        }

        MainApp app;
        pMainApp = &app;

        if (app.OnInit())
        {
            signal(SIGABRT, &signalHandler);
            signal(SIGTERM, &signalHandler);
            signal(SIGINT,  &signalHandler);
#ifdef __linux__
            signal(SIGUSR1, &signalHandler);
            signal(SIGUSR2, &signalHandler);
#endif

            while (loopForEver.get())
            {
                ::OS_Utilities::sleep(10);

                if (gSignal != 0)
                {
                    processReceivedSignal(gSignal);
                    gSignal = 0;
                }
                //else do nothing
            }
        }
        //else do nothing
    }
    catch(std::exception& e)
    {
        std::cerr << "Unhandled Exception reached the top of main: "
            << e.what() << ", application will now exit" << std::endl;
        return eERROR_UNHANDLED_EXCEPTION;
    }

    Logger::log(LOG_LEVEL_NOTICE, "Exiting program...");

    return programReturnCode;
}
