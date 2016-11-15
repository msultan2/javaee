/*Change the output file name to default so that automake process sticks to standard*/
%option outfile="lex.yy.c"
%option header="configuration_scanner.h"
%option prefix="configuration_"
%option extra-type="TConfigurationParserContext* "

/*Get rid of some of the gcc compiler warings about functions defined but not used*/
%option nounput noinput yylineno noyywrap batch nounistd never-interactive

/*Allow multiple parsers in one program*/
%option reentrant
%option bison-bridge bison-locations

%{
#ifndef _WIN32
#include "config.h"
#endif

#include "configuration_parser.hh"
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
CR (\x0d)
LF (\x0a)
CRLF (\x0d\x0a)
LFCR (\x0a\x0d)

INTEGER (\-)?{DIGIT}+
DOUBLE {DIGIT}+"."{DIGIT}+
PARAMETER_NAME {ALPHA}({ALPHA}|{DIGIT}|[_+\-])*
PARAMETER_STRING_VALUE ({ALPHA}|{DIGIT}|[_+\\. ,'";:=\(\)\[\]{}*&?/\-@'`])*


%x WAITING_FOR_EQ
%x WAITING_FOR_VALUE
%x PROCESSING_STRING
%x WAITING_FOR_NEW_LINE

%x PROCESSING_COMMENT

%% /*Beginning of rules*/

 /*Retrieve the parameter name*/
<INITIAL>{PARAMETER_NAME} { printfIfDebugging(" (PARAMETER_NAME %s)", yytext); yylval->str = yytext; BEGIN(WAITING_FOR_EQ); return TOKEN_PARAMETER_NAME; }
 /*Check for equal sign*/
<WAITING_FOR_EQ>= { printfIfDebugging(" (CHAR %s)", yytext); BEGIN(WAITING_FOR_VALUE); return yytext[0]; }
<WAITING_FOR_EQ>[\0x20\0x09] /*ignore white spaces*/
<WAITING_FOR_EQ>[,;:*&?/\-!] { printfIfDebugging(" (UNEXPECTED CHAR %s)", yytext); return ERROR_UNEXPECTED_CHARACTERS; }

 /*Retrieve the values as either int or double or string*/
<WAITING_FOR_VALUE>{INTEGER}   { printfIfDebugging(" (INTEGER %s)", yytext); yylval->number=atoi(yytext); BEGIN(WAITING_FOR_NEW_LINE); return INT_NUMBER; }
<WAITING_FOR_VALUE>{DOUBLE}    { printfIfDebugging(" (DOUBLE %s)", yytext); yylval->double_number=atof(yytext); BEGIN(WAITING_FOR_NEW_LINE); return DOUBLE_NUMBER; }
<WAITING_FOR_VALUE>{PARAMETER_STRING_VALUE} { printfIfDebugging(" %s ", yytext); yylval->str = yytext; BEGIN(WAITING_FOR_NEW_LINE); return STRING_VALUE; }
<WAITING_FOR_VALUE>({CRLF}|{LFCR}|{CR}|{LF}) { printfIfDebugging(" new_line "); BEGIN(INITIAL); return NEW_LINE; }

 /*Wait for a new line*/
<WAITING_FOR_NEW_LINE>({CRLF}|{LFCR}|{CR}|{LF}) { printfIfDebugging(" new_line "); BEGIN(INITIAL); return NEW_LINE; }

 /*Unexpected characters*/
<WAITING_FOR_NEW_LINE>([^{CR}{LF}]) { printfIfDebugging(" unexpected char %s", yytext); return ERROR_UNEXPECTED_CHARACTERS; }

 /*Process all the comments (starting with %% or #*/
<INITIAL>("#") { BEGIN(PROCESSING_COMMENT); printfIfDebugging(" comment_start "); }
<PROCESSING_COMMENT>({CRLF}|{LFCR}|{CR}|{LF}) { printfIfDebugging(" comment_end\n"); BEGIN(INITIAL); }
<PROCESSING_COMMENT>(.)* { printfIfDebugging("%s", yytext); /*ignore*/ }

 /*Ignore empty lines*/
<INITIAL>({CRLF}|{LFCR}|{CR}|{LF}) { printfIfDebugging(" empty_line\n"); }

[=] { printfIfDebugging(" (CHAR %s)", yytext); return yytext[0]; }
[,;:*&?/\-!] { printfIfDebugging(" (UNEXPECTED CHAR %s)", yytext); return ERROR_UNEXPECTED_CHARACTERS; }

%% /*Beginning of subroutines*/
