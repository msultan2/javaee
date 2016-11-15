#include "stdafx.h"
#include <gtest/gtest.h>

#include "responsebodyparser.h"


TEST(ResponseBodyParser, parse)
{
    {
        std::string input("");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_FALSE(parsingResult.body.reloadConfiguration);
        EXPECT_FALSE(parsingResult.body.openSSHConnection);
        EXPECT_FALSE(parsingResult.body.closeSSHConnection);
        EXPECT_FALSE(parsingResult.body.getStatusReport);
        EXPECT_FALSE(parsingResult.body.reboot);
        EXPECT_FALSE(parsingResult.body.changeSeed);
    }

    {
        std::string input("reloadConfiguration");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_TRUE(parsingResult.body.reloadConfiguration);
        EXPECT_FALSE(parsingResult.body.openSSHConnection);
        EXPECT_FALSE(parsingResult.body.closeSSHConnection);
        EXPECT_FALSE(parsingResult.body.getStatusReport);
        EXPECT_FALSE(parsingResult.body.reboot);
        EXPECT_FALSE(parsingResult.body.changeSeed);
    }

    {
        std::string input("openSSHConnection:16");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_FALSE(parsingResult.body.reloadConfiguration);
        EXPECT_TRUE(parsingResult.body.openSSHConnection);
        EXPECT_EQ(16, parsingResult.body.remotePortNumber);
        EXPECT_FALSE(parsingResult.body.closeSSHConnection);
        EXPECT_FALSE(parsingResult.body.getStatusReport);
        EXPECT_FALSE(parsingResult.body.reboot);
        EXPECT_FALSE(parsingResult.body.changeSeed);
    }

    {
        std::string input("closeSSHConnection");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_FALSE(parsingResult.body.reloadConfiguration);
        EXPECT_FALSE(parsingResult.body.openSSHConnection);
        EXPECT_TRUE(parsingResult.body.closeSSHConnection);
        EXPECT_FALSE(parsingResult.body.getStatusReport);
        EXPECT_FALSE(parsingResult.body.reboot);
        EXPECT_FALSE(parsingResult.body.changeSeed);
    }

    {
        std::string input("getStatusReport");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_FALSE(parsingResult.body.reloadConfiguration);
        EXPECT_FALSE(parsingResult.body.openSSHConnection);
        EXPECT_FALSE(parsingResult.body.closeSSHConnection);
        EXPECT_TRUE(parsingResult.body.getStatusReport);
        EXPECT_FALSE(parsingResult.body.reboot);
        EXPECT_FALSE(parsingResult.body.changeSeed);
    }

    {
        std::string input("reboot");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_FALSE(parsingResult.body.reloadConfiguration);
        EXPECT_FALSE(parsingResult.body.openSSHConnection);
        EXPECT_FALSE(parsingResult.body.closeSSHConnection);
        EXPECT_FALSE(parsingResult.body.getStatusReport);
        EXPECT_TRUE(parsingResult.body.reboot);
        EXPECT_FALSE(parsingResult.body.changeSeed);
    }

    {
        std::string input("changeSeed");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_FALSE(parsingResult.body.reloadConfiguration);
        EXPECT_FALSE(parsingResult.body.openSSHConnection);
        EXPECT_FALSE(parsingResult.body.closeSSHConnection);
        EXPECT_FALSE(parsingResult.body.getStatusReport);
        EXPECT_FALSE(parsingResult.body.reboot);
        EXPECT_TRUE(parsingResult.body.changeSeed);
    }

    {
        std::string input("reloadConfiguration,closeSSHConnection");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_TRUE(parsingResult.body.reloadConfiguration);
        EXPECT_FALSE(parsingResult.body.openSSHConnection);
        EXPECT_TRUE(parsingResult.body.closeSSHConnection);
    }

    {
        std::string input("reloadConfiguration,openSSHConnection:16");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_TRUE(parsingResult.body.reloadConfiguration);
        EXPECT_TRUE(parsingResult.body.openSSHConnection);
        EXPECT_EQ(16, parsingResult.body.remotePortNumber);
        EXPECT_FALSE(parsingResult.body.closeSSHConnection);
    }

    { //All at the same time
        std::string input("reloadConfiguration,openSSHConnection:16,closeSSHConnection");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_TRUE(parsingResult.body.reloadConfiguration);
        EXPECT_TRUE(parsingResult.body.openSSHConnection);
        EXPECT_EQ(16, parsingResult.body.remotePortNumber);
        EXPECT_TRUE(parsingResult.body.closeSSHConnection);
        EXPECT_FALSE(parsingResult.body.getStatusReport);
        EXPECT_FALSE(parsingResult.body.reboot);
        EXPECT_FALSE(parsingResult.body.changeSeed);
    }

    { //All at the same time in reverse order
        std::string input("closeSSHConnection,openSSHConnection:1000,reloadConfiguration");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_TRUE(parsingResult.body.reloadConfiguration);
        EXPECT_TRUE(parsingResult.body.openSSHConnection);
        EXPECT_EQ(1000, parsingResult.body.remotePortNumber);
        EXPECT_TRUE(parsingResult.body.closeSSHConnection);
        EXPECT_FALSE(parsingResult.body.getStatusReport);
        EXPECT_FALSE(parsingResult.body.reboot);
        EXPECT_FALSE(parsingResult.body.changeSeed);
    }

    //ERRORS
    { //Invalid value
        std::string input("hello");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_FALSE(ok);
    }

    { //openSSHConnection with missing parameter
        std::string input("openSSHConnection");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_FALSE(ok);
    }

    { //Invalid value
        std::string input("reloadConfiguration,hello");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_FALSE(ok);
    }

    { //Repeated value
        std::string input("reloadConfiguration,closeSSHConnection&openSSHConnection:17,reloadConfiguration");
        TResponseBodyContext parsingResult;
        bool ok = ResponseBodyParser::parse(input, parsingResult);

        EXPECT_FALSE(ok);
    }
}
