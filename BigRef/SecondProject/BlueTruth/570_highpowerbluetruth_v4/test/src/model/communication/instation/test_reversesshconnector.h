/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:

    Modification History:

    Date        Who     SCJS No     Remarks
    20/09/2013  RG      001         V1.00 First Issue
*/

#ifndef _TEST_REVERSE_SSH_CONNECTOR_H_
#define _TEST_REVERSE_SSH_CONNECTOR_H_

#include "ssh/ireversesshconnector.h"

#include <types.h>


namespace Testing
{

class TestReverseSSHConnector :
    public InStation::IReverseSSHConnector
{
public:
	TestReverseSSHConnector();

    virtual ~TestReverseSSHConnector();

    virtual bool isRunning(TConnectionParameters* pConnectionParameters = 0) const;
    void setConnectionParameters(
        const bool shouldBeConnected, const TConnectionParameters& parameters);

private:
    //! copy constructor. Not implemented
    TestReverseSSHConnector(const TestReverseSSHConnector &rhs);
    //! copy assignment operator. Not implemented
    TestReverseSSHConnector& operator=(const TestReverseSSHConnector&rhs);

    //Private members
    bool m_shouldBeConnected;
    TConnectionParameters m_connectionParameters;
};

}//namespace

#endif //_TEST_REVERSE_SSH_CONNECTOR_H_
