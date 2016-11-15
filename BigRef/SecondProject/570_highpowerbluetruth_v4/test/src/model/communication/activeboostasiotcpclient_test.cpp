#include "stdafx.h"
#include <gtest/gtest.h>

#include "activeboostasio.h"
#include "activeboostasiotcpclient.h"
#include "boostasio.h"
#include "test_connectionconsumer.h"

#include "logger.h"


using Model::ActiveBoostAsio;
using Model::ActiveBoostAsioTCPClient;
using Model::BoostAsio;
using Testing::TestConnectionConsumer;

typedef ActiveBoostAsioTCPClient::TSendDataPacket_shared_ptr TSendDataPacket_shared_ptr;
typedef ActiveBoostAsioTCPClient::TSendDataPacket TSendDataPacket;

namespace
{
    const int DEFAULT_ID = 1;
}

TEST(ActiveBoostAsioTCPClient, connect_to_local_running_date_time_service)
{
    ActiveBoostAsio hive("Hive");
    const int id = 7;
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(id, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->start();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("localhost", 13, "");

    client->openConnection();

    int i=0;
    while (
        (i++<100) &&
        !(commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened())
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(10));
    }

    client->closeConnection();
    while ((i++<100) && !commsClient.wasClosed())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(10));
    }

    EXPECT_TRUE(commsClient.wasSuccesfullyOpened());
    EXPECT_FALSE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();
    client->stop();
}

