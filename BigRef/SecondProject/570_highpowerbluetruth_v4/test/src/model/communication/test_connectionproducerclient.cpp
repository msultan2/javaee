#include "test_connectionproducerclient.h"

#include <boost/thread/shared_mutex.hpp>


using Model::IConnectionProducerClient;


namespace Testing
{


TestConnectionProducerClient::TestConnectionProducerClient()
:
IConnectionProducerClient(),
m_localAddress(),
m_remoteAddress(),
m_remotePortNumber(0),
m_dataToBeSentCollection(),
m_closeConnectionAfterSendingThisPacket(false),
m_connectionWasOpened(false),
m_connectionWasClosed(false),
m_connectionWasCancelled(false),
m_isConnectedResult(false)
{

}

TestConnectionProducerClient::~TestConnectionProducerClient()
{

}

void TestConnectionProducerClient::initialise()
{
    m_dataToBeSentCollection.clear();
    m_closeConnectionAfterSendingThisPacket = false;
    m_connectionWasOpened = false;
    m_connectionWasClosed = false;
    m_connectionWasCancelled = false;
    m_isConnectedResult = false;
}

void TestConnectionProducerClient::openConnection()
{
    m_connectionWasOpened = true;
}

bool TestConnectionProducerClient::send(FastDataPacket_shared_ptr& pDataPacket, bool closeConnectionAfterSendingThisPacket)
{
    boost::unique_lock<boost::shared_mutex> lock(m_mutex);
    TSendDataPacket_shared_ptr pData(new TSendDataPacket());
    pData->first = 0;
    pData->second = pDataPacket;
    m_dataToBeSentCollection.push_back(pData);
    m_closeConnectionAfterSendingThisPacket = closeConnectionAfterSendingThisPacket;

    return true;
}

bool TestConnectionProducerClient::send(TSendDataPacket_shared_ptr& data, bool closeConnectionAfterSendingThisPacket)
{
    boost::unique_lock<boost::shared_mutex> lock(m_mutex);
    m_dataToBeSentCollection.push_back(data);
    m_closeConnectionAfterSendingThisPacket = closeConnectionAfterSendingThisPacket;

    return true;
}

void TestConnectionProducerClient::closeConnection()
{
    m_connectionWasClosed = true;
}

void TestConnectionProducerClient::cancelConnection()
{
    m_connectionWasCancelled = true;
}

bool TestConnectionProducerClient::isConnected()
{
    return m_isConnectedResult;
}

std::string TestConnectionProducerClient::getLocalAddress() const
{
    boost::shared_lock<boost::shared_mutex> lock(m_mutex);
    return m_localAddress;
}

std::string TestConnectionProducerClient::getRemoteAddress() const
{
    boost::shared_lock<boost::shared_mutex> lock(m_mutex);
    return m_remoteAddress;
}

void TestConnectionProducerClient::setRemoteAddress(const std::string& remoteAddress)
{
    boost::unique_lock<boost::shared_mutex> lock(m_mutex);
    m_remoteAddress = remoteAddress;
}

unsigned int TestConnectionProducerClient::getRemotePortNumber() const
{
    boost::shared_lock<boost::shared_mutex> lock(m_mutex);
    return m_remotePortNumber;
}

void TestConnectionProducerClient::setRemotePortNumber(const unsigned int portNumber)
{
    boost::unique_lock<boost::shared_mutex> lock(m_mutex);
    m_remotePortNumber = portNumber;
}

std::vector<IConnectionProducerClient::TSendDataPacket_shared_ptr> TestConnectionProducerClient::getDataToBeSentCollection()
{
    boost::shared_lock<boost::shared_mutex> lock(m_mutex);
    return m_dataToBeSentCollection;
}

bool TestConnectionProducerClient::getCloseConnectionAfterSendingThisPacket() const
{
    boost::shared_lock<boost::shared_mutex> lock(m_mutex);
    return m_closeConnectionAfterSendingThisPacket;
}

}
