/*Change the output file name to default so that automake process sticks to standard*/
%option outfile="lex.yy.c"
%option header="parani_output_scanner.h"
%option prefix="parani_output_"
%option extra-type="TParaniOutputContext* "

/*Get rid of some of the gcc compiler warings about functions defined but not used*/
%option nounput noinput yylineno noyywrap batch nounistd never-interactive

/*Allow multiple parsers in one program*/
%option reentrant
%option bison-bridge bison-locations

%{
#ifndef _WIN32
#include "config.h"
#endif

#include "parani_output_parser.hh"
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

#define ECHO {}

#ifdef yycolumn
#undef yycolumn
#define yycolumn yyextra->columnNo
#endif

#define YY_USER_INIT { yylineno = 0; }

%}

 /*Begining of definitions*/

 /* RFC2616 2.2 */
ALPHA [a-zA-Z]
DIGIT [0-9]
CRLF (\x0d\x0a)|(\x0a)
COLON (:)
COMMA (,)
SEMICOLON (;)

STRING_VALUE ({ALPHA}|{DIGIT}|[_+\-.*])*

WHITE_SPACE [ \t]

%% /*Beginning of rules*/
{STRING_VALUE} { printfIfDebugging(" %s ", yytext); yylval->str = yytext; return STRING_VALUE; }

{WHITE_SPACE} { printfIfDebugging(" WS"); } /*ignore whitespace*/
{COLON} { printfIfDebugging(" : "); return TOKEN_COLON; }
{COMMA} { printfIfDebugging(" , "); return TOKEN_COMMA; }
{SEMICOLON} { printfIfDebugging(" ; "); return TOKEN_SEMICOLON; }

{CRLF}{CRLF}OK{CRLF} { printfIfDebugging(" OK "); return TOKEN_OK; }
{CRLF}{CRLF}ERROR{CRLF} { printfIfDebugging(" ERROR "); return TOKEN_ERROR; }

%% /*Beginning of subroutines*/

