/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef _IOBSERVER_H_
#define _IOBSERVER_H_


class IObservable;

class IObserver
{
public:
    //! Destructor.
    virtual ~IObserver();

    //! Called to notify the observer that the file system has become
    //! (or is no longer) operational.
    enum
    {
        eNOT_USED = 0,
    };
    virtual void notifyOfStateChange(IObservable* pObservable, const int index = eNOT_USED) = 0;


protected:
    //! Default constructor.
    IObserver();

private:
    //! Copy constructor, not implemented.
    IObserver(const IObserver&);
    //! Assignment operator, not implemented.
    IObserver& operator=(const IObserver&);
};

#endif /*_IOBSERVER_H_*/