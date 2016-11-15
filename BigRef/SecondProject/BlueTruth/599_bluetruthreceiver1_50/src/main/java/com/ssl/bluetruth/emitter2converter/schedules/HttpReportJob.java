/**
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SIMULATION SYSTEMS LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF TH
 * IS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 * 
 * Created on 19-May-2015 04:37 PM
 */
package com.ssl.bluetruth.emitter2converter.schedules;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.exceptions.UnableToSendReportException;
import com.ssl.bluetruth.emitter2converter.utils.CongestionReport;
import com.ssl.bluetruth.emitter2converter.utils.HttpReport;
import ssl.bluetruth.servlet.receiver.AbstractUnconfiguredDetector;

/**
 * The only reason why this class exists is to send the report in a different 
 * thread from the AbstractOccupancyReceiver servlet, so the OutStation 
 * doesn't have to wait for the response for a new http request
 * @author josetrujillo-brenes
 */
public class HttpReportJob implements Job {

    public final static String PARAMETER_CONGESTION_REPORT = "congestionReport";
    public final static String PARAMETER_PROPERTIES_MANAGER = "propertiesManager";
    
    private final Logger logger = LogManager.getLogger(getClass());
    
    private String idOutStation;
    private ConfigurationManager configurationManager;
    private JobDataMap jobDataMap;
    private JobKey jobKey;
    private CongestionReport congestionReport;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try { 
            saveParameters(context);
            ThreadLocalConfigurationManager.set(configurationManager);
            try {            
                sendReportUsingPOST(congestionReport.getV4String());
            } catch (UnableToSendReportException utsrex) {
                String message = "Unable to send '"+ReportEnum.CONGESTION.getName()+"' for OutStation '"+idOutStation+"'. Cause: "+utsrex.getLocalizedMessage();
                logger.error(message, utsrex); 
            } catch (InvalidConfigurationException icex) {
                String message = "Unable to send '"+ReportEnum.CONGESTION.getName()+"' for OutStation '"+idOutStation+"'. Cause: "+icex.getLocalizedMessage();
                logger.error(message, icex); 
                icex.insertInDatabaseIfUnconfiguredDetector(congestionReport.getDetectorId(), AbstractUnconfiguredDetector.UnconfiguredType.LAST_TRAFFIC_FLOW_REPORT);
            }
        } catch (RuntimeException ex) {
            String message = jobKey+": There has been a Exception during de execution of the scheduler job. Exception: "+ex+", LocalizedMessage: "+ex.getLocalizedMessage();
            logger.error(message, ex); 
        }
        ThreadLocalConfigurationManager.remove();        
    }
    
    private void saveParameters(JobExecutionContext context) {
        jobKey = context.getJobDetail().getKey();
        jobDataMap = context.getJobDetail().getJobDataMap();
         
        configurationManager = (ConfigurationManager) jobDataMap.get(PARAMETER_PROPERTIES_MANAGER);
        congestionReport = (CongestionReport) jobDataMap.get(PARAMETER_CONGESTION_REPORT);
        idOutStation = congestionReport.getDetectorId();
    }
    
    private void sendReportUsingPOST(String body) throws InvalidConfigurationException, UnableToSendReportException {        
        new HttpReport(idOutStation, ReportEnum.CONGESTION).sendReportUsingPOST(body);
    }
    
}
