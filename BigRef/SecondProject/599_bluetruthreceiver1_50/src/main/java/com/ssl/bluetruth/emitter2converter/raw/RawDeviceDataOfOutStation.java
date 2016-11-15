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

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import com.ssl.bluetruth.emitter2converter.utils.CongestionReport;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;

/**
 * Class which contains all the raw device data of each OutStation All the
 * device detection are store in a ConcurrentHashMap to avoid synchronization
 * problems. The key of the map is the device id which is the mac address.
 *
 * @author josetrujillo-brenes
 */
public class RawDeviceDataOfOutStation {

    private final Logger logger = Logger.getLogger(getClass());

    private final String idOutStation;
    private final Map<String, DeviceDetection> mapDevices;
    private final RawDeviceDataOfOutStationCleaner cleaner;
    private Timestamp instationsTimestampLastStatisticsReport;
    private Timestamp timestampLastDeviceDetection;
    private Timestamp timestampCreatedOrResurrected;
    private final ConfigurationManager configurationManager;
    private boolean dead;
    private boolean queuePresent;

    public RawDeviceDataOfOutStation(String idOutStation, Timestamp startTime) {
        this.idOutStation = idOutStation;
        configurationManager = ThreadLocalConfigurationManager.get();
        timestampCreatedOrResurrected = startTime;
        instationsTimestampLastStatisticsReport = TimeUtils.currentTimestamp();
        mapDevices = new ConcurrentHashMap<>();
        dead = false;
        queuePresent = false;
        cleaner = new RawDeviceDataOfOutStationCleaner(idOutStation, mapDevices, configurationManager);
    }

    /**
     * Marks the OutStation as alive and adds all the device detections
     * @param deviceIds
     * @param timeSeen
     */
    public void addDeviceDetections(List<String> deviceIds, Timestamp timeSeen) {
        if ((timestampLastDeviceDetection == null)
                || ((timestampLastDeviceDetection != null) && (timestampLastDeviceDetection.before(timeSeen)))) {
            timestampLastDeviceDetection = timeSeen;
        }
        if (dead) {
            dead = false;
            queuePresent = false;
            timestampCreatedOrResurrected = timeSeen;
        }
        for (String macAddress : deviceIds) {
            addDeviceDetection(macAddress, timeSeen);
        }
    }

