#include "stdafx.h"
#include "brdf_server/brdfserverreporter.h"

#include "app.h"
#include "clock.h"
//#include "brdf_server/periodicallysendstatisticsreporttask.h"
#include "brdf_server/ihttpclient.h"
#include "configuration/brdfmongoconfiguration.h"
#include "configuration/ibrdfxmlconfiguration.h"
#include "logger.h"
#include "model.h"
#include "mongo/mongoclient.h"
#include "os_utilities.h"
#include "version.h"
#include "utils.h"

#include <algorithm>
#include <boost/thread/locks.hpp>
#include <signal.h>


namespace BrdfServer
{

BrdfServerReporter::BrdfServerReporter(
    boost::shared_ptr<Model::MongoClient> pMongoClient,
    boost::shared_ptr<IHTTPClient> pRawDataReportingClient,
    boost::shared_ptr<Model::BrdfMongoConfiguration> pBrdfMongoConfiguration,
    ::Clock* pClock)
:
IBrdfServerReporter(),
::IObserver(),
::IObservable(),
m_detectionIds(),
m_pMongoClient(pMongoClient),
m_pRawDataReportingClient(pRawDataReportingClient),
m_pBrdfMongoConfiguration(pBrdfMongoConfiguration),
m_state(eSTATE_IDLE),
m_stateMutex(),
m_pClock(pClock)
{
    assert(m_pClock != 0);
}

BrdfServerReporter::~BrdfServerReporter()
{
    //do nothing
}

BrdfServerReporter::ESendRawDataResult BrdfServerReporter::sendRawData()
{
    if (m_pRawDataReportingClient == 0)
    {
        return eSEND_RAW_DATA_RESULT__ERROR_WITH_CONFIGURATION;
    }
    //else do nothing

    if (m_state == eSTATE_RAW_DATA_RETRIEVED_FROM_MONGO_AND_SUBMITTED)
    {
        return eSEND_RAW_DATA_RESULT__BUSY;
    }
    //else do nothing

    if (m_state == eSTATE_RAW_DATA_SENT)
    {
        if (!commitSendingOfRawData())
        {
            return eSEND_RAW_DATA_RESULT__ERROR_WITH_DATABASE;
        }
        //else do nothing
    }
    //else do nothing

    bool ok = true;
    if (!m_pMongoClient->isConnected())
    {
        ok = m_pMongoClient->connect();
    }
    //else do nothing - already connected

    if (ok)
    {
        if (m_pMongoClient->getConfiguration(*m_pBrdfMongoConfiguration))
        {
            Model::Model::updateParametersReadFromMongoDatabase();
        }
        //else do nothing


        std::string detectors;
        std::string detections;
        if (
            m_pMongoClient->getDetectorNames(detectors) &&
            m_pMongoClient->getDetectionIds(detectors, m_detectionIds) &&
            m_pMongoClient->getDetectionByIds(m_detectionIds, detections)
            )
        {
            if (!detections.empty())
            {
                m_pRawDataReportingClient->clearAllocatedSendRequestList();
                if (m_pRawDataReportingClient->sendRawData(detections, true, false))
                {
                    setState(eSTATE_RAW_DATA_RETRIEVED_FROM_MONGO_AND_SUBMITTED);
                    return eSEND_RAW_DATA_RESULT__OK;
                }
                else
                {
                    return eSEND_RAW_DATA_RESULT__UNABLE_TO_SEND;
                }
            }
            else
            {
                m_pMongoClient->disconnect();
                return eSEND_RAW_DATA_RESULT__OK_NO_RECORDS_FOUND;
            }
        }
        else
        {
            m_pMongoClient->disconnect();
            return eSEND_RAW_DATA_RESULT__OK_NO_RECORDS_FOUND;
        }
    }
    else
    {
        return eSEND_RAW_DATA_RESULT__ERROR_WITH_DATABASE;
    }
}

bool BrdfServerReporter::commitSendingOfRawData()
{
    bool result = false;

    if (m_pMongoClient->removeDetectionByIds(m_detectionIds))
    {
        //m_pMongoClient->disconnect(); //Commented to increase throughput in the case of a lot of data
        result = true;
    }
    //else do nothing

    m_detectionIds.clear();
    return result;
}

void BrdfServerReporter::rollbackSendingOfRawData()
{
    m_detectionIds.clear();
}

void BrdfServerReporter::notifyOfStateChange(IObservable* pObservable, const int index)
{
    assert(pObservable != 0);
    //Additional brackets have been added to isolate variables and protect against typos
    //while copy-and-paste

    {
        IHTTPClient* pHTTPClient =
            dynamic_cast<IHTTPClient* >(pObservable);

        if (pHTTPClient != 0)
        {
            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
            {
                std::ostringstream ss;
                ss << "BrdfServerReporter::notifyOfStateChange() "
                      "Signal received from IHTTPClient: ";

                if (index == eLAST_RAW_DATA_HAS_BEEN_SENT)
                    ss << "eLAST_RAW_DATA_HAS_BEEN_SENT";
                else if (index == eLAST_RAW_DATA_HAS_FAILED)
                    ss << "eLAST_RAW_DATA_HAS_FAILED";
                else
                    ss << index;

                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            }
            //else do nothing

            switch (index)
            {
                case IHTTPClient::eLAST_RAW_DATA_HAS_BEEN_SENT:
                {
                    setState(eSTATE_RAW_DATA_SENT);
                    if (commitSendingOfRawData())
                    {
                        setState(eSTATE_IDLE);
                    }
                    //else do nothing

                    notifyObservers(eLAST_RAW_DATA_HAS_BEEN_SENT);

                    break;
                }

                case IHTTPClient::eLAST_RAW_DATA_HAS_FAILED:
                {
                    rollbackSendingOfRawData();
                    setState(eSTATE_IDLE);

                    notifyObservers(eLAST_RAW_DATA_HAS_FAILED);

                    break;
                }

                default:
                {
                    //do nothing
                }
            }

            return;
        }
        else
        {
            if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
            {
                std::ostringstream ss;
                ss << "BrdfServerReporter::notifyOfStateChange() "
                      "Signal received from unknown party "<< pObservable << " : " << index;

                Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
            }
            //else do nothing
        }
    }
}

void BrdfServerReporter::setState(const ERawDataState state)
{
    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
    {
        assert(static_cast<size_t>(state) < eSTATE_SIZE);
        assert(static_cast<size_t>(m_state) < eSTATE_SIZE);

        std::ostringstream ss;
        ss << "BrdfServerReporter ----------- " //setState must be invoked from the run() function
              "State change to " << STATE_NAME[static_cast<size_t>(state)]
           << " (from " << STATE_NAME[static_cast<size_t>(m_state)] << ")";

        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
    }
    //else do nothing

    boost::unique_lock<boost::mutex> lock(m_stateMutex);
    m_state = state;
}

const char* BrdfServerReporter::STATE_NAME[eSTATE_SIZE] =
{
    "STATE_IDLE",
    "STATE_RAW_DATA_RETRIEVED_FROM_MONGO_AND_SUBMITTED",
    "STATE_RAW_DATA_SENT"
};



} //namespace
