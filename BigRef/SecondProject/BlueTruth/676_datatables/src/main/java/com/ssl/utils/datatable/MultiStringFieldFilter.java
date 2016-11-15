/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.utils.datatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing generation of Hibernate filter criteria for MultiString fields
 * with the format *[<search1>]*[search2]*[search3]...
 * @author wingc
 */
public class MultiStringFieldFilter extends AbstractFieldFilter implements DataTableFieldFilter {
    
    private static final String INPUT_WILDCARD_MULTIPLE_CHAR = "*";
    private static final String QUERY_WILDCARD_MULTIPLE_CHAR = "%";
    private static final char OPEN_BRACKET = '[';
    private static final char CLOSE_BRACKET = ']';
    private String filterString;
    /**
     * Constructs a date filter object with inclusive or exclusive upper and/or
     * lower bounds. Specifying both upper and lower bounds is optional.
     * @param fieldName - the name of the Entity field this filter applies to.
     * @param filterString - the filter date in string format
     */
    public MultiStringFieldFilter(String fieldName, String filterString) {
        super(fieldName);
        if (filterString == null) {
            throw new IllegalArgumentException("Filter string and field name must not be null.");
        }
        this.filterString = filterString;
        
        //Convert all instances of input wildcards to query wildcards
        this.filterString = filterString.replace(INPUT_WILDCARD_MULTIPLE_CHAR,
                QUERY_WILDCARD_MULTIPLE_CHAR);
    }

    @Override
    public List<String> getSQLWhereStrings() {
        List<String> sqlWhereStrings = new ArrayList<String>();
        StringBuilder sb = null;
        if (filterString != null) {
            sb = new StringBuilder();
            
            // create where-clause
            int curSel = 0, prevSel = 0;
            boolean finished = false, findClause = false;
            while (!finished) {
                prevSel = filterString.indexOf(QUERY_WILDCARD_MULTIPLE_CHAR, curSel);
                finished = (prevSel == -1);

                if (!finished) {
                    curSel = filterString.indexOf(QUERY_WILDCARD_MULTIPLE_CHAR, prevSel+1);
                    finished = (curSel == -1);
                }
                if (!finished) {
                    // append OR for more than one where-clauses
                    if (findClause) {
                        sb.append(" OR ");
                    }
                    String filterValue = filterString.substring(prevSel+1, curSel);
                    if ((filterValue.charAt(0) == OPEN_BRACKET) && (filterValue.charAt(filterValue.length()-1) == CLOSE_BRACKET)) {
                        filterValue = filterValue.substring(1, filterValue.length()-1);
                    }
                    sb.append(getFieldName()).append("='").append(filterValue).append("'");
                    findClause = true;
                }
            }
            sqlWhereStrings.add(sb.toString());
        }
        return sqlWhereStrings;
    }
}
