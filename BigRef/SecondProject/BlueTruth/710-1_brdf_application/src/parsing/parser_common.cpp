#include "stdafx.h"
#include "parser_common.h"

#include <stdio.h>


//#define DEBUG_PARSER

#ifdef DEBUG_PARSER
int (*printfIfDebugging) (const char* format, ...) = printf;
#else
int doNotPrint(const char* , ...) { return 0; }
int (*printfIfDebugging) (const char* format, ...) = doNotPrint;
#endif

void copyVectorOfCharsToString(const std::vector<char>& input, std::string* result)
{
    if (result == 0)
    {
        return;
    }
    //else do nothing

    for(size_t i=0; i<input.size(); ++i)
    {
        *result += input[i];
    }
}

int yywrap()
{
    return 1;
}
