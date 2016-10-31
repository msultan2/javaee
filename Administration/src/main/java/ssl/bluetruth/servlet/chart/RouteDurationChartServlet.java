/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.chart;

import ssl.bluetruth.chart.common.Period;
import java.util.Calendar;
import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.chart.Chart;
import ssl.bluetruth.chart.RouteDurationChart;
import ssl.bluetruth.chart.RouteDurationTotalChart;
import ssl.bluetruth.chart.common.Average;
import static ssl.bluetruth.servlet.chart.ChartMaker.makeChart;
import static ssl.bluetruth.servlet.chart.ChartResponseWriter.writeChart;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author pwood
 */
public class RouteDurationChartServlet extends HttpServlet {

    public static Period defaultPeriod() {
        return new Period(Calendar.HOUR, 24); // previous 24 hours
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param httpServletRequestrequest servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest httpServletRequestrequest, HttpServletResponse response)
            throws ServletException, IOException {
        
        Result result = new Result();
        RequestParser request = new RequestParser(httpServletRequestrequest);

        String timezone = (String) httpServletRequestrequest.getSession().getAttribute("user_timezone");
        if (timezone == null) {
            timezone = "UTC";
        }
        
        boolean useCSV = request.bool("csv", false);
        boolean useTotal = request.bool("total", false);
        
        String mimetype;
        final String route = request.string("route");
        final Period period = request.period("from", "to", "yyyy-MM-dd HH:mm:ss", defaultPeriod());
        final Average average = request.average("average", "median");
        
        if (useCSV) {
            mimetype = "application/csv";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

            String chartFilename
                    = String.format("%s_%s_%s_to_%s.csv",
                            route.replaceAll("[^A-Za-z0-9]", "-"),
                            average.toString(),
                            sdf.format(period.fromDate()),
                            sdf.format(period.toDate()));

            response.setHeader("Content-Disposition",
                    "attachment; filename=" + chartFilename);

            writeChart(RouteDurationChart.class,
                    response,
                    request.rectangle("width", "height", 800, 0.625),
                    mimetype, route, period, average, timezone);
        } else {
            mimetype = "image/png";

            Chart chart
                    = makeChart(useTotal ? RouteDurationTotalChart.class : RouteDurationChart.class,
                            request.rectangle("width", "height", 800, 0.625),
                            mimetype, route, period, average, timezone);

            result.setData(chart.getId());

            new JsonResponseProcessor().createResponse(result, response.getWriter());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
