#include "test_connectionconsumer.h"

#include "logger.h"

#include <iostream>
#include <sstream>


namespace Testing
{

TestConnectionConsumer::TestConnectionConsumer()
:
Model::IConnectionConsumer(),
m_wasSuccesfullyOpened(false),
m_couldNotBeOpened(false),
m_wasClosed(false)
{

}

TestConnectionConsumer::~TestConnectionConsumer()
{

}

bool TestConnectionConsumer::isFull() const
{
    return false;
}

void TestConnectionConsumer::onReceive(FastDataPacket_shared_ptr& packet)
{
    if (packet)
    {
        std::ostringstream ss;
        ss << "Packet received: " << packet->dumpData() << ": " << packet->data();
        Logger::log(
            LOG_LEVEL_INFO,
            "ActiveBoostAsioTCPClient::onReceive()",
            ss.str().c_str());
    }
}

void TestConnectionConsumer::onSend(const bool success, FastDataPacket_shared_ptr& pPacket)
{
    if (success)
    {
        if (pPacket)
        {
            pPacket->fixForDisplaying();

            std::ostringstream ss;
            ss << "Packet sent: " << pPacket->dumpData() << ": " << pPacket->c_str();
            Logger::log(
                LOG_LEVEL_INFO,
                "ActiveBoostAsioTCPClient::onSend()",
                ss.str().c_str());
        }
        //else do not display
    }
    else
    {
        if (pPacket)
        {
            pPacket->fixForDisplaying();

            std::ostringstream ss;
            ss << "Packet failed to sent: " << pPacket->dumpData() << ": " << pPacket->c_str();
            Logger::log(
                LOG_LEVEL_ERROR,
                "ActiveBoostAsioTCPClient::onSend()",
                ss.str().c_str());
        }
        //else do not display
    }
}

void TestConnectionConsumer::onSend(const bool success, TSendDataPacket_shared_ptr& data)
{
    if (success)
    {
        if (data->second)
        {
            data->second->fixForDisplaying();

            std::ostringstream ss;
            ss << "Packet sent: " << data->second->dumpData() << ": " << data->second->c_str();
            Logger::log(
                LOG_LEVEL_INFO,
                "ActiveBoostAsioTCPClient::onSend()",
                ss.str().c_str());
        }
        //else do not display
    }
    else
    {
        if (data->second)
        {
            std::ostringstream ss;
            ss << "Packet failed to sent: " << data->second->dumpData() << ": " << data->second->c_str();
            Logger::log(
                LOG_LEVEL_ERROR,
                "ActiveBoostAsioTCPClient::onSend()",
                ss.str().c_str());
        }
        //else do not display
    }
}

void TestConnectionConsumer::onConnect(const bool success)
{
    boost::unique_lock<boost::mutex> lock(m_mutex);
    m_wasSuccesfullyOpened = success;
    m_couldNotBeOpened = !success;

    IConnectionConsumer::onConnect(success);
}

void TestConnectionConsumer::onClose()
{
    boost::unique_lock<boost::mutex> lock(m_mutex);
    m_wasClosed = true;

    IConnectionConsumer::onClose();
}

void TestConnectionConsumer::onBackoffTimerExpired()
{
    IConnectionConsumer::onBackoffTimerExpired();
}

}
