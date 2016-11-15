#include "stdafx.h"
#include "instationreporter.h"

#include "app.h"
#include "clock.h"
#include "instation/periodicallysendstatisticsreporttask.h"
#include "datacontainer.h"
#include "configuration/icoreconfiguration.h"
#include "configuration/iseedconfiguration.h"
#include "gsmmodem/activegsmmodemmonitor.h"
#include "gsmmodem/gsmmodemsignallevelprocessor.h"
#include "iniconfiguration.h"
#include "instation/instationhttpclient.h"
#include "ssh/ireversesshconnector.h"
#include "lock.h"
#include "logger.h"
#include "os_utilities.h"
#include "queuedetector.h"
#include "version.h"
#include "utils.h"

#include <algorithm>
#include <boost/thread/locks.hpp>
#include <signal.h>


namespace
{
    const uint64_t BACKGROUND_DEVICE_TIME_TAG = 0xFFFFFFFE;
}

using Model::DataContainer;
using Model::Fault;


namespace InStation
{

InStationReporter::InStationReporter(
    const Model::ICoreConfiguration& coreConfiguration,
    const Model::ISeedConfiguration& seedConfiguration,
    const InStation::IReverseSSHConnector& reverseSSHConnector,
    boost::shared_ptr<GSMModem::SignalLevelProcessor> pSignalLevelProcessor,
    boost::shared_ptr<Model::DataContainer> pDataContainer,
    boost::shared_ptr<QueueDetection::QueueDetector> pQueueDetector,
    boost::shared_ptr<IHTTPClient> pRequestConfigurationClient,
    boost::shared_ptr<IHTTPClient> pCongestionReportingClient,
    boost::shared_ptr<IHTTPClient> pRawDeviceDetectionClient,
    boost::shared_ptr<IHTTPClient> pAlertAndStatusReportingClient,
    boost::shared_ptr<IHTTPClient> pStatusReportingClient,
    boost::shared_ptr<IHTTPClient> pFaultReportingClient,
    boost::shared_ptr<IHTTPClient> pStatisticsReportingClient,
    ::Clock* pClock)
:
IInStationReporter(),
::IObserver(),
m_coreConfiguration(coreConfiguration),
m_seedConfiguration(seedConfiguration),
m_reverseSSHConnector(reverseSSHConnector),
m_pSignalLevelProcessor(pSignalLevelProcessor),
M_PROGRAM_START_TIME(pClock->getSteadyTime()),
m_pDataContainer(pDataContainer),
m_pQueueDetector(pQueueDetector),
m_pRequestConfigurationClient(pRequestConfigurationClient),
m_pCongestionReportingClient(pCongestionReportingClient),
m_pRawDeviceDetectionClient(pRawDeviceDetectionClient),
m_pAlertAndStatusReportingClient(pAlertAndStatusReportingClient),
m_pStatusReportingClient(pStatusReportingClient),
m_pFaultReportingClient(pFaultReportingClient),
m_faultReportingClientMutex(),
m_pStatisticsReportingClient(pStatisticsReportingClient),
m_lastHashingFunctionUsed(eHASHING_FUNCTION_NONE),
m_lastHashingFunctionSHA256PreSeed(),
m_lastHashingFunctionSHA256PostSeed(),
m_pClock(pClock),
m_startupTime(m_pClock->getSteadyTime()),
m_startupTimeWithDelay(m_startupTime),
m_lastStatisticsReportTime(m_pClock->getUniversalTime()),
m_pIniConfiguration()
{
    assert(m_pClock != 0);

    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
    {
        //Check that the clients that may be used in version 4+ are not deployed
        assert(pStatusReportingClient == 0);
        assert(pFaultReportingClient == 0);
        assert(pStatisticsReportingClient == 0);
    }
    else
    {
        //Check that the clients that may be used in version 3- are not deployed
        assert(pRawDeviceDetectionClient == 0);
        assert(pAlertAndStatusReportingClient == 0);
    }
}

InStationReporter::~InStationReporter()
{
    //do nothing
}

void InStationReporter::setup(boost::shared_ptr<Model::IniConfiguration> pIniConfiguration)
{
    m_pIniConfiguration = pIniConfiguration;

    if (pIniConfiguration != 0)
    {
        int64_t startupDelayInSeconds;
        m_pIniConfiguration->getValueInt64(Model::eINITIAL_STARTUP_DELAY, startupDelayInSeconds);
        m_startupTimeWithDelay = m_startupTime + bc::seconds(startupDelayInSeconds);
    }
    //else do nothing
}

void InStationReporter::sendRawDeviceDetection()
{
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() >= 4)
        return;

    if (m_pRawDeviceDetectionClient == 0)
        return;


