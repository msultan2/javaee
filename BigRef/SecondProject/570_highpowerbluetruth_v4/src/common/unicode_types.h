/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/

#ifndef _UNICODE_TYPES_H
#define _UNICODE_TYPES_H

#include <string>
#include <sstream>
#include <fstream>

#if !defined(_WIN32) && !(defined(__TI_COMPILER_VERSION__))
#define LINUX_IN_USE 1
#endif

#if defined(UNICODE) || (LINUX_IN_USE)
#define CONVERT_TO_NON_UNICODE
#endif

#ifdef _WIN32

#include <tchar.h>
typedef TCHAR tchar;

#elif __TI_COMPILER_VERSION__

typedef char tchar;
#ifndef _T
#define _T(x) x
#endif //_T
        
#elif defined __linux__ || defined __FreeBSD__ //Linux or BSD

#ifdef UNICODE
#include <wchar.h>
typedef wchar_t tchar;

#ifndef _T
#define _T(x) L ## x
#endif //_T
#else

#ifndef _T
#define _T(x) x
#endif //_T

typedef char tchar;
#endif //UNICODE

#else
#error Not supported operating system
#endif //_WIN32


namespace std
{
    typedef basic_string<tchar, char_traits<tchar>, allocator<tchar> > tstring;
    typedef basic_stringstream<tchar, char_traits<tchar>, allocator<tchar> > tstringstream;
    typedef basic_ostringstream<tchar, char_traits<tchar>, allocator<tchar> > tostringstream;
    typedef basic_istringstream<tchar, char_traits<tchar>, allocator<tchar> > tistringstream;
    typedef basic_ofstream<tchar, char_traits<tchar> > tofstream;
    typedef basic_ifstream<tchar, char_traits<tchar> > tifstream;

    typedef basic_ofstream<wchar_t, char_traits<wchar_t> > wofstream;
    typedef basic_ifstream<wchar_t, char_traits<wchar_t> > wifstream;

#if defined UNICODE || defined _UNICODE
    #define _tstrlen wcslen
#else 
    #define _tstrlen strlen
#endif
}

#endif //_UNICODE_TYPES_H
