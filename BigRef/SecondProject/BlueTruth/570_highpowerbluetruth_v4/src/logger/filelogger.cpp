#include "stdafx.h"
#include "filelogger.h"
#include "logger.h"
#include "os_utilities.h"

#include <iostream>
#include <sstream>

FileLogger::FileLogger(const bool truncate)
:
    m_logFile(),
    m_fileName(),
    m_TRUNCATE(truncate),
    m_timeOfCreation(0)
{
    //do nothing
}

FileLogger::~FileLogger(void)
{
    // not necessary to close the file because it is done automatically when it goes out of scope
}

bool FileLogger::createFile(const std::tstring& fileName)
{
    bool result = false;

    std::ifstream existingFileWithTheSameName;
    existingFileWithTheSameName.open(OS_Utilities::StringToAnsi(fileName).c_str());

    if (existingFileWithTheSameName.is_open())
    {
        //file exists. Do not open, append it or create a new one
    }
    else
    {
        //close existing log
        m_logFile.close();

        //create new log
        m_logFile.open(OS_Utilities::StringToAnsi(fileName).c_str(), std::ios_base::out | std::ios::trunc);

        //check if it is open
        if (m_logFile.is_open())
        {
            m_fileName = fileName;
            result = true;
        }
        else
        {
            //do nothing
        }
    }

    return result;
}

bool FileLogger::openFile(const std::tstring& fileName)
{
    m_fileName = fileName;

    return openFileIfExistsElseCreate();
}

void FileLogger::closeFile()
{
    m_logFile.close();
}

bool FileLogger::openFileIfExistsElseCreate(void)
{
    bool result = false;

    // check if the file is already open
    if (m_logFile.is_open())
    {
        // the file is open, so close it
        m_logFile.close();
    }
    else
    {
        // file not open, do nothing
    }

    time(&m_timeOfCreation);

    // check to see if the file must be truncated
    if (!m_TRUNCATE)
    {
        // it should NOT be truncated ...

        // open the the file, if the file already exists, open it at the end,
        m_logFile.open(OS_Utilities::StringToAnsi(m_fileName).c_str(), std::ios_base::out | std::ios::app);
    }
    else
    {
        // truncate the file

        m_logFile.open(OS_Utilities::StringToAnsi(m_fileName).c_str(), std::ios_base::out | std::ios::trunc);
    }

    // check if the file is open
    if (m_logFile.is_open())
    {
        // success, do nothing
    }
    else
    {
        // error, the file is not open, so attempt to truncate iot
        m_logFile.open(OS_Utilities::StringToAnsi(m_fileName).c_str(), std::ios_base::out | std::ios::trunc);
    }

    // verify that the file was succesfully opened
    if (!m_logFile.is_open())
    {
        result = false;

        ////RG: this message has been put here as there are more usages of this function.
        ////Usually the file could not be created because:
        //// - directory in which the file should be created does not exist
        //// - the user has not sufficient privilidges to create the file
        //// - the disc is full
        //std::ostringstream textString;
        //textString << "The file \"" << m_fileName << "\" could not be created!";
        //Logger::logFatal(textString.str().c_str());
    }
    else
    {
        result = true;
    }

    return result;
}

bool FileLogger::logString(const std::string& logStr)
{
    // if the file is open
    bool loggedOk = false;

    // check if the file is open
    if (m_logFile.is_open()) 
    {
        // write the string to the file
        loggedOk = writeToLog(logStr);
    }
    else 
    {
        // else, if m_logFile is not open

        // try and open it again
        if (openFileIfExistsElseCreate())
        {
            loggedOk = writeToLog(logStr);
        }
        else
        {
            // do nothing
        }
    }

    return loggedOk;
}

bool FileLogger::writeToLog(const std::string& text)
{
    bool writeOk = false;

    //Reset the badbit and failbit flags
    m_logFile.clear();

    m_logFile << text << std::endl;

    // check if the write succeeded, what if it fails (disk is full, no write priveliges) ?
    if (m_logFile.fail())
    {
        // error, write failed
        writeOk = false;
    }
    else
    {
        // write was successful
        writeOk = true;
    }

    return writeOk;
}

