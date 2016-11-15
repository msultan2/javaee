/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Modification History:
*/


#ifndef _MANAGEDFILELOGGER_H_
#define _MANAGEDFILELOGGER_H_

#include "filelogger.h"
#include "unicode_types.h"

#include <boost/filesystem.hpp>


class ManagedFileLogger
{
public:
    /**
     * Constructor
     *
     * @param logDirectory - the name of the log directory (may include the full, absolute path),
     * @param maxNumberOfEntries - the maximum number of entries allowed in the file
     * @param logSuffix - the suffix to be appended to each log file
     */
    ManagedFileLogger(
        const std::tstring& logDirectory,
        const unsigned int maxNumberOfEntries,
        const std::tstring& logPrefix = _T("LOGFILE"),
        const std::tstring& logSuffix = _T("LOG"));

    //!
    //! destructor
    virtual ~ManagedFileLogger();

    //! logs to the file, will create a new file if necessary
    //! @return true on success
    bool log(const std::string& text);

    void setFileLogMaxNumberOfEntries(const unsigned int maxNumberOfEntries);
    void setFileLogMaxNumberOfCharacters(const unsigned int maxNumberOfCharacters);

    /**
     * @brief Check if a directory can be used for logging (verify if it exists and rw access)
     * @param log_directory directory to be verified
     */
    static bool checkDirectoryAccess(const std::tstring& log_directory);

private:

    //! default constructor, not implemented
    ManagedFileLogger();
    //! copy constructor, not implemented
    ManagedFileLogger(const ManagedFileLogger& rhs);
    //! copy assignment operator, not implemented
    ManagedFileLogger& operator=(const ManagedFileLogger& rhs);

    void createNewLogFile();

    //Private members
    //Remark: All filenames are kept Unicode but the logs themselves are ASCII
    const std::tstring           m_logDirectory;
    const std::tstring           m_PREFIX;
    const std::tstring           m_SUFFIX;
    FileLogger                   m_fileLogger;

    unsigned int                 m_numberOfEntries;
    unsigned int                 m_maxNumberOfEntries;

    size_t                       m_numberOfCharacters;
    size_t                       m_maxNumberOfCharacters;

    std::tstring                 m_lastFileTimeStamp;
    unsigned int                 m_sameTimeLog;
    std::tstring                 m_currentFilenameWithSuffix;
    unsigned int                 m_fileIndex;
};

#endif // _MANAGEDFILELOGGER_H_

