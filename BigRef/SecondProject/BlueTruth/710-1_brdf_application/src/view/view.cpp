#include "stdafx.h"
#include "view.h"


namespace
{
    const char MODULE_NAME[] = "View";
}

namespace View
{

View* View::m_instancePtr = 0;
bool View::m_valid = true;


View::~View()
{
    //do nothing
}

View* View::getInstancePtr()
{
    return m_instancePtr;
}

View::View(Model::BrdfXmlConfiguration& brdfConfiguration)
:
::IObserver(),
m_brdfXmlConfiguration(brdfConfiguration)
{
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

bool View::construct(Model::BrdfXmlConfiguration& brdfConfiguration)
{
    if (m_instancePtr == 0)
    {
        m_instancePtr = new View(brdfConfiguration);
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
