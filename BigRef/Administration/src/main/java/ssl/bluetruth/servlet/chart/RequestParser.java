/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.chart;

import ssl.bluetruth.common.BlueTruthException;
import ssl.bluetruth.chart.common.Average;
import ssl.bluetruth.chart.common.Period;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import ssl.bluetruth.chart.common.Rectangle;

/**
 *
 * @author pwood
 */
public class RequestParser {
    private final HttpServletRequest request;

    public RequestParser(HttpServletRequest request) {
        this.request = request;
    }

    public String string(String parameter) {
        return request.getParameter(parameter);
    }

    public String string(String parameter, String defaultResult) {
        String result = request.getParameter(parameter);
        if(result == null) {
            result = defaultResult;
        }
        return result;
    }

    public int integer(String parameter, int defaultResult) {
        int result = defaultResult;
        try{
            String param = request.getParameter(parameter);
            if(param == null) {
                result = defaultResult;
            } else {
                result = Integer.parseInt(param);
            }
        } catch (NumberFormatException nfe){
            result = defaultResult;
        }
        return result;
    }

    public Date date(String parameter, String format, Date defaultResult) {
        Date result = defaultResult;
        try{
            String param = request.getParameter(parameter);
            if(param == null) {
                result = defaultResult;
            } else {
                result = new SimpleDateFormat(format).parse(param);
            }
        } catch (ParseException pe){
            result = defaultResult;
        }
        return result;
    }

    public Rectangle rectangle(String widthParam, String heightParam, int defaultWidth, double aspectRatio) {
        int width = integer(widthParam, defaultWidth);
        int height = integer(heightParam, (int)(width * aspectRatio));
        return new Rectangle(width, height);
    }

    public Period period(String fromParam, String toParam, String dateFormat, Period defaultPeriod) {
        Date from = date(fromParam, dateFormat, defaultPeriod.fromDate());
        Date to = date(toParam, dateFormat, defaultPeriod.toDate());
        return new Period(from, to);
    }

    public boolean bool(String parameter, boolean defaultResult) {
        boolean result = defaultResult;
        String param = request.getParameter(parameter);
        if(param == null) {
            result = defaultResult;
        } else {
            result = Boolean.parseBoolean(param);
        }
        return result;
    }

    public int time(String parameter, String format, String zeroParam, int defaultResult) {
        int result = defaultResult;
        try{
            String param = request.getParameter(parameter);
            if(param == null) {
                result = defaultResult;
            } else {
                Date from = new SimpleDateFormat(format).parse(zeroParam);
                Date to = new SimpleDateFormat(format).parse(param);
                result = (int) (to.getTime() - from.getTime());
            }
        } catch (ParseException pe){
            result = defaultResult;
        }
        return result;
    }

    public Average average(String parameter, String defaultAverage) throws ServletException {
        String param = request.getParameter(parameter);
        try {
            if(param == null) {
                return new Average(defaultAverage);
            } else {
                return new Average(param, defaultAverage);
            }
        } catch(BlueTruthException bte) {
            throw new ServletException(bte);
        }
    }
}
