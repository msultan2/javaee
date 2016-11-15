#include "stdafx.h"
#include <gtest/gtest.h>

#include "activeboostasio.h"
#include "boostasio.h"

#include "logger.h"

#include <boost/system/error_code.hpp>


using Model::ActiveBoostAsio;
using Model::BoostAsio;


TEST(ActiveBoostAsio, run)
{
    const char NAME[] = "name";
    ActiveBoostAsio connection(NAME);
    EXPECT_STREQ(NAME, connection.getName().c_str());
    connection.start();

    connection.waitUntilIoServiceRunning();

    connection.stop();
}


class TestActiveBoostAsio : public ActiveBoostAsio
{
public:
    TestActiveBoostAsio(const char* name) : ActiveBoostAsio(name) {}

    virtual ~TestActiveBoostAsio() {}

    boost::shared_ptr<boost::asio::io_service>& getIoService() { return m_pIoService; }

    void stopIoServiceAndDoNotRestartIt()
    {
        m_doNotRestartIoService.set(true);
        m_pWork.reset();
    }

    static void raiseAnException()
    {
        nonAsioExceptionThrown = true;
        throw( std::runtime_error( "Oops!" ) );
    }

    static void raiseAnAsioException()
    {
        asioExceptionThrown = true;
        boost::asio::ip::address::from_string("0");
    }

    static void resetStaticMembers()
    {
        asioExceptionThrown = false;
        nonAsioExceptionThrown = false;
    }

    static bool nonAsioExceptionThrown;
    static bool asioExceptionThrown;
};

bool TestActiveBoostAsio::nonAsioExceptionThrown = false;
bool TestActiveBoostAsio::asioExceptionThrown = false;


TEST(ActiveBoostAsio, not_ASIO_exception)
{
    const char NAME[] = "name";
    TestActiveBoostAsio connection(NAME);
    EXPECT_STREQ(NAME, connection.getName().c_str());
    connection.start();

    connection.waitUntilIoServiceRunning();

    EXPECT_FALSE(connection.nonAsioExceptionThrown);
	connection.getIoService()->post(&TestActiveBoostAsio::raiseAnException);
    boost::this_thread::sleep(boost::posix_time::milliseconds(10));

    connection.stop();

    connection.shutdownThread("TestActiveBoostAsio");

    EXPECT_FALSE(connection.asioExceptionThrown);
    EXPECT_TRUE(connection.nonAsioExceptionThrown);

    connection.resetStaticMembers();
}

TEST(ActiveBoostAsio, ASIO_exception)
{
    const char NAME[] = "name";
    TestActiveBoostAsio connection(NAME);
    EXPECT_STREQ(NAME, connection.getName().c_str());
    connection.start();

    connection.waitUntilIoServiceRunning();

    EXPECT_FALSE(connection.asioExceptionThrown);
	connection.getIoService()->post(&TestActiveBoostAsio::raiseAnAsioException);
    boost::this_thread::sleep(boost::posix_time::milliseconds(10));

    connection.stop();

    boost::this_thread::sleep(boost::posix_time::milliseconds(10));

    connection.shutdownThread("TestActiveBoostAsio");
    EXPECT_FALSE(connection.nonAsioExceptionThrown);
    EXPECT_TRUE(connection.asioExceptionThrown);

    connection.resetStaticMembers();
}

TEST(ActiveBoostAsio, run_with_idling)
{
    const char NAME[] = "name";
    TestActiveBoostAsio connection(NAME);
    EXPECT_STREQ(NAME, connection.getName().c_str());
    connection.start();

    connection.waitUntilIoServiceRunning();

    connection.stopIoServiceAndDoNotRestartIt();
    boost::this_thread::sleep(boost::posix_time::milliseconds(10));

    connection.stop();

    connection.shutdownThread("ActiveBoostAsio");
}

TEST(ActiveBoostAsio, logError)
{
    Logger::setConsoleLogLevel(LOG_LEVEL_DEBUG3);
    Logger::setFileLogLevel(LOG_LEVEL_DEBUG3);

    boost::system::error_code ec;
    BoostAsio::logError("identifier", "additionalText", ec);
    //How do I check the log?

    ec = boost::asio::error::eof;
    BoostAsio::logError("identifier", "additionalText", ec);
    BoostAsio::logError("identifier", "additionalText", ec);
}
