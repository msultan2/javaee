/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef _APPLICATION_CONFIGURATION_H_
#define _APPLICATION_CONFIGURATION_H_

#include "unicode_types.h"
#include <vector>


namespace BlueTruth
{

class ApplicationConfiguration
{

public:

    virtual ~ApplicationConfiguration(void);

    static void construct();
    static void destruct();

    static ApplicationConfiguration* getInstancePtr();

    static std::vector<std::tstring> getSetOfAllDirectoriesForInitialisation();

    //! Return the location of the applications global, i.e. not user-specific, data file.
    //! Unix: prefix/share/appname
    static const std::tstring& getDataDirectory();

    //! Return the location of the application configuration, i.e. not user-specific, configuration file.
    //! Unix: /opt/bt/etc
    static const std::tstring& getSysConfDirectory();

    //! Return the location of the application cache directory, where file may be stored between program invocation
    //! Unix: /var/cache/bt
    static const std::tstring& getCacheDirectory();

    //! Return the directory for the (non-roaming) user config files:
    //! Unix: ~ (the home directory)
    //! Windows: C:\Documents and Settings\username\Local Settings\Application Data
    static const std::tstring& getUserDataDirectory();

    static const std::tstring& getTemporaryDirectory();

    static const std::tstring& getLogDirectory();

    bool isValid();

private:

    //! default constructor
    ApplicationConfiguration(void);

    //! copy constructor. Not implemented
    ApplicationConfiguration(const ApplicationConfiguration &rhs);
    //! assignment operator. Not implemented
    ApplicationConfiguration& operator=(const ApplicationConfiguration &rhs);

    static void initialiseDirectories();
    static void evaluateDataDirectory();
    static void evaluateSysConfDirectory();
    static void evaluateCacheDirectory();
    static void evaluateUserDataDirectory();
    static void evaluateTemporaryDirectory();
    static void evaluateLogDirectory();

    //Private members:

    static ApplicationConfiguration* m_instancePtr;

    static std::tstring m_dataDirectory;
    static std::tstring m_sysConfDirectory;
    static std::tstring m_cacheDirectory;
    static std::tstring m_userDataDirectory;
    static std::tstring m_temporaryDirectory;
    static std::tstring m_logDirectory;

    static bool m_valid;
};

}//namespace

#endif //_APPLICATION_CONFIGURATION_H_
