/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.utils.datatable;

/**
 * Defines an ordering for a specified entity field.
 * @author katemallichan
 */
public class SimpleOrderField {

    private String fieldName;
    private SortDirection orderBy;

    public SimpleOrderField(String fieldName, SortDirection orderBy) {
        if ((fieldName == null) || (orderBy == null)) {
            throw new IllegalArgumentException("Field name and order type must not be null");
        }
        this.fieldName = fieldName;
        this.orderBy = orderBy;
    }

    public String getFieldName() {
        return fieldName;
    }

    public SortDirection getOrderByType() {
        return orderBy;
    }

}
