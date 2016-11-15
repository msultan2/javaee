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
 * Created on 05-May-2015 07:44 AM
 */
package ssl.bluetruth.emitter2converter.utilsfortesting;

import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;


/**
 * This class is meant to be a substitute for TimeUtils in the tests, setting the current time 
 * and adding seconds in long or String in hex
 * The substitution is done with PowerMock in PowerMockTimeUtils
 * @author josetrujillo-brenes
 */
public class TimeUtilsFake {
    
    private static final Logger logger = Logger.getLogger(TimeUtilsFake.class); 
    private static long currentSeconds;
    
    private TimeUtilsFake() {} 
    
    public static void setCurrentSeconds(long newCurrentSeconds) {
        currentSeconds = newCurrentSeconds;  
        logger.info("currentSeconds set to "+currentSeconds);
    }
    
    public static void setCurrentSeconds(String secondsInHex) {
        setCurrentSeconds(TimeUtils.secondsOf(secondsInHex));  
    }
    
    public static void add(long currentSecondsToAdd) {
        currentSeconds = currentSeconds + currentSecondsToAdd;
        logger.info("Added "+currentSecondsToAdd+" seconds, setting currentSeconds to "+currentSeconds);
    }
    
    public static void add(String secondsInHex) {
        add(TimeUtils.secondsOf(secondsInHex));
    }
    
    public static long getCurrentSeconds() {
        return currentSeconds;  
    }
    
}
