/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef _INSTATION_HTTP_CLIENT_H_
#define _INSTATION_HTTP_CLIENT_H_


#include "iconnectionconsumer.h"
#include "itask.h"
#include "ihttpclient.h"
#include "identifiable.h"

#include "atomicvariable.h"
#include "boostsemaphore.h"
#include "fastdatapacket.h"
#include "httpresponseparser.h"
#include "types.h"
#include "uri.h"

#include <boost/shared_ptr.hpp>
#include <boost/system/error_code.hpp>
#include <sstream>


class Clock;

namespace Model
{
    class IConnectionProducerClient;
}

namespace BrdfServer
{

class InStationDataContainer;
class IBrdfServerReporter;


class BrdfServerHTTPClient :
    public Model::IConnectionConsumer,
    public ::ITask,
    public IHTTPClient
{
public:

    //! default constructor
    BrdfServerHTTPClient(
        Model::IConnectionProducerClient* pTCPClient,
        ::Clock *pClock);

    //! destructor
    virtual ~BrdfServerHTTPClient();

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
    virtual void shutdown(const char* requestorName = 0);
    virtual void stop();

    /**
     * @brief Send STOP signal
     */
    static void sendStopSignal(void* pCtx);


    virtual void clearAllocatedSendRequestList();

    void setup(boost::shared_ptr<IBrdfServerReporter> pBrdfServerReporter);

    void setupConnectionParameters(const Model::BrdfMongoConfiguration& configuration);

    //This is an asynchronous operation and there is no immediate result.
    virtual bool sendRawData(
        const std::string& rawData,
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
        eREQUEST_TYPE_POST_RAW_DATA,
    };

    struct RequestTypeTuple
    {
        int id; ///< id that will be used to identify tuples
        static int globalNextId;

        FastDataPacket_shared_ptr pPacket;
        ERequestType type;
        bool usePersistentConnection;
        bool shouldCloseConnectionAfterSending;

        TTime_t timeWhenSubmitted;
        TTime_t timeWhenSent;
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
    BrdfServerHTTPClient();
    //! copy constructor. Not implemented
    BrdfServerHTTPClient(const BrdfServerHTTPClient& );
    //! assignment operator. Not implemented
    BrdfServerHTTPClient& operator=(const BrdfServerHTTPClient& );


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
            eSTOP,
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


    //Private members:

    Model::IConnectionProducerClient* m_pTCPClient;

    boost::shared_ptr<IBrdfServerReporter> m_pBrdfServerReporter;

    ::Clock* m_pClock;

    std::string m_lastRequest;
    std::string m_lastResponse;
    THttpResponseContext* m_pLastResponseContext;

    TRequestTypeTupleList m_allocatedSendRequestList;
    mutable boost::shared_mutex m_allocatedSendRequestListMutex;
    size_t m_allocatedSendRequestListMaxSize;
    BoostSemaphore m_eventSemaphore;

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

    ::TTimeDiff_t m_httpResponseTimeout;
    unsigned int m_httpSendMaxAttemptNumber;

    std::string m_rawDataPath;
    Uri m_uriRawDataPath;

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
