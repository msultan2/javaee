#include "stdafx.h"
#include "coreconfiguration.h"

#include "applicationconfiguration.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"

#include <climits>


namespace
{
    //Default values:
    const long DEFAULT_LOG_LEVEL = LOG_LEVEL_DEBUG2;
    const long DEFAULT_MAX_NUMBER_OF_ENTRIES_PER_FILE = 100000;
    const long DEFAULT_MAX_NUMBER_OF_CHARACTERS_PER_FILE = std::numeric_limits<long>::max();
    const long DEFAULT_MAX_LOG_FILE_AGE_IN_SECONDS = 60*60*24*7; //7 days
    const long DEFAULT_MAX_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID = 1000;

    const char DEFAULT_VERSION[] = "4.0.0";
    const unsigned int DEFAULT_MAJOR_VERSION = 4;
    const char DEFAULT_SITE_IDENTIFIER[] = "0000";
    const char DEFAULT_SSL_SERIAL_NUMBER[] = "0000";
    const uint64_t DEFAULT_SSH_PORT_NUMBER = 22ULL;
    const uint64_t DEFAULT_LAST_USED_BLUETOOTH_DEVICE = 0;

    const char DEFAULT_PARANI_PORT_NAME[] = "/dev/ttyUSB0";
    const uint64_t DEFAULT_PARANI_BIT_RATE = 115200;

    //const char DEFAULT_CONFIGURATION_URL[] = "http://37.152.43.178/DetectorConfigurationDownload/2_00/";
    const char DEFAULT_CONFIGURATION_URL[] = "";
    const char DEFAULT_CONFIGURATION_URL_FILE_PREFIX[] = "";
    //const char DEFAULT_CONFIGURATION_URL_FILE_SUFFIX[] = "_ini.txt";
    const char DEFAULT_CONFIGURATION_URL_FILE_SUFFIX[] = "";
    const unsigned int DEFAULT_INSTATION_RESPONSE_TIMEOUT = 3000;
    const unsigned int DEFAULT_INSTATION_NUMBER_OF_RETRIES = 3;
    const long DEFAULT_INSTATION_WATCHDOG_PERIOD_IN_SECONDS = 1;

    const long DEFAULT_GSM_MODEM_TYPE = Model::ICoreConfiguration::eGSM_MODEM_TYPE_NONE;
    //Default values for GSM IR791WH01 modem
    const char DEFAULT_GSM_MODEM_IR791WH01_SSH_ADDRESS[] = "192.168.2.1";
    const long DEFAULT_GSM_MODEM_PORT_NUMBER = 23LL;
    const char DEFAULT_GSM_MODEM_IR791WH01_SSH_LOGIN[] = "adm";
    const char DEFAULT_GSM_MODEM_IR791WH01_SSH_PASSWORD[] = "123456";

    const char CONFIGURATION_FILE_NAME[] = "core_configuration.xml";

    const char MODULE_NAME[] = "CoreConfiguration";
};


