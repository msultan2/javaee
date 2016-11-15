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

package ssl.bluetruth.emitter2converter.configuration;

import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;

/**
 * Interface to obtain the configuration of a particular OutStation or a default one for every OutStation.
 * Contains public static final Strings for the property names and values when it's appropriate.
 * @author jtrujillo-brenes
 */
public interface ConfigurationManager {
    
    public static final String CONFIGURATION_MANAGER_IN_SERVLETCONTEXT = "configurationManager";
     
    public static final String MODE = "outStationMode";
    public static final int MODE_0 = 0;
    public static final int MODE_1 = 1;
    public static final int MODE_2 = 2;
    public static final int MODE_3 = 3;
    public static final String SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES = "settingsCollectionInterval1";
    public static final String SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES = "settingsCollectionInterval2";
    public static final String SETTINGS_COLLECTION_LAST_UPDATE_IN_SECONDS = "settingsCollectionLastUpdateInSeconds";
    
    public static final String URL_CONGESTION_REPORTS = "urlCongestionReports"; 
    public static final String URL_STATISTICS_REPORTS = "urlStatisticsReports"; 
    public static final String URL_STATUS_REPORTS = "urlStatusReports"; 
    public static final String URL_FAULT_REPORTS = "urlFaultReports"; 
    
    public static final String STATISTICS_REPORTS_CONTENTS = "statisticsReportContents"; 
    public static final String STATISTICS_REPORTS_CONTENTS_VALUE_BRIEF = "brief"; 
    public static final String STATISTICS_REPORTS_CONTENTS_VALUE_FULL = "full"; 
    
    public static final String STATISTICS_REPORT_PERIOD_IN_SECONDS = "statisticsReportPeriodInSeconds";     
    public static final String CONGESTION_REPORT_PERIOD_IN_SECONDS = "congestionReportPeriodInSeconds"; 
    public static final String CONGESTION_REPORT_DELAY_WHEN_INSTATION_RECEIVES_ONE_IN_SECONDS = "congestionReportDelayInSeconds"; 
    public static final String STATUS_REPORT_PERIOD_IN_SECONDS = "statusReportPeriodInSeconds"; 
    public static final String EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS = "inquiryCycleDurationInSeconds";
    public static final String MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD = "missingDeviceDetectionsToConsiderDead";
    
    public static final String FREE_FLOW_BIN_THRESHOLD_IN_SECONDS = "freeFlowBinThresholdInSeconds";     
    public static final String MODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS = "moderateFlowBinThresholdInSeconds";     
    public static final String SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS = "slowFlowBinThresholdInSeconds";     
    public static final String VERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS = "verySlowFlowBinThresholdInSeconds";
    
    public static final String ABSENCE_THREHOLD_IN_SECONDS = "absenceThresholdInSeconds"; 
    public static final String BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS = "backgroundLatchTimeThresholdInSeconds"; 
    public static final String BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS = "backgroundClearanceTimeThresholdInSeconds";
    
    public static final String QUEUE_ALERT_THREHOLD_BIN = "queueAlertThresholdBin"; 
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_FREE_STRING = "freeFlow"; 
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_FREE_INT = "1"; 
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_MODERATE_STRING = "moderateFlow"; 
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_MODERATE_INT = "2"; 
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_SLOW_STRING = "slowFlow";
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_SLOW_INT = "3";    
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_VERY_SLOW_STRING = "verySlowFlow"; 
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_VERY_SLOW_INT = "4"; 
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_STATIC_STRING = "staticFlow";
    public static final String QUEUE_ALERT_THREHOLD_BIN_VALUE_STATIC_INT = "5";
    
    public static final String QUEUE_DETECT_THREHOLD = "queueDetectThreshold"; 
    public static final String QUEUE_CLEARANCE_THREHOLD = "queueClearanceThreshold"; 
    public static final String QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS = "queueDetectionStartupIntervalInSeconds";
    public static final String TIMESTAMP_TOLERANCE_MS = "timestampToleranceInMs";
    
    public String get(String idOutStation, String propertyName) throws InvalidConfigurationException;
    public int getInt(String idOutStation, String propertyName) throws InvalidConfigurationException;
    
    public void addConfigurationManagerListener(ConfigurationManagerListener listener);
    
    
}