    const bool isBlueToothDeviceFaulty = m_pDataContainer->getBluetoothDeviceFault().get();
    if (isBlueToothDeviceFaulty)
    {
        return;
    }
    //else continue

    //Send journey time data
    using InStation::InStationHTTPClient;
    using Model::DataContainer;

    InStationHTTPClient::TRawDeviceDetectionCollection collection;
    //Note: To avoid swapping of collections we use ...ForUse rather than ...ForReport
    ::Mutex& deviceRecordsMutex = m_pDataContainer->getRemoteDeviceCollectionMutex();
    DataContainer::TRemoteDeviceRecordCollection& deviceRecords(
        m_pDataContainer->getRemoteDeviceCollection());
    ::Lock lock(deviceRecordsMutex);
    for(DataContainer::TRemoteDeviceRecordCollection::iterator
            iter(deviceRecords.begin()),
            iterEnd(deviceRecords.end());
        iter != iterEnd;
        ++iter)
    {
        if (iter->second.presentInTheLastInquiry)
        {
            collection.push_back(InStationHTTPClient::TRawDeviceDetection(iter->second.address));
        }
        //else do nothing
    }

    m_pRawDeviceDetectionClient->sendRawDeviceDetection(
        m_pDataContainer->getLastInquiryStartTime(),
        collection);
}

void InStationReporter::sendCongestionReport()
{
    if (m_pCongestionReportingClient == 0)
        return;

    const ::TTime_t CURRENT_TIME(m_pClock->getUniversalTime());
    const TSteadyTimePoint CURRENT_TIME_STEADY(m_pClock->getSteadyTime());

    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
    {
        m_pQueueDetector->updateDevices();
    }
    else
    {
        m_pQueueDetector->updateDevicesFromRawTime(CURRENT_TIME_STEADY);
    }

    QueueDetection::TCongestionReport report(m_pQueueDetector->getCongestionReport());

    const bool isBlueToothDeviceFaulty = m_pDataContainer->getBluetoothDeviceFault().get();
    if (isBlueToothDeviceFaulty)
    {
        report.queuePresenceState = QueueDetection::eQUEUE_PRESENCE_STATE_FAULT;
    }
    //else do nothing

    m_pCongestionReportingClient->sendCongestionReport(CURRENT_TIME, report);
}

void InStationReporter::sendFullStatusReport()
{
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
        return;

    if (m_pStatusReportingClient == 0)
        return;

    InStationHTTPClient::TStatusReportCollection statusReportCollection;

    {
        InStationHTTPClient::StatusReport report("boot", "0");
        statusReportCollection.push_back(report);
    }

    {
        std::string s(Version::getNumber());
        InStationHTTPClient::StatusReport report("fv", s.c_str());
        statusReportCollection.push_back(report);
    }

    if (m_pIniConfiguration != 0)
    {
        std::string s(m_coreConfiguration.getSSLSerialNumber());
        InStationHTTPClient::StatusReport report("sn", s.c_str());
        statusReportCollection.push_back(report);
    }
    //else do nothing

    if (m_pIniConfiguration != 0)
    {
        std::string s(m_pIniConfiguration->getMD5Hash());
        InStationHTTPClient::StatusReport report("cv", s.c_str());
        statusReportCollection.push_back(report);
    }
    //else do nothing

    if (m_pIniConfiguration != 0)
    {
        InStationHTTPClient::StatusReport report("sl", "-255");
        if (m_pSignalLevelProcessor != 0)
        {
            std::string s(Utils::intToString(m_pSignalLevelProcessor->getAverageSignalLevel()));
            report.value = s.c_str();
        }
        statusReportCollection.push_back(report);
    }


    {
        int64_t obfuscatingFunction = 0;
        if (m_pIniConfiguration->getValueInt64(Model::eHASHING_FUNCTION, obfuscatingFunction))
        {
            std::string s(Utils::uint64ToString(obfuscatingFunction));
            InStationHTTPClient::StatusReport report("of", s.c_str());
            statusReportCollection.push_back(report);
        }
        //else do nothing
    }

    {
        std::string s(Utils::uint64ToString(m_seedConfiguration.getId()));
        InStationHTTPClient::StatusReport report("seed", s.c_str());
        statusReportCollection.push_back(report);
    }

    {
        InStationHTTPClient::StatusReport report("ssh", "");
        IReverseSSHConnector::TConnectionParameters parameters;
        if (m_reverseSSHConnector.isRunning(&parameters))
        {
            std::ostringstream ss;
            ss << "open "
                << parameters.address << ' '
                << parameters.remotePortNumber;
            report.value = ss.str();
        }
        else
        {
            report.value = "closed";
        }
        statusReportCollection.push_back(report);
    }

    {
        const TSteadyTimePoint CURRENT_TIME(m_pClock->getSteadyTime());
        const TSteadyTimeDuration TIME_SINCE_PROGRAM_START(CURRENT_TIME - M_PROGRAM_START_TIME);
        const uint64_t TIME_SINCE_PROGRAM_START_TOTAL_SECONDS = bc::duration_cast<bc::seconds>(TIME_SINCE_PROGRAM_START).count();
        std::string s(Utils::uint64ToString(TIME_SINCE_PROGRAM_START_TOTAL_SECONDS));
        InStationHTTPClient::StatusReport report("up", s.c_str());
        statusReportCollection.push_back(report);
    }

    m_pStatusReportingClient->sendStatusReport(
        m_pClock->getUniversalTime(),
        statusReportCollection);
}

