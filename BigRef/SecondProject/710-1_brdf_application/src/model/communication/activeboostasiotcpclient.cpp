#include "stdafx.h"
#include "activeboostasiotcpclient.h"

#include "boostasio.h"
#include "fastdatapacket.h"
#include "iconnectionconsumer.h"
#include "logger.h"
#include "types.h"

#include <sstream>
#include <boost/array.hpp>
#include <boost/asio.hpp>
#include <boost/thread/thread.hpp>
#include <boost/thread/locks.hpp>


class isSomethingToSend;
namespace
{
    const size_t DEFAULT_RECEIVE_DATA_PACKET_SIZE = 0x400;

    const unsigned int SEMAPHORE_SLEEP_PERIOD_MS = 1000;

    const unsigned int DEFAULT_BACKOFF_TIME_IN_MILLISECONDS = 1000;
    const unsigned int MAX_NUMBER_OF_CONNECT_ATTEMPTS = 3;
    const unsigned int DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS = 300;
    const size_t MAX_NUMBER_OF_SEND_PACKETS_TO_STORE = 100;

    const char MODULE_NAME[] = "ActiveBoostAsioTCPClient";
}

#undef _DEBUG

#define SUCCESS (true)
#define FAILURE (false)


namespace Model
{

boost::mutex ActiveBoostAsioTCPClient::m_lexicalCastMutex;


ActiveBoostAsioTCPClient::ActiveBoostAsioTCPClient(const int id, boost::shared_ptr<boost::asio::io_service> pIoService)
:
::ActiveObject(boost::lexical_cast<std::string>(id).c_str()), //convert id to char array
IConnectionProducerClient(),
::Identifiable<int>(id),
::IObservable(),
boost::enable_shared_from_this<ActiveBoostAsioTCPClient>(),
m_pIoService(pIoService),
m_ioStrand(*m_pIoService),
m_socket(*m_pIoService),
m_deadlineTimer(*m_pIoService),
m_pCommsClient(0),
m_remoteAddress(),
m_remotePortNumber(0),
m_localAddress(),
m_localConnectionAddress(),
m_keepAlive(true),
m_keepAliveDiscrepancyReported(false),
m_reuseAddress(true),
m_reuseAddressDiscrepancyReported(false),
m_sendBufferSize(65536),
m_receiveBufferSize(65536),
m_numberOfRemainingConnectionAttempts(0),
m_maxNumberOfRemainingConnectionAttempts(0),
m_numberOfConnectionAttempts(0),
m_backoffTimeInMilliSeconds(DEFAULT_BACKOFF_TIME_IN_MILLISECONDS),
m_backoffTimer(*m_pIoService),
m_shouldCloseConnectionAfterSending(false),
m_receiveDataPacketList(),
m_receiveDataPacketListMutex(),
m_allocatedSendDataPacketList(),
m_pendingSendDataPacket(),
m_sendDataPacketListMutex(),
m_runSemaphore(MODULE_NAME, 128),
m_maxNumberOfSendPacketsToStore(MAX_NUMBER_OF_SEND_PACKETS_TO_STORE),
m_connectingTimeoutInSeconds(300),
m_state(eSTATE_CONNECTION_CLOSED),
m_signalCollectionMutex(),
m_signalCollection()
{
    //do nothing
}

ActiveBoostAsioTCPClient::~ActiveBoostAsioTCPClient()
{
    //do nothing
}

void ActiveBoostAsioTCPClient::setup(IConnectionConsumer* pCommsClient)
{
    m_pCommsClient = pCommsClient;
}

void ActiveBoostAsioTCPClient::setupConnection(
    const char* remoteAddress,
    const uint16_t remotePortNumber,
    const char* localAddress,
    const bool keepAlive,
    const bool reuseAddress,
    const int sendBufferSize,
    const int receiveBufferSize)
{
    boost::unique_lock<boost::mutex> lockSend(m_sendDataPacketListMutex);
    boost::unique_lock<boost::mutex> lockReceive(m_receiveDataPacketListMutex);

    m_remoteAddress = remoteAddress;
    m_remotePortNumber = remotePortNumber;
    m_localAddress = localAddress;

    m_keepAlive = keepAlive;
    m_reuseAddress = reuseAddress;
    m_sendBufferSize = sendBufferSize;
    m_receiveBufferSize = receiveBufferSize;
}

bool ActiveBoostAsioTCPClient::convertAddressToEndpointIterator(
    const char* address,
    const uint16_t port,
    boost::asio::io_service& ioService,
    boost::asio::ip::tcp::resolver::iterator& endpointIterator,
    boost::system::error_code& errorCode)
{
    //First check if this is a numeric format ip address
    boost::asio::ip::address::from_string(address, errorCode);
    bool isNumericAddress = !errorCode;

    // IP address may need be resolved in a different way.
    // Remark: This resolver works only if access to the network is available
    boost::unique_lock<boost::mutex> lock(m_lexicalCastMutex);

    boost::asio::ip::tcp::resolver resolver(ioService);
    boost::asio::ip::tcp::resolver::query query(
        address,
        boost::lexical_cast< std::string >(port),
        isNumericAddress?
            boost::asio::ip::resolver_query_base::numeric_service :
            boost::asio::ip::resolver_query_base::address_configured
            );

    endpointIterator = resolver.resolve( query, errorCode);
    if (errorCode) //error occurred
    {
        BoostAsio::logError(0, "ActiveBoostAsioTCPClient::convertAddressToEndpointIterator(): ", errorCode);
        return false;
    }
    //else do nothing

    return true;
}

void ActiveBoostAsioTCPClient::start()
{
    resumeThread();
}

void ActiveBoostAsioTCPClient::stop()
{
    shutdownThread(
        getName().c_str(),
        &ActiveBoostAsioTCPClient::sendStopSignal,
        (void*)this);
}

void ActiveBoostAsioTCPClient::sendStopSignal(void* pCtx)
{
    ActiveBoostAsioTCPClient* _pCtx = (ActiveBoostAsioTCPClient*)pCtx;

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eSTOP;
    _pCtx->sendInternalSignal(pSignal);
}

bool ActiveBoostAsioTCPClient::verifyAddress(const char* address)
{
    boost::asio::io_service ioService;
    boost::asio::ip::tcp::resolver::iterator endpointIterator;
    const uint16_t A_PORT_VALUE = 0; //we need to provide a port value, 0 - means use random port number
    boost::system::error_code errorCode;
    return convertAddressToEndpointIterator(address, A_PORT_VALUE, ioService, endpointIterator, errorCode);
}

void ActiveBoostAsioTCPClient::sendInternalSignal(Signal_shared_ptr pSignal)
{
    {
        boost::unique_lock<boost::mutex> lockReceive(m_signalCollectionMutex);
        pSignal->id = Signal::totalId++;

        if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
        {
            Logger::log(LOG_LEVEL_DEBUG3,
                getIdentifierName(),
                "ActiveBoostAsioTCPClient::sendInternalSignal(), signal emitted",
                Signal::TYPE_NAME[pSignal->signalType],
                boost::lexical_cast<std::string>(pSignal->id).c_str());
        }
        //else do nothing

        m_signalCollection.push_back(pSignal);
    }

    m_runSemaphore.release();
}

bool ActiveBoostAsioTCPClient::noInternalSignalReceived() const
{
    boost::unique_lock<boost::mutex> lockReceive(m_signalCollectionMutex);
    return !m_signalCollection.empty();
}

ActiveBoostAsioTCPClient::Signal_shared_ptr ActiveBoostAsioTCPClient::extractInternalSignal()
{
    Signal_shared_ptr pSignal;

    boost::unique_lock<boost::mutex> lockReceive(m_signalCollectionMutex);
    if (!m_signalCollection.empty())
    {
        pSignal = m_signalCollection.front();
        m_signalCollection.pop_front();

        if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
        {
            Logger::log(LOG_LEVEL_DEBUG3,
                getIdentifierName(),
                "ActiveBoostAsioTCPClient::extractInternalSignal(), signal received",
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
            //    "ActiveBoostAsioTCPClient::extractInternalSignal() checking queue, signal queue EMPTY");
        }
        else
        {
            for (std::list<Signal_shared_ptr>::const_iterator it=m_signalCollection.begin(), itEnd=m_signalCollection.end();
                it != itEnd;
                it++)
            {
                Logger::log(LOG_LEVEL_DEBUG3,
                    getIdentifierName(),
                    "ActiveBoostAsioTCPClient::extractInternalSignal() checking queue, signal in the queue",
                    Signal::TYPE_NAME[(*it)->signalType],
                    boost::lexical_cast<std::string>((*it)->id).c_str());
            }
            //else do nothing
        }
    }
    //else do nothing

    return pSignal;
}

void ActiveBoostAsioTCPClient::shutdownSocket()
{
    if (m_socket.is_open())
    {
        boost::system::error_code errorCode;
        boost::asio::stream_socket_service<boost::asio::ip::tcp>::endpoint_type ep =
            m_socket.remote_endpoint(errorCode);
        if (errorCode) //error occurred
        {
            BoostAsio::logError(0, "ActiveBoostAsioTCPClient::shutdownSocket(): _socket.remote_endpoint()", errorCode);
        }
        //else do nothing

        //Close the socket
        m_socket.shutdown(boost::asio::ip::tcp::socket::shutdown_both, errorCode);
        if (errorCode) //error occurred
        {
            BoostAsio::logError(0, "ActiveBoostAsioTCPClient::shutdownSocket(): socket.shutdown():", errorCode);
        }
        //else do nothing

        m_socket.close(errorCode);
        if (!errorCode) //error occurred
        {
            //Log this event
            boost::asio::ip::address address = ep.address();
            if (address.is_v4() && (address.to_v4().to_ulong() != 0)) //!is_unspecified() has been defined in boost v1.47+
            //if (!address.is_unspecified())
            {
                std::ostringstream ss;
                ss << "Connection to " << address.to_string() << " closed";
                Logger::log(LOG_LEVEL_INFO,
                    getIdentifierName(), ss.str().c_str());
            }
            //else do nothing
        }
        else
        {
            BoostAsio::logError(0, "ActiveBoostAsioTCPClient::shutdownSocket(): socket.close():", errorCode);
        }
    }
    //else do nothing
}

void ActiveBoostAsioTCPClient::startReceive()
{
    FastDataPacket_shared_ptr pDataPacket(new FastDataPacket(DEFAULT_RECEIVE_DATA_PACKET_SIZE));
    pDataPacket->fullErase();

    {
        boost::unique_lock<boost::mutex> lockReceive(m_receiveDataPacketListMutex);
        m_receiveDataPacketList.push_back(pDataPacket);
    }

    m_socket.async_read_some(
        boost::asio::buffer(
            pDataPacket->getRawBufferData(),
            pDataPacket->getMemorySize()),
            boost::bind(&ActiveBoostAsioTCPClient::onReceive, shared_from_this(), _1, _2));
}

bool ActiveBoostAsioTCPClient::isSomethingToSend() const
{
    boost::unique_lock<boost::mutex> lock(m_sendDataPacketListMutex);
    bool result =
        !m_allocatedSendDataPacketList.empty() && //i.e. there is something to send
        !m_pendingSendDataPacket; //i.e. nothing is currently being sent

    return result;
}

bool ActiveBoostAsioTCPClient::send(
    FastDataPacket_shared_ptr& pDataPacket, bool closeConnectionAfterSendingThisPacket)
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::send()",
            "Requested to send");
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType =
        closeConnectionAfterSendingThisPacket ?
            Signal::eSEND_PACKET_AND_CLOSE_REQUEST :
            Signal::eSEND_PACKET_REQUEST;

