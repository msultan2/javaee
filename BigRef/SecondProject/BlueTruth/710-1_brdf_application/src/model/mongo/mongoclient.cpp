#include "mongoclient.h"


#include "brdfmongoconfiguration.h"
#include "logger.h"
#include "utils.h"

#include <cassert>
#include <mongo/client/dbclient.h>
#include <mongo/client/connpool.h>
#include <sstream>

using boost::shared_ptr;


namespace Model
{

int MongoClient::m_numberOfInstances = 0;
bool MongoClient::m_driverOk = false;
bool MongoClient::m_driverHasBeenShutDown = false;


MongoClient::MongoClient()
:
m_pConnection(),
m_databaseHost(),
m_databasePort(),
m_databaseUser(),
m_databasePassword(),
m_sensorOwner(),
m_setupMutex(),
m_numberOfDetectionsPerReportLimit(0)
{
    initialiseDriver();
}

MongoClient::~MongoClient()
{
    shutdownDriver();
}

void MongoClient::setup(
    const std::string& databaseHost,
    const uint16_t databasePort,
    const std::string& databaseUser,
    const std::string& databasePassword,
    const std::string& sensorOwner)
{
    boost::lock_guard<boost::mutex> lock(m_setupMutex);

    m_databaseHost = databaseHost;
    m_databasePort = databasePort;
    m_databaseUser = databaseUser;
    m_databasePassword = databasePassword;
    m_sensorOwner = sensorOwner;

    std::ostringstream ss;
    ss << "Setting up mongo database connection parameters: "
        << m_databaseUser << "@" << m_databaseHost << ':' << m_databasePort;

    Logger::log(LOG_LEVEL_NOTICE, ss.str().c_str());
}

void MongoClient::initialiseDriver()
{
    if (m_numberOfInstances++ != 0)
        return; //no need to initialise

    if (m_driverHasBeenShutDown)
        return; //unable to initialise after shutdown

    try
    {
        mongo::Status status(mongo::client::initialize());
        if (status.isOK())
        {
            m_driverOk = true;
            Logger::log(LOG_LEVEL_DEBUG1, "mongoDb driver has been initialised");
        }
        else
        {
            Logger::log(LOG_LEVEL_ERROR, "mongoDb driver initialisation error: ", status.reason().c_str());
        }
    }
    catch (const mongo::DBException &e)
    {
        Logger::log(LOG_LEVEL_EXCEPTION,  "Initialisation of mongoDb driver failed", e.what());
    }
}

void MongoClient::shutdownDriver()
{
    if (--m_numberOfInstances == 0)
    {
        mongo::client::shutdown();
        m_driverHasBeenShutDown = true;
        m_driverOk = false;
    }
    //else do nothing
}


bool MongoClient::isConnected() const
{
    bool result = false;

    if (m_pConnection)
        result = m_pConnection->ok();

    return result;
}

bool MongoClient::connect()
{
    bool result = false;

    boost::lock_guard<boost::mutex> lock(m_setupMutex);

    try
    {
        if (m_databaseHost.empty())
        {
            Logger::log(LOG_LEVEL_ERROR, "Unable to connect to mongo database. Database Host is not defined");
            return false;
        }
        //else continue

        //Create database host:port string
        {
            std::ostringstream ss;
            ss << m_databaseHost << ':' << m_databasePort;

            m_pConnection = shared_ptr<mongo::ScopedDbConnection>(new mongo::ScopedDbConnection(ss.str()));
        }

        //Connect
        m_pConnection->conn();

        //Authenticate
        if (!m_databaseUser.empty() && !m_databasePassword.empty())
        {
            m_pConnection->get()->auth(BSON("user" << m_databaseUser <<
                            "pwd" << m_databasePassword <<
                            "db" << "brdf" <<
                            "mechanism" << "MONGODB-CR"));
        }
        else
        {
            Logger::log(LOG_LEVEL_DEBUG1, "Authentication skipped due to user name or password being empty");
        }

        if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG1))
        {
            std::ostringstream ss;
            ss << "Connected to mongo database at \"" << m_pConnection->getHost() << "\"";
            Logger::log(LOG_LEVEL_DEBUG1, ss.str().c_str());
        }
        //else do nothing

