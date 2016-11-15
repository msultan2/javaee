#include "stdafx.h"
#include "model.h"

#include "activeboostasio.h"
#include "activeboostasiotcpclient.h"
#include "activetask.h"
#include "app.h"
#include "clock.h"
#include "datacontainer.h"
#include "fault.h"
#include "bluetooth/acquiredevicestask.h"
#include "bluetooth/configurationsaver.h"
#include "bluetooth/devicediscoverer.h"
#include "bluetooth/periodicallyprocessbackgrounddevicestask.h"
#include "bluetooth/periodicallyremovenonpresentdevicestask.h"
#include "configuration/coreconfiguration.h"
#include "configuration/iniconfiguration.h"
#include "configuration/seedconfiguration.h"
#include "gsmmodem/activegsmmodemmonitor.h"
#include "gsmmodem/gsmmodemsignallevelprocessor.h"
#include "instation/instationhttpclient.h"
#include "instation/instationreporter.h"
#include "instation/periodicallysendcongestionreporttask.h"
#include "instation/periodicallysendstatisticsreporttask.h"
#include "instation/periodicallysendstatusreporttask.h"
#include "instation/retrieveconfigurationtask.h"
#include "instation/signaturegenerator.h"
#include "logger.h"
#include "os_utilities.h"
#include "queuedetector.h"
#include "ssh/reversesshconnector.h"
#include "uri.h"
#include "utils.h"
#include "view.h"


#if defined(WIN32) || defined(WINCE)
#include <conio.h>
#include <direct.h>
#else
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#endif

#include <signal.h>
#include <time.h>


namespace
{
    char MODULE_NAME[] = "Model";
}

