package ssl.bluetruth.servlet.update;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.AuditTrailProcessor;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author nthompson
 */
public class LogicalGroupManager extends HttpServlet {

    private static final org.apache.log4j.Logger LOGGER = LogManager.getLogger(DetectorConfigurationManager.class);
    private String INSERT_LOGICAL_GROUP = "INSERT INTO logical_group"
            + " (logical_group_name, description)"
            + " VALUES (?, ?);";
    private String DELETE_LOGICAL_GROUP = "DELETE FROM logical_group"
            + " WHERE logical_group_name = ?;";
    private String UPDATE_LOGICAL_GROUP_SQL = "UPDATE logical_group SET %s = ? WHERE logical_group_name = ?;";

    private String INSERT_INSTATION_USER_LOGICAL_GROUP_RELATIONSHIP = "INSERT INTO instation_user_logical_group"
            + " (username, logical_group_name)"
            + " VALUES (?, ?);";
    private String INSERT_DETECTOR_LOGICAL_GROUP_RELATIONSHIP = "INSERT INTO detector_logical_group"
            + " (detector_id, logical_group_name)"
            + " VALUES (?, ?);";
    private String INSERT_SPAN_LOGICAL_GROUP_RELATIONSHIP = "INSERT INTO span_logical_group"
            + " (span_name, logical_group_name)"
            + " VALUES (?, ?);";
    private String INSERT_ROUTE_LOGICAL_GROUP_RELATIONSHIP = "INSERT INTO route_logical_group"
            + " (route_name, logical_group_name)"
            + " VALUES (?, ?);";
    private String DELETE_INSTATION_USER_LOGICAL_GROUP_RELATIONSHIP = "DELETE FROM instation_user_logical_group"
            + " WHERE username = ? AND logical_group_name = ?;";
    private String DELETE_DETECTOR_LOGICAL_GROUP_RELATIONSHIP = "DELETE FROM detector_logical_group"
            + " WHERE detector_id = ? AND logical_group_name = ?;";
    private String DELETE_SPAN_LOGICAL_GROUP_RELATIONSHIP = "DELETE FROM span_logical_group"
            + " WHERE span_name = ? AND logical_group_name = ?;";
    private String DELETE_ROUTE_LOGICAL_GROUP_RELATIONSHIP = "DELETE FROM route_logical_group"
            + " WHERE route_name = ? AND logical_group_name = ?;";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Result result = new Result();
        String action = request.getParameter("action");
        String logicalGroupName = request.getParameter("logical_group_name");
        String username = request.getUserPrincipal().getName();

