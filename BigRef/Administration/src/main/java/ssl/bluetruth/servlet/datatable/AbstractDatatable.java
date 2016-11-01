/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.datatable;

import com.ssl.utils.datatable.DataTableColumnDef;
import com.ssl.utils.datatable.DataTableFieldFilter;
import com.ssl.utils.datatable.StringUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ssl.bluetruth.csv.CSVDatatableProcessor;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.servlet.response.DataResponse;
import ssl.bluetruth.utils.CSVResponseProcessor;

/**
 *
 * @author wingc
 */
public abstract class AbstractDatatable extends HttpServlet {

    public void processDownloadRequest(HttpServletRequest request, HttpServletResponse response, String filename) throws ServletException, IOException {
        Map<String, List<String>> params = StringUtils.convertRequestParams(request.getParameterMap());
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            CSVDatatableProcessor dp = new CSVDatatableProcessor(dm.getDatasource(), getTableName(request), getColumns(request), params, filename);
            addAdditionalFilters(dp, getAdditionalParams(request));
            addGroups(dp, getGroups(request));
            DataResponse responseObject = dp.processRequest();
            CSVResponseProcessor processor = new CSVResponseProcessor();
            processor.createResponse(responseObject, response);
        } catch (Exception ex) {
            response.setStatus(500);
        }
    }

    private void addAdditionalFilters(CSVDatatableProcessor dp, DataTableFieldFilter... filters) {
        if ((filters != null) && (filters.length > 0)) {
            dp.addAdditionalFilters(filters);
        }
    }

    private void addGroups(CSVDatatableProcessor dp, String[] groups) {
        if ((groups != null) && (groups.length > 0)) {
            dp.addGroups(groups);
        }
    }

    protected DataTableFieldFilter[] getAdditionalParams(HttpServletRequest request) {
        return null;
    }

    protected String[] getGroups(HttpServletRequest request) {
        return null;
    }

    protected String getTableName(HttpServletRequest request) {
        return null;
    }

    protected List<DataTableColumnDef> getColumns(HttpServletRequest request) {
        return null;
    }

    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
