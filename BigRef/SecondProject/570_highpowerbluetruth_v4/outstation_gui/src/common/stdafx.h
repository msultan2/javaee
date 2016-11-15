// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#ifndef STD_AFX_H_
#define STD_AFX_H_
#pragma once

#ifdef _WIN32
#include "targetver.h"
#endif

#include <stdio.h>
#ifdef WIN32

//Allow std::max template to work
#define NOMINMAX

#include <winsock2.h>
#include <windows.h>
#include <tchar.h>

//Disable some compiler warnings
#pragma warning(disable:4127) //warning C4127: conditional expression is constant

#define HAVE_BOOLEAN

#else
//typedef char _TCHAR;
//typedef unsigned long DWORD;
#define _tmain main
#endif

enum
{
    CONGESTION_REPORTING_CLIENT_IDENTIFIER = 1,
    RAW_DEVICE_DETECTION_CLIENT_IDENTIFIER,
    ALERT_AND_STATUS_REPORTING_CLIENT_IDENTIFIER,
    STATUS_REPORTING_CLIENT_IDENTIFIER,
    FAULT_REPORTING_CLIENT_IDENTIFIER,
    STATISTICS_REPORTING_CLIENT_IDENTIFIER,
    RETRIEVE_CONFIGURATION_CLIENT_IDENTIFIER
};


// TODO: reference additional headers your program requires here

#endif //STD_AFX_H_
