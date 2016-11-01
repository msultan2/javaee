/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.chart;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import ssl.bluetruth.chart.Chart;

/**
 *
 * @author pwood
 */
public class ChartMaker {
    public static Chart makeChart(Class chartClass, Object...params) throws ServletException {
        Constructor constructor =
                chartClass.getConstructors()[0];
        try {
            return (Chart) constructor.newInstance(params);
        } catch (InstantiationException ex) {
            throw new ServletException(ex);
        } catch (IllegalAccessException ex) {
            throw new ServletException(ex);
        } catch (IllegalArgumentException ex) {
            throw new ServletException(ex);
        } catch (InvocationTargetException ex) {
            throw new ServletException(ex);
        }
    }
}
