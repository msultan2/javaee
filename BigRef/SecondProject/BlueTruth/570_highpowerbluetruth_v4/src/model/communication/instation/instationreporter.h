/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    11/06/2010  RG      001         V1.00 First Issue

*/

#ifndef INSTATION_REPORTER_H_
#define INSTATION_REPORTER_H_

#include "iinstationreporter.h"
#include "iobserver.h"
#include "types.h"

#include "ihttpclient.h"

#include <boost/shared_ptr.hpp>
#include <boost/thread/recursive_mutex.hpp>


class Clock;

namespace GSMModem
{
    class SignalLevelProcessor;
}

namespace Model
{
    class DataContainer;
    class Fault;
    class ICoreConfiguration;
    class ISeedConfiguration;
    class IniConfiguration;
}

namespace QueueDetection
{
    class QueueDetector;
}


namespace InStation
{

class IReverseSSHConnector;


class InStationReporter :
    public IInStationReporter,
    public ::IObserver
{

public:
    InStationReporter(
        const Model::ICoreConfiguration& coreConfiguration,
        const Model::ISeedConfiguration& seedConfiguration,
        const InStation::IReverseSSHConnector& reverseSSHConnector,
        boost::shared_ptr<GSMModem::SignalLevelProcessor> pSignalLevelProcessor,
        boost::shared_ptr<Model::DataContainer> pDataContainer,
        boost::shared_ptr<QueueDetection::QueueDetector> m_pQueueDetector,
        boost::shared_ptr<IHTTPClient> pRequestConfigurationClient,
        boost::shared_ptr<IHTTPClient> pCongestionReportingClient,
        boost::shared_ptr<IHTTPClient> pRawDeviceDetectionClient,
        boost::shared_ptr<IHTTPClient> pAlertAndStatusReportingClient,
        boost::shared_ptr<IHTTPClient> pStatusReportingClient,
        boost::shared_ptr<IHTTPClient> pFaultReportingClient,
        boost::shared_ptr<IHTTPClient> pStatisticsReportingClient,
        ::Clock* pClock
        );

    virtual ~InStationReporter();

    virtual void notifyOfStateChange(IObservable* pObservable, const int index);

    void setup(boost::shared_ptr<Model::IniConfiguration> pIniConfiguration);

    virtual void sendRawDeviceDetection();
    virtual void sendCongestionReport();
    virtual void sendFullStatusReport();
    virtual void sendStatusReport(const IHTTPClient::TStatusReportCollection& statusReportCollection);
    virtual void sendStatisticsReport();
    virtual void sendConfigurationRequest();
    virtual void reportFault();

private:

    //! default constructor. Not implemented
    InStationReporter();
    //! copy constructor. Not implemented
    InStationReporter(const InStationReporter& );
    //! assignment operator. Not implemented
    InStationReporter& operator=(const InStationReporter& );

    void commitSendingOfFaultReport();
    void rollBackSendingOfFaultReport();

    static void addRecordToAlertAndStatusReportCollection(
        const std::string& faultIdentifier,
        Model::Fault& fault,
        IHTTPClient::TAlertAndStatusReportCollection* pAlertAndStatusReportCollection);

    void sendAlertAndStatus();

    static void addRecordToFaultReportCollection(
        const unsigned int faultNumber,
        Model::Fault& fault,
        IHTTPClient::TFaultReportCollection* pFaultReportCollection);

    void sendFaultReport();

    void setAllFaultsAsReported();

    //Private members
    const Model::ICoreConfiguration& m_coreConfiguration;
    const Model::ISeedConfiguration& m_seedConfiguration;
    const InStation::IReverseSSHConnector& m_reverseSSHConnector;
    boost::shared_ptr<GSMModem::SignalLevelProcessor> m_pSignalLevelProcessor;
    const TSteadyTimePoint M_PROGRAM_START_TIME;
    boost::shared_ptr<Model::DataContainer> m_pDataContainer;
    boost::shared_ptr<QueueDetection::QueueDetector> m_pQueueDetector;
    boost::shared_ptr<IHTTPClient> m_pRequestConfigurationClient;
    boost::shared_ptr<IHTTPClient> m_pCongestionReportingClient;
    boost::shared_ptr<IHTTPClient> m_pRawDeviceDetectionClient;
    boost::shared_ptr<IHTTPClient> m_pAlertAndStatusReportingClient;
    boost::shared_ptr<IHTTPClient> m_pStatusReportingClient;
    boost::shared_ptr<IHTTPClient> m_pFaultReportingClient;
    mutable boost::recursive_mutex m_faultReportingClientMutex;
    boost::shared_ptr<IHTTPClient> m_pStatisticsReportingClient;

    int m_lastHashingFunctionUsed;
    std::string m_lastHashingFunctionSHA256PreSeed;
    std::string m_lastHashingFunctionSHA256PostSeed;

    ::Clock* m_pClock;
    TSteadyTimePoint m_startupTime;
    TSteadyTimePoint m_startupTimeWithDelay;

    TTime_t m_lastStatisticsReportTime;

    boost::shared_ptr<Model::IniConfiguration> m_pIniConfiguration;
};

}

#endif //INSTATION_REPORTER_H_
