#include "stdafx.h"
#include "paranioutputparser.h"

#ifdef __linux__
#   include "config.h"
#   include "parani_output_parser.hh"
#else
#   include "parani_output_parser.h"
#endif
#include "parani_output_scanner.h"
//#include "paranioutputparser.h"
//#include "paranioutputscanner.h"

#include <cassert>
#include <iostream>
#include <iomanip>


TParaniOutputContext::TParaniOutputContext()
:
pScanner(0),
columnNo(1)
{
    //Prepare reentrant parser
    parani_output_lex_init(&pScanner);
    parani_output_set_extra(this, pScanner);
}

TParaniOutputContext::~TParaniOutputContext()
{
    //Destroy reentrant parser after use
    parani_output_lex_destroy(pScanner);
}

void TParaniOutputContext::reset()
{
    //Destructor commands
    parani_output_lex_destroy(pScanner);

    //Constructor commands
    parani_output_lex_init(&pScanner);
    parani_output_set_extra(this, pScanner);

    //paraniOutput.reset();
}

void TParaniOutput::addRegisterEntry(const TParaniOutputRegisterEntry& item)
{
    registerEntries[item.name] = item;
}

void TParaniOutput::addInquiryResultEntry(const TParaniOutputInquiryResultEntry& item)
{
    inquiryResult[item.address] = item;
}

void TParaniOutput::print()
{
    std::cout << "\n";

    for (
        TRegisterEntryCollection::const_iterator iter(registerEntries.begin()), iterEnd(registerEntries.end());
        iter != iterEnd;
        ++iter)
    {
        std::cout << "\n" << iter->first << "=";
        std::cout << iter->second.valueString;
    }

    if (!bluetoothSettings.bdaddress.empty())
    {
        std::cout << "bdaddress: " << bluetoothSettings.bdaddress << "\n";
        std::cout << "deviceName: " << bluetoothSettings.deviceName << "\n";
        std::cout << "operationMode: " << bluetoothSettings.operationMode << "\n";
        std::cout << "operationStatus: " << bluetoothSettings.operationStatus << "\n";
        std::cout << "authentication: " << bluetoothSettings.authentication << "\n";
        std::cout << "dataEncryption: " << bluetoothSettings.dataEncryption << "\n";
        std::cout << "hardwareFlowControl: " << bluetoothSettings.hardwareFlowControl << "\n";
    }
    //else do nothing

    for (
        TParaniOutputInquiryResultEntryCollection::const_iterator iter(inquiryResult.begin()), iterEnd(inquiryResult.end());
        iter != iterEnd;
        ++iter)
    {
        std::cout
            << "\n" << iter->second.address << "," << iter->second.deviceClass;
    }

    std::cout << std::endl;
}

ParaniOutputParser::~ParaniOutputParser()
{
    //do nothing
};

bool ParaniOutputParser::parse(const std::string& input, TParaniOutputContext& parsingResult)
{
    //Prepare parser to parse a string
    YY_BUFFER_STATE pBuffer = parani_output__scan_bytes(&input[0], input.size(), parsingResult.pScanner);
    parani_output__switch_to_buffer(pBuffer, parsingResult.pScanner);

    int result = parani_output_parse(&parsingResult);
    bool ok = (result == 0);

    //Free resources used by the scanner
    parani_output__delete_buffer(pBuffer, parsingResult.pScanner);

    return (ok);
}

#ifdef TESTING

void ParaniOutputParser::test()
{
    {
        std::string input("S0:0; S1: 1; S55: 000000000\n\nOK\n");
        TParaniOutputContext parsingResult;
        bool ok = parse(input, parsingResult);
        if (ok)
        {
            parsingResult.paraniOutput.print();
        }
        else
        {
            std::cerr << "ParaniOutputParser::test() Error" << std::endl;

            parsingResult.paraniOutput.print();
        }
    }

    {
        std::string input("\n\nERROR\n");
        TParaniOutputContext parsingResult;
        bool ok = parse(input, parsingResult);
        if (ok)
        {
            parsingResult.paraniOutput.print();
        }
        else
        {
            std::cerr << "ParaniOutputParser::test() Error" << std::endl;

            parsingResult.paraniOutput.print();
        }
    }

    {
        std::string input("\n\n00019518496B,SD1000Uv2.0.3-18496B,MODE0,STANDBY,0,0,NoFC\n\n\n\nOK\n\n");
        TParaniOutputContext parsingResult;
        bool ok = parse(input, parsingResult);
        if (ok)
        {
            parsingResult.paraniOutput.print();
        }
        else
        {
            std::cerr << "ParaniOutputParser::test() Error" << std::endl;

            parsingResult.paraniOutput.print();
        }
    }

    {
        std::string input("\n\nC8DF7CC81BE7,**UNKNOWN**,5A020C\n\n\n\nOK\n\n");
        TParaniOutputContext parsingResult;
        bool ok = parse(input, parsingResult);
        if (ok)
        {
            parsingResult.paraniOutput.print();
        }
        else
        {
            std::cerr << "ParaniOutputParser::test() Error" << std::endl;

            parsingResult.paraniOutput.print();
        }
    }

    {
        std::string input("\n\nCC8CE371B55C,**UNKNOWN**,5A020C\n\n\n\nC8DF7CC81BE7,**UNKNOWN**,5A020C\n\n\n\nOK\n\n");
        TParaniOutputContext parsingResult;
        bool ok = parse(input, parsingResult);
        if (ok)
        {
            parsingResult.paraniOutput.print();
        }
        else
        {
            std::cerr << "ParaniOutputParser::test() Error" << std::endl;

            parsingResult.paraniOutput.print();
        }
    }
}

#endif //TESTING
