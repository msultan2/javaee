/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.servlet.update;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author wingc
 */
public class AuditTrailActionManager extends HttpServlet {
    
    private static final org.apache.log4j.Logger LOGGER = LogManager.getLogger(AuditTrailActionManager.class);
    
    private static final String SQL_GET_AUDIT_TRAIL_ACTION = "SELECT * FROM audit_trail_action";
            
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
        Result result = new Result();
        boolean processOk = false;
        
        String action = request.getParameter("action");
        if (action.equalsIgnoreCase("get")) {
            processOk = getAuditTrailAction(result);
        }
        
        // create JSON response
        result.setSuccess(processOk);
        JsonResponseProcessor processor = new JsonResponseProcessor();
        processor.createResponse(result, response.getWriter());
    }
    
    private boolean getAuditTrailAction(Result result) {
        boolean processOk = false;
        DatabaseManager dm;
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            // get database manager
            dm = DatabaseManager.getInstance();
            // get database connection
            connection = dm.getDatasource().getConnection();
            // prepare the detector SQL statement
            ps = connection.prepareStatement(SQL_GET_AUDIT_TRAIL_ACTION);
            // execute SQL query
            ResultSet rs = ps.executeQuery();
            List data = new ArrayList();
            while (rs.next()) {
                data.add(rs.getString(1));
            }
            result.setData(data);
            // update process ok
            processOk = true;
        } catch (DatabaseManagerException ex) {
            LOGGER.warn("Database manager instance could not be retrieved", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming exception while retriving audit_trail_action", ex);
        } catch (SQLException ex) {
            result.setMessage("Unable to retrieve audit_trail_action");
            LOGGER.warn("SQL exception while retriving audit_trail_action", ex);
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
