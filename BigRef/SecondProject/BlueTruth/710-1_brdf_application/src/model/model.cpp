#include "stdafx.h"
#include "model.h"

#include "activeboostasio.h"
#include "activeboostasiotcpclient.h"
#include "activetask.h"
#include "brdf_server/brdfserverhttpclient.h"
#include "brdf_server/brdfserverreporter.h"
#include "brdf_server/periodicallysendrawdatatask.h"
#include "configuration/brdfmongoconfiguration.h"
#include "configuration/brdfxmlconfiguration.h"
#include "logger.h"
#include "mongo/mongoclient.h"
#include "view.h"


namespace
{
    const char MODULE_NAME[] = "Model";
}

namespace Model
{

Model* Model::m_pInstance = 0;
bool Model::m_valid = true;


Model::~Model()
{
    if (m_pActivePeriodicallySendRawDataTask)
    {
        m_pActivePeriodicallySendRawDataTask->shutdown();
        m_pActivePeriodicallySendRawDataTask.reset();
    }
    //else do nothing

    if (m_pPeriodicallySendRawDataTask)
    {
        m_pPeriodicallySendRawDataTask->removeAllObservers();
        m_pPeriodicallySendRawDataTask.reset();
    }
    //else do nothing


    if (m_pRawDataWorkerThread)
    {
        m_pRawDataWorkerThread->stop();
        m_pRawDataWorkerThread.reset();
    }
    //else do nothing

    if (m_pRawDataActiveTCPClientTask)
    {
        m_pRawDataActiveTCPClientTask->stop();
        m_pRawDataActiveTCPClientTask->removeAllObservers();
        m_pRawDataActiveTCPClientTask.reset();
    }
    //else do nothing

    if (m_pActiveBrdfServerHTTPClient)
    {
        using BrdfServer::BrdfServerHTTPClient;

        m_pActiveBrdfServerHTTPClient->shutdown(
            "Model",
            &BrdfServerHTTPClient::sendStopSignal,
            (void*) &*m_pBrdfServerHTTPClient);
        m_pActiveBrdfServerHTTPClient.reset();
    }
    //else do nothing

    if (m_pActivePeriodicallySendRawDataTask)
    {
        m_pActivePeriodicallySendRawDataTask->shutdown();
        m_pActivePeriodicallySendRawDataTask.reset();
    }
    //else do nothing

    if (m_pBrdfServerHTTPClient)
    {
        m_pBrdfServerHTTPClient->removeAllObservers();
        m_pBrdfServerHTTPClient.reset();
    }
    //else do nothing

    if (m_pMongoClient)
    {
        m_pMongoClient->disconnect();
        m_pMongoClient.reset();
    }
    //else do nothing

    m_pBrdfMongoConfiguration.reset();
}

Model* Model::getInstancePtr()
{
    return m_pInstance;
}

Model::Model(BrdfXmlConfiguration& brdfConfiguration)
:
m_brdfXmlConfiguration(brdfConfiguration),
m_pBrdfMongoConfiguration(),
m_pMongoClient(),
m_pRawDataWorkerThread(),
m_pRawDataActiveTCPClientTask(),
m_pBrdfServerHTTPClient(),
m_pActiveBrdfServerHTTPClient(),
m_pBrdfServerReporter(),
m_pPeriodicallySendRawDataTask(),
m_pActivePeriodicallySendRawDataTask(),
m_clock()
{
    using BrdfServer::BrdfServerHTTPClient;
    using BrdfServer::BrdfServerReporter;
    using BrdfServer::PeriodicallySendRawDataTask;


    m_pBrdfMongoConfiguration = boost::shared_ptr<BrdfMongoConfiguration>(new BrdfMongoConfiguration());

    m_pMongoClient = boost::shared_ptr<MongoClient>(new MongoClient());
    if (!MongoClient::isDriverOk())
    {
        m_valid = false;
        return;
    }
    //else do nothing

    m_pMongoClient->setup(
        m_brdfXmlConfiguration.getDatabaseHost(),
        m_brdfXmlConfiguration.getDatabasePort(),
        m_brdfXmlConfiguration.getDatabaseUser(),
        m_brdfXmlConfiguration.getDatabasePassword(),
        m_brdfXmlConfiguration.getDetectorOwner());


    m_pRawDataWorkerThread = boost::shared_ptr<ActiveBoostAsio>(
        new ActiveBoostAsio("0"));

    m_pRawDataActiveTCPClientTask = boost::shared_ptr<ActiveBoostAsioTCPClient>(
        new ActiveBoostAsioTCPClient(
            0,
            m_pRawDataWorkerThread->getIoService()));


    m_pBrdfServerHTTPClient = boost::shared_ptr<BrdfServerHTTPClient>(
        new BrdfServerHTTPClient(
            m_pRawDataActiveTCPClientTask.get(),
            &m_clock));

    m_pActiveBrdfServerHTTPClient = boost::shared_ptr<ActiveTask>(
        new ActiveTask(m_pBrdfServerHTTPClient, "ActiveBrdfServerHTTPClient"));
    m_pActiveBrdfServerHTTPClient->setSleepTime(0);


    m_pBrdfServerReporter = boost::shared_ptr<BrdfServerReporter>(
        new BrdfServerReporter(
            m_pMongoClient,
            m_pBrdfServerHTTPClient,
            m_pBrdfMongoConfiguration,
            &m_clock));

    m_pPeriodicallySendRawDataTask = boost::shared_ptr<PeriodicallySendRawDataTask>(
        new PeriodicallySendRawDataTask(
            m_pBrdfServerReporter,
            &m_clock));

    m_pActivePeriodicallySendRawDataTask = boost::shared_ptr<ActiveTask> (
        new ActiveTask(m_pPeriodicallySendRawDataTask, "ActivePeriodicallySendRawDataTask"));


    m_pBrdfServerReporter->addObserver(&*m_pPeriodicallySendRawDataTask);
    m_pBrdfServerHTTPClient->addObserver(View::View::getInstancePtr());
    m_pBrdfServerHTTPClient->addObserver(&*m_pBrdfServerReporter);
    m_pBrdfServerHTTPClient->setup(m_pBrdfServerReporter);
    m_pRawDataActiveTCPClientTask->setup(&*m_pBrdfServerHTTPClient);
    m_pRawDataActiveTCPClientTask->addObserver(View::View::getInstancePtr());
    m_pRawDataActiveTCPClientTask->notifyObservers();

    m_pRawDataWorkerThread->start();
    m_pRawDataActiveTCPClientTask->start();
    m_pActiveBrdfServerHTTPClient->start();
    m_pPeriodicallySendRawDataTask->start(1); //Initially send ASAP, then read value from database and update it
    m_pActivePeriodicallySendRawDataTask->start();
}

bool Model::construct(BrdfXmlConfiguration& brdfConfiguration)
{
    if (m_pInstance == 0)
    {
        m_pInstance = new Model(brdfConfiguration);
    }
    else
    {
        // already constructed, do nothing!
    }

    return m_valid;
}

void Model::destruct()
{
    if (m_pInstance != 0)
    {
        delete m_pInstance;
        m_pInstance = 0;
    }
    else
    {
        // already destroyed, do nothing!
    }
}

bool Model::isValid()
{
    return m_valid;
}

void Model::readXmlConfigurationAndUpdateRelevantParameters()
{
    if (m_pInstance != 0)
    {
        m_pInstance->_readXmlConfigurationAndUpdateRelevantParameters();
    }
    //else do nothing
}

void Model::_readXmlConfigurationAndUpdateRelevantParameters()
{
    m_brdfXmlConfiguration.readAllParametersFromFile();

    if (m_pMongoClient)
    {
        m_pMongoClient->setup(
            m_brdfXmlConfiguration.getDatabaseHost(),
            m_brdfXmlConfiguration.getDatabasePort(),
            m_brdfXmlConfiguration.getDatabaseUser(),
            m_brdfXmlConfiguration.getDatabasePassword(),
            m_brdfXmlConfiguration.getDetectorOwner());
    }
    //else do nothing

    //Read logging level from the configuration file and set it
    {
        int logLevel = m_brdfXmlConfiguration.getFileLogLevel();
        Logger::setFileLogLevel(static_cast<ESeverityLevel>(logLevel));

        std::ostringstream ss;
        ss << "File log level changed to " << logLevel;
        Logger::log(LOG_LEVEL_NOTICE, ss.str().c_str());
    }

    {
        int logLevel = m_brdfXmlConfiguration.getConsoleLogLevel();
        Logger::setConsoleLogLevel(static_cast<ESeverityLevel>(logLevel));

        std::ostringstream ss;
        ss << "Console log level changed to " << logLevel;
        Logger::log(LOG_LEVEL_NOTICE, ss.str().c_str());
    }
}

void Model::updateParametersReadFromMongoDatabase()
{
    if (m_pInstance != 0)
    {
        m_pInstance->_updateParametersReadFromMongoDatabase();
    }
    //else do nothing
}

void Model::_updateParametersReadFromMongoDatabase()
{
    if (!m_pBrdfMongoConfiguration)
        return;


    if (m_pPeriodicallySendRawDataTask)
    {
        m_pPeriodicallySendRawDataTask->setupParameters(m_pBrdfMongoConfiguration->mongoCheckPeriodInSeconds);
    }
    //else do nothing

    if (m_pBrdfServerHTTPClient)
    {
        m_pBrdfServerHTTPClient->setupConnectionParameters(*m_pBrdfMongoConfiguration);
    }
    //else do nothing

    if (m_pRawDataActiveTCPClientTask)
    {
        m_pRawDataActiveTCPClientTask->setupConnection(
            m_pBrdfMongoConfiguration->host.c_str(),
            m_pBrdfMongoConfiguration->port,
            "");

        m_pRawDataActiveTCPClientTask->setConnectingTimeout(m_pBrdfMongoConfiguration->connectTimeoutInSeconds);
        m_pRawDataActiveTCPClientTask->setBackoffTime(m_pBrdfMongoConfiguration->backoffTimeInSeconds);
    }
    //else do nothing
}

} //namespace
