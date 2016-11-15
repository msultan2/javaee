/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.json;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import ssl.bluetruth.chart.common.FloatPoint;
import ssl.bluetruth.chart.common.Point;
import ssl.bluetruth.chart.style.axis.AxisStyle;
import ssl.bluetruth.chart.style.chart.ChartStyle;
import ssl.bluetruth.chart.style.series.SeriesStyle;
import ssl.bluetruth.chart.writer.ChartWriter;
import ssl.bluetruth.common.BlueTruthException;

/**
 *
 * @author pwood
 */
public class JsonChartWriter implements ChartWriter {
    private final String title;
    private final String subtitle;
    private final ChartStyle chartStyle;
    private final AxisStyle xAxis;
    private final AxisStyle yAxis;

    public JsonChartWriter(String title, String subtitle, ChartStyle chartStyle, AxisStyle xAxis, AxisStyle yAxis) {
        this.title = title;
        this.subtitle = subtitle;
        this.chartStyle = chartStyle;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    @Override
    public void addFloatSeries(String seriesKey, Collection<FloatPoint> points, SeriesStyle style) {
        // to do
    }

    @Override
    public void addIntegerSeries(String seriesKey, Collection<Point> points, SeriesStyle style) {
        // to do
    }

    @Override
    public void addStackedArea(Map<String, Collection<FloatPoint>> areaPoints) {
        // to do
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public void write(OutputStream outputStream) throws BlueTruthException {
        try {
            outputStream.write("{}".getBytes());
        } catch (IOException ex) {
            throw new BlueTruthException(ex);
        }
    }
}
