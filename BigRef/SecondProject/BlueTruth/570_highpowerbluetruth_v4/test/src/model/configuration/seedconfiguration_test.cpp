#include "stdafx.h"
#include <gtest/gtest.h>

#include "configuration/seedconfiguration.h"


using Model::SeedConfiguration;


TEST(SeedConfiguration, simple)
{
    { //Empty file
        SeedConfiguration configuration;

        const char contents[] = "";
        EXPECT_FALSE(configuration.readAllParametersFromString(contents));
    }

    {
        SeedConfiguration configuration;

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<Seed>"
"	<ID>9876</ID>"
"	<Value>1234</Value>"
"</Seed>";
        EXPECT_TRUE(configuration.readAllParametersFromString(contents));

        EXPECT_EQ(9876, configuration.getId());
        EXPECT_EQ(1234, configuration.getValue());
    }
}

TEST(SeedConfiguration, readAllParametersFromFile)
{
    { //Try to read the file that does not exist
        SeedConfiguration configuration;
        const std::string FILENAME("/tmp/SeedConfiguration.readAllParametersFromFile.tmpx");

        EXPECT_FALSE(configuration.readAllParametersFromFile(FILENAME.c_str()));
    }

    { //Invalid contents
        SeedConfiguration configuration;
        const std::string FILENAME("/tmp/SeedConfiguration.readAllParametersFromFile.tmp");
        std::ofstream tmpFile;
        tmpFile.open(FILENAME, std::ofstream::out);
        ASSERT_TRUE(tmpFile.is_open());

        const char contents[] =
"<?xml version=\"1.0\" ?>"
"<Hello>World</Hello>";
        tmpFile << contents;
        tmpFile.close();

        EXPECT_FALSE(configuration.readAllParametersFromFile(FILENAME.c_str()));

        EXPECT_FALSE(configuration.readAllParametersFromString(contents));

        remove(FILENAME.c_str());
    }

    { //Read from file (contents invalid)
        SeedConfiguration configuration;
        const std::string FILENAME("/tmp/SeedConfiguration.readAllParametersFromFile.tmp");
        std::ofstream tmpFile;
        tmpFile.open(FILENAME, std::ofstream::out);
        ASSERT_TRUE(tmpFile.is_open());

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<Seed>"
"	<IDx>9876</IDx>"
"	<Value>1234</Value>"
"</Seed>";
        tmpFile << contents;
        tmpFile.close();

        EXPECT_FALSE(configuration.readAllParametersFromFile(FILENAME.c_str()));

        EXPECT_FALSE(configuration.readAllParametersFromString(contents));

        remove(FILENAME.c_str());
    }

    { //Invalid XML (see first element mismatch)
        SeedConfiguration configuration;
        const std::string FILENAME("/tmp/SeedConfiguration.readAllParametersFromFile.tmp");
        std::ofstream tmpFile;
        tmpFile.open(FILENAME, std::ofstream::out);
        ASSERT_TRUE(tmpFile.is_open());

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<Seed>"
"	<IDx>9876</ID>"
"	<Value>1234</Value>"
"</Seed>";
        tmpFile << contents;
        tmpFile.close();

        EXPECT_FALSE(configuration.readAllParametersFromFile(FILENAME.c_str()));

        EXPECT_FALSE(configuration.readAllParametersFromString(contents));

        remove(FILENAME.c_str());
    }

    { //Invalid XML (missing ID)
        SeedConfiguration configuration;
        const std::string FILENAME("/tmp/SeedConfiguration.readAllParametersFromFile.tmp");
        std::ofstream tmpFile;
        tmpFile.open(FILENAME, std::ofstream::out);
        ASSERT_TRUE(tmpFile.is_open());

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<Seed>"
"	<Value>1234</Value>"
"</Seed>";
        tmpFile << contents;
        tmpFile.close();

        EXPECT_FALSE(configuration.readAllParametersFromFile(FILENAME.c_str()));

        EXPECT_FALSE(configuration.readAllParametersFromString(contents));

        remove(FILENAME.c_str());
    }

    { //Invalid XML (duplicate ID)
        SeedConfiguration configuration;
        const std::string FILENAME("/tmp/SeedConfiguration.readAllParametersFromFile.tmp");
        std::ofstream tmpFile;
        tmpFile.open(FILENAME, std::ofstream::out);
        ASSERT_TRUE(tmpFile.is_open());

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<Seed>"
"	<ID>9876</ID>"
"	<Value>1234</Value>"
"	<ID>9876</ID>"
"</Seed>";
        tmpFile << contents;
        tmpFile.close();

        EXPECT_FALSE(configuration.readAllParametersFromFile(FILENAME.c_str()));

        EXPECT_FALSE(configuration.readAllParametersFromString(contents));

        remove(FILENAME.c_str());
    }

    { //Read from file (contents as in one of the tests above)
        SeedConfiguration configuration;
        const std::string FILENAME("/tmp/SeedConfiguration.readAllParametersFromFile.tmp");
        std::ofstream tmpFile;
        tmpFile.open(FILENAME, std::ofstream::out);
        ASSERT_TRUE(tmpFile.is_open());

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<Seed>"
"	<ID>9876</ID>"
"	<Value>1234</Value>"
"</Seed>";
        tmpFile << contents;
        tmpFile.close();

        EXPECT_TRUE(configuration.readAllParametersFromFile(FILENAME.c_str()));

        EXPECT_TRUE(configuration.readAllParametersFromString(contents));

        EXPECT_EQ(9876, configuration.getId());
        EXPECT_EQ(1234, configuration.getValue());

        remove(FILENAME.c_str());
    }
}
