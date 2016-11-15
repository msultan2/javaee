/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.chart;

import ssl.bluetruth.chart.common.Period;
import java.util.Calendar;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ssl.bluetruth.beans.Result;
import ssl.bluetruth.chart.Chart;
import ssl.bluetruth.chart.Detector2GStatisticsChart;
import static ssl.bluetruth.servlet.chart.ChartMaker.makeChart;
import ssl.bluetruth.utils.JsonResponseProcessor;

/**
 *
 * @author svenkataramanappa
 */
public class Detector2GStatisticsChartServlet extends HttpServlet {

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

        Chart chart = 
                makeChart(Detector2GStatisticsChart.class,
                request.rectangle("width", "height", 800, 0.625),
                request.string("mimetype", "image/png"),
                request.string("detector_id"),
                request.period("from", "to", "yyyy-MM-dd HH:mm:ss", defaultPeriod()),
                timezone);

        result.setData(chart.getId());
        
        new JsonResponseProcessor().createResponse(result, response.getWriter());
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
