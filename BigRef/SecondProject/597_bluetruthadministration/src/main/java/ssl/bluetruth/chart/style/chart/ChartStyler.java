/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.style.chart;

import ssl.bluetruth.chart.style.series.SeriesStyle;

/**
 *
 * @author pwood
 */
public interface ChartStyler {

    public void createXYScatterChart();

    public void createXYStackedAreaChart();

    public void applySeriesStyle(int series, SeriesStyle seriesStyle);
}
