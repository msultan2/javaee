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
 * Created on 28-Jul-2015 11:07 AM
 */
package com.ssl.bluetruth.emitter2converter.raw;

import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.configuration.AbstractConfigurationManagerListener;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_0;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_1;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_2;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_3;

/**
 *
 * @author josetrujillo-brenes
 */
public class RawDeviceDataOfOutStationCleaner extends AbstractConfigurationManagerListener {
    
    private final Logger logger = Logger.getLogger(getClass());
    
    private final String idOutStation;
    private final Map<String, DeviceDetection> mapDevices;
    private final ConfigurationManager configurationManager;
    
    private static final boolean LOG_REMOVED_DEVICES_AT_INFO_LEVEL = true;
    
    public RawDeviceDataOfOutStationCleaner(String idOutStation, Map<String, DeviceDetection> mapDevices, ConfigurationManager configurationManager) {
        this.idOutStation = idOutStation;
        this.mapDevices = mapDevices;
        this.configurationManager = configurationManager;  
        configurationManager.addConfigurationManagerListener(this);
    }
    
    public void clean(ReportEnum report) {
        Integer mode;
        try {
            mode = configurationManager.getInt(idOutStation, ConfigurationManager.MODE);
        } catch (InvalidConfigurationException icex) {
            mode = MODE_3;
            logger.warn("There has been an exception trying to obtain the mode of the Detector "+idOutStation+". This is not valid and it should be fixed. Meanwhile it will be considered as mode "+mode+". Exception: "+icex.getLocalizedMessage());            
        } 
        if(logger.isDebugEnabled()) { 
            logger.debug("Detector '"+idOutStation+"' is in mode '"+mode+"' and the cleaner has been called by '"+report.getName()+"'"); 
        }
        switch (mode) { 
            case MODE_0:
                clean(!LOG_REMOVED_DEVICES_AT_INFO_LEVEL);
                break;
            case MODE_2:                
                if(report == ReportEnum.CONGESTION) {
                    clean(!LOG_REMOVED_DEVICES_AT_INFO_LEVEL);
                }
                break;
            default:
                if(mode != MODE_1 && mode != MODE_3) {
                    logger.warn("The mode of the Detector "+idOutStation+" is "+mode+". This is not a valid value and it should be fixed. Meanwhile it will be considered as mode "+MODE_3);
                } 
                if(report == ReportEnum.STATISTICS_BRIEF || report == ReportEnum.STATISTICS_FULL) {
                    clean(!LOG_REMOVED_DEVICES_AT_INFO_LEVEL);
                }
                break;
        }
    }
 
    private void clean(boolean logRemovedDevicesAtInfoLevel) {
        Iterator it = mapDevices.entrySet().iterator();
        String methodLog = null;
        if(logger.isDebugEnabled()) {
            methodLog = idOutStation+" cleaner - ";
        }
        while (it.hasNext()) {
            String macAddress = null;
            try {                
                Map.Entry<String, DeviceDetection> entry = (Map.Entry) it.next();
                macAddress = entry.getKey();
                DeviceDetection deviceDetection = entry.getValue();                
                if (deviceDetection.isBackground()) {
                    if(deviceDetection.isBackgroundAndAbsentForTooMuchAssumingItsBackground()) {
                        if(logger.isDebugEnabled() || (logger.isInfoEnabled() && logRemovedDevicesAtInfoLevel)) {
                            String message = methodLog+"Device '"+macAddress+"' is a background device that has been absent for too much and will be removed.";
                            if(logRemovedDevicesAtInfoLevel) {
                                logger.info(message); 
                            } else {
                                logger.debug(message); 
                            }
                        }
                        it.remove(); 
                    } else {
                        if(logger.isDebugEnabled()) { 
                            logger.debug(methodLog+"'"+macAddress+"' is a background device that hasn't been absent for too much and will not be removed."); 
                        }
                    }
                } else if(deviceDetection.isAbsentAssumingItsNotBackground()) {
                    if(logger.isDebugEnabled() || (logger.isInfoEnabled() && logRemovedDevicesAtInfoLevel)) {
                        String message = methodLog+"Device '"+macAddress+"' is absent and will be removed.";
                        if(logRemovedDevicesAtInfoLevel) {
                            logger.info(message); 
                        } else {
                            logger.debug(message); 
                        }
                    }
                    it.remove(); 
                } else {
                    if(logger.isDebugEnabled()) { 
                        logger.debug(methodLog+"'"+macAddress+"' is a present device and will not be removed."); 
                    }
                }
            } catch (InvalidConfigurationException icex) {
                logger.warn("There has been an exception trying to determinate if device "+macAddress+" should be cleaned. The device would be removed. Exception: "+icex.getLocalizedMessage());            
                it.remove();  
            }             
        }
    }

    @Override
    public void ApplyConfigurationChanges(String idOutStation, Map<String, String> oldMap, Map<String, String> newMap) {
        if(logger.isDebugEnabled()) { 
            logger.debug("RawDeviceDataOfOutStationCleaner has been notify of configuration changes for Detector '"+idOutStation+"'");
        }
        cleanAccordingToOldAndNewMode(idOutStation, oldMap, newMap);        
    }
    
    private void cleanAccordingToOldAndNewMode(String idOutStation, Map<String, String> oldMap, Map<String, String> newMap) {
        int oldMode = getMode(idOutStation, oldMap, newMap, OldOrNew.OLD);
        int newMode = getMode(idOutStation, oldMap, newMap, OldOrNew.NEW);
        if(oldMode!=newMode && newMode == MODE_0) {
            if(logger.isInfoEnabled()) { 
                logger.info("Detector '"+idOutStation+"' has change to mode '"+newMode+"'");
            }
            clean(LOG_REMOVED_DEVICES_AT_INFO_LEVEL);
        }
    }
}
