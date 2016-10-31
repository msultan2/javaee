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
import java.util.Map;
import java.util.TreeMap;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.AuditTrailProcessor;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author nthompson
 */
public class DetectorConfigurationManager extends HttpServlet {

    private static final org.apache.log4j.Logger LOGGER = LogManager.getLogger(DetectorConfigurationManager.class);
    private static final String GET_DETECTOR_CONFIGURATION_SQL = "SELECT detector_name, detector_configuration.* "
            + "FROM detector "
            + "JOIN detector_configuration ON detector.detector_id = detector_configuration.detector_id "
            + "JOIN detector_logical_group ON detector.detector_id = detector_logical_group.detector_id "
            + "JOIN instation_user_logical_group ON detector_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "JOIN instation_user ON instation_user.username = instation_user_logical_group.username AND instation_user.username = '%s' "
            + "WHERE detector.detector_id=? LIMIT 1;";
    private static final String CHECK_DETECTOR_CONFIGURATION_AVAILABLE_TO_USER_SQL = "SELECT detector_name "
            + "FROM detector "
            + "JOIN detector_logical_group ON detector.detector_id = detector_logical_group.detector_id "
            + "JOIN instation_user_logical_group ON detector_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "JOIN instation_user ON instation_user.username = instation_user_logical_group.username AND instation_user.username = '%s' "
            + "WHERE detector.detector_id=? LIMIT 1;";
    private static final String SELECT_DETECTOR_CONFIGURATION_SQL = "SELECT * FROM detector_configuration WHERE false;";
    private static final String UPDATE_DETECTOR_CONFIGURATION_SQL = "UPDATE detector_configuration ";
    private static final String WHERE_DETECTOR_CONFIGURATION_SQL = "WHERE detector_configuration.detector_id=?;";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getUserPrincipal().getName();

