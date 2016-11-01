////////////////////////////////////////////////////////////////////////////////
// 
//  THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS 
//  LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND, 
//  EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, 
//  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN 
//  INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS 
//  OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL, 
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE 
//  POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO, 
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
//  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
//  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
// 
//  Copyright 2016 (C) Costain Integrated Technology Solutions Limited. 
//  All Rights Reserved.
// 
//  Product: BlueTruthAdministrator/597
//  Author: nchavan
//  Description: Insert/update or delete outstation default configuration values
//
////////////////////////////////////////////////////////////////////////////////

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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.AuditTrailProcessor;
import ssl.bluetruth.utils.JsonResponseProcessor;

public class DetectorDefaultConfigurationManager extends HttpServlet {
    
    private static final Logger LOGGER = LogManager.getLogger(DetectorDefaultConfigurationManager.class);

    private final String INSERT_DETECTOR_DEFAULT_CONFIGURATION_SQL = "INSERT INTO default_configuration (property, value) VALUES (?,?);";
    private final String UPDATE_DETECTOR_DEFAULT_CONFIGURATION_SQL = "UPDATE default_configuration SET %s = ? WHERE property = ?;";
    private final String DELETE_DETECTOR_DEFAULT_CONFIGURATION_SQL = "DELETE FROM default_configuration WHERE property = ?;";
    
    /**
     * Processes requests for both HTTP <code>GET</code> and  <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Result result = new Result();
        String action = request.getParameter("action");
        String property = request.getParameter("property");
        String value = request.getParameter("value");
        String username = request.getUserPrincipal().getName();
        
         if (action.equalsIgnoreCase("insert")) {
             insertIntoDetectorDefaultConfiguration(username, property, value, result);
        } else if (action.equalsIgnoreCase("update")) {
             updateDetectorDefaultConfiguration(username, property, "value", value, result);
        } else if (action.equalsIgnoreCase("delete")) {
             deleteDetectorDefaultConfiguration(username, property, value, result);
        }
         
        try {
            JsonResponseProcessor jrp = new JsonResponseProcessor();
            jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }
    
    private void insertIntoDetectorDefaultConfiguration(String username, String property, String value, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;        
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(INSERT_DETECTOR_DEFAULT_CONFIGURATION_SQL);
            stmt.setString(1, property);
            stmt.setString(2, value);
            stmt.executeUpdate();
            connection.commit();
            result.setSuccess(true);            
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_DEFAULT_DETECTOR_CONFIGURATION, "Default detector configuration '" + property + " , value = " + value + "' is added");
        } catch (SQLException ex) {
            if(connection != null) {
                try{
                    connection.rollback();
                } catch (SQLException e){
                }
            }
            result.setSuccess(false);
            result.setMessage("Unable to add new detector default property " + property + ". Please try again.");
            LOGGER.info("Unable to add detector property " + property + " as " + ex.getMessage());
         } catch (NamingException ex) {
            LOGGER.fatal(ex.getMessage());
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager "+ ex.getMessage());
        } finally {
            if(stmt != null){
                try{
                    stmt.close();
                } catch (SQLException ex){
                }
                stmt = null;
            }
            if(connection != null){
                try{
                    connection.setAutoCommit(true);
                } catch (SQLException ex){
                }
                try{
                    connection.close();
                } catch (SQLException ex){
                }
                connection = null;
            }            
        }        
    }
    
    private void updateDetectorDefaultConfiguration(String username, String property, String column, String value, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;        
        try{
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_DEFAULT_DETECTOR_CONFIGURATION, column,
                    (new HashMap.SimpleEntry<String, String>("property", property)));
            stmt = connection.prepareStatement(String.format(UPDATE_DETECTOR_DEFAULT_CONFIGURATION_SQL, column));
            stmt.setString(1, value);
            stmt.setString(2, property);
            stmt.executeUpdate();
            result.setSuccess(true);
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_DEFAULT_DETECTOR_CONFIGURATION,
                    "Default detector configuration key " + property + " is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex){
            LOGGER.info(ex.getMessage());
            result.setSuccess(false);
        } catch (DatabaseManagerException ex){
            LOGGER.info(ex.getMessage());
            result.setSuccess(false);
        } catch (NamingException ex){
            LOGGER.info(ex.getMessage());
            result.setSuccess(false);
        }
    }
    
    private void deleteDetectorDefaultConfiguration(String username, String property, String value, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            DatabaseManager dm;
            dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_DETECTOR_DEFAULT_CONFIGURATION_SQL);
            stmt.setString(1, property);
            stmt.executeUpdate();
            result.setSuccess(true);   
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_DEFAULT_DETECTOR_CONFIGURATION,
                    "Default detector configuration with key '" + property + "' and value '" + value + "'is removed");
        } catch (SQLException e) {
            result.setSuccess(false);
            result.setMessage("Unable to delete default detector configuration property is " + property);
        } catch (DatabaseManagerException ex) {            
            LOGGER.fatal("Naming Exception", ex);
        } catch (NamingException ex) {
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
