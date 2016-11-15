/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: This is an implementation of TCP client based on boost:asio library

    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/


#ifndef _IOBSERVABLE_H_
#define _IOBSERVABLE_H_

#include "mutex.h"

#include <list>


class IObserver;

class IObservable
{
public:
    //! Destructor.
    virtual ~IObservable();

    //! Add an observer.
    virtual void addObserver(IObserver* observerPtr);
    //! Remove an observer.
    virtual void removeObserver(IObserver* observerPtr);
    //! Remove an observer.
    virtual void removeAllObservers();

    //! Called to notify observers of state change.
    enum
    {
        eNOT_USED = 0,
    };
    void notifyObservers(const int index = eNOT_USED);

protected:
    //! Default constructor.
    IObservable();

private:
    //! Copy constructor, not implemented.
    IObservable(const IObservable&) = delete;
    //! Assignment operator, not implemented.
    IObservable& operator=(const IObservable&) = delete;

    //! The observers.
    std::list<IObserver* > m_observers;
    //! A mutex for the observers list.
    Mutex m_observersMutex;
};

#endif /*_IOBSERVABLE_H_*/
