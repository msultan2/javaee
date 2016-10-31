/*
 * ChartImageServlet.java
 * 
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Java version: JDK 1.7
 *
 * Created on 17-Jun-2015 01:15 PM
 * 
 */
package ssl.bluetruth.servlet.chart;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;

/**
 * View Chart Image
 * @author svenkataramanappa
 */
public class ChartImageServlet extends HttpServlet {

    private static final String IN_PROGRESS = "In progress";
    private static final String COMPLETE = "Complete";
    private static final String NOT_FOUND = "Not found";
    
    private static final String SELECT_CHART_IMAGE = "SELECT chart_image_data FROM chart WHERE id = ?;";
    private static final String CHECK_CHART_COMPLETE = "SELECT complete FROM chart WHERE id = ?;";
    
    private static final Logger LOGGER = LogManager.getLogger(ChartImageServlet.class);
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer chartId = Integer.parseInt(request.getParameter("id"));
        String action = request.getParameter("action");
        
        switch (action) {
            case "status":
                String status = chartStatus(chartId);
                if (status.equals(NOT_FOUND)) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.setContentType("text/plain");
                    response.getWriter().write(status);
                }
                return;

            case "image":
                response.setContentType("image/png");
                response.setCharacterEncoding("UTF-8");

                byte[] b = new byte[]{};
                b = getImageBytes(chartId);

                if (b == null) {
                    response.setContentLength(0);
                } else {
                    response.setContentLength(b.length);
                    response.getOutputStream().write(b);
                }

            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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

    private byte[] getImageBytes(Integer chartId) {
        
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        byte[] b = null;
        try {
            connection = DatabaseManager.getInstance().getDatasource().getConnection();
            ps = connection.prepareStatement(SELECT_CHART_IMAGE);
            ps.setInt(1, chartId);
            rs = ps.executeQuery();
            if (rs.next()) {
                b = rs.getBytes(1);
            }
        } catch (SQLException | DatabaseManagerException | NamingException ex) {
            LOGGER.error("Exception occured while getting chart image from the database" + ex);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close resultset");
            }
            
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close prepared statement");
            }
            
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close database connection");
            }
        }
        return b;
    }

    private String chartStatus(Integer chartId) {
        
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = NOT_FOUND;
        try {
            connection = DatabaseManager.getInstance().getDatasource().getConnection();
            ps = connection.prepareStatement(CHECK_CHART_COMPLETE);
            ps.setInt(1, chartId);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = (rs.getBoolean(1) == false) ? IN_PROGRESS : COMPLETE;
            }
            return result;
        } catch (SQLException | DatabaseManagerException | NamingException ex) {
            LOGGER.error("Exception occured while getting chart status from the database" + ex);
            return result;
        } finally {
            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close resultset");
            }

            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close prepared statement");
            }

            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close database connection");
            }
        }
    }
}
