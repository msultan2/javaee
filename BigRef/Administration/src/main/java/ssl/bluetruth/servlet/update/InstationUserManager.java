package ssl.bluetruth.servlet.update;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.catalina.realm.RealmBase;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.email.Email;
import ssl.bluetruth.utils.AuditTrailProcessor;
import ssl.bluetruth.utils.JsonResponseProcessor;
import ssl.bluetruth.utils.PasswordGenerator;

/**
 *
 * @author nthompson, svenkataramanappa
 */
public class InstationUserManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(InstationUserManager.class);
    private static String EMAIL = "java:comp/env/mail/mailSession";

    // SCJS 015 ADDED one more column 'brand'
    private String INSERT_USER_SQL = "INSERT INTO instation_user (full_name, username, md5_password, brand, timezone_name, email_address, activation_key) "
            + "VALUES (?,?,?,?,?,?,?);";
    // SCJA 015 END
    private String INSERT_USER_ROLE_SQL = "INSERT INTO instation_user_role (username, role_name) VALUES (?,?);";
    private String INSERT_USER_LOGICAL_GROUP_SQL = "INSERT INTO instation_user_logical_group (username, logical_group_name) VALUES (?,?);";
    private String UPDATE_USER_SQL = "UPDATE instation_user SET %s = ? WHERE username = ?;";
    private String DELETE_USER_SQL = "DELETE FROM instation_user WHERE username = ?;";
    private String FETCH_USER_SQL = "SELECT instation_user.full_name, instation_user.username, "
            + "instation_user.timezone_name, instation_user.email_address "
            + "FROM instation_user "
            + "WHERE instation_user.username = ?;";
    // SCJS 015 ADDED a column by name brand
    private String FETCH_USER_WITH_BRAND_SQL = "SELECT instation_user.full_name, instation_user.username, "
            + "(CASE WHEN instation_user.brand IS NOT NULL THEN instation_user.brand "
            + "ELSE '----' END) as brand, "
            + "instation_user.timezone_name, instation_user.email_address "
            + "FROM instation_user "
            + "WHERE instation_user.username = ?;";
    // SCJS 015 END
    private String FETCH_USER_INSTATION_ADMIN_SQL = "SELECT instation_user.full_name, instation_user.username, "
            + "instation_user.timezone_name, instation_user.email_address, instation_user.expiry_days, "
            + "(expiry_days-extract(epoch from (NOW() - last_password_update_timestamp))/(3600*24)) as remaining_days "
            + "FROM instation_user "
            + "WHERE instation_user.username = ?;";
    private String FETCH_USER_BRAND_INSTATION_ADMIN_SQL = "SELECT instation_user.full_name, instation_user.username, "
            + "instation_user.timezone_name, instation_user.email_address, instation_user.expiry_days, "
            + "(CASE WHEN instation_user.brand IS NOT NULL THEN instation_user.brand "
            + "ELSE '----' END) as brand, "
            + "(expiry_days-extract(epoch from (NOW() - last_password_update_timestamp))/(3600*24)) as remaining_days "
            + "FROM instation_user "
            + "WHERE instation_user.username = ?;";
    private String LIST_ROLE_SQL = "SELECT instation_user_role.role_name "
            + "FROM instation_user_role "
            + "WHERE instation_user_role.username = '%s' "
            + "ORDER BY role_name DESC;";
    private String LIST_LOGICAL_GROUP_SQL = "SELECT instation_user_logical_group.logical_group_name "
            + "FROM instation_user_logical_group "
            + "WHERE instation_user_logical_group.username = '%s' "
            + "ORDER BY logical_group_name DESC;";
    private String LIST_TIME_ZONES_SQL = "SELECT instation_user_timezone.timezone_name "
            + "FROM instation_user_timezone "
            + "ORDER BY instation_user_timezone.timezone_name ASC;";
    // SCJS 015 START
    private String LIST_BRANDS_SQL = "SELECT brand "
            + "FROM branding "
            + "ORDER BY brand ASC;";
    // SCJS 015 END
    private String CHANGE_TIMEZONE_SQL = "UPDATE instation_user SET timezone_name = ? "
            + "WHERE instation_user.username = ?;";
    // SCJS 015 START
    private String CHANGE_BRAND_SQL = "UPDATE instation_user SET brand = ? "
            + "WHERE instation_user.username = ?;";
    private String FETCH_BRAND_SQL = "SELECT brand FROM instation_user WHERE username = '%s'";
    private String SQL_ACTIVATE_USER = "UPDATE instation_user SET activated = ? WHERE username = ?;";
    // SCJS 015 END
    private String CHANGE_PASSWORD_EXPIRY_DAYS_SQL = "UPDATE instation_user SET expiry_days = ? WHERE username = ?;";
    private static String FETCH_USER_BRAND_WEBSITE = "SELECT branding.website_address "
            + "FROM instation_user, branding "
            + "WHERE instation_user.username = ? AND instation_user.brand = branding.brand; ";
    private static final String FETCH_USER_BRANDING_URL_SQL = "SELECT css_url FROM branding WHERE brand = ?;";
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // SCJS 015 START
        String creator = request.getUserPrincipal().getName();
        // SCJS 015 END
        Result result = new Result();
        String action = request.getParameter("action");
        String instationUsername = request.getParameter("username");
        if (action.equalsIgnoreCase("insert")) {
            insertInstationUser(request, instationUsername, creator, result);
        } else if (action.equalsIgnoreCase("update")) {
            updateInstationUser(request, instationUsername, creator, result);
        } else if (action.equalsIgnoreCase("delete")) {
            deleteInstationUser(instationUsername, creator, result);
        } else if (action.equalsIgnoreCase("user")) {
            fetchInstationUser(instationUsername, result, request.isUserInRole("brand_administration"), request.isUserInRole("instation_administration"));
        } else if (action.equalsIgnoreCase("timezone")) {
            String timezone = request.getParameter("timezone_name");
            updateTimezone(result, instationUsername, timezone, creator);
            //Updating current user session data.
            if(creator.equals(instationUsername)){
                request.getSession().setAttribute("timezone_name", timezone);
            }
        } 
        // SCJS 015 START
        else if(action.equalsIgnoreCase("brand")){
            String brand = request.getParameter("brand");
            updateBrand(result, instationUsername, brand, creator);
            
            if(result.isSuccess()){
                //Fetch new Branding CSS URL.
                String brandUrl= fetchUserBrandingCss(brand);
                if(brandUrl!=null){
                    //Updating current user session data.
                    if(creator.equals(instationUsername)){
                        request.getSession().setAttribute("css_url", brandUrl);
                    }
                }
            }    
        }
        
        // SCJS 015 END
        else if (action.equalsIgnoreCase("activate_user")) {
            activateInstationUser(instationUsername, true, creator, result);
        }
        else if (action.equalsIgnoreCase("deactivate_user")) {
            activateInstationUser(instationUsername, false, creator, result);
        } else if (action.equalsIgnoreCase("expiry_days")) {
            int expiryDays= Integer.parseInt(request.getParameter("expiry_days"));
            updateExpiryDays(result, instationUsername, expiryDays, creator);
        }
        
        try{
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }

    }
    
    /**
     * Get Branding URL.
     * @param brandname
     * @return 
     */
    private String fetchUserBrandingCss(String brandname) {

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String brand = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(FETCH_USER_BRANDING_URL_SQL);
            stmt.setString(1, brandname);
            rs = stmt.executeQuery();
            if (rs.next()) {
                brand = rs.getString(1);
            }
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
            return brand;
        }
    }

    private void updateExpiryDays(Result result, String username, int expiryDays, String creator) {
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

            AuditTrailProcessor.log(creator, AuditTrailProcessor.UserAction.UPDATE_USER_PASSWORD_EXPIRY_DAYS,
                    "User '" + username + "' password expiry days is updated: The column 'expiry_days' is changed FROM '" + prevResult + "' TO '" + expiryDays + "'");

        } catch (SQLException ex) {
            result.setMessage("User '"+username+"' is unable to update password expiry days.");
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
    
    private void updateInstationUser(HttpServletRequest request, String id, String creator, Result result) throws NumberFormatException {
        String value = request.getParameter("value");
        String column = request.getParameter("column");
        if (column.equalsIgnoreCase("0")) {
            updateInstationUserTable("full_name", value, id, creator, result);
        } else if (column.equalsIgnoreCase("1")) {
            updateInstationUserTable("username", value, id, creator, result);
        } else if (column.equalsIgnoreCase("2")) {
            updateInstationUserTable("email_address", value, id, creator, result);
        } 
    }

    // SCJS 015 Added argument creator
    private void insertInstationUser(HttpServletRequest request, String username, String creator, Result result) throws NumberFormatException {
        String name = request.getParameter("full_name");
        String email = request.getParameter("email");
        String[] roles = request.getParameterValues("role_names");
        String[] logicalGroups = request.getParameterValues("logical_group_names");
        String timezone = (String)request.getSession().getAttribute("user_timezone");
        if (logicalGroups == null) {
            result.setSuccess(false);
            result.setMessage("At least one logical group must be specified.");
        }
        else {
            String activationKey = getUserActivationKey(email);
            String password = PasswordGenerator.generate(8);
            insertInToInstationUserTable(name, username, password, email, activationKey, roles, logicalGroups, timezone, creator, result);
            if (result.isSuccess()) {
                try {
                    String website = fetchWebsite(username);
                    Email.sendEmailWithoutAttachment(new String[]{email}, "Activate your account", getMessageBody(username, password, activationKey, website), website);
                } catch (UnsupportedEncodingException ex) {
                    LOGGER.fatal("Encoding exception", ex);
                }
            }
        }
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

    private String getUserActivationKey(String email) {
        return Integer.toString((new Timestamp((new Date()).getTime())).hashCode()) + Integer.toString(email.hashCode());
    }

    private String getMessageBody(String username, String password, String activationKey, String website){

        return "You have been given a user login to "+website+"\n\n" +
               "Username: " + username + "\n" +
               "Password: " + password + "\n\n" +
               "Your account is currently inactive. You cannot use it until you visit the following link:\n\n" +
                website + "/Activation?action=activate&act_key="+ activationKey + "\n\n" +
               "This is an automated email, please do not reply to this email address.\n\n" +
               "Thank you.";
    }

    private void insertInToInstationUserTable(String name, String username, String password, String email, String activationKey,
            String[] roles, String[] logicalGroups, String timezone, String creator, Result result) {

        Connection connection = null;
        PreparedStatement stmt = null;
        String brand;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            // SCJS 015 START - NEW METHOD
            brand = fetchCreatorBrand(connection, creator);
            // SCJS 015 END
            stmt = connection.prepareStatement(INSERT_USER_SQL);
            stmt.setString(1, name);
            stmt.setString(2, username.trim());
            stmt.setString(3, RealmBase.Digest(password, "MD5", null));
            stmt.setString(4, brand);
            stmt.setString(5, timezone);
            stmt.setString(6, email);
            stmt.setString(7, activationKey);
            stmt.executeUpdate();
            insertInstationUserRole(connection, username, roles, creator);
            insertInstationUserLogicalGroup(connection, username, logicalGroups, creator);
            connection.commit();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(creator, AuditTrailProcessor.UserAction.ADD_USER, "User '" + username + "' with full name '" + name + "' is created");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                }
            }
            result.setMessage("Unable to create user '"+ username+"' as it already exists, Please try again.");
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

    // SCJS 015 START
    private String fetchCreatorBrand(Connection connection, String creator){

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(String.format(FETCH_BRAND_SQL, creator));
            rs = stmt.executeQuery();
            List<String> list = new ArrayList<String>();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            return list.get(0);
        } catch (SQLException ex) {
            LOGGER.info("SQL query could not execute", ex);
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
            return null;
        } 
    }
    // SCJS 015 END

    private void insertInstationUserRole(Connection connection, String username, String[] roles, String creator) throws SQLException {
        PreparedStatement stmt = null;

        String insertDetectorLogicalGroups = "";

        for (int i = 0; i < roles.length; i++) {
            insertDetectorLogicalGroups += INSERT_USER_ROLE_SQL;
        }
        stmt = connection.prepareStatement(insertDetectorLogicalGroups);
        int index = 1;
        String roleDescription = "(";
        for (int i = 0; i < roles.length; i++) {
            stmt.setString(index, username);
            index++;
            stmt.setString(index, roles[i]);
            index++;
            roleDescription += roles[i] + ",";
        }
        roleDescription = roleDescription.substring(0, roleDescription.length()-1);
        roleDescription += ")";
        stmt.executeUpdate();
        
        // log the user action
        AuditTrailProcessor.log(creator, AuditTrailProcessor.UserAction.ADD_ROLE_TO_USER, "User '" + username + "' with the roles " + roleDescription + " is created");
    }

    private void insertInstationUserLogicalGroup(Connection connection, String username, String[] logicalGroups, String creator) throws SQLException {
        PreparedStatement stmt = null;

        String insertDetectorLogicalGroups = "";

        for (int i = 0; i < logicalGroups.length; i++) {
            insertDetectorLogicalGroups += INSERT_USER_LOGICAL_GROUP_SQL;
        }
        stmt = connection.prepareStatement(insertDetectorLogicalGroups);
        int index = 1;
        String lgDescription = "(";
        for (int i = 0; i < logicalGroups.length; i++) {
            stmt.setString(index, username);
            index++;
            stmt.setString(index, logicalGroups[i]);
            index++;
            lgDescription += logicalGroups[i] + ",";
        }
        lgDescription = lgDescription.substring(0, lgDescription.length()-1);
        lgDescription += ")";
        stmt.executeUpdate();
        
        // log the user action
        AuditTrailProcessor.log(creator, AuditTrailProcessor.UserAction.ADD_LOGICAL_GROUP_USERS, "User '" + username + "' with the logical groups " + lgDescription + " is created");
    }

    private void updateInstationUserTable(String column, String value, String id, String creator, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            
            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_USER, column,
                    (new HashMap.SimpleEntry<String, String>("username", id)));
            
            stmt = connection.prepareStatement(String.format(UPDATE_USER_SQL, column));
            stmt.setString(1, value.trim());
            stmt.setString(2, id);
            stmt.executeUpdate();
            result.setSuccess(true);
            
            // log the user action
            AuditTrailProcessor.log(creator, AuditTrailProcessor.UserAction.UPDATE_USER, "User '" + id + "' is updated: The column '" + column + " is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to update instation user '" + id + "' information. Please try again.");
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

    private void deleteInstationUser(String id, String creator, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_USER_SQL);
            stmt.setString(1, id);
            stmt.execute();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(creator, AuditTrailProcessor.UserAction.REMOVE_USER, "User '" + id + "' is removed");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("Unable to delete instation user '" + id + "'. Please try again.");
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

    private void fetchInstationUser(String instationUsername, Result result, boolean brandAdministrator, boolean isInstationAdmin) {

        Map userData = new HashMap();        
        Result roleResult = new Result();
        listRoles(roleResult, instationUsername);        
        userData.put("roles", roleResult.getData());

        Result logicalGroupResult = new Result();
        listLogicalGroups(logicalGroupResult, instationUsername);
        userData.put("logical_groups", logicalGroupResult.getData());

        Result timeZoneResult = new Result();
        listTimeZones(timeZoneResult);
        userData.put("time_zones", timeZoneResult.getData());

        String user_detail_sql = FETCH_USER_SQL;

        if (brandAdministrator && isInstationAdmin) {
            Result brands = new Result();
            listBrands(brands);
            userData.put("brands", brands.getData());
            user_detail_sql = FETCH_USER_BRAND_INSTATION_ADMIN_SQL;
        } else if (brandAdministrator && !isInstationAdmin) {
            Result brands = new Result();
            listBrands(brands);
            userData.put("brands", brands.getData());
            user_detail_sql = FETCH_USER_WITH_BRAND_SQL;
        } else if (!brandAdministrator && isInstationAdmin) {
            user_detail_sql = FETCH_USER_INSTATION_ADMIN_SQL;
        }
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(user_detail_sql);
            stmt.setString(1, instationUsername);
            rs = stmt.executeQuery();
            Map map = new HashMap();
            ResultSetMetaData rsmd = rs.getMetaData();
            if(rs.next()){
                Integer columns = rsmd.getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    map.put(rsmd.getColumnName(i), rs.getObject(i));
                }
            }
            userData.put("user_details", map);
            result.setData(userData);
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage("Unable to retreive instation user '" + instationUsername + "' information. Please try again.");
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

    // SCJS 015 START
    private void listBrands(Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(LIST_BRANDS_SQL);
            rs = stmt.executeQuery();
            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            result.setData(list);
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage("Unable to retreive list of brands. Please try again.");
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
    // SCJS 015 END
    
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
            result.setMessage("Unable to retreive list of time zones. Please try again.");
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
            result.setMessage("Unable to retreive list of logical groups information. Please try again.");
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
            result.setMessage("Unable to retreive list of roles for user '"+username+"'. Please try again.");
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

    private void updateTimezone(Result result, String username, String timezone, String creator) {
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
            
            // log the user action
            AuditTrailProcessor.log(creator, AuditTrailProcessor.UserAction.UPDATE_USER, 
                    "User '" + username + "' is updated: The column 'timezone_name' is changed FROM '" + prevResult + "' TO '" + timezone + "'");
        } catch (SQLException ex) {
            result.setMessage("Unable to update user '"+ username+"' information. Please try agan.");
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

    // SCJS 015 START
    private void updateBrand(Result result, String username, String brand, String creator) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            
            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_USER, "brand",
                    (new HashMap.SimpleEntry<String, String>("username", username)));
            
            stmt = connection.prepareStatement(CHANGE_BRAND_SQL);
            stmt.setString(1, brand);
            stmt.setString(2, username);
            stmt.executeUpdate();
            result.setSuccess(true);
            
            // log the user action
            AuditTrailProcessor.log(creator, AuditTrailProcessor.UserAction.UPDATE_USER, 
                    "User '" + username + "' is updated: The column 'brand' is changed FROM '" + prevResult + "' TO '" + brand + "'");
        } catch (SQLException ex) {
            result.setMessage("User '" + username + "' is unable to update brand '" + brand + "'. Please try again.");
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
    // SCJS 015 END

    private void activateInstationUser(String instationUsername, boolean activated, String creator, Result result) {
        if ((instationUsername != null) && (creator != null)) {
            DatabaseManager dm;
            Connection connection = null;
            PreparedStatement ps = null;

            try {
                // get database manager
                dm = DatabaseManager.getInstance();
                // get database connection
                connection = dm.getDatasource().getConnection();
                // prepare the detector SQL statement
                ps = connection.prepareStatement(SQL_ACTIVATE_USER);
                ps.setBoolean(1, activated);
                ps.setString(2, instationUsername);
                // execute SQL query
                ps.executeUpdate();
                result.setSuccess(true);
                // log to audit trail
                AuditTrailProcessor.log(creator, activated ? AuditTrailProcessor.UserAction.ACTIVATE_USER : AuditTrailProcessor.UserAction.DEACTIVATE_USER, 
                        "User '" + instationUsername + "' account " + (activated ? "activated" : "deactivated"));
            } catch (DatabaseManagerException ex) {
                LOGGER.warn("Database manager instance could not be retrieved", ex);
            } catch (NamingException ex) {
                LOGGER.warn("Naming exception while update brand_contact_details", ex);
            } catch (SQLException ex) {
                result.setMessage("Unable to activate/inactive user '"+instationUsername+"'. Please try again.");
                LOGGER.warn("SQL exception while update brand_contact_details", ex);
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
