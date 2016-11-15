/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    09/10/2013  RG      001         V1.00 First Issue

*/

#ifndef PERIODICALLY_SEND_STATUS_REPORT_TASK_H_
#define PERIODICALLY_SEND_STATUS_REPORT_TASK_H_

#include "itask.h"
#include "iobservable.h"

#include "types.h"

#include <boost/shared_ptr.hpp>
#include <boost/thread/thread.hpp>


class Clock;


namespace InStation
{

class InStationReporter;

class PeriodicallySendStatusReportTask :
    public ::ITask,
    public ::IObservable
{

public:
    PeriodicallySendStatusReportTask(
        boost::shared_ptr<InStationReporter> pInStationReporter,
        ::Clock* pClock);

    virtual ~PeriodicallySendStatusReportTask();

    virtual void initialise();
    virtual void perform();
    virtual void shutdown();
    virtual void stop();

    /**
     * @brief Start the task by setting internal parameters
     * @param reportingPeriodInSeconds reporting period in seconds
     * @param startupDelayInSeconds startup delay in seconds
     */
    void start(const unsigned int reportingPeriodInSeconds, const unsigned int startupDelayInSeconds);


    bool isRunning() const;

    enum
    {
        eSENDING_STATUS_REPORT = 1,
        eSTARTING,
        eSTOPPING
    };

private:

    //! default constructor. Not implemented
    PeriodicallySendStatusReportTask();
    //! copy constructor. Not implemented
    PeriodicallySendStatusReportTask(const PeriodicallySendStatusReportTask& );
    //! assignment operator. Not implemented
    PeriodicallySendStatusReportTask& operator=(const PeriodicallySendStatusReportTask& );


    //Private members
    boost::shared_ptr<InStationReporter> m_pInStationReporter;
    ::Clock* m_pClock;

    TSteadyTimePoint m_startupTime;
    TSteadyTimePoint m_startupTimeWithDelay;
    TSteadyTimePoint m_lastReportTime;
    TSteadyTimePoint m_nextReportTime;
    TSteadyTimeDuration m_reportingPeriod;

    mutable boost::mutex m_mutex;
};

}

#endif //PERIODICALLY_SEND_STATUS_REPORT_TASK_H_
