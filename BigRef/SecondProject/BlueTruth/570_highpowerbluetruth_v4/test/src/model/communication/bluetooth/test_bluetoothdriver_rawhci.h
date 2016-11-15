/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    13/08/2013  RG      001         V1.00 First Issue

    WARNING:
    The implementation contained in this module is GPL licensed.
    This module interacts with bluez library (www.bluez.org) which is GPL
    licensed. The source contains some code directly copied from the
    library source or associated tools source (hcidump in particular).
    To overcome the licencing issues and use this module some techniques
    have to be applied - see http://www.gnu.org/licenses/gpl-faq.html#NFUseGPLPlugins.
 */


#ifndef TEST_RAW_HCI_DRIVER_H_
#define TEST_RAW_HCI_DRIVER_H_

#ifdef __linux__
#include "datacontainer.h"

namespace BlueTooth
{

class TestRawHCIDriver
{
public:

    /**
     * @brief Restore initial conditions of the class
     */
    static void initialise();

    static void setRemoteDeviceCollection(
        const Model::DataContainer::TRemoteDeviceRecordCollection& remoteDeviceRecordCollection);
    static void setScanForRemoteDevicesResult(const bool result);
    static bool scanForRemoteDevices(
        const Model::TLocalDeviceRecord& localDeviceRecord,
        const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
        Model::DataContainer::TRemoteDeviceRecordCollection* pRemoteDeviceCollection,
        ::TTime_t& inquiryStartTime);

    static void stopScanningForRemoteDevices(
        const Model::TLocalDeviceRecord& localDeviceRecord);

    static bool wasStopScanningForRemoteDevicesCalled() { return m_stopScanningForRemoteDevicesCalled; }
    static void clearStopScanningForRemoteDevicesCalled() { m_stopScanningForRemoteDevicesCalled = false; }

    static void setLocalDeviceCollection(
        const Model::DataContainer::TLocalDeviceRecordCollection& localDeviceRecordCollection);
    static void getLocalDeviceCollection(Model::DataContainer::TLocalDeviceRecordCollection* pResult);

    static bool setupLocalDevice(
        const Model::TLocalDeviceConfiguration& localDeviceConfiguration,
        Model::TLocalDeviceRecord* pLocalDeviceRecord);

    static void closeLocalDevice(
        Model::TLocalDeviceRecord* pLocalDeviceRecord);

private:

    static Model::DataContainer::TLocalDeviceRecordCollection m_localDeviceRecordCollection;
    static Model::DataContainer::TRemoteDeviceRecordCollection m_remoteDeviceRecordCollection;
    static bool m_stopScanningForRemoteDevicesCalled;
    static bool m_scanForRemoteDevicesResult;
};

}
#endif //__linux__

#endif  //TEST_RAW_HCI_DRIVER_H_