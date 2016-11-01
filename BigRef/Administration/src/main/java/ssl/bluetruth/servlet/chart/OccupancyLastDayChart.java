/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.servlet.chart;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.ui.RectangleInsets;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.servlet.update.DetectorManager;

/**
 *
 * @author nthompson
 */
public class OccupancyLastDayChart extends HttpServlet {

    private final static String OCCUPANCY_LAST_HOUR_SQL = "SELECT "
            + "reported_timestamp AT TIME ZONE '%s' AS reported_timestamp, "
            + "stationary, "
            + "very_slow, "
            + "slow, "
            + "moderate, "
            + "free "
            + "FROM occupancy "
            + "WHERE detector_id = ? "
            + "AND reported_timestamp > (now() - '24:00:00'::interval) "
            + "ORDER BY reported_timestamp;";
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String timezone = (String) request.getSession().getAttribute("user_timezone");
        if (timezone == null) {
            timezone = "UTC";
        }

        response.setContentType("image/png");
        ServletOutputStream outputStream = response.getOutputStream();

        String detectorName = request.getParameter("detectorName");
        String detectorId = request.getParameter("detectorId");
        int width = 800;
        int height = 500;
        try {
            width = Integer.parseInt(request.getParameter("w"));
        } catch (NumberFormatException nfe) {
            width = 800;
        }
        try {
            height = Integer.parseInt(request.getParameter("h"));
        } catch (NumberFormatException nfe) {
            height = (int) (width / 1.6);
        }

        JFreeChart chart = getOccupancyLastHourGraph(detectorName, detectorId, timezone);


        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            ChartUtilities.writeChartAsPNG(os, chart, width, height);
        } catch (Exception ex) {
        }

        response.setContentLength(os.size());
        os.writeTo(outputStream);

    }

    public JFreeChart getOccupancyLastHourGraph(String detectorName, String detectorId, String timezone) {

        JFreeChart chart = ChartFactory.createStackedXYAreaChart(
                "Traffic Flow recorded at detector '" + detectorName + "' in the last day.", // title
                "Reported Time", // x-axis label
                "Traffic Flow Count", // y-axis label
                createTableXYDataset(detectorId, timezone), // data
                PlotOrientation.VERTICAL,
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
                );

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        final XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(104, 255, 104));
        renderer.setSeriesPaint(1, new Color(104, 104, 255));
        renderer.setSeriesPaint(2, new Color(255, 255, 104));
        renderer.setSeriesPaint(3, new Color(255, 131, 104));
        renderer.setSeriesPaint(4, new Color(255, 104, 104));
        plot.setRenderer(renderer);

        DateAxis domainAxis = new DateAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MMM-dd HH:mm"));
        domainAxis.setLabel("Reported Time");
        plot.setDomainAxis(domainAxis);

        NumberAxis rangeAxis = new NumberAxis();
        rangeAxis.setLabel("Traffic Flow Count");
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.setRangeAxis(rangeAxis);

        return chart;
    }

    public static TableXYDataset createTableXYDataset(String detectorId, String timezone) {

        TimeTableXYDataset dataset = new TimeTableXYDataset();
        populateTimeTableXYDataset(detectorId, dataset, timezone);

        return dataset;

    }

    private static void populateTimeTableXYDataset(String detectorId, TimeTableXYDataset dataset, String timezone) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(String.format(OCCUPANCY_LAST_HOUR_SQL, timezone));
            stmt.setString(1, detectorId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Timestamp rt = rs.getTimestamp("reported_timestamp");
                Integer stationary = rs.getInt("stationary");
                Integer verySlow = rs.getInt("very_slow");
                Integer slow = rs.getInt("slow");
                Integer moderate = rs.getInt("moderate");
                Integer free = rs.getInt("free");
                Date date = new Date(rt.getTime());
                Second s = new Second(date);
                dataset.add(s, free, "free");
                dataset.add(s, moderate, "moderate");
                dataset.add(s, slow, "slow");
                dataset.add(s, verySlow, "very slow");
                dataset.add(s, stationary, "stationary");


            }
        } catch (SQLException ex) {
            Logger.getLogger(DetectorManager.class.getName()).log(Level.INFO, ex.getMessage());
        } catch (NamingException ex) {
            Logger.getLogger(DetectorManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatabaseManagerException ex) {
            Logger.getLogger(DetectorManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
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
