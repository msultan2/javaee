/*
    System: EAV
    Language/Build: Microsoft Visual Studio 2008 Express Edition
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    17/04/2013  RG      001         V1.00 First Issue

*/

#ifndef _ACTIVE_GSM_MODEM_MONITOR_H_
#define _ACTIVE_GSM_MODEM_MONITOR_H_


#include "activeobject.h"
#include "iobservable.h"
#include "iobserver.h"

#include "clock.h"
#include "mutex.h"
#include "types.h"

#include <boost/thread/recursive_mutex.hpp>


namespace GSMModem
{
    class SignalLevelProcessor;
}

namespace Model
{

class Fault;
class ICoreConfiguration;

class ActiveGSMModemMonitor :
    public ::ActiveObject,
    public ::IObservable,
    public ::IObserver
{
public:

    ActiveGSMModemMonitor(
        const ICoreConfiguration& configurationParameters,
        GSMModem::SignalLevelProcessor& signalLevelProcessor,
        Fault* pGSMModemUnableToConnectFault,
        ::Clock* pClock);
    virtual ~ActiveGSMModemMonitor();

    void setup(
        const std::string& hostName,
        const std::string& userName,
        const std::string& password);

    void setupSignalLevelSamplingPeriod(const unsigned int periodInSeconds);

    void start();
    void stop();

    virtual void notifyOfStateChange(IObservable* observablePtr, const int index);

    bool isGPSSignalOK() const { return m_GPSSignalOK; }

protected:

    virtual void initThread();
    virtual void run();
    virtual void flushThread();


private:

    //! default constructor. Not implemented
    ActiveGSMModemMonitor();
    //! copy constructor. Not implemented
    ActiveGSMModemMonitor(const ActiveGSMModemMonitor& );
    //! assignment operator. Not implemented
    ActiveGSMModemMonitor& operator=(const ActiveGSMModemMonitor& );

    static bool extractSignalValue(const std::string& signalLevelString, int& gpsModemSignalLevel);

    //Private members
    Fault* m_pGSMModemUnableToConnectFault;
    ::Clock* m_pClock;

    TTime_t m_lastActionTime;
    TTimeDiff_t m_actionPeriod;

    mutable boost::recursive_mutex m_mutex;

    const ICoreConfiguration& m_configurationParameters;
    std::string m_hostName;
    std::string m_userName;
    std::string m_password;

    bool m_GPSSignalOK;

    GSMModem::SignalLevelProcessor& m_signalLevelProcessor;

    TSteadyTimeDuration m_signalLevelSamplingPeriod;
    TSteadyTimePoint m_lastSignalLevelCheckTime;
};


}

#endif //_ACTIVE_GSM_MODEM_MONITOR_H_
