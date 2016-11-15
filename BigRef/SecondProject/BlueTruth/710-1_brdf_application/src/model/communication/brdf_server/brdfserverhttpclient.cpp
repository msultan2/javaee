#include "stdafx.h"
#include "brdf_server/brdfserverhttpclient.h"

#include "activeboostasiotcpclient.h"
#include "brdfmongoconfiguration.h"
#include "clock.h"
//#include "datacontainer.h"
#include "httpresponseparser.h"
#include "ibrdfxmlconfiguration.h"
//#include "iinstationdatacontainer.h"
#include "ibrdfserverreporter.h"
#include "logger.h"
#include "model.h"
#include "os_utilities.h"
#include "types.h"
#include "utils.h"
#include "version.h"

#include <boost/algorithm/string.hpp>
#include <boost/thread.hpp>
#include <boost/thread/locks.hpp>
#include <climits>
#include <ctype.h>

#define EOL "\x0d\x0a"

#ifdef max
#undef max
#endif

#define HEADER_ACCEPT_ALL_EOL "Accept: */*\x0d\x0a"
#define HEADER_CONNECTION_CLOSE_EOL "Connection: close\x0d\x0a"
#define HEADER_CONNECTION_KEEP_ALIVE_EOL "Connection: keep-alive\x0d\x0a"

#define RECORD_SEPARATOR ","
#define FIELD_SEPARATOR ":"


namespace
{
    const size_t DEFAULT_MAXIMUM_RESPONSE_DATA_PACKET = 1024*1024; //1MB
    const unsigned int SEMAPHORE_SLEEP_PERIOD_MS = 100;
    const unsigned int SLEEP_TIME_IN_MS_WHEN_STOPPED = 10;

    const unsigned char CRLF[] = {0x0D, 0x0A};

    const int STATUS_OK = 200;
    const int MAX_NUMBER_OF_DISPATCHES = 3;


    const unsigned char CR = 0xD;
    const unsigned char LF = 0xA;

    const unsigned int MAX_NUMBER_OF_CONNECT_ATTEMPTS = 3;

    const char MODULE_NAME[] = "BrdfServerHTTPClient";

    const int MESSAGE_ID = 3;
    const uint32_t SIGNATURE_MASK = 0xFFFFU;

    const size_t TIME_WIDTH = 8;

    bool checkForPresenceOfChars(
        const FastDataPacket& packet,
        const unsigned char char1,
        const unsigned char char2,
        const unsigned char char3,
        const unsigned char char4,
        const size_t fromWhereToSearch,
        size_t& positionAfterTheFoundString)
    {
        //Sanity check
        if (packet.size() < fromWhereToSearch + 4)
        {
            return false;
        }
        //else continue

        //Search for the character stream
        const size_t PACKET_SIZE_MINUS_SEARCH_LENGTH = packet.size() - 4;
        for(size_t i=fromWhereToSearch; i<=PACKET_SIZE_MINUS_SEARCH_LENGTH; ++i)
        {
            if (
                (packet[i  ] == char1) &&
                (packet[i+1] == char2) &&
                (packet[i+2] == char3) &&
                (packet[i+3] == char4)
                )
            {
                positionAfterTheFoundString = i+4;
                return true;
            }
            //else check the next value
        }

        return false;
    }
}


using Model::ActiveBoostAsioTCPClient;

