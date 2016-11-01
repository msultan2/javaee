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
import java.util.SortedSet;
import java.util.TreeSet;
import ssl.bluetruth.chart.common.FloatPoint;
import ssl.bluetruth.chart.common.Point;
import ssl.bluetruth.chart.query.RowProcessor;
import ssl.bluetruth.chart.writer.ChartWriter;

/**
 *
 * @author pwood
 */
public class StackPointProcessor implements RowProcessor {
    private final String xField;
    private final String yField;
    private final String groupField;
    private final Map<String, Collection<Point>> areas =
            new HashMap<String, Collection<Point>>();
    private SortedSet<Long> allXes = new TreeSet<Long>();

    public StackPointProcessor(String xField, String yField,
            String groupField) {
        this.xField = xField;
        this.yField = yField;
        this.groupField = groupField;
    }

    public void modify(ChartWriter chart) {
        chart.addStackedArea(makeTable());
    }

    private Map<String, Collection<FloatPoint>> makeTable() {
        Map<String, Collection<FloatPoint>> table = new HashMap<String, Collection<FloatPoint>>();
        for(Entry<String, Collection<Point>> area: areas.entrySet()) {
            String areaName = area.getKey();
            Collection<Point> areaPoints = area.getValue();
            Collection<FloatPoint> newAreaPoints = new ArrayList<FloatPoint>();
            table.put(areaName, newAreaPoints);
            for(Long x: allXes) {
                Point lastPoint = null;
                for(Point point: areaPoints) {
                    if(point.x == x) {
                        newAreaPoints.add(new FloatPoint(point.x, point.y));
                        break;
                    } else if(lastPoint == null) {
                        if((point.x > x)) {
                            newAreaPoints.add(new FloatPoint(x, point.y));
                            break;
                        } else {
                            lastPoint = point;
                        }
                    } else {
                        if((point.x > x) && (lastPoint.x < x)) {
                            newAreaPoints.add(lerp(lastPoint, point, x));
                            break;
                        } else {
                            // do nothing
                        }
                        lastPoint = point;
                    }
                }
            }
        }
        return table;
    }

    private Point point(long x, long y) {
        allXes.add(x);
        return new Point(x, y);
    }

    private FloatPoint lerp(Point first, Point second, long between) {
        double xDiff = second.x - first.x;
        double yDiff = second.y - first.y;
        double xPart = between - first.x;
        double xRatio = xPart / xDiff;
        double yPart = xRatio * yDiff;
        double newY = first.y + yPart;
        return new FloatPoint(between, newY);
    }

    public void plot(Map map) {
        long xFieldValue = ((Timestamp) map.get(xField)).getTime();
        int yFieldValue = (int) (double) map.get(yField);
        String area = (String) map.get(groupField);

        Collection<Point> points = areas.get(area);
        if (points == null) {
            points = new ArrayList<Point>();
            areas.put(area, points);
        }
        points.add(point(xFieldValue, yFieldValue));
        if (!allXes.contains(xFieldValue)) {
            allXes.add(xFieldValue);
        }
    }
}
