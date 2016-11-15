/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.utils.datatable;

import java.text.ParseException;

/**
 *
 * @author katemallichan
 */
public enum SortDirection {

    ASC,
    DESC;

    public static SortDirection parseType(String typeString) throws ParseException {
        SortDirection type = null;
        if (typeString == null) {
            throw new ParseException("Order by type string must not be null. ", 0);
        } else {
            if ("asc".equalsIgnoreCase(typeString)) {
                type = ASC;
            } else if ("desc".equalsIgnoreCase(typeString)) {
                type = DESC;
            } else {
                throw new ParseException("Error parsing OrderByType: " + typeString, 0);
            }
            return type;
        }
    }
}
