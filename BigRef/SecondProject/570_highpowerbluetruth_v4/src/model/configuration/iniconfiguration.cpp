#include "stdafx.h"
#include "iniconfiguration.h"

#include "applicationconfiguration.h"
#include "logger.h"
#include "md5_rfc1321.h"
#include "os_utilities.h"
#include "utils.h"


namespace
{
    const char MODULE_NAME[] = "IniConfiguration";
}


namespace Model
{

const char IniConfiguration::LAST_SAVED_CONFIGURATION_FILE_NAME[] = "last_functional_configuration.conf";
const char IniConfiguration::INSTALLATION_CONFIGURATION_FILE_NAME[] = "functional_configuration.conf";

IniConfiguration::IniConfiguration()
:
m_parameterMap(),
m_configurationText(),
m_isValid(false),
m_parameterError(false)
{
    initialiseParameterMap();
}

IniConfiguration::IniConfiguration(const std::string& configurationText)
:
m_parameterMap(),
m_configurationText(configurationText),
m_isValid(false),
m_parameterError(false)
{
    initialiseParameterMap();
    processConfigurationText();
}

IniConfiguration::IniConfiguration(const char* configurationText)
:
m_parameterMap(),
m_configurationText(configurationText),
m_isValid(false),
m_parameterError(false)
{
    initialiseParameterMap();
    processConfigurationText();
}

IniConfiguration& IniConfiguration::operator=(const IniConfiguration& rhs)
{
    if (this != &rhs)
    {
        m_parameterMap = rhs.m_parameterMap;
        m_configurationText = rhs.m_configurationText;
        m_isValid = rhs.m_isValid;
        m_parameterError = rhs.m_parameterError;
    }
    //else do nothing

    return *this;
}

bool IniConfiguration::loadFromFile(const bool useValueFromSystemConfigurationDirectory)
{
    std::string lastConfigurationFileName;
    if (useValueFromSystemConfigurationDirectory)
    {
        lastConfigurationFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getSysConfDirectory());
#ifdef _WIN32
        if (lastConfigurationFileName == ".\\")
        {
            lastConfigurationFileName = "..\\misc\\";
        }
        //else do nothing
#else
        //TODO Fix path for the case of running from local directory
#endif
        lastConfigurationFileName += INSTALLATION_CONFIGURATION_FILE_NAME;
    }
    else
    {
        lastConfigurationFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getCacheDirectory());
        lastConfigurationFileName += LAST_SAVED_CONFIGURATION_FILE_NAME;
    }

    std::ifstream lastConfigurationFile;
    lastConfigurationFile.open(
        lastConfigurationFileName.c_str(),
        std::ifstream::in);

    bool result = false;

    if (lastConfigurationFile.is_open())
    {
        do
        {
            m_configurationText += lastConfigurationFile.get();
        }
        while (lastConfigurationFile.good());

        result = true;
    }
    //else do nothing

    if (result)
    {
        std::ostringstream ss;
        ss << "Functional configuration file \"" << lastConfigurationFileName << "\" has been loaded";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());
    }
    else
    {
        std::ostringstream ss;
        ss << "Loading of functional configuration file \"" << lastConfigurationFileName << "\" has failed";
        if (!useValueFromSystemConfigurationDirectory)
        {
            ss << ". The file will be downloaded from the InStation";
        }
        //else do nothing

        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }

    return result;
}

bool IniConfiguration::saveToFile()
{
    bool result = false;

    std::string lastConfigurationFileName;
    lastConfigurationFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getCacheDirectory());
    lastConfigurationFileName += LAST_SAVED_CONFIGURATION_FILE_NAME;

    std::ofstream lastConfigurationFile;
    lastConfigurationFile.open(
        lastConfigurationFileName.c_str(),
        std::ifstream::out);

    if (lastConfigurationFile.is_open())
    {
        lastConfigurationFile << m_configurationText;
        lastConfigurationFile.close();
        result = true;
    }
    else
    {
        std::ostringstream ss;
        ss << "Unable to save configuration to a file \"" << lastConfigurationFileName << "\"";
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }

    return result;
}

