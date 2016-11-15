/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/

#ifndef _CRITICAL_SECTION
#define _CRITICAL_SECTION

#if defined(_WIN32)

#if defined(USE_WX)
#   include <wx/msw/wrapwin.h>
#else
#   include <windows.h>
#endif

#elif defined (__linux__) || defined (__FreeBSD__)
#   include <pthread.h>
#   include <time.h>

#else
#error Platform not supported
#endif

class CriticalSection
{
public:
    CriticalSection(void);
    virtual ~CriticalSection(void);

#ifdef _WIN32
    void acquire() throw()
    { 
        ::EnterCriticalSection(&m_criticalSection);
    }

    void release() throw()
    {
        ::LeaveCriticalSection(&m_criticalSection);
    }

    bool tryToAcquire(unsigned int timeout)
    {
        BOOL result = FALSE;

        for (unsigned int numberOfMilliseconds=0; numberOfMilliseconds<=timeout; ++numberOfMilliseconds)
        {
            result = ::TryEnterCriticalSection(&m_criticalSection);

            if (result == FALSE)
            {
                ::Sleep(1);
            }
            else
            {
                break;
            }
        }
        return (result == TRUE);
    }

private:
    CriticalSection(const CriticalSection &rhs);
    CriticalSection &operator=(const CriticalSection &rhs);

    ::CRITICAL_SECTION m_criticalSection;


#elif defined (__linux__) || defined (__FreeBSD__)
    void acquire() throw()
    { 
        pthread_mutex_lock(&m_mutex);
    }

    void release() throw()
    {
        pthread_mutex_unlock(&m_mutex);
    }

    bool tryToAcquire(unsigned int timeout)
    {
        int result = 0;

        for(unsigned int numberOfMilliseconds=0; numberOfMilliseconds<=timeout; ++numberOfMilliseconds)
        {
            result = pthread_mutex_trylock(&m_mutex);

            if (result != 0)
            {
                const __time_t NUMBER_OF_SECONDS = 0;
                const unsigned int NUMBER_OF_MILLISECONDS = 1;
                struct timespec req = {NUMBER_OF_SECONDS, NUMBER_OF_MILLISECONDS*1000000u};
                struct timespec rem;

                nanosleep(&req, &rem);
            }
            else
            {
                break;
            }
        }
        return (result == 0);
    }

private:
    CriticalSection(const CriticalSection &rhs);
    CriticalSection &operator=(const CriticalSection &rhs);

    pthread_mutex_t m_mutex;


#else
#endif

};

#endif //_CRITICAL_SECTION
