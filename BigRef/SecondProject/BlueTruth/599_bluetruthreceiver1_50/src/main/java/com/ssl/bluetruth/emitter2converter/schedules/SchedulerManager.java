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
 * Created on 30-Jul-2015 12:31 PM
 */
package com.ssl.bluetruth.emitter2converter.schedules;

import java.sql.Timestamp;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.configuration.AbstractConfigurationManagerListener;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.exceptions.UnableToScheduleException;
import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import com.ssl.bluetruth.emitter2converter.raw.RawDeviceDataOfOutStation;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_0;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_1;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_2;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_3;

/**
 * This class decides which reports should be schedule and which ones should be unschedule when configuration changes must apply 
 * @author josetrujillo-brenes
 */
public class SchedulerManager extends AbstractConfigurationManagerListener {
    
    private final Logger logger = LogManager.getLogger(getClass());    
    private final ConfigurationManager configurationManager;
    private final SchedulerUtils schedulerUtils;
    private final RawDeviceData rawDeviceData;
    
    public SchedulerManager(RawDeviceData rawDeviceData){        
        schedulerUtils = new SchedulerUtils();
        this.rawDeviceData = rawDeviceData;
        configurationManager = ThreadLocalConfigurationManager.get();
        configurationManager.addConfigurationManagerListener(this);
    }
    
    public void ScheduleReports(String idOutStation, Timestamp timeSeen) throws InvalidConfigurationException {
        int mode;
        try {
            mode = configurationManager.getInt(idOutStation, ConfigurationManager.MODE);
        } catch (InvalidConfigurationException icex) {
            mode = MODE_3;
            logger.warn("There has been an exception trying to obtain the mode of the Detector "+idOutStation+". This is not valid and it should be fixed. Meanwhile it will be considered as mode "+mode+". Exception: "+icex.getLocalizedMessage());            
        }
        switch (mode) {
            case MODE_0: 
                if(logger.isInfoEnabled()) { 
                    logger.info("No schedules will be created for Outstation "+idOutStation+" because it's in mode 0.");
                }
                break;
            case MODE_1:
                scheduleStatisticsReportIfThereArentAny(idOutStation, timeSeen);
                break;
            case MODE_2:                
                scheduleCongestionReportIfThereArentAny(idOutStation, timeSeen);
                break;            
            default:
                if(mode != MODE_3) {
                    logger.warn("The mode of the Detector "+idOutStation+" is "+mode+". This is not a valid value and it should be fixed. Meanwhile it will be considered as mode "+MODE_3);
                }     
                scheduleStatisticsReportIfThereArentAny(idOutStation, timeSeen);
                scheduleCongestionReportIfThereArentAny(idOutStation, timeSeen);
                break;
        }        
    }
    
    @Override
    public void ApplyConfigurationChanges(String idOutStation, Map<String, String> oldMap, Map<String, String> newMap) {
        if(logger.isDebugEnabled()) { 
            logger.debug("SchedulerManager has been notify of configuration changes for Detector '"+idOutStation+"'");
        }            
        changeSchedulesAccordingToOldAndNewMode(idOutStation, oldMap, newMap);
        changeSchedulesAccordingToOldAndNewStatisticsReportContents(idOutStation, oldMap, newMap);
    }
    
    private void UnscheduleReport(String idOutStation, ReportEnum reportEnum) {
        schedulerUtils.delete(reportEnum, idOutStation);
    }
    
    private void changeSchedulesAccordingToOldAndNewMode(String idOutStation, Map<String, String> oldMap, Map<String, String> newMap) {
        int oldMode = getMode(idOutStation, oldMap, newMap, OldOrNew.OLD);
        int newMode = getMode(idOutStation, oldMap, newMap, OldOrNew.NEW);    
        if(oldMode!=newMode) {
            if(logger.isInfoEnabled()) { 
                logger.info("Detector '"+idOutStation+"' has change mode from '"+oldMode+"' to mode '"+newMode+"'");
            }
            if(modeChangesFromNotHavingToHavingCongestionReports(oldMode, newMode)) {
                scheduleCongestionReportIfThereArentAny(idOutStation, TimeUtils.currentTimestamp());
            }
            if(modeChangesFromNotHavingToHavingStatisticsReports(oldMode, newMode)) {
                scheduleStatisticsReportIfThereArentAny(idOutStation, TimeUtils.currentTimestamp());
            }
            if(modeChangesFromHavingToNotHavingCongestionReports(oldMode, newMode)) {
                UnscheduleReport(idOutStation, ReportEnum.CONGESTION);
            }
            if(modeChangesFromHavingToNotHavingStatisticsReports(oldMode, newMode)) {
                UnscheduleReport(idOutStation, ReportEnum.STATISTICS_BRIEF);
                UnscheduleReport(idOutStation, ReportEnum.STATISTICS_FULL);
            } 
        }
    }
    
