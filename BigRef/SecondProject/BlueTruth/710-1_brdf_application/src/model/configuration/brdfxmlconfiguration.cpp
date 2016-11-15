#include "stdafx.h"
#include "brdfxmlconfiguration.h"

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
    const long DEFAULT_MAX_NUMBER_OF_ROWS_IN_EVENT_LOG_GRID = 1000;

    const char DEFAULT_VERSION[] = "1.0";

    const char DEFAULT_DATABASE_HOST[] = "localhost";
    const uint64_t DEFAULT_DATABASE_PORT = 27017; //mongoDB default port


    const char CONFIGURATION_FILE_NAME[] = "brdfconfiguration.xml";

    const char MODULE_NAME[] = "BrdfXmlConfiguration";
};


namespace Model
{

BrdfXmlConfiguration::BrdfXmlConfiguration()
:
IBrdfXmlConfiguration(),
XMLConfiguration(),
m_configurationErrorReported(false),
m_configurationVersion(),
m_detectorOwner(),
m_databaseHost(),
m_databasePort(0),
m_databaseUser(),
m_databasePassword(),
m_fileLogLevel(0),
m_consoleLogLevel(0),
m_logMaxNumberOfEntriesPerFile(0),
m_logMaxNumberOfCharactersPerFile(0)
{
    restoreDefaultValues();
}

BrdfXmlConfiguration::~BrdfXmlConfiguration()
{
    //do nothing
}

void BrdfXmlConfiguration::restoreDefaultValues()
{
    m_configurationVersion = DEFAULT_VERSION;
    m_detectorOwner.clear();

    m_databaseHost = DEFAULT_DATABASE_HOST;
    m_databasePort = DEFAULT_DATABASE_PORT;

    m_fileLogLevel = DEFAULT_LOG_LEVEL;
    m_consoleLogLevel = DEFAULT_LOG_LEVEL;
    m_logMaxNumberOfEntriesPerFile = DEFAULT_MAX_NUMBER_OF_ENTRIES_PER_FILE;
    m_logMaxNumberOfCharactersPerFile = DEFAULT_MAX_NUMBER_OF_CHARACTERS_PER_FILE;
}

bool BrdfXmlConfiguration::readAllParametersFromFile(const char* fileName)
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
        fullFileName += OS_Utilities::StringToAnsi(Brdf::ApplicationConfiguration::getSysConfDirectory());
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
            oss << "Configuration file " << fullFileName << " has been loaded";
            Logger::log(LOG_LEVEL_INFO, oss.str().c_str());
        }
        else
        {
            std::ostringstream oss;
            oss << "Failed to process XML configuration file " << fullFileName;
            Logger::log(LOG_LEVEL_ERROR, oss.str().c_str());
        }
    }
    else
    {
        std::ostringstream oss;
        oss << "Failed to load configuration file \"" << fullFileName << "\"";
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

bool BrdfXmlConfiguration::readAllParametersFromString(const char* str)
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
            oss << "Configuration has been loaded";
            Logger::log(LOG_LEVEL_INFO, oss.str().c_str());
        }
        else
        {
            std::ostringstream oss;
            oss << "Failed to process XML configuration";
            Logger::log(LOG_LEVEL_ERROR, oss.str().c_str());
        }
    }
    else
    {
        std::ostringstream oss;
        oss << "Failed to load configuration";
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

bool BrdfXmlConfiguration::parseTiXmlDocument(TiXmlDocument& doc)
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
        Logger::log(LOG_LEVEL_ERROR, "Error in XML configuration file");
    }

    return result;
}

std::string BrdfXmlConfiguration::getConfigurationVersion() const
{
    return m_configurationVersion;
}

std::string BrdfXmlConfiguration::getDetectorOwner() const
{
    return m_detectorOwner;
}

std::string BrdfXmlConfiguration::getDatabaseHost() const
{
    return m_databaseHost;
}

uint64_t BrdfXmlConfiguration::getDatabasePort() const
{
    assert(m_databasePort <= USHRT_MAX);

    return m_databasePort;
}

std::string BrdfXmlConfiguration::getDatabaseUser() const
{
    return m_databaseUser;
}

std::string BrdfXmlConfiguration::getDatabasePassword() const
{
    return m_databasePassword;
}

bool BrdfXmlConfiguration::parseTiXmlElement(TiXmlNode* pParent)
{
    bool result = true;

    assert(pParent != 0);

    if (pParent->ValueTStr() == "brdfConfiguration:BrdfConfiguration")
    {
        for (TiXmlNode* pHeaderChild = pParent->FirstChild(); pHeaderChild != 0; pHeaderChild = pHeaderChild->NextSibling())
        {
            const int parentType = pParent->Type();
            if (parentType == TiXmlNode::TINYXML_ELEMENT)
            {
                const std::string ELEMENT_NAME (pHeaderChild->ValueTStr().c_str());
                if (ELEMENT_NAME == "version")
                {
                    const std::string ELEMENT_VALUE(pHeaderChild->FirstChild()->ValueTStr().c_str());
                    m_configurationVersion = ELEMENT_VALUE;
                }
                else if (ELEMENT_NAME == "detectorOwner")
                {
                    const std::string ELEMENT_VALUE(pHeaderChild->FirstChild()->ValueTStr().c_str());
                    m_detectorOwner = ELEMENT_VALUE;
                }
                else if (ELEMENT_NAME == "databaseConnection")
                {
                    for (TiXmlNode* pChild = pHeaderChild->FirstChild(); pChild != 0; pChild = pChild->NextSibling())
                    {
                        const int childType = pChild->Type();
                        if (childType == TiXmlNode::TINYXML_ELEMENT)
                        {
                            const std::string SUBELEMENT_NAME(pChild->ValueTStr().c_str());
                            if (
                                (checkStringElement(SUBELEMENT_NAME, "host", pChild, m_databaseHost)) ||
                                (checkStringElement(SUBELEMENT_NAME, "user", pChild, m_databaseUser)) ||
                                (checkStringElement(SUBELEMENT_NAME, "password", pChild, m_databasePassword))
                                )
                            {
                                //do nothing
                            }
                            else if (checkUIntElement(SUBELEMENT_NAME, "port", pChild, m_databasePort, result))
                            {
                                result = result && (m_databasePort <= USHRT_MAX);
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
                else if (ELEMENT_NAME == "logging")
                {
                    for (TiXmlNode* pChild = pHeaderChild->FirstChild(); pChild != 0; pChild = pChild->NextSibling())
                    {
                        const int childType = pChild->Type();
                        if (childType == TiXmlNode::TINYXML_ELEMENT)
                        {
                            const std::string SUBELEMENT_NAME(pChild->ValueTStr().c_str());
                            if (
                                checkUIntElement(SUBELEMENT_NAME, "fileLogLevel", pChild, m_fileLogLevel, result) ||
                                checkUIntElement(SUBELEMENT_NAME, "consoleLogLevel", pChild, m_consoleLogLevel, result) ||
                                checkUIntElement(SUBELEMENT_NAME, "maxNumberOfEntriesPerFile", pChild, m_logMaxNumberOfEntriesPerFile, result) ||
                                checkUIntElement(SUBELEMENT_NAME, "maxNumberOfCharactersPerFile", pChild, m_logMaxNumberOfCharactersPerFile, result)
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
