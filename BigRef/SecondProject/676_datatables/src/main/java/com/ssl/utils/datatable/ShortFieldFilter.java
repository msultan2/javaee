/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.utils.datatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing generation of Hibernate filter criteria for Integer fields
 * @author katemallichan
 */
public class ShortFieldFilter extends AbstractFieldFilter implements DataTableFieldFilter {

    private final Short max;
    private final Short min;
    private final boolean minInclusive;
    private final boolean maxInclusive;

    /**
     * Constructs an integer filter for a given field with inclusive or
     * exclusive upper and/or lower bounds. Specifying both upper and #
     * lower bounds is optional.
     * @param fieldName - the name of the Entity field this filter applies to.
     * @param fromDate - the lower bound on the Integer
     * @param toDate - the upper bound on the Integer
     * @param minInclusive - true if the minimum bound is inclusive.
     * @param maxInclusive - true if the maximum bound is inclusive.
     */
    public ShortFieldFilter(String fieldName, Short min, Short max, boolean minInclusive, boolean maxInclusive) {
        super(fieldName);
         if ((min == null) && (max == null)) {
            throw new IllegalArgumentException("At least one bound must be specified");
        } else if(min != null && max != null && min == max && !(minInclusive || maxInclusive)){
            throw new IllegalArgumentException("Min value must be < max value");
        } else if (min != null && max != null && min > max){
            throw new IllegalArgumentException("Min value must be <= max value");
        }
        this.min = min;
        this.max = max;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }  

    @Override
    public List<String> getSQLWhereStrings() {
        List<String> sqlWhereStrings = new ArrayList<String>();
        StringBuilder sb = null;
        String valueName = getFieldName();
        if (min != null) {
            sb = new StringBuilder();
            sb.append(valueName);
            if (minInclusive) {
                sb.append(">=").append(min);
            } else {
                sb.append(">").append(min);
            }
            sqlWhereStrings.add(sb.toString());
        }
        if (max != null) {
            sb = new StringBuilder();
            sb.append(valueName);
            if (maxInclusive) {
                sb.append("<=").append(max);
            } else {
                sb.append("<").append(max);
            }
            sqlWhereStrings.add(sb.toString());
        }
        return sqlWhereStrings;
    }

    public Short getMax() {
        return max;
    }

    public boolean isMaxInclusive() {
        return maxInclusive;
    }

    public Short getMin() {
        return min;
    }

    public boolean isMinInclusive() {
        return minInclusive;
    }

}