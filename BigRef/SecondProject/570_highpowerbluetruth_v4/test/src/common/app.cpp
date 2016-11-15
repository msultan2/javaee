#include "stdafx.h"
#include "app.h"


#include <cassert>
#include <iostream>
#include <signal.h>
#include <stdlib.h>


namespace
{
    EProgramReturnCode programReturnCode = ePROGRAM_RETURN_OK;
}


//The first chance exception being raised is the CTRL-C event which is trapped
//by the debugging environment. This is expected behaviour.
//One can choose to ignore this: go to Debug menu/Exceptions/Win32 Exceptions
//and take out the CONTROL-C check from the "Thrown" column menu.
//This will ensure that the debugger only breaks on CONTROL-C when it is user-unhandled.
//For details see: http://stackoverflow.com/questions/13206911/why-getting-first-chace-exception-in-c

void processReceivedSignal(int sig, const EProgramReturnCode _programReturnCode)
{
    std::cout << "Signal " << sig << " caught..." << std::endl;

    programReturnCode = _programReturnCode;

    switch (sig)
    {
        case SIGABRT:
        case SIGTERM:
        case SIGINT:
#ifdef __linux
        case SIGQUIT:
#endif
        {
            break;
        }

#ifdef __linux
        case SIGUSR1:
        {
            break;
        }

        case SIGUSR2:
        {
            break;
        }
#endif

        default:
        {
            //Do nothing
            break;
        }
    }
}

EProgramReturnCode getProgramReturnCode()
{
    return programReturnCode;
}