void InStationReporter::sendStatusReport(const IHTTPClient::TStatusReportCollection& statusReportCollection)
{
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
        return;

    if (m_pStatusReportingClient == 0)
        return;

    m_pStatusReportingClient->sendStatusReport(
        m_pClock->getUniversalTime(),
        statusReportCollection);
}

void InStationReporter::sendStatisticsReport()
{
    const TTime_t CURRENT_TIME(m_pClock->getUniversalTime());

    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
        return;

    if (m_pStatisticsReportingClient == 0)
        return;


    const bool isBlueToothDeviceFaulty = m_pDataContainer->getBluetoothDeviceFault().get();
    if (isBlueToothDeviceFaulty)
    {
        return;
    }
    //else continue

    bool statisticsReportContentsReportFull = false;
    if (m_pIniConfiguration != 0)
    {
        std::string statisticsReportContents;
        if (m_pIniConfiguration->getValueString(Model::eSTATISTICS_REPORT_CONTENTS, statisticsReportContents))
        {
            //Convert to upper case
            transform(statisticsReportContents.begin(), statisticsReportContents.end(), statisticsReportContents.begin(), toupper);
            statisticsReportContentsReportFull = (statisticsReportContents == "FULL");
        }
        //else do nothing
    }
    //else do nothing

    if (m_pIniConfiguration != 0)
    {
        int64_t hashingFunction = 0;
        std::string hashingFunctionSHA256PreSeed;
        std::string hashingFunctionSHA256PostSeed;
        if (
            m_pIniConfiguration->getValueInt64(Model::eHASHING_FUNCTION, hashingFunction) &&
            m_pIniConfiguration->getValueString(Model::eHASHING_FUNCTION_SHA256_PRE_SEED, hashingFunctionSHA256PreSeed) &&
            m_pIniConfiguration->getValueString(Model::eHASHING_FUNCTION_SHA256_POST_SEED, hashingFunctionSHA256PostSeed)
            )
        {
            if (
                (hashingFunction != m_lastHashingFunctionUsed) ||
                (hashingFunctionSHA256PreSeed != m_lastHashingFunctionSHA256PreSeed) ||
                (hashingFunctionSHA256PostSeed != m_lastHashingFunctionSHA256PostSeed)
                )
            {
                ::Lock lock(m_pDataContainer->getRemoteDeviceCollectionMutex());

                Model::RemoteDeviceRecord::deviceIdentifierHashingFunction = hashingFunction;

                Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeedSize =
                    hashingFunctionSHA256PreSeed.size();
                if (Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeedSize >
                    sizeof(Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeed))
                {
                    Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeedSize =
                        sizeof(Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeed);
                }
                //else do nothing

                memcpy(
                    Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeed,
                    hashingFunctionSHA256PreSeed.c_str(),
                    Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeedSize);


                Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeedSize =
                    hashingFunctionSHA256PostSeed.size();
                if (Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeedSize >
                    sizeof(Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeed))
                {
                    Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeedSize =
                        sizeof(Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeed);
                }
                //else do nothing

                memcpy(
                    Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeed,
                    hashingFunctionSHA256PostSeed.c_str(),
                    Model::RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeedSize);


                //First report obsoleted records that do not exist in the map
                DataContainer::TRemoteDeviceRecordCollection& presentDeviceRecords(
                    m_pDataContainer->getRemoteDeviceCollection());
                for (DataContainer::TRemoteDeviceRecordCollection::iterator
                        iter(presentDeviceRecords.begin()),
                        iterEnd(presentDeviceRecords.end());
                    iter != iterEnd;
                    ++iter)
                {
                    iter->second.recalculateHash();
                }

                //First report obsoleted records that do not exist in the map
                DataContainer::TNonPendingRemoteDeviceRecordCollection& nonPresentDeviceRecords(
                    m_pDataContainer->getNonPendingRemoteDeviceCollection());
                for (DataContainer::TNonPendingRemoteDeviceRecordCollection::iterator
                        iter(nonPresentDeviceRecords.begin()),
                        iterEnd(nonPresentDeviceRecords.end());
                    iter != iterEnd;
                    ++iter)
                {
                    iter->recalculateHash();
                }

                m_lastHashingFunctionUsed = hashingFunction;
                m_lastHashingFunctionSHA256PreSeed = hashingFunctionSHA256PreSeed;
                m_lastHashingFunctionSHA256PostSeed = hashingFunctionSHA256PostSeed;
            }
            //else do nothing
        }
        //else do nothing
    }
    //else do nothing

    //Send journey time data
    using InStation::InStationHTTPClient;
    using Model::DataContainer;

    InStationHTTPClient::TStatisticsReportCollection collection;

    {
        ::Lock lock(m_pDataContainer->getRemoteDeviceCollectionMutex());

        //First report obsoleted records that do not exist in the map
        const DataContainer::TNonPendingRemoteDeviceRecordCollection& nonPresentDeviceRecords(
            m_pDataContainer->getNonPendingRemoteDeviceCollection());
        for (DataContainer::TNonPendingRemoteDeviceRecordCollection::const_iterator
                iter(nonPresentDeviceRecords.begin()),
                iterEnd(nonPresentDeviceRecords.end());
            iter != iterEnd;
            ++iter)
        {
            if (iter->binType != Model::eBIN_TYPE_BACKGROUND)
            {
                collection.push_back(InStationHTTPClient::TStatisticsReport(
                    iter->address, //deviceIdentifier
                    iter->deviceIdentifierHash,
                    iter->deviceClass,
                    iter->firstObservationTimeUTC,
                    iter->referencePointObservationTimeUTC - iter->firstObservationTimeUTC, //delta
                    iter->lastObservationTimeUTC - iter->firstObservationTimeUTC //delta
                    ));
            }
            else
            {
                collection.push_back(InStationHTTPClient::TStatisticsReport(
                    iter->address, //deviceIdentifier
                    iter->deviceIdentifierHash,
                    iter->deviceClass,
                    iter->firstObservationTimeUTC,
                    0,
                    BACKGROUND_DEVICE_TIME_TAG
                    ));
            }
        }

        //Clean those non-present remote devices
        m_pDataContainer->getNonPendingRemoteDeviceCollection().clear();


        if (statisticsReportContentsReportFull)
        {
            //Report current reports
            const DataContainer::TRemoteDeviceRecordCollection& deviceRecords(
                m_pDataContainer->getRemoteDeviceCollection());
            for(DataContainer::TRemoteDeviceRecordCollection::const_iterator
                    iter(deviceRecords.begin()),
                    iterEnd(deviceRecords.end());
                iter != iterEnd;
                ++iter)
            {
                if (iter->second.binType != Model::eBIN_TYPE_BACKGROUND)
                {
                    collection.push_back(InStationHTTPClient::TStatisticsReport(
                        iter->second.address, //deviceIdentifier
                        iter->second.deviceIdentifierHash,
                        iter->second.deviceClass,
                        iter->second.firstObservationTimeUTC,
                        0, //referencePointObservationTime
                        0  //lastObservationTime
                        ));
                }
                //else do not report
            }
        }
        //else do not report
    }

    m_pStatisticsReportingClient->sendStatisticsReport(
        m_lastStatisticsReportTime,
        CURRENT_TIME + pt::milliseconds(500),
        collection);
    m_lastStatisticsReportTime = CURRENT_TIME;
}

