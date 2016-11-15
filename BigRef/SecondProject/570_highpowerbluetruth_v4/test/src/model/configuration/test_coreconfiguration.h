/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: This is a class implementing ICoreConfiguration interface
        used for testing (e.g. of InStationHTTPClient).

    Modification History:

    Date        Who     SCJS No     Remarks
    05/06/2010  RG      001         V1.00 First Issue

*/

#ifndef _TEST_CORE_CONFIGURATION_H_
#define _TEST_CORE_CONFIGURATION_H_


#include "icoreconfiguration.h"


namespace Testing
{

class TestCoreConfiguration : public Model::ICoreConfiguration
{
public:

    //! default constructor
    TestCoreConfiguration();
    //! destructor
    virtual ~TestCoreConfiguration();

    virtual std::string getCoreConfigurationVersion() const;
    void setCoreConfigurationVersion(const std::string& value);
    virtual unsigned int getMajorCoreConfigurationVersion() const;
    void setMajorCoreConfigurationVersion(const unsigned int value);

    virtual std::string getSiteIdentifier() const;
    void setSiteIdentifier(const std::string& value);
    virtual std::string getSSLSerialNumber() const;
    void setSSLSerialNumber(const std::string& value);

    virtual std::string getDeviceDriver() const;
    void setDeviceDriver(const std::string& value);
    virtual std::string getParaniPortName() const;
    void setParaniPortName(const std::string& value);
    virtual uint64_t getParaniBitRate() const;
    void setParaniBitRate(const uint64_t value);

    virtual std::string getInstationSSHConnectionAddress() const;
    void setInstationSSHConnectionAddress(const std::string& value);
    virtual uint64_t getInstationSSHConnectionPort() const;
    void setInstationSSHConnectionPort(const uint64_t value);
    virtual std::string getInstationSSHConnectionLogin() const;
    void setInstationSSHConnectionLogin(const std::string& value);
    virtual std::string getInstationSSHConnectionPassword() const;
    void setInstationSSHConnectionPassword(const std::string& value);

    virtual uint64_t getLastUsedBlueToothDevice() const;
    virtual bool setLastUsedBlueToothDevice(const uint64_t value);

    virtual std::string getConfigurationURL() const;
    void setConfigurationURL(const std::string& value);
    virtual std::string getConfigurationURL_filePrefix() const;
    void setConfigurationURL_filePrefix(const std::string& value);
    virtual std::string getConfigurationURL_fileSuffix() const;
    void setConfigurationURL_fileSuffix(const std::string& value);

    virtual uint64_t getGSMModemType() const;
    void setGSMModemType(const uint64_t value);
    virtual std::string getGSMModemConnectionAddress() const;
    void setGSMModemConnectionAddress(const std::string& value);
    virtual uint64_t getGSMModemConnectionPort() const;
    void setGSMModemConnectionPort(const uint64_t value);
    virtual std::string getGSMModemConnectionLogin() const;
    void setGSMModemConnectionLogin(const std::string& value);
    virtual std::string getGSMModemConnectionPassword() const;
    void setGSMModemConnectionPassword(const std::string& value);

protected:

    //! copy constructor. Not implemented
    TestCoreConfiguration(const TestCoreConfiguration& );
    //! assignment operator. Not implemented
    TestCoreConfiguration& operator=(const TestCoreConfiguration& );

    //Private members:
    std::string m_coreConfigurationVersion;
    unsigned int m_majorCoreConfigurationVersion;

    std::string m_siteIdentifier;
    std::string m_SSLSerialNumber;

    std::string m_deviceDriver;
    std::string m_paraniPortName;
    uint64_t m_paraniBitRate;

    std::string m_instationSSHConnectionAddress;
    uint64_t m_instationSSHConnectionPort;
    std::string m_instationSSHConnectionLogin;
    std::string m_instationSSHConnectionPassword;

    uint64_t m_lastUsedBlueToothDevice;

    std::string m_configurationURL;
    std::string m_configurationURL_filePrefix;
    std::string m_configurationURL_fileSuffix;

    uint64_t m_GSMModemType;
    std::string m_GSMModemConnectionAddress;
    uint64_t m_GSMModemConnectionPort;
    std::string m_GSMModemConnectionLogin;
    std::string m_GSMModemConnectionPassword;
};

}

#endif //_TEST_CORE_CONFIGURATION_H_
