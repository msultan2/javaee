/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.servlet.receiver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.naming.NamingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;

/**
 *
 * @author wingc
 */
public class AbstractUnconfiguredDetector {
    public enum UnconfiguredType {
        CONFIG_DOWNLOAD_REQUEST("last_configuration_download_request", 0),
        LAST_DEVICE_DETECTION("last_device_detection", 1),
        LAST_TRAFFIC_FLOW_REPORT("last_traffic_flow_report", 2),
        LAST_MESSAGE_REPORT("last_message_report", 3),
        LAST_LOG_UPLOAD("last_log_upload", 4);
        
        public final String descriptiveName;
        public final int index;
        
        private UnconfiguredType(String descriptiveName, int index) {
            this.descriptiveName = descriptiveName;
            this.index = index;
        }
    }
    
    protected final Logger LOGGER = LogManager.getLogger(getClass());
    
    protected static final String SQL_INSERT_INTO_DEVICE_UNCONFIGURED = "INSERT INTO detector_unconfigured "
            + "(detector_id, last_configuration_download_request, last_device_detection, last_traffic_flow_report, last_message_report, last_log_upload) "
            + "VALUES (?,?,?,?,?,?);";
    protected static final String SQL_SELECT_FROM_DETECTOR = "SELECT detector_id FROM detector WHERE detector_id=?";
    protected static final String SQL_SELECT_FROM_DEVICE_UNCONFIGURED = "SELECT detector_id FROM detector_unconfigured WHERE detector_id=?";
    
    /**
     * Check if the detector is configured in the database
     * @param detectorId the detector ID
     * @return true if the detector is configured
     */
    public boolean checkDetectorConfiguredInDatabase(String detectorId) {
        return checkDetectorExists(detectorId, SQL_SELECT_FROM_DETECTOR);
    }
    
    /**
     * Insert/Update the un-configured detector into the database
     * @param detectorId the detector ID
     */
    public void insertUnconfiguredDetector(String detectorId, UnconfiguredType type) {
        DatabaseManager dm;
        Connection connection = null;
        PreparedStatement ps = null;
        
        // get database manager
        try {
            // get database manager
            dm = DatabaseManager.getInstance();
            // get database connection
            connection = dm.getDatasource().getConnection();
            // prepare the detector SQL statement
            ps = prepareDetectorStatement(connection, detectorId, type);
            // execute SQL query
            ps.executeUpdate();
        } catch (DatabaseManagerException ex) {
            LOGGER.warn("Database manager instance could not be retrieved", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming exception while insert into device_unconfigured", ex);
        } catch (SQLException ex) {
            LOGGER.warn("SQL exception while insert into device_unconfigured", ex);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (connection != null) {
                    connection.close();
                } 
            } catch (SQLException ex) {
                LOGGER.warn("SQL exception while closing connection", ex);
            }
        }
    }
    
    private PreparedStatement prepareDetectorStatement(Connection connection, String detectorId, UnconfiguredType type) {
        boolean detectorExists = checkDetectorExists(detectorId, SQL_SELECT_FROM_DEVICE_UNCONFIGURED);
        PreparedStatement ps = null;
        // get current timestamp
        Timestamp ts = Timestamp.valueOf((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
        
        try {
            if (detectorExists) {
                // prepare update statement
                String SQL_UPDATE_DEVICE_UNCONFIGURED = "UPDATE detector_unconfigured SET "
                        + type.descriptiveName
                        + "=? WHERE detector_id=?";
                ps = connection.prepareStatement(SQL_UPDATE_DEVICE_UNCONFIGURED);
                ps.setTimestamp(1, ts);
                ps.setString(2, detectorId);
            } else {
                // prepare insert statement
                ps = connection.prepareStatement(SQL_INSERT_INTO_DEVICE_UNCONFIGURED);
                ps.setString(1, detectorId);
                
                for (UnconfiguredType t : UnconfiguredType.values()) {
                    if (type.equals(t)) {
                        ps.setTimestamp(t.index+2, ts);
                    } else {
                        ps.setNull(t.index+2, java.sql.Types.TIMESTAMP);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.warn("SQL exception while closing connection", ex);
        }
        
        return ps;
    }
    
    private boolean checkDetectorExists(String detectorId, String statement) {
        boolean checkResult = false;
        DatabaseManager dm;
        Connection connection = null;
        PreparedStatement ps = null;
        
        // get database manager
        try {
            // get database manager
            dm = DatabaseManager.getInstance();
            // get database connection
            connection = dm.getDatasource().getConnection();
            // prepare the SQL statement
            ps = connection.prepareStatement(statement);
            ps.setString(1, detectorId);
            // execute SQL query
            ResultSet rs = ps.executeQuery();
            // set the check result to true if it contains an item in the result set
            checkResult = rs.next();
        } catch (DatabaseManagerException ex) {
            LOGGER.warn("Database manager instance could not be retrieved", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming exception while select detector_id from detector table", ex);
        } catch (SQLException ex) {
            LOGGER.warn("SQL exception while select detector_id from detector table", ex);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (connection != null) {
                    connection.close();
                } 
            } catch (SQLException ex) {
                LOGGER.warn("SQL exception while closing connection", ex);
            }
        }
        return checkResult;
    }
}