namespace Model
{

CoreConfiguration::CoreConfiguration()
:
ICoreConfiguration(),
XMLConfiguration(),
m_configurationErrorReported(false),
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
m_fileLogLevel(),
m_consoleLogLevel(),
m_logMaxNumberOfEntriesPerFile(),
m_logMaxNumberOfCharactersPerFile(),
m_maximumLogFileAgeInSeconds(),
m_instationResponseTimeout(),
m_instationNumberOfRetries(),
m_instationWatchdogPeriodInSeconds()
{
    restoreDefaultValues();
}

CoreConfiguration::~CoreConfiguration()
{
    //do nothing
}

void CoreConfiguration::restoreDefaultValues()
{
    m_coreConfigurationVersion = DEFAULT_VERSION;
    m_majorCoreConfigurationVersion = DEFAULT_MAJOR_VERSION;
    m_siteIdentifier = DEFAULT_SITE_IDENTIFIER;
    m_SSLSerialNumber = DEFAULT_SSL_SERIAL_NUMBER;

    m_instationSSHConnectionAddress.clear();
    m_instationSSHConnectionPort = DEFAULT_SSH_PORT_NUMBER;
    m_instationSSHConnectionLogin.clear();
    m_instationSSHConnectionPassword.clear();

    m_configurationURL = DEFAULT_CONFIGURATION_URL;
    m_configurationURL_filePrefix = DEFAULT_CONFIGURATION_URL_FILE_PREFIX;
    m_configurationURL_fileSuffix = DEFAULT_CONFIGURATION_URL_FILE_SUFFIX;

    m_lastUsedBlueToothDevice = DEFAULT_LAST_USED_BLUETOOTH_DEVICE;
    m_deviceDriver.clear();
    m_paraniPortName.clear();
    m_paraniBitRate = 0;

    m_fileLogLevel = DEFAULT_LOG_LEVEL;
    m_consoleLogLevel = DEFAULT_LOG_LEVEL;
    m_logMaxNumberOfEntriesPerFile = DEFAULT_MAX_NUMBER_OF_ENTRIES_PER_FILE;
    m_logMaxNumberOfCharactersPerFile = DEFAULT_MAX_NUMBER_OF_CHARACTERS_PER_FILE;
    m_maximumLogFileAgeInSeconds = DEFAULT_MAX_LOG_FILE_AGE_IN_SECONDS;

    m_instationResponseTimeout = DEFAULT_INSTATION_RESPONSE_TIMEOUT;
    m_instationNumberOfRetries = DEFAULT_INSTATION_NUMBER_OF_RETRIES;
    m_instationWatchdogPeriodInSeconds = DEFAULT_INSTATION_WATCHDOG_PERIOD_IN_SECONDS;

    m_GSMModemType = DEFAULT_GSM_MODEM_TYPE;
    m_GSMModemConnectionAddress.clear();
    m_GSMModemConnectionPort = DEFAULT_GSM_MODEM_PORT_NUMBER;
    m_GSMModemConnectionLogin.clear();
    m_GSMModemConnectionPassword.clear();
}

bool CoreConfiguration::readAllParametersFromFile(const char* fileName)
{
    bool result = false;

    //Derive configuration file name from application configuration
    std::string fullFileName;

    if (fileName != 0)
    {
        fullFileName = fileName;
    }
    else
    {
        fullFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getSysConfDirectory());
#ifdef _WIN32
        if (fullFileName == ".\\")
        {
            fullFileName = "..\\misc\\";
        }
        //else do nothing
#else
        //TODO Fix path for the case of running from local directory
#endif
        fullFileName += CONFIGURATION_FILE_NAME;
    }

    TiXmlDocument doc;
    if (doc.LoadFile(fullFileName.c_str()))
    {
        result = parseTiXmlDocument(doc);
        if (result)
        {
            std::ostringstream oss;
            oss << "Core configuration file " << fullFileName << " has been loaded";
            Logger::log(LOG_LEVEL_INFO, oss.str().c_str());
        }
        else
        {
            std::ostringstream oss;
            oss << "Failed to process XML core configuration file " << fullFileName;
            Logger::log(LOG_LEVEL_ERROR, oss.str().c_str());
        }
    }
    else
    {
        std::ostringstream oss;
        oss << "Failed to load core configuration file \"" << fullFileName << "\"";
        if (doc.ErrorDesc() != 0)
        {
            oss << " (" << doc.ErrorDesc();

            if ((doc.ErrorRow() >= 0) && (doc.ErrorRow() >= 0))
            {
                oss << "; Line:" << doc.ErrorRow() << ", Column:" << doc.ErrorCol();
            }
            //else do nothing

            oss << ")";
        }
        //else do nothing

        Logger::log(LOG_LEVEL_ERROR, oss.str().c_str());
    }

    return result;
}

