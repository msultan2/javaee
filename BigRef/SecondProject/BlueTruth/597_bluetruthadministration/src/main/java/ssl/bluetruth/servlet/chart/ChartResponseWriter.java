/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.chart;

import java.io.ByteArrayOutputStream;
import ssl.bluetruth.chart.Chart;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import ssl.bluetruth.common.BlueTruthException;
import static ssl.bluetruth.servlet.chart.ChartMaker.makeChart;
/**
 *
 * @author pwood
 */
public class ChartResponseWriter {

    public static void writeChart(
            Class chartClass,
            HttpServletResponse response,
            Object...params) throws IOException, ServletException {

        Chart chart = makeChart(chartClass, params);
        response.setContentType(chart.getMimeType());

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            chart.write(os);
        } catch (BlueTruthException ex) {
            throw new ServletException(ex);
        }

        response.setContentLength(os.size());
        os.writeTo(response.getOutputStream());

    }
}
