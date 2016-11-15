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

import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import com.ssl.bluetruth.emitter2converter.exceptions.DeadOutStationWithNothingElseToReportException;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;
import com.ssl.bluetruth.emitter2converter.schedules.SchedulerUtils;
import com.ssl.bluetruth.emitter2converter.utils.CongestionReport;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;
import com.ssl.bluetruth.emitter2converter.utilsfortesting.AssertUtils;
import static com.ssl.bluetruth.emitter2converter.utilsfortesting.AssertUtils.assertCongestionReport;
import ssl.bluetruth.emitter2converter.utilsfortesting.ConfigurationManagerFake;
import ssl.bluetruth.emitter2converter.utilsfortesting.LogUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.PowerMockTimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.TimeUtilsFake;

/**
 *
 * @author josetrujillo-brenes
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { TimeUtils.class })
public class RawDeviceDataTest {
    
    private final Logger logger = Logger.getLogger(getClass());    
    private static final int NUMBER_OF_COMAS_WHEN_THERE_ARE_NO_DEVICES_IN_STATISTICS_REPORT = 3;
    private static final int NUMBER_OF_COMAS_WHEN_THERE_IS_ONE_DEVICE_IN_STATISTICS_REPORT = 4;
    private final String OUTSTATION = "OutStation";
    
    public RawDeviceDataTest() {
        LogUtils.showLogsInConsoleOf("ssl", Level.DEBUG);        
    }
    
    @Before
    public void setUp() {
        newConfigurationManagerWithNeededForRawDeviceData();
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
    }
    
    @After
    public void tearDown() {
        new SchedulerUtils().deleteAllSchedules();
        ThreadLocalConfigurationManager.remove();
    }
         
    @Test 
    public void getReportOfOutStationSTATISTICS_notAbsentOrBackground_reportWithoutDevices() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {        
        RawDeviceData rawDeviceData = new RawDeviceData();        
        Timestamp now = TimeUtils.currentTimestamp();        
        String idOutStation = "7778";
        String idDevice = "123456789012";        
        String secondsDeviceDetection2 = "A";
        String secondsStatisticsReport = "B";
        Timestamp nowPlusSecondsDeviceDetection2 = TimeUtils.timestampOfAdding(now, secondsDeviceDetection2);
        Timestamp nowPlusSecondsStatisticsReport = TimeUtils.timestampOfAdding(now, secondsStatisticsReport);
        
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice), now);
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice), nowPlusSecondsDeviceDetection2);
        String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, nowPlusSecondsStatisticsReport);
        assertThat(statisticsReport,not(containsString(idDevice)));
        assertEquals(NUMBER_OF_COMAS_WHEN_THERE_ARE_NO_DEVICES_IN_STATISTICS_REPORT, numberOfComasIn(statisticsReport)); 
    }
    
    @Test
    public void getReportOfOutStationSTATISTICS_withTimeUtilsFake_manyAsserts() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime();
        TimeUtilsFake.setCurrentSeconds("FFF");        
        RawDeviceData rawDeviceData = new RawDeviceData();  
        String idOutStation = "idOutStation1";
        String idDevice1 = "idDevice1";         
        TimeUtilsFake.add("1");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.currentTimestamp());
        {
            String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, TimeUtils.currentTimestamp());
            assertEquals("idOutStation1,1000,0,0", statisticsReport);
        }        
        TimeUtilsFake.add("10");
        {
            String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, TimeUtils.currentTimestamp());
            logger.info(statisticsReport);
            assertEquals("idDevice1 seen only once (0 seconds), should report 1 second", "idOutStation1,1000,10,idDevice1:0:1000:0:1,0", statisticsReport);
            //assertEquals("OutStation is dead", false, rawDeviceData.getRawDeviceDataOfOutStation(idOutStation).isAlive());
            
        }
        String idDevice2 = "idDevice2";
        ConfigurationManagerFake configurationManagerFake = (ConfigurationManagerFake) ThreadLocalConfigurationManager.get();
        configurationManagerFake.set(ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, "1");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("10");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        {
            String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, TimeUtils.currentTimestamp());
            logger.info(statisticsReport);
            assertEquals("idDevice2 is backgrond, should should be reported once", "idOutStation1,1010,10,idDevice2:0:1010:0:FFFFFFFE,0", statisticsReport);
            assertEquals("OutStation is alive", true, rawDeviceData.getRawDeviceDataOfOutStation(idOutStation,TimeUtils.currentTimestamp()).isAlive());            
        }
    }
    
    @Test 
    public void getReportOfOutStationSTATISTICS_background_reportWithFFFFFFFEAndSecondReportWithoutDevices() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {        
        RawDeviceData rawDeviceData = new RawDeviceData();        
        Timestamp now = TimeUtils.currentTimestamp();        
        String idOutStation = "7778";
        String idDevice = "123456789012";        
        String secondsDeviceDetection2 = "AAA";
        String secondsStatisticsReport = "AAB";
        Timestamp nowPlusSecondsDeviceDetection2 = TimeUtils.timestampOfAdding(now, secondsDeviceDetection2);
        Timestamp nowPlusSecondsStatisticsReport = TimeUtils.timestampOfAdding(now, secondsStatisticsReport);
        
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice), now);
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice), nowPlusSecondsDeviceDetection2);        
        
        String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, nowPlusSecondsStatisticsReport);
        assertEquals(numberOfComasIn(statisticsReport), NUMBER_OF_COMAS_WHEN_THERE_IS_ONE_DEVICE_IN_STATISTICS_REPORT);
        assertThat(statisticsReport, allOf(containsString(idDevice), containsString("0:FFFFFFFE")));
        String secondStatisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, nowPlusSecondsStatisticsReport);
        assertEquals(numberOfComasIn(secondStatisticsReport), NUMBER_OF_COMAS_WHEN_THERE_ARE_NO_DEVICES_IN_STATISTICS_REPORT);
        assertThat(secondStatisticsReport,not(containsString(idDevice)));
    }
    
    @Test 
    public void getReportOfOutStationSTATISTICS_backgroundWithTimeUtilsFake_reportWithBackgroundDeletedAndBackgroundAgain() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {        
        ConfigurationManagerFake configurationManagerFake = (ConfigurationManagerFake) ThreadLocalConfigurationManager.get();
        configurationManagerFake.set(ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, "1");
        configurationManagerFake.set(ConfigurationManager.BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS, "20");
        configurationManagerFake.set(ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS, "30");
        configurationManagerFake.set(ConfigurationManager.MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD, "1000");
        PowerMockTimeUtils.replaceRealTimeWithFakeTime();
        TimeUtilsFake.setCurrentSeconds("FFF");        
        RawDeviceData rawDeviceData = new RawDeviceData();  
        String idOutStation = "idOutStation1";
        String idDevice1 = "idDevice1";         
        TimeUtilsFake.add("1");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("A");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.currentTimestamp());
        String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, TimeUtils.currentTimestamp());
        assertEquals("idOutStation1,1000,A,idDevice1:0:1000:0:FFFFFFFE,0", statisticsReport);
        assertEquals(true, rawDeviceData.getRawDeviceDataOfOutStation(idOutStation,TimeUtils.currentTimestamp()).isAlive());
        TimeUtilsFake.add("1AA");
        statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, TimeUtils.currentTimestamp());
        assertEquals("idOutStation1,100A,1AA,0", statisticsReport); 
        //Doesn't appear in next report and it's been so much time, that the device is deleted
        TimeUtilsFake.add("A");        
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("2");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.currentTimestamp());
        statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, TimeUtils.currentTimestamp());
        assertEquals("idOutStation1,11B4,C,idDevice1:0:11BE:0:FFFFFFFE,0", statisticsReport);
    }
    
    @Test 
    public void getReportOfOutStationSTATISTICS_absent_reportAndSecondReportWithoutDevices() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {        
        RawDeviceData rawDeviceData = new RawDeviceData();        
        Timestamp beforeNow = TimeUtils.currentTimestampAdding(-30);
        String idOutStation = "7778";
        String idDevice = "123456789012";
        String secondsDeviceDetection2 = "5";
        Timestamp nowPlusSecondsDeviceDetection2 = TimeUtils.timestampOfAdding(beforeNow, secondsDeviceDetection2);
        Timestamp now = TimeUtils.currentTimestamp();
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice), beforeNow);
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice), nowPlusSecondsDeviceDetection2);
        String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, now);
        logger.info(statisticsReport);
        assertEquals(numberOfComasIn(statisticsReport), NUMBER_OF_COMAS_WHEN_THERE_IS_ONE_DEVICE_IN_STATISTICS_REPORT);
        String secondStatisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, now);
        assertEquals(numberOfComasIn(secondStatisticsReport), NUMBER_OF_COMAS_WHEN_THERE_ARE_NO_DEVICES_IN_STATISTICS_REPORT);        
    }
    
    @Test 
    public void getReportOfOutStationSTATISTICS_faultyAndNewDetections_recoversFromFaulty() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {        
        RawDeviceData rawDeviceData = new RawDeviceData();        
        PowerMockTimeUtils.replaceRealTimeWithFakeTime();
        TimeUtilsFake.setCurrentSeconds("1");       
        String idOutStation = "idOutStation1";
        String idDevice1 = "idDevice1"; 
        String idDevice2 = "idDevice2"; 
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("100");
        String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, idOutStation, TimeUtils.currentTimestamp());      
        assertEquals("idOutStation1,1,100,idDevice1:0:1:0:FFFFFFFF,0", statisticsReport);      
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1,idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1,idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        String secondStatisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, idOutStation, TimeUtils.currentTimestamp());
        assertEquals("idOutStation1,101,1E,idDevice1:0:101:2:5,idDevice2:0:101:0:0,0", AssertUtils.sortDevicesInStatisticsReport(secondStatisticsReport));
    }
    
    @Test
    public void getReportOfOutStationSTATISTICS_disorderedDetections_timeSeenIsFixed() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        RawDeviceData rawDeviceData = new RawDeviceData();        
        PowerMockTimeUtils.replaceRealTimeWithFakeTime();
        String idOutStation = "idOutStation1";
        String idDevice1 = "idDevice1";
        String idDevice2 = "idDevice2"; 
        TimeUtilsFake.setCurrentSeconds("10"); 
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.currentTimestamp());
        TimeUtilsFake.setCurrentSeconds("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice2), TimeUtils.currentTimestamp());
        TimeUtilsFake.add("5");
        String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, idOutStation, TimeUtils.currentTimestamp());

        assertEquals("idOutStation1,10,E,idDevice1:0:5:0:0,idDevice2:0:A:0:0,0",AssertUtils.sortDevicesInStatisticsReport(statisticsReport));
        //before fixing the error, the report was idOutStation1,10,E,idDevice1:0:10:FFFFFFFFFFFFFFFB:FFFFFFFFFFFFFFF5,idDevice2:0:A:0:0,0 
    }

    @Test
    public void getReportOfOutStationSTATISTICS_disorderedAndOldDetections_deviceNotFaulty() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        ConfigurationManagerFake configurationManagerFake = (ConfigurationManagerFake) ThreadLocalConfigurationManager.get();
        configurationManagerFake.set(ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, "999");
        RawDeviceData rawDeviceData = new RawDeviceData();        
        PowerMockTimeUtils.replaceRealTimeWithFakeTime();
        String idOutStation = "idOutStation1";
        String idDevice1 = "idDevice1";
        TimeUtilsFake.setCurrentSeconds("AAA");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.timestampOf("AA0"));
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList(idDevice1), TimeUtils.timestampOf("A00"));
        TimeUtilsFake.setCurrentSeconds("ABA");
        String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, idOutStation, TimeUtils.currentTimestamp());        
        assertThat(statisticsReport,containsString(idDevice1+":0:A00:50:A0")); //Before fixing was: ,idDevice1:0:A00:0:FFFFFFFF,        
    }
    
    @Test
    public void getReportOfOutStationSTATISTICS_unsynchronizedDetections_correctReportRange() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        RawDeviceData rawDeviceData = new RawDeviceData();        
        PowerMockTimeUtils.replaceRealTimeWithFakeTime();
        String idOutStation = "idOutStation1";
        TimeUtilsFake.setCurrentSeconds("A00");
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList("devBeforeRange"), TimeUtils.timestampOf("000"));
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList("devInRange"), TimeUtils.timestampOf("AA0"));
        rawDeviceData.addDeviceDetections(idOutStation, Arrays.asList("devAfterRange"), TimeUtils.timestampOf("ABB"));
        TimeUtilsFake.setCurrentSeconds("AAA");
        String statisticsReport = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, idOutStation, TimeUtils.currentTimestamp()); 
        assertThat(statisticsReport,startsWith(idOutStation+",A00,AA")); //Before fixing was: idOutStation1,0,AAA
    }
    
    @Test 
    public void getReportOfOutStationCONGESTION_oneEach() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        RawDeviceData rawDeviceData = new RawDeviceData();              
        addDeviceDetectionsSecondsBeforeNowAndNow(rawDeviceData, OUTSTATION, Arrays.asList("staticDevice"), SECONDS_FOR_STATIC);
        addDeviceDetectionsSecondsBeforeNowAndNow(rawDeviceData, OUTSTATION, Arrays.asList("verySlowDevice"), SECONDS_FOR_VERY_SLOW);
        addDeviceDetectionsSecondsBeforeNowAndNow(rawDeviceData, OUTSTATION, Arrays.asList("slowDevice"), SECONDS_FOR_SLOW);
        addDeviceDetectionsSecondsBeforeNowAndNow(rawDeviceData, OUTSTATION, Arrays.asList("moderateDevice"), SECONDS_FOR_MODERATE);
        addDeviceDetectionsSecondsBeforeNowAndNow(rawDeviceData, OUTSTATION, Arrays.asList("freeDevice"), SECONDS_FOR_FREE);        
        assertCongestionReport(rawDeviceData, OUTSTATION, "1:1:1:1:1", CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);        
    }
    
    @Test 
    public void getReportOfOutStationCONGESTION_absentAndBackground_ShouldNotAppear() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {        
        RawDeviceData rawDeviceData = new RawDeviceData();
        addDeviceDetectionsSecondsBeforeNowAndNow(rawDeviceData, OUTSTATION, Arrays.asList("backgroundDevice"), SECONDS_FOR_BACKGROUND+1);
        addDeviceDetectionSecondsBeforeNow(rawDeviceData, OUTSTATION, Arrays.asList("absentDevice"), SECONDS_FOR_ABSENT+1);
        assertCongestionReport(rawDeviceData, OUTSTATION, "0:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);
    }
    
    @Test 
    public void getReportOfOutStationCONGESTION_neverAddedDevices_reportNotReady() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        RawDeviceData rawDeviceData = new RawDeviceData();        
        assertCongestionReport(rawDeviceData, OUTSTATION, "0:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NOT_READY);
    }
    
    @Test 
    public void getReportOfOutStationCONGESTION_justAddedDevice_reportNotReady() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        RawDeviceData rawDeviceData = new RawDeviceData();
        addDeviceDetectionSecondsBeforeNow(rawDeviceData, OUTSTATION, Arrays.asList("justAddedDevice"), SECONDS_FOR_FREE);
        assertCongestionReport(rawDeviceData, OUTSTATION, "1:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NOT_READY);
    }
    
    @Test 
    public void getReportOfOutStationCONGESTION_AddedDeviceBeforeStartUp_reportReady() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        RawDeviceData rawDeviceData = new RawDeviceData();
        addDeviceDetectionsSecondsBeforeNowAndNow(rawDeviceData, OUTSTATION, Arrays.asList("verySlowDevice"), SECONDS_FOR_VERY_SLOW);
        assertCongestionReport(rawDeviceData, OUTSTATION, "0:0:0:1:0", CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);
    }
    
    @Test 
    public void getReportOfOutStationCONGESTION_AddedManyDeviceBeforeStartUp_queuePresent() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        RawDeviceData rawDeviceData = new RawDeviceData();
        addDeviceDetectionsSecondsBeforeNowAndNow(rawDeviceData, OUTSTATION, Arrays.asList("1", "2", "3", "4", "5", "6"), SECONDS_FOR_VERY_SLOW);
        assertCongestionReport(rawDeviceData, OUTSTATION, "0:0:0:6:0", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
    }
    
    @Test 
    public void getReportOfOutStationCONGESTION_AddedDeviceJustOnce_staysInFreeUntilIsAbsent() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        RawDeviceData rawDeviceData = new RawDeviceData();
        addDeviceDetectionSecondsBeforeNow(rawDeviceData, OUTSTATION, Arrays.asList("verySlowDevice"), SECONDS_FOR_VERY_SLOW);
        assertCongestionReport(rawDeviceData, OUTSTATION, "1:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);
    }
    
    private void addDeviceDetectionsSecondsBeforeNowAndNow(RawDeviceData rawDeviceData, String outStation, List<String> deviceIds, int seconds) throws InvalidConfigurationException {
        addDeviceDetectionSecondsBeforeNow(rawDeviceData, outStation, deviceIds, seconds);
        addDeviceDetectionSecondsBeforeNow(rawDeviceData, outStation, deviceIds, 0);
    }
    
    private void addDeviceDetectionSecondsBeforeNow(RawDeviceData rawDeviceData, String outStation, List<String> deviceIds, int seconds) throws InvalidConfigurationException {
        logger.info("Device detection is going to be added at "+TimeUtils.currentTimestampAdding(-seconds));
        rawDeviceData.addDeviceDetections(outStation, deviceIds, TimeUtils.currentTimestampAdding(-seconds)); 
    }
    
    private final int SECONDS_FOR_FREE = 2;
    private final int SECONDS_FOR_MODERATE = 4;
    private final int SECONDS_FOR_SLOW = 6;
    private final int SECONDS_FOR_VERY_SLOW = 8;
    private final int SECONDS_FOR_STATIC = 10;
    private final int SECONDS_FOR_ABSENT = 15;
    private final int SECONDS_FOR_BACKGROUND = 100;
    private final int SECONDS_FOR_ABSENT_BACKGROUND = 1000;
    
    private void newConfigurationManagerWithNeededForRawDeviceData() {
        ConfigurationManagerFake configurationManagerFake = new ConfigurationManagerFake();
        configurationManagerFake.set(ConfigurationManager.FREE_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_FREE));
        configurationManagerFake.set(ConfigurationManager.MODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_MODERATE));
        configurationManagerFake.set(ConfigurationManager.SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_SLOW));
        configurationManagerFake.set(ConfigurationManager.VERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_VERY_SLOW));        
        configurationManagerFake.set(ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_ABSENT));
        configurationManagerFake.set(ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_BACKGROUND));
        configurationManagerFake.set(ConfigurationManager.BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_ABSENT_BACKGROUND));
        configurationManagerFake.set(ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN, ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_VERY_SLOW_STRING);
        configurationManagerFake.set(ConfigurationManager.QUEUE_DETECT_THREHOLD, "5");
        configurationManagerFake.set(ConfigurationManager.QUEUE_CLEARANCE_THREHOLD, "3");
        configurationManagerFake.set(ConfigurationManager.QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS, String.valueOf(SECONDS_FOR_SLOW));
        configurationManagerFake.set(ConfigurationManager.EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS, "10");
        configurationManagerFake.set(ConfigurationManager.MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD, "3");
        ThreadLocalConfigurationManager.set(configurationManagerFake);
    }
    
    private int numberOfComasIn(String string) {
        return StringUtils.countMatches(string, ",");
    }
}