bool CoreConfiguration::readAllParametersFromString(const char* str)
{
    bool result = false;

    TiXmlDocument doc;
    doc.Parse(str);
    if (doc.ErrorId() == 0)
    {
        result = parseTiXmlDocument(doc);
        if (result)
        {
            std::ostringstream oss;
            oss << "Core configuration has been loaded";
            Logger::log(LOG_LEVEL_INFO, oss.str().c_str());
        }
        else
        {
            std::ostringstream oss;
            oss << "Failed to process XML core configuration";
            Logger::log(LOG_LEVEL_ERROR, oss.str().c_str());
        }
    }
    else
    {
        std::ostringstream oss;
        oss << "Failed to load core configuration";
        if (doc.ErrorDesc() != 0)
        {
            oss << " (" << doc.ErrorDesc();

            if ((doc.ErrorRow() >= 0) && (doc.ErrorRow() >= 0))
            {
                oss << "; Line:" << doc.ErrorRow() << ", Column:" << doc.ErrorCol();
            }
            //else do nothing

            oss << ")";
        }
        //else do nothing

        Logger::log(LOG_LEVEL_ERROR, oss.str().c_str());
    }
    //else do nothing

    return result;
}

bool CoreConfiguration::parseTiXmlDocument(TiXmlDocument& doc)
{
    bool result = true;

    //Parse document
    if (doc.Type() == TiXmlNode::TINYXML_DOCUMENT)
    {
        for (TiXmlNode* pParent = doc.FirstChild(); pParent != 0; pParent = pParent->NextSibling())
        {
            switch (pParent->Type())
            {
                case TiXmlNode::TINYXML_ELEMENT:
                    result = parseTiXmlElement(pParent);
                    break;

                case TiXmlNode::TINYXML_DECLARATION:
                case TiXmlNode::TINYXML_COMMENT:
                    break;

                default:
                    result = false;
                    break;
            }
        }
    }
    else
    {
        Logger::log(LOG_LEVEL_ERROR, "Error in XML core configuration file");
    }

    return result;
}

std::string CoreConfiguration::getCoreConfigurationVersion() const
{
    return m_coreConfigurationVersion;
}

unsigned int CoreConfiguration::getMajorCoreConfigurationVersion() const
{
    return m_majorCoreConfigurationVersion;
}

std::string CoreConfiguration::getSiteIdentifier() const
{
    return m_siteIdentifier;
}

std::string CoreConfiguration::getSSLSerialNumber() const
{
    return m_SSLSerialNumber;
}

std::string CoreConfiguration::getInstationSSHConnectionAddress() const
{
    return m_instationSSHConnectionAddress;
}

uint64_t CoreConfiguration::getInstationSSHConnectionPort() const
{
    assert(m_instationSSHConnectionPort <= USHRT_MAX);

    return m_instationSSHConnectionPort;
}

std::string CoreConfiguration::getInstationSSHConnectionLogin() const
{
    return m_instationSSHConnectionLogin;
}

std::string CoreConfiguration::getInstationSSHConnectionPassword() const
{
    return m_instationSSHConnectionPassword;
}

std::string CoreConfiguration::getConfigurationURL() const
{
    return m_configurationURL;
}

std::string CoreConfiguration::getConfigurationURL_filePrefix() const
{
    return m_configurationURL_filePrefix;
}

std::string CoreConfiguration::getConfigurationURL_fileSuffix() const
{
    return m_configurationURL_fileSuffix;
}

std::string CoreConfiguration::getCommandLineStatusReportURL() const
{
    return m_commandLineStatusReportURL;
}

void CoreConfiguration::setCommandLineStatusReportURL(const std::string& commandLineStatusReportURL)
{
    m_commandLineStatusReportURL = commandLineStatusReportURL;
}

uint64_t CoreConfiguration::getLastUsedBlueToothDevice() const
{
    return m_lastUsedBlueToothDevice;
}

bool CoreConfiguration::setLastUsedBlueToothDevice(const uint64_t value)
{
    m_lastUsedBlueToothDevice = value;
    return true;
}

std::string CoreConfiguration::getDeviceDriver() const
{
    return m_deviceDriver;
}

std::string CoreConfiguration::getParaniPortName() const
{
    return m_paraniPortName;
}

uint64_t CoreConfiguration::getParaniBitRate() const
{
    return m_paraniBitRate;
}

uint64_t CoreConfiguration::getGSMModemType() const
{
    return m_GSMModemType;
}

