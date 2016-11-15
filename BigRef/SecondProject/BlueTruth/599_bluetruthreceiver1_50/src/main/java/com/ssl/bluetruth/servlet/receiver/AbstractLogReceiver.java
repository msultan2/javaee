package com.ssl.bluetruth.servlet.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.servlet.receiver.AbstractUnconfiguredDetector;

/**
 *
 * @author nthompson
 */
public abstract class AbstractLogReceiver extends HttpServlet {

    protected final Logger LOGGER = LogManager.getLogger(getClass());
    protected static final String SQL_INSERT_INTO_DETECTOR_LOG = "INSERT INTO detector_log "
            + "(detector_id, log_text)"
            + " VALUES (?,?);";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Pattern p = Pattern.compile("^.*Log/(.+)$");
        Matcher m = p.matcher(request.getRequestURI());
        if (m.matches()) {
            String logFilename = m.group(1);
            InputStream is = request.getInputStream();
            String logFileText = "";
            try {
                logFileText = new java.util.Scanner(is).useDelimiter("\\A").next();
            } catch (java.util.NoSuchElementException e) {
            } finally {
                is.close();
            }
            String detectorId = logFilename.split("_")[0];
            DatabaseManager dm;
            try {
                dm = DatabaseManager.getInstance();
                insertLog(dm.getDatasource().getConnection(), detectorId, logFileText);
            } catch (DatabaseManagerException ex) {
                LOGGER.warn("Database manager instance could not be retrieved: "
                        + request.getRemoteAddr(), ex);
            } catch (NamingException ex) {
                LOGGER.warn("Naming exception while adding log text: "
                        + request.getRemoteAddr(), ex);
            } catch (SQLException ex){
                LOGGER.warn("SQL exception while adding log text: "
                        + request.getRemoteAddr(), ex);
            }

        } else {
            LOGGER.warn("A log file was uploaded without a filename given from remote address: "
                    + request.getRemoteAddr());
            return;
        }
    }

    private void insertLog(Connection connection, String detectorId, String logFileText) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(SQL_INSERT_INTO_DETECTOR_LOG);
            ps.setString(1, detectorId);
            ps.setString(2, logFileText);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            // if there is an SQL exception, it means the detector could be not configured
            AbstractUnconfiguredDetector unconfiguredDetector = new AbstractUnconfiguredDetector();
            if (unconfiguredDetector.checkDetectorConfiguredInDatabase(detectorId)) {
                // log an exception if it is configured in database but throw an SQL exception
                LOGGER.warn("An exception occurred while inserting data into detector_log with DETECTOR ID:" + detectorId, ex);
            } else {
                // insert/update unconfigured detector
                unconfiguredDetector.insertUnconfiguredDetector(detectorId, AbstractUnconfiguredDetector.UnconfiguredType.LAST_LOG_UPLOAD);
            }
        } finally {
            if (ps != null) {
                ps.close();
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
     * Handles the HTTP <code>PUT</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Bluetruth LogReceiver";
    }// </editor-fold>
}
