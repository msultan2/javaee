#include "stdafx.h"
#include "viewlogsink.h"

#include "utils.h"
#include "lock.h"
#include "os_utilities.h"
#include "version.h"
#include "view.h"

// system includes
#include <stdio.h>
#include <iostream>
#include <sstream>


ViewLogSink::ViewLogSink()
:
ILogSink(),
m_logLevel(LOG_LEVEL_INFO)
{
    //do nothing
}

ViewLogSink::~ViewLogSink()
{
    //do nothing
}

void ViewLogSink::setLogLevel(const ESeverityLevel logLevel)
{
    m_logLevel = logLevel;
}

void ViewLogSink::setFileLogMaxNumberOfEntries(const unsigned int )
{
    //do nothing
}

void ViewLogSink::setFileLogMaxNumberOfCharacters(const unsigned int )
{
    //do nothing
}

void ViewLogSink::handleLog(const ESeverityLevel logLevel, const std::string& timeString, const std::string& text)
{
    boost::shared_ptr<View::LogRecord> pLogRecord(new View::LogRecord());

    pLogRecord->time = OS_Utilities::StringToTString(timeString);
    pLogRecord->logLevel = logLevel;
    pLogRecord->text = OS_Utilities::StringToTString(text);

    View::View::log(pLogRecord);
}

void ViewLogSink::handleLog(const ESeverityLevel logLevel, const std::wstring& timeString, const std::wstring& text)
{
    boost::shared_ptr<View::LogRecord> pLogRecord(new View::LogRecord());

    pLogRecord->time = OS_Utilities::StringToTString(timeString);
    pLogRecord->logLevel = logLevel;
    pLogRecord->text = OS_Utilities::StringToTString(text);

    View::View::log(pLogRecord);
}

void ViewLogSink::logLogLevel()
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
