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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.SETTINGS_COLLECTION_LAST_UPDATE_IN_SECONDS;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;

/**
 * Implementation of ConfigurationManager that takes the specific values of each OutStation and the default ones from the classes
 * implementing OutStationsConfiguration and DefaultConfiguration specified when creating a new instance of this class.
 * This class has a map with the configuration of each outStation. Each one of this is created putting all the values found in the 
 * OutStationsConfiguration, and then it adds the values in DefaultConfiguration if they were absent in the OutStationsConfiguration.
 * Successive calls to get will use "settingsCollectionInterval" (Value also in the database) to decide if the map has expired and 
 * needs to be reloaded.
 * @author jtrujillo-brenes
 */
public class ConfigurationManagerImpl implements ConfigurationManager {
    
    private static final String OUTSTATION_MODE_ZERO = "0";
    private final Logger logger = LogManager.getLogger(getClass());

    private final OutStationsConfiguration outStationsConfiguration;
    private final DefaultConfiguration defaultConfiguration;
    private final List<ConfigurationManagerListener> listeners;
    private final Map<String, Map<String, String>> map;     
    
    public ConfigurationManagerImpl(DefaultConfiguration defaultConfiguration, OutStationsConfiguration outStationsConfiguration) {            
        this.defaultConfiguration = defaultConfiguration;
        this.outStationsConfiguration = outStationsConfiguration;        
        map = new HashMap();
        listeners = new ArrayList<>();
    }
       
    @Override
    public String get(String idOutStation, String propertyName) throws InvalidConfigurationException {        
        if(!containsValidMap(idOutStation)) {            
            Map<String, String> newMap = addDefaultValues(getOutStationsMap(idOutStation));
            if(containsMap(idOutStation)) {
                Map<String, String> oldMap = map.get(idOutStation);
                map.put(idOutStation, newMap);
                notifyListenersToApplyConfigurationChanges(idOutStation, oldMap, newMap);
            } else {
                map.put(idOutStation, newMap);
            }
            if(logger.isInfoEnabled()) { 
                logger.info("new Configuration Map obtained for outStation '"+idOutStation+"': "+newMap); 
            }            
        }
        String stringValue = map.get(idOutStation).get(propertyName);
        if(stringValue != null) {
            if(logger.isDebugEnabled()) {
                logger.debug("Configuration property '"+propertyName+"' from OutStation '"+idOutStation+"': "+stringValue);
            }            
            return stringValue;
        } else {
            String message = "Unable to find '"+propertyName+"' for OutStation '"+idOutStation+"'.";
            if(logger.isDebugEnabled()) {
                logger.debug(message);
            }
            throw new InvalidConfigurationException(message);
        }
    }
    
    @Override
    public int getInt(String idOutStation, String propertyName) throws InvalidConfigurationException {        
        return intOf(get(idOutStation, propertyName), idOutStation, propertyName);
    }
    
    @Override
    public void addConfigurationManagerListener(ConfigurationManagerListener listener) {        
        listeners.add(listener);
        if(logger.isDebugEnabled()) {
            logger.debug("Added listener '"+listener+"' to the ConfigurationManager");
        }
    }
      
