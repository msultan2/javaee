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
 * Created on 29-Jul-2015 02:20 PM
 */
package com.ssl.bluetruth.emitter2converter.raw;

import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManagerImpl;
import ssl.bluetruth.emitter2converter.utilsfortesting.DefaultAndOutStationsConfigurationFake;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import com.ssl.bluetruth.emitter2converter.exceptions.DeadOutStationWithNothingElseToReportException;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;
import com.ssl.bluetruth.emitter2converter.schedules.SchedulerUtils;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.LogUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.PowerMockTimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.TimeUtilsFake;

/**
 *
 * @author josetrujillo-brenes
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { TimeUtils.class })
public class RawDeviceDataOfOutStationCleanerTest {
    
    private final static String OUTSTATION = "OutStation";
    private final static String BACKGROUND_DEVICE = "BackgroundDevice";
    private final static String DEVICE1 = "Device1";
    private final static String DEVICE2 = "Device2";
    
    public RawDeviceDataOfOutStationCleanerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        LogUtils.showLogsInConsoleOf("ssl", Level.DEBUG);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {        
    }
    
    @After
    public void tearDown() {
        new SchedulerUtils().deleteAllSchedules();
        ThreadLocalConfigurationManager.remove();
    }

    @Test
    public void getReportOfOutStation_mode0_AllReportsCleanDevices() throws InvalidConfigurationException, DeadOutStationWithNothingElseToReportException {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
        Map<String, String> configurationMap = getFakeConfigurationAndSetItInThreadLocal();
        configurationMap.put(ConfigurationManager.MODE, "1");
        configurationMap.put(ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS, "150");        
        RawDeviceData rawDeviceData = new RawDeviceData(); 
        
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(BACKGROUND_DEVICE), TimeUtils.currentTimestamp());
        TimeUtilsFake.add(500);
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(BACKGROUND_DEVICE, DEVICE1), TimeUtils.currentTimestamp());   
        TimeUtilsFake.add(40);
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE2), TimeUtils.currentTimestamp());
        configurationMap.put(ConfigurationManager.MODE, "0");
        TimeUtilsFake.add(60);
        assertThat("Mode has change.",
                ThreadLocalConfigurationManager.get().getInt(OUTSTATION, ConfigurationManager.MODE), is(0));
        assertThat("No device has been clean changing the mode.",
                rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(3));
        TimeUtilsFake.add(60);
        rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, OUTSTATION, TimeUtils.currentTimestamp());
        assertThat(DEVICE1+" has been declared as absent and clean by "+ReportEnum.STATISTICS_FULL.getName()+".",
                rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
        rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, OUTSTATION, TimeUtils.currentTimestamp());
        rawDeviceData.getReportOfOutStation(ReportEnum.CONGESTION, OUTSTATION, TimeUtils.currentTimestamp()); 
        assertThat("Consecutive reports shouldn't clean devices.",
                rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
        TimeUtilsFake.add(60);
        assertThat(DEVICE2+" hasn't been declared as absent and clean by any report.",
                rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
        rawDeviceData.getReportOfOutStation(ReportEnum.CONGESTION, OUTSTATION, TimeUtils.currentTimestamp()); 
        assertThat(DEVICE2+" has been declared as absent and clean by "+ReportEnum.CONGESTION.getName()+".",
                rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(1));
        TimeUtilsFake.add(1000);
        rawDeviceData.addDeviceDetections(OUTSTATION, Collections.<String>emptyList(), TimeUtils.currentTimestamp());
        assertThat(BACKGROUND_DEVICE+" hasn't been declared as absent and clean by any report.",
                rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(1));
        rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, OUTSTATION, TimeUtils.currentTimestamp());
        assertThat(BACKGROUND_DEVICE+" has been declared as absent and clean by "+ReportEnum.STATISTICS_BRIEF.getName()+".",
                rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(0));
    }
       
    @Test
    public void getReportOfOutStation_mode2_OnlyCongestionReportsCleanDevices() throws InvalidConfigurationException, DeadOutStationWithNothingElseToReportException {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);               
        Map<String, String> configurationMap = getFakeConfigurationAndSetItInThreadLocal();
        configurationMap.put(ConfigurationManager.MODE, "2");
        configurationMap.put(ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS, "15");
        configurationMap.put(ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, "100");
        RawDeviceData rawDeviceData = new RawDeviceData();

        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList("idBackgroundDevice"), TimeUtils.currentTimestamp());
        TimeUtilsFake.add(500);
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList("idBackgroundDevice", "idDevice2"), TimeUtils.currentTimestamp());   
        TimeUtilsFake.add(10);
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList("idDevice3"), TimeUtils.currentTimestamp());                 
        TimeUtilsFake.add(10);
        assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(3));
        rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, OUTSTATION, TimeUtils.currentTimestamp());    
        assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(3));
        rawDeviceData.getReportOfOutStation(ReportEnum.CONGESTION, OUTSTATION, TimeUtils.currentTimestamp()); 
        assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
        rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, OUTSTATION, TimeUtils.currentTimestamp());    
        assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
        TimeUtilsFake.add(10);
        assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
        rawDeviceData.getReportOfOutStation(ReportEnum.CONGESTION, OUTSTATION, TimeUtils.currentTimestamp()); 
        assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(1));
        TimeUtilsFake.add(1000);
        assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(1));
        rawDeviceData.getReportOfOutStation(ReportEnum.CONGESTION, OUTSTATION, TimeUtils.currentTimestamp()); 
        assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(0));
    }
    
    @Test
    public void getReportOfOutStation_modes1Or3OrInvalid_OnlyStatisticsReportsCleanDevices() throws InvalidConfigurationException, DeadOutStationWithNothingElseToReportException {                
        PowerMockTimeUtils.replaceRealTimeWithFakeTime();
        for (String mode : Arrays.asList("1", "3", "", "5")) { 
            TimeUtilsFake.setCurrentSeconds(0);              
            Map<String, String> configurationMap = getFakeConfigurationAndSetItInThreadLocal();
            configurationMap.put(ConfigurationManager.MODE, mode);
            configurationMap.put(ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS, "15");
            configurationMap.put(ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, "100");
            RawDeviceData rawDeviceData = new RawDeviceData();
        
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList("idBackgroundDevice"), TimeUtils.currentTimestamp());
            TimeUtilsFake.add(500);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList("idBackgroundDevice", "idDevice2"), TimeUtils.currentTimestamp());   
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList("idDevice3"), TimeUtils.currentTimestamp());                 
            TimeUtilsFake.add(10);      
            assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(3));
            rawDeviceData.getReportOfOutStation(ReportEnum.CONGESTION, OUTSTATION, TimeUtils.currentTimestamp()); 
            assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(3));
            rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, OUTSTATION, TimeUtils.currentTimestamp());    
            assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
            rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, OUTSTATION, TimeUtils.currentTimestamp());    
            assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
            TimeUtilsFake.add(10); 
            assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
            rawDeviceData.getReportOfOutStation(ReportEnum.CONGESTION, OUTSTATION, TimeUtils.currentTimestamp()); 
            assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(2));
            rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_FULL, OUTSTATION, TimeUtils.currentTimestamp());    
            assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(1));
            TimeUtilsFake.add(1000);
            rawDeviceData.addDeviceDetections(OUTSTATION, Collections.<String>emptyList(), TimeUtils.currentTimestamp());
            assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(1));
            rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, OUTSTATION, TimeUtils.currentTimestamp()); 
            assertThat(rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(0));
        } 
    }
    
    @Test
    public void ApplyConfigurationChanges_fromModes1Or2Or3OrInvalidTo0_CleansDevices() throws InvalidConfigurationException {      
        PowerMockTimeUtils.replaceRealTimeWithFakeTime();
        for (String mode : Arrays.asList("1", "2", "3", "5")) {             
            TimeUtilsFake.setCurrentSeconds(0);               
            Map<String, String> configurationMap = getFakeConfigurationAndSetItInThreadLocal();
            configurationMap.put(ConfigurationManager.MODE, "2");
            configurationMap.put(ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS, "15");
            configurationMap.put(ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, "100");
            configurationMap.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES, "1");
            RawDeviceData rawDeviceData = new RawDeviceData();
            
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE2), TimeUtils.currentTimestamp());
            assertThat("In mode '"+mode+"', no device should be clean before changing the mode.",
                    rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(1));
            configurationMap.put(ConfigurationManager.MODE, "0");
            TimeUtilsFake.add(100);
            assertThat("Mode has change.",
                    ThreadLocalConfigurationManager.get().getInt(OUTSTATION, ConfigurationManager.MODE), is(0));
            assertThat("From mode '"+mode+"' to mode '0', absent devices have been clean.",
                    rawDeviceData.getNumberOfDevices(OUTSTATION, TimeUtils.currentTimestamp()), is(0));
        } 
    }
    
    private final int SECONDS_FOR_FREE = 2;
    private final int SECONDS_FOR_MODERATE = 4;
    private final int SECONDS_FOR_SLOW = 6;
    private final int SECONDS_FOR_VERY_SLOW = 8;
    private final int SECONDS_FOR_ABSENT = 100;
    private final int SECONDS_FOR_BACKGROUND = 150;
    private final int SECONDS_FOR_ABSENT_BACKGROUND = 1000;
    
    private Map<String, String> newConfigurationMapWithNeededForTesting() {
        Map<String, String> map = new HashMap();
        map.put(ConfigurationManager.FREE_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_FREE));
        map.put(ConfigurationManager.MODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_MODERATE));
        map.put(ConfigurationManager.SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_SLOW));
        map.put(ConfigurationManager.VERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_VERY_SLOW));
        map.put(ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_ABSENT));
        map.put(ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_BACKGROUND));
        map.put(ConfigurationManager.BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS, String.valueOf(SECONDS_FOR_ABSENT_BACKGROUND));
        map.put(ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN, ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_VERY_SLOW_STRING);
        map.put(ConfigurationManager.QUEUE_DETECT_THREHOLD, "5");
        map.put(ConfigurationManager.QUEUE_CLEARANCE_THREHOLD, "3");
        map.put(ConfigurationManager.QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS, String.valueOf(SECONDS_FOR_SLOW));
        map.put(ConfigurationManager.EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS, "10");
        map.put(ConfigurationManager.MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD, "3");
        map.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES, "1");
        map.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES, "2");
        map.put(ConfigurationManager.STATISTICS_REPORTS_CONTENTS, ConfigurationManager.STATISTICS_REPORTS_CONTENTS_VALUE_FULL);
        map.put(ConfigurationManager.STATISTICS_REPORT_PERIOD_IN_SECONDS, "60");
        map.put(ConfigurationManager.CONGESTION_REPORT_PERIOD_IN_SECONDS, "60");
        return map;
    }
    
    private Map<String, String> getFakeConfigurationAndSetItInThreadLocal() {
        Map<String, String> outStationsMap = newConfigurationMapWithNeededForTesting();        
        DefaultAndOutStationsConfigurationFake fakeConfiguration = new DefaultAndOutStationsConfigurationFake(new HashMap(), outStationsMap);
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(fakeConfiguration, fakeConfiguration);
        ThreadLocalConfigurationManager.set(configurationManager);
        return outStationsMap;
    }
}