    private void changeSchedulesAccordingToOldAndNewStatisticsReportContents(String idOutStation, Map<String, String> oldMap, Map<String, String> newMap) {
        if(modeHasStatisticsReports(getMode(idOutStation, oldMap, newMap, OldOrNew.NEW))) {        
            String oldStatisticsReportContents = oldMap.get(ConfigurationManager.STATISTICS_REPORTS_CONTENTS);
            String newStatisticsReportContents = newMap.get(ConfigurationManager.STATISTICS_REPORTS_CONTENTS);
            if(!oldStatisticsReportContents.equalsIgnoreCase(newStatisticsReportContents)) {
                if(logger.isInfoEnabled()) { 
                    logger.info("Detector '"+idOutStation+"' has change StatisticsReportContents from '"+oldStatisticsReportContents+"' to mode '"+newStatisticsReportContents+"'");
                }
                if(ConfigurationManager.STATISTICS_REPORTS_CONTENTS_VALUE_BRIEF.equalsIgnoreCase(newStatisticsReportContents)) {
                    UnscheduleReport(idOutStation, ReportEnum.STATISTICS_FULL);
                    scheduleReportIfThereArentAny(idOutStation, ReportEnum.STATISTICS_BRIEF, TimeUtils.currentTimestamp());                     
                } else {
                    UnscheduleReport(idOutStation, ReportEnum.STATISTICS_BRIEF);
                    scheduleReportIfThereArentAny(idOutStation, ReportEnum.STATISTICS_FULL, TimeUtils.currentTimestamp()); 
                }
            }
        }        
    }
    
    private boolean modeChangesFromHavingToNotHavingCongestionReports(int oldMode, int newMode) {
        return (oldMode==MODE_3 && newMode==MODE_0) || (oldMode==MODE_3 && newMode==MODE_1) || (oldMode==MODE_2 && newMode==MODE_1) || (oldMode==MODE_2 && newMode==MODE_0);
    }
    
    private boolean modeChangesFromNotHavingToHavingCongestionReports(int oldMode, int newMode) {
        return (oldMode==MODE_0 && newMode==MODE_3) || (oldMode==MODE_1 && newMode==MODE_3) || (oldMode==MODE_1 && newMode==MODE_2) || (oldMode==MODE_0 && newMode==MODE_2);
    }
    
    private boolean modeChangesFromHavingToNotHavingStatisticsReports(int oldMode, int newMode) {
        return (oldMode==MODE_3 && newMode==MODE_0) || (oldMode==MODE_3 && newMode==MODE_2) || (oldMode==MODE_1 && newMode==MODE_0) || (oldMode==MODE_1 && newMode==MODE_2);
    }
    
    private boolean modeChangesFromNotHavingToHavingStatisticsReports(int oldMode, int newMode) {
        return (oldMode==MODE_0 && newMode==MODE_3) || (oldMode==MODE_2 && newMode==MODE_3) || (oldMode==MODE_0 && newMode==MODE_1) || (oldMode==MODE_2 && newMode==MODE_1);
    }
    
    private boolean modeHasStatisticsReports(int newMode) {
        return (newMode==MODE_1 || newMode==MODE_3);
    }
    
    
    
    private void scheduleStatisticsReportIfThereArentAny(String idOutStation, Timestamp timeSeen) {
        try {
            String statisticsReportContents = configurationManager.get(idOutStation, ConfigurationManager.STATISTICS_REPORTS_CONTENTS);
            if(ConfigurationManager.STATISTICS_REPORTS_CONTENTS_VALUE_BRIEF.equalsIgnoreCase(statisticsReportContents)) {
                scheduleReportIfThereArentAny(idOutStation, ReportEnum.STATISTICS_BRIEF, timeSeen);            
            } else {
                scheduleReportIfThereArentAny(idOutStation, ReportEnum.STATISTICS_FULL, timeSeen);
            }
        } catch (InvalidConfigurationException icex) {
            String errorMessage = "There has been an exception scheduling the Statistics Report for OutStation "+idOutStation+". Hope it works next time. Cause: "+icex.getLocalizedMessage();
            logger.error(errorMessage, icex);
        }
    }
    
    private void scheduleCongestionReportIfThereArentAny(String idOutStation, Timestamp timeSeen) {
        scheduleReportIfThereArentAny(idOutStation, ReportEnum.CONGESTION, timeSeen); 
    }
    
    private void scheduleReportIfThereArentAny(String idOutStation, ReportEnum reportEnum, Timestamp timeSeen) {
        RawDeviceDataOfOutStation rawDeviceDataOfOutStation = rawDeviceData.getRawDeviceDataOfOutStation(idOutStation, timeSeen);
        synchronized (rawDeviceDataOfOutStation) { // Only 1 thread allowed for each OutStation 
            if(!schedulerUtils.isAReportAlreadySchedule(reportEnum, idOutStation)){
                if(logger.isInfoEnabled()) { 
                    logger.info("There is no "+reportEnum.getName()+" for Outstation "+idOutStation+". One should be created");
                }
                try {
                    schedulerUtils.scheduleReports(reportEnum, idOutStation, rawDeviceData);
                } catch (UnableToScheduleException utsex) {
                    String errorMessage = "There has been an exception scheduling "+reportEnum.getName()+" for OutStation "+idOutStation+". Hope it works next time. Cause: "+utsex.getLocalizedMessage();
                    logger.error(errorMessage, utsex);
                }
            }
        }
    }
}
