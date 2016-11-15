/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef _ICONNECTION_CONSUMER_H_
#define _ICONNECTION_CONSUMER_H_


#include "fastdatapacket.h"

#include <utility>


namespace Model
{

/**
 * @brief An interface for a class that sends and receives data.
 *
 * This class receives signals from ActiveBoostAsioTCPClient
 */
class IConnectionConsumer
{
public:

    //! destructor
    virtual ~IConnectionConsumer();

    /**
     * @brief Check if the client is full.
     *
     * This function may be used when receiving
     * the packets to check if the client is capable of processing more packets.
     */
    virtual bool isFull() const = 0;

    /**
     * @brief Method called on receipt of a packet
     *
     * @param packet a packet that has been received
     */
    virtual void onReceive(FastDataPacket_shared_ptr& packet) = 0;

    /**
     * @brief Method called after a packet has been sent
     * @param packet a packet that has just been sent
     */
    virtual void onSend(const bool success, FastDataPacket_shared_ptr& packet) = 0;


    typedef std::pair<int, FastDataPacket_shared_ptr> TSendDataPacket;
    typedef boost::shared_ptr<TSendDataPacket> TSendDataPacket_shared_ptr;

    /**
     * @brief Method called after a packet has been sent
     * @param data identifier of data and the actual data that just have been sent
     */
    virtual void onSend(const bool success, TSendDataPacket_shared_ptr& data) = 0;

    /**
     * @brief Method called after the connection has been established
     * @param success a vaule of true is passed when connected, false otherwise
     */

    virtual void onConnect(const bool success);

    /**
     * @brief This method is called when the connection is being closed
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

protected:

    //! default constructor
    IConnectionConsumer();

    IConnectionConsumer(const IConnectionConsumer& ) = delete;
    IConnectionConsumer& operator=(const IConnectionConsumer& ) = delete;

};

}

#endif //_ICONNECTION_CONSUMER_H_
