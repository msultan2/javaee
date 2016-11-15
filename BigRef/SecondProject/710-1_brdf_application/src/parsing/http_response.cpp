#include "stdafx.h"
#include "http_response.h"

#include <stdio.h>
#include <stdlib.h>
#include <limits>

#ifdef max
#undef max 
#endif

status_line::status_line()
:
httpVersion(0.0),
statusCode(0),
reasonPhrase()
{
    //do nothing
}

status_line::~status_line()
{
    //do nothing
}

void status_line::reset()
{
    httpVersion = 0.0;
    statusCode = 0;
    reasonPhrase.clear();
}

http_response::http_response()
:
connection(),
contentLength(std::numeric_limits<std::size_t>::max()),
contentType(),
contentSubtype(),
transferEncoding(),
body()
{
    //do nothing
}

http_response::~http_response()
{
    //do nothing
}

void http_response::reset()
{
    statusLine.reset();
    connection.clear();
    contentLength = std::numeric_limits<std::size_t>::max();
    contentType.clear();
    contentSubtype.clear();
    transferEncoding.clear();
    body.clear();
}

void initialiseResponseContents(THttpResponse* pResponse)
{
    if (pResponse == 0)
    {
        return;
    }
    //else continue

    pResponse->statusLine.reset();
    pResponse->connection.clear();
    pResponse->contentLength = std::numeric_limits<std::size_t>::max();
    pResponse->contentType.clear();
    pResponse->contentSubtype.clear();
    pResponse->transferEncoding.clear();
}

void printResponseContents(const THttpResponse* pResponse)
{
    if (pResponse == 0)
    {
        return;
    }
    //else continue

    printf("Response: \n");
    printf("HTTP/%g %d %s\n",
        pResponse->statusLine.httpVersion,
        pResponse->statusLine.statusCode,
        pResponse->statusLine.reasonPhrase.c_str());
    if (!pResponse->connection.empty())
    {
        printf("Connection: %s\n", pResponse->connection.c_str());
    }
    //else do nothing

    if (!pResponse->contentType.empty())
    {
        printf("Content-Type: %s/%s\n", pResponse->contentType.c_str(), pResponse->contentSubtype.c_str());
    }
    //else do nothing

    if (!pResponse->contentType.empty())
    {
        printf("Transfer-Encoding: %s\n", pResponse->transferEncoding.c_str());
    }
    //else do nothing
}

void deleteResponseContents(THttpResponse* pResponse)
{
    if (pResponse == 0)
    {
        return;
    }
    //else continue
}
