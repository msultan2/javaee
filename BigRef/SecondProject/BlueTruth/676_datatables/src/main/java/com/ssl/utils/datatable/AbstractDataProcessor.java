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
 * DetectorDatatable.java
 * @author etorbett
 * 
 * Product:
 *
 * Change History: Created on August 13, 2007, 11:31 AM Version 001
 * 2015-02-27 SCJS 676/01 - Bug Fix to handle SQL conditions.
 *
 */
package com.ssl.utils.datatable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 *
 * @author etorbett
 */
public class AbstractDataProcessor {

    private final List<DataTableColumnDef> tableColumns;
    private final String tableName;
    private final Map<String, List<String>> requestParameters;
    private final List<String> columnParameterNames = new ArrayList<String>();
    private final List<String> columnDisplayNames = new ArrayList<String>();
    private final List<DatastoreOrderField> orderFields = new ArrayList<DatastoreOrderField>();
    private final List<DataTableFieldFilter> filters = new ArrayList<DataTableFieldFilter>();
    private final List<DataTableFieldFilter> additionalFilters = new ArrayList<DataTableFieldFilter>();
    private final List<String> whereStrings = new ArrayList<String>();
    private final List<String> whereAdditionalStrings = new ArrayList<String>();

    private final List<String> groupByStrings = new ArrayList<String>();

    private final DataSource dataSource;

    public AbstractDataProcessor(DataSource dataSource, String databaseTableName, List<DataTableColumnDef> columns, Map<String, List<String>> requestParameters) throws DataTablesException {
        this.requestParameters = requestParameters;
        this.dataSource = dataSource;

        tableName = databaseTableName;

        tableColumns = columns;

        List<Integer> filterIndexes = new ArrayList<Integer>();
        List<Integer> orderFieldIndexes = new ArrayList<Integer>();

        parseColumnDefinitions(orderFieldIndexes, filterIndexes, columnParameterNames, columnDisplayNames);

        parseFiltersAndOrderFields(orderFieldIndexes, orderFields, filterIndexes, filters);

        updateWhereStrings();
    }

    public void addAdditionalFilters(DataTableFieldFilter... filters) {
        additionalFilters.addAll(Arrays.asList(filters));
        updateWhereStrings();
    }

    public void addGroups(String... groups) {
        groupByStrings.addAll(Arrays.asList(groups));
    }

    private String generateQueryString(final String requestedData, final boolean includeWhereString, boolean includeOrderFields, boolean includePageStart, boolean includePageLength, boolean includeGroupBy) {
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("select ");
        queryStringBuilder.append(requestedData);
        queryStringBuilder.append(" from ");
        queryStringBuilder.append(tableName);
        generateQueryCriteria(queryStringBuilder, includeWhereString, includeOrderFields, includePageStart, includePageLength, includeGroupBy);
        final String queryString = queryStringBuilder.toString();
        return queryString;
    }

    public void generateQueryCriteria(StringBuilder queryStringBuilder, boolean includeWhereString, boolean includeOrderFields, boolean includePageStart, boolean includePageLength, boolean includeGroupBy) throws NumberFormatException {
        if (includeWhereString) {
            String whereString = createWhereString();
            if (!whereString.isEmpty()) {
                queryStringBuilder.append(" where ");
                queryStringBuilder.append(whereString);
            }
        } else {
            //Always add additional filters
            String whereAdditionalString = createWhereAdditionalString();
            if (!whereAdditionalString.isEmpty()) {
                queryStringBuilder.append(" where ");
                queryStringBuilder.append(whereAdditionalString);
            }
        }
        if (includeGroupBy) {
            queryStringBuilder.append(createGroupByString());
        }
        if (includeOrderFields && !orderFields.isEmpty()) {
            queryStringBuilder.append(" order by ");
            boolean first = true;
            for (DatastoreOrderField order : orderFields) {
                final String sqlString = order.getSQLOrderField(order.getFieldName());
                if (first) {
                    first = false;
                } else {
                    queryStringBuilder.append(", ");
                }
                queryStringBuilder.append(sqlString);
            }
        }        
        if (includePageStart) {
            Integer pageStart = getPageStart();
            if (pageStart != null) {
                queryStringBuilder.append(" offset ");
                queryStringBuilder.append(pageStart);
            }
        }
        if (includePageLength) {
            Integer pageLength = getPageLength();
            if (pageLength != null) {
                queryStringBuilder.append(" limit ");
                queryStringBuilder.append(pageLength);
            }
        }        
    }

    private String createGroupByString() {
        if(groupByStrings.isEmpty()){
            return "";
        } else {
            String combinationString = ", ";
            final String groupByString = " GROUP BY "+StringUtils.join(groupByStrings, combinationString);
            return groupByString;
        }
    }

    private String createWhereString() {
        String combinationString = " and ";
        if (getCombineFiltersWithOr()) {
            combinationString = " or ";
        }
        final String whereString = StringUtils.join(whereStrings, combinationString);
        return whereString;
    }

