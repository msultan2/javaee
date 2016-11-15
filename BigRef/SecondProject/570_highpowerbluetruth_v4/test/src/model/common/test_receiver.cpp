#include "test_receiver.h"


namespace Testing
{

TestReceiver::TestReceiver()
:
Model::IReceiver()
{

}

TestReceiver::~TestReceiver()
{

}

bool TestReceiver::isFull() const
{
    return false;
}

void TestReceiver::receive(FastDataPacket_shared_ptr& packet)
{

}

void TestReceiver::onReset()
{

}

void TestReceiver::onConnect()
{

}

void TestReceiver::onBackoffTimerExpired()
{

}

}
