/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ssl.utils.datatable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Parses request parameters and constructs filter objects based on the
 * requested filter type.
 * @author katemallichan
 */
public final class DataTableFieldFilterFactory {

    private DataTableFieldFilterFactory() {
        //Private constructor
    }
    
    /**
     * Creates an object of a class implementing the AuthenticationFieldFilter
     * interface which provides a mechanism for generating SQL where clauses to
     * implement the query filter.
     * @param filterType The data type for the column. From the FilterType
     * enumeration. This factory does not support the ENUM type as it is not
     * relevant to authentication tables in the Radius database.
     * @param colNumber The column index of the filter
     * @param colName
     * @param params
     * @return a field filter under the AuthenticationFieldFilter interface.
     * @throws ParseException - If the filter parameters could not be parsed
     * successfully.
     * @throws DataTablesException if the filter type was null or unrecognised.
     */

    public static DataTableFieldFilter createFieldFilter(ColumnType filterType, int colNumber, String colName, Map<String, List<String>> params) throws ParseException, DataTablesException {
        return createFieldFilter(filterType, null, colNumber, colName, params);
    }

    public static DataTableFieldFilter createFieldFilter(ColumnType filterType, Class<? extends Enum> enumType, int colNumber, String colName, Map<String, List<String>> params) throws ParseException, DataTablesException {
        DataTableFieldFilter filter = null;

        String minVal;
        String maxVal;
        String minValIncl;
        String maxValIncl;

        String colStr = String.valueOf(colNumber);

        List<String> sSearch = params.get(DataTableRequestParamStrings.filterStringPrefix + colStr);
        List<String> sFilterMin = params.get(DataTableRequestParamStrings.filterMinPrefix + colStr);
        List<String> sFilterMax = params.get(DataTableRequestParamStrings.filterMaxPrefix + colStr);
        List<String> sFilterMinIncl = params.get(DataTableRequestParamStrings.filterMinInclPrefix + colStr);
        List<String> sFilterMaxIncl = params.get(DataTableRequestParamStrings.filterMaxInclPrefix + colStr);

        switch (filterType) {
            case STRING:
            {
                if (sSearch == null || sSearch.size() != 1) {
                    throw new DataTablesException("Filter of type STRING specified for a column, but no filter value in parameters.");
                } else {
                    String filterString = sSearch.get(0);
                    if (filterString != null) {
                        filter = new StringFieldFilter(colName, filterString);
                    }
                }
                break;
            }
            case MULTISTRING:
            {
                if (sSearch == null || sSearch.size() != 1) {
                    throw new DataTablesException("Filter of type STRING specified for a column, but no filter value in parameters.");
                } else {
                    String filterString = sSearch.get(0);
                    if (filterString != null) {
                        filter = new MultiStringFieldFilter(colName, filterString);
                    }
                }
                break;
            }
            case SHORT:
            case INTEGER:
            case LONG:
            case DOUBLE:
            {
                minVal = null;
                maxVal = null;
                minValIncl = "false";
                maxValIncl = "false";

                boolean invalidFilterParameters =
                    (sFilterMin != null && sFilterMin.size() != 1) ||
                    (sFilterMax != null && sFilterMax.size() != 1) ||
                    (sFilterMinIncl != null && sFilterMinIncl.size() != 1) ||
                    (sFilterMaxIncl != null && sFilterMaxIncl.size() != 1);

                if (invalidFilterParameters) {
                    throw new DataTablesException("Filter of type NUMERIC specified for column:" + colName + " but filter parameters were missing or incorrectly specified");
                }

                if (sFilterMin != null) {
                    minVal = sFilterMin.get(0);
                }
                if (sFilterMax != null) {
                    maxVal = sFilterMax.get(0);
                }
                if (sFilterMinIncl != null) {
                    minValIncl = sFilterMinIncl.get(0);
                }
                if (sFilterMaxIncl != null) {
                    maxValIncl = sFilterMaxIncl.get(0);
                }

                try {
                    switch (filterType) {
                        case SHORT: {
                            filter = new ShortFieldFilter(colName,
                                    (minVal == null ? null : Short.valueOf(minVal)),
                                    (maxVal == null ? null : Short.valueOf(maxVal)),
                                    Boolean.parseBoolean(minValIncl),
                                    Boolean.parseBoolean(maxValIncl));
                            break;
                        }
                        case INTEGER: {
                            filter = new IntegerFieldFilter(colName,
                                    (minVal == null ? null : Integer.valueOf(minVal)),
                                    (maxVal == null ? null : Integer.valueOf(maxVal)),
                                    Boolean.parseBoolean(minValIncl),
                                    Boolean.parseBoolean(maxValIncl));
                            break;
                        }
                        case LONG: {
                            filter = new LongFieldFilter(colName,
                                    (minVal == null ? null : Long.valueOf(minVal)),
                                    (maxVal == null ? null : Long.valueOf(maxVal)),
                                    Boolean.parseBoolean(minValIncl),
                                    Boolean.parseBoolean(maxValIncl));
                            break;
                        }
                        case DOUBLE: {
                            filter = new DoubleFieldFilter(colName,
                                    (minVal == null ? null : Double.valueOf(minVal)),
                                    (maxVal == null ? null : Double.valueOf(maxVal)),
                                    Boolean.parseBoolean(minValIncl),
                                    Boolean.parseBoolean(maxValIncl));
                            break;
                        }
                    }
                } catch (NumberFormatException ex) {
                    throw new DataTablesException("Filter of type NUMERIC specified for column:" + colName + " but filter parameters were missing or incorrectly specified", ex);
                }
                break;
            }
            case DATE:
            {
                minVal = null;
                maxVal = null;
                minValIncl = "false";
                maxValIncl = "false";

                boolean invalidFilterParameters =
                    (sFilterMin != null && sFilterMin.size() != 1) ||
                    (sFilterMax != null && sFilterMax.size() != 1) ||
                    (sFilterMinIncl != null && sFilterMinIncl.size() != 1) ||
                    (sFilterMaxIncl != null && sFilterMaxIncl.size() != 1);

                if (invalidFilterParameters) {
                    throw new DataTablesException("Filter of type DATE specified for column:" + colName + " but filter parameters were missing or incorrectly specified");
                }

                if (sFilterMin != null) {
                    minVal = sFilterMin.get(0);
                }
                if (sFilterMax != null) {
                    maxVal = sFilterMax.get(0);
                }
                if (sFilterMinIncl != null) {
                    minValIncl = sFilterMinIncl.get(0);
                }
                if (sFilterMaxIncl != null) {
                    maxValIncl = sFilterMaxIncl.get(0);
                }

                SimpleDateFormat[] dateFormats = {
                    new SimpleDateFormat(
                        DataTableRequestParamStrings.dateComponentFormat +
                        " " +
                        DataTableRequestParamStrings.timeComponentFormat +
                        DataTableRequestParamStrings.optionalMillisComponentFormat, Locale.UK),
                    new SimpleDateFormat(
                        DataTableRequestParamStrings.dateComponentFormat +
                        " " +
                        DataTableRequestParamStrings.timeComponentFormat, Locale.UK),
                    new SimpleDateFormat(
                        DataTableRequestParamStrings.timeComponentFormat +
                        DataTableRequestParamStrings.optionalMillisComponentFormat, Locale.UK),
                    new SimpleDateFormat(
                        DataTableRequestParamStrings.timeComponentFormat, Locale.UK),
                    new SimpleDateFormat(
                        DataTableRequestParamStrings.dateComponentFormat, Locale.UK)
                };


                Date minDate = null;
                Date maxDate = null;
                if (minVal != null) {
                    for (int i = 0; i < dateFormats.length; i++) {
                        try {
                            minDate = dateFormats[i].parse(minVal);
                            break;
                        } catch (ParseException ex) {
                            //do nothing
                        }
                    }
                    if (minDate == null) {
                        throw new DataTablesException("Filter of type DATE specified for column:" + colName + " but filter parameters were missing or incorrectly specified");
                    }
                }

                if (maxVal != null) {
                    for (int i = 0; i < dateFormats.length; i++) {
                        try {
                            maxDate = dateFormats[i].parse(maxVal);
                            break;
                        } catch (ParseException ex) {
                            //do nothing
                        }
                    }
                    if (maxDate == null) {
                        throw new DataTablesException("Filter of type DATE specified for column:" + colName + " but filter parameters were missing or incorrectly specified");
                    }
                }

                try {
                    filter = new DateFieldFilter(colName,
                            minDate,
                            maxDate,
                            Boolean.parseBoolean(minValIncl),
                            Boolean.parseBoolean(maxValIncl));
                } catch (NumberFormatException ex) {
                    throw new DataTablesException("Filter of type DATE specified for column:" + colName + " but filter parameters were missing or incorrectly specified", ex);
                }
                break;
            }
            case INTERVAL:
            {
                minVal = null;
                maxVal = null;
                minValIncl = "false";
                maxValIncl = "false";

                boolean invalidFilterParameters =
                    (sFilterMin != null && sFilterMin.size() != 1) ||
                    (sFilterMax != null && sFilterMax.size() != 1) ||
                    (sFilterMinIncl != null && sFilterMinIncl.size() != 1) ||
                    (sFilterMaxIncl != null && sFilterMaxIncl.size() != 1);

                if (invalidFilterParameters) {
                    throw new DataTablesException("Filter of type INTERVAL specified for column:" + colName + " but filter parameters were missing or incorrectly specified");
                }

                if (sFilterMin != null) {
                    minVal = sFilterMin.get(0);
                }
                if (sFilterMax != null) {
                    maxVal = sFilterMax.get(0);
                }
                if (sFilterMinIncl != null) {
                    minValIncl = sFilterMinIncl.get(0);
                }
                if (sFilterMaxIncl != null) {
                    maxValIncl = sFilterMaxIncl.get(0);
                }

                String minInterval = null;
                String maxInterval = null;
                if (minVal != null) {
                    minInterval = minVal;
                    if (minInterval == null) {
                        throw new DataTablesException("Filter of type INTERVAL specified for column:" + colName + " but filter parameters were missing or incorrectly specified");
                    }
                }
                if (maxVal != null) {
                    maxInterval = maxVal;
                    if (maxInterval == null) {
                        throw new DataTablesException("Filter of type INTERVAL specified for column:" + colName + " but filter parameters were missing or incorrectly specified");
                    }
                }
                try {
                    filter = new IntervalFieldFilter(colName,
                            minInterval,
                            maxInterval,
                            Boolean.parseBoolean(minValIncl),
                            Boolean.parseBoolean(maxValIncl));
                } catch (NumberFormatException ex) {
                    throw new DataTablesException("Filter of type INTERVAL specified for column:" + colName + " but filter parameters were missing or incorrectly specified", ex);
                }
                break;
            }
            case BOOLEAN:
            {
                if (sSearch == null || sSearch.size() != 1) {
                    throw new DataTablesException("Filter of type BOOLEAN specified for a column, but no filter value in parameters.");
                }
                String booleanFilterVal;
                try {
                    booleanFilterVal = sSearch.get(0);
                } catch (NullPointerException ex) {
                    throw new DataTablesException("Filter of type BOOLEAN specified for column:" + colName + " but filter parameters were missing or incorrectly specified", ex);
                }
                filter = new BooleanFieldFilter(colName, Boolean.parseBoolean(booleanFilterVal));
                break;
            }
            case ENUM:
            {
                if (sSearch == null || sSearch.size() != 1) {
                    throw new DataTablesException("Filter of type ENUM specified for a column, but no filter value in parameters.");
                }
                String enumConcatValues;
                try {
                    enumConcatValues = sSearch.get(0);
                } catch (NullPointerException ex) {
                    throw new DataTablesException("Filter of type ENUM specified for column:" + colName + " but filter parameters were missing or incorrectly specified", ex);
                }
                String[] enumValueNames = enumConcatValues.split(",");
                filter = new EnumFieldFilter(enumType, colName, enumValueNames);
                break;
            }
        }

        return filter;
    }

}
