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
public class ColoredLinesStyle extends LinesStyle {
    private final Color color;

    public ColoredLinesStyle(Color color) {
        this.color = color;
    }

    @Override
    public void apply(SeriesStyler seriesStyler) {
        super.apply(seriesStyler);
        seriesStyler.setPaint(color);
    }

}