namespace BrdfServer
{


BrdfServerHTTPClient::BrdfServerHTTPClient(
        Model::IConnectionProducerClient* pTCPClient,
        ::Clock *pClock)
:
Model::IConnectionConsumer(),
::ITask(),
IHTTPClient(),
m_pTCPClient(pTCPClient),
m_pBrdfServerReporter(),
m_pClock(pClock),
m_lastRequest(),
m_lastResponse(),
m_pLastResponseContext(0),
m_allocatedSendRequestList(),
m_allocatedSendRequestListMutex(),
m_allocatedSendRequestListMaxSize(1000),
m_eventSemaphore("m_eventSemaphore", 0x1000),
m_requestState(eSTATE_WAITING_FOR_REQUEST),
m_maximumExpectedResponseDataPacketSize(DEFAULT_MAXIMUM_RESPONSE_DATA_PACKET),
m_pFullResponseDataPacket(),
m_pDecodedHttpTransferContents(),
m_positionFromWhereToSearchForCRLF(0),
m_httpHeadersHaveBeenProcessedAlready(false),
m_positionOfHttpBody(0),
m_positionFromWhereToSearchForEOI(0),
m_responseContext(),
m_contentType(eCONTENT_TYPE_UNDEFINED),
m_httpResponseTimeout(),
m_httpSendMaxAttemptNumber(),
m_uriRawDataPath(),
m_pSsFacet(new pt::time_facet("%a, %d %b %Y %H:%M:%S%f %ZP")),
m_ssLocale(std::locale("C"), m_pSsFacet),
m_outStationInstationTimeFacet(new pt::time_facet("%Y-%m-%d %H:%M:%S")),
m_outStationInstationLocale(std::locale("C"), m_outStationInstationTimeFacet),
HEADER_USER_AGENT_STR("User-agent: " + Version::getApplicationNameWithoutSpaces() + "/" + Version::getNumber() + " (" + Version::getDate() + ")"),
m_signalCollectionMutex(),
m_signalCollection()
{
    assert(m_pTCPClient != 0);
}

BrdfServerHTTPClient::~BrdfServerHTTPClient()
{
    //Do not explicitly delete p_ss_facet. The delete is implicit in the destruction of special_locale.
    //std::locale takes ownership of the facet.
    //See http://rhubbarb.wordpress.com/2009/10/17/boost-datetime-locales-and-facets/
}


void BrdfServerHTTPClient::setup(boost::shared_ptr<IBrdfServerReporter> pBrdfServerReporter)
{
    m_pBrdfServerReporter = pBrdfServerReporter;
}

void BrdfServerHTTPClient::setupConnectionParameters(const Model::BrdfMongoConfiguration& configuration)
{
    m_httpResponseTimeout = pt::seconds(configuration.responseTimeoutInSeconds);
    m_httpSendMaxAttemptNumber = configuration.numberOfRetries;
    m_uriRawDataPath.Host = configuration.host;
    m_uriRawDataPath.Port = Utils::uint64ToString(configuration.port);
    m_uriRawDataPath.Path = configuration.path;
}

bool BrdfServerHTTPClient::isFull() const
{
    return false;
}

void BrdfServerHTTPClient::processReceivedPacket(FastDataPacket_shared_ptr& packet)
{
    if (packet == 0)
    {
        if (m_requestState.get() != eSTATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE)
        {
            return;
        }
        else
        {
            //! It may happen that after a request and with a small delay a server closes the connection
            //! If this was a second packet or a later one flush the previous contents
            if (m_pFullResponseDataPacket == 0)
            {
                return;
            }
            //else flush after the previous packets
        }
    }
    //else continue

    bool flushContents = (packet == 0);
    if (!flushContents)
    {
        //Grab the received packet or append it to the collection of the received ones
        if (m_pFullResponseDataPacket == 0)
        {
            m_pFullResponseDataPacket = packet;
            //Declare maximum expected full response size to avoid copying when expanding the packet size
            m_pFullResponseDataPacket->reserve(m_maximumExpectedResponseDataPacketSize);

            m_positionFromWhereToSearchForCRLF = 0;
            m_httpHeadersHaveBeenProcessedAlready = false;
            m_positionOfHttpBody = 0;
            m_positionFromWhereToSearchForEOI = 0;
            m_contentType = eCONTENT_TYPE_UNDEFINED;

            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
            {
                std::ostringstream ss;
                ss << "BrdfServerHTTPClient::receive() Starting new packet of size " << packet->size();
                Logger::log(LOG_LEVEL_DEBUG3, ss.str().c_str());
            }
            //else do nothing
        }
        else
        {
            //The position to look for CRLF should be less one because 0xD can be in the previous packet and 0xA in the current one
            if (m_pFullResponseDataPacket->size() >= 4)
            {
                m_positionFromWhereToSearchForCRLF = m_pFullResponseDataPacket->size() - 4;
                m_positionFromWhereToSearchForEOI = m_pFullResponseDataPacket->size() - 4;
            }
            else
            {
                m_positionFromWhereToSearchForCRLF = 0;
                m_positionFromWhereToSearchForEOI = 0;
            }

            m_pFullResponseDataPacket->append(*packet);

            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
            {
                std::ostringstream ss;
                ss << "BrdfServerHTTPClient::receive() Another part of packet received of size " << packet->size();
                Logger::log(LOG_LEVEL_DEBUG3, ss.str().c_str());
            }
            //else do nothing
        }

        if (!m_httpHeadersHaveBeenProcessedAlready)
        {
            //Check for presence of empty line after http headers but before http body
            bool emptyLineFound = checkForPresenceOfChars(
                *m_pFullResponseDataPacket,
                CRLF[0],
                CRLF[1],
                CRLF[0],
                CRLF[1],
                m_positionFromWhereToSearchForCRLF,
                m_positionOfHttpBody);
            if (!emptyLineFound)
            {
                //Wait for more data
                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
                {
                    Logger::log(LOG_LEVEL_DEBUG3,
                        "BrdfServerHTTPClient::receive() The http response has been received but there is no empty line in. Waiting for more data...");
                }
                //else do nothing

                return;
            }
            //else continue

            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
            {
                Logger::log(LOG_LEVEL_DEBUG3,
                    "BrdfServerHTTPClient::receive() Full HTTP header received");
            }
            //else do nothing

            //Parse the http data
            const std::string inputString(reinterpret_cast<const char*>(m_pFullResponseDataPacket->data()), m_positionOfHttpBody);
            bool ok = HttpResponseParser::parse(inputString, m_responseContext);
            if (!ok)
            {
                //Empty line was found but the entire http message could not be parsed. Discard and close connection

                //First report the problem to the user
                std::ostringstream ss;
                ss << "The received http response could not be parsed:\n \"" << inputString << "\"";
                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

                //Discard, close connection
                m_pFullResponseDataPacket.reset();
                m_pTCPClient->cancelConnection();
                return;
            }
            else if (
                !m_responseContext.httpResponse.contentType.empty() &&
                !m_responseContext.httpResponse.contentSubtype.empty() &&
                m_responseContext.httpResponse.transferEncoding.empty() &&
                (m_responseContext.httpResponse.contentLength == std::numeric_limits<std::size_t>::max())
                )
            {
                //Missing content length, but not a chunked transfer

                //First report the problem to the user
                std::ostringstream ss;
                ss << "The received http response is not chunked transfer but does not contain content length field:\n \"" << inputString << "\"";
                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

                //Discard, close connection
                m_pFullResponseDataPacket.reset();
                m_pTCPClient->cancelConnection();
                return;
            }
            //else do nothing

            m_httpHeadersHaveBeenProcessedAlready = true;

            //Compare the strings only once upon receipt of http headers
            if (
                (m_responseContext.httpResponse.contentType == "text") &&
                (m_responseContext.httpResponse.contentSubtype == "plain")
                )
            {
                m_contentType = eCONTENT_TYPE_PLAIN_TEXT;
            }
            else if (
                (m_responseContext.httpResponse.contentType == "text") &&
                (m_responseContext.httpResponse.contentSubtype == "html")
                )
            {
                m_contentType = eCONTENT_TYPE_HTML_TEXT;
            }
            else if (
                (m_responseContext.httpResponse.contentType.empty()) &&
                (m_responseContext.httpResponse.contentSubtype.empty())
                )
            {
                m_contentType = eCONTENT_TYPE_PLAIN_TEXT;

                if (m_responseContext.httpResponse.contentLength == std::numeric_limits<std::size_t>::max()) //i.e. undefined
                {
                    m_responseContext.httpResponse.contentLength = 0;
                }
                //else do nothing
            }
            else
            {
                //do nothing
            }
        }
        //else do nothing - HTTP headers have been processed already
    }
    else
    {
        boost::shared_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
        if (m_allocatedSendRequestList.empty())
        {
            //Do nothing. Do not event continue
            return;
        }
        //else continue
    }


    switch (m_contentType)
    {
        case eCONTENT_TYPE_UNDEFINED: // this is the case when the response body is empty
        case eCONTENT_TYPE_PLAIN_TEXT:
        case eCONTENT_TYPE_HTML_TEXT:
        {
            if (m_pFullResponseDataPacket->size() - m_positionOfHttpBody >= m_responseContext.httpResponse.contentLength)
            {
                //Full response received
                {
                    //Log the whole received packet
                    m_lastResponse.clear();
                    if (m_pFullResponseDataPacket != 0)
                    {
                        m_lastResponse.append(
                            reinterpret_cast<const char*>(m_pFullResponseDataPacket->data()),
                            m_pFullResponseDataPacket->size());
                    }
                    //else do nothing

                    if (!m_lastResponse.empty())
                    {
                        Logger::log(LOG_LEVEL_INFO, "RX:", m_lastResponse.c_str());
                    }
                    else
                    {
                        Logger::log(LOG_LEVEL_INFO, "RX: No response");
                    }
                }


                if (m_responseContext.httpResponse.statusLine.statusCode == STATUS_OK)
                {
                    const std::string inputString(
                        reinterpret_cast<const char*>(m_pFullResponseDataPacket->data() + m_positionOfHttpBody),
                        m_responseContext.httpResponse.contentLength);
                    ERequestType requestType = eREQUEST_TYPE_UNDEFINED;

                    {
                        boost::upgrade_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
                        if (!m_allocatedSendRequestList.empty())
                        {
                            RequestTypeTuple& requestTuple = m_allocatedSendRequestList.front();
                            requestType = requestTuple.type;
                            {
                                //boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
                                requestTuple.state = RequestTypeTuple::eSENT_AND_REPLIED;
                            }
                        }
                        else
                        {
                            Logger::log(LOG_LEVEL_WARNING,
                                "Unexpected response received");
                        }
                    }

                    if (
                        (m_contentType == eCONTENT_TYPE_PLAIN_TEXT) &&
                        (!inputString.empty()) && (requestType == eREQUEST_TYPE_POST_RAW_DATA)
                       )
                    {
                        processPlainTextResponse(inputString);
                    }
                    //else do nothing


                    switch (requestType)
                    {
                        case eREQUEST_TYPE_POST_RAW_DATA:
                        {
                            notifyObservers(eLAST_RAW_DATA_HAS_BEEN_SENT);
                            break;
                        }

                        default:
                        {
                            //do nothing
                            break;
                        }
                    } //switch
                }
                else
                {
                    //Set this record as replied
                    ERequestType requestType = eREQUEST_TYPE_UNDEFINED;
                    {
                        boost::upgrade_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
                        if (!m_allocatedSendRequestList.empty())
                        {
                            RequestTypeTuple& requestTuple = m_allocatedSendRequestList.front();
                            requestType = requestTuple.type;
                            {
                                boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
                                requestTuple.state = RequestTypeTuple::eSENT_AND_REPLIED;
                            }
                        }
                        else
                        {
                            Logger::log(LOG_LEVEL_WARNING,
                                "Unexpected response received");
                        }
                    }

                    //Depending on the message/request type notify all interested parties
                    switch (requestType)
                    {
                        case eREQUEST_TYPE_POST_RAW_DATA:
                        {
                            notifyObservers(eLAST_RAW_DATA_HAS_FAILED);
                            break;
                        }

                        default:
                        {
                            //do nothing
                            break;
                        }
                    } //switch
                }
            }
            else
            {
                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
                {
                    //Wait for more data
                    Logger::log(LOG_LEVEL_DEBUG3,
                        "BrdfServerHTTPClient::receive() The received plain/text response is not full. Waiting for more data...");
                }
                //else do nothing

                return;
            }

            break;
        }
        default:
        {
            break;
        }
    }

    //Use persistent connection if the server responded with "Connection: keep-alive"
    {
        boost::upgrade_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
        if (!m_allocatedSendRequestList.empty())
        {
            RequestTypeTuple& requestTuple = m_allocatedSendRequestList.front();
            if (!requestTuple.usePersistentConnection)
            {
                std::string connectionString(m_responseContext.httpResponse.connection);
                boost::to_upper(connectionString);

                const char STRING_TO_SEARCH[] = "KEEP-ALIVE";
                connectionString = connectionString.substr(0, sizeof(STRING_TO_SEARCH) - 1);
                if (connectionString == STRING_TO_SEARCH)
                {
                    boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
                    requestTuple.usePersistentConnection = true;
                }
                //else do nothing
            }
            //else do nothing
        }
        //else do nothing
    }

    m_pLastResponseContext = &m_responseContext;


    //Update all interested parties
    notifyObservers(m_contentType);
    m_lastRequest.clear();
    m_lastResponse.clear();
    m_pFullResponseDataPacket.reset();
    m_pDecodedHttpTransferContents.reset();
    m_pLastResponseContext = 0;
    m_contentType = eCONTENT_TYPE_UNDEFINED;

    //Last check of the status code... The location of this snippet looks strange but the log should appear after th RX log.
    if (m_responseContext.httpResponse.statusLine.statusCode != STATUS_OK)
    {
        //The response is a valid http response but the status code is not OK. Discard and close connection

        //First report the problem to the user
        std::ostringstream ss;
        ss << "The last request to the InStation was not successful";

        if (m_responseContext.httpResponse.statusLine.statusCode != 0)
        {
            ss << ": STATUS CODE=" << m_responseContext.httpResponse.statusLine.statusCode
                << " (" << m_responseContext.httpResponse.statusLine.reasonPhrase << ")";
        }
        //else do nothing

        Logger::log(LOG_LEVEL_WARNING,
            ss.str().c_str());

        //Discard, close connection
        m_pTCPClient->closeConnection();
    }
    //else continue

    m_responseContext.reset();

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            "BrdfServerHTTPClient::receive() Processing of response completed");
    }
    //else do nothing


    m_eventSemaphore.release();
}

