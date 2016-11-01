/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.csv;

import com.ssl.utils.datatable.AbstractDataProcessor;
import com.ssl.utils.datatable.DataTableColumnDef;
import com.ssl.utils.datatable.DataTablesException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import ssl.bluetruth.servlet.response.DataResponse;

/**
 *
 * @author svenkataramanappa, wingc
 */
public class CSVDatatableProcessor extends AbstractDataProcessor{

    private String filename;
    @SuppressWarnings("unchecked")
    public CSVDatatableProcessor(DataSource dataSource, String databaseTableName, List<DataTableColumnDef> columns, Map<String, List<String>> requestParameters, String filename) throws DataTablesException {
        super(dataSource, databaseTableName, columns, requestParameters);
        this.filename = filename;
    }

    public DataResponse processRequest() throws DataTablesException {
        DataResponse retVal = new DataResponse();

        // set response header
        retVal.getResponseHeaders().put("Content-Disposition", "attachment; filename=" + filename);
        retVal.getResponseHeaders().put("Content-Type", "application/csv; charset=UTF-8");

        // set response data
        List<String> columnNames = getColumnDisplayNames();
        Iterator<String[]> dataIterator = processData();
        ScrollingCSVInputStream inputStream = new ScrollingCSVInputStream(columnNames, dataIterator);
        retVal.setResponseData(inputStream);

        return retVal;
    }
}
