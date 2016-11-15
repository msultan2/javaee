/*
    System: BRDF client
    Language/Build: MS VC 2008 / Linux GCC 4.2+
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
    explicit ActiveTask(
        boost::shared_ptr<ITask> pActivity,
        const char* name = "ActiveTask");

    //! Destructor
    virtual ~ActiveTask();

    void setSleepTime(const unsigned int sleepTime);
    void doNotSleep();

    virtual void start();
    virtual void stop();

    virtual void shutdown(const char* requestorName = 0);
    virtual void shutdown(
        const char* requestorName,
        THookFunction pHookFunction,
        void* pHookFunctionParameter);

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
