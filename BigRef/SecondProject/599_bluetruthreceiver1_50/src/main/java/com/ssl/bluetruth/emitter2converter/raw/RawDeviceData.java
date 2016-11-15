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

package com.ssl.bluetruth.emitter2converter.raw;

import com.ssl.bluetruth.emitter2converter.exceptions.DeadOutStationWithNothingElseToReportException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_0;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_1;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_2;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_3;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;
import com.ssl.bluetruth.emitter2converter.schedules.SchedulerUtils;
import com.ssl.bluetruth.emitter2converter.utils.CongestionReport;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import com.ssl.bluetruth.emitter2converter.schedules.SchedulerManager;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;

/**
 * Class which contains all the raw device data of all OutStations
 * @author josetrujillo-brenes
 */
public class RawDeviceData {
    
    private final Logger logger = Logger.getLogger(getClass());    
    private final Map<String, RawDeviceDataOfOutStation> mapRawDeviceDataOfEachOutStation;
    private final SchedulerManager schedulerManager;
    
    public RawDeviceData() {
        mapRawDeviceDataOfEachOutStation = new HashMap<>();
        schedulerManager = new SchedulerManager(this);        
        if(logger.isInfoEnabled()) { 
            logger.info("Storage of the Raw Device Data created successfully");
        }
    }
    
    /**
     * Creates a new RawDeviceDataOfOutStation and stores it in the map
     * @param idOutStation
     * @param startTime 
     */
    private void newRawDeviceDataOfOutStation(String idOutStation, Timestamp startTime) {
        mapRawDeviceDataOfEachOutStation.put(idOutStation, new RawDeviceDataOfOutStation(idOutStation, startTime));
        if(logger.isDebugEnabled()) {
            logger.debug("New RawDeviceDataOfOutStation created for OutStation "+idOutStation+" at "+startTime+". Schedulers reports should be created");
        }
    }    
    
    /**
     * Gets the RawDeviceDataOfOutStation of the map or creates a new one if there isn't any.
     * @param idOutStation
     * @param timeSeen
     * @return 
     */
    public RawDeviceDataOfOutStation getRawDeviceDataOfOutStation(String idOutStation, Timestamp timeSeen) {
        if(!mapRawDeviceDataOfEachOutStation.containsKey(idOutStation)) {            
            newRawDeviceDataOfOutStation(idOutStation, timeSeen);
        } 
        return mapRawDeviceDataOfEachOutStation.get(idOutStation);
    }
         
