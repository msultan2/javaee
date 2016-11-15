#include "stdafx.h"

#ifdef _WIN32
#pragma warning( push )
#pragma warning( disable : 4512 ) // assignment operator could not be generated
#pragma warning( disable : 4714 ) // ... marked as __forceinline not inlined
#endif

#include "boostlogger.h"

#include "logger.h"

// system includes
#include <fstream>
#include <iostream>
#include <sstream>

#ifdef _WIN32
#else
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#endif

#include <boost/log/core.hpp>
#include <boost/log/trivial.hpp>

#include <boost/log/common.hpp>
#include <boost/log/expressions.hpp>
#include <boost/log/sources/severity_logger.hpp>

#include <boost/log/support/date_time.hpp>

#include <boost/log/utility/exception_handler.hpp>
#include <boost/log/utility/setup/common_attributes.hpp>
#include <boost/log/utility/setup/console.hpp>
#include <boost/log/utility/setup/file.hpp>


namespace attrs = boost::log::attributes;
namespace expr = boost::log::expressions;
namespace fs = boost::filesystem;
namespace logging = boost::log;
namespace keywords = boost::log::keywords;
namespace sinks = boost::log::sinks;
namespace src = boost::log::sources;


BOOST_LOG_ATTRIBUTE_KEYWORD(severity, "Severity", ESeverityLevel)


// The formatting logic for the severity level
template<typename TraitsT>
inline std::basic_ostream< char, TraitsT >& operator<< (
    std::basic_ostream< char, TraitsT >& strm, ESeverityLevel level)
{
    static const char* const str[] =
    {
        "DEBUG3",
        "DEBUG2",
        "DEBUG1",
        "NORMAL",
        "NOTIFICATION",
        "WARNING",
        "ERROR",
        "EXCEPTION",
        "FATAL"
    };

    if (static_cast< std::size_t >(level) < (sizeof(str) / sizeof(*str)))
    {
        strm << str[level];
    }
    else
    {
        strm << static_cast< int >(level);
    }

    return strm;
}

template<typename TraitsT>
inline std::basic_ostream< wchar_t, TraitsT >& operator<< (
    std::basic_ostream< wchar_t, TraitsT >& strm, ESeverityLevel level)
{
    static const wchar_t* const str[] =
    {
        L"DEBUG3",
        L"DEBUG2",
        L"DEBUG1",
        L"NORMAL",
        L"NOTIFICATION",
        L"WARNING",
        L"ERROR",
        L"EXCEPTION",
        L"FATAL"
    };

    if (static_cast< std::size_t >(level) < (sizeof(str) / sizeof(*str)))
    {
        strm << str[level];
    }
    else
    {
        strm << static_cast< int >(level);
    }

    return strm;
}

namespace
{
struct myExceptionHandler
{
    typedef void result_type;

    void operator() (std::runtime_error const& e) const
    {
        std::cerr << "std::runtime_error: " << e.what() << std::endl;
    }

    void operator() (std::logic_error const& e) const
    {
        std::cerr << "std::logic_error: " << e.what() << std::endl;
        throw;
    }
};
}


bool BoostLogger::checkDirectoryAccess(const boost::filesystem::path& log_directory)
{
    // ensure directory exists or can be created
    boost::system::error_code errorCode;
    fs::create_directory(log_directory, errorCode);
    if (errorCode)
    {
        std::cerr << "ERROR: Log directory \"" << log_directory.c_str() << "\" cannot be created" << std::endl;
        return false;
    }
    //else do nothing

    boost::filesystem::path test_file_path = log_directory / "testdiraccess";
    // ensure we can create file there
    std::ofstream temp_file(test_file_path.string().c_str());
    if (!temp_file.is_open())
    {
        std::cerr << "ERROR: Log directory \"" << log_directory.c_str() << "\" is read only" << std::endl;
        return false;
    }
    //else do nothing

    temp_file.close();

    // ensure we can remove files
    remove(test_file_path, errorCode);
    if (errorCode)
    {
        std::cerr << "ERROR:  Test file cannot be removed. Log directory \"" << log_directory.c_str()
            << "\" should provide full access for correct work" << std::endl;
        return false;
    }
    //else do nothing

    return true;
}


