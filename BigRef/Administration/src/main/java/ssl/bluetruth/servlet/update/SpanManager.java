package ssl.bluetruth.servlet.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.AuditTrailProcessor;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author nthompson, Santhosh
 */
public class SpanManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(SpanManager.class);
    private String INSERT_SPAN_SQL = "INSERT INTO span (span_name, start_detector_id, end_detector_id) VALUES(?,?,?);";
    private String INSERT_SPAN_LOGICAL_GROUP_SQL = "INSERT INTO span_logical_group (span_name, logical_group_name) VALUES (?,?);";
    private String UPDATE_SPAN_SQL = "UPDATE span SET %s = ? WHERE span_name = ?;";
    private String DELETE_SPAN_SQL = "DELETE FROM span WHERE span_name = ?;";
    private String FETCH_SPAN_START_END_DETECTOR_LOCATIONS_SQL =
            "SELECT d1.latitude || ',' || d1.longitude AS startLatLng , "
            + "d2.latitude || ',' || d2.longitude AS ENDLatLng "
            + "FROM detector AS d1, detector AS d2, span "
            + "WHERE span.span_name = ? AND span.start_detector_id = d1.detector_id AND span.end_detector_id = d2.detector_id;";
    private String UPDATE_SPAN_OSRM_SQL = "UPDATE span_osrm SET route_geometry = ? , total_distance = ? , total_time = ? WHERE span_name = ?;";
    private String UPDATE_SPAN_OSRM_TOTAL_DISTANCE_SQL = "UPDATE span_osrm SET total_distance = ? WHERE span_name = ?;";
    private String UPDATE_SPAN_SPEED_THRESHOLDS_SQL = "UPDATE span_speed_thresholds SET stationary = ? , very_slow = ? , slow = ? , moderate = ? WHERE span_name = ?;";
    private String UPDATE_SPAN_SINGLE_SPEED_THRESHOLD_SQL = "UPDATE span_speed_thresholds SET %s = ? WHERE span_name = ?;";

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
        String name = request.getParameter("span_name");
        String username = request.getUserPrincipal().getName();
        if (action.equalsIgnoreCase("insert")) {
            String startDetectorID = request.getParameter("start_detector_id");
            String endDetectorID = request.getParameter("end_detector_id");
            Integer stationary = Integer.parseInt(request.getParameter("stationary"));
            Integer verySlow = Integer.parseInt(request.getParameter("very_slow"));
            Integer slow = Integer.parseInt(request.getParameter("slow"));
            Integer moderate = Integer.parseInt(request.getParameter("moderate"));
            String[] logicalGroups = request.getParameterValues("logical_group_names");
            if (logicalGroups == null) {
                result.setSuccess(false);
                result.setMessage("At least one logical group must be specified.");
            } else {
                insertSpan(username, name, startDetectorID, endDetectorID, logicalGroups, result);
                if (result.isSuccess()) {
                    calculateAndUpdateOSRM(username, name, result);
                    updateSpanThresholdSpeed(username, name, stationary, verySlow, slow, moderate, result);
                }
            }
        } else if (action.equalsIgnoreCase("update")) {
            String value = request.getParameter("value");
            String column = request.getParameter("column");
            if (column.equalsIgnoreCase("0")) {
                updateSpan(username, "span_name", value, name, result);
            } else if (column.equalsIgnoreCase("1")) {
                updateSpan(username, "start_detector_id", value, name, result);
                if (result.isSuccess()) {
                    // SCJS 010
                    calculateAndUpdateOSRM(username, name, result);
                }
            } else if (column.equalsIgnoreCase("2")) {
                updateSpan(username, "end_detector_id", value, name, result);
                if (result.isSuccess()) {
                    // SCJS 010
                    calculateAndUpdateOSRM(username, name, result);
                }
                //SCJS 009 START
            } else if (column.equalsIgnoreCase("3")) {
                updateOSRMTotalDistance(username, name, Integer.parseInt(value), result);
            } else if (column.equalsIgnoreCase("4")) {
                updateSpanThresholdSpeed(username, "stationary", name, Integer.parseInt(value), result);
            } else if (column.equalsIgnoreCase("5")) {
                updateSpanThresholdSpeed(username, "very_slow", name, Integer.parseInt(value), result);
            } else if (column.equalsIgnoreCase("6")) {
                updateSpanThresholdSpeed(username, "slow", name, Integer.parseInt(value), result);
            } else if (column.equalsIgnoreCase("7")) {
                updateSpanThresholdSpeed(username, "moderate", name, Integer.parseInt(value), result);
            }
            //SCJS 009 END
        } else if (action.equalsIgnoreCase("delete")) {
            String id = request.getParameter("span_name");
            deleteSpan(username, id, result);
        } else if (action.equalsIgnoreCase("update_span_osrm")) {
            // SCJS 010 
            calculateAndUpdateOSRM(username, name, result);
        } else if (action.equalsIgnoreCase("update_span_osrm_client")) {
            String routeGeometry = request.getParameter("route_geometry");
            int totalDistance = Integer.parseInt(request.getParameter("total_distance"));
            int totalTime = Integer.parseInt(request.getParameter("total_time"));
            updateSpanOSRMDataFromClient(username, name, routeGeometry, totalDistance, totalTime, result);
        }
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        try {
            jrp.createResponse(result, response.getWriter());
        } catch (IOException ex) {
            LOGGER.fatal(ex.getMessage());
        }
    }

    // SCJS 010 START
    private HashMap fetchRouteGeometry(HashMap map) {

        String jsonString = "";
        String loc1 = (String) map.get("startlatlng");
        String loc2 = (String) map.get("endlatlng");
        String requestUrl = "http://192.168.11.131/viaroute?z=15&output=json&loc=" + loc1 + "&loc=" + loc2 + "&instructions=true";
        try {
            URL url = new URL(requestUrl.toString());
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                jsonString = inputLine;
            }
            in.close();
            JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(jsonString);
            String routeGeometry = jsonObj.getString("route_geometry");
            map.put("Route_geometry", routeGeometry);
            JSONObject jsonObj1 = jsonObj.getJSONObject("route_summary");
            int totalDistance = Integer.parseInt(jsonObj1.getString("total_distance"));
            int totalEstimatedTime = Integer.parseInt(jsonObj1.getString("total_time"));
            map.put("total_distance", totalDistance);
            map.put("total_time", totalEstimatedTime);
            return map;
        } catch (IOException e) {
            LOGGER.fatal(e.getMessage());
            return null;
        }
    }
    // SCJS 010 END

    // SCJS 010 START
    private void updateSpanOSRMData(String username, Connection connection, HashMap OSRMData) {

        PreparedStatement stmt = null;
        try {
            String name = (String) OSRMData.get("span_name");
            String routeGeometry = (String) OSRMData.get("Route_geometry");
            Integer totalDistance = (Integer) OSRMData.get("total_distance");
            Integer totalEstimatedTime = (Integer) OSRMData.get("total_time");

            // get the query result before updating to the database
            String[] columns = {"route_geometry", "total_distance", "total_time"};
            Map<String, String[]> prevResult = AuditTrailProcessor.getColumnsValue(AuditTrailProcessor.UserAction.UPDATE_SPAN_OSRM, columns,
                    (new HashMap.SimpleEntry<String, String>("span_name", name)));

            stmt = connection.prepareStatement(UPDATE_SPAN_OSRM_SQL);
            stmt.setString(1, routeGeometry);
            stmt.setInt(2, totalDistance);
            stmt.setInt(3, totalEstimatedTime);
            stmt.setString(4, name);
            stmt.executeUpdate();

            // log the user action
            Map<String, String[]> curResult = new HashMap<String, String[]>();
            curResult.put(columns[0], (new String[]{routeGeometry}));
            curResult.put(columns[1], (new String[]{totalDistance.toString()}));
            curResult.put(columns[2], (new String[]{totalEstimatedTime.toString()}));
            logToAuditTrail(username, AuditTrailProcessor.UserAction.UPDATE_SPAN_OSRM, columns, prevResult, curResult,
                    "Span '" + name + "' OSRM is updated");
        } catch (SQLException ex) {
            LOGGER.info(ex.getMessage());
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
    // SCJS 010 END

    private void logToAuditTrail(String username, AuditTrailProcessor.UserAction action, String[] columns, Map<String, String[]> prevResult, Map<String, String[]> curResult, String descPrefix) {

        String description = descPrefix + ": The columns (" + columns[0];
        for (int i = 1; i < columns.length; i++) {
            description += "," + columns[i];
        }
        description += ") is changed FROM (" + prevResult.get(columns[0])[0];
        for (int i = 1; i < columns.length; i++) {
            description += "," + prevResult.get(columns[i])[0];
        }
        description += ") TO (" + curResult.get(columns[0])[0];
        for (int i = 1; i < columns.length; i++) {
            description += "," + curResult.get(columns[i])[0];
        }
        description += ")";
        AuditTrailProcessor.log(username, action, description);
    }

    private void insertSpan(String username, String name, String startDetectorID, String endDetectorID, String[] logicalGroups, Result result) {

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(INSERT_SPAN_SQL);
            stmt.setString(1, name.trim());
            stmt.setString(2, startDetectorID);
            stmt.setString(3, endDetectorID);
            stmt.executeUpdate();
            insertSpanLogicalGroup(username, connection, name, logicalGroups);
            connection.commit();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_SPAN, "Span '" + name + "' is added");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();

                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to add span '"+name+"'. Please try again.");
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

    // SCJS 009 START
    private void updateSpanThresholdSpeed(String username, String column, String name, Integer value, Result result) {

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
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_SPAN_SPEED_THRESHOLD, column,
                    (new HashMap.SimpleEntry<String, String>("span_name", name)));

            stmt = connection.prepareStatement(String.format(UPDATE_SPAN_SINGLE_SPEED_THRESHOLD_SQL, column));
            stmt.setInt(1, value);
            stmt.setString(2, name);
            stmt.executeUpdate();
            result.setSuccess(true);

            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_SPAN_SPEED_THRESHOLD,
                    "Span '" + name + "' speed threshold is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex) {
            LOGGER.info(ex.getMessage());
            result.setSuccess(false);
            if (ex.getMessage().contains("moderateGreaterThanslow")) {
                result.setMessage("'Moderate' speed should be greater than 'slow' speed.");
            } else if (ex.getMessage().contains("slowLessThanModerateAndGreaterThanverySlow")) {
                result.setMessage("'Slow' speed should lie between speeds 'very slow' and 'moderate'");
            } else if (ex.getMessage().contains("stationaryLessthanVerySlow")) {
                result.setMessage("'Stationary' speed should be less than 'very slow' speed ");
            } else if (ex.getMessage().contains("verySlowLessThanSlowGreaterThanStationary")) {
                result.setMessage("'Very Slow' speed should lie between speeds 'slow' and 'stationary' / 'Slow' speed should lie between speeds 'very slow' and 'moderate'");
            }
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

    private void updateSpanThresholdSpeed(String username, String name, Integer stationary, Integer verySlow, Integer slow,
            Integer moderate, Result result) {

        Connection connection = null;
        PreparedStatement stmt = null;
        DatabaseManager dm = null;
        try {
            try {
                dm = DatabaseManager.getInstance();
            } catch (DatabaseManagerException ex) {
                LOGGER.fatal(ex.getMessage());
            }
            connection = dm.getDatasource().getConnection();

            // get the query result before updating to the database
            String[] columns = {"stationary", "very_slow", "slow", "moderate"};
            Map<String, String[]> prevResult = AuditTrailProcessor.getColumnsValue(AuditTrailProcessor.UserAction.UPDATE_SPAN_SPEED_THRESHOLD, columns,
                    (new HashMap.SimpleEntry<String, String>("span_name", name)));

            stmt = connection.prepareStatement(UPDATE_SPAN_SPEED_THRESHOLDS_SQL);
            stmt.setInt(1, stationary);
            stmt.setInt(2, verySlow);
            stmt.setInt(3, slow);
            stmt.setInt(4, moderate);
            stmt.setString(5, name);
            stmt.executeUpdate();
            result.setSuccess(true);

            // log the user action
            Map<String, String[]> curResult = new HashMap<String, String[]>();
            curResult.put(columns[0], (new String[]{stationary.toString()}));
            curResult.put(columns[1], (new String[]{verySlow.toString()}));
            curResult.put(columns[2], (new String[]{slow.toString()}));
            curResult.put(columns[3], (new String[]{moderate.toString()}));
            logToAuditTrail(username, AuditTrailProcessor.UserAction.UPDATE_SPAN_SPEED_THRESHOLD, columns, prevResult, curResult,
                    "Span '" + name + "' speed threshold is updated");
        } catch (NamingException ex) {
            LOGGER.fatal(ex.getMessage());
        } catch (SQLException ex) {
            LOGGER.info(ex.getMessage());
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to update span "+name+ " speed threshold. Please try again.");
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
    // SCJS 009 END

    // SCJS 010 START
    private HashMap resetOSRMData(HashMap map) {
        HashMap resetOSRM = map;
        resetOSRM.put("Route_geometry", null);
        resetOSRM.put("total_distance", 0);
        resetOSRM.put("total_time", 0);
        return resetOSRM;
    }

    private HashMap fetchStartEndLatLong(Connection connection, String name) {
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            stmt = connection.prepareStatement(FETCH_SPAN_START_END_DETECTOR_LOCATIONS_SQL);
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("span_name", name);
            while (rs.next()) {
                map.put("startlatlng", rs.getString(1));
                map.put("endlatlng", rs.getString(2));
            }
            return map;
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getMessage());
            return null;
        }
    }

    private boolean isValidLatLong(HashMap map) {
        if (((map.get("startlatlng")).toString().equals("0,0")) || ((map.get("endlatlng")).toString().equals("0,0"))) {
            return false;
        } else {
            return true;
        }
    }
    // SCJS 010 END

    private void insertSpanLogicalGroup(String username, Connection connection, String id, String[] logicalGroups) {
        try {
            PreparedStatement stmt = null;
            String insertSpanLogicalGroups = "";
            for (int i = 0; i < logicalGroups.length; i++) {
                insertSpanLogicalGroups += INSERT_SPAN_LOGICAL_GROUP_SQL;
            }
            stmt = connection.prepareStatement(insertSpanLogicalGroups);
            int index = 1;
            String lgDescription = "(";
            for (int i = 0; i < logicalGroups.length; i++) {
                stmt.setString(index, id);
                index++;
                stmt.setString(index, logicalGroups[i]);
                index++;
                lgDescription += logicalGroups[i] + ",";
            }
            lgDescription = lgDescription.substring(0, lgDescription.length() - 1);
            lgDescription += ")";
            stmt.executeUpdate();
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_SPAN_LOGICAL_GROUP, "Span '" + id + "' with the logical groups " + lgDescription + " is added");
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getMessage());
        }
    }

    private void updateSpanOSRMDataFromClient(String username, String name, String routeGeometry, int totalDistance, int totalTime, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        HashMap OSRMData = new HashMap();
        OSRMData.put("span_name", name);
        OSRMData.put("Route_geometry", routeGeometry);
        OSRMData.put("total_distance", totalDistance);
        OSRMData.put("total_time", totalTime);

        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            updateSpanOSRMData(username, connection, OSRMData);
            connection.commit();
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to update span OSRM data. Please try again.");
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

    // SCJS 010 START
    private void calculateAndUpdateOSRM(String username, String name, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        HashMap StartEndLatLong = null;
        HashMap OSRMData = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            connection.setAutoCommit(false);
            StartEndLatLong = fetchStartEndLatLong(connection, name);
            if (isValidLatLong(StartEndLatLong)) {
                OSRMData = fetchRouteGeometry(StartEndLatLong);
            } else {
                OSRMData = resetOSRMData(StartEndLatLong);
            }
            updateSpanOSRMData(username, connection, OSRMData);
            connection.commit();
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to update OSRM data.");
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
    // SCJS 010 END

    private void updateSpan(String username, String column, String value, String name, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();

            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_SPAN, column,
                    (new HashMap.SimpleEntry<String, String>("span_name", name)));

            stmt = connection.prepareStatement(String.format(UPDATE_SPAN_SQL, column));
            stmt.setString(1, value.trim());
            stmt.setString(2, name);
            stmt.executeUpdate();
            result.setSuccess(true);

            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_SPAN,
                    "Span '" + name + "' is updated: The column '" + column + "' is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to update span '"+name+"'. Please try again.");
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

    // SCJS 009 START
    private void updateOSRMTotalDistance(String username, String name, Integer value, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();

            // get the query result before updating to the database
            String prevResult = AuditTrailProcessor.getColumnValue(AuditTrailProcessor.UserAction.UPDATE_SPAN_OSRM, "total_distance",
                    (new HashMap.SimpleEntry<String, String>("span_name", name)));

            stmt = connection.prepareStatement(UPDATE_SPAN_OSRM_TOTAL_DISTANCE_SQL);
            stmt.setInt(1, value);
            stmt.setString(2, name);
            stmt.executeUpdate();
            result.setSuccess(true);

            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_SPAN_OSRM,
                    "Span '" + name + "' is updated: The column 'total_distance' is changed FROM '" + prevResult + "' TO '" + value + "'");
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to update OSRM information.");
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
    // SCJS 009 END

    private void deleteSpan(String username, String id, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_SPAN_SQL);
            stmt.setString(1, id);
            stmt.executeUpdate();
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_SPAN, "Span '" + id + "' is removed");
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
            throws ServletException {
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
            throws ServletException {
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