    private boolean isExpiredMap(String idOutStation) throws InvalidConfigurationException {
        Map<String, String> outStationsMap = map.get(idOutStation);
        String settingsCollectionLastUpdateInSeconds = outStationsMap.get(SETTINGS_COLLECTION_LAST_UPDATE_IN_SECONDS);
        if(settingsCollectionLastUpdateInSeconds == null) {
            String message = "Unable to find out if the configuration for OutStation '"+idOutStation+"' has expired because '"+SETTINGS_COLLECTION_LAST_UPDATE_IN_SECONDS+"' is missing.";
            throw new InvalidConfigurationException(message);
        }
        long minutesSinceLastUpdate = TimeUtils.minutesOf(TimeUtils.secondsSince(Long.valueOf(settingsCollectionLastUpdateInSeconds)));
        int settingsCollectionIntervalInMinutes;
        String outStationMode = outStationsMap.get(MODE);
        if(outStationMode == null) {
            String message = "Unable to find out if the configuration for OutStation '"+idOutStation+"' has expired because '"+MODE+"' is missing.";
            throw new InvalidConfigurationException(message);
        }
        if(OUTSTATION_MODE_ZERO.equalsIgnoreCase(outStationMode)) {
            String settingsCollectionIntervalForMode0InMinutes = outStationsMap.get(SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES);
            if(settingsCollectionIntervalForMode0InMinutes == null) {
                String message = "Unable to find out if the configuration for OutStation '"+idOutStation+"' has expired because '"+SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES+"' is missing.";
                throw new InvalidConfigurationException(message);
            }
            settingsCollectionIntervalInMinutes = Integer.valueOf(settingsCollectionIntervalForMode0InMinutes);
        } else {
            String settingsCollectionIntervalForModes123InMinutes = outStationsMap.get(SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES);
            if(settingsCollectionIntervalForModes123InMinutes == null) {
                String message = "Unable to find out if the configuration for OutStation '"+idOutStation+"' has expired because '"+SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES+"' is missing.";
                throw new InvalidConfigurationException(message);
            }
            settingsCollectionIntervalInMinutes = Integer.valueOf(settingsCollectionIntervalForModes123InMinutes);
        }
        return (minutesSinceLastUpdate >= settingsCollectionIntervalInMinutes);
    } 
    
    private boolean containsValidMap(String idOutStation) throws InvalidConfigurationException {
        if(containsMap(idOutStation)) {
            if(isExpiredMap(idOutStation)) {
                if(logger.isDebugEnabled()) {
                    logger.debug("The configuration map for the OutStation "+idOutStation+" has expired");
                }
                return false;
            }
            return true;
        } else {
            if(logger.isDebugEnabled()) {
                logger.debug("There is no configuration map for the OutStation "+idOutStation);
            }
            return false;
        }
    }
    
    private boolean containsMap(String idOutStation) {
        return map.containsKey(idOutStation);
    } 
    
    private Map<String, String> getOutStationsMap(String idOutStation) throws InvalidConfigurationException {
        Map<String, String> newMap = new HashMap();
        newMap.putAll(outStationsConfiguration.getMap(idOutStation));        
        newMap.put(SETTINGS_COLLECTION_LAST_UPDATE_IN_SECONDS, String.valueOf(TimeUtils.currentSeconds()));
        return newMap;
    }
    
    private Map<String, String> addDefaultValues(Map<String, String> map) throws InvalidConfigurationException {
        for(Entry<String, String> entry : defaultConfiguration.getMap().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            map.putIfAbsent(key, value);
        }
        return map;
    }
    
    private void notifyListenersToApplyConfigurationChanges(String idOutStation, Map<String, String> oldMap, Map<String, String> newMap) {
        for (ConfigurationManagerListener listener : listeners) {            
            listener.ApplyConfigurationChanges(idOutStation, oldMap, newMap);
        }
    }
       
    private int intOf(String propertyValue, String idOutStationForLog, String propertyNameForLog) throws InvalidConfigurationException {
        try {
            return Integer.valueOf(propertyValue);
        } catch(NumberFormatException nfex) {             
            String configurationForLog;
            if(idOutStationForLog != null) {
                configurationForLog = "OutStation '"+idOutStationForLog+"'";                
            } else {
                configurationForLog = "default";                
            }
            String message = "Unable to convert de value '"+propertyValue+"' of '"+propertyNameForLog+"' in the "+configurationForLog+" configuration into a int. cause: "+nfex.getLocalizedMessage();
            logger.warn(message, nfex); 
            throw new InvalidConfigurationException(message, nfex); 
        }
    }
}
