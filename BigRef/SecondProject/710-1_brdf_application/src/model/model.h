/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
 */

#ifndef _MODEL_H_
#define _MODEL_H_


#include "clock.h"
#include "types.h"

#include <boost/shared_ptr.hpp>
#include <string>
#include <vector>


namespace BrdfServer
{
    class BrdfServerHTTPClient;
    class BrdfServerReporter;
    class PeriodicallySendRawDataTask;
}


/**
 * The Model namespace contains all classes related to Model
 * in the Model-View-Controller (MVC) pattern.
 */
namespace Model
{

class ActiveBoostAsio;
class ActiveBoostAsioTCPClient;
class ActiveTask;
class BrdfMongoConfiguration;
class BrdfXmlConfiguration;
class MongoClient;


class Model
{
public:
    //! destructor
    virtual ~Model();

    static bool construct(BrdfXmlConfiguration& brdfConfiguration);
    static void destruct();

    static Model* getInstancePtr();
    static bool isValid();

    static void readXmlConfigurationAndUpdateRelevantParameters();
    static void updateParametersReadFromMongoDatabase();

private:

    //! default constructor
    explicit Model(BrdfXmlConfiguration& bfdfConfiguration);

    //! default constructor. Not implemented
    Model();
    //! copy constructor. Not implemented
    Model(const Model& );
    //! assignment operator. Not implemented
    Model& operator=(const Model& );


    void _readXmlConfigurationAndUpdateRelevantParameters();
    void _updateParametersReadFromMongoDatabase();

    //Private members:

    static Model* m_pInstance;
    static bool m_valid;

    BrdfXmlConfiguration& m_brdfXmlConfiguration;
    boost::shared_ptr<BrdfMongoConfiguration> m_pBrdfMongoConfiguration;

    boost::shared_ptr<MongoClient> m_pMongoClient;

    boost::shared_ptr<ActiveBoostAsio> m_pRawDataWorkerThread;
    boost::shared_ptr<ActiveBoostAsioTCPClient> m_pRawDataActiveTCPClientTask;

    boost::shared_ptr<BrdfServer::BrdfServerHTTPClient> m_pBrdfServerHTTPClient;
    boost::shared_ptr<ActiveTask> m_pActiveBrdfServerHTTPClient;
    boost::shared_ptr<BrdfServer::BrdfServerReporter> m_pBrdfServerReporter;

    boost::shared_ptr<BrdfServer::PeriodicallySendRawDataTask> m_pPeriodicallySendRawDataTask;
    boost::shared_ptr<ActiveTask> m_pActivePeriodicallySendRawDataTask;


    ::Clock m_clock;

};

} //namespace Model

#endif //_MODEL_H_