    private String createWhereAdditionalString() {
        String combinationString = " and ";
        if (getCombineFiltersWithOr()) {
            combinationString = " or ";
        }
        final String whereString = StringUtils.join(whereAdditionalStrings, combinationString);
        return whereString;
    }

    private void parseFiltersAndOrderFields(List<Integer> orderFieldIndexes, List<DatastoreOrderField> orderFields, List<Integer> filterIndexes, List<DataTableFieldFilter> filters) throws DataTablesException {
        try {
            if (orderFieldIndexes != null) {
                for (int i = 0; i < orderFieldIndexes.size(); i++) {
                    int column = orderFieldIndexes.get(i);
                    if (requestParameters.containsKey(DataTableRequestParamStrings.sortColNamePrefix + String.valueOf(i))) {
                        List<String> orderStrings = requestParameters.get(DataTableRequestParamStrings.sortDirPrefix + String.valueOf(i));
                        if (orderStrings != null) {
                            String orderType = orderStrings.get(0);
                            DatastoreOrderField orderField = new DatastoreOrderField(tableColumns.get(column).getAliasName(), SortDirection.parseType(orderType));
                            orderFields.add(orderField);
                        }
                    }
                }
            }
            for (int index : filterIndexes) {
                DataTableColumnDef column = tableColumns.get(index);

                filters.add(DataTableFieldFilterFactory.createFieldFilter(
                        column.getFilterType(),
                        column.getEnumType(),
                        index,
                        column.getPropertyNameForWhere(),
                        requestParameters));
            }
        } catch (ParseException ex) {
            throw new DataTablesException("Error parsing request parameters.");
        }
    }

    private Integer getPageLength() throws NumberFormatException {
        Integer pageLength = null;
        if (requestParameters.get(DataTableRequestParamStrings.pageLengthFieldName) != null) {
            pageLength = Integer.parseInt(requestParameters.get(DataTableRequestParamStrings.pageLengthFieldName).get(0));
        }
        return pageLength;
    }

    private Integer getPageStart() throws NumberFormatException {
        Integer pageStart = null;
        //Extract page start and length parameters if specified
        if (requestParameters.get(DataTableRequestParamStrings.pageStartFieldName) != null) {
            pageStart = Integer.parseInt(requestParameters.get(DataTableRequestParamStrings.pageStartFieldName).get(0));
        }
        return pageStart;
    }

    private void parseColumnDefinitions(List<Integer> orderFieldIndexes, List<Integer> filterIndexes, List<String> columnParameterNames, List<String> columnDisplayNames) throws NumberFormatException {
        //Loop through list of column definitions for this table type
        for (int colIndex = 0; colIndex < tableColumns.size(); colIndex++) {
            //Get the column name and look up the name of the filter flag parameter to look for.
            final String colName = getColumnWithAlias(tableColumns.get(colIndex));
            final List<String> columnFilterVar = requestParameters.get(DataTableRequestParamStrings.filterFlagPrefix + String.valueOf(colIndex));
            final String sortColName = DataTableRequestParamStrings.sortColNamePrefix + String.valueOf(colIndex);
            if (requestParameters.containsKey(sortColName)) {
                String sortCol = requestParameters.get(sortColName).get(0);
                orderFieldIndexes.add(Integer.parseInt(sortCol));
            } //the index of this column to the filter indexes list.
            if (columnFilterVar != null && columnFilterVar.get(0) != null) {
                String columnFilterFlag = columnFilterVar.get(0);
                if (Boolean.parseBoolean(columnFilterFlag)) {
                    filterIndexes.add(colIndex);
                }
            }
            columnParameterNames.add(colName);
            columnDisplayNames.add(tableColumns.get(colIndex).getColName());
        }
    }

    private String getColumnWithAlias(DataTableColumnDef dtcd){
        if(dtcd.getPropertyName().equals(dtcd.getAliasName())){
            return dtcd.getPropertyName();
        }
        return dtcd.getPropertyName() + " AS " + dtcd.getAliasName();
    }

    private boolean getCombineFiltersWithOr() {
        boolean combineFiltersWithOr = false;
        if (requestParameters.containsKey(DataTableRequestParamStrings.filterTypeString)) {
            combineFiltersWithOr = Boolean.parseBoolean(requestParameters.get(DataTableRequestParamStrings.filterTypeString).get(0));
        }
        return combineFiltersWithOr;
    }

    protected final List<String> getColumnDisplayNames() {
        return columnDisplayNames;
    }

    private void updateWhereStrings() {
        whereStrings.clear();
        whereAdditionalStrings.clear();
        for (DataTableFieldFilter filter : filters) {
            List<String> whereStringsForFilter = filter.getSQLWhereStrings();
            String joinedWhereString = StringUtils.join(whereStringsForFilter, " and ");
            whereStrings.add(joinedWhereString);
        }
        for (DataTableFieldFilter filter : additionalFilters) {
            List<String> whereStringsForFilter = filter.getSQLWhereStrings();
            String joinedWhereString = StringUtils.join(whereStringsForFilter, " and ");
            whereStrings.add(joinedWhereString);
            whereAdditionalStrings.add(joinedWhereString);
        }
    }

