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

package ssl.bluetruth.emitter2converter.utilsfortesting;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManagerListener;
import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;

/**
 * Implementation of ConfigurationManager that is meant to be a substitute for ConfigurationManagerImpl in the tests
 * It allows to use the default map created in newMapWithDefaultFakeValues, create your own one or use the default one and change values with a set.
 * This class makes no difference between a OutStation configuration and a default one. Always uses the default configuration. 
 * @author jtrujillo-brenes
 */
public class ConfigurationManagerFake implements ConfigurationManager{
    
    private final Logger logger = LogManager.getLogger(getClass());
    private final Map<String, String> fakeConfigurationMap;
    
    
    public ConfigurationManagerFake() {        
        this(null);
    }
    
    public ConfigurationManagerFake(Map<String, String> fakeConfigurationMap) {
        if(fakeConfigurationMap == null) {
            this.fakeConfigurationMap = newMapWithDefaultFakeValues();
        } else {
            this.fakeConfigurationMap = fakeConfigurationMap;
        }
    }    
    
    public String get(String propertyName) {
        String value = fakeConfigurationMap.get(propertyName);
        if(value != null) {
            return value;
        } else {
            throw new NullPointerException("The property "+propertyName+" isn't present in the fake ConfigurationManager");
        }
    }
    
    @Override
    public String get(String idOutStation, String propertyName) {
        return get(propertyName);
    } 
    
    public void set(String propertyName, String popertyValue) {
        fakeConfigurationMap.put(propertyName, popertyValue);
    }
    
    private Map<String, String> newMapWithDefaultFakeValues() { 
        Map<String, String> map = new HashMap();
        map.put(MODE, "3");
        map.put(URL_CONGESTION_REPORTS, "http://localhost:8080/BlueTruthReceiver2/Congestion");
        map.put(URL_STATISTICS_REPORTS, "http://localhost:8080/BlueTruthReceiver2/Statistics");
        map.put(STATISTICS_REPORT_PERIOD_IN_SECONDS, "5");
        map.put(CONGESTION_REPORT_PERIOD_IN_SECONDS, "9");
        map.put(ABSENCE_THREHOLD_IN_SECONDS, "60");
        map.put(BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, "3000");
        map.put(BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS, "30000");
        map.put(FREE_FLOW_BIN_THRESHOLD_IN_SECONDS, "2");
        map.put(MODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, "4");
        map.put(SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, "6");
        map.put(VERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, "8");
        map.put(QUEUE_ALERT_THREHOLD_BIN, QUEUE_ALERT_THREHOLD_BIN_VALUE_VERY_SLOW_STRING);
        map.put(QUEUE_DETECT_THREHOLD, "5");
        map.put(QUEUE_CLEARANCE_THREHOLD, "3");
        map.put(QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS, "8");
        map.put(EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS, "10");
        map.put(MISSING_DEVICE_DETECTIONS_TO_CONSIDER_DEAD, "3");
        map.put(STATISTICS_REPORTS_CONTENTS, STATISTICS_REPORTS_CONTENTS_VALUE_BRIEF);
        map.put(CONGESTION_REPORT_DELAY_WHEN_INSTATION_RECEIVES_ONE_IN_SECONDS, "5");
        return map;
    }

    @Override
    public int getInt(String idOutStation, String propertyName) throws InvalidConfigurationException {
        return getInt(propertyName);
    }

    public int getInt(String propertyName) throws InvalidConfigurationException {        
         try {
            return Integer.valueOf(get(propertyName));
        } catch(NumberFormatException nfex) {             
            String message = "Unable to convert de value of '"+propertyName+"' into a int. cause: "+nfex.getLocalizedMessage();
            logger.warn(message, nfex); 
            throw new InvalidConfigurationException(message, nfex); 
        }
    }
    
    public static void delete() {
        ThreadLocalConfigurationManager.remove();
    }

    @Override
    public void addConfigurationManagerListener(ConfigurationManagerListener listener) {
        
    }
    
}

