#include "stdafx.h"
#include "retrieveconfigurationtask.h"

#include "instation/instationhttpclient.h"
#include "iniconfiguration.h"
#include "clock.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"

#include <sstream>


namespace InStation
{

RetrieveConfigurationTask::RetrieveConfigurationTask(
    boost::shared_ptr<InStationHTTPClient> pInStationHTTPClient,
    ::Clock* pClock)
:
::ITask(),
::IObserver(),
::IObservable(),
m_pInStationHTTPClient(pInStationHTTPClient),
m_pClock(pClock),
m_startupTime(m_pClock->getSteadyTime()),
m_startupTimeWithDelay(m_startupTime),
m_lastConfigurationRequestTime(),
m_nextConfigurationRequestTime(),
m_configurationRequestPeriod()
{
    //do nothing
}

RetrieveConfigurationTask::~RetrieveConfigurationTask()
{
    //do nothing
}

void RetrieveConfigurationTask::initialise()
{
    //do nothing
}

void RetrieveConfigurationTask::perform()
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    if (m_configurationRequestPeriod.count() != 0)
    {
        const TSteadyTimePoint currentTime(m_pClock->getSteadyTime());

        if (
            (currentTime >= m_startupTimeWithDelay) &&
            (currentTime >= m_nextConfigurationRequestTime)
            )
        {
            m_lastConfigurationRequestTime = currentTime;
            m_nextConfigurationRequestTime = currentTime + m_configurationRequestPeriod;

            notifyObservers(eREQUESTING_CONFIGURATION);

            Logger::log(LOG_LEVEL_DEBUG2, "CONFIGURATION REQUEST will be submitted for sending");

            assert(m_pInStationHTTPClient != 0);
            m_pInStationHTTPClient->sendConfigurationRequest(false, false);

            Logger::log(LOG_LEVEL_DEBUG2, "CONFIGURATION REQUEST has been submitted for sending");
        }
        //else do nothing
    }
    //else do nothing
}

void RetrieveConfigurationTask::shutdown()
{
    //do nothing
}

void RetrieveConfigurationTask::start(
    const unsigned int configurationRequestPeriodInSeconds, const unsigned int startupDelayInSeconds)
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    if (
        (m_configurationRequestPeriod.count() != 0) &&
        (configurationRequestPeriodInSeconds == bc::duration_cast<bc::seconds>(m_configurationRequestPeriod).count())
        )
    {
        //do nothing
    }
    else
    {
        if (configurationRequestPeriodInSeconds > 0)
        {
            m_configurationRequestPeriod = bc::seconds(configurationRequestPeriodInSeconds);
            if (m_lastConfigurationRequestTime == ZERO_TIME_STEADY)
            {
                //Retrieve the configuration as soon as possible
                m_nextConfigurationRequestTime = m_pClock->getSteadyTime();
            }
            else
            {
                //Do it sometime in the future
                m_nextConfigurationRequestTime = m_lastConfigurationRequestTime + m_configurationRequestPeriod;
            }

            std::ostringstream ss;
            ss << "Starting sending periodic requests for configuration (period=" << configurationRequestPeriodInSeconds << "s)";
            Logger::log(LOG_LEVEL_INFO, ss.str().c_str());

            notifyObservers(eSTARTING);
        }
        else
        {
            m_configurationRequestPeriod = bc::steady_clock::duration::zero();

            std::ostringstream ss;
            ss << "Requests for configuration to the InStation have been disabled";
            Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
        }
    }

    //We do not modify the startupTime so that it does not get moved forward
    //with the subsequent start invocations
    m_startupTimeWithDelay = m_startupTime + bc::seconds(startupDelayInSeconds);
}

void RetrieveConfigurationTask::stop()
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    Logger::log(LOG_LEVEL_INFO, "Stopping sending periodic requests for configuration");

    m_configurationRequestPeriod = bc::steady_clock::duration::zero();
    notifyObservers(eSTOPPING);
}

void RetrieveConfigurationTask::notifyOfStateChange(::IObservable* pObservable, const int index)
{
    assert(pObservable != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste

    {
        InStationHTTPClient* pInStationHTTPClient =
            dynamic_cast<InStationHTTPClient* >(pObservable);

        if (pInStationHTTPClient != 0)
        {
            const TSteadyTimePoint currentTime(m_pClock->getSteadyTime());

            if (index == InStationHTTPClient::eLAST_CONFIGURATION_REQUEST_HAS_SUCCEDED) //i.e. STATUS OK
            {
                m_nextConfigurationRequestTime = currentTime + m_configurationRequestPeriod;
            }
            else if (index == InStationHTTPClient::eLAST_CONFIGURATION_REQUEST_HAS_FAILED)
            {
                //In the case of failure send new request earilier, but not to early not to hog the connection
                m_nextConfigurationRequestTime = currentTime +
                    bc::seconds(std::min((int)bc::duration_cast<bc::seconds>(m_configurationRequestPeriod).count(), 60));
            }
            else
            {
                //do nothing
            }

            return;
        }
        //else do nothing
    }
}

} //namespace
