#include "stdafx.h"
#include <gtest/gtest.h>

#include "uri.h"


TEST(Uri, parse)
{
    { //http full address of a website
        Uri uri(Uri::parse("http://www.onet.pl/hello.html"));
        EXPECT_STREQ("", uri.QueryString.c_str());
        EXPECT_STREQ("/hello.html", uri.Path.c_str());
        EXPECT_STREQ("http", uri.Protocol.c_str());
        EXPECT_STREQ("www.onet.pl", uri.Host.c_str());
        EXPECT_STREQ("", uri.Port.c_str());
    }

    { //Plain numeric address
        Uri uri(Uri::parse("192.168.100.100"));
        EXPECT_STREQ("", uri.QueryString.c_str());
        EXPECT_STREQ("", uri.Path.c_str());
        EXPECT_STREQ("", uri.Protocol.c_str());
        EXPECT_STREQ("192.168.100.100", uri.Host.c_str());
        EXPECT_STREQ("", uri.Port.c_str());
    }

    { //Plain numeric address with specified port number
        Uri uri(Uri::parse("192.168.100.100:99"));
        EXPECT_STREQ("", uri.QueryString.c_str());
        EXPECT_STREQ("", uri.Path.c_str());
        EXPECT_STREQ("", uri.Protocol.c_str());
        EXPECT_STREQ("192.168.100.100", uri.Host.c_str());
        EXPECT_STREQ("99", uri.Port.c_str());
    }
}
