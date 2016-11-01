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
public class MapSpanDatatable extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(MapSpanDatatable.class);
    private static final String TABLE_NAME = "span "
            + "JOIN span_statistic "
            + "ON span.span_name = span_statistic.span_name "
            + "JOIN span_speed_thresholds "
            + "ON span.span_name = span_speed_thresholds.span_name "
            + "JOIN span_osrm "
            + "ON span.span_name = span_osrm.span_name "
            + "JOIN detector AS d1 "
            + "ON span.start_detector_id = d1.detector_id "
            + "JOIN detector AS d2 "
            + "ON span.end_detector_id = d2.detector_id AND NOT "
            + "("
            + "(d1.latitude > %s AND d2.latitude > %s) OR (d1.latitude < %s AND d2.latitude < %s) "
            + "OR "
            + "(d1.longitude > %s AND d2.longitude > %s) OR (d1.longitude < %s AND d2.longitude < %s)"
            + ") ";

    private static final String SPAN_JOIN = "JOIN ( "
            + "   SELECT span_logical_group.span_name, "
            + "   ('['::text || array_to_string(array_agg(DISTINCT span_logical_group.logical_group_name), '] ['::text)) || ']'::text AS logical_groups, "
            + "   '' AS routes "
            + "   FROM span_logical_group "
            + "	  JOIN instation_user_logical_group ON "
            + "	  span_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "	  JOIN instation_user ON "
            + "	  instation_user.username = instation_user_logical_group.username "
            + "	  AND instation_user.username = '%s'"
            + "	  GROUP BY span_logical_group.span_name"
            + ") available ON available.span_name = span.span_name";
    private static final String ROUTE_JOIN = "JOIN ( "
            + "   SELECT route_span.span_name AS span_name, "
            + "   '' AS logical_groups, "
            + "   ('['::text || array_to_string(array_agg(DISTINCT route_span.route_name), '] ['::text)) || ']'::text AS routes "
            + "   FROM route_span "
            + "   JOIN route_logical_group ON "
            + "   route_logical_group.route_name = route_span.route_name "
            + "	  JOIN instation_user_logical_group ON "
            + "	  route_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "	  JOIN instation_user ON "
            + "	  instation_user.username = instation_user_logical_group.username "
            + "	  AND instation_user.username = '%s'"
            + "	  GROUP BY route_span.span_name"
            + ") available ON available.span_name = span.span_name";
    private static final String SPAN_ROUTE_JOIN = "JOIN ( "
            + "   SELECT CASE WHEN spans.span_name IS NULL THEN routes.span_name ELSE spans.span_name END AS span_name, logical_groups, routes "
            + "   FROM ("
            + "   SELECT span_logical_group.span_name, "
            + "   ('['::text || array_to_string(array_agg(DISTINCT span_logical_group.logical_group_name), '] ['::text)) || ']'::text AS logical_groups "
            + "   FROM span_logical_group "
            + "	  JOIN instation_user_logical_group ON "
            + "	  span_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "	  JOIN instation_user ON "
            + "	  instation_user.username = instation_user_logical_group.username "
            + "	  AND instation_user.username = '%s'"
            + "	  GROUP BY span_logical_group.span_name "
            + "   ) spans FULL JOIN ( "
            + "   SELECT route_span.span_name,  "
            + "   ('['::text || array_to_string(array_agg(DISTINCT route_span.route_name), '] ['::text)) || ']'::text AS routes "
            + "   FROM route_span "
            + "   JOIN route_logical_group ON "
            + "   route_logical_group.route_name = route_span.route_name "
            + "	  JOIN instation_user_logical_group ON "
            + "	  route_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "	  JOIN instation_user ON "
            + "	  instation_user.username = instation_user_logical_group.username "
            + "	  AND instation_user.username = '%s'"
            + "	  GROUP BY route_span.span_name "
            + "   ) routes ON routes.span_name = spans.span_name"
            + ") available ON available.span_name = span.span_name";

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
            String northEastLat = params.get("northeastlat").get(0);
            String northEastLng = params.get("northeastlng").get(0);
            String soutWestLat = params.get("southwestlat").get(0);
            String soutWestLng = params.get("southwestlng").get(0);

            String sql = String.format(TABLE_NAME+SPAN_ROUTE_JOIN,
                    northEastLat,northEastLat,
                    soutWestLat,soutWestLat,
                    northEastLng,northEastLng,
                    soutWestLng,soutWestLng,
                    request.getUserPrincipal().getName(),request.getUserPrincipal().getName());

            List<String> span_layer = params.get("span_layer");
            List<String> route_layer = params.get("route_layer");

            if(span_layer != null && route_layer == null){
                sql = String.format(TABLE_NAME+SPAN_JOIN,
                    northEastLat,northEastLat,
                    soutWestLat,soutWestLat,
                    northEastLng,northEastLng,
                    soutWestLng,soutWestLng,
                    request.getUserPrincipal().getName());
            } else if(span_layer == null && route_layer != null){
                sql = String.format(TABLE_NAME+ROUTE_JOIN,
                    northEastLat,northEastLat,
                    soutWestLat,soutWestLat,
                    northEastLng,northEastLng,
                    soutWestLng,soutWestLng,
                    request.getUserPrincipal().getName());
            }
        
            DatabaseManager dm = DatabaseManager.getInstance();
            DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), 
                    sql, columns, params);
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
        columns.add(new DataTableColumnDef("Name", "span.span_name", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Route Geometry", "span_osrm.route_geometry", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Total Distance", "span_osrm.total_distance", ColumnType.INTEGER));
        columns.add(new DataTableColumnDef("Start Detector", "d1.detector_id", ColumnType.STRING));
        columns.add(new DataTableColumnDef("End Detector", "d2.detector_id", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Status", "CASE "
                + "WHEN span_statistic.last_journey_detection_timestamp > NOW() - interval '45 mins' THEN 'Reporting' "
                + "ELSE 'Silent' END", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Speed", "CASE "
                + "WHEN span_osrm.total_distance != 0 "
                + "AND span_statistic.last_reported_journey_time != '00:00:00'::interval "
                + "THEN (span_osrm.total_distance/EXTRACT(EPOCH FROM span_statistic.last_reported_journey_time))*2.23693629 "
                + "ELSE NULL END", ColumnType.DOUBLE));
        columns.add(new DataTableColumnDef("Stationary", "span_speed_thresholds.stationary", ColumnType.INTEGER));
        columns.add(new DataTableColumnDef("Very Slow", "span_speed_thresholds.very_slow", ColumnType.INTEGER));
        columns.add(new DataTableColumnDef("Slow", "span_speed_thresholds.slow", ColumnType.INTEGER));
        columns.add(new DataTableColumnDef("Moderate", "span_speed_thresholds.moderate", ColumnType.INTEGER));
        columns.add(new DataTableColumnDef("Logical Groups", "logical_groups", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Routes", "routes", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Total Time", "span_osrm.total_time", ColumnType.INTEGER));
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
