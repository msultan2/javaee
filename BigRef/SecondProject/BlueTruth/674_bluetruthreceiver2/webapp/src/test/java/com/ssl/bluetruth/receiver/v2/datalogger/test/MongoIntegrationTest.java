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
package com.ssl.bluetruth.receiver.v2.datalogger.test;

import static com.ssl.bluetruth.receiver.v2.datalogger.MongoDbManager.DETECTIONS;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.ssl.bluetruth.receiver.v2.datalogger.MongoDbManager;
import com.ssl.bluetruth.receiver.v2.entities.BluetoothDevice;
import com.ssl.bluetruth.receiver.v2.entities.StatisticsReport;
import com.ssl.bluetruth.receiver.v2.misc.RequestParserFactory;
import com.ssl.bluetruth.receiver.v2.test.SpringTestConfig;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import java.time.Instant;

/**
 * Integration tests for the MongoDbManagerClass. Note these tests require a
 * Mongo database service to run.
 */
//Note currently disabled for test build environment
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringTestConfig.class})
public class MongoIntegrationTest {

    private static final Logger logger = LogManager.getLogger(MongoIntegrationTest.class.getName());

    @Autowired
    private RequestParserFactory requestParserFactory;
    private MongoDbManager mongoDbManager;
    private MongodForTestsFactory factory;
    private MongoClient mongoClient;
    
    private static final String DB = "brdf";

    @Before
    public void setUp() throws Exception {
        logger.debug("In setUp()...");
        
        factory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
        mongoClient = factory.newMongo();
        
        mongoDbManager = new MongoDbManager(mongoClient, DB);
        
        logger.debug("setUp() completed");
    }
    
    @After
    public void tearDown() {
        logger.debug("In tearDown()...");

        if (factory != null) {
            factory.shutdown();
        }

        logger.debug("tearDown() completed");
    }

    @Test
    public void testInsertStatisticsReport() throws UnknownHostException {
        logger.debug("testInsertStatisticsReport()...");

        StatisticsReport report = getStatsReport();
        mongoDbManager.insertStatsReport(report);

        logger.debug("Checking for new Detection...");

        //Check there was an update in the DB
        MongoCollection mc = mongoClient.getDatabase(DB).getCollection(DETECTIONS);
        BasicDBObject query = new BasicDBObject("outstationId", "A");
        FindIterable find = mc.find(query);
        Document doc = (Document) find.first();

        //validate the statistics report
        validateReport(report, doc);

        //validate the detections
        logger.debug("Validating detections...");
        List<Document> devicesAsDocs = (ArrayList) doc.get("detections");
        logger.debug("devicesAsDocs = " + devicesAsDocs);

        assertTrue(devicesAsDocs != null);

        for (Document deviceAsDoc : devicesAsDocs) {
            assertTrue(devicesAsDocs.size() == 2);

            if ((((String) deviceAsDoc.get("id")).equalsIgnoreCase("abcdef012345"))) {
                for (BluetoothDevice device : report.getDevices()) {
                    if (device.getId().equalsIgnoreCase("abcdef012345")) {
                        validateDevice(device, deviceAsDoc);
                    }
                }
            } else if ((((String) deviceAsDoc.get("id")).equalsIgnoreCase("abcdef543210"))) {
                for (BluetoothDevice device : report.getDevices()) {
                    if (device.getId().equalsIgnoreCase("abcdef543210")) {
                        validateDevice(device, deviceAsDoc);
                    }
                }
            }
        }

        logger.debug("New Detection was added to the MongoDB: " + doc);

    }