namespace Model
{

Model* Model::m_instancePtr = 0;
bool Model::m_valid = true;

Model* Model::getInstancePtr()
{
    return m_instancePtr;
}

Model::Model(CoreConfiguration& coreConfiguration)
:
m_pDataContainer(),
m_pQueueDetector(),
m_pIniConfiguration(),
m_coreConfiguration(coreConfiguration),
m_pSeedConfiguration(),
m_pSignatureGenerator(),
m_modeNumber(0),
m_retrieveConfigurationConnectionParametersAreValid(false),
m_retrieveConfigurationRemoteAddress(),
m_retrieveConfigurationRemotePortNumber(0),
m_retrieveConfigurationLocalAddress(),
m_congestionReportingConnectionParametersAreValid(false),
m_congestionReportingRemoteAddress(),
m_congestionReportingRemotePortNumber(0),
m_congestionReportingLocalAddress(),
m_rawDeviceDetectionConnectionParametersAreValid(false),
m_rawDeviceDetectionRemoteAddress(),
m_rawDeviceDetectionRemotePortNumber(0),
m_rawDeviceDetectionLocalAddress(),
m_alertAndStatusReportingConnectionParametersAreValid(false),
m_alertAndStatusReportingRemoteAddress(),
m_alertAndStatusReportingRemotePortNumber(0),
m_alertAndStatusReportingLocalAddress(),
m_statusReportingConnectionParametersAreValid(false),
m_statusReportingRemoteAddress(),
m_statusReportingRemotePortNumber(0),
m_statusReportingLocalAddress(),
m_faultReportingConnectionParametersAreValid(false),
m_faultReportingRemoteAddress(),
m_faultReportingRemotePortNumber(0),
m_faultReportingLocalAddress(),
m_statisticsReportingConnectionParametersAreValid(false),
m_statisticsReportingRemoteAddress(),
m_statisticsReportingRemotePortNumber(0),
m_statisticsReportingLocalAddress(),
m_pRetrieveConfigurationWorkerThread(),
m_pRetrieveConfigurationActiveTCPClient(),
m_pRetrieveConfigurationClient(),
m_pActiveRetrieveConfigurationClientTask(),
m_pInStationReporter(),
m_pGeneralWorkerThread(),
m_pCongestionReportingWorkerThread(),
m_pCongestionReportingActiveTCPClient(),
m_pCongestionReportingClient(),
m_pActiveCongestionReportingClientTask(),
m_pRawDeviceDetectionWorkerThread(),
m_pRawDeviceDetectionActiveTCPClient(),
m_pRawDeviceDetectionClient(),
m_pActiveRawDeviceDetectionClientTask(),
m_pAlertAndStatusReportingWorkerThread(),
m_pAlertAndStatusReportingActiveTCPClient(),
m_pAlertAndStatusReportingClient(),
m_pActiveAlertAndStatusReportingClientTask(),
m_pStatusReportingWorkerThread(),
m_pStatusReportingActiveTCPClient(),
m_pStatusReportingClient(),
m_pActiveStatusReportingClientTask(),
m_pFaultReportingWorkerThread(),
m_pFaultReportingActiveTCPClient(),
m_pFaultReportingClient(),
m_pActiveFaultReportingClientTask(),
m_pStatisticsReportingWorkerThread(),
m_pStatisticsReportingActiveTCPClient(),
m_pStatisticsReportingClient(),
m_pActiveStatisticsReportingClientTask(),
m_pActiveRetrieveConfigurationTask(),
m_pRetrieveConfigurationTask(),
m_pDeviceDiscoverer(),
m_pActiveAcquireDevicesTask(),
m_pAcquireDevicesTask(),
m_pActivePeriodicallyProcessBackgroundDevicesTask(),
m_pPeriodicallyProcessBackgroundDevicesTask(),
m_pActivePeriodicallyRemoveNonPresentDevicesTask(),
m_pPeriodicallyRemoveNonPresentDevicesTask(),
m_pActivePeriodicallySendCongestionReportTask(),
m_pPeriodicallySendCongestionReportTask(),
m_pActivePeriodicallySendStatisticsReportTask(),
m_pPeriodicallySendStatisticsReportTask(),
m_pActivePeriodicallySendStatusReportTask(),
m_pPeriodicallySendStatusReportTask(),
m_pActiveInStationReverseSSHConnectorTask(),
m_pInStationReverseSSHConnector(),
m_pConfigurationSaver(),
m_pActiveGSMModemMonitor(),
m_pSignalLevelProcessor()
{
    m_pDataContainer = boost::shared_ptr<DataContainer>(new DataContainer());
    m_pDataContainer->addObserver(View::View::getInstancePtr());

    //Set device driver depending on the setting from the configuration file
    {
        std::string currentDeviceDriver(m_coreConfiguration.getDeviceDriver());
#if defined _WIN32
        if (currentDeviceDriver == "WindowsWSA")
        {
            m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                eDEVICE_DRIVER_WINDOWS_WSA;
        }
        else if (currentDeviceDriver == "WindowsBluetooth")
        {
            m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                eDEVICE_DRIVER_WINDOWS_BLUETOOTH;
        }
        else
        {
            Logger::log(LOG_LEVEL_WARNING, "Invalid Bluetooth driver name in the core configuration file. Using WindowsBluetooth instead");
            m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                eDEVICE_DRIVER_WINDOWS_BLUETOOTH;
        }

#elif defined __linux__

        if (currentDeviceDriver == "NativeBluez")
        {
            m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ;
        }
        else if (currentDeviceDriver == "Parani")
        {
            m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                eDEVICE_DRIVER_LINUX_PARANI;
            //Additonally copy settings from configuration file to data container (port name and bit rate)
            m_pDataContainer->getLocalDeviceConfiguration().paraniPortName =
                m_coreConfiguration.getParaniPortName();
            m_pDataContainer->getLocalDeviceConfiguration().paraniBitRate =
                m_coreConfiguration.getParaniBitRate();
        }
        else
        {
            m_pDataContainer->getLocalDeviceConfiguration().deviceDriver =
                eDEVICE_DRIVER_LINUX_RAW_HCI;
        }

#else
#error Operating System not supported
#endif
    }

    //Send notification to clean some labels left dirty during design
    m_pDataContainer->notifyObservers();


    m_pQueueDetector = boost::shared_ptr<QueueDetection::QueueDetector>(
        new QueueDetection::QueueDetector(
            m_pDataContainer->getRemoteDeviceCollection(),
            m_pDataContainer->getRemoteDeviceCollectionMutex(),
            &m_clock));
    m_pQueueDetector->addObserver(View::View::getInstancePtr());
    m_pDataContainer->addObserver(&*m_pQueueDetector);

    m_pInStationReverseSSHConnector = boost::shared_ptr<InStation::ReverseSSHConnector>(
        new InStation::ReverseSSHConnector(&m_pDataContainer->getInStationSSHUnableToConnectFault()));
    m_pActiveInStationReverseSSHConnectorTask = boost::shared_ptr<ActiveTask>(
        new ActiveTask(m_pInStationReverseSSHConnector));
    m_pInStationReverseSSHConnector->setup(
        m_coreConfiguration.getInstationSSHConnectionAddress(),
        m_coreConfiguration.getInstationSSHConnectionPort(),
        m_coreConfiguration.getInstationSSHConnectionLogin(),
        m_coreConfiguration.getInstationSSHConnectionPassword()
    );

    m_pSeedConfiguration = boost::shared_ptr<SeedConfiguration>(
        new SeedConfiguration());

    m_pSignatureGenerator = boost::shared_ptr<InStation::SignatureGenerator>(
        new InStation::SignatureGenerator());

    m_pConfigurationSaver = boost::shared_ptr<BlueTooth::ConfigurationSaver>(
        new BlueTooth::ConfigurationSaver(m_coreConfiguration));
    m_pDataContainer->addObserver(m_pConfigurationSaver.get());


    m_pIniConfiguration = boost::shared_ptr<IniConfiguration>(new IniConfiguration());
    m_pIniConfiguration->overwriteValuesWithCommandLineParameters(
        m_coreConfiguration.getCommandLineStatusReportURL());

    //Start retrieve configuration classes only if the configuration URL is not empty
    if (!m_coreConfiguration.getConfigurationURL().empty())
    {
        //Check if URL to retrieve configuration parameters are correct
        if (_setRetrieveConfigurationConnectionParameters())
        {
            // Read last retrieved configuration file.
            // If the file exists use its contents, otherwise use default settings
            if (m_pIniConfiguration->loadFromFile(IniConfiguration::LOAD_FILE_FROM_CACHE_DIRECTORY))
            {
                if (m_pIniConfiguration->processConfigurationText())
                {
                    _applyNewIniConfiguration(m_pIniConfiguration, true, true);
                }
                else
                {
                    Logger::log(LOG_LEVEL_WARNING,
                        "The functional configuration file has been loaded but is corrupted. A new version will be downloaded from the InStation");
                }
            }
            //else do nothing. Use default values and wait for the update
        }
        else
        {
            Logger::log(LOG_LEVEL_FATAL,
                "Unable to continue. Invalid functional connection parameters");
            m_valid = false;
        }
    }
    else
    {
        if (m_pIniConfiguration->loadFromFile(IniConfiguration::LOAD_FILE_FROM_SYSTEM_CONFIGURATION_DIRECTORY))
        {
            if (m_pIniConfiguration->processConfigurationText())
            {
                _applyNewIniConfiguration(m_pIniConfiguration, true, false);
            }
            else
            {
                Logger::log(LOG_LEVEL_FATAL,
                    "Unable to continue. "
                    "The functional configuration file has been loaded but is corrupted");
                m_valid = false;
            }
        }
        else
        {
            Logger::log(LOG_LEVEL_FATAL,
                "Unable to continue. "
                "The local functional configuration file is missing and "
                "the functional configuration file cannot be downloaded because the core configuration URL is missing");
            m_valid = false;
        }
    }


    //Report that there was a parameter error. There is no point to do it for syntax because in this case the file is wrong totally
    if (!m_pIniConfiguration->isParameterErrorSet())
    {
        //Clear functional configuration parameter fault if set
        Fault& configurationParameterFault = m_pDataContainer->getFunctionalConfigurationParameterValueFault();
        if (configurationParameterFault.get())
        {
            configurationParameterFault.clear();
        }
        //else do nothing
    }
    else
    {
        //Set functional configuration parameter fault if set
        Fault& configurationParameterFault = m_pDataContainer->getFunctionalConfigurationParameterValueFault();
        if (!configurationParameterFault.get())
        {
            configurationParameterFault.set();
        }
        //else do nothing
    }
}

Model::~Model()
{
    m_pDataContainer->removeAllObservers();

    m_pDataContainer->removeAllObservers();

    _stopBlueToothDeviceDiscovery();
    _stopAndDestroyAllInstationClientClasses();
    _stopAndDestroyInstationRetrieveConfigurationClasses();
    _stopGSMModemMonitor();

    m_pQueueDetector->removeAllObservers();
    m_pDataContainer->removeAllObservers();

    if (m_pGeneralWorkerThread != 0)
        m_pGeneralWorkerThread->stop();
    m_pGeneralWorkerThread.reset();

    m_pQueueDetector.reset();
    m_pDataContainer.reset();
}

bool Model::construct(CoreConfiguration& coreConfiguration)
{
    if (m_instancePtr == 0)
    {
        m_instancePtr = new Model(coreConfiguration);
    }
    //else do nothing

    return m_valid;
}

void Model::destruct()
{
    if (m_instancePtr != 0)
    {
        delete m_instancePtr;
        m_instancePtr = 0;
    }
    else
    {
        // already destroyed, do nothing!
    }
}

bool Model::isValid()
{
    return m_valid;
}

void Model::applyNewIniConfiguration(const boost::shared_ptr<IniConfiguration> pIniConfiguration)
{
    getInstancePtr()->_applyNewIniConfiguration(pIniConfiguration, false, false);
}

void Model::_applyNewIniConfiguration(
    const boost::shared_ptr<IniConfiguration> pIniConfiguration,
    const bool isConstructing,
    const bool ignoreSomeActionsUntilFunctionalConfigurationIsDownloaded)
{
    pIniConfiguration->overwriteValuesWithCommandLineParameters(
        m_coreConfiguration.getCommandLineStatusReportURL());

    IniConfiguration& iniConfiguration = *pIniConfiguration;
    if (!isConstructing)
    {
        const bool configurationIpAddressesHaveChanged = _verifyIfIpAddressesChanged(iniConfiguration);
        const bool modeHasChanged = _verifyIfModeChanged(iniConfiguration);
        if (configurationIpAddressesHaveChanged || modeHasChanged)
        {
            //object layout may change so this scenario will require program restart
            Logger::log(LOG_LEVEL_NOTICE, "Some major configuration addresses changed and program restart is required");

#ifdef __linux
            processReceivedSignal(SIGQUIT, ePROGRAM_RESTART_REQUIRED);
#else
            processReceivedSignal(SIGTERM, ePROGRAM_RESTART_REQUIRED);
#endif

            return;
        }
        else
        {
            //some minor parameters may have changed what does not require reinstatiation of object
            //Continue...
        }
    }
    //else do nothing

    _applyInquiryDeviceParameters(iniConfiguration);
    _applyQueueDetectionAlgorithmParameters(iniConfiguration);
    _applyPeriodicReportParameters(iniConfiguration);
    if (!ignoreSomeActionsUntilFunctionalConfigurationIsDownloaded)
    {
        _applyDeviceMode(iniConfiguration);
    }
    //else do nothing
    _applyGSMModemSignalLevelParameters(iniConfiguration);

    //Update configuration for HTTP classes
    if (m_pRetrieveConfigurationClient != 0)
    {
        m_pRetrieveConfigurationClient->setup(pIniConfiguration);
    }
    //else do nothing

    if (m_pCongestionReportingClient != 0)
    {
        m_pCongestionReportingClient->setup(pIniConfiguration);
    }
    //else do nothing

    if (m_pRawDeviceDetectionClient != 0)
    {
        m_pRawDeviceDetectionClient->setup(pIniConfiguration);
    }
    //else do nothing

    if (m_pAlertAndStatusReportingClient != 0)
    {
        m_pAlertAndStatusReportingClient->setup(pIniConfiguration);
    }
    //else do nothing

    if (m_pStatusReportingClient != 0)
    {
        m_pStatusReportingClient->setup(pIniConfiguration);
    }
    //else do nothing

    if (m_pFaultReportingClient != 0)
    {
        m_pFaultReportingClient->setup(pIniConfiguration);
    }
    //else do nothing

    if (m_pStatisticsReportingClient != 0)
    {
        m_pStatisticsReportingClient->setup(pIniConfiguration);
    }
    //else do nothing


    //Check http timeout value
    int64_t connectionTimeout = 0;
    bool found = false;

    found = m_pIniConfiguration->getValueInt64(eHTTP_CONNECTION_TIMEOUT_IN_SECONDS, connectionTimeout);
    if (!found)
    {
        Logger::log(
            LOG_LEVEL_EXCEPTION,
            MODULE_NAME,
            "setup",
            "eHTTP_CONNECTION_TIMEOUT_IN_SECONDS entry not found in configuration");
    }
    //else do nothing

    const unsigned int DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS = 300;
    if (connectionTimeout <= 0)
    {
        connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS; //default value
    }
    //else do nothing

    if (m_pRetrieveConfigurationActiveTCPClient)
    {
        m_pRetrieveConfigurationActiveTCPClient->setConnectingTimeout(connectionTimeout);
    }
    //else do nothing

    if (m_pCongestionReportingActiveTCPClient)
    {
        m_pCongestionReportingActiveTCPClient->setConnectingTimeout(connectionTimeout);
    }
    //else do nothing

    if (m_pRawDeviceDetectionActiveTCPClient)
    {
        m_pRawDeviceDetectionActiveTCPClient->setConnectingTimeout(connectionTimeout);
    }
    //else do nothing

    if (m_pAlertAndStatusReportingActiveTCPClient)
    {
        m_pAlertAndStatusReportingActiveTCPClient->setConnectingTimeout(connectionTimeout);
    }
    //else do nothing

    if (m_pStatusReportingActiveTCPClient)
    {
        m_pStatusReportingActiveTCPClient->setConnectingTimeout(connectionTimeout);
    }
    //else do nothing

    if (m_pFaultReportingActiveTCPClient)
    {
        m_pFaultReportingActiveTCPClient->setConnectingTimeout(connectionTimeout);
    }
    //else do nothing

    if (m_pStatisticsReportingActiveTCPClient)
    {
        m_pStatisticsReportingActiveTCPClient->setConnectingTimeout(connectionTimeout);
    }
    //else do nothing

    m_pIniConfiguration->operator=(iniConfiguration);
    m_pIniConfiguration->printAllValuesToTheLog();
}

bool Model::startBlueToothDeviceDiscovery()
{
    return getInstancePtr()->_startBlueToothDeviceDiscovery();
}

bool Model::_startBlueToothDeviceDiscovery()
{
    if (!m_pIniConfiguration->isValid())
    {
        return false;
    }
    //else continue

    //! Device Inquiry classes
    m_pDeviceDiscoverer = boost::shared_ptr<BlueTooth::DeviceDiscoverer>(
        new BlueTooth::DeviceDiscoverer(
            m_pDataContainer,
            (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
        ));

    m_pAcquireDevicesTask = boost::shared_ptr<BlueTooth::AcquireDevicesTask>(
        new BlueTooth::AcquireDevicesTask(
            m_pDeviceDiscoverer,
            &m_clock));

    m_pDeviceDiscoverer->setup(m_pQueueDetector, m_pInStationReporter);
    m_pDeviceDiscoverer->addObserver(View::View::getInstancePtr());

    m_pPeriodicallyProcessBackgroundDevicesTask = boost::shared_ptr<BlueTooth::PeriodicallyProcessBackgroundDevicesTask>(
        new BlueTooth::PeriodicallyProcessBackgroundDevicesTask(
            m_pDataContainer,
            &m_clock));

    m_pPeriodicallyRemoveNonPresentDevicesTask = boost::shared_ptr<BlueTooth::PeriodicallyRemoveNonPresentDevicesTask>(
        new BlueTooth::PeriodicallyRemoveNonPresentDevicesTask(
            m_pDataContainer,
            &m_clock));

    m_pAcquireDevicesTask->addObserver(View::View::getInstancePtr());


    m_pActiveAcquireDevicesTask = boost::shared_ptr<ActiveTask>(
        new ActiveTask(m_pAcquireDevicesTask));
    m_pActiveAcquireDevicesTask->setSleepTime(0);

    m_pActivePeriodicallyProcessBackgroundDevicesTask = boost::shared_ptr<ActiveTask>(
        new ActiveTask(m_pPeriodicallyProcessBackgroundDevicesTask));
    m_pActivePeriodicallyProcessBackgroundDevicesTask->setSleepTime(100);

    m_pActivePeriodicallyRemoveNonPresentDevicesTask = boost::shared_ptr<ActiveTask>(
        new ActiveTask(m_pPeriodicallyRemoveNonPresentDevicesTask));
    m_pActivePeriodicallyRemoveNonPresentDevicesTask->setSleepTime(100);

    _applyInquiryDeviceParameters(*m_pIniConfiguration);

    if (m_pActiveAcquireDevicesTask != 0)
    {
        m_pActiveAcquireDevicesTask->start();
    }
    //else do nothing

    if (m_pActivePeriodicallyProcessBackgroundDevicesTask != 0)
    {
        m_pActivePeriodicallyProcessBackgroundDevicesTask->start();
    }
    //else do nothing

    if (m_pActivePeriodicallyRemoveNonPresentDevicesTask != 0)
    {
        m_pActivePeriodicallyRemoveNonPresentDevicesTask->start();
    }
    //else do nothing

    return true;
}

void Model::stopBlueToothDeviceDiscovery()
{
    getInstancePtr()->_stopBlueToothDeviceDiscovery();
}

void Model::_stopBlueToothDeviceDiscovery()
{
    if (m_pDeviceDiscoverer != 0)
    {
        m_pDeviceDiscoverer->interruptInquireDevices();
        m_pDeviceDiscoverer->removeAllObservers();
    }
    //else do nothing


    //Signal all the threads to prepare for stop
    if (m_pActiveAcquireDevicesTask != 0)
    {
        m_pActiveAcquireDevicesTask->stop();
    }
    //else do nothing

    if (m_pAcquireDevicesTask != 0)
    {
        m_pAcquireDevicesTask->removeAllObservers();
    }
    //else do nothing


    if (m_pActivePeriodicallyProcessBackgroundDevicesTask != 0)
    {
        m_pActivePeriodicallyProcessBackgroundDevicesTask->stop();
    }
    //else do nothing

    if (m_pPeriodicallyProcessBackgroundDevicesTask != 0)
    {
        m_pPeriodicallyProcessBackgroundDevicesTask->removeAllObservers();
    }
    //else do nothing


    if (m_pActivePeriodicallyRemoveNonPresentDevicesTask != 0)
    {
        m_pActivePeriodicallyRemoveNonPresentDevicesTask->stop();
    }
    //else do nothing

    if (m_pPeriodicallyRemoveNonPresentDevicesTask != 0)
    {
        m_pPeriodicallyRemoveNonPresentDevicesTask->removeAllObservers();
    }
    //else do nothing


    if (m_pActivePeriodicallySendCongestionReportTask != 0)
    {
        m_pActivePeriodicallySendCongestionReportTask->stop();
    }
    //else do nothing

    if (m_pPeriodicallySendCongestionReportTask != 0)
    {
        m_pPeriodicallySendCongestionReportTask->removeAllObservers();
    }
    //else do nothing


    if (m_pActivePeriodicallySendStatisticsReportTask != 0)
    {
        m_pActivePeriodicallySendStatisticsReportTask->stop();
    }
    //else do nothing

    if (m_pPeriodicallySendStatisticsReportTask != 0)
    {
        m_pPeriodicallySendStatisticsReportTask->removeAllObservers();
    }
    //else do nothing


    if (m_pActivePeriodicallySendStatusReportTask != 0)
    {
        m_pActivePeriodicallySendStatusReportTask->stop();
    }
    //else do nothing

    if (m_pPeriodicallySendStatusReportTask != 0)
    {
        m_pPeriodicallySendStatusReportTask->removeAllObservers();
    }
    //else do nothing


    //Now start killing all active threads
    if (m_pActiveAcquireDevicesTask != 0)
    {
        m_pActiveAcquireDevicesTask->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    if (m_pActivePeriodicallySendStatisticsReportTask != 0)
    {
        m_pActivePeriodicallySendStatisticsReportTask->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    if (m_pActivePeriodicallyProcessBackgroundDevicesTask != 0)
    {
        m_pActivePeriodicallyProcessBackgroundDevicesTask->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    if (m_pActivePeriodicallyRemoveNonPresentDevicesTask != 0)
    {
        m_pActivePeriodicallyRemoveNonPresentDevicesTask->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    if (m_pActivePeriodicallySendCongestionReportTask != 0)
    {
        m_pActivePeriodicallySendCongestionReportTask->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    if (m_pActivePeriodicallySendStatusReportTask != 0)
    {
        m_pActivePeriodicallySendStatusReportTask->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    m_pActivePeriodicallyProcessBackgroundDevicesTask.reset();
    m_pActivePeriodicallyRemoveNonPresentDevicesTask.reset();
    m_pActiveAcquireDevicesTask.reset();
    m_pActivePeriodicallySendCongestionReportTask.reset();
    m_pActivePeriodicallySendStatisticsReportTask.reset();
    m_pActivePeriodicallySendStatusReportTask.reset();
    m_pAcquireDevicesTask.reset();
    m_pPeriodicallySendCongestionReportTask.reset();
    m_pPeriodicallySendStatisticsReportTask.reset();
    m_pPeriodicallySendStatusReportTask.reset();
    m_pPeriodicallyProcessBackgroundDevicesTask.reset();
    m_pPeriodicallyRemoveNonPresentDevicesTask.reset();
    m_pDeviceDiscoverer.reset();
}

bool Model::startAllInstationClients()
{
    return getInstancePtr()->_constructAndStartAllInstationClientClasses();
}

bool Model::_constructAndStartAllInstationClientClasses()
{
    _createAndStartStatusReportClasses();

    if (m_pIniConfiguration->isValid())
        _createAndStartInstationReportingClasses();

    _constructInStationReporter();

    return true;
}

void Model::_createAndStartInstationRetrieveConfigurationClasses()
{
    //Start retrieve configuration classes only if the configuration URL is not empty
    if (m_coreConfiguration.getConfigurationURL().empty())
    {
        return;
    }
    //else do nothing

    m_pRetrieveConfigurationWorkerThread = boost::shared_ptr<ActiveBoostAsio>(
        new ActiveBoostAsio(boost::lexical_cast<std::string>(
            static_cast<int>(RETRIEVE_CONFIGURATION_CLIENT_IDENTIFIER)).c_str()));

    m_pRetrieveConfigurationActiveTCPClient = boost::shared_ptr<ActiveBoostAsioTCPClient>(
        new ActiveBoostAsioTCPClient(
            RETRIEVE_CONFIGURATION_CLIENT_IDENTIFIER,
            m_pRetrieveConfigurationWorkerThread->getIoService()));

    m_pRetrieveConfigurationClient = boost::shared_ptr<InStation::InStationHTTPClient>(
        new InStation::InStationHTTPClient(
            m_coreConfiguration,
            m_pSeedConfiguration.get(),
            m_pRetrieveConfigurationActiveTCPClient.get(),
            m_pSignatureGenerator.get(),
            &m_clock,
            RETRIEVE_CONFIGURATION_CLIENT_IDENTIFIER,
            &m_pDataContainer->getRetrieveConfigurationClientCommunicationFault(),
            &m_pDataContainer->getRetrieveConfigurationClientResponseNotOkFault(),
            &m_pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault()));

    m_pActiveRetrieveConfigurationClientTask = boost::shared_ptr<ActiveTask>(
        new ActiveTask(m_pRetrieveConfigurationClient));
    m_pActiveRetrieveConfigurationClientTask->setSleepTime(50);


    m_pRetrieveConfigurationClient->addObserver(View::View::getInstancePtr());
    m_pRetrieveConfigurationClient->setup(m_pIniConfiguration);
    m_pRetrieveConfigurationClient->setup(m_pDataContainer);
    m_pRetrieveConfigurationActiveTCPClient->setup(&*m_pRetrieveConfigurationClient);
    m_pRetrieveConfigurationActiveTCPClient->setNumberOfRetries(0);
    m_pRetrieveConfigurationActiveTCPClient->addObserver(View::View::getInstancePtr());
    m_pRetrieveConfigurationActiveTCPClient->notifyObservers();

    m_pRetrieveConfigurationTask = boost::shared_ptr<InStation::RetrieveConfigurationTask>(
        new InStation::RetrieveConfigurationTask(
            m_pRetrieveConfigurationClient,
            &m_clock));
    m_pActiveRetrieveConfigurationTask = boost::shared_ptr<ActiveTask>(
        new ActiveTask(m_pRetrieveConfigurationTask));
    m_pRetrieveConfigurationClient->addObserver(&*m_pRetrieveConfigurationTask);

    _applyDeviceMode(*m_pIniConfiguration);

    //Start the worker thread
    m_pRetrieveConfigurationWorkerThread->start();

    //Start the communication with the InStation thread
    m_pActiveRetrieveConfigurationClientTask->start();

    //Do not connect. Connect only when there is something to send
    m_pRetrieveConfigurationActiveTCPClient->setupConnection(
        m_retrieveConfigurationRemoteAddress.c_str(),
        m_retrieveConfigurationRemotePortNumber,
        m_retrieveConfigurationLocalAddress.c_str());
    m_pRetrieveConfigurationActiveTCPClient->start();

    m_pActiveRetrieveConfigurationTask->start();

    if ((m_pActiveInStationReverseSSHConnectorTask != 0) && (!m_pActiveInStationReverseSSHConnectorTask->isRunning()))
        m_pActiveInStationReverseSSHConnectorTask->start();
}

void Model::_stopAndDestroyInstationRetrieveConfigurationClasses()
{
    _stopAndDestroyInstationClient(
        RETRIEVE_CONFIGURATION_CLIENT_IDENTIFIER,
        m_pRetrieveConfigurationWorkerThread,
        m_pRetrieveConfigurationActiveTCPClient,
        m_pRetrieveConfigurationClient,
        m_pActiveRetrieveConfigurationClientTask);
}

void Model::stopAllInstationClients()
{
    getInstancePtr()->_stopAndDestroyAllInstationClientClasses();
}

void Model::_stopAndDestroyAllInstationClientClasses()
{
    if ((m_pActiveInStationReverseSSHConnectorTask != 0) && (m_pActiveInStationReverseSSHConnectorTask->isRunning()))
    {
        m_pActiveInStationReverseSSHConnectorTask->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    if (m_pActiveRetrieveConfigurationTask != 0)
    {
        m_pActiveRetrieveConfigurationTask->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    //_stopAndDestroyInstationRetrieveConfigurationClasses();

    _destructInStationReporter();
    _stopAndDestroyInstationReportingClasses();

    m_pActiveRetrieveConfigurationTask.reset();
    m_pRetrieveConfigurationTask.reset();
    m_pInStationReverseSSHConnector.reset();
    m_pActiveInStationReverseSSHConnectorTask.reset();
}

void Model::_stopAndDestroyInstationReportingClasses()
{
    //Stop and destroy all reporting classes
    _stopAndDestroyInstationClient(
        CONGESTION_REPORTING_CLIENT_IDENTIFIER,
        m_pCongestionReportingWorkerThread,
        m_pCongestionReportingActiveTCPClient,
        m_pCongestionReportingClient,
        m_pActiveCongestionReportingClientTask);
    _stopAndDestroyInstationClient(
        RAW_DEVICE_DETECTION_CLIENT_IDENTIFIER,
        m_pRawDeviceDetectionWorkerThread,
        m_pRawDeviceDetectionActiveTCPClient,
        m_pRawDeviceDetectionClient,
        m_pActiveRawDeviceDetectionClientTask);
    _stopAndDestroyInstationClient(
        ALERT_AND_STATUS_REPORTING_CLIENT_IDENTIFIER,
        m_pAlertAndStatusReportingWorkerThread,
        m_pAlertAndStatusReportingActiveTCPClient,
        m_pAlertAndStatusReportingClient,
        m_pActiveAlertAndStatusReportingClientTask);
    _stopAndDestroyInstationClient(
        STATUS_REPORTING_CLIENT_IDENTIFIER,
        m_pStatusReportingWorkerThread,
        m_pStatusReportingActiveTCPClient,
        m_pStatusReportingClient,
        m_pActiveStatusReportingClientTask);
    _stopAndDestroyInstationClient(
        FAULT_REPORTING_CLIENT_IDENTIFIER,
        m_pFaultReportingWorkerThread,
        m_pFaultReportingActiveTCPClient,
        m_pFaultReportingClient,
        m_pActiveFaultReportingClientTask);
    _stopAndDestroyInstationClient(
        STATISTICS_REPORTING_CLIENT_IDENTIFIER,
        m_pStatisticsReportingWorkerThread,
        m_pStatisticsReportingActiveTCPClient,
        m_pStatisticsReportingClient,
        m_pActiveStatisticsReportingClientTask);
}

bool Model::setRetrieveConfigurationConnectionParameters()
{
    return getInstancePtr()->_setRetrieveConfigurationConnectionParameters();
}

bool Model::_setRetrieveConfigurationConnectionParameters()
{
    m_retrieveConfigurationConnectionParametersAreValid = false;

    //Verify host part
    std::string remoteAddress(OS_Utilities::StringToAnsi(m_coreConfiguration.getConfigurationURL()));
    Uri remoteUri = Uri::parse(remoteAddress);
    bool remoteAddressOk = (!remoteUri.Host.empty());
    remoteAddressOk = remoteAddressOk && ActiveBoostAsioTCPClient::verifyAddress(remoteUri.Host.c_str());
    if (!remoteAddressOk)
    {
        Logger::log(LOG_LEVEL_DEBUG1, "Invalid host part of retrieve configuration address", remoteUri.Host.c_str());
    }
    //else do nothing

    //Verify port part
    bool remotePortOk = true;
    int portNumber = 80;
    if (!remoteUri.Port.empty())
    {
        remotePortOk = remotePortOk && Utils::stringToInt(remoteUri.Port, portNumber);
        remotePortOk = remotePortOk && (portNumber>0) && (portNumber<=65535);
    }
    //else do nothing

    if (!remotePortOk)
    {
        Logger::log(LOG_LEVEL_DEBUG1, "Invalid port of retrieve configuration address", remoteUri.Port.c_str());
    }
    //else do nothing

    //Do not setup local ip address. Pass empty value


    //Do something if Ok
    if (remoteAddressOk && remotePortOk)
    {
        m_retrieveConfigurationRemoteAddress = remoteUri.Host;
        m_retrieveConfigurationRemotePortNumber = static_cast<uint16_t>(portNumber);

        m_retrieveConfigurationConnectionParametersAreValid = true;

        if (m_pRetrieveConfigurationActiveTCPClient != 0)
        {
            m_pRetrieveConfigurationActiveTCPClient->setupConnection(
                m_retrieveConfigurationRemoteAddress.c_str(),
                m_retrieveConfigurationRemotePortNumber,
                m_retrieveConfigurationLocalAddress.c_str());
        }
        //else do nothing
    }
    //else do nothing

    return m_retrieveConfigurationConnectionParametersAreValid;
}


void Model::_constructAndStartInstationClient(
    const int identifier,
    Fault* pPrimaryCommunicationFault,
    Fault* pPrimaryResponseNotOKFault,
    Fault* pPrimaryResponseMessageBodyErrorFault,
    const std::string& remoteAddress,
    const uint16_t remotePortNumber,
    const std::string& localAddress,
    boost::shared_ptr<ActiveBoostAsio>& pWorkerThread,
    boost::shared_ptr<ActiveBoostAsioTCPClient>& pActiveTCPClient,
    boost::shared_ptr<InStation::InStationHTTPClient>& pHttpClient,
    boost::shared_ptr<ActiveTask>& pActiveHttpClientTask)
{
    pWorkerThread = boost::shared_ptr<ActiveBoostAsio>(
        new ActiveBoostAsio(boost::lexical_cast<std::string>(identifier).c_str()));

    pActiveTCPClient = boost::shared_ptr<ActiveBoostAsioTCPClient>(
        new ActiveBoostAsioTCPClient(
            identifier,
            pWorkerThread->getIoService()));
    pActiveTCPClient->setNumberOfRetries(0);

    pHttpClient = boost::shared_ptr<InStation::InStationHTTPClient>(
        new InStation::InStationHTTPClient(
            m_coreConfiguration,
            m_pSeedConfiguration.get(),
            pActiveTCPClient.get(),
            m_pSignatureGenerator.get(),
            &m_clock,
            identifier,
            pPrimaryCommunicationFault,
            pPrimaryResponseNotOKFault,
            pPrimaryResponseMessageBodyErrorFault));

    pActiveHttpClientTask = boost::shared_ptr<ActiveTask>(
        new ActiveTask(pHttpClient));
    pActiveHttpClientTask->setSleepTime(50);

    pHttpClient->setup(m_pDataContainer);
    pHttpClient->setup(m_pIniConfiguration);
    pHttpClient->addObserver(View::View::getInstancePtr());
    pHttpClient->addObserver(&*m_pInStationReverseSSHConnector);
    pActiveTCPClient->setup(&*pHttpClient);
    pActiveTCPClient->addObserver(View::View::getInstancePtr());
    pActiveTCPClient->notifyObservers();

    //Start the worker thread
    if (pWorkerThread != 0)
    {
        pWorkerThread->start();
    }
    //else do nothing

    //Start the communication with the InStation thread
    if (pActiveHttpClientTask != 0)
    {
        pActiveHttpClientTask->start();
    }
    //else do nothing

    //Do not connect. Connect only when there is something to send
    if (pActiveTCPClient != 0)
    {
        pActiveTCPClient->setupConnection(
            remoteAddress.c_str(),
            remotePortNumber,
            localAddress.c_str());
        pActiveTCPClient->start();
    }
    //else do nothing
}

void Model::_constructInStationReporter()
{
    assert(m_pSeedConfiguration != 0);
    assert(m_pInStationReverseSSHConnector != 0);

    m_pInStationReporter = boost::shared_ptr<InStation::InStationReporter>(
        new InStation::InStationReporter(
            m_coreConfiguration,
            *m_pSeedConfiguration,
            *m_pInStationReverseSSHConnector,
            m_pSignalLevelProcessor,
            m_pDataContainer,
            m_pQueueDetector,
            m_pRetrieveConfigurationClient,
            m_pCongestionReportingClient,
            m_pRawDeviceDetectionClient,
            m_pAlertAndStatusReportingClient,
            m_pStatusReportingClient,
            m_pFaultReportingClient,
            m_pStatisticsReportingClient,
            &m_clock
            ));

    m_pInStationReporter->setup(m_pIniConfiguration);

    m_pGeneralWorkerThread = boost::shared_ptr<ActiveBoostAsio>(
        new ActiveBoostAsio(boost::lexical_cast<std::string>(
            static_cast<int>(GENERAL_IDENTIFIER)).c_str()));
    m_pGeneralWorkerThread->start();

    //Setup the timer so that FAULT REPORT will be sent after the startup delay
    int64_t initialStartupDelayInSeconds = 0;
    bool found = m_pIniConfiguration->getValueInt64(eINITIAL_STARTUP_DELAY, initialStartupDelayInSeconds);
    if (found && (initialStartupDelayInSeconds > 0))
    {
        boost::shared_ptr<boost::asio::deadline_timer> pStartupTimer(
            new boost::asio::deadline_timer( *m_pGeneralWorkerThread->getIoService() ));
        pStartupTimer->expires_from_now(boost::posix_time::milliseconds(initialStartupDelayInSeconds + 1));
        pStartupTimer->async_wait(
            boost::bind(&InStation::InStationReporter::reportFault, m_pInStationReporter));
    }
    //else do nothing

    //Bind other classes with InStationReporter
    if (m_pRetrieveConfigurationClient != 0)
    {
        m_pRetrieveConfigurationClient->addObserver(&*m_pInStationReporter);
        m_pRetrieveConfigurationClient->setup(m_pInStationReporter);
    }
    //else do nothing

    m_pQueueDetector->addObserver(m_pInStationReporter.get());

    if (m_pCongestionReportingClient != 0)
    {
        m_pCongestionReportingClient->addObserver(&*m_pInStationReporter);
        m_pCongestionReportingClient->setup(m_pInStationReporter);
    }
    //else do nothing

    if (m_pRawDeviceDetectionClient != 0)
    {
        m_pRawDeviceDetectionClient->addObserver(&*m_pInStationReporter);
        m_pRawDeviceDetectionClient->setup(m_pInStationReporter);
    }
    //else do nothing

    if (m_pAlertAndStatusReportingClient != 0)
    {
        m_pAlertAndStatusReportingClient->addObserver(&*m_pInStationReporter);
        m_pAlertAndStatusReportingClient->setup(m_pInStationReporter);
    }
    //else do nothing

    if (m_pStatusReportingClient != 0)
    {
        m_pStatusReportingClient->addObserver(&*m_pInStationReporter);
        m_pStatusReportingClient->setup(m_pInStationReporter);
    }
    //else do nothing

    if (m_pFaultReportingClient != 0)
    {
        m_pFaultReportingClient->addObserver(&*m_pInStationReporter);
        m_pFaultReportingClient->setup(m_pInStationReporter);
    }
    //else do nothing

    if (m_pStatisticsReportingClient != 0)
    {
        m_pStatisticsReportingClient->addObserver(&*m_pInStationReporter);
        m_pStatisticsReportingClient->setup(m_pInStationReporter);
    }
    //else do nothing

    if (m_pInStationReverseSSHConnector != 0)
    {
        m_pInStationReverseSSHConnector->addObserver(&*m_pInStationReporter);
    }
    //else do nothing

    if (m_pActiveGSMModemMonitor != 0)
    {
        m_pActiveGSMModemMonitor->addObserver(&*m_pInStationReporter);
    }
    //else do nothing

    if (m_coreConfiguration.getMajorCoreConfigurationVersion() >= 4)
    {
        //--- Reporting of Status to the InStation classes
        if (m_pStatusReportingClient != 0)
        {
            m_pPeriodicallySendStatusReportTask = boost::shared_ptr<InStation::PeriodicallySendStatusReportTask>(
                new InStation::PeriodicallySendStatusReportTask(
                    m_pInStationReporter,
                    &m_clock));
            m_pActivePeriodicallySendStatusReportTask = boost::shared_ptr<ActiveTask>(
                new ActiveTask(m_pPeriodicallySendStatusReportTask));
            m_pActivePeriodicallySendStatusReportTask->setSleepTime(100);
        }
        //else do nothing

        //--- Reporting of Devices to the InStation classes
        if (m_pStatisticsReportingClient != 0)
        {
            m_pPeriodicallySendStatisticsReportTask = boost::shared_ptr<InStation::PeriodicallySendStatisticsReportTask>(
                new InStation::PeriodicallySendStatisticsReportTask(
                    m_pInStationReporter,
                    &m_clock));
            m_pActivePeriodicallySendStatisticsReportTask = boost::shared_ptr<ActiveTask>(
                new ActiveTask(m_pPeriodicallySendStatisticsReportTask));
            m_pActivePeriodicallySendStatisticsReportTask->setSleepTime(100);
        }
        //else do nothing

        //--- Reporting of Congestion to the InStation classes
        if (m_pCongestionReportingClient != 0)
        {
            m_pPeriodicallySendCongestionReportTask = boost::shared_ptr<InStation::PeriodicallySendCongestionReportTask>(
                new InStation::PeriodicallySendCongestionReportTask(
                    m_pInStationReporter,
                    &m_clock));
            m_pActivePeriodicallySendCongestionReportTask = boost::shared_ptr<ActiveTask>(
                new ActiveTask(m_pPeriodicallySendCongestionReportTask));
            m_pActivePeriodicallySendCongestionReportTask->setSleepTime(100);
        }
        //else do nothing

        _applyPeriodicReportParameters(*m_pIniConfiguration);

        if (m_pActivePeriodicallySendStatusReportTask != 0)
        {
            m_pActivePeriodicallySendStatusReportTask->start();
        }
        //else do nothing

        if (m_pActivePeriodicallySendStatisticsReportTask != 0)
        {
            m_pActivePeriodicallySendStatisticsReportTask->start();
        }
        //else do nothing

        if (m_pActivePeriodicallySendCongestionReportTask != 0)
        {
            m_pActivePeriodicallySendCongestionReportTask->start();
        }
        //else do nothing
    }
    //else do nothing

    //If there were any faults that were reported on program startup - send them
    m_pInStationReporter->reportFault();
}

void Model::_destructInStationReporter()
{
    if (m_pInStationReporter == 0)
    {
        return;
    }
    //else continue

    m_pQueueDetector->removeObserver(&*m_pInStationReporter);
    m_pDataContainer->removeObserver(&*m_pInStationReporter);

    boost::shared_ptr<InStation::InStationReporter> nullInStationReporter;

    if (m_pRetrieveConfigurationClient != 0)
    {
        m_pRetrieveConfigurationClient->removeObserver(&*m_pInStationReporter);
        m_pRetrieveConfigurationClient->setup(nullInStationReporter);
    }
    //else do nothing

    if (m_pCongestionReportingClient != 0)
    {
        m_pCongestionReportingClient->removeObserver(&*m_pInStationReporter);
        m_pCongestionReportingClient->setup(nullInStationReporter);
    }
    //else do nothing

    if (m_pRawDeviceDetectionClient != 0)
    {
        m_pRawDeviceDetectionClient->removeObserver(&*m_pInStationReporter);
        m_pRawDeviceDetectionClient->setup(nullInStationReporter);
    }
    //else do nothing

    if (m_pAlertAndStatusReportingClient != 0)
    {
        m_pAlertAndStatusReportingClient->removeObserver(&*m_pInStationReporter);
        m_pAlertAndStatusReportingClient->setup(nullInStationReporter);
    }
    //else do nothing

    if (m_pStatusReportingClient != 0)
    {
        m_pStatusReportingClient->removeObserver(&*m_pInStationReporter);
        m_pStatusReportingClient->setup(nullInStationReporter);
    }
    //else do nothing

    if (m_pFaultReportingClient != 0)
    {
        m_pFaultReportingClient->removeObserver(&*m_pInStationReporter);
        m_pFaultReportingClient->setup(nullInStationReporter);
    }
    //else do nothing

    if (m_pStatisticsReportingClient != 0)
    {
        m_pStatisticsReportingClient->removeObserver(&*m_pInStationReporter);
        m_pStatisticsReportingClient->setup(nullInStationReporter);
    }
    //else do nothing

    if (m_pActiveGSMModemMonitor != 0)
    {
        m_pActiveGSMModemMonitor->removeObserver(&*m_pInStationReporter);
    }
    //else do nothing

    m_pInStationReporter.reset();
}

//void Model::stopInstationClient()
//{
//    getInstancePtr()->_stopInstationClient();
//}

void Model::_stopAndDestroyInstationClient(
    const int identifier,
    boost::shared_ptr<ActiveBoostAsio>& pWorkerThread,
    boost::shared_ptr<ActiveBoostAsioTCPClient>& pActiveTCPClient,
    boost::shared_ptr<InStation::InStationHTTPClient>& pHttpClient,
    boost::shared_ptr<ActiveTask>& pActiveHttpClientTask)
{
    if ((pActiveTCPClient != 0) && (pActiveTCPClient->getNumberOfIdentifiers() == 1))
    {
        pActiveTCPClient->stop();
        pActiveTCPClient->notifyObservers();
        pActiveTCPClient->removeAllObservers();
        pActiveTCPClient->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    if (pActiveHttpClientTask != 0)
    {
        pActiveHttpClientTask->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    if (pWorkerThread != 0)
    {
        pWorkerThread->stop();
    }
    //else do nothing

    if ((pHttpClient != 0) && (pHttpClient->getNumberOfIdentifiers() == 1))
    {
        pHttpClient->removeAllObservers();
    }
    //else do nothing

    if (pActiveTCPClient != 0)
    {
        pActiveTCPClient->removeIdentifier(identifier);
    }
    //else do nothing

    if (pHttpClient != 0)
    {
        pHttpClient->removeIdentifier(identifier);
    }
    //else do nothing

    pActiveHttpClientTask.reset();
    pHttpClient.reset();
    pActiveTCPClient.reset();
}

bool Model::startGSMModemMonitor()
{
    return getInstancePtr()->_startGSMModemMonitor();
}

bool Model::_startGSMModemMonitor()
{
    bool result = false;
    if (
        (m_coreConfiguration.getGSMModemType() != ICoreConfiguration::eGSM_MODEM_TYPE_NONE) &&
        (!m_coreConfiguration.getGSMModemConnectionAddress().empty()) &&
        (!m_coreConfiguration.getGSMModemConnectionLogin().empty()) &&
        (!m_coreConfiguration.getGSMModemConnectionPassword().empty())
        )
    {
        m_pSignalLevelProcessor = boost::shared_ptr<GSMModem::SignalLevelProcessor>(
            new GSMModem::SignalLevelProcessor());

        m_pActiveGSMModemMonitor = boost::shared_ptr<ActiveGSMModemMonitor>(
            new ActiveGSMModemMonitor(
                m_coreConfiguration,
                *m_pSignalLevelProcessor,
                &m_pDataContainer->getGSMModemUnableToConnectFault(),
                &m_clock
                ));

        m_pActiveGSMModemMonitor->setup(
            m_coreConfiguration.getGSMModemConnectionAddress(),
            m_coreConfiguration.getGSMModemConnectionLogin(),
            m_coreConfiguration.getGSMModemConnectionPassword()
        );

        _applyGSMModemSignalLevelParameters(*m_pIniConfiguration);

        m_pActiveGSMModemMonitor->start();

        //if (pMainFrame != 0)
        //{
        //    pMainFrame->setup(m_pActiveGSMModemMonitor);
        //}
        ////else do nothing

        result = true;
    }
    //else do nothing

    return result;
}

void Model::stopGSMModemMonitor()
{
    getInstancePtr()->_stopGSMModemMonitor();
}

void Model::_stopGSMModemMonitor()
{
    if (m_pActiveGSMModemMonitor != 0)
    {
        m_pActiveGSMModemMonitor->stop();
        m_pActiveGSMModemMonitor->notifyObservers();
        m_pActiveGSMModemMonitor->removeAllObservers();
        m_pActiveGSMModemMonitor->shutdownThread(MODULE_NAME);
    }
    //else do nothing

    m_pActiveGSMModemMonitor.reset();
    m_pSignalLevelProcessor.reset();
}

bool Model::_verifyIfIpAddressesChanged(const IniConfiguration& iniConfiguration)
{
    bool result = false;
    const bool isInLegacyMode = (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4);

    if ((m_modeNumber == 2) || (m_modeNumber == 3)) //'occupancy' (mode 2) or 'journey time & occupancy' (mode 3)
    {
        bool congestionReportingAddressOrPortChanged =
            _verifyIfIpAddressChanged(
                iniConfiguration,
                isInLegacyMode? eURL_CONGESTION_REPORTING : eURL_CONGESTION_REPORTS,
                m_pCongestionReportingActiveTCPClient);
        if (congestionReportingAddressOrPortChanged)
        {
            Logger::log(LOG_LEVEL_NOTICE, "Congestion Reporting TCP IP parameters have changed");
        }
        //else do nothing
        result = result || congestionReportingAddressOrPortChanged;
    }
    //else do nothing

    if (isInLegacyMode && ((m_modeNumber == 1) || (m_modeNumber == 3)))  //'journey time' (mode 1) or 'journey time & occupancy' (mode 3)
    {
        bool rawDeviceDetectionAddressOrPortChanged =
            _verifyIfIpAddressChanged(
                iniConfiguration,
                eURL_JOURNEY_TIMES_REPORTING,
                m_pRawDeviceDetectionActiveTCPClient);
        if (rawDeviceDetectionAddressOrPortChanged)
        {
            Logger::log(LOG_LEVEL_NOTICE, "Journey Times Reporting TCP IP parameters have changed");
        }
        //else do nothing
        result = result || rawDeviceDetectionAddressOrPortChanged;
    }
    //else do nothing

    if (isInLegacyMode)
    {
        bool alertAndStatusReportingAddressOrPortChanged =
            _verifyIfIpAddressChanged(
                iniConfiguration,
                eURL_ALERT_AND_STATUS_REPORTS,
                m_pAlertAndStatusReportingActiveTCPClient);
        if (alertAndStatusReportingAddressOrPortChanged)
        {
            Logger::log(LOG_LEVEL_NOTICE, "Alert and Status Reporting TCP IP parameters have changed");
        }
        //else do nothing
        result = result || alertAndStatusReportingAddressOrPortChanged;
    }
    //else do nothing

    if (!isInLegacyMode)
    {
        bool statusReportingAddressOrPortChanged =
            _verifyIfIpAddressChanged(
                iniConfiguration,
                eURL_STATUS_REPORTS,
                m_pStatusReportingActiveTCPClient);
        if (statusReportingAddressOrPortChanged)
        {
            Logger::log(LOG_LEVEL_NOTICE, "Status Reporting TCP IP parameters have changed");
        }
        //else do nothing
        result = result || statusReportingAddressOrPortChanged;
    }
    //else do nothing

    if (!isInLegacyMode)
    {
        bool faultReportingAddressOrPortChanged =
            _verifyIfIpAddressChanged(
                iniConfiguration,
                eURL_FAULT_REPORTS,
                m_pFaultReportingActiveTCPClient);
        if (faultReportingAddressOrPortChanged)
        {
            Logger::log(LOG_LEVEL_NOTICE, "Fault Reporting TCP IP parameters have changed");
        }
        //else do nothing
        result = result || faultReportingAddressOrPortChanged;
    }
    //else do nothing

    if (!isInLegacyMode)
    {
        bool statisticsReportingAddressOrPortChanged =
            _verifyIfIpAddressChanged(
                iniConfiguration,
                eURL_STATISTICS_REPORTS,
                m_pStatisticsReportingActiveTCPClient);
        if (statisticsReportingAddressOrPortChanged)
        {
            Logger::log(LOG_LEVEL_NOTICE, "Statistics Reporting TCP IP parameters have changed");
        }
        //else do nothing
        result = result || statisticsReportingAddressOrPortChanged;
    }
    //else do nothing

    return result;
}

bool Model::_verifyIfIpAddressChanged(
    const IniConfiguration& iniConfiguration,
    const int typeId,
    const boost::shared_ptr<ActiveBoostAsioTCPClient>& pTcpClient)
{
    bool result = false;

    std::string urlString;
    iniConfiguration.getValueString(static_cast<EValueTypeId>(typeId), urlString);
    Uri parsedUrlString = Uri::parse(urlString);

    if ((pTcpClient == 0) && !parsedUrlString.Host.empty())
    {
        std::ostringstream ss;
        ss << "Comparing currently used TCP IP address and the new one from received configuration. ";
        ss << "Current addres = UNDEFINED, new address = " << urlString;
        Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());

        return true;
    }
    else if ((pTcpClient != 0) && parsedUrlString.Host.empty())
    {
        std::ostringstream ss;
        ss << "Current addres = " << pTcpClient->getRemoteAddress() << ":" << pTcpClient->getRemotePortNumber() << ", new address = EMPTY";
        Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());

        return true;
    }
    else if (pTcpClient != 0) //implicit: && !parsedUrlString.Host.empty()
    {
        if (pTcpClient->getRemoteAddress() != parsedUrlString.Host)
        {
            std::ostringstream ss;
            ss << "Current addres = " << pTcpClient->getRemoteAddress() << ", new address = " << parsedUrlString.Host;
            Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());

            return true;
        }
        else //The host part has not changed, check if port has changed
        {
            int portNumber = 80;
            if (!parsedUrlString.Port.empty())
            {
                Utils::stringToInt(parsedUrlString.Port, portNumber);
            }
            //else do nothing

            if (static_cast<int>(pTcpClient->getRemotePortNumber()) != portNumber)
            {
                std::ostringstream ss;
                ss << "Current port number = " << pTcpClient->getRemotePortNumber() << ", new port number = " << portNumber;
                Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());

                return true;
            }
            //else do nothing - the same
        }
    }
    else
    {
        assert(pTcpClient == 0);
        assert(parsedUrlString.Host.empty());
        //The same and void
    }

    return result;
}

void Model::_applyQueueDetectionAlgorithmParameters(const IniConfiguration& iniConfiguration)
{
    assert(m_pQueueDetector != 0);

    bool found = true;

    //Verify parameters related to queue detection algorithm
    int64_t inquiryDurationInSeconds = 0;
    int64_t freeFlowBinThresholdInSeconds = 0;
    int64_t moderateFlowBinThresholdInSeconds = 0;
    int64_t slowFlowBinThresholdInSeconds = 0;
    int64_t verySlowFlowBinThresholdInSeconds = 0;
    int64_t absenceThresholdInSeconds = 0;
    std::string queueAlertThresholdBinString;
    int64_t queueDetectionThresholdNumber = 0;
    int64_t queueClearanceThresholdNumber = 0;
    int64_t queueDetectionStartupIntervalInSeconds = 0;

    //For version 3- values are expressed in scan cycles,
    //for version 4+ values are expressed in seconds
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
    {
        found = found && m_pIniConfiguration->getValueInt64(eINQUIRY_CYCLE_PERIOD, inquiryDurationInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eFREE_FLOW_SPEED_CYCLES_THRESHOLD, freeFlowBinThresholdInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eMODERATE_SPEED_CYCLES_THRESHOLD, moderateFlowBinThresholdInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eSLOW_SPEED_CYCLES_THRESHOLD, slowFlowBinThresholdInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eVERY_SLOW_SPEED_CYCLES_THRESHOLD, verySlowFlowBinThresholdInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eBIN_MAC_BIN_DROP_OUT_SCAN_CYCLE, absenceThresholdInSeconds);
        freeFlowBinThresholdInSeconds *= inquiryDurationInSeconds;
        moderateFlowBinThresholdInSeconds *= inquiryDurationInSeconds;
        slowFlowBinThresholdInSeconds *= inquiryDurationInSeconds;
        verySlowFlowBinThresholdInSeconds *= inquiryDurationInSeconds;
        absenceThresholdInSeconds *= inquiryDurationInSeconds;
        found = found && m_pIniConfiguration->getValueInt64(eQUEUE_DETECT_THRESHOLD, queueDetectionThresholdNumber);
        found = found && m_pIniConfiguration->getValueInt64(eQUEUE_CLEARANCE_THRESHOLD_DETECTION_NUMBER, queueClearanceThresholdNumber);
    }
    else
    {
        found = found && m_pIniConfiguration->getValueInt64(eINQUIRY_CYCLE_DURATION_IN_SECONDS, inquiryDurationInSeconds);
        if (inquiryDurationInSeconds > 0)
            inquiryDurationInSeconds = 1; //which means - do not perform scanning
        found = found && m_pIniConfiguration->getValueInt64(eFREE_FLOW_BIN_THRESHOLD_IN_SECONDS, freeFlowBinThresholdInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eMODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, moderateFlowBinThresholdInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eSLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, slowFlowBinThresholdInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eVERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, verySlowFlowBinThresholdInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eABSENCE_THRESHOLD_IN_SECONDS, absenceThresholdInSeconds);
        found = found && m_pIniConfiguration->getValueInt64(eQUEUE_DETECT_THRESHOLD, queueDetectionThresholdNumber);
        found = found && m_pIniConfiguration->getValueInt64(eQUEUE_CLEARANCE_THRESHOLD, queueClearanceThresholdNumber);
        found = found && m_pIniConfiguration->getValueInt64(eQUEUE_DETECTION_STARTUP_INTERVAL_IN_SECONDS, queueDetectionStartupIntervalInSeconds);
    }
    found = found && m_pIniConfiguration->getValueString(eQUEUE_ALERT_THRESHOLD_BIN, queueAlertThresholdBinString);


    int64_t newInquiryDurationInSeconds = 0;
    int64_t newFreeFlowBinThresholdInSeconds = 0;
    int64_t newModerateFlowBinThresholdInSeconds = 0;
    int64_t newSlowFlowBinThresholdInSeconds = 0;
    int64_t newVerySlowFlowBinThresholdInSeconds = 0;
    int64_t newAbsenceThresholdInSeconds = 0;
    std::string newQueueAlertThresholdBinString;
    int64_t newQueueDetectionThresholdNumber = 0;
    int64_t newQueueClearanceThresholdNumber = 0;
    int64_t newQueueDetectionStartupIntervalInSeconds = 0;
    found = true;

    //For version 3- values are expressed in scan cycles,
    //for version 4+ values are expressed in seconds
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
    {
        found = found && iniConfiguration.getValueInt64(eINQUIRY_CYCLE_PERIOD, newInquiryDurationInSeconds);
        found = found && iniConfiguration.getValueInt64(eFREE_FLOW_SPEED_CYCLES_THRESHOLD, newFreeFlowBinThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eMODERATE_SPEED_CYCLES_THRESHOLD, newModerateFlowBinThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eSLOW_SPEED_CYCLES_THRESHOLD, newSlowFlowBinThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eVERY_SLOW_SPEED_CYCLES_THRESHOLD, newVerySlowFlowBinThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eBIN_MAC_BIN_DROP_OUT_SCAN_CYCLE, newAbsenceThresholdInSeconds);
        newFreeFlowBinThresholdInSeconds *= newInquiryDurationInSeconds;
        newModerateFlowBinThresholdInSeconds *= newInquiryDurationInSeconds;
        newSlowFlowBinThresholdInSeconds *= newInquiryDurationInSeconds;
        newVerySlowFlowBinThresholdInSeconds *= newInquiryDurationInSeconds;
        newAbsenceThresholdInSeconds *= newInquiryDurationInSeconds;
        found = found && iniConfiguration.getValueInt64(eQUEUE_DETECT_THRESHOLD, newQueueDetectionThresholdNumber);
        found = found && iniConfiguration.getValueInt64(eQUEUE_CLEARANCE_THRESHOLD_DETECTION_NUMBER, newQueueClearanceThresholdNumber);
    }
    else
    {
        found = found && iniConfiguration.getValueInt64(eINQUIRY_CYCLE_DURATION_IN_SECONDS, newInquiryDurationInSeconds);
        if (newInquiryDurationInSeconds > 0)
            newInquiryDurationInSeconds = 1; //which means - do not perform scanning
        found = found && iniConfiguration.getValueInt64(eFREE_FLOW_BIN_THRESHOLD_IN_SECONDS, newFreeFlowBinThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eMODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, newModerateFlowBinThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eSLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, newSlowFlowBinThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eVERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, newVerySlowFlowBinThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eABSENCE_THRESHOLD_IN_SECONDS, newAbsenceThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eQUEUE_DETECT_THRESHOLD, newQueueDetectionThresholdNumber);
        found = found && iniConfiguration.getValueInt64(eQUEUE_CLEARANCE_THRESHOLD, newQueueClearanceThresholdNumber);
        found = found && iniConfiguration.getValueInt64(eQUEUE_DETECTION_STARTUP_INTERVAL_IN_SECONDS, newQueueDetectionStartupIntervalInSeconds);
    }

    found = found && iniConfiguration.getValueString(eQUEUE_ALERT_THRESHOLD_BIN, newQueueAlertThresholdBinString);

    //Convert to uppercase
    transform(queueAlertThresholdBinString.begin(), queueAlertThresholdBinString.end(), queueAlertThresholdBinString.begin(), toupper);
    transform(newQueueAlertThresholdBinString.begin(), newQueueAlertThresholdBinString.end(), newQueueAlertThresholdBinString.begin(), toupper);

    if (!found && !m_pQueueDetector->isConfigured())
    {
        Logger::log(LOG_LEVEL_ERROR, "Some parameters in the functional configuration file"
            "were not found. Queue detector will be disabled");
    }
    else if (!found && m_pQueueDetector->isConfigured())
    {
        Logger::log(LOG_LEVEL_ERROR, "Some parameters in the functional configuration file"
            "were not found. New functional configuration will not be used");
    }
    else if (
        found &&
        (
            !m_pQueueDetector->isConfigured() ||
            (newInquiryDurationInSeconds != inquiryDurationInSeconds) ||
            (newFreeFlowBinThresholdInSeconds != freeFlowBinThresholdInSeconds) ||
            (newModerateFlowBinThresholdInSeconds != moderateFlowBinThresholdInSeconds) ||
            (newSlowFlowBinThresholdInSeconds != slowFlowBinThresholdInSeconds) ||
            (newVerySlowFlowBinThresholdInSeconds != verySlowFlowBinThresholdInSeconds) ||
            (newAbsenceThresholdInSeconds != absenceThresholdInSeconds) ||
            (newQueueAlertThresholdBinString != queueAlertThresholdBinString) ||
            (newQueueDetectionThresholdNumber != queueDetectionThresholdNumber) ||
            (newQueueClearanceThresholdNumber != queueClearanceThresholdNumber) ||
            (newQueueDetectionStartupIntervalInSeconds != queueDetectionStartupIntervalInSeconds)
        )
        )
    {
        EBinType queueAlertThresholdBin = eBIN_TYPE_FREE_FLOW;
        if (
            (newQueueAlertThresholdBinString == "FREEFLOW") ||
            (newQueueAlertThresholdBinString == "FREE")
            )
        {
            queueAlertThresholdBin = eBIN_TYPE_FREE_FLOW;
        }
        else if (
            (newQueueAlertThresholdBinString == "MODERATEFLOW") ||
            (newQueueAlertThresholdBinString == "MODERATE")
            )
        {
            queueAlertThresholdBin = eBIN_TYPE_MODERATE_FLOW;
        }
        else if (
            (newQueueAlertThresholdBinString == "SLOWFLOW") ||
            (newQueueAlertThresholdBinString == "SLOW")
            )
        {
            queueAlertThresholdBin = eBIN_TYPE_SLOW_FLOW;
        }
        else if (
            (newQueueAlertThresholdBinString == "VERYSLOWFLOW") ||
            (newQueueAlertThresholdBinString == "VERYSLOW")
            )
        {
            queueAlertThresholdBin = eBIN_TYPE_VERY_SLOW_FLOW;
        }
        else if (
            (newQueueAlertThresholdBinString == "STATICFLOW") ||
            (newQueueAlertThresholdBinString == "STATIC")
            )
        {
            queueAlertThresholdBin = eBIN_TYPE_STATIC_FLOW;
        }
        else
        {
            //Otherwise take as "free flow"
            std::ostringstream ss;
            ss << "Unrecognised queueAlertThresholdBin value (" << newQueueAlertThresholdBinString << ")."
                " Using default freeFlow value";
            Logger::log(LOG_LEVEL_WARNING, ss.str().c_str());
        }

        if (newInquiryDurationInSeconds > 0)
        {
            m_pQueueDetector->setup(
                static_cast<unsigned int>(newInquiryDurationInSeconds),
                static_cast<unsigned int>(newAbsenceThresholdInSeconds),
                static_cast<unsigned int>(newFreeFlowBinThresholdInSeconds),
                static_cast<unsigned int>(newModerateFlowBinThresholdInSeconds),
                static_cast<unsigned int>(newSlowFlowBinThresholdInSeconds),
                static_cast<unsigned int>(newVerySlowFlowBinThresholdInSeconds),
                queueAlertThresholdBin,
                static_cast<unsigned int>(newQueueDetectionThresholdNumber),
                static_cast<unsigned int>(newQueueClearanceThresholdNumber),
                static_cast<unsigned int>(newQueueDetectionStartupIntervalInSeconds)
                );
        }
        //else do not scan
    }
    //else do nothing
}

void Model::_applyPeriodicReportParameters(const IniConfiguration& iniConfiguration)
{
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() >= 4)
    {
        int64_t initialStartupDelayInSeconds = 0;

        //Update reporting period used by active report devices task
        int64_t statusReportingPeriod = 0;
        bool found = iniConfiguration.getValueInt64(eSTATUS_REPORTING_PERIOD_IN_SECONDS, statusReportingPeriod);
        found = found && iniConfiguration.getValueInt64(eINITIAL_STARTUP_DELAY, initialStartupDelayInSeconds);
        if (found)
        {
            if (m_pPeriodicallySendStatusReportTask != 0)
            {
                m_pPeriodicallySendStatusReportTask->start(
                    static_cast<unsigned int>(statusReportingPeriod),
                    static_cast<unsigned int>(initialStartupDelayInSeconds));
            }
            //else do nothing
        }
        else
        {
            if (m_pPeriodicallySendStatusReportTask != 0)
            {
                m_pPeriodicallySendStatusReportTask->start(0, 0); //stop sending requests
            }
            //else do nothing
        }

        //Update reporting period used by active report devices task
        int64_t statisticsReportingPeriod = 0;
        found = iniConfiguration.getValueInt64(eSTATISTICS_REPORTING_PERIOD_IN_SECONDS, statisticsReportingPeriod);
        found = found && iniConfiguration.getValueInt64(eINITIAL_STARTUP_DELAY, initialStartupDelayInSeconds);
        if (found)
        {
            if (m_pPeriodicallySendStatisticsReportTask != 0)
            {
                m_pPeriodicallySendStatisticsReportTask->start(
                    static_cast<unsigned int>(statisticsReportingPeriod),
                    static_cast<unsigned int>(initialStartupDelayInSeconds));
            }
            //else do nothing
        }
        else
        {
            if (m_pPeriodicallySendStatisticsReportTask != 0)
            {
                m_pPeriodicallySendStatisticsReportTask->start(0, 0); //stop sending requests
            }
            //else do nothing
        }

        //Update reporting period used by active report devices task
        int64_t congestionReportingPeriod = 0;
        found = iniConfiguration.getValueInt64(eCONGESTION_REPORTING_PERIOD_IN_SECONDS, congestionReportingPeriod);
        found = found && iniConfiguration.getValueInt64(eINITIAL_STARTUP_DELAY, initialStartupDelayInSeconds);
        if (found)
        {
            if (m_pPeriodicallySendCongestionReportTask != 0)
            {
                m_pPeriodicallySendCongestionReportTask->start(
                    static_cast<unsigned int>(congestionReportingPeriod),
                    static_cast<unsigned int>(initialStartupDelayInSeconds));
            }
            //else do nothing
        }
        else
        {
            if (m_pPeriodicallySendCongestionReportTask != 0)
            {
                m_pPeriodicallySendCongestionReportTask->start(0, 0); //stop sending requests
            }
            //else do nothing
        }
    }
    //else do nothing
}

void Model::_applyGSMModemSignalLevelParameters(const IniConfiguration& iniConfiguration)
{
    if (
        (m_pActiveGSMModemMonitor == 0) ||
        (m_pSignalLevelProcessor == 0)
        )
    {
        return;
    }
    //else do nothing

    int64_t gsmModemSignalLevelSamplingPeriodInSeconds = 0;
    bool ok = iniConfiguration.getValueInt64(
        eGSM_MODEM_SIGNAL_LEVEL_SAMPLING_PERIOD_IN_SECONDS, gsmModemSignalLevelSamplingPeriodInSeconds);

    int64_t gsmModemSignalLevelStatisticsWindowInSeconds = 0;
    ok = ok && iniConfiguration.getValueInt64(
        eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS, gsmModemSignalLevelStatisticsWindowInSeconds);

    if (ok)
    {
        m_pActiveGSMModemMonitor->setupSignalLevelSamplingPeriod(
            static_cast<unsigned int>(gsmModemSignalLevelSamplingPeriodInSeconds));

        if (gsmModemSignalLevelSamplingPeriodInSeconds > 0)
        {
            int gsmModemSignalLevelStatisticsWindowInSamples = gsmModemSignalLevelStatisticsWindowInSeconds /
                gsmModemSignalLevelSamplingPeriodInSeconds;
            if (gsmModemSignalLevelStatisticsWindowInSamples <= 0)
                gsmModemSignalLevelStatisticsWindowInSamples = 1;
            m_pSignalLevelProcessor->setup(static_cast<size_t>(gsmModemSignalLevelStatisticsWindowInSamples));
        }
        //else do nothing
    }
    else
    {
        Logger::log(LOG_LEVEL_ERROR, "Invalid settingsCollectionInterval. Using default value");
    }
}

bool Model::_verifyIfModeChanged(const IniConfiguration& iniConfiguration) const
{
    const bool isInLegacyMode = (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4);

    int64_t modeNumber = 0;
    iniConfiguration.getValueInt64(isInLegacyMode ? eOUTSTATION_MODE_LEGACY : eOUTSTATION_MODE, modeNumber);

    bool modeHasChanged = (modeNumber != m_modeNumber);
    if (modeHasChanged)
    {
        std::ostringstream ss;
        ss << "Comparing current program mode and the new one from received configuration. ";
        ss << "Current mode = " << m_modeNumber << ", new mode = " << modeNumber;
        Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
    }
    //else do nothing

    return modeHasChanged;
}

void Model::_applyDeviceMode(const IniConfiguration& iniConfiguration)
{
    const bool isInLegacyMode = (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4);
    iniConfiguration.getValueInt64(isInLegacyMode ? eOUTSTATION_MODE_LEGACY : eOUTSTATION_MODE, m_modeNumber);

    int64_t sendIntervalInSeconds = 0;
    int64_t initialStartupDelayInSeconds = 0;
    bool found = iniConfiguration.getValueInt64(
        (m_modeNumber > 0) ? eSETTINGS_COLLECTION_INTERVAL_1 : eSETTINGS_COLLECTION_INTERVAL_2,
        sendIntervalInSeconds);
    found = found && m_pIniConfiguration->getValueInt64(eINITIAL_STARTUP_DELAY, initialStartupDelayInSeconds);

    //Multiply by a number of seconds in a minute
    sendIntervalInSeconds *= 60;

    if (!found)
    {
        Logger::log(LOG_LEVEL_WARNING, "Invalid settingsCollectionInterval. Using default value");
    }
    //else do nothing

    if (m_pRetrieveConfigurationTask != 0)
        m_pRetrieveConfigurationTask->start(
            static_cast<long>(sendIntervalInSeconds),
            static_cast<long>(initialStartupDelayInSeconds));
}

void Model::_applyInquiryDeviceParameters(const IniConfiguration& iniConfiguration)
{
    if (m_pAcquireDevicesTask == 0)
        return;

    const bool isInLegacyMode = (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4);

    //Update inquiry cycle period used by active acquire devices task / device detector
    int64_t inquiryCyclePeriod = 0;
    bool found = iniConfiguration.getValueInt64(
        isInLegacyMode ? eINQUIRY_CYCLE_PERIOD : eINQUIRY_CYCLE_DURATION_IN_SECONDS,
        inquiryCyclePeriod);
    if (found && (inquiryCyclePeriod > 0))
    {
        m_pDataContainer->getLocalDeviceConfiguration().inquiryDurationInSeconds = inquiryCyclePeriod;
        m_pAcquireDevicesTask->start(
            static_cast<unsigned int>(inquiryCyclePeriod),
            255U);
    }
    else
    {
        m_pAcquireDevicesTask->start(0, 0); //stop sending requests
    }

    int64_t inquiryPower = 0;
    found = iniConfiguration.getValueInt64(eINQUIRY_POWER, inquiryPower);
    if (found)
    {
        m_pDataContainer->getLocalDeviceConfiguration().inquiryPower = inquiryPower;
    }
    //else do nothing

    //Update inquiry interval at which non-present devices will be removed
    //For version 3- values are expressed in scan cycles,
    //for version 4+ values are expressed in seconds
    int64_t absenceThresholdInSeconds = 0;
    int64_t backgroundStartTimeThresholdInSeconds = 0;
    int64_t backgroundEndTimeThresholdInSeconds = 0;
    if (isInLegacyMode)
    {
        int64_t inquiryDurationInSeconds = 0;
        found =          iniConfiguration.getValueInt64(eINQUIRY_CYCLE_PERIOD, inquiryDurationInSeconds);
        found = found && iniConfiguration.getValueInt64(eBIN_MAC_BIN_DROP_OUT_SCAN_CYCLE, absenceThresholdInSeconds);
        absenceThresholdInSeconds *= inquiryDurationInSeconds;
    }
    else
    {
        found = iniConfiguration.getValueInt64(eABSENCE_THRESHOLD_IN_SECONDS, absenceThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eBACKGROUND_START_TIME_THRESHOLD_IN_SECONDS, backgroundStartTimeThresholdInSeconds);
        found = found && iniConfiguration.getValueInt64(eBACKGROUND_END_TIME_THRESHOLD_IN_SECONDS, backgroundEndTimeThresholdInSeconds);
    }

    if (found)
    {
        if (m_pPeriodicallyRemoveNonPresentDevicesTask != 0)
        {
            m_pPeriodicallyRemoveNonPresentDevicesTask->start(
                10, //check every 10 seconds
                static_cast<unsigned int>(absenceThresholdInSeconds),
                true);
        }
        //else do nothing
    }
    else
    {
        if (m_pPeriodicallyRemoveNonPresentDevicesTask != 0)
        {
            //stop sending requests
            m_pPeriodicallyRemoveNonPresentDevicesTask->start(10, 0, false);
        }
        //else do nothing
    }

    if (found)
    {
        if (m_pPeriodicallyProcessBackgroundDevicesTask != 0)
        {
            m_pPeriodicallyProcessBackgroundDevicesTask->start(
                10, //check every 10 seconds
                static_cast<unsigned int>(backgroundStartTimeThresholdInSeconds),
                static_cast<unsigned int>(backgroundEndTimeThresholdInSeconds));
        }
        //else do nothing
    }
    //else do nothing
}

bool Model::_applyInstationParameter(
    const IniConfiguration& iniConfiguration,
    const int typeId,
    std::string& remoteAddress,
    uint16_t& remotePort)
{
    bool result = false;

    //Verify host part
    std::string urlString;
    bool found = iniConfiguration.getValueString(static_cast<EValueTypeId>(typeId), urlString);
    if (!found)
    {
        std::ostringstream ss;
        ss << "Parameter typeId=" << typeId << " has not been found in the configuration file";
        Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());

        return false;
    }
    //else do nothing

    Uri parsedUrlString = Uri::parse(urlString);

    bool addressOk = (!parsedUrlString.Host.empty());
    addressOk = addressOk && ActiveBoostAsioTCPClient::verifyAddress(parsedUrlString.Host.c_str());
    if (!addressOk)
    {
        std::ostringstream ss;
        ss <<  "Invalid host part of address (typeId=" << typeId << ")";
        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

        return false;
    }
    //else do nothing

    //Verify port part
    bool portOk = true;
    int portNumber = 80;
    if (!parsedUrlString.Port.empty())
    {
        portOk = portOk && Utils::stringToInt(parsedUrlString.Port, portNumber);
        portOk = portOk && (portNumber>0) && (portNumber<=65535);
    }
    //else do nothing

    if (!portOk)
    {
        std::ostringstream ss;
        ss <<  "Invalid port part of address (typeId=" << typeId << ")";
        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

        return false;
    }
    //else do nothing

    //Do not setup local ip address. Pass empty value


    //Do something if Ok
    if (addressOk && portOk)
    {
        remoteAddress = parsedUrlString.Host;
        remotePort = static_cast<uint16_t>(portNumber);

        result = true;
    }
    //else do nothing

    return result;
}

void Model::_createAndStartStatusReportClasses()
{
    typedef std::pair< boost::shared_ptr<ActiveBoostAsioTCPClient>, boost::shared_ptr<InStation::InStationHTTPClient> > TClientPair;
    typedef std::vector< TClientPair > TActiveBoostAsioTCPClientCollection;
    TActiveBoostAsioTCPClientCollection tcpClientCollection;

    const bool isInLegacyMode = (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4);
    if (!isInLegacyMode)
    {
        //Evaluate if a new alarm and status reporting pair should be created or not
        m_statusReportingConnectionParametersAreValid = _applyInstationParameter(
            *m_pIniConfiguration,
            eURL_STATUS_REPORTS,
            m_statusReportingRemoteAddress,
            m_statusReportingRemotePortNumber);

        if (m_statusReportingConnectionParametersAreValid)
        {
            TActiveBoostAsioTCPClientCollection::iterator iter(tcpClientCollection.begin());
            TActiveBoostAsioTCPClientCollection::const_iterator iterEnd(tcpClientCollection.end());
            while (iter != iterEnd)
            {
                if (
                    (iter->first->getRemoteAddress() == m_statusReportingRemoteAddress) &&
                    (iter->first->getRemotePortNumber() == m_statusReportingRemotePortNumber)
                    )
                {
                    break;
                }
                //else continue

                ++iter;
            }

            if (iter == iterEnd) //not found
            {
                _constructAndStartInstationClient(
                    STATUS_REPORTING_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getStatusReportingClientCommunicationFault(),
                    &m_pDataContainer->getStatusReportingClientResponseNotOkFault(),
                    &m_pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault(),
                    m_statusReportingRemoteAddress,
                    m_statusReportingRemotePortNumber,
                    "", //local address
                    m_pStatusReportingWorkerThread,
                    m_pStatusReportingActiveTCPClient,
                    m_pStatusReportingClient,
                    m_pActiveStatusReportingClientTask);

                tcpClientCollection.push_back(TClientPair(m_pStatusReportingActiveTCPClient, m_pStatusReportingClient));
            }
            else
            {
                m_pStatusReportingActiveTCPClient = iter->first;
                m_pStatusReportingClient = iter->second;
                m_pStatusReportingActiveTCPClient->addIdentifier(STATUS_REPORTING_CLIENT_IDENTIFIER);
                m_pStatusReportingClient->addIdentifier(
                    STATUS_REPORTING_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getStatusReportingClientCommunicationFault(),
                    &m_pDataContainer->getStatusReportingClientResponseNotOkFault(),
                    &m_pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault()
                    );
            }
        }
        else
        {
            std::ostringstream ss;
            ss <<  "Status reporting URL is missing or invalid. Status reporting will be disabled";
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
    }
    //else do nothing
}

void Model::_createAndStartInstationReportingClasses()
{
    typedef std::pair< boost::shared_ptr<ActiveBoostAsioTCPClient>, boost::shared_ptr<InStation::InStationHTTPClient> > TClientPair;
    typedef std::vector< TClientPair > TActiveBoostAsioTCPClientCollection;
    TActiveBoostAsioTCPClientCollection tcpClientCollection;

    const bool isInLegacyMode = (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4);

    //Add retrieve configuration pair to the vector
    if (
        (m_pRetrieveConfigurationActiveTCPClient != 0) &&
        (m_pRetrieveConfigurationClient != 0)
        )
    {
        tcpClientCollection.push_back(std::make_pair(m_pRetrieveConfigurationActiveTCPClient, m_pRetrieveConfigurationClient));
    }
    //else do nothing

    if ((m_modeNumber == 2) || (m_modeNumber == 3)) //'occupancy' (mode 2) or 'journey time & occupancy' (mode 3)
    {
        m_congestionReportingConnectionParametersAreValid = _applyInstationParameter(
            *m_pIniConfiguration,
            isInLegacyMode ? eURL_CONGESTION_REPORTING : eURL_CONGESTION_REPORTS,
            m_congestionReportingRemoteAddress,
            m_congestionReportingRemotePortNumber);

        //Evaluate if a new congestion reporting pair should be created or not
        if (m_congestionReportingConnectionParametersAreValid)
        {
            TActiveBoostAsioTCPClientCollection::iterator iter(tcpClientCollection.begin());
            TActiveBoostAsioTCPClientCollection::const_iterator iterEnd(tcpClientCollection.end());
            while (iter != iterEnd)
            {
                if (
                    (iter->first->getRemoteAddress() == m_congestionReportingRemoteAddress) &&
                    (iter->first->getRemotePortNumber() == m_congestionReportingRemotePortNumber)
                    )
                {
                    break;
                }
                //else continue

                ++iter;
            }

            if (iter == iterEnd) //not found
            {
                _constructAndStartInstationClient(
                    CONGESTION_REPORTING_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getCongestionReportingClientCommunicationFault(),
                    &m_pDataContainer->getCongestionReportingClientResponseNotOkFault(),
                    &m_pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault(),
                    m_congestionReportingRemoteAddress,
                    m_congestionReportingRemotePortNumber,
                    "", //local address
                    m_pCongestionReportingWorkerThread,
                    m_pCongestionReportingActiveTCPClient,
                    m_pCongestionReportingClient,
                    m_pActiveCongestionReportingClientTask);

                tcpClientCollection.push_back(std::make_pair(m_pCongestionReportingActiveTCPClient, m_pCongestionReportingClient));
            }
            else
            {
                m_pCongestionReportingActiveTCPClient = iter->first;
                m_pCongestionReportingClient = iter->second;
                m_pCongestionReportingActiveTCPClient->addIdentifier(CONGESTION_REPORTING_CLIENT_IDENTIFIER);
                m_pCongestionReportingClient->addIdentifier(
                    CONGESTION_REPORTING_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getCongestionReportingClientCommunicationFault(),
                    &m_pDataContainer->getCongestionReportingClientResponseNotOkFault(),
                    &m_pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault());

                m_pCongestionReportingClient->setup(m_pIniConfiguration);
            }
        }
        else
        {
            std::ostringstream ss;
            ss <<  "Congestion reporting URL is missing or invalid. Congestion reporting will be disabled";
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
    }
    //do nothing. Not relevant mode

    if (
        ((m_modeNumber == 1) || (m_modeNumber == 3)) //'journey time' (mode 1) or 'journey time & occupancy' (mode 3)
        && isInLegacyMode //journey times reporting (aka Raw Device Detection report) is used only for legacy bluetruth
        )
    {
        //Evaluate if a new journey times reporting pair should be created or not
        m_rawDeviceDetectionConnectionParametersAreValid = _applyInstationParameter(
            *m_pIniConfiguration,
            eURL_JOURNEY_TIMES_REPORTING,
            m_rawDeviceDetectionRemoteAddress,
            m_rawDeviceDetectionRemotePortNumber);

        if (m_rawDeviceDetectionConnectionParametersAreValid)
        {
            TActiveBoostAsioTCPClientCollection::iterator iter(tcpClientCollection.begin());
            TActiveBoostAsioTCPClientCollection::const_iterator iterEnd(tcpClientCollection.end());
            while (iter != iterEnd)
            {
                if (
                    (iter->first->getRemoteAddress() == m_rawDeviceDetectionRemoteAddress) &&
                    (iter->first->getRemotePortNumber() == m_rawDeviceDetectionRemotePortNumber)
                    )
                {
                    break;
                }
                //else continue

                ++iter;
            }

            if (iter == iterEnd) //not found
            {
                _constructAndStartInstationClient(
                    RAW_DEVICE_DETECTION_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getRawDeviceDetectionClientCommunicationFault(),
                    &m_pDataContainer->getRawDeviceDetectionClientResponseNotOkFault(),
                    &m_pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault(),
                    m_rawDeviceDetectionRemoteAddress,
                    m_rawDeviceDetectionRemotePortNumber,
                    "", //local address
                    m_pRawDeviceDetectionWorkerThread,
                    m_pRawDeviceDetectionActiveTCPClient,
                    m_pRawDeviceDetectionClient,
                    m_pActiveRawDeviceDetectionClientTask);

                tcpClientCollection.push_back(std::make_pair(m_pRawDeviceDetectionActiveTCPClient, m_pRawDeviceDetectionClient));
            }
            else
            {
                m_pRawDeviceDetectionActiveTCPClient = iter->first;
                m_pRawDeviceDetectionClient = iter->second;
                m_pRawDeviceDetectionActiveTCPClient->addIdentifier(RAW_DEVICE_DETECTION_CLIENT_IDENTIFIER);
                m_pRawDeviceDetectionClient->addIdentifier(
                    RAW_DEVICE_DETECTION_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getRawDeviceDetectionClientCommunicationFault(),
                    &m_pDataContainer->getRawDeviceDetectionClientResponseNotOkFault(),
                    &m_pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault());
            }
        }
        else
        {
            std::ostringstream ss;
            ss <<  "Journey times reporting URL is missing or invalid. Journey time reporting will be disabled";
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
    }
    //else do nothing

    if (isInLegacyMode)
    {
        //Evaluate if a new alarm and status reporting pair should be created or not
        m_alertAndStatusReportingConnectionParametersAreValid = _applyInstationParameter(
            *m_pIniConfiguration,
            eURL_ALERT_AND_STATUS_REPORTS,
            m_alertAndStatusReportingRemoteAddress,
            m_alertAndStatusReportingRemotePortNumber);

        if (m_alertAndStatusReportingConnectionParametersAreValid)
        {
            TActiveBoostAsioTCPClientCollection::iterator iter(tcpClientCollection.begin());
            TActiveBoostAsioTCPClientCollection::const_iterator iterEnd(tcpClientCollection.end());
            while (iter != iterEnd)
            {
                if (
                    (iter->first->getRemoteAddress() == m_alertAndStatusReportingRemoteAddress) &&
                    (iter->first->getRemotePortNumber() == m_alertAndStatusReportingRemotePortNumber)
                    )
                {
                    break;
                }
                //else continue

                ++iter;
            }

            if (iter == iterEnd) //not found
            {
                _constructAndStartInstationClient(
                    ALERT_AND_STATUS_REPORTING_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getAlertAndStatusReportingClientCommunicationFault(),
                    &m_pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault(),
                    &m_pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault(),
                    m_alertAndStatusReportingRemoteAddress,
                    m_alertAndStatusReportingRemotePortNumber,
                    "", //local address
                    m_pAlertAndStatusReportingWorkerThread,
                    m_pAlertAndStatusReportingActiveTCPClient,
                    m_pAlertAndStatusReportingClient,
                    m_pActiveAlertAndStatusReportingClientTask);

                tcpClientCollection.push_back(TClientPair(m_pAlertAndStatusReportingActiveTCPClient, m_pAlertAndStatusReportingClient));
            }
            else
            {
                m_pAlertAndStatusReportingActiveTCPClient = iter->first;
                m_pAlertAndStatusReportingClient = iter->second;
                m_pAlertAndStatusReportingActiveTCPClient->addIdentifier(ALERT_AND_STATUS_REPORTING_CLIENT_IDENTIFIER);
                m_pAlertAndStatusReportingClient->addIdentifier(
                    ALERT_AND_STATUS_REPORTING_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getAlertAndStatusReportingClientCommunicationFault(),
                    &m_pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault(),
                    &m_pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault());
            }
        }
        else
        {
            std::ostringstream ss;
            ss <<  "Alert and status reporting URL is missing or invalid. Alert and status reporting will be disabled";
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
    }
    //else do nothing

    if (!isInLegacyMode)
    {
        //Evaluate if a new alarm and status reporting pair should be created or not
        m_faultReportingConnectionParametersAreValid = _applyInstationParameter(
            *m_pIniConfiguration,
            eURL_FAULT_REPORTS,
            m_faultReportingRemoteAddress,
            m_faultReportingRemotePortNumber);

        if (m_faultReportingConnectionParametersAreValid)
        {
            TActiveBoostAsioTCPClientCollection::iterator iter(tcpClientCollection.begin());
            TActiveBoostAsioTCPClientCollection::const_iterator iterEnd(tcpClientCollection.end());
            while (iter != iterEnd)
            {
                if (
                    (iter->first->getRemoteAddress() == m_faultReportingRemoteAddress) &&
                    (iter->first->getRemotePortNumber() == m_faultReportingRemotePortNumber)
                    )
                {
                    break;
                }
                //else continue

                ++iter;
            }

            if (iter == iterEnd) //not found
            {
                _constructAndStartInstationClient(
                    FAULT_REPORTING_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getFaultReportingClientCommunicationFault(),
                    &m_pDataContainer->getFaultReportingClientResponseNotOkFault(),
                    &m_pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault(),
                    m_faultReportingRemoteAddress,
                    m_faultReportingRemotePortNumber,
                    "", //local address
                    m_pFaultReportingWorkerThread,
                    m_pFaultReportingActiveTCPClient,
                    m_pFaultReportingClient,
                    m_pActiveFaultReportingClientTask);

                tcpClientCollection.push_back(TClientPair(m_pFaultReportingActiveTCPClient, m_pFaultReportingClient));
            }
            else
            {
                m_pFaultReportingActiveTCPClient = iter->first;
                m_pFaultReportingClient = iter->second;
                m_pFaultReportingActiveTCPClient->addIdentifier(FAULT_REPORTING_CLIENT_IDENTIFIER);
                m_pFaultReportingClient->addIdentifier(
                    FAULT_REPORTING_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getFaultReportingClientCommunicationFault(),
                    &m_pDataContainer->getFaultReportingClientResponseNotOkFault(),
                    &m_pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault());
            }
        }
        else
        {
            std::ostringstream ss;
            ss << "Fault reporting URL is missing or invalid. Fault reporting will be disabled";
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
    }
    //else do nothing

    if (!isInLegacyMode)
    {
        //Evaluate if a new alarm and status reporting pair should be created or not
        m_statisticsReportingConnectionParametersAreValid = _applyInstationParameter(
            *m_pIniConfiguration,
            eURL_STATISTICS_REPORTS,
            m_statisticsReportingRemoteAddress,
            m_statisticsReportingRemotePortNumber);

        if (m_statisticsReportingConnectionParametersAreValid)
        {
            TActiveBoostAsioTCPClientCollection::iterator iter(tcpClientCollection.begin());
            TActiveBoostAsioTCPClientCollection::const_iterator iterEnd(tcpClientCollection.end());
            while (iter != iterEnd)
            {
                if (
                    (iter->first->getRemoteAddress() == m_statisticsReportingRemoteAddress) &&
                    (iter->first->getRemotePortNumber() == m_statisticsReportingRemotePortNumber)
                    )
                {
                    break;
                }
                //else continue

                ++iter;
            }

            if (iter == iterEnd) //not found
            {
                _constructAndStartInstationClient(
                    STATISTICS_REPORTING_CLIENT_IDENTIFIER,
                     &m_pDataContainer->getStatisticsReportingClientCommunicationFault(),
                     &m_pDataContainer->getStatisticsReportingClientResponseNotOkFault(),
                     &m_pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault(),
                    m_statisticsReportingRemoteAddress,
                    m_statisticsReportingRemotePortNumber,
                    "", //local address
                    m_pStatisticsReportingWorkerThread,
                    m_pStatisticsReportingActiveTCPClient,
                    m_pStatisticsReportingClient,
                    m_pActiveStatisticsReportingClientTask);

                tcpClientCollection.push_back(TClientPair(m_pStatisticsReportingActiveTCPClient, m_pStatisticsReportingClient));
            }
            else
            {
                m_pStatisticsReportingActiveTCPClient = iter->first;
                m_pStatisticsReportingClient = iter->second;
                m_pStatisticsReportingActiveTCPClient->addIdentifier(STATISTICS_REPORTING_CLIENT_IDENTIFIER);
                m_pStatisticsReportingClient->addIdentifier(
                    STATISTICS_REPORTING_CLIENT_IDENTIFIER,
                    &m_pDataContainer->getStatisticsReportingClientCommunicationFault(),
                    &m_pDataContainer->getStatisticsReportingClientResponseNotOkFault(),
                    &m_pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault());
            }
        }
        else
        {
            std::ostringstream ss;
            ss << "Statistics reporting URL is missing or invalid. Statistics reporting will be disabled";
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
    }
    //else do nothing

    if ((m_pActiveInStationReverseSSHConnectorTask != 0) && (!m_pActiveInStationReverseSSHConnectorTask->isRunning()))
        m_pActiveInStationReverseSSHConnectorTask->start();

}

bool Model::startRetrieveConfigurationClient()
{
    getInstancePtr()->_createAndStartInstationRetrieveConfigurationClasses();

    return true;
}

void Model::stopRetrieveConfigurationClient()
{
    getInstancePtr()->_stopAndDestroyInstationRetrieveConfigurationClasses();
}

} //namespace
