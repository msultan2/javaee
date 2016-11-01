/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.jfreechart;

import ssl.bluetruth.chart.style.series.SeriesStyler;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.util.ShapeUtilities;

/**
 *
 * @author pwood
 */
public class XYLineAndShapeSeriesStyler implements SeriesStyler {

    private final XYLineAndShapeRenderer renderer;
    private final int series;

    private static final Shape CROSS = ShapeUtilities.createDiagonalCross(1, 0.001f);

    public XYLineAndShapeSeriesStyler(XYLineAndShapeRenderer renderer, int series) {
        this.renderer = renderer;
        this.series = series;
    }

    public void setStroke(Stroke stroke) {
        renderer.setSeriesStroke(series, stroke);
    }

    public void setLinesVisible(boolean visible) {
        renderer.setSeriesLinesVisible(series, visible);
    }

    public void setShapesVisible(boolean visible) {
        renderer.setSeriesShapesVisible(series, visible);
    }

    public void setPaint(Color color) {
        renderer.setSeriesPaint(series, color);
    }

    public void setShape(Shape shape) {
        renderer.setSeriesShape(series, shape);
    }

    public Shape cross() {
        return CROSS;
    }
}
