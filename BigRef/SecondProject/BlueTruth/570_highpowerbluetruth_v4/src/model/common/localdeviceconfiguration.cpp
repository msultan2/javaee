#include "stdafx.h"
#include "localdeviceconfiguration.h"


namespace Model
{

LocalDeviceConfiguration::LocalDeviceConfiguration()
:
address(0),
deviceDriver(eDEVICE_DRIVER_UNDEFINED),
paraniPortName(),
paraniBitRate(0),
inquiryPower(20), //maximum value defined in the specification
inquiryDurationInSeconds(10),
deviceDiscoveryMaxDevices(255)
{
    //do nothing
}

}
