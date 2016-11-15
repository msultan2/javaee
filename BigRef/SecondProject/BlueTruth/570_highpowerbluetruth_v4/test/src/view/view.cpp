#include "stdafx.h"
#include "view.h"

#include "activeboostasiotcpclient.h"
#include "bluetooth/acquiredevicestask.h"
#include "datacontainer.h"
#include "icoreconfiguration.h"
#include "lock.h"
#include "logger.h"
#include "model.h"
#include "os_utilities.h"
#include "queuedetector.h"


namespace
{
    const char MODULE_NAME[] = "View";
}

namespace View
{

View* View::m_instancePtr = 0;
bool View::m_valid = true;


View* View::getInstancePtr()
{
    return m_instancePtr;
}

View::View(Model::ICoreConfiguration& configurationParameters)
:
::IObserver(),
m_configurationParameters(configurationParameters)
{
}

View::~View()
{
    //do nothing
}

void View::notifyOfStateChange(::IObservable* observablePtr)
{
    notifyOfStateChange(observablePtr, 0);
}

void View::notifyOfStateChange(::IObservable* observablePtr, const int )
{
    assert(observablePtr != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste

}

bool View::construct()
{
    if (m_instancePtr == 0)
    {
        //m_instancePtr = new View(*OutStationConfigurationParameters::getInstancePtr());
    }
    else
    {
        // already constructed, do nothing!
    }

    return m_valid;
}

void View::destruct()
{
    if (m_instancePtr != 0)
    {
        delete m_instancePtr;
        m_instancePtr = 0;
    }
    else
    {
        // already destroyed, do nothing!
    }
}

bool View::isValid()
{
    return m_valid;
}

void View::log(boost::shared_ptr<LogRecord> )
{
}

} //namespace
