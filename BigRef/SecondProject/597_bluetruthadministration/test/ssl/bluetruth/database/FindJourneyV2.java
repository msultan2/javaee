/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.naming.NamingException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.util.PGInterval;
import ssl.vwim.utils.db.DBHasResult;
import ssl.vwim.utils.db.DBSetsParams;
import ssl.vwim.utils.db.Db;

import ssl.vwim.utils.db.DbCon;

/**
 *
 * @author liban
 */
public class FindJourneyV2 {

    private static class Device {

        public String addr, detector_id;
        public Date firstSeen, lastSeen;

        public static String insertD() {
            return "INSERT INTO statistics_device(addr, first_seen, last_seen, detector_id, report_id) VALUES (?,?,?,?,?);";
        }

        public static String insertR() {
            return "INSERT INTO statistics_report(detector_id, report_start, report_end, report_id) VALUES (?,?,?,?);";
        }

        public Device() {
        }

        public Device(Calendar c) {
            c.add(Calendar.MINUTE, 1);
            this.firstSeen = c.getTime();
            this.lastSeen = c.getTime();
        }

        public Device addr(String addr) {
            this.addr = addr;
            return this;
        }

        public Device firstSeen(Calendar fsc) {
            this.firstSeen = fsc.getTime();
            return this;
        }

        public Device lastSeen(Calendar lsc) {
            this.lastSeen = lsc.getTime();
            return this;
        }

        public Device detector_id(String detector_id) {
            this.detector_id = detector_id;
            return this;
        }

        public static String insertAll(ArrayList<Device> devs) {
            StringBuilder sb = new StringBuilder(insertR());
            for (int i = 0; i < devs.size(); i++) {
                sb.append(insertD());
            }
            return sb.toString();
        }
        private static int reportId = 1;

        public static DBSetsParams setAll(final ArrayList<Device> devs) {
            return new DBSetsParams() {
                @Override
                public void set(PreparedStatement ps) throws SQLException {
                    ps.setString(1, devs.get(0).detector_id);
                    ps.setTimestamp(2, new Timestamp(devs.get(0).lastSeen.getTime()));
                    ps.setTimestamp(3, new Timestamp(devs.get(devs.size() - 1).lastSeen.getTime()));
                    ps.setInt(4, ++reportId);
                    int report_set_n = 4;
                    int device_set_n = 5;
                    for (int i = 0; i < devs.size(); i++) {
                        ps.setString(report_set_n + device_set_n * i + 1, devs.get(i).addr);
                        ps.setTimestamp(report_set_n + device_set_n * i + 2, new Timestamp(devs.get(i).firstSeen.getTime()));
                        ps.setTimestamp(report_set_n + device_set_n * i + 3, new Timestamp(devs.get(i).lastSeen.getTime()));
                        ps.setString(report_set_n + device_set_n * i + 4, devs.get(i).detector_id);
                        ps.setInt(report_set_n + device_set_n * i + 5, reportId);
                    }
                }
            };
        }
    }

    public FindJourneyV2() {
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
    }

    @After
    public void tearDown() {
        clearDatabase();
        setUpDetectors();
        setUpSpans();
    }

    private void insertDetectionsFromDetectorAToDetectorEAndBack() {
        Calendar c = Calendar.getInstance();
        c.set(2012, 5, 2, 13, 0, 0);

        ArrayList<Device> devs = new ArrayList<>();
        devs.add(new Device(c).addr("Q").detector_id("A"));
        devs.add(new Device(c).addr("Q").detector_id("B"));
        devs.add(new Device(c).addr("Q").detector_id("C"));
        devs.add(new Device(c).addr("Q").detector_id("D"));
        devs.add(new Device(c).addr("Q").detector_id("E"));
        devs.add(new Device(c).addr("Q").detector_id("D"));
        devs.add(new Device(c).addr("Q").detector_id("C"));
        devs.add(new Device(c).addr("Q").detector_id("B"));
        devs.add(new Device(c).addr("Q").detector_id("A"));

        new Db().sql(Device.insertAll(devs)).set(Device.setAll(devs)).go().close();
    }

