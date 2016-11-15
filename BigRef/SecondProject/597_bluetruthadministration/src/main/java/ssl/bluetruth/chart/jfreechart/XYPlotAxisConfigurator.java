/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.jfreechart;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.RelativeDateFormat;
import ssl.bluetruth.chart.common.Period;
import ssl.bluetruth.chart.style.axis.AxisStyler;

/**
 *
 * @author pwood
 */
public class XYPlotAxisConfigurator implements AxisStyler {
    private final XYPlot xyPlot;
    private final boolean isDomainAxis;

    public XYPlotAxisConfigurator(XYPlot xyPlot, boolean isDomainAxis) {
        this.xyPlot = xyPlot;
        this.isDomainAxis = isDomainAxis;
    }

    public void setRelativeDateAxis(String label, String format) {
        setAxis(makeRelativeDateAxis(format, label));
    }

    public void setRelativeDateAxis(String label, String format, long axisMaximum) {
        DateAxis axis = makeRelativeDateAxis(format, label);
        axis.setRange(0, axisMaximum);
        setAxis(axis);
    }

    public void setDateAxis(String label, String format, Period period) {
        DateAxis axis = new DateAxis();
        axis.setDateFormatOverride(new SimpleDateFormat(format));
        axis.setLabel(label);
        axis.setMinimumDate(period.fromDate());
        axis.setMaximumDate(period.toDate());
        setAxis(axis);
    }

    public void setNumberAxis(String label, long axisMaximum) {
        NumberAxis axis = new NumberAxis();
        axis.setLabel(label);
        axis.setRange(0, axisMaximum);
        setAxis(axis);
    }

    public void setNumberAxis(String label) {
        NumberAxis axis = new NumberAxis();
        axis.setLabel(label);
        setAxis(axis);
    }

    private DateAxis makeRelativeDateAxis(String format, String label) {
        DateAxis axis = new DateAxis();
        axis.setDateFormatOverride(new SimpleDateFormat(format));
        axis.setLabel(label);
        RelativeDateFormat rdf = new RelativeDateFormat();
        rdf.setSecondFormatter(new DecimalFormat("0"));
        axis.setDateFormatOverride(rdf);
//        axis.setMinimumDate(new Date(0));
        return axis;
    }

    private void setAxis(ValueAxis axis) {
        if(isDomainAxis) {
            xyPlot.setDomainAxis(axis);
        } else {
            xyPlot.setRangeAxis(axis);
        }
    }
}
