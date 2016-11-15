#include "stdafx.h"
#include "outstationconfigurationparameters.h"

#include "utils.h"

#ifdef _WIN32
#define USE_REGISTRY
#endif

#if !defined(USE_REGISTRY) && !defined(_DEBUG) && defined(_WIN32)
#define USE_REGISTRY
#endif

#ifndef _WIN32
#undef _T
#endif

#if defined(USE_REGISTRY)
#include <wx/config.h>
#else
//undefine _T macro on linux
#include <wx/fileconf.h>
#endif


namespace
{
    //Parameter labels:
    const tchar LABEL_CORE_CONFIGURATION_VERSION[] = _T("Core/CoreConfigurationVersion");
    const tchar LABEL_SITE_IDENTIFIER[] = _T("Core/SiteIdentifier");
    const tchar LABEL_SSL_SERIAL_NUMBER[] = _T("Core/SSLSerialNumber");

    const tchar LABEL_INSTATION_SSH_ADDRESS[] = _T("Core/InStationSSHAddress");
    const tchar LABEL_INSTATION_SSH_PORT_NUMBER[] = _T("Core/InStationSSHPortNumber");
    const tchar LABEL_INSTATION_SSH_LOGIN[] = _T("Core/InStationSSHLogin");
    const tchar LABEL_INSTATION_SSH_PASSWORD[] = _T("Core/InStationSSHPassword");

    const tchar LABEL_CONFIGURATION_URL[] = _T("Core/ConfigurationURL");
    const tchar LABEL_CONFIGURATION_URL_FILE_PREFIX[] = _T("Core/ConfigurationURL_FilePrefix");
    const tchar LABEL_CONFIGURATION_URL_FILE_SUFFIX[] = _T("Core/ConfigurationURL_FileSuffix");

    const tchar LABEL_LAST_USED_BLUETOOTH_DEVICE[] = _T("Core/LastUsedBlueToothDevice");

    const tchar LABEL_DEVICE_DRIVER[] = _T("Core/DeviceDriver");
    const tchar LABEL_PARANI_PORT_NAME[] = _T("Core/ParaniPortName");
    const tchar LABEL_PARANI_BIT_RATE[] = _T("Core/ParaniBitRate");

    const tchar LABEL_HASHING_FUNCTION[] = _T("Core/HashingFunction");

    const tchar LABEL_INSTATION_RESPONSE_TIMEOUT[] = _T("Connection/InstationResponseTimeoutInMs");
    const tchar LABEL_INSTATION_NUMBER_OF_RETRIES[] = _T("Connection/InstationNumberOfRetries");
    const tchar LABEL_INSTATION_WATCHDOG_PERIOD_IN_SECONDS[] = _T("Connection/InstationWatchdogPeriodInSeconds");

    const tchar LABEL_GSM_MODEM_TYPE[] = _T("GSM/GSMModemType");
    const tchar LABEL_GSM_MODEM_ADDRESS[] = _T("GSM/GSMModemAddress");
    const tchar LABEL_GSM_MODEM_PORT_NUMBER[] = _T("GSM/GSMModemPortNumber");
    const tchar LABEL_GSM_MODEM_LOGIN[] = _T("GSM/GSMModemLogin");
    const tchar LABEL_GSM_MODEM_PASSWORD[] = _T("GSM/GSMModemPassword");


    //Default values:
    const char DEFAULT_CORE_CONFIGURATION_VERSION[] = "4.0.0";
    const unsigned int DEFAULT_MAJOR_CORE_CONFIGURATION_VERSION = 4;
    const char DEFAULT_SITE_IDENTIFIER[] = "0000";
    const char DEFAULT_SSL_SERIAL_NUMBER[] = "0000";

    const uint64_t DEFAULT_INSTATION_SSH_PORT_NUMBER = 22ULL;

    const char DEFAULT_CONFIGURATION_URL[] = "";
    const char DEFAULT_CONFIGURATION_URL_FILE_PREFIX[] = "";
    const char DEFAULT_CONFIGURATION_URL_FILE_SUFFIX[] = "_ini.txt";