    @Test
    public void deviceIsDetectedByDetectorAToDetectorEAndBack() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        insertDetectionsFromDetectorAToDetectorEAndBack();
        Connection connection = dm.getDatasource().getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT span_name, duration, completed_timestamp FROM span_journey_detection ORDER BY completed_timestamp");
        int resultCount = 0;
        while (rs.next()) {
            PGInterval pgi = (PGInterval) rs.getObject("duration");
            assertEquals("Span name: " + rs.getString("span_name"), "0 years 0 mons 0 days 0 hours 1 mins 0.00 secs", pgi.getValue());
            resultCount++;
        }
        assertEquals(8, resultCount);
    }

    private void insertDetectionsFromDetectorACBD() {
        Calendar c = Calendar.getInstance();
        c.set(2012, 5, 2, 13, 0, 0);

        ArrayList<Device> devs = new ArrayList<>();
        devs.add(new Device(c).addr("Q").firstSeen(c).lastSeen(c).detector_id("A"));
        devs.add(new Device(c).addr("Q").firstSeen(c).lastSeen(c).detector_id("C"));
        devs.add(new Device(c).addr("Q").firstSeen(c).lastSeen(c).detector_id("B"));
        devs.add(new Device(c).addr("Q").firstSeen(c).lastSeen(c).detector_id("D"));

        new Db().sql(Device.insertAll(devs)).set(Device.setAll(devs)).go().close();
    }

    private void insertDetectionsFromDetectorABRepeatedly() {
        Calendar c = Calendar.getInstance();
        c.set(2012, 5, 2, 13, 0, 0);

        ArrayList<Device> devs = new ArrayList<>();
// same timestamps
        devs.add(new Device(c).addr("Q").detector_id("A"));
        devs.add(new Device(c).addr("Q").detector_id("B"));
        devs.add(new Device(c).addr("Q").detector_id("A"));
        devs.add(new Device(c).addr("Q").detector_id("B"));
        devs.add(new Device(c).addr("Q").detector_id("A"));
        devs.add(new Device(c).addr("Q").detector_id("B"));
        devs.add(new Device(c).addr("Q").detector_id("A"));
        devs.add(new Device(c).addr("Q").detector_id("B"));

        new Db().sql(Device.insertAll(devs)).set(Device.setAll(devs)).go().close();
    }

    @Test
    public void deviceIsDetectedByDetectorACBD() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        insertDetectionsFromDetectorACBD();
        Connection connection = dm.getDatasource().getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT span_journey_detection.duration AS duration "
                + "FROM span_journey_detection ORDER BY span_journey_detection.duration;");
        rs.next();
        PGInterval pgi = (PGInterval) rs.getObject("duration");
        assertEquals("0 years 0 mons 0 days 0 hours 1 mins 0.00 secs", pgi.getValue());
        rs.next();
        pgi = (PGInterval) rs.getObject("duration");
        assertEquals("0 years 0 mons 0 days 0 hours 2 mins 0.00 secs", pgi.getValue());
        rs.next();
        pgi = (PGInterval) rs.getObject("duration");
        assertEquals("0 years 0 mons 0 days 0 hours 2 mins 0.00 secs", pgi.getValue());
    }

   // @Test
    public void deviceIsDetectedByDetectorABRepeatedlyWithSameTimestamps() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        insertDetectionsFromDetectorABRepeatedly();
        Connection connection = dm.getDatasource().getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT span_journey_detection.duration AS duration FROM span_journey_detection ORDER BY span_journey_detection.duration;");
        int resultCount = 0;
        while (rs.next()) {
            resultCount++;
        }
        assertEquals(4, resultCount);
    }

    @Test
    public void deviceIsDetectedByDetectorAB() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        insertDetectionsFromDetectorAB(dm);
        Connection connection = dm.getDatasource().getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT span_journey_detection.duration AS duration FROM span_journey_detection ORDER BY span_journey_detection.duration;");
        int resultCount = 0;
        while (rs.next()) {
            resultCount++;
            PGInterval pgi = (PGInterval) rs.getObject(1);
            assertEquals(10, pgi.getMinutes());
        }
        assertEquals(1, resultCount);
    }

    private void insertDetectionsFromDetectorAB(DatabaseManager dm) throws NamingException, SQLException {
        Calendar c = Calendar.getInstance();
        c.set(2012, 5, 2, 13, 0, 0);

        ArrayList<Device> devs = new ArrayList<>();

        Device d1 = new Device().addr("Q").detector_id("A").firstSeen(c);
        c.add(Calendar.MINUTE, 5);
        d1.lastSeen(c);
        devs.add(d1);
        c.add(Calendar.MINUTE, 10); // journey time

        Device d2 = new Device().addr("Q").detector_id("B");
        d2.firstSeen(c);
        c.add(Calendar.MINUTE, 2);
        d2.lastSeen(c);
        devs.add(d2);

        new Db().sql(Device.insertAll(devs)).set(Device.setAll(devs)).go().close();
    }

    private void clearDatabase() {
        new Db().sql(
                "DELETE FROM span_journey_detection;"
                + "DELETE FROM occupancy;"
                + "DELETE FROM span_journey_average_duration;"
                + "DELETE FROM span;"
                + "DELETE FROM statistics_device;"
                + "DELETE FROM statistics_report;"
                + "DELETE FROM detector;").go().close();
    }

    private void setUpDetectors() {
        new Db().sql(
                "INSERT INTO detector (detector_id, detector_name) VALUES ('A', 'Detector A');"
                + "INSERT INTO detector (detector_id, detector_name) VALUES ('B', 'Detector B');"
                + "INSERT INTO detector (detector_id, detector_name) VALUES ('C', 'Detector C');"
                + "INSERT INTO detector (detector_id, detector_name) VALUES ('D', 'Detector D');"
                + "INSERT INTO detector (detector_id, detector_name) VALUES ('E', 'Detector E');").go().close();
    }

    private void setUpSpans() {
        new Db().sql(
                "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('A to B', 'A', 'B');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('B to C', 'B', 'C');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('C to D', 'C', 'D');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('D to E', 'D', 'E');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('E to D', 'E', 'D');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('D to C', 'D', 'C');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('C to B', 'C', 'B');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('B to A', 'B', 'A');").go().close();
    }
}