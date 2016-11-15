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


#ifndef REMOTE_DEVICE_RECORD_H_
#define REMOTE_DEVICE_RECORD_H_

namespace Model
{

enum EBinType
{
    eBIN_TYPE_UNDEFINED = 0,
    eBIN_TYPE_FREE_FLOW,
    eBIN_TYPE_MODERATE_FLOW,
    eBIN_TYPE_SLOW_FLOW,
    eBIN_TYPE_VERY_SLOW_FLOW,
    eBIN_TYPE_STATIC_FLOW,
    eBIN_TYPE_BACKGROUND
};

#define DEVICE_IDENTIFIER_HASHING_FUNCTION_SEED_MAX_SIZE_IN_BYTES (16)

/**
 The structure to store information for each device. This information will be
 used for device reporting and deriving conjestion/queue related information.
 */
struct RemoteDeviceRecord
{
    uint64_t address; //address of the bluetooth radio
    static int deviceIdentifierHashingFunction;
    static char deviceIdentifierHashingFunctionPreSeed[DEVICE_IDENTIFIER_HASHING_FUNCTION_SEED_MAX_SIZE_IN_BYTES];
    static size_t deviceIdentifierHashingFunctionPreSeedSize;
    static char deviceIdentifierHashingFunctionPostSeed[DEVICE_IDENTIFIER_HASHING_FUNCTION_SEED_MAX_SIZE_IN_BYTES];
    static size_t deviceIdentifierHashingFunctionPostSeedSize;
    uint8_t deviceIdentifierHash[LENGTH_OF_HASH_IN_BYTES];
    bool deviceIdentifierHashCalculated;
    uint32_t deviceClass; //class of device
    std::string name; //name of the radio

    uint64_t firstObservationTimeUTC;
    uint64_t referencePointObservationTimeUTC;
    uint64_t lastObservationTimeUTC;
    uint64_t firstObservationTimeSteady;
    uint64_t referencePointObservationTimeSteady;
    uint64_t lastObservationTimeSteady;
    bool presentInTheLastInquiry;
    unsigned int visibilityCounter;

    unsigned int numberOfScans;
    unsigned int numberOfScansPresent;
    unsigned int numberOfScansAbsent;
    EBinType binType;

    RemoteDeviceRecord();
    RemoteDeviceRecord(const uint64_t& _address, const uint32_t _deviceClass);
    RemoteDeviceRecord(const uint64_t& _address, const uint32_t _deviceClass, const std::string& _name);

    //! assignment operator. Not implemented
    RemoteDeviceRecord& operator=(const RemoteDeviceRecord& );

    void reset();
    void recalculateHash();
    void calculateHashIfRequired();

    //! Check if this record address matches the provided _address.
    //! This method is used in std::find method
    bool operator==(const uint64_t& _address) const;

private:
    void updateTimeFields();
};

typedef struct RemoteDeviceRecord TRemoteDeviceRecord;

} //namespace

#endif //REMOTE_DEVICE_RECORD_H_
