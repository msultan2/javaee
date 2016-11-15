/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    28/09/2013  RG      001         V1.00 First Issue
*/

#include "types.h"

#include <string>


#ifndef LOCAL_DEVICE_CONFIGURATION_H_
#define LOCAL_DEVICE_CONFIGURATION_H_

namespace Model
{

enum EDeviceDriver
{
    eDEVICE_DRIVER_UNDEFINED = 1,
#if defined _WIN32
    eDEVICE_DRIVER_WINDOWS_BLUETOOTH,
    eDEVICE_DRIVER_WINDOWS_WSA,
#elif defined __linux__
    eDEVICE_DRIVER_LINUX_RAW_HCI,
    eDEVICE_DRIVER_LINUX_NATIVE_BLUEZ,
    eDEVICE_DRIVER_LINUX_PARANI,
#if defined TESTING
    eDEVICE_DRIVER_LINUX_TESTING_RAW_HCI,
    eDEVICE_DRIVER_LINUX_TESTING_OTHER,
#endif
#else
#error Operating System not supported
#endif
};

//Parameters of device configuration read from either core or functional configuration
struct LocalDeviceConfiguration
{
    uint64_t address; //address of the bluetooth radio

    EDeviceDriver deviceDriver; //device driver to be used

    std::string paraniPortName; //port name (serial, e.g. ttyUSB0) used for Parani driver only
    unsigned int paraniBitRate; //bit rate used for Parani driver only

    int8_t inquiryPower; //values -70..20, used only for Raw HCI driver

    unsigned int inquiryDurationInSeconds;
    unsigned int deviceDiscoveryMaxDevices;

    LocalDeviceConfiguration();
};
typedef struct LocalDeviceConfiguration TLocalDeviceConfiguration;

} //namespace

#endif //LOCAL_DEVICE_CONFIGURATION_H_
