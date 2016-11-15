/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef _ICONNECTION_PRODUCER_CLIENT_H_
#define _ICONNECTION_PRODUCER_CLIENT_H_


#include "fastdatapacket.h"

#include <stdint.h>


namespace Model
{

/**
 * @brief An interface to a class that as client can produce (open) a connection,
 * send or receive data over it and close the connection.
 */
class IConnectionProducerClient
{
public:

    virtual ~IConnectionProducerClient();

    /**
     * @brief Request opening of the connection
     */
    virtual void openConnection() = 0;

    /**
     * @brief Request sending of a packet through the connection
     * @param pDataPacket data to be sent
     * @param closeConnectionAfterSendingThisPacket if connection must be send after sending this packet set it to true
    */
    virtual bool send(FastDataPacket_shared_ptr& pDataPacket, bool closeConnectionAfterSendingThisPacket) = 0;


    typedef std::pair<int, FastDataPacket_shared_ptr> TSendDataPacket;
    typedef boost::shared_ptr<TSendDataPacket> TSendDataPacket_shared_ptr;

    /**
     * @brief Request sending of a packet through the connection. Once send ICommsClient::onSend(..) method will be called
     * @param data identifier of data and the actual data to be sent
     * @param closeConnectionAfterSendingThisPacket if connection must be send after sending this packet set it to true
     */
    virtual bool send(TSendDataPacket_shared_ptr& data, bool closeConnectionAfterSendingThisPacket) = 0;

    /**
     * @brief Request closing of the connection
     */
    virtual void closeConnection() = 0;

    /**
     * @brief Cancel a pending connection and remove all the submitted packets to send
     */
    virtual void cancelConnection() = 0;

    /**
     * @brief Say if connection is established
     * @return true if so, false otherwise
     */
    virtual bool isConnected() = 0;

    /**
     * @brief Provide local address (e.g. "localhost")
     * @return local address
     */
    virtual std::string getLocalAddress() const = 0;

    /**
     * @brief Provide remote address (e.g. "192.168.1.5")
     * @return remote address
     */
    virtual std::string getRemoteAddress() const = 0;

    /**
     * @brief Provide remote port (e.g. 80)
     * @return remote port to connect to
     */
    virtual unsigned int getRemotePortNumber() const = 0;


protected:

    IConnectionProducerClient();

    // Copy constructor and assignment operator not defined
    IConnectionProducerClient(const IConnectionProducerClient& rhs);
    IConnectionProducerClient& operator=(const IConnectionProducerClient& rhs);

};

}

#endif // _ICONNECTION_PRODUCER_CLIENT_H_
