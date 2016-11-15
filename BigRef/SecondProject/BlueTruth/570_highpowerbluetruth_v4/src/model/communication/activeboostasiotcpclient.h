/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: This is a generic class used by implementation of
                 TCP server and client based on boost:asio library

    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef _ACTIVE_BOOST_ASIO_TCP_CLIENT_H_
#define _ACTIVE_BOOST_ASIO_TCP_CLIENT_H_

#include "activeobject.h"
#include "iconnectionproducerclient.h"
#include "identifiable.h"
#include "iobservable.h"

#include "activeboostasio.h"
#include "atomicvariable.h"
#include "boostsemaphore.h"
#include "fastdatapacket.h"

#include <boost/asio.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>
#include <list>
#include <utility>  // std::pair

namespace Model
{

class IConnectionConsumer;

/**
 * @brief This is a class that implements an asynchronous TCP IP client.
 *
 * The class needs an associated class ActiveBoostAsio to work.
 * To run it:
 * -# Instantiate it and ActiveBoostAsio class,
 * -# Start the thread and ActiveBoostAsio class (start),
 * -# If required wait for the thread to start (waitUntilIoServiceRunning),
 * -# Setup and open a connection,
 * -# Send some data or wait for data to be received and then process it,
 * -# Close the connection,
 * -# Shut it down after the work has been completed and ActiveBoostAsio class (stop).
 *
 * Example:
   \code
    #include "activeboostasiotcpclient.h"
     *
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient("Client", hive.getIoService()));
    TestCommsClient commsClient;
    client->setup(&commsClient);

    hive.start();
    client->start();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("localhost", 13, "");

    client->openConnection();

    while (!commsClient.wasSuccesfullyOpened() && !commsClient.couldNotBeOpened())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(10));
    }

    client->closeConnection();
    while (!commsClient.wasClosed())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(10));
    }

    hive.stop();
    client->stop();
   \endcode
 */
class ActiveBoostAsioTCPClient :
    public ::ActiveObject,
    public IConnectionProducerClient,
    public ::Identifiable<int>,
    public ::IObservable,
    public boost::enable_shared_from_this<ActiveBoostAsioTCPClient>
{
public:
    /**
     * @brief Main constructor
     * @param name a name to be given to the instance
     * @param pIoService io_service shared pointer to be used when posting actions
     * to be executed
     */
    explicit ActiveBoostAsioTCPClient(const int id, boost::shared_ptr<boost::asio::io_service> pIoService);

    virtual ~ActiveBoostAsioTCPClient();

    /**
     * @brief Second phase setup of this class
     * @param pCommsClient the client that will be signalled upon completion of actions
     */
    void setup(IConnectionConsumer* pCommsClient);

    /**
     * @brief Prepare the class for use by providing connection parameters
     * @param remoteAddress an address to connect to
     * @param remotePortNumber an address port to connect to
     * @param localAddress an interface name to be used for the connection. Be aware that
     * a PC can have multiple network interfaces and the connection can be made on one of them.
     * If blank the interface name will be resolved by the operating system.
     * @param keepAlive IP protocol parameter
     * @param reuseAddress IP protocol parameter
     * @param sendBufferSize the size of the send buffer used for the connection
     * @param receiveBufferSize the size of the receive buffer used for the connection
     */
    void setupConnection(
        const char* remoteAddress,
        const uint16_t remotePortNumber,
        const char* localAddress,
        const bool keepAlive = true,
        const bool reuseAddress = true,
        const int sendBufferSize = 65536,
        const int receiveBufferSize = 65536);

    /**
     * @brief Verify if a given address is valid and can be resolved
     *
     * Example: "127.0.0.257" or "a.b.c.d.e.f" addresses are invalid, "127.0.0.1" or "localhost" should be valid
     *
     * @param address address to be verified
     * @return true if is valid and can be resolved, false otherwise
     */
    static bool verifyAddress(const char* address);

    /**
     * @brief Request opening of the connection. Once connected ICommsClient::onConnect(..) method will be called
     */
    virtual void openConnection();

    /**
     * @brief Request sending of a packet through the connection. Once send
     * ICommsClient::onSend(FastDataPacket_shared_ptr) method will be called
     * @param pDataPacket data to be sent
     * @param closeConnectionAfterSendingThisPacket if connection must be send after sending this packet set it to true
     */
    virtual bool send(FastDataPacket_shared_ptr& pDataPacket, bool closeConnectionAfterSendingThisPacket = false);

    /**
     * @brief Request sending of a packet through the connection. Once send
     * ICommsClient::onSend(std::pair<int, FastDataPacket_shared_ptr>& ) method will be called
     * @param data identifier of data and the actual data to be sent.
     * An identifier of value 0 is reserved for internal purposes and must not be used
     * @param closeConnectionAfterSendingThisPacket if connection must be send after sending this packet set it to true
     */
    virtual bool send(TSendDataPacket_shared_ptr& data, bool closeConnectionAfterSendingThisPacket = false);

    /**
     * @brief Request closing of the connection. Once closed ICommsClient::onClose(..) method will be called
     */
    virtual void closeConnection();

    /**
     * @brief Cancel a pending connection and remove all the submitted packets to send
     */
    virtual void cancelConnection();

    /**
     * @brief Say if connection is established
     * @return true if so, false otherwise
     */
    virtual bool isConnected();

    /**
     * @brief Provide local address (e.g. "localhost")
     * @return local address
     */
    virtual std::string getLocalAddress() const;

    /**
     * @brief Provide remote address (e.g. "192.168.1.5")
     * @return remote address
     */
    virtual std::string getRemoteAddress() const;

    /**
     * @brief Provide remote port (e.g. 80)
     * @return remote port to connect to
     */
    virtual unsigned int getRemotePortNumber() const;

    /**
     * @brief Set maximum number of send packets that can be stored in the
     * collection when requesting to send.
     *
     * All packets are sent asynchronously. If requested to send via send(..) method
     * the packets are stored locally and submitted to the underlaying layer when the client
     * is connected.
     *
     * @param maxNumberOfSendPacketsToStore maximum number of packets to store
     */
    void setMaxNumberOfSendPacketsToStore(const size_t maxNumberOfSendPacketsToStore);

    /**
     * @brief Set number of retries before announcing that the connection has failed
     */
    void setNumberOfRetries(const int value);

    /**
     * @brief Set timeout value when trying to connect
     * @param timeInSeconds timeout value in seconds
     */
    void setConnectingTimeout(const unsigned int timeInSeconds);

    /**
     * @brief Start the thread
     */
    virtual void start();

    /**
     * @brief Shutdown the thread
     */
    virtual void stop();

private:

    virtual void run();

    void shutdownSocket();

    void startConnect();
    void startBackoffTimer();
    void startReceive();
    bool sendPacket();
    void disconnect();

    void onConnect(const boost::system::error_code& errorCode);
    void onBackoffTimerExpired(const boost::system::error_code& errorCode);
    void onReceive(const boost::system::error_code& errorCode, int32_t actualBytes);
    void onSend(
        const boost::system::error_code& errorCode,
        TSendDataPacket_shared_ptr& data);

    static bool convertAddressToEndpointIterator(
        const char* address,
        const uint16_t port,
        boost::asio::io_service& ioService,
        boost::asio::ip::tcp::resolver::iterator& endpointIterator,
        boost::system::error_code& errorCode);

    bool isSomethingToSend() const;

    /**
     * A struct used to pass signals to be processed by this class
     */
    struct Signal
    {
        typedef enum
        {
            eUNKNOWN = 0,
            eON_CONNECT,
            eON_BACKOFF_TIMER_EXPIRED,
            eON_SENT,
            eON_RECEIVE,
            eCONNECT_REQUEST,
            eSEND_PACKET_REQUEST,
            eSEND_PACKET_AND_CLOSE_REQUEST,
            eCLOSE_CURRENT_CONNECTION_REQUEST,
            eCANCEL_CURRENT_CONNECTION_REQUEST,
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
    bool noInternalSignalReceived() const;
    Signal_shared_ptr extractInternalSignal();

    /**
     * This function is to provide onConnect deadline functionality, i.e. we try to connect for
     * some time and this timer defines the time limit (apart from the OS socket connection
     * limits.
     * @param errorCode error code returned. If 0 success, otherwise a failure
     */
    void checkDeadline(const boost::system::error_code& errorCode);

    ActiveBoostAsioTCPClient() = delete;
    ActiveBoostAsioTCPClient(const ActiveBoostAsioTCPClient& rhs) = delete;
    ActiveBoostAsioTCPClient& operator=(const ActiveBoostAsioTCPClient& rhs) = delete;


    //Private members:
    boost::shared_ptr<boost::asio::io_service> m_pIoService;
    boost::asio::strand m_ioStrand;
    boost::asio::ip::tcp::socket m_socket;
    boost::asio::deadline_timer m_deadlineTimer;

    IConnectionConsumer* m_pCommsClient;

    std::string m_remoteAddress;
    uint16_t m_remotePortNumber;

    std::string m_localAddress;
    //This address will be assigned after connection is established.
    //If m_localAddress is specified it should be equal to it
    std::string m_localConnectionAddress;

    bool m_keepAlive;
    bool m_keepAliveDiscrepancyReported;
    bool m_reuseAddress;
    bool m_reuseAddressDiscrepancyReported;
    int m_sendBufferSize;
    int m_receiveBufferSize;

    enum
    {
        eDO_NOT_RETRY = 0,
        eRETRY_FOR_EVER = -1
    };

    //! Number of remaining attempts. This variable should be decreased on every attempt if not 0 or -1
    //! 0  : do not retry,
    //! -1 : try for ever
    ::AtomicVariable<int> m_numberOfRemainingConnectionAttempts;
    int m_maxNumberOfRemainingConnectionAttempts;

    //! This variable will be used to manage backoff time in the case of unsuccessful disconnections
    ::AtomicVariable<unsigned int> m_numberOfConnectionAttempts;

    unsigned int m_backoffTimeInMilliSeconds;
    boost::asio::deadline_timer m_backoffTimer;


    ::AtomicVariable<bool> m_shouldCloseConnectionAfterSending;

    std::list<FastDataPacket_shared_ptr> m_receiveDataPacketList;
    mutable boost::mutex m_receiveDataPacketListMutex;

    typedef std::list<TSendDataPacket_shared_ptr> TSendDataPacketList;
    TSendDataPacketList m_allocatedSendDataPacketList;
    TSendDataPacket_shared_ptr m_pendingSendDataPacket;
    mutable boost::mutex m_sendDataPacketListMutex;

    BoostSemaphore m_runSemaphore;
    size_t m_maxNumberOfSendPacketsToStore;
    unsigned int m_connectingTimeoutInSeconds;

    //This mutex has been added to avoid racing conditions inside boost library
    static boost::mutex m_lexicalCastMutex;


    enum EState
    {
        eSTATE_UNKNOWN = 0,
        eSTATE_CONNECTION_CLOSED,
        eSTATE_OPENING_CONNECTION,
        eSTATE_CONNECTION_ESTABLISHED,
        eSTATE_WAITING_FOR_BACKOFF_TIMER_TO_EXPIRE,
        eSTATE_SIZE
    };
    static const char* STATE_NAME[eSTATE_SIZE];

    EState m_state;
    mutable boost::mutex m_stateMutex;
    void setState(const EState state);

    mutable boost::mutex m_signalCollectionMutex;
    std::list<Signal_shared_ptr> m_signalCollection;

};

}

#endif //_ACTIVE_BOOST_ASIO_TCP_CLIENT_H_
