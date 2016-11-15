#include "stdafx.h"
#include "instationhttpclient.h"

#include "activeboostasiotcpclient.h"
#include "bluetooth/bluetooth_utils.h"
#include "clock.h"
#include "datacontainer.h"
#include "fault.h"
#include "httpresponseparser.h"
#include "icoreconfiguration.h"
#include "iinstationdatacontainer.h"
#include "iinstationreporter.h"
#include "iniconfiguration.h"
#include "iseedconfiguration.h"
#include "lock.h"
#include "logger.h"
#include "model.h"
#include "os_utilities.h"
#include "queuedetector.h"
#include "responsebodyparser.h"
#include "signaturegenerator.h"
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

    const unsigned char CRLF[] = {0x0D, 0x0A};

    const int STATUS_OK = 200;
    const int MAX_NUMBER_OF_DISPATCHES = 3;


    const unsigned char CR = 0xD;
    const unsigned char LF = 0xA;

    const unsigned int MAX_NUMBER_OF_CONNECT_ATTEMPTS = 3;

    const char MODULE_NAME[] = "InStationHTTPClient";

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

namespace InStation
{


InStationHTTPClient::InStationHTTPClient(
    const Model::ICoreConfiguration& coreConfiguration,
    Model::ISeedConfiguration* pSeedConfiguration,
    Model::IConnectionProducerClient* pTCPClient,
    ISignatureGenerator* pSignatureGenerator,
    ::Clock* pClock,
    const int primaryId,
    Model::Fault* pPrimaryCommunicationFault,
    Model::Fault* pPrimaryResponseNotOKFault,
    Model::Fault* pPrimaryResponseMessageBodyErrorFault)
:
Model::IConnectionConsumer(),
::ITask(),
::Identifiable<int>(primaryId),
m_identityCollection(),
m_identifierCollectionMutex(),
m_pTCPClient(pTCPClient),
m_pDataContainer(),
m_coreConfiguration(coreConfiguration),
m_pSeedConfiguration(pSeedConfiguration),
m_pIniConfiguration(),
m_pInStationReporter(),
m_pSignatureGenerator(pSignatureGenerator),
m_signatureGeneratorMutex(),
m_pClock(pClock),
m_lastRequest(),
m_lastResponse(),
m_pLastResponseContext(0),
m_allocatedSendRequestList(),
m_allocatedSendRequestListMutex(),
m_allocatedSendRequestListMaxSize(1000),
m_eventSemaphore("m_eventSemaphore", 0x1000),
m_remoteSSHPortNumber(0),
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
m_httpResponseTimeout(bc::seconds(15)),
m_httpSendMaxAttemptNumber(3),
m_rawDeviceDetectionPath(),
m_uriRawDeviceDetectionPath(),
m_congestionReportsPath(),
m_uriCongestionReportsPath(),
m_alertAndStatusReportsPath(),
m_uriAlertAndStatusReportsPath(),
m_shouldInformAboutAlertAndStatus(true),
m_statusReportsPath(),
m_uriStatusReportsPath(),
m_faultReportsPath(),
m_uriFaultReportsPath(),
m_statisticsReportsPath(),
m_uriStatisticsReportsPath(),
m_pSsFacet(new pt::time_facet("%a, %d %b %Y %H:%M:%S%f %ZP")),
m_ssLocale(std::locale("C"), m_pSsFacet),
m_outStationInstationTimeFacet(new pt::time_facet("%Y-%m-%d %H:%M:%S")),
m_outStationInstationLocale(std::locale("C"), m_outStationInstationTimeFacet),
HEADER_USER_AGENT_STR("User-agent: " + Version::getApplicationNameWithoutSpaces() + "/" + Version::getNumber() + " (" + Version::getDate() + ")"),
m_signalCollectionMutex(),
m_signalCollection()
{
    assert(m_pTCPClient != 0);

    {
        ::Lock lock(m_identifierCollectionMutex);
        m_identityCollection.push_back(
            TIdentity(primaryId, pPrimaryCommunicationFault, pPrimaryResponseNotOKFault, pPrimaryResponseMessageBodyErrorFault));
    }

    if (m_pSeedConfiguration != 0)
    {
        ::Lock lock(m_signatureGeneratorMutex);
        m_pSignatureGenerator->setSeed(m_pSeedConfiguration->getValue());
    }
    //else do nothing
}

InStationHTTPClient::~InStationHTTPClient()
{
    //Do not explicitly delete p_ss_facet. The delete is implicit in the destruction of special_locale.
    //std::locale takes ownership of the facet.
    //See http://rhubbarb.wordpress.com/2009/10/17/boost-datetime-locales-and-facets/
}

void InStationHTTPClient::setup(boost::shared_ptr<Model::DataContainer> pDataContainer)
{
    m_pDataContainer = pDataContainer;
}

void InStationHTTPClient::setup(boost::shared_ptr<Model::IniConfiguration> pIniConfiguration)
{
    m_pIniConfiguration = pIniConfiguration;

    if (pIniConfiguration != 0)
    {
        {
            //Check http timeout value
            int64_t httpResponseTimeout = 0;
            bool found = false;

            if (m_coreConfiguration.getMajorCoreConfigurationVersion()>=4)
            {
                found = pIniConfiguration->getValueInt64(Model::eHTTP_RESPONSE_TIMEOUT_IN_SECONDS, httpResponseTimeout);
            }
            else
            {
                found = pIniConfiguration->getValueInt64(Model::eHTTP_TIMEOUT, httpResponseTimeout);
            }

            if (httpResponseTimeout <= 0)
            {
                httpResponseTimeout = 30; //default value
            }
            //else do nothing

            changeResponseTimeout(static_cast<int>(httpResponseTimeout));

            if (!found)
            {
                Logger::log(LOG_LEVEL_EXCEPTION,
                    getIdentifierName(),
                    "InStationHTTPClient::setup()",
                    "eHTTP_TIMEOUT entry not found in configuration");
            }
            //else do nothing
        }

        {
            int64_t allocatedSendRequestListMaxSize = 1000;
            bool found = m_pIniConfiguration->getValueInt64(
                Model::eREPORT_STORAGE_CAPACITY, allocatedSendRequestListMaxSize);
            m_allocatedSendRequestListMaxSize = allocatedSendRequestListMaxSize;
            if (!found)
            {
                Logger::log(LOG_LEVEL_EXCEPTION,
                    getIdentifierName(),
                    "InStationHTTPClient::setup()",
                    "eREPORT_STORAGE_CAPACITY entry not found in configuration");
            }
            //else do nothing
        }

        {
            bool found = m_pIniConfiguration->getValueString(
                Model::eURL_JOURNEY_TIMES_REPORTING, m_rawDeviceDetectionPath);
            m_uriRawDeviceDetectionPath = Uri::parse(m_rawDeviceDetectionPath);
            if (!found)
            {
                Logger::log(LOG_LEVEL_EXCEPTION,
                    getIdentifierName(),
                    "InStationHTTPClient::setup()",
                    "eURL_JOURNEY_TIMES_REPORTING entry not found in configuration");
            }
            //else do nothing
        }

        {
            if (m_coreConfiguration.getMajorCoreConfigurationVersion()>=4)
            {
                bool found = m_pIniConfiguration->getValueString(
                    Model::eURL_CONGESTION_REPORTS, m_congestionReportsPath);
                m_uriCongestionReportsPath = Uri::parse(m_congestionReportsPath);
                if (!found)
                {
                    Logger::log(LOG_LEVEL_EXCEPTION,
                        getIdentifierName(),
                        "InStationHTTPClient::setup()",
                        "eURL_CONGESTION_REPORTS entry not found in configuration");
                }
                //else do nothing
            }
            else
            {
                bool found = m_pIniConfiguration->getValueString(
                    Model::eURL_CONGESTION_REPORTING, m_congestionReportsPath);
                m_uriCongestionReportsPath = Uri::parse(m_congestionReportsPath);
                if (!found)
                {
                    Logger::log(
                        LOG_LEVEL_EXCEPTION,
                        getIdentifierName(),
                        "InStationHTTPClient::setup()",
                        "eURL_CONGESTION_REPORTING entry not found in configuration");
                }
                //else do nothing
            }
        }

        {
            bool found = m_pIniConfiguration->getValueString(
                Model::eURL_ALERT_AND_STATUS_REPORTS, m_alertAndStatusReportsPath);
            m_uriAlertAndStatusReportsPath = Uri::parse(m_alertAndStatusReportsPath);
            if (!found)
            {
                Logger::log(LOG_LEVEL_EXCEPTION,
                    getIdentifierName(),
                    "InStationHTTPClient::setup()",
                    "eURL_ALERT_AND_STATUS_REPORTS entry not found in configuration");
            }
            //else do nothing
        }

        {
            bool found = m_pIniConfiguration->getValueString(
                Model::eURL_STATUS_REPORTS, m_statusReportsPath);
            m_uriStatusReportsPath = Uri::parse(m_statusReportsPath);
            if (!found)
            {
                Logger::log(LOG_LEVEL_EXCEPTION,
                    getIdentifierName(),
                    "InStationHTTPClient::setup()",
                    "eURL_STATUS_REPORTS entry not found in configuration");
            }
            //else do nothing
        }

        {
            bool found = m_pIniConfiguration->getValueString(
                Model::eURL_FAULT_REPORTS, m_faultReportsPath);
            m_uriFaultReportsPath = Uri::parse(m_faultReportsPath);
            if (!found)
            {
                Logger::log(LOG_LEVEL_EXCEPTION,
                    getIdentifierName(),
                    "InStationHTTPClient::setup()",
                    "eURL_FAULT_REPORTS entry not found in configuration");
            }
            //else do nothing
        }

        {
            bool found = m_pIniConfiguration->getValueString(
                Model::eURL_STATISTICS_REPORTS, m_statisticsReportsPath);
            m_uriStatisticsReportsPath = Uri::parse(m_statisticsReportsPath);
            if (!found)
            {
                Logger::log(LOG_LEVEL_EXCEPTION,
                    getIdentifierName(),
                    "InStationHTTPClient::setup()",
                    "eURL_STATISTICS_REPORTS entry not found in configuration");
            }
            //else do nothing
        }

    }
    //else do nothing
}

void InStationHTTPClient::setup(boost::shared_ptr<IInStationReporter> pInStationReporter)
{
    m_pInStationReporter = pInStationReporter;
}

void InStationHTTPClient::addIdentifier(
    const int id,
    Model::Fault* pCommunicationFault,
    Model::Fault* pResponseNotOKFault,
    Model::Fault* pResponseMessageBodyErrorFault)
{
    Identifiable<int>::addIdentifier(id);

    ::Lock lock(m_identifierCollectionMutex);
    m_identityCollection.push_back(
        TIdentity(id, pCommunicationFault, pResponseNotOKFault, pResponseMessageBodyErrorFault));
}

void InStationHTTPClient::removeIdentifier(const int id)
{
    Identifiable<int>::removeIdentifier(id);

    ::Lock lock(m_identifierCollectionMutex);
    for (TIdentityCollection::iterator iter(m_identityCollection.begin()), iterEnd(m_identityCollection.end());
        iter != iterEnd;
        ++iter)
    {
        if (iter->identifier == id)
        {
            m_identityCollection.erase(iter);
            break;
        }
        //else do nothing
    }
}


bool InStationHTTPClient::isFull() const
{
    return false;
}

void InStationHTTPClient::processReceivedPacket(FastDataPacket_shared_ptr& packet)
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

