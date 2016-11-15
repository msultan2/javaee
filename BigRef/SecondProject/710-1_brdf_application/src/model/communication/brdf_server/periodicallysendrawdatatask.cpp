#include "stdafx.h"
#include "brdf_server/periodicallysendrawdatatask.h"

#include "clock.h"
#include "brdf_server/ibrdfserverreporter.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"

#include <sstream>

#include <signal.h>
#include "app.h"


namespace
{
    const unsigned int DEFAULT_SEND_INTERVAL_IN_S = 10000;
    const uint64_t MILLISECONDS_IN_SECOND = 1000ULL;
}


namespace BrdfServer
{

PeriodicallySendRawDataTask::PeriodicallySendRawDataTask(
    boost::shared_ptr<IBrdfServerReporter> pBrdfServerReporter,
    ::Clock* pClock)
:
::ITask(),
::IObserver(),
::IObservable(),
m_pBrdfServerReporter(pBrdfServerReporter),
m_pClock(pClock),
m_lastReportTime(m_pClock->getUniversalTime()),
m_nextReportTime(pt::not_a_date_time),
m_reportingPeriod(pt::not_a_date_time),
m_requestReceived(true)
{
    //do nothing
}

PeriodicallySendRawDataTask::~PeriodicallySendRawDataTask()
{
    //do nothing
}

void PeriodicallySendRawDataTask::setupParameters(const unsigned int reportingPeriodInSeconds)
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    if (
        !m_reportingPeriod.is_not_a_date_time() &&
        (static_cast<long>(reportingPeriodInSeconds) == m_reportingPeriod.total_seconds())
        )
    {
        //do nothing
    }
    else
    {
        if (reportingPeriodInSeconds > 0)
        {
            m_reportingPeriod = pt::seconds(static_cast<long>(reportingPeriodInSeconds));
            m_nextReportTime = m_lastReportTime + m_reportingPeriod;

            std::ostringstream ss;
            ss << "Setting RAW DATA period to value " << reportingPeriodInSeconds << "s";
            Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
        }
        else
        {
            m_reportingPeriod = pt::not_a_date_time;

            std::ostringstream ss;
            ss << "Sending of RAW DATA has been disabled";
            Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
        }
    }
}

void PeriodicallySendRawDataTask::initialise()
{
    //do nothing
}

void PeriodicallySendRawDataTask::perform()
{
    //This method has been divided into two parts to release lock when calling
    //sendRawData(). This lock may be acquired when setup parameters are
    //updated.

    bool doSendRawData = false;

    {
        boost::unique_lock<boost::mutex> lock(m_mutex);

        if (!m_reportingPeriod.is_not_a_date_time())
        {
            const ::TTime_t currentTime(m_pClock->getUniversalTime());

            if (
                m_requestReceived ||
                (currentTime >= m_nextReportTime)
                )
            {
                m_requestReceived = false;
                m_lastReportTime = currentTime;
                m_nextReportTime = currentTime + m_reportingPeriod;

                doSendRawData = true;
            }
            //else do nothing
        }
        //else do nothing
    }

    if (doSendRawData)
    {
        Logger::log(LOG_LEVEL_DEBUG3, "RAW DATA will be submitted for sending");

        notifyObservers(eSENDING_RAW_DATA);
        IBrdfServerReporter::ESendRawDataResult result = m_pBrdfServerReporter->sendRawData();

        if (result == IBrdfServerReporter::eSEND_RAW_DATA_RESULT__OK)
        {
            Logger::log(LOG_LEVEL_DEBUG3, "RAW DATA has been submitted for sending");
        }
        else
        {
            Logger::log(LOG_LEVEL_DEBUG3, "RAW DATA has not been submitted for sending");
        }
    }
    //else do nothing
}

void PeriodicallySendRawDataTask::shutdown(const char* )
{
    if (!m_reportingPeriod.is_not_a_date_time())
    {
        stop();
    }
    //else do nothing
}

void PeriodicallySendRawDataTask::start(const unsigned int reportingPeriodInSeconds)
{
    setupParameters(reportingPeriodInSeconds);

    if (reportingPeriodInSeconds > 0)
    {
        std::ostringstream ss;
        ss << "Starting sending of RAW DATA (period=" << reportingPeriodInSeconds << "s)";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());

        notifyObservers(eSTARTING);
    }
    //else do nothing
}

void PeriodicallySendRawDataTask::stop()
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    Logger::log(LOG_LEVEL_INFO, "Stopping periodic sending of RAW DATA");

    m_reportingPeriod = pt::not_a_date_time;
    notifyObservers(eSTOPPING);
}

bool PeriodicallySendRawDataTask::isRunning() const
{
    boost::unique_lock<boost::mutex> lock(m_mutex);

    return (!m_reportingPeriod.is_not_a_date_time());
}

void PeriodicallySendRawDataTask::notifyOfStateChange(IObservable* pObservable, const int index)
{
    assert(pObservable != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste

    {
        IBrdfServerReporter* brdfServerReporter =
            dynamic_cast<IBrdfServerReporter* >(pObservable);

        if (brdfServerReporter == m_pBrdfServerReporter.get())
        {
            switch (index)
            {
                case IBrdfServerReporter::eLAST_RAW_DATA_HAS_BEEN_SENT:
                {
                    push();
                    break;
                }

                case IBrdfServerReporter::eLAST_RAW_DATA_HAS_FAILED:
                {
                    break;
                }

                default:
                {
                    //do nothing
                }
            }

            return;
        }
        //else do nothing
    }
}

void PeriodicallySendRawDataTask::push()
{
    m_requestReceived = true;
}

} //namespace
