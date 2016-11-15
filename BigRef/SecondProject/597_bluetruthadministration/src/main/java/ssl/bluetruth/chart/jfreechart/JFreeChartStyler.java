/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.jfreechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import ssl.bluetruth.chart.style.axis.AxisStyle;
import ssl.bluetruth.chart.style.axis.AxisStyler;
import ssl.bluetruth.chart.style.chart.ChartStyler;
import ssl.bluetruth.chart.style.series.SeriesStyle;

/**
 *
 * @author pwood
 */
public class JFreeChartStyler implements ChartStyler {
    private final String title;
    private final String subtitle;
    private JFreeChart chart;
    private XYPlot plot;
    private final XYDataset dataset;

    public JFreeChartStyler(String title, String subtitle, XYDataset dataset) {
        this.title = title;
        this.subtitle = subtitle;
        this.dataset = dataset;
    }

    public void createXYScatterChart() {
        chart = ChartFactory.createScatterPlot(
                title, null, null, dataset, PlotOrientation.VERTICAL,
                true, true, false);

        chart.addSubtitle(new TextTitle(subtitle));
        chart.setBackgroundPaint(Color.white);
        plot = (XYPlot) chart.getPlot();
        applyBaseSettings();
        final CustomLineAndShapeRenderer renderer = new CustomLineAndShapeRenderer();
        Stroke stroke = new BasicStroke(2);
        renderer.setBaseStroke(stroke);
        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        plot.setRenderer(renderer);
    }

    public void createXYStackedAreaChart() {
        TableXYDataset table;
        if(dataset.getSeriesCount() == 0) {
            table = new DefaultTableXYDataset();
        } else {
            table = (TableXYDataset)dataset;
        }
        chart = ChartFactory.createStackedXYAreaChart(
                title, null, null, table, PlotOrientation.VERTICAL,
                true, true, false);
        chart.addSubtitle(new TextTitle(subtitle));
        plot = (XYPlot) chart.getPlot();
        applyBaseSettings();
    }

    private void applyBaseSettings() {
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
    }

    JFreeChart chart() {
        return chart;
    }

    void applyAxisStyles(AxisStyle xAxisStyle, AxisStyle yAxisStyle) {
        xAxisStyle.apply(toDomain(plot));
        yAxisStyle.apply(toRange(plot));
    }

    private AxisStyler toDomain(final XYPlot plot) {
        return new XYPlotAxisConfigurator(plot, true);
    }

    private AxisStyler toRange(final XYPlot plot) {
        return new XYPlotAxisConfigurator(plot, false);
    }

    public void applySeriesStyle(int series, SeriesStyle seriesStyle) {
        seriesStyle.apply(new XYLineAndShapeSeriesStyler(
                (XYLineAndShapeRenderer) plot.getRenderer(), series));
    }
}
