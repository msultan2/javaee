/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef _BOOST_LOGGER_H
#define _BOOST_LOGGER_H

#include "logger.h"

#define BOOST_LOG_DYN_LINK 1

#include <boost/filesystem.hpp>
#include <boost/log/common.hpp>
#include <boost/log/utility/setup/console.hpp>
#include <boost/log/utility/setup/file.hpp>


/**
 * @brief A class that implements logging functionality based on boost::logger library.
 *
 * Any logs generated with the Logger::log() functions will be routed to this class.
 */
class BoostLogger
{
public:
    /**
     * Main constructor
     * @param logDirectoryName the directory where the log files are to be written
     */
    BoostLogger(const char* logDirectoryName);

    /**
     * Main constructor (wchar_t version)
     * @param logDirectoryName the directory where the log files are to be written
     */
    BoostLogger(const wchar_t* logDirectoryName);

    //! destructor
    virtual ~BoostLogger();

    /**
     * @brief Main ASCII string log function. Log to file or console depending on severity level.
     * @param text text to be logged
     */
    void log(const ESeverityLevel logLevel, const std::string& text);

    /**
     * @brief Main wide string (UTF) log function. Log to file or console depending on severity level.
     * @param text text to be logged
     */
    void log(const ESeverityLevel logLevel, const std::wstring& text);

    /**
     * @brief Sets the current console logging level
     * @param logLevel new severity level
     */
    void setConsoleLogLevel(const ESeverityLevel logLevel);

    /**
     * @brief Sets the current file logging level
     * @param logLevel new severity level
     */
    void setFileLogLevel(const ESeverityLevel logLevel);

    /**
     * @brief Check if a severity level is above the logging threshold.
     */
    bool isLogLevelAboveThreshold(const ESeverityLevel logLevel);

    /**
     * @brief Log log level to the console and the log file
     */
    void logLogLevel();

    void setFileLogMaxNumberOfEntries(const long maxNumberOfEntries);

    void setFileLogMaxNumberOfCharacters(const long maxNumberOfCharacters);

    bool isValid();

    /**
     * @brief Check if a directory can be used for logging (verify if it exists and rw access)
     * @param log_directory directory to be verified
     */
    static bool checkDirectoryAccess(const boost::filesystem::path& log_directory);

private:
    //! default constructor, not implemented
    BoostLogger();
    //! copy constructor, not implemented
    BoostLogger(const BoostLogger& rhs);
    //! copy assignment operator, not implemented
    BoostLogger& operator=(const BoostLogger& rhs);


    void addConsoleLog();
    bool addFileLog(const char* logDirectoryName);
    bool addFileLog(const wchar_t* logDirectoryName);
    void setExceptionHandler();
    void add_common_attributes();

    //Private members:
    boost::shared_ptr< boost::log::sinks::synchronous_sink< boost::log::sinks::text_ostream_backend > > m_pConsoleSink;
    boost::shared_ptr< boost::log::sinks::synchronous_sink< boost::log::sinks::text_file_backend > > m_pFileSink;

    ESeverityLevel m_consoleLogLevel;
    ESeverityLevel m_fileLogLevel;

    long m_numberOfLoggedEntries; ///counter of logged lines
    long m_maxNumberOfLoggedEntries; ///after this value is exceeded rotation (new file) should occur
};

#endif // _BOOST_LOGGER_H
