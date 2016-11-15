/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    02/10/2013  RG      001         V1.00 First Issue

*/

#ifndef PERIODICALLY_REMOVE_NON_PRESENT_DEVICES_TASK_H_
#define PERIODICALLY_REMOVE_NON_PRESENT_DEVICES_TASK_H_

#include "itask.h"
#include "iobservable.h"
#include "clock.h"
#include "types.h"

#include <boost/shared_ptr.hpp>


class Clock;


namespace Model
{
    class DataContainer;
}


namespace BlueTooth
{

class PeriodicallyRemoveNonPresentDevicesTask :
    public ::ITask,
    public ::IObservable
{

public:
    PeriodicallyRemoveNonPresentDevicesTask(
        boost::shared_ptr<Model::DataContainer> pDataContainer,
        ::Clock* pClock);

    virtual ~PeriodicallyRemoveNonPresentDevicesTask();

    virtual void initialise();
    virtual void perform();
    virtual void shutdown();
    virtual void stop();

    /**
     * @brief Start the task by setting internal parameters
     * @param actionPeriodInSeconds action period in seconds
     */
    void start(
        const unsigned int actionPeriodInSeconds,
        const unsigned int absenceThresholdInSeconds,
        const bool moveToNonPresentCollectionForReporting);


    bool isRunning() const;

    enum
    {
        eREMOVING_NON_PRESENT_REMOTE_DEVICES = 1,
        eSTARTING,
        eSTOPPING
    };

private:

    //! default constructor. Not implemented
    PeriodicallyRemoveNonPresentDevicesTask();
    //! copy constructor. Not implemented
    PeriodicallyRemoveNonPresentDevicesTask(const PeriodicallyRemoveNonPresentDevicesTask& );
    //! assignment operator. Not implemented
    PeriodicallyRemoveNonPresentDevicesTask& operator=(const PeriodicallyRemoveNonPresentDevicesTask& );


    //Private members
    boost::shared_ptr<Model::DataContainer> m_pDataContainer;
    ::Clock* m_pClock;

    TSteadyTimePoint m_lastActionTime;
    TSteadyTimeDuration m_actionPeriod;

    unsigned int m_absenceThresholdInSeconds;
    bool m_moveToNonPresentCollectionForReporting;
};

}

#endif //PERIODICALLY_REMOVE_NON_PRESENT_DEVICES_TASK_H_
