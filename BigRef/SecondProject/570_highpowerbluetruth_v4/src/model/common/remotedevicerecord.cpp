#include "stdafx.h"
#include "remotedevicerecord.h"

#include "bluetooth/bluetooth_utils.h"
#include "types.h"
#include "utils.h"

#include <openssl/sha.h>


namespace Model
{

int RemoteDeviceRecord::deviceIdentifierHashingFunction = eHASHING_FUNCTION_NONE;
char RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeed[DEVICE_IDENTIFIER_HASHING_FUNCTION_SEED_MAX_SIZE_IN_BYTES];
size_t RemoteDeviceRecord::deviceIdentifierHashingFunctionPreSeedSize = 0;
char RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeed[DEVICE_IDENTIFIER_HASHING_FUNCTION_SEED_MAX_SIZE_IN_BYTES];
size_t RemoteDeviceRecord::deviceIdentifierHashingFunctionPostSeedSize = 0;


RemoteDeviceRecord::RemoteDeviceRecord()
:
address(0),
deviceIdentifierHash(),
deviceIdentifierHashCalculated(false),
deviceClass(0),
name(),
firstObservationTimeUTC(0),
referencePointObservationTimeUTC(0),
lastObservationTimeUTC(0),
firstObservationTimeSteady(0),
referencePointObservationTimeSteady(0),
lastObservationTimeSteady(0),
presentInTheLastInquiry(false),
visibilityCounter(0),
numberOfScans(0),
numberOfScansPresent(0),
numberOfScansAbsent(0),
binType(eBIN_TYPE_UNDEFINED)
{
    //do nothing
}

RemoteDeviceRecord::RemoteDeviceRecord(
    const uint64_t& _address,
    const uint32_t _deviceClass)
:
address(_address),
deviceIdentifierHash(),
deviceIdentifierHashCalculated(false),
deviceClass(_deviceClass),
name(),
firstObservationTimeUTC(0),
referencePointObservationTimeUTC(0),
lastObservationTimeUTC(0),
firstObservationTimeSteady(0),
referencePointObservationTimeSteady(0),
lastObservationTimeSteady(0),
presentInTheLastInquiry(true),
visibilityCounter(1),
numberOfScans(0),
numberOfScansPresent(0),
numberOfScansAbsent(0),
binType(eBIN_TYPE_UNDEFINED)
{
    updateTimeFields();
    calculateHashIfRequired();
}

RemoteDeviceRecord::RemoteDeviceRecord(
    const uint64_t& _address,
    const uint32_t _deviceClass,
    const std::string& _name)
:
address(_address),
deviceIdentifierHash(),
deviceIdentifierHashCalculated(false),
deviceClass(_deviceClass),
name(_name),
firstObservationTimeUTC(0),
referencePointObservationTimeUTC(0),
lastObservationTimeUTC(0),
firstObservationTimeSteady(0),
referencePointObservationTimeSteady(0),
lastObservationTimeSteady(0),
presentInTheLastInquiry(true),
visibilityCounter(1),
numberOfScans(0),
numberOfScansPresent(0),
numberOfScansAbsent(0),
binType(eBIN_TYPE_UNDEFINED)
{
    updateTimeFields();
    calculateHashIfRequired();
}

void RemoteDeviceRecord::reset()
{
    address = 0;
    deviceIdentifierHashCalculated = false;
    memset(deviceIdentifierHash, 0, sizeof(deviceIdentifierHash));
    deviceClass = 0;
    name.clear();
    firstObservationTimeUTC = 0;
    referencePointObservationTimeUTC = 0;
    lastObservationTimeUTC = 0;
    firstObservationTimeSteady = 0;
    referencePointObservationTimeSteady = 0;
    lastObservationTimeSteady = 0;
    presentInTheLastInquiry = true;
    visibilityCounter = 0;
    numberOfScans = 0;
    numberOfScansPresent = 0;
    numberOfScansAbsent = 0;
    binType = eBIN_TYPE_UNDEFINED;
}

bool RemoteDeviceRecord::operator==(const uint64_t& _address) const
{
    return (address == _address);
}

void RemoteDeviceRecord::updateTimeFields()
{
    const TTime_t CURRENT_TIME_UTC(pt::second_clock::universal_time());
    const TTimeDiff_t TIME_SINCE_ZERO_UTC(CURRENT_TIME_UTC - ZERO_TIME_UTC);
    const uint64_t TIME_SINCE_ZERO_TOTAL_SECONDS_UTC = TIME_SINCE_ZERO_UTC.total_seconds();

    const TSteadyTimePoint CURRENT_TIME_STEADY(bc::steady_clock::now());
    const TSteadyTimeDuration TIME_SINCE_ZERO_STEADY(CURRENT_TIME_STEADY - ZERO_TIME_STEADY);
    const uint64_t TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY = bc::duration_cast<bc::seconds>(TIME_SINCE_ZERO_STEADY).count();

    firstObservationTimeUTC = TIME_SINCE_ZERO_TOTAL_SECONDS_UTC;
    lastObservationTimeUTC = TIME_SINCE_ZERO_TOTAL_SECONDS_UTC;
    referencePointObservationTimeUTC = TIME_SINCE_ZERO_TOTAL_SECONDS_UTC;

    firstObservationTimeSteady = TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
    lastObservationTimeSteady = TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
    referencePointObservationTimeSteady = TIME_SINCE_ZERO_TOTAL_SECONDS_STEADY;
}

void RemoteDeviceRecord::recalculateHash()
{
    //Verify of what hash function is used and if so calculate it once only
    switch (RemoteDeviceRecord::deviceIdentifierHashingFunction)
    {
        case eHASHING_FUNCTION_RAND1:
        {
            uint64_t value = address;

            //Append a random value
            value |= ((static_cast<uint64_t>(rand()) << 32) & 0xFFFF000000000000ULL);

            memset(deviceIdentifierHash, 0, sizeof(deviceIdentifierHash));

            //Mix digits
            uint8_t* pHash = deviceIdentifierHash;
            *pHash++ = (value >> 8*(8-3-2) ) & 0xFFULL;
            *pHash++ = (value >> 8*(8-5-2) ) & 0xFFULL;
            *pHash++ = (value >> 8*(8-1) ) & 0xFFULL;
            *pHash++ = (value >> 8*(8-1-2) ) & 0xFFULL;
            *pHash++ = (value >> 8*(8-6-2) ) & 0xFFULL;
            *pHash++ = (value >> 8*(8-2) ) & 0xFFULL;
            *pHash++ = (value >> 8*(8-2-2) ) & 0xFFULL;
            *pHash   = (value >> 8*(8-4-2) ) & 0xFFULL;

            break;
        }
        case eHASHING_FUNCTION_SHA256:
        {
            //Calculate SHA256

#if (SHA256_DIGEST_LENGTH != LENGTH_OF_SHA256_HASH_IN_BYTES)
#   error "SHA256_DIGEST_LENGTH and LENGTH_OF_SHA256_HASH_IN_BYTES do not match"
#endif
            assert(deviceIdentifierHashingFunctionPreSeedSize <= DEVICE_IDENTIFIER_HASHING_FUNCTION_SEED_MAX_SIZE_IN_BYTES);
            assert(deviceIdentifierHashingFunctionPostSeedSize <= DEVICE_IDENTIFIER_HASHING_FUNCTION_SEED_MAX_SIZE_IN_BYTES);

            SHA256_CTX ctx;
            SHA256_Init(&ctx);
            if (deviceIdentifierHashingFunctionPreSeedSize > 0)
                SHA256_Update(&ctx, (void*)&deviceIdentifierHashingFunctionPreSeed, deviceIdentifierHashingFunctionPreSeedSize);
            SHA256_Update(&ctx, (void*)&address, sizeof(address));
            if (deviceIdentifierHashingFunctionPostSeedSize > 0)
                SHA256_Update(&ctx, (void*)&deviceIdentifierHashingFunctionPostSeed, deviceIdentifierHashingFunctionPostSeedSize);
            SHA256_Final(deviceIdentifierHash, &ctx);

            break;
        }
        case eHASHING_FUNCTION_NONE:
        default:
        {
            uint8_t* pHash = deviceIdentifierHash;
            *pHash++ = (address & 0xFF0000000000ULL) >> 8*5;
            *pHash++ = (address & 0x00FF00000000ULL) >> 8*4;
            *pHash++ = (address & 0x0000FF000000ULL) >> 8*3;
            *pHash++ = (address & 0x000000FF0000ULL) >> 8*2;
            *pHash++ = (address & 0x00000000FF00ULL) >> 8*1;
            *pHash   = (address & 0x0000000000FFULL)       ;

            break;
        }
    }

    deviceIdentifierHashCalculated = true;
}

void RemoteDeviceRecord::calculateHashIfRequired()
{
    if (!deviceIdentifierHashCalculated)
    {
        recalculateHash();
    }
    //else do nothing
}

RemoteDeviceRecord& RemoteDeviceRecord::operator=(const RemoteDeviceRecord& rhs)
{
    if (this != &rhs)
    {
        address = rhs.address;
        memcpy(deviceIdentifierHash, rhs.deviceIdentifierHash, sizeof(deviceIdentifierHash));
        deviceIdentifierHashCalculated = rhs.deviceIdentifierHashCalculated;
        deviceClass = rhs.deviceClass;
        name = rhs.name;
        firstObservationTimeUTC = rhs.firstObservationTimeUTC;
        referencePointObservationTimeUTC = rhs.referencePointObservationTimeUTC;
        lastObservationTimeUTC = rhs.lastObservationTimeUTC;
        firstObservationTimeSteady = rhs.firstObservationTimeSteady;
        referencePointObservationTimeSteady = rhs.referencePointObservationTimeSteady;
        lastObservationTimeSteady = rhs.lastObservationTimeSteady;
        presentInTheLastInquiry = rhs.presentInTheLastInquiry;
        visibilityCounter = rhs.visibilityCounter;
        numberOfScans = rhs.numberOfScans;
        numberOfScansPresent = rhs.numberOfScansPresent;
        numberOfScansAbsent = rhs.numberOfScansAbsent;
        binType = rhs.binType;
    }
    else
    {
        //do nothing
    }

    return *this;
}

}
