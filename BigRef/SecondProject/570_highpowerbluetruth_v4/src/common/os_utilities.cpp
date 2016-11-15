#include "stdafx.h"
#include "os_utilities.h"
#include "logger.h"


#include <string.h>
#include <cassert>
#include <cerrno>

#if defined _WIN32
#   include <windows.h>
#   include <errno.h>
#   include <direct.h>
#   include <cstdlib>
#   include <sstream>

#elif defined __linux__

#   include <cstdlib>
#   include <unistd.h>
#   include <sys/stat.h>
#   include <sys/types.h>
#   include <asm-generic/errno-base.h>
#   include <errno.h>

#else
#error Platform is not supported
#endif


#define LOGGER_DISABLED


namespace
{
    const unsigned int DEFAULT_BLOCK_ALIGN = 4U;
    const uint64_t BYTES_PER_MEGA_BYTE = 1024L * 1024L;

    const size_t BUFFER_SIZE = 256U;
    const int OS_ERROR = 0;
}


OS_Utilities::~OS_Utilities(void)
{
    //do nothing
}

bool OS_Utilities::directoryExists(const tchar* directoryName)
{
#ifdef _WIN32
    DWORD dwAttrib = ::GetFileAttributes(directoryName);

    return (dwAttrib != INVALID_FILE_ATTRIBUTES &&
           (dwAttrib & FILE_ATTRIBUTE_DIRECTORY));
#else
    if (directoryName == NULL)
        return false;

    struct stat st;
    std::string directoryNameTString(StringToAnsi(directoryName));
    if (stat(directoryNameTString.c_str(), &st) == 0)
    {
        return S_ISDIR(st.st_mode);
    }
    else
    {
        return false;
    }
#endif
}

bool OS_Utilities::createDirectory(const tchar* directoryName)
{

#ifdef _WIN32
    const int STATUS = _tmkdir(directoryName);
    const int ERROR_NUMBER = errno;

    const bool result = ((STATUS == 0) || (ERROR_NUMBER != ENOENT));

#else
    const int STATUS = mkdir(OS_Utilities::StringToAnsi(directoryName).c_str(), 0755);
    const int ERROR_NUMBER = errno;

    const bool result = ((STATUS == 0) || (ERROR_NUMBER == EEXIST));

#endif

    if (!result)
    {
        std::tostringstream ss;
        ss << _T("Fatal error: could not create directory \"") << directoryName << _T("\"");
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }
    //else do nothing

    return result;
}

bool OS_Utilities::removeFile(const tchar* fileName)
{

#ifdef _WIN32
    const int STATUS = _tremove(fileName);
    const int ERROR_NUMBER = errno;

    const bool result = ((STATUS == 0) || (ERROR_NUMBER != ENOENT));

#else
    const int STATUS = remove(OS_Utilities::StringToAnsi(fileName).c_str());
    const int ERROR_NUMBER = errno;

    const bool result = ((STATUS == 0) || (ERROR_NUMBER == ENOENT));

#endif

    if (!result)
    {
        std::tostringstream ss;
        ss << _T("Fatal error: could not delete file \"") << fileName << _T("\"");
		// TODO - uncomment
		//Logger::logFatal(ss.str().c_str());
    }
    //else do nothing

    return result;
}

const std::tstring OS_Utilities::StringToTString(const std::string& strFrom)
{
#if defined(UNICODE)
    return StringToUTF16(strFrom);
#else
    //No conversion
    return strFrom;
#endif
}

const std::tstring OS_Utilities::StringToTString(const std::wstring& strFrom)
{
#if defined(UNICODE)
    //No conversion
    return strFrom;
#else
    return StringToAnsi(strFrom);
#endif
}

const std::tstring OS_Utilities::StringToTString(const char* strFrom)
{
#if defined(UNICODE)
    return StringToUTF16(strFrom);
#else
    //No conversion
    const std::string result(strFrom);
    return result;
#endif
}

const std::tstring OS_Utilities::StringToTString(const wchar_t* strFrom)
{
#if defined(UNICODE)
    //No conversion
    const std::wstring result(strFrom);
    return result;
#else
    return StringToAnsi(strFrom);
#endif
}

const std::wstring OS_Utilities::StringToUTF16(const std::string& strFrom)
{
    return StringToUTF16(strFrom.c_str());
}

const std::string OS_Utilities::StringToAnsi(const std::wstring& strFrom)
{
    return StringToAnsi(strFrom.c_str());
}

const std::wstring OS_Utilities::StringToUTF16(const char* strFrom)
{
    std::wstring result;

    if (strFrom != 0)
    {
        const size_t LENGTH_OF_STR_FROM = ::strlen(strFrom);
        const size_t LENGTH_OF_STR_TO = LENGTH_OF_STR_FROM + 1;
        wchar_t* strTo = new wchar_t[LENGTH_OF_STR_TO];

        const size_t CONVERSION_RESULT = ::mbstowcs(strTo, strFrom, LENGTH_OF_STR_TO);

        if (CONVERSION_RESULT != static_cast<size_t>(-1))
        {
            result = strTo;
        }
        else
        {
#if !defined(LOGGER_DISABLED)
            Logger::logError("StringToUTF16, Invalid conversion");
#endif
        }

        delete[] strTo;
    }
    //else do nothing

    return result;
}

