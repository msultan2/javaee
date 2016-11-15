/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
 */

#ifndef BRDF_XML_CONFIGURATION_H_
#define BRDF_XML_CONFIGURATION_H_


#include "ibrdfxmlconfiguration.h"
#include "xmlconfiguration.h"
#include "types.h"

#include <string>
#include <map>
#include <vector>

#include <tinyxml/tinyxml.h>


namespace Model
{

class BrdfXmlConfiguration :
    public IBrdfXmlConfiguration,
    public XMLConfiguration
{
public:

    //! Default constructor
    BrdfXmlConfiguration();

    //! Destructor
    virtual ~BrdfXmlConfiguration();

    bool isConfigurationErrorReported() const { return m_configurationErrorReported; }

    virtual void restoreDefaultValues();
    virtual bool readAllParametersFromFile(const char* fileName = 0);
    bool readAllParametersFromString(const char* str);


    //Configuration
    virtual std::string getConfigurationVersion() const;

    virtual std::string getDetectorOwner() const;

    virtual std::string getDatabaseHost() const;
    virtual uint64_t getDatabasePort() const;
    virtual std::string getDatabaseUser() const;
    virtual std::string getDatabasePassword() const;

    long getFileLogLevel() const { return m_fileLogLevel; }
    long getConsoleLogLevel() const { return m_consoleLogLevel; }
    long getLogMaxNumberOfEntriesPerFile() const { return m_logMaxNumberOfEntriesPerFile; }
    long getLogMaxNumberOfCharactersPerFile() const { return m_logMaxNumberOfCharactersPerFile; }

private:

    //! copy constructor. Not implemented
    BrdfXmlConfiguration(const BrdfXmlConfiguration& rhs);
    //! copy assignment operator. Not implemented
    BrdfXmlConfiguration& operator=(const BrdfXmlConfiguration& rhs);

    bool parseTiXmlDocument(TiXmlDocument& doc);
    bool parseTiXmlElement(TiXmlNode* pParent);
    bool updateNodes(TiXmlNode* pParent);

    void validateConfigurationFile();

    //Private members
    bool m_configurationErrorReported;


    std::string m_configurationVersion;

    std::string m_detectorOwner;

    std::string m_databaseHost;
    uint64_t m_databasePort;
    std::string m_databaseUser;
    std::string m_databasePassword;

    long m_fileLogLevel;
    long m_consoleLogLevel;
    long m_logMaxNumberOfEntriesPerFile;
    long m_logMaxNumberOfCharactersPerFile;
};

} //namespace

#endif // BRDF_XML_CONFIGURATION_H_
