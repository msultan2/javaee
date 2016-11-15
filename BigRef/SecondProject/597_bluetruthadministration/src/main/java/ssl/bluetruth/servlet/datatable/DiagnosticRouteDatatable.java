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
 * SJCS 014 START
 * @author svenkataramanappa
 */
public class DiagnosticRouteDatatable extends AbstractDatatable {

    private static final Logger LOGGER = LogManager.getLogger(DiagnosticRouteDatatable.class);
    private static final String TABLE_NAME = "route "
            + "JOIN ( "
            + "   SELECT route_logical_group.route_name, "
            + "   ('['::text || array_to_string(array_agg(DISTINCT route_logical_group.logical_group_name), '] ['::text)) || ']'::text AS logical_groups "
            + "   FROM route_logical_group "
            + "	  JOIN instation_user_logical_group ON "
            + "	  route_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "	  JOIN instation_user ON "
            + "	  instation_user.username = instation_user_logical_group.username "
            + "	  AND instation_user.username = '%s'"
            + "	  GROUP BY route_logical_group.route_name"
            + ") available ON available.route_name = route.route_name "
            + "JOIN ( "
            + "   SELECT route_span.route_name, max(span_statistic.last_journey_detection_timestamp) as max_journey_detection_timestamp, "
            + "   MIN(CASE WHEN span_statistic.last_journey_detection_timestamp IS NOT NULL THEN span_statistic.last_journey_detection_timestamp "
                + "ELSE '1970-01-01 00:00:00.0+00'::timestamp with time zone END) as min_journey_detection_timestamp "
            + "   FROM route_span, span_statistic "
            + "   WHERE route_span.span_name = span_statistic.span_name "
            + "	  GROUP BY route_span.route_name"
            + ") status ON status.route_name = route.route_name";

    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();
    private static final List<DataTableColumnDef> columns = new ArrayList<DataTableColumnDef>();
    private static final String fileName = "diagnostic_routes.csv";

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
                DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), String.format(TABLE_NAME, request.getUserPrincipal().getName()), columns, params);
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
        columns.add(new DataTableColumnDef("Route", "route.route_name", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Description", "route.description", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Logical Groups", "logical_groups", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Status","CASE "
                + "WHEN max_journey_detection_timestamp > NOW() - '00:45:00'::interval "
                + "AND min_journey_detection_timestamp > NOW() - '00:45:00'::interval THEN 'Reporting' "
                + "ELSE 'Silent' END", ColumnType.STRING));
    }

    @Override
    protected String getTableName(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        return String.format(TABLE_NAME, username);
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

// SCJS 014 END