#include "stdafx.h"
#include <gtest/gtest.h>

#include "configuration/iniconfiguration.h"

#include "applicationconfiguration.h"
#include "os_utilities.h"


using Model::IniConfiguration;


TEST(IniConfiguration, defaultValues)
{
    IniConfiguration configuration(std::string(""));

    ASSERT_TRUE(configuration.isValid());

    int64_t intValue = 0;
    std::string strValue;

    ASSERT_TRUE(configuration.getValueInt64(Model::eOUTSTATION_MODE_LEGACY, intValue));
    EXPECT_EQ(3, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eOUTSTATION_MODE, intValue));
    EXPECT_EQ(3, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eSETTINGS_COLLECTION_INTERVAL_1, intValue));
    EXPECT_EQ(15, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eSETTINGS_COLLECTION_INTERVAL_2, intValue));
    EXPECT_EQ(5, intValue);
    ASSERT_TRUE(configuration.getValueString(Model::eURL_CONGESTION_REPORTING, strValue));
    EXPECT_TRUE(strValue.empty());
    ASSERT_TRUE(configuration.getValueString(Model::eURL_CONGESTION_REPORTS, strValue));
    EXPECT_TRUE(strValue.empty());
    ASSERT_TRUE(configuration.getValueString(Model::eURL_JOURNEY_TIMES_REPORTING, strValue));
    EXPECT_TRUE(strValue.empty());
    ASSERT_TRUE(configuration.getValueString(Model::eURL_ALERT_AND_STATUS_REPORTS, strValue));
    EXPECT_TRUE(strValue.empty());
    ASSERT_TRUE(configuration.getValueString(Model::eURL_STATUS_REPORTS, strValue));
    EXPECT_TRUE(strValue.empty());
    ASSERT_TRUE(configuration.getValueString(Model::eURL_FAULT_REPORTS, strValue));
    EXPECT_TRUE(strValue.empty());
    ASSERT_TRUE(configuration.getValueString(Model::eURL_STATISTICS_REPORTS, strValue));
    EXPECT_TRUE(strValue.empty());
    ASSERT_TRUE(configuration.getValueInt64(Model::eINQUIRY_CYCLE_PERIOD, intValue));
    EXPECT_EQ(10, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eINQUIRY_CYCLE_DURATION_IN_SECONDS, intValue));
    EXPECT_EQ(10, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eINQUIRY_POWER, intValue));
    EXPECT_EQ(20, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eOBFUSCATING_FUNCTION, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eSTATISTICS_REPORTING_PERIOD_IN_SECONDS, intValue));
    EXPECT_EQ(60, intValue);
    ASSERT_TRUE(configuration.getValueString(Model::eSTATISTICS_REPORT_CONTENTS, strValue));
    EXPECT_STREQ("", strValue.c_str());
    ASSERT_TRUE(configuration.getValueInt64(Model::eCONGESTION_REPORTING_PERIOD_IN_SECONDS, intValue));
    EXPECT_EQ(60, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eSTATUS_REPORTING_PERIOD_IN_SECONDS, intValue));
    EXPECT_EQ(600, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eBACKGROUND_START_TIME_THRESHOLD_IN_SECONDS, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eBACKGROUND_END_TIME_THRESHOLD_IN_SECONDS, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eFREE_FLOW_SPEED_CYCLES_THRESHOLD, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eFREE_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eMODERATE_SPEED_CYCLES_THRESHOLD, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eMODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eSLOW_SPEED_CYCLES_THRESHOLD, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eSLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eVERY_SLOW_SPEED_CYCLES_THRESHOLD, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eVERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eBIN_MAC_BIN_DROP_OUT_SCAN_CYCLE, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eABSENCE_THRESHOLD_IN_SECONDS, intValue));
    EXPECT_EQ(10, intValue);
    ASSERT_TRUE(configuration.getValueString(Model::eQUEUE_ALERT_THRESHOLD_BIN, strValue));
    EXPECT_TRUE(strValue.empty());
    ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_DETECT_THRESHOLD, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_CLEARANCE_THRESHOLD_DETECTION_NUMBER, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_CLEARANCE_THRESHOLD, intValue));
    EXPECT_EQ(0, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eSIGN_REPORTS, intValue));
    EXPECT_EQ(1, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eHTTP_TIMEOUT, intValue));
    EXPECT_EQ(15, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eHTTP_RESPONSE_TIMEOUT_IN_SECONDS, intValue));
    EXPECT_EQ(15, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eHTTP_CONNECTION_TIMEOUT_IN_SECONDS, intValue));
    EXPECT_EQ(300, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eREPORT_STORAGE_CAPACITY, intValue));
    EXPECT_EQ(1000, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eINITIAL_STARTUP_DELAY, intValue));
    EXPECT_EQ(30, intValue);
    ASSERT_TRUE(configuration.getValueInt64(Model::eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS, intValue));
    EXPECT_EQ(300, intValue);
}

