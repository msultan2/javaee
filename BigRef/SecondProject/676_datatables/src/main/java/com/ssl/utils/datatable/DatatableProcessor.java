/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.utils.datatable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 *
 * @author katemallichan
 */

public class DatatableProcessor extends AbstractDataProcessor {



    @SuppressWarnings("unchecked")
    public DatatableProcessor(DataSource dataSource, String databaseTableName, List<DataTableColumnDef> columns, Map<String, List<String>> requestParameters) throws DataTablesException {
        super(dataSource, databaseTableName, columns, requestParameters);
    }

    public DataTableResponse processRequest() throws DataTablesException {
        DataTableResponse retVal = new DataTableResponse();
        //Added to the response object as a comma seperated list of column names

        retVal.setColumnNames(
                StringUtils.join(getColumnDisplayNames(), ","));

        populateUnfilteredResultsCount(retVal);
        populateFilteredResultsCount(retVal);

        try {
            populateResults(retVal);
        } catch (DataTablesException backendException) {
            throw new DataTablesException(backendException.getMessage(), backendException);
        }

        if (getRequestParameters().get("sEcho") != null && getRequestParameters().get("sEcho").size() == 1) {
            retVal.setClientEchoRequest(getRequestParameters().get("sEcho").get(0));
        }

        return retVal;
    }

    private void populateResults(final DataTableResponse retVal) throws DataTablesException {
        Iterator<String[]> dataIterator = processData();

        while (dataIterator.hasNext()) {
            retVal.getData().add(dataIterator.next());
        }
    }

    private void populateUnfilteredResultsCount(DataTableResponse retVal) {
        retVal.setTotalRecordCount(getUnfilteredResultCount());
    }

    private void populateFilteredResultsCount(DataTableResponse retVal) {
        retVal.setFilteredRecordCount(getFilteredResultCount());
    }
}