    assert(getNumberOfIdentifiers() > 0);

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
                ss << "InStationHTTPClient::receive() Starting new packet of size " << packet->size();
                Logger::log(LOG_LEVEL_DEBUG3,
                    getIdentifierName(),
                    ss.str().c_str());
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
                ss << "InStationHTTPClient::receive() Another part of packet received of size " << packet->size();
                Logger::log(LOG_LEVEL_DEBUG3,
                    getIdentifierName(),
                    ss.str().c_str());
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
                        getIdentifierName(),
                        "InStationHTTPClient::receive() The http response has been received but there is no empty line in. Waiting for more data...");
                }
                //else do nothing

                return;
            }
            //else continue

            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
            {
                Logger::log(LOG_LEVEL_DEBUG3,
                    getIdentifierName(),
                    "InStationHTTPClient::receive() Full HTTP header received");
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
                Logger::log(LOG_LEVEL_ERROR,
                    getIdentifierName(), ss.str().c_str());

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
                Logger::log(LOG_LEVEL_ERROR,
                    getIdentifierName(), ss.str().c_str());

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


    m_pLastResponseContext = &m_responseContext;

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
                        Logger::log(LOG_LEVEL_INFO,
                            getIdentifierName(), "RX:", m_lastResponse.c_str());
                    }
                    else
                    {
                        Logger::log(LOG_LEVEL_INFO,
                            getIdentifierName(), "RX: No response");
                    }
                }


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
                            getIdentifierName(), "Unexpected response received");
                    }
                }


                if (m_responseContext.httpResponse.statusLine.statusCode == STATUS_OK)
                {
                    const std::string inputString(
                        reinterpret_cast<const char*>(m_pFullResponseDataPacket->data() + m_positionOfHttpBody),
                        m_responseContext.httpResponse.contentLength);

                    if (
                        (m_contentType == eCONTENT_TYPE_PLAIN_TEXT) &&
                        (!inputString.empty()) &&
                        (
                            (requestType == eREQUEST_TYPE_POST_CONGESTION_REPORT) ||
                            (requestType == eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION) ||
                            (requestType == eREQUEST_TYPE_POST_ALARM_AND_STATUS_REPORT) ||
                            (requestType == eREQUEST_TYPE_POST_STATUS_REPORT) ||
                            (requestType == eREQUEST_TYPE_POST_FAULT_REPORT) ||
                            (requestType == eREQUEST_TYPE_POST_STATISTICS_REPORT)
                        )
                       )
                    {
                        processPlainTextResponse(inputString);
                    }
                    //else do nothing

                    //Clear Response not OK faults
                    bool anyFaultHasBeenCleared = false;
                    {
                        ::Lock lock(m_identifierCollectionMutex);
                        for (TIdentityCollection::const_iterator iter(m_identityCollection.begin()), iterEnd(m_identityCollection.end());
                            iter != iterEnd;
                            ++iter)
                        {
                            if (
                                (
                                    ((requestType == eREQUEST_TYPE_POST_CONGESTION_REPORT) && (iter->identifier == CONGESTION_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION) && (iter->identifier == RAW_DEVICE_DETECTION_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_ALARM_AND_STATUS_REPORT) && (iter->identifier == ALERT_AND_STATUS_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_STATUS_REPORT) && (iter->identifier == STATUS_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_FAULT_REPORT) && (iter->identifier == FAULT_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_STATISTICS_REPORT) && (iter->identifier == STATISTICS_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_GET_CONFIGURATION) && (iter->identifier == RETRIEVE_CONFIGURATION_CLIENT_IDENTIFIER))
                                ) &&
                                ((iter->pResponseNotOKFault != 0) && (iter->pResponseNotOKFault->get()))
                                )
                            {
                                iter->pResponseNotOKFault->clear();
                                anyFaultHasBeenCleared = true;
                                break;
                            }
                            //else do nothing
                        }
                    }

                    if (anyFaultHasBeenCleared)
                        notifyObservers(eSEND_FAULT_REPORT);


                    switch (requestType)
                    {
                        case eREQUEST_TYPE_POST_CONGESTION_REPORT:
                        {
                            notifyObservers(eLAST_CONGESTION_REPORT_HAS_BEEN_SENT);
                            break;
                        }
                        case eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION:
                        {
                            notifyObservers(eLAST_RAW_DEVICE_DETECTION_HAS_BEEN_SENT);
                            break;
                        }
                        case eREQUEST_TYPE_POST_ALARM_AND_STATUS_REPORT:
                        {
                            notifyObservers(eLAST_ALARM_AND_STATUS_REPORT_HAS_BEEN_SENT);
                            break;
                        }
                        case eREQUEST_TYPE_POST_STATUS_REPORT:
                        {
                            notifyObservers(eLAST_STATUS_REPORT_HAS_BEEN_SENT);
                            break;
                        }
                        case eREQUEST_TYPE_POST_FAULT_REPORT:
                        {
                            notifyObservers(eLAST_FAULT_REPORT_HAS_BEEN_SENT);
                            break;
                        }
                        case eREQUEST_TYPE_POST_STATISTICS_REPORT:
                        {
                            notifyObservers(eLAST_STATISTICS_REPORT_HAS_BEEN_SENT);
                            break;
                        }
                        case eREQUEST_TYPE_GET_CONFIGURATION:
                        {
                            notifyObservers(eLAST_CONFIGURATION_REQUEST_HAS_SUCCEDED);
                            processNewConfiguration(inputString);
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
                    //Set Response not OK faults
                    bool anyFaultHasBeenSet = false;
                    {
                        ::Lock lock(m_identifierCollectionMutex);
                        for (TIdentityCollection::const_iterator iter(m_identityCollection.begin()), iterEnd(m_identityCollection.end());
                            iter != iterEnd;
                            ++iter)
                        {
                            if (
                                (
                                    ((requestType == eREQUEST_TYPE_POST_CONGESTION_REPORT) && (iter->identifier == CONGESTION_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION) && (iter->identifier == RAW_DEVICE_DETECTION_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_ALARM_AND_STATUS_REPORT) && (iter->identifier == ALERT_AND_STATUS_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_STATUS_REPORT) && (iter->identifier == STATUS_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_FAULT_REPORT) && (iter->identifier == FAULT_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_POST_STATISTICS_REPORT) && (iter->identifier == STATISTICS_REPORTING_CLIENT_IDENTIFIER)) ||
                                    ((requestType == eREQUEST_TYPE_GET_CONFIGURATION) && (iter->identifier == RETRIEVE_CONFIGURATION_CLIENT_IDENTIFIER))
                                ) &&
                                ((iter->pResponseNotOKFault != 0) && (!iter->pResponseNotOKFault->get()))
                                )
                            {
                                iter->pResponseNotOKFault->set();
                                anyFaultHasBeenSet = true;
                                break;
                            }
                            //else do nothing
                        }
                    }

                    //Inform whoever may be interested
                    if (anyFaultHasBeenSet)
                        notifyObservers(eSEND_FAULT_REPORT);


                    switch (requestType)
                    {
                        case eREQUEST_TYPE_POST_CONGESTION_REPORT:
                        {
                            notifyObservers(eLAST_CONGESTION_REPORT_HAS_FAILED);
                            break;
                        }
                        case eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION:
                        {
                            notifyObservers(eLAST_RAW_DEVICE_DETECTION_HAS_FAILED);
                            break;
                        }
                        case eREQUEST_TYPE_POST_ALARM_AND_STATUS_REPORT:
                        {
                            notifyObservers(eLAST_ALARM_AND_STATUS_REPORT_HAS_FAILED);
                            break;
                        }
                        case eREQUEST_TYPE_POST_STATUS_REPORT:
                        {
                            notifyObservers(eLAST_STATUS_REPORT_HAS_FAILED);
                            break;
                        }
                        case eREQUEST_TYPE_POST_FAULT_REPORT:
                        {
                            notifyObservers(eLAST_FAULT_REPORT_HAS_FAILED);
                            break;
                        }
                        case eREQUEST_TYPE_POST_STATISTICS_REPORT:
                        {
                            notifyObservers(eLAST_STATISTICS_REPORT_HAS_FAILED);
                            break;
                        }
                        case eREQUEST_TYPE_GET_CONFIGURATION:
                        {
                            notifyObservers(eLAST_CONFIGURATION_REQUEST_HAS_FAILED);
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
                        getIdentifierName(),
                        "InStationHTTPClient::receive() The received plain/text response is not full. Waiting for more data...");
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
            getIdentifierName(), ss.str().c_str());

        //Discard, close connection
        m_pTCPClient->closeConnection();
    }
    //else continue

    m_responseContext.reset();

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "InStationHTTPClient::receive() Processing of response completed");
    }
    //else do nothing

    //Clear all faults
    bool anyFaultHasBeenCleared = false;
    {
        ::Lock lock(m_identifierCollectionMutex);
        for (TIdentityCollection::const_iterator iter(m_identityCollection.begin()), iterEnd(m_identityCollection.end());
            iter != iterEnd;
            ++iter)
        {
            if ((iter->pCommunicationFault != 0) && (iter->pCommunicationFault->get()))
            {
                iter->pCommunicationFault->clear();
                anyFaultHasBeenCleared = true;
            }
            //else do nothing
        }
    }

    if (anyFaultHasBeenCleared)
        notifyObservers(eSEND_FAULT_REPORT);


    m_eventSemaphore.release();
}

