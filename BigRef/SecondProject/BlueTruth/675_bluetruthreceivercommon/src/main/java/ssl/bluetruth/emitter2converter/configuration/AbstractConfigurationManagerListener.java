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
 * Created on 04-Aug-2015 09:59 AM
 */
package ssl.bluetruth.emitter2converter.configuration;

import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.MODE_3;

/**
 * This class contains useful methods for classes that want to implement ConfigurationManagerListener
 * @author josetrujillo-brenes
 */
public abstract class AbstractConfigurationManagerListener implements ConfigurationManagerListener {
    
    public enum OldOrNew {OLD, NEW};
    
    private final Logger logger = LogManager.getLogger(getClass()); 
    
    protected int getMode(String idOutStation, Map<String, String> oldMap, Map<String, String> newMap, OldOrNew oldOrNew) {
       int mode;
        try {
            if(oldOrNew == OldOrNew.OLD){
                mode = Integer.valueOf(oldMap.get(ConfigurationManager.MODE));
            } else {
                mode = Integer.valueOf(newMap.get(ConfigurationManager.MODE));
            }
        } catch (NumberFormatException nfex) {
            mode = MODE_3;
            logger.warn("There has been an exception trying to obtain the "+String.valueOf(oldOrNew).toLowerCase()+" mode of the Detector "+idOutStation+". This is not valid and it should be fixed. Meanwhile it will be considered as mode "+mode+". Exception: "+nfex.getLocalizedMessage());            
        }
        return mode;
    } 
    
    
}
