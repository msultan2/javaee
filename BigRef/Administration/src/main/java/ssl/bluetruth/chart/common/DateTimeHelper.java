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
 * Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 */
package ssl.bluetruth.chart.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public final class DateTimeHelper {

    private final String timeZone = System.getProperty("user.timezone");
    private static final Logger LOGGER = LogManager.getLogger(DateTimeHelper.class.getName());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final TimeSource timeSource;
    public static final int OFFSET_ONE_HOUR = 1;
    public static final String EUROPE_LONDON = "Europe/London";
    public static final String UTC = "ETC/UTC";

    public DateTimeHelper(TimeSource timeSource) {
        this.timeSource = timeSource;
    }

    public String getCurrentLocalTimestamp() {
        String londonNow = getLondonTime().format(formatter);
        LOGGER.debug("now = " + londonNow);
        return londonNow;
    }

    public String getYesterdayTimestamp() {
        String londonYesterday = getLondonTime().minusDays(1).format(formatter);
        LOGGER.debug("Yesterday = " + londonYesterday);
        return londonYesterday;
    }

    private LocalDateTime getLondonTime() {
        LocalDateTime systemCurrentDateTime = timeSource.getCurrentLocalDateTime();

        Date systemDateTime = Date.from(systemCurrentDateTime.atZone(ZoneId.systemDefault()).toInstant());

        if (timeZone.trim().equalsIgnoreCase(UTC)
                && TimeZone.getTimeZone(EUROPE_LONDON).inDaylightTime(systemDateTime))
            return systemCurrentDateTime.plusHours(OFFSET_ONE_HOUR);
        else
            return systemCurrentDateTime;
    }
}
