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
:
m_blueToothDeviceDiscoveryState(eINTERFACE_STOPPED),
m_retrieveConfigurationClientState(eINTERFACE_STOPPED),
m_instationClientState(eINTERFACE_STOPPED),
m_GSMMonitorClientState(eINTERFACE_STOPPED)
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
        delete m_instancePtr;
        m_instancePtr = 0;
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


void Controller::startStopBlueToothDeviceDiscovery()
{
    getInstancePtr()->_startStopBlueToothDeviceDiscovery();
}

void Controller::_startStopBlueToothDeviceDiscovery()
{
    switch (static_cast<int>(m_blueToothDeviceDiscoveryState))
    {
        case eINTERFACE_STARTED:
        {
            m_blueToothDeviceDiscoveryState = eINTERFACE_STOPPED;
            Model::Model::stopBlueToothDeviceDiscovery();

            break;
        }

        case eINTERFACE_STOPPED:
        {
            if (Model::Model::startBlueToothDeviceDiscovery())
            {
                m_blueToothDeviceDiscoveryState = eINTERFACE_STARTED;
            }
            //else do nothing

            break;
        }

        default:
        {
            assert(false);
            break;
        }
    }
}

Controller::EInterfaceState Controller::getBlueToothDeviceDiscoveryStatus()
{
    return getInstancePtr()->m_blueToothDeviceDiscoveryState;
}


void Controller::startStopRetrieveConfigurationClient()
{
    getInstancePtr()->_startStopRetrieveConfigurationClient();
}

void Controller::_startStopRetrieveConfigurationClient()
{
    switch (static_cast<int>(m_retrieveConfigurationClientState))
    {
        case eINTERFACE_STARTED:
        {
            m_retrieveConfigurationClientState = eINTERFACE_STOPPED;
            Model::Model::stopRetrieveConfigurationClient();

            break;
        }

        case eINTERFACE_STOPPED:
        {
            if (Model::Model::startRetrieveConfigurationClient())
            {
                m_retrieveConfigurationClientState = eINTERFACE_STARTED;
            }
            //else do nothing

            break;
        }

        default:
        {
            assert(false);
            break;
        }
    }
}

Controller::EInterfaceState Controller::getRetrieveConfiguationClientStatus()
{
    return getInstancePtr()->m_retrieveConfigurationClientState;
}

void Controller::startStopInstationClient()
{
    getInstancePtr()->_startStopInstationClient();
}

void Controller::_startStopInstationClient()
{
    switch (static_cast<int>(m_instationClientState))
    {
        case eINTERFACE_STARTED:
        {
            m_instationClientState = eINTERFACE_STOPPED;
            Model::Model::stopAllInstationClients();

            break;
        }

        case eINTERFACE_STOPPED:
        {
            if (Model::Model::startAllInstationClients())
            {
                m_instationClientState = eINTERFACE_STARTED;
            }
            //else do nothing

            break;
        }

        default:
        {
            assert(false);
            break;
        }
    }
}

Controller::EInterfaceState Controller::getInstationClientStatus()
{
    return getInstancePtr()->m_instationClientState;
}

void Controller::startStopGSMModemMonitor()
{
    getInstancePtr()->_startStopGSMModemMonitor();
}

void Controller::_startStopGSMModemMonitor()
{
    switch (static_cast<int>(m_GSMMonitorClientState))
    {
        case eINTERFACE_STARTED:
        {
            m_GSMMonitorClientState = eINTERFACE_STOPPED;
            Model::Model::stopGSMModemSSHMonitor();

            break;
        }

        case eINTERFACE_STOPPED:
        {
            if (Model::Model::startGSMModemSSHMonitor())
            {
                m_GSMMonitorClientState = eINTERFACE_STARTED;
            }
            //else do nothing

            break;
        }

        default:
        {
            assert(false);
            break;
        }
    }
}

Controller::EInterfaceState Controller::getGSMModemMonitor()
{
    return getInstancePtr()->m_GSMMonitorClientState;
}

} //namespace
