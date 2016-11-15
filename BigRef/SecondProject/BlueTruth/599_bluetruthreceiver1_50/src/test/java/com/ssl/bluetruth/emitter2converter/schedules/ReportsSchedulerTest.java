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
 * Created on 01-May-2015 05:13 PM
 */
package com.ssl.bluetruth.emitter2converter.schedules;

import com.ssl.bluetruth.emitter2converter.schedules.SchedulerUtils;
import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;
import junit.framework.Assert;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.ssl.bluetruth.emitter2converter.exceptions.UnableToScheduleException;
import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.utilsfortesting.ConfigurationManagerFake;
import ssl.bluetruth.emitter2converter.utilsfortesting.LogUtils;

/**
 *
 * @author josetrujillo-brenes
 */
public class ReportsSchedulerTest {
    
    private final Logger logger = LogManager.getLogger(getClass());
    
    public ReportsSchedulerTest() {
        LogUtils.showLogsInConsoleOf("ssl", Level.DEBUG);
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        newConfigurationManagerWithNeededForReportsScheduler();
    }
    
    @After
    public void tearDown() {
        SchedulerUtils reportsScheduler = new SchedulerUtils();
        reportsScheduler.logAllSchedules();
        reportsScheduler.deleteAllSchedules();
        deleteConfigurationManager();
    }
    
    @Test
    public void scheduleAllReports_shouldCreateAllSchedulesForOneReport() throws UnableToScheduleException {
        RawDeviceData rawDeviceData = new RawDeviceData();
        SchedulerUtils reportsScheduler = new SchedulerUtils();
        String idOutStation = "0001";         
        reportsScheduler.scheduleReports(ReportEnum.STATISTICS_BRIEF, idOutStation, rawDeviceData);
        Assert.assertTrue(reportsScheduler.isAReportAlreadySchedule(ReportEnum.STATISTICS_BRIEF, idOutStation));
    }
    
    @Test
    public void deleteReport_afterDeleting_shouldntFindIt() throws UnableToScheduleException {
        RawDeviceData rawDeviceData = new RawDeviceData();
        SchedulerUtils reportsScheduler = new SchedulerUtils();
        String idOutStation = "0001"; 
        reportsScheduler.scheduleReports(ReportEnum.STATISTICS_BRIEF, idOutStation, rawDeviceData);
        reportsScheduler.delete(ReportEnum.STATISTICS_BRIEF, idOutStation);
        Assert.assertFalse(reportsScheduler.isAReportAlreadySchedule(ReportEnum.STATISTICS_BRIEF, idOutStation));        
    }

    private void newConfigurationManagerWithNeededForReportsScheduler() {
        ConfigurationManagerFake configurationManagerFake = new ConfigurationManagerFake();
        ThreadLocalConfigurationManager.set(configurationManagerFake);
    }
    
    private static void deleteConfigurationManager() {
        ThreadLocalConfigurationManager.remove();
    }
    
}
