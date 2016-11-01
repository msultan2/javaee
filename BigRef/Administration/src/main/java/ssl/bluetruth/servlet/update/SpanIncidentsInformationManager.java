package ssl.bluetruth.servlet.update;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;
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

/**
 * SCJS 008
 * @author svenkataramanappa
 */
public class SpanIncidentsInformationManager extends AbstractSpanInformationManager{

    private static final Logger LOGGER = LogManager.getLogger(SpanIncidentsInformationManager.class);

    private String INSERT_SPAN_INCIDENT_SQL = 
            "INSERT INTO span_incidents_information (span_name, description, start_timestamp, end_timestamp) "
            + "VALUES(?,?,?,?);";
    private String DELETE_SPAN_INCIDENT_SQL =
            "DELETE FROM span_incidents_information "
            + "WHERE span_name = ? "
            + "AND incident_id = ?;";

    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String action = null;
        try {
            Result result = new Result();
            action = request.getParameter("action");
            String name = URLDecoder.decode(request.getParameter("span_name"),"UTF-8");
            String username = request.getUserPrincipal().getName();
            if (action.equalsIgnoreCase("insert")) {
                String description = request.getParameter("description");     
                Timestamp start = getTimestampInUtc(request, INCIDENT_START_TIMESTAMP_PARAMETER);
                Timestamp end = getTimestampInUtc(request, INCIDENT_END_TIMESTAMP_PARAMETER);                
                insertSpanIncident(username, name, description, start, end, result);                
            } else if(action.equalsIgnoreCase("delete")){
                Integer incident_id = Integer.parseInt(request.getParameter("span_incident_id"));
                deleteSpanIncident(username, name, incident_id, result);
            }
            JsonResponseProcessor jrp = new JsonResponseProcessor();
            jrp.createResponse(result, response.getWriter());            
        } catch (IOException | RuntimeException mex) {
            response.setStatus(500);
            LOGGER.warn("Could not process the action '"+action+"' cause: "+mex.getLocalizedMessage(), mex);
        }        
    }

    private void insertSpanIncident(String username, String name, String description, Timestamp start, Timestamp end, Result result){

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(INSERT_SPAN_INCIDENT_SQL);
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setTimestamp(3, start);
            stmt.setTimestamp(4, end);
            stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_SPAN_INCIDENT, "Span '" + name + "' with the incident '" + description + "' is added");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();

                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to add span '"+name+ "' with the incident '" + description +"'. Please try again.");
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

     private void deleteSpanIncident(String username, String name, Integer incident_id, Result result){

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_SPAN_INCIDENT_SQL);
            stmt.setString(1, name);
            stmt.setInt(2, incident_id);
            stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_SPAN_INCIDENT, "Span '" + name + "' with incident id " + incident_id.toString() + " is removed");
        } catch (SQLException ex) {
            LOGGER.info(ex.getMessage());
            result.setSuccess(false);
        } catch (NamingException ex) {
            LOGGER.fatal(ex.getMessage());
            result.setSuccess(false);
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal(ex.getMessage());
            result.setSuccess(false);
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
