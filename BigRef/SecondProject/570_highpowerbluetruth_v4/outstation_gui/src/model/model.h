/*
    System: BlueTruth Outstation Project
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
 */

#ifndef _MODEL_H_
#define _MODEL_H_

#include "types.h"

#include <boost/shared_ptr.hpp>
#include <string>
#include <vector>


namespace BlueTooth
{
    class AcquireDevicesTask;
    class ConfigurationSaver;
    class DeviceDiscoverer;
    class PeriodicallyProcessBackgroundDevicesTask;
    class PeriodicallyRemoveNonPresentDevicesTask;
}

namespace GSMModem
{
    class SignalLevelProcessor;
}

namespace InStation
{
    class InStationHTTPClient;
    class InStationReporter;
    class PeriodicallySendCongestionReportTask;
    class PeriodicallySendStatisticsReportTask;
    class PeriodicallySendStatusReportTask;
    class RetrieveConfigurationTask;
    class ReverseSSHConnector;
    class SignatureGenerator;
}

namespace QueueDetection
{
    class QueueDetector;
}

namespace View
{
    class OutStationConfigurationParameters;
}

namespace Model
{
    class ActiveBoostAsioTCPClient;
    class ActiveGSMModemSshMonitor;
    class ActiveTask;
    class DataContainer;
    class Fault;
    class IniConfiguration;
    class SeedConfiguration;

class Model
{
public:
    //! destructor
    virtual ~Model();

    static bool construct();
    static void destruct();

    static Model* getInstancePtr();
    static bool isValid();

    static void applyNewIniConfiguration(const IniConfiguration& iniConfiguration);

    static boost::shared_ptr<DataContainer> getDataContainer();
    static boost::shared_ptr<BlueTooth::DeviceDiscoverer> getDeviceDiscoverer();

    static bool startBlueToothDeviceDiscovery();
    static void stopBlueToothDeviceDiscovery();

    static bool startRetrieveConfigurationClient();
    static void stopRetrieveConfigurationClient();
    static bool setRetrieveConfigurationConnectionParameters();

    static bool startAllInstationClients();
    static void stopAllInstationClients();

    static bool startGSMModemSSHMonitor();
    static void stopGSMModemSSHMonitor();

    enum
    {
        eNOT_USED = 0,
        eINSTATION_CLIENT_IS_STARTING,
        eINSTATION_CLIENT_IS_STOPPING
    };

    //!get ip address of the host to which to connect
    std::string getRetrieveConfigurationRemoteAddress() const { return m_retrieveConfigurationRemoteAddress; }
    //!get port of the host to which to connect
    uint16_t getRetrieveConfigurationRemotePortNumber() const { return m_retrieveConfigurationRemotePortNumber; }


private:

    //! default constructor
    explicit Model(View::OutStationConfigurationParameters& configurationParameters);

    //! default constructor. Not implemented
    Model();
    //! copy constructor. Not implemented
    Model(const Model& );
    //! assignment operator. Not implemented
    Model& operator=(const Model& );


    void _applyNewIniConfiguration(const IniConfiguration& iniConfiguration, const bool isConstructing);

    bool _startBlueToothDeviceDiscovery();
    void _stopBlueToothDeviceDiscovery();
    void _setBlueToothDeviceDiscoveryParameters();

    bool _constructAndStartAllInstationClientClasses();
    void _stopAndDestroyAllInstationClientClasses();

    void _constructAndStartInstationClient(
        const int identifier,
        Fault* pPrimaryCommunicationFault,
        Fault* pPrimaryResponseNotOKFault,
        Fault* pPrimaryResponseMessageBodyErrorFault,
        const std::string& remoteAddress,
        const uint16_t remotePortNumber,
        const std::string& localAddress,
        boost::shared_ptr<ActiveBoostAsioTCPClient>& pActiveTCPClient,
        boost::shared_ptr<InStation::InStationHTTPClient>& pHttpClient,
        boost::shared_ptr<ActiveTask>& pActiveHttpClientTask);

    void _stopAndDestroyInstationClient(
        const int identifier,
        boost::shared_ptr<ActiveBoostAsioTCPClient>& pActiveTCPClient,
        boost::shared_ptr<InStation::InStationHTTPClient>& pHttpClient,
        boost::shared_ptr<ActiveTask>& pActiveHttpClientTask);

    void _constructInStationReporter();
    void _destructInStationReporter();

    bool _startGSMModemSSHMonitor();
    void _stopGSMModemSSHMonitor();

    bool _setRetrieveConfigurationConnectionParameters();

    //! For each TCP IP client check if the new address and port from the new configuration (iniConfiguration)
    //! is the same as it is currently used
    //! @return true if for any client (conjestion, journey time, alert and status) the address or port does not match
    //! @param iniConfiguration new ini configuration to be used for comparison
    bool _verifyIfIpAddressesChanged(const IniConfiguration& iniConfiguration);