void InStationHTTPClient::onReceive(FastDataPacket_shared_ptr& pPacket)
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
                getIdentifierName(),
                "InStationHTTPClient::onReceive()",
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

void InStationHTTPClient::onSend(const bool , FastDataPacket_shared_ptr& )
{
    Logger::log(LOG_LEVEL_EXCEPTION,
        getIdentifierName(),
        "InStationHTTPClient::onSend() This method should not be called");
}

void InStationHTTPClient::onSend(const bool success, TSendDataPacket_shared_ptr& pData)
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
                getIdentifierName(), "TX:", pData->second->c_str());
        }
        //else do nothing
    }
    else
    {
        Logger::log(LOG_LEVEL_INFO,
            getIdentifierName(), "TX:", "Error");
    }

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = success ? Signal::eON_SENT_SUCCESS : Signal::eON_SENT_FAILURE;
    pSignal->pSendData = pData;
    sendInternalSignal(pSignal);
}


void InStationHTTPClient::processNewConfiguration(const std::string& inputString)
{
    boost::shared_ptr<Model::IniConfiguration> pIniConfiguration(new Model::IniConfiguration(inputString));
    pIniConfiguration->overwriteValuesWithCommandLineParameters(
        m_coreConfiguration.getCommandLineStatusReportURL());

    Model::IniConfiguration& iniConfiguration = *pIniConfiguration;

    if (iniConfiguration.isValid())
    {
        iniConfiguration.saveToFile();

        //Clear configuration fault if set
        Model::Fault& configurationFileFault = m_pDataContainer->getFunctionalConfigurationSyntaxFault();
        if (configurationFileFault.get())
        {
            configurationFileFault.clear();
            notifyObservers(eSEND_FAULT_REPORT);
        }
        //else do nothing

        if (!iniConfiguration.isParameterErrorSet())
        {
            //Clear functional configuration parameter fault if set
            Model::Fault& configurationParameterFault = m_pDataContainer->getFunctionalConfigurationParameterValueFault();
            if (configurationParameterFault.get())
            {
                configurationParameterFault.clear();
                notifyObservers(eSEND_FAULT_REPORT);
            }
            //else do nothing
        }
        else
        {
            //Set functional configuration parameter fault if set
            Model::Fault& configurationParameterFault = m_pDataContainer->getFunctionalConfigurationParameterValueFault();
            if (!configurationParameterFault.get())
            {
                configurationParameterFault.set();
                notifyObservers(eSEND_FAULT_REPORT);
            }
            //else do nothing
        }

        Model::Model::applyNewIniConfiguration(pIniConfiguration);
    }
    else
    {
        //Set fault if cleared
        Model::Fault& configurationFileFault = m_pDataContainer->getFunctionalConfigurationSyntaxFault();
        if (!configurationFileFault.get())
        {
            configurationFileFault.set();
            notifyObservers(eSEND_FAULT_REPORT);
        }
        //else do nothing

        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "New configuration has been rejected due to errors.\nOld configuration will remain in use");
    }
}

