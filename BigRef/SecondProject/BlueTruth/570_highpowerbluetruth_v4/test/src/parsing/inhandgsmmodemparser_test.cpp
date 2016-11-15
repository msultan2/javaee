#include "stdafx.h"
#include <gtest/gtest.h>

#include "inhandgsmmodemoutputparser.h"
#include "gsmmodem/activegsmmodemsshmonitor.h"
#include "gsmmodem/gsmmodemsignallevelprocessor.h"

/*
 *Test sequences:

 *
Modem Type           : EM770W
Status               : ok
Manufacturer         : Huawei
Product              : EM770W
Signal Level         : 19 asu -75 dBm
Register Status      : registered
IMEI(ESN) Code       : 357789049141176
IMSI Code            : 260031997493264
Network Type         : 2G
PLMN                 : 26003
LAC                  : DF16
Cell ID              : 484A
Router>
 *
Modem Type           : EM770W
Status               : ok
Manufacturer         : Huawei
Product              : EM770W
Signal Level         : 0 asu -113 dBm
Register Status      : registered
IMEI(ESN) Code       : 357789049141176
IMSI Code            : 260031997493264
Network Type         : 2G
PLMN                 : 26003
LAC                  : DF16
Cell ID              : 484A
Router>
 *
A different SIM card:
 *
Modem Type           : EM770W
Status               : ok
Manufacturer         : Huawei
Product              : EM770W
Signal Level         : 17 asu -79 dBm
Register Status      : registered
IMEI(ESN) Code       : 357789049141176
IMSI Code            : 260034556909182
Network Type         : 2G
PLMN                 : 26003
LAC                  : DF16
Cell ID              : 484A
Router>
 *
A different router
Router> show modem
Modem Type           : MC55I
Status               : ok
Manufacturer         : Siemens
Product              : MC55I
Signal Level         : 27
Register Status      : registered
IMEI(ESN) Code       : 359108042821528
IMSI Code            : 234159151742089
Network Type         : 2G
PLMN                 : 23415
LAC                  : 005D
Cell ID              : A9C5
Router>
 */


TEST(InhandGSMModemOutputParser, parse)
{
    {
        std::string input("");
        TInhandGSMModemOutputContext parsingResult;
        bool ok = InhandGSMModemOutputParser::parse(input, parsingResult);

        EXPECT_FALSE(ok);
        EXPECT_TRUE(parsingResult.output.lineCollection.empty());
    }

    { //Nomral use case
        std::string input(
"Modem Type           : EM770W\n"
"Status               : ok\n"
"Manufacturer         : Huawei\n"
"Product              : EM770W\n"
"Signal Level         : 23 asu -67 dBm\n"
"Register Status      : registered\n"
"IMEI(ESN) Code       : 357789049141176\n"
"IMSI Code            : 260031997493264\n"
"Network Type         : 2G\n"
"PLMN                 : 26003\n"
"LAC                  : DF16\n"
"Cell ID              : C5E0\n"
"Router> "
);
        TInhandGSMModemOutputContext parsingResult;
        bool ok = InhandGSMModemOutputParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_FALSE(parsingResult.output.lineCollection.empty());

        TInhandGSMModemOutput::TLineCollection::const_iterator iter =
            parsingResult.output.lineCollection.find("Signal Level");
        EXPECT_TRUE(iter != parsingResult.output.lineCollection.end());
        if (iter != parsingResult.output.lineCollection.end())
            EXPECT_STREQ("23 asu -67 dBm", iter->second.c_str());
    }

    { //This is the case of modem after switching on but before
    //connecting to the base stations (to reproduce disconnect
    //antenna and restart modem)
        std::string input(
"Modem Type           : EM770W\n"
"Status               : detecting\n"
"Manufacturer         : Huawei\n"
"Product              : EM770W\n"
"Signal Level         : 0 asu -113 dBm\n"
"Register Status      :\n"
"IMEI(ESN) Code       : 357789049141176\n"
"IMSI Code            :\n"
"Network Type         :\n"
"PLMN                 :\n"
"LAC                  :\n"
"Cell ID              :\n"
"Router>"
);
        TInhandGSMModemOutputContext parsingResult;
        bool ok = InhandGSMModemOutputParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_FALSE(parsingResult.output.lineCollection.empty());

        TInhandGSMModemOutput::TLineCollection::const_iterator iter =
            parsingResult.output.lineCollection.find("Signal Level");
        EXPECT_TRUE(iter != parsingResult.output.lineCollection.end());
        if (iter != parsingResult.output.lineCollection.end())
            EXPECT_STREQ("0 asu -113 dBm", iter->second.c_str());
    }
}

