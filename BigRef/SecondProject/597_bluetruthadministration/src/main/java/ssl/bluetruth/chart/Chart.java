/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart;

import ssl.bluetruth.chart.writer.ChartWriter;
import ssl.bluetruth.chart.writer.ChartWriterFactory;
import ssl.bluetruth.chart.plots.TimeSeriesIntegerProcessor;
import ssl.bluetruth.chart.plots.TimeSeriesFloatProcessor;
import java.util.Collection;
import ssl.bluetruth.chart.query.RowProcessor;
import ssl.bluetruth.chart.style.axis.NumberAxisStyleNoMax;
import ssl.bluetruth.chart.style.chart.XYScatter;
import ssl.bluetruth.chart.style.chart.XYStackedArea;
import java.awt.Color;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import javax.naming.NamingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.chart.actions.CreateChartAction;
import ssl.bluetruth.common.BlueTruthException;
import ssl.bluetruth.chart.common.Period;
import ssl.bluetruth.chart.common.Rectangle;
import ssl.bluetruth.chart.plots.GroupedTimeSeriesFloatProcessor;
import ssl.bluetruth.chart.plots.ScatterMultiSeriesTotalProcessor;
import ssl.bluetruth.chart.plots.StackPointProcessor;
import ssl.bluetruth.chart.query.AggregateRowProcessor;
import ssl.bluetruth.chart.style.chart.ChartStyle;
import ssl.bluetruth.chart.query.Query;
import ssl.bluetruth.chart.query.RecordSet;
import ssl.bluetruth.chart.query.RowProcessorBuilder;
import ssl.bluetruth.chart.style.axis.AxisStyle;
import ssl.bluetruth.chart.style.series.ColoredLinesStyle;
import ssl.bluetruth.chart.style.series.CrossesStyle;
import ssl.bluetruth.chart.style.axis.DateAxisStyle;
import ssl.bluetruth.chart.style.series.LinesStyle;
import ssl.bluetruth.chart.style.axis.NumberAxisStyle;
import ssl.bluetruth.chart.style.series.PointsStyle;
import ssl.bluetruth.chart.style.axis.RelativeDateAxisStyle;
import ssl.bluetruth.chart.style.series.SeriesStyle;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;

/**
 *
 * @author pwood
 */
public class Chart {

    private int Id;
    private final ChartWriterFactory chartWriterFactory = new ChartWriterFactory();
    private final ChartWriter chartWriter;
    private final Query query;
    private final RowProcessor rowProcessor;
    private final String parameters;
    public static final String COMMA_SPACE = ", ";
    private final Period period;
    private static final String CREATE_CHART_SQL = "INSERT INTO chart (parameters, type) VALUES (?,?) RETURNING id;";
    private static final String GET_CHART_SQL = "SELECT Id FROM chart WHERE parameters = ? AND type = ? ORDER BY created_timestamp DESC";

    private static final Logger LOGGER = LogManager.getLogger(Chart.class);
    
    public Chart(String parameters, Period period, Rectangle chartSize, String mimetype, String title, String subtitle,
            ChartStyle chartStyle, AxisStyle xAxis, AxisStyle yAxis, Query query,
            RowProcessorBuilder...rowProcessorBuilders) throws BlueTruthException {
        this.parameters = parameters;
        this.period = period;
        this.chartWriter = chartWriterFactory.create(mimetype, title, subtitle,
                chartStyle, xAxis, yAxis, chartSize);
        this.query = query;
        this.rowProcessor = buildRowProcessor(rowProcessorBuilders);

        if (mimetype.equals("image/png")) {
            this.Id = createId();
        }
    }

    public String getMimeType() {
        return chartWriter.getMimeType();
    }

    public void write(OutputStream outputStream) throws BlueTruthException {
        query.iterateResults(rowProcessor);
        rowProcessor.modify(chartWriter);
        chartWriter.write(outputStream);
        query.getFieldNames().clear();
    }

    protected static Query query(final String sql, RecordSet fieldNames, boolean fillNullValuesWithZero, Object... queryParams) {
        return new Query(sql, fieldNames, fillNullValuesWithZero, queryParams);
    }

    protected static ChartStyle xyScatter() {
        return new XYScatter();
    }

    protected static ChartStyle xyStackedArea() {
        return new XYStackedArea();
    }

    protected static RowProcessorBuilder intTimeSeries(final String seriesKey,
            final String xField, final String yField, final SeriesStyle style) {
        return new RowProcessorBuilder(
                new TimeSeriesIntegerProcessor(seriesKey, xField, yField, style));
    }

    protected static RowProcessorBuilder floatTimeSeries(final String seriesKey,
            final String xField, final String yField, final SeriesStyle style) {
        return new RowProcessorBuilder(
                new TimeSeriesFloatProcessor(seriesKey, xField, yField, style));
    }