void BrdfServerHTTPClient::onReceive(FastDataPacket_shared_ptr& pPacket)
{
    if (!pPacket)
        return;

    //Log the just-received packet
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
    {
        pPacket->fixForDisplaying();
        if (pPacket)
        {
            const std::string description("Packet received (size " +
                boost::lexical_cast<std::string>(pPacket->size()) + " bytes):");
            Logger::log(LOG_LEVEL_DEBUG2,
                "BrdfServerHTTPClient::onReceive()",
                description.c_str(),
                pPacket->c_str());
        }
        //else do nothing
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_RECEIVE;
    pSignal->pReceiveData = pPacket;
    sendInternalSignal(pSignal);
}

void BrdfServerHTTPClient::onSend(const bool , FastDataPacket_shared_ptr& )
{
    Logger::log(LOG_LEVEL_EXCEPTION,
        "BrdfServerHTTPClient::onSend() This method should not be called");
}

void BrdfServerHTTPClient::onSend(const bool success, TSendDataPacket_shared_ptr& pData)
{
    if (!pData)
        return;

    if (success)
    {
        //Log the just-sent packet
        if (pData && pData->second != 0)
        {
            pData->second->fixForDisplaying();
            Logger::log(LOG_LEVEL_INFO,
                "TX:", pData->second->c_str());
        }
        //else do nothing
    }
    else
    {
        Logger::log(LOG_LEVEL_INFO,
            "TX:", "Error");
    }

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = success ? Signal::eON_SENT_SUCCESS : Signal::eON_SENT_FAILURE;
    pSignal->pSendData = pData;
    sendInternalSignal(pSignal);
}


void BrdfServerHTTPClient::processPlainTextResponse(const std::string& )
{
    //do nothing
}

void BrdfServerHTTPClient::onClose()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG2,
            "BrdfServerHTTPClient::onClose()");
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_CLOSE;
    sendInternalSignal(pSignal);
}

