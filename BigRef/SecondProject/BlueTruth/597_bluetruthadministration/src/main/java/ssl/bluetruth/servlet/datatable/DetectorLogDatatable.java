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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author nthompson
 */
public class DetectorLogDatatable extends AbstractDatatable {

    private static final Logger LOGGER = LogManager.getLogger(DetectorLogDatatable.class);
    private static final String TABLE_NAME = "detector_log "
            + "JOIN ( "
            + "   SELECT detector_logical_group.detector_id, "
            + "   ('['::text || array_to_string(array_agg(DISTINCT detector_logical_group.logical_group_name), '] ['::text)) || ']'::text AS logical_groups "
            + "   FROM detector_logical_group "
            + "	  JOIN instation_user_logical_group ON "
            + "	  detector_logical_group.logical_group_name = instation_user_logical_group.logical_group_name "
            + "	  JOIN instation_user ON "
            + "	  instation_user.username = instation_user_logical_group.username "
            + "	  AND instation_user.username = '%s'"
            + "	  GROUP BY detector_logical_group.detector_id"
            + ") available ON available.detector_id = detector_log.detector_id";
    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();
    private final String DETECTOR_ID_REQUEST = "detector_id";
    private final String DETECTOR_ID_DB = "detector_log.detector_id";
    private static final String fileName = "detector_logs.csv";

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
            String detectorId = request.getParameter(DETECTOR_ID_REQUEST);
            if (detectorId != null) {
                try {
                    DatabaseManager dm = DatabaseManager.getInstance();
                    DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), String.format(TABLE_NAME, request.getUserPrincipal().getName()), getColumns(request), params);
                    dp.addAdditionalFilters(new StringFieldFilter(DETECTOR_ID_DB, detectorId));
                    DataTableResponse responseObject = dp.processRequest();
                    responseProcessor.createResponse(responseObject, response.getWriter());
                } catch (Exception ex) {
                    response.setStatus(500);
                    LOGGER.warn("Detector log could not be downloaded", ex);
                }
            } else {
                response.setStatus(500);
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
        columns.add(new DataTableColumnDef("Detector Log ID", "detector_log.detector_log_id", ColumnType.INTEGER));
        columns.add(new DataTableColumnDef("Uploaded Timestamp", "detector_log.uploaded_timestamp AT TIME ZONE '"+timezone+"'", ColumnType.DATE));
        return columns;
    }

    @Override
    protected DataTableFieldFilter[] getAdditionalParams(HttpServletRequest request) {
        DataTableFieldFilter[] list = new DataTableFieldFilter[1];
        String detectorId = request.getParameter(DETECTOR_ID_REQUEST);
        list[0] = new StringFieldFilter(DETECTOR_ID_DB, detectorId);
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