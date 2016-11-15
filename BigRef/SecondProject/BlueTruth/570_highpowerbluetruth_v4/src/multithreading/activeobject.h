/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef ACTIVE_OBJECT
#define ACTIVE_OBJECT


#include <boost/thread.hpp>

#include <string>


/**
 * @brief The base-class implementing Active Object pattern.
 *
 * To run it:
 * -# Instantiate an instance,
 * -# Start the thread (resumeThread)
 * -# If required wait for the thread to start (waitUntilRunning)
 * -# Shut it down after the work has been completed (shutdownThread)
 *
 * Example:
   \code
    #include "activeobject.h"
     *
    ActiveObject object("NAME");
    object.resumeThread();
    object.waitUntilRunning();

    //do something

    object.shutdownThread("me");
   \endcode

 * When implementing a derived class the run() method should follow the pattern:
    \code
    void ActiveObjectDerivedClass::run()
    {
        do
        {
            //do something
        }
        while (!runShouldFinish());
    }
    \endcode
 */
class ActiveObject
{
public:

    /**
     * @brief Main constructor.
     * @param name the name that will be given to this object.
     * This name is useful for debuging in particular when multiple objects of one type are deployed.
     * @param stackSize size of stack in bytes
     * @param priority thread priority (not implemented)
     */
    ActiveObject(const char* name, const size_t stackSize = 0, const int priority = -1);

    //! destructor
    virtual ~ActiveObject();

    /**
     * @brief Resume/start the thread.
     */
    void resumeThread();

    /**
     * @brief Shutdown the thread.
     * @param requestorName the name of the requesting party (used for debugging)
     */
    void shutdownThread(const char* requestorName = 0);

    /**
     * @brief Check if the object is in idle state, i.e. resumeThread() has not already been called
     * @return true is so, false otherwise
     */
    bool isIdle()
    {
        boost::mutex::scoped_lock lock(m_globalMutex);
        return (m_state == eIDLE);
    }

    /**
     * @brief Check if the object is in running state, i.e. run() method is currently being executed
     * @return true is so, false otherwise
     */
    bool isRunning()
    {
        boost::mutex::scoped_lock lock(m_globalMutex);
        return (m_state == eRUNNING);
    }

    /**
     * @brief Wait until the thread has been created and run. This method blocks.
     */
    void waitUntilRunning()
    {
        boost::mutex::scoped_lock lock(m_globalMutex);
        while (m_state != eRUNNING)
        {
            m_stateConditionVariable.wait(lock);
        }
    }

    /**
     * @brief Check if the object is in dying state (should complete its run method)
     */
    bool isDying()
    {
        boost::mutex::scoped_lock lock(m_globalMutex);
        return (m_state == eFINISHING);
    }

    /**
     * @brief Check if the object should complete its run method
     */
    bool runShouldFinish() { return isDying(); }

    /**
     * Get the object name
     * @return Object name
     */
    const std::string& getName() const { return M_NAME; }

protected:
    /**
     * @brief A function to be called before the run method is invoked
     */
    virtual void initThread();

    /**
     * @brief The main thread function
     */
    virtual void run();

    /**
     * @brief A function to be called after the run method has been completed
     */
    virtual void flushThread();

private:
    //! default constructor. Not implemented
    ActiveObject() = delete;
    //! copy constructor. Not implemented
    ActiveObject(const ActiveObject& rhs) = delete;
    //! copy assignment operator
    ActiveObject& operator=(const ActiveObject& rhs) = delete;


    void main();

    typedef enum
    {
        eIDLE = 1,
        eDEPLOYING,
        eRUNNING,
        eFINISHING,
        eCOMPLETED
    } EState;
    EState m_state;

#if BOOST_VERSION >= 105000
    boost::thread::attributes m_attrs;
#endif
    boost::mutex m_globalMutex;
    boost::condition_variable m_stateConditionVariable;
    boost::shared_ptr<boost::thread> m_pThread;

    const std::string M_NAME;
};

#endif