const std::string OS_Utilities::StringToAnsi(const wchar_t* strFrom)
{
    std::string result;

    if (strFrom != 0)
    {
        const size_t LENGTH_OF_STR_FROM = ::wcslen(strFrom);
        const size_t LENGTH_OF_STR_TO = LENGTH_OF_STR_FROM + 1;

        char* strTo = 0;

        try
        {

            strTo = new char[LENGTH_OF_STR_TO];

        }
        catch (std::bad_alloc& )
        {
            assert(false);
        }

        const size_t CONVERSION_RESULT = ::wcstombs(strTo, strFrom, LENGTH_OF_STR_TO);

        if (CONVERSION_RESULT != static_cast<size_t>(-1))
        {
            result = strTo;
        }
        else
        {
#if !defined(LOGGER_DISABLED)
            Logger::logError("StringToAnsi, Invalid conversion");
#endif
        }

        delete[] strTo;
    }
    //else do nothing

    return result;
}

#if defined __linux__
void __nsleep(const struct timespec *req, struct timespec *rem)
{
    struct timespec temp_rem;
    if (::clock_nanosleep(CLOCK_MONOTONIC, 0, req, rem)==-1)
    {
        __nsleep(rem, &temp_rem);
    }
    //else do nothing
}
#endif

void OS_Utilities::sleep(const unsigned long milliseconds)
{
#ifdef _WIN32
    //Windows version
    ::Sleep(milliseconds);

#elif __linux__
    //Linux version
    const time_t SECONDS = static_cast<time_t>(milliseconds/1000);
    const unsigned int REMAINING_MILLISECONDS = milliseconds-(SECONDS*1000);

    struct timespec req={0, 0};
    struct timespec rem={0, 0};
    req.tv_sec=SECONDS;
    req.tv_nsec=REMAINING_MILLISECONDS*1000000L;

    __nsleep(&req, &rem);
#else
#endif
}

void OS_Utilities::logLastOSError(const tchar* /*headingText*/, bool /*display*/)
{
#ifdef _WIN32
    unsigned int lastErrorValue = ::GetLastError();

    if (lastErrorValue!=0)
    {
        //tchar buffer[BUFFER_SIZE];

        //::FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM,
        //    0,
        //    lastErrorValue,
        //    0,
        //    buffer,
        //    BUFFER_SIZE,
        //    0);

        //std::tostringstream errorStream;
        //errorStream << headingText << ": " << buffer;

        //Logger::logError(errorStream.str().c_str(), display);
    }
    //else do nothing
#endif
}

#ifdef _WIN32
void OS_Utilities::setFileModificationTime(const tchar* /*filename*/, const time_t modificationTime)
{
    ::HANDLE fileToOpen = ::CreateFile(
        filename,
        GENERIC_WRITE,
        0,
        0,
        OPEN_EXISTING,
        FILE_ATTRIBUTE_NORMAL,
        0);

    if (fileToOpen == INVALID_HANDLE_VALUE)
    {
        //std::tostringstream errorStream;
        //errorStream << "setFileModificationTime: Unable to open file: " << filename;
        //Logger::logError(errorStream.str().c_str(), false);
    }
    else
    {
        ::FILETIME fileTimeToSet = {0};

        const int64_t WINDOWS_FORMAT_MODIFICATION_TIME =
            static_cast<int64_t>(modificationTime)
            * 10000000L
            + 116444736000000000L;

        fileTimeToSet.dwLowDateTime =
            static_cast<DWORD>(WINDOWS_FORMAT_MODIFICATION_TIME & 0xFFFFFFFF);

        fileTimeToSet.dwHighDateTime =
            static_cast<DWORD>((WINDOWS_FORMAT_MODIFICATION_TIME >> 32) & 0xFFFFFFFF);

        ::SYSTEMTIME systemTimeToSet = {0};
        ::SYSTEMTIME localSystemTimeToSet = {0};
        ::FILETIME localFileTimeToSet = {0};

        //The following three steps are needed to counter the automatic conversion from
        //local (i.e. BST) time to UTC. As we are not working with local times, this effectively
        //increments the file modification time by one hour during BST.

        ::FileTimeToSystemTime(&fileTimeToSet, &systemTimeToSet);
        ::SystemTimeToTzSpecificLocalTime(NULL, &systemTimeToSet, &localSystemTimeToSet);
        ::SystemTimeToFileTime(&localSystemTimeToSet, &localFileTimeToSet);

        ::SetFileTime(fileToOpen, &localFileTimeToSet, &localFileTimeToSet, &localFileTimeToSet);

        ::CloseHandle(fileToOpen);
    }
}
#else
void OS_Utilities::setFileModificationTime(const tchar* /*filename*/, const time_t /*modificationTime*/)
{
    //TODO Linux version to be implemented
}
#endif
