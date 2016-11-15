/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/

#ifndef _MUTEX
#define _MUTEX


class CriticalSection;

class Mutex
{
public:

    //! constructor
    Mutex();

    //! destructor
    virtual ~Mutex();

    void acquire();

    void release();

    bool tryToAcquire(const unsigned int timeout);

private:

    //! copy constructor, not implemented
    Mutex(const Mutex& rhs);

    //! copy assignment operator, not implemented
    Mutex& operator=(const Mutex& rhs);

    CriticalSection* m_criticalSectionPtr;
};

#endif //_MUTEX