    {
        boost::unique_lock<boost::mutex> lockSend(m_sendDataPacketListMutex);
        if (m_allocatedSendDataPacketList.size() < m_maxNumberOfSendPacketsToStore)
        {
            TSendDataPacket_shared_ptr pSendDataPacket(new TSendDataPacket(0, pDataPacket));
            m_allocatedSendDataPacketList.push_back(pSendDataPacket);
        }
        else
        {
            return false;
        }
    }

    sendInternalSignal(pSignal);

    return true;
}

bool ActiveBoostAsioTCPClient::send(
    TSendDataPacket_shared_ptr& data, bool closeConnectionAfterSendingThisPacket)
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::send()",
            "Requested to send");
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType =
        closeConnectionAfterSendingThisPacket ?
            Signal::eSEND_PACKET_AND_CLOSE_REQUEST :
            Signal::eSEND_PACKET_REQUEST;

    {
        boost::unique_lock<boost::mutex> lockSend(m_sendDataPacketListMutex);
        if (m_allocatedSendDataPacketList.size() < m_maxNumberOfSendPacketsToStore)
        {
            m_allocatedSendDataPacketList.push_back(data);
        }
        else
        {
            return false;
        }
    }

    sendInternalSignal(pSignal);

    return true;
}

bool ActiveBoostAsioTCPClient::sendPacket()
{
    TSendDataPacket_shared_ptr pDataToSend;

    {
        boost::unique_lock<boost::mutex> lock(m_sendDataPacketListMutex);
        while (!m_allocatedSendDataPacketList.empty()) //we use while to remove empty packets
        {
            pDataToSend = m_allocatedSendDataPacketList.front();
            m_allocatedSendDataPacketList.pop_front();

            if (pDataToSend->second->size() != 0)
            {
                m_pendingSendDataPacket = pDataToSend;
                break;
            }
            else
            {
                if (m_pCommsClient != 0)
                {
                    if (pDataToSend->first == 0)
                    {
                        m_pCommsClient->onSend(true, pDataToSend->second);
                    }
                    else
                    {
                        m_pCommsClient->onSend(true, pDataToSend);
                    }
                }
                //else do nothing
            }
        }
        //else do nothing
    }

    if (!pDataToSend) //i.e. empty
    {
        return false;
    }
    //else continue

    FastDataPacket_shared_ptr& dataPacketToSend = pDataToSend->second;
    if (!dataPacketToSend || (dataPacketToSend->size() == 0))
    {
        return false;
    }
    //else continue

    Logger::log(LOG_LEVEL_DEBUG2,
        getIdentifierName(),
        "ActiveBoostAsioTCPClient::sendPacket() Submitted for TX:",
        boost::lexical_cast<std::string>(pDataToSend->first).c_str(),
        pDataToSend->second->c_str());

    boost::asio::async_write(
        m_socket,
        boost::asio::buffer(
            dataPacketToSend->getRawBufferData(),
            dataPacketToSend->size()),
            boost::bind(&ActiveBoostAsioTCPClient::onSend, shared_from_this(), _1, m_pendingSendDataPacket));

    /* Excerpt from http://www.boost.org/doc/libs/1_50_0/doc/html/boost_asio/reference/async_write/overload1.html
     * This operation is implemented in terms of zero or more calls to the
     * stream's async_write_some function, and is known as a composed operation.
     * The program must ensure that the stream performs no other write operations
     * (such as async_write, the stream's async_write_some function, or any
     * other composed operations that perform writes) until this operation completes.
     *
     * That means if a packet has been submitted no other packet must be submitted
     * until this operation is completed
     */

    return true;
}

