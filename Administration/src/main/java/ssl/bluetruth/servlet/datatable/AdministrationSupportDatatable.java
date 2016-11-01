package ssl.bluetruth.servlet.datatable;

import com.ssl.utils.datatable.ColumnType;
import com.ssl.utils.datatable.DataTableColumnDef;
import com.ssl.utils.datatable.DataTableFieldFilter;
import com.ssl.utils.datatable.DataTableResponse;
import com.ssl.utils.datatable.DatatableProcessor;
import com.ssl.utils.datatable.StringFieldFilter;
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
import ssl.bluetruth.servlet.update.BrandConfigurationManager;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 * @author wingc
 */
public class AdministrationSupportDatatable extends AbstractDatatable {

    private static final Logger LOGGER = LogManager.getLogger(AdministrationSupportDatatable.class);
    private static final String TABLE_NAME = "branding_contact_details "
            + "JOIN instation_user ON "
            + "instation_user.brand = branding_contact_details.brand "
            + "AND instation_user.username = '%s'";

    private final JsonResponseProcessor responseProcessor = new JsonResponseProcessor();
    private static final List<DataTableColumnDef> columns = new ArrayList<DataTableColumnDef>();
    private static final String fileName = "contact.csv";
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String action = request.getParameter("action");
        if ((action != null) && (action.equalsIgnoreCase("csv_download"))) {
            processDownloadRequest(request, response, fileName);
        } else {
            // retrieve the table columns with this brand
            String contactMethod = request.getParameter("contact_method");
            if (contactMethod != null) {
                Map<String, List<String>> params = StringUtils.convertRequestParams(request.getParameterMap());
                try {
                    DatabaseManager dm = DatabaseManager.getInstance();
                    DatatableProcessor dp = new DatatableProcessor(dm.getDatasource(), String.format(TABLE_NAME, request.getUserPrincipal().getName()), columns, params);
                    // create additional filter for checking branding_contact_details.brand
                    dp.addAdditionalFilters(new StringFieldFilter(
                            BrandConfigurationManager.BrandConfigurationColumn.CONTACT_METHOD.descriptiveName, contactMethod));
                    DataTableResponse responseObject = dp.processRequest();
                    responseProcessor.createResponse(responseObject, response.getWriter());
                } catch (Exception ex) {
                    response.setStatus(500);
                    LOGGER.warn("Could not process request", ex);
                }
            } else {
                response.setStatus(500);
                LOGGER.warn("Could not process request");
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        //Set up datatable columns list
        columns.add(new DataTableColumnDef("Title", "branding_contact_details.title", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Contact", "branding_contact_details.contact", ColumnType.STRING));
        columns.add(new DataTableColumnDef("Description", "branding_contact_details.description", ColumnType.STRING));
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

    @Override
    protected DataTableFieldFilter[] getAdditionalParams(HttpServletRequest request) {
        DataTableFieldFilter[] list = new DataTableFieldFilter[1];
        try {
            String contactMethod = request.getParameter("contact_method");
            list[0] = new StringFieldFilter(BrandConfigurationManager.BrandConfigurationColumn.CONTACT_METHOD.descriptiveName, contactMethod);
        } catch (Exception ex) {
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

// SCJS 015 END