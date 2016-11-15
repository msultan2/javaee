%require "3.0"
%define api.pure
%define api.prefix {http_response_}

%locations
%defines
%error-verbose

%parse-param { THttpResponseContext* pContext }
%lex-param { void* scannerPtr }

%code requires {
#include <stdio.h>
#include <string.h>
#include <limits>
#include "httpresponseparser.h"
#include "parser_common.h"


/*Basic set of lexer and parser functions*/
typedef struct HTTP_RESPONSE_LTYPE
{
    int first_line;
    int first_column;
    int last_line;
    int last_column;
} HTTP_RESPONSE_LTYPE;
#define HTTP_RESPONSE_LTYPE_IS_DECLARED 1
#define HTTP_RESPONSE_LTYPE_IS_TRIVIAL 1
#define YYLTYPE HTTP_RESPONSE_LTYPE

union HTTP_RESPONSE_STYPE
{
    int number;
    double double_number;
    char* str;
};
typedef union HTTP_RESPONSE_STYPE HTTP_RESPONSE_STYPE;
#define YYSTYPE HTTP_RESPONSE_STYPE

extern "C" int http_response_lex(HTTP_RESPONSE_STYPE* lvalp, HTTP_RESPONSE_LTYPE* yylloc_param, void* pScanner);
extern int (*printfIfDebugging) (const char* format, ...);

void http_response_error(HTTP_RESPONSE_LTYPE* yylloc_param, THttpResponseContext* pContext, const char* err);

#define scannerPtr (pContext->pScanner)

}


%token TOKEN_CONNECTION
%token TOKEN_CONTENT_LENGTH
%token TOKEN_CONTENT_TYPE
%token TOKEN_TRANSFER_ENCODING

%token TOKEN_CRLF


%token <number> INT_NUMBER
%token <double_number> DOUBLE_NUMBER
%token <str> TOKEN_ELEMENT
%token <str> TOKEN_QUOTED_STRING
%token <str> TOKEN_ABSOLUTE_URI
%token <str> TOKEN_ABSOLUTE_PATH
%token <str> TOKEN_METHOD
%token <str> TOKEN_HTTP_SLASH
%token <str> TOKEN_METHOD_EXTENSION_METHOD


%%

http_response_message: /*4.1*/
    /*generic_message: 5*/
    status_line
    headers
    empty_line
    message_body
    ;

status_line:
    http_slash_literal status_code status_code_reason_phrase TOKEN_CRLF
    {
        printfIfDebugging("Response Line: HTTP/%g %d %s\n",
            pContext->httpResponse.statusLine.httpVersion,
            pContext->httpResponse.statusLine.statusCode,
            pContext->httpResponse.statusLine.reasonPhrase.c_str());
    };

http_slash_literal:
    TOKEN_HTTP_SLASH
    {
        pContext->httpResponse.statusLine.httpVersion = yylval.double_number;
        printfIfDebugging("http_slash_literal (version: %g), ", yylval.double_number);
    }

status_code:
    INT_NUMBER
    {
        pContext->httpResponse.statusLine.statusCode = $1;
        printfIfDebugging("status_code (%d), ", $1);
    }

status_code_reason_phrase:
    token_element
    {
        pContext->httpResponse.statusLine.reasonPhrase = pContext->tmpString;
        printfIfDebugging("[status_code_reason_phrase received]");
    }


headers:
      /*empty*/
    | headers header
    ;

header:
    /*general headers: 4.5*/
       connection_
    /*response headers: 5.3*/
    | content_length_
    | content_type_
    | transfer_encoding_
    ;

/* -----------------------------------------------------------------------------
 * Generic elements
 */

empty_line:
    TOKEN_CRLF { printfIfDebugging("\t[Empty line]\n"); };



optional_parameter_list: /*3.6*/
      /*empty*/
    | parameter_list;

parameter_list:
    ';' parameter
    | parameter_list ';' parameter;

parameter: /*3.6*/
      token_element '=' INT_NUMBER
    | token_element '=' DOUBLE_NUMBER
    | token_element '=' token_element
    | token_element '=' TOKEN_QUOTED_STRING


token_element:
    TOKEN_ELEMENT
    {
        pContext->tmpString = $1;
    }

