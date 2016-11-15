#include "stdafx.h"
#include "logger.h"

#include "ierrordialog.h"
#include "legacyfilelogsink.h"
#include "legacyconsolelogsink.h"
#include "loghandler.h"
#include "os_utilities.h"
#include "types.h"
#include "utils.h"
#include "viewlogsink.h"

#ifdef _WIN32
#include <windows.h>
#endif

#include <iostream>
#include <sstream>


LegacyConsoleLogSink* Logger::m_pLegacyConsoleLogSink = 0;
LegacyFileLogSink* Logger::m_pLegacyFileLogSink = 0;
ViewLogSink* Logger::m_pViewLogSink = 0;
LogHandler Logger::m_logHandler;

View::IErrorDialog* Logger::c_errorDialogPtr = 0;


void Logger::initialise(const char* logDirectoryName)
{
    m_pLegacyConsoleLogSink = new LegacyConsoleLogSink();
    m_logHandler.addConsoleLogSink(m_pLegacyConsoleLogSink);
    m_pLegacyFileLogSink = new LegacyFileLogSink(logDirectoryName);
    m_logHandler.addFileLogSink(m_pLegacyFileLogSink);

    //The view has been created. Now bind it to logHandler
    m_pViewLogSink = new ViewLogSink();
    m_logHandler.addConsoleLogSink(m_pViewLogSink);
}

void Logger::initialise(const wchar_t* logDirectoryName)
{
    m_pLegacyConsoleLogSink = new LegacyConsoleLogSink();
    m_logHandler.addConsoleLogSink(m_pLegacyConsoleLogSink);
    m_pLegacyFileLogSink = new LegacyFileLogSink(logDirectoryName);
    m_logHandler.addFileLogSink(m_pLegacyFileLogSink);

    //The view has been created. Now bind it to logHandler
    m_pViewLogSink = new ViewLogSink();
    m_logHandler.addConsoleLogSink(m_pViewLogSink);
}

void Logger::destruct()
{
    //Disconnect view logging first
    m_logHandler.delConsoleLogSink(m_pViewLogSink);
    delete m_pViewLogSink;

    m_logHandler.clearLogSinks();
    delete m_pLegacyConsoleLogSink;
    delete m_pLegacyFileLogSink;
}

void Logger::log(
    const ESeverityLevel logLevel,
    const char* description1,
    const char* description2,
    const char* description3,
    const char* description4)
{
    if (!m_logHandler.isVoid())
    {
        if (m_logHandler.isLogLevelAboveThreshold(logLevel))
        {
            std::string logStream;
            logStream.reserve(1024);

            if (description1 != 0)
            {
                logStream += description1;
            }
            //else do nothing

            // check if optional parameter exists
            if (description2 != 0)
            {
                logStream += ", ";
                logStream += description2;
            }
            //else do nothing

            if (description3 != 0)
            {
                logStream += ", ";
                logStream += description3;
            }
            //else do nothing

            if (description4 != 0)
            {
                logStream += ", ";
                logStream += description4;
            }
            //else do nothing

            m_logHandler.log(
                logLevel,
                logStream);
        }
        //else do nothing
    }
    else
    {
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
            std::cout <<
                Utils::getTimeStampStringWithMicroseconds() << ": " << str[logLevel];
        }
        else
        {
            std::cout << static_cast< int >(logLevel);
        }

        if (description1 != 0)
            std::cout << " " << description1;

        // check if optional parameter exists
        if (description2 != 0)
            std::cout << ", " << description2;

        if (description3 != 0)
            std::cout << ", " << description3;

        if (description4 != 0)
            std::cout << ", " << description4;

        std::cout << std::endl;
    }
}

void Logger::log(
    const ESeverityLevel logLevel,
    const wchar_t* description1,
    const wchar_t* description2,
    const wchar_t* description3,
    const wchar_t* description4)
{
    if (!m_logHandler.isVoid())
    {
        if (m_logHandler.isLogLevelAboveThreshold(logLevel))
        {
            std::wstring logStream;
            logStream.reserve(1024);

            if (description1 != 0)
            {
                logStream += description1;
            }
            //else do nothing

            // check if optional parameter exists
            if (description2 != 0)
            {
                logStream += L", ";
                logStream += description2;
            }
            //else do nothing

            if (description3 != 0)
            {
                logStream += L", ";
                logStream += description3;
            }
            //else do nothing

            if (description4 != 0)
            {
                logStream += L", ";
                logStream += description4;
            }
            //else do nothing

            m_logHandler.log(
                logLevel,
                logStream);
        }
        //else do nothing
    }
    else
    {
        static const wchar_t* const str[] =
        {
            L"<DEBUG3>",
            L"<DEBUG2>",
            L"<DEBUG1>",
            L"<NORMAL>",
            L"<NOTIFICATION>",
            L"<WARNING>",
            L"<ERROR>",
            L"<EXCEPTION>",
            L"<FATAL>"
        };

        if (static_cast< std::size_t >(logLevel) < (sizeof(str) / sizeof(*str)))
        {
            std::wcout <<
                Utils::getTimeStampWStringWithMicroseconds() << L": " << str[logLevel];
        }
        else
        {
            std::wcout << static_cast< int >(logLevel);
        }

        if (description1 != 0)
            std::wcout << L" " << description1;

        // check if optional parameter exists
        if (description2 != 0)
            std::wcout << L", " << description2;

        if (description3 != 0)
            std::wcout << L", " << description3;

        if (description4 != 0)
            std::wcout << L", " << description4;

        std::wcout << std::endl;
    }

}

bool Logger::isLogLevelAboveThreshold(const ESeverityLevel logLevel)
{
    return (m_logHandler.isLogLevelAboveThreshold(logLevel));
}

void Logger::setConsoleLogLevel(const ESeverityLevel logLevel)
{
    m_logHandler.setConsoleLogLevel(logLevel);
}

void Logger::setFileLogLevel(const ESeverityLevel logLevel)
{
    m_logHandler.setFileLogLevel(logLevel);
}

void Logger::setFileLogMaxNumberOfEntries(const long maxNumberOfEntries)
{
    m_logHandler.setFileLogMaxNumberOfEntries(maxNumberOfEntries);
}

void Logger::setFileLogMaxNumberOfCharacters(const long maxNumberOfCharacters)
{
    m_logHandler.setFileLogMaxNumberOfCharacters(maxNumberOfCharacters);
}

void Logger::logLogLevel()
{
    m_logHandler.logLogLevel();
}

/*
void Logger::setLogHandler(LogHandler* pLogger)
{
    m_pLogHandler = pLogger;
}
*/

void Logger::setErrorDialog(View::IErrorDialog* errorDialogPtr)
{
    c_errorDialogPtr = errorDialogPtr;
}
