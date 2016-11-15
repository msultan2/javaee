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
import java.text.ParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import com.ssl.bluetruth.emitter2converter.exceptions.RequestParametersException;
import com.ssl.bluetruth.emitter2converter.exceptions.UnableToScheduleException;
import com.ssl.bluetruth.emitter2converter.utils.CongestionReport;
import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;
import com.ssl.bluetruth.emitter2converter.schedules.SchedulerUtils;
import com.ssl.bluetruth.emitter2converter.utils.HttpServletRequestUtils;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_0;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_1;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_2;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_3;

/**
 *
 * @author nthompson, jtrujillo-brenes
 */
public abstract class AbstractOccupancyReceiver extends HttpServlet {
    private final Logger LOGGER = LogManager.getLogger(getClass());
    private static final String PARAMETER_DETECTOR_ID = "id";
    private static final String PARAMETER_STATIONARY = "st";
    private static final String PARAMETER_VERY_SLOW = "vs";
    private static final String PARAMETER_SLOW = "s";
    private static final String PARAMETER_MODERATE = "m";
    private static final String PARAMETER_FREE = "f";
    private static final String PARAMETER_QUEUE_START = "qs";
    private static final String PARAMETER_QUEUE_END = "qe";
    private static final String TIMEZONE_UTC = "UTC";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final HttpServletRequestUtils requestUtils;
    
    public AbstractOccupancyReceiver() {    
        requestUtils = new HttpServletRequestUtils();
    } 
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("AbstractOccupancyReceiver: "+requestUtils.getInfo(request));
            }            
            CongestionReport congestionReport = new CongestionReport("UNKNOWN");
            try {
                ThreadLocalConfigurationManager.setFrom(request);
                congestionReport = setValuesFromHTTPRequest(congestionReport, request);
                int mode = ThreadLocalConfigurationManager.get().getInt(congestionReport.getDetectorId(), ConfigurationManager.MODE);                
                if (mode != MODE_0 && mode != MODE_1) { 
                    if (mode != MODE_2 && mode != MODE_3) {
                        LOGGER.warn("The mode of the Detector is "+mode+". This is not a valid value and it should be fixed. Meanwhile it will be considered as mode 3");
                    }
                    SchedulerUtils schedulerUtils = new SchedulerUtils();                    
                    schedulerUtils.rescheduleReports(ReportEnum.CONGESTION, congestionReport.getDetectorId());
                    schedulerUtils.sendReportImmediately(congestionReport);                   
                } else {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Received congestion report wouldn't be translated because "+congestionReport.getDetectorId()+" is in mode "+mode+", which means we shouldn't send congestion reports to the receiver2");
                    }
                }
            } catch(IllegalArgumentException | RequestParametersException | ParseException mex) {
                LOGGER.warn(mex.getLocalizedMessage(), mex);                 
            } catch (UnableToScheduleException utsex) {
                LOGGER.warn("Unable to schedule immediately the translated report or reschedule the periodically reports: "+utsex.getLocalizedMessage(), utsex);
            } catch (InvalidConfigurationException icex) {
                LOGGER.warn("Unable to treat the occupancy report because "+congestionReport.getDetectorId()+" doesn't have a valid configuration: "+icex.getLocalizedMessage(), icex);
            } 
        } catch (RuntimeException rex) {
            LOGGER.error("An exception occurred while processing the request from "+request.getRemoteAddr()+". Cause: "+rex.getLocalizedMessage()+" "+rex, rex);
        } 
        response.setStatus(HttpServletResponse.SC_OK); 
        ThreadLocalConfigurationManager.remove();        
    }
    
    private CongestionReport setValuesFromHTTPRequest(CongestionReport congestionReport, HttpServletRequest request) throws ParseException, RequestParametersException  {
        congestionReport.setFreeBin(getFreeFromHTTPRequest(request));
        congestionReport.setModerateBin(getModerateFromHTTPRequest(request));
        congestionReport.setSlowBin(getSlowFromHTTPRequest(request));
        congestionReport.setVerySlowBin(getVerySlowFromHTTPRequest(request));
        congestionReport.setStationaryBin(getStationaryFromHTTPRequest(request));
        congestionReport.setDetectorId(getDetectorIdFromHTTPRequest(request));
        congestionReport.setTimeReport(getTimestampFromHTTPRequest(request));
        congestionReport.setQueueStartAndQueuePresent(getQueueStartFromHTTPRequest(request));
        congestionReport.setQueueEndAndQueuePresent(getQueueEndFromHTTPRequest(request));
        return congestionReport;
    }
    
    private boolean hasDevices(CongestionReport congestionReport) {
        return (congestionReport.getFreeBin() != 0)
                && (congestionReport.getModerateBin() != 0)
                && (congestionReport.getSlowBin() != 0)
                && (congestionReport.getVerySlowBin() != 0)
                && (congestionReport.getStationaryBin() != 0);
    }

    public String getDetectorIdFromHTTPRequest(HttpServletRequest request) throws RequestParametersException {
        return requestUtils.getString(request, PARAMETER_DETECTOR_ID);
    }

    public Integer getStationaryFromHTTPRequest(HttpServletRequest request) throws RequestParametersException {
        return requestUtils.getInt(request, PARAMETER_STATIONARY);
    }
    public Integer getVerySlowFromHTTPRequest(HttpServletRequest request) throws RequestParametersException {
        return requestUtils.getInt(request, PARAMETER_VERY_SLOW);
    }
    public Integer getSlowFromHTTPRequest(HttpServletRequest request) throws RequestParametersException {
        return requestUtils.getInt(request, PARAMETER_SLOW); 
    }
    public Integer getModerateFromHTTPRequest(HttpServletRequest request) throws RequestParametersException {
        return requestUtils.getInt(request, PARAMETER_MODERATE);
    }
    public Integer getFreeFromHTTPRequest(HttpServletRequest request) throws RequestParametersException {
        return requestUtils.getInt(request, PARAMETER_FREE);
    }

    public Timestamp getQueueStartFromHTTPRequest(HttpServletRequest request) throws ParseException, RequestParametersException {
        String timestamp = requestUtils.getStringIfPossible(request, PARAMETER_QUEUE_START); 
        if(timestamp == null){
            return null;
        }
        long timeInMillis = AbstractDeviceDetectionReceiver.convertTimestampToMillis(timestamp, DATE_FORMAT, TIMEZONE_UTC);
        return new Timestamp(timeInMillis);
    }

    public Timestamp getQueueEndFromHTTPRequest(HttpServletRequest request) throws ParseException, RequestParametersException {
        String timestamp = requestUtils.getStringIfPossible(request, PARAMETER_QUEUE_END); 
        if(timestamp == null){
            return null;
        }
        long timeInMillis = AbstractDeviceDetectionReceiver.convertTimestampToMillis(timestamp, DATE_FORMAT, TIMEZONE_UTC);
        return new Timestamp(timeInMillis);
    }

    public abstract Timestamp getTimestampFromHTTPRequest(HttpServletRequest request) throws ParseException;
    
 

    
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
        return "Bluetruth OccupancyReceiver";
    }// </editor-fold>
}
