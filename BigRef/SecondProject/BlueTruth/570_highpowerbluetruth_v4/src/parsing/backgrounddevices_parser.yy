%require "3.0"
%define api.pure
%define api.prefix {backgrounddevices_}

%locations
%defines
%error-verbose

%parse-param { TBackgroundDevicesParserContext* pContext }
%lex-param { void* scannerPtr }

%code requires {
#include <stdio.h>
#include <string>
#include <algorithm>
#include "parser_common.h"
#include "backgrounddevicesparser.h"
#include "utils.h"

/*Basic set of lexer and parser functions*/
typedef struct BACKGROUND_DEVICE_LTYPE
{
    int first_line;
    int first_column;
    int last_line;
    int last_column;
} BACKGROUND_DEVICE_LTYPE;
#define BACKGROUND_DEVICE_LTYPE_IS_DECLARED 1
#define BACKGROUND_DEVICE_LTYPE_IS_TRIVIAL 1

#ifdef YYLTYPE
#undef YYLTYPE
#endif
#define YYLTYPE BACKGROUND_DEVICE_LTYPE

union BACKGROUND_DEVICE_STYPE
{
    int number;
    double double_number;
    char* str;
};
typedef union BACKGROUND_DEVICE_STYPE BACKGROUND_DEVICE_STYPE;
#ifdef YYSTYPE
#undef YYSTYPE
#endif
#define YYSTYPE BACKGROUND_DEVICE_STYPE

extern "C" int backgrounddevices_lex(BACKGROUND_DEVICE_STYPE* lvalp, BACKGROUND_DEVICE_LTYPE* yylloc_param, void* pScanner);
extern int (*printfIfDebugging) (const char* format, ...);

void backgrounddevices_error(BACKGROUND_DEVICE_LTYPE* yylloc_param, TBackgroundDevicesParserContext* pContext, const char* err);

#define scannerPtr (pContext->pScanner)

}


%token <str> TOKEN_ADDRESS
%token <str> TOKEN_FIRST_OBSERVED
%token <str> TOKEN_LAST_OBSERVED

%token TOKEN_NEW_LINE


%%

backgrounddevices:
    backgrounddevices_lines
    ;

backgrounddevices_lines:
      /*empty*/
    | backgrounddevices_lines backgrounddevices_line
    ;

backgrounddevices_line:
    address firstObserved lastObserved TOKEN_NEW_LINE
    {
        pContext->backgrounddevices.addItem(pContext->tmpBackgroundDevicesItem);
        printfIfDebugging(" [new background device added]\n");
    }

address:
    TOKEN_ADDRESS
    {
        bool ok = Utils::stringToUInt64(
            $1,
            pContext->tmpBackgroundDevicesItem.address, Utils::HEX);
        if (ok)
        {
            printfIfDebugging(" [address] ");
        }
        else
        {
            printfIfDebugging(" Error to convert address number to integer: ", $1);
            yyerrok;
        }
    }

firstObserved:
    TOKEN_FIRST_OBSERVED
    {
        bool ok = Utils::stringToUInt64(
            $1,
            pContext->tmpBackgroundDevicesItem.firstObservationTimeUTC, Utils::HEX);
        if (ok)
        {
            printfIfDebugging(" [firstObserved] ");
        }
        else
        {
            printfIfDebugging(" Error to convert firstObserved number to integer: ", $1);
            yyerrok;
        }
    }

lastObserved:
    TOKEN_LAST_OBSERVED
    {
        bool ok = Utils::stringToUInt64(
            $1,
            pContext->tmpBackgroundDevicesItem.lastObservationTimeUTC, Utils::HEX);
        if (ok)
        {
            printfIfDebugging(" [lastObserved] ");
        }
        else
        {
            printfIfDebugging(" Error to convert lastObserved number to integer: ", $1);
            yyerrok;
        }
    }


%%


void backgrounddevices_error(BACKGROUND_DEVICE_LTYPE* yylloc_param, TBackgroundDevicesParserContext* , const char* err)
{
    printf(" *** Lexical Error %s %d.%d-%d.%d\n", err,
         yylloc_param->first_line, yylloc_param->first_column,
         yylloc_param->last_line, yylloc_param->last_column);
}