    protected static RowProcessorBuilder stack(
            final String xField, final String yField, final String group) {
        return new RowProcessorBuilder(
                new StackPointProcessor(xField, yField, group));
    }

    protected static RowProcessorBuilder multiSeriesTotal(
            final String xField, final String yField, final String group) {
        return new RowProcessorBuilder(
                new ScatterMultiSeriesTotalProcessor(xField, yField, group));
    }    
    
    protected static RowProcessorBuilder multipleFloatSeries(final String xField,
            final String yField, final String groupField, final SeriesStyle style) {
        return new RowProcessorBuilder(
                new GroupedTimeSeriesFloatProcessor(xField, yField, groupField, style));
    }

    protected static RecordSet fillFieldNames(final String xField, final String... yField) {
        RecordSet fieldNames = new RecordSet();
        fieldNames.setxField(xField);
        for (String yFieldValue : yField) {
            fieldNames.setyField(yFieldValue);
        }
        return fieldNames;
    }

    protected static RecordSet fillFieldNamesWithPredectedField(final String xField, final String predicatedFieldName, final String... yField) {
        RecordSet fieldNames = fillFieldNames(xField, yField);
        fieldNames.setPredicatedFieldName(predicatedFieldName);
        return fieldNames;
    }

    protected static RecordSet fillFieldNames(final String xField, final String additionalFieldName, final String... yField) {
        RecordSet fieldNames = fillFieldNames(xField, yField);
        fieldNames.setAdditionalFieldName(additionalFieldName);
        return fieldNames;
    }

    protected static Timestamp timestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    protected static CrossesStyle crosses(Color color) {
        return new CrossesStyle(color);
    }

    protected static LinesStyle lines() {
        return new LinesStyle();
    }

    protected static LinesStyle lines(Color color) {
        return new ColoredLinesStyle(color);
    }

    protected static PointsStyle points(Color color) {
        return new PointsStyle(color);
    }

    protected static AxisStyle dateAxis(String label, String format, Period period) {
        return new DateAxisStyle(label, format, period);
    }

    protected static AxisStyle relativeDateAxis(String label, String format, long maxValue) {
        return new RelativeDateAxisStyle(label, format, maxValue);
    }

    protected static AxisStyle relativeDateAxis(String label, String format) {
        return new RelativeDateAxisStyle(label, format);
    }

    protected static AxisStyle numberAxis(String label, int max) {
        return new NumberAxisStyle(label, max);
    }

    protected static AxisStyle numberAxis(String label) {
        return new NumberAxisStyleNoMax(label);
    }

    private static RowProcessor buildRowProcessor(RowProcessorBuilder...rowProcessorBuilders) {
        Collection<RowProcessor> rowProcessors =
                new ArrayList<RowProcessor>(rowProcessorBuilders.length);
        for(RowProcessorBuilder builder: rowProcessorBuilders) {
            rowProcessors.add(builder.build());
        }
        return new AggregateRowProcessor(rowProcessors);
    }

    private int createId() {
        long now = Instant.now().toEpochMilli();
        int chartId = getChartId();
        
        if (chartId == -1 || (period.fromDate().getTime() >= now) || (period.toDate().getTime() >= now)) {
            chartId = setId();
            
            //CREATE CHART
            CreateChartAction action = new CreateChartAction(this);
            action.actionPerformed();
        }
        return chartId;
    } 
    
    private int setId() {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getInstance().getDatasource().getConnection();
            ps = conn.prepareStatement(CREATE_CHART_SQL);
            ps.setString(1, parameters);
            ps.setString(2, getChartType());
            rs = ps.executeQuery();
            if (rs.next()) {
                Id = rs.getInt(1);
            }
        } catch (SQLException | DatabaseManagerException | NamingException ex) {
            LOGGER.error("Exception occured while creating chart entry in the database" + ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close prepared statement");
            }
            
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close database connection");
            }

            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close resultset");
            }
        }
        return Id;
    }
    
    /**
     * Return chart Id if exists
     * @return chart Id 
     */
    private int getChartId() {
        
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int chartId = -1;
        try {
            connection = DatabaseManager.getInstance().getDatasource().getConnection();
            ps = connection.prepareStatement(GET_CHART_SQL);
            ps.setString(1, parameters);
            ps.setString(2, getChartType());
            rs = ps.executeQuery();
            if (rs.next()) {
                chartId = rs.getInt(1);
            }
        } catch (SQLException | DatabaseManagerException | NamingException ex) {
            LOGGER.error("Exception occured while getting chart Id from the database" + ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close prepared statement");
            }
            
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close database connection");
            }

            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception, failed to close resultset");
            }
        }
        return chartId;
    }
    
    public int getId() {
        return Id;
    }
    
    private String getChartType() {
        return this.getClass().getSimpleName();
    }
}