        Result result = new Result();
        String action = request.getParameter("action");
        String id = request.getParameter("detector_id");
        if (action.equalsIgnoreCase("get")) {
            getDetectorConfiguration(id, username, result);
        } else if (action.equalsIgnoreCase("update")) {
            Map<String, String[]> parameters = new HashMap(request.getParameterMap());
            updateDetectorConfiguration(id, username, parameters, result);
        }
        try{
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    private static void getDetectorConfiguration(String id, String username, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(String.format(GET_DETECTOR_CONFIGURATION_SQL, username));
            stmt.setString(1, id);
            rs = stmt.executeQuery();
            result.setData(resultSetToArrayList(rs));
            result.setSuccess(true);
        } catch (SQLException ex) {
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
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
                if(o != null){
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

    private void updateDetectorConfiguration(String id, String username, Map<String, String[]> parameters, Result result) {

        if (!checkDetectorIsAvailableToUser(id, username)) {
            result.setSuccess(false);
            result.setMessage("This user does not have permission to update this detector configuration.");
            return;
        }

        //Remove parameters that should not update configuration
        parameters.remove("detector_id");
        parameters.remove("action");

        String setDetectorConfigurationSQL = "SET ";

        Connection columnConnection = null;
        PreparedStatement columnSTMT = null;
        ResultSetMetaData rmd = null;
        boolean columnsReceived = false;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            columnConnection = dm.getDatasource().getConnection();
            columnSTMT = columnConnection.prepareStatement(SELECT_DETECTOR_CONFIGURATION_SQL);            
            rmd = columnSTMT.getMetaData();
            
            Integer columns = rmd.getColumnCount();
            boolean first = true;
            for (int i = 1; i <= columns; i++) {
                String column = rmd.getColumnName(i);
                Object value = parameters.get(rmd.getColumnName(i));
                if(value != null){
                    if (first) {
                        first = false;
                    } else {
                        setDetectorConfigurationSQL += ",";
                    }
                    setDetectorConfigurationSQL += " \"" + column + "\"=? "; 
                }
            }            
            columnsReceived = true;
        } catch (SQLException ex) {
            result.setMessage("This user is unable to update detector configuration. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {            
            if (columnSTMT != null) {
                try {
                    columnSTMT.close();
                } catch (SQLException e) {
                }
                columnSTMT = null;
            }
            if (columnConnection != null) {
                try {
                    columnConnection.close();
                } catch (SQLException e) {
                }
                columnConnection = null;
            }
        }
        
        if (!columnsReceived) {
            return;
        }

        Connection updateConnection = null;
        PreparedStatement updateSTMT = null;
        ResultSet updateRS = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            updateConnection = dm.getDatasource().getConnection();
            updateSTMT = updateConnection.prepareStatement(
                    UPDATE_DETECTOR_CONFIGURATION_SQL
                    + setDetectorConfigurationSQL
                    + WHERE_DETECTOR_CONFIGURATION_SQL);
            
            Integer columns = rmd.getColumnCount();
            Integer parameterIndex = 1;
            for (int i = 1; i <= columns; i++) {
                Object value = parameters.get(rmd.getColumnName(i));
                if(value != null){
                    if("null".equals((parameters.get(rmd.getColumnName(i))[0]))){
                        updateSTMT.setNull(parameterIndex, rmd.getColumnType(i));
                    }else{
                        updateSTMT.setObject(parameterIndex, parameters.get(rmd.getColumnName(i))[0], rmd.getColumnType(i));
                    }
                    parameterIndex++;
                } 
            }

            updateSTMT.setString(parameterIndex, id);

            // get the query result before updating to the database
            Map<String, String[]> prevResult = AuditTrailProcessor.getTableValues(AuditTrailProcessor.UserAction.UPDATE_DETECTOR_CONFIGURATION,
                    (new HashMap.SimpleEntry<String, String>("detector_id", id)));
            
            updateSTMT.executeUpdate();
            result.setSuccess(true);
            
            // log the user action
            logToAuditTrail(username, id, prevResult, parameters);
        } catch (SQLException ex) {
            result.setMessage("This user is unable to update detector configuration. Please try again.");
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (updateRS != null) {
                try {
                    updateRS.close();
                } catch (SQLException e) {
                }
                updateRS = null;
            }
            if (updateSTMT != null) {
                try {
                    updateSTMT.close();
                } catch (SQLException e) {
                }
                updateSTMT = null;
            }
            if (updateConnection != null) {
                try {
                    updateConnection.close();
                } catch (SQLException e) {
                }
                updateConnection = null;
            }
        }
    }
    
    private void logToAuditTrail(String username, String id, Map<String, String[]> prevResult, Map<String, String[]> curResult) {
        String columnDescription = "(", prevDescription = "(", curDescription = "(";
        
        for (Map.Entry<String, String[]> entry : curResult.entrySet()) {
            String curKey = entry.getKey();
            String curValue = entry.getValue()[0];
            String prevValue = prevResult.get(curKey)[0];
            boolean same = false;
            if (prevValue != null) {
                same = curValue.equals(prevValue);
            }
            if (!same) {
                columnDescription += curKey + ",";
                prevDescription += prevValue + ",";
                curDescription += curValue + ",";
            }
        }
        
        if (columnDescription.charAt(columnDescription.length()-1) == ',') {
            columnDescription = columnDescription.substring(0, columnDescription.length()-1);
        }
        if (prevDescription.charAt(prevDescription.length()-1) == ',') {
            prevDescription = prevDescription.substring(0, prevDescription.length()-1);
        }
        if (curDescription.charAt(curDescription.length()-1) == ',') {
            curDescription = curDescription.substring(0, curDescription.length()-1);
        }
        
        columnDescription += ")";
        prevDescription += ")";
        curDescription += ")";
        
        if ((columnDescription.length() != 2) || (prevDescription.length() != 2) || (curDescription.length() != 2)) {
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.UPDATE_DETECTOR_CONFIGURATION, 
                    "Detector id '" + id + "' is updated: The columns " + columnDescription + " are changed FROM " 
                    +  prevDescription + " TO " + curDescription);
        }
    }

    private boolean checkDetectorIsAvailableToUser(String id, String username) {
        boolean detectorAvailable = false;

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(String.format(CHECK_DETECTOR_CONFIGURATION_AVAILABLE_TO_USER_SQL, username));
            stmt.setString(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                detectorAvailable = true;
            }
        } catch (SQLException ex) {
            LOGGER.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            LOGGER.fatal("Naming Exception", ex);
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
        return detectorAvailable;
    }
}