TEST(IniConfiguration, ranges_OutStationModeLegacy)
{
    {
        IniConfiguration configuration(std::string(
            "OutStationMode=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eOUTSTATION_MODE_LEGACY, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "OutStationMode=3\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eOUTSTATION_MODE_LEGACY, intValue));
        EXPECT_EQ(3, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "OutStationMode=4\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_OutStationMode)
{
    {
        IniConfiguration configuration(std::string(
            "outStationMode=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eOUTSTATION_MODE, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "outStationMode=3\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eOUTSTATION_MODE, intValue));
        EXPECT_EQ(3, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "outStationMode=4\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_settingsCollectionInterval1)
{
    {
        IniConfiguration configuration(std::string(
            "settingsCollectionInterval1=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSETTINGS_COLLECTION_INTERVAL_1, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "settingsCollectionInterval1=1440\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSETTINGS_COLLECTION_INTERVAL_1, intValue));
        EXPECT_EQ(1440, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "settingsCollectionInterval1=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "settingsCollectionInterval1=1441\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_settingsCollectionInterval2)
{
    {
        IniConfiguration configuration(std::string(
            "settingsCollectionInterval2=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSETTINGS_COLLECTION_INTERVAL_2, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "settingsCollectionInterval2=1440\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSETTINGS_COLLECTION_INTERVAL_2, intValue));
        EXPECT_EQ(1440, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "settingsCollectionInterval2=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "settingsCollectionInterval2=1441\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_inquiryCyclePeriod)
{
    {
        IniConfiguration configuration(std::string(
            "inquiryCyclePeriod=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eINQUIRY_CYCLE_PERIOD, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "inquiryCyclePeriod=60\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eINQUIRY_CYCLE_PERIOD, intValue));
        EXPECT_EQ(60, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "inquiryCyclePeriod=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "inquiryCyclePeriod=61\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_inquiryCycleDurationInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "inquiryCycleDurationInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eINQUIRY_CYCLE_DURATION_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "inquiryCycleDurationInSeconds=60\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eINQUIRY_CYCLE_DURATION_IN_SECONDS, intValue));
        EXPECT_EQ(60, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "inquiryCycleDurationInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "inquiryCycleDurationInSeconds=61\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_inquiryPower)
{
    {
        IniConfiguration configuration(std::string(
            "inquiryPower=-70\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eINQUIRY_POWER, intValue));
        EXPECT_EQ(-70, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "inquiryPower=20\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eINQUIRY_POWER, intValue));
        EXPECT_EQ(20, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "inquiryPower=-71\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "inquiryPower=21\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_obfuscatingFunction)
{
    {
        IniConfiguration configuration(std::string(
            "obfuscatingFunction=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eOBFUSCATING_FUNCTION, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "obfuscatingFunction=2\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eOBFUSCATING_FUNCTION, intValue));
        EXPECT_EQ(2, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "obfuscatingFunction=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "obfuscatingFunction=3\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_statisticsReportPeriodInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "statisticsReportPeriodInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSTATISTICS_REPORTING_PERIOD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "statisticsReportPeriodInSeconds=600\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSTATISTICS_REPORTING_PERIOD_IN_SECONDS, intValue));
        EXPECT_EQ(600, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "statisticsReportPeriodInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "statisticsReportPeriodInSeconds=601\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_congestionReportPeriodInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "congestionReportPeriodInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eCONGESTION_REPORTING_PERIOD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "congestionReportPeriodInSeconds=600\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eCONGESTION_REPORTING_PERIOD_IN_SECONDS, intValue));
        EXPECT_EQ(600, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "congestionReportPeriodInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "congestionReportPeriodInSeconds=601\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_statusReportPeriodInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "statusReportPeriodInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSTATUS_REPORTING_PERIOD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "statusReportPeriodInSeconds=3600\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSTATUS_REPORTING_PERIOD_IN_SECONDS, intValue));
        EXPECT_EQ(3600, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "statusReportPeriodInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "statusReportPeriodInSeconds=3601\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_backgroundLatchTimeThresholdInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "backgroundLatchTimeThresholdInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eBACKGROUND_START_TIME_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "backgroundLatchTimeThresholdInSeconds=86400\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eBACKGROUND_START_TIME_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(86400, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "backgroundLatchTimeThresholdInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "backgroundLatchTimeThresholdInSeconds=86401\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_backgroundClearanceTimeThresholdInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "backgroundClearanceTimeThresholdInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eBACKGROUND_END_TIME_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "backgroundClearanceTimeThresholdInSeconds=86400\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eBACKGROUND_END_TIME_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(86400, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "backgroundClearanceTimeThresholdInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "backgroundClearanceTimeThresholdInSeconds=2678401\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_freeFlowSpeedCyclesThreshold)
{
    {
        IniConfiguration configuration(std::string(
            "freeFlowSpeedCyclesThreshold=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eFREE_FLOW_SPEED_CYCLES_THRESHOLD, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "freeFlowSpeedCyclesThreshold=10000\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eFREE_FLOW_SPEED_CYCLES_THRESHOLD, intValue));
        EXPECT_EQ(10000, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "freeFlowSpeedCyclesThreshold=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "freeFlowSpeedCyclesThreshold=10001\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_freeFlowBinThresholdInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "freeFlowBinThresholdInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eFREE_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "freeFlowBinThresholdInSeconds=86400\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eFREE_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(86400, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "freeFlowBinThresholdInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "freeFlowBinThresholdInSeconds=86401\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_moderateSpeedCyclesThreshold)
{
    {
        IniConfiguration configuration(std::string(
            "moderateSpeedCyclesThreshold=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eMODERATE_SPEED_CYCLES_THRESHOLD, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "moderateSpeedCyclesThreshold=10000\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eMODERATE_SPEED_CYCLES_THRESHOLD, intValue));
        EXPECT_EQ(10000, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "moderateSpeedCyclesThreshold=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "moderateSpeedCyclesThreshold=10001\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_moderateFlowBinThresholdInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "moderateFlowBinThresholdInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eMODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "moderateFlowBinThresholdInSeconds=86400\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eMODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(86400, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "moderateFlowBinThresholdInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "moderateFlowBinThresholdInSeconds=86401\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_slowSpeedCyclesThreshold)
{
    {
        IniConfiguration configuration(std::string(
            "slowSpeedCyclesThreshold=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSLOW_SPEED_CYCLES_THRESHOLD, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "slowSpeedCyclesThreshold=10000\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSLOW_SPEED_CYCLES_THRESHOLD, intValue));
        EXPECT_EQ(10000, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "slowSpeedCyclesThreshold=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "slowSpeedCyclesThreshold=10001\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_slowFlowBinThresholdInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "slowFlowBinThresholdInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "slowFlowBinThresholdInSeconds=86400\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(86400, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "slowFlowBinThresholdInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "slowFlowBinThresholdInSeconds=86401\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_verySlowSpeedCyclesThreshold)
{
    {
        IniConfiguration configuration(std::string(
            "verySlowSpeedCyclesThreshold=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eVERY_SLOW_SPEED_CYCLES_THRESHOLD, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "verySlowSpeedCyclesThreshold=10000\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eVERY_SLOW_SPEED_CYCLES_THRESHOLD, intValue));
        EXPECT_EQ(10000, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "verySlowSpeedCyclesThreshold=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "verySlowSpeedCyclesThreshold=10001\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_verySlowFlowBinThresholdInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "verySlowFlowBinThresholdInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eVERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "verySlowFlowBinThresholdInSeconds=86400\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eVERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(86400, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "verySlowFlowBinThresholdInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "verySlowFlowBinThresholdInSeconds=86401\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_btMACBinDropOutScanCycle)
{
    {
        IniConfiguration configuration(std::string(
            "btMACBinDropOutScanCycle=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eBIN_MAC_BIN_DROP_OUT_SCAN_CYCLE, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "btMACBinDropOutScanCycle=10000\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eBIN_MAC_BIN_DROP_OUT_SCAN_CYCLE, intValue));
        EXPECT_EQ(10000, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "btMACBinDropOutScanCycle=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "btMACBinDropOutScanCycle=10001\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_absenceThresholdInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "absenceThresholdInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eABSENCE_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "absenceThresholdInSeconds=86400\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eABSENCE_THRESHOLD_IN_SECONDS, intValue));
        EXPECT_EQ(86400, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "absenceThresholdInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "absenceThresholdInSeconds=86401\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_queueAlertThresholdBin)
{
    {
        IniConfiguration configuration(std::string(
            "queueAlertThresholdBin=freeFlow\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        std::string strValue;
        ASSERT_TRUE(configuration.getValueString(Model::eQUEUE_ALERT_THRESHOLD_BIN, strValue));
        EXPECT_STREQ("freeFlow", strValue.c_str());
    }

    {
        IniConfiguration configuration(std::string(
            "queueAlertThresholdBin=1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_queueDetectThreshold)
{
    {
        IniConfiguration configuration(std::string(
            "queueDetectThreshold=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_DETECT_THRESHOLD, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "queueDetectThreshold=65535\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_DETECT_THRESHOLD, intValue));
        EXPECT_EQ(65535, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "queueDetectThreshold=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "queueDetectThreshold=65536\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_queueClearanceThresholdDetectionNumber)
{
    {
        IniConfiguration configuration(std::string(
            "queueClearanceThresholdDetectionNumber=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_CLEARANCE_THRESHOLD_DETECTION_NUMBER, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "queueClearanceThresholdDetectionNumber=65535\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_CLEARANCE_THRESHOLD_DETECTION_NUMBER, intValue));
        EXPECT_EQ(65535, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "queueClearanceThresholdDetectionNumber=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "queueClearanceThresholdDetectionNumber=65536\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_queueClearanceThreshold)
{
    {
        IniConfiguration configuration(std::string(
            "queueClearanceThreshold=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_CLEARANCE_THRESHOLD, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "queueClearanceThreshold=65535\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_CLEARANCE_THRESHOLD, intValue));
        EXPECT_EQ(65535, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "queueClearanceThreshold=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "queueClearanceThreshold=65536\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_queueDetectionStatupIntervalInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "queueDetectionStatupIntervalInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_DETECTION_STARTUP_INTERVAL_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "queueDetectionStatupIntervalInSeconds=3600\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eQUEUE_DETECTION_STARTUP_INTERVAL_IN_SECONDS, intValue));
        EXPECT_EQ(3600, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "queueDetectionStatupIntervalInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "queueDetectionStatupIntervalInSeconds=3601\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_signReports)
{
    {
        IniConfiguration configuration(std::string(
            "signReports=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSIGN_REPORTS, intValue));
        EXPECT_EQ(0, intValue);
        bool boolValue = true;
        ASSERT_TRUE(configuration.getValueBool(Model::eSIGN_REPORTS, boolValue));
        EXPECT_FALSE(boolValue);
    }

    {
        IniConfiguration configuration(std::string(
            "signReports=1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eSIGN_REPORTS, intValue));
        EXPECT_EQ(1, intValue);
        bool boolValue = false;
        ASSERT_TRUE(configuration.getValueBool(Model::eSIGN_REPORTS, boolValue));
        EXPECT_TRUE(boolValue);
    }

    {
        IniConfiguration configuration(std::string(
            "signReports=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "signReports=2\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_httpTimeOut)
{
    {
        IniConfiguration configuration(std::string(
            "httpTimeOut=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eHTTP_TIMEOUT, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "httpTimeOut=600\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eHTTP_TIMEOUT, intValue));
        EXPECT_EQ(600, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "httpTimeOut=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "httpTimeOut=601\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_httpResponseTimeOutInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "httpResponseTimeOutInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eHTTP_RESPONSE_TIMEOUT_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "httpResponseTimeOutInSeconds=600\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eHTTP_RESPONSE_TIMEOUT_IN_SECONDS, intValue));
        EXPECT_EQ(600, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "httpResponseTimeOutInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "httpResponseTimeOutInSeconds=601\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_httpConnectionTimeOutInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "httpConnectionTimeOutInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eHTTP_CONNECTION_TIMEOUT_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "httpConnectionTimeOutInSeconds=600\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eHTTP_CONNECTION_TIMEOUT_IN_SECONDS, intValue));
        EXPECT_EQ(600, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "httpConnectionTimeOutInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "httpConnectionTimeOutInSeconds=601\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_gsmModemSignalLevelSamplingPeriodInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "gsmModemSignalLevelSamplingPeriodInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eGSM_MODEM_SIGNAL_LEVEL_SAMPLING_PERIOD_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "gsmModemSignalLevelSamplingPeriodInSeconds=600\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eGSM_MODEM_SIGNAL_LEVEL_SAMPLING_PERIOD_IN_SECONDS, intValue));
        EXPECT_EQ(600, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "gsmModemSignalLevelSamplingPeriodInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "gsmModemSignalLevelSamplingPeriodInSeconds=601\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_gsmModemSignalLevelStatisticsWindowInSeconds)
{
    {
        IniConfiguration configuration(std::string(
            "gsmModemSignalLevelStatisticsWindowInSeconds=0\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS, intValue));
        EXPECT_EQ(0, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "gsmModemSignalLevelStatisticsWindowInSeconds=3600\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS, intValue));
        EXPECT_EQ(3600, intValue);
    }

    {
        IniConfiguration configuration(std::string(
            "gsmModemSignalLevelStatisticsWindowInSeconds=-1\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }

    {
        IniConfiguration configuration(std::string(
            "gsmModemSignalLevelStatisticsWindowInSeconds=3601\n"
            ));

        ASSERT_TRUE(configuration.isValid());
        ASSERT_TRUE(configuration.isParameterErrorSet());
    }
}

TEST(IniConfiguration, ranges_assignmentOperator)
{
    {
        IniConfiguration configuration(
            "gsmModemSignalLevelStatisticsWindowInSeconds=60\n"
            );

        IniConfiguration copiedConfiguration(std::string(""));
        copiedConfiguration = configuration;

        ASSERT_TRUE(copiedConfiguration.isValid());
        ASSERT_FALSE(copiedConfiguration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(copiedConfiguration.getValueInt64(Model::eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS, intValue));
        EXPECT_EQ(60, intValue);
    }
}

TEST(IniConfiguration, ranges_unknownParameter)
{
    {
        IniConfiguration configuration(std::string(
            "gsmModemSignalLevelStatisticsWindowInSecondsx=50\n"
            ));

        IniConfiguration copiedConfiguration(std::string(""));
        copiedConfiguration = configuration;

        ASSERT_TRUE(copiedConfiguration.isValid());
        ASSERT_FALSE(copiedConfiguration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(copiedConfiguration.getValueInt64(Model::eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS, intValue));
        EXPECT_EQ(300, intValue);
    }
}

TEST(IniConfiguration, ranges_saveAndLoadFromFile)
{
    { //First save the file (last_functional_configuration.conf)
        IniConfiguration configuration(
            "gsmModemSignalLevelStatisticsWindowInSeconds=55\n"
            );

        ASSERT_TRUE(configuration.saveToFile());
    }

    { //Read the file from cache directory (last_functional_configuration.conf)
        IniConfiguration configuration;
        ASSERT_TRUE(configuration.loadFromFile(IniConfiguration::LOAD_FILE_FROM_CACHE_DIRECTORY));
        configuration.processConfigurationText();

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS, intValue));
        EXPECT_EQ(55, intValue);
    }

    { //Read the file from system conf directory. The file does not exist yet (functional_configuration.conf)
        IniConfiguration configuration; //should be from temporary directory anyway

        ASSERT_FALSE(configuration.isValid());
    }

    { //Change the file name so that it matches the system conf one
        std::string systemConfFileName;
        systemConfFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getSysConfDirectory());
        systemConfFileName += IniConfiguration::INSTALLATION_CONFIGURATION_FILE_NAME;

        std::string cacheFileName;
        cacheFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getCacheDirectory());
        cacheFileName += IniConfiguration::LAST_SAVED_CONFIGURATION_FILE_NAME;

        std::string command;
        command += "mv ";
        command += cacheFileName;
        command += " ";
        command += systemConfFileName;

        ::system(command.c_str());
    }

    { //Read the file from system conf directory (functional_configuration.conf)
        IniConfiguration configuration;
        ASSERT_TRUE(configuration.loadFromFile(IniConfiguration::LOAD_FILE_FROM_SYSTEM_CONFIGURATION_DIRECTORY));
        configuration.processConfigurationText();

        ASSERT_TRUE(configuration.isValid());
        ASSERT_FALSE(configuration.isParameterErrorSet());
        int64_t intValue = 0;
        ASSERT_TRUE(configuration.getValueInt64(Model::eGSM_MODEM_SIGNAL_LEVEL_STATISTICS_WINDOW_IN_SECONDS, intValue));
        EXPECT_EQ(55, intValue);
    }

    { //Remove the file
        std::string systemConfFileName;
        systemConfFileName += OS_Utilities::StringToAnsi(BlueTruth::ApplicationConfiguration::getSysConfDirectory());
        systemConfFileName += IniConfiguration::INSTALLATION_CONFIGURATION_FILE_NAME;

        std::string command;
        command += "rm -f ";
        command += systemConfFileName;

        ::system(command.c_str());
    }

}
