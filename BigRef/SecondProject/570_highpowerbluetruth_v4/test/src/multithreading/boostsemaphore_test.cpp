#include "stdafx.h"
#include <gtest/gtest.h>

#include "boostsemaphore.h"

#include "activeobject.h"
#include <boost/thread/thread.hpp>


namespace
{

const unsigned int SEMAPHORE_SLEEP_PERIOD_MS = 10;
const unsigned int SEMAPHORE_MAX_COUNT = 4;

class TestActiveObject : public ActiveObject
{
public:
    TestActiveObject(const char* name)
    :
    ActiveObject(name),
    m_semaphore(name, SEMAPHORE_MAX_COUNT),
    m_counter(0),
    m_waitWithTimeout(false),
    m_waitWithTimeoutResult(false),
    m_shouldIdleInTheLoop(false),
    m_mutex()
    {}

    virtual ~TestActiveObject() {}

    virtual void run()
    {
        while (!runShouldFinish())
        {
            if (getWaitWithTimeout())
            {
                setWaitWithTimeoutResult(m_semaphore.wait(SEMAPHORE_SLEEP_PERIOD_MS));
            }
            else
            {
                m_semaphore.wait();
            }

            incCounter();

            while (shouldIdleInTheLoop())
            {
                boost::this_thread::sleep(boost::posix_time::milliseconds(1));
            }
        }
    }

    bool shouldIdleInTheLoop() const
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        return m_shouldIdleInTheLoop;
    }

    void setShouldIdleInTheLoop(const bool shouldIdleInTheLoop)
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        m_shouldIdleInTheLoop = shouldIdleInTheLoop;
    }

    int getCounter() const
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        return m_counter;
    }

    void incCounter()
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        m_counter++;
    }

    bool getWaitWithTimeout() const
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        return m_waitWithTimeout;
    }

    void setWaitWithTimeout(const bool waitWithTimeout)
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        m_waitWithTimeout = waitWithTimeout;
    }

    bool getWaitWithTimeoutResult() const
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        return m_waitWithTimeoutResult;
    }

    void setWaitWithTimeoutResult(const bool waitWithTimeoutResult)
    {
        boost::unique_lock<boost::mutex> lock(m_mutex);
        m_waitWithTimeoutResult = waitWithTimeoutResult;
    }

    BoostSemaphore m_semaphore;

private:
    int m_counter;
    bool m_waitWithTimeout;
    bool m_waitWithTimeoutResult;
    bool m_shouldIdleInTheLoop;
    mutable boost::mutex m_mutex;
};

}

TEST(boostsemaphore, no_timeout)
{
    TestActiveObject object("Test");
    object.resumeThread();

    boost::this_thread::sleep(boost::posix_time::milliseconds(100));

    EXPECT_EQ(0, object.getCounter());
    EXPECT_EQ(0, object.m_semaphore.get_count());

    object.m_semaphore.release();
    //give some time to thread schedule to switch to our thread
    boost::this_thread::sleep(boost::posix_time::milliseconds(10));
    EXPECT_EQ(1, object.getCounter());
    EXPECT_EQ(0, object.m_semaphore.get_count());

    object.setShouldIdleInTheLoop(true);
    object.m_semaphore.release();
    //give some time to thread schedule to switch to our thread
    boost::this_thread::sleep(boost::posix_time::milliseconds(10));
    EXPECT_EQ(2, object.getCounter());
    EXPECT_EQ(0, object.m_semaphore.get_count());

    object.m_semaphore.release();
    EXPECT_EQ(1, object.m_semaphore.get_count());
    object.m_semaphore.release();
    EXPECT_EQ(2, object.m_semaphore.get_count());
    object.m_semaphore.release();
    EXPECT_EQ(3, object.m_semaphore.get_count());
    object.m_semaphore.release();
    EXPECT_EQ(4, object.m_semaphore.get_count());
    object.m_semaphore.release();
    EXPECT_EQ(5, object.m_semaphore.get_count());
    EXPECT_EQ(2, object.getCounter());

    object.setShouldIdleInTheLoop(false);

    //give some time to thread schedule to switch to our thread
    boost::this_thread::sleep(boost::posix_time::milliseconds(10));
    EXPECT_EQ(7, object.getCounter());

    while (object.m_semaphore.get_count() > 0)
    {
        object.m_semaphore.release();
        //give some time to thread schedule to switch to our thread
        boost::this_thread::sleep(boost::posix_time::milliseconds(1));
    }

    object.setWaitWithTimeout(true);
    object.setShouldIdleInTheLoop(false);
    object.m_semaphore.release();
    object.shutdownThread("me");
}

TEST(boostsemaphore, timeout)
{
    TestActiveObject object("Test");
    object.setWaitWithTimeout(true);
    object.setShouldIdleInTheLoop(true);
    object.resumeThread();
    EXPECT_EQ(0, object.getCounter());

    boost::this_thread::sleep(boost::posix_time::milliseconds(100));

    EXPECT_EQ(1, object.getCounter());
    EXPECT_FALSE(object.getWaitWithTimeoutResult());
    EXPECT_EQ(0, object.m_semaphore.get_count());

    object.setShouldIdleInTheLoop(false);
    //give some time to thread schedule to switch to our thread and exit the loop
    boost::this_thread::sleep(boost::posix_time::milliseconds(5));
    object.setShouldIdleInTheLoop(true);
    object.m_semaphore.release();
    //give some time to thread schedule to switch to our thread and increase counter
    boost::this_thread::sleep(boost::posix_time::milliseconds(5));
    EXPECT_EQ(2, object.getCounter());
    EXPECT_TRUE(object.getWaitWithTimeoutResult());
    EXPECT_EQ(0, object.m_semaphore.get_count());

    object.m_semaphore.release();
    EXPECT_EQ(1, object.m_semaphore.get_count());
    object.setShouldIdleInTheLoop(false);
    //give some time to thread schedule to switch to our thread and exit the loop
    boost::this_thread::sleep(boost::posix_time::milliseconds(3));
    object.setShouldIdleInTheLoop(true);
    //give some time to thread schedule to switch to our thread and increase counter
    boost::this_thread::sleep(boost::posix_time::milliseconds(3));
    EXPECT_EQ(3, object.getCounter());
    EXPECT_TRUE(object.getWaitWithTimeoutResult());
    EXPECT_EQ(0, object.m_semaphore.get_count());


    object.setWaitWithTimeout(true);
    object.setShouldIdleInTheLoop(false);
    object.m_semaphore.release();
    object.shutdownThread("me");
}
