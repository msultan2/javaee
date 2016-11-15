/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/


#ifndef __OS_UTILITIES_H
#define __OS_UTILITIES_H

#include "types.h"
#include "unicode_types.h"
#include <vector>

class OS_Utilities
{
public:

    virtual ~OS_Utilities(void);

    static bool directoryExists(const tchar* directoryName);
#ifndef __TI_COMPILER_VERSION__
    static bool createDirectory(const tchar* directoryName);

    static bool removeFile(const tchar* fileName);
#endif

    //String versions
    static const std::tstring StringToTString(const std::string& strFrom);
    static const std::tstring StringToTString(const std::wstring& strFrom);

    //Char versions
    static const std::tstring StringToTString(const char* strFrom);
    static const std::tstring StringToTString(const wchar_t* strFrom);

    //String versions
    static const std::wstring StringToUTF16(const std::string& strFrom);
    static const std::wstring& StringToUTF16(const std::wstring& strFrom) { return strFrom; }
    static const std::string StringToAnsi(const std::wstring& strFrom);
    static const std::string& StringToAnsi(const std::string& strFrom) { return strFrom; }

    //Char versions
    static const std::wstring StringToUTF16(const char* strFrom);
    static const std::wstring StringToUTF16(const wchar_t* strFrom) { const std::wstring result(strFrom); return result; }
    static const std::string StringToAnsi(const wchar_t* strFrom);
    static const std::string StringToAnsi(const char* strFrom) { const std::string result(strFrom); return result; }

    static void sleep(const unsigned long milliseconds);

    static void logLastOSError(const tchar* headingText, bool display = false);

    static void setFileModificationTime(const tchar* filename, const time_t modificationTime);

private:
    //Not implemeted
    OS_Utilities(void);
    OS_Utilities(const OS_Utilities &rhs);
    OS_Utilities& operator=(const OS_Utilities &rhs);
    
};

#endif //__OS_UTILITIES_H