void InStationHTTPClient::processPlainTextResponse(const std::string& inputString)
{
    TResponseBodyContext responseBodyContext;
    bool ok = ResponseBodyParser::parse(inputString, responseBodyContext);
    if (ok)
    {
        //Clear RESPONSE MESSAGE BODY ERROR faults
        bool anyFaultHasBeenCleared = false;
        {
            ::Lock lock(m_identifierCollectionMutex);
            for (TIdentityCollection::const_iterator iter(m_identityCollection.begin()), iterEnd(m_identityCollection.end());
                iter != iterEnd;
                ++iter)
            {
                if ((iter->pResponseMessageBodyErrorFault != 0) && (iter->pResponseMessageBodyErrorFault->get()))
                {
                    iter->pResponseMessageBodyErrorFault->clear();
                    anyFaultHasBeenCleared = true;
                }
                //else do nothing
            }
        }

        if (anyFaultHasBeenCleared)
        {
            notifyObservers(eSEND_FAULT_REPORT);
        }
        //else do nothing


        if (responseBodyContext.body.openSSHConnection)
        {
            m_remoteSSHPortNumber = responseBodyContext.body.remotePortNumber;
            notifyObservers(eOPEN_SSH_CONNECTION);
        }
        //else do nothing

        if (responseBodyContext.body.closeSSHConnection)
        {
            notifyObservers(eCLOSE_SSH_CONNECTION);
        }
        //else do nothing

        if (responseBodyContext.body.reloadConfiguration)
        {
            notifyObservers(eRELOAD_CONFIGURATION);
        }
        //else do nothing

        if (responseBodyContext.body.getStatusReport)
        {
            notifyObservers(eSEND_STATUS_REPORT);
        }
        //else do nothing

        if (responseBodyContext.body.reboot)
        {
            if (m_pInStationReporter != 0)
            {
                TStatusReportCollection statusReportCollection;
                StatusReport report("boot", "1");
                statusReportCollection.push_back(report);
                m_pInStationReporter->sendStatusReport(statusReportCollection);
            }
            //else do nothing

            notifyObservers(eREBOOT);

            //Remember: Canceling inquiry may take some time
        }
        //else do nothing

        if (responseBodyContext.body.changeSeed)
        {
            if ((m_pSignatureGenerator != 0) && (m_pInStationReporter != 0))
            {
                TStatusReportCollection statusReportCollection;
                StatusReport report("seed", "0");

                if (m_pSeedConfiguration != 0)
                {
                    if (m_pSeedConfiguration->readAllParametersFromFile())
                    {
                        //Apply new seed value
                        {
                            ::Lock lock(m_signatureGeneratorMutex);
                            m_pSignatureGenerator->setSeed(m_pSeedConfiguration->getValue());
                        }
                        report.value = Utils::uint64ToString(m_pSeedConfiguration->getId());


                        if (m_pDataContainer != 0)
                        {
                            //Clear seed fault if set
                            Model::Fault& seedFileFault = m_pDataContainer->getSeedFileFault();
                            if (seedFileFault.get())
                            {
                                seedFileFault.clear();
                                notifyObservers(eSEND_FAULT_REPORT);
                            }
                            //else do nothing
                        }
                        //else do nothing
                    }
                    else
                    {
                        if (m_pDataContainer != 0)
                        {
                            //Set fault if cleared
                            Model::Fault& seedFileFault = m_pDataContainer->getSeedFileFault();
                            if (!seedFileFault.get())
                            {
                                seedFileFault.set();
                                notifyObservers(eSEND_FAULT_REPORT);
                            }
                            //else do nothing
                        }
                        //else do nothing
                    }
                }
                //else do nothing

                statusReportCollection.push_back(report);
                m_pInStationReporter->sendStatusReport(statusReportCollection);
            }
            //else do nothing
            notifyObservers(eCHANGE_SEED);
        }
        //else do nothing

        if (responseBodyContext.body.latchBackground)
        {
            const TTime_t CURRENT_TIME_UTC(m_pClock->getUniversalTime());
            const TTimeDiff_t TIME_SINCE_ZERO_UTC(CURRENT_TIME_UTC - ZERO_TIME_UTC);
            const uint64_t TIME_SINCE_ZERO_TOTAL_SECONDS_UTC = TIME_SINCE_ZERO_UTC.total_seconds();

            m_pDataContainer->latchBackgroundDevices(
                TIME_SINCE_ZERO_TOTAL_SECONDS_UTC,
                responseBodyContext.body.latchBackgroundTimeInSeconds);
            notifyObservers(eLATCH_BACKGROUND);
        }
        //else do nothing

        if (responseBodyContext.body.flushBackground)
        {
            m_pDataContainer->flushBackgroundDevices();
            notifyObservers(eFLUSH_BACKGROUND);
        }
        //else do nothing
    }
    else
    {
        Logger::log(LOG_LEVEL_WARNING,
            getIdentifierName(),
            "Message body of the last HTTP response could not be parsed");

        //Set RESPONSE MESSAGE BODY ERROR faults
        bool anyFaultHasBeenSet = false;
        {
            ::Lock lock(m_identifierCollectionMutex);
            for (TIdentityCollection::const_iterator iter(m_identityCollection.begin()), iterEnd(m_identityCollection.end());
                iter != iterEnd;
                ++iter)
            {
                if ((iter->pResponseMessageBodyErrorFault != 0) && (!iter->pResponseMessageBodyErrorFault->get()))
                {
                    iter->pResponseMessageBodyErrorFault->set();
                    anyFaultHasBeenSet = true;
                }
                //else do nothing
            }
        }

        if (anyFaultHasBeenSet)
            notifyObservers(eSEND_FAULT_REPORT);
    }
}

void InStationHTTPClient::onClose()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG2,
            getIdentifierName(),
            "InStationHTTPClient::onClose()");
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_CLOSE;
    sendInternalSignal(pSignal);
}

void InStationHTTPClient::onBackoffTimerStarted()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "InStationHTTPClient::onBackoffTimerStarted");
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_BACKOFF_TIMER_STARTED;
    sendInternalSignal(pSignal);
}

void InStationHTTPClient::onBackoffTimerExpired()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "InStationHTTPClient::onBackoffTimerExpired");
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

void InStationHTTPClient::onConnect(const bool success)
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
    {
        Logger::log(LOG_LEVEL_DEBUG2,
            getIdentifierName(),
            "InStationHTTPClient::onConnect()",
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
            getIdentifierName(),
            "The last request to the InStation was not successful");

        //Do not remove packets. Try to sent them as long as possible. The collection is protected by
        //counting its entries against overflow (sendRequest())

        //Set all relevant faults
        {
            ::Lock lock(m_identifierCollectionMutex);
            for (TIdentityCollection::const_iterator iter(m_identityCollection.begin()), iterEnd(m_identityCollection.end());
                iter != iterEnd;
                ++iter)
            {
                if (iter->pCommunicationFault != 0)
                {
                    iter->pCommunicationFault->set();
                }
                //else do nothing
            }
        }

        notifyObservers(eSEND_FAULT_REPORT);


        //Additionally inform observers about some more details
        if (isOfIdentifier(ALERT_AND_STATUS_REPORTING_CLIENT_IDENTIFIER))
        {
            notifyObservers(eLAST_ALARM_AND_STATUS_REPORT_HAS_FAILED);
        }
        //else do nothing

        if (isOfIdentifier(STATUS_REPORTING_CLIENT_IDENTIFIER))
        {
            notifyObservers(eLAST_STATUS_REPORT_HAS_FAILED);
        }
        //else do nothing

        if (isOfIdentifier(FAULT_REPORTING_CLIENT_IDENTIFIER))
        {
            notifyObservers(eLAST_FAULT_REPORT_HAS_FAILED);
        }
        //else do nothing

        if (isOfIdentifier(STATISTICS_REPORTING_CLIENT_IDENTIFIER))
        {
            notifyObservers(eLAST_STATISTICS_REPORT_HAS_FAILED);
        }
        //else do nothing

        if (isOfIdentifier(RETRIEVE_CONFIGURATION_CLIENT_IDENTIFIER))
        {
            notifyObservers(eLAST_CONFIGURATION_REQUEST_HAS_FAILED);
        }
        //else do nothing
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = success ? Signal::eON_CONNECT_SUCCESS : Signal::eON_CONNECT_FAILURE;
    sendInternalSignal(pSignal);
}


/**
  @brief Send an asynchronous request to the server containing journey time
  @param[in] startTime time to be included in the report
  @param[in] rawJourneyTimeCollection collection of code/count pairs
  @param[in] useHttpVersion1_1 if true the persistent HTTP connection (HTTP version 1.1) will be used.
  @param[in] shouldCloseConnectionAfterSending if true and HTTP v1.1 then after receipt of response
    the connection will not be automatically closed
*/
void InStationHTTPClient::sendRawDeviceDetection(
        const ::TTime_t& startTime,
        const TRawDeviceDetectionCollection& rawDeviceDetectionCollection,
        const bool useHttpVersion1_1,
        const bool shouldCloseConnectionAfterSending)
{
    //Do not send configuration if there is nothing to send or version is lower than 4
    if (rawDeviceDetectionCollection.empty() || (m_coreConfiguration.getMajorCoreConfigurationVersion()>=4))
    {
        return;
    }
    //else continue

    if (m_rawDeviceDetectionPath.empty())
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "URL JourneyTimesReportingPath value has not been found in the configuration. Message will not be sent");
        return;
    }
    //else continue

    std::ostringstream header;
    std::ostringstream body;

    header << "POST ";

    if (m_uriRawDeviceDetectionPath.Path.empty() && m_uriRawDeviceDetectionPath.QueryString.empty())
        header << "/";
    else
        header << m_uriRawDeviceDetectionPath.Path << m_uriRawDeviceDetectionPath.QueryString;

    if (useHttpVersion1_1)
        header << " HTTP/1.1" EOL;
    else
        header << " HTTP/1.0" EOL;

    header <<
        "Host: " << m_pTCPClient->getRemoteAddress() << ":" << m_pTCPClient->getRemotePortNumber() << EOL
        //<< HEADER_USER_AGENT_STR << EOL
        //<< HEADER_ACCEPT_ALL_EOL
        "Content-Type: application/x-www-form-urlencoded" EOL;

    //header << shouldCloseConnectionAfterSending ? HEADER_CONNECTION_CLOSE_EOL : HEADER_CONNECTION_KEEP_ALIVE_EOL;

    body.imbue(m_outStationInstationLocale);
    body << std::setfill('0');
    body << "outstationID=" << m_coreConfiguration.getSiteIdentifier()
         << "&startTime=" << startTime
         << "&devCount=" << rawDeviceDetectionCollection.size();

    //Output device identifiers for all detected devices
    unsigned int id = 1;
    for (TRawDeviceDetectionCollection::const_iterator
            iter(rawDeviceDetectionCollection.begin()),
            iterEnd(rawDeviceDetectionCollection.end());
        iter != iterEnd;
        ++iter)
    {
        body << "&d" << id++ << '=' << std::hex << std::setw(12) << iter->deviceIdentifier;
    }

    //Finalise the header by adding the content length
    const std::string bodyString(body.str());
    header << "Content-Length: " << bodyString.size() << EOL EOL;

    std::ostringstream& headerAndBody = header;
    headerAndBody << bodyString;

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "Sending RAW_DEVICE_DETECTION to the InStation");
    }
    //else do nothing

    sendRequest(headerAndBody.str(), eREQUEST_TYPE_POST_RAW_DEVICE_DETECTION, useHttpVersion1_1, shouldCloseConnectionAfterSending);
}

