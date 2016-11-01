package ssl.bluetruth.servlet.update;

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
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.JsonResponseProcessor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.utils.AuditTrailProcessor;

/**
 * SCJS 015 START
 * @author Santhosh
 */
public class BrandManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(BrandManager.class);

    private String INSERT_BRAND_SQL = "INSERT INTO branding (brand, css_url, website_address) VALUES(?,?,?);";
    private String UPDATE_BRAND_SQL = "UPDATE branding SET %s = ? WHERE brand = ?;";
    private String DELETE_BRAND_SQL = "DELETE FROM branding WHERE brand = ?;";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

            Result result = new Result();
            String action = request.getParameter("action");
            String brand = request.getParameter("brand_name");
            String username = request.getUserPrincipal().getName();
            if (action.equalsIgnoreCase("insert")){
                String cssUrl = request.getParameter("css_url");
                String website = request.getParameter("website");
                insertBrand(username, brand, cssUrl, website, result);
            } else if (action.equalsIgnoreCase("update")){
                String value = request.getParameter("value");
                String column = request.getParameter("column");
                if (column.equalsIgnoreCase("0")){
                    updateBrand(username, "brand", value, brand, result);
                } else if (column.equalsIgnoreCase("1")) {
                    updateBrand(username, "css_url", value, brand, result);
                } else if (column.equalsIgnoreCase("2")) {
                    updateBrand(username, "website_address", value, brand, result);
                }
            } else if (action.equalsIgnoreCase("delete")){
                deleteBrand(username, brand, result);
            }

        JsonResponseProcessor jrp = new JsonResponseProcessor();
        try {
            jrp.createResponse(result, response.getWriter());
        } catch (IOException ex) {
            LOGGER.fatal(ex.getMessage());
        }
    }

    private void insertBrand(String username, String brand, String cssUrl, String website, Result result) {

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(INSERT_BRAND_SQL);
            stmt.setString(1, brand.trim());
            stmt.setString(2, cssUrl);
            stmt.setString(3, website);
            stmt.executeUpdate();
            connection.commit();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_BRAND, "Brand '" + brand + "' is added");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();

                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("Unable to add brand '"+brand+"' as it already exists. Please try again.");
            LOGGER.info("Unable to add brand as "+ex.getMessage());
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

    private void updateBrand(String username, String column, String value, String brand, Result result){

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = null;
            try {
                dm = DatabaseManager.getInstance();
            } catch (DatabaseManagerException ex) {
                LOGGER.fatal(ex.getMessage());
            }
            try {
                connection = dm.getDatasource().getConnection();
            } catch (NamingException ex) {
                LOGGER.fatal(ex.getMessage());
            }
            
            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_BRAND, column,
                    (new HashMap.SimpleEntry<String, String>("brand", brand)));
            
            stmt = connection.prepareStatement(String.format(UPDATE_BRAND_SQL, column));
            stmt.setString(1, value);
            stmt.setString(2, brand);
            stmt.executeUpdate();
            result.setSuccess(true);
            
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_BRAND, 
                    "Brand '" + brand + "' is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex) {
            LOGGER.info(ex.getMessage());
            result.setSuccess(false);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
        }
    }

    private void deleteBrand(String username, String brand, Result result){
        
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_BRAND_SQL);
            stmt.setString(1, brand);
            stmt.executeUpdate();
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_BRAND, "Brand '" + brand + "' is removed");
        } catch (SQLException ex) {
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
            throws ServletException{
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
            throws ServletException{
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

// SCJS 015 END