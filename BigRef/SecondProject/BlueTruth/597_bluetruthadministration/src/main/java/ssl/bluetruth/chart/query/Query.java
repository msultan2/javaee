/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.chart.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.naming.NamingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.common.BlueTruthException;

public class Query {

    private final String sql;
    private final List queryParams;
    private final String zeroFillingSQL;
    private final boolean appendZeroFilling;
    private final RecordSet fieldNames;
    private static final Logger LOGGER = LogManager.getLogger(Query.class);

    private static final String FILLING_INTERVAL_IN_MINUTES = "5";
    private static final String CREATE_ZERO_VALUES_IN_CHART = "SELECT date_trunc('H',Series)+floor(EXTRACT('minute' FROM Series)/" + FILLING_INTERVAL_IN_MINUTES + ")*" + FILLING_INTERVAL_IN_MINUTES + " * '1 minute'::interval time_series,0 zero "
            + "from generate_series('%s'::timestamp,'%s', '" + FILLING_INTERVAL_IN_MINUTES + " minutes') Series";

    public Query(String sql, RecordSet fieldNames, boolean fillNullValuesWithZero, Object... queryParams) {
        this.sql = sql;
        this.fieldNames = fieldNames;
        this.queryParams = Arrays.asList(queryParams);
        this.zeroFillingSQL = String.format(CREATE_ZERO_VALUES_IN_CHART, this.queryParams.get(1), this.queryParams.get(2));
        this.appendZeroFilling = fillNullValuesWithZero;
    }

    public RecordSet getFieldNames() {
        return fieldNames;
    }

    private Map<String, Object> getChartData() throws SQLException, DatabaseManagerException, NamingException {
        Connection connection = DatabaseManager.getInstance().getDatasource().getConnection();
        PreparedStatement stmt;
        ResultSet rs;
        stmt = connection.prepareStatement(sql);
        for (int index = 0; index < queryParams.size(); ++index) {
            stmt.setObject(index + 1, queryParams.get(index));
        }
        rs = stmt.executeQuery();
        Map<String, Object> chartData = readResultSetInChartData(rs);
        stmt.close();
        rs.close();
        connection.close();
        return chartData;
    }

    public void iterateResults(RowProcessor resultSetProcessor) throws BlueTruthException {
        try {
            Map<String, Object> chartData = getChartData();
            if (appendZeroFilling) {
                chartData = fillChartDataWithZeros(chartData);
            }
            plotChartData(chartData, resultSetProcessor);
        } catch (SQLException | NamingException | DatabaseManagerException ex) {
            throw new BlueTruthException(ex);
        } catch (Exception ex) {
            LOGGER.error("Exception: {}", ex);
        }
    }

    private Map<String, Object> fillChartDataWithZeros(Map<String, Object> chartData) throws DatabaseManagerException, NamingException, SQLException {
        Map<String, Object> zeroFillingData = getZeroFillingData();
        return mergeZeroFillerWithChartData(zeroFillingData, chartData);
    }

    private Map<String, Object> getZeroFillingData() throws SQLException, DatabaseManagerException, NamingException {
        Connection connection = DatabaseManager.getInstance().getDatasource().getConnection();
        Statement zeroStmt = connection.createStatement();
        ResultSet zeroFillingRS;
        zeroFillingRS = zeroStmt.executeQuery(zeroFillingSQL);
        Map<String, Object> zeroFillingData = readZeroFillingResultSetInChartData(zeroFillingRS);
        zeroStmt.close();
        zeroFillingRS.close();
        connection.close();
        return zeroFillingData;
    }

    private Map<String, Object> mergeZeroFillerWithChartData(Map<String, Object> zeroFillingData, Map<String, Object> chartData) {
        Map<String, Object> combinedData = zeroFillingData;
        chartData.entrySet().stream()
                .forEach(chartDataValue -> combinedData.put(chartDataValue.getKey(), chartDataValue.getValue()));
        return combinedData;
    }

    private Map<String, Object> readResultSetInChartData(ResultSet rs) throws SQLException {
        Map<String, Object> data = new TreeMap<>();
        while (rs.next()) {
            Map<String, Object> chartData = new HashMap<>();
            if (isValid(fieldNames.getxField())) {
                chartData.put(fieldNames.getxField(), rs.getTimestamp(fieldNames.getxField()));
            }
            if (isValid(fieldNames.getyField())) {
                for (String fieldName : fieldNames.getyField()) {
                    chartData.put(fieldName, rs.getDouble(fieldName));
                }
            }
            if (isValid(fieldNames.getPredicatedFieldName())) {
                chartData.put(fieldNames.getPredicatedFieldName(), rs.getBoolean(fieldNames.getPredicatedFieldName()));
            }
            if (isValid(fieldNames.getAdditionalFieldName())) {
                chartData.put(fieldNames.getAdditionalFieldName(), rs.getString(fieldNames.getAdditionalFieldName()));
            }
            data.put(rs.getString(fieldNames.getxField()), chartData);
        }
        return data;
    }

    private Map<String, Object> readZeroFillingResultSetInChartData(ResultSet rs) throws SQLException {
        Map<String, Object> data = new TreeMap<>();
        while (rs.next()) {
            Map<String, Object> zeroChartData = new HashMap<>();
            if (isValid(fieldNames.getxField())) {
                zeroChartData.put(fieldNames.getxField(), rs.getTimestamp("time_series"));
            }
            if (isValid(fieldNames.getyField())) {
                fieldNames.getyField().stream().forEach(fieldName -> zeroChartData.put(fieldName, 0d));
            }
            if (isValid(fieldNames.getAdditionalFieldName())) {
                zeroChartData.put(fieldNames.getAdditionalFieldName(), false);
            }
            data.put(rs.getString("time_series"), zeroChartData);
        }
        return data;
    }

    private boolean isValid(String data) {
        return !(data == null || data.isEmpty());
    }

    private boolean isValid(Set<String> data) {
        boolean isValid = false;
        if (!data.isEmpty()) {
            for (String fieldName : data) {
                if (!fieldName.isEmpty()) {
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    private void plotChartData(Map<String, Object> combinedData, RowProcessor resultSetProcessor) {
        combinedData.entrySet().stream()
                .forEach(combinedDataValue -> resultSetProcessor.plot(convertMapElementToSingleRecord(combinedDataValue)));
    }

    private Map<String, Object> convertMapElementToSingleRecord(Entry<String, Object> dataValue) {
        return (Map) dataValue.getValue();
    }
}
