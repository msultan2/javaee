/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef I_HTTP_CLIENT_H_
#define I_HTTP_CLIENT_H_

#include "iobservable.h"
#include "types.h"


namespace Model
{
    class BrdfMongoConfiguration;
}


namespace BrdfServer
{

class IHTTPClient : public ::IObservable
{

public:

    //! destructor
    virtual ~IHTTPClient();

    enum
    {
        eLAST_RAW_DATA_HAS_BEEN_SENT = 1000,
        eLAST_RAW_DATA_HAS_FAILED,
    };


    virtual void setupConnectionParameters(
        const Model::BrdfMongoConfiguration& configuration) = 0;


    /**
     * @brief Send BlueTruth Raw Data
     * @param data data to be sent
     * @param useHttpVersion1_1
     * @param shouldCloseConnectionAfterSending
     */
    virtual bool sendRawData(
        const std::string& rawData,
        const bool useHttpVersion1_1 = true,
        const bool shouldCloseConnectionAfterSending = true) = 0;

    /**
     * Remove all previously submitted packets
     */
    virtual void clearAllocatedSendRequestList() = 0;

protected:

    //! default constructor
    IHTTPClient();
    //! copy constructor, not implemented
    IHTTPClient(const IHTTPClient& );
    //! assignment operator, not implemented
    IHTTPClient& operator=(const IHTTPClient& );

};

}

#endif //I_HTTP_CLIENT_H_
