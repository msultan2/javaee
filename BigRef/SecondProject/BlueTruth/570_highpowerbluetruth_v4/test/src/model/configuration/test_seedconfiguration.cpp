#include "test_seedconfiguration.h"


namespace Testing
{

TestSeedConfiguration::TestSeedConfiguration()
:
m_id(0),
m_value(0)
{
    //do nothing
}

TestSeedConfiguration::~TestSeedConfiguration()
{
    //do nothing
}


uint32_t TestSeedConfiguration::getId() const
{
    return m_id;
}

void TestSeedConfiguration::setId(const uint32_t value)
{
    m_id = value;
}

uint32_t TestSeedConfiguration::getValue() const
{
    return m_value;
}

void TestSeedConfiguration::setValue(const uint32_t value)
{
    m_value = value;
}

bool TestSeedConfiguration::readAllParametersFromFile(const char* )
{
    return true;
}

}
