package ssl.bluetruth.servlet.update;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.utils.JsonResponseProcessor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.database.entities.mapLocation;

public class MapManager extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(MapManager.class);
    private static final double UK_VIEW_NORTH_WEST_LAT = 56.3774187738762;
    private static double UK_VIEW_NORTH_WEST_LNG = -12.1728515625;
    private static double UK_VIEW_SOUTH_EAST_LAT = 49.5822260446217;
    private static double UK_VIEW_SOUTH_EAST_LNG = 4.06494140625;

    private final String MAP_BOUNDS_SQL = "SELECT "
            + "MAX(detector.longitude) AS south_east_lng, "
            + "MIN(detector.longitude) AS north_west_lng, "
            + "MAX(detector.latitude) AS north_west_lat, "
            + "MIN(detector.latitude) AS south_east_lat "
            + "FROM ( "
            + "SELECT detector.longitude, detector.latitude "
            + "FROM "
            + "detector "
            + "JOIN detector_logical_group  ON detector.detector_id = detector_logical_group.detector_id "
            + "JOIN instation_user_logical_group ON detector_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "WHERE instation_user_logical_group.username = ? "
            + "UNION "
            + "SELECT detector.longitude, detector.latitude "
            + "FROM "
            + "detector "
            + "JOIN span ON detector.detector_id = span.start_detector_id OR detector.detector_id = span.end_detector_id "
            + "JOIN span_logical_group ON span.span_name = span_logical_group.span_name "
            + "JOIN instation_user_logical_group ON span_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "WHERE instation_user_logical_group.username = ? "
            + "UNION "
            + "SELECT detector.longitude, detector.latitude "
            + "FROM "
            + "detector "
            + "JOIN span ON detector.detector_id = span.start_detector_id OR detector.detector_id = span.end_detector_id "
            + "JOIN route_span ON span.span_name = route_span.span_name "
            + "JOIN route_logical_group ON route_span.route_name = route_logical_group.route_name "
            + "JOIN instation_user_logical_group ON route_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "WHERE instation_user_logical_group.username = ? "
            + ") detector";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        String username = request.getUserPrincipal().getName();

        Result result = new Result();
        String action = request.getParameter("action");
        if (action.equalsIgnoreCase("bounds")) {
            calculateMapBounds(result, username);
        }
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        try {
            jrp.createResponse(result, response.getWriter());
        } catch (IOException ex) {
            LOGGER.fatal(ex.getMessage());
        }
    }

    //Get map bounds
    private void calculateMapBounds(Result result, String username) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(MAP_BOUNDS_SQL);
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, username);
            rs = stmt.executeQuery();
            Map userViewMapCoordinates = new HashMap();
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rs.next()) {
                Integer columns = rsmd.getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    userViewMapCoordinates.put(rsmd.getColumnName(i), rs.getObject(i));
                }
            }

            checkMapBoundLimits(userViewMapCoordinates);

            result.setData(userViewMapCoordinates);
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setSuccess(false);
            result.setMessage("User '" + username + "' is unable to calculate map information.");
            LOGGER.info(ex.getMessage());
        } catch (NamingException ex) {
            LOGGER.fatal(ex.getMessage());
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal(ex.getMessage());
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

    private boolean checkPointInsideUK(mapLocation point) {
        return (point.getLat() >= UK_VIEW_SOUTH_EAST_LAT && point.getLat() <= UK_VIEW_NORTH_WEST_LAT)
                && (point.getLng() <= UK_VIEW_SOUTH_EAST_LNG && point.getLng() >= UK_VIEW_NORTH_WEST_LNG);
    }

    private void checkMapBoundLimits(Map userViewMapCoordinates) {
        mapLocation user_northWest = new mapLocation((double) userViewMapCoordinates.get("north_west_lat"),
                (double) userViewMapCoordinates.get("north_west_lng"));
        mapLocation user_southEast = new mapLocation((double) userViewMapCoordinates.get("south_east_lat"),
                (double) userViewMapCoordinates.get("south_east_lng"));

        if (!checkPointInsideUK(user_northWest) || !checkPointInsideUK(user_southEast)) {
            setUkDefaultCoordinates(userViewMapCoordinates);
        }
    }

    private void setUkDefaultCoordinates(Map userViewMapCoordinates) {
        userViewMapCoordinates.clear();
        userViewMapCoordinates.put("north_west_lat", UK_VIEW_NORTH_WEST_LAT);
        userViewMapCoordinates.put("north_west_lng", UK_VIEW_NORTH_WEST_LNG);
        userViewMapCoordinates.put("south_east_lat", UK_VIEW_SOUTH_EAST_LAT);
        userViewMapCoordinates.put("south_east_lng", UK_VIEW_SOUTH_EAST_LNG);
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
            throws ServletException {
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
            throws ServletException {
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
