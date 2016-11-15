#ifndef _TEST_CONNECTION_PRODUCER_CLIENT_H_
#define _TEST_CONNECTION_PRODUCER_CLIENT_H_


#include "iconnectionproducerclient.h"

#include <algorithm>
#include <string>
#include <vector>
#include <boost/thread/shared_mutex.hpp>


namespace Testing
{

class TestConnectionProducerClient: public Model::IConnectionProducerClient
{
public:

    //! default constructor
    TestConnectionProducerClient();

    //! destructor
    virtual ~TestConnectionProducerClient();

    void initialise();

    virtual void openConnection();
    bool wasConnectionOpened() const { return m_connectionWasOpened; }

    virtual bool send(FastDataPacket_shared_ptr& pDataPacket, bool closeConnectionAfterSendingThisPacket);

    virtual bool send(TSendDataPacket_shared_ptr& data, bool closeConnectionAfterSendingThisPacket);

    virtual void closeConnection();
    bool wasConnectionClosed() const { return m_connectionWasClosed; }

    virtual void cancelConnection();
    bool wasConnectionCancelled() const { return m_connectionWasCancelled; }

    virtual bool isConnected();
    void setIsConnectedResult(const bool value) { m_isConnectedResult = value; }

    virtual std::string getLocalAddress() const;

    virtual std::string getRemoteAddress() const;
    void setRemoteAddress(const std::string& remoteAddress);

    virtual unsigned int getRemotePortNumber() const;
    void setRemotePortNumber(const unsigned int portNumber);

    std::vector<TSendDataPacket_shared_ptr> getDataToBeSentCollection();

    bool getCloseConnectionAfterSendingThisPacket() const;

private:
    //! copy constructor. Not implemented
    TestConnectionProducerClient(const TestConnectionProducerClient& ) = delete;
    //! assignment operator. Not implemented
    TestConnectionProducerClient& operator=(const TestConnectionProducerClient& ) = delete;

    std::string m_localAddress;
    std::string m_remoteAddress;
    int m_remotePortNumber;
    std::vector<TSendDataPacket_shared_ptr> m_dataToBeSentCollection;
    bool m_closeConnectionAfterSendingThisPacket;
    bool m_connectionWasOpened;
    bool m_connectionWasClosed;
    bool m_connectionWasCancelled;
    bool m_isConnectedResult;

    mutable boost::shared_mutex m_mutex;
};

}

#endif //_TEST_CONNECTION_PRODUCER_CLIENT_H_
