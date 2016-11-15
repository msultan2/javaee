/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    06/10/2013  RG      001         V1.00 First Issue
*/

#ifndef RESPONSE_BODY_PARSER_H_
#define RESPONSE_BODY_PARSER_H_


#include <string>


struct TResponseBody
{
    bool reloadConfiguration;
    bool openSSHConnection;
    unsigned short remotePortNumber;
    bool closeSSHConnection;
    bool getStatusReport;
    bool reboot;
    bool changeSeed;
    bool latchBackground;
    unsigned int latchBackgroundTimeInSeconds;
    bool flushBackground;

    bool ok;

    TResponseBody();

    void print();
};

struct TResponseBodyContext
{
    void* pScanner;
    int columnNo;
    TResponseBody body;

    TResponseBodyContext();
    ~TResponseBodyContext();
};

class ResponseBodyParser
{
public:

    virtual ~ResponseBodyParser();

    static bool parse(const std::string& input, TResponseBodyContext& parsingResult);

private:
    //! default constructor, not implemented
    ResponseBodyParser();
    //! copy constructor, not implemented
    ResponseBodyParser& operator=(const ResponseBodyParser&);
    //! copy assignment operator, not implemented
    ResponseBodyParser(const ResponseBodyParser& rhs);

};

#endif // RESPONSE_BODY_PARSER_H_
