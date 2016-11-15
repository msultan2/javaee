package ssl.bluetruth.database;


import java.sql.Connection;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.NamingException;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.postgresql.util.PGInterval;

/**
 *
 * @author nthompson
 */
@Ignore("Freezes")
public class InsertDetectionTriggerTest {

    public InsertDetectionTriggerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        InitialContext ic = new InitialContext();
        ic.createSubcontext("java:");
        ic.createSubcontext("java:/comp");
        ic.createSubcontext("java:/comp/env");
        ic.createSubcontext("java:/comp/env/jdbc");

        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("org.postgresql.Driver");
        bds.setUrl("jdbc:postgresql://localhost:5432/bluetruth");
        bds.setUsername("bluetruth");
        bds.setPassword("ssl1324");

        ic.bind("java:/comp/env/jdbc/bluetruth", bds);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        clearDatabase(dm);
        setUpDetectors(dm);
        setUpspans(dm);
    }

    @After
    public void tearDown() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        clearDatabase(dm);
        setUpDetectors(dm);
        setUpspans(dm);
    }

    @Test
    public void deviceIsDetectedByDetectorAToDetectorEAndBack() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        insertDetectionsFromDetectorAToDetectorEAndBack(dm);
        Connection connection = dm.getDatasource().getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT journey.start_detector_id, journey.end_detector_id, journey.duration AS duration FROM journey ORDER BY journey.duration;");
        int resultCount = 0;
        while (rs.next()) {
            PGInterval pgi = (PGInterval) rs.getObject("duration");
            assertEquals("Start: " + rs.getString("start_detector_id") + " End:" + rs.getString("end_detector_id"), "0 years 0 mons 0 days 0 hours 1 mins 0.00 secs", pgi.getValue());
            resultCount++;
        }
        assertEquals(8, resultCount);
    }

    @Test
    public void deviceIsDetectedByDetectorACBD() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        insertDetectionsFromDetectorACBD(dm);
        Connection connection = dm.getDatasource().getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT journey.duration AS duration FROM journey ORDER BY journey.duration;");
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

    @Test
    public void deviceIsDetectedByDetectorABRepeatedlyWithSameTimestamps() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        insertDetectionsFromDetectorABRepeatedly(dm);
        Connection connection = dm.getDatasource().getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT journey.duration AS duration FROM journey ORDER BY journey.duration;");
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
        ResultSet rs = s.executeQuery("SELECT journey.duration AS duration FROM journey ORDER BY journey.duration;");
        int resultCount = 0;
        while (rs.next()) {
            resultCount++;
        }
        assertEquals(1, resultCount);
    }

    @Test
    public void deviceIsDetectedFromDetectorAAABBBAAABBBAAABBB() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        insertDetectionsFromDetectorAAABBBAAABBBAAABBB(dm);
        Connection connection = dm.getDatasource().getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT journey.duration AS duration FROM journey ORDER BY journey.duration;");
        int resultCount = 0;
        while (rs.next()) {
            resultCount++;
        }
        assertEquals(15, resultCount);
    }

    @Test
    public void deviceIsDetectedFromDetectorAAABBBAAABBBAAABBBWithNoise() throws NamingException, SQLException, DatabaseManagerException {
        DatabaseManager dm = DatabaseManager.getInstance();
        insertDetectionsFromDetectorC(dm);
        insertDetectionsFromDetectorAAABBBAAABBBAAABBB(dm);
        Connection connection = dm.getDatasource().getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT journey.duration AS duration FROM journey ORDER BY journey.duration;");
        int resultCount = 0;
        while (rs.next()) {
            resultCount++;
        }
        assertEquals(15, resultCount);
    }

    private void clearDatabase(DatabaseManager dm) throws NamingException, SQLException {
        dm.getDatasource().getConnection().prepareStatement(
                "DELETE FROM journey;"
                + "DELETE FROM occupancy;"
                + "DELETE FROM span;"
                + "DELETE FROM device_detection;"
                + "DELETE FROM detector;").execute();
    }

    private void setUpDetectors(DatabaseManager dm) throws NamingException, SQLException {
        dm.getDatasource().getConnection().prepareStatement(
                "INSERT INTO detector (detector_id, detector_name) VALUES ('A', 'Detector A');"
                + "INSERT INTO detector (detector_id, detector_name) VALUES ('B', 'Detector B');"
                + "INSERT INTO detector (detector_id, detector_name) VALUES ('C', 'Detector C');"
                + "INSERT INTO detector (detector_id, detector_name) VALUES ('D', 'Detector D');"
                + "INSERT INTO detector (detector_id, detector_name) VALUES ('E', 'Detector E');").execute();
    }

    private void setUpspans(DatabaseManager dm) throws NamingException, SQLException {
        dm.getDatasource().getConnection().prepareStatement(
                "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('A to B', 'A', 'B');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('B to C', 'B', 'C');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('C to D', 'C', 'D');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('D to E', 'D', 'E');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('E to D', 'E', 'D');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('D to C', 'D', 'C');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('C to B', 'C', 'B');"
                + "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES ('B to A', 'B', 'A');").execute();
    }

    private void insertDetectionsFromDetectorAToDetectorEAndBack(DatabaseManager dm) throws NamingException, SQLException {
        dm.getDatasource().getConnection().prepareStatement(
                "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:00:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:01:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:02:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:03:00', 'D');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:04:00', 'E');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:05:00', 'D');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:06:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:07:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:08:00', 'A');").execute();
    }

    private void insertDetectionsFromDetectorACBD(DatabaseManager dm) throws NamingException, SQLException {
        dm.getDatasource().getConnection().prepareStatement(
                "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:00:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:01:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:02:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:03:00', 'D');").execute();
    }

    private void insertDetectionsFromDetectorABRepeatedly(DatabaseManager dm) throws NamingException, SQLException {
        dm.getDatasource().getConnection().prepareStatement(
                "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:00:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:01:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:00:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:01:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:00:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:01:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:00:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:01:00', 'B');").execute();
    }

    private void insertDetectionsFromDetectorAB(DatabaseManager dm) throws NamingException, SQLException {
        dm.getDatasource().getConnection().prepareStatement(
                "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:00:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:01:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:02:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:03:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:04:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:05:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:06:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:07:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:08:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q', '2012-05-02 13:09:00', 'B');").execute();
    }

    private void insertDetectionsFromDetectorAAABBBAAABBBAAABBB(DatabaseManager dm) throws NamingException, SQLException {
        dm.getDatasource().getConnection().prepareStatement(
                "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q1', '2012-05-02 13:00:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q2', '2012-05-02 13:01:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q3', '2012-05-02 13:02:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q1', '2012-05-02 13:03:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q2', '2012-05-02 13:04:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q3', '2012-05-02 13:05:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q1', '2012-05-02 13:06:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q2', '2012-05-02 13:07:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q3', '2012-05-02 13:08:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q1', '2012-05-02 13:09:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q2', '2012-05-02 13:10:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q3', '2012-05-02 13:11:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q1', '2012-05-02 13:12:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q2', '2012-05-02 13:13:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q3', '2012-05-02 13:14:00', 'A');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q1', '2012-05-02 13:15:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q2', '2012-05-02 13:16:00', 'B');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('Q3', '2012-05-02 13:17:00', 'B');").execute();

    }

    private void insertDetectionsFromDetectorC(DatabaseManager dm) throws NamingException, SQLException {
        dm.getDatasource().getConnection().prepareStatement(
                "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:00:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:01:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:02:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:03:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:04:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:05:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:06:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:07:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:08:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:09:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:10:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:11:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:12:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:13:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:14:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:15:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:16:00', 'C');"
                + "INSERT INTO device_detection(device_id, detection_timestamp, detector_id) VALUES ('W', '2012-05-02 13:17:00', 'C');").execute();

    }
}
