/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ssl.utils.datatable;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author edt
 */
public class DataTableResponse {
    private long totalRecordCount;
    private long filteredRecordCount;
    private String clientEchoRequest;
    private String columnNames;
    private final List<String[]> data = new ArrayList<String[]>();

    @JsonProperty(value="sEcho")
    public String getClientEchoRequest() {
        return clientEchoRequest;
    }

    @JsonProperty(value="sEcho")
    public void setClientEchoRequest(String clientEchoRequest) {
        this.clientEchoRequest = clientEchoRequest;
    }

    @JsonProperty(value="sColumns")
    public String getColumnNames() {
        return columnNames;
    }

    @JsonProperty(value="sColumns")
    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    @JsonProperty(value="iTotalDisplayRecords")
    public long getFilteredRecordCount() {
        return filteredRecordCount;
    }

    @JsonProperty(value="iTotalDisplayRecords")
    public void setFilteredRecordCount(long filteredRecordCount) {
        this.filteredRecordCount = filteredRecordCount;
    }

    @JsonProperty(value="iTotalRecords")
    public long getTotalRecordCount() {
        return totalRecordCount;
    }

    @JsonProperty(value="iTotalRecords")
    public void setTotalRecordCount(long totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    @JsonProperty(value="aaData")
    public List<String[]> getData() {
        return data;
    }
    
}
