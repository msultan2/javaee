/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef ACTIVE_TASK_H
#define ACTIVE_TASK_H

#include "activeobject.h"
#include "itask.h"

#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>


namespace Model
{

class ActiveTask : public ::ActiveObject
{
public:

    //! Default constructor
    explicit ActiveTask(boost::shared_ptr<ITask> pActivity);
    //! Destructor
    virtual ~ActiveTask();

    void setSleepTime(const unsigned int sleepTime);
    void doNotSleep();

    void start();
    void stop();

    virtual void initThread();
    virtual void run();
    virtual void flushThread();

private:
    //! Default constructor, not implemented
    ActiveTask();
    //! Copy constructor, not implemented
    ActiveTask(const ActiveTask& );
    //! Assignment operator, not implemented
    const ActiveTask& operator=(const ActiveTask& );

    boost::shared_ptr<ITask> m_pTask;
    unsigned int m_sleepTime;
    bool m_doSleep;

    mutable boost::mutex m_actionMutex;
};

}

#endif /* ACTIVE_TASK_H */
