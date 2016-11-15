#include <fstream>
#include <iostream>
#include <sstream>
#include <stdio.h>
#include <string>
#include <string.h>
#include <sys/wait.h>
#include <unistd.h>
#include <vector>

#include "logger.h"


#define PIPE_READ 0
#define PIPE_WRITE 1


//Based on comment from
//http://stackoverflow.com/questions/548063/kill-a-process-started-with-popen
/**
 * @brief Execute a command in a new process
 * @param command A command to be executed
 * @param pInFileDescriptor input file pointer. All input to this file will be passed to the newly created process
 * @param pOutFileDescriptor output file pointer. All output from the created process will be streamed to this file
 * @return pid of the newly created process
 */
::pid_t popen2(char** command, int* , int* )
//static ::pid_t popen2(char** command, int* pInFileDescriptor, int* pOutFileDescriptor)
{
    //TODO This function must be reviewed and debugged in NativeBlueZ mode

    int fd[2];
    ::pid_t pid;

    if (::pipe(fd) != 0)
    {
        return -1;
    }
    //else continue

    pid = ::fork();
    if (pid < 0) //failure
    {
        return pid;
    }
    else if (pid == 0) //child process
    {
        if (::dup2(fd[PIPE_WRITE], STDOUT_FILENO) < 0)
            std::cerr << "Error occurred in dup2 in popen2 (child, stdout)";
        ::close(fd[PIPE_WRITE]);

        if (::dup2(fd[PIPE_READ], STDIN_FILENO) < 0)
            std::cerr << "Error occurred in dup2 in popen2 (child, stdin)";
        ::close(fd[PIPE_READ]);

        ::execvp(*command, command); //this function may only return in the case of error
        ::perror("execvp");
        exit(1);
    }
    else //continue - this is the parent thread
    {
        std::ostringstream ss;
        ss << "New process started PID=" << static_cast<unsigned int>(pid);
        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

        //if (pInFileDescriptor == NULL)
            ::close(fd[PIPE_WRITE]);
        //else
        //    *pInFileDescriptor = fd[PIPE_WRITE];

        //if (pOutFileDescriptor == NULL)
            ::close(fd[PIPE_READ]);
        //else
        //    *pOutFileDescriptor = fd[PIPE_READ];
    }

    return pid;
}

/**
 * @brief Create argv from string array.
 * The non-const argv array is required for execvp function and needs to be constructed before its invocation.
 */
typedef std::vector<std::string> TStringArray;
char** createArgVFromVector(const TStringArray& allArgs)
{
    size_t nArgs = allArgs.size();
    if (nArgs == 0)
    {
        return NULL;
    }
    //else continue

    //allocate argv buffer
    char **buf = new char*[nArgs + 1];
    for (size_t i=0; i<nArgs; ++i)
    {
        buf[i] = new char[allArgs[i].size() + 1];
        strcpy(buf[i], allArgs[i].c_str());
        //strcat( buf[i], 0 );
    }
    buf[nArgs] = NULL;
    return buf;
}

/**
 * @brief delete argv array created with createArgVFromVector
 */
void destroyArgV(char** argv, const size_t nArgs)
{
    for (size_t i=0; i<nArgs; ++i)
    {
        delete[] argv[i];
    }
    delete[] argv;
}


/**
 * @brief execute a command and provide return result
 * */
int execute(const TStringArray& argvStringArray)
{
    int result = -1;
    char** command = createArgVFromVector(argvStringArray);
    {
        std::ostringstream ss;
        ss << "COMMAND to be executed: ";
        for (size_t i=0; i<argvStringArray.size(); ++i)
        {
            ss << argvStringArray[i] << " ";
        }

        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
    }
    std::string commandTobExecuted(command[0]);

    int inFileDescriptor = 0;
    int outFileDescriptor = 0;
    ::pid_t pid = popen2(command, &inFileDescriptor, &outFileDescriptor);

    destroyArgV(command, argvStringArray.size()+1);

    //Wait for the program to finish
    if (pid != 0)
    {
        int status = 0;
        //Wait for the process to exit
        ::pid_t wait_pid_result = ::waitpid(pid, &status, 0);
        if (wait_pid_result >= 0)
        {
            //The process exists, check its status
            if (WIFEXITED(status))
            {
                //Process existed, display returned value by the program
                std::ostringstream ss;
                ss << "The " << commandTobExecuted << " program returned " << WEXITSTATUS(status);
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

                result = WEXITSTATUS(status);
            }
            else if (WIFSIGNALED(status))
            {
                //Process has been terminated
                std::ostringstream ss;
                ss << "The " << commandTobExecuted << " program was terminated";
                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

                result = -2;
            }
            else
            {
                //Process is running (it may be also stopped but we will not do in this case)
            }
        }
        else
        {
            ::perror("waitpid");

            result = -3;
        }
    }
    //else do nothing

    return result;
}

