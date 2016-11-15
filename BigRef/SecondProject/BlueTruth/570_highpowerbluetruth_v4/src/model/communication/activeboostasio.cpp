#include "stdafx.h"
#include "activeboostasio.h"

#include "boostasio.h"
#include "logger.h"


namespace
{
    const char MODULE_NAME[] = "ActiveBoostAsio";
}


namespace Model
{


ActiveBoostAsio::ActiveBoostAsio(const char* name)
:
ActiveObject(name),
m_pIoService(new boost::asio::io_service()),
m_pWork(),
m_doNotRestartIoService(false),
m_pStartupTimer(new boost::asio::deadline_timer( *m_pIoService )),
m_startupTimerExpired(false),
m_startupTimerExpiredMutex(),
m_startupTimerExpiredConditionVariable()
{
    //Setup the timer. Once the io_service is run the timer will run
    m_pStartupTimer->expires_from_now(boost::posix_time::milliseconds(1));
    m_pStartupTimer->async_wait(
        boost::bind(&ActiveBoostAsio::onStartupTimerExpire, this));
}

ActiveBoostAsio::~ActiveBoostAsio()
{
    //We try to stop if not stopped yet. This feature enables to create
    //an object as a shared pointer, start it and pass to another class.
    //When this class gets destroyed the associated (passed as shared pointer)
    //ActiveBoostAsion will get stopped and destroyed too
    stop();
}

void ActiveBoostAsio::start()
{
    //run the thread
    resumeThread();
}

void ActiveBoostAsio::run()
{
    while (!isDying())
    {
        if (!m_doNotRestartIoService.get())
        {
            try
            {
                m_pWork = boost::shared_ptr<boost::asio::io_service::work>(
                    new boost::asio::io_service::work(*m_pIoService));

                m_pIoService->reset();
                Logger::log(LOG_LEVEL_DEBUG3,
                    getName().c_str(), "ActiveBoostAsio::run() m_pIoService->run() Start");
                m_pIoService->run();
                Logger::log(LOG_LEVEL_DEBUG3,
                    getName().c_str(), "ActiveBoostAsio::run() m_pIoService->run() End");
            }
            catch (std::exception& e)
            {
                std::ostringstream ss;
                ss << "Exception: " << e.what();
                Logger::log(
                    LOG_LEVEL_EXCEPTION,
                    getName().c_str(), "ActiveBoostAsio::run()",
                    ss.str().c_str());
            }
        }
        else
        {
            boost::this_thread::sleep(boost::posix_time::milliseconds(0));
        }
    }
}

void ActiveBoostAsio::stop()
{
    m_doNotRestartIoService.set(true);
    m_pWork.reset();

    shutdownThread(getName().c_str());
}

void ActiveBoostAsio::flushThread()
{
    m_pIoService->stop();
    m_pIoService->run();
}

void ActiveBoostAsio::waitUntilIoServiceRunning()
{
    boost::mutex::scoped_lock lock(m_startupTimerExpiredMutex);
    while (!m_startupTimerExpired)
    {
        m_startupTimerExpiredConditionVariable.wait(lock);
    }
}

void ActiveBoostAsio::onStartupTimerExpire()
{
    {
        boost::lock_guard<boost::mutex> lock(m_startupTimerExpiredMutex);
        m_startupTimerExpired = true;
    }
    m_startupTimerExpiredConditionVariable.notify_one();
}

} // namespace
