/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import ssl.bluetruth.chart.common.FloatPoint;
import ssl.bluetruth.chart.common.Point;
import ssl.bluetruth.chart.style.series.SeriesStyle;
import ssl.bluetruth.common.BlueTruthException;

/**
 *
 * @author pwood
 */
class CsvChartWriter implements ChartWriter {
    private final Map<String, Collection<FloatPoint>> areaPoints =
            new HashMap<String, Collection<FloatPoint>>();

    @Override
    public String getMimeType() {
        return "application/csv";
    }

    @Override
    public void write(OutputStream outputStream) throws BlueTruthException {
        
        final SimpleDateFormat sdf = new SimpleDateFormat("\"dd/MM/yyyy HH:mm:ss\"");
        
        if(areaPoints.isEmpty()) {
            return;
        }
        try {
            outputStream.write("\"Detection Time".getBytes());
            for(String area: areaPoints.keySet()) {
                outputStream.write("\",\"".getBytes());
                outputStream.write(area.getBytes());
            }
            outputStream.write("\",\"total\"\r\n".getBytes());
            Collection<Iterator<FloatPoint>> iterators = new ArrayList<Iterator<FloatPoint>>();
            for(String area: areaPoints.keySet()) {
                 iterators.add(areaPoints.get(area).iterator());
            }
            boolean more = !iterators.isEmpty();
            while(more) {
                if(!allHaveNext(iterators)) {
                    break;
                }
                boolean firstTimeThrough = true;
                int total = 0;
                long timestamp = 0;
                for(Iterator<FloatPoint> it: iterators) {
                    final FloatPoint next = it.next();
                    if(firstTimeThrough) {
                        timestamp = (long)next.x;
                        Date d = new Date(timestamp);
                        outputStream.write(sdf.format(d).getBytes());
                        firstTimeThrough = false;
                    } else {
                        final long expected = (long)next.x;
                        if(timestamp != expected) {
                            throw new BlueTruthException(String.format(
                                    "Missing timestamp: expected %s, got %s", timestamp, expected));
                        }
                    }
                    outputStream.write(",".getBytes());
                    final long value = ((long)next.y / 1000);
                    total += value;
                    outputStream.write(Long.toString(value).getBytes());
                    more = it.hasNext();
                }
                outputStream.write(",".getBytes());
                outputStream.write(String.valueOf(total).getBytes());
                outputStream.write("\r\n".getBytes());
            }
        } catch (IOException ex) {
            throw new BlueTruthException(ex);
        }
    }

    @Override
    public void addFloatSeries(String seriesKey, Collection<FloatPoint> points, SeriesStyle style) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addIntegerSeries(String seriesKey, Collection<Point> points, SeriesStyle style) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addStackedArea(Map<String, Collection<FloatPoint>> areaPoints) {
        this.areaPoints.putAll(areaPoints);
    }

    private boolean allHaveNext(Collection<Iterator<FloatPoint>> iterators) {
        for(Iterator<FloatPoint> iterator: iterators) {
            if(!iterator.hasNext()) {
                return false;
            }
        }
        return true;
    }
}
