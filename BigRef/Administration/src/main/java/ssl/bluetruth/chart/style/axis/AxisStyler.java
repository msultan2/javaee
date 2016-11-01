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
public interface AxisStyler {

    public void setRelativeDateAxis(String label, String format, long axisMaximum);

    public void setRelativeDateAxis(String label, String format);

    public void setNumberAxis(String label, long axisMaximum);

    public void setNumberAxis(String label);

    public void setDateAxis(String label, String format, Period period);

}
