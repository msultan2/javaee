/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef _IOBSERVABLE_H_
#define _IOBSERVABLE_H_

#include <boost/thread.hpp>
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
    IObservable(const IObservable&);
    //! Assignment operator, not implemented.
    IObservable& operator=(const IObservable&);

    //! The observers.
    std::list<IObserver* > m_observers;
    //! A mutex for the observers list.
    boost::mutex m_observersMutex;
};

#endif /*_IOBSERVABLE_H_*/
