/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.update;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.realm.RealmBase;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.JsonResponseProcessor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.email.Email;
import ssl.bluetruth.utils.AuditTrailProcessor;
import ssl.bluetruth.utils.PasswordGenerator;

/**
 *
 * @author svenkataramanappa
 */
public class ActivationManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(ActivationManager.class);
    private static String EMAIL = "java:comp/env/mail/mailSession";
    private static String CHECK_USER_ACTIVATION_SQL = "SELECT activated FROM instation_user WHERE activation_key = ?;";
    private static String ACTIVATE_USER_SQL = "UPDATE instation_user SET activated = TRUE WHERE activation_key = ?;";
    private static String GET_USER_EMAIL_SQL = "SELECT email_address FROM instation_user WHERE username = ?;";
    private static String UPDATE_USER_PASSWORD_SQL = "UPDATE instation_user SET md5_password = ?, "
            + "last_password_update_timestamp = NULL WHERE username = ?;";
    private static String FETCH_USER_BRAND_WEBSITE = "SELECT branding.website_address "
            + "FROM instation_user, branding "
            + "WHERE instation_user.username = ? AND instation_user.brand = branding.brand; ";

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
        if (action == null) {
            response.setStatus(404);
        } else {
            if (action.equalsIgnoreCase("activate")) {
                String activationKey = request.getParameter("act_key");
                activateUser(result, activationKey);
            } else if (action.equalsIgnoreCase("forgotPassword")) {
                recoverPassword(result, request.getParameter("username"), request.getParameter("email"));
            } else {
                result.setMessage("Error activating user");
            }
            JsonResponseProcessor jrp = new JsonResponseProcessor();
            try {
                jrp.createResponse(result, response.getWriter());
            } catch (Exception ex) {
                response.setStatus(500);
                LOGGER.warn("Could not process request", ex);
            }
        }
    } 

    private void recoverPassword(Result result, String username, String email) {

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String userEmail = null;
        boolean success = false;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(GET_USER_EMAIL_SQL);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (rs.next()) {
                userEmail = rs.getString(1);
                if (userEmail.equalsIgnoreCase(email)) {
                    String password = PasswordGenerator.generate(8);
                    if (updateInstationUserTable(connection, username, password)) {
                        String website = fetchWebsite(username);
                        Email.sendEmailWithoutAttachment(
                                new String[]{userEmail},
                                "Password recovery",
                                getMessageBody(username, password, website), website);
                        success = true;
                    }
                } else {
                    result.setMessage("Entered email address doesn't match with the users registered email address");
                }
            } else {
                result.setMessage("Invalid username");
            }
        } catch (UnsupportedEncodingException ex) {
            LOGGER.fatal("Encoding exception", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("SQL query could not execute", ex);
        } catch (SQLException ex) {
            result.setMessage("Unable to recover user password"); 
            LOGGER.info("SQL query could not execute", ex);
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

        result.setSuccess(success);
    }

    private boolean updateInstationUserTable(Connection connection, String username, String newPassword) {

        boolean success = false;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(UPDATE_USER_PASSWORD_SQL);
            stmt.setString(1, RealmBase.Digest(newPassword, "MD5", null));
            stmt.setString(2, username);
            stmt.executeUpdate();
            success = true;

            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_USER,
                        "User '" + username + "' password is changed");
        } catch (SQLException ex) {
             LOGGER.fatal("Unable to execute sql query", ex);
        } finally {
            return success;
        }
    }

    private String getMessageBody(String username, String password, String website) {

        return "To initiate the password reset process for your account, please login with the following credentials: \n\n"
                + website + "\n\n"
                + "Username: " + username + "\n"
                + "Password: " + password + "\n\n"
                + "This is an automated email as a result of user forgotten password mechanism, please do not reply to this email address.\n\nThank you.";
    }

    private String fetchWebsite(String username) {

        String website = fetchUserBrandWebsite(username);
        if (website == null) {
            try {
                Context initCtx = new InitialContext();
                Session session = (Session) initCtx.lookup(EMAIL);
                website = session.getProperty("mail.smtp.website");
            } catch (NamingException ex) {
                LOGGER.fatal("Naming exception", ex);
            }
        }
        return website;
    }

    private String fetchUserBrandWebsite(String username){

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String website = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(FETCH_USER_BRAND_WEBSITE);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if(rs.next()){
                website = rs.getString(1);
            }
        } catch (NamingException ex) {
            LOGGER.fatal("Naming exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("SQL query could not execute", ex);
        } catch (SQLException ex) {
            LOGGER.info("SQL query could not execute", ex);
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
            return website;
        }
    }

    private void activateUser(Result result, String activationKey) {
        Connection connection = null;
        PreparedStatement stmt = null;
        int rowsAffected = 0;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            checkIsUserActive(result, connection, activationKey);
            if (result.isSuccess()) {
                if (!Boolean.parseBoolean(result.getData().toString())) {
                    stmt = connection.prepareStatement(ACTIVATE_USER_SQL);
                    stmt.setString(1, activationKey);
                    rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 1) {
                        result.setSuccess(true);
                        result.setMessage("User activated successsfully");
                    } else {
                        result.setSuccess(false);
                    }
                } else {
                    result.setMessage("User already activated");
                }
            }
        } catch (NamingException ex) {
            LOGGER.fatal("Naming exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("SQL query could not execute", ex);
        } catch (SQLException ex) {
            result.setMessage("Unable to activate User");
            LOGGER.info("SQL query could not execute", ex);
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

    private void checkIsUserActive(Result result, Connection connection, String activationKey){
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(CHECK_USER_ACTIVATION_SQL);
            stmt.setString(1, activationKey);
            rs = stmt.executeQuery();
            if(rs.next()) {
                result.setData(rs.getBoolean(1));
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setSuccess(false);
            LOGGER.fatal("SQL query could not execute", ex);
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
