#include "stdafx.h"
#include "activetask.h"

#include "os_utilities.h"

//#define USE_LOGGER
#ifdef USE_LOGGER
#include "logger.h"
#endif

#include <cassert>


namespace
{
    const unsigned long DEFAULT_SLEEP_PERIOD_MS = 10;

    const char MODULE_NAME[] = "ActiveTask";
}


namespace Model
{

ActiveTask::ActiveTask(
    boost::shared_ptr<ITask> pTask,
    const char* name)
:
::ActiveObject(name),
m_pTask(pTask),
m_sleepTime(DEFAULT_SLEEP_PERIOD_MS),
m_doSleep(true),
m_actionMutex()
{
    assert(m_pTask!=0);
}

ActiveTask::~ActiveTask()
{
    while (!isCompleted())
    {
        OS_Utilities::sleep(1);
    }
}

void ActiveTask::setSleepTime(const unsigned int sleepTime)
{
    m_sleepTime = sleepTime;
}

void ActiveTask::doNotSleep()
{
    m_doSleep = false;
}

void ActiveTask::start()
{
    {
        boost::lock_guard<boost::mutex> lock(m_actionMutex);
        m_pTask->initialise();
    }

    //run the thread
    resumeThread();

#ifdef USE_LOGGER
    std::string ss;
    ss += "Task ";
    ss += getName();
    ss += " has been started";
    Logger::log(LOG_LEVEL_DEBUG3, ss.c_str());
#endif
}

void ActiveTask::stop()
{
    {
        boost::lock_guard<boost::mutex> lock(m_actionMutex);
        m_pTask->stop();
    }
}

void ActiveTask::shutdown(const char* requestorName)
{
#ifdef USE_LOGGER
    {
        std::string ss;
        ss += "Task ";
        ss += getName();
        ss += " is requested to stop";
        Logger::log(LOG_LEVEL_DEBUG3, ss.c_str());
    }
#endif

    {
        boost::lock_guard<boost::mutex> lock(m_actionMutex);
        m_pTask->stop();
    }

    shutdownThread(requestorName);

#ifdef USE_LOGGER
    {
        std::string ss;
        ss += "Task ";
        ss += getName();
        ss += " has been stopped";
        Logger::log(LOG_LEVEL_DEBUG3, ss.c_str());
    }
#endif
}

void ActiveTask::shutdown(
    const char* requestorName,
    THookFunction pHookFunction,
    void* pHookFunctionParameter)
{
#ifdef USE_LOGGER
    {
        std::string ss;
        ss += "Task ";
        ss += getName();
        ss += " is requested to stop";
        Logger::log(LOG_LEVEL_DEBUG3, ss.c_str());
    }
#endif

    {
        boost::lock_guard<boost::mutex> lock(m_actionMutex);
        m_pTask->stop();
    }

    shutdownThread(requestorName, pHookFunction, pHookFunctionParameter);

#ifdef USE_LOGGER
    {
        std::string ss;
        ss += "Task ";
        ss += getName();
        ss += " has been stopped";
        Logger::log(LOG_LEVEL_DEBUG3, ss.c_str());
    }
#endif
}

void ActiveTask::initThread()
{
    //do nothing
}

void ActiveTask::run()
{
    while (!isDying())
    {
        {
            boost::lock_guard<boost::mutex> lock(m_actionMutex);
            m_pTask->perform();
        }

        if (m_doSleep)
        {
            OS_Utilities::sleep(static_cast<unsigned long>(m_sleepTime));
        }
        //else do nothing
    }
}

void ActiveTask::flushThread()
{
    boost::lock_guard<boost::mutex> lock(m_actionMutex);
    m_pTask->shutdown(getName().c_str());
}

}