/**
  @brief Send an asynchronous request to the server containing current congestion report
  @param[in] reportTime time to be included in the report
  @param[in] congestionReport record containing information about the congestion to be reported
  @param[in] useHttpVersion1_1 if true the persistent HTTP connection (HTTP version 1.1) will be used.
  @param[in] shouldCloseConnectionAfterSending if true and HTTP v1.1 then after receipt of response
    the connection will not be automatically closed
*/
void InStationHTTPClient::sendCongestionReport(
        const ::TTime_t& reportTime,
        const struct QueueDetection::CongestionReport& congestionReport,
        const bool useHttpVersion1_1,
        const bool shouldCloseConnectionAfterSending)
{
    if (m_congestionReportsPath.empty())
    {
        if (m_coreConfiguration.getMajorCoreConfigurationVersion()>=4)
            Logger::log(LOG_LEVEL_ERROR,
                getIdentifierName(),
                "URL CongestionReportsPath value has not been found in the configuration. Report will not be sent");
        else
            Logger::log(LOG_LEVEL_ERROR,
                getIdentifierName(),
                "URL CongestionReportingPath value has not been found in the configuration. Report will not be sent");

        return;
    }
    //else continue

    if (reportTime.is_not_a_date_time())
    {
        Logger::log(LOG_LEVEL_EXCEPTION,
            getIdentifierName(),
            "InStationHTTPClient::sendCongestionReport() reportTime is invalid");

        return;
    }
    //else do nothing

    std::ostringstream header;
    std::ostringstream body;

    header << "POST ";

    if (m_uriCongestionReportsPath.Path.empty() && m_uriCongestionReportsPath.QueryString.empty())
        header << "/";
    else
        header << m_uriCongestionReportsPath.Path << m_uriCongestionReportsPath.QueryString;

    if (m_coreConfiguration.getMajorCoreConfigurationVersion()>=4)
    {
        if (useHttpVersion1_1)
            header << " HTTP/1.1" EOL;
        else
            header << " HTTP/1.0" EOL;
    }
    else
    {
        header << " HTTP/1.0" EOL;
    }

    header <<
        "Host: " << m_pTCPClient->getRemoteAddress() << ":" << m_pTCPClient->getRemotePortNumber() << EOL;

    if (m_coreConfiguration.getMajorCoreConfigurationVersion()>=4)
    { //VERSION 4+
        header << "Content-Type: text/plain" EOL;

        const TTimeDiff_t TIME_SINCE_ZERO(reportTime - ZERO_TIME_UTC);
        body << m_coreConfiguration.getSiteIdentifier()
             << RECORD_SEPARATOR << std::hex << std::setw(TIME_WIDTH) << std::setfill('0')
             << TIME_SINCE_ZERO.total_seconds() << std::dec
             << RECORD_SEPARATOR << congestionReport.numberOfDevicesInFreeFlowBin
             << FIELD_SEPARATOR << congestionReport.numberOfDevicesInModerateFlowBin
             << FIELD_SEPARATOR << congestionReport.numberOfDevicesInSlowFlowBin
             << FIELD_SEPARATOR << congestionReport.numberOfDevicesInVerySlowFlowBin
             << FIELD_SEPARATOR << congestionReport.numberOfDevicesInStaticFlowBin;

        body << RECORD_SEPARATOR << std::hex << static_cast<int>(congestionReport.queuePresenceState) << std::dec;

        if (m_pSignatureGenerator != 0)
        {
            ::Lock lock(m_signatureGeneratorMutex);
            body << RECORD_SEPARATOR
                << std::hex << (m_pSignatureGenerator->getNewSignature() & SIGNATURE_MASK)
                << std::dec;
        }
        else
        {
            body << RECORD_SEPARATOR << 0;
        }
    }
    else
    { //VERSION 3-
        header << "Content-Type: application/x-www-form-urlencoded" EOL;

        body.imbue(m_outStationInstationLocale);
        body << "id=" << m_coreConfiguration.getSiteIdentifier()
             << "&t=" << reportTime
             << "&f=" << congestionReport.numberOfDevicesInFreeFlowBin
             << "&m=" << congestionReport.numberOfDevicesInModerateFlowBin
             << "&s=" << congestionReport.numberOfDevicesInSlowFlowBin
             << "&vs=" << congestionReport.numberOfDevicesInVerySlowFlowBin
             << "&st=" << congestionReport.numberOfDevicesInStaticFlowBin;
        if (!congestionReport.queueStartTime.is_not_a_date_time())
        {
            body << "&qs=" << congestionReport.queueStartTime;
        }
        //else do nothing

        if (!congestionReport.queueEndTime.is_not_a_date_time())
        {
            body << "&qe=" << congestionReport.queueEndTime;
        }
        //else do nothing
    }

    //Finalise the header by adding the content length
    const std::string bodyString(body.str());
    header << "Content-Length: " << bodyString.size() << EOL EOL;

    std::ostringstream& headerAndBody = header;
    headerAndBody << bodyString;

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "Sending CONGESTION_REPORT to the InStation");
    }
    //else do nothing

    sendRequest(headerAndBody.str(), eREQUEST_TYPE_POST_CONGESTION_REPORT, useHttpVersion1_1, shouldCloseConnectionAfterSending);
}

/**
  @brief Send an asynchronous request to the server to get INI configuration file
  @param[in] useHttpVersion1_1 if true the persistent HTTP connection (HTTP version 1.1) will be used.
  @param[in] shouldCloseConnectionAfterSending if true and HTTP v1.1 then after receipt of response
    the connection will not be automatically closed
*/
void InStationHTTPClient::sendConfigurationRequest(
        const bool useHttpVersion1_1,
        const bool shouldCloseConnectionAfterSending)
{
    std::string urlConfigurationPath(m_coreConfiguration.getConfigurationURL());
    if (urlConfigurationPath.empty())
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "URL of configuration path value is empty. Request for configuration will not be sent");
        return;
    }
    //else continue

    std::ostringstream header;
    std::ostringstream body;

    //TODO Consider striping of uri only once after the configuration has been loaded
    Uri uri = Uri::parse(urlConfigurationPath);

    if (uri.Path.find_last_not_of('/') == uri.Path.size() - 1)
    {
        uri.Path += '/';
    }
    //else do nothing

    header <<
        "GET "
        << uri.Path
        << m_coreConfiguration.getConfigurationURL_filePrefix()
        << m_coreConfiguration.getSiteIdentifier()
        << m_coreConfiguration.getConfigurationURL_fileSuffix()
        << uri.QueryString;

    if (useHttpVersion1_1)
        header << " HTTP/1.1" EOL;
    else
        header << " HTTP/1.0" EOL;

    header <<
        "Host: " << m_pTCPClient->getRemoteAddress() << ":" << m_pTCPClient->getRemotePortNumber() << EOL;
        //<< HEADER_USER_AGENT_STR << EOL
        //   HEADER_ACCEPT_ALL_EOL

    //header << shouldCloseConnectionAfterSending ? HEADER_CONNECTION_CLOSE_EOL : HEADER_CONNECTION_KEEP_ALIVE_EOL;
    header << EOL;

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "Sending CONFIGURATION_REQUEST to the InStation");
    }
    //else do nothing

    sendRequest(header.str(), eREQUEST_TYPE_GET_CONFIGURATION, useHttpVersion1_1, shouldCloseConnectionAfterSending);
}