        result = true;
    }
    catch (const mongo::DBException &e)
    {
        Logger::log(LOG_LEVEL_ERROR,  "Connecting to mongo database failed", e.what());
    }

    return result;
}

void MongoClient::disconnect()
{
    if (m_pConnection)
    {
        Logger::log(LOG_LEVEL_DEBUG1, "Disconnected from mongo database");
        m_pConnection->done();
    }
    //else do nothing
}

void MongoClient::getConfigurationParameterStr(const char* name, std::string& value)
{
    //Setup query
    mongo::BSONObj query;
    {
        mongo::BSONObjBuilder builder;
        builder << "name"  << name;
        query = builder.obj();
    }

    // Set projection - return only _id field
    mongo::BSONObj projection = BSON("value" << 1 << "_id" << 0);

    //Query
    std::auto_ptr<mongo::DBClientCursor> cursor =
        m_pConnection->get()->query("brdf.configuration", query, 0, 0, &projection);

    //Only analyse the first value
    if (cursor->more())
    {
        mongo::BSONObj obj(cursor->next());
        value = obj.getField("value").toString(false);
    }
    //else do nothing

    //Remove trailing quotation marks
    size_t pos1 = value.find_first_of('\"');
    if (pos1 != std::string::npos)
    {
        pos1++;
        size_t pos2 = value.find_first_of('\"', pos1);
        if (pos2 != std::string::npos)
        {
            if (pos2 < pos1)
            {
                std::ostringstream ss;
                ss << "name =" << name << ", value=" << value << ", pos1=" << pos1 << ", pos2=" << pos2;
                Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());

            }
            //else do nothing

            assert(pos2 >= pos1);

            value = value.substr(pos1, pos2-pos1);
        }
        //else do nothing
    }
    //else do nothing
}

bool MongoClient::getConfigurationParameterUInt(const char* name, unsigned int& value)
{
    std::string strValue;
    getConfigurationParameterStr(name, strValue);

    if (strValue.empty())
        return false;

    bool result = Utils::stringToUInt(strValue, value);
    if (!result)
    {
        std::ostringstream ss;
        ss << "Unable to convert parameter value of " << name
            << " to unsigned int (" << strValue << ")";
        Logger::log(LOG_LEVEL_ERROR, ss.str().c_str());
    }
    //else do nothing

    return result;
}

bool MongoClient::getConfiguration(Model::BrdfMongoConfiguration& configuration)
{
    if (!m_pConnection)
        return false;

    boost::lock_guard<boost::mutex> lock(m_setupMutex);
    bool ok = true;

    getConfigurationParameterStr("host", configuration.host);
    //Use & instead of && to force reading other parameters after an error
    ok = ok & getConfigurationParameterUInt("port", configuration.port);
    getConfigurationParameterStr("path", configuration.path);
    ok = ok & getConfigurationParameterUInt("mongoCheckPeriodInSeconds", configuration.mongoCheckPeriodInSeconds);
    ok = ok & getConfigurationParameterUInt("connectTimeoutInSeconds", configuration.connectTimeoutInSeconds);
    ok = ok & getConfigurationParameterUInt("responseTimeoutInSeconds", configuration.responseTimeoutInSeconds);
    ok = ok & getConfigurationParameterUInt("backoffTimeInSeconds", configuration.backoffTimeInSeconds);
    ok = ok & getConfigurationParameterUInt("numberOfRetries", configuration.numberOfRetries);
    ok = ok & getConfigurationParameterUInt("numberOfDetectionsPerReportLimit", configuration.numberOfDetectionsPerReportLimit);

    if (!ok)
    {
        Logger::log(LOG_LEVEL_WARNING, "Some parameters could not be read. Using default ones");
    }
    //else do nothing

    m_numberOfDetectionsPerReportLimit = configuration.numberOfDetectionsPerReportLimit;

    return true;
}