bool IniConfiguration::processConfigurationText()
{
    bool result = true;

    TConfigurationParserContext configurationParserContext;
    m_isValid = ConfigurationParser::parse(m_configurationText, configurationParserContext);
    if (m_isValid)
    {
        for (TConfiguration::TConfigurationCollection::const_iterator
                iter(configurationParserContext.configuration.items.begin()),
                iterEnd(configurationParserContext.configuration.items.end());
            iter != iterEnd;
            ++iter)
        {
            if (assignNewValueByName(iter->first, iter->second))
            {
                std::ostringstream logText;
                logText
                    << "parameter: \"" << iter->first
                    << "\", value=";
                switch (iter->second.type)
                {
                    case TConfigurationItem::eTYPE_INT:
                    {
                        logText << iter->second.valueInt;
                        break;
                    }

                    case TConfigurationItem::eTYPE_DOUBLE:
                    {
                        logText << iter->second.valueDouble;
                        break;
                    }

                    case TConfigurationItem::eTYPE_STRING:
                    {
                        logText << iter->second.valueString;
                        break;
                    }

                    default:
                    {
                        //do nothing
                        break;
                    }
                }

                Logger::log(LOG_LEVEL_DEBUG3, logText.str().c_str());
            }
            else
            {
                std::string errorText;
                errorText += "Unknown parameter \"";
                errorText += iter->first;
                errorText += "\" found in the configuration file";

                Logger::log(LOG_LEVEL_DEBUG1, errorText.c_str());
            }
        }
    }
    else
    {
        result = false;
        Logger::log(LOG_LEVEL_ERROR, "Unable to parse functional configuration file");
    }

    //The following snippet is to use (copy) obsoleted obfuscation function
    //to hashing function parameter if hashing function parameter is zero i.e.
    //(possibly not defined)
    int64_t hashingFunction = 0;
    if (
        (getValueInt64(Model::eHASHING_FUNCTION, hashingFunction)) &&
        (hashingFunction == 0)
        )
    {
        //i.e. hashing function not-defined
        int64_t obfuscatingFunction = 0;
        if (getValueInt64(Model::eOBFUSCATING_FUNCTION, obfuscatingFunction))
        {
            setValueInt64(Model::eHASHING_FUNCTION, obfuscatingFunction);
        }
        //else do nothing
    }
    //else do nothing

    return result;
}

IniConfiguration::~IniConfiguration()
{
    //do nothing
}