/**
  @brief Send an asynchronous request to the server containing current status codes
  @param[in] reportTime time to be included in the report
  @param[in] alertAndStatusReportCollection collection of code/count pairs
  @param[in] useHttpVersion1_1 if true the persistent HTTP connection (HTTP version 1.1) will be used.
  @param[in] shouldCloseConnectionAfterSending if true and HTTP v1.1 then after receipt of response
    the connection will not be automatically closed
*/
void InStationHTTPClient::sendAlertAndStatusReport(
        const ::TTime_t& reportTime,
        const TAlertAndStatusReportCollection& alertAndStatusReportCollection,
        const bool useHttpVersion1_1,
        const bool shouldCloseConnectionAfterSending)
{
    //Do not send alertAndStatusReport if version is 4 or greater
    if (m_coreConfiguration.getMajorCoreConfigurationVersion()>=4)
    {
        return;
    }
    //else continue

    if (!m_shouldInformAboutAlertAndStatus)
    {
        Logger::log(LOG_LEVEL_NOTICE,
            getIdentifierName(),
            "Sending alerts and status is currently disabled. Report will not be sent");
        return;
    }
    //else continue

    if (m_alertAndStatusReportsPath.empty())
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "urlAlertAndStatusReports value has not been found in the configuration. Report will not be sent");
        return;
    }
    //else continue

    if (alertAndStatusReportCollection.empty())
    {
        return;
    }
    //else continue

    std::ostringstream header;
    std::ostringstream body;

    header << "POST ";

    if (m_uriAlertAndStatusReportsPath.Path.empty() && m_uriAlertAndStatusReportsPath.QueryString.empty())
        header << "/";
    else
        header << m_uriAlertAndStatusReportsPath.Path << m_uriAlertAndStatusReportsPath.QueryString;

    if (useHttpVersion1_1)
        header << " HTTP/1.1" EOL;
    else
        header << " HTTP/1.0" EOL;

    header <<
        "Host: " << m_pTCPClient->getRemoteAddress() << ":" << m_pTCPClient->getRemotePortNumber() << EOL
        //<< HEADER_USER_AGENT_STR << EOL
        //<< HEADER_ACCEPT_ALL_EOL
        "Content-Type: application/x-www-form-urlencoded" EOL;

    //header << shouldCloseConnectionAfterSending ? HEADER_CONNECTION_CLOSE_EOL : HEADER_CONNECTION_KEEP_ALIVE_EOL;

    body.imbue(m_outStationInstationLocale);
    body << "id=" << m_coreConfiguration.getSiteIdentifier()
         << "&dt=" << reportTime
         << "&m=" << MESSAGE_ID
         << '&';
    bool isEmpty = true;
    for (TAlertAndStatusReportCollection::const_iterator
            iter(alertAndStatusReportCollection.begin()),
            iterEnd(alertAndStatusReportCollection.end());
        iter != iterEnd;
        ++iter)
    {
        if (!isEmpty)
        {
            body << ',';
        }
        else
        {
            body << "s=";
        }

        body << iter->code << ':' << iter->count;
        isEmpty = false;
    }

    //Finalise the header by adding the content length
    const std::string bodyString(body.str());
    header << "Content-Length: " << bodyString.size() << EOL EOL;

    std::ostringstream& headerAndBody = header;
    headerAndBody << bodyString;

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "Sending ALERT_AND_STATUS_REPORT to the InStation");
    }
    //else do nothing

    sendRequest(headerAndBody.str(), eREQUEST_TYPE_POST_ALARM_AND_STATUS_REPORT, useHttpVersion1_1, shouldCloseConnectionAfterSending);

    //We pass the responsibility to sent ALARM AND STATUS REPORT to the InStationHTTPClient class
    notifyObservers(eLAST_ALARM_AND_STATUS_REPORT_HAS_BEEN_SENT);
}

/**
  @brief Send an asynchronous request to the server containing current status values
  @param[in] statusReportCollection collection of code/count pairs
  @param[in] useHttpVersion1_1 if true the persistent HTTP connection (HTTP version 1.1) will be used.
  @param[in] shouldCloseConnectionAfterSending if true and HTTP v1.1 then after receipt of response
    the connection will not be automatically closed
*/
void InStationHTTPClient::sendStatusReport(
        const ::TTime_t& reportTime,
        const TStatusReportCollection& statusReportCollection,
        const bool useHttpVersion1_1,
        const bool shouldCloseConnectionAfterSending)
{
    //Do not send configuration if version is lower than 4
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
    {
        return;
    }
    //else continue

    if (!m_shouldInformAboutAlertAndStatus)
    {
        Logger::log(LOG_LEVEL_NOTICE,
            getIdentifierName(),
            "Sending status is currently disabled. Report will not be sent");
        return;
    }
    //else continue

    if (m_statusReportsPath.empty())
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "urlStatusReports value has not been found in the configuration. Report will not be sent");
        return;
    }
    //else continue

    if (statusReportCollection.empty())
    {
        return;
    }
    //else continue

    std::ostringstream header;
    std::ostringstream body;

    header << "POST ";

    if (m_uriStatusReportsPath.Path.empty() && m_uriStatusReportsPath.QueryString.empty())
        header << "/";
    else
        header << m_uriStatusReportsPath.Path << m_uriStatusReportsPath.QueryString;

    if (useHttpVersion1_1)
        header << " HTTP/1.1" EOL;
    else
        header << " HTTP/1.0" EOL;

    header <<
        "Host: " << m_pTCPClient->getRemoteAddress() << ":" << m_pTCPClient->getRemotePortNumber() << EOL
        //<< HEADER_USER_AGENT_STR << EOL
        //<< HEADER_ACCEPT_ALL_EOL
        "Content-Type: text/plain" EOL;

    //header << shouldCloseConnectionAfterSending ? HEADER_CONNECTION_CLOSE_EOL : HEADER_CONNECTION_KEEP_ALIVE_EOL;

    body << m_coreConfiguration.getSiteIdentifier();

    const TTimeDiff_t REPORT_TIME_SINCE_ZERO(reportTime - ZERO_TIME_UTC);
    body << std::hex << RECORD_SEPARATOR << std::setw(TIME_WIDTH) << std::setfill('0')
        << REPORT_TIME_SINCE_ZERO.total_seconds() << std::dec;

    for (TStatusReportCollection::const_iterator
            iter(statusReportCollection.begin()),
            iterEnd(statusReportCollection.end());
        iter != iterEnd;
        ++iter)
    {
        body << RECORD_SEPARATOR << iter->name << '=' << iter->value;
    }

    if (m_pSignatureGenerator != 0)
    {
        ::Lock lock(m_signatureGeneratorMutex);
        body << RECORD_SEPARATOR
            << std::hex << (m_pSignatureGenerator->getNewSignature() & SIGNATURE_MASK)
                << std::dec;
    }
    else
    {
        body << RECORD_SEPARATOR << 0;
    }

    //Finalise the header by adding the content length
    const std::string bodyString(body.str());
    header << "Content-Length: " << bodyString.size() << EOL EOL;

    std::ostringstream& headerAndBody = header;
    headerAndBody << bodyString;

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "Sending STATUS_REPORT to the InStation");
    }
    //else do nothing

    sendRequest(headerAndBody.str(), eREQUEST_TYPE_POST_STATUS_REPORT, useHttpVersion1_1, shouldCloseConnectionAfterSending);
}

/**
  @brief Send an asynchronous request to the server containing current fault values
  @param[in] faultReportCollection collection of code/count pairs
  @param[in] useHttpVersion1_1 if true the persistent HTTP connection (HTTP version 1.1) will be used.
  @param[in] shouldCloseConnectionAfterSending if true and HTTP v1.1 then after receipt of response
    the connection will not be automatically closed
*/
void InStationHTTPClient::sendFaultReport(
        const ::TTime_t& reportTime,
        const TFaultReportCollection& faultReportCollection,
        const bool useHttpVersion1_1,
        const bool shouldCloseConnectionAfterSending)
{
    //Do not send configuration if version is lower than 4
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
    {
        return;
    }
    //else continue

    if (faultReportCollection.empty())
    {
        return;
    }
    //else continue

    if (!m_shouldInformAboutAlertAndStatus)
    {
        Logger::log(LOG_LEVEL_NOTICE,
            getIdentifierName(),
            "Sending fault is currently disabled. Report will not be sent");
        return;
    }
    //else continue

    if (m_faultReportsPath.empty())
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "urlFaultReports value has not been found in the configuration. Report will not be sent");
        return;
    }
    //else continue

    std::ostringstream header;
    std::ostringstream body;

    header << "POST ";

    if (m_uriFaultReportsPath.Path.empty() && m_uriFaultReportsPath.QueryString.empty())
        header << "/";
    else
        header << m_uriFaultReportsPath.Path << m_uriFaultReportsPath.QueryString;

    if (useHttpVersion1_1)
        header << " HTTP/1.1" EOL;
    else
        header << " HTTP/1.0" EOL;

    header <<
        "Host: " << m_pTCPClient->getRemoteAddress() << ":" << m_pTCPClient->getRemotePortNumber() << EOL
        //<< HEADER_USER_AGENT_STR << EOL
        //<< HEADER_ACCEPT_ALL_EOL
        "Content-Type: text/plain" EOL;

    //header << shouldCloseConnectionAfterSending ? HEADER_CONNECTION_CLOSE_EOL : HEADER_CONNECTION_KEEP_ALIVE_EOL;

    body << m_coreConfiguration.getSiteIdentifier();

    const ::TTimeDiff_t REPORT_TIME_SINCE_ZERO(reportTime - ZERO_TIME_UTC);
    body << std::hex << RECORD_SEPARATOR << std::setw(TIME_WIDTH) << std::setfill('0')
        << REPORT_TIME_SINCE_ZERO.total_seconds() << std::dec;

    for (TFaultReportCollection::const_iterator
            iter(faultReportCollection.begin()),
            iterEnd(faultReportCollection.end());
        iter != iterEnd;
        ++iter)
    {
        const ::TTimeDiff_t EVENT_TIME_SINCE_ZERO(iter->eventTime - ZERO_TIME_UTC);
        body << RECORD_SEPARATOR << iter->id << ':' << std::hex <<
            std::setw(TIME_WIDTH) << EVENT_TIME_SINCE_ZERO.total_seconds() << std::dec << ':' << iter->status;
    }

    if (m_pSignatureGenerator != 0)
    {
        ::Lock lock(m_signatureGeneratorMutex);
        body << RECORD_SEPARATOR
            << std::hex << (m_pSignatureGenerator->getNewSignature() & SIGNATURE_MASK)
            << std::dec;
    }
    else
    {
        body << RECORD_SEPARATOR << 0;
    }

    //Finalise the header by adding the content length
    const std::string bodyString(body.str());
    header << "Content-Length: " << bodyString.size() << EOL EOL;

    std::ostringstream& headerAndBody = header;
    headerAndBody << bodyString;

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "Sending FAULT_REPORT to the InStation");
    }
    //else do nothing

    sendRequest(headerAndBody.str(), eREQUEST_TYPE_POST_FAULT_REPORT, useHttpVersion1_1, shouldCloseConnectionAfterSending);

    //We pass the responsibility to sent FAULT REPORT to the InStationHTTPClient class
    notifyObservers(eLAST_FAULT_REPORT_HAS_BEEN_SENT);
}

