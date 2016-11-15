/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.writer;

import ssl.bluetruth.chart.common.Rectangle;
import ssl.bluetruth.chart.jfreechart.JFreeChartWriter;
import ssl.bluetruth.chart.json.JsonChartWriter;
import ssl.bluetruth.chart.style.axis.AxisStyle;
import ssl.bluetruth.chart.style.chart.ChartStyle;
import ssl.bluetruth.common.BlueTruthException;

/**
 *
 * @author pwood
 */
public class ChartWriterFactory {

    public ChartWriter create(String mimeType, String title, String subtitle,
            ChartStyle chartStyle, AxisStyle xAxis, AxisStyle yAxis, Rectangle chartSize) throws BlueTruthException {
        
        switch (mimeType) {
            case "image/png":
                return new JFreeChartWriter(title, subtitle, chartStyle, xAxis, yAxis, chartSize);
            case "application/json":
                return new JsonChartWriter(title, subtitle, chartStyle, xAxis, yAxis);
            case "application/csv":
                return new CsvChartWriter();
            default:
                throw new BlueTruthException("Unknown mimetype for chart");
        }
    }
}
