/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    15/08/2013  RG      001         V1.00 First Issue
 */


#ifndef BLUETOOTH_DRIVER_WINDOWS_WSA_H_
#define BLUETOOTH_DRIVER_WINDOWS_WSA_H_

#ifdef _WIN32
#include "datacontainer.h"

namespace BlueTooth
{

class WindowsWSADriver
{
public:

    //! @brief This function inquires a list of all visible BlueTooth devices
    static bool scanForRemoteDevices(
        const Model::TLocalDeviceRecord& localDeviceRecord,
        const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
        Model::DataContainer::TRemoteDeviceRecordCollection* pRemoteDeviceCollection,
        ::Mutex& remoteDeviceCollectionMutex);

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
#endif //_WIN32

#endif //BLUETOOTH_DRIVER_WINDOWS_WSA_H_
