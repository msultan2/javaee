/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue

 */

#ifndef _CORE_CONFIGURATION_H_
#define _CORE_CONFIGURATION_H_


#include "icoreconfiguration.h"
#include "xmlconfiguration.h"
#include "types.h"

#include <string>
#include <map>
#include <vector>

#include <tinyxml/tinyxml.h>


namespace Model
{

class CoreConfiguration :
    public ICoreConfiguration,
    public XMLConfiguration
{
public:

    //! Default constructor. This is singleton design pattern so this constructor is made private
    CoreConfiguration();

    //! Destructor
    virtual ~CoreConfiguration();

    bool isConfigurationErrorReported() const { return m_configurationErrorReported; }

    virtual void restoreDefaultValues();
    virtual bool readAllParametersFromFile(const char* fileName = 0);
    bool readAllParametersFromString(const char* str);


    //Core configuration
    virtual std::string getCoreConfigurationVersion() const;
    virtual unsigned int getMajorCoreConfigurationVersion() const;

    virtual std::string getSiteIdentifier() const;
    virtual std::string getSSLSerialNumber() const;

    virtual std::string getInstationSSHConnectionAddress() const;
    virtual uint64_t getInstationSSHConnectionPort() const;
    virtual std::string getInstationSSHConnectionLogin() const;
    virtual std::string getInstationSSHConnectionPassword() const;

    virtual std::string getConfigurationURL() const;
    virtual std::string getConfigurationURL_filePrefix() const;
    virtual std::string getConfigurationURL_fileSuffix() const;

    virtual std::string getCommandLineStatusReportURL() const;
    void setCommandLineStatusReportURL(const std::string& commandLineStatusReportURL);

    virtual uint64_t getLastUsedBlueToothDevice() const;
    virtual bool setLastUsedBlueToothDevice(const uint64_t value);

    virtual std::string getDeviceDriver() const;
    virtual std::string getParaniPortName() const;
    virtual uint64_t getParaniBitRate() const;

    long getFileLogLevel() const { return m_fileLogLevel; }
    long getConsoleLogLevel() const { return m_consoleLogLevel; }
    long getLogMaxNumberOfEntriesPerFile() const { return m_logMaxNumberOfEntriesPerFile; }
    long getLogMaxNumberOfCharactersPerFile() const { return m_logMaxNumberOfCharactersPerFile; }
    long getMaximumLogFileAgeInSeconds() const { return m_maximumLogFileAgeInSeconds; }

    virtual uint64_t getGSMModemType() const;
    virtual std::string getGSMModemConnectionAddress() const;
    virtual uint64_t getGSMModemConnectionPort() const;
    virtual std::string getGSMModemConnectionLogin() const;
    virtual std::string getGSMModemConnectionPassword() const;

private:

    //! copy constructor. Not implemented
    CoreConfiguration(const CoreConfiguration& rhs);
    //! copy assignment operator. Not implemented
    CoreConfiguration& operator=(const CoreConfiguration& rhs);

    bool parseTiXmlDocument(TiXmlDocument& doc);
    bool parseTiXmlElement(TiXmlNode* pParent);
    bool updateNodes(TiXmlNode* pParent);

    void validateConfigurationFile();

    //Private members
    bool m_configurationErrorReported;

    // Core configuration
    std::string m_coreConfigurationVersion;
    unsigned int m_majorCoreConfigurationVersion;

    std::string m_siteIdentifier;
    std::string m_SSLSerialNumber;

    std::string m_instationSSHConnectionAddress;
    uint64_t m_instationSSHConnectionPort;
    std::string m_instationSSHConnectionLogin;
    std::string m_instationSSHConnectionPassword;

    std::string m_configurationURL;
    std::string m_configurationURL_filePrefix;
    std::string m_configurationURL_fileSuffix;

    uint64_t m_lastUsedBlueToothDevice;
    std::string m_deviceDriver;
    //Parani specific parameters
    std::string m_paraniPortName;
    uint64_t m_paraniBitRate;

    long m_fileLogLevel;
    long m_consoleLogLevel;
    long m_logMaxNumberOfEntriesPerFile;
    long m_logMaxNumberOfCharactersPerFile;
    long m_maximumLogFileAgeInSeconds;

    // Instation configuration
    long m_instationResponseTimeout;
    long m_instationNumberOfRetries;
    long m_instationWatchdogPeriodInSeconds;

    uint64_t m_GSMModemType;
    std::string m_GSMModemConnectionAddress;
    uint64_t m_GSMModemConnectionPort;
    std::string m_GSMModemConnectionLogin;
    std::string m_GSMModemConnectionPassword;

    std::string m_commandLineStatusReportURL;
};

} //namespace

#endif // _CORE_CONFIGURATION_H_
