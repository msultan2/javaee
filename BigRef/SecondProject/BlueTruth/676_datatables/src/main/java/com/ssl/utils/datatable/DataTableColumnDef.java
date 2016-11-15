/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.utils.datatable;

/**
 *
 * @author katemallichan
 */
public class DataTableColumnDef {

    private final String colName;
    private final String propertyName;
    private final String propertyNameForWhere;
    private final String aliasName;
    private final ColumnType filterType;
    private final Class<? extends Enum> enumType;

    public DataTableColumnDef(String name, String propertyName, ColumnType filterType) {
        this(name, propertyName, propertyName, filterType, propertyName);
    }

    public DataTableColumnDef(String name, String propertyName, ColumnType filterType, String propertyNameForWhere) {
        this(name, propertyName, propertyName, filterType, propertyName);
    }

    public DataTableColumnDef(String name, String propertyName, String aliasName, ColumnType filterType, String propertyNameForWhere) {
        this.colName = name;
        if (filterType == ColumnType.ENUM) {
            throw new IllegalArgumentException("Enumeration Types must use the Class<Enum> constructor");
        }
        this.filterType = filterType;
        this.propertyName = propertyName;
        this.propertyNameForWhere = propertyNameForWhere;
        this.aliasName = aliasName;
        this.enumType = null;
    }

    public DataTableColumnDef(String colName, String propertyName, Class<? extends Enum> enumType) {
        this(colName, propertyName, propertyName, enumType, propertyName);
    }

    public DataTableColumnDef(String colName, String propertyName, Class<? extends Enum> enumType, String propertyNameForWhere) {
        this(colName, propertyName, propertyName, enumType, propertyNameForWhere);
    }

    public DataTableColumnDef(String colName, String propertyName, String aliasName, Class<? extends Enum> enumType, String propertyNameForWhere) {
        this.colName = colName;
        this.filterType = ColumnType.ENUM;        
        this.propertyName = propertyName;
        this.propertyNameForWhere = propertyNameForWhere;
        this.aliasName = aliasName;
        this.enumType = enumType;
    }

    public String getColName() {
        return colName;
    }

    public ColumnType getFilterType() {
        return filterType;
    }

    public Class<? extends Enum> getEnumType() {
        return enumType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public String getPropertyNameForWhere() {
        return propertyNameForWhere;
    }
}
