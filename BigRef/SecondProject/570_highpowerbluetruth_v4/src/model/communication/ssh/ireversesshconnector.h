/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    23/10/2013  RG      001         V1.00 First Issue
*/

#ifndef I_REVERSE_SSH_CONNECTOR_H_
#define I_REVERSE_SSH_CONNECTOR_H_

#include "types.h"


namespace InStation
{

class IReverseSSHConnector
{

public:

    //! destructor
    virtual ~IReverseSSHConnector();


    struct ConnectionParameters
    {
        std::string address;
        unsigned short remotePortNumber;
    };
    typedef struct ConnectionParameters TConnectionParameters;

    /**
     * @brief Check if the reverse SSH channel is currently running
     *
     * @return true - if it is running, false - otherwise.
     * */
    virtual bool isRunning(TConnectionParameters* pConnectionParameters = 0) const = 0;

protected:

    //! default constructor
    IReverseSSHConnector();
    //! copy constructor
    IReverseSSHConnector(const IReverseSSHConnector& );
    //! assignment operator
    IReverseSSHConnector& operator=(const IReverseSSHConnector& );

};

}

#endif //I_REVERSE_SSH_CONNECTOR_H_
