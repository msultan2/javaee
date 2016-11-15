#include "stdafx.h"
#include "applicationconfiguration.h"
#ifdef USE_CONFIGURATION_PARAMETERS
#include "configurationparameters.h"
#endif
#include "logger.h"
#include "os_utilities.h"

#undef USE_WX

#ifdef USE_WX
#include <wx/msw/wrapwin.h>
#include <wx/dir.h>
#else
#ifdef _WIN32
#include <windows.h>
#endif
#endif

#ifdef _WIN32
#include <shlobj.h>
#include <io.h>   // For access().
#else
#include <pwd.h> //for getpwuid
#endif

#include <algorithm>
#include <sys/types.h>  // For stat().
#include <sys/stat.h>   // For stat().
#include <iostream>

#include <types.h>

namespace
{
    const tchar DEFAULT_USER_DIRECTORY[] = _T("BlueTruth");

    const tchar BLUETRUTH_FALLBACK_TEMP_DIRECTORY_NAME[] = _T("Temp");

    const tchar BLUETRUTH_INSTALL_DIR_KEY_LOCATION[] = _T("SOFTWARE\\Simulation Systems Ltd\\BlueTruth");
    const tchar BLUETRUTH_INSTALL_DIR_VALUE_NAME[] = _T("InstallPath");
    const tchar BLUETRUTH_WORKING_DIR_VALUE_NAME[] = _T("DataDirectory");

    const char MODULE_NAME[] = "ApplicationConfiguration";

    const tchar DEFAULT_LOG_SUBDIRECTORY[] = _T("log");

} //namespace


