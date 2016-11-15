/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef PERIODICALLY_SEND_RAW_DATA_TASK_H_
#define PERIODICALLY_SEND_RAW_DATA_TASK_H_

#include "itask.h"
#include "iobserver.h"
#include "iobservable.h"

#include "types.h"

#include <boost/shared_ptr.hpp>
#include <boost/thread/thread.hpp>


class Clock;


namespace BrdfServer
{

class IBrdfServerReporter;

class PeriodicallySendRawDataTask :
    public ::ITask,
    public ::IObserver,
    public ::IObservable
{

public:
    PeriodicallySendRawDataTask(
        boost::shared_ptr<IBrdfServerReporter> pBrdfServerReporter,
        ::Clock* pClock);

    virtual ~PeriodicallySendRawDataTask();

    void setupParameters(const unsigned int reportingPeriodInSeconds);

    virtual void initialise();
    virtual void perform();
    virtual void shutdown(const char* requestorName = 0);
    virtual void stop();

    /**
     * @brief Start the task by setting internal parameters
     * @param reportingPeriodInSeconds reporting period in seconds
     */
    void start(const unsigned int reportingPeriodInSeconds);


    bool isRunning() const;

    enum
    {
        eSENDING_RAW_DATA = 1,
        eSTARTING,
        eSTOPPING
    };

    virtual void notifyOfStateChange(IObservable* pObservable, const int index);

    void push();


private:

    //! default constructor. Not implemented
    PeriodicallySendRawDataTask();
    //! copy constructor. Not implemented
    PeriodicallySendRawDataTask(const PeriodicallySendRawDataTask& );
    //! assignment operator. Not implemented
    PeriodicallySendRawDataTask& operator=(const PeriodicallySendRawDataTask& );


    //Private members
    boost::shared_ptr<IBrdfServerReporter> m_pBrdfServerReporter;
    ::Clock* m_pClock;

    ::TTime_t m_lastReportTime;
    ::TTime_t m_nextReportTime;
    ::TTimeDiff_t m_reportingPeriod;

    bool m_requestReceived;

    mutable boost::mutex m_mutex;
};

}

#endif //PERIODICALLY_SEND_RAW_DATA_TASK_H_
