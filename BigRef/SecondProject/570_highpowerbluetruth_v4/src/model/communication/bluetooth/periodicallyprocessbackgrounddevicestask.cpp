#include "stdafx.h"
#include "periodicallyprocessbackgrounddevicestask.h"

#include "applicationconfiguration.h"
#include "backgrounddevicesparser.h"
#include "clock.h"
#include "datacontainer.h"
#include "lock.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"


namespace
{
    const unsigned int DEFAULT_SEND_INTERVAL_IN_S = 1000;
    const uint64_t MILLISECONDS_IN_SECOND = 1000ULL;

    const char BACKGROUND_DEVICES_FILE_NAME[] = "background_devices.csv";
}


namespace BlueTooth
{

PeriodicallyProcessBackgroundDevicesTask::PeriodicallyProcessBackgroundDevicesTask(
    boost::shared_ptr<Model::DataContainer> pDataContainer,
    ::Clock* pClock)
:
::ITask(),
::IObservable(),
m_pDataContainer(pDataContainer),
m_pClock(pClock),
m_lastActionTime(m_pClock->getSteadyTime()),
m_actionPeriod(),
m_backgroundPresenceThresholdInSeconds(0),
m_backgroundAbsenceThresholdInSeconds(0)
{
    std::string contents;
    if (loadBackgroundDevicesFromFile(contents))
    {
        processBackgroundDeviceFileContents(contents);
    }
    //else do nothing
}

PeriodicallyProcessBackgroundDevicesTask::~PeriodicallyProcessBackgroundDevicesTask()
{
    //do nothing
}

void PeriodicallyProcessBackgroundDevicesTask::initialise()
{
    //do nothing
}

void PeriodicallyProcessBackgroundDevicesTask::perform()
{
    if (
        (m_actionPeriod.count() != 0) &&
        (m_backgroundPresenceThresholdInSeconds > 0) &&
        (m_backgroundAbsenceThresholdInSeconds > 0)
        )
    {
        const TSteadyTimePoint CURRENT_TIME_STEADY(m_pClock->getSteadyTime());

        if (CURRENT_TIME_STEADY - m_lastActionTime >= m_actionPeriod)
        {
            m_lastActionTime = CURRENT_TIME_STEADY;

            assert(m_pDataContainer != 0);

            notifyObservers(ePROCESSING_BACKGROUND_CRITERIA_FOR_DEVICES);

            const TTime_t CURRENT_TIME_UTC(m_pClock->getUniversalTime());
            const TTimeDiff_t TIME_SINCE_ZERO_UTC(CURRENT_TIME_UTC - ZERO_TIME_UTC);
            const uint64_t TIME_SINCE_ZERO_TOTAL_SECONDS_UTC = TIME_SINCE_ZERO_UTC.total_seconds();
            m_pDataContainer->reviewRemoteDevicesAgainstBackgroundCriteria(
                TIME_SINCE_ZERO_TOTAL_SECONDS_UTC,
                m_backgroundPresenceThresholdInSeconds,
                m_backgroundAbsenceThresholdInSeconds);

            std::vector<Model::TRemoteDeviceRecord> collectionOfBackgroundDevices;
            m_pDataContainer->getBackgroundDevicesCollection(collectionOfBackgroundDevices);

            saveBackgroundDevicesToFile(collectionOfBackgroundDevices);
        }
        //else do nothing
    }
    //else do nothing
}

void PeriodicallyProcessBackgroundDevicesTask::shutdown()
{
    stop();
}

void PeriodicallyProcessBackgroundDevicesTask::start(
    const unsigned int actionPeriodInSeconds,
    const unsigned int backgroundPresenceThresholdInSeconds,
    const unsigned int backgroundAbsenceThresholdInSeconds)
{
    if (
        (m_actionPeriod.count() != 0) &&
        (static_cast<long>(actionPeriodInSeconds) == bc::duration_cast<bc::seconds>(m_actionPeriod).count()) &&
        (backgroundPresenceThresholdInSeconds == m_backgroundPresenceThresholdInSeconds) &&
        (backgroundAbsenceThresholdInSeconds == m_backgroundAbsenceThresholdInSeconds)
        )
    {
        return;
    }
    //else something changed - update parameters and continue

    if (actionPeriodInSeconds > 0)
    {
        m_actionPeriod = bc::seconds(static_cast<long>(actionPeriodInSeconds));

        std::ostringstream ss;
        ss << "Starting regular \"background\" processing of devices (period=" << actionPeriodInSeconds << "s)";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());

        notifyObservers(eSTARTING);
    }
    else
    {
        m_actionPeriod = bc::steady_clock::duration::zero();

        std::ostringstream ss;
        ss << "\"Background\" processing of devices has been disabled";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
    }

    m_backgroundPresenceThresholdInSeconds = backgroundPresenceThresholdInSeconds;
    m_backgroundAbsenceThresholdInSeconds = backgroundAbsenceThresholdInSeconds;
}

