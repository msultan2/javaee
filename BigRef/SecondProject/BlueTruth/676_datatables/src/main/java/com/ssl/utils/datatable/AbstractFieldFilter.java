/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ssl.utils.datatable;

/**
 *
 * @author katemallichan
 */
public abstract class AbstractFieldFilter {
    private String fieldName = null;

    public AbstractFieldFilter(String fieldName){
        if(fieldName == null) {
            throw new IllegalArgumentException("Field name must not be null");
        }
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}
