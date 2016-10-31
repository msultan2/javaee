/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;

/**
 *
 * @author wingc
 */
public class AuditTrailProcessor {
    /**
     * User action
     */
    public enum UserAction {
        ADD_BRAND_CONTACT_DETAILS("ADD BRAND CONTACT DETAILS", "branding_contact_details"),
        REMOVE_BRAND_CONTACT_DETAILS("REMOVE BRAND CONTACT DETAILS", "branding_contact_details"),
        UPDATE_BRAND_CONTACT_DETAILS("UPDATE BRAND CONTACT DETAILS", "branding_contact_details"),
        ADD_ROUTE("ADD ROUTE", "route"),
        REMOVE_ROUTE("REMOVE ROUTE", "route"),
        UPDATE_ROUTE("UPDATE ROUTE", "route"),
        ADD_SPAN("ADD SPAN", "span"),
        REMOVE_SPAN("REMOVE SPAN", "span"),
        UPDATE_SPAN("UPDATE SPAN", "span"),
        //ADD_SPAN_OSRM("ADD SPAN OSRM", "span_osrm"),
        //REMOVE_SPAN_OSRM("REMOVE SPAN OSRM", "span_osrm"),
        UPDATE_SPAN_OSRM("UPDATE SPAN OSRM", "span_osrm"),
        //ADD_SPAN_SPEED_THRESHOLD("ADD SPAN SPEED THRESHOLD", "span_speed_thresholds"),
        //REMOVE_SPAN_SPEED_THRESHOLD("REMOVE SPAN SPEED THRESHOLD", "span_speed_thresholds"),
        UPDATE_SPAN_SPEED_THRESHOLD("UPDATE SPAN SPEED THRESHOLD", "span_speed_thresholds"),
        ADD_DETECTOR("ADD DETECTOR", "detector"),
        REMOVE_DETECTOR("REMOVE DETECTOR", "detector"),
        UPDATE_DETECTOR("UPDATE DETECTOR", "detector"),
        //ADD_UNCONFIG_DETECTOR("ADD UNCONFIGURED DETECTOR", "detector_unconfigured"),
        REMOVE_UNCONFIG_DETECTOR("REMOVE UNCONFIGURED DETECTOR", "detector_unconfigured"),
        ADD_SPAN_TO_ROUTE("ADD SPAN TO ROUTE", "route_span"),
        REMOVE_SPAN_FROM_ROUTE("REMOVE SPAN FROM ROUTE", "route_span"),
        ADD_DETECTOR_LOGICAL_GROUP("ADD DETECTOR LOGICAL GROUP", "detector_logical_group"),
        ADD_ROUTE_LOGICAL_GROUP("ADD ROUTE LOGICAL GROUP", "route_logical_group"),
        ADD_SPAN_LOGICAL_GROUP("ADD SPAN LOGICAL GROUP", "span_logical_group"),
        ADD_SPAN_NOTE("ADD SPAN NOTE", "span_notes_information"),
        REMOVE_SPAN_NOTE("REMOVE SPAN NOTE", "span_notes_information"),
        ADD_SPAN_EVENT("ADD SPAN EVENT", "span_events_information"),
        REMOVE_SPAN_EVENT("REMOVE SPAN EVENT", "span_events_information"),
        ADD_SPAN_INCIDENT("ADD SPAN INCIDENT", "span_incidents_information"),
        REMOVE_SPAN_INCIDENT("REMOVE SPAN INCIDENT", "span_incidents_information"),
        ADD_DETECTOR_NOTE("ADD DETECTOR NOTE", "detector_engineer_notes"),
        REMOVE_DETECTOR_NOTE("REMOVE DETECTOR NOTE", "detector_engineer_notes"),
        ADD_USER("ADD USER", "instation_user"),
        REMOVE_USER("REMOVE USER", "instation_user"),
        UPDATE_USER("UPDATE USER", "instation_user"),
        UPDATE_USER_PASSWORD_EXPIRY_DAYS("UPDATE USER PASSWORD EXPIRY DAYS", "instation_user"),
        ACTIVATE_USER("ACTIVATE USER", "instation_user"),
        DEACTIVATE_USER("DEACTIVATE USER", "instation_user"),
        ADD_ROLE_TO_USER("ADD ROLE TO USER", "instation_user_role"),
        REMOVE_ROLE_FROM_USER("REMOVE ROLE FROM USER", "instation_user_role"),
        ADD_LOGICAL_GROUP("ADD LOGICAL GROUP", "logical_group"),
        REMOVE_LOGICAL_GROUP("REMOVE LOGICAL GROUP", "logical_group"),
        UPDATE_LOGICAL_GROUP("UPDATE LOGICAL GROUP", "logical_group"),
        ADD_LOGICAL_GROUP_USERS("ADD LOGICAL GROUP USERS", "instation_user_logical_group"),
        REMOVE_LOGICAL_GROUP_USERS("REMOVE LOGICAL GROUP USERS", "instation_user_logical_group"),
        ADD_LOGICAL_GROUP_ROUTES("ADD LOGICAL GROUP ROUTES", "route_logical_group"),
        REMOVE_LOGICAL_GROUP_ROUTES("REMOVE LOGICAL GROUP ROUTES", "route_logical_group"),
        ADD_LOGICAL_GROUP_SPANS("ADD LOGICAL GROUP SPANS", "span_logical_group"),
        REMOVE_LOGICAL_GROUP_SPANS("REMOVE LOGICAL GROUP SPANS", "span_logical_group"),
        ADD_LOGICAL_GROUP_DETECTORS("ADD LOGICAL GROUP DETECTORS", "detector_logical_group"),
        REMOVE_LOGICAL_GROUP_DETECTORS("REMOVE LOGICAL GROUP DETECTORS", "detector_logical_group"),
        ADD_BRAND("ADD BRAND", "branding"),
        REMOVE_BRAND("REMOVE BRAND", "branding"),
        UPDATE_BRAND("UPDATE BRAND", "branding"),
        ADD_BROADCAST_MESSAGE_LOGICAL_GROUP("ADD BROADCAST MESSAGE LOGICAL GROUP", "broadcast_message_logical_group"),
        ADD_BROADCAST_MESSAGE("ADD BROADCAST MESSAGE", "broadcast_message"),
        REMOVE_BROADCAST_MESSAGE("REMOVE BROADCAST MESSAGE", "broadcast_message"),
        UPDATE_BROADCAST_MESSAGE("UPDATE BROADCAST MESSAGE", "broadcast_message"),
        UPDATE_DETECTOR_CONFIGURATION("UPDATE DETECTOR CONFIGURATION", "detector_configuration"),
        USER_LOGIN("USER LOGIN", "instation_user"),
        USER_LOGOUT("USER LOGOUT", "instation_user"),
        QUERY_USER_BRAND("QUERY USER BRAND", "instation_user"), // not for logging
        ADD_DEFAULT_DETECTOR_CONFIGURATION("ADD DEFAULT DETECTOR CONFIGURATION", "default_configuration"),
        UPDATE_DEFAULT_DETECTOR_CONFIGURATION("UPDATE DEFAULT DETECTOR CONFIGURATION", "default_configuration"),
        REMOVE_DEFAULT_DETECTOR_CONFIGURATION("DELETE DEFAULT DETECTOR CONFIGURATION", "default_configuration");
        