void BrdfServerHTTPClient::onBackoffTimerStarted()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            "BrdfServerHTTPClient::onBackoffTimerStarted");
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_BACKOFF_TIMER_STARTED;
    sendInternalSignal(pSignal);
}

void BrdfServerHTTPClient::onBackoffTimerExpired()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            "BrdfServerHTTPClient::onBackoffTimerExpired");
    }
    //else do nothing

    //Check again if there is something in the collection and if so send it
    boost::upgrade_lock<boost::shared_mutex> lock2(m_allocatedSendRequestListMutex);
    if (!m_allocatedSendRequestList.empty())
    {
        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock2);

        RequestTypeTuple& requestTuple = m_allocatedSendRequestList.front();
        requestTuple.state = RequestTypeTuple::eSUBMITTED_TO_SEND;
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_BACKOFF_TIMER_EXPIRED;
    sendInternalSignal(pSignal);
}

void BrdfServerHTTPClient::onConnect(const bool success)
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
    {
        Logger::log(LOG_LEVEL_DEBUG2,
            "BrdfServerHTTPClient::onConnect()",
            success ? "Success" : "Failure");
    }
    //else do nothing

    if (success)
    {
        //do nothing
    }
    else
    {
        Logger::log(LOG_LEVEL_ERROR,
            "The last request to the InStation was not successful");

        //Do not remove packets. Try to sent them as long as possible. The collection is protected by
        //counting its entries against overflow (sendRequest())


        notifyObservers(eLAST_RAW_DATA_HAS_FAILED);
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = success ? Signal::eON_CONNECT_SUCCESS : Signal::eON_CONNECT_FAILURE;
    sendInternalSignal(pSignal);
}

