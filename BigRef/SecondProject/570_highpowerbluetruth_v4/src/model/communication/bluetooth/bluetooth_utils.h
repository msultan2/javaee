/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef BLUETOOTH_UTILS_H_
#define BLUETOOTH_UTILS_H_


#include "types.h"

#include <string>


namespace BlueTooth
{

std::string decodeDeviceClass(const uint32_t deviceClass);

}

#endif //BLUETOOTH_UTILS_H_
