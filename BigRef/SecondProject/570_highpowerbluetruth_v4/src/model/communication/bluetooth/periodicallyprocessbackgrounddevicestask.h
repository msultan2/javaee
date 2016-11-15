/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    13/11/2013  RG      001         V1.00 First Issue

*/

#ifndef PERIODICALLY_PROCESS_BACKGROUND_DEVICES_TASK_H_
#define PERIODICALLY_PROCESS_BACKGROUND_DEVICES_TASK_H_

#include "itask.h"
#include "iobservable.h"
#include "clock.h"
#include "types.h"

#include "remotedevicerecord.h"

#include <fstream>
#include <boost/shared_ptr.hpp>


class Clock;


namespace Model
{
    class DataContainer;
}


namespace BlueTooth
{

class PeriodicallyProcessBackgroundDevicesTask :
    public ::ITask,
    public ::IObservable
{

public:
    PeriodicallyProcessBackgroundDevicesTask(
        boost::shared_ptr<Model::DataContainer> pDataContainer,
        ::Clock* pClock);

    virtual ~PeriodicallyProcessBackgroundDevicesTask();

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
        const unsigned int backgroundPresenceThresholdInSeconds,
        const unsigned int backgroundAbsenceThresholdInSeconds);

    bool isRunning() const;

    enum
    {
        ePROCESSING_BACKGROUND_CRITERIA_FOR_DEVICES = 1,
        eSTARTING,
        eSTOPPING
    };

private:

    //! default constructor. Not implemented
    PeriodicallyProcessBackgroundDevicesTask();
    //! copy constructor. Not implemented
    PeriodicallyProcessBackgroundDevicesTask(const PeriodicallyProcessBackgroundDevicesTask& );
    //! assignment operator. Not implemented
    PeriodicallyProcessBackgroundDevicesTask& operator=(const PeriodicallyProcessBackgroundDevicesTask& );

    bool loadBackgroundDevicesFromFile(std::string& contents);
    bool saveBackgroundDevicesToFile(const std::vector<Model::TRemoteDeviceRecord>& data);
    void processBackgroundDeviceFileContents(const std::string& contents);

    //Private members
    boost::shared_ptr<Model::DataContainer> m_pDataContainer;
    ::Clock* m_pClock;

    TSteadyTimePoint m_lastActionTime;
    TSteadyTimeDuration m_actionPeriod;

    unsigned int m_backgroundPresenceThresholdInSeconds;
    unsigned int m_backgroundAbsenceThresholdInSeconds;
};

}

#endif //PERIODICALLY_PROCESS_BACKGROUND_DEVICES_TASK_H_
