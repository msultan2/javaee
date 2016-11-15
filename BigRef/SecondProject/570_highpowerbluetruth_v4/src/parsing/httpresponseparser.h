/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef HTTP_RESPONSE_PARSER_H_
#define HTTP_RESPONSE_PARSER_H_

#include "http_response.h"
#include <string>


struct THttpResponseContext
{
    void* pScanner;
    int columnNo;
    THttpResponse httpResponse;
    std::string tmpString;

    THttpResponseContext();
    ~THttpResponseContext();

    void reset();
};


class HttpResponseParser
{
public:

    virtual ~HttpResponseParser();


    static bool parse(const std::string& input, THttpResponseContext& parsingResult);

private:
    //! default constructor, not implemented
    HttpResponseParser();
    //! copy constructor, not implemented
    HttpResponseParser& operator=(const HttpResponseParser&);
    //! copy assignment operator, not implemented
    HttpResponseParser(const HttpResponseParser& rhs);

};

#endif // HTTP_RESPONSE_PARSER_H_