void ActiveBoostAsioTCPClient::openConnection()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::openConnection()",
            "Requested to open connection");
    }
    //else do nothing

    m_numberOfRemainingConnectionAttempts = MAX_NUMBER_OF_CONNECT_ATTEMPTS;
    m_numberOfConnectionAttempts = 0;

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eCONNECT_REQUEST;
    sendInternalSignal(pSignal);
}

void ActiveBoostAsioTCPClient::closeConnection()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::closeConnection()",
            "Requested to close connection");
    }
    //else do nothing

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eCLOSE_CURRENT_CONNECTION_REQUEST;
    sendInternalSignal(pSignal);
}

void ActiveBoostAsioTCPClient::cancelConnection()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::cancelConnection()",
            "Requested to cancel connection");
    }
    //else do nothing

    m_numberOfRemainingConnectionAttempts = 0;

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eCANCEL_CURRENT_CONNECTION_REQUEST;
    sendInternalSignal(pSignal);
}

bool ActiveBoostAsioTCPClient::isConnected()
{
    boost::unique_lock<boost::mutex> lock(m_stateMutex);
    return (m_state == eSTATE_CONNECTION_ESTABLISHED);
}

std::string ActiveBoostAsioTCPClient::getLocalAddress() const
{
    return m_localAddress;
}

std::string ActiveBoostAsioTCPClient::getRemoteAddress() const
{
    return m_remoteAddress;
}

unsigned int ActiveBoostAsioTCPClient::getRemotePortNumber() const
{
    return m_remotePortNumber;
}

