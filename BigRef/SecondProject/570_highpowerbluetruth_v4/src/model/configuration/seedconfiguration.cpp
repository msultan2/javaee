#include "stdafx.h"
#include "seedconfiguration.h"

#include "applicationconfiguration.h"
#include "logger.h"
#include "os_utilities.h"
#include "utils.h"

#include <climits>


namespace
{
    const char CONFIGURATION_FILE_NAME[] = "seed.xml";

    const char MODULE_NAME[] = "SeedConfiguration";
};


namespace Model
{

SeedConfiguration::SeedConfiguration()
:
XMLConfiguration(),
m_configurationErrorReported(false),
m_id(),
m_value()
{
    restoreDefaultValues();
}

SeedConfiguration::~SeedConfiguration()
{
    //do nothing
}

void SeedConfiguration::restoreDefaultValues()
{
    m_id = 0;
    m_value = 0;
}

bool SeedConfiguration::readAllParametersFromFile(const char* fileName)
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
        fullFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getCacheDirectory());
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
            oss << "seedConfiguration file " << fullFileName << " has been loaded";
            Logger::log(LOG_LEVEL_INFO, oss.str().c_str());
        }
        else
        {
            std::ostringstream oss;
            oss << "Failed to process XML seedConfiguration file " << fullFileName;
            Logger::log(LOG_LEVEL_ERROR, oss.str().c_str());
        }
    }
    else
    {
        std::ostringstream oss;
        oss << "Failed to load seedConfiguration file " << fullFileName;
        if (doc.ErrorDesc() != 0)
        {
            oss << " (" << doc.ErrorDesc();

            if ((doc.ErrorRow() >= 0) && (doc.ErrorRow() >= 0))
            {
                oss << " Line:" << doc.ErrorRow() << ", Column:" << doc.ErrorCol();
            }
            //else do nothing

            oss << ")";
        }
        //else do nothing

        Logger::log(LOG_LEVEL_ERROR, oss.str().c_str());
    }

    return result;
}

bool SeedConfiguration::readAllParametersFromString(const char* str)
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
            oss << "seedConfiguration has been loaded";
            Logger::log(LOG_LEVEL_INFO, oss.str().c_str());
        }
        else
        {
            std::ostringstream oss;
            oss << "Failed to process XML seedConfiguration";
            Logger::log(LOG_LEVEL_ERROR, oss.str().c_str());
        }
    }
    else
    {
        std::ostringstream oss;
        oss << "Failed to load seedConfiguration";
        if (doc.ErrorDesc() != 0)
        {
            oss << " (" << doc.ErrorDesc();

            if ((doc.ErrorRow() >= 0) && (doc.ErrorRow() >= 0))
            {
                oss << " Line:" << doc.ErrorRow() << ", Column:" << doc.ErrorCol();
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

bool SeedConfiguration::parseTiXmlDocument(TiXmlDocument& doc)
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
        Logger::log(LOG_LEVEL_ERROR, "Error with XML file");
    }

    return result;
}

uint32_t SeedConfiguration::getId() const
{
    return m_id;
}

uint32_t SeedConfiguration::getValue() const
{
    return m_value;
}


bool SeedConfiguration::parseTiXmlElement(TiXmlNode* pParent)
{
    bool result = true;

    //Flags used to mark presence of obligatory element
    bool idOk = false;
    bool valueOk = false;

    assert(pParent != 0);
    const std::string PARENT_NAME (pParent->ValueTStr().c_str());
    if (PARENT_NAME == "Seed")
    {
        for (TiXmlNode* pChild = pParent->FirstChild(); pChild != 0; pChild = pChild->NextSibling())
        {
            const int parentType = pParent->Type();
            if (parentType == TiXmlNode::TINYXML_ELEMENT)
            {
                const std::string ELEMENT_NAME(pChild->ValueTStr().c_str());
                if (
                    checkUIntElement(ELEMENT_NAME, "ID", pChild, m_id, result, &idOk) ||
                    checkUIntElement(ELEMENT_NAME, "Value", pChild, m_value, result, &valueOk)
                    )
                {
                    //do nothing
                }
                else
                {
                    result = false;

                    std::ostringstream ss;
                    ss << "Invalid element \"" << ELEMENT_NAME << "\" in \"" << PARENT_NAME << "\" group";
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

        std::ostringstream ss;
        ss << "Invalid \"" << PARENT_NAME << "\" group";
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }

    result = result &&
        idOk && valueOk;
    return result;
}

} //namespace
