/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    14/08/2013  RG      001         V1.00 First Issue

 */


#ifndef DEVICE_DISCOVERER_PARANI_H_
#define DEVICE_DISCOVERER_PARANI_H_

#ifdef __linux__
#include "datacontainer.h"

namespace BlueTooth
{

class ParaniDriver
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

    /**
     * @brief Test if Parani device is accessible on port localDeviceRecord.name. Upto one device will be returned (if found)
     *
     * @return result A list (in the Parani case maximum one record in the list) of Parani devices that have been found
     */
    static void getLocalDeviceCollection(
        const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
        Model::DataContainer::TLocalDeviceRecordCollection* pResult);

    static bool setupLocalDevice(
        const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
        Model::TLocalDeviceRecord* pLocalDeviceRecord);

    static void closeLocalDevice(
        Model::TLocalDeviceRecord* pLocalDeviceRecord);

private:
    static unsigned int m_lastInquiryDurationInSeconds;
    static unsigned int m_lastDeviceDiscoveryMaxDevices;
    static unsigned int m_currentBitRate;
};

}
#endif //__linux__

#endif
