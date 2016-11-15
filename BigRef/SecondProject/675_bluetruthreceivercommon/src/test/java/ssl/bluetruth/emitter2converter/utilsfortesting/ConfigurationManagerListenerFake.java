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
 * Created on 31-Jul-2015 01:47 PM
 */
package ssl.bluetruth.emitter2converter.utilsfortesting;

import java.util.Map;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManagerListener;

/**
 *
 * @author josetrujillo-brenes
 */
public class ConfigurationManagerListenerFake implements ConfigurationManagerListener {
    
    private static boolean notified = false;

    @Override
    public void ApplyConfigurationChanges(String idOutStation, Map<String, String> oldMap, Map<String, String> newMap) {        
        notified = true;
    }
    
    public boolean getNotified() {
        return notified;
    }
    
}
