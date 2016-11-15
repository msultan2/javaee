#include "stdafx.h"
#include <gtest/gtest.h>

#include "httpresponseparser.h"

#define CRLF "\x0d\x0a"


TEST(HttpResponseParser, parse)
{
    {
        std::string input("");
        THttpResponseContext parsingResult;
        bool ok = HttpResponseParser::parse(input, parsingResult);

        EXPECT_FALSE(ok);
    }

    {
        std::string input(
"HTTP/1.1 200 OK" CRLF
"Date: Sat, 05 Oct 2013 21:21:12 GMT" CRLF
"Content-Type: text/plain" CRLF
"Content-Length: 23" CRLF
"Connection: close" CRLF
 CRLF
);
        THttpResponseContext parsingResult;
        bool ok = HttpResponseParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        if (ok)
        {
            EXPECT_EQ(1.1, parsingResult.httpResponse.statusLine.httpVersion);
            EXPECT_EQ(200, parsingResult.httpResponse.statusLine.statusCode);
            EXPECT_STREQ("OK", parsingResult.httpResponse.statusLine.reasonPhrase.c_str());
            EXPECT_STREQ("text", parsingResult.httpResponse.contentType.c_str());
            EXPECT_STREQ("plain", parsingResult.httpResponse.contentSubtype.c_str());
            EXPECT_EQ(23, parsingResult.httpResponse.contentLength);
            EXPECT_STREQ("close", parsingResult.httpResponse.connection.c_str());
        }
        //else do nothing
    }

    {
        std::string input(
"HTTP/1.0 400 Something Bad" CRLF
"Date: Sat, 05 Oct 2013 21:21:12 GMT" CRLF
"Content-Type: text/plain" CRLF
"Content-Length: 1000" CRLF
"Connection: open" CRLF
 CRLF
"This is message body"
);
        THttpResponseContext parsingResult;
        bool ok = HttpResponseParser::parse(input, parsingResult);

        EXPECT_TRUE(ok);
        if (ok)
        {
            EXPECT_EQ(1.0, parsingResult.httpResponse.statusLine.httpVersion);
            EXPECT_EQ(400, parsingResult.httpResponse.statusLine.statusCode);
            EXPECT_STREQ("Something Bad", parsingResult.httpResponse.statusLine.reasonPhrase.c_str());
            EXPECT_STREQ("text", parsingResult.httpResponse.contentType.c_str());
            EXPECT_STREQ("plain", parsingResult.httpResponse.contentSubtype.c_str());
            EXPECT_EQ(1000, parsingResult.httpResponse.contentLength);
            EXPECT_STREQ("open", parsingResult.httpResponse.connection.c_str());
            EXPECT_STREQ("This is message body", parsingResult.httpResponse.body.c_str());
        }
        //else do nothing
    }

}