TEST(ActiveBoostAsioTCPClient, connect_to_local_running_date_time_service_via_send_packet)
{
    ActiveBoostAsio hive("Hive");
    const int id = 5;
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(id, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);
    client->setMaxNumberOfSendPacketsToStore(1);

    hive.start();
    client->start();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("localhost", 13, "localhost");

    //Instead of calling client->openConnection() we request a packet to be sent
    FastDataPacket_shared_ptr packet(new FastDataPacket("GET http://www.google.com/index.html HTTP/1.0\x0d\x0a\x0d\x0a"));
    EXPECT_TRUE(client->send(packet));
    //max number of send packets is 1 so the next one should not fit
    EXPECT_FALSE(client->send(packet));

    int i=0;
    while (
        (i++<100) &&
        !(commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened())
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    client->closeConnection();
    while ((i++<100) && !commsClient.wasClosed())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    EXPECT_TRUE(commsClient.wasSuccesfullyOpened());
    EXPECT_FALSE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();
    client->stop();
}

TEST(ActiveBoostAsioTCPClient, connect_to_local_running_date_time_service_via_send_packet_pair)
{
    ActiveBoostAsio hive("Hive");
    const int id = 5;
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(id, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);
    client->setMaxNumberOfSendPacketsToStore(1);

    hive.start();
    client->start();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("localhost", 13, "localhost");
    EXPECT_STREQ("localhost", client->getLocalAddress().c_str());
    EXPECT_STREQ("localhost", client->getRemoteAddress().c_str());
    EXPECT_EQ(13, client->getRemotePortNumber());

    //Instead of calling client->openConnection() we request a packet to be sent
    FastDataPacket_shared_ptr packet(new FastDataPacket("GET http://www.google.com/index.html HTTP/1.0\x0d\x0a\x0d\x0a"));
    TSendDataPacket_shared_ptr pData(new TSendDataPacket(15, packet));
    EXPECT_TRUE(client->send(pData, false));
    //max number of send packets is 1 so the next one should not fit
    EXPECT_FALSE(client->send(pData, false));

    int i=0;
    while (
        (i++<100) &&
        !(commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened())
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    client->closeConnection();
    while ((i++<100) && !commsClient.wasClosed())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    EXPECT_TRUE(commsClient.wasSuccesfullyOpened());
    EXPECT_FALSE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();
    client->stop();
}

TEST(ActiveBoostAsioTCPClient, connect_to_local_running_date_time_service_via_empty_send_packet)
{
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(DEFAULT_ID, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->resumeThread();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("localhost", 13, "127.0.0.1");

    //Instead of calling client->openConnection() we request a packet to be sent
    FastDataPacket_shared_ptr packet(new FastDataPacket);
    client->send(packet);

    int i=0;
    while (
        (i++<100) &&
        !(commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened())
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    client->closeConnection();
    while ((i++<100) && !commsClient.wasClosed())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    EXPECT_TRUE(commsClient.wasSuccesfullyOpened());
    EXPECT_FALSE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();

    client->shutdownThread();
    hive.shutdownThread();
}

TEST(ActiveBoostAsioTCPClient, cancel_closed_connection_and_connect_to_local_running_date_time_service)
{
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(DEFAULT_ID, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->resumeThread();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("localhost", 13, "127.0.0.1");

    client->cancelConnection();

    client->openConnection();

    int i=0;
    while (
        (i++<100) &&
        !(commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened())
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    client->closeConnection();
    while ((i++<100) && !commsClient.wasClosed())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    EXPECT_TRUE(commsClient.wasSuccesfullyOpened());
    EXPECT_FALSE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();

    client->shutdownThread();
    hive.shutdownThread();
}

TEST(ActiveBoostAsioTCPClient, connect_to_google_but_store_to_send_packet_before_connecting)
{
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(DEFAULT_ID, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->resumeThread();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("google.com", 80, "");
    EXPECT_STREQ("google.com", client->getRemoteAddress().c_str());
    EXPECT_STREQ("", client->getLocalAddress().c_str());
    EXPECT_EQ(80, client->getRemotePortNumber());

    client->openConnection();

    FastDataPacket_shared_ptr packet(new FastDataPacket("GET http://www.google.com/index.html HTTP/1.0\x0d\x0a\x0d\x0a"));
    client->send(packet);

    int i = 0;
    while (
        (i++<100) &&
        !(commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened())
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    client->closeConnection();
    boost::this_thread::sleep(boost::posix_time::milliseconds(10));

    while ((i++<100) && !commsClient.wasClosed())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    EXPECT_TRUE(commsClient.wasSuccesfullyOpened());
    EXPECT_FALSE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();

    client->shutdownThread();
    hive.shutdownThread();
}

TEST(ActiveBoostAsioTCPClient, connect_to_google_and_then_send_a_packet)
{
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(DEFAULT_ID, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->resumeThread();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("google.com", 80, "");

    client->openConnection();

    int i = 0;
    while (
        (i++<100) &&
        !(commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened())
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    FastDataPacket_shared_ptr packet(new FastDataPacket("GET http://www.google.com/index.html HTTP/1.0\x0d\x0a\x0d\x0a"));
    client->send(packet, true);

    while ((i++<100) && !commsClient.wasClosed())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    client->closeConnection();
    boost::this_thread::sleep(boost::posix_time::milliseconds(10));

    EXPECT_TRUE(commsClient.wasSuccesfullyOpened());
    EXPECT_FALSE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();

    client->shutdownThread();
    hive.shutdownThread();
}

TEST(ActiveBoostAsioTCPClient, connect_to_google_and_then_send_a_packet_requesting_connection_closure)
{
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(DEFAULT_ID, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->resumeThread();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("google.com", 80, "");

    client->openConnection();

    int i=0;
    while (
        (i++<100) &&
        !(commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened())
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    FastDataPacket_shared_ptr packet(new FastDataPacket("GET http://www.google.com/index.html HTTP/1.0\x0d\x0a\x0d\x0a"));
    //Send this packet and request to close the connection
    client->send(packet, true);

    while ((i++<100) && !commsClient.wasClosed())
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    EXPECT_TRUE(commsClient.wasSuccesfullyOpened());
    EXPECT_FALSE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();

    client->shutdownThread();
    hive.shutdownThread();
}

TEST(ActiveBoostAsioTCPClient, connect_to_google_interrupted)
{
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(DEFAULT_ID, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->resumeThread();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("google.com", 80, "");

    client->openConnection();
    FastDataPacket_shared_ptr packet(new FastDataPacket("GET http://www.google.com/index.html HTTP/1.0\x0d\x0a\x0d\x0a"));
    client->send(packet);
    client->cancelConnection();

    int i=0;
    while ((i++<100) && !commsClient.wasClosed()) //we cannot check if it connected or not because it is random
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    //we cannot check if it connected or not because it is random
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();

    client->shutdownThread();
    hive.shutdownThread();
}

TEST(ActiveBoostAsioTCPClient, connect_to_invalid_address)
{
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(DEFAULT_ID, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->resumeThread();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("a.b.c.d.e.f.g", 80, "");
    client->setConnectingTimeout(10);

    client->openConnection();

    FastDataPacket_shared_ptr packet(new FastDataPacket("GET http://www.google.com/index.html HTTP/1.0\x0d\x0a\x0d\x0a"));
    client->send(packet);

    client->closeConnection();
    boost::this_thread::sleep(boost::posix_time::milliseconds(100));

    int i=0;
    while (
        (i++<100) &&
        !(
            (commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened()) &&
            commsClient.wasClosed()
        )
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    EXPECT_FALSE(commsClient.wasSuccesfullyOpened());
    EXPECT_TRUE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();

    client->shutdownThread();
    hive.shutdownThread();
}

TEST(ActiveBoostAsioTCPClient, connect_to_google_via_invalid_address1)
{
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(DEFAULT_ID, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->resumeThread();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("google.com", 80, "1.2.3.4"); //invalid local interface address
    client->setConnectingTimeout(10);

    client->openConnection();
    FastDataPacket_shared_ptr packet(new FastDataPacket("GET http://www.google.com/index.html HTTP/1.0\x0d\x0a\x0d\x0a"));
    client->send(packet, true);

    int i=0;
    while (
        (i++<100) &&
        !(
            (commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened()) &&
            commsClient.wasClosed()
        )
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    EXPECT_FALSE(commsClient.wasSuccesfullyOpened());
    EXPECT_TRUE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();

    client->shutdownThread();
    hive.shutdownThread();
}

TEST(ActiveBoostAsioTCPClient, connect_to_google_via_invalid_address2)
{
    ActiveBoostAsio hive("Hive");
    boost::shared_ptr<ActiveBoostAsioTCPClient> client(new ActiveBoostAsioTCPClient(DEFAULT_ID, hive.getIoService()));
    TestConnectionConsumer commsClient;
    client->setup(&commsClient);

    hive.start();
    client->resumeThread();

    hive.waitUntilIoServiceRunning();

    client->setupConnection("google.com", 80, "invalid address"); //invalid local interface address
    client->setConnectingTimeout(10);

    client->openConnection();
    FastDataPacket_shared_ptr packet(new FastDataPacket("GET http://www.google.com/index.html HTTP/1.0\x0d\x0a\x0d\x0a"));
    client->send(packet, true);

    int i=0;
    while (
        (i++<100) &&
        !(
            (commsClient.wasSuccesfullyOpened() || commsClient.couldNotBeOpened()) &&
            commsClient.wasClosed()
        )
        )
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(100));
    }

    EXPECT_FALSE(commsClient.wasSuccesfullyOpened());
    EXPECT_TRUE(commsClient.couldNotBeOpened());
    EXPECT_TRUE(commsClient.wasClosed());

    hive.stop();

    client->shutdownThread();
    hive.shutdownThread();
}

TEST(ActiveBoostAsioTCPClient, verifyAddress)
{
    EXPECT_TRUE(ActiveBoostAsioTCPClient::verifyAddress("127.0.0.1"));
    EXPECT_TRUE(ActiveBoostAsioTCPClient::verifyAddress("localhost"));
    EXPECT_FALSE(ActiveBoostAsioTCPClient::verifyAddress("127.0.0.257"));
    EXPECT_FALSE(ActiveBoostAsioTCPClient::verifyAddress("x.y.z.b.c.d.e.f"));
}
