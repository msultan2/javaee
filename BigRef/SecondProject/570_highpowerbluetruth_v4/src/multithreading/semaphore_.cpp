
#include "stdafx.h"
#include "semaphore_.h"
//#include "logger.h"
#include "types.h"
//#include "utils.h"

#include <cassert>

#if defined (_WIN32)
#   include "lock.h"
#   include <sstream>
#elif defined (__linux__) || defined (__FreeBSD__)
#   include <time.h>
#elif defined (__TI_COMPILER_VERSION__)
#   include <clk.h>
#else
#endif

//#define DEBUG_INFO


namespace
{
    const char MODULE_NAME[] = "Semaphore";

#if defined (__TI_COMPILER_VERSION__) || defined (__linux__) || defined (__FreeBSD__)
    const int64_t NANOSECONDS_IN_SECOND = 1000000000LL;
    const int64_t NANOSECONDS_IN_MILLISECOND = 1000000LL;
#endif
}

#if defined (_WIN32) || defined (__TI_COMPILER_VERSION__)
Semaphore::Semaphore(const char* name, const unsigned int maximumCount, const unsigned int initialCount)
#elif defined (__linux__) || defined (__FreeBSD__)
Semaphore::Semaphore(const char* , const unsigned int , const unsigned int initialCount)
#else
#error Platform not supported
#endif

#if defined (_WIN32)
:
m_handle(0),
M_NAME(name),
M_MAXIMUM_COUNT(maximumCount),
m_count(initialCount),
m_countMutex(),
m_lastResult(0)
#endif
{
#ifdef _WIN32
    UNREFERENCED_PARAMETER(name);

    HANDLE aSemaphoreWithThisHandle = ::OpenSemaphoreA(
        NULL,
        TRUE,
        name);
    assert(aSemaphoreWithThisHandle==0);

    m_handle = ::CreateSemaphoreA(
        NULL,             // default security attributes
        static_cast<LONG>(initialCount),     // initial count
        static_cast<LONG>(M_MAXIMUM_COUNT),  // maximum count
        NULL);            // unnamed semaphore

    if (m_handle == 0)
    {
        //Logger::logSoftwareException(
        //    MODULE_NAME,
        //    "constructor",
        //    "CreateSemaphoreA() has failed");
    }
    //else do nothing

#elif defined (__linux__) || defined (__FreeBSD__)
    ::sem_init(
        &m_handle,
        0, //pshared between the threads of a process
        initialCount);

#elif defined (__TI_COMPILER_VERSION__)

    //Verify that the system ticks happen every 1ms
    const unsigned int NUMBER_HARDWARE_TIMER_COUNTS_PER_MILLISECOND =
        static_cast<unsigned int>(CLK_countspms());
    const unsigned int NUMBER_HIGH_RESOLUTION_TIMER_COUNTS_PER_LOW_RESOLUTION_INTERRUPT =
        static_cast<unsigned int>(CLK_getprd());
    assert(NUMBER_HARDWARE_TIMER_COUNTS_PER_MILLISECOND ==
        NUMBER_HIGH_RESOLUTION_TIMER_COUNTS_PER_LOW_RESOLUTION_INTERRUPT);

    SEM_Attrs attrs = SEM_ATTRS;
    if (name!=0)
    {
        attrs.name = const_cast<char*>(name);
    }
    //else do nothing

    m_handle = SEM_create(
        initialCount, //initial semaphore count
        &attrs);

    assert(m_handle!=0);

#else
#endif
}

Semaphore::~Semaphore()
{
#ifdef _WIN32
    ::CloseHandle(m_handle);
#elif defined (__linux__) || defined (__FreeBSD__)
    ::sem_destroy(&m_handle);
#elif defined (__TI_COMPILER_VERSION__)
    SEM_delete(m_handle);
#else
#endif
}

void Semaphore::release()
{
#ifdef _WIN32
    LONG previousCount = 0;

#ifdef DEBUG_INFO
        std::ostringstream ss;
        ss << M_NAME << ", " << m_count << "++ => " << m_count+1;
        Logger::logAction(ss.str().c_str());
#endif //DEBUG_INFO

    {
        Lock lock(m_countMutex);

        m_count++;
        if (m_count>M_MAXIMUM_COUNT)
        {
            //std::string errSS("Error m_count > M_MAXIMUM_COUNT (");
            //errSS += Utils::int64ToString(m_count);
            //errSS += " > ";
            //errSS += Utils::int64ToString(M_MAXIMUM_COUNT);
            //errSS += ")";

            //Logger::logSoftwareException(
            //    MODULE_NAME,
            //    "release",
            //    errSS.c_str());
        }
        //else do nothing
    }

    BOOL result = ::ReleaseSemaphore(
        m_handle,
        static_cast<LONG>(1), //lReleaseCount
        &previousCount);

    if (!result)
    {
        //DWORD errorNumber = ::GetLastError();
        //std::ostringstream errSS;
        //errSS << "Handle 0x" << m_handle
        //    << " release(), ERROR_NUMBER=" << errorNumber
        //    << ", previousCount = " << previousCount << std::endl;

        //Logger::logSoftwareException(
        //    MODULE_NAME,
        //    "release",
        //    errSS.str().c_str());
    }
    //else do nothing

#elif defined (__linux__) || defined (__FreeBSD__)
    ::sem_post(&m_handle);
#elif defined (__TI_COMPILER_VERSION__)
    SEM_post(m_handle);
#else
#endif

}

bool Semaphore::wait(const unsigned int timeoutInMilliseconds)
{
#ifdef _WIN32

    DWORD result = ::WaitForSingleObject(
        m_handle,
        static_cast<DWORD>(timeoutInMilliseconds));

    m_lastResult = result;

    if (result == WAIT_OBJECT_0)
    {
#ifdef DEBUG_INFO
        std::ostringstream ss;
        ss << M_NAME << ", " << m_count << "-- => " << m_count-1;
        Logger::logAction(ss.str().c_str());
#endif //DEBUG_INFO

        Lock lock(m_countMutex);

        m_count--;
    }
    //else do nothing

    return (result == WAIT_OBJECT_0);
#elif defined (__linux__) || defined (__FreeBSD__)
    struct timespec currentTime;
    ::clock_gettime(CLOCK_REALTIME, &currentTime);

    const int64_t numberOfNanosecondsSinceEpochWhenFinishToWait =
            currentTime.tv_sec* NANOSECONDS_IN_SECOND + currentTime.tv_nsec +
            timeoutInMilliseconds * NANOSECONDS_IN_MILLISECOND;

    struct timespec whenToFinishTime;
    whenToFinishTime.tv_sec = numberOfNanosecondsSinceEpochWhenFinishToWait / NANOSECONDS_IN_SECOND;
    whenToFinishTime.tv_nsec = numberOfNanosecondsSinceEpochWhenFinishToWait % NANOSECONDS_IN_SECOND;

    int result = ::sem_timedwait(&m_handle, &whenToFinishTime);

    return ( result==0); //0 = no error
#elif defined (__TI_COMPILER_VERSION__)

    bool result = SEM_pend(
        m_handle,
        timeoutInMilliseconds);

    return result;
#else
#endif
}
