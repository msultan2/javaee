#include "stdafx.h"
#include <gtest/gtest.h>

#include "ssh/reversesshconnector.h"


using InStation::ReverseSSHConnector;


TEST(ReverseSSHConnector, parsePidFile)
{
    {
        const std::string content("7454 127.0.0.1 50000");
        ReverseSSHConnector::TConnectionParameters connectionParameters;
        const bool result = ReverseSSHConnector::parsePidFileContent(
            content, &connectionParameters);

        ASSERT_TRUE(result);
        EXPECT_STREQ("127.0.0.1", connectionParameters.address.c_str());
        EXPECT_EQ(50000, connectionParameters.remotePortNumber);
    }

    {
        const std::string content("7454 www.google.co.uk 9999");
        ReverseSSHConnector::TConnectionParameters connectionParameters;
        const bool result = ReverseSSHConnector::parsePidFileContent(
            content, &connectionParameters);

        ASSERT_TRUE(result);
        EXPECT_STREQ("www.google.co.uk", connectionParameters.address.c_str());
        EXPECT_EQ(9999, connectionParameters.remotePortNumber);
    }

    { //Wrong port number
        const std::string content("1234 www.google.co.uk 65536");
        ReverseSSHConnector::TConnectionParameters connectionParameters;
        const bool result = ReverseSSHConnector::parsePidFileContent(
            content, &connectionParameters);

        ASSERT_FALSE(result);
    }

    { //additional space after port number
        const std::string content("3333 www.google.co.uk 65535 ");
        ReverseSSHConnector::TConnectionParameters connectionParameters;
        const bool result = ReverseSSHConnector::parsePidFileContent(
            content, &connectionParameters);

        ASSERT_TRUE(result);
        EXPECT_STREQ("www.google.co.uk", connectionParameters.address.c_str());
        EXPECT_EQ(65535, connectionParameters.remotePortNumber);
    }

    { //missing port number
        const std::string content("3333 www.google.co.uk");
        ReverseSSHConnector::TConnectionParameters connectionParameters;
        const bool result = ReverseSSHConnector::parsePidFileContent(
            content, &connectionParameters);

        ASSERT_FALSE(result);
    }

    { //missing port number
        const std::string content("3333 www.google.co.uk ");
        ReverseSSHConnector::TConnectionParameters connectionParameters;
        const bool result = ReverseSSHConnector::parsePidFileContent(
            content, &connectionParameters);

        ASSERT_FALSE(result);
    }

    { //empty line
        const std::string content(" ");
        ReverseSSHConnector::TConnectionParameters connectionParameters;
        const bool result = ReverseSSHConnector::parsePidFileContent(
            content, &connectionParameters);

        ASSERT_FALSE(result);
    }
}
