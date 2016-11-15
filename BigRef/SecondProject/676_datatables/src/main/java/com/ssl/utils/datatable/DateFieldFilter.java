/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.utils.datatable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class implementing generation of Hibernate filter criteria for Date fields
 * @author katemallichan
 */
public class DateFieldFilter extends AbstractFieldFilter implements DataTableFieldFilter {

    private Date fromDate;
    private Date toDate;
    private boolean minInclusive;
    private boolean maxInclusive;
    private Map<String, Object> hqlParams = new HashMap<String, Object>();
    
    /**
     * Constructs a date filter object with inclusive or exclusive upper and/or
     * lower bounds. Specifying both upper and lower bounds is optional.
     * @param fieldName - the name of the Entity field this filter applies to.
     * @param fromDate - the lower bound on Date
     * @param toDate - the upper bound on Date
     * @param minInclusive - true if the minimum bound is inclusive.
     * @param maxInclusive - true if the maximum bound is inclusive.
     */
    public DateFieldFilter(String fieldName, Date fromDate, Date toDate, Boolean minInclusive, Boolean maxInclusive) {
        super(fieldName);
        if ((fromDate == null) && (toDate == null)) {
            throw new IllegalArgumentException("At least one Date bound must be specified.");
        } else if (fromDate != null && toDate != null && (fromDate.compareTo(toDate) >= 0)) {
            throw new IllegalArgumentException("Lower date bound must be precede upper bound.");
        }
        if (fromDate != null) {
            this.fromDate = new Date(fromDate.getTime());
        }
        if (toDate != null) {
            this.toDate = new Date(toDate.getTime());
        }
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    @Override
    public List<String> getSQLWhereStrings() {
        List<String> sqlWhereStrings = new ArrayList<String>();
        StringBuilder sb = null;
        if (fromDate != null) {
            sb = new StringBuilder();
            sb.append(getFieldName());
            if (minInclusive) {
                sb.append(">='").append(fromDate).append("'");
            } else {
                sb.append(">'").append(fromDate).append("'");
            }
            sqlWhereStrings.add(sb.toString());
        }
        if (toDate != null) {
            sb = new StringBuilder();
            sb.append(getFieldName());
            if (maxInclusive) {
                sb.append("<='").append(toDate).append("'");
            } else {
                sb.append("<'").append(toDate).append("'");
            }
            sqlWhereStrings.add(sb.toString());
        }
        return sqlWhereStrings;
    }

    public Map<String, Object> getHQLParams() {
        return hqlParams;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public boolean isMaxInclusive() {
        return maxInclusive;
    }

    public boolean isMinInclusive() {
        return minInclusive;
    }

    public Date getToDate() {
        return toDate;
    }

}
