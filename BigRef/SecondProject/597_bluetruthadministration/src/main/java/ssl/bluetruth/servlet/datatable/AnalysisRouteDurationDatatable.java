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
import com.ssl.utils.datatable.DataTableFieldFilter;
import com.ssl.utils.datatable.DataTableResponse;
import ssl.bluetruth.utils.JsonResponseProcessor;
import com.ssl.utils.datatable.DatatableProcessor;
import com.ssl.utils.datatable.StringFieldFilter;
import com.ssl.utils.datatable.StringUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author nthompson, Santhosh
 */
public class AnalysisRouteDurationDatatable extends AbstractDatatable {

    private static final Logger LOGGER = LogManager.getLogger(AnalysisRouteDurationDatatable.class);
    private static final String TABLE_NAME = "span_journey_detection "
            + "JOIN route_span "
            + "ON route_span.span_name = span_journey_detection.span_name "
            + "JOIN span_journey_detection_analytics "
            + "ON span_journey_detection_analytics.span_journey_detection_id = span_journey_detection.span_journey_detection_id "
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
            + ") available ON available.span_name = span_journey_detection.span_name";

    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();
    private final String ROUTE_NAME_REQUEST = "route";
    private final String ROUTE_NAME_DB = "route_span.route_name";
    private static final String fileName = "route_graph_duration.csv";

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
            String routeName = URLDecoder.decode(request.getParameter(ROUTE_NAME_REQUEST), "UTF-8");
            try {
                DatabaseManager dm = DatabaseManager.getInstance();
                DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), String.format(TABLE_NAME, request.getUserPrincipal().getName()), getColumns(request), params);
                dp.addAdditionalFilters(new StringFieldFilter(ROUTE_NAME_DB, routeName));
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
    }

    @Override
    protected String getTableName(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        return String.format(TABLE_NAME, username);
    }

    @Override
    protected List<DataTableColumnDef> getColumns(HttpServletRequest request) {
        String timezone = (String) request.getSession().getAttribute("user_timezone");
        if (timezone == null) {
            timezone = "UTC";
        }

        //Set up datatable columns list
        List<DataTableColumnDef> columns = new ArrayList<DataTableColumnDef>();
        columns.add(new DataTableColumnDef("Span Name", "span_journey_detection.span_name", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Completed Timestamp", "span_journey_detection.completed_timestamp AT TIME ZONE '" + timezone + "'", ColumnType.DATE));
        columns.add(new DataTableColumnDef("Mean", "span_journey_detection_analytics.duration_mean", ColumnType.INTERVAL));
        columns.add(new DataTableColumnDef("Median", "span_journey_detection_analytics.duration_median", ColumnType.INTERVAL));
        columns.add(new DataTableColumnDef("Mode", "span_journey_detection_analytics.duration_mode", ColumnType.INTERVAL));
        return columns;
    }

    @Override
    protected DataTableFieldFilter[] getAdditionalParams(HttpServletRequest request) {
        DataTableFieldFilter[] list = new DataTableFieldFilter[1];
        try {
            String routeName = URLDecoder.decode(request.getParameter(ROUTE_NAME_REQUEST),"UTF-8");
            list[0] = new StringFieldFilter(ROUTE_NAME_DB, routeName);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.fatal("Could not process request", ex);
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
}