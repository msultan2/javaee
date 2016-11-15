#include "stdafx.h"
#include "periodicallysendcongestionreporttask.h"

#include "clock.h"
#include "instation/instationreporter.h"
#include "iniconfiguration.h"
#include "lock.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"

#include <sstream>


namespace
{
    const uint64_t MILLISECONDS_IN_SECOND = 1000ULL;
}


namespace InStation
{

PeriodicallySendCongestionReportTask::PeriodicallySendCongestionReportTask(
    boost::shared_ptr<InStationReporter> pInStationReporter,
    ::Clock* pClock)
:
::ITask(),
::IObservable(),
m_pInStationReporter(pInStationReporter),
m_pClock(pClock),
m_startupTime(m_pClock->getSteadyTime()),
m_startupTimeWithDelay(m_startupTime),
m_lastReportTime(m_pClock->getSteadyTime()),
m_nextReportTime(),
m_reportingPeriod()
{
    //do nothing
}

PeriodicallySendCongestionReportTask::~PeriodicallySendCongestionReportTask()
{
    //do nothing
}

void PeriodicallySendCongestionReportTask::initialise()
{
    //do nothing
}

void PeriodicallySendCongestionReportTask::perform()
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    if (m_reportingPeriod.count() != 0)
    {
        const TSteadyTimePoint currentTime(m_pClock->getSteadyTime());

        if (
            (currentTime >= m_startupTimeWithDelay) &&
            (currentTime >= m_nextReportTime)
            )
        {
            m_lastReportTime = currentTime;
            m_nextReportTime = currentTime + m_reportingPeriod;

            notifyObservers(eSENDING_CONGESTION_REPORT);

            Logger::log(LOG_LEVEL_DEBUG2, "CONGESTION_REPORT will be submitted for sending");

            assert(m_pInStationReporter != 0);
            m_pInStationReporter->sendCongestionReport();

            Logger::log(LOG_LEVEL_DEBUG2, "CONGESTION_REPORT has been submitted for sending");
        }
        //else do nothing
    }
    //else do nothing
}

void PeriodicallySendCongestionReportTask::shutdown()
{
    stop();
}

void PeriodicallySendCongestionReportTask::start(
    const unsigned int reportingPeriodInSeconds, const unsigned int startupDelayInSeconds)
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    if (
        (m_reportingPeriod.count() != 0) &&
        (reportingPeriodInSeconds == bc::duration_cast<bc::seconds>(m_reportingPeriod).count())
        )
    {
        //do nothing
    }
    else
    {
        if (reportingPeriodInSeconds > 0)
        {
            m_reportingPeriod = bc::seconds(reportingPeriodInSeconds);
            m_nextReportTime = m_lastReportTime + m_reportingPeriod;

            std::ostringstream ss;
            ss << "Starting reporting of congestion to the InStation (period=" << reportingPeriodInSeconds << "s)";
            Logger::log(LOG_LEVEL_INFO, ss.str().c_str());

            notifyObservers(eSTARTING);
        }
        else
        {
            m_reportingPeriod = bc::steady_clock::duration::zero();

            std::ostringstream ss;
            ss << "Reporting of congestion to the InStation has been disabled";
            Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
        }
    }

    //We do not modify the startupTime so that it does not get moved forward
    //with the subsequent start invocations
    m_startupTimeWithDelay = m_startupTime + bc::seconds(startupDelayInSeconds);
}

void PeriodicallySendCongestionReportTask::stop()
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    Logger::log(LOG_LEVEL_INFO, "Stopping periodic reporting of congestion to the InStation");

    m_reportingPeriod = bc::steady_clock::duration::zero();
    notifyObservers(eSTOPPING);
}

bool PeriodicallySendCongestionReportTask::isRunning() const
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    return (m_reportingPeriod.count() != 0);
}


} //namespace
