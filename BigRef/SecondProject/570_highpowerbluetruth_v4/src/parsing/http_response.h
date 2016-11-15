#ifndef HTTP_RESPONSE_H_
#define HTTP_RESPONSE_H_

#include <string>


/*Elements of the response line*/
struct status_line
{
    double httpVersion;
    int statusCode;
    std::string reasonPhrase;

    status_line();
    ~status_line();

    void reset();
};
typedef struct status_line TStatusLine;

struct http_response
{
    TStatusLine statusLine;
    std::string connection;
    size_t contentLength;
    std::string contentType;
    std::string contentSubtype;
    std::string transferEncoding;
    std::string body;

    http_response();
    ~http_response();

    void reset();
};
typedef struct http_response THttpResponse;


void initialiseResponseContents(THttpResponse* pResponse);
void printResponseContents(const THttpResponse* pResponse);
void deleteResponseContents(THttpResponse* pResponse);


#endif
