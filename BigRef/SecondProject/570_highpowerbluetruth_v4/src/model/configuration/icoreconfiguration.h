/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    05/06/2010  RG      001         V1.00 First Issue

*/

#ifndef _I_CORE_CONFIGURATION_H_
#define _I_CORE_CONFIGURATION_H_


#include "types.h"
#include <string>


namespace Model
{

class ICoreConfiguration
{
public:

    //! destructor
    virtual ~ICoreConfiguration();

    virtual std::string getCoreConfigurationVersion() const = 0;
    virtual unsigned int getMajorCoreConfigurationVersion() const = 0;

    virtual std::string getSiteIdentifier() const = 0;
    virtual std::string getSSLSerialNumber() const = 0;

    virtual std::string getDeviceDriver() const = 0;
    virtual std::string getParaniPortName() const = 0;
    virtual uint64_t getParaniBitRate() const = 0;

    virtual std::string getInstationSSHConnectionAddress() const = 0;
    virtual uint64_t getInstationSSHConnectionPort() const = 0;
    virtual std::string getInstationSSHConnectionLogin() const = 0;
    virtual std::string getInstationSSHConnectionPassword() const = 0;

    virtual uint64_t getLastUsedBlueToothDevice() const = 0;
    virtual bool setLastUsedBlueToothDevice(const uint64_t value) = 0;

    virtual std::string getConfigurationURL() const = 0;
    virtual std::string getConfigurationURL_filePrefix() const = 0;
    virtual std::string getConfigurationURL_fileSuffix() const = 0;

    virtual std::string getCommandLineStatusReportURL() const = 0;

    enum
    {
        eGSM_MODEM_TYPE_NONE = 0,
        eGSM_MODEM_TYPE_IR791WH01 = 1
    };
    virtual uint64_t getGSMModemType() const = 0;
    virtual std::string getGSMModemConnectionAddress() const = 0;
    virtual uint64_t getGSMModemConnectionPort() const = 0;
    virtual std::string getGSMModemConnectionLogin() const = 0;
    virtual std::string getGSMModemConnectionPassword() const = 0;

protected:

    //! default constructor
    ICoreConfiguration();
    //! copy constructor. Not implemented
    ICoreConfiguration(const ICoreConfiguration& );
    //! assignment operator. Not implemented
    ICoreConfiguration& operator=(const ICoreConfiguration& );

};

}

#endif //_I_CORE_CONFIGURATION_H_