bool IniConfiguration::getValueInt64(const EValueTypeId valueTypeId, int64_t& value) const
{
    bool result = false;

    TIniParameterMap::const_iterator iter(m_parameterMap.find(valueTypeId));

    if (iter!=m_parameterMap.end())
    {
        if (
            (iter->second.getValue() >= iter->second.getMinValue()) &&
            (iter->second.getValue() <= iter->second.getMaxValue())
            )
        {
            result = true;
            value = iter->second.getValue();
        }
        else
        {
            value = iter->second.getValue();

            std::ostringstream ss;
            ss << "Invalid configuration value. The value of " << iter->second.getName()
                << " is not valid ("
                << iter->second.getValue() << ")";
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
    }
    //else do nothing

    return result;
}

bool IniConfiguration::setValueInt64(const EValueTypeId valueTypeId, const int64_t value)
{
    bool result = false;

    TIniParameterMap::iterator iter(m_parameterMap.find(valueTypeId));
    if (iter!=m_parameterMap.end())
    {
        result = assignNewIntValueWithCheck(value, &(iter->second));
    }
    //else do nothing

    return result;
}

bool IniConfiguration::getValueDouble(const EValueTypeId valueTypeId, double& value) const
{
    bool result = false;

    TIniParameterMap::const_iterator iter(m_parameterMap.find(valueTypeId));
    if (iter!=m_parameterMap.end())
    {
        if (
            (iter->second.getDoubleValue() >= iter->second.getMinDoubleValue()) &&
            (iter->second.getDoubleValue() <= iter->second.getMaxDoubleValue())
            )
        {
            result = true;
            value = iter->second.getDoubleValue();
        }
        else
        {
            value = iter->second.getDoubleValue();

            std::ostringstream ss;
            ss << "Invalid configuration value. The value of \"" << iter->second.getName()
                << " is not valid ("
                << iter->second.getDoubleValue() << ")";
            Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
        }
    }
    //else do nothing

    return result;
}

bool IniConfiguration::setValueDouble(const EValueTypeId valueTypeId, const double value)
{
    bool result = false;

    TIniParameterMap::iterator iter(m_parameterMap.find(valueTypeId));
    if (iter!=m_parameterMap.end())
    {
        result = assignNewDoubleValueWithCheck(value, &(iter->second));
    }
    //else do nothing

    return result;
}

bool IniConfiguration::getValueBool(const EValueTypeId valueTypeId, bool& value) const
{
    bool result = false;

    TIniParameterMap::const_iterator iter(m_parameterMap.find(valueTypeId));

    if (iter!=m_parameterMap.end())
    {
        switch (iter->second.getValue())
        {
            case 0:
            {
                result = true;
                value = false;

                break;
            }
            case 1:
            {
                result = true;
                value = true;

                break;
            }
            default:
            {
                //do nothing

                break;
            }
        } //switch
    }
    //else do nothing

    return result;
}

bool IniConfiguration::setValueBool(const EValueTypeId valueTypeId, const bool value)
{
    bool result = false;

    TIniParameterMap::iterator iter(m_parameterMap.find(valueTypeId));

    if (iter!=m_parameterMap.end())
    {
        result = true;

        if (value)
        {
            iter->second.setValue(1);
        }
        else
        {
            iter->second.setValue(0);
        }
    }
    //else do nothing

    return result;
}

bool IniConfiguration::getValueString(const EValueTypeId valueTypeId, std::string& value) const
{
    bool result = false;

    TIniParameterMap::const_iterator iter(m_parameterMap.find(valueTypeId));

    if (iter!=m_parameterMap.end())
    {
        result = true;
        value = iter->second.getTextValue();
    }
    //else do nothing

    return result;
}

bool IniConfiguration::setValueString(const EValueTypeId valueTypeId, const std::string& value)
{
    bool result = false;

    TIniParameterMap::iterator iter(m_parameterMap.find(valueTypeId));

    if (iter!=m_parameterMap.end())
    {
        result = true;
        iter->second.setTextValue(value);
    }
    //else do nothing

    return result;
}

void IniConfiguration::initialiseParameterMap()
{
    m_parameterMap[eOUTSTATION_MODE_LEGACY] = IniParameter("OutStationMode", eInt64, (int64_t)3, 0, 3);
    m_parameterMap[eOUTSTATION_MODE] = IniParameter("outStationMode", eInt64, (int64_t)3, 0, 3);
    m_parameterMap[eSETTINGS_COLLECTION_INTERVAL_1] = IniParameter("settingsCollectionInterval1", eInt64, (int64_t)15, 0, 60*24); //In minutes
    m_parameterMap[eSETTINGS_COLLECTION_INTERVAL_2] = IniParameter("settingsCollectionInterval2", eInt64, (int64_t)5, 0, 60*24); //In minutes
    m_parameterMap[eURL_CONGESTION_REPORTING] = IniParameter("urlCongestionReporting", eString, "");
    m_parameterMap[eURL_CONGESTION_REPORTS] = IniParameter("urlCongestionReports", eString, "");
    m_parameterMap[eURL_JOURNEY_TIMES_REPORTING] = IniParameter("urlJourneyTimesReporting", eString, "");
    m_parameterMap[eURL_ALERT_AND_STATUS_REPORTS] = IniParameter("urlAlertAndStatusReports", eString, "");
    m_parameterMap[eURL_STATUS_REPORTS] = IniParameter("urlStatusReports", eString, "");
    m_parameterMap[eURL_FAULT_REPORTS] = IniParameter("urlFaultReports", eString, "");
    m_parameterMap[eURL_STATISTICS_REPORTS] = IniParameter("urlStatisticsReports", eString, "");
    m_parameterMap[eINQUIRY_CYCLE_PERIOD] = IniParameter("inquiryCyclePeriod", eInt64, (int64_t)10, 0, 60);
    m_parameterMap[eINQUIRY_CYCLE_DURATION_IN_SECONDS] = IniParameter("inquiryCycleDurationInSeconds", eInt64, (int64_t)10, 0, 60);
    m_parameterMap[eINQUIRY_POWER] = IniParameter("inquiryPower", eInt64, (int64_t)20, -70, 20);
    m_parameterMap[eOBFUSCATING_FUNCTION] = IniParameter("obfuscatingFunction", eInt64, (int64_t)0, 0, 2);
    m_parameterMap[eHASHING_FUNCTION] = IniParameter("hashingFunction", eInt64, (int64_t)0, 0, 2);
    m_parameterMap[eHASHING_FUNCTION_SHA256_PRE_SEED] = IniParameter("hashingFunctionSHA256PreSeed", eString, "");
    m_parameterMap[eHASHING_FUNCTION_SHA256_POST_SEED] = IniParameter("hashingFunctionSHA256PostSeed", eString, "");
    m_parameterMap[eSTATISTICS_REPORTING_PERIOD_IN_SECONDS] = IniParameter("statisticsReportPeriodInSeconds", eInt64, (int64_t)60, 0, 10*60);
    m_parameterMap[eSTATISTICS_REPORT_CONTENTS] = IniParameter("statisticsReportContents", eString, "");
    m_parameterMap[eCONGESTION_REPORTING_PERIOD_IN_SECONDS] = IniParameter("congestionReportPeriodInSeconds", eInt64, (int64_t)60, 0, 10*60);
    m_parameterMap[eSTATUS_REPORTING_PERIOD_IN_SECONDS] = IniParameter("statusReportPeriodInSeconds", eInt64, (int64_t)600, 0, 60*60);
    m_parameterMap[eBACKGROUND_START_TIME_THRESHOLD_IN_SECONDS] = IniParameter("backgroundLatchTimeThresholdInSeconds", eInt64, (int64_t)0, 0, 24*60*60);
    m_parameterMap[eBACKGROUND_END_TIME_THRESHOLD_IN_SECONDS] = IniParameter("backgroundClearanceTimeThresholdInSeconds", eInt64, (int64_t)0, 0, 31*24*60*60);
    m_parameterMap[eFREE_FLOW_SPEED_CYCLES_THRESHOLD] = IniParameter("freeFlowSpeedCyclesThreshold", eInt64, (int64_t)0, 0, 10000);
    m_parameterMap[eFREE_FLOW_BIN_THRESHOLD_IN_SECONDS] = IniParameter("freeFlowBinThresholdInSeconds", eInt64, (int64_t)0, 0, 24*60*60);
    m_parameterMap[eMODERATE_SPEED_CYCLES_THRESHOLD] = IniParameter("moderateSpeedCyclesThreshold", eInt64, (int64_t)0, 0, 10000);
    m_parameterMap[eMODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS] = IniParameter("moderateFlowBinThresholdInSeconds", eInt64, (int64_t)0, 0, 24*60*60);
    m_parameterMap[eSLOW_SPEED_CYCLES_THRESHOLD] = IniParameter("slowSpeedCyclesThreshold", eInt64, (int64_t)0, 0, 10000);
    m_parameterMap[eSLOW_FLOW_BIN_THRESHOLD_IN_SECONDS] = IniParameter("slowFlowBinThresholdInSeconds", eInt64, (int64_t)0, 0, 24*60*60);
    m_parameterMap[eVERY_SLOW_SPEED_CYCLES_THRESHOLD] = IniParameter("verySlowSpeedCyclesThreshold", eInt64, (int64_t)0, 0, 10000);
    m_parameterMap[eVERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS] = IniParameter("verySlowFlowBinThresholdInSeconds", eInt64, (int64_t)0, 0, 24*60*60);
    m_parameterMap[eBIN_MAC_BIN_DROP_OUT_SCAN_CYCLE] = IniParameter("btMACBinDropOutScanCycle", eInt64, (int64_t)0, 0, 10000);
    m_parameterMap[eABSENCE_THRESHOLD_IN_SECONDS] = IniParameter("absenceThresholdInSeconds", eInt64, (int64_t)10, 0, 24*60*60);
    m_parameterMap[eQUEUE_ALERT_THRESHOLD_BIN] = IniParameter("queueAlertThresholdBin", eString, "");
    m_parameterMap[eQUEUE_DETECT_THRESHOLD] = IniParameter("queueDetectThreshold", eInt64, (int64_t)0, 0, 65535);
    m_parameterMap[eQUEUE_CLEARANCE_THRESHOLD_DETECTION_NUMBER] = IniParameter("queueClearanceThresholdDetectionNumber", eInt64, (int64_t)0, 0, 65535);
    m_parameterMap[eQUEUE_CLEARANCE_THRESHOLD] = IniParameter("queueClearanceThreshold", eInt64, (int64_t)0, 0, 65535);
    m_parameterMap[eQUEUE_DETECTION_STARTUP_INTERVAL_IN_SECONDS] = IniParameter("queueDetectionStatupIntervalInSeconds", eInt64, (int64_t)0, 0, 60*60);
    m_parameterMap[eSIGN_REPORTS] = IniParameter("signReports", eBool, true);
    m_parameterMap[eHTTP_TIMEOUT] = IniParameter("httpTimeOut", eInt64, (int64_t)15, 0, 10*60);
    m_parameterMap[eHTTP_RESPONSE_TIMEOUT_IN_SECONDS] = IniParameter("httpResponseTimeOutInSeconds", eInt64, (int64_t)15, 0, 10*60);
    m_parameterMap[eHTTP_CONNECTION_TIMEOUT_IN_SECONDS] = IniParameter("httpConnectionTimeOutInSeconds", eInt64, (int64_t)5*60, 0, 10*60);
    m_parameterMap[eREPORT_STORAGE_CAPACITY] = IniParameter("reportStorageCapacity", eInt64, (int64_t)1000, 100, 100000);
    m_parameterMap[eINITIAL_STARTUP_DELAY] = IniParameter("initialStartupDelay", eInt64, (int64_t)30, 0, 600);
    m_parameterMap[eGSM_MODEM_SIGNAL_LEVEL_SAMPLING_PERIOD_IN_SECONDS] = IniParameter("gsmModemSignalLevelSamplingPeriodInSeconds", eInt64, (int64_t)1*30, 0, 10*60);
    m_parameterMap[eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS] = IniParameter("gsmModemSignalLevelStatisticsWindowInSeconds", eInt64, (int64_t)5*60, 0, 60*60);
}


bool IniConfiguration::assignNewIntValueWithCheck(
    const int64_t value,
    IniParameter* pParameter)
{
    if (
        (pParameter == 0) ||
        (
            (pParameter->getValueType() != eInt64) &&
            (pParameter->getValueType() != eBool)
        )
       )
    {
        std::ostringstream ss;
        ss << "Invalid configuration value. The type (Int64) of value of \"" << pParameter->getName()
            << "\" is not consistent with the type to be used (" << pParameter->getTypeAsString() << ")";
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

        m_parameterError = true;
        return false;
    }
    //else continue

    bool result = false;
    pParameter->setValue(value);
    if (
        (pParameter->getValue() >= pParameter->getMinValue()) &&
        (pParameter->getValue() <= pParameter->getMaxValue())
        )
    {
        result = true;
    }
    else
    {
        std::ostringstream ss;
        ss << "Invalid configuration value. The value of \"" << pParameter->getName()
            << "\" is not valid ("
            << "current value: " << pParameter->getValue()
            << ", expected range: " << pParameter->getMinValue() << "..." << pParameter->getMaxValue() << ")";
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

        m_parameterError = true;
    }

    return result;
}

bool IniConfiguration::assignNewDoubleValueWithCheck(
    const double value,
    IniParameter* pParameter)
{
    if ((pParameter == 0) || (pParameter->getValueType() != eDouble))
    {
        std::ostringstream ss;
        ss << "Invalid configuration value. The type (Double) of value of \"" << pParameter->getName()
            << "\" is not consistent with the type to be used (" << pParameter->getTypeAsString() << ")";
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

        m_parameterError = true;
        return false;
    }
    //else continue

    bool result = false;
    pParameter->setDoubleValue(value);
    if (
        (pParameter->getDoubleValue() >= pParameter->getMinDoubleValue()) &&
        (pParameter->getDoubleValue() <= pParameter->getMaxDoubleValue())
        )
    {
        result = true;
    }
    else
    {
        std::ostringstream ss;
        ss << "Invalid configuration value. The value of \"" << pParameter->getName()
            << "\" is not valid ("
            << "current value: " << pParameter->getDoubleValue()
            << ", expected range: " << pParameter->getMinDoubleValue() << "..." << pParameter->getMaxDoubleValue() << ")";
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

        m_parameterError = true;
    }

    return result;
}

bool IniConfiguration::assignNewStringValueWithCheck(
    const std::string& value,
    IniParameter* pParameter)
{
    if ((pParameter == 0) || (pParameter->getValueType() != eString))
    {
        std::ostringstream ss;
        ss << "Invalid configuration value. The type (String) of value of \"" << pParameter->getName()
            << "\" is not consistent with the type to be used (" << pParameter->getTypeAsString() << ")";
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

        m_parameterError = true;
        return false;
    }
    //else continue

    pParameter->setTextValue(value);

    return true;
}

bool IniConfiguration::assignNewValueByName(
    const std::string& entryName,
    const TConfigurationItem& entryValue)
{
    bool result = false;

    for (TIniParameterMap::iterator iter(m_parameterMap.begin()), iterEnd(m_parameterMap.end());
        iter != iterEnd;
        ++iter)
    {
        if (entryName == iter->second.getName())
        {
            result = true;

            switch (entryValue.type)
            {
                case TConfigurationItem::eTYPE_INT:
                {
                    assignNewIntValueWithCheck(entryValue.valueInt, &(iter->second));
                    break;
                }

                case TConfigurationItem::eTYPE_DOUBLE:
                {
                    assignNewDoubleValueWithCheck(entryValue.valueDouble, &(iter->second));
                    break;
                }

                case TConfigurationItem::eTYPE_STRING:
                {
                    assignNewStringValueWithCheck(entryValue.valueString, &(iter->second));
                    break;
                }

                default:
                {
                    break;
                }
            }

            if (result)
            {
                break;
            }
            //else continue
        }
    } //for

    return result;
}

std::string IniConfiguration::getMD5Hash() const
{
    DiggestType md5Hash = {0};

    calculateMD5FromString(
        (unsigned char*)m_configurationText.c_str(),
        m_configurationText.size(),
        &md5Hash);

    //Copy the result
    std::ostringstream result;
    for (size_t i=0; i<Model::SIZEOF_DIGEST; ++i)
    {
        result << std::hex << (int)md5Hash[i];
    }

    return result.str();
}

void IniConfiguration::printAllValuesToTheLog()
{
    std::ostringstream ss;
    ss << "Functional configuration file values to be used (some of the values may be not relevant):\n";

    for (TIniParameterMap::const_iterator iter(m_parameterMap.begin()), iterEnd(m_parameterMap.end());
        iter != iterEnd;
        ++iter)
    {
        switch (iter->second.getValueType())
        {
            case eInt64:
            case eBool:
            {
                ss << iter->second.getName() << "=" << iter->second.getValue() << "\n";
                break;
            }
            case eDouble:
            {
                ss << iter->second.getName() << "=" << iter->second.getDoubleValue() << "\n";
                break;
            }
            case eString:
            {
                ss << iter->second.getName() << "=" << iter->second.getTextValue() << "\n";
                break;
            }
            default:
            {
                break;
            }
        }
    }

    Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
}

void IniConfiguration::overwriteValuesWithCommandLineParameters(const std::string& statusReportURL)
{
    if (!statusReportURL.empty())
    {
        setValueString(eURL_STATUS_REPORTS, statusReportURL);

        std::ostringstream ss;
        ss << "Functional configuration urlStatusReports value has been overridden by a value read from the command line: " << statusReportURL << "\n";
        Logger::log(LOG_LEVEL_INFO, ss.str().c_str());

        ss.str("");
        ss << "\n#urlStatusReports value has been overridden by a value read from the command line: " << statusReportURL << "\n";
        m_configurationText += ss.str();
    }
    //else do nothing
}

}
