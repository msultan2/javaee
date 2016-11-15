#include "stdafx.h"

#ifdef _WIN32
#pragma warning( push )
#pragma warning( disable : 4512 ) // assignment operator could not be generated
#pragma warning( disable : 4714 ) // ... marked as __forceinline not inlined
#endif

#include "boostlogsink.h"

#include "utils.h"
#include "lock.h"
#include "logger.h"
#include "applicationconfiguration.h"
#include "os_utilities.h"
#include "version.h"

// system includes
#include <stdio.h>
#include <iostream>
#include <sstream>

#ifdef _WIN32
#else
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#endif

#include <boost/filesystem.hpp>
#include <boost/log/core.hpp>
#include <boost/log/trivial.hpp>

#include <boost/log/common.hpp>
#include <boost/log/formatters.hpp>
#include <boost/log/filters.hpp>

#include <boost/log/utility/init/from_stream.hpp>
#include <boost/log/utility/init/to_file.hpp>
#include <boost/log/utility/init/to_console.hpp>
#include <boost/log/utility/init/common_attributes.hpp>

#include <boost/log/attributes/timer.hpp>


namespace attrs = boost::log::attributes;
namespace flt = boost::log::filters;
namespace fmt = boost::log::formatters;
namespace logging = boost::log;
namespace keywords = boost::log::keywords;
namespace sinks = boost::log::sinks;
namespace src = boost::log::sources;


// Here we define our application severity levels.
enum severity_level
{
    normal,
    notification,
    warning,
    error,
    critical
};

// The formatting logic for the severity level
template< typename char, typename TraitsT >
inline std::basic_ostream< char, TraitsT >& operator<< (
    std::basic_ostream< char, TraitsT >& strm, severity_level lvl)
{
    static const char* const str[] =
    {
        "NORMAL",
        "NOTIFICATION",
        "WARNING",
        "ERROR",
        "CRITICAL"
    };

    if (static_cast< std::size_t >(lvl) < (sizeof(str) / sizeof(*str)))
    {
        strm << str[lvl];
    }
    else
    {
        strm << static_cast< int >(lvl);
    }

    return strm;
}

template< typename wchar_t, typename TraitsT >
inline std::basic_ostream< wchar_t, TraitsT >& operator<< (
    std::basic_ostream< wchar_t, TraitsT >& strm, severity_level lvl)
{
    static const wchar_t* const str[] =
    {
        L"NORMAL",
        L"NOTIFICATION",
        L"WARNING",
        L"ERROR",
        L"CRITICAL"
    };

    if (static_cast< std::size_t >(lvl) < (sizeof(str) / sizeof(*str)))
    {
        strm << str[lvl];
    }
    else
    {
        strm << static_cast< int >(lvl);
    }

    return strm;
}


boost::log::sources::logger BoostLogSink::m_boostLogger;


BoostLogSink::BoostLogSink()
:
ILogSink(),
m_logLevel(LOG_LEVEL_INFO)
{
    // The first thing we have to do to get using the library is
    // to set up the logging sinks - i.e. where the logs will be written to.
    logging::init_log_to_console(
        std::clog,
        //keywords::format = "%TimeStamp%: %_%");
        keywords::format = fmt::format("%1% %2%: %3%")
            % fmt::date_time("TimeStamp", keywords::format = "%y/%m/%d %H:%M:%S.%f", std::nothrow)
            % fmt::attr< severity_level >("Severity", std::nothrow)
            % fmt::message()
    );

    std::tstring logFileNamePattern;
    logFileNamePattern += EAV::ApplicationConfiguration::getLogDirectory();
    logFileNamePattern += _T("LOGFILE_"); //file name
    logFileNamePattern += _T("%Y%m%d_%H%M%S"); //date extension
    logFileNamePattern += _T("_%N"); //number extension when size grows too big
    logFileNamePattern += _T(".LOG"); //file extension
    logging::init_log_to_file(
        keywords::file_name = logFileNamePattern.c_str(), // file name pattern
        keywords::rotation_size = 10 * 1024 * 1024,       // rotate files every 10 MiB...
                                                          // ...or at midnight
        keywords::time_based_rotation = sinks::file::rotation_at_time_point(0, 0, 0),
        //keywords::filter = flt::attr< severity_level >("Severity", std::nothrow) >= normal,
        keywords::format = fmt::format("%1% %2%: %3%")
            % fmt::date_time("TimeStamp", keywords::format = "%y/%m/%d %H:%M:%S.%f", std::nothrow)
            % fmt::attr< severity_level >("Severity", std::nothrow)
            % fmt::message()
    );

    // Also let's add some commonly used attributes, like timestamp and record counter.
    //(attributes "LineID", "TimeStamp", "ProcessID" and "ThreadID" will be registered globally)
    logging::add_common_attributes();

    logLogLevel();
}

