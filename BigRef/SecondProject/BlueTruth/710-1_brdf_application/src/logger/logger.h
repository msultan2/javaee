/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef _LOGGER_H_
#define _LOGGER_H_

#include <boost/shared_ptr.hpp>

class BoostLogger;


enum ESeverityLevel
{
    LOG_LEVEL_DEBUG3        = 0,
    LOG_LEVEL_DEBUG2        = 1,
    LOG_LEVEL_DEBUG1        = 2,
    LOG_LEVEL_INFO          = 3,
    LOG_LEVEL_NOTICE        = 4,
    LOG_LEVEL_WARNING       = 5,
    LOG_LEVEL_ERROR         = 6,
    LOG_LEVEL_EXCEPTION     = 7,
    LOG_LEVEL_FATAL         = 8,
    MAX_LOG_LEVEL_PLUS_ONE
};


/**
 * @brief A facade to to log all application events.
 *
 * This class is not designed to be instantiated, it is designed to act
 * a little like a singleton. It exposes only static logging functions, which
 * can echo all logs to the screen. If configured to do so, it will also use
 * a callback-like mechanism to send those logs to another object, which can
 * then log the data to a file, or database, or whatever
*/
class Logger
{
public:

    /**
     * @brief Main ASCII log function. Log to file or console depending on severity level.
     *
     * All descriptions will be separated by a comma and a single space character.
     * @param description1 first description
     * @param description2 second description
     * @param description3 third description
     * @param description4 fourth description
     */
    static void log(
        const ESeverityLevel logLevel,
        const char* description1,
        const char* description2 = 0,
        const char* description3 = 0,
        const char* description4 = 0);

    /**
     * @brief Main wide string (UTF) log function. Log to file or console depending on severity level.
     *
     * All descriptions will be separated by a comma and a single space character.
     * @param description1 first description
     * @param description2 second description
     * @param description3 third description
     * @param description4 fourth description
     */
    static void log(
        const ESeverityLevel logLevel,
        const wchar_t* description1,
        const wchar_t* description2 = 0,
        const wchar_t* description3 = 0,
        const wchar_t* description4 = 0);


    /**
     * @brief Check if a severity level is above the logging threshold.
     *
     * This function can be used for lasy logging, i.e. first verifying if the
     * log to be created can be output (is above threshold) and if so create it.
     * @param logLevel severity log level to be compared to
     * @return true if above, false otherwise
     */
    static bool isLogLevelAboveThreshold(const ESeverityLevel logLevel);

    /**
     * @brief Sets the current console logging level
     * @param logLevel new severity level
     */
    static void setConsoleLogLevel(const ESeverityLevel logLevel);

    /**
     * @brief Sets the current file logging level
     * @param logLevel new severity level
     */
    static void setFileLogLevel(const ESeverityLevel logLevel);

    /**
     * @brief Initialise logger (ASCII version). The log files will be output to logDirectoryName.
     * @param logDirectoryName the directory where the log files are to be written
     */
    static void initialise(const char* logDirectoryName);

    /**
     * @brief Initialise logger (wchar_t version). The log files will be output to logDirectoryName.
     * @param logDirectoryName the directory where the log files are to be written
     */
    static void initialise(const wchar_t* logDirectoryName);

    /**
     * @brief Close all log files and stop logging.
     */
    static void destruct();

    /**
     * @brief Log log level to the console and the log file
     */
    static void logLogLevel();


    static void setFileLogMaxNumberOfEntries(const long maxNumberOfEntries);

    static void setFileLogMaxNumberOfCharacters(const long maxNumberOfCharacters);

    static bool isValid();

private:
    //! default constructor, not implemented
    Logger();
    //! copy constructor, not implemented
    Logger(const Logger& rhs);
    //! copy assignment operator, not implemented
    Logger& operator=(const Logger& rhs);
    //! destructor, not implemented
    virtual ~Logger();

    static boost::shared_ptr< ::BoostLogger > m_pLogger;
};

#endif // _LOGGER_H_
