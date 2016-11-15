////////////////////////////////////////////////////////////////////////////////
// 
//  THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS 
//  LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND, 
//  EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, 
//  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN 
//  INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS 
//  OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL, 
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE 
//  POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO, 
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
//  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
//  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
// 
//  Copyright 2016 (C) Costain Integrated Technology Solutions Limited. 
//  All Rights Reserved.
//
////////////////////////////////////////////////////////////////////////////////

package ssl.bluetruth.servlet.datatable;

import com.ssl.utils.datatable.ColumnType;
import com.ssl.utils.datatable.DataTableColumnDef;
import com.ssl.utils.datatable.DataTableResponse;
import com.ssl.utils.datatable.DatatableProcessor;
import com.ssl.utils.datatable.StringUtils;
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

public class DetectorDefaultConfigurationDatatable extends AbstractDatatable {

    private static final Logger LOGGER = LogManager.getLogger(DetectorDefaultConfigurationDatatable.class);
    private static final String TABLE_NAME = "default_configuration ";
    private static final String ACTION = "action";
    private static final String CSV_DOWNLOAD = "csv_download";
    private static final String DEFAULT_CSV_OUTSTATION_CONFIG = "detector_default_config.csv";
    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();
    private static final List<DataTableColumnDef> columns = new ArrayList<DataTableColumnDef>();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        String action = request.getParameter(ACTION);
        if ((action != null) && (action.equalsIgnoreCase(CSV_DOWNLOAD))) {
            processDownloadRequest(request, response, DEFAULT_CSV_OUTSTATION_CONFIG);
            } else{
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
        columns.add(new DataTableColumnDef("property", "property", ColumnType.STRING));
        columns.add(new DataTableColumnDef("value", "value", ColumnType.STRING));
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
    
     @Override
    protected String getTableName(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        return String.format(TABLE_NAME, username);
    }

    @Override
    protected List<DataTableColumnDef> getColumns(HttpServletRequest request) {
        return columns;
    }
}