void ActiveBoostAsioTCPClient::run()
{
    while (!isDying())
    {
        if (
            !isSomethingToSend() &&
            !noInternalSignalReceived() &&
            !m_runSemaphore.wait (SEMAPHORE_SLEEP_PERIOD_MS) //i.e. no signal received
            )
        {
            //do nothing
        }
        else
        {
            //Remove outstanding signals off the semaphore
            m_runSemaphore.wait(0);
        }

        Signal_shared_ptr pSignal = extractInternalSignal();

        switch (m_state)
        {
            case eSTATE_CONNECTION_CLOSED:
            {
                if (!pSignal)
                {
                    break;
                }
                //else do nothing

                if (pSignal && Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
                {
                    Logger::log(LOG_LEVEL_DEBUG3,
                        getIdentifierName(),
                        "ActiveBoostAsioTCPClient::run()::eSTATE_CONNECTION_CLOSED, signal received",
                        Signal::TYPE_NAME[pSignal->signalType],
                        boost::lexical_cast<std::string>(pSignal->id).c_str());
                }
                //else do nothing

                if (pSignal && (pSignal->signalType == Signal::eCANCEL_CURRENT_CONNECTION_REQUEST))
                {
                    m_pIoService->post(
                        m_ioStrand.wrap(
                            boost::bind(&Model::ActiveBoostAsioTCPClient::startBackoffTimer, this)));
                    setState(eSTATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE);
                }
                else if (
                    (pSignal && (pSignal->signalType == Signal::eCONNECT_REQUEST)) ||
                    isSomethingToSend() ||
                    (m_numberOfRemainingConnectionAttempts.get() > 0)
                    )
                {
                    m_pIoService->post(
                        boost::bind(&Model::ActiveBoostAsioTCPClient::startConnect, this));
                    setState(eSTATE_OPENING_CONNECTION);
                }
                else
                {
                    //do nothing
                }

                break;
            }

            case eSTATE_OPENING_CONNECTION:
            {
                if (!pSignal)
                {
                    break;
                }
                //else do nothing

                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
                {
                    Logger::log(LOG_LEVEL_DEBUG3,
                        getIdentifierName(),
                        "ActiveBoostAsioTCPClient::run()::eSTATE_CONNECTION_CLOSED, signal received",
                        Signal::TYPE_NAME[pSignal->signalType],
                        boost::lexical_cast<std::string>(pSignal->id).c_str());
                }
                //else do nothing

                if (pSignal->signalType == Signal::eON_CONNECT)
                {
                    if (!pSignal->errorCode)
                    {
                        //Log this event
                        boost::system::error_code ec;
                        const boost::asio::stream_socket_service<boost::asio::ip::tcp>::endpoint_type remoteEndpoint = m_socket.remote_endpoint(ec);
                        if (ec) //error occurred
                        {
                            Logger::log(LOG_LEVEL_WARNING,
                                getIdentifierName(),
                                "ActiveBoostAsioTCPConnection::onConnect(): socket.remote_endpoint() failed",
                                ec.message().c_str());
                        }
                        //else do nothing

                        const boost::asio::stream_socket_service<boost::asio::ip::tcp>::endpoint_type localEndpoint = m_socket.local_endpoint(ec);
                        if (ec) //error occurred
                        {
                            Logger::log(LOG_LEVEL_WARNING,
                                getIdentifierName(),
                                "ActiveBoostAsioTCPConnection::onConnect(): socket.local_endpoint() failed",
                                ec.message().c_str());
                        }
                        //else do nothing

                        boost::asio::ip::address localAddress = localEndpoint.address();
                        m_localConnectionAddress = localAddress.to_string();
                        boost::asio::ip::address remoteAddress = remoteEndpoint.address();
                        std::ostringstream ss;
                        ss << "Connection from " << m_localConnectionAddress
                            << " to " << remoteAddress.to_string() << ":" << remoteEndpoint.port()
                            << " established";
                        Logger::log(LOG_LEVEL_INFO,
                            getIdentifierName(),
                            ss.str().c_str());

                        if (m_pCommsClient != 0)
                        {
                            m_pCommsClient->onConnect(SUCCESS);
                        }
                        //else do nothing

                        //Protect against subsequent connects
                        m_numberOfRemainingConnectionAttempts = 0;

                        startReceive();
                        m_runSemaphore.release();

                        setState(eSTATE_CONNECTION_ESTABLISHED);

                        break;
                    }
                    else //error occurred
                    {
                        std::ostringstream ss;
                        ss << "Connecting to " << m_remoteAddress << ":" << m_remotePortNumber << " failed";
                        Logger::log(LOG_LEVEL_ERROR,
                            getIdentifierName(), ss.str().c_str());

                        m_numberOfRemainingConnectionAttempts--;
                        m_numberOfConnectionAttempts++;

                        if (m_numberOfRemainingConnectionAttempts == 0)
                        {
                            if (m_pCommsClient != 0)
                            {
                                m_pCommsClient->onConnect(FAILURE);
                            }
                            //else do nothing

                            {
                                boost::unique_lock<boost::mutex> lock(m_sendDataPacketListMutex);
                                m_allocatedSendDataPacketList.clear();
                            }
                        }
                        //else do nothing

                        m_pIoService->post(
                            m_ioStrand.wrap(
                                boost::bind(&Model::ActiveBoostAsioTCPClient::disconnect, this)));
                        m_pIoService->post(
                            m_ioStrand.wrap(
                                boost::bind(&Model::ActiveBoostAsioTCPClient::startBackoffTimer, this)));
                        setState(eSTATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE);
                    }
                }
                else if (pSignal->signalType == Signal::eCLOSE_CURRENT_CONNECTION_REQUEST)
                {
                    m_shouldCloseConnectionAfterSending.set(true);
                }
                else if (pSignal->signalType == Signal::eCANCEL_CURRENT_CONNECTION_REQUEST)
                {
                    m_pIoService->post(
                        m_ioStrand.wrap(
                            boost::bind(&Model::ActiveBoostAsioTCPClient::disconnect, this)));
                    m_pIoService->post(
                        m_ioStrand.wrap(
                            boost::bind(&Model::ActiveBoostAsioTCPClient::startBackoffTimer, this)));
                    setState(eSTATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE);
                }
                else
                {
                    //do nothing
                }


                break;
            }

            case eSTATE_CONNECTION_ESTABLISHED:
            {
                if (!pSignal)
                {
                    if (isSomethingToSend())
                    {
                        sendPacket();
                    }
                    //else do nothing

                    break;
                }
                //else continue


                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
                {
                    Logger::log(LOG_LEVEL_DEBUG3,
                        getIdentifierName(),
                        "ActiveBoostAsioTCPClient::run()::eSTATE_CONNECTION_ESTABLISHED, signal received",
                        Signal::TYPE_NAME[pSignal->signalType],
                        boost::lexical_cast<std::string>(pSignal->id).c_str());
                }
                //else do nothing


                if (pSignal->signalType == Signal::eON_RECEIVE)
                {
                    if (!pSignal->errorCode) //no error occurred
                    {
                        //Process received bytes...
                        if (m_pCommsClient != 0)
                        {
                            m_pCommsClient->onReceive(pSignal->pReceiveData);
                        }
                        //else do nothing
#ifdef _DEBUG
                        //Test parameters of connection
                        {
                            boost::system::error_code ec;
                            boost::asio::socket_base::keep_alive keepAliveOption;
                            m_socket.get_option(keepAliveOption, ec);
                            if (ec) //error occurred
                            {
                                Logger::log(LOG_LEVEL_ERROR,
                                    getIdentifierName(),
                                    "ActiveBoostAsioTCPClient::run(): socket.open(keep_alive) failed",
                                    ec.message().c_str());
                            }
                            else if ((keepAliveOption.value() != m_keepAlive) && !m_keepAliveDiscrepancyReported)
                            {
                                Logger::log(LOG_LEVEL_DEBUG1,
                                    getIdentifierName(),
                                    "Value of keepAlive for connection socket has changed");
                                m_keepAliveDiscrepancyReported = true;
                            }
                            //else do nothing

                            boost::asio::socket_base::reuse_address reuseAddressOption;
                            m_socket.get_option(reuseAddressOption, ec);
                            if (ec) //error occurred
                            {
                                Logger::log(LOG_LEVEL_ERROR,
                                    getIdentifierName(),
                                    "ActiveBoostAsioTCPClient::run(): socket.open(reuseAddressOption) failed",
                                    ec.message().c_str());
                            }
                            else if ((reuseAddressOption.value() != m_reuseAddress) && (!m_reuseAddressDiscrepancyReported))
                            {
                                Logger::log(LOG_LEVEL_DEBUG1,
                                    getIdentifierName(),
                                    "Value of reuseAddress for connection socket has changed");
                                m_reuseAddressDiscrepancyReported = true;
                            }
                            //else do nothing
                        }
#endif
                    }
                    else
                    {
                        if (m_socket.is_open()) //when processing the received packet the socket may have been closed
                        {
                            if (m_pCommsClient != 0)
                            {
                                if (pSignal->pReceiveData &&
                                    (pSignal->pReceiveData->size() != 0))
                                {
                                    m_pCommsClient->onReceive(pSignal->pReceiveData);
                                }
                                //else do nothing
                            }
                            //else do nothing

                            m_pIoService->post(
                                m_ioStrand.wrap(
                                    boost::bind(&Model::ActiveBoostAsioTCPClient::disconnect, this)));
                            if (pSignal->errorCode == boost::asio::error::eof)
                            {
                                setState(eSTATE_CONNECTION_CLOSED);
                            }
                            else
                            {
                                m_pIoService->post(
                                    m_ioStrand.wrap(
                                        boost::bind(&Model::ActiveBoostAsioTCPClient::startBackoffTimer, this)));
                                setState(eSTATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE);
                            }
                        }
                        else
                        {
                            m_pIoService->post(
                                m_ioStrand.wrap(
                                    boost::bind(&Model::ActiveBoostAsioTCPClient::disconnect, this)));
                            setState(eSTATE_CONNECTION_CLOSED);
                        }
                    }
                }
                else if (pSignal->signalType == Signal::eON_SENT)
                {
                    if (!pSignal->errorCode)
                    {
                        if (m_pCommsClient != 0)
                        {
                            m_pCommsClient->onSend(true, pSignal->pSendData);
                        }
                        //else do nothing

                        if (m_shouldCloseConnectionAfterSending.get() && !sendPacket())
                        {
                            //Close the socket in the send direction
                            boost::system::error_code errorCode;
                            m_socket.shutdown(boost::asio::ip::tcp::socket::shutdown_send, errorCode);
                            if (errorCode) //error occurred
                            {
                                BoostAsio::logError(0, "ActiveBoostAsioTCPClient::run(): socket.shutdown(send):", errorCode);
                            }
                            else
                            {
                                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
                                {
                                    Logger::log(LOG_LEVEL_DEBUG3,
                                        getIdentifierName(),
                                        "ActiveBoostAsioTCPClient::run(): socket.shutdown(send) requested");
                                }
                                //else do nothing
                            }
                        }
                        //else do nothing
                    }
                    else //error occurred
                    {
                        Logger::log(
                            LOG_LEVEL_ERROR,
                            getIdentifierName(),
                            "ActiveBoostAsioTCPClient::run(): sending of a message failed",
                            pSignal->errorCode.message().c_str());

                        if (m_pCommsClient != 0)
                        {
                            m_pCommsClient->onSend(false, pSignal->pSendData);
                        }
                        //else do nothing

                        m_pIoService->post(
                            m_ioStrand.wrap(
                                boost::bind(&Model::ActiveBoostAsioTCPClient::disconnect, this)));
                        m_pIoService->post(
                            m_ioStrand.wrap(
                                boost::bind(&Model::ActiveBoostAsioTCPClient::startBackoffTimer, this)));
                        setState(eSTATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE);
                    }
                }
                else if (pSignal->signalType == Signal::eSEND_PACKET_REQUEST)
                {
                    if (isSomethingToSend())
                    {
                        sendPacket();
                    }
                    //else do nothing
                }
                else if (pSignal->signalType == Signal::eSEND_PACKET_AND_CLOSE_REQUEST)
                {
                    if (isSomethingToSend())
                    {
                        sendPacket();
                    }
                    //else do nothing

                    m_shouldCloseConnectionAfterSending.set(true);
                }
                else if (pSignal->signalType == Signal::eCLOSE_CURRENT_CONNECTION_REQUEST)
                {
                    m_shouldCloseConnectionAfterSending.set(true);
                    if (!isSomethingToSend())
                    {
                        m_pIoService->post(
                            m_ioStrand.wrap(
                                boost::bind(&Model::ActiveBoostAsioTCPClient::disconnect, this)));
                    }
                    //else do nothing
                }
                else if (pSignal->signalType == Signal::eCANCEL_CURRENT_CONNECTION_REQUEST)
                {
                    m_pIoService->post(
                        m_ioStrand.wrap(
                            boost::bind(&Model::ActiveBoostAsioTCPClient::disconnect, this)));
                    m_pIoService->post(
                        m_ioStrand.wrap(
                            boost::bind(&Model::ActiveBoostAsioTCPClient::startBackoffTimer, this)));
                    setState(eSTATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE);
                }
                else
                {
                    //do nothing
                }

                break;
            }

            case eSTATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE:
            {
                if (!pSignal)
                {
                    break;
                }
                //else continue


                if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
                {
                    Logger::log(LOG_LEVEL_DEBUG3,
                        getIdentifierName(),
                        "ActiveBoostAsioTCPClient::run()::eSTATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE, signal received",
                        Signal::TYPE_NAME[pSignal->signalType],
                        boost::lexical_cast<std::string>(pSignal->id).c_str());
                }
                //else do nothing


                if (pSignal->signalType == Signal::eON_BACKOFF_TIMER_EXPIRED)
                {
                    if (pSignal->errorCode) //error occurred
                    {
                        boost::system::error_code ec;
                        m_backoffTimer.cancel(ec);
                        if (ec)
                        {
                            Logger::log(LOG_LEVEL_ERROR,
                                getIdentifierName(),
                                "ActiveBoostAsioTCPClient::onBackoffTimerExpired(): backoffTimer.cancel() failed",
                                ec.message().c_str());
                        }
                        //else do nothing
                    }
                    //else do nothing

                    //Send internal signal so that we continue attempts to connect
                    if (m_numberOfRemainingConnectionAttempts > 0)
                    {
                        Signal_shared_ptr pSignal(new Signal());
                        pSignal->signalType = Signal::eCONNECT_REQUEST;
                        sendInternalSignal(pSignal);
                    }
                    //else do nothing

                    if (m_pCommsClient != 0)
                    {
                        m_pCommsClient->onBackoffTimerExpired();
                    }
                    //else do nothing

                    setState(eSTATE_CONNECTION_CLOSED);
                }
                else
                {
                    //do nothing
                }

                break;
            }

            default:
            {
                break;
            }
        } //switch
    } //while
}


void ActiveBoostAsioTCPClient::startConnect()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::startConnect()");
    }
    //else do nothing

    //Create a signal that will be sent on any failure
    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_CONNECT;
    boost::system::error_code& errorCode = pSignal->errorCode;

    boost::asio::ip::tcp::resolver::iterator remoteEndpointIterator;
    if (!convertAddressToEndpointIterator(
            m_remoteAddress.c_str(),
            m_remotePortNumber,
            *m_pIoService,
            remoteEndpointIterator,
            errorCode)) //error occurred
    {
        std::ostringstream ss;
        ss << getIdentifierName() << ", ActiveBoostAsioTCPClient::startConnect(): remoteResolver.resolve() failed"
            " for address \"" << m_remoteAddress << "\".";
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(), ss.str().c_str());

        sendInternalSignal(pSignal);
        return;
    }
    //else do nothing

    //Open the socket before binding
    m_socket.open(boost::asio::ip::tcp::v4(), errorCode);

    if (errorCode) //error occurred
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::startConnect(): socket.open() failed",
            errorCode.message().c_str());

        shutdownSocket();

        sendInternalSignal(pSignal);
        return;
    }
    //else do nothing

    //------------------------------------------------------------------------
    //Set connection options
    boost::asio::socket_base::keep_alive keepAliveOption(m_keepAlive);
    m_socket.set_option(keepAliveOption, errorCode);
    m_keepAliveDiscrepancyReported = false;
    if (errorCode) //error occurred
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::startConnect(): m_socket.set_option(keep_alive) failed",
            errorCode.message().c_str());
        m_keepAliveDiscrepancyReported = true;

        //Continue regardless of the problem
    }
    //else do nothing

    boost::asio::ip::tcp::acceptor::reuse_address reuseAddressOption(m_reuseAddress);
    m_socket.set_option(reuseAddressOption, errorCode);
    m_reuseAddressDiscrepancyReported = false;
    if (errorCode) //error occurred
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::startConnect(): m_socket.set_option(reuse_address) failed",
            errorCode.message().c_str());
        m_reuseAddressDiscrepancyReported = true;

        //Continue regardless of the problem
    }
    //else do nothing

    boost::asio::socket_base::send_buffer_size sendBufferSizeOption(m_sendBufferSize);
    m_socket.set_option(sendBufferSizeOption, errorCode);
    if (errorCode) //error occurred
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::startConnect(): m_socket.set_option(send_buffer_size) failed",
            errorCode.message().c_str());

        //Continue regardless of the problem
    }
    //else do nothing

    boost::asio::socket_base::receive_buffer_size receiveBufferSizeOption(m_receiveBufferSize);
    m_socket.set_option(receiveBufferSizeOption, errorCode);
    if (errorCode) //error occurred
    {
        Logger::log(LOG_LEVEL_ERROR,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::startConnect(): m_socket.set_option(receive_buffer_size) failed",
            errorCode.message().c_str());

        //Continue regardless of the problem
    }
    //else do nothing
    //------------------------------------------------------------------------


    if (!m_localAddress.empty())
    {
        //The same for the local endpoint. This part of code is important for PCs with multiple network interfaces
        //Leave localAddress blank if should connect on all interfaces
        boost::asio::ip::tcp::resolver::iterator localEndpointIterator;
        if (!convertAddressToEndpointIterator(
                m_localAddress.c_str(),
                0, //0 - means use random port number
                *m_pIoService,
                localEndpointIterator,
                errorCode)) //error occurred
        {
            std::ostringstream ss;
            ss << getIdentifierName() << ", ActiveBoostAsioTCPClient::startConnect(): localResolver.resolve() failed"
                " for address \"" << m_localAddress << "\".";
            Logger::log(LOG_LEVEL_ERROR,
                getIdentifierName(), ss.str().c_str());

            sendInternalSignal(pSignal);
            return;
        }
        //else do nothing

        m_socket.bind(*localEndpointIterator, errorCode);
        if (errorCode) //error occurred
        {
            Logger::log(LOG_LEVEL_ERROR,
                getIdentifierName(),
                "ActiveBoostAsioTCPClient::startConnect(): socket.bind() failed",
                errorCode.message().c_str());

            m_socket.close();
            sendInternalSignal(pSignal);
            return;
        }
        //else do nothing
    }
    //else do nothing


