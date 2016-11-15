package ssl.bluetruth.servlet.update;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.naming.NamingException;
import javax.servlet.ServletException;
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
public class SpanEventsInformationManager extends AbstractSpanInformationManager {

    private static final Logger LOGGER = LogManager.getLogger(SpanManager.class);

    private String INSERT_SPAN_EVENT_SQL = "INSERT INTO span_events_information (span_name, description, start_timestamp, end_timestamp) VALUES(?,?,?,?);";
    private String DELETE_SPAN_EVENT_SQL = "DELETE FROM span_events_information WHERE span_name = ? AND event_id = ?;";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String action = null;
        try {
            Result result = new Result();
            action = request.getParameter("action");
            String name = URLDecoder.decode(request.getParameter("span_name"), "UTF-8");
            String username = request.getUserPrincipal().getName();               
            if (action.equalsIgnoreCase("insert")) {
                    String description = request.getParameter("description");     
                    Timestamp start = getTimestampInUtc(request, EVENT_START_TIMESTAMP_PARAMETER);
                    Timestamp end = getTimestampInUtc(request, EVENT_END_TIMESTAMP_PARAMETER);
                    insertSpanEvent(username, name, description, start, end, result); 
            } else if(action.equalsIgnoreCase("delete")){
                Integer event_id = Integer.parseInt(request.getParameter("span_event_id"));
                deleteSpanEvent(username, name, event_id, result);
            }
            JsonResponseProcessor jrp = new JsonResponseProcessor();
            jrp.createResponse(result, response.getWriter());            
        } catch (IOException | RuntimeException mex) {
            response.setStatus(500);
            LOGGER.warn("Could not process the action '"+action+"' cause: "+mex.getLocalizedMessage(), mex);
        }
        
    }

    private void insertSpanEvent(String username, String name, String description, Timestamp start, Timestamp end, Result result) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(INSERT_SPAN_EVENT_SQL);
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setTimestamp(3, start);
            stmt.setTimestamp(4, end);
            stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.ADD_SPAN_EVENT, "Span '" + name + "' with the event '" + description + "' is added");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    LOGGER.info(e.getMessage());
                }
            }
            result.setSuccess(false);
            result.setMessage("User '"+username+"' is unable to add span '"+name+ "' with the event '" + description +"'. Please try again.");
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
                    LOGGER.info(e.getMessage());
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.info(e.getMessage());
                }
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.info(e.getMessage());
                }
                connection = null;
            }
        }
    }

     private void deleteSpanEvent(String username, String name, Integer event_id, Result result){
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_SPAN_EVENT_SQL);
            stmt.setString(1, name);
            stmt.setInt(2, event_id);
            stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.REMOVE_SPAN_EVENT, "Span '" + name + "' with event id " + event_id.toString() + " is removed");
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
