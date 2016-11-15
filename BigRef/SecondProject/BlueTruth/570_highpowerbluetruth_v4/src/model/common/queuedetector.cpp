#include "stdafx.h"
#include "queuedetector.h"

#include "clock.h"
#include "lock.h"
#include "logger.h"


namespace
{
    const char MODULE_NAME[] = "QueueDetector";
}


namespace QueueDetection
{

using Model::TRemoteDeviceRecord;


QueueDetector::QueueDetector(
        TRemoteDeviceRecordCollection& deviceCollection,
        ::Mutex& deviceCollectionMutex,
        ::Clock* pClock)
:
::IObservable(),
::IObserver(),
m_deviceCollection(deviceCollection),
m_deviceCollectionMutex(deviceCollectionMutex),
m_pClock(pClock),
m_isConfigured(false),
m_inquiryScanDurationInSeconds(0),
m_dropOutScanCycleThresholdInSeconds(0),
m_dropOutScanCycleThreshold(0),
m_freeFlowThresholdInSeconds(0),
m_freeFlowThreshold(0),
m_moderateFlowThresholdInSeconds(0),
m_moderateFlowThreshold(0),
m_slowFlowThresholdInSeconds(0),
m_slowFlowThreshold(0),
m_verySlowFlowThresholdInSeconds(0),
m_verySlowFlowThreshold(0),
m_queuePresenceState(eQUEUE_PRESENCE_STATE_NOT_READY),
m_queueAlertThresholdBin(Model::eBIN_TYPE_UNDEFINED),
m_queueDetectionThresholdNumber(0),
m_queueClearanceThresholdNumber(0),
m_congestionReport(),
m_congestionReportMutex(),
m_startupTime(pClock->getSteadyTime()),
m_startupTimeSinceZeroInSeconds(bc::duration_cast<bc::seconds>(m_startupTime - ZERO_TIME_STEADY).count()),
m_startupIntervalInSeconds()
{
    //do nothing
}

QueueDetector::~QueueDetector()
{
    //do nothing
}

/** @brief Setup all necessary variables required for Queue Detector functionality
*/
//TODO For clarity this method should be split into two: one for v3- and one for v4+
void QueueDetector::setup(
    const unsigned int inquiryScanDurationInSeconds,
    const unsigned int dropOutScanCycleThresholdInSeconds,
    const unsigned int freeFlowThresholdInSeconds,
    const unsigned int moderateFlowThresholdInSeconds,
    const unsigned int slowFlowThresholdInSeconds,
    const unsigned int verySlowFlowThresholdInSeconds,
    const Model::EBinType queueAlertThresholdBin,
    const unsigned int queueDetectionThresholdNumber,
    const unsigned int queueClearanceThresholdNumber,
    const unsigned int startupIntervalInSeconds)
{
    m_inquiryScanDurationInSeconds = inquiryScanDurationInSeconds;

    m_dropOutScanCycleThresholdInSeconds = dropOutScanCycleThresholdInSeconds;
    m_freeFlowThresholdInSeconds = freeFlowThresholdInSeconds;
    m_moderateFlowThresholdInSeconds = moderateFlowThresholdInSeconds;
    m_slowFlowThresholdInSeconds = slowFlowThresholdInSeconds;
    m_verySlowFlowThresholdInSeconds = verySlowFlowThresholdInSeconds;
    m_queueAlertThresholdBin = queueAlertThresholdBin;
    m_queueDetectionThresholdNumber = queueDetectionThresholdNumber;
    m_queueClearanceThresholdNumber = queueClearanceThresholdNumber;
    m_startupIntervalInSeconds = startupIntervalInSeconds;

    if (m_inquiryScanDurationInSeconds > 0)
    {
        m_dropOutScanCycleThreshold = dropOutScanCycleThresholdInSeconds / inquiryScanDurationInSeconds;
        m_freeFlowThreshold = freeFlowThresholdInSeconds / inquiryScanDurationInSeconds;
        m_moderateFlowThreshold = m_freeFlowThreshold + moderateFlowThresholdInSeconds / inquiryScanDurationInSeconds;
        m_slowFlowThreshold = m_moderateFlowThreshold + slowFlowThresholdInSeconds / inquiryScanDurationInSeconds;
        m_verySlowFlowThreshold = m_slowFlowThreshold+ verySlowFlowThresholdInSeconds / inquiryScanDurationInSeconds;
    }
    //else do nothing

    //Verify if configuration is valid and log what is wrong
    std::vector<std::string> errors;
    if (!(m_inquiryScanDurationInSeconds > 0))
        errors.push_back("Invalid Scan Duration");

    if (!(m_dropOutScanCycleThresholdInSeconds > 0))
        errors.push_back("Invalid Drop-Out Scan Cycle Threshold");

    if (!(m_queueDetectionThresholdNumber >= m_queueClearanceThresholdNumber))
        errors.push_back("Queue Alert Threshold Detection Number must be greater than Clearance Threshold Detection Number");

    if (!(m_queueAlertThresholdBin != Model::eBIN_TYPE_UNDEFINED))
        errors.push_back("Queue Alert Threshold Bin must be defined");

    if (!(m_freeFlowThreshold < m_moderateFlowThreshold))
        errors.push_back("Free Flow Threshold must be greater than Moderate Flow Threshold");

    if (!(m_moderateFlowThreshold < m_slowFlowThreshold))
        errors.push_back("Moderate Flow Threshold must be greater than Slow Flow Threshold");

    if (!(m_slowFlowThreshold < m_verySlowFlowThreshold))
        errors.push_back("Slow Flow Threshold must be greater than Very Slow Flow Threshold");

    if (errors.empty())
    {
        m_isConfigured = true;
    }
    else
    {
        for (size_t i=0; i<errors.size(); ++i)
        {
            std::ostringstream ss;
            ss << "Configuration error: " << errors[i] << "! Queue Detector will be disabled";
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
    }
}

void QueueDetector::notifyOfStateChange(IObservable* pObservable, const int )
{
    assert(pObservable != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste

    {
        Model::DataContainer* pDataContainer =
            dynamic_cast<Model::DataContainer* >(pObservable);

        if (pDataContainer != 0)
        {
            if (pDataContainer->getBluetoothDeviceFault().get())
            {
                //Fault reported
                m_startupTime = ZERO_TIME_STEADY;
                m_startupTimeSinceZeroInSeconds = 0;
                m_queuePresenceState = eQUEUE_PRESENCE_STATE_FAULT;
            }
            else
            {
                if (m_startupTime == ZERO_TIME_STEADY)
                {
                    //Fault clearance, start-up
                    m_startupTime = m_pClock->getSteadyTime();
                    m_startupTimeSinceZeroInSeconds = bc::duration_cast<bc::seconds>(m_startupTime - ZERO_TIME_STEADY).count();
                    m_queuePresenceState = eQUEUE_PRESENCE_STATE_NOT_READY;
                }
                //else do nothing
            }
            return;
        }
        //else do nothing
    }
}

void QueueDetector::updateDevices()
{
    if (!m_isConfigured)
    {
        return;
    }
    //else continue


    ::Lock lock1(m_deviceCollectionMutex);
    ::Lock lock2(m_congestionReportMutex);

    unsigned int numberOfDevicesAboveThreshold = 0;
    m_congestionReport.numberOfDevicesInFreeFlowBin = 0;
    m_congestionReport.numberOfDevicesInModerateFlowBin = 0;
    m_congestionReport.numberOfDevicesInSlowFlowBin = 0;
    m_congestionReport.numberOfDevicesInVerySlowFlowBin = 0;
    m_congestionReport.numberOfDevicesInStaticFlowBin = 0;

    //Finally verify which bin each device should be and if queue is detected
    ::Lock lock(m_deviceCollectionMutex);
    for (TRemoteDeviceRecordCollection::iterator
            iter(m_deviceCollection.begin()), iterEnd(m_deviceCollection.end());
        iter != iterEnd;
        ++iter)
    {
        //Ignore background devices
        if (iter->second.binType == Model::eBIN_TYPE_BACKGROUND)
            continue;

        //Evaluate bin type the device belongs
        assert(iter->second.numberOfScans > iter->second.numberOfScansAbsent);
        const unsigned int correctedNumberOfScansDeviceIsVisible =
            iter->second.numberOfScans - iter->second.numberOfScansAbsent;
        if (correctedNumberOfScansDeviceIsVisible <= m_freeFlowThreshold)
        {
            iter->second.binType = Model::eBIN_TYPE_FREE_FLOW;
            ++m_congestionReport.numberOfDevicesInFreeFlowBin;
        }
        else if (correctedNumberOfScansDeviceIsVisible <= m_moderateFlowThreshold)
        {
            iter->second.binType = Model::eBIN_TYPE_MODERATE_FLOW;
            ++m_congestionReport.numberOfDevicesInModerateFlowBin;
        }
        else if (correctedNumberOfScansDeviceIsVisible <= m_slowFlowThreshold)
        {
            iter->second.binType = Model::eBIN_TYPE_SLOW_FLOW;
            ++m_congestionReport.numberOfDevicesInSlowFlowBin;
        }
        else if (correctedNumberOfScansDeviceIsVisible <= m_verySlowFlowThreshold)
        {
            iter->second.binType = Model::eBIN_TYPE_VERY_SLOW_FLOW;
            ++m_congestionReport.numberOfDevicesInVerySlowFlowBin;
        }
        else
        {
            iter->second.binType = Model::eBIN_TYPE_STATIC_FLOW;
            ++m_congestionReport.numberOfDevicesInStaticFlowBin;
        }

        //Compare if device is equal or above threshold
        if (static_cast<int>(iter->second.binType) >= static_cast<int>(m_queueAlertThresholdBin))
        {
            ++numberOfDevicesAboveThreshold;
        }
        //else do nothing
    }

    switch (m_queuePresenceState)
    {
        case eQUEUE_PRESENCE_STATE_QUEUE_PRESENT:
        {
            //Check if queue can be cleared
            if (numberOfDevicesAboveThreshold < m_queueClearanceThresholdNumber)
            {
                m_queuePresenceState = eQUEUE_PRESENCE_STATE_NO_QUEUE;
                m_congestionReport.queueStartTime = pt::not_a_date_time;
                m_congestionReport.queueEndTime = m_pClock->getUniversalTime();
            }
            //else do nothing

            break;
        }

        case eQUEUE_PRESENCE_STATE_NOT_READY:
        case eQUEUE_PRESENCE_STATE_FAULT:
        case eQUEUE_PRESENCE_STATE_NO_QUEUE:
        {
            if (
                (m_queuePresenceState == eQUEUE_PRESENCE_STATE_NOT_READY) ||
                (m_queuePresenceState == eQUEUE_PRESENCE_STATE_FAULT)
                )
            {
                m_queuePresenceState = eQUEUE_PRESENCE_STATE_NO_QUEUE;
            }
            //else do nothing

            //Check if queue should be alarmed
            if (numberOfDevicesAboveThreshold >= m_queueDetectionThresholdNumber)
            {
                m_queuePresenceState = eQUEUE_PRESENCE_STATE_QUEUE_PRESENT;
                m_congestionReport.queueStartTime = m_pClock->getUniversalTime();
                m_congestionReport.queueEndTime = pt::not_a_date_time;
            }
            //else do nothing

            break;
        }

        default:
        {
            break;
        }
    }

    m_congestionReport.queuePresenceState = m_queuePresenceState;
    notifyObservers();
}

/** @brief Update bin-membership of all currently seen devices
 *
 * @param numberOfSecondsSinceZero current time as number of seconds since 1970/1/1 0:00
 */
void QueueDetector::updateDevicesFromRawTime(const TSteadyTimePoint& currentTime)
{
    //This function should be only used for version 4 (periodic non-present devices must be cleaned in the background)
    if (!m_isConfigured)
    {
        return;
    }
    //else continue

    Logger::log(LOG_LEVEL_DEBUG2, "QueueDetector::updateDevicesFromRawTime()");

    const TSteadyTimeDuration CURRENT_TIME_SINCE_ZERO(currentTime - ZERO_TIME_STEADY);
    uint64_t currentTimeSinceZeroInSeconds = bc::duration_cast<bc::seconds>(CURRENT_TIME_SINCE_ZERO).count();

    ::Lock lock1(m_deviceCollectionMutex);
    ::Lock lock2(m_congestionReportMutex);

    unsigned int numberOfDevicesAboveThreshold = 0;
    m_congestionReport.numberOfDevicesInFreeFlowBin = 0;
    m_congestionReport.numberOfDevicesInModerateFlowBin = 0;
    m_congestionReport.numberOfDevicesInSlowFlowBin = 0;
    m_congestionReport.numberOfDevicesInVerySlowFlowBin = 0;
    m_congestionReport.numberOfDevicesInStaticFlowBin = 0;

    // Analyse all records already present (m_deviceCollection) and remove the obsoleted ones
    for (TRemoteDeviceRecordCollection::iterator
            iter(m_deviceCollection.begin()), iterEnd(m_deviceCollection.end());
        iter != iterEnd;
        ++iter)
    {
        //Ignore background devices
        if (iter->second.binType == Model::eBIN_TYPE_BACKGROUND)
            continue;

        const uint64_t DEVICE_OBSERVED_FOR_X_SECONDS =
            iter->second.lastObservationTimeSteady - iter->second.firstObservationTimeSteady;
        if (DEVICE_OBSERVED_FOR_X_SECONDS <= m_freeFlowThresholdInSeconds)
        {
            if (iter->second.binType != Model::eBIN_TYPE_FREE_FLOW)
            {
                iter->second.binType = Model::eBIN_TYPE_FREE_FLOW;

                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
                {
                    std::ostringstream ss;
                    ss << "Device " << std::hex << iter->second.address << " has been MOVED to FREE FLOW BIN";
                    Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
                }
                //else do nothing
            }
            //else do nothing

            ++m_congestionReport.numberOfDevicesInFreeFlowBin;
        }
        else if (DEVICE_OBSERVED_FOR_X_SECONDS <= m_moderateFlowThresholdInSeconds)
        {
            if (iter->second.binType != Model::eBIN_TYPE_MODERATE_FLOW)
            {
                iter->second.binType = Model::eBIN_TYPE_MODERATE_FLOW;

                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
                {
                    std::ostringstream ss;
                    ss << "Device " << std::hex << iter->second.address << " has been MOVED to MODERATE FLOW BIN";
                    Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
                }
                //else do nothing
            }
            //else do nothing

            ++m_congestionReport.numberOfDevicesInModerateFlowBin;
        }
        else if (DEVICE_OBSERVED_FOR_X_SECONDS <= m_slowFlowThresholdInSeconds)
        {
            if (iter->second.binType != Model::eBIN_TYPE_SLOW_FLOW)
            {
                iter->second.binType = Model::eBIN_TYPE_SLOW_FLOW;

                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
                {
                    std::ostringstream ss;
                    ss << "Device " << std::hex << iter->second.address << " has been MOVED to SLOW FLOW BIN";
                    Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
                }
                //else do nothing
            }
            //else do nothing

            ++m_congestionReport.numberOfDevicesInSlowFlowBin;
        }
        else if (DEVICE_OBSERVED_FOR_X_SECONDS <= m_verySlowFlowThresholdInSeconds)
        {
            if (iter->second.binType != Model::eBIN_TYPE_VERY_SLOW_FLOW)
            {
                iter->second.binType = Model::eBIN_TYPE_VERY_SLOW_FLOW;

                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
                {
                    std::ostringstream ss;
                    ss << "Device " << std::hex << iter->second.address << " has been MOVED to VERY SLOW FLOW BIN";
                    Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
                }
                //else do nothing
            }
            //else do nothing

            ++m_congestionReport.numberOfDevicesInVerySlowFlowBin;
        }
        else
        {
            if (iter->second.binType != Model::eBIN_TYPE_STATIC_FLOW)
            {
                iter->second.binType = Model::eBIN_TYPE_STATIC_FLOW;

                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
                {
                    std::ostringstream ss;
                    ss << "Device " << std::hex << iter->second.address << " has been MOVED to STATIC FLOW BIN";
                    Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
                }
                //else do nothing
            }
            //else do nothing

            ++m_congestionReport.numberOfDevicesInStaticFlowBin;
        }

        //Compare if device is equal or above threshold
        if (static_cast<int>(iter->second.binType) >= static_cast<int>(m_queueAlertThresholdBin))
        {
            ++numberOfDevicesAboveThreshold;
        }
        //else do nothing
    }

    //Evaluate the queue state
    if (m_queuePresenceState == eQUEUE_PRESENCE_STATE_FAULT)
    {
        //do nothing
    }
    else if (currentTimeSinceZeroInSeconds - m_startupTimeSinceZeroInSeconds >= m_startupIntervalInSeconds)
    {
        switch (m_queuePresenceState)
        {
            case eQUEUE_PRESENCE_STATE_QUEUE_PRESENT:
            {
                //Check if queue can be cleared
                if (numberOfDevicesAboveThreshold < m_queueClearanceThresholdNumber)
                {
                    m_queuePresenceState = eQUEUE_PRESENCE_STATE_NO_QUEUE;
                    m_congestionReport.queueStartTime = pt::not_a_date_time;
                    m_congestionReport.queueEndTime = m_pClock->getUniversalTime();
                }
                //else do nothing

                break;
            }

            case eQUEUE_PRESENCE_STATE_NOT_READY:
            case eQUEUE_PRESENCE_STATE_FAULT:
            case eQUEUE_PRESENCE_STATE_NO_QUEUE:
            {
                if (m_queuePresenceState == eQUEUE_PRESENCE_STATE_NOT_READY)
                {
                    m_queuePresenceState = eQUEUE_PRESENCE_STATE_NO_QUEUE;
                }
                //else do nothing

                //Check if queue should be alarmed
                if (numberOfDevicesAboveThreshold >= m_queueDetectionThresholdNumber)
                {
                    m_queuePresenceState = eQUEUE_PRESENCE_STATE_QUEUE_PRESENT;
                    m_congestionReport.queueStartTime = m_pClock->getUniversalTime();
                    m_congestionReport.queueEndTime = pt::not_a_date_time;
                }
                //else do nothing

                break;
            }

            default:
            {
                break;
            }
        }
    }
    else
    {
        m_queuePresenceState = eQUEUE_PRESENCE_STATE_NOT_READY;
    }

    m_congestionReport.queuePresenceState = m_queuePresenceState;
    notifyObservers();
}

void QueueDetector::reset()
{
    ::Lock lock1(m_deviceCollectionMutex);
    ::Lock lock(m_congestionReportMutex);
    m_deviceCollection.clear();
    m_congestionReport.reset();
}

unsigned int QueueDetector::getDeviceCount(const Model::EBinType binType) const
{
    unsigned int result = 0;

    for (TRemoteDeviceRecordCollection::const_iterator
            iter(m_deviceCollection.begin()), iterEnd(m_deviceCollection.end());
        iter != iterEnd;
        ++iter)
    {
        if (iter->second.binType == binType)
        {
            ++result;
        }
        //else do nothing
    }

    return result;
}

TCongestionReport QueueDetector::getCongestionReport() const
{
    ::Lock lock(m_congestionReportMutex);
    return m_congestionReport;
}

}

namespace Model
{

const char* getBinTypeName(const EBinType type)
{
    const char* result = "UNDEFINED";
    switch (type)
    {
        case eBIN_TYPE_FREE_FLOW:
        {
            result = "FREE FLOW";
            break;
        }
        case eBIN_TYPE_MODERATE_FLOW:
        {
            result = "MODERATE FLOW";
            break;
        }
        case eBIN_TYPE_SLOW_FLOW:
        {
            result = "SLOW FLOW";
            break;
        }
        case eBIN_TYPE_VERY_SLOW_FLOW:
        {
            result = "VERY SLOW FLOW";
            break;
        }
        case eBIN_TYPE_STATIC_FLOW:
        {
            result = "STATIC FLOW";
            break;
        }
        case eBIN_TYPE_BACKGROUND:
        {
            result = "BACKGROUND";
            break;
        }
        default:
        {
            break;
        }
    }

    return result;
}

}
