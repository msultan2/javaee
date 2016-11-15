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
package com.ssl.bluetruth.receiver.v2.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.naming.NamingException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;

import com.ssl.bluetruth.db.DBHasResult;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.entities.BluetoothDevice;
import com.ssl.bluetruth.receiver.v2.entities.StatisticsReport;
import com.ssl.bluetruth.receiver.v2.misc.RequestParserFactory;
import com.ssl.bluetruth.receiver.v2.test.mocks.MockStatisticsReport;
import java.time.Instant;

/**
 *
 * @author liban
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringTestConfig.class})
public class StatisticsReportTest {

    @Autowired
    private RequestParserFactory requestParserFactory;
    
    private BluetoothDevice btd, btd2, btd3;
    @Autowired
    private ConfigurationManager configManager;

    public StatisticsReportTest() {
    }

    @BeforeClass
    public static void setUpClass() throws NamingException {
        DbCon.init();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        btd = new BluetoothDevice();
        btd.setId("abcdef0123456");
        btd.setCod(Integer.valueOf("527b7f29", 16));
        btd.setFirstSeen(Instant.ofEpochMilli(1383825193000L)); // 527b7f29 (seconds)
        btd.setReferencePoint(Instant.ofEpochMilli(1383825193000L + 5000)); // 5 (seconds)
        btd.setLastSeen(Instant.ofEpochMilli(1383825193000L + 10000)); // a (seconds)

        btd2 = new BluetoothDevice();
        btd2.setId("abcdef5432102");
        btd2.setCod(Integer.valueOf("524db869", 16));
        btd2.setFirstSeen(Instant.ofEpochMilli(1380825193000L)); // 524db869 (seconds)
        btd2.setReferencePoint(Instant.ofEpochMilli(1380825193000L + 5000)); // 5 (seconds)
        btd2.setLastSeen(Instant.ofEpochMilli(1380825193000L + 10000)); // 10 (seconds)

        //Using: OneHourTest133602:0:5576CFA2:0:FFFFFFFF
        //device:class of device: first observation time: estimated reference observation time: last observation time
        btd3 = new BluetoothDevice();
        btd3.setId("OneHourTest1336023");
        btd3.setCod(Integer.valueOf("0", 16));
        btd3.setFirstSeen(Instant.ofEpochSecond(Long.valueOf("5576CFA2", 16)));
        btd3.setReferencePoint(btd3.getFirstSeen().plusSeconds(Long.valueOf("0", 16)));
        long lastDeltams = Long.valueOf("FFFFFFFF", 16);
        btd3.setLastSeen(btd3.getFirstSeen().plusSeconds(lastDeltams));

        clearDatabase();
        setUpDetectors();
    }

    @After
    public void tearDown() {
        clearDatabase();
    }

    private void clearDatabase() {

        new Db().sql(
                "DELETE FROM statistics_device;"
                + "DELETE FROM statistics_report;"
                + "DELETE FROM device_detection;"
                + "DELETE FROM detector_configuration;"
                + "DELETE FROM detector;").go().close();
    }

    private void setUpDetectors() {
        new Db().sql("INSERT INTO detector (detector_id, detector_name) VALUES ('A', 'Detector A');").go().close();
    }
        
    @Test
    public void deviceDeserialiser() {
        assertEquals(btd.getId() + ":" + Integer.toHexString(btd.getCod()) + ":527b7f29:5:a", btd.toString());
        assertEquals(BluetoothDevice.deserialise(btd.toString()).toString(), btd.toString());
    }

    @Test
    public void batchDeserialiser() {
        StatisticsReport sr = requestParserFactory.getRequestParser(StatisticsReport.class)
                .data("A,ffff,20," + btd.toString() + "," + btd2.toString() + ",1234")
                .parse();
        assertEquals(btd.toString(), sr.getDevices().get(0).toString());
        assertEquals(btd2.toString(), sr.getDevices().get(1).toString());
    }

    @Test
    public void insertDevices() {
        StatisticsReport sr = requestParserFactory.getRequestParser(StatisticsReport.class)
                .data("A,ffff,20," + btd.toString() + "," + btd2.toString() + ",1234")
                .parse();
        int report_id = sr.insertReport();
        sr.insertDevices(report_id); 

        Boolean status = (Boolean) (new Db().sql("SELECT id,addr,cod,"
                + "first_seen,reference_point,last_seen FROM statistics_device")
                .go(new DBHasResult<Boolean>() {
                    @Override
                    public Boolean done(ResultSet rs) throws SQLException {
                        // Assuming the same order
                        rs.next();
                        assertEquals(btd.getId(), rs.getString("addr"));
                        assertEquals(btd.getCod(), rs.getInt("cod"));
                        assertEquals(btd.getFirstSeen(), rs.getTimestamp("first_seen").toInstant());
                        assertEquals(btd.getReferencePoint(), rs.getTimestamp("reference_point").toInstant());
                        assertEquals(btd.getLastSeen(), rs.getTimestamp("last_seen").toInstant());

                        rs.next();
                        assertEquals(btd2.getId(), rs.getString("addr"));
                        assertEquals(btd2.getCod(), rs.getInt("cod"));
                        assertEquals(btd2.getFirstSeen(), rs.getTimestamp("first_seen").toInstant());
                        assertEquals(btd2.getReferencePoint(), rs.getTimestamp("reference_point").toInstant());
                        assertEquals(btd2.getLastSeen(), rs.getTimestamp("last_seen").toInstant());

                        boolean isException = false;

                        //This device should not have been saved as is faulty
                        try {
                            rs.next();
                            assertEquals(btd3.getId(), rs.getString("addr"));

                        } catch (SQLException se) {
                            isException = true;
                        }

                        //As the device contains a faulty timestamp this should not have been saved
                        assertTrue(isException);

                        return true;
                    }
                }).close().response());

        System.out.println(status);
        assertTrue("No exceptions", status);
    }
//FOREIGN KEY (detector_id) REFERENCES detector(detector_id) ON UPDATE CASCADE ON DELETE CASCADE

    @Test
    public void insertReport() {
        final String reportDurationHex = Integer.toHexString(20);
        StatisticsReport sr = requestParserFactory.getRequestParser(StatisticsReport.class)
                .data(String.format("A,ffff,%s,%s,%s,1234",
                                reportDurationHex,
                                btd.toString(),
                                btd2.toString()))
                .parse();
        final int report_id = sr.insertReport();
        new Db().sql("SELECT report_id,report_start,report_end,detector_id FROM statistics_report").go(new DBHasResult() {
            @Override
            public Object done(ResultSet rs) throws SQLException {
                rs.next();
                assertEquals(report_id, rs.getInt("report_id"));

                assertEquals(new Timestamp(Integer.valueOf("ffff", 16) * 1000), rs.getTimestamp("report_start"));
                assertEquals(new Timestamp(
                        (Integer.valueOf("ffff", 16) + Integer.valueOf(reportDurationHex, 16)) * 1000),
                        rs.getTimestamp("report_end"));
                return null;
            }
        }).close();
    }

    //Checks whether the lastSeen device value is rejected if > 100 years in the future
    //Note: in scenario where lastSeen is approaching 'firstSeen + FFFFFFFF' this is considered a fault and should be filtered out
    @Test
    public void faultyReport() {
        String data = "Detector10,5576DD68,F,"
                + "OneHourTest133608:0:5576CFA8:0:0," //firstSeen: Tue Jun 09 12:36:08 BST 2015,lastSeen: null
                + "OneHourTest133602:0:5576CFA2:0:FFFFFFFF," //firstSeen: Tue Jun 09 12:36:02 BST 2015, lastSeen: Fri Jul 16 19:04:17 BST 2151
                + "OneHourTest133614:0:5576CFAE:0:FFFFFFFF,0"; //firstSeen: Tue Jun 09 12:36:14 BST 2015, lastSeen: Fri Jul 16 19:04:29 BST 2151
        StatisticsReport sr = requestParserFactory.getRequestParser(StatisticsReport.class)
                .data(data)
                .parse();
        assertEquals(1, sr.getDevices().size());
    }

    //Checks whether the device firstSeen value is rejected if > timestampToleranceInMs for the associated detector in the database
    @Test
    public void deviceTimestampFirstSeenInFuture() {

        BluetoothDevice btdOkay = new BluetoothDevice();
        btdOkay.setId("okay");
        btdOkay.setCod(0);
        btdOkay.setFirstSeen(Instant.ofEpochMilli(1383825193000L)); // 527b7f29 (seconds)
        btdOkay.setReferencePoint(Instant.ofEpochMilli(1383825193000L + 5000)); // 5 (seconds)
        btdOkay.setLastSeen(Instant.ofEpochMilli(1383825193000L + 10000)); // a (seconds)

        BluetoothDevice btdWithTimestampLimit = new BluetoothDevice();
        btdWithTimestampLimit.setId("timestampLimit");
        btdWithTimestampLimit.setCod(0);
        btdWithTimestampLimit.setFirstSeen(Instant.now().plusSeconds(4)); // 4s in future
        btdWithTimestampLimit.setReferencePoint(btdWithTimestampLimit.getFirstSeen().plusSeconds(5)); // 5 (seconds)
        btdWithTimestampLimit.setLastSeen(btdWithTimestampLimit.getFirstSeen().plusSeconds(10)); // a (seconds)

        BluetoothDevice btdOverTimestampLimit = new BluetoothDevice();
        btdOverTimestampLimit.setId("overTimestampLimit");
        btdOverTimestampLimit.setCod(0);
        btdOverTimestampLimit.setFirstSeen(Instant.now().plusSeconds(10)); //10s in future
        btdOverTimestampLimit.setReferencePoint(btdOverTimestampLimit.getFirstSeen().plusSeconds(5)); // 5 (seconds)
        btdOverTimestampLimit.setLastSeen(btdOverTimestampLimit.getFirstSeen().plusSeconds(10)); // a (seconds)

        StatisticsReport sr = requestParserFactory.getRequestParser(MockStatisticsReport.class)
        		.data("A,ffff,20," + btdOkay.toString() + "," + btdWithTimestampLimit.toString() +  "," + btdOverTimestampLimit + ",1234")
        		.parse();

        System.out.println(sr.getDevices().size());
        assertEquals(2, sr.getDevices().size());
    }

    @Test
    public void realWorldParse() {
        String data = "A,528df39f,0,22fc4ceb87:5a020c:528df227:0:0,"
                + "2669968a1a:5a0204:528df229:0:0,bd3ae230d0:5a0204:528df228:0:0,"
                + "c1420467eab:5a0204:528df22a:0:0,402ba12b6c94:5a0204:528df22a:0:0,"
                + "b0799405a81c:5a0204:528df229:0:0,f49f544b0587:5a020c:528df228:0:0,0";
        StatisticsReport sr = requestParserFactory.getRequestParser(MockStatisticsReport.class)
                .data(data)
                .parse();
        assertEquals(7, sr.getDevices().size());
    }

    @Test
    public void realWorldParseVoid() {
        String data = "A,528df39f,5A,0";
        StatisticsReport sr = new StatisticsReport();
        sr.data = data;
        sr.id = "A";
        sr.postParse();
        assertEquals(0, sr.getDevices().size());
    }
}