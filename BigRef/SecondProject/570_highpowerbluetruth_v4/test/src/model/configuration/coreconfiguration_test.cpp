#include "stdafx.h"
#include <gtest/gtest.h>

#include "configuration/coreconfiguration.h"


using Model::CoreConfiguration;


TEST(CoreConfiguration, simple)
{
    {
        CoreConfiguration configuration;

        const char contents[] = "";
        EXPECT_FALSE(configuration.readAllParametersFromString(contents));
    }

    {
        CoreConfiguration configuration;

        const char contents[] =
            "<?xml version=\"1.0\" ?><Hello>World</Hello>";
        EXPECT_FALSE(configuration.readAllParametersFromString(contents));
    }

    {
        CoreConfiguration configuration;

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<coreConfiguration:CoreConfiguration xmlns:coreConfiguration=\"http://www.simulation-systems.co.uk/BlueTruth/coreConfigurationSchema\""
"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
"    xsi:schemaLocation=\"http://www.simulation-systems.co.uk/BlueTruth/coreConfigurationSchema misc/core_configurationSchema.xsd\">"
"	<Version>4.0.0</Version>"
"	<Identity>"
"		<SiteIdentifier>3412</SiteIdentifier>"
"		<SerialNumber>1234</SerialNumber>"
"	</Identity>"
"	<InStationSSHConnection>"
"		<Address>127.0.0.1</Address>"
"		<Login>special_login</Login>"
"		<Password>secret_password</Password>"
"	</InStationSSHConnection>"
"	<IniConfigurationURL>"
"		<Path>my_path</Path>"
"		<File_Prefix>your_prefix</File_Prefix>"
"		<File_Suffix>his_suffix</File_Suffix>"
"	</IniConfigurationURL>"
"	<BlueToothDevice>"
"       <MAC_AddressOfDeviceToBeUsed>AB:CD:EF:01:23:45</MAC_AddressOfDeviceToBeUsed>"
"		<Driver><RawHCI/></Driver>"
"	</BlueToothDevice>"
"	<GSMModemConnection>"
"		<Type>0</Type>"
"		<Address></Address>"
"		<Login></Login>"
"		<Password></Password>"
"	</GSMModemConnection>"
"	<Logging>"
"		<FileLogLevel>5</FileLogLevel>"
"		<ConsoleLogLevel>6</ConsoleLogLevel>"
"		<MaxNumberOfEntriesPerFile>5000</MaxNumberOfEntriesPerFile>"
"		<MaxNumberOfCharactersPerFile>2000000</MaxNumberOfCharactersPerFile>"
"		<MaxLogFileAgeInSeconds>345600</MaxLogFileAgeInSeconds>"
"	</Logging>"
"</coreConfiguration:CoreConfiguration>";
        EXPECT_TRUE(configuration.readAllParametersFromString(contents));

        EXPECT_STREQ("4.0.0", configuration.getCoreConfigurationVersion().c_str());
        EXPECT_EQ(4, configuration.getMajorCoreConfigurationVersion());
        EXPECT_STREQ("1234", configuration.getSSLSerialNumber().c_str());

        EXPECT_STREQ("127.0.0.1", configuration.getInstationSSHConnectionAddress().c_str());
        EXPECT_EQ(22, configuration.getInstationSSHConnectionPort());
        EXPECT_STREQ("special_login", configuration.getInstationSSHConnectionLogin().c_str());
        EXPECT_STREQ("secret_password", configuration.getInstationSSHConnectionPassword().c_str());

        EXPECT_STREQ("my_path", configuration.getConfigurationURL().c_str());
        EXPECT_STREQ("your_prefix", configuration.getConfigurationURL_filePrefix().c_str());
        EXPECT_STREQ("his_suffix", configuration.getConfigurationURL_fileSuffix().c_str());

        EXPECT_EQ(0x452301EFCDAB, configuration.getLastUsedBlueToothDevice());
        EXPECT_STREQ("RawHCI", configuration.getDeviceDriver().c_str());

        EXPECT_EQ(0, configuration.getGSMModemType());
        EXPECT_STREQ("", configuration.getGSMModemConnectionAddress().c_str());
        EXPECT_EQ(22, configuration.getGSMModemConnectionPort());
        EXPECT_STREQ("", configuration.getGSMModemConnectionLogin().c_str());
        EXPECT_STREQ("", configuration.getGSMModemConnectionPassword().c_str());

        EXPECT_EQ(5, configuration.getFileLogLevel());
        EXPECT_EQ(6, configuration.getConsoleLogLevel());
        EXPECT_EQ(5000, configuration.getLogMaxNumberOfEntriesPerFile());
        EXPECT_EQ(2000000, configuration.getLogMaxNumberOfCharactersPerFile());
        EXPECT_EQ(345600, configuration.getMaximumLogFileAgeInSeconds());
    }

    {
        CoreConfiguration configuration;

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<coreConfiguration:CoreConfiguration xmlns:coreConfiguration=\"http://www.simulation-systems.co.uk/BlueTruth/coreConfigurationSchema\""
"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
"    xsi:schemaLocation=\"http://www.simulation-systems.co.uk/BlueTruth/coreConfigurationSchema misc/core_configurationSchema.xsd\">"
"	<Version>4.0.0</Version>"
"	<Identity>"
"		<SiteIdentifier>3412</SiteIdentifier>"
"		<SerialNumber>1234</SerialNumber>"
"	</Identity>"
"	<InStationSSHConnection>"
"		<Address>127.0.0.1</Address>"
"		<Port>1034</Port>"
"		<Login>special_login</Login>"
"		<Password>secret_password</Password>"
"	</InStationSSHConnection>"
"	<IniConfigurationURL>"
"		<Path>my_path</Path>"
"		<File_Prefix>your_prefix</File_Prefix>"
"		<File_Suffix>his_suffix</File_Suffix>"
"	</IniConfigurationURL>"
"	<BlueToothDevice>"
"       <MAC_AddressOfDeviceToBeUsed>AB:CD:EF:01:23:45</MAC_AddressOfDeviceToBeUsed>"
"		<Driver><Parani/></Driver>"
"	</BlueToothDevice>"
"	<GSMModemConnection>"
"		<Type>1</Type>"
"		<Address>192.168.2.1</Address>"
"		<Login>adm</Login>"
"		<Password>123456</Password>"
"	</GSMModemConnection>"
"	<Logging>"
"		<FileLogLevel>5</FileLogLevel>"
"		<ConsoleLogLevel>6</ConsoleLogLevel>"
"		<MaxNumberOfEntriesPerFile>5000</MaxNumberOfEntriesPerFile>"
"		<MaxNumberOfCharactersPerFile>2000000</MaxNumberOfCharactersPerFile>"
"		<MaxLogFileAgeInSeconds>345600</MaxLogFileAgeInSeconds>"
"	</Logging>"
"</coreConfiguration:CoreConfiguration>";
        EXPECT_TRUE(configuration.readAllParametersFromString(contents));

        EXPECT_STREQ("4.0.0", configuration.getCoreConfigurationVersion().c_str());
        EXPECT_EQ(4, configuration.getMajorCoreConfigurationVersion());
        EXPECT_STREQ("1234", configuration.getSSLSerialNumber().c_str());
        EXPECT_STREQ("127.0.0.1", configuration.getInstationSSHConnectionAddress().c_str());
        EXPECT_EQ(1034, configuration.getInstationSSHConnectionPort());

        EXPECT_STREQ("special_login", configuration.getInstationSSHConnectionLogin().c_str());
        EXPECT_STREQ("secret_password", configuration.getInstationSSHConnectionPassword().c_str());
        EXPECT_STREQ("my_path", configuration.getConfigurationURL().c_str());
        EXPECT_STREQ("your_prefix", configuration.getConfigurationURL_filePrefix().c_str());
        EXPECT_STREQ("his_suffix", configuration.getConfigurationURL_fileSuffix().c_str());

        EXPECT_EQ(0x452301EFCDAB, configuration.getLastUsedBlueToothDevice());
        EXPECT_STREQ("Parani", configuration.getDeviceDriver().c_str());
        EXPECT_STREQ("/dev/ttyUSB0", configuration.getParaniPortName().c_str());
        EXPECT_EQ(115200, configuration.getParaniBitRate());

        EXPECT_EQ(1, configuration.getGSMModemType());
        EXPECT_STREQ("192.168.2.1", configuration.getGSMModemConnectionAddress().c_str());
        EXPECT_EQ(22, configuration.getGSMModemConnectionPort());
        EXPECT_STREQ("adm", configuration.getGSMModemConnectionLogin().c_str());
        EXPECT_STREQ("123456", configuration.getGSMModemConnectionPassword().c_str());

        EXPECT_EQ(5, configuration.getFileLogLevel());
        EXPECT_EQ(6, configuration.getConsoleLogLevel());
        EXPECT_EQ(5000, configuration.getLogMaxNumberOfEntriesPerFile());
        EXPECT_EQ(2000000, configuration.getLogMaxNumberOfCharactersPerFile());
        EXPECT_EQ(345600, configuration.getMaximumLogFileAgeInSeconds());
    }

    {
        CoreConfiguration configuration;

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<coreConfiguration:CoreConfiguration xmlns:coreConfiguration=\"http://www.simulation-systems.co.uk/BlueTruth/coreConfigurationSchema\""
"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
"    xsi:schemaLocation=\"http://www.simulation-systems.co.uk/BlueTruth/coreConfigurationSchema misc/core_configurationSchema.xsd\">"
"	<Version>4.0.0</Version>"
"	<Identity>"
"		<SiteIdentifier>3412</SiteIdentifier>"
"		<SerialNumber>1234</SerialNumber>"
"	</Identity>"
"	<InStationSSHConnection>"
"		<Address>127.0.0.1</Address>"
"		<Port>1034</Port>"
"		<Login>special_login</Login>"
"		<Password>secret_password</Password>"
"	</InStationSSHConnection>"
"	<IniConfigurationURL>"
"		<Path>my_path</Path>"
"		<File_Prefix>your_prefix</File_Prefix>"
"		<File_Suffix>his_suffix</File_Suffix>"
"	</IniConfigurationURL>"
"	<BlueToothDevice>"
"       <MAC_AddressOfDeviceToBeUsed>AB:CD:EF:01:23:45</MAC_AddressOfDeviceToBeUsed>"
"		<Driver><Parani PortName=\"/dev/hello\" BitRate=\"9600\"/></Driver>"
"	</BlueToothDevice>"
"	<GSMModemConnection>"
"		<Type>1</Type>"
"		<Address>192.168.2.1</Address>"
"		<Port>8080</Port>"
"		<Login>adm</Login>"
"		<Password>123456</Password>"
"	</GSMModemConnection>"
"	<Logging>"
"		<FileLogLevel>5</FileLogLevel>"
"		<ConsoleLogLevel>6</ConsoleLogLevel>"
"		<MaxNumberOfEntriesPerFile>5000</MaxNumberOfEntriesPerFile>"
"		<MaxNumberOfCharactersPerFile>2000000</MaxNumberOfCharactersPerFile>"
"		<MaxLogFileAgeInSeconds>345600</MaxLogFileAgeInSeconds>"
"	</Logging>"
"</coreConfiguration:CoreConfiguration>";
        EXPECT_TRUE(configuration.readAllParametersFromString(contents));

        EXPECT_STREQ("4.0.0", configuration.getCoreConfigurationVersion().c_str());
        EXPECT_EQ(4, configuration.getMajorCoreConfigurationVersion());
        EXPECT_STREQ("1234", configuration.getSSLSerialNumber().c_str());
        EXPECT_STREQ("127.0.0.1", configuration.getInstationSSHConnectionAddress().c_str());
        EXPECT_EQ(1034, configuration.getInstationSSHConnectionPort());

        EXPECT_STREQ("special_login", configuration.getInstationSSHConnectionLogin().c_str());
        EXPECT_STREQ("secret_password", configuration.getInstationSSHConnectionPassword().c_str());
        EXPECT_STREQ("my_path", configuration.getConfigurationURL().c_str());
        EXPECT_STREQ("your_prefix", configuration.getConfigurationURL_filePrefix().c_str());
        EXPECT_STREQ("his_suffix", configuration.getConfigurationURL_fileSuffix().c_str());

        EXPECT_EQ(0x452301EFCDAB, configuration.getLastUsedBlueToothDevice());
        EXPECT_STREQ("Parani", configuration.getDeviceDriver().c_str());
        EXPECT_STREQ("/dev/hello", configuration.getParaniPortName().c_str());
        EXPECT_EQ(9600, configuration.getParaniBitRate());

        EXPECT_STREQ("192.168.2.1", configuration.getGSMModemConnectionAddress().c_str());
        EXPECT_EQ(8080, configuration.getGSMModemConnectionPort());
        EXPECT_STREQ("adm", configuration.getGSMModemConnectionLogin().c_str());
        EXPECT_STREQ("123456", configuration.getGSMModemConnectionPassword().c_str());

        EXPECT_EQ(5, configuration.getFileLogLevel());
        EXPECT_EQ(6, configuration.getConsoleLogLevel());
        EXPECT_EQ(5000, configuration.getLogMaxNumberOfEntriesPerFile());
        EXPECT_EQ(2000000, configuration.getLogMaxNumberOfCharactersPerFile());
        EXPECT_EQ(345600, configuration.getMaximumLogFileAgeInSeconds());
    }
}

