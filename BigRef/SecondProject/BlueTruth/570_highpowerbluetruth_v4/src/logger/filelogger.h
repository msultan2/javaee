//////////////////////////////////////////////////////////////////////////////
//
// System: TSS Tunnel Access Controller
// Module: FileLogger
// Author: P.Reilly
// Compiler: Microsoft Visual C++
// Description: Encapsulates all the functionality of logging data to a 
//              standard ASCII text file and provides a simple interface
//              for external objects to log data
//
// Modification History:
//
// 2006-01-03 PKR 001 Created
// 2008-10-02 RG  002 SCJS 68
//                    - A few meaningless const qualifiers removed to stop 
//                      Cantata from complaining
//
//////////////////////////////////////////////////////////////////////////////

/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Modification History:

    Date        Who     SCJS No     Remarks
    05/06/2009  EWT     001         V1.00 First Issue
  
*/


#ifndef _FILELOGGER_H_
#define _FILELOGGER_H_

#include "unicode_types.h"

// system includes
#include <ctime>


class FileLogger 
{
    public:
        // constructor
        explicit FileLogger(const bool truncate = false);

        // destructor
        virtual ~FileLogger(void);  
        
        // writes a string to the file, @returns true on success
        bool logString(const std::string& logStr);

        // open a file of the given name
        bool createFile(const std::tstring& fileName);

        // open a file of the given name
        bool openFile(const std::tstring& fileName);

        // close a file
        void closeFile();

        const std::tstring getName(void) const {return m_fileName;}

        // @return the time the file was created
        const time_t& getTimeOfCreation(void) const { return m_timeOfCreation; }

    private:
        // copy constructor, not implemented
        FileLogger(const FileLogger& rhs);
        // copy assignment operator, not implemented
        FileLogger& operator=(const FileLogger& rhs); 

        bool openFileIfExistsElseCreate(void);

        bool writeToLog(const std::string& text);

        // file to which data will be written
        std::ofstream   m_logFile;

        // name of the file to which data will be written
        std::tstring     m_fileName;
        const bool      m_TRUNCATE;
        time_t          m_timeOfCreation;
};

#endif // _FILELOGGER_H_



