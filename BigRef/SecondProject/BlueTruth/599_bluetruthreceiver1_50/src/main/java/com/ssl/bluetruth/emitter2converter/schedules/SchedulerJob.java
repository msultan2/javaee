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
 * Created on 27th April 2015
 */

package com.ssl.bluetruth.emitter2converter.schedules;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import com.ssl.bluetruth.emitter2converter.exceptions.DeadOutStationWithNothingElseToReportException;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.exceptions.UnableToSendReportException;
import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import com.ssl.bluetruth.emitter2converter.utils.HttpReport;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;
import ssl.bluetruth.servlet.receiver.AbstractUnconfiguredDetector;

/**
 * Job to execute the schedule. 
 * Attempts to create the report and send it.
 * If it fails because the OutStation is dead or the configuration is invalid: It will delete the next schedules.
 * If it fails for another reason: It will just write in the log error hopping it work next time.
 * @author jtrujillo-brenes
 */
public class SchedulerJob implements Job {
    
    
    public final static String PARAMETER_ID_OUTSTATION = "idOutStation";
    public final static String PARAMETER_RAW_DEVICE_DATA = "rawDeviceData";
    public final static String PARAMETER_PROPERTIES_MANAGER = "propertiesManager";    
    public final static String PARAMETER_REPORT = "report";
    public final static String PARAMETER_COUNT = "count";
    public final static String PARAMETER_AUTOSCHEDULABLE = "autoSchedulable";
    
    private final Logger logger = LogManager.getLogger(getClass());
    
    private String idOutStation;
    private RawDeviceData rawDeviceData;
    private ConfigurationManager configurationManager;
    private JobDataMap jobDataMap;
    private JobKey jobKey;
    private ReportEnum report;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            saveParameters(context);
            ThreadLocalConfigurationManager.set(configurationManager);
            SchedulerUtils reportsScheduler = new SchedulerUtils();            
            try {
                String body = rawDeviceData.getReportOfOutStation(report, idOutStation, TimeUtils.currentTimestamp());
                sendReportUsingPOST(body);
            } catch (DeadOutStationWithNothingElseToReportException sslex) {
                reportsScheduler.delete(report, idOutStation);
            } catch (InvalidConfigurationException icex) {
                reportsScheduler.delete(report, idOutStation);
                String message = "Unable to send '"+report.getName()+"' for OutStation '"+idOutStation+"' becuase it's has a invalid configuration. All schedules will be deleted. Cause: "+icex.getLocalizedMessage();
                logger.error(message, icex);
            } catch (UnableToSendReportException utsrex) {
                String message = "Unable to send '"+report.getName()+"' for OutStation '"+idOutStation+"'. Cause: "+utsrex.getLocalizedMessage();
                logger.error(message, utsrex); 
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
        report = (ReportEnum) jobDataMap.get(PARAMETER_REPORT);
        idOutStation = jobDataMap.getString(PARAMETER_ID_OUTSTATION);
        rawDeviceData = (RawDeviceData) jobDataMap.get(PARAMETER_RAW_DEVICE_DATA);  
        configurationManager = (ConfigurationManager) jobDataMap.get(PARAMETER_PROPERTIES_MANAGER);
    }
    
    private void sendReportUsingPOST(String body) throws InvalidConfigurationException, UnableToSendReportException {        
        new HttpReport(idOutStation, report).sendReportUsingPOST(body);
    }
}
