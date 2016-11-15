/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: This is an implementation of a class to start/stop and monitor
    SSH reverse tunnel. This class works closely with reverse_ssh script
    which is written in as debian service. For more details see the script itself.

    Modification History:

    Date        Who     SCJS No     Remarks
    20/09/2013  RG      001         V1.00 First Issue
*/

#ifndef _REVERSE_SSH_CONNECTOR_H_
#define _REVERSE_SSH_CONNECTOR_H_

#include "ireversesshconnector.h"
#include "itask.h"
#include "iobserver.h"
#include "iobservable.h"

#include "atomicvariable.h"
#include "boostsemaphore.h"

#include <boost/thread/recursive_mutex.hpp>
#include <list>
#include <types.h>


namespace Model
{
    class Fault;
}

namespace InStation
{

class ReverseSSHConnector :
    public IReverseSSHConnector,
    public ::ITask,
    public ::IObserver,
    public ::IObservable
{
public:
    explicit ReverseSSHConnector(Model::Fault* pInStationSSHUnableToConnectFault);

    virtual ~ReverseSSHConnector();

    virtual void initialise();

    virtual void perform();

    virtual void shutdown();

    virtual void stop();

    virtual void notifyOfStateChange(::IObservable* pObservable, const int index);


    /**
     * @brief Setup the class for subsequent use. The parameters should be read
     * from the BlueTruth configuration file
     *
     * This function works closely with reverse_ssh script which is written in
     * as debian service. For more details see the script itself.
     * @param [in] address where to connect to
     * @param [in] portNumber local port number to be used (should be generally 22)
     * @param [in] login user name to be used
     * @param [in] password very secret word to establish connection
     * */
    void setup(
        const std::string& address,
        const unsigned short portNumber,
        const std::string& login,
        const std::string& password);

    /**
     * @brief Open reverse SSH channel.
     *
     * If the reverse ssh channel is not running start it. Do nothing if it does run.
     * This function works closely with reverse_ssh script which is written in
     * as debian service. For more details see the script itself.
     * @param remotePort remote entry port
     * */
    void open(const unsigned short remotePort);

    /**
     * @brief Close the currently running reverse SSH channel.
     *
     * If the reverse ssh channel is running stop it. Do nothing if it does not run.
     * This function works closely with reverse_ssh script which is written in
     * as debian service. For more details see the script itself.
     * */
    void close();


    /**
     * @brief Check if the reverse SSH channel is currently running
     *
     * During function call the PID file is read.
     * @param [out] pConnectionParameters a structure containing the read values from the PID file
     * @return true - if it is running, false - otherwise.
     * */
    virtual bool isRunning(TConnectionParameters* pConnectionParameters = 0) const;


    /**
     * @brief When the ssh service/connection is run a file containing proces PID is saved in the /tmp directory.
     * This file contains also where the connection was made and what is the remote port number.
     * This function parses this file contents and saves them in pConnnectionParameters object
     *
     * @param content the PID file content
     * @param pConnectionParameters an object where the parsed/extracted values should be saved
     * @return true if the string syntax is correct and parameters could be extracted, false otherwise
     */
    static bool parsePidFileContent(
        const std::string& content,
        TConnectionParameters* pConnectionParameters);

private:
    //! default constructor. Not implemented
    ReverseSSHConnector();
    //! copy constructor. Not implemented
    ReverseSSHConnector(const ReverseSSHConnector &rhs);
    //! copy assignment operator. Not implemented
    ReverseSSHConnector& operator=(const ReverseSSHConnector &rhs);


    /**
     * @brief Asynchronously open reverse SSH channel
     */
    bool _open(const unsigned short remotePort);


    /**
     * @brief Asynchronously close reverse SSH channel
     */
    void _close();


    //Private members
    std::string m_address;
    unsigned short m_portNumber;
    std::string m_login;
    std::string m_password;

    enum EActionToBePerformed
    {
        eActionToBePerformed_OPEN,
        eActionToBePerformed_CLOSE
    };

    struct TAction
    {
        EActionToBePerformed whatToDo;
        uint16_t remotePort;
    };
    typedef std::list<TAction> TActionCollection;

    TActionCollection m_actionCollection;
    mutable boost::mutex m_mutex;
    ///This semaphore acts here as accelerator, a signal
    //is generated whenever there is something to do
    ::BoostSemaphore m_semaphoreAction;
    ::AtomicVariable<bool> m_enabled;

    Model::Fault* m_pInStationSSHUnableToConnectFault;
};

}//namespace

#endif //_REVERSE_SSH_CONNECTOR_H_
