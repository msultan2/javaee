#include "stdafx.h"
#include "test_reversesshconnector.h"


namespace Testing
{

TestReverseSSHConnector::TestReverseSSHConnector()
:
InStation::IReverseSSHConnector(),
m_shouldBeConnected(false),
m_connectionParameters()
{
    //do nothing
}

TestReverseSSHConnector::~TestReverseSSHConnector()
{
    //do nothing
}

bool TestReverseSSHConnector::isRunning(
    TConnectionParameters* pConnectionParameters) const
{
    if (pConnectionParameters != 0)
        *pConnectionParameters = m_connectionParameters;

    return m_shouldBeConnected;
}

void TestReverseSSHConnector::setConnectionParameters(
    const bool shouldBeConnected, const TConnectionParameters& parameters)
{
    m_shouldBeConnected = shouldBeConnected;
    m_connectionParameters = parameters;
}

}
