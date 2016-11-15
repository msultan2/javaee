#ifndef _TEST_RECEIVER_H_
#define _TEST_RECEIVER_H_


#include "fastdatapacket.h"
#include "ireceiver.h"


namespace Testing
{

class TestReceiver : public Model::IReceiver
{
public:

    //! default constructor
    TestReceiver();

    //! destructor
    virtual ~TestReceiver();

    virtual bool isFull() const;

    virtual void receive(FastDataPacket_shared_ptr& packet);

    ///@brief This method is called when the connection is being reset
    virtual void onReset();

    ///@brief Method called after the connection has been established
    virtual void onConnect();

    ///@brief Method called when the backoff timer has expired
    virtual void onBackoffTimerExpired();


private:

    //! copy constructor. Not implemented
    TestReceiver(const TestReceiver& );
    //! assignment operator. Not implemented
    TestReceiver& operator=(const TestReceiver& );

};

}

#endif //_TEST_RECEIVER_H_
