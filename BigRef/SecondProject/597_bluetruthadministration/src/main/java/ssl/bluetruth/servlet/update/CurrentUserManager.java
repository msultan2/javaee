package ssl.bluetruth.servlet.update;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.catalina.realm.RealmBase;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.email.Email;
import ssl.bluetruth.utils.AuditTrailProcessor;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author nthompson
 */
public class CurrentUserManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(CurrentUserManager.class);
    private static String EMAIL = "java:comp/env/mail/mailSession";
    private String FETCH_USER_SQL = "SELECT instation_user.full_name, instation_user.username, "
            + "instation_user.timezone_name, instation_user.email_address "
            + "FROM instation_user "
            + "WHERE instation_user.username = ?;";
    private String FETCH_USER_INSTATION_ADMIN_SQL = "SELECT instation_user.full_name, instation_user.username, "
            + "instation_user.timezone_name, instation_user.email_address, instation_user.expiry_days, "
            + "(expiry_days-extract(epoch from (NOW() - last_password_update_timestamp))/(3600*24)) as remaining_days "
            + "FROM instation_user "
            + "WHERE instation_user.username = ?;";
    private String CHANGE_PASSWORD_SQL = "UPDATE instation_user SET md5_password= ?, last_password_update_timestamp = NOW() "
            + "WHERE md5_password = ? "
            + "AND instation_user.username = ?;";
    private String CHANGE_TIMEZONE_SQL = "UPDATE instation_user SET timezone_name= ? "
            + "WHERE instation_user.username = ?;";
    private String LIST_ROLE_SQL = "SELECT instation_user_role.role_name "
            + "FROM instation_user_role "
            + "WHERE instation_user_role.username = '%s' "
            + "ORDER BY role_name ASC;";
    private String LIST_LOGICAL_GROUP_SQL = "SELECT instation_user_logical_group.logical_group_name "
            + "FROM instation_user_logical_group "
            + "WHERE instation_user_logical_group.username = '%s' "
            + "ORDER BY logical_group_name ASC;";
    private String LIST_TIME_ZONES_SQL = "SELECT instation_user_timezone.timezone_name "
            + "FROM instation_user_timezone "
            + "ORDER BY instation_user_timezone.timezone_name ASC;";
    private String CHANGE_PASSWORD_EXPIRY_DAYS_SQL = "UPDATE instation_user SET expiry_days = ? WHERE username = ?;";
    private String FETCH_USER_EMAIL_SQL = "SELECT email_address FROM instation_user WHERE username = ?;";
    private String FETCH_USER_BRAND_SQL = "SELECT brand FROM instation_user WHERE username = ?;";
    private String FETCH_BRAND_CONTACT_DETAILS = "SELECT contact_method, title, contact, description FROM branding_contact_details "
            + "WHERE brand = ? "
            + "ORDER BY contact_method;";
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

        String username = request.getUserPrincipal().getName();
        Result result = new Result();
        String action = request.getParameter("action");
        if (action.equalsIgnoreCase("user")) {
            fetchUser(result, username, request.isUserInRole("instation_administration"));
        } else if (action.equalsIgnoreCase("visible_logical_groups")) {
            listLogicalGroups(result, username);
        } else if (action.equalsIgnoreCase("visible_roles")) {
            listRoles(result, username);
        } else if (action.equalsIgnoreCase("password")) {
            String newPassword = request.getParameter("password");
            if (newPassword.equals(request.getParameter("password_confirm"))) {
                String oldPassword = request.getParameter("password_current");
                updatePassword(result, username, newPassword, oldPassword, action);
            } else {
                result.setMessage("Password and confirmation did not match.");
            }
        } else if (action.equalsIgnoreCase("password_expire")) {
            String newPassword = request.getParameter("password");
            if (newPassword.equals(request.getParameter("password_confirm"))) {
                String oldPassword = request.getParameter("password_current");
                updatePassword(result, username, newPassword, oldPassword, action);
                HttpServletRequest hsr = (HttpServletRequest) request;
                HttpSession session = hsr.getSession(true);
                session.setAttribute("active", true);
            } else {
                result.setMessage("Password and confirmation did not match.");
            }
        } else if (action.equalsIgnoreCase("timezone")) {
            String timezone = request.getParameter("timezone_name");
            updateTimezone(result, username, timezone);
            if (result.isSuccess()) {
                HttpServletRequest hsr = (HttpServletRequest) request;
                HttpSession session = hsr.getSession(true);
                session.setAttribute("user_timezone", timezone);
            }
        } else if (action.equalsIgnoreCase("expiry_days")) {
            int expiryDays = Integer.parseInt(request.getParameter("expiry_days"));
            updateExpiryDays(result, username, expiryDays);
        }
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        try {
            jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    private void updateExpiryDays(Result result, String username, int expiryDays) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean success = false;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(CHANGE_PASSWORD_EXPIRY_DAYS_SQL);
            stmt.setInt(1, expiryDays);
            stmt.setString(2, username);

            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_USER_PASSWORD_EXPIRY_DAYS, "expiry_days",
                    (new HashMap.SimpleEntry<String, String>("username", username)));

            stmt.executeUpdate();
            success = true;

            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_USER_PASSWORD_EXPIRY_DAYS,
                    "User '" + username + "' password expiry days is updated: The column 'expiry_days' is changed FROM '" + prevResult + "' TO '" + expiryDays + "'");

        } catch (SQLException ex) {
            result.setMessage("Unable to update password expiry days for user '" + username+"'");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
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
        result.setSuccess(success);
    }

    private void fetchUser(Result result, String username, boolean isInstationAdmin) {

        Map userData = new HashMap();
        Result roleResult = new Result();
        listRoles(roleResult, username);
        userData.put("roles", roleResult.getData());

        Result logicalGroupResult = new Result();
        listLogicalGroups(logicalGroupResult, username);
        userData.put("logical_groups", logicalGroupResult.getData());

        Result timeZoneResult = new Result();
        listTimeZones(timeZoneResult);
        userData.put("time_zones", timeZoneResult.getData());

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(isInstationAdmin ? FETCH_USER_INSTATION_ADMIN_SQL : FETCH_USER_SQL);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            Map map = new HashMap();
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rs.next()) {
                Integer columns = rsmd.getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    map.put(rsmd.getColumnName(i), rs.getObject(i));
                }
            }
            userData.put("user_details", map);
            result.setData(userData);
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage("Unable to retreive user '"+username+"' details.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
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

    private void listTimeZones(Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(LIST_TIME_ZONES_SQL);
            rs = stmt.executeQuery();
            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            result.setData(list);
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage("Unable to retreive timezones.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
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

    private void listLogicalGroups(Result result, String username) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(String.format(LIST_LOGICAL_GROUP_SQL, username));
            rs = stmt.executeQuery();
            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            result.setData(list);
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage("Unable to retreive list of logical groups.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
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

    private void listRoles(Result result, String username) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(String.format(LIST_ROLE_SQL, username));
            rs = stmt.executeQuery();
            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            result.setData(list);
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage("Unable to retreive list of user '"+username+ "' roles");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
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

    private void updatePassword(Result result, String username, String newPassword, String oldPassword, String action) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(CHANGE_PASSWORD_SQL);
            stmt.setString(1, RealmBase.Digest(newPassword, "MD5", null));
            stmt.setString(2, RealmBase.Digest(oldPassword, "MD5", null));
            stmt.setString(3, username);
            if (stmt.executeUpdate() > 0) {
                result.setSuccess(true);
                if (sendEmail(result, username, connection, action)) {
                    connection.commit();
                    AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_USER,
                            "User '" + username + "' password is changed");
                } else {
                    connection.rollback();
                }
            } else {
                result.setMessage("Current password incorrect.");
            }
        } catch (SQLException ex) {
            result.setMessage("Unable to update user '"+username+"' password.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
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
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    private boolean sendEmail(Result result, String username, Connection connection, String action) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String website = null;
        boolean success = false;
        try {
            stmt = connection.prepareStatement(FETCH_USER_EMAIL_SQL);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (rs.next()) {
                website = fetchWebsite(username);
                if (action.equalsIgnoreCase("password")) {
                    try {
                        Email.sendEmailWithoutAttachment(new String[]{rs.getString(1)}, "Password changed", getMessageBody(username, result, website), website);
                    } catch (UnsupportedEncodingException ex) {
                        LOGGER.fatal("Encoding exception", ex);
                    }
                } else {
                    try {
                        Email.sendEmailWithoutAttachment(new String[]{rs.getString(1)}, "Password changed", getMessageBodyPasswordExpired(username, result, website), website);
                    } catch (UnsupportedEncodingException ex) {
                        LOGGER.fatal("Encoding exception", ex);
                    }
                }
                success = true;
            }
        } catch (SQLException ex) {
            result.setMessage("User '"+username+ "' is unable to send email.");
            LOGGER.info("SQL query could not execute", ex);
        }
        return success;
    }

    private String getMessageBodyPasswordExpired(String username, Result result, String website) {

        String administratorContact = fetchAdministratorDetails(username, result);
        return "Your user account with username: "
                + username + " has been reset, please login with your updated login credentials: \n\n"
                + website + "\n\n"
                + "If you are not expecting this email, Please contact your administrator \n\n"
                + administratorContact
                + "This is an automated email sent as a result of user password expire mechanism/ password forgotten mechanism, please do not reply to this email address.\n\n"
                + "Thank you.";
    }

    private String getMessageBody(String username, Result result, String website) {

        String administratorContact = fetchAdministratorDetails(username, result);
        return "Your user account with username: "
                + username + " has been reset, please login with your updated login credentials: \n\n"
                + website + "\n\n"
                + "If you are not expecting this email, Please contact your administrator \n\n"
                + administratorContact
                + "This is an automated email sent as a result of user change password mechanism, please do not reply to this email address.\n\n"
                + "Thank you.";
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

    private String fetchAdministratorDetails(String username, Result result) {

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String brand = null;
        String administratorContact = "";
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            brand = fetchUserBrand(connection, username, result);
            if (result.isSuccess()) {
                stmt = connection.prepareStatement(FETCH_BRAND_CONTACT_DETAILS);
                stmt.setString(1, brand);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    //contact_method, title, contact, description
                    administratorContact += rs.getString(1) + "\n" + rs.getString(2) + " - " + rs.getString(3) + "\n" + rs.getString(4) + "\n\n";
                }
            }
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } catch (SQLException ex) {
            LOGGER.info("SQL query could not execute", ex);
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
            return administratorContact;
        }
    }

    private String fetchUserBrand(Connection connection, String username, Result result) {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        String brand = null;
        boolean success = false;
        try {
            stmt = connection.prepareStatement(FETCH_USER_BRAND_SQL);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (rs.next()) {
                brand = rs.getString(1);
                success = true;
            }
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
            LOGGER.info("SQL query could not execute", ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            result.setSuccess(success);
            return brand;
        }
    }

    private void updateTimezone(Result result, String username, String timezone) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();

            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_USER, "timezone_name",
                    (new HashMap.SimpleEntry<String, String>("username", username)));

            stmt = connection.prepareStatement(CHANGE_TIMEZONE_SQL);
            stmt.setString(1, timezone);
            stmt.setString(2, username);
            stmt.executeUpdate();
            result.setSuccess(true);
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_USER,
                    "User '" + username + "' timezone is updated: The column 'timezone_name' is changed FROM '" + prevResult + "' TO '" + timezone + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '" + username + "' is not able to change timezone information. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
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
