#include "stdafx.h"
#include "boostsemaphore.h"


#include <cassert>
#include <iostream>


BoostSemaphore::BoostSemaphore(const char* name, const unsigned int maximumCount, const unsigned int initialCount )
:
M_NAME(name),
M_MAXIMUM_COUNT(maximumCount),
m_count(initialCount),
m_mutex(),
m_conditionVariable()
{
    assert(M_MAXIMUM_COUNT > 0);
}

BoostSemaphore::~BoostSemaphore()
{
    reset();
    release();
}

unsigned int BoostSemaphore::get_count() //for debugging/testing only
{
    boost::unique_lock<boost::mutex> lock(m_mutex);
    return m_count;
}

void BoostSemaphore::release()
{
    {
        boost::lock_guard<boost::mutex> lock(m_mutex);
        if (++m_count > M_MAXIMUM_COUNT)
        {
            std::cerr << "ERROR: M_MAXIMUM_COUNT=" << M_MAXIMUM_COUNT << ", m_count=" << m_count << std::endl;
        }
        //else do nothing
    }

    //Wake up any waiting threads.
    //Always do this, even if count_ wasn't 0 on entry.
    //Otherwise, we might not wake up enough waiting threads if we
    //get a number of signal() calls in a row.
    m_conditionVariable.notify_one();
}

void BoostSemaphore::wait()
{
    boost::unique_lock<boost::mutex> lock(m_mutex);
    if (m_count>0)
    {
        m_count--;
    }
    else
    {
        m_conditionVariable.wait(lock);

        assert(m_count>0);
        m_count--;
    }
}

bool BoostSemaphore::wait(const unsigned int timeoutInMilliseconds)
{
    bool result = true;
    boost::unique_lock<boost::mutex> lock(m_mutex);
    if (m_count > 0)
    {
        --m_count;
    }
    else
    {
        result = m_conditionVariable.timed_wait(
                    lock,
                    boost::posix_time::milliseconds(timeoutInMilliseconds));
        if (result)
        {
            assert(m_count>0);
            --m_count;
        }
        //else do nothing
    }

    return result;
}

void BoostSemaphore::reset()
{
    boost::lock_guard<boost::mutex> lock(m_mutex);
    m_count = 0;
}
