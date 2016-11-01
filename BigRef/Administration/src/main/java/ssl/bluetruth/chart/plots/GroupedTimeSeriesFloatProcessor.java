/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.plots;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import ssl.bluetruth.chart.writer.ChartWriter;
import ssl.bluetruth.chart.common.FloatPoint;
import ssl.bluetruth.chart.query.RowProcessor;
import ssl.bluetruth.chart.style.series.SeriesStyle;

/**
 *
 * @author pwood
 */
public class GroupedTimeSeriesFloatProcessor implements RowProcessor {
    private final String xField;
    private final String yField;
    private final Map<String, Collection<FloatPoint>> groupPoints =
            new HashMap<String, Collection<FloatPoint>>();
    private final String groupField;
    private final SeriesStyle style;

    public GroupedTimeSeriesFloatProcessor(String xField, String yField,
            String groupField, SeriesStyle style) {
        this.xField = xField;
        this.yField = yField;
        this.groupField = groupField;
        this.style = style;
    }

    public void modify(ChartWriter chart) {
        for(Entry<String, Collection<FloatPoint>> entries: groupPoints.entrySet()) {
            chart.addFloatSeries(entries.getKey(), entries.getValue(), style);
        }
    }

    private Collection<FloatPoint> groupPoints(String group) {
        Collection<FloatPoint> points = groupPoints.get(group);
        if(points == null) {
            points = new ArrayList<FloatPoint>();
            groupPoints.put(group, points);
        }
        return points;
    }

    public void plot(Map map) {
        double xFieldValue = (double) ((Timestamp) map.get(xField)).getTime();
        double yFieldValue = (double) map.get(yField);
        String group = (String) map.get(groupField);
        groupPoints(group).add(new FloatPoint(xFieldValue, yFieldValue));
    }
}