// Attribute value tag type
struct severity_tag;


BoostLogger::BoostLogger(const char* logDirectoryName)
:
m_pConsoleSink(),
m_pFileSink(),
m_consoleLogLevel(LOG_LEVEL_INFO),
m_fileLogLevel(LOG_LEVEL_INFO),
m_numberOfLoggedEntries(0),
m_maxNumberOfLoggedEntries(10000L)
{
    addConsoleLog();
    addFileLog(logDirectoryName);

    setExceptionHandler();
    add_common_attributes();

    setConsoleLogLevel(m_consoleLogLevel);
    setFileLogLevel(m_fileLogLevel);
}

BoostLogger::BoostLogger(const wchar_t* logDirectoryName)
:
m_pConsoleSink(),
m_pFileSink(),
m_consoleLogLevel(LOG_LEVEL_INFO),
m_fileLogLevel(LOG_LEVEL_INFO),
m_numberOfLoggedEntries(0),
m_maxNumberOfLoggedEntries(10000L)
{
    addConsoleLog();
    addFileLog(logDirectoryName);

    setExceptionHandler();
    add_common_attributes();

    setConsoleLogLevel(m_consoleLogLevel);
    setFileLogLevel(m_fileLogLevel);
}

void BoostLogger::addConsoleLog()
{
    // The first thing we have to do to get using the library is
    // to set up the logging sinks - i.e. where the logs will be written to.
    m_pConsoleSink = logging::add_console_log(
        std::clog,
        keywords::format =
        (
            expr::stream
                << expr::format_date_time< boost::posix_time::ptime >("TimeStamp", "%y/%m/%d %H:%M:%S.%f")
                << ": <" << expr::attr< ESeverityLevel, severity_tag >("Severity") << "> "
                << expr::smessage
        )
    );
}

bool BoostLogger::addFileLog(const char* logDirectoryName)
{
    bool result = false;

    if (checkDirectoryAccess(logDirectoryName))
    {
        std::string logFileNamePattern;
        logFileNamePattern += logDirectoryName;
        logFileNamePattern += "/LOGFILE_"; //file name
        logFileNamePattern += "%Y%m%d_%H%M%S"; //date extension
        logFileNamePattern += "_%N"; //number extension when size grows too big
        logFileNamePattern += ".LOG"; //file extension
        m_pFileSink = logging::add_file_log(
            //keywords::target = logDirectoryName, // target directory to store rotated files
            keywords::file_name = logFileNamePattern.c_str(), // file name pattern
            keywords::auto_flush = true,
            keywords::rotation_size = 10 * 1024 * 1024,       // rotate files every 10 MiB...
                                                              // ...or at midnight
            keywords::time_based_rotation = sinks::file::rotation_at_time_point(0, 0, 0),
            keywords::format =
            (
                expr::stream
                    << expr::format_date_time< boost::posix_time::ptime >("TimeStamp", "%y/%m/%d %H:%M:%S.%f")
                    << ": <" << expr::attr< ESeverityLevel, severity_tag >("Severity") << "> "
                    << expr::smessage
            )
        );

        // Enable auto-flushing after each log record written
        m_pFileSink->locked_backend()->auto_flush(true);

        result = true;
    }
    else
    {
        std::cerr << "ERROR: Directory \"" << logDirectoryName <<
            "\" does not exist or cannot be accessed\n";
    }

    return result;
}

bool BoostLogger::addFileLog(const wchar_t* logDirectoryName)
{
    bool result = false;

    if (checkDirectoryAccess(logDirectoryName))
    {
        std::wstring logFileNamePattern;
        logFileNamePattern += logDirectoryName;
        logFileNamePattern += L"/LOGFILE_"; //file name
        logFileNamePattern += L"%Y%m%d_%H%M%S"; //date extension
        logFileNamePattern += L"_%N"; //number extension when size grows too big
        logFileNamePattern += L".LOG"; //file extension
        m_pFileSink = logging::add_file_log(
            //keywords::target = logDirectoryName, // target directory to store rotated files
            keywords::file_name = logFileNamePattern.c_str(), // file name pattern
            keywords::auto_flush = true,
            keywords::rotation_size = 10 * 1024 * 1024,       // rotate files every 10 MiB...
                                                              // ...or at midnight
            keywords::time_based_rotation = sinks::file::rotation_at_time_point(0, 0, 0),
            keywords::format =
            (
                expr::stream
                    << expr::format_date_time< boost::posix_time::ptime >("TimeStamp", "%y/%m/%d %H:%M:%S.%f")
                    << ": <" << expr::attr< ESeverityLevel, severity_tag >("Severity") << "> "
                    << expr::smessage
            )
        );

        // Enable auto-flushing after each log record written
        m_pFileSink->locked_backend()->auto_flush(true);

        result = true;
    }
    else
    {
        std::wcerr << L"ERROR: Directory \"" << logDirectoryName <<
            L"\" does not exist or cannot be accessed\n";
    }

    return result;
}

