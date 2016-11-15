package ssl.bluetruth.detector.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.servlet.datarequest.DataRequestException;
import ssl.bluetruth.servlet.datarequest.file.DetectorConfigIniFileDownloadProcessor;
import ssl.bluetruth.servlet.response.DataResponse;

public class DetectorConfigurationDownload extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(DetectorConfigurationDownload.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Pattern p = Pattern.compile("^.*/([^/]*)/(([^/_]*)_.*)$");
        Matcher m = p.matcher(request.getRequestURI());
        if (m.matches()) {
            String view = m.group(1);
            String filename = m.group(2);
            String detectorId = m.group(3);
            
            DetectorConfigIniFileDownloadProcessor dcIniDownloadProcessor = new DetectorConfigIniFileDownloadProcessor(view, detectorId);
            try {
                DataResponse dataResponse = dcIniDownloadProcessor.processRequest();

                response.setHeader("Content-Disposition", "attachment; filename=" + filename);
                response.setHeader("Content-Type", "text/plain; charset=utf-8");
                
                InputStream responseData = dataResponse.getResponseData();

                byte[] buf = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = responseData.read(buf)) > 0) {
                    response.getOutputStream().write(buf, 0, bytesRead);
                }
            } catch (DataRequestException ex) {
                LOGGER.warn("Failed to serve configuration file for detector ID : "+detectorId, ex);
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
