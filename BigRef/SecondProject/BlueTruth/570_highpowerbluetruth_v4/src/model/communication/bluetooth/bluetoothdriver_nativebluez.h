/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    15/08/2013  RG      001         V1.00 First Issue

    WARNING:
    The implementation contained in this module is GPL licensed.
    This module interacts with bluez library (www.bluez.org) which is GPL
    licensed. The source contains some code directly copied from the
    library source or associated tools source (hcidump in particular).
    To overcome the licencing issues and use this module some techniques
    have to be applied - see http://www.gnu.org/licenses/gpl-faq.html#NFUseGPLPlugins.
 */


#ifndef BLUETOOTH_DRIVER_NATIVE_BLUEZ_H_
#define BLUETOOTH_DRIVER_NATIVE_BLUEZ_H_

#ifdef __linux__
#include "datacontainer.h"

namespace BlueTooth
{

class NativeBluezDriver
{
public:

    static bool scanForRemoteDevices(
        const Model::TLocalDeviceRecord& localDeviceRecord,
        const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
        Model::DataContainer::TRemoteDeviceRecordCollection* pRemoteDeviceCollection,
        ::Mutex& remoteDeviceCollectionMutex,
        ::TTime_t& inquiryStartTime);

    static void stopScanningForRemoteDevices(
        const Model::TLocalDeviceRecord& localDeviceRecord);

    static void getLocalDeviceCollection(Model::DataContainer::TLocalDeviceRecordCollection* pResult);

    static bool setupLocalDevice(
        const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
        Model::TLocalDeviceRecord* pLocalDeviceRecord);

    static void closeLocalDevice(
        Model::TLocalDeviceRecord* pLocalDeviceRecord);

};

}
#endif //__linux__

#endif //BLUETOOTH_DRIVER_NATIVE_BLUEZ_H_
