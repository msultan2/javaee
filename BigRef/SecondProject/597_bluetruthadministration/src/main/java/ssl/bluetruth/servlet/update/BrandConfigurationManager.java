/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.servlet.update;

import java.io.IOException;
import java.net.URLDecoder;
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
 * @author wingc
 */
public class BrandConfigurationManager extends HttpServlet {
    
    public enum BrandConfigurationColumn {
        BRAND("brand", 0),
        TITLE("title", 1),
        CONTACT("contact", 2),
        DESCRIPTION("description", 3),
        CONTACT_METHOD("contact_method", 4);
        
        public final String descriptiveName;
        public final int index;
        
        private BrandConfigurationColumn(String descriptiveName, int index) {
            this.descriptiveName = descriptiveName;
            this.index = index;
        }
    }
    
    private static final Logger LOGGER = LogManager.getLogger(BrandConfigurationManager.class);
    
    private static final String SQL_INSERT_INTO_BRAND_CONTACT_DETAILS = "INSERT INTO branding_contact_details " 
            + "(brand,title,contact,description,contact_method) " 
            + "VALUES(?,?,?,?,?)";
    private static final String SQL_DELETE_BRAND_CONTACT_DETAILS = "DELETE FROM branding_contact_details " 
            + "WHERE brand=? AND title=? AND contact_method=?";
    
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
        // process request
        Result result = new Result();
        boolean processOk = false;
        String action = request.getParameter("action");
        String username = request.getUserPrincipal().getName();
        
        // decode brand name to deal with special characters
        String brandName = request.getParameter("brand_name");
        if (brandName != null) {
            brandName = URLDecoder.decode(brandName,"UTF-8");
        }
        
        if (action.equalsIgnoreCase("insert")) {
            processOk = insertBrandContactDetails(
                    username,
                    brandName,
                    request.getParameter("title"),
                    request.getParameter("contact"),
                    request.getParameter("description"),
                    request.getParameter("contact_method"),
                    false,
                    result);
        }
        else if (action.equalsIgnoreCase("update")) {
            processOk = updateBrandContactDetails(
                    username,
                    brandName,
                    request.getParameter("title"),
                    request.getParameter("contact_method"),
                    request.getParameter("column"),
                    request.getParameter("value"),
                    false,
                    result);
        } else if (action.equalsIgnoreCase("delete")) {
            processOk = deleteBrandContactDetails(
                    username,
                    brandName,
                    request.getParameter("title"),
                    request.getParameter("contact_method"),
                    false,
                    result);
        }
        else if (action.equalsIgnoreCase("insert-own-brand")) {
            processOk = insertBrandContactDetails(
                    username,
                    brandName,
                    request.getParameter("title"),
                    request.getParameter("contact"),
                    request.getParameter("description"),
                    request.getParameter("contact_method"),
                    true,
                    result);
        }
        else if (action.equalsIgnoreCase("update-own-brand")) {
            processOk = updateBrandContactDetails(
                    username,
                    brandName,
                    request.getParameter("title"),
                    request.getParameter("contact_method"),
                    request.getParameter("column"),
                    request.getParameter("value"),
                    true,
                    result);
        } else if (action.equalsIgnoreCase("delete-own-brand")) {
            processOk = deleteBrandContactDetails(
                    username,
                    brandName,
                    request.getParameter("title"),
                    request.getParameter("contact_method"),
                    true,
                    result);
        }
        
