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
 * Created on 31-July-2015 09:18 AM
 */
package ssl.bluetruth.emitter2converter.utilsfortesting;

import java.util.Map;
import ssl.bluetruth.emitter2converter.configuration.DefaultConfiguration;
import ssl.bluetruth.emitter2converter.configuration.OutStationsConfiguration;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;

/**
 * @author josetrujillo-brenes
 */
public class DefaultAndOutStationsConfigurationFake implements DefaultConfiguration, OutStationsConfiguration {
 
    Map<String, String> defaultMap;
    Map<String, String> outStationMap;
    
    public DefaultAndOutStationsConfigurationFake(Map<String, String> defaultMap, Map<String, String> outStationMap) {
        this.defaultMap = defaultMap;
        this.outStationMap = outStationMap;
    }
    
    @Override
    public Map<String, String> getMap() throws InvalidConfigurationException {
        return defaultMap; 
    }
    
    @Override
    public Map<String, String> getMap(String idOutStation) throws InvalidConfigurationException {
        return outStationMap; 
    }
        
    public void setDefaultMap(String propertyName, String propertyValue) {
        defaultMap.put(propertyName, propertyValue);
    }
    
    public void setDefaultMap(Map<String, String> defaultMap) {
        this.defaultMap = defaultMap;
    }
    
    public void setOutStationsMap(String propertyName, String propertyValue) {
        outStationMap.put(propertyName, propertyValue);
    }
    
    public void setOutStationsMap(Map<String, String> outStationMap) {
        this.outStationMap = outStationMap;
    }
    
    
   
    
}