    protected final long getUnfilteredResultCount() {
        long retVal = 0;
        Connection connection = null;
        Statement statement = null;
        ResultSet results = null;
        try {
            
            String unfilteredRowCountQueryString = "";
            if(groupByStrings.isEmpty()){
                unfilteredRowCountQueryString = generateQueryString("count(*)", false, false, false, false, false);
            } else {
                unfilteredRowCountQueryString = generateQueryString("count(*) OVER() AS count, "+StringUtils.join(columnParameterNames, ","), false, false, false, false, true);

            }

            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(unfilteredRowCountQueryString);
            if (results.next()) {
                retVal = results.getLong(1);
            }
        } catch (Exception ex) {
            //Log but don't rethrow
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException sQLException) {
                    //do nothing
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sQLException) {
                    //do nothing
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sQLException) {
                    //do nothing
                }
            }
        }

        return retVal;
    }


    protected final long getFilteredResultCount() {
        long retVal = 0;

        Connection connection = null;
        Statement statement = null;
        ResultSet results = null;
        try {
            
            String filteredRowCountQueryString = "";
            if(groupByStrings.isEmpty()){
                filteredRowCountQueryString = generateQueryString("count(*)", true, false, false, false, false);
            } else {
                filteredRowCountQueryString = generateQueryString("count(*) OVER() AS count, "+StringUtils.join(columnParameterNames, ","), true, false, false, false, true);

            }
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(filteredRowCountQueryString);

            if (results.next()) {
                retVal = results.getLong(1);
            }
        } catch (Exception ex) {
            //Log but don't rethrow
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException sQLException) {
                    //do nothing
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sQLException) {
                    //do nothing
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sQLException) {
                    //do nothing
                }
            }
        }

        return retVal;
    }

    public Iterator<String[]> processData() throws DataTablesException {
        Iterator<String[]> retVal = null;

        Connection connection = null;
        Statement statement = null;
        ResultSet results = null;
        try {
            final String requestedData = StringUtils.join(columnParameterNames, ",");
            final String queryString = generateQueryString(requestedData,
                    true,
                    true,
                    true,
                    true,
                    true);

            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(queryString);
        } catch (Exception ex) {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException sQLException) {
                    //do nothing
                }
                results = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sQLException) {
                    //do nothing
                }
                statement = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sQLException) {
                    //do nothing
                }
                connection = null;
            }
            throw new DataTablesException("Unable to read from cached report table", ex);
        }

        final int rowWidth = columnParameterNames.size();

        try {
            retVal = new CachedDataIterator(connection, statement, results, rowWidth);
        } catch (Exception exception) {
            throw new DataTablesException("Unable to read from cached report table", exception);
        }

        return retVal;
    }

    public static class CachedDataIterator implements Iterator<String[]> {

        public CachedDataIterator(Connection connection, Statement statement, ResultSet results, int rowWidth) throws Exception {
            this.connection = connection;
            this.statement = statement;
            this.results = results;
            this.rowWidth = rowWidth;

            populateNext();
        }
        private final Connection connection;
        private final Statement statement;
        private final ResultSet results;
        private final int rowWidth;
        private String[] next = null;

        @Override
        public boolean hasNext() {
            return (next != null);
        }

        @Override
        public String[] next() {
            String[] retVal = next;
            next = null;
            try {
                populateNext();
            } catch (Exception exception) {
                //do nothing - already cleaned up
            }

            return retVal;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        private void cleanUp() {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception exception) {
                    //do nothing
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception exception) {
                    //do nothing
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception exception) {
                    //do nothing
                }
            }
        }

        private void populateNext() throws Exception {
            try {
                if (results.next()) {
                    String[] rowData = new String[rowWidth];
                    for (int i = 0; i < rowWidth; i++) {
                        switch (results.getMetaData().getColumnType(i + 1)) {
                            case java.sql.Types.BIT:
                            case java.sql.Types.BOOLEAN: {
                                rowData[i] = Boolean.toString(results.getBoolean(i + 1));
                                break;
                            }
                            case java.sql.Types.FLOAT:
                            case java.sql.Types.DOUBLE: {
                            rowData[i] = String.valueOf(results.getDouble(i + 1));
                                break;
                            }
                            default: {
                            rowData[i] = results.getString(i + 1);
                                break;
                            }
                        }
                    }
                    next = rowData;
                } else {
                    //We've finished
                    cleanUp();
                }
            } catch (Exception exception) {
                cleanUp();
                throw exception;
            }
        }
    }

    protected final String getTableName() {
        return tableName;
    }

    public Map<String, List<String>> getRequestParameters() {
        return requestParameters;
    }
}
