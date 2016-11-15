/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.utils.datatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing generation of Hibernate filter criteria forString fields
 * @author katemallichan
 */
public class StringFieldFilter extends AbstractFieldFilter implements DataTableFieldFilter {


    private static final String INPUT_WILDCARD_MULTIPLE_CHAR = "*";
    private static final String QUERY_WILDCARD_MULTIPLE_CHAR = "%";
    private static final String ESCAPED_QUERY_WILDCARD_MULTIPLE_CHAR = "!%";
    private static final String INPUT_WILDCARD_SINGLE_CHAR = "?";
    private static final String QUERY_WILDCARD_SINGLE_CHAR = "_";
    private static final String ESCAPED_QUERY_WILDCARD_SINGLE_CHAR = "!_";

    private static final String QUERY_ESCAPE_CHAR = "!";
    private static final String QUERY_ESCAPED_ESCAPE_CHAR = "!!";
    private static final String QUERY_BACKLASH_CHAR = "\\";
    private static final String QUERY_ESCAPE_BACKLASH_CHAR = "\\\\";

    private String filterString;

    public StringFieldFilter(String fieldName, String filterString) {
        super(fieldName);
        if (filterString == null) {
            throw new IllegalArgumentException("Filter string and field name must not be null");
        }

        //Escape all instances of the escape char first
        this.filterString = filterString.replace(QUERY_ESCAPE_CHAR,
                QUERY_ESCAPED_ESCAPE_CHAR);

        //Escape all instances of the query wildcards
        this.filterString = this.filterString.replace(QUERY_WILDCARD_SINGLE_CHAR,
                ESCAPED_QUERY_WILDCARD_SINGLE_CHAR);
        this.filterString = filterString.replace(QUERY_WILDCARD_MULTIPLE_CHAR,
                ESCAPED_QUERY_WILDCARD_MULTIPLE_CHAR);

        //Convert all instances of input wildcards to query wildcards
        this.filterString = filterString.replace(INPUT_WILDCARD_MULTIPLE_CHAR,
                QUERY_WILDCARD_MULTIPLE_CHAR);
        this.filterString = this.filterString.replace(INPUT_WILDCARD_SINGLE_CHAR,
                QUERY_WILDCARD_SINGLE_CHAR);

        this.filterString = this.filterString.replace(QUERY_BACKLASH_CHAR,
                QUERY_ESCAPE_BACKLASH_CHAR);
    }
   
    @Override
    public List<String> getSQLWhereStrings() {
        List<String> sqlWhereStrings = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        String valueName = getFieldName();
        sb.append(valueName).append(" LIKE ").append("'").append(filterString).append("'");
        sqlWhereStrings.add(sb.toString());
        return sqlWhereStrings;
    }

    public String getFilterString() {
        return filterString;
    }
}