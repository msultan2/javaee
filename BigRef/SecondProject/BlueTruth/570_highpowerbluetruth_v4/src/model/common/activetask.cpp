#include "stdafx.h"
#include "activetask.h"

#include "os_utilities.h"

#include <cassert>


namespace
{
    const unsigned long DEFAULT_SLEEP_PERIOD_MS = 10;

    const char MODULE_NAME[] = "ActiveTask";
}


namespace Model
{

ActiveTask::ActiveTask(boost::shared_ptr<ITask> pTask)
:
::ActiveObject("ActiveTask"),
m_pTask(pTask),
m_sleepTime(DEFAULT_SLEEP_PERIOD_MS),
m_doSleep(true),
m_actionMutex()
{
    assert(m_pTask!=0);
}

ActiveTask::~ActiveTask()
{
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
}

void ActiveTask::stop()
{
    boost::lock_guard<boost::mutex> lock(m_actionMutex);
    m_pTask->stop();
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
    m_pTask->shutdown();
}

}
