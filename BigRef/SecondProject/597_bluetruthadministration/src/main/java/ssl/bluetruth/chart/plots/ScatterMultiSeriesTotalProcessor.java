/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.plots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import ssl.bluetruth.chart.common.FloatPoint;
import ssl.bluetruth.chart.common.Point;
import ssl.bluetruth.chart.query.RowProcessor;
import ssl.bluetruth.chart.style.series.SeriesStyle;
import ssl.bluetruth.chart.style.series.SeriesStyler;
import ssl.bluetruth.chart.writer.ChartWriter;

/**
 *
 * @author pwood
 */
public class ScatterMultiSeriesTotalProcessor implements RowProcessor {
    private final String xField;
    private final String yField;
    private final String groupField;
    private final Map<String, Collection<Point>> areas =
            new HashMap<String, Collection<Point>>();
    private SortedSet<Long> allXes = new TreeSet<Long>();

    public ScatterMultiSeriesTotalProcessor(String xField, String yField,
            String groupField) {
        this.xField = xField;
        this.yField = yField;
        this.groupField = groupField;
    }

    public void modify(ChartWriter chart) {
        chart.addFloatSeries("Total", makeTable(), new SeriesStyle() {
            public void apply(SeriesStyler seriesStyler) {
                seriesStyler.setStroke(new BasicStroke(2));
                seriesStyler.setLinesVisible(true);
                seriesStyler.setShapesVisible(false);        
                seriesStyler.setPaint(Color.BLACK);
            }
        });
    }

    private Collection<FloatPoint> makeTable() {
        SortedMap<Long, Double> totals = new TreeMap<Long, Double>();
        
        for(Entry<String, Collection<Point>> area: areas.entrySet()) {
            String areaName = area.getKey();
            Collection<Point> areaPoints = area.getValue();
            for(Long x: allXes) {
                Point lastPoint = null;
                for(Point point: areaPoints) {
                    if(point.x == x) {
                        addToTotal(totals, point.x, point.y);
                        break;
                    } else if(lastPoint == null) {
                        if((point.x > x)) {
                            addToTotal(totals, x, point.y);
                            break;
                        } else {
                            lastPoint = point;
                        }
                    } else {
                        if((point.x > x) && (lastPoint.x < x)) {
                            addToTotal(totals, x, lerp(lastPoint, point, x));
                            break;
                        } else {
                            // do nothing
                        }
                        lastPoint = point;
                    }
                }
            }
        }
        
        Collection<FloatPoint> retVal = new ArrayList<FloatPoint>(totals.size());
        
        for (Entry<Long, Double> entry : totals.entrySet()) {
            retVal.add(new FloatPoint(entry.getKey(), entry.getValue()));
        }
        
        return retVal;
    }

    private Point point(long x, long y) {
        allXes.add(x);
        return new Point(x, y);
    }

    private double lerp(Point first, Point second, long between) {
        double xDiff = second.x - first.x;
        double yDiff = second.y - first.y;
        double xPart = between - first.x;
        double xRatio = xPart / xDiff;
        double yPart = xRatio * yDiff;
        double newY = first.y + yPart;
        return newY;
    }

    private void addToTotal(Map<Long, Double> totals, long x, double y) {
        Double d = totals.get(x);
        if (d == null) {
            totals.put(x, y);
        } else {
            totals.put(x, y + d);
        }
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