    const uint64_t DEFAULT_LAST_USED_BLUETOOTH_DEVICE = 0;
#if defined _WIN32
    const char DEFAULT_DEVICE_DRIVER[] = "WindowsBluetooth";
#elif defined __linux__
    const char DEFAULT_DEVICE_DRIVER[] = "RawHCI";
#else
#error Operating System not supported
#endif

    const char DEFAULT_PARANI_PORT_NAME[] = "/dev/ttyUSB0";
    const long DEFAULT_PARANI_BIT_RATE = 115200;

    const long DEFAULT_HASHING_FUNCTION = 0;

    const unsigned int DEFAULT_INSTATION_RESPONSE_TIMEOUT = 3000;
    const unsigned int DEFAULT_INSTATION_NUMBER_OF_RETRIES = 3;
    const long DEFAULT_INSTATION_WATCHDOG_PERIOD_IN_SECONDS = 1;

    const long DEFAULT_GSM_MODEM_TYPE = Model::ICoreConfiguration::eGSM_MODEM_TYPE_IR791WH01;
    //Default values for GSM IR791WH01 modem
    const char DEFAULT_GSM_MODEM_IR791WH01_ADDRESS[] = "192.168.2.1";
    const long DEFAULT_GSM_MODEM_IR791WH01_PORT_NUMBER = 22;
    const char DEFAULT_GSM_MODEM_IR791WH01_LOGIN[] = "adm";
    const char DEFAULT_GSM_MODEM_IR791WH01_PASSWORD[] = "123456";

    const long DEFAULT_SSH_PORT_NUMBER = 22;

    const char MODULE_NAME[] = "OutStationConfigurationParameters";
};


