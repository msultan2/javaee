/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.common;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author pwood
 */
public class Period {
    private final Date from;
    private final Date to;

    public Period(Date from, Date to) {
        this.from = from;
        this.to = to;
    }

    public Period(int calendarField, int period) {
        Calendar calendar = Calendar.getInstance();
        this.to = calendar.getTime(); // now
        calendar.add(calendarField, -period);
        this.from = calendar.getTime();
    }

    public Date fromDate()  {
        return this.from;
    }

    public Date toDate()  {
        return this.to;
    }
}
