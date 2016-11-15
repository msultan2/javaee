#include "stdafx.h"
#include "model.h"

#include "icoreconfiguration.h"
#include "iniconfiguration.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"


namespace Model
{

Model* Model::m_instancePtr = 0;
bool Model::m_valid = true;

Model* Model::getInstancePtr()
{
    return m_instancePtr;
}

Model::Model()
:
m_pIniConfiguration(),
m_coreConfiguration()
{
    m_pIniConfiguration = boost::shared_ptr<IniConfiguration>(new IniConfiguration());

    if (isValid())
    {
        _applyNewIniConfiguration(m_pIniConfiguration);
    }
    //else do nothing
}

Model::~Model()
{
    //do nothing
}

bool Model::construct()
{
    if (m_instancePtr == 0)
    {
        m_instancePtr = new Model();
    }
    //else do nothing

    return m_valid;
}

void Model::destruct()
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

bool Model::isValid()
{
    return m_valid;
}

void Model::applyNewIniConfiguration(const boost::shared_ptr<IniConfiguration> pIniConfiguration)
{
    getInstancePtr()->_applyNewIniConfiguration(pIniConfiguration);
}

void Model::_applyNewIniConfiguration(const boost::shared_ptr<IniConfiguration> pIniConfiguration)
{
    m_pIniConfiguration = pIniConfiguration;
}

} //namespace
