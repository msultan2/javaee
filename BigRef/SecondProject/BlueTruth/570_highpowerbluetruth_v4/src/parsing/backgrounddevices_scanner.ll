/*Change the output file name to default so that automake process sticks to standard*/
%option outfile="lex.yy.c"
%option header="backgrounddevices_scanner.h"
%option prefix="backgrounddevices_"
%option extra-type="TBackgroundDevicesParserContext* "

/*Get rid of some of the gcc compiler warings about functions defined but not used*/
%option nounput noinput yylineno noyywrap batch nounistd never-interactive

/*Allow multiple parsers in one program*/
%option reentrant
%option bison-bridge bison-locations

%{
#ifndef _WIN32
#include "config.h"
#endif

#include "backgrounddevices_parser.hh"
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

#define YY_USER_INIT { yylineno = 0; }

%}

 /*Begining of definitions*/
ALPHA [a-zA-Z]
DIGIT [0-9]
HEXDIGIT [0-9a-fA-F]
CR (\x0d)
LF (\x0a)
CRLF (\x0d\x0a)
LFCR (\x0a\x0d)

HEXINTEGER (\-)?{HEXDIGIT}+


%x WAITING_FOR_FIRST_COMMA
%x WAITING_FOR_FIRST_OBSERVED
%x WAITING_FOR_SECOND_COMMA
%x WAITING_FOR_LAST_OBSERVED
%x WAITING_FOR_NEW_LINE

%x PROCESSING_COMMENT

%% /*Beginning of rules*/

<INITIAL>{HEXINTEGER}                      { printfIfDebugging(" (ADDRESS %s)", yytext); yylval->str = yytext; BEGIN(WAITING_FOR_FIRST_COMMA); return TOKEN_ADDRESS; }
<WAITING_FOR_FIRST_COMMA>,                 { printfIfDebugging(" ,", yytext); BEGIN(WAITING_FOR_FIRST_OBSERVED); }
<WAITING_FOR_FIRST_OBSERVED>{HEXINTEGER}   { printfIfDebugging(" (FT %s)",      yytext); yylval->str = yytext; BEGIN(WAITING_FOR_SECOND_COMMA);  return TOKEN_FIRST_OBSERVED; }
<WAITING_FOR_SECOND_COMMA>,                { printfIfDebugging(" ,", yytext); BEGIN(WAITING_FOR_LAST_OBSERVED); }
<WAITING_FOR_LAST_OBSERVED>{HEXINTEGER}    { printfIfDebugging(" (LT %s)",      yytext); yylval->str = yytext; BEGIN(WAITING_FOR_NEW_LINE);       return TOKEN_LAST_OBSERVED; }

 /*Wait for a new line*/
<WAITING_FOR_NEW_LINE>({CRLF}|{LFCR}|{CR}|{LF}) { printfIfDebugging(" new_line "); BEGIN(INITIAL); return TOKEN_NEW_LINE; }

%% /*Beginning of subroutines*/
