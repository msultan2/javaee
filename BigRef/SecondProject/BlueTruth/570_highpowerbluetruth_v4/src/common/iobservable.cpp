#include "stdafx.h"
#include "iobservable.h"

#include "iobserver.h"
#include "lock.h"
#include "logger.h"


IObservable::~IObservable()
{
}

void IObservable::addObserver(IObserver* observerPtr)
{
    Lock lock(m_observersMutex);
    bool found = false;

    //Check if this observer does not exist in the list
    for (std::list<IObserver* >::iterator iterator(m_observers.begin());
        iterator != m_observers.end();
        )
    {
        if (*iterator++ == observerPtr)
        {
            found = true;
            break;
        }
        //else continue
    }

    if (!found)
    {
        m_observers.push_back(observerPtr);
    }
    else
    {
        Logger::log(LOG_LEVEL_DEBUG3, "IObservable::addObserver", "double observer!");
    }
}

void IObservable::removeObserver(IObserver* observerPtr)
{
    Lock lock(m_observersMutex);
    m_observers.remove(observerPtr);
}

void IObservable::removeAllObservers()
{
    Lock lock(m_observersMutex);
    m_observers.clear();
}

IObservable::IObservable()
:
m_observers(),
m_observersMutex()
{
}

void IObservable::notifyObservers(const int index)
{
    Lock lock(m_observersMutex);
    for (std::list<IObserver* >::iterator iterator(m_observers.begin());
        iterator != m_observers.end();
        )
    {
        IObserver* OBSERVER_PTR = *iterator;
        ++iterator; //an observer may remove itself from the list
        OBSERVER_PTR->notifyOfStateChange(this, index);
    }
}
