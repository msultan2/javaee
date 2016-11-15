/*Change the output file name to default so that automake process sticks to standard*/
%option outfile="lex.yy.c"
%option header="http_response_scanner.h"
%option prefix="http_response_"
%option extra-type="THttpResponseContext* "

/*Get rid of some of the gcc compiler warings about functions defined but not used*/
%option nounput noinput yylineno noyywrap batch nounistd never-interactive

/*Allow multiple parsers in one program*/
%option reentrant
%option bison-bridge bison-locations

%{
#ifndef _WIN32
#include "config.h"
#endif

#include "http_response_parser.hh"
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

 /* RFC2616 2.2 */
ALPHA [a-zA-Z]
DIGIT [0-9]
CRLF (\x0d\x0a)

INTEGER {DIGIT}+
DOUBLE {DIGIT}+"."{DIGIT}+
TTOKEN {ALPHA}({ALPHA}|{DIGIT}|[_+\-])*
QUOTED_STRING \"[^\"]\"
WHITE_SPACE [ \t]

TIME_STRING {DIGIT}{2}+":"{DIGIT}{2}+":"{DIGIT}{2}

/*3.2.2*/
ABSOLUTE_URI "http://"{ALPHA}({ALPHA}|{DIGIT}|[_.\-])*(":"{INTEGER})*({ALPHA}|{DIGIT}|[_./\-])*
ABS_PATH "/"({ALPHA}|{DIGIT}|[_./?=\-])*

%s sCONTENTS_STATUS_LINE_WAITING_FOR_INTEGER
%s sCONTENTS_STATUS_LINE_WAITING_FOR_REASON_PHRASE
%s sCONTENTS_STATUS_LINE_WAITING_FOR_CRLF
%s sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS
%s sCONTENTS_RELEVANT
%s sCONTENTS_NOT_RELEVANT
%s HTML_SECTION

%% /*Beginning of rules*/

 /*Status line*/
<INITIAL>"HTTP/"{INTEGER}"."{INTEGER} {
    printfIfDebugging(" %s ", yytext);
    yylval->double_number=atof(yytext+5) /*offset to ignore HTTP literal*/;
    BEGIN(sCONTENTS_STATUS_LINE_WAITING_FOR_INTEGER);
    return TOKEN_HTTP_SLASH; }
<sCONTENTS_STATUS_LINE_WAITING_FOR_INTEGER>{INTEGER} {
    printfIfDebugging(" (INTEGER %s) ", yytext);
    yylval->number=atoi(yytext);
    BEGIN(sCONTENTS_STATUS_LINE_WAITING_FOR_REASON_PHRASE);
    return INT_NUMBER; }
<sCONTENTS_STATUS_LINE_WAITING_FOR_REASON_PHRASE>{ALPHA}.*/{CRLF} { /*Ignore the starting space*/
    printfIfDebugging(" Reason Phrase (%s) ", yytext);
    yylval->str = yytext;
    BEGIN(sCONTENTS_STATUS_LINE_WAITING_FOR_CRLF);
    return TOKEN_ELEMENT;}
<sCONTENTS_STATUS_LINE_WAITING_FOR_CRLF>{CRLF} {
    printfIfDebugging(" CRLF\n");
    BEGIN(sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS);
    return TOKEN_CRLF; }
<sCONTENTS_STATUS_LINE_WAITING_FOR_INTEGER,sCONTENTS_STATUS_LINE_WAITING_FOR_REASON_PHRASE>{WHITE_SPACE} { printfIfDebugging(" (WS)"); } /*ignore whitespace*/

 /*Headers*/
<sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS>(?i:Connection:)" " {
    printfIfDebugging("%s", yytext);
    BEGIN(sCONTENTS_RELEVANT);
    return TOKEN_CONNECTION; }
<sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS>(?i:Content-Length:)" " {
    printfIfDebugging("%s", yytext);
    BEGIN(sCONTENTS_RELEVANT);
    return TOKEN_CONTENT_LENGTH; }
<sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS>(?i:Content-Type:)" " {
    printfIfDebugging("%s", yytext);
    BEGIN(sCONTENTS_RELEVANT);
    return TOKEN_CONTENT_TYPE; }
<sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS>(?i:Transfer-Encoding:)" " {
    printfIfDebugging("%s", yytext);
    BEGIN(sCONTENTS_RELEVANT);
    return TOKEN_TRANSFER_ENCODING; }
<sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS>({ALPHA}({ALPHA}|{DIGIT}|[_+\-])*[:])" " {
    printfIfDebugging("\t\tIgnoring: %s", yytext);
    BEGIN(sCONTENTS_NOT_RELEVANT); /*do not return anything*/}


<sCONTENTS_RELEVANT>{INTEGER}                    { printfIfDebugging(" (INTEGER %s)", yytext); yylval->number=atoi(yytext); return INT_NUMBER; }
<sCONTENTS_RELEVANT>{DOUBLE}                     { printfIfDebugging(" (DOUBLE %s)", yytext); yylval->double_number=atof(yytext); return DOUBLE_NUMBER; }
<sCONTENTS_RELEVANT>{TTOKEN}                     { printfIfDebugging(" (TTOKEN %s)", yytext); yylval->str = yytext; return TOKEN_ELEMENT; }
<sCONTENTS_RELEVANT>{QUOTED_STRING}              { printfIfDebugging(" (QUOTED_STRING %s)", yytext); return TOKEN_QUOTED_STRING; }
<sCONTENTS_RELEVANT>[,;:/=*\-]                   { printfIfDebugging(" (CHAR %s)", yytext); return yytext[0]; }
<sCONTENTS_RELEVANT>{WHITE_SPACE}                { printfIfDebugging(" (WS)"); } /*ignore whitespace*/
<sCONTENTS_RELEVANT>{CRLF}                       { printfIfDebugging(" CRLF\n"); BEGIN(sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS); return TOKEN_CRLF; }

 /*Ignore anything else*/
<sCONTENTS_NOT_RELEVANT>{CRLF}                   { printfIfDebugging(" CRLF\n"); BEGIN(sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS); }
<sCONTENTS_NOT_RELEVANT>.*/{CRLF}                { printfIfDebugging("Ignored (%s)", yytext); }

<sCONTENTS_STATUS_LINE_READ_SCANNING_HEADERS>{CRLF} { printfIfDebugging(" CRLF\n"); BEGIN(HTML_SECTION); return TOKEN_CRLF; }
<HTML_SECTION>(?s:.*)                             { printfIfDebugging(" (HTML_CONTENTS %s)", yytext); yylval->str = yytext; return TOKEN_ELEMENT; }

%% /*Beginning of subroutines*/

