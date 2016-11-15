/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    11/06/2010  RG      001         V1.00 First Issue

*/

#ifndef ACQUIRE_DEVICES_TASK_H_
#define ACQUIRE_DEVICES_TASK_H_

#include "itask.h"
#include "iobservable.h"

#include "atomicvariable.h"
#include "clock.h"
#include "types.h"

#include <boost/shared_ptr.hpp>


class Clock;


namespace BlueTooth
{

class DeviceDiscoverer;


class AcquireDevicesTask :
    public ::ITask,
    public ::IObservable
{

public:
    AcquireDevicesTask(
        boost::shared_ptr<DeviceDiscoverer> pDeviceDiscoverer,
        ::Clock* pClock);

    virtual ~AcquireDevicesTask();

    virtual void initialise();
    virtual void perform();
    virtual void shutdown();

    /**
     * @brief Start the task by setting internal parameters
     * @param inquiryDurationInSeconds device inquiry period in seconds
     * @param deviceDiscoveryMaxDevices maximum number of devices to be reported in one scan
     */
    void start(
        const unsigned int inquiryDurationInSeconds,
        const unsigned int deviceDiscoveryMaxDevices);

    unsigned int getInquiryDurationInSeconds() const;

    /**
    * @brief Stop the task
    */
    void stop();

    bool isRunning() const;

    enum
    {
        eSTARTING,
        eSTOPPING
    };

private:

    //! default constructor. Not implemented
    AcquireDevicesTask();
    //! copy constructor. Not implemented
    AcquireDevicesTask(const AcquireDevicesTask& );
    //! assignment operator. Not implemented
    AcquireDevicesTask& operator=(const AcquireDevicesTask& );


    //Private members
    boost::shared_ptr<DeviceDiscoverer> m_pDeviceDiscoverer;
    ::Clock* m_pClock;

    TSteadyTimePoint m_lastSendTime;
    ::AtomicVariable<TSteadyTimeDuration> m_sendInterval;
    ::AtomicVariable<TSteadyTimeDuration> m_inquiryDuration;
    TSteadyTimeDuration m_bluetoothFailureInterval;

    unsigned int m_deviceDiscoveryMaxDevices;
};

}

#endif //ACQUIRE_DEVICES_TASK_H_
