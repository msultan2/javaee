#include "stdafx.h"
#include "periodicallyremovenonpresentdevicestask.h"

#include "clock.h"
#include "datacontainer.h"
#include "iniconfiguration.h"
#include "lock.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"


namespace
{
    const unsigned int DEFAULT_SEND_INTERVAL_IN_S = 1000;
    const uint64_t MILLISECONDS_IN_SECOND = 1000ULL;
}


namespace BlueTooth
{

PeriodicallyRemoveNonPresentDevicesTask::PeriodicallyRemoveNonPresentDevicesTask(
    boost::shared_ptr<Model::DataContainer> pDataContainer,
    ::Clock* pClock)
:
::ITask(),
::IObservable(),
m_pDataContainer(pDataContainer),
m_pClock(pClock),
m_lastActionTime(m_pClock->getSteadyTime()),
m_actionPeriod(),
m_absenceThresholdInSeconds(0),
m_moveToNonPresentCollectionForReporting(false)
{
    //do nothing
}

PeriodicallyRemoveNonPresentDevicesTask::~PeriodicallyRemoveNonPresentDevicesTask()
{
    //do nothing
}

void PeriodicallyRemoveNonPresentDevicesTask::initialise()
{
    //do nothing
}

void PeriodicallyRemoveNonPresentDevicesTask::perform()
{
    if (
        (m_actionPeriod.count() != 0) &&
        (m_absenceThresholdInSeconds > 0)
        )
    {
        const TSteadyTimePoint CURRENT_TIME_STEADY(m_pClock->getSteadyTime());
        if (CURRENT_TIME_STEADY - m_lastActionTime >= m_actionPeriod)
        {
            m_lastActionTime = CURRENT_TIME_STEADY;

            assert(m_pDataContainer != 0);

            const TTime_t CURRENT_TIME_UTC(m_pClock->getUniversalTime());
            const TTimeDiff_t TIME_SINCE_ZERO_UTC(CURRENT_TIME_UTC - ZERO_TIME_UTC);
            const uint64_t TIME_SINCE_ZERO_TOTAL_SECONDS_UTC = TIME_SINCE_ZERO_UTC.total_seconds();

            notifyObservers(eREMOVING_NON_PRESENT_REMOTE_DEVICES);

            m_pDataContainer->removeNonPresentRemoteDeviceRecords(
                TIME_SINCE_ZERO_TOTAL_SECONDS_UTC,
                m_absenceThresholdInSeconds,
                m_moveToNonPresentCollectionForReporting);
        }
        //else do nothing
    }
    //else do nothing
}

void PeriodicallyRemoveNonPresentDevicesTask::shutdown()
{
    stop();
}

void PeriodicallyRemoveNonPresentDevicesTask::start(
    const unsigned int actionPeriodInSeconds,
    const unsigned int absenceThresholdInSeconds,
    const bool moveToNonPresentCollectionForReporting)
{
    if (
        (m_actionPeriod.count() != 0) &&
        (static_cast<long>(actionPeriodInSeconds) == bc::duration_cast<bc::seconds>(m_actionPeriod).count()) &&
        (absenceThresholdInSeconds == m_absenceThresholdInSeconds) &&
        (moveToNonPresentCollectionForReporting == m_moveToNonPresentCollectionForReporting)
        )
    {
        return;
    }
    //else something changed - update parameters and continue

    if (actionPeriodInSeconds > 0)
    {
        m_actionPeriod = bc::seconds(static_cast<long>(actionPeriodInSeconds));

        std::ostringstream ss;
        ss << "Starting regular removal of non-present devices (period=" << actionPeriodInSeconds << "s)";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());

        notifyObservers(eSTARTING);
    }
    else
    {
        m_actionPeriod = bc::steady_clock::duration::zero();

        std::ostringstream ss;
        ss << "Removal of non-present devices has been disabled";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
    }

    m_absenceThresholdInSeconds = absenceThresholdInSeconds;
    m_moveToNonPresentCollectionForReporting = moveToNonPresentCollectionForReporting;
}

void PeriodicallyRemoveNonPresentDevicesTask::stop()
{
    Logger::log(LOG_LEVEL_INFO, "Stopping removal of non-present devices");

    m_actionPeriod = bc::steady_clock::duration::zero();
    notifyObservers(eSTOPPING);
}

bool PeriodicallyRemoveNonPresentDevicesTask::isRunning() const
{
    return (m_actionPeriod.count() != 0);
}


} //namespace