/**
  @brief Send an asynchronous request to the server containing current congestion report
  @param[in] reportTime time to be included in the report
  @param[in] congestionReport record containing information about the congestion to be reported
  @param[in] useHttpVersion1_1 if true the persistent HTTP connection (HTTP version 1.1) will be used.
  @param[in] shouldCloseConnectionAfterSending if true and HTTP v1.1 then after receipt of response
    the connection will not be automatically closed
*/
bool BrdfServerHTTPClient::sendRawData(
        const std::string& rawData,
        const bool useHttpVersion1_1,
        const bool shouldCloseConnectionAfterSending)
{
    if (m_uriRawDataPath.Host.empty())
    {
        Logger::log(LOG_LEVEL_ERROR,
            "BRDF Server URL value is empty. Raw data will not be sent");

        return false;
    }
    //else continue

    if (rawData.empty())
    {
        //Do nothing
        return false;
    }
    //else continue

    std::ostringstream header;
    std::ostringstream body;

    header << "POST ";

    if (m_uriRawDataPath.Path.empty() && m_uriRawDataPath.QueryString.empty())
        header << "/";
    else
        header << m_uriRawDataPath.Path << m_uriRawDataPath.QueryString;

    if (useHttpVersion1_1)
        header << " HTTP/1.1" EOL;
    else
        header << " HTTP/1.0" EOL;

    header <<
        "Host: " << m_pTCPClient->getRemoteAddress() << ":" << m_pTCPClient->getRemotePortNumber() << EOL;

    header << "Content-Type: text/plain" EOL;



    //Finalise the header by adding the content length
    header << "Content-Length: " << rawData.size() << EOL EOL;

    std::ostringstream& headerAndBody = header;
    headerAndBody << rawData;

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            "Sending RAW DATA to the InStation");
    }
    //else do nothing

    sendRequest(headerAndBody.str(), eREQUEST_TYPE_POST_RAW_DATA, useHttpVersion1_1, shouldCloseConnectionAfterSending);

    return true;
}


void BrdfServerHTTPClient::changeResponseTimeout(const int valueInSeconds)
{
    m_httpResponseTimeout = pt::seconds(static_cast<long>(valueInSeconds));

    std::ostringstream ss;
    ss << "Response timeout for the Brdf Server has been changed to "
        << m_httpResponseTimeout.total_seconds() << " seconds";
    Logger::log(LOG_LEVEL_DEBUG2,
        ss.str().c_str());
}

void BrdfServerHTTPClient::clearAllocatedSendRequestList()
{
    size_t numberOfItemsToClear;

    {
        numberOfItemsToClear = m_allocatedSendRequestList.size();
        m_allocatedSendRequestList.clear();
    }

    if (
        Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2) &&
        (numberOfItemsToClear > 0)
        )
    {
        std::ostringstream ss;
        ss << "The list of packets to be sent (" << numberOfItemsToClear << " items) has been cleared";
        Logger::log(LOG_LEVEL_DEBUG2,
            ss.str().c_str());
    }
    //else do nothing
}

void BrdfServerHTTPClient::sendRequest(
    const std::string& requestString,
    const ERequestType requestType,
    const bool usePersistentConnection,
    const bool shouldCloseConnectionAfterSending)
{
    //Protect the list against overflowing which may result with program crash
    {
        boost::shared_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
        TRequestTypeTupleList::iterator it = m_allocatedSendRequestList.begin();
        TRequestTypeTupleList::const_iterator itEnd = m_allocatedSendRequestList.end();
        while (it != itEnd)
        {
            if (m_allocatedSendRequestList.size() < m_allocatedSendRequestListMaxSize)
                break;

            // Remove only these records which are not currently being sent
            // (only the first record may be currently in the process of sending)
            if (it->state == RequestTypeTuple::eNOT_SENT)
            {
                FastDataPacket_shared_ptr pPacket = it->pPacket;
                m_allocatedSendRequestList.erase(it);

                Logger::log(LOG_LEVEL_WARNING,
                    "Overflow protection. Outdated packet has been removed",
                    (const char*)pPacket->data());
            }
            //else do nothing

            it++;
        }
    }

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            "BrdfServerHTTPClient::sendRequest() New packet added to the send queue");
    }
    //else do nothing

    RequestTypeTuple request;
    request.id = RequestTypeTuple::globalNextId++;
    request.pPacket = FastDataPacket_shared_ptr(new FastDataPacket(requestString));
    request.type = requestType;
    request.usePersistentConnection = usePersistentConnection;
    request.shouldCloseConnectionAfterSending = shouldCloseConnectionAfterSending;
    request.state = RequestTypeTuple::eNOT_SENT;
    request.timeWhenSubmitted = m_pClock->getUniversalTime();
    request.numberOfAttemptsToSent = 0;
    {
        boost::unique_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
        m_allocatedSendRequestList.push_back(request);
    }

//#ifdef TESTING
    static bool printTime = true;
    if (printTime)
    {
        boost::shared_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);

        std::ostringstream ss;
        ss << "BrdfServerHTTPClient::sendRequest() Number of items to be sent: " << m_allocatedSendRequestList.size();
        Logger::log(LOG_LEVEL_DEBUG2,
            ss.str().c_str());
    }
    //else do nothing
//#endif //TESTING

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_SOMETHING_TO_SENT_RECEIVED;
    sendInternalSignal(pSignal);
}

void BrdfServerHTTPClient::initialise()
{
    setState(eSTATE_WAITING_FOR_REQUEST, "initialise");
    m_eventSemaphore.reset();
}

