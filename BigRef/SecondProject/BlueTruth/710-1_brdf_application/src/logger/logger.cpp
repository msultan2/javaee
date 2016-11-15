#include "logger.h"

#include "boostlogger.h"


boost::shared_ptr< ::BoostLogger > Logger::m_pLogger;


void Logger::initialise(const char* logDirectoryName)
{
    if (m_pLogger == 0)
    {
        m_pLogger = boost::shared_ptr<BoostLogger>(new ::BoostLogger(logDirectoryName));
    }
    //do nothing
}

void Logger::initialise(const wchar_t* logDirectoryName)
{
    if (m_pLogger == 0)
    {
        m_pLogger = boost::shared_ptr<BoostLogger>(new ::BoostLogger(logDirectoryName));
    }
    //do nothing
}


void Logger::destruct()
{
    m_pLogger.reset();
}

void Logger::setConsoleLogLevel(const ESeverityLevel logLevel)
{
    m_pLogger->setConsoleLogLevel(logLevel);
}

void Logger::setFileLogLevel(const ESeverityLevel logLevel)
{
    m_pLogger->setFileLogLevel(logLevel);
}

void Logger::log(
    const ESeverityLevel logLevel,
    const char* description1,
    const char* description2,
    const char* description3,
    const char* description4)
{
    if (m_pLogger)
    {
        if (m_pLogger->isLogLevelAboveThreshold(logLevel))
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

            m_pLogger->log(logLevel, logStream);
        }
    }
    else
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

        if (static_cast< std::size_t >(logLevel) < (sizeof(str) / sizeof(*str)))
        {
            std::cout << "<" << str[logLevel] << ">";
        }
        else
        {
            std::cout << static_cast< int >(logLevel);
        }

        if (description1 != 0)
            std::cout << ": " << description1;

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
    if (m_pLogger)
    {
        if (m_pLogger->isLogLevelAboveThreshold(logLevel))
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

            m_pLogger->log(logLevel, logStream);
        }
    }
    else
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

        if (static_cast< std::size_t >(logLevel) < (sizeof(str) / sizeof(*str)))
        {
            std::wcout << L"<" << str[logLevel] << L">";
        }
        else
        {
            std::wcout << static_cast< int >(logLevel);
        }

        if (description1 != 0)
            std::wcout << L": " << description1;

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

void Logger::logLogLevel()
{
    m_pLogger->logLogLevel();
}

bool Logger::isLogLevelAboveThreshold(const ESeverityLevel logLevel)
{
    return m_pLogger->isLogLevelAboveThreshold(logLevel);
}

void Logger::setFileLogMaxNumberOfEntries(const long maxNumberOfEntries)
{
    m_pLogger->setFileLogMaxNumberOfEntries(maxNumberOfEntries);
}

void Logger::setFileLogMaxNumberOfCharacters(const long maxNumberOfCharacters)
{
    m_pLogger->setFileLogMaxNumberOfCharacters(maxNumberOfCharacters);
}

bool Logger::isValid()
{
    return m_pLogger->isValid();
}
