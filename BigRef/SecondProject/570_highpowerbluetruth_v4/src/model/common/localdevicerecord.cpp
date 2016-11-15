#include "stdafx.h"
#include "localdevicerecord.h"


namespace Model
{

LocalDeviceRecord::LocalDeviceRecord()
:
address(0),
deviceClass(0),
name(),
manufacturerID(0),
impSubversion(0),
hciRoute(-1),
deviceDescriptor(-1)
{
    //do nothing
}

LocalDeviceRecord::LocalDeviceRecord(const uint64_t& _address, const uint32_t _deviceClass, const std::string& _name)
:
address(_address),
deviceClass(_deviceClass),
name(_name),
manufacturerID(0),
impSubversion(0),
hciRoute(-1),
deviceDescriptor(-1)
{
    //do nothing
}

LocalDeviceRecord::LocalDeviceRecord(const uint64_t& _address)
:
address(_address),
deviceClass(0),
name(),
manufacturerID(0),
impSubversion(0),
hciRoute(-1),
deviceDescriptor(-1)
{
    //do nothing
}

LocalDeviceRecord::LocalDeviceRecord(const LocalDeviceRecord &rhs)
:
address(rhs.address),
deviceClass(rhs.deviceClass),
name(rhs.name),
manufacturerID(rhs.manufacturerID),
impSubversion(rhs.impSubversion),
hciRoute(rhs.hciRoute),
deviceDescriptor(rhs.deviceDescriptor)
{
}

LocalDeviceRecord& LocalDeviceRecord::operator=(const LocalDeviceRecord &rhs)
{
    if (this != &rhs)
    {
        address = rhs.address;
        deviceClass = rhs.deviceClass;
        name = rhs.name;
        manufacturerID = rhs.manufacturerID;
        impSubversion = rhs.impSubversion;
        hciRoute = rhs.hciRoute;
        deviceDescriptor = rhs.deviceDescriptor;
    }
    //else do nothing

    return *this;
}

void LocalDeviceRecord::reset()
{
    address = 0;
    deviceClass = 0;
    name.clear();
    manufacturerID = 0;
    impSubversion = 0;
    hciRoute = -1;
    deviceDescriptor = -1;
}

void LocalDeviceRecord::partialReset()
{
    deviceClass = 0;
    name.clear();
    manufacturerID = 0;
    impSubversion = 0;
    hciRoute = -1;
    deviceDescriptor = -1;
}

bool LocalDeviceRecord::isReset() const
{
    return (deviceDescriptor == -1);
}

}
