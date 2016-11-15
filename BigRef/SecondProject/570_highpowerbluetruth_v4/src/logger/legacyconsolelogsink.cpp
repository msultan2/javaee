#include "stdafx.h"
#include "legacyconsolelogsink.h"

#include "utils.h"
#include "lock.h"
#include "os_utilities.h"
#include "version.h"

// system includes
#include <stdio.h>
#include <iostream>
#include <sstream>


LegacyConsoleLogSink::LegacyConsoleLogSink()
:
ILogSink(),
m_mutex(),
m_logLevel(LOG_LEVEL_INFO)
{
    //do nothing
}

LegacyConsoleLogSink::~LegacyConsoleLogSink()
{
    //do nothing
}

void LegacyConsoleLogSink::setLogLevel(const ESeverityLevel logLevel)
{
    m_logLevel = logLevel;
}

void LegacyConsoleLogSink::setFileLogMaxNumberOfEntries(const unsigned int )
{
    //do nothing
}

void LegacyConsoleLogSink::setFileLogMaxNumberOfCharacters(const unsigned int )
{
    //do nothing
}

void LegacyConsoleLogSink::handleLog(const ESeverityLevel logLevel, const std::string& timeString, const std::string& text)
{
    std::string logStream;
    logStream.reserve(std::max(128u + text.size(), static_cast<size_t>(1024u))); //Reserve enough space to protect copy on expansion

    logStream += timeString;
    logStream += ": ";

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

    // enter critical section to ensure that multiple threads do not cause the console to muddled
    Lock lock(m_mutex);

    std::cout << logStream << std::endl;
}

void LegacyConsoleLogSink::handleLog(const ESeverityLevel logLevel, const std::wstring& timeString, const std::wstring& text)
{
    handleLog(logLevel, OS_Utilities::StringToAnsi(timeString), OS_Utilities::StringToAnsi(text));
}

void LegacyConsoleLogSink::logLogLevel()
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
