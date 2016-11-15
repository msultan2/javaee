#include "stdafx.h"
#include "responsebodyparser.h"

#ifndef _WIN32
#include "config.h"
#endif

#include "response_body_parser.hh"
#include "response_body_scanner.h"

#include <cassert>
#include <iostream>
#include <iomanip>


TResponseBody::TResponseBody()
:
reloadConfiguration(false),
openSSHConnection(false),
remotePortNumber(0),
closeSSHConnection(false),
getStatusReport(false),
reboot(false),
changeSeed(false),
latchBackground(false),
latchBackgroundTimeInSeconds(0),
flushBackground(false),
ok(true)
{
    //do nothing
}

void TResponseBody::print()
{
    std::cout << "reloadConfiguration=" << (reloadConfiguration?"true\n":"false\n");
    std::cout << "openSSHConnection=" << (openSSHConnection?"true\n":"false\n");
    std::cout << "closeSSHConnection=" << (closeSSHConnection?"true\n":"false\n");
    std::cout << "getStatusReport=" << (getStatusReport?"true\n":"false\n");
    std::cout << "reboot=" << (reboot?"true\n":"false\n");
    std::cout << "changeSeed=" << (changeSeed?"true\n":"false\n");
    std::cout << "latchBackground=" << (latchBackground?"true\n":"false\n");
    std::cout << "flushBackground=" << (flushBackground?"true\n":"false\n");
    std::cout << std::endl;
}


TResponseBodyContext::TResponseBodyContext()
:
pScanner(0),
columnNo(1),
body()
{
    //Prepare reentrant parser
    response_body_lex_init(&pScanner);
    response_body_set_extra(this, pScanner);
}

TResponseBodyContext::~TResponseBodyContext()
{
    //Destroy reentrant parser after use
    response_body_lex_destroy(pScanner);
}


ResponseBodyParser::~ResponseBodyParser()
{
    //do nothing
};

bool ResponseBodyParser::parse(const std::string& input, TResponseBodyContext& parsingResult)
{
    //Prepare parser to parse a string
    YY_BUFFER_STATE pBuffer = response_body__scan_bytes(&input[0], input.size(), parsingResult.pScanner);
    response_body__switch_to_buffer(pBuffer, parsingResult.pScanner);

	int result = response_body_parse(&parsingResult);
    bool ok = ((result == 0) && parsingResult.body.ok);

    //Free resources used by the scanner
    response_body__delete_buffer(pBuffer, parsingResult.pScanner);

    return (ok);
}
