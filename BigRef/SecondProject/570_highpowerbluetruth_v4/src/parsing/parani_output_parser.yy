%require "2.6"
%define api.pure
%define api.prefix {parani_output_}

%locations
%defines
%error-verbose

%parse-param { TParaniOutputContext* pContext }
%lex-param { void* scannerPtr }

%code requires {
#include <stdio.h>
#include <string.h>
#include <limits>
#include "paranioutputparser.h"
#include "parser_common.h"


/*Basic set of lexer and parser functions*/
typedef struct PARANI_OUTPUT_LTYPE
{
    int first_line;
    int first_column;
    int last_line;
    int last_column;
} PARANI_OUTPUT_LTYPE;
#define PARANI_OUTPUT_LTYPE_IS_DECLARED 1
#define PARANI_OUTPUT_LTYPE_IS_TRIVIAL 1
#define YYLTYPE PARANI_OUTPUT_LTYPE

union PARANI_OUTPUT_STYPE
{
    int number;
    double double_number;
    char* str;
};
typedef union PARANI_OUTPUT_STYPE PARANI_OUTPUT_STYPE;
#define YYSTYPE PARANI_OUTPUT_STYPE

extern "C" int parani_output_lex(PARANI_OUTPUT_STYPE* lvalp, PARANI_OUTPUT_LTYPE* yylloc_param, void* pScanner);
extern int (*printfIfDebugging) (const char* format, ...);

void parani_output_error(PARANI_OUTPUT_LTYPE* yylloc_param, TParaniOutputContext* pContext, const char* err);

#define scannerPtr (pContext->pScanner)

}

%token TOKEN_CRLF
%token TOKEN_COLON
%token TOKEN_COMMA
%token TOKEN_SEMICOLON
%token TOKEN_OK
%token TOKEN_ERROR

%token <number> INT_NUMBER
%token <double_number> DOUBLE_NUMBER
%token <str> ASSIGNMENT_VALUE
%token <str> STRING_VALUE


%%

parser_output:
      result_ok
    | result_error
    | register_entries result_error
    | register_entries result_ok
    | dip_switch_value result_ok
    | bluetooth_settings result_error
    | bluetooth_settings result_ok
    | inquiry_results result_error
    | inquiry_results result_ok
    ;

string_value:
    STRING_VALUE
    {
        pContext->tmpStringCollection.push_back($1);
        printfIfDebugging(" [string_value]\n");
    }

result_ok:
    TOKEN_OK
    {
        pContext->paraniOutput.result = true;
        printfIfDebugging(" [ok]\n");
    }

result_error:
    TOKEN_ERROR
    {
        pContext->paraniOutput.result = false;
        printfIfDebugging(" [errror]\n");
    }


register_entries:
      register_entry
    | register_entries TOKEN_SEMICOLON register_entry
    ;

register_entry:
    string_value TOKEN_COLON string_value
    {
        TParaniOutputRegisterEntry entry;
        entry.name = pContext->tmpStringCollection[0];
        entry.valueString = pContext->tmpStringCollection[1];
        pContext->paraniOutput.addRegisterEntry(entry);
        pContext->tmpStringCollection.clear();

        printfIfDebugging(" [new register entry added]\n");
    }

dip_switch_value:
    string_value
    {
        TParaniOutputRegisterEntry entry;
        entry.name = "DIP";
        entry.valueString = pContext->tmpStringCollection[0];
        pContext->paraniOutput.addRegisterEntry(entry);
        pContext->tmpStringCollection.clear();

        printfIfDebugging(" [dip position]\n");
    }

bluetooth_settings:
    string_value TOKEN_COMMA
    string_value TOKEN_COMMA
    string_value TOKEN_COMMA
    string_value TOKEN_COMMA
    string_value TOKEN_COMMA
    string_value TOKEN_COMMA
    string_value
    {
        pContext->paraniOutput.bluetoothSettings.bdaddress = pContext->tmpStringCollection[0];
        pContext->paraniOutput.bluetoothSettings.deviceName = pContext->tmpStringCollection[1];
        pContext->paraniOutput.bluetoothSettings.operationMode = pContext->tmpStringCollection[2];
        pContext->paraniOutput.bluetoothSettings.operationStatus = pContext->tmpStringCollection[3];
        pContext->paraniOutput.bluetoothSettings.authentication = pContext->tmpStringCollection[4];
        pContext->paraniOutput.bluetoothSettings.dataEncryption = pContext->tmpStringCollection[5];
        pContext->paraniOutput.bluetoothSettings.hardwareFlowControl = pContext->tmpStringCollection[6];
        pContext->tmpStringCollection.clear();
        printfIfDebugging(" [bluetooth_setting]\n");
    }

inquiry_results:
      inquiry_result
    | inquiry_results inquiry_result
    ;

inquiry_result:
    string_value TOKEN_COMMA
    string_value TOKEN_COMMA
    string_value
    {
        TParaniOutputInquiryResultEntry entry;
        entry.address = pContext->tmpStringCollection[0];
        entry.name = pContext->tmpStringCollection[1];
        entry.deviceClass = pContext->tmpStringCollection[2];
        pContext->paraniOutput.addInquiryResultEntry(entry);
        pContext->tmpStringCollection.clear();

        printfIfDebugging(" [new inquiry result added (%s)]\n", entry.address.c_str());
    }


%%

void parani_output_error(PARANI_OUTPUT_LTYPE* yylloc_param, TParaniOutputContext* , const char* err)
{
    printf(" *** Lexical Error %s %d.%d-%d.%d\n", err,
         yylloc_param->first_line, yylloc_param->first_column,
         yylloc_param->last_line, yylloc_param->last_column);
}
