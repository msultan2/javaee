#include "test_bluetoothdriver_rawhci.h"


namespace BlueTooth
{

//Static memebers
Model::DataContainer::TLocalDeviceRecordCollection TestRawHCIDriver::m_localDeviceRecordCollection;
Model::DataContainer::TRemoteDeviceRecordCollection TestRawHCIDriver::m_remoteDeviceRecordCollection;
bool TestRawHCIDriver::m_stopScanningForRemoteDevicesCalled = false;
bool TestRawHCIDriver::m_scanForRemoteDevicesResult = false;


void TestRawHCIDriver::initialise()
{
    m_localDeviceRecordCollection.clear();
    m_remoteDeviceRecordCollection.clear();
    m_stopScanningForRemoteDevicesCalled = false;
    m_scanForRemoteDevicesResult = false;
}

void TestRawHCIDriver::setRemoteDeviceCollection(
    const Model::DataContainer::TRemoteDeviceRecordCollection& remoteDeviceRecordCollection)
{
    m_remoteDeviceRecordCollection = remoteDeviceRecordCollection;
}

void TestRawHCIDriver::setScanForRemoteDevicesResult(const bool result)
{
    m_scanForRemoteDevicesResult = result;
}

bool TestRawHCIDriver::scanForRemoteDevices(
    const Model::TLocalDeviceRecord& ,
    const Model::TLocalDeviceConfiguration& ,
    Model::DataContainer::TRemoteDeviceRecordCollection* pRemoteDeviceCollection,
    ::TTime_t& )
{
    assert(pRemoteDeviceCollection != 0);

    *pRemoteDeviceCollection = m_remoteDeviceRecordCollection;
    return m_scanForRemoteDevicesResult;
}


void TestRawHCIDriver::stopScanningForRemoteDevices(const Model::TLocalDeviceRecord& )
{
    m_stopScanningForRemoteDevicesCalled = true;
}


void TestRawHCIDriver::setLocalDeviceCollection(
    const Model::DataContainer::TLocalDeviceRecordCollection& localDeviceRecordCollection)
{
    m_localDeviceRecordCollection = localDeviceRecordCollection;
}

void TestRawHCIDriver::getLocalDeviceCollection(
    Model::DataContainer::TLocalDeviceRecordCollection* pResult)
{
    assert(pResult != 0);

    *pResult = m_localDeviceRecordCollection;
}


bool TestRawHCIDriver::setupLocalDevice(
    const Model::TLocalDeviceConfiguration& ,
    Model::TLocalDeviceRecord* pLocalDeviceRecord)
{
    pLocalDeviceRecord->deviceDescriptor = 0;

    return true;
}

void TestRawHCIDriver::closeLocalDevice(Model::TLocalDeviceRecord* pLocalDeviceRecord)
{
    pLocalDeviceRecord->partialReset();
}

};
