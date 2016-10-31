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
 * Created on 11-Aug-2015 02:18 PM
 */
package ssl.bluetruth.servlet.update;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author josetrujillo-brenes
 */
public abstract class AbstractSpanInformationManager extends HttpServlet {

    public static final String DATE_TIME_FORMAT_STRING = "yyyy-MM-dd H:mm:ss";
    public static final String USER_TIMEZONE_PARAMETER = "user_timezone";
    public static final String EVENT_START_TIMESTAMP_PARAMETER = "startTimestamp";
    public static final String EVENT_END_TIMESTAMP_PARAMETER = "endTimestamp";
    public static final String INCIDENT_START_TIMESTAMP_PARAMETER = "startTimestamp1";
    public static final String INCIDENT_END_TIMESTAMP_PARAMETER = "endTimestamp1";
    
    private final Logger logger = LogManager.getLogger(getClass());
    
    protected Timestamp getTimestampInUtc(HttpServletRequest request, String parameterName) {
        String userTimezone = (String) request.getSession().getAttribute(USER_TIMEZONE_PARAMETER);
        DateTimeFormatter zoneDateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_STRING).withZone(ZoneId.of(userTimezone));
        ZonedDateTime clientZonedDateTime = ZonedDateTime.parse(request.getParameter(parameterName), zoneDateTimeFormatter);
        ZonedDateTime serverZonedDateTime = clientZonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        Timestamp timeStamp = Timestamp.valueOf(serverZonedDateTime.toLocalDateTime());        
        return timeStamp;
    }
}