void PeriodicallyProcessBackgroundDevicesTask::stop()
{
    Logger::log(LOG_LEVEL_INFO, "Stopping \"background\" processing of devices");

    m_actionPeriod = bc::steady_clock::duration::zero();
    notifyObservers(eSTOPPING);
}

bool PeriodicallyProcessBackgroundDevicesTask::isRunning() const
{
    return (m_actionPeriod.count() != 0);
}

bool PeriodicallyProcessBackgroundDevicesTask::loadBackgroundDevicesFromFile(std::string& contents)
{
    //Open output file in the user data directory
    std::string backgroundDevicesFileName;
    backgroundDevicesFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getCacheDirectory());
    backgroundDevicesFileName += BACKGROUND_DEVICES_FILE_NAME;

    std::ifstream backgroundDevicesFile;
    backgroundDevicesFile.open(backgroundDevicesFileName.c_str(), std::ifstream::in);

    bool result = false;

    if (backgroundDevicesFile.is_open())
    {
        int c = 0;

        while (backgroundDevicesFile.good())
        {
            c = backgroundDevicesFile.get();
            if (backgroundDevicesFile.good())
            {
                contents += static_cast<char>(c);
            }
            //else do nothing
        }

        result = true;
    }
    //else do nothing

    if (result)
    {
        std::ostringstream ss;
        ss << "Background devices file \"" << backgroundDevicesFileName << "\" has been loaded";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
    }
    else
    {
        std::ostringstream ss;
        ss << "Loading of background devices file \"" << backgroundDevicesFileName << "\" has failed";
        Logger::log(LOG_LEVEL_NOTICE, ss.str().c_str());
    }

    backgroundDevicesFile.close();

    return result;
}

bool PeriodicallyProcessBackgroundDevicesTask::saveBackgroundDevicesToFile(const std::vector<Model::TRemoteDeviceRecord>& data)
{
    //Open output file in the user data directory
    std::string backgroundDevicesFileName;
    backgroundDevicesFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getCacheDirectory());
    backgroundDevicesFileName += BACKGROUND_DEVICES_FILE_NAME;

    std::ofstream backgroundDevicesFile;
    backgroundDevicesFile.open(backgroundDevicesFileName.c_str(), std::ofstream::out);

    bool result = false;
    std::string fileContents;

    if (backgroundDevicesFile.is_open())
    {
        backgroundDevicesFile << std::hex;

        for (std::vector<Model::TRemoteDeviceRecord>::const_iterator iter(data.begin()), iterEnd(data.end());
            iter != iterEnd;
            ++iter)
        {
            backgroundDevicesFile
                << iter->address << ","
                << iter->firstObservationTimeUTC << ","
                << iter->lastObservationTimeUTC << "\n";
        }

        backgroundDevicesFile.close();
    }
    else
    {
        std::string logString("Could not open file \"");
        logString += BACKGROUND_DEVICES_FILE_NAME;
        logString += "\" for writing! Background devices will not be recorded!";
        Logger::log(LOG_LEVEL_ERROR, logString.c_str());
    }

    return result;
}

void PeriodicallyProcessBackgroundDevicesTask::processBackgroundDeviceFileContents(const std::string& contents)
{
    TBackgroundDevicesParserContext context;
    bool ok = BackgroundDevicesParser::parse(contents, context);

    if (ok)
    {
        ::Lock lock(m_pDataContainer->getRemoteDeviceCollectionMutex());
        Model::DataContainer::TRemoteDeviceRecordCollection& remoteDeviceCollection =
            m_pDataContainer->getRemoteDeviceCollection();

        for (TBackgroundDevices::TBackgroundDevicesCollection::const_iterator
                iter(context.backgrounddevices.items.begin()),
                iterEnd(context.backgrounddevices.items.end());
            iter != iterEnd;
            ++iter)
        {
            Model::TRemoteDeviceRecord record;
            record.address = iter->address;
            record.firstObservationTimeUTC = iter->firstObservationTimeUTC;
            record.lastObservationTimeUTC = iter->lastObservationTimeUTC;
            //record.binType = eBIN_TYPE_BACKGROUND;
            remoteDeviceCollection[iter->address] = record;
        }

        const TTime_t CURRENT_TIME_UTC(m_pClock->getUniversalTime());
        const TTimeDiff_t TIME_SINCE_ZERO_UTC(CURRENT_TIME_UTC - ZERO_TIME_UTC);
        const uint64_t TIME_SINCE_ZERO_TOTAL_SECONDS_UTC = TIME_SINCE_ZERO_UTC.total_seconds();
        m_pDataContainer->reviewRemoteDevicesAgainstBackgroundCriteria(
            TIME_SINCE_ZERO_TOTAL_SECONDS_UTC,
            m_backgroundPresenceThresholdInSeconds,
            m_backgroundAbsenceThresholdInSeconds);
    }
    else
    {
        Logger::log(LOG_LEVEL_ERROR, "Parsing of background device file has failed");
    }
}

} //namespace
