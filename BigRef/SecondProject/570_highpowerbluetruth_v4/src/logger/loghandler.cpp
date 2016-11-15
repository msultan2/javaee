#include "stdafx.h"
#include "loghandler.h"

#include "ilogsink.h"
#include "logger.h"
#include "utils.h"


LogHandler::LogHandler()
:
m_consoleLogLevel(LOG_LEVEL_INFO),
m_fileLogLevel(LOG_LEVEL_INFO),
m_consoleLogSinkCollection(),
m_fileLogSinkCollection()
{
    //do nothing
}

LogHandler::~LogHandler()
{
    //do nothing
}

bool LogHandler::isVoid() const
{
    if (!m_consoleLogSinkCollection.empty())
        return false;

    if (!m_fileLogSinkCollection.empty())
        return false;

    return true;
}

void LogHandler::addConsoleLogSink(ILogSink* pLogSink)
{
    if (pLogSink != 0)
    {
        m_consoleLogSinkCollection.push_back(pLogSink);
        pLogSink->setLogLevel(m_consoleLogLevel);
    }
    //else do nothing
}

void LogHandler::delConsoleLogSink(ILogSink* pLogSink)
{
    if (pLogSink != 0)
    {
        for (
            std::list<ILogSink* >::iterator iter(m_consoleLogSinkCollection.begin());
            iter != m_consoleLogSinkCollection.end();
            ++iter)
        {
            if ((*iter) == pLogSink)
            {
                m_consoleLogSinkCollection.erase(iter);
                break;
            }
            //else do nothing
        }
    }
    //else do nothing
}

void LogHandler::addFileLogSink(ILogSink* pLogSink)
{
    if (pLogSink != 0)
    {
        m_fileLogSinkCollection.push_back(pLogSink);
        pLogSink->setLogLevel(m_fileLogLevel);
    }
    //else do nothing
}

void LogHandler::delFileLogSink(ILogSink* pLogSink)
{
    if (pLogSink != 0)
    {
        for (
            std::list<ILogSink* >::iterator iter(m_fileLogSinkCollection.begin());
            iter != m_fileLogSinkCollection.end();
            ++iter)
        {
            if ((*iter) == pLogSink)
            {
                m_fileLogSinkCollection.erase(iter);
                break;
            }
            //else do nothing
        }
    }
    //else do nothing
}

void LogHandler::clearLogSinks()
{
    m_consoleLogSinkCollection.clear();
    m_fileLogSinkCollection.clear();
}

void LogHandler::log(
        const ESeverityLevel logLevel,
        const std::string& text)
{
    std::string timeString(Utils::getTimeStampStringWithMicroseconds());

    if (logLevel >= m_consoleLogLevel)
    {
        // this log's priority is at least that of the current log level, so allow it to be handled
        for (
            std::list<ILogSink* >::iterator iter(m_consoleLogSinkCollection.begin());
            iter != m_consoleLogSinkCollection.end();
            ++iter)
        {
            size_t pos = text.find_first_of("\x0d\x0a");
            if (pos == std::string::npos)
            { //the log fits into one line
                (*iter)->handleLog(logLevel, timeString, text);
            }
            else
            { //the log spans upon multiple lines - print only the first one
                if (pos > 0)
                {
                    std::string newText(text.substr(0, pos));
                    newText += " (...)";
                    (*iter)->handleLog(logLevel, timeString, newText);
                }
                //else do not print - this log is ackward (e.g. the log may start with \n character)
            }
        }

#if (defined _WIN32) && (defined FULL_DEBUGGING)
        std::string textWithLineEnding(text);
        textWithLineEnding += "\n";
        ::OutputDebugStringA(textWithLineEnding.c_str());
#endif

    }
    //else ignore this log!

    if (logLevel >= m_fileLogLevel)
    {
        // this log's priority is at least that of the current log level, so allow it to be handled
        for (
            std::list<ILogSink* >::iterator iter(m_fileLogSinkCollection.begin());
            iter != m_fileLogSinkCollection.end();
            ++iter)
        {
            (*iter)->handleLog(logLevel, timeString, text);
        }
    }
    //else ignore this log!
}

void LogHandler::log(
        const ESeverityLevel logLevel,
        const std::wstring& text)
{
    std::wstring timeString(Utils::getTimeStampWStringWithMicroseconds());

    if (logLevel >= m_consoleLogLevel)
    {
        // this log's priority is at least that of the current log level, so allow it to be handled
        for (
            std::list<ILogSink* >::iterator iter(m_consoleLogSinkCollection.begin());
            iter != m_consoleLogSinkCollection.end();
            ++iter)
        {
            (*iter)->handleLog(logLevel, timeString, text);
        }

#if (defined _WIN32) && (defined FULL_DEBUGGING)
        std::wstring textWithLineEnding(text);
        textWithLineEnding += L"\n";
        ::OutputDebugStringW(textWithLineEnding.c_str());
#endif

    }
    //else ignore this log!

    if (logLevel >= m_fileLogLevel)
    {
        // this log's priority is at least that of the current log level, so allow it to be handled
        for (
            std::list<ILogSink* >::iterator iter(m_fileLogSinkCollection.begin());
            iter != m_fileLogSinkCollection.end();
            ++iter)
        {
            (*iter)->handleLog(logLevel, timeString, text);
        }
    }
    //else ignore this log!
}

void LogHandler::setConsoleLogLevel(const ESeverityLevel logLevel)
{
    m_consoleLogLevel = logLevel;

    for (
        std::list<ILogSink* >::iterator iter(m_consoleLogSinkCollection.begin());
        iter != m_consoleLogSinkCollection.end();
        ++iter)
    {
        (*iter)->setLogLevel(logLevel);
    }
}

void LogHandler::logLogLevel()
{
    for (
        std::list<ILogSink* >::iterator iter(m_fileLogSinkCollection.begin());
        iter != m_fileLogSinkCollection.end();
        ++iter)
    {
        (*iter)->logLogLevel();
    }
}

void LogHandler::setFileLogLevel(const ESeverityLevel logLevel)
{
    m_fileLogLevel = logLevel;

    for (
        std::list<ILogSink* >::iterator iter(m_fileLogSinkCollection.begin());
        iter != m_fileLogSinkCollection.end();
        ++iter)
    {
        (*iter)->setLogLevel(logLevel);
    }
}

bool LogHandler::isLogLevelAboveThreshold(const ESeverityLevel logLevel)
{
    bool result = (logLevel >= m_fileLogLevel) || (logLevel >= m_consoleLogLevel);

    return result;
}

void LogHandler::setFileLogMaxNumberOfEntries(const long maxNumberOfEntries)
{
    for (
        std::list<ILogSink* >::iterator iter(m_fileLogSinkCollection.begin());
        iter != m_fileLogSinkCollection.end();
        ++iter)
    {
        (*iter)->setFileLogMaxNumberOfEntries(maxNumberOfEntries);
    }
}

void LogHandler::setFileLogMaxNumberOfCharacters(const long maxNumberOfCharacters)
{
    for (
        std::list<ILogSink* >::iterator iter(m_fileLogSinkCollection.begin());
        iter != m_fileLogSinkCollection.end();
        ++iter)
    {
        (*iter)->setFileLogMaxNumberOfCharacters(maxNumberOfCharacters);
    }
}
