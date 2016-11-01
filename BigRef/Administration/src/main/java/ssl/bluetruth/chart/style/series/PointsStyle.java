/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.style.series;

import java.awt.Color;

/**
 *
 * @author pwood
 */
public class PointsStyle implements SeriesStyle {
    private final Color color;

    public PointsStyle(Color color) {
        this.color = color;
    }

    public void apply(SeriesStyler seriesStyler) {
        seriesStyler.setLinesVisible(false);
        seriesStyler.setShapesVisible(true);
        seriesStyler.setPaint(color);
    }

}