//#if BOOST_VERSION >= 105400
//    boost::asio::async_connect(
//        m_socket,
//        remoteEndpointIterator,
//        boost::bind(&ActiveBoostAsioTCPClient::onConnect, shared_from_this(), _1));
//#else
    boost::asio::ip::tcp::endpoint remoteEndpoint = *remoteEndpointIterator;
    m_socket.async_connect(
        remoteEndpoint,
        boost::bind(&ActiveBoostAsioTCPClient::onConnect, this, boost::asio::placeholders::error));
//#endif
    {
        std::ostringstream ss;
        ss << "Connecting to " << m_remoteAddress << ":" << m_remotePortNumber;
        Logger::log(LOG_LEVEL_INFO,
            getIdentifierName(),
            ss.str().c_str());
    }

     // Set a deadline for the connect operation.
    m_deadlineTimer.expires_from_now(boost::posix_time::seconds(m_connectingTimeoutInSeconds));
    // Start the deadline actor.
    m_deadlineTimer.async_wait(
        boost::bind(&ActiveBoostAsioTCPClient::checkDeadline, shared_from_this(), _1));

    return;
}

void ActiveBoostAsioTCPClient::checkDeadline(const boost::system::error_code& errorCode)
{
    // Check whether the deadline has passed. We compare the deadline against
    // the current time since a new asynchronous operation may have moved the
    // deadline before this actor had a chance to run.
    if (!errorCode)
    {
        if (m_deadlineTimer.expires_at() <= boost::asio::deadline_timer::traits_type::now())
        {
            Logger::log(LOG_LEVEL_DEBUG2,
                getIdentifierName(),
                "ActiveBoostAsioTCPClient::checkDeadline: TIMEOUT OCCURRED");

            // The deadline has passed.
            shutdownSocket();

            // There is no longer an active deadline. The expiry is set to positive
            // infinity so that the actor takes no action until a new deadline is set.
            m_deadlineTimer.cancel();
            m_deadlineTimer.expires_at(boost::posix_time::pos_infin);
        }
        //else do nothing
    }
    else
    {
        if (errorCode != boost::asio::error::operation_aborted)
        {
            BoostAsio::logError(0, "ActiveBoostAsioTCPClient::checkDeadline(): Connection timeout error", errorCode);

            // There is no longer an active deadline. The expiry is set to positive
            // infinity so that the actor takes no action until a new deadline is set.
            m_deadlineTimer.cancel();
            m_deadlineTimer.expires_at(boost::posix_time::pos_infin);
        }
        //else do nothing
    }
}

