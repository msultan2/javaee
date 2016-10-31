package ssl.bluetruth.servlet.update;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
 * SCJS 018
 * @author svenkataramanappa
 */
public class BroadcastMessageManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(BroadcastMessageManager.class);
    private String INSERT_BROADCAST_MESSAGE_SQL = "INSERT INTO broadcast_message (title, message) VALUES(?,?);";
    private String INSERT_BROADCAST_MESSAGE_LOGICAL_GROUP_SQL = "INSERT INTO broadcast_message_logical_group (message_id, logical_group) VALUES (?,?);";
    private String UPDATE_BROADCAST_MESSAGE_SQL = "UPDATE broadcast_message SET %s = ? WHERE message_id = ?;";
    private String DELETE_BROADCAST_MESSAGE_SQL = "DELETE FROM broadcast_message WHERE message_id = ?;";
    private String FETCH_TOP_MESSAGE_ID_SQL = "SELECT MAX(message_id) FROM broadcast_message;";

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
        String username = request.getUserPrincipal().getName();
        if (action.equalsIgnoreCase("insert")) {
            String title = request.getParameter("broadcast_name");
            String description = request.getParameter("description");
            String[] logicalGroups = request.getParameterValues("logical_group_names");
            if (logicalGroups == null) {
                result.setSuccess(false);
                result.setMessage("At least one logical group must be specified.");
            } else {
                insertInToBroadcastMessageTable(username, title, description, logicalGroups, result);
            }
        } else if (action.equalsIgnoreCase("update")) {
            Integer message_id = Integer.parseInt(request.getParameter("message_id"));
            String value = request.getParameter("value");
            String column = request.getParameter("column");
            if (column.equalsIgnoreCase("1")) {
                updateBroadcastMessage(username, "title", value, message_id, result);
            } else if (column.equalsIgnoreCase("2")) {
                updateBroadcastMessage(username, "message", value, message_id, result);
            }
        } else if (action.equalsIgnoreCase("delete")) {
            Integer id = Integer.parseInt(request.getParameter("message_id"));
            System.out.println(id);
            deleteBroadcastMessage(username, id, result);
        }

        try{
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        jrp.createResponse(result, response.getWriter());
        }
        catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    private void insertInToBroadcastMessageTable(String username, String title, String description, String[] logicalGroups, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(INSERT_BROADCAST_MESSAGE_SQL);
            stmt.setString(1, title.trim());
            stmt.setString(2, description);
            stmt.executeUpdate();
            Integer messageId = fetchTopMessageId(connection, result);
            if(result.isSuccess()){
            insertBroadcastMessageLogicalGroup(username, connection, messageId, logicalGroups, result);
            connection.commit();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_BROADCAST_MESSAGE, "Broadcast message '" + title + "' is added");
            }else{
                result.setSuccess(false);
                connection.rollback();
            }
        } catch (SQLException ex) {
            if (connection != null) {
                try {

                    connection.rollback();
                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("Broadcast message  '" + title + "' is not added, Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
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

    private void insertBroadcastMessageLogicalGroup(String username, Connection connection, Integer messageId, String[] logicalGroups, Result result) throws SQLException {
        PreparedStatement stmt = null;

        String insertDetectorLogicalGroups = "";

        for (int i = 0; i < logicalGroups.length; i++) {
            insertDetectorLogicalGroups += INSERT_BROADCAST_MESSAGE_LOGICAL_GROUP_SQL;
        }
        stmt = connection.prepareStatement(insertDetectorLogicalGroups);
        int index = 1;
        String lgDescription = "(";
        for (int i = 0; i < logicalGroups.length; i++) {
            stmt.setInt(index, messageId);
            index++;
            stmt.setString(index, logicalGroups[i]);
            index++;
            lgDescription += logicalGroups[i] + ",";
        }
        lgDescription = lgDescription.substring(0, lgDescription.length()-1);
        lgDescription += ")";
        stmt.executeUpdate();
        // log the user action
        AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_BROADCAST_MESSAGE_LOGICAL_GROUP, "Broadcast message id " + messageId.toString() + " with the logical groups " + lgDescription + " is added");
    }
   
    private void deleteBroadcastMessage(String username, Integer id, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_BROADCAST_MESSAGE_SQL);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_BROADCAST_MESSAGE, "Broadcast message id " + id.toString() + " is removed");
        } catch (SQLException ex) {
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
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

    private void updateBroadcastMessage(String username, String column, String value, Integer messageId, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            
            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_BROADCAST_MESSAGE, column,
                    (new HashMap.SimpleEntry<String, String>("message_id", messageId.toString())));
            
            stmt = connection.prepareStatement(String.format(UPDATE_BROADCAST_MESSAGE_SQL, column));
            stmt.setString(1, value.trim());
            stmt.setInt(2, messageId);
            stmt.executeUpdate();
            result.setSuccess(true);
            
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_BROADCAST_MESSAGE, 
                    "Broadcast message id '" + messageId + "' is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to update Broadcast message id '"+messageId+"'");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
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

    private Integer fetchTopMessageId(Connection connection, Result result){
       try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            stmt = connection.prepareStatement(FETCH_TOP_MESSAGE_ID_SQL);
            rs = stmt.executeQuery();
            Integer messageId = null;
            while (rs.next()) {
                messageId = rs.getInt(1);
            }
            result.setSuccess(true);
            return messageId;
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getMessage());
            result.setSuccess(false);
            return null;
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