TEST(CoreConfiguration, readAllParametersFromFile)
{
    { //Try to read the file that does not exist
        CoreConfiguration configuration;
        const std::string FILENAME("/tmp/CoreConfiguration.readAllParametersFromFile.tmpx");

        EXPECT_FALSE(configuration.readAllParametersFromFile(FILENAME.c_str()));
    }

    { //Read from file (contents as in one of the tests above)
        CoreConfiguration configuration;
        const std::string FILENAME("/tmp/CoreConfiguration.readAllParametersFromFile.tmp");
        std::ofstream tmpFile;
        tmpFile.open(FILENAME, std::ofstream::out);
        ASSERT_TRUE(tmpFile.is_open());

        const char contents[] =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
"<coreConfiguration:CoreConfiguration xmlns:coreConfiguration=\"http://www.simulation-systems.co.uk/BlueTruth/coreConfigurationSchema\""
"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
"    xsi:schemaLocation=\"http://www.simulation-systems.co.uk/BlueTruth/coreConfigurationSchema misc/core_configurationSchema.xsd\">"
"	<Version>4.0.0</Version>"
"	<Identity>"
"		<SiteIdentifier>3412</SiteIdentifier>"
"		<SerialNumber>1234</SerialNumber>"
"	</Identity>"
"	<InStationSSHConnection>"
"		<Address>127.0.0.1</Address>"
"		<Port>1034</Port>"
"		<Login>special_login</Login>"
"		<Password>secret_password</Password>"
"	</InStationSSHConnection>"
"	<IniConfigurationURL>"
"		<Path>my_path</Path>"
"		<File_Prefix>your_prefix</File_Prefix>"
"		<File_Suffix>his_suffix</File_Suffix>"
"	</IniConfigurationURL>"
"	<BlueToothDevice>"
"       <MAC_AddressOfDeviceToBeUsed>AB:CD:EF:01:23:45</MAC_AddressOfDeviceToBeUsed>"
"		<Driver><Parani PortName=\"/dev/hello\" BitRate=\"9600\"/></Driver>"
"	</BlueToothDevice>"
"	<GSMModemConnection>"
"		<Type>1</Type>"
"		<Address>192.168.2.1</Address>"
"		<Port>8080</Port>"
"		<Login>adm</Login>"
"		<Password>123456</Password>"
"	</GSMModemConnection>"
"	<Logging>"
"		<FileLogLevel>5</FileLogLevel>"
"		<ConsoleLogLevel>6</ConsoleLogLevel>"
"		<MaxNumberOfEntriesPerFile>5000</MaxNumberOfEntriesPerFile>"
"		<MaxNumberOfCharactersPerFile>2000000</MaxNumberOfCharactersPerFile>"
"		<MaxLogFileAgeInSeconds>345600</MaxLogFileAgeInSeconds>"
"	</Logging>"
"</coreConfiguration:CoreConfiguration>";
        tmpFile << contents;
        tmpFile.close();

        EXPECT_TRUE(configuration.readAllParametersFromFile(FILENAME.c_str()));

        EXPECT_STREQ("4.0.0", configuration.getCoreConfigurationVersion().c_str());
        EXPECT_EQ(4, configuration.getMajorCoreConfigurationVersion());
        EXPECT_STREQ("1234", configuration.getSSLSerialNumber().c_str());
        EXPECT_STREQ("127.0.0.1", configuration.getInstationSSHConnectionAddress().c_str());
        EXPECT_EQ(1034, configuration.getInstationSSHConnectionPort());

        EXPECT_STREQ("special_login", configuration.getInstationSSHConnectionLogin().c_str());
        EXPECT_STREQ("secret_password", configuration.getInstationSSHConnectionPassword().c_str());
        EXPECT_STREQ("my_path", configuration.getConfigurationURL().c_str());
        EXPECT_STREQ("your_prefix", configuration.getConfigurationURL_filePrefix().c_str());
        EXPECT_STREQ("his_suffix", configuration.getConfigurationURL_fileSuffix().c_str());

        EXPECT_EQ(0x452301EFCDAB, configuration.getLastUsedBlueToothDevice());
        EXPECT_STREQ("Parani", configuration.getDeviceDriver().c_str());
        EXPECT_STREQ("/dev/hello", configuration.getParaniPortName().c_str());
        EXPECT_EQ(9600, configuration.getParaniBitRate());

        EXPECT_EQ(1, configuration.getGSMModemType());
        EXPECT_STREQ("192.168.2.1", configuration.getGSMModemConnectionAddress().c_str());
        EXPECT_EQ(8080, configuration.getGSMModemConnectionPort());
        EXPECT_STREQ("adm", configuration.getGSMModemConnectionLogin().c_str());
        EXPECT_STREQ("123456", configuration.getGSMModemConnectionPassword().c_str());

        EXPECT_EQ(5, configuration.getFileLogLevel());
        EXPECT_EQ(6, configuration.getConsoleLogLevel());
        EXPECT_EQ(5000, configuration.getLogMaxNumberOfEntriesPerFile());
        EXPECT_EQ(2000000, configuration.getLogMaxNumberOfCharactersPerFile());
        EXPECT_EQ(345600, configuration.getMaximumLogFileAgeInSeconds());

        remove(FILENAME.c_str());
    }
}
