/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.writer;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import ssl.bluetruth.chart.common.FloatPoint;
import ssl.bluetruth.chart.common.Point;
import ssl.bluetruth.chart.style.series.SeriesStyle;
import ssl.bluetruth.common.BlueTruthException;

/**
 *
 * @author pwood
 */
public interface ChartWriter {

    String getMimeType();

    void write(OutputStream outputStream) throws BlueTruthException;

    void addFloatSeries(String seriesKey, Collection<FloatPoint> points, SeriesStyle style);

    void addIntegerSeries(String seriesKey, Collection<Point> points, SeriesStyle style);

    void addStackedArea(Map<String, Collection<FloatPoint>> areaPoints);
}
