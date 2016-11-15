/*Change the output file name to default so that automake process sticks to standard*/
%option outfile="lex.yy.c"
%option header="response_body_scanner.h"
%option prefix="response_body_"
%option extra-type="TResponseBodyContext* "

/*Get rid of some of the gcc compiler warings about functions defined but not used*/
%option nounput noinput yylineno noyywrap batch nounistd never-interactive

/*Allow multiple parsers in one program*/
%option reentrant
%option bison-bridge bison-locations

%{
#ifndef _WIN32
#include "config.h"
#endif

#include "response_body_parser.hh"
#include <cassert>

extern int (*printfIfDebugging) (const char* format, ...);

#define YY_USER_ACTION \
    assert(yyextra != 0); \
    yylloc->first_line = yylineno; \
    yylloc->first_column = yyextra->columnNo; \
    yylloc->last_line = yylineno; \
    yylloc->last_column = yyextra->columnNo+yyleng; \
    yyextra->columnNo += yyleng; \
    printfIfDebugging(" <advancing %d/%d> ", yylineno, yyextra->columnNo);

#ifdef yycolumn
#undef yycolumn
#define yycolumn yyextra->columnNo
#endif
%}

 /*Begining of definitions*/
DIGIT [0-9]
ALPHA [a-zA-Z]

INTEGER {DIGIT}+
DOUBLE {DIGIT}+"."{DIGIT}+
STRING_VALUE ({ALPHA}|{DIGIT}|[_+\-.*])*
QUOTED_STRING \"[^\"]\"

RELOAD_CONFIGURATION (reloadConfiguration)
OPEN_SSH_CONNECTION (openSSHConnection)
CLOSE_SSH_CONNECTION (closeSSHConnection)
GET_STATUS_REPORT (getStatusReport)
REBOOT (reboot)
CHANGE_SEED (changeSeed)
LATCH_BACKGROUND (latchBackground)
FLUSH_BACKGROUND (flushBackground)
SEPARATOR (,)
COLON (:)

WHITE_SPACE [ \t]

%% /*Beginning of rules*/
{RELOAD_CONFIGURATION} { printfIfDebugging(" RELOAD_CONFIGURATION "); return TOKEN_RELOAD_CONFIGURATION; }
{OPEN_SSH_CONNECTION}  { printfIfDebugging(" OPEN_SSH_CONNECTION "); return TOKEN_OPEN_SSH_CONNECTION; }
{CLOSE_SSH_CONNECTION} { printfIfDebugging(" CLOSE_SSH_CONNECTION "); return TOKEN_CLOSE_SSH_CONNECTION; }
{GET_STATUS_REPORT}    { printfIfDebugging(" GET_STATUS_REPORT "); return TOKEN_GET_STATUS_REPORT; }
{REBOOT}               { printfIfDebugging(" REBOOT "); return TOKEN_REBOOT; }
{CHANGE_SEED}          { printfIfDebugging(" CHANGE_SEED "); return TOKEN_CHANGE_SEED; }
{LATCH_BACKGROUND}     { printfIfDebugging(" LATCH_BACKGROUND "); return TOKEN_LATCH_BACKGROUND; }
{FLUSH_BACKGROUND}     { printfIfDebugging(" FLUSH_BACKGROUND "); return TOKEN_FLUSH_BACKGROUND; }
{SEPARATOR}            { printfIfDebugging(" SEPARATOR "); return TOKEN_SEPARATOR; }
{COLON}                { printfIfDebugging(" COLON "); return TOKEN_COLON; }
{WHITE_SPACE}          { printfIfDebugging(" WS"); } /*ignore whitespace*/
{INTEGER}              { printfIfDebugging(" (INTEGER %s)", yytext); yylval->number=atoi(yytext); return INT_NUMBER; }

{DOUBLE}               { printfIfDebugging(" (DOUBLE %s)", yytext); return TOKEN_UNEXPECTED_ITEM; }
{STRING_VALUE}         { printfIfDebugging(" (STRING_VALUE %s)", yytext); return TOKEN_UNEXPECTED_ITEM; }
{QUOTED_STRING}        { printfIfDebugging(" (QUOTED_STRING %s)", yytext); return TOKEN_UNEXPECTED_ITEM; }
[,;/=*\-]              { printfIfDebugging(" (CHAR %s)", yytext); return yytext[0]; }

%% /*Beginning of subroutines*/
