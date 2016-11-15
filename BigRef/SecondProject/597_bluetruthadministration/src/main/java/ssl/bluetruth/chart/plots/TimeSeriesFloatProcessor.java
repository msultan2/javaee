/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.plots;

import java.sql.Timestamp;
import ssl.bluetruth.chart.query.RowProcessor;
import ssl.bluetruth.chart.style.series.SeriesStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import ssl.bluetruth.chart.common.FloatPoint;
import ssl.bluetruth.chart.writer.ChartWriter;

/**
 *
 * @author pwood
 */
public class TimeSeriesFloatProcessor implements RowProcessor {
    private final String seriesKey;
    private final String xField;
    private final String yField;
    private final Collection<FloatPoint> points = new ArrayList<FloatPoint>();
    private final SeriesStyle style;

    public TimeSeriesFloatProcessor(String seriesKey, String xField, String yField, SeriesStyle style) {
        this.seriesKey = seriesKey;
        this.xField = xField;
        this.yField = yField;
        this.style = style;
    }

    public void modify(ChartWriter chart) {
        chart.addFloatSeries(seriesKey, points, style);
    }

    public void plot(Map map) {
        long xFieldValue = ((Timestamp) map.get(xField)).getTime();
        double yFieldValue = (double) map.get(yField);
        points.add(new FloatPoint(xFieldValue, yFieldValue));
    }
}