void ActiveBoostAsioTCPClient::onConnect(const boost::system::error_code& errorCode)
{
    Logger::log(!errorCode ? LOG_LEVEL_DEBUG2 : LOG_LEVEL_WARNING,
        getIdentifierName(),
        "ActiveBoostAsioTCPClient::onConnect()",
        errorCode.message().c_str());

    // There is no longer need for the active deadline. The expiry is set to positive
    // infinity so that the actor takes no action until a new deadline is set.
    m_deadlineTimer.cancel();
    m_deadlineTimer.expires_at(boost::posix_time::pos_infin);

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_CONNECT;
    pSignal->errorCode = errorCode;
    sendInternalSignal(pSignal);
}

void ActiveBoostAsioTCPClient::startBackoffTimer()
{
    //Shape backoff time depending on number of connection attempts.
    unsigned int backoffTimeInMilliSeconds = m_backoffTimeInMilliSeconds;

    //after the 2rd retry (3 total attempts) set backoff time to a value 10 times bigger
    if (
        (backoffTimeInMilliSeconds <= 1000) && //do not move beyond 10s
        (m_numberOfConnectionAttempts >= 3)
        )
    {
        backoffTimeInMilliSeconds = 10 * m_backoffTimeInMilliSeconds;
    }
    //else do nothing


    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        std::ostringstream ss;
        ss << "ActiveBoostAsioTCPClient::startBackoffTimer(): Backoff timer set to " << backoffTimeInMilliSeconds << " ms";
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            ss.str().c_str());
    }
    //else do nothing

    m_backoffTimer.expires_from_now(boost::posix_time::milliseconds(backoffTimeInMilliSeconds));
    m_backoffTimer.async_wait(
        boost::bind(
            &ActiveBoostAsioTCPClient::onBackoffTimerExpired, shared_from_this(), _1));

    if (m_pCommsClient != 0)
    {
        m_pCommsClient->onBackoffTimerStarted();
    }
    //else do nothing
}

