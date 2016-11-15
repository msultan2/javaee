#include "stdafx.h"
#include "controller.h"

#include "model.h"
//#include "logger.h"

namespace
{
    const char MODULE_NAME[] = "Controller";
}


namespace Controller
{

Controller* Controller::m_instancePtr = 0;
bool Controller::m_valid = true;


Controller* Controller::getInstancePtr()
{
    return m_instancePtr;
}

Controller::Controller()
{
    //do nothing
}

Controller::~Controller()
{
    //do nothing
}

bool Controller::construct()
{
    if (m_instancePtr == 0) 
    {
        m_instancePtr = new Controller();
    }
    else
    {
        // already constructed, do nothing!
    }

    return m_valid;
}

void Controller::destruct()
{
    if (m_instancePtr != 0) 
    {
        std::cout << "The controller will be destroyed" << std::endl;

        delete m_instancePtr;
        m_instancePtr = 0;

        std::cout << "The controller has been destroyed" << std::endl;
    }
    else
    {
        // already destroyed, do nothing!
    }
}

bool Controller::isValid()
{
    return m_valid;
}

} //namespace
