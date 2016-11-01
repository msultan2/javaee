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
public class AnalysisDetectorDetectionsDatatable extends AbstractDatatable {

    private static final Logger LOGGER = LogManager.getLogger(AnalysisDetectorDetectionsDatatable.class);
    private static final String TABLE_NAME = "device_detection "
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
            + ") available ON available.detector_id = device_detection.detector_id";

    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();

    private final String DETECTOR_ID_REQUEST = "detector_id";
    private final String DETECTOR_ID_DB = "device_detection.detector_id";
    private static final String fileName = "detector_detection_graph_data.csv";

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

        String detectorId = URLDecoder.decode(request.getParameter(DETECTOR_ID_REQUEST),"UTF-8");

        String action = request.getParameter("action");
        if ((action != null) && (action.equalsIgnoreCase("csv_download"))) {
            processDownloadRequest(request, response, fileName);
        } else {
        //Convert Params from MAP of Strings-String[] to MAP String-List of Strings
        Map<String, List<String>> params = StringUtils.convertRequestParams(request.getParameterMap());
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), String.format(TABLE_NAME, request.getUserPrincipal().getName()), getColumns(request), params);
            dp.addAdditionalFilters(new StringFieldFilter(DETECTOR_ID_DB, detectorId));
            dp.addGroups("detected_timestamp_section");
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
        columns.add(new DataTableColumnDef("Detection Period", "(date_trunc('H',device_detection.detection_timestamp) + "
                + "floor(EXTRACT('minute' FROM device_detection.detection_timestamp)/5)*5 * '1 minute'::interval) "
                + "AT TIME ZONE '" + timezone + "'", "detected_timestamp_section", ColumnType.DATE, "device_detection.detection_timestamp"));
        columns.add(new DataTableColumnDef("Count", "COUNT(device_id)", ColumnType.INTEGER));
        return columns;
    }

    @Override
    protected DataTableFieldFilter[] getAdditionalParams(HttpServletRequest request) {
        DataTableFieldFilter[] list = new DataTableFieldFilter[1];
        try {
            String detectorId = URLDecoder.decode(request.getParameter(DETECTOR_ID_REQUEST), "UTF-8");
            list[0] = new StringFieldFilter(DETECTOR_ID_DB, detectorId);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.fatal("Could not process request", ex);
        }
        return list;
    }

    @Override
    protected String[] getGroups(HttpServletRequest request) {
        String[] groups = new String[1];
        groups[0] = "detected_timestamp_section";
        return groups;

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