/**
  @brief Send an asynchronous request to the server containing current statistics values
  @param[in] reportStartTime span start time of this record
  @param[in] reportEndTime span end time of this record
  @param[in] useHttpVersion1_1 if true the persistent HTTP connection (HTTP version 1.1) will be used.
  @param[in] shouldCloseConnectionAfterSending if true and HTTP v1.1 then after receipt of response
    the connection will not be automatically closed
*/
void InStationHTTPClient::sendStatisticsReport(
        const ::TTime_t& reportStartTime,
        const ::TTime_t& reportEndTime,
        const TStatisticsReportCollection& statisticsReportCollection,
        const bool useHttpVersion1_1,
        const bool shouldCloseConnectionAfterSending)
{
    //Do not send configuration if version is lower than 4
    if (m_coreConfiguration.getMajorCoreConfigurationVersion() < 4)
    {
        return;
    }
    //else continue

    //Send statistics report even if the collection is empty

    if (m_statisticsReportsPath.empty())
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "urlStatisticsReports value has not been found in the configuration. Report will not be sent");
        return;
    }
    //else continue

    std::ostringstream header;
    std::ostringstream body;

    header << "POST ";

    if (m_uriStatisticsReportsPath.Path.empty() && m_uriStatisticsReportsPath.QueryString.empty())
        header << "/";
    else
        header << m_uriStatisticsReportsPath.Path << m_uriStatisticsReportsPath.QueryString;

    if (useHttpVersion1_1)
        header << " HTTP/1.1" EOL;
    else
        header << " HTTP/1.0" EOL;

    header <<
        "Host: " << m_pTCPClient->getRemoteAddress() << ":" << m_pTCPClient->getRemotePortNumber() << EOL
        //<< HEADER_USER_AGENT_STR << EOL
        //<< HEADER_ACCEPT_ALL_EOL
        "Content-Type: text/plain" EOL;

    //header << shouldCloseConnectionAfterSending ? HEADER_CONNECTION_CLOSE_EOL : HEADER_CONNECTION_KEEP_ALIVE_EOL;

    const ::TTimeDiff_t REPORT_START_TIME_SINCE_ZERO(reportStartTime - ZERO_TIME_UTC);
    const ::TTimeDiff_t REPORT_END_TIME_SINCE_ZERO(reportEndTime - ZERO_TIME_UTC);
    const ::TTimeDiff_t REPORT_TIME_DURATION(reportEndTime - reportStartTime);

    body.imbue(m_outStationInstationLocale);
    body << std::setfill('0');
    body << m_coreConfiguration.getSiteIdentifier()
         << std::hex
         << RECORD_SEPARATOR << std::setw(TIME_WIDTH) << std::setfill('0') << REPORT_START_TIME_SINCE_ZERO.total_seconds()
         << RECORD_SEPARATOR << REPORT_TIME_DURATION.total_seconds()
         << std::dec;

    int64_t hashingFunction = 0;
    if (m_pIniConfiguration->getValueInt64(Model::eHASHING_FUNCTION, hashingFunction))
    {
        //Do not report anything here, otherwise we will overflow the log - this is a software exception
    }
    //else do nothing

    //Output device identifiers for all detected devices
    for (TStatisticsReportCollection::const_iterator
            iter(statisticsReportCollection.begin()), iterEnd(statisticsReportCollection.end());
        iter != iterEnd;
        ++iter)
    {
        body << RECORD_SEPARATOR << std::hex;

        size_t hashingLength = LENGTH_OF_NONE_HASH_IN_BYTES;
        if (hashingFunction == eHASHING_FUNCTION_RAND1)
        {
            hashingLength = LENGTH_OF_RAND1_HASH_IN_BYTES;
        }
        else if (hashingFunction == eHASHING_FUNCTION_SHA256)
        {
            hashingLength = LENGTH_OF_SHA256_HASH_IN_BYTES;
        }
        else
        {
            //do nothing
        }

        const uint8_t* pHash = iter->deviceIdentifierHash;
        char hashNibble; //we print nibble by nibble
        char x;
        for (size_t i=0; i<hashingLength; ++i)
        {
            hashNibble = (*pHash >> 4) & 0x0F;
            x = hashNibble + (hashNibble < 10 ? '0' : 'a'-10);
            body << x;

            hashNibble = *pHash & 0x0F;
            x = hashNibble + (hashNibble < 10 ? '0' : 'a'-10);
            body << x;

            pHash++;
        }

        body
            << ':' << iter->cod //hex
            << ':' << std::setw(TIME_WIDTH) << std::setfill('0') << iter->firstObservationTime
            << ':' << iter->referencePointObservationTimeDelta
            << ':' << iter->lastObservationTimeDelta;
    }

    if (m_pSignatureGenerator != 0)
    {
        ::Lock lock(m_signatureGeneratorMutex);
        body << RECORD_SEPARATOR
            << std::hex << (m_pSignatureGenerator->getNewSignature() & SIGNATURE_MASK)
            << std::dec;
    }
    else
    {
        body << RECORD_SEPARATOR << 0;
    }

    //Finalise the header by adding the content length
    const std::string bodyString(body.str());
    header << "Content-Length: " << bodyString.size() << EOL EOL;

    std::ostringstream& headerAndBody = header;
    headerAndBody << bodyString;

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3)) //Reduce overhead if log level not suitable
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "Sending STATISTICS_REPORT to the InStation");
    }
    //else do nothing

    sendRequest(headerAndBody.str(), eREQUEST_TYPE_POST_STATISTICS_REPORT, useHttpVersion1_1, shouldCloseConnectionAfterSending);
}


void InStationHTTPClient::changeResponseTimeout(const int valueInSeconds)
{
    m_httpResponseTimeout = bc::seconds(valueInSeconds);

    std::ostringstream ss;
    ss << "Response timeout for the InStation has been changed to "
        << valueInSeconds << " seconds";
    Logger::log(LOG_LEVEL_DEBUG2,
        getIdentifierName(), ss.str().c_str());
}

void InStationHTTPClient::clearAllocatedSendRequestList()
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
            getIdentifierName(), ss.str().c_str());
    }
    //else do nothing
}

void InStationHTTPClient::sendRequest(
    const std::string& requestString,
    const ERequestType requestType,
    const bool usePersistentConnection,
    const bool shouldCloseConnectionAfterSending)
{
    //Protect the list against overflowing which may result with program crash
    {
        boost::upgrade_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
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

                {
                    boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
                    m_allocatedSendRequestList.erase(it);
                }

                Logger::log(LOG_LEVEL_WARNING,
                    getIdentifierName(),
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
            getIdentifierName(),
            "InStationHTTPClient::sendRequest() New packet added to the send queue");
    }
    //else do nothing

    RequestTypeTuple request;
    request.id = RequestTypeTuple::globalNextId++;
    request.pPacket = FastDataPacket_shared_ptr(new FastDataPacket(requestString));
    request.type = requestType;
    request.usePersistentConnection = usePersistentConnection;
    request.shouldCloseConnectionAfterSending = shouldCloseConnectionAfterSending;
    request.state = RequestTypeTuple::eNOT_SENT;
    request.timeWhenSubmitted = m_pClock->getSteadyTime();
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
        ss << "InStationHTTPClient::sendRequest() Number of items to be sent: " << m_allocatedSendRequestList.size();
        Logger::log(LOG_LEVEL_DEBUG2,
            getIdentifierName(), ss.str().c_str());
    }
    //else do nothing
