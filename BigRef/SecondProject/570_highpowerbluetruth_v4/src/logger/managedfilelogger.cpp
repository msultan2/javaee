#include "stdafx.h"
#include "managedfilelogger.h"

#include "types.h"
#include "os_utilities.h"
#include "utils.h"

#include <iostream>
#include <sstream>
#include <cstdio>

namespace
{
    std::ostringstream s_ss;

    const char END_OF_LOG_TEXT[] = "[ENDOFLOG]";
}

ManagedFileLogger::ManagedFileLogger(const std::tstring& logDirectory,
                                     const unsigned int maxNumberOfEntries,
                                     const std::tstring& logPrefix,
                                     const std::tstring& logSuffix)
:
m_logDirectory(logDirectory),
m_PREFIX(logPrefix),
m_SUFFIX(logSuffix),
m_fileLogger(true),
m_numberOfEntries(0),
m_maxNumberOfEntries(maxNumberOfEntries),
m_numberOfCharacters(0),
m_maxNumberOfCharacters(0),
m_lastFileTimeStamp(),
m_sameTimeLog(0),
m_currentFilenameWithSuffix(),
m_fileIndex(0)
{
    if (checkDirectoryAccess(m_logDirectory))
    {
        createNewLogFile();
    }
    //else do nothing
}

ManagedFileLogger::~ManagedFileLogger()
{
    //do nothing
}

void ManagedFileLogger::createNewLogFile()
{
    // first, find out what the name of the file is
    std::tstring fullFileName;
    fullFileName.reserve(256);

    bool createFileResult = false;
    do
    {
        //Change the name of the file
        //Wait one second so that the time signature will change and hope no other file is created
        //at this moment...
        OS_Utilities::sleep(1000);

        fullFileName.clear();
        fullFileName += m_logDirectory;
        fullFileName += m_PREFIX;
        fullFileName += _T("_");
        fullFileName += OS_Utilities::StringToTString(Utils::getFileTimeStampString());
        if (m_fileIndex > 0)
        {
            fullFileName += _T("_");
            fullFileName += OS_Utilities::StringToTString(Utils::uint64ToString(m_fileIndex));
        }
        //else do nothing
        fullFileName += _T(".");
        fullFileName += m_SUFFIX;
        createFileResult = m_fileLogger.createFile(fullFileName);

        if (!createFileResult)
        {
#if defined(UNICODE)
            std::wcerr << L"Failed to create file " << fullFileName << std::endl;
#else
            std::cerr << "Failed to create file " << fullFileName << std::endl;
#endif

            //Change the name of the file
            //Wait one second so that the time signature will change and hope no other file is created
            //at this moment...
            OS_Utilities::sleep(100);
        }
        //else do nothing
    }
    while (!createFileResult);

    m_currentFilenameWithSuffix = fullFileName;
    m_numberOfEntries = 0;
    m_numberOfCharacters = 0;
    ++m_fileIndex;
}

bool ManagedFileLogger::log(const std::string& text)
{
    bool logOk = m_fileLogger.logString(text);

    //entries' counter
    static const size_t SIZE_OF_NEW_LINE = sizeof("\n");
    m_numberOfCharacters += text.size() + SIZE_OF_NEW_LINE;
    m_numberOfEntries++;

    if (
        (m_numberOfEntries >= m_maxNumberOfEntries) ||
        (
            (m_maxNumberOfCharacters > 0) &&
            (m_numberOfCharacters >= m_maxNumberOfCharacters)
        )
       )
    {
        logOk = m_fileLogger.logString(END_OF_LOG_TEXT);
        std::tstring lastLogFileName(m_currentFilenameWithSuffix);

        if (checkDirectoryAccess(m_logDirectory))
        {
            createNewLogFile();
        }
        //else do nothing

        std::ostringstream ss;
        ss << "### Last log file: \"" << OS_Utilities::StringToAnsi(lastLogFileName)
            << "\". New file created due to number of "
            << ((m_numberOfEntries >= m_maxNumberOfEntries) ? "entries" : "characters")
            << " greater than max allowed";
        m_fileLogger.logString(ss.str());
    }
    else
    {
        //do nothing
    }

    return logOk;
}

void ManagedFileLogger::setFileLogMaxNumberOfEntries(const unsigned int maxNumberOfEntries)
{
    m_maxNumberOfEntries = maxNumberOfEntries;
}

void ManagedFileLogger::setFileLogMaxNumberOfCharacters(const unsigned int maxNumberOfCharacters)
{
    m_maxNumberOfCharacters = maxNumberOfCharacters - sizeof(END_OF_LOG_TEXT);
}

bool ManagedFileLogger::checkDirectoryAccess(const std::tstring& log_directory)
{
    //We use an old syntax - no error_code because boost 1.42 does not have this implementation yet
    try
    {
        // ensure directory exists or can be created

        //TODO For Windows platform some add #ifdef and do not convert to Ansi so that it does compiles
        boost::filesystem::path log_file_path(OS_Utilities::StringToAnsi(log_directory));
        boost::filesystem::create_directory(log_file_path);

        std::tstring testDirectoryName(log_directory + _T("/testdiraccess"));
        boost::filesystem::path test_file_path(OS_Utilities::StringToAnsi(testDirectoryName));

        // ensure we can create file there
        std::ofstream temp_file(test_file_path.string());
        if (!temp_file.is_open())
        {
            std::cerr << "Log directory is read only." << std::endl;
            return false;
        }
        //else do nothing

        temp_file.close();

        // ensure we can remove files
        remove(test_file_path);

        return true;
    }
    catch (const boost::filesystem::filesystem_error& ex)
    {
        std::cerr << "ERROR: Directory " << OS_Utilities::StringToAnsi(log_directory) << " cannot be created or accessed" << std::endl;
        std::cerr << ex.what() << std::endl;
        return false;
    }
    catch (...)
    {
        std::cerr << "ERROR: Directory " << OS_Utilities::StringToAnsi(log_directory) << " cannot be created or accessed" << std::endl;
        return false;
    }
}
