/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef I_BRDF_SERVER_REPORTER_H_
#define I_BRDF_SERVER_REPORTER_H_

#include "ihttpclient.h"


namespace BrdfServer
{

class IBrdfServerReporter
{
public:

    //! destructor
    virtual ~IBrdfServerReporter();

    enum ESendRawDataResult
    {
        eSEND_RAW_DATA_RESULT__OK,
        eSEND_RAW_DATA_RESULT__OK_NO_RECORDS_FOUND,
        eSEND_RAW_DATA_RESULT__BUSY,
        eSEND_RAW_DATA_RESULT__ERROR_WITH_DATABASE,
        eSEND_RAW_DATA_RESULT__ERROR_WITH_CONFIGURATION,
        eSEND_RAW_DATA_RESULT__UNABLE_TO_SEND,
    };
    virtual ESendRawDataResult sendRawData() = 0;


    enum
    {
        eLAST_RAW_DATA_HAS_BEEN_SENT = 1000,
        eLAST_RAW_DATA_HAS_FAILED,
    };


protected:

    //! default constructor
    IBrdfServerReporter();
    //! copy constructor
    IBrdfServerReporter(const IBrdfServerReporter& );
    //! assignment operator
    IBrdfServerReporter& operator=(const IBrdfServerReporter& );

};

}
//namespace

#endif //I_BRDF_SERVER_REPORTER_H_
