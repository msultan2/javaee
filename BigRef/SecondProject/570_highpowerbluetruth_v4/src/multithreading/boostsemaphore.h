/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: This code has been copied from:
        http://stackoverflow.com/questions/3928853/how-can-i-achieve-something-similar-to-a-semaphore-using-boost-in-c
*/

#ifndef _BOOST_SEMAPHORE_H
#define _BOOST_SEMAPHORE_H


#include <boost/thread.hpp>

/**
 * @brief This class is implementing a semaphore pattern based on condition variable
 */
class BoostSemaphore
{
public:
    /**
     * @brief Main constructor
     * @param name the name to be given (for debugging)
     * @param maximumCount expected maximum semaphore count
     * @param initialCount initial value of the semaphore
     */
    BoostSemaphore(const char* name, const unsigned int maximumCount, const unsigned int initialCount = 0);

    virtual ~BoostSemaphore();

    /**
     * @brief Get current number of received releases (used for debugging)
     * @return the current count value
     */
    unsigned int get_count(); //for debugging/testing only

    /**
     * @brief Release the semaphore
     */
    void release();

    /**
     * @brief Wait for a release on the semaphore (called acquire in Java)
     */
    void wait();

    /**
     * @brief Wait for a release on the semaphore for a specified time (called acquire in Java)
     * @param timeoutInMilliseconds time to wait for semaphore release
     * @return true if release encountered, false otherwise
     */
    bool wait(const unsigned int timeoutInMilliseconds);

    /**
     * @brief Move the semaphore to initial conditions
     */
    void reset();

private:
    const std::string M_NAME;

    //The current semaphore count.
    const unsigned int M_MAXIMUM_COUNT;
    unsigned int m_count;
    mutable boost::mutex m_mutex;

    //Code that increments count_ must notify the condition variable.
    boost::condition_variable m_conditionVariable;
};

#endif //_BOOST_SEMAPHORE_H