TEST(ActiveGSMModemSshMonitor, extractSignalValue)
{
    { //This is the case of modem after switching on but before
    //connecting to the base stations (to reproduce disconnect
    //antenna and restart modem)
        std::string input(
"Modem Type           : EM770W\n"
"Status               : ok\n"
"Manufacturer         : Huawei\n"
"Product              : EM770W\n"
"Signal Level         : 23 asu -67 dBm\n"
"Register Status      : registered\n"
"IMEI(ESN) Code       : 357789049141176\n"
"IMSI Code            : 260031997493264\n"
"Network Type         : 2G\n"
"PLMN                 : 26003\n"
"LAC                  : DF16\n"
"Cell ID              : C5E0\n"
"Router> "
);

        int gpsModemSignalLevel = 0xFFFF;
        bool result = Model::extractSignalValue(input, gpsModemSignalLevel);
        EXPECT_TRUE(result);
        EXPECT_EQ(23, gpsModemSignalLevel);
    }

    { //This is the case of modem after switching on but before
    //connecting to the base stations (to reproduce disconnect
    //antenna and restart modem)
        std::string input(
"Modem Type           : MC55I\n"
"Status               : ok\n"
"Manufacturer         : Siemens\n"
"Product              : MC55I\n"
"Signal Level         : 27\n"
"Register Status      : registered\n"
"IMEI(ESN) Code       : 359108042821528\n"
"IMSI Code            : 234159151742089\n"
"Network Type         : 2G\n"
"PLMN                 : 23415\n"
"LAC                  : 005D\n"
"Cell ID              : A9C5\n"
"Router>"
);

        int gpsModemSignalLevel = 0xFFFF;
        bool result = Model::extractSignalValue(input, gpsModemSignalLevel);
        EXPECT_TRUE(result);
        EXPECT_EQ(27, gpsModemSignalLevel);
    }

    { //Empty signal value
        std::string input(
"Modem Type           : MC55I\n"
"Status               : ok\n"
"Manufacturer         : Siemens\n"
"Product              : MC55I\n"
"Signal Level         :\n"
"Register Status      : registered\n"
"IMEI(ESN) Code       : 359108042821528\n"
"IMSI Code            : 234159151742089\n"
"Network Type         : 2G\n"
"PLMN                 : 23415\n"
"LAC                  : 005D\n"
"Cell ID              : A9C5\n"
"Router>"
);

        int gpsModemSignalLevel = 0xFFFF;
        bool result = Model::extractSignalValue(input, gpsModemSignalLevel);
        EXPECT_FALSE(result);
    }

    { //Invalid signal value
        std::string input(
"Modem Type           : MC55I\n"
"Status               : ok\n"
"Manufacturer         : Siemens\n"
"Product              : MC55I\n"
"Signal Level         : error\n"
"Register Status      : registered\n"
"IMEI(ESN) Code       : 359108042821528\n"
"IMSI Code            : 234159151742089\n"
"Network Type         : 2G\n"
"PLMN                 : 23415\n"
"LAC                  : 005D\n"
"Cell ID              : A9C5\n"
"Router>"
);

        int gpsModemSignalLevel = 0xFFFF;
        bool result = Model::extractSignalValue(input, gpsModemSignalLevel);
        EXPECT_FALSE(result);
    }
};
