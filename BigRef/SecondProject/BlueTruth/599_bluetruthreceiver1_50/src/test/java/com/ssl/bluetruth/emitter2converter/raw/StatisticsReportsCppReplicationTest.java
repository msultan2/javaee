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
import com.ssl.bluetruth.emitter2converter.raw.DeviceDetection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.ssl.bluetruth.emitter2converter.exceptions.DeadOutStationWithNothingElseToReportException;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.schedules.SchedulerUtils;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.ConfigurationManagerFake;
import ssl.bluetruth.emitter2converter.utilsfortesting.LogUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.PowerMockTimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.TimeUtilsFake;
import static com.ssl.bluetruth.emitter2converter.utilsfortesting.AssertUtils.*;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.*;
import static org.junit.Assert.assertEquals;

/**
 * This test attempts to replicate Cpp tests
 * @author josetrujillo-brenes
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { TimeUtils.class })
public class StatisticsReportsCppReplicationTest {
    
    private final Logger logger = Logger.getLogger(getClass()); 
    private final static String OUTSTATION = "7778";
    private final static String DEVICE = "123456789012"; 
    
    public StatisticsReportsCppReplicationTest() {
        LogUtils.showAllLogsInConsole();
    }
    
    @Before
    public void setUp() throws Exception {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
    }
    
    @After
    public void tearDown() {
        new SchedulerUtils().deleteAllSchedules();
        ConfigurationManagerFake.delete();
    }
    
    @Test
    public void preparingForCppTestReplication() {
        assertEquals("2000", TimeUtils.secondsInHexOfAdding("1000","1000"));
        assertEquals("1000", TimeUtils.secondsInHexOfAdding("1000","0"));
        assertEquals("CC43", TimeUtils.secondsInHexOfSubtracting(TimeUtils.secondsInHexOfAdding("3456","CC43"), "3456"));
        assertEquals("6621", TimeUtils.halfSecondsInHexOfSubtracting(TimeUtils.secondsInHexOfAdding("3456","CC43"), "3456"));
    }
    
    /**
     * Test prepare_statistics_report in
     * 577-3_lowpowerbluetruth_application_module\test\src\task_send_statistics_report_test.cpp
     * @throws DeadOutStationWithNothingElseToReportException
     * @throws InvalidConfigurationException 
     */    
    @Test
    public void getReportOfOutStation_STATISTICS_ReplicationOfCppTest_prepare_statistics_report() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        newConfigurationManagerWithNeededForCppStatisticsReportsReplicationTest(); 
        RawDeviceData rawDeviceData = new RawDeviceData();
        {   
            String testId = "TestWithoutName (No Devices)";
            assertStatisticsBriefReport(testId, rawDeviceData, OUTSTATION, "0,0");
            TimeUtilsFake.add("10000");
            assertStatisticsBriefReport(testId, rawDeviceData, OUTSTATION ,"0,10000");            
        }
        {   
            String testId = "TestWithoutName (One Device)";            
            String secondsFirstSeenInHex = "3456";            
            String secondsLastSeenInHex = TimeUtils.secondsInHexOfAdding(secondsFirstSeenInHex,"CC43"); 
            addDeviceDetectionStub(rawDeviceData, OUTSTATION, DEVICE, newDeviceDetectionStub(secondsFirstSeenInHex, secondsLastSeenInHex, ABSENT, NOT_BACKGROUND, NOT_BACKGROUND_AND_ABSENT));
            TimeUtilsFake.add("100");
            assertStatisticsBriefReport(testId, rawDeviceData, OUTSTATION, "10000,100,123456789012:0:3456:6621:CC43");             
        }
        {  
            String testId = "NormalDevicex1";            
            String secondsFirstSeenInHex = "10000000";
            String secondsLastSeenInHex = "10200000";
            addDeviceDetectionStub(rawDeviceData, OUTSTATION, DEVICE, newDeviceDetectionStub(secondsFirstSeenInHex, secondsLastSeenInHex, ABSENT, NOT_BACKGROUND, NOT_BACKGROUND_AND_ABSENT));
            TimeUtilsFake.add("100");    
            assertStatisticsBriefReport(testId, rawDeviceData, OUTSTATION, "10100,100,123456789012:0:10000000:100000:200000");
        }
        {   // Can't replicate the exact same test because the Java implementation doesn't allow to add the same device twice. In this test the id of the device is different
            String testId = "NormalDevicex10";
            String secondsFirstSeenInHex = "2710";
            String secondsLastSeenInHex = TimeUtils.secondsInHexOfAdding(secondsFirstSeenInHex,"C8");  
            for(int i = 0; i<10; i++) {
                addDeviceDetectionStub(rawDeviceData, OUTSTATION, DEVICE+i, newDeviceDetectionStub(secondsFirstSeenInHex, secondsLastSeenInHex, ABSENT, NOT_BACKGROUND, NOT_BACKGROUND_AND_ABSENT));
            } 
            TimeUtilsFake.add("100");
            assertStatisticsBriefReport(testId, rawDeviceData, OUTSTATION, "10200,100,"+"1234567890120:0:2710:64:C8,"+"1234567890121:0:2710:64:C8,"+"1234567890122:0:2710:64:C8,"+"1234567890123:0:2710:64:C8,"
                            +"1234567890124:0:2710:64:C8,"+"1234567890125:0:2710:64:C8,"+"1234567890126:0:2710:64:C8,"+"1234567890127:0:2710:64:C8,"+"1234567890128:0:2710:64:C8,"+"1234567890129:0:2710:64:C8");
        }
        {   
            String testId = "BackgroudDevice";
            String secondsFirstSeenInHex = "2710";
            String secondsLastSeenInHex = "2711"; 
            addDeviceDetectionStub(rawDeviceData, OUTSTATION, DEVICE, newDeviceDetectionStub(secondsFirstSeenInHex, secondsLastSeenInHex, NOT_ABSENT, BACKGROUND, NOT_BACKGROUND_AND_ABSENT));
            TimeUtilsFake.add("100");
            assertStatisticsBriefReport(testId, rawDeviceData, OUTSTATION, "10300,100,123456789012:0:2710:0:FFFFFFFE");
        }
        {   // Here is a difference between the results we return. The InStation doesn't report a faulty device until it's absent or background 
            setConfigurationManagerForAllDevicesToBeFaulty();
            
            String testId = "faulty device (present)"; 
            String secondsFirstSeenInHex = "2710";
            String secondsLastSeenInHex = "2711"; 
            addDeviceDetectionStub(rawDeviceData, OUTSTATION, DEVICE, newDeviceDetectionStub(secondsFirstSeenInHex, secondsLastSeenInHex, NOT_ABSENT, NOT_BACKGROUND, NOT_BACKGROUND_AND_ABSENT));
            TimeUtilsFake.add("100");
            assertStatisticsBriefReport(testId, rawDeviceData, OUTSTATION, "10400,100");
            
            reviveOutStation(rawDeviceData, OUTSTATION);
            
            testId = "faulty device (absent)"; 
            secondsFirstSeenInHex = "2710";
            secondsLastSeenInHex = "2711"; 
            addDeviceDetectionStub(rawDeviceData, OUTSTATION, DEVICE, newDeviceDetectionStub(secondsFirstSeenInHex, secondsLastSeenInHex, ABSENT, NOT_BACKGROUND, NOT_BACKGROUND_AND_ABSENT));
            TimeUtilsFake.add("100");
            assertStatisticsBriefReport(testId, rawDeviceData, OUTSTATION, "10500,100,123456789012:0:2710:0:FFFFFFFF");
            
            reviveOutStation(rawDeviceData, OUTSTATION);
            
            testId = "faulty device (background)";
            secondsFirstSeenInHex = "2710";
            secondsLastSeenInHex = "2711"; 
            addDeviceDetectionStub(rawDeviceData, OUTSTATION, DEVICE, newDeviceDetectionStub(secondsFirstSeenInHex, secondsLastSeenInHex, NOT_ABSENT, BACKGROUND, NOT_BACKGROUND_AND_ABSENT));
            TimeUtilsFake.add("100");            
            assertStatisticsBriefReport(testId, rawDeviceData, OUTSTATION, "10600,100,123456789012:0:2710:0:FFFFFFFF");
        }  
    }
    
    /**
     * Continuation of test prepare_statistics_report in
     * 577-3_lowpowerbluetruth_application_module\test\src\task_send_statistics_report_test.cpp
     * @throws DeadOutStationWithNothingElseToReportException
     * @throws InvalidConfigurationException 
     */  
    @Test 
    public void getReportOfOutStation_STATISTICS_ReplicationOfCppTest_SubsequentReports() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {    
        newConfigurationManagerWithNeededForCppStatisticsReportsReplicationTest();
        RawDeviceData rawDeviceData = new RawDeviceData(); 
        assertStatisticsBriefReport("", rawDeviceData, OUTSTATION, "0,0");
        TimeUtilsFake.add("1000");
        String secondsFirstSeenInHex = "2710";
        String secondsLastSeenInHex = "2710"; 
        addDeviceDetectionStub(rawDeviceData, OUTSTATION, DEVICE, newDeviceDetectionStub(secondsFirstSeenInHex, secondsLastSeenInHex, ABSENT, NOT_BACKGROUND, NOT_BACKGROUND_AND_ABSENT));
        assertStatisticsBriefReport("First SubsequentReport", rawDeviceData, OUTSTATION, "0,1000,123456789012:0:2710:0:1");
        TimeUtilsFake.add("2000");
        addDeviceDetectionStub(rawDeviceData, OUTSTATION, DEVICE, newDeviceDetectionStub(secondsFirstSeenInHex, secondsLastSeenInHex, ABSENT, NOT_BACKGROUND, NOT_BACKGROUND_AND_ABSENT));
        assertStatisticsBriefReport("Second SubsequentReport", rawDeviceData, OUTSTATION, "1000,2000,123456789012:0:2710:0:1");
        TimeUtilsFake.add("2000");
        assertStatisticsBriefReport("Third SubsequentReport", rawDeviceData, OUTSTATION, "3000,2000");        
    }

    private void reviveOutStation(RawDeviceData rawDeviceData, String idOutStation) throws InvalidConfigurationException {
        rawDeviceData.addDeviceDetections(idOutStation, new ArrayList(), TimeUtils.currentTimestamp());
    }
    
    private ConfigurationManagerFake newConfigurationManagerWithNeededForCppStatisticsReportsReplicationTest() {
        Map<String, String> map = new HashMap();
        map.put(MODE, "3");
        map.put(EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS, "10");
        map.put(MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD, "999999999");
        map.put(FREE_FLOW_BIN_THRESHOLD_IN_SECONDS, "10");
        map.put(MODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, "20");
        map.put(SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, "30");
        map.put(VERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, "40");
        map.put(ABSENCE_THREHOLD_IN_SECONDS, "15");
        map.put(QUEUE_ALERT_THREHOLD_BIN, QUEUE_ALERT_THREHOLD_BIN_VALUE_FREE_INT);
        map.put(QUEUE_DETECT_THREHOLD, "4");
        map.put(QUEUE_CLEARANCE_THREHOLD, "2");
        map.put(STATISTICS_REPORT_PERIOD_IN_SECONDS, "1000");
        map.put(CONGESTION_REPORT_PERIOD_IN_SECONDS, "1000");        
        map.put(BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, "1000");
        map.put(BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS, "1000");
        map.put(STATISTICS_REPORTS_CONTENTS, STATISTICS_REPORTS_CONTENTS_VALUE_BRIEF);
        ConfigurationManagerFake configurationManagerFake = new ConfigurationManagerFake(map);
        ThreadLocalConfigurationManager.set(configurationManagerFake);
        return configurationManagerFake;
    }    
    
    private void setConfigurationManagerForAllDevicesToBeFaulty() {
        ConfigurationManagerFake configurationManagerFake = (ConfigurationManagerFake) ThreadLocalConfigurationManager.get();
        configurationManagerFake.set(MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD, "0");
    }
    
    private void addDeviceDetectionStub(RawDeviceData rawDeviceData, String idOutStation, String deviceMacAdress, DeviceDetection deviceDetectionStub) {
        rawDeviceData.getRawDeviceDataOfOutStation(idOutStation,TimeUtils.currentTimestamp()).setDeviceDetection(deviceMacAdress, deviceDetectionStub);
    }
    
    private static final Boolean ABSENT = true;
    private static final Boolean NOT_ABSENT = false;
    private static final Boolean BACKGROUND = true;
    private static final Boolean NOT_BACKGROUND = false;
    private static final Boolean BACKGROUND_AND_ABSENT = true;
    private static final Boolean NOT_BACKGROUND_AND_ABSENT = false;
    private DeviceDetection newDeviceDetectionStub(String secondsFirstSeenInHex, String secondsLastSeenInHex, Boolean isAbsent, Boolean isBackground, Boolean isBackgroundAndAbsentForTooMuch) throws InvalidConfigurationException {
        DeviceDetection deviceDetection = EasyMock.createNiceMock(DeviceDetection.class);
        EasyMock.expect(deviceDetection.isAbsent()).andStubReturn(isAbsent);
        EasyMock.expect(deviceDetection.isAbsentAssumingItsNotBackground()).andStubReturn(isAbsent);
        EasyMock.expect(deviceDetection.isBackground()).andStubReturn(isBackground);        
        EasyMock.expect(deviceDetection.isBackgroundAndAbsentForTooMuch()).andStubReturn(isBackgroundAndAbsentForTooMuch);
        EasyMock.expect(deviceDetection.getSecondsFirstSeenInHex()).andStubReturn(secondsFirstSeenInHex);
        EasyMock.expect(deviceDetection.getHalfSecondsSeenInHex()).andStubReturn(TimeUtils.halfSecondsInHexOfSubtracting(secondsLastSeenInHex, secondsFirstSeenInHex)); 
        String valueForgetSecondsSeenInHexForStatisticsReport;
        String secondsSeenInHex = TimeUtils.secondsInHexOfSubtracting(secondsLastSeenInHex, secondsFirstSeenInHex);
        if("0".equalsIgnoreCase(secondsSeenInHex)) {
            valueForgetSecondsSeenInHexForStatisticsReport = "1";
        } else {
            valueForgetSecondsSeenInHexForStatisticsReport = secondsSeenInHex;
        }
        EasyMock.expect(deviceDetection.getSecondsSeenInHex()).andStubReturn(valueForgetSecondsSeenInHexForStatisticsReport);
        EasyMock.replay(deviceDetection);
        logger.info("newDeviceDetectionStub where"
                +" getSecondsFirstSeenInHex()="+deviceDetection.getSecondsFirstSeenInHex()
                +" getHalfSecondsSeenInHexForStatisticsReport()="+deviceDetection.getHalfSecondsSeenInHex()
                +" getSecondsSeenInHexForStatisticsReport()="+deviceDetection.getSecondsSeenInHex()
                +" isAbsent()="+deviceDetection.isAbsent()
                +" isBackground()="+deviceDetection.isBackground()
                +" isBackgroundAndAbsentForTooMuch()="+deviceDetection.isBackgroundAndAbsentForTooMuch()
                +" isAlreadyReportedAsAbsentOrBackground()="+deviceDetection.isAlreadyReportedAsAbsentOrBackground());
        return deviceDetection;
    }   


}
