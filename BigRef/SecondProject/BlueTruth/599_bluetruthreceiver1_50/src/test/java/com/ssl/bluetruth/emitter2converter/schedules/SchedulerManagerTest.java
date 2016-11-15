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
 * Created on 05-Aug-2015 02:33 PM
 */
package com.ssl.bluetruth.emitter2converter.schedules;

import com.ssl.bluetruth.emitter2converter.schedules.SchedulerUtils;
import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;
import java.util.Arrays;
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
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.DefaultAndOutStationsConfigurationFake;
import ssl.bluetruth.emitter2converter.utilsfortesting.LogUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.PowerMockTimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.TimeUtilsFake;

/**
 *
 * @author josetrujillo-brenes
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { TimeUtils.class })
public class SchedulerManagerTest {
    
    private final static String OUTSTATION = "OutStation";
    private final static String DEVICE1 = "Device1";
    
    public SchedulerManagerTest() {
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
    public void SchedulerManager_beforeDeviceDetections_noReportsAreSchedule() throws InvalidConfigurationException {        
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
        Map<String, String> configurationMap = getFakeConfigurationAndSetItInThreadLocal();
        configurationMap.put(ConfigurationManager.MODE, "1");        
        RawDeviceData rawDeviceData = new RawDeviceData();        
        SchedulerUtils schedulerUtils = new SchedulerUtils();        
        assertThat("There are not "+ReportEnum.CONGESTION.getName()+" schedule before a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.CONGESTION, OUTSTATION), is(false));
        assertThat("There are not "+ReportEnum.STATISTICS_BRIEF.getName()+" schedule before a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_BRIEF, OUTSTATION), is(false));
        assertThat("There are not "+ReportEnum.STATISTICS_FULL.getName()+" schedule before a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_FULL, OUTSTATION), is(false));
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1), TimeUtils.currentTimestamp());
    }
    
    @Test
    public void SchedulerManager_inMode2AfterDeviceDetections_onlyCongestionReportsAreSchedule() throws InvalidConfigurationException {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
        Map<String, String> configurationMap = getFakeConfigurationAndSetItInThreadLocal();
        configurationMap.put(ConfigurationManager.MODE, "2");        
        RawDeviceData rawDeviceData = new RawDeviceData();        
        SchedulerUtils schedulerUtils = new SchedulerUtils(); 
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1), TimeUtils.currentTimestamp());
        assertThat("There is a "+ReportEnum.CONGESTION.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.CONGESTION, OUTSTATION), is(true));
        assertThat("There are not "+ReportEnum.STATISTICS_BRIEF.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_BRIEF, OUTSTATION), is(false));
        assertThat("There are not "+ReportEnum.STATISTICS_FULL.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_FULL, OUTSTATION), is(false));
    }
    
    @Test
    public void SchedulerManager_inMode3AfterDeviceDetections_BothReportsAreSchedule() throws InvalidConfigurationException {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
        Map<String, String> configurationMap = getFakeConfigurationAndSetItInThreadLocal();
        configurationMap.put(ConfigurationManager.MODE, "3");        
        RawDeviceData rawDeviceData = new RawDeviceData(); 
        SchedulerUtils schedulerUtils = new SchedulerUtils(); 
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1), TimeUtils.currentTimestamp());
        assertThat("There is a "+ReportEnum.CONGESTION.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.CONGESTION, OUTSTATION), is(true));
        assertThat("There is a "+ReportEnum.STATISTICS_BRIEF.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_BRIEF, OUTSTATION), is(true));
        assertThat("There are not "+ReportEnum.STATISTICS_FULL.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_FULL, OUTSTATION), is(false));
    }
    
    @Test
    public void SchedulerManager_inMode1AfterDeviceDetectionsChangingToMode0_StatisticsReportIsUnschedule() throws InvalidConfigurationException {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
        Map<String, String> configurationMap = getFakeConfigurationAndSetItInThreadLocal();
        configurationMap.put(ConfigurationManager.MODE, "1");        
        RawDeviceData rawDeviceData = new RawDeviceData(); 
        SchedulerUtils schedulerUtils = new SchedulerUtils(); 
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1), TimeUtils.currentTimestamp());        
        assertThat("There is a "+ReportEnum.STATISTICS_BRIEF.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_BRIEF, OUTSTATION), is(true));
        configurationMap.put(ConfigurationManager.MODE, "0");
        TimeUtilsFake.add(60);
        assertThat("Mode has change.",
                ThreadLocalConfigurationManager.get().getInt(OUTSTATION, ConfigurationManager.MODE), is(0));
        assertThat("There are not "+ReportEnum.STATISTICS_BRIEF.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_BRIEF, OUTSTATION), is(false));
    }
    
    @Test
    public void SchedulerManager_statisticsReportContentsChange_ScheduleChange() throws InvalidConfigurationException {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
        Map<String, String> configurationMap = getFakeConfigurationAndSetItInThreadLocal();
        configurationMap.put(ConfigurationManager.MODE, "1");        
        configurationMap.put(ConfigurationManager.STATISTICS_REPORTS_CONTENTS, ConfigurationManager.STATISTICS_REPORTS_CONTENTS_VALUE_BRIEF);        
        RawDeviceData rawDeviceData = new RawDeviceData(); 
        SchedulerUtils schedulerUtils = new SchedulerUtils(); 
        rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1), TimeUtils.currentTimestamp());
        assertThat("There is a "+ReportEnum.STATISTICS_BRIEF.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_BRIEF, OUTSTATION), is(true));
        assertThat("There is not a "+ReportEnum.STATISTICS_FULL.getName()+" schedule after a device detection",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_FULL, OUTSTATION), is(false));
        configurationMap.put(ConfigurationManager.STATISTICS_REPORTS_CONTENTS, ConfigurationManager.STATISTICS_REPORTS_CONTENTS_VALUE_FULL);
        TimeUtilsFake.add(60);
        assertThat(ConfigurationManager.STATISTICS_REPORTS_CONTENTS+" has change.",
                ThreadLocalConfigurationManager.get().get(OUTSTATION, ConfigurationManager.STATISTICS_REPORTS_CONTENTS), is(ConfigurationManager.STATISTICS_REPORTS_CONTENTS_VALUE_FULL));
        assertThat("There is not a "+ReportEnum.STATISTICS_BRIEF.getName()+" schedule after "+ConfigurationManager.STATISTICS_REPORTS_CONTENTS+" change",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_BRIEF, OUTSTATION), is(false));
        assertThat("There is a "+ReportEnum.STATISTICS_FULL.getName()+" schedule after "+ConfigurationManager.STATISTICS_REPORTS_CONTENTS+" change",
                schedulerUtils.isAReportAlreadySchedule(ReportEnum.STATISTICS_FULL, OUTSTATION), is(true));
    }
    
    private Map<String, String> newConfigurationMapWithNeededForTesting() {
        Map<String, String> map = new HashMap();
        map.put(ConfigurationManager.FREE_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(2));
        map.put(ConfigurationManager.MODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(4));
        map.put(ConfigurationManager.SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(6));
        map.put(ConfigurationManager.VERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, String.valueOf(8));
        map.put(ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS, String.valueOf(100));
        map.put(ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, String.valueOf(150));
        map.put(ConfigurationManager.BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS, String.valueOf(1000));
        map.put(ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN, ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_VERY_SLOW_STRING);
        map.put(ConfigurationManager.QUEUE_DETECT_THREHOLD, "5");
        map.put(ConfigurationManager.QUEUE_CLEARANCE_THREHOLD, "3");
        map.put(ConfigurationManager.QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS, String.valueOf(1));
        map.put(ConfigurationManager.EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS, "10");
        map.put(ConfigurationManager.MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD, "3");
        map.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES, "1");
        map.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES, "2");
        map.put(ConfigurationManager.STATISTICS_REPORTS_CONTENTS, ConfigurationManager.STATISTICS_REPORTS_CONTENTS_VALUE_BRIEF);
        map.put(ConfigurationManager.STATISTICS_REPORT_PERIOD_IN_SECONDS, "120");
        map.put(ConfigurationManager.CONGESTION_REPORT_PERIOD_IN_SECONDS, "120");
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
