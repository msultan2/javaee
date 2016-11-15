/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
 */

#ifndef _CONFIGURATION_PARSER_H_
#define _CONFIGURATION_PARSER_H_


#include <string>
#include <map>

struct TConfigurationItem
{
    std::string name;
    enum EType
    {
        eTYPE_UNDEFINED = 0,
        eTYPE_INT,
        eTYPE_DOUBLE,
        eTYPE_STRING
    };
    EType type;
    int valueInt;
    double valueDouble;
    std::string valueString;

    TConfigurationItem() : name(), type(eTYPE_UNDEFINED), valueInt(0), valueDouble(0.0), valueString() {}
};

struct TConfiguration
{
    typedef std::map<std::string, TConfigurationItem> TConfigurationCollection;
    TConfigurationCollection items;

    TConfiguration() : items() {}

    void addItem(const TConfigurationItem& item);
    void print();
};

struct TConfigurationParserContext
{
    void* pScanner;
    int columnNo;
    TConfiguration configuration;
    TConfigurationItem tmpConfigurationItem;

    TConfigurationParserContext();
    ~TConfigurationParserContext();
};

class ConfigurationParser
{
public:

    virtual ~ConfigurationParser();


    static bool parse(const std::string& input, TConfigurationParserContext& parsingResult);
    static bool parse(const char* input, const size_t size, TConfigurationParserContext& parsingResult);

private:
    //! default constructor, not implemented
    ConfigurationParser();
    //! copy constructor, not implemented
    ConfigurationParser& operator=(const ConfigurationParser&);
    //! copy assignment operator, not implemented
    ConfigurationParser(const ConfigurationParser& rhs);
};

#endif // _CONFIGURATION_PARSER_H_
