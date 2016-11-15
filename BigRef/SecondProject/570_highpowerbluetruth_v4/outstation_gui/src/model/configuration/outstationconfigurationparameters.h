/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue

 */

#ifndef _OUTSTATION_CONFIGURATION_PARAMETERS_H_
#define _OUTSTATION_CONFIGURATION_PARAMETERS_H_


#include "viewconfigurationparameters.h"
#include "icoreconfiguration.h"
#include "types.h"

#include <string>
#include <map>
#include <vector>


namespace View
{

class OutStationConfigurationParameters :
    public ViewConfigurationParameters,
    public Model::ICoreConfiguration
{
public:

    static OutStationConfigurationParameters* getInstancePtr();

    static bool construct();
    static void destruct();

    //! Destructor
    virtual ~OutStationConfigurationParameters();


    virtual void restoreDefaultValues();
    virtual void readAllParametersFromFile();


    //Core configuration
    virtual std::string getCoreConfigurationVersion() const;
    virtual unsigned int getMajorCoreConfigurationVersion() const;

    virtual std::string getSiteIdentifier() const;
	bool setSiteIdentifier(const std::string& value);

    virtual std::string getSSLSerialNumber() const;
	bool setSSLSerialNumber(const std::string& value);

    virtual std::string getInstationSSHConnectionAddress() const;
    virtual uint64_t getInstationSSHConnectionPort() const;
    virtual std::string getInstationSSHConnectionLogin() const;
    virtual std::string getInstationSSHConnectionPassword() const;

    virtual uint64_t getLastUsedBlueToothDevice() const;
    virtual bool setLastUsedBlueToothDevice(const uint64_t value);

    virtual std::string getDeviceDriver() const;
	bool setDeviceDriver(const std::string& value);

    virtual std::string getParaniPortName() const;
	bool setParaniPortName(const std::string& value);

    virtual uint64_t getParaniBitRate() const;
	bool setParaniBitRate(const uint64_t value);

    virtual std::string getConfigurationURL() const;
	bool setConfigurationURL(const std::string& value);

    virtual std::string getConfigurationURL_filePrefix() const;
    virtual std::string getConfigurationURL_fileSuffix() const;

    virtual uint64_t getGSMModemType() const;
    virtual std::string getGSMModemConnectionAddress() const;
    virtual uint64_t getGSMModemConnectionPort() const;
    virtual std::string getGSMModemConnectionLogin() const;
    virtual std::string getGSMModemConnectionPassword() const;

private:
    //! Default constructor. This is singleton design pattern so this constructor is made private
    OutStationConfigurationParameters();

    //! copy constructor. Not implemented
    OutStationConfigurationParameters(const OutStationConfigurationParameters& rhs);
    //! copy assignment operator. Not implemented
    OutStationConfigurationParameters& operator=(const OutStationConfigurationParameters& rhs);

    //Private members
    static OutStationConfigurationParameters* m_instancePtr;
    static bool m_valid;

    // Core configuration
    std::string m_coreConfigurationVersion;
    unsigned int m_majorCoreConfigurationVersion;

    std::string m_siteIdentifier;
    std::string m_SSLSerialNumber;

    std::string m_instationSSHConnectionAddress;
    long m_instationSSHConnectionPort;
    std::string m_instationSSHConnectionLogin;
    std::string m_instationSSHConnectionPassword;

    std::string m_configurationURL;
    std::string m_configurationURL_filePrefix;
    std::string m_configurationURL_fileSuffix;

    uint64_t m_lastUsedBlueToothDevice;
    std::string m_deviceDriver;
    //Parani specific parameters
    std::string m_paraniPortName;
    long m_paraniBitRate;

    // Instation configuration
    long m_instationResponseTimeout;
    long m_instationNumberOfRetries;
    long m_instationWatchdogPeriodInSeconds;

    long m_GSMModemType;
    std::string m_GSMModemConnectionAddress;
    long m_GSMModemConnectionPort;
    std::string m_GSMModemConnectionLogin;
    std::string m_GSMModemConnectionPassword;

};

} //namespace

#endif // _OUTSTATION_CONFIGURATION_PARAMETERS_H_
