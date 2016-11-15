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
 * Copyright 2002 (C) Simulation Systems Ltd. All Rights Reserved.
 * 
 * DetectorManager.java
 * @author nthompson, svenkataramanappa
 * 
 * Product:
 *
 * Change History: Created on August 13, 2007, 11:31 AM Version 001
 * 2015-02-27 SCJS 597/030 - Display only active icons on Map
 *
 */

package ssl.bluetruth.servlet.update;

import com.ssl.bluetruth.db.DBHasResult;
import com.ssl.bluetruth.db.DBSetsParams;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.QueueManager;
import com.ssl.bluetruth.receiver.v2.seed.SSH;
import com.ssl.bluetruth.receiver.v2.seed.Seed;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ssl.bluetruth.beans.ManagerStatusReport;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.AuditTrailProcessor;
import ssl.bluetruth.utils.JsonResponseProcessor;

public class DetectorManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(DetectorManager.class);
    private final String INSERT_DETECTOR_SQL = "INSERT INTO detector (detector_name, detector_id, mode, carriageway, latitude, longitude) VALUES (?,?,?,?,?,?);";
    private final String INSERT_DETECTOR_LOGICAL_GROUP_SQL = "INSERT INTO detector_logical_group (detector_id, logical_group_name) VALUES (?,?);";
    private final String UPDATE_DETECTOR_SQL = "UPDATE detector SET %s = ? WHERE detector_id = ?;";
    private final String DELETE_DETECTOR_SQL = "DELETE FROM detector WHERE detector_id = ?;";
    private final String LIST_DETECTOR_SQL = "SELECT detector.detector_id, detector.detector_name "
            + "FROM detector "
            + "JOIN detector_logical_group ON detector.detector_id = detector_logical_group.detector_id "
            + "JOIN instation_user_logical_group ON detector_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "JOIN instation_user ON instation_user.username = instation_user_logical_group.username AND instation_user.username = '%s' "
            + "ORDER BY detector_name ASC;";
    // SCJS 016 START
    private final String FETCH_DETECTOR_DESCRIPTION = "SELECT detector_description "
            + "FROM detector "
            + "WHERE detector_id = ?";
    // SCJS 016 END
    // SCJS 017 START
    private final String DELETE_UNCONFIGURED_DETECTOR_SQL = "DELETE FROM detector_unconfigured WHERE detector_id = ?;";
    // SCJS 017 END
    private final String RELOAD_SEED = "UPDATE detector_seed SET last=?, retries=0 WHERE detector_id=? AND id=?";

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getUserPrincipal().getName();

        Result result = new Result();
        String action = request.getParameter("action");
        String id = request.getParameter("id");
        if (action.equalsIgnoreCase("insert")) {
            insertDetector(username, request, id, result);
        } else if (action.equalsIgnoreCase("update")) {
            updateDetector(username, request, id, result);
        } else if (action.equalsIgnoreCase("delete")) {
            deleteDetector(username, id, result);
        } else if (action.equalsIgnoreCase("list")) {
            listDetectors(result, username);
        } // SCJS 016 START
        else if (action.equalsIgnoreCase("get")) {
            fetchDetectorDescription(id, result);
        } // SCJS 016 END
        else if (action.equalsIgnoreCase("delete-unconfig")) {
            deleteUnconfigDetector(username, null, id, result);
        } else if (action.equalsIgnoreCase("update-location")) {
            updateDetectorLocation(username, request, id, result);
        } else if (action.equalsIgnoreCase("queue-commands")
                && request.getParameterMap().containsKey("commands")) { // for 2.00
            queueCommands(username, id, request.getParameter("commands").split(","), result);
        } else if (action.equalsIgnoreCase("get-status")) {
            JsonResponseProcessor jrp = new JsonResponseProcessor();
            ManagerStatusReport msr = new ManagerStatusReport(id);
            jrp.createResponse(msr, response.getWriter());
            return;
        } else if (action.equalsIgnoreCase("new-seed")) {
            int port = getPortNumber(id);
            if (port != -1) {
                // Transfer seed file on current SSH connection
                Seed s = new Seed().setDetectorId(id).setPort(port);
                s.save();
                if (s.sendToOutstation()) {
                    new QueueManager(id).enqueue("changeSeed");
                    result.setSuccess(true);
                } else {
                    result.setSuccess(false);
                }
            } else {
                // Request SSH connection on seed port
                QueueManager qm = new QueueManager(id);
                qm.enqueue("openSSHConnection:50000");
                result.setSuccess(true);
            }
        } else if (action.equalsIgnoreCase("reload-key")) {
            int port = getPortNumber(id);
            if (port != -1) {
                SSH ssh = new SSH()
                        .user("remote")
                        .host("localhost")
                        .port(port)
                        .instationPrivateKey("/home/bt/.ssh/to_outstation_key")
                        .setDetectorId(id);
                ssh.newkeys();
                result.setSuccess(true);
            } else {
                LOGGER.warn(id + ": Failed to send keys (Reason: No connection)");
                result.setSuccess(false);
            }
        } else if (action.equalsIgnoreCase("add-outstation-to-instation-key")) {
            // add 
            SSH ssh = new SSH()
                    .setDetectorId(id);
            ssh.regenerateInstationAuthorizedFile();
            ssh.addOutstationToInstationKey();
        } else if (action.equalsIgnoreCase("remove-outstation-to-instation-key")) {
            // delete
            SSH ssh = new SSH()
                    .setDetectorId(id);
            ssh.regenerateInstationAuthorizedFile();
        }
        try {
            JsonResponseProcessor jrp = new JsonResponseProcessor();
            jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    private void queueCommands(String username, String detector_id, String[] commands, Result result) {
        AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_DETECTOR,
                String.format("Commands '%s' were sent to detector id '%s'", StringUtils.join(commands, ","), detector_id));
        QueueManager qm = new QueueManager(detector_id);
        for (String command : commands) {
            qm.enqueue(command);
            if (command.equals("changeSeed")) {
                reloadSeed(detector_id);
            }
        }
        result.setSuccess(true);
    }

    private void reloadSeed(String detector_id) {
        HashMap<String, Integer> seedMap = fetchSeed(detector_id);
        reloadSeed(detector_id, seedMap);
    }
    
    private void reloadSeed(String detector_id, HashMap<String, Integer> seedMap) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();

            stmt = connection.prepareStatement(RELOAD_SEED);
            stmt.setInt(1, seedMap.get("Value"));
            stmt.setString(2, detector_id);            
            stmt.setInt(3, seedMap.get("ID"));
            stmt.executeUpdate();
            
            LOGGER.info("Reload seed successful on Instation");
        } catch (SQLException ex) {
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }
    
    private HashMap<String, Integer> fetchSeed(String detectorId) {
        HashMap<String, Integer> seedMap = new HashMap<>();
        try {
            File seedFile = new File("/tmp/seed_" + detectorId + ".xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(seedFile);
            NodeList seed = doc.getDocumentElement().getChildNodes();
            
            seedMap.put("ID", Integer.parseInt(seed.item(0).getFirstChild().getNodeValue()));       // ID
            seedMap.put("Value", Integer.parseInt(seed.item(1).getFirstChild().getNodeValue()));    // Value
            
        } catch (ParserConfigurationException ex) {
            LOGGER.fatal("Parser Exception", ex);
        } catch (SAXException ex) {
            LOGGER.fatal("SAX Exception", ex);
        } catch (IOException ex) {
            LOGGER.fatal("File not found exception", ex);
        } finally {
            return seedMap;
        }
    }
    
    private void updateDetector(String username, HttpServletRequest request, String id, Result result) throws NumberFormatException {
        String value = request.getParameter("value");
        String column = request.getParameter("column");
        if (column.equalsIgnoreCase("0")) {
            updateDetectorTable(username, "detector_name", value, id, result);
        } else if (column.equalsIgnoreCase("1")) {
            updateDetectorTable(username, "detector_id", value, id, result);
        } else if (column.equalsIgnoreCase("2")) {
            updateDetectorTable(username, "location", value, id, result);
        } else if (column.equalsIgnoreCase("3")) {
            updateDetectorTable(username, "latitude", Double.parseDouble(value), id, result);
        } else if (column.equalsIgnoreCase("4")) {
            updateDetectorTable(username, "longitude", Double.parseDouble(value), id, result);
        } else if (column.equalsIgnoreCase("5")) {
            updateDetectorTable(username, "mode", Integer.parseInt(value), id, result);
        } else if (column.equalsIgnoreCase("6")) {
            updateDetectorTable(username, "carriageway", value, id, result);
        } else if (column.equalsIgnoreCase("7")) {
            updateDetectorTable(username, "active", Boolean.parseBoolean(value), id, result);
        }// SCJS 016 START
        else if (column.equalsIgnoreCase("description")) {
            updateDetectorTable(username, "detector_description", value, id, result);
        } // SCJS 016 END
    }

    private void updateDetectorLocation(String username, HttpServletRequest request, String id, Result result) throws NumberFormatException {

        String columnOne = "latitude";
        String columnTwo = "longitude";
        Double valueOne = Double.parseDouble(request.getParameter(columnOne));
        Double valueTwo = Double.parseDouble(request.getParameter(columnTwo));

        Connection connection = null;
        PreparedStatement stmtOne = null;
        PreparedStatement stmtTwo = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            // get the query result before updating to the database
            String prevResultOne = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_DETECTOR, columnOne,
                    (new HashMap.SimpleEntry<String, String>("detector_id", id)));
            String prevResultTwo = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_DETECTOR, columnTwo,
                    (new HashMap.SimpleEntry<String, String>("detector_id", id)));

            stmtOne = connection.prepareStatement(String.format(UPDATE_DETECTOR_SQL, columnOne));
            stmtOne.setDouble(1, valueOne);
            stmtOne.setString(2, id);
            stmtOne.executeUpdate();

            stmtTwo = connection.prepareStatement(String.format(UPDATE_DETECTOR_SQL, columnTwo));
            stmtTwo.setDouble(1, valueTwo);
            stmtTwo.setString(2, id);
            stmtTwo.executeUpdate();

            connection.commit();
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_DETECTOR,
                    "Detector id '" + id + "' is updated: The column '" + columnOne + "' is changed FROM '" + prevResultOne + "' TO '" + valueOne.toString() + "'");

            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_DETECTOR,
                    "Detector id '" + id + "' is updated: The column '" + columnTwo + "' is changed FROM '" + prevResultTwo + "' TO '" + valueTwo.toString() + "'");
            result.setSuccess(true);
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("Unable to update detector location. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmtOne != null) {
                try {
                    stmtOne.close();
                } catch (SQLException e) {
                }
                stmtOne = null;
            }
            if (stmtTwo != null) {
                try {
                    stmtTwo.close();
                } catch (SQLException e) {
                }
                stmtTwo = null;
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                }
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }

    }

    private void insertDetector(String username, HttpServletRequest request, String id, Result result) throws NumberFormatException {
        String name = request.getParameter("name");
        Double latitude = Double.parseDouble(request.getParameter("latitude"));
        Double longitude = Double.parseDouble(request.getParameter("longitude"));
        Integer mode = Integer.parseInt(request.getParameter("mode"));
        String carriageway = request.getParameter("carriageway");
        String[] logicalGroups = request.getParameterValues("logical_group_names");
        if (logicalGroups == null) {
            result.setSuccess(false);
            result.setMessage("At least one logical group must be specified.");
        } else {
            insertInToDetectorTable(username, name, id, latitude, longitude, mode, carriageway, logicalGroups, result);
        }
    }

    private void insertInToDetectorTable(String username, String name, String id, Double latitude, Double longitude, Integer mode, String carriageway, String[] logicalGroups, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(INSERT_DETECTOR_SQL);
            stmt.setString(1, name);
            stmt.setString(2, id.trim());
            stmt.setInt(3, mode);
            stmt.setString(4, carriageway);
            stmt.setDouble(5, latitude);
            stmt.setDouble(6, longitude);
            stmt.executeUpdate();
            insertDetectorLogicalGroup(username, connection, id, logicalGroups, result);
            // SCJS 017 START
            deleteUnconfigDetector(username, connection, id, result);
            // SCJS 017 END
            connection.commit();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_DETECTOR, "Detector id '" + id + "' is added");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("Unable to add new detector id '"+id+"' as it already exists. Please try again."); 
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                }
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    private void insertDetectorLogicalGroup(String username, Connection connection, String id, String[] logicalGroups, Result result) throws SQLException {
        PreparedStatement stmt = null;

        String insertDetectorLogicalGroups = "";

        for (int i = 0; i < logicalGroups.length; i++) {
            insertDetectorLogicalGroups += INSERT_DETECTOR_LOGICAL_GROUP_SQL;
        }
        stmt = connection.prepareStatement(insertDetectorLogicalGroups);
        int index = 1;
        String lgDescription = "(";
        for (int i = 0; i < logicalGroups.length; i++) {
            stmt.setString(index, id);
            index++;
            stmt.setString(index, logicalGroups[i]);
            index++;
            lgDescription += logicalGroups[i] + ",";
        }
        lgDescription = lgDescription.substring(0, lgDescription.length() - 1);
        lgDescription += ")";
        stmt.executeUpdate();
        // log the user action
        AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_DETECTOR_LOGICAL_GROUP, "Detector id '" + id + "' with the logical groups " + lgDescription + " is added");
    }

    private void updateDetectorTable(String username, String column, String value, String id, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();

            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_DETECTOR, column,
                    (new HashMap.SimpleEntry<String, String>("detector_id", id)));

            stmt = connection.prepareStatement(String.format(UPDATE_DETECTOR_SQL, column));
            stmt.setString(1, value.trim());
            stmt.setString(2, id);
            stmt.executeUpdate();
            // SCJS 016 START
            result.setData(value);
            // SCJS 016 END
            result.setSuccess(true);

            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_DETECTOR,
                    "Detector id '" + id + "' is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to update detector id '" + id + "' information. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    //SCJS 030
    private void updateDetectorTable(String username, String column, Boolean value, String id, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();

            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_DETECTOR, column,
                    (new HashMap.SimpleEntry<String, String>("detector_id", id)));

            stmt = connection.prepareStatement(String.format(UPDATE_DETECTOR_SQL, column));
            stmt.setBoolean(1, value);
            stmt.setString(2, id);
            stmt.executeUpdate();
            // SCJS 016 START
            result.setData(value);
            // SCJS 016 END
            result.setSuccess(true);

            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_DETECTOR,
                    "Detector id '" + id + "' is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to update Detector id '" + id+"' information. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }
    
    private void updateDetectorTable(String username, String column, Integer value, String id, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();

            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_DETECTOR, column,
                    (new HashMap.SimpleEntry<String, String>("detector_id", id)));

            stmt = connection.prepareStatement(String.format(UPDATE_DETECTOR_SQL, column));
            stmt.setInt(1, value);
            stmt.setString(2, id);
            stmt.executeUpdate();
            result.setSuccess(true);

            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_DETECTOR,
                    "Detector id '" + id + "' is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value.toString() + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to update detector id '" + id+"'");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    private void updateDetectorTable(String username, String column, Double value, String id, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();

            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_DETECTOR, column,
                    (new HashMap.SimpleEntry<String, String>("detector_id", id)));

            stmt = connection.prepareStatement(String.format(UPDATE_DETECTOR_SQL, column));
            stmt.setDouble(1, value);
            stmt.setString(2, id);
            stmt.executeUpdate();
            result.setSuccess(true);

            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_DETECTOR,
                    "Detector id '" + id + "' is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value.toString() + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to update Detector id '" + id + "'. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    private void deleteDetector(String username, String id, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_DETECTOR_SQL);
            stmt.setString(1, id);
            stmt.execute();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_DETECTOR, "Detector id '" + id + "' is removed");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to delete detector id '"+id+"'. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    private void deleteUnconfigDetector(String username, Connection connection, String id, Result result) {
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_UNCONFIGURED_DETECTOR_SQL);
            stmt.setString(1, id);
            int rowCount = stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            if (rowCount > 0) {
                AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_UNCONFIG_DETECTOR, "Unconfigured detector id '" + id + "' is removed");
            }
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to delete unconfigured detector id '" + id+"'. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    // SCJS 016 START
    private void fetchDetectorDescription(String id, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(FETCH_DETECTOR_DESCRIPTION);
            stmt.setString(1, id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                result.setData(rs.getString(1));
            }
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage("Unable to retreive detector information.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }
    // SCJS 016 END

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void listDetectors(Result result, String username) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(String.format(LIST_DETECTOR_SQL, username));
            rs = stmt.executeQuery();
            Map map = new HashMap();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
            result.setData(map);
            result.setSuccess(true);
        } catch (SQLException ex) {
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }
    
    private int getPortNumber(final String detector_id) {
        int port = (int) (new Db().sql("SELECT ssh FROM detector_status WHERE detector_id = ?")
                .set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                ps.setString(1, detector_id);
            }
        }).go(new DBHasResult<Integer>() {
            @Override
            public Integer done(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    String ssh = rs.getString(1);
                    if (ssh.contains("open") 
                            && ssh.split(" ").length == 3) {
                        return Integer.parseInt(ssh.split(" ")[2]);
                    }
                }
                return -1;
            }
        }).close().response());
        return port;
    }
}
