#ifndef _TEST_CONNECTION_CONSUMER_H_
#define _TEST_CONNECTION_CONSUMER_H_


#include "iconnectionconsumer.h"

#include <boost/thread.hpp>


namespace Testing
{

class TestConnectionConsumer : public Model::IConnectionConsumer
{
public:

    //! default constructor
    TestConnectionConsumer();

    //! destructor
    virtual ~TestConnectionConsumer();

    virtual bool isFull() const;

    virtual void onReceive(FastDataPacket_shared_ptr& packet);

    virtual void onSend(const bool success, FastDataPacket_shared_ptr& packet);

    virtual void onSend(const bool success, TSendDataPacket_shared_ptr& data);

    ///@brief Method called after the connection has been established
    virtual void onConnect(const bool success);

    ///@brief This method is called when the connection is being reset
    virtual void onClose();

    ///@brief Method called when the backoff timer has expired
    virtual void onBackoffTimerExpired();

    bool wasSuccesfullyOpened()
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        return m_wasSuccesfullyOpened;
    }

    bool couldNotBeOpened()
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        return m_couldNotBeOpened;
    }

    bool wasClosed()
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        return m_wasClosed;
    }

private:
    //! copy constructor. Not implemented
    TestConnectionConsumer(const TestConnectionConsumer& ) = delete;
    //! assignment operator. Not implemented
    TestConnectionConsumer& operator=(const TestConnectionConsumer& ) = delete;

    bool m_wasSuccesfullyOpened;
    bool m_couldNotBeOpened;
    bool m_wasClosed;
    mutable boost::mutex m_mutex;
};

}

#endif //_TEST_CONNECTION_CONSUMER_H_
