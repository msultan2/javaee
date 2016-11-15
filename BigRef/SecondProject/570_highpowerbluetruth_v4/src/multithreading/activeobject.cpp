#include "activeobject.h"


ActiveObject::ActiveObject(const char* name, const size_t stackSize, const int )
:
m_state(eIDLE),
#if BOOST_VERSION >= 105000
m_attrs(),
#endif
m_globalMutex(),
m_stateConditionVariable(),
m_pThread(),
M_NAME(name)
{
#if BOOST_VERSION >= 105000
    m_attrs.set_stack_size(stackSize);
#endif
}

ActiveObject::~ActiveObject()
{
    shutdownThread(M_NAME.c_str());
}

void ActiveObject::resumeThread()
{
    {
        boost::lock_guard<boost::mutex> lock(m_globalMutex);
        if (m_state == eIDLE)
        {
            m_state = eDEPLOYING;
        }
        else
        {
            return;
        }
    }

#if BOOST_VERSION >= 105000
    m_pThread =
        boost::shared_ptr<boost::thread>(new boost::thread(
            m_attrs,
            boost::bind(&ActiveObject::main, this)));
#else
    m_pThread =
        boost::shared_ptr<boost::thread>(new boost::thread(
            boost::bind(&ActiveObject::main, this)));
#endif
}

void ActiveObject::main()
{
    {
        boost::lock_guard<boost::mutex> lock(m_globalMutex);
        m_state = eRUNNING;
    }
    m_stateConditionVariable.notify_one();

    initThread();
    run();
    flushThread();

    {
        boost::mutex::scoped_lock lock(m_globalMutex);
        m_pThread.reset();
        m_state = eCOMPLETED;
    }
}

void ActiveObject::shutdownThread(const char* )
{
    boost::shared_ptr<boost::thread> pThread;

    {
        boost::mutex::scoped_lock lock(m_globalMutex);
        if (m_state != eRUNNING)
        {
            return;
        }

        pThread = m_pThread;
        m_state = eFINISHING;
    }

    if (pThread != 0)
        pThread->join();
}

void ActiveObject::initThread()
{

}

void ActiveObject::run()
{
    do
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(10));
    }
    while (!runShouldFinish());
}

void ActiveObject::flushThread()
{

}