//#endif //TESTING

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_SOMETHING_TO_SENT_RECEIVED;
    sendInternalSignal(pSignal);
}

void InStationHTTPClient::initialise()
{
    setState(eSTATE_WAITING_FOR_REQUEST, "initialise");
    m_eventSemaphore.reset();
}

void InStationHTTPClient::perform()
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
                    getIdentifierName(),
                    "InStationHTTPClient::perform()::eSTATE_REQUEST_RECEIVED_AND_WAITING_FOR_CONNECTION, signal received",
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
            if (!pSignal)
            {
                //Check if timeout has not occurred
                boost::upgrade_lock<boost::shared_mutex> lock2(m_allocatedSendRequestListMutex);
                if (!m_allocatedSendRequestList.empty())
                {
                    RequestTypeTuple& lastRequestedTuple = m_allocatedSendRequestList.front();
                    //Check if timeout has not occurred
                    const TSteadyTimePoint CURRENT_TIME(m_pClock->getSteadyTime());
                    const TSteadyTimeDuration TIME_SINCE_LAST_SENDING = CURRENT_TIME - lastRequestedTuple.timeWhenSent;
                    if (TIME_SINCE_LAST_SENDING > m_httpResponseTimeout)
                    {
                        //Check if max number of send attempts has not been exceeded
                        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock2);
                        if (lastRequestedTuple.numberOfAttemptsToSent++ <= m_httpSendMaxAttemptNumber)
                        {
                            Logger::log(LOG_LEVEL_WARNING,
                                getIdentifierName(), "The last request has been timed out. Retrying...");

                            //The packet will be automaticall resent
                        }
                        else
                        {
                            Logger::log(LOG_LEVEL_ERROR,
                                getIdentifierName(), "The last request has been timed out. Aborting");

                            //Remove the packet to be sent off the list
                            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_NOTICE)) //Reduce overhead if log level not suitable
                            {
                                std::ostringstream ss;
                                ss << "Aborting sending of the packet:\n"
                                    << (const char*)lastRequestedTuple.pPacket->data();
                                Logger::log(LOG_LEVEL_NOTICE,
                                    getIdentifierName(), ss.str().c_str());
                            }
                            //else do not log

                            m_allocatedSendRequestList.pop_front(); //remove the aborted packet from the list
                        }

                        m_pTCPClient->cancelConnection();
                        setState(eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY, "perfom");
                    }
                    //else do nothing
                }
                //else do nothing

                break;
            }
            //else do nothing

            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
            {
                Logger::log(LOG_LEVEL_DEBUG3,
                    getIdentifierName(),
                    "InStationHTTPClient::perform()::eSTATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE, signal received",
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
                        getIdentifierName(),
                        "InStationHTTPClient::perform() Signal ON_SENT received but pSendData is empty");
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
                            getIdentifierName(),
                            "InStationHTTPClient::perform() Identifier not found (1)");

                        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock2);
                        clearAllocatedSendRequestList();
                    }
                }
                else
                {
                    Logger::log(LOG_LEVEL_EXCEPTION,
                        getIdentifierName(),
                        "InStationHTTPClient::perform() Identifier not found (2)");

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
                        Signal_shared_ptr pSignal(new Signal());
                        pSignal->signalType = Signal::eDUMMY_SIGNAL;
                        sendInternalSignal(pSignal);

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
                        getIdentifierName(),
                        "InStationHTTPClient::perform() Signal eON_RECEIVE received but allocatedSendRequestList is empty");
                }
            }
            else if (
                (pSignal->signalType == Signal::eON_CLOSE) ||
                (pSignal->signalType == Signal::eON_BACKOFF_TIMER_EXPIRED)
                )
            {
                //Send a signal, so that we process eSTATE_WAITING_FOR_REQUEST without waiting
                Signal_shared_ptr pSignal(new Signal());
                pSignal->signalType = Signal::eDUMMY_SIGNAL;
                sendInternalSignal(pSignal);

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
                Signal_shared_ptr pSignal(new Signal());
                pSignal->signalType = Signal::eDUMMY_SIGNAL;
                sendInternalSignal(pSignal);

                setState(eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY, "perfom");
            }
            else
            {
                //do nothing
            }

            break;
        }

        case eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY:
        {
            if (!pSignal)
                break;

            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
            {
                Logger::log(LOG_LEVEL_DEBUG3,
                    getIdentifierName(),
                    "InStationHTTPClient::perform()::eSTATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY, signal received",
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
            m_eventSemaphore.wait(SEMAPHORE_SLEEP_PERIOD_MS);
            break;
        }

        default:
        {
            break;
        }
    }
}

void InStationHTTPClient::sendPacketOutOfCollection()
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
        requestTuple.timeWhenSent = m_pClock->getSteadyTime();
        requestTuple.numberOfAttemptsToSent++;
    }
    //else do nothing
}

void InStationHTTPClient::shutdown()
{
    boost::unique_lock<boost::shared_mutex> lock(m_allocatedSendRequestListMutex);
    m_allocatedSendRequestList.clear();

    //If the main loop (perfom()) is waiting on any of the semaphores release it to avoid waiting
    m_eventSemaphore.reset();
}

void InStationHTTPClient::stop()
{
    setState(eSTATE_STOPPED, "stop");
    m_eventSemaphore.reset();
}

unsigned short InStationHTTPClient::getRemoteSSHPortNumber() const
{
    return m_remoteSSHPortNumber;
}

void InStationHTTPClient::setState(const ERequestState state, const char* functionName)
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
            getIdentifierName(), ss.str().c_str());
    }
    //else do nothing

    m_requestState = state;
}

const char* InStationHTTPClient::STATE_NAME[eSTATE_SIZE]
{
    "STATE_UNKNOWN",
    "STATE_WAITING_FOR_REQUEST",
    "STATE_REQUEST_RECEIVED_AND_WAITING_FOR_CONNECTION",
    "STATE_REQUEST_SENT_AND_WAITING_FOR_RESPONSE",
    "STATE_REQUEST_SENT_BUT_SOMETHING_WENT_WRONG_WAITING_FOR_RECOVERY",
    "STATE_STOPPED"
};

int InStationHTTPClient::Signal::totalId = 0;

const char* InStationHTTPClient::Signal::TYPE_NAME[eSIGNAL_TYPE_SIZE]
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
    "DUMMY_SIGNAL"
};


void InStationHTTPClient::sendInternalSignal(Signal_shared_ptr pSignal)
{
    {
        boost::unique_lock<boost::mutex> lockReceive(m_signalCollectionMutex);
        pSignal->id = Signal::totalId++;
        m_signalCollection.push_back(pSignal);

        if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
        {
            Logger::log(LOG_LEVEL_DEBUG3,
                getIdentifierName(),
                "InStationHTTPClient::sendInternalSignal(), signal emitted",
                Signal::TYPE_NAME[pSignal->signalType],
                boost::lexical_cast<std::string>(pSignal->id).c_str());
        }
        //else do nothing
    }

    m_eventSemaphore.release();
}

InStationHTTPClient::Signal_shared_ptr InStationHTTPClient::extractInternalSignal()
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
                getIdentifierName(),
                "InStationHTTPClient::extractInternalSignal(), signal received",
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
            //    getIdentifierName(),
            //    "InStationHTTPClient::extractInternalSignal() checking queue, signal queue EMPTY");
        }
        else
        {
            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
            {
                for (std::list<Signal_shared_ptr>::const_iterator it=m_signalCollection.begin(), itEnd=m_signalCollection.end();
                    it != itEnd;
                    it++)
                {
                    Logger::log(LOG_LEVEL_DEBUG3,
                        getIdentifierName(),
                        "InStationHTTPClient::extractInternalSignal() checking queue, signal in the queue",
                        Signal::TYPE_NAME[(*it)->signalType],
                        boost::lexical_cast<std::string>((*it)->id).c_str());
                }
            }
            //else do nothing
        }
    }
    //else do nothing

    return pSignal;
}


#ifdef TESTING

InStationHTTPClient::TRequestTypeTupleList& InStationHTTPClient::getAllocatedSendRequestList()
{
    return m_allocatedSendRequestList;
}

const FastDataPacket_shared_ptr& InStationHTTPClient::getFullResponseDataPacket() const
{
    return m_pFullResponseDataPacket;
}

void InStationHTTPClient::test_processReceivedPacket(FastDataPacket_shared_ptr& pPacket)
{
    processReceivedPacket(pPacket);
}

InStationHTTPClient::ERequestState InStationHTTPClient::getRequestState() const
{
    return m_requestState.get();
}

void InStationHTTPClient::sendRequest__testing(
    const std::string& requestString,
    const InStationHTTPClient::ERequestType requestType,
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


int InStationHTTPClient::RequestTypeTuple::globalNextId = 0;

} //namespace
