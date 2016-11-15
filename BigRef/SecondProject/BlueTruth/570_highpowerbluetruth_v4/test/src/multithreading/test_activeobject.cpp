#include "test_activeobject.h"


namespace Testing
{

TestActiveObject::TestActiveObject(
    const char* name,
    const size_t stackSize,
    const int priority)
:
ActiveObject(name, stackSize, priority),
m_localMutex(),
_initThreadCalled(false),
_runCalled(false),
_flushThreadCalled(false)
{
}

TestActiveObject::~TestActiveObject()
{

}

void TestActiveObject::initThread()
{
    boost::lock_guard<boost::mutex> lock(m_localMutex);
    _initThreadCalled = true;
}

void TestActiveObject::run()
{
    {
        boost::lock_guard<boost::mutex> lock(m_localMutex);
        _runCalled = true;
    }

    do
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(10));
    }
    while (!isDying());
}

void TestActiveObject::flushThread()
{
    boost::lock_guard<boost::mutex> lock(m_localMutex);
    _flushThreadCalled = true;
}

void TestActiveObject::resumeThread()
{
    ActiveObject::resumeThread();
}

void TestActiveObject::clearFlags()
{
    boost::lock_guard<boost::mutex> lock(m_localMutex);
    _initThreadCalled = false;
    _runCalled = false;
    _flushThreadCalled = false;
}

bool TestActiveObject::isInitThreadCalled() const
{
    boost::lock_guard<boost::mutex> lock(m_localMutex);
    return _initThreadCalled;
}

bool TestActiveObject::isRunCalled() const
{
    boost::lock_guard<boost::mutex> lock(m_localMutex);
    return _runCalled;
}

bool TestActiveObject::isFlushThreadCalled() const
{
    boost::lock_guard<boost::mutex> lock(m_localMutex);
    return _flushThreadCalled;
}

}
