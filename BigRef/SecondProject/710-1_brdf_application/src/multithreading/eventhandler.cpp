#include "stdafx.h"
#include "eventhandler.h"

#include <cassert>
#include <stdlib.h>
#include <sstream>

//#define DEBUG_INFO


namespace
{
    const char MODULE_NAME[] = "EventHandler";
}

EventHandler::EventHandler(const char* name)
:
m_eventCollection(),
m_eventMutex(),
M_NAME(std::string(std::string(name) + std::string("_") + std::string("EventHandler"))),
m_semaphore(M_NAME.c_str(), 0xFFFF)
{
    //do nothing
}

EventHandler::~EventHandler()
{
    //do nothing
}

bool EventHandler::waitOnAnyEvent(const unsigned long timeoutInMilliseconds, size_t &eventID)
{
    bool collectionIsEmpty = false;
    {
        boost::lock_guard<boost::mutex> lock(m_eventMutex);
        collectionIsEmpty = m_eventCollection.empty();
    }

    if (collectionIsEmpty)
    {
        //Wait, may be an event will be reported

#ifdef DEBUG_INFO
        const bool RESULT = m_semaphore.wait(timeoutInMilliseconds);
        if (RESULT)
        {
            std::ostringstream ss;
            ss << M_NAME << ", " << "waitOnAnyEvent()[1], noOfElements=" << m_eventCollection.size();
            Logger::logAction(ss.str().c_str());
        }
        //else do nothing
#else
        m_semaphore.wait(timeoutInMilliseconds);
#endif

    }
    else
    {
#ifdef DEBUG_INFO
        std::ostringstream ss;
        ss << M_NAME << ", " << "waitOnAnyEvent()[2], noOfElements=" << m_eventCollection.size();
        Logger::logAction(ss.str().c_str());
#endif //DEBUG_INFO

        //We must release the semaphore
        const bool RESULT = m_semaphore.wait(0);
        assert(RESULT);
    }

    boost::lock_guard<boost::mutex> lock(m_eventMutex);
    bool collectionIsNotEmpty = !m_eventCollection.empty();

    if (collectionIsNotEmpty)
    {
        eventID = m_eventCollection.front();
        m_eventCollection.pop_front();
    }
    //else do nothing

    return collectionIsNotEmpty;
}

void EventHandler::setEvent(const int eventID)
{
#ifdef DEBUG_INFO
        std::ostringstream ss;
        ss << M_NAME << ", setEvent(" << eventID << ")";
        Logger::logAction(ss.str().c_str());
#endif //DEBUG_INFO

    boost::lock_guard<boost::mutex> lock(m_eventMutex);
    m_eventCollection.push_back(eventID);

    m_semaphore.release();
}
