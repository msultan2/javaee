/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    22/09/2013  RG      001         V1.00 First Issue
*/

#ifndef _INI_PARAMETER_H_
#define _INI_PARAMETER_H_


#include "types.h"

#include <map>
#include <string>


namespace Model
{

enum EValueType
{
    eUNDEFINED_VALUE_TYPE = 0,
    eInt64  = 1,
    eBool   = 2,
    eDouble = 3,
    eString = 4
};

enum EValueTypeId
{
    eUNDEFINED_VALUE_TYPE_NAME = 0,

    eOUTSTATION_MODE_LEGACY, //version 3
    eOUTSTATION_MODE, //version 4+
    eSETTINGS_COLLECTION_INTERVAL_1, //mode 0 interval
    eSETTINGS_COLLECTION_INTERVAL_2, //mode 1, 2 and 3 interval
    eURL_CONGESTION_REPORTING,
    eURL_CONGESTION_REPORTS, //used from version 4+
    eURL_JOURNEY_TIMES_REPORTING,
    eURL_ALERT_AND_STATUS_REPORTS,
    eURL_STATUS_REPORTS,
    eURL_FAULT_REPORTS,
    eURL_STATISTICS_REPORTS,
    eINQUIRY_CYCLE_PERIOD,
    eINQUIRY_CYCLE_DURATION_IN_SECONDS,
    eINQUIRY_POWER,
    eOBFUSCATING_FUNCTION, //used from version 4+, obsoleted by HASHING_FUNCTION
    eHASHING_FUNCTION, //used from version 4+
    eHASHING_FUNCTION_SHA256_PRE_SEED,
    eHASHING_FUNCTION_SHA256_POST_SEED,
    eSTATISTICS_REPORTING_PERIOD_IN_SECONDS, //used from version 4+
    eSTATISTICS_REPORT_CONTENTS,
    eCONGESTION_REPORTING_PERIOD_IN_SECONDS, //used from version 4+
    eSTATUS_REPORTING_PERIOD_IN_SECONDS, //used from version 4+
    eBACKGROUND_START_TIME_THRESHOLD_IN_SECONDS,
    eBACKGROUND_END_TIME_THRESHOLD_IN_SECONDS,
    eFREE_FLOW_SPEED_CYCLES_THRESHOLD,
    eFREE_FLOW_BIN_THRESHOLD_IN_SECONDS, //used from version 4+
    eMODERATE_SPEED_CYCLES_THRESHOLD,
    eMODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, //used from version 4+
    eSLOW_SPEED_CYCLES_THRESHOLD,
    eSLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, //used from version 4+
    eVERY_SLOW_SPEED_CYCLES_THRESHOLD,
    eVERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, //used from version 4+
    eBIN_MAC_BIN_DROP_OUT_SCAN_CYCLE,
    eABSENCE_THRESHOLD_IN_SECONDS, //used from version 4+
    eQUEUE_ALERT_THRESHOLD_BIN,
    eQUEUE_DETECT_THRESHOLD,
    eQUEUE_CLEARANCE_THRESHOLD_DETECTION_NUMBER,
    eQUEUE_CLEARANCE_THRESHOLD, //used from version 4+
    eQUEUE_DETECTION_STARTUP_INTERVAL_IN_SECONDS, //used from version 4+
    eSIGN_REPORTS, //used from version 4+
    eHTTP_TIMEOUT, //in seconds
    eHTTP_RESPONSE_TIMEOUT_IN_SECONDS, //used from version 4+
    eHTTP_CONNECTION_TIMEOUT_IN_SECONDS, //used from version 4+
    eREPORT_STORAGE_CAPACITY,
    eINITIAL_STARTUP_DELAY,
    eGSM_MODEM_SIGNAL_LEVEL_SAMPLING_PERIOD_IN_SECONDS,
    eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS,
};

class IniParameter
{
public:
    IniParameter();


    IniParameter(
        const std::string name,
        const EValueType valueType,
        const int64_t value,
        const int64_t minValue,
        const int64_t maxValue);

    IniParameter(
        const std::string name,
        const EValueType valueType,
        const bool value);

    IniParameter(
        const std::string name,
        const EValueType valueType,
        const double value,
        const double minValue,
        const double maxValue);

    IniParameter(
        const std::string name,
        const EValueType valueType,
        const char* value);

    IniParameter(
        const std::string name,
        const EValueType valueType,
        const std::string& value);

    IniParameter(const IniParameter& rhs);

    virtual ~IniParameter();

    IniParameter& operator=(const IniParameter& rhs);

    const std::string& getName() const { return m_name; }
    EValueType getValueType() const { return m_valueType; }

    int64_t getValue() const { return m_value; }
    void setValue(const int64_t value) { m_value = value; }

    double getDoubleValue() const { return m_doubleValue; }
    void setDoubleValue(const double value) { m_doubleValue = value; }

    std::string getTextValue() const { return m_textValue; }
    void setTextValue(const std::string& value) { m_textValue = value; }

    int64_t getMinValue() const { return m_minValue; }
    int64_t getMaxValue() const { return m_maxValue; }
    double getMinDoubleValue() const { return m_minDoubleValue; }
    double getMaxDoubleValue() const { return m_maxDoubleValue; }

    std::string getTypeAsString() const;

private:
    std::string m_name;
    EValueType m_valueType;
    int64_t m_value;
    int64_t m_minValue;
    int64_t m_maxValue;
    double m_doubleValue;
    double m_minDoubleValue;
    double m_maxDoubleValue;
    std::string m_textValue;
};

typedef std::map<EValueTypeId, IniParameter> TIniParameterMap;

}

#endif //_INI_PARAMETER_H_
