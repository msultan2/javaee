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
 * Created on 10-Aug-2015 02:19 PM
 */
package ssl.bluetruth.servlet.update;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.springframework.mock.web.MockHttpServletRequest;
import static ssl.bluetruth.servlet.update.SpanEventsInformationManager.*;

/**
 *
 * @author josetrujillo-brenes
 */
public class SpanEventsInformationManagerTest {
    
    public static final String TIMEZONE_LONDON = "Europe/London";
    public static final String TIMEZONE_TASHKENT = "Asia/Tashkent";
    
    public SpanEventsInformationManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void getTimestampInUtc_withTimeFromTashkentSummer_convertsCorrectly() {
        String time = "2015-08-19 12:00:00";
        String expectedUtcTime = "2015-08-19 07:00:00";
        assertTimeToUtc(TIMEZONE_TASHKENT, time, expectedUtcTime);
    }
    
    @Test
    public void getTimestampInUtc_withTimeFromTashkentWinter_convertsCorrectly() {
        String time = "2015-02-19 12:00:00";
        String expectedUtcTime = "2015-02-19 07:00:00";
        assertTimeToUtc(TIMEZONE_TASHKENT, time, expectedUtcTime);
    }
    
    @Test
    public void getTimestampInUtc_withTimeFromLondonSummer_convertsCorrectly() {
        String time = "2015-08-19 12:00:00";
        String expectedUtcTime = "2015-08-19 11:00:00";
        assertTimeToUtc(TIMEZONE_LONDON, time, expectedUtcTime);
    }
    
    @Test
    public void getTimestampInUtc_withTimeFromLondonWinter_convertsCorrectly() {
        String time = "2015-02-19 12:00:00";
        String expectedUtcTime = "2015-02-19 12:00:00";
        assertTimeToUtc(TIMEZONE_LONDON, time, expectedUtcTime);
    }
    
    public void assertTimeToUtc(String timeZone, String time, String expectedUtcTime){
        try {
            SpanEventsInformationManager spanEventsInformationManager = new SpanEventsInformationManager();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.getSession().setAttribute(USER_TIMEZONE_PARAMETER, timeZone);
            request.addParameter(EVENT_START_TIMESTAMP_PARAMETER, time);
            Timestamp expectedTimestamp = new Timestamp(new SimpleDateFormat(DATE_TIME_FORMAT_STRING).parse(expectedUtcTime).getTime());
            Timestamp actualTimestamp = spanEventsInformationManager.getTimestampInUtc(request, EVENT_START_TIMESTAMP_PARAMETER);
            assertThat(actualTimestamp, is(expectedTimestamp));
        } catch (ParseException pex) {
            fail("ParseException: "+pex.getLocalizedMessage());
        }        
    }    
    
    @Test @Ignore()
    public void learningToWorkWith_LocalDateTime_and_ZonedDateTime() {
        LocalDateTime currentLocalDateTimeInLondon = LocalDateTime.now(ZoneId.of(TIMEZONE_LONDON));
        ZonedDateTime currentZonedDateTimeInLondon1 = ZonedDateTime.of(currentLocalDateTimeInLondon, ZoneId.of(TIMEZONE_LONDON));  
        ZonedDateTime currentZonedDateTimeInLondon2 = currentLocalDateTimeInLondon.atZone(ZoneId.of(TIMEZONE_LONDON));
        LocalDateTime currentLocalDateTimeInUtc = LocalDateTime.now(Clock.systemUTC());
        
        System.out.println("currentLocalDateTimeInUtc: "+currentLocalDateTimeInUtc);        
        System.out.println("currentLocalDateTimeInLondon: "+currentLocalDateTimeInLondon);
        System.out.println("currentZonedDateTimeInLondon1: "+currentZonedDateTimeInLondon1);
        System.out.println("currentZonedDateTimeInLondon2: "+currentZonedDateTimeInLondon2);
        
        String stringDate = "2015-08-19 12:00:00";
        DateTimeFormatter ZONE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_STRING).withZone(ZoneId.of(TIMEZONE_LONDON));
        ZonedDateTime zonedDateTimeInLondon = ZonedDateTime.parse(stringDate, ZONE_DATE_TIME_FORMAT);
        LocalDateTime localDateTimeInLondon = zonedDateTimeInLondon.toLocalDateTime();
        ZonedDateTime zonedDateTimeInTashkent = zonedDateTimeInLondon.withZoneSameInstant(ZoneId.of(TIMEZONE_TASHKENT));
        LocalDateTime localDateTimeInTashkent = zonedDateTimeInTashkent.toLocalDateTime();
        ZonedDateTime zonedDateTimeInUtc = zonedDateTimeInLondon.withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime localDateTimeInUtc = zonedDateTimeInUtc.toLocalDateTime();
        
        System.out.println("zonedDateTimeInLondon: "+zonedDateTimeInLondon);
        System.out.println("localDateTimeInLondon: "+localDateTimeInLondon);
        System.out.println("zonedDateTimeInTashkent: "+zonedDateTimeInTashkent);
        System.out.println("localDateTimeInTashkent: "+localDateTimeInTashkent);
        System.out.println("zonedDateTimeInUtc: "+zonedDateTimeInUtc);
        System.out.println("localDateTimeInUtc: "+localDateTimeInUtc);
        
        Timestamp timestampInLondon = Timestamp.valueOf(localDateTimeInLondon);
        Timestamp timestampInTashkent = Timestamp.valueOf(localDateTimeInTashkent);
        Timestamp timestampInUtc = Timestamp.valueOf(localDateTimeInUtc);
        
        System.out.println("timestampInLondon: "+timestampInLondon);
        System.out.println("timestampInTashkent: "+timestampInTashkent);
        System.out.println("timestampInUtc: "+timestampInUtc);
    }
}