void InStationReporter::sendConfigurationRequest()
{
    if (m_pRequestConfigurationClient != 0)
        m_pRequestConfigurationClient->sendConfigurationRequest();
}

void InStationReporter::reportFault()
{
    const TSteadyTimePoint currentTime(m_pClock->getSteadyTime());
    if (currentTime > m_startupTimeWithDelay)
    {
        if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
        {
            if (m_pAlertAndStatusReportingClient == 0)
            {
                setAllFaultsAsReported();
            }
            else
            {
                sendAlertAndStatus();
            }
        }
        else
        {
            if (m_pFaultReportingClient == 0)
            {
                setAllFaultsAsReported();
            }
            else
            {
                sendFaultReport();
            }
        }
    }
    else
    {
        //do nothing
    }
}

void InStationReporter::notifyOfStateChange(IObservable* pObservable, const int index)
{
    assert(pObservable != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste

    {
        InStation::PeriodicallySendStatisticsReportTask* pReportDevicesTask =
            dynamic_cast<InStation::PeriodicallySendStatisticsReportTask* >(pObservable);

        if (pReportDevicesTask != 0)
        {
            sendStatisticsReport();

            return;
        }
        //else do nothing
    }

    {
        IHTTPClient* pHTTPClient =
            dynamic_cast<IHTTPClient* >(pObservable);

        if (pHTTPClient != 0)
        {
            switch (index)
            {
                case InStationHTTPClient::eRELOAD_CONFIGURATION:
                {
                    sendConfigurationRequest();
                    break;
                }

                case InStationHTTPClient::eLAST_ALARM_AND_STATUS_REPORT_HAS_BEEN_SENT:
                case InStationHTTPClient::eLAST_FAULT_REPORT_HAS_BEEN_SENT:
                {
                    commitSendingOfFaultReport();
                    break;
                }

                case InStationHTTPClient::eLAST_ALARM_AND_STATUS_REPORT_HAS_FAILED:
                case InStationHTTPClient::eLAST_FAULT_REPORT_HAS_FAILED:
                {
                    rollBackSendingOfFaultReport();
                    break;
                }

                case InStationHTTPClient::eREBOOT:
                {
                    processReceivedSignal(SIGTERM, eSYSTEM_RESTART_REQUIRED);
                    break;
                }

                case InStationHTTPClient::eSEND_FAULT_REPORT:
                {
                    reportFault();
                    break;
                }

                case InStationHTTPClient::eSEND_STATUS_REPORT:
                {
                    sendFullStatusReport();
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

    {
        InStation::IReverseSSHConnector* pReverseSSHConnector =
            dynamic_cast<InStation::IReverseSSHConnector* >(pObservable);
        if (pReverseSSHConnector != 0)
        {
            //Send STATUS REPORT
            InStationHTTPClient::TStatusReportCollection statusReportCollection;

            {
                InStationHTTPClient::StatusReport report("ssh", "");
                IReverseSSHConnector::TConnectionParameters parameters;
                if (m_reverseSSHConnector.isRunning(&parameters))
                {
                    std::ostringstream ss;
                    ss << "open "
                        << parameters.address << ' '
                        << parameters.remotePortNumber;
                    report.value = ss.str();
                }
                else
                {
                    report.value = "closed";
                }
                statusReportCollection.push_back(report);
            }

            sendStatusReport(statusReportCollection);


            //Try to send FAULT REPORT if status of a fault related to SSH changed
            sendFaultReport();

            return;
        }
        //else do nothing
    }

    {
        Model::ActiveGSMModemMonitor* pActiveGSMModemMonitor =
            dynamic_cast<Model::ActiveGSMModemMonitor* >(pObservable);
        if (pActiveGSMModemMonitor != 0)
        {
            if (pActiveGSMModemMonitor->isGPSSignalOK())
                m_pDataContainer->getGSMModemUnableToConnectFault().clear();
            else
                m_pDataContainer->getGSMModemUnableToConnectFault().set();

            reportFault();
        }
        //else do nothing
    }
}

void InStationReporter::commitSendingOfFaultReport()
{
    boost::lock_guard<boost::recursive_mutex> lock(m_faultReportingClientMutex);

    m_pDataContainer->getBluetoothDeviceFault().setWasReported();
    m_pDataContainer->getRetrieveConfigurationClientCommunicationFault().setWasReported();
    m_pDataContainer->getRetrieveConfigurationClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault().setWasReported();
    m_pDataContainer->getCongestionReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getCongestionReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault().setWasReported();
    m_pDataContainer->getRawDeviceDetectionClientCommunicationFault().setWasReported();
    m_pDataContainer->getRawDeviceDetectionClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault().setWasReported();
    m_pDataContainer->getAlertAndStatusReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault().setWasReported();
    m_pDataContainer->getStatusReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getStatusReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault().setWasReported();
    m_pDataContainer->getFaultReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getFaultReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault().setWasReported();
    m_pDataContainer->getStatisticsReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getStatisticsReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault().setWasReported();
    m_pDataContainer->getFunctionalConfigurationSyntaxFault().setWasReported();
    m_pDataContainer->getFunctionalConfigurationParameterValueFault().setWasReported();
    m_pDataContainer->getSeedFileFault().setWasReported();
    m_pDataContainer->getInStationSSHUnableToConnectFault().setWasReported();
    m_pDataContainer->getGSMModemUnableToConnectFault().setWasReported();
}

void InStationReporter::rollBackSendingOfFaultReport()
{
    boost::lock_guard<boost::recursive_mutex> lock(m_faultReportingClientMutex);

    m_pDataContainer->getBluetoothDeviceFault().clearPending();
    m_pDataContainer->getRetrieveConfigurationClientCommunicationFault().clearPending();
    m_pDataContainer->getRetrieveConfigurationClientResponseNotOkFault().clearPending();
    m_pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault().clearPending();
    m_pDataContainer->getCongestionReportingClientCommunicationFault().clearPending();
    m_pDataContainer->getCongestionReportingClientResponseNotOkFault().clearPending();
    m_pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault().clearPending();
    m_pDataContainer->getRawDeviceDetectionClientCommunicationFault().clearPending();
    m_pDataContainer->getRawDeviceDetectionClientResponseNotOkFault().clearPending();
    m_pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault().clearPending();
    m_pDataContainer->getAlertAndStatusReportingClientCommunicationFault().clearPending();
    m_pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault().clearPending();
    m_pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault().clearPending();
    m_pDataContainer->getStatusReportingClientCommunicationFault().clearPending();
    m_pDataContainer->getStatusReportingClientResponseNotOkFault().clearPending();
    m_pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault().clearPending();
    m_pDataContainer->getFaultReportingClientCommunicationFault().clearPending();
    m_pDataContainer->getFaultReportingClientResponseNotOkFault().clearPending();
    m_pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault().clearPending();
    m_pDataContainer->getStatisticsReportingClientCommunicationFault().clearPending();
    m_pDataContainer->getStatisticsReportingClientResponseNotOkFault().clearPending();
    m_pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault().clearPending();
    m_pDataContainer->getFunctionalConfigurationSyntaxFault().clearPending();
    m_pDataContainer->getFunctionalConfigurationParameterValueFault().clearPending();
    m_pDataContainer->getSeedFileFault().clearPending();
    m_pDataContainer->getInStationSSHUnableToConnectFault().clearPending();
    m_pDataContainer->getGSMModemUnableToConnectFault().clearPending();
}

void InStationReporter::addRecordToAlertAndStatusReportCollection(
    const std::string& faultIdentifier,
    Model::Fault& fault,
    InStationHTTPClient::TAlertAndStatusReportCollection* pAlertAndStatusReportCollection)
{
    if (!fault.wasReported() && !fault.isPending())
    {
        const unsigned int FAULT_VALUE = fault.get();
        InStationHTTPClient::TAlertAndStatusReport alertAndStatusReport(
            faultIdentifier,
            FAULT_VALUE);
        pAlertAndStatusReportCollection->push_back(alertAndStatusReport);

        fault.setPending();
    }
    //else do nothing
}

void InStationReporter::sendAlertAndStatus()
{
    //Legacy BlueTruth outstation
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() >= 4)
        return;

    if (m_pAlertAndStatusReportingClient == 0)
        return;


    InStationHTTPClient::TAlertAndStatusReportCollection alertAndStatusReportCollection;
    using Model::DataContainer;

    {
        boost::lock_guard<boost::recursive_mutex> lock(m_faultReportingClientMutex);

        //TODO Consider changing of this value in the new version (could be 04a, 04b,c,e,g,h,i too)
        addRecordToAlertAndStatusReportCollection(
            DataContainer::FAULT_STR_FUNCTIONAL_CONFIGURATION_SYNTAX,
            m_pDataContainer->getFunctionalConfigurationSyntaxFault(),
            &alertAndStatusReportCollection);

        addRecordToAlertAndStatusReportCollection(
            DataContainer::FAULT_STR_FUNCTIONAL_CONFIGURATION_PARAMETER_ERROR,
            m_pDataContainer->getFunctionalConfigurationParameterValueFault(),
            &alertAndStatusReportCollection);


        addRecordToAlertAndStatusReportCollection(
            DataContainer::FAULT_STR_NUMBER_RETRIEVE_CONFIGURATION_RESPONSE_NOT_OK,
            m_pDataContainer->getRetrieveConfigurationClientResponseNotOkFault(),
            &alertAndStatusReportCollection);

        addRecordToAlertAndStatusReportCollection(
            DataContainer::FAULT_STR_NUMBER_CONGESTION_REPORTING_RESPONSE_NOT_OK,
            m_pDataContainer->getCongestionReportingClientResponseNotOkFault(),
            &alertAndStatusReportCollection);

        addRecordToAlertAndStatusReportCollection(
            DataContainer::FAULT_STR_NUMBER_RAW_DEVICE_DETECTION_REPORTING_RESPONSE_NOT_OK,
            m_pDataContainer->getRawDeviceDetectionClientResponseNotOkFault(),
            &alertAndStatusReportCollection);

        addRecordToAlertAndStatusReportCollection(
            DataContainer::FAULT_STR_NUMBER_ALERT_AND_STATUS_REPORTING_RESPONSE_NOT_OK,
            m_pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault(),
            &alertAndStatusReportCollection);


        addRecordToAlertAndStatusReportCollection(
            DataContainer::FAULT_STR_NUMBER_RETRIEVE_CONFIGURATION_COMMUNICATION,
            m_pDataContainer->getRetrieveConfigurationClientCommunicationFault(),
            &alertAndStatusReportCollection);

        addRecordToAlertAndStatusReportCollection(
            DataContainer::FAULT_STR_NUMBER_RAW_DEVICE_DETECTION_REPORTING_COMMUNICATION,
            m_pDataContainer->getRawDeviceDetectionClientCommunicationFault(),
            &alertAndStatusReportCollection);

        addRecordToAlertAndStatusReportCollection(
            DataContainer::FAULT_STR_NUMBER_BLUETOOTH_DEVICE,
            m_pDataContainer->getBluetoothDeviceFault(),
            &alertAndStatusReportCollection);
    }


    if (!alertAndStatusReportCollection.empty())
    {
        const ::TTime_t CURRENT_TIME(m_pClock->getUniversalTime());
        m_pAlertAndStatusReportingClient->sendAlertAndStatusReport(
            CURRENT_TIME,
            alertAndStatusReportCollection);
    }
    //else do nothing
}

void InStationReporter::addRecordToFaultReportCollection(
    const unsigned int faultNumber,
    Model::Fault& fault,
    InStationHTTPClient::TFaultReportCollection* pFaultReportCollection)
{
    if (
        !fault.wasReported() &&
        !fault.isPending()
        )
    {
        const unsigned int FAULT_VALUE = fault.get();
        InStationHTTPClient::TFaultReport faultReport(
            faultNumber,
            (FAULT_VALUE == 0)?
                fault.getClearTime():
                fault.getSetTime(),
            FAULT_VALUE);
        pFaultReportCollection->push_back(faultReport);

        fault.setPending();
    }
    //else do nothing
}

void InStationReporter::sendFaultReport()
{
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
        return;

    if (m_pFaultReportingClient == 0)
        return;

    InStationHTTPClient::TFaultReportCollection faultReportCollection;
    using Model::DataContainer;


    {
        boost::lock_guard<boost::recursive_mutex> lock(m_faultReportingClientMutex);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_FUNCTIONAL_CONFIGURATION_SYNTAX,
            m_pDataContainer->getFunctionalConfigurationSyntaxFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_FUNCTIONAL_CONFIGURATION_PARAMETER_ERROR,
            m_pDataContainer->getFunctionalConfigurationParameterValueFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_SEED_FILE,
            m_pDataContainer->getSeedFileFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_BLUETOOTH_DEVICE,
            m_pDataContainer->getBluetoothDeviceFault(),
            &faultReportCollection);


        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_RETRIEVE_CONFIGURATION_COMMUNICATION,
            m_pDataContainer->getRetrieveConfigurationClientCommunicationFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_CONGESTION_REPORTING_COMMUNICATION,
            m_pDataContainer->getCongestionReportingClientCommunicationFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_STATUS_REPORTING_COMMUNICATION,
            m_pDataContainer->getStatusReportingClientCommunicationFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_FAULT_REPORTING_COMMUNICATION,
            m_pDataContainer->getFaultReportingClientCommunicationFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_STATISTICS_REPORTING_COMMUNICATION,
            m_pDataContainer->getStatisticsReportingClientCommunicationFault(),
            &faultReportCollection);


        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_RETRIEVE_CONFIGURATION_RESPONSE_NOT_OK,
            m_pDataContainer->getRetrieveConfigurationClientResponseNotOkFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_CONGESTION_REPORTING_RESPONSE_NOT_OK,
            m_pDataContainer->getCongestionReportingClientResponseNotOkFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_STATUS_REPORTING_RESPONSE_NOT_OK,
            m_pDataContainer->getStatusReportingClientResponseNotOkFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_FAULT_REPORTING_RESPONSE_NOT_OK,
            m_pDataContainer->getFaultReportingClientResponseNotOkFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_STATISTICS_REPORTING_RESPONSE_NOT_OK,
            m_pDataContainer->getStatisticsReportingClientResponseNotOkFault(),
            &faultReportCollection);


        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_RETRIEVE_CONFIGURATION_RESPONSE_MESSAGE_BODY_ERROR,
            m_pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_CONGESTION_REPORTING_RESPONSE_MESSAGE_BODY_ERROR,
            m_pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_STATUS_REPORTING_RESPONSE_MESSAGE_BODY_ERROR,
            m_pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_FAULT_REPORTING_RESPONSE_MESSAGE_BODY_ERROR,
            m_pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_NUMBER_STATISTICS_REPORTING_RESPONSE_MESSAGE_BODY_ERROR,
            m_pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault(),
            &faultReportCollection);


        addRecordToFaultReportCollection(
            DataContainer::eFAULT_INSTATION_SSH_UNABLE_TO_CONNECT,
            m_pDataContainer->getInStationSSHUnableToConnectFault(),
            &faultReportCollection);

        addRecordToFaultReportCollection(
            DataContainer::eFAULT_GSM_MODEM_UNABLE_TO_CONNECT,
            m_pDataContainer->getGSMModemUnableToConnectFault(),
            &faultReportCollection);
    }

    if (!faultReportCollection.empty())
    {
        const ::TTime_t CURRENT_TIME(m_pClock->getUniversalTime());
        m_pFaultReportingClient->sendFaultReport(
            CURRENT_TIME,
            faultReportCollection);
    }
    //else do nothing
}