        public final String descriptiveName;
        public final int index;
        public final String tableAffected;
        private static int localIndex = 0;
        
        private UserAction(String descriptiveName, String tableAffected) {
            this.descriptiveName = descriptiveName;
            this.tableAffected = tableAffected;
            this.index = getLocalIndex();
            incrementLocalIndex();
        }
        
        private static int getLocalIndex() {
            return localIndex;
        }
        
        private static void incrementLocalIndex() {
            localIndex++;
        }
    }
    
    private static final Logger LOGGER = LogManager.getLogger(AuditTrailProcessor.class);
    
    private static final String SQL_INSERT_INTO_AUDIT_TRAIL = "INSERT INTO audit_trail " 
            + "(username,timestamp,action_type,description) "
            + "VALUES(?,?,?,?)";
    
    /**
     * Retrieve a value from the table with the filter
     * @param action user action
     * @param column column name
     * @param params the database filter
     * @return the column value
     */
    public static String getColumnValue(UserAction action, String column, Object... params) {
        String result = null;
        DatabaseManager dm;
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            // get database manager
            dm = DatabaseManager.getInstance();
            // get database connection
            connection = dm.getDatasource().getConnection();
            // prepare the SQL statement
            String statement = getQueryStatement(action.tableAffected, params);
            ps = connection.prepareStatement(statement);
            // execute SQL query
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString(column);
            }
        } catch (DatabaseManagerException ex) {
            LOGGER.warn("Database manager instance could not be retrieved", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming exception while updating audit_trail", ex);
        } catch (SQLException ex) {
            LOGGER.warn("SQL exception while updateing audit_trail", ex);
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
        return result;
    }
    
    /**
     * Retrieve the specific values from the table with the filter
     * @param action user action
     * @param column column name
     * @param params the database filter
     * @return a list of values with both column name and its value
     */
    public static Map<String, String[]> getColumnsValue(UserAction action, String [] columns, Object... params) {
        Map result = null;
        DatabaseManager dm;
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            // get database manager
            dm = DatabaseManager.getInstance();
            // get database connection
            connection = dm.getDatasource().getConnection();
            // prepare the SQL statement
            String statement = getQueryStatement(action.tableAffected, params);
            ps = connection.prepareStatement(statement);
            // execute SQL query
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = new HashMap<String, String[]>();
                for (int i = 0; i < columns.length; i++) {
                    String columnName = columns[i];
                    String [] value = {rs.getString(columnName)};
                    result.put(columnName, value);
                }
            }
        } catch (DatabaseManagerException ex) {
            LOGGER.warn("Database manager instance could not be retrieved", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming exception while updating audit_trail", ex);
        } catch (SQLException ex) {
            LOGGER.warn("SQL exception while updateing audit_trail", ex);
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
        return result;
    }
    
    /**
     * Retrieve all the values from the table with the filter
     * @param action user action
     * @param column column name
     * @param params the database filter
     * @return a list of values with both column name and its value
     */
    public static Map<String, String[]> getTableValues(UserAction action, Object... params) {
        Map result = null;
        DatabaseManager dm;
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            // get database manager
            dm = DatabaseManager.getInstance();
            // get database connection
            connection = dm.getDatasource().getConnection();
            // prepare the SQL statement
            String statement = getQueryStatement(action.tableAffected, params);
            ps = connection.prepareStatement(statement);
            // execute SQL query
            ResultSet rs = ps.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();
            if (rs.next()) {
                result = new HashMap<String, String[]>();
                for (int i = 1; i <= columnCount; i++) {
                    String key = rs.getMetaData().getColumnName(i);
                    if ((rs.getMetaData().getColumnType(i) == Types.BOOLEAN) || ((rs.getMetaData().getColumnType(i) == Types.BIT))) {
                        String boolValue = Boolean.toString(rs.getBoolean(i));
                        String [] value = {boolValue};
                        result.put(key, value);
                    } else if (rs.getMetaData().getColumnType(i) == Types.TIMESTAMP) {
                        String dateValue = rs.getTimestamp(i).toString();
                        String [] value = {dateValue};
                        result.put(key, value);
                    } else {
                        String [] value = {rs.getString(i)};
                        result.put(key, value);
                    }
                }
            }
        } catch (DatabaseManagerException ex) {
            LOGGER.warn("Database manager instance could not be retrieved", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming exception while updating audit_trail", ex);
        } catch (SQLException ex) {
            LOGGER.warn("SQL exception while updateing audit_trail", ex);
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
        return result;
    }
    
    /**
     * Log the user action that access the database to audit trail.
     * @param user the login user
     * @param action the action to access the database
     * @param description the description of the action
     */
    public static void log(String user, UserAction action, String description) {

        //I think in the future we need to break the connnection out of this and make it so that it becomes part of a transaction.

        DatabaseManager dm;
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            Timestamp ts = Timestamp.valueOf((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
            // get database manager
            dm = DatabaseManager.getInstance();
            // get database connection
            connection = dm.getDatasource().getConnection();
            // prepare the detector SQL statement
            ps = connection.prepareStatement(SQL_INSERT_INTO_AUDIT_TRAIL);
            ps.setString(1, user);
            ps.setTimestamp(2, ts);
            ps.setString(3, action.descriptiveName);
            ps.setString(4, description);
            // execute SQL query
            ps.executeUpdate();
            // update process ok
        } catch (DatabaseManagerException ex) {
            LOGGER.warn("Database manager instance could not be retrieved", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming exception while updating audit_trail", ex);
        } catch (SQLException ex) {
            LOGGER.warn("SQL exception while updateing audit_trail", ex);
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
    
    private static String getQueryStatement(String tableName, Object... params) {
        String statement = "";
        if (params.length > 0) {
            Map.Entry<String, String> element = (Map.Entry<String, String>)params[0];
            statement = "SELECT * FROM " + tableName + " WHERE " + element.getKey() + "='" + element.getValue() + "'";
            for (int i = 1; i < params.length; i++) {
                element = (Map.Entry<String, String>)params[i];
                statement += " AND " + element.getKey() + "='" + element.getValue() + "'";
            }
        }
        return statement;
    }
}
