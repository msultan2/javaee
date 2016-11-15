/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Modification History:

    Date        Who     SCJS No     Remarks
    05/06/2009  EWT     001         V1.00 First Issue
  
*/

#include "stdafx.h"
#include "event.h"

#ifdef _WIN32
#include <windows.h>

Event::Event()
:
m_handle(0)
{
    // start in non-signaled state (red light)
    // auto reset after every wait
    m_handle = ::CreateEvent(0, FALSE, FALSE, 0);
}

Event::~Event()
{
    try
    {
        ::CloseHandle(m_handle);
    }
    catch (...)
    {
        //do nothing
    }
}

void Event::release()
{
    ::SetEvent(m_handle);
}

void Event::wait()
{
    ::WaitForSingleObject(m_handle, INFINITE);
}
#endif