/* -----------------------------------------------------------------------------
 * Specific header fields
 */


/*---------------------------------------------------------*/
connection_: /*14.10*/
    token_connection_received token_element TOKEN_CRLF
    {
        pContext->httpResponse.connection = pContext->tmpString;
        printfIfDebugging("Connection: %s\n", pContext->httpResponse.connection.c_str());
    }

token_connection_received:
    TOKEN_CONNECTION
    {
        if (!pContext->httpResponse.connection.empty())
        {
            yyerror(&yylloc, pContext, "Repeated Connection token received");
        }
        //else do nothing

        printfIfDebugging("[Token connection received]");
    }

/*---------------------------------------------------------*/
content_length_: /*14.13*/
    token_content_length_received INT_NUMBER TOKEN_CRLF
    {
        if ($2 >= 0)
        {
            pContext->httpResponse.contentLength = $2;
            printfIfDebugging("Content-Length: %d\n", pContext->httpResponse.contentLength);
        }
        else
        {
            yyerror(&yylloc, pContext, "Invalid content length");
        }
    }

token_content_length_received:
    TOKEN_CONTENT_LENGTH
    {
        if (pContext->httpResponse.contentLength != std::numeric_limits<std::size_t>::max())
        {
            yyerror(&yylloc, pContext, "Repeated Content-Length token received");
        }
        //else do nothing

        printfIfDebugging("[Token content_length received]");
    }

/*---------------------------------------------------------*/
content_type_: /*14.17*/
    token_content_type_received token_content_type_type_received '/' token_content_type_subtype_received optional_parameter_list TOKEN_CRLF
    {
        printfIfDebugging("Content-Type: %s/%s\n",
            pContext->httpResponse.contentType.c_str(),
            pContext->httpResponse.contentSubtype.c_str());
    }

token_content_type_received:
    TOKEN_CONTENT_TYPE
    {
        if (
            (!pContext->httpResponse.contentType.empty()) &&
            (!pContext->httpResponse.contentSubtype.empty())
            )
        {
            yyerror(&yylloc, pContext, "Repeated Content-Type token received");
        }
        //else do nothing

        printfIfDebugging("[Token content_type received]");
    }

token_content_type_type_received:
    token_element
    {
        pContext->httpResponse.contentType = pContext->tmpString;
        printfIfDebugging("[Token content_type_type received]");
    }

token_content_type_subtype_received:
    token_element
    {
        pContext->httpResponse.contentSubtype = pContext->tmpString;
        printfIfDebugging("[Token content_type_subtype received]");
    }

/*---------------------------------------------------------*/
transfer_encoding_: /*14.13*/
    token_transfer_encoding_received token_transfer_encoding_value_received TOKEN_CRLF
    {
        printfIfDebugging("Transfer-Encoding: %s\n", pContext->httpResponse.transferEncoding.c_str());
    }

token_transfer_encoding_received:
    TOKEN_TRANSFER_ENCODING
    {
        if (!pContext->httpResponse.transferEncoding.empty())
        {
            yyerror(&yylloc, pContext, "Repeated Transfer-Encoding token received");
        }
        //else do nothing

        printfIfDebugging("[Token transfer_encoding received]");
    }

token_transfer_encoding_value_received:
    token_element
    {
        pContext->httpResponse.transferEncoding = pContext->tmpString;
        printfIfDebugging("[Token transfer_encoding_value received]");
    }

/*---------------------------------------------------------*/

message_body:
    /*empty*/
    | token_element
    {
        if (!pContext->httpResponse.body.empty())
        {
            yyerror(&yylloc, pContext, "Repeated http body contents received");
        }
        //else do nothing

        pContext->httpResponse.body = pContext->tmpString;
        printfIfDebugging("Http body: %s\n", pContext->httpResponse.body.c_str());
    }

%%

void http_response_error(HTTP_RESPONSE_LTYPE* yylloc_param, THttpResponseContext* , const char* err)
{
    printf(" *** Lexical Error %s %d.%d-%d.%d\n", err,
         yylloc_param->first_line, yylloc_param->first_column,
         yylloc_param->last_line, yylloc_param->last_column);
}