void BrdfServerHTTPClient::perform()
{
    bool signalCollectionIsEmpty = true;
    {
        boost::unique_lock<boost::mutex> lockReceive(m_signalCollectionMutex);
        signalCollectionIsEmpty = m_signalCollection.empty();
    }

    if (signalCollectionIsEmpty &&
        !m_eventSemaphore.wait(SEMAPHORE_SLEEP_PERIOD_MS)) //i.e. no signal received
    {
        //do nothing
    }
    else
    {
        //Remove outstanding signals off the semaphore
        m_eventSemaphore.wait(0);
    }

    Signal_shared_ptr pSignal = extractInternalSignal();

    switch (m_requestState.get())
    {
        case eSTATE_WAITING_FOR_REQUEST:
        {
            //Ignore the signal

            {
                //Check if there is something in the collection and if so send it
                boost::upgrade_lock<boost::shared_mutex> lock2(m_allocatedSendRequestListMutex);
                if (m_allocatedSendRequestList.empty())
                    break;
            }

            if (!m_pTCPClient->isConnected())
            {
                m_pTCPClient->openConnection();
                setState(eSTATE_REQUEST_RECEIVED_AND_WAITING_FOR_CONNECTION, "perfom");
            }
            else
            {
                sendPacketOutOfCollection();
                setState(eSTATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE, "perfom");
            }

            break;
        }

        case eSTATE_REQUEST_RECEIVED_AND_WAITING_FOR_CONNECTION:
        {
            if (!pSignal)
                break;

            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
            {
                Logger::log(LOG_LEVEL_DEBUG3,
                    "BrdfServerHTTPClient::perform()::eSTATE_REQUEST_RECEIVED_AND_WAITING_FOR_CONNECTION, signal received",
                    Signal::TYPE_NAME[pSignal->signalType],
                    boost::lexical_cast<std::string>(pSignal->id).c_str());
            }
            //else do nothing

            if (pSignal->signalType == Signal::eON_CONNECT_SUCCESS)
            {
                setState(eSTATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE, "perfom");
                sendPacketOutOfCollection();
            }
            else if (pSignal->signalType == Signal::eON_CONNECT_FAILURE)
            {
                setState(eSTATE_WAITING_FOR_REQUEST, "perfom");
            }
            else
            {
                //do nothing
            }

            break;
        }

        case eSTATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE:
        {
            bool checkIfTimeoutOccurred = false;

            if (pSignal)
            {
                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
                {
                    Logger::log(LOG_LEVEL_DEBUG3,
                        "BrdfServerHTTPClient::perform()::eSTATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE, signal received",
                        Signal::TYPE_NAME[pSignal->signalType],
                        boost::lexical_cast<std::string>(pSignal->id).c_str());
                }
                //else do nothing

                if (pSignal->signalType == Signal::eON_SENT_SUCCESS)
                {
                    TSendDataPacket_shared_ptr& pData = pSignal->pSendData;
                    if (!pData)
                    {
                        Logger::log(LOG_LEVEL_EXCEPTION,
                            "BrdfServerHTTPClient::perform() Signal ON_SENT received but pSendData is empty");
                    }
                    //else continue

                    //Find the relevant packet and change its state
                    boost::upgrade_lock<boost::shared_mutex> lock2(m_allocatedSendRequestListMutex);
                    if (!m_allocatedSendRequestList.empty())
                    {
                        RequestTypeTuple& requestTuple = m_allocatedSendRequestList.front();
                        if (requestTuple.id == pData->first)
                        {
                            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock2);
                            requestTuple.state = RequestTypeTuple::eSENT_BUT_NOT_REPLIED;
                        }
                        else
                        {
                            Logger::log(LOG_LEVEL_EXCEPTION,
                                "BrdfServerHTTPClient::perform() Identifier not found (1)");

                            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock2);
                            clearAllocatedSendRequestList();
                        }
                    }
                    else
                    {
                        Logger::log(LOG_LEVEL_EXCEPTION,
                            "BrdfServerHTTPClient::perform() Identifier not found (2)");

                        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock2);
                        clearAllocatedSendRequestList();
                    }
                }
                else if (pSignal->signalType == Signal::eON_RECEIVE)
                {
                    processReceivedPacket(pSignal->pReceiveData);

                    boost::upgrade_lock<boost::shared_mutex> lock2(m_allocatedSendRequestListMutex);
                    if (!m_allocatedSendRequestList.empty())
                    {
                        RequestTypeTuple& lastRequestedTuple = m_allocatedSendRequestList.front();
                        if (lastRequestedTuple.state == RequestTypeTuple::eSENT_AND_REPLIED)
                        {
                            //We request a unique lock to the list
                            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock2);
                            m_allocatedSendRequestList.pop_front();

                            //Send a signal, so that we process eSTATE_WAITING_FOR_REQUEST without waiting
                            Signal_shared_ptr _pSignal(new Signal());
                            _pSignal->signalType = Signal::eDUMMY_SIGNAL;
                            sendInternalSignal(_pSignal);

                            setState(eSTATE_WAITING_FOR_REQUEST, "perfom");
                        }
                        else if (lastRequestedTuple.state == RequestTypeTuple::eSENT_BUT_FAILED)
                        {
                            m_pTCPClient->cancelConnection();
                            setState(eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY, "perfom");
                        }
                        else
                        {
                            //do nothing, wait for more data
                        }
                    }
                    else
                    {
                        Logger::log(LOG_LEVEL_WARNING,
                            "BrdfServerHTTPClient::perform() Signal eON_RECEIVE received but allocatedSendRequestList is empty");
                    }
                }
                else if (
                    (pSignal->signalType == Signal::eON_CLOSE) ||
                    (pSignal->signalType == Signal::eON_BACKOFF_TIMER_EXPIRED)
                    )
                {
                    //Send a signal, so that we process eSTATE_WAITING_FOR_REQUEST without waiting
                    Signal_shared_ptr _pSignal(new Signal());
                    _pSignal->signalType = Signal::eDUMMY_SIGNAL;
                    sendInternalSignal(_pSignal);

                    setState(eSTATE_WAITING_FOR_REQUEST, "perfom");
                }
                else if (pSignal->signalType == Signal::eON_SENT_FAILURE)
                {
                    m_pTCPClient->cancelConnection();
                    setState(eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY, "perfom");
                }
                else if (pSignal->signalType == Signal::eON_BACKOFF_TIMER_STARTED)
                {
                    //Check again if there is something in the collection and if so send it
                    boost::upgrade_lock<boost::shared_mutex> lock2(m_allocatedSendRequestListMutex);
                    if (!m_allocatedSendRequestList.empty())
                    {
                        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock2);

                        RequestTypeTuple& requestTuple = m_allocatedSendRequestList.front();
                        requestTuple.state = RequestTypeTuple::eSENT_BUT_FAILED;
                    }
                    //else do nothing


                    //Send a signal, so that we process eSTATE_WAITING_FOR_REQUEST without waiting
                    Signal_shared_ptr _pSignal(new Signal());
                    _pSignal->signalType = Signal::eDUMMY_SIGNAL;
                    sendInternalSignal(_pSignal);

                    setState(eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY, "perfom");
                }
                else
                {
                    checkIfTimeoutOccurred = true;
                }
            }
            else
            {
                checkIfTimeoutOccurred = true;
            }

            if (checkIfTimeoutOccurred)
            {
                //Check if timeout has not occurred
                boost::upgrade_lock<boost::shared_mutex> lock2(m_allocatedSendRequestListMutex);
                if (!m_allocatedSendRequestList.empty())
                {
                    RequestTypeTuple& lastRequestedTuple = m_allocatedSendRequestList.front();
                    //Check if timeout has not occurred
                    const TTime_t CURRENT_TIME(m_pClock->getUniversalTime());
                    const TTimeDiff_t TIME_SINCE_LAST_SENDING = CURRENT_TIME - lastRequestedTuple.timeWhenSent;
                    if (TIME_SINCE_LAST_SENDING > m_httpResponseTimeout)
                    {
                        //Check if max number of send attempts has not been exceeded
                        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock2);
                        if (lastRequestedTuple.numberOfAttemptsToSent++ <= m_httpSendMaxAttemptNumber)
                        {
                            Logger::log(LOG_LEVEL_WARNING,
                                "The last request has been timed out. Retrying...");

                            //The packet will be automaticall resent
                        }
                        else
                        {
                            Logger::log(LOG_LEVEL_ERROR,
                                "The last request has been timed out. Aborting");

                            //Remove the packet to be sent off the list
                            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_NOTICE)) //Reduce overhead if log level not suitable
                            {
                                std::ostringstream ss;
                                ss << "Aborting sending of the packet:\n"
                                    << (const char*)lastRequestedTuple.pPacket->data();
                                Logger::log(LOG_LEVEL_NOTICE,
                                    ss.str().c_str());
                            }
                            //else do not log

                            m_allocatedSendRequestList.pop_front(); //remove the aborted packet from the list
                            notifyObservers(eLAST_RAW_DATA_HAS_FAILED);
                        }

                        m_pTCPClient->cancelConnection();
                        setState(eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY, "perfom");
                    }
                    //else do nothing
                }
                else
                {
                    //do nothing
                }
            }
            //else do nothing

            break;
        }

        case eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY:
        {
            if (!pSignal)
                break;

            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
            {
                Logger::log(LOG_LEVEL_DEBUG3,
                    "BrdfServerHTTPClient::perform()::eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY, signal received",
                    Signal::TYPE_NAME[pSignal->signalType],
                    boost::lexical_cast<std::string>(pSignal->id).c_str());
            }
            //else do nothing

            if (pSignal->signalType == Signal::eON_BACKOFF_TIMER_EXPIRED)
            {
                //Send a signal, so that we process eSTATE_WAITING_FOR_REQUEST without waiting
                Signal_shared_ptr pSignal(new Signal());
                pSignal->signalType = Signal::eDUMMY_SIGNAL;
                sendInternalSignal(pSignal);

                setState(eSTATE_WAITING_FOR_REQUEST, "perfom");
            }
            //else do nothing

            break;
        }

        case eSTATE_STOPPED:
        {
            m_eventSemaphore.wait(SLEEP_TIME_IN_MS_WHEN_STOPPED);
            break;
        }

        default:
        {
            break;
        }
    }
}

