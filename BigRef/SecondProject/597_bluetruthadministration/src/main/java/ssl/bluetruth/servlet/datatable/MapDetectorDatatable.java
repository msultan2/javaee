/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2002 (C) Simulation Systems Ltd. All Rights Reserved.
 * 
 * MapDetectorDatatable.java
 * @author svenkataramanappa
 * 
 * Product:
 *
 * Change History: Created on August 13, 2007, 11:31 AM Version 001
 * 2015-02-27 SCJS 597/030 - Display only active icons on Map
 *
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

public class MapDetectorDatatable extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(MapDetectorDatatable.class);

    // SCJS 016 START
    private static final String TABLE_NAME = "detector "
            + "JOIN detector_statistic ON detector.detector_id = detector_statistic.detector_id "
            + "JOIN detector_configuration ON detector.detector_id = detector_configuration.detector_id "
            + "JOIN detector_status ON  detector.detector_id = detector_status.detector_id "
            + "LEFT JOIN detector_heartbeat ON detector.detector_id = detector_heartbeat.detector_id "
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
            + ") available ON available.detector_id = detector.detector_id"
            + " AND detector.active = true";

    // SCJS 016 END

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
    throws ServletException {
        request.getParameterMap();

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

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        //Set up datatable columns list
        columns.add(new DataTableColumnDef("Detector", "detector.detector_name", ColumnType.STRING));
        columns.add(new DataTableColumnDef("ID", "detector.detector_id", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Location", "detector.location", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Longitude", "detector.latitude", ColumnType.DOUBLE));
        columns.add(new DataTableColumnDef("Latitude", "detector.longitude", ColumnType.DOUBLE));
        columns.add(new DataTableColumnDef("MODE", "CASE detector.mode "
                + "WHEN 0 THEN 'MODE 0 - Idle' "
                + "WHEN 1 THEN 'MODE 1 - Journey Time' "
                + "WHEN 2 THEN 'MODE 2 - Occupancy' "
                + "WHEN 3 THEN 'MODE 3 - Journey Time & Occupancy' "
                + "ELSE 'MODE 0 - Idle' END", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Carriageway", "detector.carriageway", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Active", "detector.active", ColumnType.BOOLEAN));
        columns.add(new DataTableColumnDef("Logical Groups", "logical_groups", ColumnType.STRING));
        // SCJS 016 START
        columns.add(new DataTableColumnDef("Status", "CASE "
                + "WHEN GREATEST(timestamp,COALESCE(last_recorded_message_timestamp,'1970-01-01 00:00:00.0'))<now()-\"silentThresholdDelayInSeconds\" * interval '1 min' THEN 'Silent' "
                + "WHEN last_detection_timestamp > NOW() - \"detectorReportingStatusInMinutes\" * interval '1 min' THEN 'Reporting' "
                + "ELSE 'Degraded' END", ColumnType.STRING));
        // SCJS 016 END
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
    throws ServletException{
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
    throws ServletException {
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
