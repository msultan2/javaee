#include "stdafx.h"
#include "criticalsection.h"

#ifdef _WIN32

CriticalSection::CriticalSection(void)
:
m_criticalSection()
{
    ::InitializeCriticalSection(& m_criticalSection);
}

CriticalSection::~CriticalSection(void)
{
    try
    {
        ::DeleteCriticalSection(& m_criticalSection);
    }
    catch (...)
    {
        //do nothing
    }
}

#elif defined (__linux__) || defined (__FreeBSD__)

CriticalSection::CriticalSection(void)
:
m_mutex()
{
    pthread_mutexattr_t attr;

    //Initialise mutex attributes object with values
    pthread_mutexattr_init(&attr);
    pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE);

    //Initialise mutex with specified attributes
    pthread_mutex_init(&m_mutex, &attr);
}

CriticalSection::~CriticalSection(void)
{
    try
    {
        pthread_mutex_destroy(&m_mutex);
    }
    catch (...)
    {
        //do nothing
    }
}

#elif defined (__TI_COMPILER_VERSION__)

CriticalSection::CriticalSection(void)
:
m_lock(),
m_lockAttrs()
{
    m_lock = LCK_create(&m_lockAttrs);
}

CriticalSection::~CriticalSection(void)
{
    LCK_delete(m_lock);
}

#else
#endif