std::string CoreConfiguration::getGSMModemConnectionAddress() const
{
    return m_GSMModemConnectionAddress;
}

uint64_t CoreConfiguration::getGSMModemConnectionPort() const
{
    return m_GSMModemConnectionPort;
}

std::string CoreConfiguration::getGSMModemConnectionLogin() const
{
    return m_GSMModemConnectionLogin;
}

std::string CoreConfiguration::getGSMModemConnectionPassword() const
{
    return m_GSMModemConnectionPassword;
}

bool CoreConfiguration::parseTiXmlElement(TiXmlNode* pParent)
{
    bool result = true;

    assert(pParent != 0);

    if (pParent->ValueTStr() == "coreConfiguration:CoreConfiguration")
    {
        for (TiXmlNode* pHeaderChild = pParent->FirstChild(); pHeaderChild != 0; pHeaderChild = pHeaderChild->NextSibling())
        {
            const int parentType = pParent->Type();
            if (parentType == TiXmlNode::TINYXML_ELEMENT)
            {
                const std::string ELEMENT_NAME (pHeaderChild->ValueTStr().c_str());
                if (ELEMENT_NAME == "Version")
                {
                    const std::string ELEMENT_VALUE(pHeaderChild->FirstChild()->ValueTStr().c_str());
                    m_coreConfigurationVersion = ELEMENT_VALUE;

                    //Extract major version part
                    size_t dotPosition = m_coreConfigurationVersion.find_first_of(".");
                    if (dotPosition != std::string::npos)
                    {
                        Utils::stringToUInt(
                            m_coreConfigurationVersion.substr(0, dotPosition),
                            m_majorCoreConfigurationVersion);
                    }
                    //else do nothing, error
                }
                else if (ELEMENT_NAME == "Identity")
                {
                    for (TiXmlNode* pChild = pHeaderChild->FirstChild(); pChild != 0; pChild = pChild->NextSibling())
                    {
                        const int childType = pChild->Type();
                        if (childType == TiXmlNode::TINYXML_ELEMENT)
                        {
                            const std::string SUBELEMENT_NAME(pChild->ValueTStr().c_str());
                            if (
                                (checkStringElement(SUBELEMENT_NAME, "SiteIdentifier", pChild, m_siteIdentifier)) ||
                                (checkStringElement(SUBELEMENT_NAME, "SerialNumber", pChild, m_SSLSerialNumber))
                                )
                            {
                                //do nothing
                            }
                            else
                            {
                                result = false;

                                std::ostringstream ss;
                                ss << "Invalid element \"" << SUBELEMENT_NAME << "\" in \"" << ELEMENT_NAME << "\" group";
                                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                            }
                        }
                        else
                        {
                            result = false;
                        }
                    }
                }
                else if (ELEMENT_NAME == "InStationSSHConnection")
                {
                    for (TiXmlNode* pChild = pHeaderChild->FirstChild(); pChild != 0; pChild = pChild->NextSibling())
                    {
                        const int childType = pChild->Type();
                        if (childType == TiXmlNode::TINYXML_ELEMENT)
                        {
                            const std::string SUBELEMENT_NAME(pChild->ValueTStr().c_str());
                            if (
                                checkStringElement(SUBELEMENT_NAME, "Address", pChild, m_instationSSHConnectionAddress) ||
                                checkStringElement(SUBELEMENT_NAME, "Login", pChild, m_instationSSHConnectionLogin) ||
                                checkStringElement(SUBELEMENT_NAME, "Password", pChild, m_instationSSHConnectionPassword)
                                )
                            {
                                //do nothing
                            }
                            else if (checkUIntElement(SUBELEMENT_NAME, "Port", pChild, m_instationSSHConnectionPort, result))
                            {
                                result = result && (m_instationSSHConnectionPort <= USHRT_MAX);
                            }
                            else
                            {
                                result = false;

                                std::ostringstream ss;
                                ss << "Invalid element \"" << SUBELEMENT_NAME << "\" in \"" << ELEMENT_NAME << "\" group";
                                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                            }
                        }
                        else
                        {
                            result = false;
                        }
                    }
                }
                else if (ELEMENT_NAME == "IniConfigurationURL")
                {
                    for (TiXmlNode* pChild = pHeaderChild->FirstChild(); pChild != 0; pChild = pChild->NextSibling())
                    {
                        const int childType = pChild->Type();
                        if (childType == TiXmlNode::TINYXML_ELEMENT)
                        {
                            const std::string SUBELEMENT_NAME(pChild->ValueTStr().c_str());
                            if (
                                checkStringElement(SUBELEMENT_NAME, "Path", pChild, m_configurationURL) ||
                                checkStringElement(SUBELEMENT_NAME, "File_Prefix", pChild, m_configurationURL_filePrefix) ||
                                checkStringElement(SUBELEMENT_NAME, "File_Suffix", pChild, m_configurationURL_fileSuffix)
                                )
                            {
                                //do nothing
                            }
                            else
                            {
                                result = false;

                                std::ostringstream ss;
                                ss << "Invalid element \"" << SUBELEMENT_NAME << "\" in \"" << ELEMENT_NAME << "\" group";
                                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                            }
                        }
                        else
                        {
                            result = false;
                        }
                    }
                }
                else if (ELEMENT_NAME == "BlueToothDevice")
                {
                    for (TiXmlNode* pChild = pHeaderChild->FirstChild(); pChild != 0; pChild = pChild->NextSibling())
                    {
                        const int childType = pChild->Type();
                        const std::string SUBELEMENT_NAME(pChild->ValueTStr().c_str());
                        if (childType == TiXmlNode::TINYXML_ELEMENT)
                        {
                            if (SUBELEMENT_NAME == "MAC_AddressOfDeviceToBeUsed")
                            {
                                if (pChild->FirstChild() != 0)
                                {
                                    m_lastUsedBlueToothDevice = 0;
                                    const std::string SUBELEMENT_VALUE(pChild->FirstChild()->ValueTStr().c_str());
                                    result = result && Utils::convertStringToMACAddress(SUBELEMENT_VALUE, m_lastUsedBlueToothDevice);
                                }
                                else
                                {
                                    result = false;

                                    std::ostringstream ss;
                                    ss << "Invalid element \"" << SUBELEMENT_NAME << "\" in \"" << ELEMENT_NAME << "\" group";
                                    Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                                }
                            }
                            else if (SUBELEMENT_NAME == "Driver")
                            {
                                TiXmlNode* pGrandChild = pChild->FirstChild();
                                if (pGrandChild != 0)
                                {
                                    const int grandChildType = pGrandChild->Type();
                                    const std::string SUBSUBELEMENT_NAME(pGrandChild->ValueTStr().c_str());
                                    if (grandChildType == TiXmlNode::TINYXML_ELEMENT)
                                    {
                                        if (
                                            (SUBSUBELEMENT_NAME == "WindowsBluetooth") ||
                                            (SUBSUBELEMENT_NAME == "WindowsWSA") ||
                                            (SUBSUBELEMENT_NAME == "NativeBluez") ||
                                            (SUBSUBELEMENT_NAME == "RawHCI")
                                            )
                                        {
                                            m_deviceDriver = SUBSUBELEMENT_NAME;
                                        }
                                        else if (SUBSUBELEMENT_NAME == "Parani")
                                        {
                                            m_deviceDriver = SUBSUBELEMENT_NAME;
                                            m_paraniPortName = DEFAULT_PARANI_PORT_NAME;
                                            m_paraniBitRate = DEFAULT_PARANI_BIT_RATE;

                                            //Try to extract attributes for Parani driver (port name and bit rate)
                                            for (TiXmlAttribute* pAttrib = pGrandChild->ToElement()->FirstAttribute(); pAttrib != 0; pAttrib = pAttrib->Next())
                                            {
                                                if ((pAttrib != 0) && (pAttrib->NameTStr() == "PortName"))
                                                {
                                                    m_paraniPortName = pAttrib->Value();
                                                }
                                                else if ((pAttrib != 0) && (pAttrib->NameTStr() == "BitRate"))
                                                {
                                                    const std::string BIT_RATE_VALUE(pAttrib->Value());
                                                    result = result && Utils::stringToUInt64(BIT_RATE_VALUE, m_paraniBitRate);
                                                }
                                                else
                                                {
                                                    //do nothing, unknown attribute
                                                }
                                            }
                                        }
                                        else
                                        {
                                            result = false;

                                            std::ostringstream ss;
                                            ss << "Invalid element \"" << SUBELEMENT_NAME << "\" in \"" << ELEMENT_NAME << "\" group";
                                            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                                        }
                                    }
                                }
                                //else do nothing (leave empty)
                            }
                            else
                            {
                                result = false;
                            }
                        }
                        else
                        {
                            result = false;
                        }
                    }
                }
                else if (ELEMENT_NAME == "GSMModemConnection")
                {
                    for (TiXmlNode* pChild = pHeaderChild->FirstChild(); pChild != 0; pChild = pChild->NextSibling())
                    {
                        const int childType = pChild->Type();
                        if (childType == TiXmlNode::TINYXML_ELEMENT)
                        {
                            const std::string SUBELEMENT_NAME(pChild->ValueTStr().c_str());
                            if (checkUIntElement(SUBELEMENT_NAME, "Type", pChild, m_GSMModemType, result))
                            {
                                result = result && (m_GSMModemConnectionPort <= USHRT_MAX);
                            }
                            else if (
                                checkStringElement(SUBELEMENT_NAME, "Address", pChild, m_GSMModemConnectionAddress) ||
                                checkStringElement(SUBELEMENT_NAME, "Login", pChild, m_GSMModemConnectionLogin) ||
                                checkStringElement(SUBELEMENT_NAME, "Password", pChild, m_GSMModemConnectionPassword)
                                )
                            {
                                //do nothing
                            }
                            else if (checkUIntElement(SUBELEMENT_NAME, "Port", pChild, m_GSMModemConnectionPort, result))
                            {
                                result = result && (m_GSMModemConnectionPort <= USHRT_MAX);
                            }
                            else
                            {
                                result = false;

                                std::ostringstream ss;
                                ss << "Invalid element \"" << SUBELEMENT_NAME << "\" in \"" << ELEMENT_NAME << "\" group";
                                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                            }
                        }
                        else
                        {
                            result = false;
                        }
                    }
                }
                else if (ELEMENT_NAME == "Logging")
                {
                    for (TiXmlNode* pChild = pHeaderChild->FirstChild(); pChild != 0; pChild = pChild->NextSibling())
                    {
                        const int childType = pChild->Type();
                        if (childType == TiXmlNode::TINYXML_ELEMENT)
                        {
                            const std::string SUBELEMENT_NAME(pChild->ValueTStr().c_str());
                            if (
                                checkUIntElement(SUBELEMENT_NAME, "FileLogLevel", pChild, m_fileLogLevel, result) ||
                                checkUIntElement(SUBELEMENT_NAME, "ConsoleLogLevel", pChild, m_consoleLogLevel, result) ||
                                checkUIntElement(SUBELEMENT_NAME, "MaxNumberOfEntriesPerFile", pChild, m_logMaxNumberOfEntriesPerFile, result) ||
                                checkUIntElement(SUBELEMENT_NAME, "MaxNumberOfCharactersPerFile", pChild, m_logMaxNumberOfCharactersPerFile, result) ||
                                checkUIntElement(SUBELEMENT_NAME, "MaxLogFileAgeInSeconds", pChild, m_maximumLogFileAgeInSeconds, result)
                                )
                            {
                                //do nothing
                            }
                            else
                            {
                                result = false;

                                std::ostringstream ss;
                                ss << "Invalid element \"" << SUBELEMENT_NAME << "\" in \"" << ELEMENT_NAME << "\" group";
                                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
                            }
                        }
                        else
                        {
                            result = false;
                        }
                    } //for
                }
                else
                {
                    result = false;
                }
            }
            else
            {
                result = false;
            }
        } //for
    }
    else
    {
        result = false;
    }

    return result;
}

} //namespace
