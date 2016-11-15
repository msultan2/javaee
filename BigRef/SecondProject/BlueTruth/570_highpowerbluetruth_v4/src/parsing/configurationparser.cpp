#include "stdafx.h"
#include "configurationparser.h"

#ifndef _WIN32
#include "config.h"
#endif

#include "configuration_parser.hh"
#include "configuration_scanner.h"

#include <cassert>
#include <iostream>
#include <iomanip>


TConfigurationParserContext::TConfigurationParserContext()
:
pScanner(0),
columnNo(1)
{
    //Prepare reentrant parser
    configuration_lex_init(&pScanner);
    configuration_set_extra(this, pScanner);
}

TConfigurationParserContext::~TConfigurationParserContext()
{
    //Destroy reentrant parser after use
    configuration_lex_destroy(pScanner);
}

void TConfiguration::addItem(const TConfigurationItem& item)
{
    items[item.name] = item;
}

void TConfiguration::print()
{
    std::cout << "\n";
    for (
        TConfigurationCollection::const_iterator iter(items.begin()), iterEnd(items.end());
        iter != iterEnd;
        ++iter)
    {
        std::cout << "\n" << iter->first << "=";
        switch (iter->second.type)
        {
            case TConfigurationItem::eTYPE_INT:
            {
                std::cout << iter->second.valueInt;
                break;
            }
            case TConfigurationItem::eTYPE_DOUBLE:
            {
                std::cout << iter->second.valueDouble;
                break;
            }
            case TConfigurationItem::eTYPE_STRING:
            {
                std::cout << iter->second.valueString;
                break;
            }
            default:
            {
                break;
            }
        }
    }
    std::cout << std::endl;
}


ConfigurationParser::~ConfigurationParser()
{
    //do nothing
};

bool ConfigurationParser::parse(const std::string& input, TConfigurationParserContext& parsingResult)
{
    return parse(&input[0], input.size(), parsingResult);
}

bool ConfigurationParser::parse(const char* input, const size_t size, TConfigurationParserContext& parsingResult)
{
    //Prepare parser to parse a string
    YY_BUFFER_STATE pBuffer = configuration__scan_bytes(input, size, parsingResult.pScanner);
    configuration__switch_to_buffer(pBuffer, parsingResult.pScanner);

    int result = configuration_parse(&parsingResult);
    bool ok = (result == 0);

    //Free resources used by the scanner
    configuration__delete_buffer(pBuffer, parsingResult.pScanner);

    return (ok);
}