        // create JSON response
        result.setSuccess(processOk);
        JsonResponseProcessor processor = new JsonResponseProcessor();
        processor.createResponse(result, response.getWriter());
    }

    private boolean insertBrandContactDetails(
            String username,
            String brandName, 
            String title, 
            String contact, 
            String description, 
            String contactMethod,
            boolean ownBrand,
            Result result) {
        boolean processOk = false;
        
        if ((username != null) && (title != null) && (contact != null) && (description != null) && (contactMethod != null)) {
            DatabaseManager dm;
            Connection connection = null;
            PreparedStatement ps = null;

            try {
                // get database manager
                dm = DatabaseManager.getInstance();
                // get database connection
                connection = dm.getDatasource().getConnection();
                
                if (ownBrand) {
                    brandName = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.QUERY_USER_BRAND, "brand", 
                            (new HashMap.SimpleEntry<String, String>("username", username)));
                }
                
                // prepare the detector SQL statement
                ps = connection.prepareStatement(SQL_INSERT_INTO_BRAND_CONTACT_DETAILS);
                ps.setString(1, brandName);
                ps.setString(2, title);
                ps.setString(3, contact);
                ps.setString(4, description);
                ps.setString(5, contactMethod);
                // execute SQL query
                ps.executeUpdate();
                // update process ok
                processOk = true;
                // log to audit trail
                AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_BRAND_CONTACT_DETAILS, 
                        "Brand '" + brandName + "' with contact details '" + title + "', contact_method '" + contactMethod + "' is added");
            } catch (DatabaseManagerException ex) {
                LOGGER.warn("Database manager instance could not be retrieved", ex);
            } catch (NamingException ex) {
                LOGGER.warn("Naming exception while update brand_contact_details", ex);
            } catch (SQLException ex) {
                result.setMessage("Brand '" + brandName+"' with contact details '"+contact +"' already exists. Please try again.");
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
        return processOk;
    }
    
    private boolean updateBrandContactDetails(
            String username,
            String brandName, 
            String title,
            String contactMethod,
            String columnIndex,
            String value,
            boolean ownBrand,
            Result result) {
        boolean processOk = false;
        
        if ((username != null) && (title != null) && (contactMethod != null) && (columnIndex != null) && (value != null)) {
            DatabaseManager dm;
            Connection connection = null;
            PreparedStatement ps = null;

            try {
                // get database manager
                dm = DatabaseManager.getInstance();
                // get database connection
                connection = dm.getDatasource().getConnection();
                
                // prepare the detector SQL statement
                int dbIndex = Integer.parseInt(columnIndex) + 1;
                String SQL_UPDATE_BRAND_CONTACT_DETAILS =  "UPDATE branding_contact_details SET "
                        + BrandConfigurationColumn.values()[dbIndex].descriptiveName
                        + "=? WHERE "
                        + BrandConfigurationColumn.BRAND.descriptiveName
                        + "=? AND "
                        + BrandConfigurationColumn.TITLE.descriptiveName
                        + "=? AND "
                        + BrandConfigurationColumn.CONTACT_METHOD.descriptiveName
                        + "=?";
                
                if (ownBrand) {
                    brandName = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.QUERY_USER_BRAND, "brand",
                            (new HashMap.SimpleEntry<String, String>("username", username)));
                }
                
                // get the query result before updating to the database
                String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_BRAND_CONTACT_DETAILS, 
                        BrandConfigurationColumn.values()[dbIndex].descriptiveName,
                        (new HashMap.SimpleEntry<String, String>(BrandConfigurationColumn.BRAND.descriptiveName, brandName)),
                        (new HashMap.SimpleEntry<String, String>(BrandConfigurationColumn.TITLE.descriptiveName, title)),
                        (new HashMap.SimpleEntry<String, String>(BrandConfigurationColumn.CONTACT_METHOD.descriptiveName, contactMethod)));
                
                ps = connection.prepareStatement(SQL_UPDATE_BRAND_CONTACT_DETAILS);
                ps.setString(1, value);
                ps.setString(2, brandName);
                ps.setString(3, title);
                ps.setString(4, contactMethod);
                // execute SQL query
                ps.executeUpdate();
                // update process ok
                processOk = true;
                
                // log the user action
                AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_BRAND_CONTACT_DETAILS, 
                        "Brand '" + brandName + "' with contact details '" + title + "', contact_method '" + contactMethod + "' is updated: The column '" 
                        + BrandConfigurationColumn.values()[dbIndex].descriptiveName + "' is changed FROM '" + prevResult + "' TO '" + value + "'");
            } catch (DatabaseManagerException ex) {
                LOGGER.warn("Database manager instance could not be retrieved", ex);
            } catch (NamingException ex) {
                LOGGER.warn("Naming exception while update brand_contact_details", ex);
            } catch (SQLException ex) {
                
                result.setMessage("Unable to update brand contact details. Please try again.");
                
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
        return processOk;
    }
    
    private boolean deleteBrandContactDetails(
            String username,
            String brandName, 
            String title,
            String contactMethod,
            boolean ownBrand,
            Result result) {
        boolean processOk = false;
        
        if ((username != null) && (title != null) && (contactMethod != null)) {
            DatabaseManager dm;
            Connection connection = null;
            PreparedStatement ps = null;

            try {
                // get database manager
                dm = DatabaseManager.getInstance();
                // get database connection
                connection = dm.getDatasource().getConnection();
                
                if (ownBrand) {
                    brandName = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.QUERY_USER_BRAND, "brand",
                            (new HashMap.SimpleEntry<String, String>("username", username)));
                }
                
                // prepare the detector SQL statement
                ps = connection.prepareStatement(SQL_DELETE_BRAND_CONTACT_DETAILS);
                ps.setString(1, brandName);
                ps.setString(2, title);
                ps.setString(3, contactMethod);
                // execute SQL query
                ps.executeUpdate();
                // update process ok
                processOk = true;
                // log to audit trail
                AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_BRAND_CONTACT_DETAILS, 
                        "Brand '" + brandName + "' with contact details '" + title + "', contact_method '" + contactMethod + "' is removed");
            } catch (DatabaseManagerException ex) {
                LOGGER.warn("Database manager instance could not be retrieved", ex);
            } catch (NamingException ex) {
                LOGGER.warn("Naming exception while update brand_contact_details", ex);
            } catch (SQLException ex) {
                result.setMessage("Unable to delete brand '" + brandName+"' contact details. Please try again.");
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
        return processOk;
    }
    
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
}
