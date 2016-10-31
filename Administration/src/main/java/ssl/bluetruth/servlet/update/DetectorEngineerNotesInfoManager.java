package ssl.bluetruth.servlet.update;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
 * SCJS 016
 * @author svenkataramanappa
 */
public class DetectorEngineerNotesInfoManager extends HttpServlet{

    private static final Logger LOGGER = LogManager.getLogger(DetectorEngineerNotesInfoManager.class);

    private String INSERT_DETECTOR_NOTE_SQL = "INSERT INTO detector_engineer_notes (detector_id, description, author) VALUES(?,?,?);";
    private String DELETE_DETECTOR_NOTE_SQL = "DELETE FROM detector_engineer_notes WHERE note_id = ? ;";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Result result = new Result();
        String action = request.getParameter("action");
        String name = URLDecoder.decode(request.getParameter("detector_id"),"UTF-8");
        String author = request.getUserPrincipal().getName();
        if (action.equalsIgnoreCase("insert")) {
            String description = request.getParameter("description");
            insertDetectorNote(name, description, author, result);
        } else if(action.equalsIgnoreCase("delete")){
            Integer note_id = Integer.parseInt(request.getParameter("note_id"));
            deleteDetectorNote(note_id, name, author, result);
        }
        try{
        JsonResponseProcessor jrp = new JsonResponseProcessor();
        jrp.createResponse(result, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    private void insertDetectorNote(String name, String description, String author, Result result) {

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(INSERT_DETECTOR_NOTE_SQL);
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, author);
            stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(author, AuditTrailProcessor.UserAction.ADD_DETECTOR_NOTE, "Detector id '" + name + "' with the note '" + description + "' is added");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();

                } catch (SQLException e) {
                }
            }
            result.setSuccess(false);
            result.setMessage("Unable to insert detector note information.");
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

     private void deleteDetectorNote(Integer note_id, String name, String author, Result result){
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(DELETE_DETECTOR_NOTE_SQL);
            stmt.setInt(1, note_id);
            stmt.executeUpdate();
            result.setSuccess(true);
            // log the user action
            AuditTrailProcessor.log(author, AuditTrailProcessor.UserAction.REMOVE_DETECTOR_NOTE, "Detector id '" + name + "' with note id " + note_id.toString() + " is removed");
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
 