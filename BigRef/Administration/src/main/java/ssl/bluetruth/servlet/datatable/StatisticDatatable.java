/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.servlet.datatable;

import java.io.IOException;
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
import java.util.ArrayList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author liban
 */
public class StatisticDatatable extends AbstractDatatable {

    private static final Logger LOGGER = LogManager.getLogger(StatisticDatatable.class);
    private static final String TABLE_NAME = "statistics_report "
            + "JOIN detector ON detector.detector_id = statistics_report.detector_id "
            + "JOIN ( "
            + "   SELECT detector_logical_group.detector_id, "
            + "   ('['::text || array_to_string(array_agg(DISTINCT detector_logical_group.logical_group_name), '] ['::text)) || ']'::text AS logical_groups "
            + "   FROM detector_logical_group "
            + "	  JOIN instation_user_logical_group ON "
            + "	  detector_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "	  AND instation_user_logical_group.username = '%s'"
            + "	  GROUP BY detector_logical_group.detector_id"
            + ") available ON available.detector_id = detector.detector_id "
            + "JOIN statistics_device ON statistics_device.report_id = statistics_report.report_id AND "
            + "statistics_report.report_id = %d";
    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();
    private static final String fileName = "traffic_flow.csv";

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getParameterMap();
        String action = request.getParameter("action");
        String reportIds = request.getParameter("reportId");
        int reportId = Integer.parseInt(reportIds);

        if ((action != null) && (action.equalsIgnoreCase("csv_download"))) {
            processDownloadRequest(request, response, fileName);
        } else {
            //Convert Params from MAP of Strings-String[] to MAP String-List of Strings
            Map<String, List<String>> params = StringUtils.convertRequestParams(request.getParameterMap());
            if (params.containsKey("reportId")) {
                params.remove("reportId");
            }
            try {
                DatabaseManager dm = DatabaseManager.getInstance();
                DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(),
                        String.format(TABLE_NAME, request.getUserPrincipal().getName(), reportId),
                        getColumns(request), params);
                DataTableResponse responseObject = dp.processRequest();
                responseProcessor.createResponse(responseObject, response.getWriter());
            } catch (Exception ex) {
                ex.printStackTrace();
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
        return String.format(TABLE_NAME, username, Integer.parseInt(request.getParameter("reportId")));
    }

    @Override
    protected List<DataTableColumnDef> getColumns(HttpServletRequest request) {
        String timezone = "UTC";
        String localTimeZone = (String) request.getSession().getAttribute("user_timezone");
        if (localTimeZone != null) {
            timezone = localTimeZone;
        }

        List<DataTableColumnDef> tableColumns = new ArrayList<DataTableColumnDef>();
        tableColumns.add(new DataTableColumnDef("Address or Hash", "statistics_device.addr", ColumnType.STRING));
        tableColumns.add(new DataTableColumnDef("COD", "statistics_device.cod", ColumnType.INTEGER));
        tableColumns.add(new DataTableColumnDef("First Seen", "statistics_device.first_seen AT TIME ZONE '" + timezone + "'", ColumnType.DATE));
        tableColumns.add(new DataTableColumnDef("Last Seen", "statistics_device.last_seen AT TIME ZONE '" + timezone + "'", ColumnType.DATE));
        tableColumns.add(new DataTableColumnDef("Obfuscating Function ID", "statistics_report.of_id", ColumnType.INTEGER));

        return tableColumns;
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
