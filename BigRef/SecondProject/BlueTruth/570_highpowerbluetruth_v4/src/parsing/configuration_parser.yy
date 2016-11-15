%require "3.0"
%define api.pure
%define api.prefix {configuration_}

%locations
%defines
%error-verbose

%parse-param { TConfigurationParserContext* pContext }
%lex-param { void* scannerPtr }

%code requires {
#include <stdio.h>
#include <string>
#include <algorithm>
#include "parser_common.h"
#include "configurationparser.h"

/*Basic set of lexer and parser functions*/
typedef struct CONFIGURATION_LTYPE
{
    int first_line;
    int first_column;
    int last_line;
    int last_column;
} CONFIGURATION_LTYPE;
#define CONFIGURATION_LTYPE_IS_DECLARED 1
#define CONFIGURATION_LTYPE_IS_TRIVIAL 1
#define YYLTYPE CONFIGURATION_LTYPE

union CONFIGURATION_STYPE
{
    int number;
    double double_number;
    char* str;
};
typedef union CONFIGURATION_STYPE CONFIGURATION_STYPE;
#define YYSTYPE CONFIGURATION_STYPE

extern "C" int configuration_lex(CONFIGURATION_STYPE* lvalp, CONFIGURATION_LTYPE* yylloc_param, void* pScanner);
extern int (*printfIfDebugging) (const char* format, ...);

void configuration_error(CONFIGURATION_LTYPE* yylloc_param, TConfigurationParserContext* pContext, const char* err);

#define scannerPtr (pContext->pScanner)

}


%token <str> TOKEN_PARAMETER_NAME
%token <number> INT_NUMBER
%token <double_number> DOUBLE_NUMBER
%token <str> STRING_VALUE

%token NEW_LINE
%token ERROR_UNEXPECTED_CHARACTERS


%%

configuration:
    configuration_parameters
    ;

configuration_parameters:
      /*empty*/
    | configuration_parameters configuration_parameter
    ;

configuration_parameter:
    parameter_name '=' assignment_value NEW_LINE
    {
        pContext->configuration.addItem(pContext->tmpConfigurationItem);
        printfIfDebugging(" [new parameter added]\n");
    }
    | parameter_name '=' NEW_LINE
    {
        pContext->tmpConfigurationItem.valueString.clear();
        pContext->tmpConfigurationItem.type = TConfigurationItem::eTYPE_STRING;
        printfIfDebugging(" [EMPTY TOKEN_PARAMETER] ");
        
        pContext->configuration.addItem(pContext->tmpConfigurationItem);
        printfIfDebugging(" [new parameter added]\n");
    }

parameter_name:
    TOKEN_PARAMETER_NAME
    {
        pContext->tmpConfigurationItem.name = $1;
        printfIfDebugging(" [parameter_name] ");
    }

assignment_value:
      INT_NUMBER {
        pContext->tmpConfigurationItem.valueInt = $1;
        pContext->tmpConfigurationItem.type = TConfigurationItem::eTYPE_INT;
        printfIfDebugging(" [INT_NUMBER] "); }
    | DOUBLE_NUMBER {
        pContext->tmpConfigurationItem.valueDouble = $1;
        pContext->tmpConfigurationItem.type = TConfigurationItem::eTYPE_DOUBLE;
        printfIfDebugging(" [DOUBLE_NUMBER] "); }
    | parameter_value {
        printfIfDebugging(" [TOKEN_PARAMETER] "); }

parameter_value:
    STRING_VALUE
    {
        pContext->tmpConfigurationItem.valueString = $1;
        pContext->tmpConfigurationItem.type = TConfigurationItem::eTYPE_STRING;
    }

%%


void configuration_error(CONFIGURATION_LTYPE* yylloc_param, TConfigurationParserContext* , const char* err)
{
    printf(" *** Lexical Error %s %d.%d-%d.%d\n", err,
         yylloc_param->first_line, yylloc_param->first_column,
         yylloc_param->last_line, yylloc_param->last_column);
}
