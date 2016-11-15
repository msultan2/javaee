/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
 */

#ifndef MONGO_CLIENT_H_
#define MONGO_CLIENT_H_


#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>
#include <stdint.h>
#include <string>


namespace mongo
{
    class DBClientConnection;
    class ScopedDbConnection;
}


namespace Model
{

class BrdfMongoConfiguration;


class MongoClient
{

public:

    //! Default constructor
    MongoClient();

    //! Destructor
    virtual ~MongoClient();

    void setup(
        const std::string& databaseHost,
        const uint16_t databasePort,
        const std::string& databaseUser,
        const std::string& databasePassword,
        const std::string& sensorOwner);

    ///Tell if is connected to the database
    bool isConnected() const;

    ///Synchronously connect to the database
    bool connect();

    ///Synchronously connect from the database
    void disconnect();

    /**
     * @brief Retrieve the contents of the configuration collection
     * @return
     */
    bool getConfiguration(BrdfMongoConfiguration& configuration);

    /**
     * @brief Get all the detectors belonging to detectors owner (m_sensorOwner)
     * @param detectors result, a collection of names separated by comma
     * @return true if everything is OK, false otherwise
     */
    bool getDetectorNames(std::string& detectors);

    /**
     * @brief Get all unreported detection ids for the specified sensors
     * @param detectors a collection of names separated by comma
     * @param detectionIds result, a collection of detection _id separated by comma
     * @return true if everything is OK, false otherwise
     */
    bool getDetectionIds(const std::string& detectors, std::string& detectionIds);

    /**
     * @brief Get all detection of a specified id
     * @param detectorIds a collection of ids separated by comma
     * @param detections result, a collection of detections separated by comma
     * @return true if everything is OK, false otherwise
     */
    bool getDetectionByIds(const std::string& detectorIds, std::string& detections);

    /**
     * @brief Remove all detection of a specified id
     * @param detectorIds a collection of ids separated by comma
     * @return true if everything is OK, false otherwise
     */
    bool removeDetectionByIds(const std::string& detectorIds);

    ///Check if the mongo driver is Ok
    static bool isDriverOk() { return m_driverOk; }

    void initialiseDriver();
    void shutdownDriver();

private:

    //! copy constructor. Not implemented
    MongoClient(const MongoClient& rhs);
    //! copy assignment operator. Not implemented
    MongoClient& operator=(const MongoClient& rhs);

    void getConfigurationParameterStr(const char* name, std::string& value);
    bool getConfigurationParameterUInt(const char* name, unsigned int& value);

    //Private members:
    boost::shared_ptr<mongo::ScopedDbConnection> m_pConnection;
    std::string m_databaseHost;
    uint16_t m_databasePort;
    std::string m_databaseUser;
    std::string m_databasePassword;
    std::string m_sensorOwner;
    boost::mutex m_setupMutex;

    uint64_t m_numberOfDetectionsPerReportLimit;

    static int m_numberOfInstances;
    static bool m_driverOk;
    static bool m_driverHasBeenShutDown;
};

}

#endif //MONGO_CLIENT_H_
