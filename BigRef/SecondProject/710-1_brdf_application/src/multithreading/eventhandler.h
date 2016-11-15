/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef _EVENT_HANDLER_H_
#define _EVENT_HANDLER_H_

#include "boostsemaphore.h"

#include <boost/thread.hpp>
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
    boost::mutex m_eventMutex;

    const std::string M_NAME;
    BoostSemaphore m_semaphore;
};

#endif //_EVENT_HANDLER_H_

