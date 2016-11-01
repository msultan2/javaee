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
import org.apache.log4j.Logger;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.AuditTrailProcessor;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author nthompson
 */
public class RouteManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(RouteManager.class);
    private String INSERT_ROUTE_SQL = "INSERT INTO route (route_name, description) VALUES(?,?);";
    private String INSERT_ROUTE_LOGICAL_GROUP_SQL = "INSERT INTO route_logical_group (route_name, logical_group_name) VALUES (?,?);";
    private String UPDATE_ROUTE_SQL = "UPDATE route SET %s = ? WHERE route_name = ?;";
    private String DELETE_ROUTE_SQL = "DELETE FROM route WHERE route_name = ?;";
    private String INSERT_ROUTE_RELATIONSHIP = "INSERT INTO route_span"
            + " (span_name, route_name)"
            + " VALUES (?, ?);";
    private String DELETE_ROUTE_RELATIONSHIP = "DELETE FROM route_span"
            + " WHERE span_name = ? AND route_name = ?;";

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
        String routeName = request.getParameter("route_name");
        String username = request.getUserPrincipal().getName();
        if (action.equalsIgnoreCase("add_span")) {
            String identifier = request.getParameter("span");
            updateRouteRelationship(username, INSERT_ROUTE_RELATIONSHIP, identifier, routeName, result);
        } else if (action.equalsIgnoreCase("remove_span")) {
            String identifier = request.getParameter("span");
            updateRouteRelationship(username, DELETE_ROUTE_RELATIONSHIP, identifier, routeName, result);
        } else if (action.equalsIgnoreCase("insert")) {
            String name = request.getParameter("route_name");
            String description = request.getParameter("description");
            String[] logicalGroups = request.getParameterValues("logical_group_names");
            if (logicalGroups == null) {
                result.setSuccess(false);
                result.setMessage("At least one logical group must be specified.");
            } else {
                insertInToRouteTable(username, name, description, logicalGroups, result);
            }
        } else if (action.equalsIgnoreCase("update")) {
            String value = request.getParameter("value");
            String column = request.getParameter("column");
            if (column.equalsIgnoreCase("0")) {
                updateRoute(username, "route_name", value, routeName, result);
            } else if (column.equalsIgnoreCase("1")) {
                updateRoute(username, "description", value, routeName, result);
            }
        } else if (action.equalsIgnoreCase("delete")) {
            deleteRoute(username, request.getParameter("route_name"), result);
        }

        try{
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    private void updateRouteRelationship(String username, String sql, String identifier, String routeName, Result result) throws NumberFormatException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, identifier);
            stmt.setString(2, routeName);
            stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            if (sql.indexOf("DELETE") == -1) {
                AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_SPAN_TO_ROUTE, 
                        "Route '" + routeName + "' with the span '" + identifier + "' is added");
            } else {
                AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_SPAN_FROM_ROUTE, 
                        "Route '" + routeName + "' with the span '" + identifier + "' is removed");
            }
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to add/remove route '"+routeName+"' with the span '"+identifier+"'. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
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

    private void insertInToRouteTable(String username, String name, String description, String[] logicalGroups, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(INSERT_ROUTE_SQL);
            stmt.setString(1, name.trim());
            stmt.setString(2, description);
            stmt.executeUpdate();
            insertRouteLogicalGroup(username, connection, name, logicalGroups, result);
            connection.commit();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_ROUTE, "Route '" + name + "' is added");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to add route '"+name+"'. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        }  finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    ;
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
                    ;
                }
                connection = null;
            }
        }
    }

    private void insertRouteLogicalGroup(String username, Connection connection, String id, String[] logicalGroups, Result result) throws SQLException {
        PreparedStatement stmt = null;

        String insertDetectorLogicalGroups = "";

        for (int i = 0; i < logicalGroups.length; i++) {
            insertDetectorLogicalGroups += INSERT_ROUTE_LOGICAL_GROUP_SQL;
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
        lgDescription = lgDescription.substring(0, lgDescription.length()-1);
        lgDescription += ")";
        stmt.executeUpdate();
        // log the user action
        AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_ROUTE_LOGICAL_GROUP, "Route '" + id + "' with the logical groups " + lgDescription + " is added");
    }

    private void updateRoute(String username, String column, String value, String name, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            
            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_ROUTE, column,
                    (new HashMap.SimpleEntry<String, String>("route_name", name)));
            
            stmt = connection.prepareStatement(String.format(UPDATE_ROUTE_SQL, column));
            stmt.setString(1, value.trim());
            stmt.setString(2, name);
            stmt.executeUpdate();
            result.setSuccess(true);
            
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_ROUTE, 
                    "Route '" + name + "' is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value.trim() + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to update route '"+name+"'. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        }  finally {
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

    private void deleteRoute(String username, String id, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_ROUTE_SQL);
            stmt.setString(1, id);
            stmt.executeUpdate();
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_ROUTE, "Route '" + id + "' is removed");
        } catch (SQLException ex) {
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        }  finally {
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
}
