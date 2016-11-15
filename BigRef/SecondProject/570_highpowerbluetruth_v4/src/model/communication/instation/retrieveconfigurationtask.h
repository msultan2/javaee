/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef RETRIEVE_CONFIGURATION_TASK_H_
#define RETRIEVE_CONFIGURATION_TASK_H_

#include "itask.h"
#include "iobserver.h"
#include "iobservable.h"

#include "clock.h"
#include "types.h"

#include <boost/shared_ptr.hpp>
#include <boost/thread/thread.hpp>


class Clock;

namespace Model
{
    class IniConfiguration;
}

namespace InStation
{

    class InStationHTTPClient;


class RetrieveConfigurationTask :
    public ::ITask,
    public ::IObserver,
    public ::IObservable
{

public:
    RetrieveConfigurationTask(
        boost::shared_ptr<InStationHTTPClient> pInStationHTTPClient,
        ::Clock* pClock);

    virtual ~RetrieveConfigurationTask();

    virtual void initialise();
    virtual void perform();
    virtual void shutdown();

    virtual void notifyOfStateChange(::IObservable* pObservable, const int index);

    /**
     * @brief Start the task by setting internal parameters
     * @param configurationRequestPeriodInSeconds interval at which configuration will be retrieved
     * @param startupDelayInSeconds startup delay in seconds
     */
    void start(
        const unsigned int configurationRequestPeriodInSeconds,
        const unsigned int startupDelayInSeconds);
    void stop();


    enum
    {
        eREQUESTING_CONFIGURATION = 1,
        eSTARTING,
        eSTOPPING
    };

private:

    //! default constructor. Not implemented
    RetrieveConfigurationTask();
    //! copy constructor. Not implemented
    RetrieveConfigurationTask(const RetrieveConfigurationTask& );
    //! assignment operator. Not implemented
    RetrieveConfigurationTask& operator=(const RetrieveConfigurationTask& );


    //Private members
    boost::shared_ptr<InStationHTTPClient> m_pInStationHTTPClient;
    ::Clock* m_pClock;

    TSteadyTimePoint m_startupTime;
    TSteadyTimePoint m_startupTimeWithDelay;
    TSteadyTimePoint m_lastConfigurationRequestTime;
    TSteadyTimePoint m_nextConfigurationRequestTime;
    TSteadyTimeDuration m_configurationRequestPeriod;

    mutable boost::mutex m_mutex;
};

}

#endif //RETRIEVE_CONFIGURATION_TASK_H_
