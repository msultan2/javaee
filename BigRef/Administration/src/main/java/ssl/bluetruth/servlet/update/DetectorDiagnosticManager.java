package ssl.bluetruth.servlet.update;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
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
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author nthompson
 */
public class DetectorDiagnosticManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(DetectorDiagnosticManager.class);
    private static final String GET_DETECTOR_DIAGNOSTIC_SQL =
            "SELECT "
            + "detector.detector_name, "
            + "detector.detector_id, "
            + "last_device_detection.last_device_detection AT TIME ZONE '%s' AS last_device_detection, "
            + "last_occupancy.last_occupancy_report_timestamp AT TIME ZONE '%s' AS last_occupancy_report_timestamp, "
            + "last_configuration_download_request_timestamp AT TIME ZONE '%s' AS last_configuration_download_request_timestamp, "
            + "last_configuration_download_version, "
            + "NOW() AT TIME ZONE '%s' AS diagnostic_information_requested_timestamp, "
            + "total_occupancy.total_occupancy_reports, "
            + "total_device_detections.total_device_detections, "
            + "total_device_detections_last_5_minutes.total_device_detections_last_5_minutes "

            + "FROM detector "
            + "LEFT JOIN ( "
            + "SELECT detector_id, last_detection_timestamp AS last_device_detection "
            + "FROM detector_statistic "
            + "WHERE detector_statistic.detector_id=? "
            + ") last_device_detection ON detector.detector_id = last_device_detection.detector_id "
            
            + "LEFT JOIN ( "
            + "SELECT detector_id, reported_timestamp AS last_occupancy_report_timestamp "
            + "FROM occupancy "
            + "WHERE occupancy.detector_id=? "
            + "ORDER BY occupancy.occupancy_id DESC LIMIT 1 "
            + ") last_occupancy ON detector.detector_id = last_occupancy.detector_id "

            + "JOIN detector_statistic ON detector.detector_id = detector_statistic.detector_id "

            + "LEFT JOIN ( "
            + "SELECT detector_id, COUNT(detector_id) AS total_device_detections "
            + "FROM device_detection "
            + "WHERE device_detection.detector_id=? "
            + "GROUP BY detector_id "
            + ") total_device_detections ON detector.detector_id = total_device_detections.detector_id "

            + "LEFT JOIN ( "
            + "SELECT detector_id, COUNT(detector_id) AS total_device_detections_last_5_minutes "
            + "FROM device_detection "
            + "WHERE device_detection.detector_id=? "
            + "AND device_detection.detection_timestamp > NOW() - '00:05:00'::interval "
            + "GROUP BY detector_id "
            + ") total_device_detections_last_5_minutes ON detector.detector_id = total_device_detections_last_5_minutes.detector_id "


            + "LEFT JOIN ( "
            + "SELECT detector_id, COUNT(detector_id) AS total_occupancy_reports "
            + "FROM occupancy "
            + "WHERE occupancy.detector_id=? "
            + "GROUP BY detector_id "
            + ") total_occupancy ON detector.detector_id = total_occupancy.detector_id "

            + "JOIN detector_logical_group ON detector.detector_id = detector_logical_group.detector_id "
            + "JOIN instation_user_logical_group ON detector_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "JOIN instation_user ON instation_user.username = instation_user_logical_group.username AND instation_user.username = '%s' "

            + "WHERE detector.detector_id=? "

            + "GROUP BY "
            + "detector.detector_name, "
            + "detector.detector_id, "
            + "last_device_detection.last_device_detection, "
            + "last_occupancy.last_occupancy_report_timestamp, "
            + "last_configuration_download_request_timestamp, "
            + "last_configuration_download_version, "
            + "total_occupancy.total_occupancy_reports, "
            + "total_device_detections.total_device_detections, "
            + "total_device_detections_last_5_minutes.total_device_detections_last_5_minutes, "
            + "diagnostic_information_requested_timestamp;";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String timezone = (String)request.getSession().getAttribute("user_timezone");        
        if(timezone == null){
            timezone = "UTC";
        }
        
        String username = request.getUserPrincipal().getName();

        Result result = new Result();
        String action = request.getParameter("action");
        String id = request.getParameter("detector_id");
        if (action.equalsIgnoreCase("get")) {
            getDetectorDiagnosticInformation(id, username, timezone, result);
        }
        try{
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    private static void getDetectorDiagnosticInformation(String id, String username, String timezone, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(String.format(GET_DETECTOR_DIAGNOSTIC_SQL, timezone, timezone, timezone, timezone, username));
            stmt.setString(1, id);
            stmt.setString(2, id);
            stmt.setString(3, id);
            stmt.setString(4, id);
            stmt.setString(5, id);
            stmt.setString(6, id);
            rs = stmt.executeQuery();
            result.setData(resultSetToArrayList(rs));
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage("User '"+username+"' unable to retreive detector diagnostic information. Please try again.");
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

    public static List resultSetToArrayList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        ArrayList list = new ArrayList();
        while (rs.next()) {
            TreeMap row = new TreeMap();
            for (int i = 1; i <= columns; ++i) {
                Object o = rs.getObject(i);
                HashMap columnData = new HashMap(2);
                if (o != null) {
                    Class c = o.getClass();
                    String className = c.getName();
                    className = className.substring(className.lastIndexOf(".") + 1);
                    columnData.put("type", className);
                    if (o instanceof Timestamp) {
                        columnData.put("value", o.toString());
                    } else {
                        columnData.put("value", o);
                    }
                } else {
                    columnData.put("type", "null");
                    columnData.put("value", "null");
                }
                row.put(md.getColumnName(i), columnData);
            }
            list.add(row);
        }
        return list;
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
