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
 * Created on 13th April 2015
 */

package com.ssl.bluetruth.emitter2converter.raw;

import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
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
import com.ssl.bluetruth.emitter2converter.utils.CongestionReport;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.ConfigurationManagerFake;
import ssl.bluetruth.emitter2converter.utilsfortesting.LogUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.PowerMockTimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.TimeUtilsFake;
import static com.ssl.bluetruth.emitter2converter.utilsfortesting.AssertUtils.*;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.*;

/**
 * This test attempts to replicate Cpp tests
 * @author josetrujillo-brenes
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { TimeUtils.class })
public class CongestionReportsCppReplicationTest {
    
    private final Logger logger = Logger.getLogger(getClass());
    private final String OUTSTATION = "OutStation";
    private final String DEVICE1 = "DEVICE1";
    private final String DEVICE2 = "DEVICE2";
    private final String DEVICE3 = "DEVICE3";
    private final String DEVICE4 = "DEVICE4";
    private final String DEVICE5 = "DEVICE5";
    
    public CongestionReportsCppReplicationTest() {
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
    
    /**
     * test updateDevicesFromRawTimeFreeFlowWithStartupInterval in
     * 570_highpowerbluetruth_v4\test\src\model\common\queuedetector_test.cpp
     * @throws DeadOutStationWithNothingElseToReportException
     * @throws InvalidConfigurationException 
     */
    @Test
    public void getReportOfOutStationCONGESTION_ReplicationOfCppTest_updateDevicesFromRawTimeFreeFlowWithStartupInterval() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException { 
        ConfigurationManagerFake configurationManagerFake = newConfigurationManagerWithNeededForCppCongestionReportsReplicationTest(); 
        configurationManagerFake.set(QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS, "0");
        RawDeviceData rawDeviceData = new RawDeviceData();
        {
            String assertMesage = "Report empty collection. Check that nothing changes";
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);
        }
        {
            String assertMesage = "Report the first device. Only one bin should be affected";
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1), TimeUtils.currentTimestamp());
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1), TimeUtils.currentTimestamp());            
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "1:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);
        }
        { 
            String assertMesage = "Report other devices multiple times and check how this update propagates through the bins";
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());            
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "3:1:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:3:1:0:0", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:3:1:0", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:3:1", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:4", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
        }
        { 
            String assertMesage = "Now one should go into UNDEFINED bin (above STATIC bin)";
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:4", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
        }
        { 
            String assertMesage = "Report empty collection and see how the queue clearance is detected";
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, new ArrayList(), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:4", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, new ArrayList(), TimeUtils.currentTimestamp());            
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);
        }
        //Report other devices multiple times and check how this affects the report. (This test is no replicable )
    }
    
    /**
     * test updateDevicesFromRawTimeFreeFlowWithStartupIntervalAndBlueToothDeviceFault in
     * 570_highpowerbluetruth_v4\test\src\model\common\queuedetector_test.cpp
     * @throws DeadOutStationWithNothingElseToReportException
     * @throws InvalidConfigurationException 
     */
    @Test
    public void getReportOfOutStationCONGESTION_ReplicationOfCppTest_updateDevicesFromRawTimeFreeFlowWithStartupIntervalAndBlueToothDeviceFault() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        ConfigurationManagerFake configurationManagerFake = newConfigurationManagerWithNeededForCppCongestionReportsReplicationTest(); 
        configurationManagerFake.set(QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS, "30");
        RawDeviceData rawDeviceData = new RawDeviceData();
         { 
            String assertMesage = "Report all devices multiple times and check how this update propagates through the bins";
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "4:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NOT_READY);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:4:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NOT_READY);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:4:0:0", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:4:0", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:4", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
         }
         configurationManagerFake.set(EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS, "0");
         {
            String assertMesage = "Report all devices multiple times and check that if device is faulty no queue is reported";
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:4", CongestionReport.QUEUE_PRESENT_VALUE_FAULTY);
         }
         configurationManagerFake.set(EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS, "10");
         { 
            String assertMesage = "Report all devices multiple times and check how this update propagates through the bins";
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:4", CongestionReport.QUEUE_PRESENT_VALUE_NOT_READY);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:4", CongestionReport.QUEUE_PRESENT_VALUE_NOT_READY);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:4", CongestionReport.QUEUE_PRESENT_VALUE_NOT_READY);
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:0:0:0:4", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
         }
    }
    
    /**
     * The replication wasn't possible for test queue_detection__process in
     * 577-3_lowpowerbluetruth_application_module\test\src\task_send_congestion_report_test.cpp
     * But here is a similar one that could be useful
     * @throws DeadOutStationWithNothingElseToReportException
     * @throws InvalidConfigurationException 
     */
    @Test
    public void getReportOfOutStationCONGESTION_ReplicationOfCppTest_queue_detection__process() throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        ConfigurationManagerFake configurationManagerFake = newConfigurationManagerWithNeededForCppCongestionReportsReplicationTest(); 
        configurationManagerFake.set(QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS, "0");
        configurationManagerFake.set(QUEUE_DETECT_THREHOLD, "6");
        configurationManagerFake.set(QUEUE_CLEARANCE_THREHOLD, "6");
        RawDeviceData rawDeviceData = new RawDeviceData();
        { 
            String assertMesage = "5 devices in free flow bin";
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4, DEVICE5), TimeUtils.currentTimestamp());
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4, DEVICE5), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "5:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);
        }
        { 
            String assertMesage = "The same but with threshold changed above queue detection threshold";
            configurationManagerFake.set(QUEUE_DETECT_THREHOLD, "5");
            configurationManagerFake.set(QUEUE_CLEARANCE_THREHOLD, "5");
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "5:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
        }
        {
            String assertMesage = "The same but with threshold changed again below queue detection threshold";
            configurationManagerFake.set(QUEUE_DETECT_THREHOLD, "6");
            configurationManagerFake.set(QUEUE_CLEARANCE_THREHOLD, "6");
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "5:0:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);
        }
        {
            String assertMesage = "5 devices in moderate bin with Thresholds set below queue detection level";
            configurationManagerFake.set(QUEUE_DETECT_THREHOLD, "5");
            configurationManagerFake.set(QUEUE_CLEARANCE_THREHOLD, "5");
            configurationManagerFake.set(QUEUE_ALERT_THREHOLD_BIN, QUEUE_ALERT_THREHOLD_BIN_VALUE_MODERATE_STRING);            
            TimeUtilsFake.add(10);
            rawDeviceData.addDeviceDetections(OUTSTATION, Arrays.asList(DEVICE1, DEVICE2, DEVICE3, DEVICE4, DEVICE5), TimeUtils.currentTimestamp());
            assertCongestionReport(assertMesage, rawDeviceData, OUTSTATION, "0:5:0:0:0", CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
        }
    }

    private ConfigurationManagerFake newConfigurationManagerWithNeededForCppCongestionReportsReplicationTest() {
        Map<String, String> map = new HashMap();
        map.put(MODE, "3");
        map.put(EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS, "10");
        map.put(MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD, "2");
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
}
