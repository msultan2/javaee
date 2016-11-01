/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.style.axis;

/**
 *
 * @author pwood
 */
public class RelativeDateAxisStyle implements AxisStyle {
    private final String label;
    private final String format;
    private final long maxValue;
    private final boolean hasMaxValue;

    public RelativeDateAxisStyle(String label, String format, long maxValue) {
        this.label = label;
        this.format = format;
        this.maxValue = maxValue;
        this.hasMaxValue = true;
    }

    public RelativeDateAxisStyle(String label, String format) {
        this.label = label;
        this.format = format;
        this.maxValue = 0;
        this.hasMaxValue = false;
    }

    public void apply(AxisStyler axisStyler) {
        if(hasMaxValue) {
            axisStyler.setRelativeDateAxis(label, format, maxValue);
        } else {
            axisStyler.setRelativeDateAxis(label, format);
        }
    }
}
