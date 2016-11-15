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
 * Created on 05-May-2015 07:52 AM
 */
package ssl.bluetruth.emitter2converter.utilsfortesting;

import java.lang.reflect.Method;
import org.powermock.api.easymock.PowerMock;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;

/**
 * This class uses PowerMock to substitute the method currentSeconds from TimeUtils with getCurrentSeconds from TimeUtilsFake
 * currentSeconds is use by many other static methods like currentTimestamp or secondsSince. This means that the behaviour
 * of TimeUtils changes considerably.  
 * 
 * To use this class you have to include in the test class:
 *      @RunWith(PowerMockRunner.class)
 *      @PrepareForTest( { ssl.bluetruth.emitter2converter.utils.TimeUtils }) 
 * 
 * @author josetrujillo-brenes
 */
public class PowerMockTimeUtils {

    public static void replaceRealTimeWithFakeTime() {
        replaceRealTimeWithFakeTime(TimeUtils.currentSeconds());
    }

    public static void replaceRealTimeWithFakeTime(long seconds) {
        TimeUtilsFake.setCurrentSeconds(seconds);
        PowerMock.replace(getMethodCurrentSecondsFromClassTimeUtils()).with(getMethodGetCurrentSecondsFromClassTimeUtilsFake());
    }

    private static Method getMethodCurrentSecondsFromClassTimeUtils() {
        return PowerMock.method(TimeUtils.class, "currentSeconds");
    }

    private static Method getMethodGetCurrentSecondsFromClassTimeUtilsFake() {
        return PowerMock.method(TimeUtilsFake.class, "getCurrentSeconds");
    }

}
