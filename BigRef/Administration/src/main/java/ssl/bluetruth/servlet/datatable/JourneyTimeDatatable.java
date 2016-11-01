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
 *
 * @author nthompson
 */
public class JourneyTimeDatatable extends AbstractDatatable {

    private static final Logger LOGGER = LogManager.getLogger(JourneyTimeDatatable.class);
    private static final String TABLE_NAME = "route "
            + "JOIN "
            + "( "
            + "SELECT route_logical_group.route_name, "
            + "('['::text || array_to_string(array_agg(DISTINCT route_logical_group.logical_group_name), '] ['::text)) || ']'::text AS logical_groups "
            + "FROM route_logical_group "
            + "JOIN instation_user_logical_group ON route_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "AND instation_user_logical_group.username = '%s' "
            + "GROUP BY route_logical_group.route_name "
            + ") available ON available.route_name = route.route_name "
            + "JOIN "
            + "( "
            + "SELECT route_span.route_name, "
            + "max(span_statistic.last_journey_detection_timestamp) as max_journey_detection_timestamp, "
            + "min(span_statistic.last_reported_journey_time_strength) as journey_time_strength, "
            + "MIN( "
            + "CASE "
            + "WHEN span_statistic.last_journey_detection_timestamp IS NOT NULL "
            + "THEN span_statistic.last_journey_detection_timestamp "
            + "ELSE '1970-01-01 00:00:00.0+00'::timestamp with time zone END) as min_journey_detection_timestamp, "
            + "SUM(span_osrm.total_distance) AS total_distance, "
            + "MIN(span_osrm.total_distance) AS min_total_distance, "
            + "SUM(span_statistic.last_reported_journey_time) AS total_duration, "
            + "MIN(CASE "
            + "WHEN span_statistic.last_reported_journey_time IS NOT NULL "
            + "THEN span_statistic.last_reported_journey_time "
            + "ELSE '00:00:00'::interval END) AS min_duration "
            + "FROM route_span "
            + "JOIN span_statistic ON route_span.span_name = span_statistic.span_name "
            + "JOIN span_osrm ON span_osrm.span_name = route_span.span_name "
            + "GROUP BY route_span.route_name "
            + ") route_status ON route_status.route_name = route.route_name";
    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();
    private static final String fileName = "journey_times.csv";

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
            Map<String, List<String>> params = StringUtils.convertRequestParams(request.getParameterMap());
            try {
                DatabaseManager dm = DatabaseManager.getInstance();
                DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), String.format(TABLE_NAME, request.getUserPrincipal().getName()), getColumns(request), params);
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
        String timezone = "UTC";
        String localTimeZone = (String)request.getSession().getAttribute("user_timezone");
        if (localTimeZone != null) {
            timezone = localTimeZone;
        }

        List<DataTableColumnDef> columns = new ArrayList<DataTableColumnDef>();
        columns.add(new DataTableColumnDef("Route Name", "route.route_name", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Route Duration", "route_status.total_duration", ColumnType.INTERVAL));
        columns.add(new DataTableColumnDef("Route Speed", "CASE "
                + "WHEN min_total_distance != 0 "
                + "AND min_duration != '00:00:00'::interval "
                + "THEN (min_total_distance/EXTRACT(EPOCH FROM min_duration))*2.23693629 "
                + "ELSE NULL END", ColumnType.DOUBLE));
        columns.add(new DataTableColumnDef("Strength", "CASE "
                + "WHEN route_status.journey_time_strength IS NULL "
                + "THEN 0 "
                + "ELSE route_status.journey_time_strength END", ColumnType.INTEGER));
        columns.add(new DataTableColumnDef("Status", "CASE "
                + "WHEN max_journey_detection_timestamp > NOW() - '00:45:00'::interval "
                + "AND min_journey_detection_timestamp > NOW() - '00:45:00'::interval THEN 'Reporting' "
                + "ELSE 'Silent' END", ColumnType.STRING));

        columns.add(new DataTableColumnDef("Calculated Timestamp", "min_journey_detection_timestamp AT TIME ZONE '" + timezone + "'", ColumnType.DATE));
        columns.add(new DataTableColumnDef("Logical Groups", "logical_groups", ColumnType.STRING));
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
