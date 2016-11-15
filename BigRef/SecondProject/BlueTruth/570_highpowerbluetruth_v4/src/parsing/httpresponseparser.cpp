#include "stdafx.h"
#include "httpresponseparser.h"

#ifndef _WIN32
#include "config.h"
#endif

#include "http_response_parser.hh"
#include "http_response_scanner.h"

#include <cassert>
#include <iostream>
#include <iomanip>


THttpResponseContext::THttpResponseContext()
:
pScanner(0),
columnNo(1)
{
    //Prepare reentrant parser
    http_response_lex_init(&pScanner);
    http_response_set_extra(this, pScanner);
}

THttpResponseContext::~THttpResponseContext()
{
    //Destroy reentrant parser after use
    http_response_lex_destroy(pScanner);
}

void THttpResponseContext::reset()
{
    //Destructor commands
    http_response_lex_destroy(pScanner);

    //Constructor commands
    http_response_lex_init(&pScanner);
    http_response_set_extra(this, pScanner);

    httpResponse.reset();
}

HttpResponseParser::~HttpResponseParser()
{
    //do nothing
};

bool HttpResponseParser::parse(const std::string& input, THttpResponseContext& parsingResult)
{
    //Prepare parser to parse a string
    YY_BUFFER_STATE pBuffer = http_response__scan_bytes(&input[0], input.size(), parsingResult.pScanner);
    http_response__switch_to_buffer(pBuffer, parsingResult.pScanner);

    int result = http_response_parse(&parsingResult);
    bool ok = (result == 0);

    //Free resources used by the scanner
    http_response__delete_buffer(pBuffer, parsingResult.pScanner);

    return (ok);
}