bool MongoClient::getDetectorNames(std::string& detectors)
{
/*
//Retrieve all detectors to be reported
ownerDetectors = db.detectors.find(
	{owner: ownerName}, {name: 1, _id: 0}
	).toArray().map(function(doc) { return doc['name']});
if (ownerDetectors.length == 0)
	throw new Error("No sensors found for owner \"" + ownerName + "\"");
*/

    boost::lock_guard<boost::mutex> lock(m_setupMutex);

    if (m_sensorOwner.empty())
        return true;

    if (!m_pConnection)
        return false;

    // Get detectors belonging to sensor owner
    // Restrict query to only these detectors that belong to the specified owner
    mongo::BSONObj query;
    if (!m_sensorOwner.empty())
    {
        mongo::BSONObjBuilder builder;
        builder << "owner"  << m_sensorOwner;
        builder << "send_reports" << "1";
        query = builder.obj();
    }
    //else continuenumberOfDetectionsPerReportLimit

    // Set projection - return only _id field
    mongo::BSONObj projection = BSON("name" << 1 << "_id" << 0);

    //Query
    std::auto_ptr<mongo::DBClientCursor> cursor =
        m_pConnection->get()->query("brdf.detectors", query, 0, 0, &projection);

    detectors.reserve(0x4000);
    std::string sensor;
    std::string sensorName;

    while ( cursor->more() )
    {
        mongo::BSONObj obj(cursor->next());
        sensor = obj.toString();

        bool extractionOK = false;
        size_t pos1 = sensor.find_first_of('\"');
        if (pos1 != std::string::npos)
        {
            size_t pos2 = sensor.find_first_of('\"', pos1+1);
            if (pos2 != std::string::npos)
            {
                assert(pos2 > pos1);
                sensorName = sensor.substr(pos1, pos2-pos1+1);
                extractionOK = true;
            }
            //else do nothing
        }
        //else do nothing

        if (!extractionOK)
        {
            Logger::log(LOG_LEVEL_ERROR, "Unable to extract detector name");
            return false;
        }

        if (!detectors.empty())
            detectors += ',';
        detectors += sensorName;
    }

    std::ostringstream ss;
    ss << "Extracted owner \"" << m_sensorOwner << "\" detectors: "  << detectors.c_str();
    Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());

    return true;
}

/**
 * @brief Extract detector names and and specify query
 *
 * E.g having detectors = "A60PD-1","A60PD-2","A60PD-3"
 * query.jsonString() would produce:
 * { "outstationId" : { "$in" : [ "A60PD-1", "A60PD-2", "A60PD-3" ] } }
 *
 * @param detectors
 * @param query
 */
void extractDetectorNames(const std::string& detectors, mongo::BSONObj& query)
{
    if (!detectors.empty())
    {
        mongo::BSONObjBuilder objBuilder;
        mongo::BSONArrayBuilder arrayBuilder;

        size_t pos1 = 0;
        size_t pos2 = 0;

        //Extract names without quotation marks
        while ((pos1 != std::string::npos) && (pos2 != std::string::npos))
        {
            pos1 = detectors.find_first_of('\"', pos1);
            if (pos1 != std::string::npos)
            {
                ++pos1;
                pos2 = detectors.find_first_of('\"', pos1+1);
                if (pos2 != std::string::npos)
                {
                    assert(pos2 > pos1);
                    arrayBuilder.append(detectors.substr(pos1, pos2-pos1));
                }
                //else do nothing

                pos1 = pos2 + 1;
            }
            //else do nothing
        }

        objBuilder << "outstationId" << BSON("$in" << arrayBuilder.arr());
        query = objBuilder.obj();
    }
    //else do nothing

    //std::cout << detectors << std::endl;
    //std::cout << query.jsonString() << std::endl;
}

bool MongoClient::getDetectionIds(const std::string& detectors, std::string& detectionIds)
{
    if (detectors.empty())
        return true;

    if (!m_pConnection)
        return false;

    uint64_t numberOfDetectionsPerReportLimit = 0;
    {
        boost::lock_guard<boost::mutex> lock(m_setupMutex);
        numberOfDetectionsPerReportLimit = m_numberOfDetectionsPerReportLimit;
    }

    mongo::BSONObj query;
    extractDetectorNames(detectors, query);

    // Set projection - show only _id field
    mongo::BSONObj projection = BSON("_id" << 1);

    // Execute query
    std::auto_ptr<mongo::DBClientCursor> cursor =
        m_pConnection->get()->query("brdf.detections", query, numberOfDetectionsPerReportLimit, 0, &projection);

    detectionIds.reserve(0x4000);
    std::string detection;
    std::string detectionId;

    while ( cursor->more() )
    {
        mongo::BSONObj obj(cursor->next());
        detection = obj.toString();

        bool extractionOK = false;

        size_t pos1 = detection.find_first_of('\'');
        if (pos1 != std::string::npos)
        {
            size_t pos2 = detection.find_first_of('\'', pos1+1);
            if (pos1 != std::string::npos)
            {
                assert(pos2 > pos1);
                detectionId = detection.substr(pos1, pos2-pos1+1);
                extractionOK = true;
            }
            //else do nothing
        }
        //else do nothing

        extractionOK = true;
        if (!extractionOK)
        {
            Logger::log(LOG_LEVEL_ERROR, "Unable to extract detection id");
            return false;
        }

        if (!detectionIds.empty())
            detectionIds += ',';
        detectionIds += detectionId;
    }

    if (!detectionIds.empty())
    {
        Logger::log(LOG_LEVEL_DEBUG2, "Extracted detection ids", detectionIds.c_str());
    }
    else
    {
        Logger::log(LOG_LEVEL_DEBUG2, "No detections found");
    }

    return true;
}


