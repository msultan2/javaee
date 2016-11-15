#include "stdafx.h"
#include "instation/ihttpclient.h"


namespace InStation
{

IHTTPClient::IHTTPClient()
{

}

IHTTPClient::~IHTTPClient()
{

}

IHTTPClient::RawDeviceDetection::RawDeviceDetection(const uint64_t _deviceIdentifier)
:
deviceIdentifier(_deviceIdentifier)
{
    //do nothing
}

IHTTPClient::AlertAndStatusReport::AlertAndStatusReport(const std::string& _code, const unsigned int _count)
:
code(_code),
count(_count)
{
    //do nothing
}

IHTTPClient::StatusReport::StatusReport(const char* _name, const char* _value)
:
name(_name),
value(_value)
{
    //do nothing
}

IHTTPClient::FaultReport::FaultReport(
    const unsigned int _id,
    const ::TTime_t&_eventTime,
    const unsigned int _status)
:
id(_id),
eventTime(_eventTime),
status(_status)
{
    //do nothing
}

IHTTPClient::StatisticsReport::StatisticsReport(
    const uint64_t _deviceIdentifier,
    const uint8_t* _deviceIdentifierHash,
    const uint32_t _cod, //class of device
    const uint64_t _firstObservationTime,
    const uint64_t _referencePointObservationTimeDelta,
    const uint64_t _lastObservationTimeDelta
)
:
deviceIdentifier(_deviceIdentifier),
deviceIdentifierHash(),
cod(_cod),
firstObservationTime(_firstObservationTime),
referencePointObservationTimeDelta(_referencePointObservationTimeDelta),
lastObservationTimeDelta(_lastObservationTimeDelta)
{
    memcpy(deviceIdentifierHash, _deviceIdentifierHash, sizeof(deviceIdentifierHash));
}

}
