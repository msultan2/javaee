/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.datatable;

import com.ssl.utils.datatable.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author nthompson
 */
public class InstationUserDatatable extends AbstractDatatable {

    private static final Logger LOGGER = LogManager.getLogger(InstationUserDatatable.class);
    private static final String TABLE_NAME = "instation_user_view";
    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();
    private static final List<DataTableColumnDef> columns = new ArrayList<DataTableColumnDef>();
    private static final String fileName = "users.csv";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        request.getParameterMap();

        String action = request.getParameter("action");
        if ((action != null) && (action.equalsIgnoreCase("csv_download"))) {
            processDownloadRequest(request, response, fileName);
        } else {
            //Convert Params from MAP of Strings-String[] to MAP String-List of Strings
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
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        //Set up datatable columns list
        columns.add(new DataTableColumnDef("Name", "full_name", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Username", "username", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Email", "email_address", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Roles", "roles", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Logical Groups", "logical_groups", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Activated", "activated", ColumnType.STRING));
    }

    @Override
    protected String getTableName(HttpServletRequest request) {
        return TABLE_NAME;
    }

    @Override
    protected List<DataTableColumnDef> getColumns(HttpServletRequest request) {
        return columns;
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