void ActiveBoostAsioTCPClient::disconnect()
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::disconnect()");
    }
    //else do nothing

    shutdownSocket();

    if (m_numberOfRemainingConnectionAttempts == 0)
    {
        if (m_pCommsClient != 0)
        {
            m_pCommsClient->onClose();
        }
        //else do nothing

        {
            boost::unique_lock<boost::mutex> lockReceive(m_receiveDataPacketListMutex);
            m_receiveDataPacketList.clear();
        }

        {
            boost::unique_lock<boost::mutex> lock(m_sendDataPacketListMutex);
            m_allocatedSendDataPacketList.clear();
        }
    }
    //else do nothing

    m_localConnectionAddress.clear();
    m_shouldCloseConnectionAfterSending.set(false);
}

void ActiveBoostAsioTCPClient::setState(const EState state)
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG3))
    {
        assert(static_cast<size_t>(state) < eSTATE_SIZE);
        assert(static_cast<size_t>(m_state) < eSTATE_SIZE);

        std::ostringstream ss;
        ss << "ActiveBoostAsioTCPClient::run() ----------- " //setState must be invoked from the run() function
              "State change to " << STATE_NAME[static_cast<size_t>(state)]
           << " (from " << STATE_NAME[static_cast<size_t>(m_state)] << ")";

        Logger::log(LOG_LEVEL_DEBUG3,
            getIdentifierName(), ss.str().c_str());
    }
    //else do nothing

    boost::unique_lock<boost::mutex> lock(m_stateMutex);
    m_state = state;
}

