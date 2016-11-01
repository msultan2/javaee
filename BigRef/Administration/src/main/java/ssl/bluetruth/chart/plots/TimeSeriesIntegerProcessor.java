/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.plots;

import java.sql.Timestamp;
import ssl.bluetruth.chart.common.Point;
import ssl.bluetruth.chart.query.RowProcessor;
import ssl.bluetruth.chart.style.series.SeriesStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import ssl.bluetruth.chart.writer.ChartWriter;

/**
 *
 * @author pwood
 */
public class TimeSeriesIntegerProcessor implements RowProcessor {
    private final String seriesKey;
    private final String xField;
    private final String yField;
    private final Collection<Point> points = new ArrayList<Point>();
    private final SeriesStyle style;

    public TimeSeriesIntegerProcessor(String seriesKey, String xField, String yField, SeriesStyle style) {
        this.seriesKey = seriesKey;
        this.xField = xField;
        this.yField = yField;
        this.style = style;
    }

    public void modify(ChartWriter chart) {
        chart.addIntegerSeries(seriesKey, points, style);
    }

    public void plot(Map map) {
        long xFieldValue = ((Timestamp) map.get(xField)).getTime();
        int yFieldValue = (int) (double) map.get(yField);
        points.add(new Point(xFieldValue, yFieldValue));
    }
}
