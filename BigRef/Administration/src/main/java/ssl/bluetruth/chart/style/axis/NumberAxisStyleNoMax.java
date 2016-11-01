/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.style.axis;

/**
 *
 * @author pwood
 */
public class NumberAxisStyleNoMax implements AxisStyle {
    private final String label;

    public NumberAxisStyleNoMax(String label) {
        this.label = label;
    }

    public void apply(AxisStyler axisStyler) {
        axisStyler.setNumberAxis(label);
    }

}