void ActiveBoostAsioTCPClient::onBackoffTimerExpired(const boost::system::error_code& errorCode)
{
    Logger::log(!errorCode ? LOG_LEVEL_DEBUG2 : LOG_LEVEL_WARNING,
        getIdentifierName(),
        "ActiveBoostAsioTCPClient::onBackoffTimerExpired()",
        errorCode.message().c_str());

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_BACKOFF_TIMER_EXPIRED;
    pSignal->errorCode = errorCode;
    sendInternalSignal(pSignal);
}

void ActiveBoostAsioTCPClient::onReceive(const boost::system::error_code& errorCode, int32_t actualBytes)
{
    if (!errorCode)
    {
        const std::string description("Success, " + boost::lexical_cast<std::string>(actualBytes) + " bytes received");
        Logger::log(LOG_LEVEL_DEBUG2,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::onReceive()",
            description.c_str());
    }
    else if (errorCode == boost::asio::error::eof)
    {
        Logger::log(LOG_LEVEL_DEBUG2,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::onReceive()",
            errorCode.message().c_str());
    }
    else
    {
        Logger::log(LOG_LEVEL_WARNING,
            getIdentifierName(),
            "ActiveBoostAsioTCPClient::onReceive()",
            errorCode.message().c_str());
    }

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_RECEIVE;
    pSignal->errorCode = errorCode;
    {
        boost::unique_lock<boost::mutex> lock(m_receiveDataPacketListMutex);
        if (!m_receiveDataPacketList.empty()) //Due to multithreading this list may get empty on connection reset
        {
            pSignal->pReceiveData = m_receiveDataPacketList.front();
            pSignal->pReceiveData->addSizeForRawBufferAccess(actualBytes);
            m_receiveDataPacketList.pop_front();
        }
        //else do nothing
    }

    //Allow for new packets to be received
    if (!errorCode && m_socket.is_open()) //when processing the received packet the socket may have been closed
    {
        startReceive();
    }
    //else do not setup receive process

    sendInternalSignal(pSignal);
}

void ActiveBoostAsioTCPClient::onSend(
    const boost::system::error_code& errorCode,
    TSendDataPacket_shared_ptr& data)
{
    Logger::log(!errorCode ? LOG_LEVEL_DEBUG2 : LOG_LEVEL_WARNING,
        getIdentifierName(),
        "ActiveBoostAsioTCPClient::onSend()",
        errorCode.message().c_str());

    Signal_shared_ptr pSignal(new Signal());
    pSignal->signalType = Signal::eON_SENT;
    pSignal->pSendData = data;

    m_pendingSendDataPacket.reset();

    pSignal->errorCode = errorCode;
    sendInternalSignal(pSignal);
}


void ActiveBoostAsioTCPClient::setMaxNumberOfSendPacketsToStore(const size_t maxNumberOfSendPacketsToStore)
{
    m_maxNumberOfSendPacketsToStore = maxNumberOfSendPacketsToStore;
}

void ActiveBoostAsioTCPClient::setNumberOfRetries(const int value)
{
    m_maxNumberOfRemainingConnectionAttempts = value;
}

void ActiveBoostAsioTCPClient::setConnectingTimeout(const unsigned int timeInSeconds)
{
    m_maxNumberOfRemainingConnectionAttempts = timeInSeconds;
}

void ActiveBoostAsioTCPClient::setBackoffTime(const unsigned int timeInSeconds)
{
    m_backoffTimeInMilliSeconds = timeInSeconds * MILLISECONDS_PER_SECOND;
}

const char* ActiveBoostAsioTCPClient::Signal::TYPE_NAME[eSIGNAL_TYPE_SIZE] =
{
    "UNKNOWN",
    "ON_CONNECT",
    "ON_BACKOFF_TIMER_EXPIRED",
    "ON_SENT",
    "ON_RECEIVE",
    "CONNECT_REQUEST",
    "SEND_PACKET_REQUEST",
    "SEND_PACKET_AND_CLOSE_REQUEST",
    "CLOSE_CURRENT_CONNECTION_REQUEST",
    "CANCEL_CURRENT_CONNECTION_REQUEST",
    "eSTOP"
};

int ActiveBoostAsioTCPClient::Signal::totalId = 0;

const char* ActiveBoostAsioTCPClient::STATE_NAME[eSTATE_SIZE] =
{
    "STATE_UNKNOWN",
    "STATE_CONNECTION_CLOSED",
    "STATE_OPENING_CONNECTION",
    "STATE_CONNECTION_ESTABLISHED",
    "STATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE",
};


} // namespace
