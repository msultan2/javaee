#include "stdafx.h"
#include "activegsmmodemmonitor.h"

#include "fault.h"
#include "execute_command.h"
#include "gsmmodem/gsmmodemsignallevelprocessor.h"
#include "icoreconfiguration.h"
#include "lock.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"

#include <boost/thread/locks.hpp>
#include <climits>


namespace
{
    const char GET_MODEM_SIGNAL_LEVEL_PROGRAM_NAME[] = BIN_DIRECTORY "/get_modem_signal_level";
    const char GET_MODEM_SIGNAL_LEVEL_OUTPUT_FILE_NAME[] = "/tmp/signal_level.txt";

    const char MODULE_NAME[] = "ActiveGSMModemMonitor";
}


namespace Model
{


ActiveGSMModemMonitor::ActiveGSMModemMonitor(
    const ICoreConfiguration& configurationParameters,
    GSMModem::SignalLevelProcessor& signalLevelProcessor,
    Fault* pGSMModemUnableToConnectFault,
    ::Clock* pClock)
:
::ActiveObject(MODULE_NAME),
::IObservable(),
::IObserver(),
m_pGSMModemUnableToConnectFault(pGSMModemUnableToConnectFault),
m_pClock(pClock),
m_lastActionTime(pt::min_date_time),
m_mutex(),
m_configurationParameters(configurationParameters),
m_hostName(),
m_userName(),
m_password(),
m_GPSSignalOK(true),
m_signalLevelProcessor(signalLevelProcessor),
m_signalLevelSamplingPeriod(60),
m_lastSignalLevelCheckTime()
{
    //do nothing
}

ActiveGSMModemMonitor::~ActiveGSMModemMonitor()
{
}


bool ActiveGSMModemMonitor::extractSignalValue(const std::string& signalLevelString, int& gpsModemSignalLevel)
{
    bool result = false;
    if (Utils::stringToInt(signalLevelString, gpsModemSignalLevel))
    {
        std::string plainSignalValue;
        plainSignalValue += "GSM Signal level: ";
        plainSignalValue += signalLevelString;
        plainSignalValue += "[ASU]";

        Logger::log(LOG_LEVEL_DEBUG2, plainSignalValue.c_str());
        result = true;
    }
    else
    {
        std::ostringstream ss;
        ss << "Invalid GSM signal level value: " << signalLevelString;
        Logger::log(
            LOG_LEVEL_ERROR,
            MODULE_NAME,
            ss.str().c_str());
    }

    return result;
}



void ActiveGSMModemMonitor::setup(
        const std::string& hostName,
        const std::string& userName,
        const std::string& password)
{
    boost::lock_guard<boost::recursive_mutex> lock(m_mutex);
    m_hostName = hostName;
    m_userName = userName;
    m_password = password;
}

void ActiveGSMModemMonitor::setupSignalLevelSamplingPeriod(const unsigned int periodInSeconds)
{
    m_signalLevelSamplingPeriod = bc::seconds(static_cast<long>(periodInSeconds));
}

void ActiveGSMModemMonitor::start()
{
    //run the thread
    resumeThread();
}

void ActiveGSMModemMonitor::stop()
{
    ::IObservable::notifyObservers(-1);
}

void ActiveGSMModemMonitor::notifyOfStateChange(IObservable* , const int )
{
    //do nothing
}

void ActiveGSMModemMonitor::initThread()
{
    //do nothing
}

void ActiveGSMModemMonitor::run()
{
    while (!isDying())
    {
        if (m_signalLevelSamplingPeriod.count() == 0)
        {
            OS_Utilities::sleep(1000); //sleep for 1 second (to enable swift exit from the program)
            break;
        }

        //TODO All modem specific commands/processing should be migrated to a dedicated class/driver
        const TSteadyTimePoint CURRENT_TIME_STEADY(m_pClock->getSteadyTime());

        if (CURRENT_TIME_STEADY - m_lastSignalLevelCheckTime >= m_signalLevelSamplingPeriod)
        {
            m_lastSignalLevelCheckTime = CURRENT_TIME_STEADY;

            //The connect operation may take a long time so to avoid blocking on mutex
            //copy all necessary values in one atomic operation
            TStringArray argvStringArray;
            {
                boost::lock_guard<boost::recursive_mutex> lock(m_mutex);
                argvStringArray.push_back(GET_MODEM_SIGNAL_LEVEL_PROGRAM_NAME);
                argvStringArray.push_back(m_hostName);
                argvStringArray.push_back(m_userName);
                argvStringArray.push_back(m_password);
                argvStringArray.push_back(GET_MODEM_SIGNAL_LEVEL_OUTPUT_FILE_NAME);
            }

            const bool result = (::execute(argvStringArray) == 0);
            bool GPSSignalOK = false;
            if (result)
            {
                std::string signalLevelString;
                std::ifstream fileWithSignalLevel;
                fileWithSignalLevel.open(GET_MODEM_SIGNAL_LEVEL_OUTPUT_FILE_NAME);
                if (fileWithSignalLevel.is_open())
                    fileWithSignalLevel >> signalLevelString;
                fileWithSignalLevel.close();

                if (signalLevelString.empty())
                Logger::log(
                    LOG_LEVEL_ERROR,
                    MODULE_NAME,
                    "Unable to read GSM signal level");


                int gpsModemSignalLevel = -255;
                GPSSignalOK = extractSignalValue(signalLevelString, gpsModemSignalLevel);
                if (GPSSignalOK)
                    m_signalLevelProcessor.updateSignalLevel(gpsModemSignalLevel);
            }
            //else do nothing

            if (m_GPSSignalOK != GPSSignalOK)
            {
                m_GPSSignalOK = GPSSignalOK;
                ::IObservable::notifyObservers();
            }
            //else do nothing
        }
        //else do nothing

        OS_Utilities::sleep(1000);
    }
}

void ActiveGSMModemMonitor::flushThread()
{
}

} //namespace