        if (action.equalsIgnoreCase("insert")) {
            String description = request.getParameter("description");
            insertLogicalGroup(username, logicalGroupName, description, result);
        } else if (action.equalsIgnoreCase("delete")) {
            deleteLogicalGroup(username, logicalGroupName, result);
        } else if (action.equalsIgnoreCase("update")) {
            String value = request.getParameter("value");
            String column = request.getParameter("column");
            if (column.equalsIgnoreCase("0")) {
                updateLogicalGroup(username, "logical_group_name", value, logicalGroupName, result);
            } else if (column.equalsIgnoreCase("1")) {
                updateLogicalGroup(username, "description", value, logicalGroupName, result);
            }
        } else if (action.equalsIgnoreCase("add_user")) {
            String identifier = request.getParameter("username");
            updateLogicalGroupRelationship(username, AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP_USERS, INSERT_INSTATION_USER_LOGICAL_GROUP_RELATIONSHIP, identifier, logicalGroupName, result);
        } else if (action.equalsIgnoreCase("add_detector")) {
            String identifier = request.getParameter("detector");
            updateLogicalGroupRelationship(username, AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP_DETECTORS, INSERT_DETECTOR_LOGICAL_GROUP_RELATIONSHIP, identifier, logicalGroupName, result);
        } else if (action.equalsIgnoreCase("add_span")) {
            String identifier = request.getParameter("span");
            updateLogicalGroupRelationship(username, AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP_SPANS, INSERT_SPAN_LOGICAL_GROUP_RELATIONSHIP, identifier, logicalGroupName, result);
        } else if (action.equalsIgnoreCase("add_route")) {
            String identifier = request.getParameter("route");
            updateLogicalGroupRelationship(username, AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP_ROUTES, INSERT_ROUTE_LOGICAL_GROUP_RELATIONSHIP, identifier, logicalGroupName, result);
        } else if (action.equalsIgnoreCase("remove_user")) {
            String identifier = request.getParameter("username");
            updateLogicalGroupRelationship(username, AuditTrailProcessor.UserAction.REMOVE_LOGICAL_GROUP_USERS, DELETE_INSTATION_USER_LOGICAL_GROUP_RELATIONSHIP, identifier, logicalGroupName, result);
        } else if (action.equalsIgnoreCase("remove_detector")) {
            String identifier = request.getParameter("detector");
            updateLogicalGroupRelationship(username, AuditTrailProcessor.UserAction.REMOVE_LOGICAL_GROUP_DETECTORS, DELETE_DETECTOR_LOGICAL_GROUP_RELATIONSHIP, identifier, logicalGroupName, result);
        } else if (action.equalsIgnoreCase("remove_span")) {
            String identifier = request.getParameter("span");
            updateLogicalGroupRelationship(username, AuditTrailProcessor.UserAction.REMOVE_LOGICAL_GROUP_SPANS, DELETE_SPAN_LOGICAL_GROUP_RELATIONSHIP, identifier, logicalGroupName, result);
        } else if (action.equalsIgnoreCase("remove_route")) {
            String identifier = request.getParameter("route");
            updateLogicalGroupRelationship(username, AuditTrailProcessor.UserAction.REMOVE_LOGICAL_GROUP_ROUTES, DELETE_ROUTE_LOGICAL_GROUP_RELATIONSHIP, identifier, logicalGroupName, result);
        }
        try{
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    private void insertLogicalGroup(String username, String name, String description, Result result) {

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(INSERT_LOGICAL_GROUP);
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.executeUpdate();
            connection.commit();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP, "Logical group '" + name + "' is created");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to create logical group '"+name+"' as it already exists. Please try again.");
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

    private void deleteLogicalGroup(String username, String name, Result result) {

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(DELETE_LOGICAL_GROUP);
            stmt.setString(1, name);
            stmt.executeUpdate();
            connection.commit();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_LOGICAL_GROUP, "Logical group '" + name + "' is removed");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to delete logical group '"+name+"'. Please try again.");
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

    private void updateLogicalGroupRelationship(String username, AuditTrailProcessor.UserAction action, String sql, String identifier, String logicalGroupName, Result result) throws NumberFormatException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, identifier);
            stmt.setString(2, logicalGroupName);
            stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            logToAuditTrail(username, action, identifier, logicalGroupName);
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to update logical group '"+logicalGroupName+"'. Please try again.");
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

    private void logToAuditTrail(String username, AuditTrailProcessor.UserAction action, String identifier, String logicalGroupName) {
        if (action == AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP_USERS) {
            AuditTrailProcessor.log(username, action, "Logical group '" + logicalGroupName + "' with user '" + identifier + "' is added");
        } else if (action == AuditTrailProcessor.UserAction.REMOVE_LOGICAL_GROUP_USERS) {
            AuditTrailProcessor.log(username, action, "Logical group '" + logicalGroupName + "' with user '" + identifier + "' is removed");
        } if (action == AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP_ROUTES) {
            AuditTrailProcessor.log(username, action, "Logical group '" + logicalGroupName + "' with route '" + identifier + "' is added");
        } if (action == AuditTrailProcessor.UserAction.REMOVE_LOGICAL_GROUP_ROUTES) {
            AuditTrailProcessor.log(username, action, "Logical group '" + logicalGroupName + "' with route '" + identifier + "' is removed");
        } if (action == AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP_SPANS) {
            AuditTrailProcessor.log(username, action, "Logical group '" + logicalGroupName + "' with span '" + identifier + "' is added");
        } if (action == AuditTrailProcessor.UserAction.REMOVE_LOGICAL_GROUP_SPANS) {
            AuditTrailProcessor.log(username, action, "Logical group '" + logicalGroupName + "' with span '" + identifier + "' is removed");
        } if (action == AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP_DETECTORS) {
            AuditTrailProcessor.log(username, action, "Logical group '" + logicalGroupName + "' with detector id '" + identifier + "' is added");
        } if (action == AuditTrailProcessor.UserAction.REMOVE_LOGICAL_GROUP_DETECTORS) {
            AuditTrailProcessor.log(username, action, "Logical group '" + logicalGroupName + "' with detector id '" + identifier + "' is removed");
        } 
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void updateLogicalGroup(String username, String column, String value, String logicalGroupName, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            
            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_LOGICAL_GROUP, column,
                    (new HashMap.SimpleEntry<String, String>("logical_group_name", logicalGroupName)));
            
            stmt = connection.prepareStatement(String.format(UPDATE_LOGICAL_GROUP_SQL, column));
            stmt.setString(1, value);
            stmt.setString(2, logicalGroupName);
            stmt.executeUpdate();
            result.setSuccess(true);
            
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_LOGICAL_GROUP, 
                    "Logical group '" + logicalGroupName + "' is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to update logical group '"+logicalGroupName+"'. Please try again.");
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
}
