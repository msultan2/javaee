/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.style.series;

import java.awt.BasicStroke;

/**
 *
 * @author pwood
 */
public class LinesStyle implements SeriesStyle {
    private final static BasicStroke oneWide = new BasicStroke(1);

    public void apply(SeriesStyler seriesStyler) {
        seriesStyler.setStroke(oneWide);
        seriesStyler.setLinesVisible(true);
        seriesStyler.setShapesVisible(false);
    }
}