namespace BlueTruth
{

ApplicationConfiguration* ApplicationConfiguration::m_instancePtr = 0;

std::tstring ApplicationConfiguration::m_dataDirectory;
std::tstring ApplicationConfiguration::m_sysConfDirectory;
std::tstring ApplicationConfiguration::m_cacheDirectory;
std::tstring ApplicationConfiguration::m_userDataDirectory;
std::tstring ApplicationConfiguration::m_temporaryDirectory;
std::tstring ApplicationConfiguration::m_logDirectory;

bool ApplicationConfiguration::m_valid = false;


ApplicationConfiguration::ApplicationConfiguration(void)
{
    initialiseDirectories();
}

ApplicationConfiguration::~ApplicationConfiguration(void)
{
    //do nothing
}

ApplicationConfiguration* ApplicationConfiguration::getInstancePtr()
{
    return m_instancePtr;
}

void ApplicationConfiguration::construct()
{
    if (m_instancePtr == 0) 
    {
        m_instancePtr = new ApplicationConfiguration();
        m_valid = true;
    }
    else
    {
        // already constructed, do nothing!
    }
}

void ApplicationConfiguration::destruct()
{
    if (m_instancePtr!=0)
    {
        delete m_instancePtr;
        m_instancePtr = 0;
    }
    else
    {
        //do nothing
    }
}

void ApplicationConfiguration::initialiseDirectories()
{
    evaluateDataDirectory();
    evaluateSysConfDirectory();
    evaluateCacheDirectory();
    evaluateUserDataDirectory();
    evaluateTemporaryDirectory();
    evaluateLogDirectory();
}

void ApplicationConfiguration::evaluateDataDirectory()
{
#ifdef _WIN32
    HKEY installationDirectoryKey;

    if (ERROR_SUCCESS == ::RegOpenKeyEx(
            HKEY_LOCAL_MACHINE,
            BLUETRUTH_INSTALL_DIR_KEY_LOCATION,
            REG_OPTION_NON_VOLATILE,
            KEY_READ,
            &installationDirectoryKey)
        )
    {
        tchar buf[512];
        DWORD bufSize = 512 * sizeof(tchar);

        //Get the size of the data required
        if (ERROR_SUCCESS == ::RegQueryValueEx(
            installationDirectoryKey,
            BLUETRUTH_INSTALL_DIR_VALUE_NAME,
            0,
            NULL,
            (LPBYTE)buf,
            &bufSize))
        {
            m_dataDirectory.assign(buf);
            m_dataDirectory.append(DIRECTORY_SEPARATOR);
        }
        else
        {
            //do nothing
        }

        ::RegCloseKey(installationDirectoryKey);
    }
    else
    {
        //Probably program is running from a local directory (e.g. during program development)
        m_dataDirectory = _T(".\\");
    }
#else
#ifdef PKG_DATA_DIRECTORY
    m_dataDirectory += OS_Utilities::StringToTString(PKG_DATA_DIRECTORY);
    m_dataDirectory += DIRECTORY_SEPARATOR;
#else
    m_dataDirectory = _T("/usr/local/share/bt/");
#endif //PKG_DATA_DIRECTORY

    //Check if directory exists
    if (!OS_Utilities::directoryExists(m_dataDirectory.c_str()))
    {
        //Probably program is running from a local directory (e.g. during program development)
        m_dataDirectory = _T("./");
    }
    //else do nothing
#endif
}

void ApplicationConfiguration::evaluateSysConfDirectory()
{
#ifdef _WIN32
    HKEY installationDirectoryKey;

    if (ERROR_SUCCESS == ::RegOpenKeyEx(
            HKEY_LOCAL_MACHINE,
            BLUETRUTH_INSTALL_DIR_KEY_LOCATION,
            REG_OPTION_NON_VOLATILE,
            KEY_READ,
            &installationDirectoryKey)
        )
    {
        tchar buf[512];
        DWORD bufSize = 512 * sizeof(tchar);

        //Get the size of the data required
        if (ERROR_SUCCESS == ::RegQueryValueEx(
            installationDirectoryKey,
            BLUETRUTH_INSTALL_DIR_VALUE_NAME,
            0,
            NULL,
            (LPBYTE)buf,
            &bufSize))
        {
            m_dataDirectory.assign(buf);
            m_dataDirectory.append(DIRECTORY_SEPARATOR);
        }
        else
        {
            //do nothing
        }

        ::RegCloseKey(installationDirectoryKey);
    }
    else
    {
        //Probably program is running from a local directory (e.g. during program development)
        m_sysConfDirectory = _T(".\\");
    }
#else //__linux__
#ifdef SYSCONF_DIRECTORY
    m_sysConfDirectory += OS_Utilities::StringToTString(SYSCONF_DIRECTORY);
    m_sysConfDirectory += DIRECTORY_SEPARATOR;
#else
    m_sysConfDirectory = _T(".");
#endif //SYSCONF_DIRECTORY

    //Check if directory exists
    if (!OS_Utilities::directoryExists(m_sysConfDirectory.c_str()))
    {
        //Probably program is running from a local directory (e.g. during program development)
        m_sysConfDirectory = _T("./");
    }
    //else do nothing
#endif
}

void ApplicationConfiguration::evaluateCacheDirectory()
{
#ifdef _WIN32
    evaluateUserDataDirectory();
    m_cacheDirectory= m_userDataDirectory;
#else //_linux

#ifdef LOCAL_STATE_DIRECTORY
    m_cacheDirectory += OS_Utilities::StringToTString(LOCAL_STATE_DIRECTORY);
    m_cacheDirectory += DIRECTORY_SEPARATOR;
    m_cacheDirectory += _T("cache");
    m_cacheDirectory += DIRECTORY_SEPARATOR;
    m_cacheDirectory += _T("bt");
    m_cacheDirectory += DIRECTORY_SEPARATOR;
#else
    m_cacheDirectory = _T(".");
#endif //LOCAL_STATE_DIRECTORY

#endif
}

void ApplicationConfiguration::evaluateUserDataDirectory()
{
#ifdef _WIN32
    TCHAR wszPath[MAX_PATH];

    //Read Value from local machine registry (HKEY_LOCAL_MACHINE)
    std::tstring userDataDirectoryFromLocalMachineRegistry;
    HKEY userDataDirectoryFromLocalMachineRegistryKey;

    if (ERROR_SUCCESS == ::RegOpenKeyEx(
            HKEY_LOCAL_MACHINE,
            BLUETRUTH_INSTALL_DIR_KEY_LOCATION,
            REG_OPTION_NON_VOLATILE,
            KEY_READ,
            &userDataDirectoryFromLocalMachineRegistryKey)
        )
    {
        tchar buf[512];
        DWORD bufSize = 512 * sizeof(tchar);

        //Get the size of the data required
        if (ERROR_SUCCESS == ::RegQueryValueEx(
            userDataDirectoryFromLocalMachineRegistryKey,
            BLUETRUTH_WORKING_DIR_VALUE_NAME,
            0,
            NULL,
            (LPBYTE)buf,
            &bufSize))
        {
            userDataDirectoryFromLocalMachineRegistry.assign(buf);
            userDataDirectoryFromLocalMachineRegistry.append(DIRECTORY_SEPARATOR);
        }
        else
        {
            //do nothing
        }

        ::RegCloseKey(userDataDirectoryFromLocalMachineRegistryKey);
    }
    else
    {
        //Probably the BlueTruth is running from a local directory (e.g. during program development)
        //do nothing
    }

    //Read Value from current user registry (HKEY_CURRENT_USER)
#ifdef USE_CONFIGURATION_PARAMETERS
    View::ConfigurationParameters::construct();
    const std::tstring USER_DATA_DIRECTORY_FROM_CURRENT_USER_REGISTRY(
        View::ConfigurationParameters::getInstancePtr()->getUserDataDirectory());
    _tcscpy(&wszPath[0], USER_DATA_DIRECTORY_FROM_CURRENT_USER_REGISTRY.c_str());
#else
    const std::tstring USER_DATA_DIRECTORY_FROM_CURRENT_USER_REGISTRY;
#endif
    if (!USER_DATA_DIRECTORY_FROM_CURRENT_USER_REGISTRY.empty())
    {
        m_userDataDirectory = USER_DATA_DIRECTORY_FROM_CURRENT_USER_REGISTRY;
    }
    else if (!userDataDirectoryFromLocalMachineRegistry.empty())
    {
        m_userDataDirectory = userDataDirectoryFromLocalMachineRegistry;
#ifdef USE_CONFIGURATION_PARAMETERS
        View::ConfigurationParameters::getInstancePtr()->setUserDataDirectory(m_userDataDirectory);
#endif
    }
    else
    {
        //Get local user application data directory 
        if (SUCCEEDED(
                ::SHGetFolderPath(
                    NULL, 
                    CSIDL_LOCAL_APPDATA|CSIDL_FLAG_CREATE, 
                    NULL, 
                    0, 
                    wszPath))
            )
        {

            m_userDataDirectory = wszPath;

            //Modify user directory for use by the BlueTruth
            m_userDataDirectory.append(DIRECTORY_SEPARATOR);
            m_userDataDirectory.append(DEFAULT_USER_DIRECTORY);
            m_userDataDirectory.append(DIRECTORY_SEPARATOR);

#ifdef USE_CONFIGURATION_PARAMETERS
            View::ConfigurationParameters::getInstancePtr()->setUserDataDirectory(m_userDataDirectory);
#endif
        }
        else
        {
            //do nothing
        }
    }
#else //__linux__
#ifdef USE_CONFIGURATION_PARAMETERS
    View::ConfigurationParameters::construct();
    const std::tstring USER_DATA_DIRECTORY_FROM_CURRENT_USER_REGISTRY(
        View::ConfigurationParameters::getInstancePtr()->getUserDataDirectory());
#else
    const std::tstring USER_DATA_DIRECTORY_FROM_CURRENT_USER_REGISTRY;
#endif

    if (!USER_DATA_DIRECTORY_FROM_CURRENT_USER_REGISTRY.empty())
    {
        m_userDataDirectory = USER_DATA_DIRECTORY_FROM_CURRENT_USER_REGISTRY;
    }
    else
    {
        struct passwd *pw = getpwuid(getuid());
        m_userDataDirectory = OS_Utilities::StringToTString(pw->pw_dir);
        m_userDataDirectory.append(DIRECTORY_SEPARATOR);
        m_userDataDirectory.append(DEFAULT_USER_DIRECTORY);
        m_userDataDirectory.append(DIRECTORY_SEPARATOR);

#ifdef USE_CONFIGURATION_PARAMETERS
        View::ConfigurationParameters::getInstancePtr()->setUserDataDirectory(m_userDataDirectory);
#endif
    }
#endif
}

void ApplicationConfiguration::evaluateTemporaryDirectory()
{
#ifdef _WIN32
    TCHAR wszPath[MAX_PATH];
    DWORD pathLen = ::GetTempPath(MAX_PATH, wszPath);
    if (
        (pathLen <= MAX_PATH) && 
        (pathLen > 0)
        )
    {
        bool fallBackToLocalTempDirectory = false;

        //Got the temporary path here
        m_temporaryDirectory = wszPath;

        if (OS_Utilities::directoryExists(m_temporaryDirectory.c_str()))
        {
            m_temporaryDirectory.append(DEFAULT_USER_DIRECTORY);

            if (OS_Utilities::directoryExists(m_temporaryDirectory.c_str()))
            {
                //Already exists
                m_temporaryDirectory.append(DIRECTORY_SEPARATOR);
            }
            else
            {
                //Try to create temporary folder
                BOOL result = ::CreateDirectory(m_temporaryDirectory.c_str(), 0);

                if (result != FALSE)
                {
                    m_temporaryDirectory.append(DIRECTORY_SEPARATOR);
                }
                else
                {
                    fallBackToLocalTempDirectory = true;
                }
            }
        }
        else
        {
            fallBackToLocalTempDirectory = true;
        }

        if (fallBackToLocalTempDirectory)
        {
            m_temporaryDirectory.assign(m_userDataDirectory);
            m_temporaryDirectory.append(BLUETRUTH_FALLBACK_TEMP_DIRECTORY_NAME);

            BOOL result = ::CreateDirectory(m_temporaryDirectory.c_str(), 0);
            if (result != FALSE)
            {
                m_temporaryDirectory.append(DIRECTORY_SEPARATOR);
            }
            else
            {
                //All else fails - use local directory
                m_temporaryDirectory.clear();
            }
        }
        else
        {
            //do nothing
        }
    }
    else
    {
        //do nothing
    }
#else
    m_temporaryDirectory = _T("/tmp/");
#endif
}

void ApplicationConfiguration::evaluateLogDirectory()
{
#ifdef _WIN32
    m_logDirectory = m_userDataDirectory +
        std::tstring(DEFAULT_LOG_SUBDIRECTORY) + std::tstring(DIRECTORY_SEPARATOR);
#else //_linux

#ifdef LOCAL_STATE_DIRECTORY
    m_logDirectory += OS_Utilities::StringToTString(LOCAL_STATE_DIRECTORY);
    m_logDirectory += DIRECTORY_SEPARATOR;
    m_logDirectory += _T("log");
    m_logDirectory += DIRECTORY_SEPARATOR;
    m_logDirectory += _T("bt");
    m_logDirectory += DIRECTORY_SEPARATOR;
#else
    m_logDirectory = _T(".");
#endif //LOCAL_STATE_DIRECTORY

#endif
}

std::vector<std::tstring> ApplicationConfiguration::getSetOfAllDirectoriesForInitialisation()
{
    construct();

    std::vector<std::tstring> result;

    result.push_back(m_dataDirectory);
#ifdef _WIN32
    result.push_back(m_userDataDirectory);
#else
    //The user data directory will not be used
#endif
    result.push_back(m_cacheDirectory);
    result.push_back(m_logDirectory);

    return result;
}

const std::tstring& ApplicationConfiguration::getDataDirectory()
{
    construct();
    return m_dataDirectory;
}

const std::tstring& ApplicationConfiguration::getSysConfDirectory()
{
    construct();
    return m_sysConfDirectory;
}

const std::tstring& ApplicationConfiguration::getCacheDirectory()
{
    construct();
    return m_cacheDirectory;
}

const std::tstring& ApplicationConfiguration::getUserDataDirectory()
{
    construct();
    return m_userDataDirectory;
}

const std::tstring& ApplicationConfiguration::getTemporaryDirectory()
{
    construct();
    return m_temporaryDirectory;
}

const std::tstring& ApplicationConfiguration::getLogDirectory()
{
    construct();
    return m_logDirectory;
}

bool ApplicationConfiguration::isValid()
{
    return m_valid;
}

} //namespace
