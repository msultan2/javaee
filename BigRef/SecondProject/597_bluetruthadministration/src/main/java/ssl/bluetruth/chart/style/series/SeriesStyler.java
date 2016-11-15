/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.style.series;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;

/**
 *
 * @author pwood
 */
public interface SeriesStyler {

    Shape cross();

    void setLinesVisible(boolean visible);

    void setPaint(Color color);

    void setShape(Shape shape);

    void setShapesVisible(boolean visible);

    void setStroke(Stroke stroke);

}