/**
 * @brief Extract detector names and and specify query
 *
 * E.g having detectorIds = "A60PD-1","A60PD-2","A60PD-3"
 * query.jsonString() would produce:
 * { "outstationId" : { "$in" : [ "A60PD-1", "A60PD-2", "A60PD-3" ] } }
 *
 * @param detectorIds
 * @param query
 */
void extractDetectorIds(const std::string& detectorIds, mongo::BSONObj& query)
{
    if (!detectorIds.empty())
    {
        mongo::BSONObjBuilder objBuilder;
        mongo::BSONArrayBuilder arrayBuilder;

        size_t pos1 = 0;
        size_t pos2 = 0;
        std::string detectorId;

        //Extract names without quotation marks
        while ((pos1 != std::string::npos) && (pos2 != std::string::npos))
        {
            pos1 = detectorIds.find_first_of('\'', pos1);
            if (pos1 != std::string::npos)
            {
                ++pos1;
                pos2 = detectorIds.find_first_of('\'', pos1+1);
                if (pos2 != std::string::npos)
                {
                    assert(pos2 > pos1);
                    detectorId = detectorIds.substr(pos1, pos2-pos1);
                    arrayBuilder.append(mongo::OID(detectorId));
                }
                //else do nothing

                pos1 = pos2 + 1;
            }
            //else do nothing
        }

        objBuilder << "_id" << BSON("$in" << arrayBuilder.arr());
        query = objBuilder.obj();
    }
    //else do nothing

    std::cout << detectorIds << std::endl;
    std::cout << query.jsonString() << std::endl;
}

bool MongoClient::getDetectionByIds(const std::string& detectorIds, std::string& detections)
{
    if (detectorIds.empty())
        return true;

    if (!m_pConnection)
        return false;

    mongo::BSONObj query;
    extractDetectorIds(detectorIds, query);

    // Set projection - do not show _id field
    mongo::BSONObj projection = BSON("_id" << 0);

    // Execute query
    std::auto_ptr<mongo::DBClientCursor> cursor =
        m_pConnection->get()->query("brdf.detections", query, 0, 0, &projection);

    detections.reserve(0x4000);

    while ( cursor->more() )
    {
        if (!detections.empty())
            detections += ',';
        detections += cursor->next().toString();
        //detections += cursor->next().jsonString(mongo::Strict, 1);
    }

    if (!detections.empty())
    {
        Logger::log(LOG_LEVEL_DEBUG2, "Extracted detections", detections.c_str());
    }
    else
    {
        Logger::log(LOG_LEVEL_DEBUG2, "No detections found");
    }

    return true;
}

bool MongoClient::removeDetectionByIds(const std::string& detectorIds)
{
    if (detectorIds.empty())
        return true;

    if (!m_pConnection)
        return false;

    mongo::BSONObj query;
    extractDetectorIds(detectorIds, query);

    // Remove selected records
    m_pConnection->get()->remove("brdf.detections", query);

    if (Logger::isLogLevelAboveThreshold(LOG_LEVEL_DEBUG2))
    {
        std::ostringstream ss;
        ss << "Detections " << detectorIds << " have been removed";
        Logger::log(LOG_LEVEL_DEBUG2, ss.str().c_str());
    }
    //else do nothing

    return true;
}

} //namespace
