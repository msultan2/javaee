/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Modification History:

    DateWho     SCJS No     Remarks
    05/06/2009  EWT     001 V1.00 First Issue

*/


#ifndef _LOG_HANDLER_H_
#define _LOG_HANDLER_H_

#include "logger.h"
#include "unicode_types.h"

#include <list>

class ILogSink;

class LogHandler
{
public:
    //! default constructor
    LogHandler();

    //! destructor
    virtual ~LogHandler();

    bool isVoid() const;

    void addConsoleLogSink(ILogSink* pLogSink);
    void delConsoleLogSink(ILogSink* pLogSink);
    void addFileLogSink(ILogSink* pLogSink);
    void delFileLogSink(ILogSink* pLogSink);
    void clearLogSinks();


    //! sets the current console logging level
    void setConsoleLogLevel(const ESeverityLevel logLevel);
    //! @return the current system file log level
    ESeverityLevel getConsoleLogLevel(){ return m_consoleLogLevel; }


    //! sets the current file logging level
    void setFileLogLevel(const ESeverityLevel logLevel);
    //! @return the current system file log level
    ESeverityLevel getFileLogLevel() { return m_fileLogLevel; }


    void log(const ESeverityLevel logLevel, const std::string& text);
    void log(const ESeverityLevel logLevel, const std::wstring& text);

    /**
     * @brief Log log level to the console and the log file
     */
    void logLogLevel();

    bool isLogLevelAboveThreshold(const ESeverityLevel logLevel);

    /**
     * @brief Sets the maximum number of entries in the file.
     *
     * After this number is exceeded a new log file should be opened.
     * @param maxNumberOfEntries the value of maximum number of entries
     */
    void setFileLogMaxNumberOfEntries(const long maxNumberOfEntries);

    /**
     * @brief Sets the maximum number of characters in the file.
     *
     * After this number is exceeded a new log file should be opened.
     * @param maxNumberOfEntries the value of maximum number of characters
     */
    void setFileLogMaxNumberOfCharacters(const long maxNumberOfCharacters);

private:
    //! copy constructor
    LogHandler(const LogHandler& rhs);
    //! assignment operator
    LogHandler& operator=(const LogHandler& rhs);

    ESeverityLevel m_consoleLogLevel;
    ESeverityLevel m_fileLogLevel;

    std::list< ILogSink* > m_consoleLogSinkCollection;
    std::list< ILogSink* > m_fileLogSinkCollection;
};

#endif // _LOG_HANDLER_H_
