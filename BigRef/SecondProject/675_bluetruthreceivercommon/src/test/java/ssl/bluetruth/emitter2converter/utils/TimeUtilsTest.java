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

package ssl.bluetruth.emitter2converter.utils;

import java.sql.Timestamp;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ssl.bluetruth.emitter2converter.utilsfortesting.LogUtils;
import ssl.bluetruth.emitter2converter.utilsfortesting.PowerMockTimeUtils;

/**
 *
 * @author jtrujillo-brenes
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { TimeUtils.class })
public class TimeUtilsTest {
    
    private final Logger logger = Logger.getLogger(getClass());
    
    public TimeUtilsTest() {
        LogUtils.showAllLogsInConsole();
        PowerMockTimeUtils.replaceRealTimeWithFakeTime(0);
    }

       
    //Tests of methods which return a long with seconds:    
    @Test
    public void currentSeconds_withPowerMock_fakeTime() {      
        long seconds = TimeUtils.currentSeconds();
        long expSeconds = 0;
        assertEquals(expSeconds, seconds);        
    }
    
    
    @Test
    public void secondsOfTimestamp_10TimestampSeconds_10LongSeconds() {      
        long seconds = TimeUtils.secondsOf(new Timestamp(10000));
        long expSeconds = 10;
        assertEquals(expSeconds, seconds);        
    }
    
    @Test
    public void secondsOfHex_10HexSecondsWithout0x_16LongSeconds() {      
        long seconds = TimeUtils.secondsOf("10");
        long expSeconds = 16;
        assertEquals(expSeconds, seconds);        
    }
    
    @Test
    public void secondsOfHex_10HexSecondsWith0x_16LongSeconds() {      
        long seconds = TimeUtils.secondsOf("0x10");
        long expSeconds = 16;
        assertEquals(expSeconds, seconds);        
    }   
    
    //Tests of methods which return a String with seconds in Hexadecimal:    
    
    @Test
    public void secondsInHexOfLong_10LongSeconds_AHexSeconds() {      
        String secondsInHex = TimeUtils.secondsInHexOf(10);
        String expSecondsInHex = "A";
        assertEquals(expSecondsInHex, secondsInHex);        
    }
    
    @Test
    public void secondsInHexOfTimestamp_10LongSeconds_AHexSeconds() {      
        String secondsInHex = TimeUtils.secondsInHexOf(new Timestamp(10000));
        String expSecondsInHex = "A";
        assertEquals(expSecondsInHex, secondsInHex);        
    }
    
    @Test
    public void secondsInHexOfAddingSecondsInHexAndSecondsInHex_5And5_AHexSeconds() {      
        String secondsInHex = TimeUtils.secondsInHexOfAdding("5", "5");
        String expSecondsInHex = "A";
        assertEquals(expSecondsInHex, secondsInHex);        
    }
    
    @Test
    public void secondsInHexOfSubtracting_secondTimeStampBeforeFirst_0() {        
        assertEquals("0",TimeUtils.secondsInHexOfSubtracting(TimeUtils.timestampOf(1000),TimeUtils.timestampOf(2000)));        
    }
    
    //Tests of methods which return a Timestamp:
    
}
