#include "stdafx.h"
#include "reversesshconnector.h"

#include "fault.h"
#include "instation/ihttpclient.h"
#include "logger.h"
#include "utils.h"

#include <boost/thread/locks.hpp>
#include <climits>

namespace
{
    const char REVERSE_SSH_PROGRAM_NAME[] = BIN_DIRECTORY "/reverse_ssh";
    const char PARAMETERS_FILE_NAME[] = "/tmp/reverse_ssh.conf";

    const unsigned int DEFAULT_TIMEOUT_IN_MS = 100;
}



#ifdef _WIN32

//In the case of windows we do not do anything
namespace InStation
{

ReverseSSHConnector::ReverseSSHConnector(
    Model::Fault* )
:
IReverseSSHConnector(),
::ITask(),
::IObserver(),
::IObservable(),
m_address(),
m_portNumber(0),
m_login(),
m_password(),
m_actionCollection(),
m_mutex(),
m_semaphoreAction("ReverseSSHConnector::semaphoreAction", UINT_MAX),
m_enabled(false),
m_pInStationSSHUnableToConnectFault(0)
{
    //do nothing
}

ReverseSSHConnector::~ReverseSSHConnector()
{
    //do nothing
}

void ReverseSSHConnector::initialise()
{
    //do nothing
}

void ReverseSSHConnector::perform()
{
    //do nothing
}

void ReverseSSHConnector::shutdown()
{
    //do nothing
}

void ReverseSSHConnector::stop()
{
    //do nothing
}

void ReverseSSHConnector::notifyOfStateChange(::IObservable* , const int )
{
    //do nothing
}

void ReverseSSHConnector::setup(
    const std::string& address,
    const unsigned short portNumber,
    const std::string& login,
    const std::string& password)
{
    m_address = address;
    m_portNumber = portNumber;
    m_login = login;
    m_password = password;
}

void ReverseSSHConnector::open(const unsigned short )
{
    //do nothing
}

void ReverseSSHConnector::close()
{
    //do nothing
}

bool ReverseSSHConnector::isRunning(TConnectionParameters* ) const
{
    return false;
}

} //namespace


#else

#include "execute_command.h"

