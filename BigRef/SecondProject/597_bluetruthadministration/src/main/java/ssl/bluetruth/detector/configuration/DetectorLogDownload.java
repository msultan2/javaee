package ssl.bluetruth.detector.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class DetectorLogDownload extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(DetectorLogDownload.class);
    private final String RETRIEVE_DETECTOR_LOG_DATA_STATEMENT = "SELECT detector_log_id, detector_id, uploaded_timestamp, log_text "
            + "FROM detector_log "
            + "WHERE detector_log_id = ?;";
    private final String DETECTOR_LOG_ID = "detector_log_id";

    private String detectorId = "";
    private String logText = "";
    private String timestamp = "";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            getLogFileDownloadData(request.getParameter(DETECTOR_LOG_ID));
        } catch (SQLException ex) {
            LOGGER.warn("SQL Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.warn("Database Manager Exception", ex);
        } catch (NamingException ex) {
            LOGGER.warn("Naming Exception", ex);
        }

        detectorId = detectorId.replace("/", "").replace("\\", "").replace(".", "").replace(":", "").replace(" ", "_");
        timestamp = timestamp.substring(0,timestamp.indexOf(".")).replace("-", "").replace(":", "").replace(" ", "_");

        String filename = "LOG_" + detectorId + "_" + timestamp + ".txt";

        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        response.setHeader("Content-Type", "text/plain; charset=utf-8");

        InputStream responseData = new ByteArrayInputStream(logText.getBytes("UTF-8"));

        byte[] buf = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = responseData.read(buf)) > 0) {
            response.getOutputStream().write(buf, 0, bytesRead);
        }

    }

    private void getLogFileDownloadData(String detectorLogId)
            throws SQLException, DatabaseManagerException, NamingException {

        DatabaseManager dm = DatabaseManager.getInstance();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {

            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(RETRIEVE_DETECTOR_LOG_DATA_STATEMENT);
            stmt.setInt(1, Integer.parseInt(detectorLogId));

            rs = stmt.executeQuery();

            if (rs.next()) {
                detectorId = rs.getString("detector_id");
                logText = rs.getString("log_text");
                timestamp = rs.getTimestamp("uploaded_timestamp").toString();
            } else {
                throw new IllegalArgumentException("No data found for the specified detector log ID");
            }
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