namespace View
{

OutStationConfigurationParameters* OutStationConfigurationParameters::m_instancePtr = 0;
bool OutStationConfigurationParameters::m_valid = true;

OutStationConfigurationParameters::OutStationConfigurationParameters()
:
ViewConfigurationParameters(),
Model::ICoreConfiguration(),
m_coreConfigurationVersion(),
m_majorCoreConfigurationVersion(),
m_siteIdentifier(),
m_SSLSerialNumber(),
m_instationSSHConnectionAddress(),
m_instationSSHConnectionPort(),
m_instationSSHConnectionLogin(),
m_instationSSHConnectionPassword(),
m_configurationURL(),
m_configurationURL_filePrefix(),
m_configurationURL_fileSuffix(),
m_lastUsedBlueToothDevice(),
m_deviceDriver(),
m_paraniPortName(),
m_paraniBitRate(),
m_instationResponseTimeout(),
m_instationNumberOfRetries(),
m_instationWatchdogPeriodInSeconds(),
m_GSMModemType(),
m_GSMModemConnectionAddress(),
m_GSMModemConnectionPort(),
m_GSMModemConnectionLogin(),
m_GSMModemConnectionPassword()
{
    restoreDefaultValues();
}

bool OutStationConfigurationParameters::construct()
{
    if (m_instancePtr)
    {
        // do nothing
    }
    else
    {
        m_instancePtr = new OutStationConfigurationParameters();
    }

    return m_valid;
}

void OutStationConfigurationParameters::destruct()
{
    delete m_instancePtr;
    m_instancePtr = 0;
}

OutStationConfigurationParameters::~OutStationConfigurationParameters()
{
    //do nothing
}

OutStationConfigurationParameters* OutStationConfigurationParameters::getInstancePtr()
{
    return m_instancePtr;
}

void OutStationConfigurationParameters::restoreDefaultValues()
{
    ViewConfigurationParameters::restoreDefaultValues();

    m_coreConfigurationVersion = DEFAULT_CORE_CONFIGURATION_VERSION;
    m_majorCoreConfigurationVersion = DEFAULT_MAJOR_CORE_CONFIGURATION_VERSION;

    m_siteIdentifier = DEFAULT_SITE_IDENTIFIER;
    m_SSLSerialNumber = DEFAULT_SSL_SERIAL_NUMBER;

    m_instationSSHConnectionAddress.clear();
    m_instationSSHConnectionPort = DEFAULT_INSTATION_SSH_PORT_NUMBER;
    m_instationSSHConnectionLogin.clear();
    m_instationSSHConnectionPassword.clear();

    m_configurationURL = DEFAULT_CONFIGURATION_URL;
    m_configurationURL_filePrefix = DEFAULT_CONFIGURATION_URL_FILE_PREFIX;
    m_configurationURL_fileSuffix = DEFAULT_CONFIGURATION_URL_FILE_SUFFIX;

    m_lastUsedBlueToothDevice = DEFAULT_LAST_USED_BLUETOOTH_DEVICE;
#if defined _WIN32
    m_deviceDriver = "WindowsBluetooth";
#elif defined __linux__
    m_deviceDriver = "NativeBluez";
#else
#error Operating System not supported
#endif

    m_paraniPortName = DEFAULT_PARANI_PORT_NAME;
    m_paraniBitRate = DEFAULT_PARANI_BIT_RATE;

    m_instationResponseTimeout = DEFAULT_INSTATION_RESPONSE_TIMEOUT;
    m_instationNumberOfRetries = DEFAULT_INSTATION_NUMBER_OF_RETRIES;
    m_instationWatchdogPeriodInSeconds = DEFAULT_INSTATION_WATCHDOG_PERIOD_IN_SECONDS;

    m_GSMModemType = DEFAULT_GSM_MODEM_TYPE;
    switch (m_GSMModemType)
    {
        case eGSM_MODEM_TYPE_IR791WH01:
        {
            m_GSMModemConnectionAddress = DEFAULT_GSM_MODEM_IR791WH01_ADDRESS;
            m_GSMModemConnectionPort = DEFAULT_GSM_MODEM_IR791WH01_PORT_NUMBER;
            m_GSMModemConnectionLogin = DEFAULT_GSM_MODEM_IR791WH01_LOGIN;
            m_GSMModemConnectionPassword = DEFAULT_GSM_MODEM_IR791WH01_PASSWORD;
            break;
        }

        case eGSM_MODEM_TYPE_NONE:
        default:
        {
            m_GSMModemConnectionAddress.clear();
            m_GSMModemConnectionPort = 0;
            m_GSMModemConnectionLogin.clear();
            m_GSMModemConnectionPassword.clear();
            break;
        }
    }
}

void OutStationConfigurationParameters::readAllParametersFromFile()
{
    ViewConfigurationParameters::readAllParametersFromFile();

    readStringFromConfigFile(LABEL_CORE_CONFIGURATION_VERSION, m_coreConfigurationVersion, DEFAULT_CORE_CONFIGURATION_VERSION);
    size_t dotPosition = m_coreConfigurationVersion.find_first_of(".");
    if (dotPosition != std::string::npos)
    {
        Utils::stringToUInt(
            m_coreConfigurationVersion.substr(0, dotPosition),
            m_majorCoreConfigurationVersion);
    }
    //else do nothing, error


    readStringFromConfigFile(LABEL_SITE_IDENTIFIER, m_siteIdentifier, DEFAULT_SITE_IDENTIFIER);
    readStringFromConfigFile(LABEL_SSL_SERIAL_NUMBER, m_SSLSerialNumber, DEFAULT_SSL_SERIAL_NUMBER);

    std::string lastUsedBlueToothDeviceAsString;
    readStringFromConfigFile(LABEL_LAST_USED_BLUETOOTH_DEVICE, lastUsedBlueToothDeviceAsString, "");
    wxULongLong_t lastUsedBlueToothDeviceConversionResult = 0;
    wxString::FromAscii(lastUsedBlueToothDeviceAsString.c_str()).ToULongLong(&lastUsedBlueToothDeviceConversionResult);
    m_lastUsedBlueToothDevice = lastUsedBlueToothDeviceConversionResult;

    readStringFromConfigFile(LABEL_INSTATION_SSH_ADDRESS, m_instationSSHConnectionAddress, 0);
    readLongFromConfigFile(LABEL_INSTATION_SSH_PORT_NUMBER, m_instationSSHConnectionPort, DEFAULT_INSTATION_SSH_PORT_NUMBER, 1, USHRT_MAX);
    readStringFromConfigFile(LABEL_INSTATION_SSH_LOGIN, m_instationSSHConnectionLogin, 0);
    readStringFromConfigFile(LABEL_INSTATION_SSH_PASSWORD, m_instationSSHConnectionPassword, 0);

    readStringFromConfigFile(LABEL_CONFIGURATION_URL, m_configurationURL, DEFAULT_CONFIGURATION_URL);
    readStringFromConfigFile(LABEL_CONFIGURATION_URL_FILE_PREFIX, m_configurationURL_filePrefix, DEFAULT_CONFIGURATION_URL_FILE_PREFIX);
    readStringFromConfigFile(LABEL_CONFIGURATION_URL_FILE_SUFFIX, m_configurationURL_fileSuffix, DEFAULT_CONFIGURATION_URL_FILE_SUFFIX);

    readStringFromConfigFile(LABEL_DEVICE_DRIVER, m_deviceDriver, DEFAULT_DEVICE_DRIVER);
    readStringFromConfigFile(LABEL_PARANI_PORT_NAME, m_paraniPortName, DEFAULT_PARANI_PORT_NAME);
    readLongFromConfigFile(LABEL_PARANI_BIT_RATE, m_paraniBitRate, DEFAULT_PARANI_BIT_RATE, 9600, 115200);

    readLongFromConfigFile(LABEL_INSTATION_RESPONSE_TIMEOUT, m_instationResponseTimeout, DEFAULT_INSTATION_RESPONSE_TIMEOUT, 1, 0x10000);
    readLongFromConfigFile(LABEL_INSTATION_NUMBER_OF_RETRIES, m_instationNumberOfRetries, DEFAULT_INSTATION_NUMBER_OF_RETRIES, 0, 65535);
    readLongFromConfigFile(LABEL_INSTATION_WATCHDOG_PERIOD_IN_SECONDS, m_instationWatchdogPeriodInSeconds, DEFAULT_INSTATION_WATCHDOG_PERIOD_IN_SECONDS, 0, 2*60);

    readLongFromConfigFile(LABEL_GSM_MODEM_TYPE, m_GSMModemType, DEFAULT_GSM_MODEM_TYPE, 0, 1);
    readStringFromConfigFile(LABEL_GSM_MODEM_ADDRESS, m_GSMModemConnectionAddress, DEFAULT_GSM_MODEM_IR791WH01_ADDRESS);
    readLongFromConfigFile(LABEL_GSM_MODEM_PORT_NUMBER, m_GSMModemConnectionPort, DEFAULT_GSM_MODEM_IR791WH01_PORT_NUMBER, 0, USHRT_MAX);
    readStringFromConfigFile(LABEL_GSM_MODEM_LOGIN, m_GSMModemConnectionLogin, DEFAULT_GSM_MODEM_IR791WH01_LOGIN);
    readStringFromConfigFile(LABEL_GSM_MODEM_PASSWORD, m_GSMModemConnectionPassword, DEFAULT_GSM_MODEM_IR791WH01_PASSWORD);

    m_configPtr->Flush();
}

bool OutStationConfigurationParameters::setSSLSerialNumber(const std::string& value)
{
    m_SSLSerialNumber = value;
    bool result = m_configPtr->Write(LABEL_SSL_SERIAL_NUMBER, wxString::FromAscii(m_SSLSerialNumber.c_str()));
    m_configPtr->Flush();
    return result;
}

bool OutStationConfigurationParameters::setSiteIdentifier(const std::string& value)
{
    m_siteIdentifier = value;
    bool result = m_configPtr->Write(LABEL_SITE_IDENTIFIER, wxString::FromAscii(m_siteIdentifier.c_str()));
    m_configPtr->Flush();
    return result;
}

std::string OutStationConfigurationParameters::getCoreConfigurationVersion() const
{
    return m_coreConfigurationVersion;
}

unsigned int OutStationConfigurationParameters::getMajorCoreConfigurationVersion() const
{
    return m_majorCoreConfigurationVersion;
}

std::string OutStationConfigurationParameters::getSiteIdentifier() const
{
    return m_siteIdentifier;
}

std::string OutStationConfigurationParameters::getSSLSerialNumber() const
{
    return m_SSLSerialNumber;
}

std::string OutStationConfigurationParameters::getInstationSSHConnectionAddress() const
{
    return m_instationSSHConnectionAddress;
}

uint64_t OutStationConfigurationParameters::getInstationSSHConnectionPort() const
{
    assert(m_instationSSHConnectionPort <= USHRT_MAX);

    return m_instationSSHConnectionPort;
}

std::string OutStationConfigurationParameters::getInstationSSHConnectionLogin() const
{
    return m_instationSSHConnectionLogin;
}

std::string OutStationConfigurationParameters::getInstationSSHConnectionPassword() const
{
    return m_instationSSHConnectionPassword;
}

bool OutStationConfigurationParameters::setLastUsedBlueToothDevice(const uint64_t value)
{
    m_lastUsedBlueToothDevice = value;
    wxString valueAsString;
    valueAsString << value;
    bool result = m_configPtr->Write(LABEL_LAST_USED_BLUETOOTH_DEVICE, valueAsString);
    m_configPtr->Flush();
    return result;
}

uint64_t OutStationConfigurationParameters::getLastUsedBlueToothDevice() const
{
     return m_lastUsedBlueToothDevice;
}

bool OutStationConfigurationParameters::setDeviceDriver(const std::string& value)
{
    m_deviceDriver = value;
    bool result = m_configPtr->Write(LABEL_DEVICE_DRIVER, wxString::FromAscii(m_deviceDriver.c_str()));
    m_configPtr->Flush();
    return result;
}

bool OutStationConfigurationParameters::setConfigurationURL(const std::string& value)
{
    m_configurationURL = value;
    bool result = m_configPtr->Write(LABEL_CONFIGURATION_URL, wxString::FromAscii(m_configurationURL.c_str()));
    m_configPtr->Flush();
    return result;
}

std::string OutStationConfigurationParameters::getDeviceDriver() const
{
    return m_deviceDriver;
}

std::string OutStationConfigurationParameters::getParaniPortName() const
{
    return m_paraniPortName;
}

bool OutStationConfigurationParameters::setParaniPortName(const std::string& value)
{
    m_paraniPortName = value;
    bool result = m_configPtr->Write(LABEL_PARANI_PORT_NAME, wxString::FromAscii(m_paraniPortName.c_str()));
    m_configPtr->Flush();
    return result;
}

uint64_t OutStationConfigurationParameters::getParaniBitRate() const
{
    return m_paraniBitRate;
}

bool OutStationConfigurationParameters::setParaniBitRate(const uint64_t value)
{
    m_paraniBitRate = value;
    wxString valueAsString;
    valueAsString << value;
    bool result = m_configPtr->Write(LABEL_PARANI_BIT_RATE, valueAsString);
    m_configPtr->Flush();
    return result;
}

std::string OutStationConfigurationParameters::getConfigurationURL() const
{
    return m_configurationURL;
}

std::string OutStationConfigurationParameters::getConfigurationURL_filePrefix() const
{
    return m_configurationURL_filePrefix;
}

std::string OutStationConfigurationParameters::getConfigurationURL_fileSuffix() const
{
    return m_configurationURL_fileSuffix;
}

uint64_t OutStationConfigurationParameters::getGSMModemType() const
{
    return m_GSMModemType;
}

std::string OutStationConfigurationParameters::getGSMModemConnectionAddress() const
{
    return m_GSMModemConnectionAddress;
}

uint64_t OutStationConfigurationParameters::getGSMModemConnectionPort() const
{
    return m_GSMModemConnectionPort;
}

std::string OutStationConfigurationParameters::getGSMModemConnectionLogin() const
{
    return m_GSMModemConnectionLogin;
}

std::string OutStationConfigurationParameters::getGSMModemConnectionPassword() const
{
    return m_GSMModemConnectionPassword;
}

} //namespace
