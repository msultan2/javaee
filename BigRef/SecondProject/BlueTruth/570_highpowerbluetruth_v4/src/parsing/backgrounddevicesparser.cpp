#include "stdafx.h"
#include "backgrounddevicesparser.h"

#ifndef _WIN32
#include "config.h"
#endif

#include "backgrounddevices_parser.hh"
#include "backgrounddevices_scanner.h"

#include <cassert>
#include <iostream>
#include <iomanip>


TBackgroundDevicesParserContext::TBackgroundDevicesParserContext()
:
pScanner(0),
columnNo(1)
{
    //Prepare reentrant parser
    backgrounddevices_lex_init(&pScanner);
    backgrounddevices_set_extra(this, pScanner);
}

TBackgroundDevicesParserContext::~TBackgroundDevicesParserContext()
{
    //Destroy reentrant parser after use
    backgrounddevices_lex_destroy(pScanner);
}

void TBackgroundDevices::addItem(const TBackgroundDevicesItem& item)
{
    items.push_back(item);
}

void TBackgroundDevices::print()
{
    std::cout << "\n";
    for (
        TBackgroundDevicesCollection::const_iterator iter(items.begin()), iterEnd(items.end());
        iter != iterEnd;
        ++iter)
    {
        std::cout << "\n"
            << iter->address << ","
            << iter->firstObservationTimeUTC << ","
            << iter->lastObservationTimeUTC;
    }
    std::cout << std::endl;
}


BackgroundDevicesParser::~BackgroundDevicesParser()
{
    //do nothing
};

bool BackgroundDevicesParser::parse(const std::string& input, TBackgroundDevicesParserContext& parsingResult)
{
    return parse(&input[0], input.size(), parsingResult);
}

bool BackgroundDevicesParser::parse(const char* input, const size_t size, TBackgroundDevicesParserContext& parsingResult)
{
    //Prepare parser to parse a string
    YY_BUFFER_STATE pBuffer = backgrounddevices__scan_bytes(input, size, parsingResult.pScanner);
    backgrounddevices__switch_to_buffer(pBuffer, parsingResult.pScanner);

    int result = backgrounddevices_parse(&parsingResult);
    bool ok = (result == 0);

    //Free resources used by the scanner
    backgrounddevices__delete_buffer(pBuffer, parsingResult.pScanner);

    return (ok);
}
