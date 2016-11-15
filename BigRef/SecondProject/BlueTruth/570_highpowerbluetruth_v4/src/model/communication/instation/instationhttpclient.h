/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: This is an implementation of TCP client based on boost:asio library

    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef _INSTATION_HTTP_CLIENT_H_
#define _INSTATION_HTTP_CLIENT_H_


#include "iconnectionconsumer.h"
#include "itask.h"
#include "ihttpclient.h"
#include "identifiable.h"

#include "atomicvariable.h"
#include "boostsemaphore.h"
#include "clock.h"
#include "fastdatapacket.h"
#include "fault.h"
#include "httpresponseparser.h"
#include "mutex.h"
#include "types.h"
#include "uri.h"

#include <boost/shared_ptr.hpp>
#include <boost/system/error_code.hpp>
#include <sstream>


class Clock;

namespace Model
{
    class IConnectionProducerClient;
    class DataContainer;
    class ICoreConfiguration;
    class ISeedConfiguration;
    class IniConfiguration;
}

namespace QueueDetection
{
    struct CongestionReport;
}


namespace InStation
{

class InStationDataContainer;
class IInStationReporter;
class ISignatureGenerator;


class InStationHTTPClient :
    public Model::IConnectionConsumer,
    public ::ITask,
    public IHTTPClient,
    public ::Identifiable<int>
{
public:

    //! default constructor
    InStationHTTPClient(
        const Model::ICoreConfiguration& coreConfiguration,
        Model::ISeedConfiguration* pSeedConfiguration,
        Model::IConnectionProducerClient* pTCPClient,
        ISignatureGenerator* pSignatureGenerator,
        ::Clock *pClock,
        const int primaryId,
        Model::Fault* pPrimaryCommunicationFault,
        Model::Fault* pPrimaryResponseNotOKFault,
        Model::Fault* pPrimaryResponseMessageBodyErrorFault);

    //! destructor
    virtual ~InStationHTTPClient();

    virtual bool isFull() const;


    /**
    * @brief This function contains actions executed when the connection
    * to the server is established or fails upon connection
    *
    * @param success true is success, false if failure
    */
    virtual void onConnect(const bool success);

    virtual void onReceive(FastDataPacket_shared_ptr& pPacket);

    /**
     * @brief Method called after a packet has been sent. Should not used in this class
     * @param packet a packet that has just been sent
     */
    virtual void onSend(const bool success, FastDataPacket_shared_ptr& pPacket);

    /**
     * @brief Method called after a packet has been sent
     * @param data identifier of data and the actual data that just have been sent
     */
    virtual void onSend(const bool success, TSendDataPacket_shared_ptr& pData);

    /**
      @brief This function contains actions executed when the connection to the server is being reset
    */
    virtual void onClose();

    /**
     * @brief Method called when the backoff timer has started
     */
    virtual void onBackoffTimerStarted();

    /**
     * @brief Method called when the backoff timer has expired
     */
    virtual void onBackoffTimerExpired();

    virtual void initialise();
    virtual void perform();
    virtual void shutdown();
    virtual void stop();

    virtual unsigned short getRemoteSSHPortNumber() const;

    void setup(boost::shared_ptr<Model::DataContainer> pDataContainer);
    void setup(boost::shared_ptr<Model::IniConfiguration> pIniConfiguration);
    void setup(boost::shared_ptr<IInStationReporter> pInStationReporter);

    void addIdentifier(
        const int id,
        Model::Fault* pCommunicationFault,
        Model::Fault* pResponseNotOKFault,
        Model::Fault* pResponseMessageBodyErrorFault);

    void removeIdentifier(const int id);

    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into InStationDataContainer
    virtual void sendRawDeviceDetection(
        const ::TTime_t& startTime,
        const TRawDeviceDetectionCollection& rawDeviceDetectionCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = false);

    //-------------------------
    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into InStationDataContainer
    virtual void sendCongestionReport(
        const ::TTime_t& reportTime,
        const struct QueueDetection::CongestionReport& congestionReport,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = false);


    //-------------------------
    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into IniConfiguration
    virtual void sendConfigurationRequest(
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = false);


    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into InStationDataContainer
    virtual void sendAlertAndStatusReport(
        const ::TTime_t& reportTime,
        const TAlertAndStatusReportCollection& alertAndStatusReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true);

    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into InStationDataContainer
    virtual void sendStatusReport(
        const ::TTime_t& reportTime,
        const TStatusReportCollection& statusReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true);

    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into InStationDataContainer
    virtual void sendFaultReport(
        const ::TTime_t& reportTime,
        const TFaultReportCollection& faultReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true);

    //This is an asynchronous operation and there is no immediate result.
    //After the result has been received it will be put into InStationDataContainer
    virtual void sendStatisticsReport(
        const ::TTime_t& reportStartTime,
        const ::TTime_t& reportEndTime,
        const TStatisticsReportCollection& statisticsReportCollection,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = false);

    //-------------------------
    const std::string& getLastRequest() const { return m_lastRequest; }
    const std::string& getLastResponse() const { return m_lastResponse; }
    const THttpResponseContext* getLastResponseContext() { return m_pLastResponseContext; }

    void changeResponseTimeout(const int valueInSeconds);
    void setMaximumResponseDataPacket(const size_t value) { m_maximumExpectedResponseDataPacketSize = value; }

    enum EContentType
    {
        eCONTENT_TYPE_UNDEFINED = 0,
        eCONTENT_TYPE_PLAIN_TEXT,
        eCONTENT_TYPE_HTML_TEXT
    };


    enum ERequestType
    {
        eREQUEST_TYPE_UNDEFINED = 0,
        eREQUEST_TYPE_POST_CONGESTION_REPORT,
        eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION,
        eREQUEST_TYPE_POST_ALARM_AND_STATUS_REPORT,
        eREQUEST_TYPE_POST_STATUS_REPORT,
        eREQUEST_TYPE_POST_FAULT_REPORT,
        eREQUEST_TYPE_POST_STATISTICS_REPORT,
        eREQUEST_TYPE_GET_CONFIGURATION,
    };

    struct RequestTypeTuple
    {
        int id; ///< id that will be used to identify tuples
        static int globalNextId;

        FastDataPacket_shared_ptr pPacket;
        ERequestType type;
        bool usePersistentConnection;
        bool shouldCloseConnectionAfterSending;

        TSteadyTimePoint timeWhenSubmitted;
        TSteadyTimePoint timeWhenSent;
        unsigned int numberOfAttemptsToSent;

        enum EState
        {
            eNOT_SENT = 1,
            eSUBMITTED_TO_SEND,
            eSENT_BUT_NOT_REPLIED,
            eSENT_BUT_FAILED,
            eSENT_AND_REPLIED
        };
        EState state;
    };
    typedef std::list<RequestTypeTuple> TRequestTypeTupleList;


    enum ERequestState
    {
        eSTATE_UNKNOWN = 0,
        eSTATE_WAITING_FOR_REQUEST,
        eSTATE_REQUEST_RECEIVED_AND_WAITING_FOR_CONNECTION,
        eSTATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE,
        eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY,
        eSTATE_STOPPED,
        eSTATE_SIZE
    };
    static const char* STATE_NAME[eSTATE_SIZE];


private:
    //! default constructor. Not implemented
    InStationHTTPClient();
    //! copy constructor. Not implemented
    InStationHTTPClient(const InStationHTTPClient& );
    //! assignment operator. Not implemented
    InStationHTTPClient& operator=(const InStationHTTPClient& );


    void sendRequest(
        const std::string& requestString,
        const ERequestType requestType,
        const bool usePersistentConnection,
        const bool shouldCloseConnectionAfterSending);

    /**
     * @brief Check if there is something in the collection and if so send it
     */
    void sendPacketOutOfCollection();

    void processReceivedPacket(FastDataPacket_shared_ptr& pPacket);
    void processNewConfiguration(const std::string& inputString);
    void processPlainTextResponse(const std::string& inputString);

    void setState(const ERequestState state, const char* functionName);

    /**
     * A struct used to pass signals to be processed by this class
     */
    struct Signal
    {
        typedef enum
        {
            eUNKNOWN = 0,
            eON_CONNECT_SUCCESS,
            eON_CONNECT_FAILURE,
            eON_BACKOFF_TIMER_STARTED,
            eON_BACKOFF_TIMER_EXPIRED,
            eON_SOMETHING_TO_SENT_RECEIVED,
            eON_SENT_SUCCESS,
            eON_SENT_FAILURE,
            eON_RECEIVE,
            eON_CLOSE,
            eDUMMY_SIGNAL,
            eSIGNAL_TYPE_SIZE
        } EType;

        static const char* TYPE_NAME[eSIGNAL_TYPE_SIZE];

        EType signalType;
        int id;
        static int totalId;
        TSendDataPacket_shared_ptr pSendData;
        FastDataPacket_shared_ptr pReceiveData;
        boost::system::error_code errorCode;
    };
    typedef boost::shared_ptr<Signal> Signal_shared_ptr;


    void sendInternalSignal(Signal_shared_ptr pSignal);
    Signal_shared_ptr extractInternalSignal();

    void clearAllocatedSendRequestList();

    //Private members:

    //More objects of this kind may get deployed. So this is a flag to identify
    //them, especially when they use observer pattern
    //In some case this class may serve more goals (e.g. both to retrieve configuration and to report)
    //so it may have multiple identifiers
    struct TIdentity
    {
        int identifier;
        Model::Fault* pCommunicationFault;
        Model::Fault* pResponseNotOKFault;
        Model::Fault* pResponseMessageBodyErrorFault;

        TIdentity(
            const int _identifier,
            Model::Fault* _pCommunicationFault,
            Model::Fault* _pResponseNotOKFault,
            Model::Fault* _pResponseMessageBodyErrorFault)
        {
            identifier = _identifier;
            pCommunicationFault = _pCommunicationFault;
            pResponseNotOKFault = _pResponseNotOKFault;
            pResponseMessageBodyErrorFault = _pResponseMessageBodyErrorFault;
        }
    };
    typedef std::vector<TIdentity> TIdentityCollection;
    TIdentityCollection m_identityCollection;
    mutable ::Mutex m_identifierCollectionMutex;

    Model::IConnectionProducerClient* m_pTCPClient;
    boost::shared_ptr<Model::DataContainer> m_pDataContainer;

    const Model::ICoreConfiguration& m_coreConfiguration;
    Model::ISeedConfiguration* m_pSeedConfiguration;
    boost::shared_ptr<Model::IniConfiguration> m_pIniConfiguration;

    boost::shared_ptr<IInStationReporter> m_pInStationReporter;

    ISignatureGenerator* m_pSignatureGenerator;
    mutable ::Mutex m_signatureGeneratorMutex;

    ::Clock* m_pClock;

    std::string m_lastRequest;
    std::string m_lastResponse;
    THttpResponseContext* m_pLastResponseContext;

    TRequestTypeTupleList m_allocatedSendRequestList;
    mutable boost::shared_mutex m_allocatedSendRequestListMutex;
    size_t m_allocatedSendRequestListMaxSize;
    BoostSemaphore m_eventSemaphore;

    unsigned short m_remoteSSHPortNumber;

    ::AtomicVariable<ERequestState> m_requestState;

    size_t m_maximumExpectedResponseDataPacketSize;
    FastDataPacket_shared_ptr m_pFullResponseDataPacket;
    FastDataPacket_shared_ptr m_pDecodedHttpTransferContents;
    size_t m_positionFromWhereToSearchForCRLF;
    bool m_httpHeadersHaveBeenProcessedAlready;
    size_t m_positionOfHttpBody;
    size_t m_positionFromWhereToSearchForEOI;
    THttpResponseContext m_responseContext;
    EContentType m_contentType;

    TSteadyTimeDuration m_httpResponseTimeout;
    unsigned int m_httpSendMaxAttemptNumber;

    std::string m_rawDeviceDetectionPath;
    Uri m_uriRawDeviceDetectionPath;
    std::string m_congestionReportsPath;
    Uri m_uriCongestionReportsPath;
    std::string m_alertAndStatusReportsPath;
    Uri m_uriAlertAndStatusReportsPath;
    bool m_shouldInformAboutAlertAndStatus;
    std::string m_statusReportsPath;
    Uri m_uriStatusReportsPath;
    std::string m_faultReportsPath;
    Uri m_uriFaultReportsPath;
    std::string m_statisticsReportsPath;
    Uri m_uriStatisticsReportsPath;

    // Adding a facet formats the date/time string. The format is RFC 822.
    // The fractional seconds part is not added due to too high resolution.
    pt::time_facet* m_pSsFacet;
    std::locale m_ssLocale;

    pt::time_facet* m_outStationInstationTimeFacet;
    std::locale m_outStationInstationLocale;

    const std::string HEADER_USER_AGENT_STR;

    mutable boost::mutex m_signalCollectionMutex;
    std::list<Signal_shared_ptr> m_signalCollection;

//#define TESTING
#ifdef TESTING

public:

    //The following methods are provided for testing only
    TRequestTypeTupleList& getAllocatedSendRequestList();
    const FastDataPacket_shared_ptr& getFullResponseDataPacket() const;

    void test_processReceivedPacket(FastDataPacket_shared_ptr& pPacket);

    ERequestState getRequestState() const;

    void sendRequest__testing(
        const std::string& requestString,
        const ERequestType requestType,
        const bool usePersistentConnection,
        const bool shouldCloseConnectionAfterSending);

#endif
};

}

#endif //_INSTATION_HTTP_CLIENT_H_
