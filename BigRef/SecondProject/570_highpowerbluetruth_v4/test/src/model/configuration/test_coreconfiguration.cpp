#include "test_coreconfiguration.h"


namespace Testing
{

TestCoreConfiguration::TestCoreConfiguration()
:
m_coreConfigurationVersion(),
m_majorCoreConfigurationVersion(0),
m_siteIdentifier(),
m_SSLSerialNumber(),
m_deviceDriver(),
m_paraniPortName(),
m_paraniBitRate(0),
m_instationSSHConnectionAddress(),
m_instationSSHConnectionPort(0),
m_instationSSHConnectionLogin(),
m_instationSSHConnectionPassword(),
m_lastUsedBlueToothDevice(0),
m_configurationURL(),
m_configurationURL_filePrefix(),
m_configurationURL_fileSuffix(),
m_GSMModemType(0),
m_GSMModemConnectionAddress(),
m_GSMModemConnectionPort(22),
m_GSMModemConnectionLogin(),
m_GSMModemConnectionPassword()
{
    //do nothing
}

TestCoreConfiguration::~TestCoreConfiguration()
{
    //do nothing
}

std::string TestCoreConfiguration::getCoreConfigurationVersion() const
{
    return m_coreConfigurationVersion;
}

void TestCoreConfiguration::setCoreConfigurationVersion(const std::string& value)
{
    m_coreConfigurationVersion = value;
}

unsigned int TestCoreConfiguration::getMajorCoreConfigurationVersion() const
{
    return m_majorCoreConfigurationVersion;
}

void TestCoreConfiguration::setMajorCoreConfigurationVersion(const unsigned int value)
{
    m_majorCoreConfigurationVersion = value;
}

std::string TestCoreConfiguration::getSiteIdentifier() const
{
    return m_siteIdentifier;
}

void TestCoreConfiguration::setSiteIdentifier(const std::string& value)
{
    m_siteIdentifier = value;
}

std::string TestCoreConfiguration::getSSLSerialNumber() const
{
    return m_SSLSerialNumber;
}

void TestCoreConfiguration::setSSLSerialNumber(const std::string& value)
{
    m_SSLSerialNumber = value;
}

std::string TestCoreConfiguration::getDeviceDriver() const
{
    return m_deviceDriver;
}

void TestCoreConfiguration::setDeviceDriver(const std::string& value)
{
    m_deviceDriver = value;
}

std::string TestCoreConfiguration::getParaniPortName() const
{
    return m_paraniPortName;
}

void TestCoreConfiguration::setParaniPortName(const std::string& value)
{
    m_paraniPortName = value;
}

uint64_t TestCoreConfiguration::getParaniBitRate() const
{
    return m_paraniBitRate;
}

void TestCoreConfiguration::setParaniBitRate(const uint64_t value)
{
    m_paraniBitRate = value;
}

std::string TestCoreConfiguration::getInstationSSHConnectionAddress() const
{
    return m_instationSSHConnectionAddress;
}

void TestCoreConfiguration::setInstationSSHConnectionAddress(const std::string& value)
{
    m_instationSSHConnectionAddress = value;
}

uint64_t TestCoreConfiguration::getInstationSSHConnectionPort() const
{
    return m_instationSSHConnectionPort;
}

void TestCoreConfiguration::setInstationSSHConnectionPort(const uint64_t value)
{
    m_instationSSHConnectionPort = value;
}

std::string TestCoreConfiguration::getInstationSSHConnectionLogin() const
{
    return m_instationSSHConnectionLogin;
}

void TestCoreConfiguration::setInstationSSHConnectionLogin(const std::string& value)
{
    m_instationSSHConnectionLogin = value;
}

std::string TestCoreConfiguration::getInstationSSHConnectionPassword() const
{
    return m_instationSSHConnectionPassword;
}

void TestCoreConfiguration::setInstationSSHConnectionPassword(const std::string& value)
{
    m_instationSSHConnectionPassword = value;
}

uint64_t TestCoreConfiguration::getLastUsedBlueToothDevice() const
{
    return m_lastUsedBlueToothDevice;
}

bool TestCoreConfiguration::setLastUsedBlueToothDevice(const uint64_t value)
{
    m_lastUsedBlueToothDevice = value;
    return true;
}

std::string TestCoreConfiguration::getConfigurationURL() const
{
    return m_configurationURL;
}

void TestCoreConfiguration::setConfigurationURL(const std::string& value)
{
    m_configurationURL = value;
}

std::string TestCoreConfiguration::getConfigurationURL_filePrefix() const
{
    return m_configurationURL_filePrefix;
}

void TestCoreConfiguration::setConfigurationURL_filePrefix(const std::string& value)
{
    m_configurationURL_filePrefix = value;
}

std::string TestCoreConfiguration::getConfigurationURL_fileSuffix() const
{
    return m_configurationURL_fileSuffix;
}

void TestCoreConfiguration::setConfigurationURL_fileSuffix(const std::string& value)
{
    m_configurationURL_fileSuffix = value;
}

uint64_t TestCoreConfiguration::getGSMModemType() const
{
    return m_GSMModemType;
}

void TestCoreConfiguration::setGSMModemType(const uint64_t value)
{
    m_GSMModemType = value;
}

std::string TestCoreConfiguration::getGSMModemConnectionAddress() const
{
    return m_GSMModemConnectionAddress;
}

void TestCoreConfiguration::setGSMModemConnectionAddress(const std::string& value)
{
    m_GSMModemConnectionAddress = value;
}

uint64_t TestCoreConfiguration::getGSMModemConnectionPort() const
{
    return m_GSMModemConnectionPort;
}

void TestCoreConfiguration::setGSMModemConnectionPort(const uint64_t value)
{
    m_GSMModemConnectionPort = value;
}

std::string TestCoreConfiguration::getGSMModemConnectionLogin() const
{
    return m_GSMModemConnectionLogin;
}

void TestCoreConfiguration::setGSMModemConnectionLogin(const std::string& value)
{
    m_GSMModemConnectionLogin = value;
}

std::string TestCoreConfiguration::getGSMModemConnectionPassword() const
{
    return m_GSMModemConnectionPassword;
}

void TestCoreConfiguration::setGSMModemConnectionPassword(const std::string& value)
{
    m_GSMModemConnectionPassword = value;
}

}
