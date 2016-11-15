package com.ssl.bluetruth.servlet.receiver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.servlet.receiver.AbstractUnconfiguredDetector;

/**
 *
 * @author nthompson
 */
public abstract class AbstractMessageReceiver extends HttpServlet {

    protected final Logger LOGGER = LogManager.getLogger(getClass());
    protected static final String PARAMETER_DATE = "dt";
    protected static final String PARAMETER_DETECTOR_ID = "id";
    protected static final String PARAMETER_MESSAGE_CONTEXT = "m";
    protected static final String PARAMETER_MESSAGE_CODE_COUNT_PAIRS = "s";
    protected static final String CODE_COUNT_PAIR_DELIMITER = ",";
    protected static final String CODE_COUNT_DELIMITER = ":";
    protected static final String SQL_INSERT_INTO_DETECTOR_MESSAGE = "INSERT INTO detector_message "
            + "(detector_id, recorded_timestamp, code, count, category, description, description_detail)"
            + " VALUES (?,?,?,?,?,?,?);";
    protected static final String SYSTEM_MESSAGE_CONTEXT = "3";
    protected static final String UNKNOWN = "MESSAGE";
    protected static final String UNKNOWN_DESCRIPTION = "No associated message description for current version";
    protected static final String UNKNOWN_DESCRIPTION_DETAIL = "No associated message description for current version";

    public abstract Long parseDateString(String parameterDate) throws ParseException;

    public Timestamp getTimestampFromHTTPRequest(HttpServletRequest request) throws ParseException {

        String parameterDate = request.getParameter(PARAMETER_DATE);
        Long time = null;

        time = parseDateString(parameterDate);

        return new Timestamp(time);
    }

    public static long convertTimestampToMillis(String timestamp, String timestampFormat, String timeZone) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(timestampFormat);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        Date date = dateFormat.parse(timestamp);
        return date.getTime();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String detectorId = UNKNOWN;
        try {
            detectorId = getDetectorIdFromHTTPRequest(request);
            if (getMessageContextFromHTTPRequest(request).equals(SYSTEM_MESSAGE_CONTEXT)) {
                DatabaseManager dm = DatabaseManager.getInstance();
                Timestamp timestamp = getTimestampFromHTTPRequest(request);
                String[] codeCountPairs = getMessageCodeCountPairsFromHTTPRequest(request).split(CODE_COUNT_PAIR_DELIMITER);
                for (int i = 0; i < codeCountPairs.length; i++) {
                    String[] codeCount = codeCountPairs[i].split(CODE_COUNT_DELIMITER);
                    String code = codeCount[0];
                    String count = codeCount[1];
                    insertMessage(dm.getDatasource().getConnection(), 
                            detectorId, timestamp,
                            code, Integer.parseInt(count),
                            UNKNOWN, 
                            UNKNOWN_DESCRIPTION,
                            UNKNOWN_DESCRIPTION_DETAIL);
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("An exception occurred while processing message data received from "
                    + request.getRemoteAddr() + " DETECTOR ID:" + detectorId, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public String getDetectorIdFromHTTPRequest(HttpServletRequest request) {
        return request.getParameter(PARAMETER_DETECTOR_ID);
    }

    public String getMessageContextFromHTTPRequest(HttpServletRequest request) {
        return request.getParameter(PARAMETER_MESSAGE_CONTEXT);
    }

    public String getMessageCodeCountPairsFromHTTPRequest(HttpServletRequest request) {
        return request.getParameter(PARAMETER_MESSAGE_CODE_COUNT_PAIRS);
    }

    public void insertMessage(Connection connection, 
            String detectorId, Timestamp timestamp,
            String code, int count,
            String category, String description, String descriptionDetail) throws NamingException, SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(SQL_INSERT_INTO_DETECTOR_MESSAGE);
            ps.setString(1, detectorId);
            ps.setTimestamp(2, timestamp);
            ps.setString(3, code);
            ps.setInt(4, count);
            ps.setString(5, category);
            ps.setString(6, description);
            ps.setString(7, descriptionDetail);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            // if there is an SQL exception, it means the detector could be not configured
            AbstractUnconfiguredDetector unconfiguredDetector = new AbstractUnconfiguredDetector();
            if (unconfiguredDetector.checkDetectorConfiguredInDatabase(detectorId)) {
                // log an exception if it is configured in database but throw an SQL exception
                LOGGER.warn("An exception occurred while inserting data into detector_message with DETECTOR ID:" + detectorId, ex);
            } else {
                // insert/update unconfigured detector
                unconfiguredDetector.insertUnconfiguredDetector(detectorId, AbstractUnconfiguredDetector.UnconfiguredType.LAST_MESSAGE_REPORT);
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
        return "Bluetruth MessageReceiver";
    }// </editor-fold>
}
