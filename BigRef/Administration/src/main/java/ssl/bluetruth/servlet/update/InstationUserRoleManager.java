package ssl.bluetruth.servlet.update;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
public class InstationUserRoleManager extends HttpServlet {

   private static final org.apache.log4j.Logger LOGGER = LogManager.getLogger(SpanManager.class);
   private String INSERT_USER_ROLE_RELATIONSHIP = "INSERT INTO instation_user_role"
            + " (username, role_name)"
            + " VALUES (?, ?);";
    private String DELETE_USER_ROLE_RELATIONSHIP = "DELETE FROM instation_user_role"
            + " WHERE username = ? AND role_name = ?;";

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
        String instationUsername = request.getParameter("username");
        String roleName = request.getParameter("role_name");
        String username = request.getUserPrincipal().getName();

        if (action.equalsIgnoreCase("add")) {
            updateUserRoleRelationship(username, INSERT_USER_ROLE_RELATIONSHIP, instationUsername, roleName, result);
        } else if (action.equalsIgnoreCase("remove")) {
            updateUserRoleRelationship(username, DELETE_USER_ROLE_RELATIONSHIP, instationUsername, roleName, result);
        }
        try{
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    private void updateUserRoleRelationship(String username, String sql, String identifier, String roleName, Result result) throws NumberFormatException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, identifier);
            stmt.setString(2, roleName);
            stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            if (sql.indexOf("DELETE") == -1) {
                AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_ROLE_TO_USER, 
                        "User '" + username + "' with the role '" + roleName + "' is added");
            } else {
                AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_ROLE_FROM_USER, 
                        "User '" + username + "' with the role '" + roleName + "' is removed");
            }
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to  add/remove user '" + username + "' with the role '" + roleName+"'. Please try again.");
            LOGGER.info(ex.getMessage());
        } catch (NamingException ex) {
            LOGGER.fatal(ex.getMessage());
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal(ex.getMessage());
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