    /**
     * Adds one device detection, updating with setTimeLastSeen or creating a
     * new instance if it wasn't found in the map
     *
     * @param macAddress
     * @param timeSeen
     */
    private void addDeviceDetection(String macAddress, Timestamp timeSeen) {
        String methodLog = null;
        if (logger.isDebugEnabled() || logger.isInfoEnabled()) {
            methodLog = "addDeviceDetection for device '" + macAddress + "' from OutStation '" + idOutStation + "' at " + timeSeen + " - ";
        }
        if (mapDevices.containsKey(macAddress)) {
            mapDevices.get(macAddress).setTimeLastSeen(timeSeen);
            if (logger.isInfoEnabled()) {
                logger.info(methodLog + "Already stored. Time last seen updated succefully with value '" + TimeUtils.secondsOf(timeSeen) + "'");
            }
        } else {
            mapDevices.put(macAddress, new DeviceDetection(idOutStation, timeSeen));
            if (logger.isInfoEnabled()) {
                logger.info(methodLog + "Stored successfully with time first seen value '" + TimeUtils.secondsOf(timeSeen) + "'");
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("After " + methodLog + ". The Map for this Detector has the keys: " + mapDevices.keySet());
        }
    }

    /**
     * If the OutStation is dead, it will return false immediately, but if it's
     * alive, it will check if it still alive. A OutStation is dead if the
     * InStation doesn't receive a device detection in
     * EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS *
     * MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD
     *
     * @return
     * @throws InvalidConfigurationException
     */
    public boolean isAlive() throws InvalidConfigurationException {
        String methodLog = null;
        if (logger.isDebugEnabled()) {
            methodLog = "Is OutStation " + idOutStation + " Alive? - ";
        }
        if (dead) {
            if (logger.isDebugEnabled()) {
                logger.debug(methodLog + " It was already dead");
            }
            return false;
        } else {
            Timestamp timestampLastHeartbeat = timestampLastDeviceDetection;
            if (timestampLastHeartbeat == null) {
                timestampLastHeartbeat = timestampCreatedOrResurrected;
            }
            long secondsSinceLastDeviceDetection = TimeUtils.secondsSince(timestampLastHeartbeat);
            int expectedDeviceDetectionPeriodInSeconds = configurationManager.getInt(idOutStation, ConfigurationManager.EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS);
            int missingDeviceDetectionsToConsiderDead = configurationManager.getInt(idOutStation, ConfigurationManager.MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD);
            int secondsWithoutDeviceDetectionsToConsiderDead = expectedDeviceDetectionPeriodInSeconds * missingDeviceDetectionsToConsiderDead;
            if (secondsSinceLastDeviceDetection >= secondsWithoutDeviceDetectionsToConsiderDead) {
                if (logger.isDebugEnabled()) {
                    logger.debug(methodLog + " No because we haven't received a device detection in " + secondsSinceLastDeviceDetection + " seconds, we expect them each " + expectedDeviceDetectionPeriodInSeconds + " seconds and we allow " + missingDeviceDetectionsToConsiderDead + " misses.");
                }
                dead = true;
                return !dead;
            }
            if (logger.isDebugEnabled()) {
                logger.debug(methodLog + " Yes because we received a device detection " + secondsSinceLastDeviceDetection + " seconds ago, we expect them each " + expectedDeviceDetectionPeriodInSeconds + " seconds and we allow " + missingDeviceDetectionsToConsiderDead + " misses.");
            }
            return !dead;
        }
    }

    /**
     * @return True if there is at least one device which is not background
     * @throws InvalidConfigurationException
     */
    public boolean hasUnreportedDevices() throws InvalidConfigurationException {
        String methodLog = null;
        if (logger.isDebugEnabled()) {
            methodLog = "Has OutStation " + idOutStation + " devices which are not background? - ";
        }
        for (Map.Entry<String, DeviceDetection> entry : mapDevices.entrySet()) {
            String device = entry.getKey();
            DeviceDetection deviceDetection = entry.getValue();
            if (!deviceDetection.isBackground() || !deviceDetection.isAlreadyReportedAsAbsentOrBackground()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(methodLog + " At least one");
                }
                return true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(methodLog + " No");
        }
        return false;
    }

    public void setTimestampLastStatisticsReport(Timestamp timestampLastStatisticsReport) {
        this.instationsTimestampLastStatisticsReport = timestampLastStatisticsReport;
    }

    public String getSecondsLastStatisticsReportInHex() {
        return TimeUtils.secondsInHexOf(instationsTimestampLastStatisticsReport);
    }

    public String getSecondsInHexBetweenLastStatisticsReportAnd(Timestamp timestamp) {
        return TimeUtils.secondsInHexOfSubtracting(timestamp, instationsTimestampLastStatisticsReport);
    }

    public void setDeviceDetection(String macAddress, DeviceDetection deviceDetection) {
        mapDevices.put(macAddress, deviceDetection);
    }

    /**
     * Creates a String with the devices part of a statistics report
     *
     * @param report Indicates if the report is brief or full
     * @param reportTime Used to set the time of the last statistics report
     * @return
     * @throws InvalidConfigurationException
     */
    public String getDevicesPartOfStatisticsReport(ReportEnum report, Timestamp reportTime) throws InvalidConfigurationException {
        String methodLog = null;
        if (logger.isDebugEnabled()) {
            methodLog = report.getName() + " of OutStation '" + idOutStation + "' in " + reportTime + " - ";
        }
        StringBuilder sb = new StringBuilder();
        Iterator it = mapDevices.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DeviceDetection> entry = (Entry) it.next();
            String macAddress = entry.getKey();
            DeviceDetection deviceDetection = entry.getValue();
            String methodLogForEachDevice = null;
            if (logger.isDebugEnabled()) {
                methodLogForEachDevice = methodLog + "Device '" + macAddress + "' ";
            }
            if (deviceDetection.isAlreadyReportedAsAbsentOrBackground()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(methodLogForEachDevice + "was already reported as absent or background");
                }
            } else if (deviceDetection.isBackground()) {
                deviceDetection.setAlreadyReportedAsAbsentOrBackground();
                if (!isAlive()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(methodLogForEachDevice + "is background but the OutStation is faulty");
                    }
                    sb = appendFaulty(sb, macAddress, deviceDetection);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(methodLogForEachDevice + "is background");
                    }
                    sb = appendBackground(sb, macAddress, deviceDetection);
                }
            } else if (deviceDetection.isAbsentAssumingItsNotBackground()) {
                deviceDetection.setAlreadyReportedAsAbsentOrBackground();
                if (!isAlive()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(methodLogForEachDevice + "is absent but the OutStation is faulty.");
                    }

                    sb = appendFaulty(sb, macAddress, deviceDetection);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(methodLogForEachDevice + "is absent.");
                    }
                    sb = appendAbsent(sb, macAddress, deviceDetection);
                }
            } else if (report == ReportEnum.STATISTICS_FULL) {
                if (logger.isDebugEnabled()) {
                    logger.debug(methodLogForEachDevice + "is present and it would show in the report because it's a full one");
                }
                sb = appendPresent(sb, macAddress, deviceDetection);
            } else if (logger.isDebugEnabled()) {
                logger.debug(methodLogForEachDevice + "is present but it wouldn't show in the report because it's a brief one");
            }
        }
        String devicesPartOfStatisticsReport = sb.toString();
        return devicesPartOfStatisticsReport;
    }

    /**
     * Method that helps getDevicesPartOfStatisticsReport adding a present
     * device
     *
     * @param sb The StringBuilder where the device is going to be appenf
     * @param macAddress The device id
     * @param deviceDetection
     * @return
     * @throws InvalidConfigurationException
     */
    private StringBuilder appendPresent(StringBuilder sb, String macAddress, DeviceDetection deviceDetection) throws InvalidConfigurationException {
        sb.append(macAddress).append(":0:").append(deviceDetection.getSecondsFirstSeenInHex()).append(":0:0,");
        return sb;
    }

    /**
     * Method that helps getDevicesPartOfStatisticsReport adding a absent device
     * @param sb The StringBuilder where the device is going to be appenf
     * @param macAddress The device id
     * @param deviceDetection
     * @return
     * @throws InvalidConfigurationException
     */
    private StringBuilder appendAbsent(StringBuilder sb, String macAddress, DeviceDetection deviceDetection) throws InvalidConfigurationException {
        sb.append(macAddress).append(":0:");
        sb.append(deviceDetection.getSecondsFirstSeenInHex()).append(":");
        sb.append(deviceDetection.getHalfSecondsSeenInHex()).append(":");
        sb.append(deviceDetection.getSecondsSeenInHex()).append(",");
        return sb;
    }

    /**
     * Method that helps getDevicesPartOfStatisticsReport adding a background
     * device
     *
     * @param sb The StringBuilder where the device is going to be appenf
     * @param macAddress The device id
     * @param deviceDetection
     * @return
     * @throws InvalidConfigurationException
     */
    private StringBuilder appendBackground(StringBuilder sb, String macAddress, DeviceDetection deviceDetection) throws InvalidConfigurationException {
        sb.append(macAddress).append(":0:").append(deviceDetection.getSecondsFirstSeenInHex()).append(":0:FFFFFFFE,");
        return sb;
    }

    /**
     * Method that helps getDevicesPartOfStatisticsReport adding a faulty device
     *
     * @param sb The StringBuilder where the device is going to be appenf
     * @param macAddress The device id
     * @param deviceDetection
     * @return
     * @throws InvalidConfigurationException
     */
    private StringBuilder appendFaulty(StringBuilder sb, String macAddress, DeviceDetection deviceDetection) throws InvalidConfigurationException {
        sb.append(macAddress).append(":0:").append(deviceDetection.getSecondsFirstSeenInHex()).append(":0:FFFFFFFF,");
        return sb;
    }

    /**
     * Sets the bins of the CongestionReport, reading all the device detections
     * and incrementing each bin
     *
     * @param congestionReport
     * @return
     * @throws InvalidConfigurationException
     */
    public CongestionReport setBinsOf(CongestionReport congestionReport) throws InvalidConfigurationException {
        for (Map.Entry<String, DeviceDetection> entry : mapDevices.entrySet()) {
            DeviceDetection deviceDetection = entry.getValue();
            if (!deviceDetection.isBackground() && !deviceDetection.isAbsent()) {
                DeviceDetection.Flow flow = deviceDetection.getBin();
                if (flow == DeviceDetection.Flow.FREE) {
                    congestionReport.increaseFreeBin();
                } else if (flow == DeviceDetection.Flow.MODERATE) {
                    congestionReport.increaseModerateBin();
                } else if (flow == DeviceDetection.Flow.SLOW) {
                    congestionReport.increaseSlowBin();
                } else if (flow == DeviceDetection.Flow.VERY_SLOW) {
                    congestionReport.increaseVerySlowBin();
                } else {
                    congestionReport.increaseStationaryBin();
                }
            }
        }
        return congestionReport;
    }

    /**
     * Sets if a queue is present in a CongestionReport depending in the device
     * is ready for a congestion report and if there is a queue
     *
     * @param congestionReport
     * @return
     * @throws InvalidConfigurationException
     */
    public CongestionReport setQueuePresentOf(CongestionReport congestionReport) throws InvalidConfigurationException {
        if (isReadyForCongestionReports()) {
            if (isAlive()) {
                if (isQueuePresent(congestionReport)) {
                    congestionReport.setQueuePresent(CongestionReport.QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
                } else {
                    congestionReport.setQueuePresent(CongestionReport.QUEUE_PRESENT_VALUE_NO_QUEUE);
                }
            } else {
                congestionReport.setQueuePresent(CongestionReport.QUEUE_PRESENT_VALUE_FAULTY);
            }
        } else {
            congestionReport.setQueuePresent(CongestionReport.QUEUE_PRESENT_VALUE_NOT_READY);
        }
        return congestionReport;
    }

    /**
     * A OutStation is ready if its been created or resurrected for more than
     * QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS
     *
     * @return
     * @throws InvalidConfigurationException
     */
    private boolean isReadyForCongestionReports() throws InvalidConfigurationException {
        int queueDetectionStartupIntervalInSeconds = configurationManager.getInt(idOutStation, ConfigurationManager.QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS);
        long secondsSinceFirstDeviceDetectionSinceAlive = TimeUtils.secondsSince(timestampCreatedOrResurrected);
        if (logger.isDebugEnabled()) {
            logger.debug("Is OutStation " + idOutStation + " ready for Congestion Reports? - secondsSinceFirstDeviceDetectionSinceAlive(" + secondsSinceFirstDeviceDetectionSinceAlive + ") >= queueDetectionStartupIntervalInSeconds(" + queueDetectionStartupIntervalInSeconds + ")");
        }
        return secondsSinceFirstDeviceDetectionSinceAlive >= queueDetectionStartupIntervalInSeconds;
    }

    /**
     * @param congestionReport
     * @return True if there is a queue in a CongestionReport depending if there
     * was already one, and how many devices are in QUEUE_ALERT_THREHOLD_BIN
     * @throws InvalidConfigurationException
     */
    private boolean isQueuePresent(CongestionReport congestionReport) throws InvalidConfigurationException {
        if (queuePresent) {
            int queueClearanceThreshold = configurationManager.getInt(idOutStation, ConfigurationManager.QUEUE_CLEARANCE_THREHOLD);
            if (getDevicesInQueueAlertThresholdBin(congestionReport) < queueClearanceThreshold) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The OutStation " + idOutStation + " hasn't detected a queue present because before there was a queue and now the queue clearance threhold is " + queueClearanceThreshold);
                }
                queuePresent = false;
            } else if (logger.isDebugEnabled()) {
                logger.debug("The OutStation " + idOutStation + " has detected a queue present because before there was a queue and now the queue clearance threhold is " + queueClearanceThreshold);
            }
        } else {
            int queueDetectThreshold = configurationManager.getInt(idOutStation, ConfigurationManager.QUEUE_DETECT_THREHOLD);
            if (getDevicesInQueueAlertThresholdBin(congestionReport) >= queueDetectThreshold) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The OutStation " + idOutStation + " has detected a queue present because before there wasn't a queue and now the queue detect threhold is " + queueDetectThreshold);
                }
                queuePresent = true;
            } else if (logger.isDebugEnabled()) {
                logger.debug("The OutStation " + idOutStation + " hasn't detected a queue present because before there wasn't a queue and now the queue detect threhold is " + queueDetectThreshold);
            }
        }
        return queuePresent;
    }

    /**
     * @param congestionReport
     * @return Number of devices in the bin specified in
     * QUEUE_ALERT_THREHOLD_BIN QUEUE_ALERT_THREHOLD_BIN should have one of the
     * values of QUEUE_ALERT_THREHOLD_BIN_VALUE_* (String or int)
     * @throws InvalidConfigurationException
     */
    private int getDevicesInQueueAlertThresholdBin(CongestionReport congestionReport) throws InvalidConfigurationException {
        String queueAlertThresholdBin = configurationManager.get(idOutStation, ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN);
        int devicesInQueueAlertThresholdBin;
        if (ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_FREE_STRING.equalsIgnoreCase(queueAlertThresholdBin)
                || ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_FREE_INT.equalsIgnoreCase(queueAlertThresholdBin)) {
            devicesInQueueAlertThresholdBin = congestionReport.getFromFreeToStationaryBin();
        } else if (ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_MODERATE_STRING.equalsIgnoreCase(queueAlertThresholdBin)
                || ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_MODERATE_INT.equalsIgnoreCase(queueAlertThresholdBin)) {
            devicesInQueueAlertThresholdBin = congestionReport.getFromModerateToStationaryBin();
        } else if (ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_SLOW_STRING.equalsIgnoreCase(queueAlertThresholdBin)
                || ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_SLOW_INT.equalsIgnoreCase(queueAlertThresholdBin)) {
            devicesInQueueAlertThresholdBin = congestionReport.getFromSlowToStationaryBin();
        } else if (ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_VERY_SLOW_STRING.equalsIgnoreCase(queueAlertThresholdBin)
                || ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_VERY_SLOW_INT.equalsIgnoreCase(queueAlertThresholdBin)) {
            devicesInQueueAlertThresholdBin = congestionReport.getFromVerySlowToStationaryBin();
        } else if (ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_STATIC_STRING.equalsIgnoreCase(queueAlertThresholdBin)
                || ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN_VALUE_STATIC_INT.equalsIgnoreCase(queueAlertThresholdBin)) {
            devicesInQueueAlertThresholdBin = congestionReport.getStationaryBin();
        } else {
            throw new InvalidConfigurationException(ConfigurationManager.QUEUE_ALERT_THREHOLD_BIN + " doesn't have a valid value. It's " + queueAlertThresholdBin + " but should be freeFlow, moderateFlow, slowFlow, verySlowFlow, staticFlow or numbers from 1 to 5");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("The OutStation " + idOutStation + " has detected " + devicesInQueueAlertThresholdBin + " devices in the bin " + queueAlertThresholdBin + " or above.");
        }
        return devicesInQueueAlertThresholdBin;
    }

    public RawDeviceDataOfOutStationCleaner getCleaner() {
        return cleaner;
    }

    public int getNumberOfDevices() {
        return mapDevices.size();
    }
}
