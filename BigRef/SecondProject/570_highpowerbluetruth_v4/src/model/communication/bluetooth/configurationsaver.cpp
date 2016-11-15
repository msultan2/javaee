#include "stdafx.h"
#include "configurationsaver.h"

#include "datacontainer.h"
#include "icoreconfiguration.h"


namespace BlueTooth
{

ConfigurationSaver::ConfigurationSaver(Model::ICoreConfiguration& coreConfiguration)
:
::IObserver(),
m_coreConfiguration(coreConfiguration),
m_lastSavedAddress(0)
{
    //do nothing
}

ConfigurationSaver::~ConfigurationSaver()
{
    //do nothing
}

void ConfigurationSaver::notifyOfStateChange(IObservable* observablePtr, const int )
{
    assert(observablePtr != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste

    {
        Model::DataContainer* pDataContainer =
            dynamic_cast<Model::DataContainer* >(observablePtr);

        if ((pDataContainer != 0) && (pDataContainer->getLocalDeviceRecord() != 0))
        {
            const uint64_t address = pDataContainer->getLocalDeviceRecord()->address;
            if (address != m_lastSavedAddress)
            {
                m_coreConfiguration.setLastUsedBlueToothDevice(address);
                m_lastSavedAddress = address;
            }
            //else do nothing

            return;
        }
        //else do nothing
    }
}

} //namespace
