/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/

#ifndef _EVENT_HANDLER_H_
#define _EVENT_HANDLER_H_


#include "semaphore_.h"
#include "mutex.h"

#include <list>
#include <string>


class EventHandler
{
public:

    /** EventHandler constructor. */
    explicit EventHandler(const char* name);

    /** Virtual destructor. */
    virtual ~EventHandler();

    /** Waits until any event is signalled or timeout expires. */
    bool waitOnAnyEvent(const unsigned long timeoutInMilliseconds, size_t &eventID);

    /** Sets the specified event. */
    void setEvent(const int eventID);

private:
    //! default constructor, not implemented
    EventHandler();
    //! copy constructor, not implemented
    EventHandler(const EventHandler& rhs);
    //! copy assignment operator, not implemented
    EventHandler& operator=(const EventHandler& rhs);

    std::list<int> m_eventCollection;
    Mutex m_eventMutex;

    const std::string M_NAME;
    Semaphore m_semaphore;
};

#endif //_EVENT_HANDLER_H_