void InStationReporter::setAllFaultsAsReported()
{
    boost::lock_guard<boost::recursive_mutex> lock(m_faultReportingClientMutex);

    m_pDataContainer->getBluetoothDeviceFault().setPending();
    m_pDataContainer->getBluetoothDeviceFault().setWasReported();

    m_pDataContainer->getRetrieveConfigurationClientCommunicationFault().setPending();
    m_pDataContainer->getRetrieveConfigurationClientCommunicationFault().setWasReported();
    m_pDataContainer->getRetrieveConfigurationClientResponseNotOkFault().setPending();
    m_pDataContainer->getRetrieveConfigurationClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault().setPending();
    m_pDataContainer->getRetrieveConfigurationClientResponseMessageBodyErrorFault().setWasReported();

    m_pDataContainer->getCongestionReportingClientCommunicationFault().setPending();
    m_pDataContainer->getCongestionReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getCongestionReportingClientResponseNotOkFault().setPending();
    m_pDataContainer->getCongestionReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault().setPending();
    m_pDataContainer->getCongestionReportingClientResponseMessageBodyErrorFault().setWasReported();

    m_pDataContainer->getRawDeviceDetectionClientCommunicationFault().setPending();
    m_pDataContainer->getRawDeviceDetectionClientCommunicationFault().setWasReported();
    m_pDataContainer->getRawDeviceDetectionClientResponseNotOkFault().setPending();
    m_pDataContainer->getRawDeviceDetectionClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault().setPending();
    m_pDataContainer->getRawDeviceDetectionClientResponseMessageBodyErrorFault().setWasReported();

    m_pDataContainer->getAlertAndStatusReportingClientCommunicationFault().setPending();
    m_pDataContainer->getAlertAndStatusReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault().setPending();
    m_pDataContainer->getAlertAndStatusReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault().setPending();
    m_pDataContainer->getAlertAndStatusReportingClientResponseMessageBodyErrorFault().setWasReported();

    m_pDataContainer->getStatusReportingClientCommunicationFault().setPending();
    m_pDataContainer->getStatusReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getStatusReportingClientResponseNotOkFault().setPending();
    m_pDataContainer->getStatusReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault().setPending();
    m_pDataContainer->getStatusReportingClientResponseMessageBodyErrorFault().setWasReported();

    m_pDataContainer->getFaultReportingClientCommunicationFault().setPending();
    m_pDataContainer->getFaultReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getFaultReportingClientResponseNotOkFault().setPending();
    m_pDataContainer->getFaultReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault().setPending();
    m_pDataContainer->getFaultReportingClientResponseMessageBodyErrorFault().setWasReported();

    m_pDataContainer->getStatisticsReportingClientCommunicationFault().setPending();
    m_pDataContainer->getStatisticsReportingClientCommunicationFault().setWasReported();
    m_pDataContainer->getStatisticsReportingClientResponseNotOkFault().setPending();
    m_pDataContainer->getStatisticsReportingClientResponseNotOkFault().setWasReported();
    m_pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault().setPending();
    m_pDataContainer->getStatisticsReportingClientResponseMessageBodyErrorFault().setWasReported();

    m_pDataContainer->getFunctionalConfigurationSyntaxFault().setPending();
    m_pDataContainer->getFunctionalConfigurationSyntaxFault().setWasReported();
    m_pDataContainer->getFunctionalConfigurationParameterValueFault().setPending();
    m_pDataContainer->getFunctionalConfigurationParameterValueFault().setWasReported();
    m_pDataContainer->getSeedFileFault().setPending();
    m_pDataContainer->getSeedFileFault().setWasReported();
    m_pDataContainer->getInStationSSHUnableToConnectFault().setPending();
    m_pDataContainer->getInStationSSHUnableToConnectFault().setWasReported();
    m_pDataContainer->getGSMModemUnableToConnectFault().setPending();
    m_pDataContainer->getGSMModemUnableToConnectFault().setWasReported();
}

} //namespace
