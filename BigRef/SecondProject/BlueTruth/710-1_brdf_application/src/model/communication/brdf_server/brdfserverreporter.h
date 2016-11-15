/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef IBRDF_SERVER_REPORTER_H_
#define IBRDF_SERVER_REPORTER_H_

#include "ibrdfserverreporter.h"
#include "iobserver.h"
#include "iobservable.h"
#include "types.h"

#include "ihttpclient.h"

#include <boost/shared_ptr.hpp>
#include <boost/thread/recursive_mutex.hpp>


class Clock;


namespace Model
{
    class MongoClient;
}


namespace BrdfServer
{


class BrdfServerReporter :
    public IBrdfServerReporter,
    public ::IObserver,
    public ::IObservable
{

public:
    BrdfServerReporter(
        boost::shared_ptr<Model::MongoClient> pMongoClient,
        boost::shared_ptr<IHTTPClient> pRawDataReportingClient,
        boost::shared_ptr<Model::BrdfMongoConfiguration> pBrdfMongoConfiguration,
        ::Clock* pClock);

    virtual ~BrdfServerReporter();

    virtual void notifyOfStateChange(IObservable* pObservable, const int index);

    virtual ESendRawDataResult sendRawData();

private:

    //! default constructor. Not implemented
    BrdfServerReporter();
    //! copy constructor. Not implemented
    BrdfServerReporter(const BrdfServerReporter& );
    //! assignment operator. Not implemented
    BrdfServerReporter& operator=(const BrdfServerReporter& );

    enum ERawDataState
    {
        eSTATE_IDLE,
        eSTATE_RAW_DATA_RETRIEVED_FROM_MONGO_AND_SUBMITTED,
        eSTATE_RAW_DATA_SENT,
        eSTATE_SIZE
    };
    static const char* STATE_NAME[eSTATE_SIZE];


    bool commitSendingOfRawData();
    void rollbackSendingOfRawData();
    void setState(const ERawDataState state);

    //Private members
    std::string m_detectionIds;
    boost::shared_ptr<Model::MongoClient> m_pMongoClient;
    boost::shared_ptr<IHTTPClient> m_pRawDataReportingClient;
    boost::shared_ptr<Model::BrdfMongoConfiguration> m_pBrdfMongoConfiguration;

    ERawDataState m_state;
    mutable boost::mutex m_stateMutex;

    ::Clock* m_pClock;
};

}

#endif //IBRDF_SERVER_REPORTER_H_
