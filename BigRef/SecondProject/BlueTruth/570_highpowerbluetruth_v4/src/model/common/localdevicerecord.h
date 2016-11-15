/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef LOCAL_DEVICE_RECORD_H_
#define LOCAL_DEVICE_RECORD_H_

#include "types.h"

#include <string>
#include <boost/shared_ptr.hpp>


namespace Model
{

class LocalDeviceRecord
{
public:

    LocalDeviceRecord();
    LocalDeviceRecord(const uint64_t& _address, const uint32_t _deviceClass, const std::string& _name);
    explicit LocalDeviceRecord(const uint64_t& _address);

    //! copy constructor
    LocalDeviceRecord(const LocalDeviceRecord& rhs);
    //! assignment operator
    LocalDeviceRecord& operator=(const LocalDeviceRecord& rhs);

    void reset();
    void partialReset();
    bool isReset() const;

    //Memebers.
    // This class has been derived from a struct so its members have been left
    // public to avoid changing of significant amount of code
    uint64_t address; //address of the bluetooth radio
    uint32_t deviceClass; //class of device
    std::string name; //name of the radio
    uint32_t manufacturerID; //manufacturer of the bluetooth radio
    uint32_t impSubversion; // implementation subversion
    int hciRoute; //in linux version this value is used to pass hci route
    int deviceDescriptor; //in linux version this value is used to store device descriptor
};
typedef LocalDeviceRecord TLocalDeviceRecord;
typedef boost::shared_ptr<TLocalDeviceRecord> TLocalDeviceRecord_shared_ptr;

} //namespace

#endif //LOCAL_DEVICE_RECORD_H_