namespace InStation
{

ReverseSSHConnector::ReverseSSHConnector(
    Model::Fault* pInStationSSHUnableToConnectFault)
:
IReverseSSHConnector(),
::ITask(),
::IObserver(),
::IObservable(),
m_address(),
m_portNumber(0),
m_login(),
m_password(),
m_actionCollection(),
m_mutex(),
m_semaphoreAction("ReverseSSHConnector::semaphoreAction", UINT_MAX),
m_enabled(false),
m_pInStationSSHUnableToConnectFault(pInStationSSHUnableToConnectFault)
{
    //TODO: add verification that address makes any sense
}

ReverseSSHConnector::~ReverseSSHConnector()
{
    //do nothing
}

void ReverseSSHConnector::initialise()
{
    m_enabled.set(true);
}

void ReverseSSHConnector::perform()
{
    if (!m_enabled.get())
        return;

    m_semaphoreAction.wait(DEFAULT_TIMEOUT_IN_MS);

    bool collectionIsEmpty = false;
    {
        boost::lock_guard<boost::mutex> lock(m_mutex);
        collectionIsEmpty = m_actionCollection.empty();
    }

    if (!collectionIsEmpty)
    {
        TAction actionToDo;

        {
            boost::lock_guard<boost::mutex> lock(m_mutex);
            actionToDo = m_actionCollection.front();
        }

        switch (actionToDo.whatToDo)
        {
            case eActionToBePerformed_OPEN:
            {
                if (_open(actionToDo.remotePort))
                {
                    if ((m_pInStationSSHUnableToConnectFault != 0) && m_pInStationSSHUnableToConnectFault->get())
                    {
                        m_pInStationSSHUnableToConnectFault->clear();
                    }
                    //else do nothing
                }
                else
                {
                    if ((m_pInStationSSHUnableToConnectFault != 0) && !m_pInStationSSHUnableToConnectFault->get())
                    {
                        m_pInStationSSHUnableToConnectFault->set();
                    }
                    //else do nothing
                }

                notifyObservers();

                break;
            }

            case eActionToBePerformed_CLOSE:
            {
                _close();
                notifyObservers();

                break;
            }

            default:
            {
                break;
            }
        } //switch

        //Remove the just processed entry from the collection
        {
            boost::lock_guard<boost::mutex> lock(m_mutex);
            m_actionCollection.pop_front();
        }
    }
    //else do nothing
}

void ReverseSSHConnector::shutdown()
{
    stop();
}

void ReverseSSHConnector::stop()
{
    m_enabled.set(false);
    m_semaphoreAction.release();
}


void ReverseSSHConnector::notifyOfStateChange(
    ::IObservable* pObservable, const int index)
{
    assert(pObservable != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste

    {
        IHTTPClient* pHTTPClient =
            dynamic_cast<IHTTPClient* >(pObservable);
        if (pHTTPClient != 0)
        {
            if (index == IHTTPClient::eOPEN_SSH_CONNECTION)
            {
                open(pHTTPClient->getRemoteSSHPortNumber());
            }
            else if (index == IHTTPClient::eCLOSE_SSH_CONNECTION)
            {
                close();
            }
            else
            {
                //do nothing
            }

            return;
        }
        //else do nothing
    }
}

void ReverseSSHConnector::setup(
    const std::string& address,
    const unsigned short portNumber,
    const std::string& login,
    const std::string& password)
{
    boost::lock_guard<boost::mutex> lock(m_mutex);
    m_address = address;
    m_portNumber = portNumber;
    m_login = login;
    m_password = password;
}

void ReverseSSHConnector::open(const unsigned short remotePort)
{
    close();

    TAction action;
    action.whatToDo = eActionToBePerformed_OPEN;
    action.remotePort = remotePort;

    boost::lock_guard<boost::mutex> lock(m_mutex);
    m_actionCollection.push_back(action);
    m_semaphoreAction.release();
}

bool ReverseSSHConnector::_open(const unsigned short remotePort)
{
    bool result = false;

    std::string address;
    std::string login;
    std::string password;
    {
        boost::lock_guard<boost::mutex> lock(m_mutex);
        address = m_address;
        login = m_login;
        password = m_password;
    }

    if (!address.empty())
    {
        if (!isRunning())
        {
            //Prepare configuration file
            std::ofstream parametersFile;
            parametersFile.open(PARAMETERS_FILE_NAME, std::ofstream::out);
            if (parametersFile.is_open())
            {
                boost::lock_guard<boost::mutex> lock(m_mutex);
                parametersFile
                    << address << " "
                    << remotePort << " "
                    << login << " "
                    << password;
                parametersFile.close();
            }
            else
            {
                std::ostringstream ss;
                ss << "Unable to open parametersFile " << PARAMETERS_FILE_NAME;
                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

                return false;
            }

            //Prepare reverse SSH script.
            TStringArray argvStringArray;
            argvStringArray.push_back(REVERSE_SSH_PROGRAM_NAME);
            argvStringArray.push_back("start");

            result = (::execute(argvStringArray) == 0);
        }
        else
        {
            std::ostringstream ss;
            ss << "The " << REVERSE_SSH_PROGRAM_NAME << " program is still running and cannot be started";
            Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
        }
    }
    else
    {
        std::ostringstream ss;
        ss << "The IP address of the SSH connection is empty. " << REVERSE_SSH_PROGRAM_NAME << " will not be run";
        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
    }

    return result;
}

void ReverseSSHConnector::close()
{
    TAction action;
    action.whatToDo = eActionToBePerformed_CLOSE;

    boost::lock_guard<boost::mutex> lock(m_mutex);
    m_actionCollection.push_back(action);
    m_semaphoreAction.release();
}

void ReverseSSHConnector::_close()
{
    if (isRunning())
    {
        //Prepare reverse SSH script.
        TStringArray argvStringArray;
        argvStringArray.push_back(REVERSE_SSH_PROGRAM_NAME);
        argvStringArray.push_back("stop");

        ::execute(argvStringArray); //Ignore result
    }
    //else do nothing
}

bool ReverseSSHConnector::isRunning(
    TConnectionParameters* pConnectionParameters) const
{
    bool result = false;

    //Prepare reverse SSH script.
    TStringArray argvStringArray;
    argvStringArray.push_back(REVERSE_SSH_PROGRAM_NAME);
    argvStringArray.push_back("status");

    int programReturnValue = ::execute(argvStringArray);
    result = (programReturnValue == 0);

    if ((pConnectionParameters != 0) && result)
    {
        std::ifstream pidFile;
        pidFile.open("/tmp/reverse_ssh.pid");
        if (pidFile.is_open())
        {
            //Read file contents
            std::string firstLine;
            std::getline(pidFile, firstLine);

            result = parsePidFileContent(firstLine, pConnectionParameters);
        }
        //else do nothing

        pidFile.close();
    }
    //else do nothing

    return result;
}

bool ReverseSSHConnector::parsePidFileContent(
    const std::string& content,
    TConnectionParameters* pConnectionParameters)
{
    if (pConnectionParameters == 0)
        return false;

    bool result = false;

    //TODO Add logging information to identify the cause of failure

    //Ignore the first value - pid
    size_t firstSpaceLocation = content.find_first_of(' ');
    size_t secondSpaceLocation = content.find_first_of(' ', firstSpaceLocation+1);
    if (
        (firstSpaceLocation != std::string::npos) &&
        (secondSpaceLocation != std::string::npos)
        )
    {
        pConnectionParameters->address = content.substr(
            firstSpaceLocation + 1, secondSpaceLocation-firstSpaceLocation-1);

        int portNumber = 0;
        result = Utils::stringToInt(content.substr(
            secondSpaceLocation + 1),
            portNumber);
        if (result && (portNumber>0) && (portNumber<=USHRT_MAX))
        {
            pConnectionParameters->remotePortNumber = portNumber;
        }
        else
        {
            result = false;
        }
    }
    //else do nothing

    return result;
}

} //namespace

#endif