void BoostLogger::setExceptionHandler()
{
    // Setup a global exception handler that will call my_handler::operator()
    // for the specified exception types
    logging::core::get()->set_exception_handler(
        logging::make_exception_handler<std::runtime_error, std::logic_error>(
            myExceptionHandler()));
}

void BoostLogger::add_common_attributes()
{
    // Also let's add some commonly used attributes, like timestamp and record counter.
    //(attributes "LineID", "TimeStamp", "ProcessID" and "ThreadID" will be registered globally)
    logging::register_simple_formatter_factory< ESeverityLevel, char >("Severity");
    logging::add_common_attributes();
}

BoostLogger::~BoostLogger()
{
    //do nothing
}

void BoostLogger::log(const ESeverityLevel logLevel, const std::string& text)
{
    logging::sources::severity_logger<ESeverityLevel> serverityLevel;
    BOOST_LOG_SEV(serverityLevel, logLevel) << text;

    if (++m_numberOfLoggedEntries >= m_maxNumberOfLoggedEntries)
    {
        m_numberOfLoggedEntries = 0;
        m_pFileSink->locked_backend()->rotate_file();
    }
    //else do nothing
}

void BoostLogger::log(const ESeverityLevel logLevel, const std::wstring& text)
{
    logging::sources::wseverity_logger<ESeverityLevel> wserverityLevel;
    BOOST_LOG_SEV(wserverityLevel, logLevel) << text;

    if (++m_numberOfLoggedEntries >= m_maxNumberOfLoggedEntries)
    {
        m_numberOfLoggedEntries = 0;
        m_pFileSink->locked_backend()->rotate_file();
    }
    //else do nothing
}

void BoostLogger::setConsoleLogLevel(const ESeverityLevel logLevel)
{
    m_consoleLogLevel = logLevel;
    if (m_pConsoleSink)
    {
        m_pConsoleSink->set_filter(severity >= logLevel);
    }
    //else do nothing
}

void BoostLogger::setFileLogLevel(const ESeverityLevel logLevel)
{
    m_fileLogLevel = logLevel;

    if (m_pFileSink)
    {
        m_pFileSink->set_filter(severity >= logLevel);
    }
    //else do nothing
}

bool BoostLogger::isLogLevelAboveThreshold(const ESeverityLevel logLevel)
{
    bool result = (logLevel >= m_fileLogLevel) || (logLevel >= m_consoleLogLevel);

    return result;
}

void BoostLogger::logLogLevel()
{
    // generate a startup log
    std::ostringstream logStream;

#ifdef _DEBUG
    logStream << "DEBUG ";
#endif

    logStream << "Starting Up (loggingLevel="
        << static_cast<int>(m_consoleLogLevel) << "/"
        << static_cast<int>(m_fileLogLevel) << ")";
    log(LOG_LEVEL_NOTICE, logStream.str());
}

void BoostLogger::setFileLogMaxNumberOfEntries(const long maxNumberOfEntries)
{
    m_maxNumberOfLoggedEntries = maxNumberOfEntries;
}

void BoostLogger::setFileLogMaxNumberOfCharacters(const long maxNumberOfCharacters)
{
    m_pFileSink->locked_backend()->flush();
    m_pFileSink->locked_backend()->set_rotation_size(maxNumberOfCharacters);
}

bool BoostLogger::isValid()
{
    return (m_pConsoleSink != 0) && (m_pFileSink != 0);
}

#ifdef _WIN32
#pragma warning( pop )
#endif
