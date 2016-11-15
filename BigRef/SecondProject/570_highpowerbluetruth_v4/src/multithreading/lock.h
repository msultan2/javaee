/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/

#ifndef _LOCK
#define _LOCK

#include "mutex.h"

class Lock
{
public:

    //! Acquire the state of the semaphore
    explicit Lock( Mutex& mutex) : m_mutex(mutex) 
    {
        m_mutex.acquire();
    }

    //! Release the state of the semaphore
    virtual ~Lock();

private:
    
    //! default constructor, not implemented
    Lock();

    //! copy constructor, not implemented
    Lock(const Lock& rhs);

    //! copy assignment operator, not implemented
    Lock& operator=(const Lock& rhs);

    Mutex & m_mutex;
};

#endif //_LOCK
