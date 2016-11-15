#include "stdafx.h"
#include "legacyfilelogsink.h"

#include "utils.h"
#include "lock.h"
#include "applicationconfiguration.h"
#include "os_utilities.h"
#include "version.h"

// system includes
#include <algorithm>
#include <stdio.h>
#include <iostream>
#include <sstream>

#ifdef _WIN32
#else
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#endif

namespace {

    const wchar_t DEFAULT_LOG_FILENAME_W[] = L"LOGFILE";

    const unsigned int DEFAULT_MAX_LOG_ENTRIES = 100000;
}

LegacyFileLogSink::LegacyFileLogSink(const char* logDirectoryName)
:
ILogSink(),
m_fileLogger(
    OS_Utilities::StringToUTF16(logDirectoryName),
    DEFAULT_MAX_LOG_ENTRIES),
m_criticalSection(),
m_logLevel(LOG_LEVEL_INFO)
{
    //do nothing
}

LegacyFileLogSink::LegacyFileLogSink(const wchar_t* logDirectoryName)
:
ILogSink(),
m_fileLogger(
    std::wstring(logDirectoryName),
    DEFAULT_MAX_LOG_ENTRIES),
m_criticalSection(),
m_logLevel(LOG_LEVEL_INFO)
{
    //do nothing
}

LegacyFileLogSink::~LegacyFileLogSink()
{
    //do nothing
}

void LegacyFileLogSink::setLogLevel(const ESeverityLevel logLevel)
{
    m_logLevel = logLevel;
}

void LegacyFileLogSink::setFileLogMaxNumberOfEntries(const unsigned int value)
{
    m_fileLogger.setFileLogMaxNumberOfEntries(value);
}

void LegacyFileLogSink::setFileLogMaxNumberOfCharacters(const unsigned int value)
{
    m_fileLogger.setFileLogMaxNumberOfCharacters(value);
}

void LegacyFileLogSink::handleLog(const ESeverityLevel logLevel, const std::string& timeString, const std::string& text)
{
    std::string logStream;
    logStream.reserve(std::max(128u + text.size(), static_cast<size_t>(1024u))); //Reserve enough space to protect copy on expansion

    logStream += timeString;
    logStream += ": ";

#if defined(_WIN32) && defined(DEBUG)
    //Log thread identifier of the calling thread
    logStream += "(";
    logStream += Utils::int64ToString(::GetCurrentThreadId());
    logStream += ") ";
#endif
    static const char* const str[] =
    {
        "<DEBUG3>",
        "<DEBUG2>",
        "<DEBUG1>",
        "<NORMAL>",
        "<NOTIFICATION>",
        "<WARNING>",
        "<ERROR>",
        "<EXCEPTION>",
        "<FATAL>"
    };

    if (static_cast< std::size_t >(logLevel) < (sizeof(str) / sizeof(*str)))
    {
        logStream += str[logLevel];
    }
    else
    {
        logStream += "<";
        logStream += Utils::intToString(static_cast< int >(logLevel)) ;
        logStream += ">";
    }

    logStream += " ";

    // add the text to the stream,
    logStream += text;

    // keep the time spent in the critical section to a minimum
    Lock lock(m_criticalSection);

    m_fileLogger.log(logStream);
}

void LegacyFileLogSink::handleLog(const ESeverityLevel logLevel, const std::wstring& timeString, const std::wstring& text)
{
    handleLog(logLevel, OS_Utilities::StringToAnsi(timeString), OS_Utilities::StringToAnsi(text));
}

void LegacyFileLogSink::logLogLevel()
{
    // generate a startup log
    std::ostringstream logStream;
    logStream << Version::getApplicationName()
        << " " << Version::getVersionAsString();

#ifdef _DEBUG
    logStream << " DEBUG";
#endif

    logStream << " Starting Up (loggingLevel=" << static_cast<int>(m_logLevel) << ")";
    handleLog(LOG_LEVEL_NOTICE, Utils::getTimeStampStringWithMilliseconds(), logStream.str());
}

void LegacyFileLogSink::deleteOldLogFiles(const time_t maximumLogFileAgeInSeconds)
{
    const std::tstring LOG_DIRECTORY(
        BlueTruth::ApplicationConfiguration::getInstancePtr()->getLogDirectory());
    const std::tstring LOG_DIRECTORY_SEARCH_PATTERN(LOG_DIRECTORY + _T("*.log"));

#ifdef _WIN32
    WIN32_FIND_DATA findFileData;
    HANDLE hFind;

    //Iterate over the entire collection of files in the log directory
    hFind = ::FindFirstFile(LOG_DIRECTORY_SEARCH_PATTERN.c_str(), &findFileData);
    if (hFind != INVALID_HANDLE_VALUE)
    { //something has been found
        do
        {
            if (findFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
            {
                 //this is a directory. Ignore it
            }
            else
            { //This is a file. Get last write to file time and compare it to the current time
                ::time_t currentTimeAsTime_t = ::time(NULL);

                ::ULARGE_INTEGER lastFileWriteTimeTmp;
                lastFileWriteTimeTmp.LowPart = findFileData.ftLastWriteTime.dwLowDateTime;
                lastFileWriteTimeTmp.HighPart = findFileData.ftLastWriteTime.dwHighDateTime;
                time_t lastFileWriteTimeAsTime_t = lastFileWriteTimeTmp.QuadPart / 10000000ULL - 11644473600ULL;

                if (currentTimeAsTime_t > lastFileWriteTimeAsTime_t + maximumLogFileAgeInSeconds)
                {
                    const std::tstring FILE_TO_DELETE(LOG_DIRECTORY + DIRECTORY_SEPARATOR + findFileData.cFileName);
                    int result = ::remove(OS_Utilities::StringToAnsi(FILE_TO_DELETE).c_str());
                    if (result != 0)
                    {
                        ::perror("Error deleting file");
                    }
                    //else do nothing
                }
                //else do nothing
            }
        }
        while (::FindNextFile(hFind, &findFileData) != 0);

        ::FindClose(hFind);
    }
    //else do nothing
#else
    DIR* directoryPtr = opendir(OS_Utilities::StringToAnsi(LOG_DIRECTORY).c_str());
    if (directoryPtr != 0)
    {
        ::time_t currentTimeAsTime_t = ::time(NULL);

        struct dirent* entryPtr = 0;
        while ( (entryPtr = readdir(directoryPtr)) )
        {
            std::string fullFileNameAndPath(OS_Utilities::StringToAnsi(LOG_DIRECTORY));
            fullFileNameAndPath += entryPtr->d_name;

            struct stat fileStatus;
            int statResult = stat(fullFileNameAndPath.c_str(), &fileStatus);

            if (statResult==0)
            {
                if (
                    S_ISREG(fileStatus.st_mode) &&
                    (currentTimeAsTime_t > fileStatus.st_mtime + maximumLogFileAgeInSeconds)
                    )
                {
                    ::remove(fullFileNameAndPath.c_str());
                }
                //else do nothing
            }
            else
            {
                ::perror("stat() error");
            }
        }

        closedir(directoryPtr);
    }
    else
    {
        std::cerr << "Could not open directory: " << OS_Utilities::StringToAnsi(LOG_DIRECTORY).c_str() << std::endl;
    }
#endif
}
