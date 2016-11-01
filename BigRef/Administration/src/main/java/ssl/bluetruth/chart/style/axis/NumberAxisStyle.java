/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.style.axis;

/**
 *
 * @author pwood
 */
public class NumberAxisStyle implements AxisStyle {
    private final String label;
    private final long axisMaximum;

    public NumberAxisStyle(String label, long axisMaximum) {
        this.label = label;
        this.axisMaximum = axisMaximum;
    }

    public void apply(AxisStyler axisStyler) {
        axisStyler.setNumberAxis(label, axisMaximum);
    }

}
