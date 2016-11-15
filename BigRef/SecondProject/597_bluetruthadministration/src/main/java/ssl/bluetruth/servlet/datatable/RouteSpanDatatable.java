/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.datatable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ssl.bluetruth.database.DatabaseManager;
import com.ssl.utils.datatable.ColumnType;
import com.ssl.utils.datatable.DataTableColumnDef;
import com.ssl.utils.datatable.DataTableResponse;
import ssl.bluetruth.utils.JsonResponseProcessor;
import com.ssl.utils.datatable.DatatableProcessor;
import com.ssl.utils.datatable.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author nthompson
 */
public class RouteSpanDatatable extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(RouteSpanDatatable.class);
    //Possible SQL injection here
    private static final String TABLE_NAME = "span "
            + "JOIN ( "
            + "   SELECT span_logical_group.span_name, "
            + "   ('['::text || array_to_string(array_agg(DISTINCT span_logical_group.logical_group_name), '] ['::text)) || ']'::text AS logical_groups "
            + "   FROM span_logical_group "
            + "	  JOIN instation_user_logical_group ON "
            + "	  span_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "	  JOIN instation_user ON "
            + "	  instation_user.username = instation_user_logical_group.username "
            + "	  AND instation_user.username = '%s'"
            + "	  GROUP BY span_logical_group.span_name"
            + ") available ON available.span_name = span.span_name "
            + "LEFT JOIN route_span "
            + "ON span.span_name = route_span.span_name "
            + "AND (route_span.route_name = '' OR route_span.route_name = '%s')";

    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();

    private final List<DataTableColumnDef> columns = new ArrayList<DataTableColumnDef>();

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

        //Convert Params from MAP of Strings-String[] to MAP String-List of Strings
        Map<String, List<String>> params = StringUtils.convertRequestParams(request.getParameterMap());

        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), String.format(TABLE_NAME, request.getUserPrincipal().getName(), request.getParameter("route")), columns, params);

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
        columns.add(new DataTableColumnDef("Span Name", "span.span_name", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Logical Groups", "available.logical_groups", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Route Name", "route_span.route_name", ColumnType.STRING));
        
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