void BrdfServerHTTPClient::sendPacketOutOfCollection()
{
    boost::upgrade_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
    if (!m_allocatedSendRequestList.empty())
    {
        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);

        //Sent the packet
        RequestTypeTuple& requestTuple = m_allocatedSendRequestList.front();
        TSendDataPacket_shared_ptr pRequestDataPair(
            new TSendDataPacket(requestTuple.id, requestTuple.pPacket));
        m_pTCPClient->send(pRequestDataPair, requestTuple.shouldCloseConnectionAfterSending);

        requestTuple.state = RequestTypeTuple::eSUBMITTED_TO_SEND;
        requestTuple.timeWhenSent = m_pClock->getUniversalTime();
        requestTuple.numberOfAttemptsToSent++;
    }
    //else do nothing
}

void BrdfServerHTTPClient::shutdown(const char* )
{
    boost::unique_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
    m_allocatedSendRequestList.clear();

    //If the main loop (perfom()) is waiting on any of the semaphores release it to avoid waiting
    m_eventSemaphore.reset();

    stop();
}

void BrdfServerHTTPClient::stop()
{
    setState(eSTATE_STOPPED, "stop");
    m_eventSemaphore.reset();
}

void BrdfServerHTTPClient::sendStopSignal(void* pCtx)
{
    BrdfServerHTTPClient* _pCtx = (BrdfServerHTTPClient*)pCtx;

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eSTOP;
    _pCtx->sendInternalSignal(pSignal);
}

