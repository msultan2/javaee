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
 * Created on 31-Jul-2015 09:31 AM
 */
package ssl.bluetruth.emitter2converter.configuration;

import ssl.bluetruth.emitter2converter.utilsfortesting.ConfigurationManagerListenerFake;
import ssl.bluetruth.emitter2converter.utilsfortesting.DefaultAndOutStationsConfigurationFake;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringContains.containsString;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.LogUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.PowerMockTimeUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.TimeUtilsFake;

/**
 *
 * @author josetrujillo-brenes
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest( { TimeUtils.class })
public class ConfigurationManagerImplTest {
    
    private final Logger logger = Logger.getLogger(getClass());   
    private final static String OUTSTATION = "idOutStation"; 
    
    public ConfigurationManagerImplTest() {
        LogUtils.showLogsInConsoleOf("ssl", Level.DEBUG);    
    }

    @Test(expected=InvalidConfigurationException.class)
    public void get_anyValuePresent_throwsInvalidConfigurationException() throws InvalidConfigurationException {
        Map<String, String> defaultMap = new HashMap();
        Map<String, String> outStationsMap = new HashMap();
        
        DefaultAndOutStationsConfigurationFake defaultAndOutStationsConfigurationFake = new DefaultAndOutStationsConfigurationFake(defaultMap, outStationsMap);
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultAndOutStationsConfigurationFake, defaultAndOutStationsConfigurationFake);
        configurationManager.get(OUTSTATION, ConfigurationManager.MODE);
    }
    
    @Test
    public void get_bothValuesPresent_getsOutStationsValue() throws InvalidConfigurationException {
        Map<String, String> defaultMap = new HashMap();
        defaultMap.put(ConfigurationManager.MODE, "1");
        Map<String, String> outStationsMap = new HashMap();
        outStationsMap.put(ConfigurationManager.MODE, "2");
        
        DefaultAndOutStationsConfigurationFake defaultAndOutStationsConfigurationFake = new DefaultAndOutStationsConfigurationFake(defaultMap, outStationsMap);
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultAndOutStationsConfigurationFake, defaultAndOutStationsConfigurationFake);
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("2"));
    }
    
    @Test
    public void get_onlyOutStationsValuePresent_getsOutStationsValue() throws InvalidConfigurationException {
        Map<String, String> defaultMap = new HashMap();
        defaultMap.put(ConfigurationManager.MODE, null);
        Map<String, String> outStationsMap = new HashMap();
        outStationsMap.put(ConfigurationManager.MODE, "2");
        outStationsMap.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES, "2");
        
        DefaultAndOutStationsConfigurationFake defaultAndOutStationsConfigurationFake = new DefaultAndOutStationsConfigurationFake(defaultMap, outStationsMap);
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultAndOutStationsConfigurationFake, defaultAndOutStationsConfigurationFake);
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("2"));
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES), is("2"));
    }
    
    @Test
    public void get_onlyDefaultValuePresent_getsOutStationsValue() throws InvalidConfigurationException {
        Map<String, String> defaultMap = new HashMap();
        defaultMap.put(ConfigurationManager.MODE, "2");
        defaultMap.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES, "2");
        
        Map<String, String> outStationsMap = new HashMap();
        outStationsMap.put(ConfigurationManager.MODE, null);
        
        DefaultAndOutStationsConfigurationFake defaultAndOutStationsConfigurationFake = new DefaultAndOutStationsConfigurationFake(defaultMap, outStationsMap);
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultAndOutStationsConfigurationFake, defaultAndOutStationsConfigurationFake);
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("2"));
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES), is("2"));
    }
    
    @Test
    public void get_noConfigurationExpireValuesPresent_secondGetThrowsInvalidConfigurationException() throws InvalidConfigurationException {
        try {
            Map<String, String> defaultMap = new HashMap();
            Map<String, String> outStationsMap = new HashMap();
            outStationsMap.put(ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS, "2");

            DefaultAndOutStationsConfigurationFake defaultAndOutStationsConfigurationFake = new DefaultAndOutStationsConfigurationFake(defaultMap, outStationsMap);
            ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultAndOutStationsConfigurationFake, defaultAndOutStationsConfigurationFake);
            configurationManager.get(OUTSTATION, ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS);
            configurationManager.get(OUTSTATION, ConfigurationManager.ABSENCE_THREHOLD_IN_SECONDS);
            
            fail("Expecting InvalidConfigurationException when there is no "+ConfigurationManager.MODE);
        } catch (InvalidConfigurationException icex) {
            logger.info(icex.getLocalizedMessage());
            assertThat(icex.getLocalizedMessage(), containsString(ConfigurationManager.MODE));
        } catch (RuntimeException rex) {
            fail("Expecting InvalidConfigurationException when there is no "+ConfigurationManager.MODE+" but had "+rex);
        }
        
        try {
            Map<String, String> defaultMap = new HashMap();
            defaultMap.put(ConfigurationManager.MODE, "1");
            Map<String, String> outStationsMap = new HashMap();

            DefaultAndOutStationsConfigurationFake defaultAndOutStationsConfigurationFake = new DefaultAndOutStationsConfigurationFake(defaultMap, outStationsMap);
            ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultAndOutStationsConfigurationFake, defaultAndOutStationsConfigurationFake);
            configurationManager.get(OUTSTATION, ConfigurationManager.MODE);
            configurationManager.get(OUTSTATION, ConfigurationManager.MODE);
            
            fail("Expecting InvalidConfigurationException when there is no "+ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES);
        } catch (InvalidConfigurationException icex) {
            logger.info(icex.getLocalizedMessage());
            assertThat(icex.getLocalizedMessage(), containsString(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES));
        } catch (RuntimeException rex) {
            fail("Expecting InvalidConfigurationException when there is no "+ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES+" but had "+rex);
        }
        
        try {
            Map<String, String> defaultMap = new HashMap();            
            Map<String, String> outStationsMap = new HashMap();
            outStationsMap.put(ConfigurationManager.MODE, "0");

            DefaultAndOutStationsConfigurationFake defaultAndOutStationsConfigurationFake = new DefaultAndOutStationsConfigurationFake(defaultMap, outStationsMap);
            ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultAndOutStationsConfigurationFake, defaultAndOutStationsConfigurationFake);
            configurationManager.get(OUTSTATION, ConfigurationManager.MODE);
            configurationManager.get(OUTSTATION, ConfigurationManager.MODE);
            
            fail("Expecting InvalidConfigurationException when there is no "+ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES);
        } catch (InvalidConfigurationException icex) {
            logger.info(icex.getLocalizedMessage());
            assertThat(icex.getLocalizedMessage(), containsString(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES));
        } catch (RuntimeException rex) {
            fail("Expecting InvalidConfigurationException when there is no "+ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES+" but had "+rex);
        }        
    }
    
    @Test
    public void get_configurationChanges_changesShowAfterTryingToGetAValueAndConfigurationExpires() throws InvalidConfigurationException {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime();
        TimeUtilsFake.setCurrentSeconds(0);       
        Map<String, String> defaultMap = new HashMap();
        defaultMap.put(ConfigurationManager.MODE, "1");
        defaultMap.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES, "1");        
        defaultMap.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES, "2");        
        Map<String, String> outStationsMap = new HashMap();
        
        DefaultAndOutStationsConfigurationFake defaultAndOutStationsConfigurationFake = new DefaultAndOutStationsConfigurationFake(defaultMap, outStationsMap);
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultAndOutStationsConfigurationFake, defaultAndOutStationsConfigurationFake);
        
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("1"));
        defaultMap.put(ConfigurationManager.MODE, "2");
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("1"));
        TimeUtilsFake.add(59);
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("1"));
        TimeUtilsFake.add(1);
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("2"));
        defaultMap.put(ConfigurationManager.MODE, "1");
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("2"));
        TimeUtilsFake.add(100);
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("1"));
        TimeUtilsFake.add(20);
        assertThat(configurationManager.get(OUTSTATION, ConfigurationManager.MODE), is("1"));
    } 

    @Test
    public void get_configurationChanges_notifiesListenerWhenTryingToGetAValueAndConfigurationExpires() throws InvalidConfigurationException {
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
        Map<String, String> defaultMap = new HashMap();
        defaultMap.put(ConfigurationManager.MODE, "1");
        defaultMap.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES, "1");        
        defaultMap.put(ConfigurationManager.SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES, "2");        
        Map<String, String> outStationsMap = new HashMap();
        
        DefaultAndOutStationsConfigurationFake defaultAndOutStationsConfigurationFake = new DefaultAndOutStationsConfigurationFake(defaultMap, outStationsMap);
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultAndOutStationsConfigurationFake, defaultAndOutStationsConfigurationFake);
        ConfigurationManagerListenerFake fakeListener = new ConfigurationManagerListenerFake();
        configurationManager.addConfigurationManagerListener(fakeListener);        
               
        configurationManager.get(OUTSTATION, ConfigurationManager.MODE);
        assertThat(fakeListener.getNotified(), is(false));
        defaultMap.put(ConfigurationManager.MODE, "2");
        TimeUtilsFake.add(30);
        configurationManager.get(OUTSTATION, ConfigurationManager.MODE);
        assertThat(fakeListener.getNotified(), is(false));
        TimeUtilsFake.add(30);
        configurationManager.get(OUTSTATION, ConfigurationManager.MODE);
        assertThat(fakeListener.getNotified(), is(true));
    }
}
