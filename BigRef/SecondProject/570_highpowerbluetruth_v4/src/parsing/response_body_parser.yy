%require "3.0"
%define api.pure
%define api.prefix {response_body_}

%locations
%defines
%error-verbose

%parse-param { TResponseBodyContext* pContext }
%lex-param { void* scannerPtr }

%code requires {
#include <climits>
#include <stdio.h>
#include <string>
#include <algorithm>
#include "parser_common.h"
#include "responsebodyparser.h"

/*Basic set of lexer and parser functions*/
typedef struct RESPONSE_BODY_LTYPE
{
    int first_line;
    int first_column;
    int last_line;
    int last_column;
} RESPONSE_BODY_LTYPE;
#define RESPONSE_BODY_LTYPE_IS_DECLARED 1
#define RESPONSE_BODY_LTYPE_IS_TRIVIAL 1
#define YYLTYPE RESPONSE_BODY_LTYPE

union RESPONSE_BODY_STYPE
{
    int number;
};
typedef union RESPONSE_BODY_STYPE RESPONSE_BODY_STYPE;
#define YYSTYPE RESPONSE_BODY_STYPE

extern "C" int response_body_lex(RESPONSE_BODY_STYPE* lvalp, RESPONSE_BODY_LTYPE* yylloc_param, void* pScanner);
extern int (*printfIfDebugging) (const char* format, ...);

void response_body_error(RESPONSE_BODY_LTYPE* yylloc_param, TResponseBodyContext* pContext, const char* err);

#define scannerPtr (pContext->pScanner)
}

%token TOKEN_RELOAD_CONFIGURATION
%token TOKEN_OPEN_SSH_CONNECTION
%token TOKEN_CLOSE_SSH_CONNECTION
%token TOKEN_GET_STATUS_REPORT
%token TOKEN_REBOOT
%token TOKEN_CHANGE_SEED
%token TOKEN_LATCH_BACKGROUND
%token TOKEN_FLUSH_BACKGROUND

%token TOKEN_SEPARATOR
%token TOKEN_COLON
%token TOKEN_UNEXPECTED_ITEM

%token <number> INT_NUMBER

%%

message_body:
      /*empty*/
    | command
    | message_body separator command
    ;

command:
    TOKEN_RELOAD_CONFIGURATION
    {
        if (pContext->body.reloadConfiguration)
            pContext->body.ok = false; //repeated command
        pContext->body.reloadConfiguration = true;
        printfIfDebugging(" [reloadConfiguration] ");
    }
    | TOKEN_OPEN_SSH_CONNECTION TOKEN_COLON INT_NUMBER
    {
        if (pContext->body.openSSHConnection)
            pContext->body.ok = false; //repeated command
        if (($3 <= 0) || ($3 > USHRT_MAX))
            pContext->body.ok = false; //repeated command
        pContext->body.openSSHConnection = true;
        pContext->body.remotePortNumber = $3;
        printfIfDebugging(" [openSSHConnection %d] ", $3);
    }
    | TOKEN_CLOSE_SSH_CONNECTION
    {
        if (pContext->body.closeSSHConnection)
            pContext->body.ok = false; //repeated command
        pContext->body.closeSSHConnection = true;
        printfIfDebugging(" [closeSSHConnection] ");
    }
    | TOKEN_GET_STATUS_REPORT
    {
        if (pContext->body.getStatusReport)
            pContext->body.ok = false; //repeated command
        pContext->body.getStatusReport = true;
        printfIfDebugging(" [getStatusReport] ");
    }
    | TOKEN_REBOOT
    {
        if (pContext->body.reboot)
            pContext->body.ok = false; //repeated command
        pContext->body.reboot = true;
        printfIfDebugging(" [reboot] ");
    }
    | TOKEN_CHANGE_SEED
    {
        if (pContext->body.changeSeed)
            pContext->body.ok = false; //repeated command
        pContext->body.changeSeed = true;
        printfIfDebugging(" [changeSeed] ");
    }
    | TOKEN_LATCH_BACKGROUND TOKEN_COLON INT_NUMBER
    {
        if (pContext->body.latchBackground)
            pContext->body.ok = false; //repeated command
        if (($3 <= 0) || ($3 > UINT_MAX))
            pContext->body.ok = false; //repeated command
        pContext->body.latchBackground = true;
        pContext->body.latchBackgroundTimeInSeconds = $3;
        printfIfDebugging(" [latchBackground] ", $3);
    }
    | TOKEN_FLUSH_BACKGROUND
    {
        if (pContext->body.flushBackground)
            pContext->body.ok = false; //repeated command
        pContext->body.flushBackground = true;
        printfIfDebugging(" [flushBackground] ");
    }
    ;

separator:
    TOKEN_SEPARATOR   { printfIfDebugging(" [SEPARATOR] "); }
    ;

%%


void response_body_error(RESPONSE_BODY_LTYPE* yylloc_param, TResponseBodyContext* , const char* err)
{
    printf(" *** Lexical Error %s %d.%d-%d.%d\n", err,
         yylloc_param->first_line, yylloc_param->first_column,
         yylloc_param->last_line, yylloc_param->last_column);
}
