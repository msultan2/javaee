/**
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Created on ??
 */

package com.ssl.bluetruth.servlet.receiver;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.ssl.bluetruth.emitter2converter.exceptions.RequestParametersException;
import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.utils.HttpServletRequestUtils;
import ssl.bluetruth.servlet.receiver.AbstractUnconfiguredDetector.UnconfiguredType;

/** 
 *
 * @author nthompson, jtrujillo-brenes
 */
public abstract class AbstractDeviceDetectionReceiver extends HttpServlet {
    protected final Logger LOGGER = LogManager.getLogger(getClass());
    protected RawDeviceData rawDeviceData;
    protected HttpServletRequestUtils requestUtils;

    protected static final String PARAMETER_DETECTOR_ID = "outstationID";
    protected static final String PARAMETER_DEVICE_COUNT = "devCount";
    protected static final String PARAMETER_DEVICE_PREFIX = "d";
    protected static final String PARAMETER_DATE = "startTime";
    
    public AbstractDeviceDetectionReceiver() { 
        
    } 
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        try {               
            ThreadLocalConfigurationManager.setFrom(config);
            rawDeviceData = new RawDeviceData();  
            requestUtils = new HttpServletRequestUtils();
        } catch (RuntimeException rex) {
            String errorMessage = "There has been an exception constructing RawDeviceData. Cause: "+rex.getLocalizedMessage();
            LOGGER.error(errorMessage, rex);
            throw rex;
        }
    }

    public abstract Long parseDateString(String parameterDate) throws ParseException;

    public Timestamp getTimestampFromHTTPRequest(HttpServletRequest request) throws ParseException, RequestParametersException {

        String parameterDate = requestUtils.getString(request, PARAMETER_DATE);
        Long time = null;
  
        time = parseDateString(parameterDate);

        return new Timestamp(time);
    }

    public static long convertTimestampToMillis(String timestamp, String timestampFormat, String timeZone) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(timestampFormat);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        Date date = dateFormat.parse(timestamp);
        return date.getTime();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("AbstractDeviceDetectionReceiver: "+requestUtils.getInfo(request));
            } 
            String detectorId = "UNKNOWN";   
            try {             
                detectorId = getDetectorIdFromHTTPRequest(request);
                List<String> devicesIds = getDeviceIdsFromHTTPRequest(request);
                Timestamp timeDetection = getTimestampFromHTTPRequest(request);
                ThreadLocalConfigurationManager.setFrom(request);
                rawDeviceData.addDeviceDetections(detectorId, devicesIds, timeDetection);               
            } catch (InvalidConfigurationException icex) {                
                LOGGER.warn("Unable to store the detection because the OutStation '"+detectorId+"' has a invalid configuration: "+icex.getLocalizedMessage(), icex);
                icex.insertInDatabaseIfUnconfiguredDetector(detectorId, UnconfiguredType.LAST_DEVICE_DETECTION);                
            } catch(IllegalArgumentException | RequestParametersException | ParseException mex) {
                LOGGER.warn(mex.getLocalizedMessage(), mex);
            }         
        } catch (RuntimeException rex) {
            LOGGER.error("An exception occurred while processing the request from "+request.getRemoteAddr()+". Cause: "+rex.getLocalizedMessage()+" "+rex, rex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } 
        response.setStatus(HttpServletResponse.SC_OK);
        ThreadLocalConfigurationManager.remove();
    }

    public String getDetectorIdFromHTTPRequest(HttpServletRequest request) throws RequestParametersException {
        return requestUtils.getString(request, PARAMETER_DETECTOR_ID); 
    }

    public List<String> getDeviceIdsFromHTTPRequest(HttpServletRequest request) throws RequestParametersException {
        Integer deviceCount = requestUtils.getInt(request, PARAMETER_DEVICE_COUNT);
        List<String> deviceIds = new ArrayList<String>();
        for (int i = 1; i <= deviceCount; i++) {              
            deviceIds.add(requestUtils.getString(request, PARAMETER_DEVICE_PREFIX + i));  
        }        
        return deviceIds;
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
        return "Bluetruth DeviceDetectionReceiver";
    }// </editor-fold>
}