    @Test
    public void testInsertZeroDatesStatisticsReport() {
        logger.debug("testInsertZeroDatesStatisticsReport()...");

        StatisticsReport report = getStatsReportZeroDates();
        mongoDbManager.insertStatsReport(report);

        logger.debug("Checking for new Detection...");

        //Check there was an update in the DB
        MongoCollection mc = mongoClient.getDatabase(DB).getCollection(DETECTIONS);
        BasicDBObject query = new BasicDBObject("outstationId", "B");
        FindIterable find = mc.find(query);
        Document doc = (Document) find.first();

        //validate the statistics report
        validateReport(report, doc);

        //validate the detections
        logger.debug("Validating detections...");
        List<Document> deviceDocs = (ArrayList) doc.get("detections");
        logger.debug("devicesAsDocs = " + deviceDocs);

        assertTrue(deviceDocs != null);

        for (Document deviceAsDoc : deviceDocs) {
            assertTrue(deviceDocs.size() == 1);

            if ((((String) deviceAsDoc.get("id")).equalsIgnoreCase("f49f544b0587"))) {
                for (BluetoothDevice device : report.getDevices()) {
                    if (device.getId().equalsIgnoreCase("f49f544b0587")) {
                        validateDevice(device, deviceAsDoc);
                    }
                }
            }
        }
    }

    private StatisticsReport getStatsReportZeroDates() {
        String data = "B,0,0,f49f544b0587:5a020c:0:0:0,0";

        StatisticsReport sr = requestParserFactory.getRequestParser(StatisticsReport.class).data(data).parse();

        return sr;
    }

    private StatisticsReport getStatsReport() {
        String reportDurationHex = Integer.toHexString(20);
        BluetoothDevice btd1 = getBluetoothDevice1();
        BluetoothDevice btd2 = getBluetoothDevice2();

        StatisticsReport sr = requestParserFactory.getRequestParser(StatisticsReport.class)
                .data(String.format("A,ffff,%s,%s,%s,1234",
                                reportDurationHex,
                                btd1.toString(),
                                btd2.toString()))
                .parse();

        return sr;
    }

    private BluetoothDevice getBluetoothDevice1() {
        BluetoothDevice btd = new BluetoothDevice();
        btd.setId("abcdef012345");
        btd.setCod(Integer.valueOf("527b7f29", 16));
        btd.setFirstSeen(Instant.ofEpochMilli(1383825193000L)); // 527b7f29 (seconds)
        btd.setReferencePoint(Instant.ofEpochMilli(1383825193000L).plusSeconds(5)); // 5 (seconds)
        btd.setLastSeen(Instant.ofEpochMilli(1383825193000L).plusSeconds(10)); // a (seconds)
        return btd;
    }

    private BluetoothDevice getBluetoothDevice2() {
        BluetoothDevice btd = new BluetoothDevice();
        btd.setId("abcdef543210");
        btd.setCod(Integer.valueOf("524db869", 16));
        btd.setFirstSeen(Instant.ofEpochMilli(1380825193000L)); // 524db869 (seconds)
        btd.setReferencePoint(Instant.ofEpochMilli(1380825193000L).plusSeconds(5)); // 5 (seconds)
        btd.setLastSeen(Instant.ofEpochMilli(1380825193000L).plusSeconds(10)); // 10 (seconds)

        return btd;
    }

    //validate the statistics report
    private void validateReport(StatisticsReport report, Document doc) {
        logger.debug("Validating statistics report parameters... ");

        assertTrue((((String) doc.get("outstationId")).equalsIgnoreCase(report.id)));
        assertTrue((Integer) doc.get("time") == report.startTime);
        assertTrue((((String) doc.get("status")).equalsIgnoreCase("OK")));
        assertTrue((Integer) doc.get("duration") == report.reportDuration);

        logger.debug("Statistics report " + report.id + " parameters are valid.");

    }

    private void validateDevice(BluetoothDevice device, Document deviceAsDoc) {
        logger.debug("Validating device " + device.getId() + "...");

        int retrievedStartTime = (Integer) deviceAsDoc.get("startTime");
        int expectedStartTime = (int)(device.getFirstSeen().getEpochSecond());
        assertTrue(retrievedStartTime == expectedStartTime);

        int retrievedRefTime = (Integer) deviceAsDoc.get("refTime");
        int expectedRefTime = device.getRefTime();
        assertTrue(retrievedRefTime == expectedRefTime);

        int retrievedEndTime = (Integer) deviceAsDoc.get("endTime");

        if (device.getLastSeen() != null) {
            int expectedEndTime = device.getEndTime();
            assertTrue(retrievedEndTime == expectedEndTime);

        } else {
            assertTrue(retrievedEndTime == 0);
        }

        logger.debug("Device " + device.getId() + " is valid.");
    }
}
