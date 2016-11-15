/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/

#ifndef _EVENT
#define _EVENT


class Event
{
public:

    //! default constructor
    Event();

    //! destructor
    virtual ~Event();

    //! put into signaled state
    void release();

    //! Wait until event is in signaled (green) state
    void wait();

    void* getHandle() { return m_handle; }

private:

    //! copy constructor, not implemented
    Event(const Event& rhs);

    //! copy assignment operator, not implemented
    Event& operator=(const Event& rhs);

    void* m_handle;
};

#endif //_EVENT
