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

import java.util.Date;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.exceptions.UnableToScheduleException;
import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import com.ssl.bluetruth.emitter2converter.utils.CongestionReport;
import ssl.bluetruth.servlet.receiver.AbstractUnconfiguredDetector;

/**
 * Utility class that encapsulates Quartz Scheduler third party library
 * This class allows to schedule, reschedule, delete, or ask if there is already schedules for the reports of a outstation.
 * It also allows to delete or log all schedules.
 * Despite its a utility class, it doesn't have static methods. 
 * @author jtrujillo-brenes
 */
public class SchedulerUtils {      
      
    private final Logger logger = LogManager.getLogger(getClass());    
    private ConfigurationManager configurationManager;
    
    public SchedulerUtils() {
    }
    
    private ConfigurationManager getConfigurationManager() {
        if(configurationManager != null) {
            return configurationManager;
        } else {
            configurationManager = ThreadLocalConfigurationManager.get();
            return configurationManager;
        }
    }    
    
    public void scheduleReports(ReportEnum report, String idOutStation, RawDeviceData rawDeviceData) throws UnableToScheduleException {
        String groupId = "OutStation"+idOutStation;
        String jobId = report.getName();
        int periodInSeconds = getPeriodInSeconds(report, idOutStation);
        String triggerId = jobId+groupId+"StarsIn"+periodInSeconds;
        Date startInDate = DateBuilder.futureDate(periodInSeconds, DateBuilder.IntervalUnit.SECOND);
        String scheduleLogInfo = "OutStation "+idOutStation+" to start at "+startInDate+" and repeating each "+periodInSeconds+" seconds. Scheduler info: Identity: "+groupId+"."+jobId+". Trigger: "+triggerId;
        try { 
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            JobDetail job = newJob(report, idOutStation, rawDeviceData, groupId, jobId); 
            Trigger trigger = newTrigger(groupId, jobId, periodInSeconds, startInDate);
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
            if(logger.isInfoEnabled()) { 
                logger.info("Scheduler for the "+report.getName()+" created succesfully for "+scheduleLogInfo);
            }
        } catch(SchedulerException sex) {
            String errorMessage = "Couldn't create scheduler for the "+report.getName()+" for "+scheduleLogInfo+". cause: "+sex.getLocalizedMessage();
            throw new UnableToScheduleException(errorMessage,sex);
        }
    }
    
    public void sendReportImmediately(CongestionReport congestionReport) throws UnableToScheduleException {
        String groupId = "OutStation"+congestionReport.getDetectorId();
        String jobId = "Translated"+ReportEnum.CONGESTION.getName();   
        try {                     
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            jobId = jobId+JobKey.createUniqueName(groupId);
            JobKey jobKey = JobKey.jobKey(jobId, groupId);            
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if(jobDetail == null) {
                jobDetail = JobBuilder.newJob(HttpReportJob.class).withIdentity(jobKey).build();
                jobDetail.getJobDataMap().put(HttpReportJob.PARAMETER_PROPERTIES_MANAGER, getConfigurationManager());
                jobDetail.getJobDataMap().put(HttpReportJob.PARAMETER_CONGESTION_REPORT, congestionReport);
                Trigger trigger = newTrigger(groupId, jobId);
                scheduler.scheduleJob(jobDetail, trigger);
                scheduler.start();
                if(logger.isInfoEnabled()) { 
                    logger.info("Scheduler for the immediatly "+jobId+" for the OutStation "+congestionReport.getDetectorId()+" created succesfully");
                }
            } else {
                logger.warn("Unable to schedule the immediatly "+jobId+" for the OutStation "+congestionReport.getDetectorId()+" because there is already a translation in progress for this detector in this id.");
            }            
        } catch (SchedulerException sex) {
            String message = "Unable to schedule the immediatly "+jobId+" for the OutStation "+congestionReport.getDetectorId()+" because there has been an exception: "+sex.getLocalizedMessage();
            logger.error(message,sex); 
            throw new UnableToScheduleException(message,sex);
        }
    }
    