    /**
     * It will add the devices but also create the schedules if it doesn't find any.
     * @param idOutStation
     * @param deviceIds
     * @param timeSeen
     * @throws InvalidConfigurationException 
     */
    public void addDeviceDetections(String idOutStation, List<String> deviceIds, Timestamp timeSeen) throws InvalidConfigurationException { 
        Integer mode;
        try {
            mode = ThreadLocalConfigurationManager.get().getInt(idOutStation, ConfigurationManager.MODE);
        } catch (InvalidConfigurationException icex) {
            mode = MODE_3;
            logger.warn("There has been an exception trying to obtain the mode of the Detector "+idOutStation+". This is not valid and it should be fixed. Meanwhile it will be considered as mode "+mode+". Exception: "+icex.getLocalizedMessage());            
        } 
        switch (mode) {
            case MODE_0:                
                getRawDeviceDataOfOutStation(idOutStation, timeSeen).addDeviceDetections(Collections.<String>emptyList(), timeSeen);
                if(logger.isInfoEnabled()) { 
                    logger.info("The device detection received at "+timeSeen+" will be ignored because the Outstation "+idOutStation+" is in mode "+MODE_0);
                }
                break;
            case MODE_1:                
                getRawDeviceDataOfOutStation(idOutStation, timeSeen).addDeviceDetections(deviceIds, timeSeen);                
                break;
            case MODE_2:                
                getRawDeviceDataOfOutStation(idOutStation, timeSeen).addDeviceDetections(deviceIds, timeSeen);
                break;            
            default:
                if(mode != MODE_3) {
                    logger.warn("The mode of the Detector "+idOutStation+" is "+mode+". This is not a valid value and it should be fixed. Meanwhile it will be considered as mode "+MODE_3);
                }     
                getRawDeviceDataOfOutStation(idOutStation, timeSeen).addDeviceDetections(deviceIds, timeSeen);
                break;
        }
        schedulerManager.ScheduleReports(idOutStation, timeSeen);
    } 
   
    
    /**
     * Creates a String with the statistics or congestion report
     * @param report which Report
     * @param idOutStation Used as part of the report and to get the RawDeviceDataOfOutStation
     * @param reportTime Used as the current time of the report 
     * @return
     * @throws DeadOutStationWithNothingElseToReportException
     * @throws InvalidConfigurationException 
     */
    public String getReportOfOutStation(ReportEnum report, String idOutStation, Timestamp reportTime) 
            throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        String methodLog = null;
        if(logger.isDebugEnabled()) {
            methodLog = "getReportOfOutStation - report: '"+report.getName()+"', idOutStation: '"+idOutStation+"', reportTime: '"+reportTime+"'";
            logger.debug(methodLog);
        }
        String result;
        if(report == ReportEnum.STATISTICS_BRIEF || report == ReportEnum.STATISTICS_FULL) {
            result = getStatisticsReportOfOutStation(report, idOutStation, reportTime);
        } else if(report == ReportEnum.CONGESTION) {
            result = getCongestionReportOfOutStation(idOutStation, reportTime);
        } else {
            throw new UnsupportedOperationException("There is no implementation for report "+report.getName());
        }
        if(logger.isDebugEnabled()) {
            logger.debug(methodLog+" Result: "+result);
        }
        return result;
    } 
    
    /**
     * Creates a String with the congestion report
     * @param idOutStation Used as part of the report and to get the RawDeviceDataOfOutStation
     * @param reportTime Used as the current time of the report 
     * @return
     * @throws InvalidConfigurationException 
     */
    private String getCongestionReportOfOutStation(String idOutStation, Timestamp startTime) throws InvalidConfigurationException, DeadOutStationWithNothingElseToReportException {
        RawDeviceDataOfOutStation rawDeviceDataOfOutStation = getRawDeviceDataOfOutStation(idOutStation,TimeUtils.currentTimestamp());
        CongestionReport congestionReport = new CongestionReport(idOutStation);
        congestionReport.setTimeReport(startTime);
        congestionReport = rawDeviceDataOfOutStation.setBinsOf(congestionReport);        
        congestionReport = rawDeviceDataOfOutStation.setQueuePresentOf(congestionReport);
        if(!rawDeviceDataOfOutStation.isAlive() && (congestionReport.isEmpty())){ 
            if(logger.isInfoEnabled()) {
                String mesage = "The OutStation '"+idOutStation+"' is dead and the Congestion Report is empty. Congestion Schedule should be shutdown until next device detection is received";
                logger.info(mesage); 
            }                       
            new SchedulerUtils().delete(ReportEnum.CONGESTION, idOutStation);
        }
        rawDeviceDataOfOutStation.getCleaner().clean(ReportEnum.CONGESTION);
        return congestionReport.getV4String();
    }
    
    /**
     * Creates a String with the statistics report
     * @param report Indicates if the report is brief or full
     * @param idOutStation Used as part of the report and to get the RawDeviceDataOfOutStation
     * @param reportTime Used first as the current time of the report and once finish is store as the timestampLastStatisticsReport 
     * @return
     * @throws InvalidConfigurationException 
     */
    private String getStatisticsReportOfOutStation(ReportEnum report, String idOutStation, Timestamp reportTime) throws InvalidConfigurationException, DeadOutStationWithNothingElseToReportException {
        RawDeviceDataOfOutStation rawDeviceDataOfOutStation = getRawDeviceDataOfOutStation(idOutStation, reportTime);
        if(rawDeviceDataOfOutStation.isAlive() || rawDeviceDataOfOutStation.hasUnreportedDevices()) {
            StringBuilder sb = new StringBuilder();
            sb.append(idOutStation).append(",");
            sb.append(rawDeviceDataOfOutStation.getSecondsLastStatisticsReportInHex()).append(",");
            sb.append(rawDeviceDataOfOutStation.getSecondsInHexBetweenLastStatisticsReportAnd(reportTime)).append(",");
            sb.append(rawDeviceDataOfOutStation.getDevicesPartOfStatisticsReport(report, reportTime));
            sb.append("0"); //No need to implement RND 
            String statisticsReportOfOutStation = sb.toString();
            rawDeviceDataOfOutStation.setTimestampLastStatisticsReport(reportTime);
            rawDeviceDataOfOutStation.getCleaner().clean(report);
            return statisticsReportOfOutStation;
        } else {
            String mesage = "The OutStation '"+idOutStation+"' is dead and all devices have been reported in the Statistics report. Statistic Schedule should be shutdown until next device detection is received";
            logger.info(mesage);
            throw new DeadOutStationWithNothingElseToReportException(mesage);
        }   
    }
    
    public int getNumberOfDevices(String idOutStation, Timestamp reportTime) {
        return getRawDeviceDataOfOutStation(idOutStation, reportTime).getNumberOfDevices();
    }
}
