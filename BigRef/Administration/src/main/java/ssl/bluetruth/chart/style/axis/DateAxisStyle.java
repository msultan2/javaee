/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.style.axis;

import ssl.bluetruth.chart.common.Period;

/**
 *
 * @author pwood
 */
public class DateAxisStyle implements AxisStyle {
    private final String label;
    private final String format;
    private final Period period;

    public DateAxisStyle(String label, String format, Period period) {
        this.label = label;
        this.format = format;
        this.period = period;
    }

    public void apply(AxisStyler axisStyler) {
        axisStyler.setDateAxis(label, format, period);
    }
}
