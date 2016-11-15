/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/

#ifndef _SEMAPHORE_H__
#define _SEMAPHORE_H__

#if defined(_WIN32)
#   include <windows.h>
#   include <string>
#   include "mutex.h"
#elif defined (__linux__) || defined (__FreeBSD__)
#   include <semaphore.h>
#elif defined (__TI_COMPILER_VERSION__)
#   include <std.h>
#   include <sem.h>
#else
#error Platform is not supported
#endif

class Semaphore
{
public:

    //Constructors
    Semaphore(const char* name, const unsigned int maximumCount, const unsigned int initialCount = 0); 

    //Destructor
    virtual ~Semaphore();

    void release(); //post
    bool wait(const unsigned int timeoutInMilliseconds); //pend

private:
    
    //! default constructor, not implemented
    Semaphore();
    //! copy constructor, not implemented
    Semaphore(const Semaphore& rhs);
    //! copy assignment operator, not implemented
    Semaphore& operator=(const Semaphore& rhs);

#ifdef _WIN32
    HANDLE m_handle;
    const std::string M_NAME;

    const unsigned int M_MAXIMUM_COUNT; 
    unsigned int m_count;
    Mutex m_countMutex;
    DWORD m_lastResult;
#elif defined (__linux__) || defined (__FreeBSD__)
    ::sem_t m_handle;
#elif defined (__TI_COMPILER_VERSION__)
    SEM_Handle m_handle;
#else
#endif

};

#endif //_SEMAPHORE_H__