    //! Method to compare if for a particular TCP IP client an address or port is different from that in the configuration file
    //! @return true if for the TCP IP client its address or port does not match the one specified in the new configuration file
    //! @param iniConfiguration new ini configuration to be used for comparison
    //! @param typeId an int value of casted from IniConfiguration::EValueTypeId identifying a parameter to be read from the new configuration
    //! @param pTcpClient the TCP IP client used for comparison
    static bool _verifyIfIpAddressChanged(
        const IniConfiguration& iniConfiguration,
        const int typeId,
        const boost::shared_ptr<ActiveBoostAsioTCPClient>& pTcpClient
        );

    //! Method to compare if current mode is different from that in the configuration file
    //! @return true if different, false otherwise
    bool _verifyIfModeChanged(const IniConfiguration& iniConfiguration) const;

    void _applyQueueDetectionAlgorithmParameters(const IniConfiguration& iniConfiguration);
    void _applyPeriodicReportParameters(const IniConfiguration& iniConfiguration);
    void _applyGSMModemSignalLevelParameters(const IniConfiguration& iniConfiguration);
    void _applyDeviceMode(const IniConfiguration& iniConfiguration);
    void _applyInquiryDeviceParameters(const IniConfiguration& iniConfiguration);

    void _createAndStartInstationRetrieveConfigurationClasses();
    void _stopAndDestroyInstationRetrieveConfigurationClasses();

    void _createAndStartInstationReportingClasses();
    void _stopAndDestroyInstationReportingClasses();

    static bool _applyInstationParameter(
        const IniConfiguration& iniConfiguration,
        const int typeId,
        std::string& remoteAddress,
        uint16_t& remotePort);


    //Private members:

    static Model* m_instancePtr;
    static bool m_valid;


    boost::shared_ptr<DataContainer> m_pDataContainer;
    boost::shared_ptr<QueueDetection::QueueDetector> m_pQueueDetector;
    boost::shared_ptr<IniConfiguration> m_pIniConfiguration;
    View::OutStationConfigurationParameters& m_coreConfiguration;
    boost::shared_ptr<SeedConfiguration> m_pSeedConfiguration;
    boost::shared_ptr<InStation::SignatureGenerator> m_pSignatureGenerator;


    int64_t m_modeNumber;

    //InStation Parameters
    bool m_retrieveConfigurationConnectionParametersAreValid;
    std::string m_retrieveConfigurationRemoteAddress; //ip address of the host to which to connect
    uint16_t m_retrieveConfigurationRemotePortNumber; //port of the host to which to connect
    std::string m_retrieveConfigurationLocalAddress; //ip address of the local interface which will be used to connect to host

    bool m_congestionReportingConnectionParametersAreValid;
    std::string m_congestionReportingRemoteAddress; //ip address of the host to which to connect
    uint16_t m_congestionReportingRemotePortNumber; //port on which this host will be listening
    std::string m_congestionReportingLocalAddress; //ip address of the local interface which will be used to connect to host

    bool m_rawDeviceDetectionConnectionParametersAreValid;
    std::string m_rawDeviceDetectionRemoteAddress; //ip address of the host to which to connect
    uint16_t m_rawDeviceDetectionRemotePortNumber; //port on which this host will be listening
    std::string m_rawDeviceDetectionLocalAddress; //ip address of the local interface which will be used to connect to host

    bool m_alertAndStatusReportingConnectionParametersAreValid;
    std::string m_alertAndStatusReportingRemoteAddress; //ip address of the host to which to connect
    uint16_t m_alertAndStatusReportingRemotePortNumber; //port on which this host will be listening
    std::string m_alertAndStatusReportingLocalAddress; //ip address of the local interface which will be used to connect to host

    bool m_statusReportingConnectionParametersAreValid;
    std::string m_statusReportingRemoteAddress; //ip address of the host to which to connect
    uint16_t m_statusReportingRemotePortNumber; //port on which this host will be listening
    std::string m_statusReportingLocalAddress; //ip address of the local interface which will be used to connect to host

    bool m_faultReportingConnectionParametersAreValid;
    std::string m_faultReportingRemoteAddress; //ip address of the host to which to connect
    uint16_t m_faultReportingRemotePortNumber; //port on which this host will be listening
    std::string m_faultReportingLocalAddress; //ip address of the local interface which will be used to connect to host

    bool m_statisticsReportingConnectionParametersAreValid;
    std::string m_statisticsReportingRemoteAddress; //ip address of the host to which to connect
    uint16_t m_statisticsReportingRemotePortNumber; //port on which this host will be listening
    std::string m_statisticsReportingLocalAddress; //ip address of the local interface which will be used to connect to host


