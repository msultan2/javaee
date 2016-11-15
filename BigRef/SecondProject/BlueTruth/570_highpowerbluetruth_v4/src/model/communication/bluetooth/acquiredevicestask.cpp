#include "stdafx.h"
#include "acquiredevicestask.h"

#include "clock.h"
#include "devicediscoverer.h"
#include "iniconfiguration.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"


namespace
{
    const unsigned int DEFAULT_SEND_INTERVAL_IN_S = 1000;
    const uint64_t MILLISECONDS_IN_SECOND = 1000ULL;
}


namespace BlueTooth
{

AcquireDevicesTask::AcquireDevicesTask(
    boost::shared_ptr<DeviceDiscoverer> pDeviceDiscoverer,
    ::Clock* pClock)
:
::ITask(),
::IObservable(),
m_pDeviceDiscoverer(pDeviceDiscoverer),
m_pClock(pClock),
m_lastSendTime(ZERO_TIME_STEADY),
m_sendInterval(bc::seconds(0)),
m_inquiryDuration(bc::seconds(10)),
m_bluetoothFailureInterval(bc::seconds(1)),
m_deviceDiscoveryMaxDevices(254) //default value specified in the SSL3820 document
{
    //do nothing
}

AcquireDevicesTask::~AcquireDevicesTask()
{
    //do nothing
}

void AcquireDevicesTask::initialise()
{
    //do nothing
}

void AcquireDevicesTask::perform()
{
    if (m_sendInterval.get().count() != 0)
    {
        m_lastSendTime = m_pClock->getSteadyTime();

        assert(m_pDeviceDiscoverer != 0);
        bool ok = m_pDeviceDiscoverer->inquireDevices();

        if (!ok)
        {
            OS_Utilities::sleep(
                bc::duration_cast<bc::milliseconds>(m_bluetoothFailureInterval).count());
        }
        //else do nothing
    }
    else
    {
        OS_Utilities::sleep(
            bc::duration_cast<bc::milliseconds>(m_bluetoothFailureInterval).count());
    }
}

void AcquireDevicesTask::shutdown()
{
    stop();
}

void AcquireDevicesTask::start(
    const unsigned int inquiryDurationInSeconds,
    const unsigned int deviceDiscoveryMaxDevices)
{
    if (
        (m_sendInterval.get().count() != 0) &&
        (static_cast<long>(inquiryDurationInSeconds) == bc::duration_cast<bc::seconds>(m_sendInterval.get()).count()) &&
        (deviceDiscoveryMaxDevices == m_deviceDiscoveryMaxDevices)
        )
    {
        return;
    }
    //else something changed - update parameters and continue

    if (inquiryDurationInSeconds > 0)
    {
        m_sendInterval = bc::seconds(inquiryDurationInSeconds);
    }
    else
    {
        m_sendInterval = bc::seconds(0);
    }

    std::ostringstream ss;
    ss << "Starting periodic device scanning (period=" << inquiryDurationInSeconds << "s)";
    Logger::log(LOG_LEVEL_INFO, ss.str().c_str());

    m_inquiryDuration = bc::seconds(inquiryDurationInSeconds);
    m_deviceDiscoveryMaxDevices = deviceDiscoveryMaxDevices;

    notifyObservers(eSTARTING);
}

void AcquireDevicesTask::stop()
{
    Logger::log(LOG_LEVEL_INFO, "Stopping periodic device scanning");

    m_sendInterval = bc::seconds(0);
    notifyObservers(eSTOPPING);
}

bool AcquireDevicesTask::isRunning() const
{
    return (m_sendInterval.get().count() != 0);
}

unsigned int AcquireDevicesTask::getInquiryDurationInSeconds() const
{
    return static_cast<unsigned int>(
        bc::duration_cast<bc::seconds>(m_inquiryDuration.get()).count());
}

} //namespace
