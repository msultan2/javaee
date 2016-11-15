/*
    System: BlueTruth Outstation
    Module: LegacyFileLogSink
    Author: Radoslaw Golebiewski
    Compiler: Visual Studio 2008 Express Edition
    Description: This class that implements loggin. Any logs generated
        with the Logger::log() functions will be routed to this class.
        This is the legacy log handler used in NECT and IPT projects
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/


#ifndef _LEGACY_LOG_SINK_H
#define _LEGACY_LOG_SINK_H

#include "ilogsink.h"
#include "logger.h"
#include "mutex.h"
#include "managedfilelogger.h"
#include "applicationconfiguration.h"


class LegacyFileLogSink : public ILogSink
{
public:
    //! constructor
    explicit LegacyFileLogSink(const char* logDirectoryName);
    explicit LegacyFileLogSink(const wchar_t* logDirectoryName);

    //! destructor
    virtual ~LegacyFileLogSink();

    virtual void handleLog(const ESeverityLevel logLevel, const std::string& timeString, const std::string& text);
    virtual void handleLog(const ESeverityLevel logLevel, const std::wstring& timeString, const std::wstring& text);

    virtual void setLogLevel(const ESeverityLevel logLevel);

    //! sets the current file logging level
    virtual void setFileLogMaxNumberOfEntries(const unsigned int maxNumberOfEntries);
    virtual void setFileLogMaxNumberOfCharacters(const unsigned int maxNumberOfCharacters);

    void deleteOldLogFiles(const time_t maximumLogFileAgeInSeconds);

private:
    //! constructor
    LegacyFileLogSink();
    //! copy constructor, not implemented
    LegacyFileLogSink(const LegacyFileLogSink& rhs);
    //! copy assignment operator, not implemented
    LegacyFileLogSink& operator=(const LegacyFileLogSink& rhs);

    //! log logging level to the log file
    void logLogLevel();

    //private members:
    ManagedFileLogger m_fileLogger;
    Mutex m_criticalSection;

    ESeverityLevel m_logLevel;
};

#endif // _LEGACY_LOG_SINK_H