    public void rescheduleReports(ReportEnum report, String idOutStation) throws UnableToScheduleException {
        String groupId = "OutStation"+idOutStation;
        int periodInSeconds = getPeriodInSeconds(report, idOutStation);
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            JobKey jobKey = getJobKey(report, idOutStation);
            if(jobKey != null) {
                Trigger trigger = getTrigger(jobKey);
                if(trigger != null) {
                    int delay = 0;
                    if(report == ReportEnum.CONGESTION) {
                        delay = getConfigurationManager().getInt(idOutStation, ConfigurationManager.CONGESTION_REPORT_DELAY_WHEN_INSTATION_RECEIVES_ONE_IN_SECONDS);
                        if(logger.isInfoEnabled()) { 
                            logger.info("A delay of '"+delay+"' seconds is going to be added to the period just in case we receive another one");
                        }
                    }                
                    int startsInSeconds = delay + getConfigurationManager().getInt(idOutStation, report.getPropertyNameForPeriod());                
                    Date startInDate = DateBuilder.futureDate(startsInSeconds, DateBuilder.IntervalUnit.SECOND);
                    Trigger newTrigger = newTrigger(groupId, jobKey.getName(), periodInSeconds, startInDate);
                    scheduler.rescheduleJob(trigger.getKey(), newTrigger);
                    if(logger.isInfoEnabled()) { 
                        logger.info("Scheduler for the "+report.getName()+" for OutStation "+idOutStation+" has been reschedule to start at "+startInDate+". Scheduler info: Identity: "+groupId+"."+jobKey.getName());
                    }
                } else {
                    logger.warn("No trigger found to reschedule the "+report.getName()+" with jobKey "+jobKey.getName()+" for the OutStation "+idOutStation);
                }
            } else {
                logger.warn("No Job found to reschedule the "+report.getName()+" for the OutStation "+idOutStation);
            }            
        } catch (SchedulerException sex) {
            String message = "Unable to reschedule the "+report.getName()+" for the OutStation "+idOutStation+" because there has been an exception: "+sex.getLocalizedMessage();
            logger.error(message,sex); 
            throw new UnableToScheduleException(message,sex);
        } catch (InvalidConfigurationException icex) {
            String message = "Couldn't rescheduler for the '"+report.getName()+"' for OutStation '"+idOutStation+"' because it doesn't have a valid configuration. cause: "+icex.getLocalizedMessage();
            throw new UnableToScheduleException(message,icex);
        }
    }
    
    public boolean isAReportAlreadySchedule(ReportEnum report, String idOutStation) {
        JobKey jobKey = getJobKey(report, idOutStation);
        return (jobKey != null);         
    }
    
    public void delete(ReportEnum report, String idOutStation) {
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            JobKey jobKey = getJobKey(report, idOutStation);
            if(jobKey != null) {
                Trigger trigger = getTrigger(jobKey);
                if(trigger != null) {
                    scheduler.unscheduleJob(trigger.getKey());
                    if(logger.isInfoEnabled()) { 
                        logger.info(report.getName()+" schedule for the OutStation "+idOutStation+" deleted successfully");
                    }
                } else {
                    if(logger.isDebugEnabled()) { 
                        logger.debug("Unable to delete the "+report.getName()+" schedule for the OutStation "+idOutStation+" because no trigger was found");
                    }
                }
            } else {
                if(logger.isDebugEnabled()) { 
                    logger.debug("Unable to delete the "+report.getName()+" schedule for the OutStation "+idOutStation+" because no jobkey was found");
                }
            }            
        } catch (SchedulerException sex) {
            String message = "Unable to delete the "+report.getName()+" schedule for the OutStation "+idOutStation+" because there has been an exception: "+sex.getLocalizedMessage();
            logger.error(message, sex);            
        }
    }  
    
    public void deleteAllSchedules() {
       try {
            new StdSchedulerFactory().getScheduler().clear();
            if(logger.isInfoEnabled()) { 
                logger.info("All schedules deteled");
            }
        } catch (SchedulerException sex) {
            String message = "Unable to delete the schedules because there has been an exception: "+sex.getLocalizedMessage();
            logger.error(message, sex);
        } 
    }
    
    public void logAllSchedules() {
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            for (String group : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys((GroupMatcher<JobKey>) GroupMatcher.groupEquals(group))) {
                    if(logger.isInfoEnabled()) { 
                        logger.info("Found job identified by "+jobKey);
                    }                     
                }
            }
        } catch (SchedulerException sex) {
            String message = "Unable to log the jobs because there has been an exception: "+sex.getLocalizedMessage();
            logger.error(message, sex);
        }
    } 
    
    private JobKey getJobKey(ReportEnum report, String idOutStation) {
        String groupId = "OutStation"+idOutStation;
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            for(JobKey jobKey : scheduler.getJobKeys((GroupMatcher<JobKey>) GroupMatcher.groupEquals(groupId))) {
                String jobId = jobKey.getName();
                if(jobId.startsWith(report.getName())) {
                    return jobKey;
                }
            }            
        } catch (SchedulerException sex) {
            String message = "Unable to get the jobKey for the "+report.getName()+" for the OutStation "+idOutStation+" because there has been an exception: "+sex.getLocalizedMessage();
            logger.error(message, sex);            
        }
        if(logger.isDebugEnabled()) {
            logger.debug("Unable to find the jobKey for the "+report.getName()+" for the OutStation "+idOutStation);
        }
        return null;
    } 
    
    private Trigger newTrigger(String groupId, String jobId, int periodInSeconds, Date startInDate) {
        String triggerId = jobId+groupId+"StartEach"+periodInSeconds;
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerId, groupId)
            .startAt(startInDate)
            .withSchedule(simpleSchedule().withIntervalInSeconds(periodInSeconds).repeatForever())
            .build();
        return trigger;
    }
    
    private Trigger newTrigger(String groupId, String jobId) {
        String triggerId = jobId+groupId+"Immediately";
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerId, groupId)
            .startAt(new Date())
            .build();
        return trigger;
    }
    
    private JobDetail newJob(ReportEnum report, String idOutStation, RawDeviceData rawDeviceData, String groupId, String jobId) {
        JobDetail job = JobBuilder.newJob(SchedulerJob.class).withIdentity(jobId, groupId).build();
        job.getJobDataMap().put(SchedulerJob.PARAMETER_ID_OUTSTATION, idOutStation);
        job.getJobDataMap().put(SchedulerJob.PARAMETER_RAW_DEVICE_DATA, rawDeviceData); 
        job.getJobDataMap().put(SchedulerJob.PARAMETER_PROPERTIES_MANAGER, getConfigurationManager());
        job.getJobDataMap().put(SchedulerJob.PARAMETER_REPORT, report); 
        return job;
    }
    
    private int getPeriodInSeconds(ReportEnum report, String idOutStation) throws UnableToScheduleException {
        try {
            return getConfigurationManager().getInt(idOutStation, report.getPropertyNameForPeriod());
        } catch (InvalidConfigurationException icex) {
            String message = "Couldn't create scheduler for the '"+report.getName()+"' for OutStation '"+idOutStation+"' because it doesn't have a valid configuration. cause: "+icex.getLocalizedMessage();
            icex.insertInDatabaseIfUnconfiguredDetector(idOutStation, AbstractUnconfiguredDetector.UnconfiguredType.LAST_DEVICE_DETECTION);
            throw new UnableToScheduleException(message,icex);
        }
    }
    
    private Trigger getTrigger(JobKey jobKey) {
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);            
            if (!triggers.isEmpty()) {
                Trigger oldTrigger = triggers.get(0);
                if (oldTrigger != null) {
                    return oldTrigger;
                } 
            }                                       
        } catch (SchedulerException sex) {
            String message = "Unable to get the trigger for the jobKey "+jobKey.getName()+" because there has been an exception: "+sex.getLocalizedMessage();
            logger.error(message, sex);
        }
        if(logger.isDebugEnabled()) {
            logger.debug("Unable to get the trigger for the jobKey "+jobKey);
        }
        return null;
    }
}
