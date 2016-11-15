#include "stdafx.h"
#include <gtest/gtest.h>

#include "configurationparser.h"


TEST(ConfigurationParser, parse)
{
    {
        std::string input("");
        TConfigurationParserContext parsingResult;
        bool ok = ConfigurationParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_EQ(0, parsingResult.configuration.items.size());
    }

    {
        std::string input(
"#This is a comment\n"
"#Parameter without value\n"
"parameterNameWithEmptyValue=\n"
"\n"
"parameterName=hello\n"
"OutStationMode=112\n"
        );
        TConfigurationParserContext parsingResult;
        bool ok = ConfigurationParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        EXPECT_EQ(3, parsingResult.configuration.items.size());

        {
            TConfiguration::TConfigurationCollection::const_iterator iter =
                parsingResult.configuration.items.find("parameterNameWithEmptyValue");
            EXPECT_TRUE(iter != parsingResult.configuration.items.end());
            if (iter != parsingResult.configuration.items.end())
                EXPECT_EQ(TConfigurationItem::eTYPE_STRING, iter->second.type);
            if (iter != parsingResult.configuration.items.end())
                EXPECT_STREQ("", iter->second.valueString.c_str());
        }

        {
            TConfiguration::TConfigurationCollection::const_iterator iter =
                parsingResult.configuration.items.find("parameterName");
            EXPECT_TRUE(iter != parsingResult.configuration.items.end());
            if (iter != parsingResult.configuration.items.end())
                EXPECT_EQ(TConfigurationItem::eTYPE_STRING, iter->second.type);
            if (iter != parsingResult.configuration.items.end())
                EXPECT_STREQ("hello", iter->second.valueString.c_str());
        }

        {
            TConfiguration::TConfigurationCollection::const_iterator iter =
                parsingResult.configuration.items.find("OutStationMode");
            EXPECT_TRUE(iter != parsingResult.configuration.items.end());
            if (iter != parsingResult.configuration.items.end())
                EXPECT_EQ(TConfigurationItem::eTYPE_INT, iter->second.type);
            if (iter != parsingResult.configuration.items.end())
                EXPECT_EQ(112, iter->second.valueInt);
        }
    }

}
