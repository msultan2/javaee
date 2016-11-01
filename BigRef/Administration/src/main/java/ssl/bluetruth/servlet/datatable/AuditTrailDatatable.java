/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.servlet.datatable;

import com.ssl.utils.datatable.ColumnType;
import com.ssl.utils.datatable.DataTableColumnDef;
import com.ssl.utils.datatable.DataTableResponse;
import com.ssl.utils.datatable.DatatableProcessor;
import com.ssl.utils.datatable.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author wingc
 */
public class AuditTrailDatatable extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(AuditTrailDatatable.class);
    private static final String TABLE_NAME = "audit_trail";

    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();
    private final List<DataTableColumnDef> columns = new ArrayList<DataTableColumnDef>();
    private static final char CHAR_ASTERISK = '*';
    private static final String ACTION_TYPE = "action_type";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        // retrieve the table columns with this brand
        Map<String, List<String>> params = StringUtils.convertRequestParams(request.getParameterMap());
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), TABLE_NAME, columns, params);
            DataTableResponse responseObject = dp.processRequest();
            responseProcessor.createResponse(responseObject, response.getWriter());
        } catch (Exception ex) {
            response.setStatus(500);
            LOGGER.warn("Could not process request", ex);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        //Set up datatable columns list
        columns.add(new DataTableColumnDef("Audit Trail Id", "audit_trail.audit_trail_id", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Username", "audit_trail.username", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Timestamp", "audit_trail.timestamp", ColumnType.DATE));
        columns.add(new DataTableColumnDef("Action", "audit_trail.action_type", ColumnType.MULTISTRING));
        columns.add(new DataTableColumnDef("Description", "audit_trail.description", ColumnType.STRING));
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
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
     * Handles the HTTP
     * <code>POST</code> method.
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