    boost::shared_ptr<ActiveBoostAsioTCPClient> m_pRetrieveConfigurationActiveTCPClient;
    boost::shared_ptr<InStation::InStationHTTPClient> m_pRetrieveConfigurationClient;
    boost::shared_ptr<ActiveTask> m_pActiveRetrieveConfigurationClientTask;

    boost::shared_ptr<InStation::InStationReporter> m_pInStationReporter;

    boost::shared_ptr<ActiveBoostAsioTCPClient> m_pCongestionReportingActiveTCPClient;
    boost::shared_ptr<InStation::InStationHTTPClient> m_pCongestionReportingClient;
    boost::shared_ptr<ActiveTask> m_pActiveCongestionReportingClientTask;

    boost::shared_ptr<ActiveBoostAsioTCPClient> m_pRawDeviceDetectionActiveTCPClient;
    boost::shared_ptr<InStation::InStationHTTPClient> m_pRawDeviceDetectionClient;
    boost::shared_ptr<ActiveTask> m_pActiveRawDeviceDetectionClientTask;

    boost::shared_ptr<ActiveBoostAsioTCPClient> m_pAlertAndStatusReportingActiveTCPClient;
    boost::shared_ptr<InStation::InStationHTTPClient> m_pAlertAndStatusReportingClient;
    boost::shared_ptr<ActiveTask> m_pActiveAlertAndStatusReportingClientTask;

    boost::shared_ptr<ActiveBoostAsioTCPClient> m_pStatusReportingActiveTCPClient;
    boost::shared_ptr<InStation::InStationHTTPClient> m_pStatusReportingClient;
    boost::shared_ptr<ActiveTask> m_pActiveStatusReportingClientTask;

    boost::shared_ptr<ActiveBoostAsioTCPClient> m_pFaultReportingActiveTCPClient;
    boost::shared_ptr<InStation::InStationHTTPClient> m_pFaultReportingClient;
    boost::shared_ptr<ActiveTask> m_pActiveFaultReportingClientTask;

    boost::shared_ptr<ActiveBoostAsioTCPClient> m_pStatisticsReportingActiveTCPClient;
    boost::shared_ptr<InStation::InStationHTTPClient> m_pStatisticsReportingClient;
    boost::shared_ptr<ActiveTask> m_pActiveStatisticsReportingClientTask;

    boost::shared_ptr<ActiveTask> m_pActiveRetrieveConfigurationTask;
    boost::shared_ptr<InStation::RetrieveConfigurationTask> m_pRetrieveConfigurationTask;

    boost::shared_ptr<BlueTooth::DeviceDiscoverer> m_pDeviceDiscoverer;
    boost::shared_ptr<ActiveTask> m_pActiveAcquireDevicesTask;
    boost::shared_ptr<BlueTooth::AcquireDevicesTask> m_pAcquireDevicesTask;

    boost::shared_ptr<ActiveTask> m_pActivePeriodicallyProcessBackgroundDevicesTask;
    boost::shared_ptr<BlueTooth::PeriodicallyProcessBackgroundDevicesTask> m_pPeriodicallyProcessBackgroundDevicesTask;

    boost::shared_ptr<ActiveTask> m_pActivePeriodicallyRemoveNonPresentDevicesTask;
    boost::shared_ptr<BlueTooth::PeriodicallyRemoveNonPresentDevicesTask> m_pPeriodicallyRemoveNonPresentDevicesTask;

    boost::shared_ptr<ActiveTask> m_pActivePeriodicallySendCongestionReportTask;
    boost::shared_ptr<InStation::PeriodicallySendCongestionReportTask> m_pPeriodicallySendCongestionReportTask;

    boost::shared_ptr<ActiveTask> m_pActivePeriodicallySendStatisticsReportTask;
    boost::shared_ptr<InStation::PeriodicallySendStatisticsReportTask> m_pPeriodicallySendStatisticsReportTask;

    boost::shared_ptr<ActiveTask> m_pActivePeriodicallySendStatusReportTask;
    boost::shared_ptr<InStation::PeriodicallySendStatusReportTask> m_pPeriodicallySendStatusReportTask;

    boost::shared_ptr<ActiveTask> m_pActiveInStationReverseSSHConnectorTask;
    boost::shared_ptr<InStation::ReverseSSHConnector> m_pInStationReverseSSHConnector;

    boost::shared_ptr<BlueTooth::ConfigurationSaver> m_pConfigurationSaver;

    boost::shared_ptr<ActiveGSMModemSshMonitor> m_pActiveGSMModemSshMonitor;
    boost::shared_ptr<GSMModem::SignalLevelProcessor> m_pSignalLevelProcessor;
};

} //namespace Model

#endif //_MODEL_H_
