/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 */
package com.ssl.bluetruth.receiver.v2.datalogger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.ssl.bluetruth.receiver.v2.entities.BluetoothDevice;
import com.ssl.bluetruth.receiver.v2.entities.StatisticsReport;
import com.ssl.bluetruth.receiver.v2.entities.StatusReport;
import com.ssl.bluetruth.receiver.v2.misc.Utils;
import com.ssl.mongodb.jndi.MongoClientObjectFactory;

import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Mongo database manager class
 */
@Component
@Scope("singleton")
public class MongoDbManager {

    public static final String OWNERS = "owners";
    public static final String DETECTORS = "detectors";
    public static final String OUTSTATION_ID = "outstationId";
    public static final String TIME = "time";
    public static final String DURATION = "duration";
    public static final String STATUS = "status";
    public static final String DETECTIONS = "detections";
    public static final String ID = "id";
    public static final String START_TIME = "startTime";
    public static final String REF_TIME = "refTime";
    public static final String END_TIME = "endTime";
    public static final String MONGO_CLIENT_FACTORY_JNDI = "java:comp/env/mongoClientFactory";
    private MongoClient mongoClient;
    private String db;
    private static final Logger logger = LogManager.getLogger(MongoDbManager.class.getName());

    public MongoDbManager() {
        if (logger.isInfoEnabled()) {
            logger.info("MongoDbManager instance (" + this + ") created");
        }
    }

    public MongoDbManager(MongoClient mongoClient, String db) {
        if (logger.isInfoEnabled()) {
            logger.info("MongoDbManager instance (" + this + ") created");
        }
        this.mongoClient = mongoClient;
        this.db = db;
    }
        
    /**
     * Method to close down any database connections via the MongoClient
     * instance.
     */
    public void closeConnections() {
        logger.debug("About to close down Mongo connection data...");

        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }

        logger.debug("Mongo DB connections now closed and MongoClient instance "
                + "destroyed.");
    }

    /**
     * Gets the MongClient and its associated Mongo DB connection(s)
     *
     * @return MongoClient
     */
    public MongoClient getMongoClient() throws MongoException {

        if (mongoClient == null) {
            logger.debug("About to create new MongoClient instance...");

            mongoClient = connect();
            if (mongoClient != null) {
                db = mongoClient.getCredentialsList().get(0).getSource();

                if (logger.isInfoEnabled()) {
                    logger.debug("new MongoClient instance created: " + mongoClient);
                }
            } else {
                logger.error("Failed to create MongoClient");
            }
        }

        return mongoClient;
    }

    /**
     * The MongoClient provides the Mongo database connection. Once a connection
     * is opened, it may be re-used for all database operations.
     *
     * @return mongoClient instance
     */
    public MongoClient connect() {
        logger.debug("Creating mongoClient instance...");
        try {
            MongoClientObjectFactory mongoClientFactory = (MongoClientObjectFactory) new InitialContext().lookup(MONGO_CLIENT_FACTORY_JNDI);
            return mongoClientFactory.getMongoClient();
        } catch (NamingException ex) {
            logger.error("Failed to find \"" + MONGO_CLIENT_FACTORY_JNDI + "\" jndi ", ex);
        }
        return null;
    }

    public void insertStatsReport(StatisticsReport report) {
        if (mongoClient == null) {
            getMongoClient();
        }
        
        if (mongoClient != null) {
            MongoCollection collection = getMongoDetections();
            Document doc = getReportAsBsonDocument(report);
            insertDocument(doc, collection);
        }
    }

    public MongoCollection getMongoDetections() {
        mongoClient = getMongoClient();
        MongoCollection collection = mongoClient.getDatabase(db).getCollection(DETECTIONS);
        return collection;
    }

    private Document getReportAsBsonDocument(StatisticsReport report) {
        if (logger.isDebugEnabled()) {
            logger.debug("Converting Statistics report to JSON format with ID: "
                    + report.id);
        }

        Document doc = new Document(OUTSTATION_ID, report.id);
        doc.put(TIME, Utils.getIntTimeFromDate(report.reportStart));
        doc.put(DURATION, report.reportDuration);
        //Set status to default to OK as a detection is successfully received
        doc.put(STATUS, StatusReport.Statuses.OK.getStatus());

        //Get the devices from the data
        report.postParse();

        //add the devices to the report
        List<BluetoothDevice> devices = report.getDevices();
        BasicDBList dbl = new BasicDBList();

        for (BluetoothDevice device : devices) {
            BasicDBObject detectionObj = new BasicDBObject();
            detectionObj.put(ID, device.getId());
            detectionObj.put(START_TIME, (int)(device.getFirstSeen().getEpochSecond()));
            detectionObj.put(REF_TIME, device.getRefTime());

            if (device.getEndTime() != null) {
                detectionObj.put(END_TIME, device.getEndTime());

                //Note: Method deserialise() in class BluetoothDevice creates
                //an Integer object for each time value that is captured from the 
                //incoming servlet request. If the endTime (<tl>) is set to '0', 
                //this method sets the value to a null Integer object. However, the 
                //MongoDB expects to see a '0' value, denoting that the device has 
                //not left the scanned area, so it is reset here.    
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Note a null Date was retrieved denoting an "
                            + "'endTime' when the device has not left the "
                            + "scanned area. This will be set back to a value "
                            + "of '0'");
                }

                detectionObj.put(END_TIME, 0);
            }

            dbl.add(detectionObj);
        }

        doc.append("detections", dbl);

        return doc;
    }

    private void insertDocument(Document doc, MongoCollection collection) {
        if (logger.isDebugEnabled()) {
            logger.debug("About to insert BSON document into Mongo DB: " + doc);
        }

        try {
            collection.insertOne(doc);
            FindIterable find = collection.find(doc);
            MongoCursor cursor = find.iterator();

            if (cursor.hasNext()) {
                logger.info("New statistics report saved to Mongo DB for "
                        + "DBObject: " + cursor.next());
            }

        } catch (MongoWriteException | MongoWriteConcernException ex) {
            logger.error("Error saving result to the Mongo DB for DBObject: "
                    + doc.toString(), ex);
        } catch (MongoException me) {
            logger.error("Error saving result to the Mongo DB for DBObject: "
                    + doc.toString(), me);
        } catch (Exception ex) {
            logger.error("Error saving result to the Mongo DB for DBObject: "
                    + doc.toString(), ex);
        }
    }
}
