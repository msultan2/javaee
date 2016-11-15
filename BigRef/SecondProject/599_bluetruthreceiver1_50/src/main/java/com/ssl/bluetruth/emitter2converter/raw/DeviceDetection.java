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
import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;

/**
 * This class stores the seconds first and last seen of a device.
 * With this information it calculates if the device is absent, background, or background for too much.
 * It also provides all the time units needed for the reports and the flow where the device is.
 * Another information stored here is if a device has already been reported as absent or background.
 * @author josetrujillo-brenes
 */
public class DeviceDetection {
    
    public enum Flow { FREE, MODERATE, SLOW, VERY_SLOW, STATIC }
    
    private final Logger logger = Logger.getLogger(getClass());
    private final ConfigurationManager configurationManager;
    private final String idOutStation;
    
    private long secondsFirstSeen;    
    private long secondsLastSeen;
    private boolean alreadyReportedAsAbsentOrBackground;
        
    /**
     * Each DeviceDetection of a new device should call the constructor
     * @param idOutStation Used to read configuration values
     * @param timestampFirstSeen 
     */
    public DeviceDetection(String idOutStation, Timestamp timestampFirstSeen) { 
        this.idOutStation = idOutStation;
        configurationManager = ThreadLocalConfigurationManager.get();        
        secondsFirstSeen = TimeUtils.secondsOf(timestampFirstSeen);
        secondsLastSeen = secondsFirstSeen;
        alreadyReportedAsAbsentOrBackground = false;
    }
    
    /**
     * Each DeviceDetection of a device already detected should call this method
     * @param timestampLastSeen 
     */
    public void setTimeLastSeen(Timestamp timestampLastSeen) {
        long secondsLastSeenTemp = TimeUtils.secondsOf(timestampLastSeen);     
        if (secondsLastSeenTemp > secondsLastSeen) {
            secondsLastSeen = secondsLastSeenTemp;
        } else {
            String logMessage = null;
            if(logger.isInfoEnabled()) { 
                logMessage = "Detections came disordered (secondsFirstSeen='"+secondsFirstSeen+"', secondsLastSeen='"+secondsLastSeen+"', and the new one is '"+secondsLastSeenTemp+"') but we can fix it. ";
            }
            if (secondsLastSeenTemp < secondsFirstSeen) {
                if(logger.isInfoEnabled()) { 
                   logger.info(logMessage+"It really is the first seen so we just change the order."); 
                }                
                secondsFirstSeen = secondsLastSeenTemp;
            } else {
                if(logger.isInfoEnabled()) { 
                    logger.info(logMessage+"It's in the middle, so we ignore the new detection.");
                }                
            }
        }
    }
    
    /**
     * Devices should be reported in statistics report only once, to achieve this setAlreadyReportedAsAbsentOrBackground() 
     * should be called when a device is included as absent or background in a statistics report
     */
    public void setAlreadyReportedAsAbsentOrBackground() {
        this.alreadyReportedAsAbsentOrBackground = true;
    }
    
    
    public boolean isAlreadyReportedAsAbsentOrBackground() {
        return alreadyReportedAsAbsentOrBackground;
    }
    
    public boolean isBackground() throws InvalidConfigurationException {
        int backgroundLatchTimeThresholdInSeconds = configurationManager.getInt(idOutStation, ConfigurationManager.BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS);
        return (getSecondsSeen() > backgroundLatchTimeThresholdInSeconds);
    }    
    
    /**
     * This method is meant to be called in a context where is unknown if the device is background.
     * If a device is background, it wouldn't be absent
     * @return
     * @throws InvalidConfigurationException 
     */
    public boolean isAbsent() throws InvalidConfigurationException {
        if (isBackground()) {
            return false;
        } else {
            return isAbsentAssumingItsNotBackground();
        }          
    }
    
    /**
     * This method is meant to be called in a context where is known that a device is not background.
     * @return
     * @throws InvalidConfigurationException 
     */
    public boolean isAbsentAssumingItsNotBackground() throws InvalidConfigurationException {
        int absenceThresholdInSeconds = configurationManager.getInt(idOutStation, ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS);
        long notSeenSince = TimeUtils.secondsSince(secondsLastSeen);
        return (notSeenSince > absenceThresholdInSeconds);
    }
    
    /**
     * This method is meant to be called in a context where is unknown if the device is background.
     * If a device isn't background, it wouldn't be background and absent for too much
     * @return
     * @throws InvalidConfigurationException 
     */
    public boolean isBackgroundAndAbsentForTooMuch() throws InvalidConfigurationException {
        if (isBackground()) {
            return isBackgroundAndAbsentForTooMuchAssumingItsBackground();
        } else {
            return false;
        }  
    }
    
    /**
     * This method is meant to be called in a context where is known that a device is background.
     * @return
     * @throws InvalidConfigurationException 
     */
    public boolean isBackgroundAndAbsentForTooMuchAssumingItsBackground() throws InvalidConfigurationException {
        int backgroundClearanceTimeThresholdInSeconds = configurationManager.getInt(idOutStation, ConfigurationManager.BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS);
        long notSeenSince = TimeUtils.secondsSince(secondsLastSeen);
        return (notSeenSince > backgroundClearanceTimeThresholdInSeconds);
    }
    
    
    public long getSecondsSeen() {        
        return secondsLastSeen - secondsFirstSeen;
    }
    
    public String getSecondsFirstSeenInHex() {        
        return TimeUtils.secondsInHexOf(secondsFirstSeen);
    }
    
    public String getSecondsSeenInHex() {
        String secondsSeenInHex = TimeUtils.secondsInHexOf(getSecondsSeen());
        if ("0".equalsIgnoreCase(secondsSeenInHex)) {
            return "1";
        } else {
            return secondsSeenInHex;
        }     
    }
    
    public String getHalfSecondsSeenInHex() {
        return TimeUtils.secondsInHexOf(getSecondsSeen()/2);
    }
    
    /**
     * A device would be in a bin if it's been seen for the bin threshold in seconds, or less
     * @return
     * @throws InvalidConfigurationException 
     */
    public Flow getBin() throws InvalidConfigurationException {
        int freeFlowBinThresholdInSeconds = configurationManager.getInt(idOutStation, ConfigurationManager.FREE_FLOW_BIN_THRESHOLD_IN_SECONDS);
        if(getSecondsSeen() <= freeFlowBinThresholdInSeconds) {
            return Flow.FREE;
        } 
        int moderateFlowBinThresholdInSeconds = configurationManager.getInt(idOutStation, ConfigurationManager.MODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS);
        if(getSecondsSeen() <= moderateFlowBinThresholdInSeconds) {
            return Flow.MODERATE;
        } 
        int slowFlowBinThresholdInSeconds = configurationManager.getInt(idOutStation, ConfigurationManager.SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS);
        if(getSecondsSeen() <= slowFlowBinThresholdInSeconds) {
            return Flow.SLOW;
        } 
        int verySlowFlowBinThresholdInSeconds = configurationManager.getInt(idOutStation, ConfigurationManager.VERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS);
        if(getSecondsSeen() <= verySlowFlowBinThresholdInSeconds) {
            return Flow.VERY_SLOW;
        } else {
            return Flow.STATIC;
        } 
    } 
}