BoostLogSink::~BoostLogSink()
{
    //do nothing
}

void BoostLogSink::handleLog(const LoggingLevel logLevel, const std::string& text)
{
    //Do not use locks when using boost log library (one of the main library goals)
    //Lock lock(m_mutex);

    src::severity_logger< severity_level > slg;

    switch (static_cast<int>(logLevel))
    {
        case LOG_LEVEL_DEBUG3:
        case LOG_LEVEL_DEBUG2:
        case LOG_LEVEL_DEBUG1:
        {
            BOOST_LOG_SEV(slg, normal) << text;
            break;
        }
        case LOG_LEVEL_INFO:
        case LOG_LEVEL_NOTICE:
        {
            BOOST_LOG_SEV(slg, notification) << text;
            break;
        }
        case LOG_LEVEL_WARNING:
        {
            BOOST_LOG_SEV(slg, warning) << text;
            break;
        }
        case LOG_LEVEL_ERROR:
        {
            BOOST_LOG_SEV(slg, error) << text;
            break;
        }
        case LOG_LEVEL_EXCEPTION:
        case LOG_LEVEL_FATAL:
        default:
        {
            BOOST_LOG_SEV(slg, critical) << text;
            break;
        }
    }
}

void BoostLogSink::handleLog(const LoggingLevel logLevel, const std::wstring& text)
{
    //Do not use locks when using boost log library (one of the main library goals)
    //Lock lock(m_mutex);

    src::wseverity_logger< severity_level > slg;

    switch (static_cast<int>(logLevel))
    {
        case LOG_LEVEL_DEBUG3:
        case LOG_LEVEL_DEBUG2:
        case LOG_LEVEL_DEBUG1:
        {
            BOOST_LOG_SEV(slg, normal) << text;
            break;
        }
        case LOG_LEVEL_INFO:
        case LOG_LEVEL_NOTICE:
        {
            BOOST_LOG_SEV(slg, notification) << text;
            break;
        }
        case LOG_LEVEL_WARNING:
        {
            BOOST_LOG_SEV(slg, warning) << text;
            break;
        }
        case LOG_LEVEL_ERROR:
        {
            BOOST_LOG_SEV(slg, error) << text;
            break;
        }
        case LOG_LEVEL_EXCEPTION:
        case LOG_LEVEL_FATAL:
        default:
        {
            BOOST_LOG_SEV(slg, critical) << text;
            break;
        }
    }
}

void BoostLogSink::setLogLevel(const LoggingLevel logLevel)
{
    m_logLevel = logLevel;
}

void BoostLogSink::deleteOldLogFiles(const time_t maximumLogFileAgeInSeconds)
{
    const std::tstring LOG_DIRECTORY(
        EAV::ApplicationConfiguration::getInstancePtr()->getLogDirectory());
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
    }
    else
    {
        std::cerr << "Could not open directory: " << OS_Utilities::StringToAnsi(LOG_DIRECTORY).c_str() << std::endl;
    }
#endif
}

void BoostLogSink::logLogLevel()
{
    // generate a startup log
    std::ostringstream logStream;
    logStream << Version::getApplicationName()
        << " " << Version::getVersionAsString();

#ifdef _DEBUG
    logStream << " DEBUG";
#endif

    logStream << " Starting Up (loggingLevel=" << static_cast<int>(m_logLevel) << ")";
    handleLog(LOG_LEVEL_NOTICE, logStream.str());
}

#ifdef _WIN32
#pragma warning( pop )
#endif
