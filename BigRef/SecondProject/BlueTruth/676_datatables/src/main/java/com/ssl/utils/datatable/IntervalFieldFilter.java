/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.utils.datatable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class implementing generation of Hibernate filter criteria for Date fields
 * @author katemallichan
 */
public class IntervalFieldFilter extends AbstractFieldFilter implements DataTableFieldFilter {

    private String minInterval;
    private String maxInterval;
    private boolean minInclusive;
    private boolean maxInclusive;
    private Map<String, Object> hqlParams = new HashMap<String, Object>();
    private static final Pattern intervalFormat = Pattern.compile("[0-9]+:[0-5][0-9]:[0-5][0-9]");
    
    /**
     * Constructs a date filter object with inclusive or exclusive upper and/or
     * lower bounds. Specifying both upper and lower bounds is optional.
     * @param fieldName - the name of the Entity field this filter applies to.
     * @param minInterval - the lower bound on Date
     * @param maxInterval - the upper bound on Date
     * @param minInclusive - true if the minimum bound is inclusive.
     * @param maxInclusive - true if the maximum bound is inclusive.
     */
    public IntervalFieldFilter(String fieldName, String minInterval, String maxInterval, Boolean minInclusive, Boolean maxInclusive) {
        super(fieldName);
        if ((minInterval == null) && (maxInterval == null)) {
            throw new IllegalArgumentException("At least one interval bound must be specified.");
        } 
        
        else if (minInterval != null && !intervalFormat.matcher(minInterval).matches()) {
            throw new IllegalArgumentException("Lower interval bounds do not match pattern.");
        }

        else if (maxInterval != null && !intervalFormat.matcher(maxInterval).matches()) {
            throw new IllegalArgumentException("Upper interval bounds do not match pattern.");
        }

        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    @Override
    public List<String> getSQLWhereStrings() {
        List<String> sqlWhereStrings = new ArrayList<String>();
        StringBuilder sb = null;
        if (minInterval != null) {
            sb = new StringBuilder();
            sb.append(getFieldName());
            if (minInclusive) {
                sb.append(">='").append(minInterval).append("'");
            } else {
                sb.append(">'").append(minInterval).append("'");
            }
            sqlWhereStrings.add(sb.toString());
        }
        if (maxInterval != null) {
            sb = new StringBuilder();
            sb.append(getFieldName());
            if (maxInclusive) {
                sb.append("<='").append(maxInterval).append("'");
            } else {
                sb.append("<'").append(maxInterval).append("'");
            }
            sqlWhereStrings.add(sb.toString());
        }
        return sqlWhereStrings;
    }

    public Map<String, Object> getHQLParams() {
        return hqlParams;
    }

    public String getMinInterval() {
        return minInterval;
    }

    public boolean isMaxInclusive() {
        return maxInclusive;
    }

    public boolean isMinInclusive() {
        return minInclusive;
    }

    public String getMaxInterval() {
        return maxInterval;
    }

}