void BrdfServerHTTPClient::setState(const ERequestState state, const char* functionName)
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        assert(static_cast<size_t>(state) < eSTATE_SIZE);
        assert(static_cast<size_t>(m_requestState.get()) < eSTATE_SIZE);

        std::ostringstream ss;
        ss << MODULE_NAME << "::" << functionName << "() ----------- "
               "State change to " << STATE_NAME[static_cast<size_t>(state)]
            << " (from " << STATE_NAME[static_cast<size_t>(m_requestState.get())] << ")";

        Logger::log(LOG_LEVEL_DEBUG3,
            ss.str().c_str());
    }
    //else do nothing

    m_requestState = state;
}

const char* BrdfServerHTTPClient::STATE_NAME[eSTATE_SIZE] =
{
    "STATE_UNKNOWN",
    "STATE_WAITING_FOR_REQUEST",
    "STATE_REQUEST_RECEIVED_AND_WAITING_FOR_CONNECTION",
    "STATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE",
    "STATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY",
    "STATE_STOPPED"
};

int BrdfServerHTTPClient::Signal::totalId = 0;

const char* BrdfServerHTTPClient::Signal::TYPE_NAME[eSIGNAL_TYPE_SIZE] =
{
    "UNKNOWN",
    "ON_CONNECT_SUCCESS",
    "ON_CONNECT_FAILURE",
    "ON_BACKOFF_TIMER_STARTED",
    "ON_BACKOFF_TIMER_EXPIRED",
    "ON_SOMETHING_TO_SENT_RECEIVED",
    "ON_SENT_SUCCESS",
    "ON_SENT_FAILURE",
    "ON_RECEIVE",
    "ON_CLOSE",
    "eSTOP",
    "DUMMY_SIGNAL"
};


void BrdfServerHTTPClient::sendInternalSignal(Signal_shared_ptr pSignal)
{
    {
        boost::unique_lock<boost::mutex> lockReceive(m_signalCollectionMutex);
        pSignal->id = Signal::totalId++;
        m_signalCollection.push_back(pSignal);

        if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
        {
            Logger::log(LOG_LEVEL_DEBUG3,
                "BrdfServerHTTPClient::sendInternalSignal(), signal emitted",
                Signal::TYPE_NAME[pSignal->signalType],
                boost::lexical_cast<std::string>(pSignal->id).c_str());
        }
        //else do nothing
    }

    m_eventSemaphore.release();
}

BrdfServerHTTPClient::Signal_shared_ptr BrdfServerHTTPClient::extractInternalSignal()
{
    Signal_shared_ptr pSignal;

    boost::unique_lock<boost::mutex> lockReceive(m_signalCollectionMutex);
    if (!m_signalCollection.empty()) //Due to multithreading this list may get empty on connection reset
    {
        pSignal = m_signalCollection.front();
        m_signalCollection.pop_front();

        if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
        {
            Logger::log(LOG_LEVEL_DEBUG3,
                "BrdfServerHTTPClient::extractInternalSignal(), signal received",
                Signal::TYPE_NAME[pSignal->signalType],
                boost::lexical_cast<std::string>(pSignal->id).c_str());
        }
        //else do nothing
    }
    //else do nothing

    //Display all stored signals from signalCollection
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        if (m_signalCollection.empty())
        {
            //Logger::log(LOG_LEVEL_DEBUG3,
            //    "BrdfServerHTTPClient::extractInternalSignal() checking queue, signal queue EMPTY");
        }
        else
        {
            for (std::list<Signal_shared_ptr>::const_iterator it=m_signalCollection.begin(), itEnd=m_signalCollection.end();
                it != itEnd;
                it++)
            {
                Logger::log(LOG_LEVEL_DEBUG3,
                    "BrdfServerHTTPClient::extractInternalSignal() checking queue, signal in the queue",
                    Signal::TYPE_NAME[(*it)->signalType],
                    boost::lexical_cast<std::string>((*it)->id).c_str());
            }
            //else do nothing
        }
    }
    //else do nothing

    return pSignal;
}


#ifdef TESTING

BrdfServerHTTPClient::TRequestTypeTupleList& BrdfServerHTTPClient::getAllocatedSendRequestList()
{
    return m_allocatedSendRequestList;
}

const FastDataPacket_shared_ptr& BrdfServerHTTPClient::getFullResponseDataPacket() const
{
    return m_pFullResponseDataPacket;
}

void BrdfServerHTTPClient::test_processReceivedPacket(FastDataPacket_shared_ptr& pPacket)
{
    processReceivedPacket(pPacket);
}

BrdfServerHTTPClient::ERequestState BrdfServerHTTPClient::getRequestState() const
{
    return m_requestState.get();
}

void BrdfServerHTTPClient::sendRequest__testing(
    const std::string& requestString,
    const BrdfServerHTTPClient::ERequestType requestType,
    const bool usePersistentConnection,
    const bool shouldCloseConnectionAfterSending)
{
    sendRequest(
        requestString,
        requestType,
        usePersistentConnection,
        shouldCloseConnectionAfterSending);
}

#endif


int BrdfServerHTTPClient::RequestTypeTuple::globalNextId = 0;

} //namespace
