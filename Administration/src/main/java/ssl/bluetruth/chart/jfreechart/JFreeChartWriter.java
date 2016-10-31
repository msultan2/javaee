/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.jfreechart;

import ssl.bluetruth.chart.style.chart.ChartStyle;
import ssl.bluetruth.chart.style.chart.ChartStyler;
import ssl.bluetruth.chart.style.series.SeriesStyle;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ssl.bluetruth.chart.writer.ChartWriter;
import ssl.bluetruth.common.BlueTruthException;
import ssl.bluetruth.chart.common.FloatPoint;
import ssl.bluetruth.chart.common.Point;
import ssl.bluetruth.chart.common.Rectangle;
import ssl.bluetruth.chart.style.axis.AxisStyle;

/**
 *
 * @author pwood
 */
public class JFreeChartWriter implements ChartWriter {
    private final XYSeriesCollection seriesDataset = new XYSeriesCollection();
    private final XYSeriesCollection floatSeriesDataset = new XYSeriesCollection();
    private final DefaultTableXYDataset tableDataset = new DefaultTableXYDataset();
    private final Map<Integer, SeriesStyle> seriesStyles = new HashMap<Integer, SeriesStyle>();
    private final String title;
    private final String subtitle;
    private final ChartStyle chartStyle;
    private final AxisStyle xAxisStyle;
    private final AxisStyle yAxisStyle;
    private final Rectangle chartSize;

    public JFreeChartWriter(String title, String subtitle, ChartStyle chartStyle,
            AxisStyle xAxisStyle, AxisStyle yAxisStyle, Rectangle chartSize) {
        this.title = title;
        this.subtitle = subtitle;
        this.chartStyle = chartStyle;
        this.xAxisStyle = xAxisStyle;
        this.yAxisStyle = yAxisStyle;
        this.chartSize = chartSize;
    }

    @Override
    public String getMimeType() {
        return "image/png";
    }

    @Override
    public void write(OutputStream outputStream) throws BlueTruthException {
        write(outputStream, chart(), chartSize);
    }

    @Override
    public void addIntegerSeries(String seriesKey, Collection<Point> points, SeriesStyle style) {
        seriesDataset.addSeries(createJFreeChartIntegerSeries(seriesKey, points, true));
        seriesStyles.put(seriesDataset.getSeriesIndex(seriesKey), style);
    }

    @Override
    public void addFloatSeries(String seriesKey, Collection<FloatPoint> points, SeriesStyle style) {
        floatSeriesDataset.addSeries(createJFreeChartFloatSeries(seriesKey, points, true));
        seriesStyles.put(floatSeriesDataset.getSeriesIndex(seriesKey), style);
    }

    @Override
    public void addStackedArea(Map<String, Collection<FloatPoint>> areaPoints) {
        for(Entry<String, Collection<FloatPoint>> area: areaPoints.entrySet()) {
            tableDataset.addSeries(createJFreeChartFloatSeries(area.getKey(), area.getValue(), false));
        }
    }

    private XYDataset getData() {
        if(seriesDataset.getSeriesCount() != 0) {
            return seriesDataset;
        } else if(tableDataset.getSeriesCount() != 0) {
            return tableDataset;
        } else if(floatSeriesDataset.getSeriesCount() != 0) {
            return floatSeriesDataset;
        }
        return new DefaultXYDataset();
    }

    private void write(OutputStream outputStream, JFreeChart chart, Rectangle chartSize) throws BlueTruthException {
        try {
            ChartUtilities.writeChartAsPNG(outputStream, chart, chartSize.width, chartSize.height);
        } catch (IOException ex) {
            throw new BlueTruthException(ex);
        }
    }

    private JFreeChart chart() {
        JFreeChartStyler chartStyler = new JFreeChartStyler(title, subtitle, getData());
        chartStyle.create(chartStyler);
        chartStyler.applyAxisStyles(xAxisStyle, yAxisStyle);
        applySeriesStyles(chartStyler);
        return chartStyler.chart();
    }

    private void applySeriesStyles(ChartStyler chartStyler) {
        for (Entry<Integer, SeriesStyle> styleEntries : seriesStyles.entrySet()) {
            SeriesStyle seriesStyle = styleEntries.getValue();
            int series = styleEntries.getKey();
            chartStyler.applySeriesStyle(series, seriesStyle);
        }
    }

    private XYSeries createJFreeChartIntegerSeries(String seriesKey, Collection<Point> points,
            boolean allowDuplicates) {
        XYSeries series = new XYSeries(seriesKey, false, allowDuplicates);
        for (Point point : points) {
            series.add(point.x, point.y);
        }
        return series;
    }

    private XYSeries createJFreeChartFloatSeries(String seriesKey, Collection<FloatPoint> points,
            boolean allowDuplicates) {
        XYSeries series = new XYSeries(seriesKey, false, allowDuplicates);
        for (FloatPoint point : points) {
            series.add(point.x, point.y);
        }
        return series;
    }
}
