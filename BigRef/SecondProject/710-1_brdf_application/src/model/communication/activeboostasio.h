/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef _ACTIVE_BOOST_ASIO_H_
#define _ACTIVE_BOOST_ASIO_H_

#include "activeobject.h"
#include "atomicvariable.h"

#include <boost/asio.hpp>
#include <boost/thread.hpp>

/**
 * The Model namespace contains all classes related to Model
 * in the Model-View-Controller (MVC) pattern.
 */
namespace Model
{

/**
 * @brief This is a worker class used by implementation of boost ASIO TCP server, client
 * and others.
 *
 * When run the class deployes another thread which runs io_service::run() method.
 * A method waitUntilIoServiceRunning() can be used to verify if io_service is
 * actually running.

 * To run it:
 * -# Instantiate an instance,
 * -# Start it (start)
 * -# If required wait for the io_service to start (waitUntilIoServiceRunning)
 * -# Shut it down after all the work has been completed (shutdownThread)
 *
 * Example:
   \code
    #include "activeboostasio.h"

    ActiveBoostAsio hive("NAME");
    hive.start();

    hive.waitUntilIoServiceRunning();

    //do something, e.g. post functionality to be executed

    hive.stop();
   \endcode
 */
class ActiveBoostAsio : public ::ActiveObject
{
public:
    /**
     * @brief Main constructor
     * @param name the name that will be given to this object.
     */
    explicit ActiveBoostAsio(const char* name);

    virtual ~ActiveBoostAsio();

    /**
     * @brief Start the thread
     */
    virtual void start();

    /**
     * @brief Shutdown the thread
     */
    virtual void stop();

    /**
     * @brief Wait until is_service is running. This method blocks.
     */
    void waitUntilIoServiceRunning();

    /**
     * Get io_service so that other classes can connect to it (e.g. TCP client)
     * @return a shared pointer to io_service
     */
    boost::shared_ptr<boost::asio::io_service> getIoService()
    {
        return m_pIoService;
    }

protected:
    virtual void run();
    virtual void flushThread();


    //Protected members:
    boost::shared_ptr<boost::asio::io_service> m_pIoService;
    boost::shared_ptr<boost::asio::io_service::work> m_pWork;

    ::AtomicVariable<bool> m_doNotRestartIoService;

private:

    //! default constructor
    ActiveBoostAsio();
    //! copy constructor. Not implemented
    ActiveBoostAsio(const ActiveBoostAsio& rhs);
    //! copy assignment operator. Not implemented
    ActiveBoostAsio& operator=(const ActiveBoostAsio& rhs);

    void onStartupTimerExpire();

    //Private members:

    /**
     * I use the deadline timer to indicate that the service is running.
     * If you find a better way to indicate that the io_service has
     * already started, please replace the code whith the new one.
     */
    boost::shared_ptr<boost::asio::deadline_timer> m_pStartupTimer;

    bool m_startupTimerExpired;
    boost::mutex m_startupTimerExpiredMutex;
    boost::condition_variable m_startupTimerExpiredConditionVariable;
};

}

#endif //_ACTIVE_BOOST_ASIO_H_
