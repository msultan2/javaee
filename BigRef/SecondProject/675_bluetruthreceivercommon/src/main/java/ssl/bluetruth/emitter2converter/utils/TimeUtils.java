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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * Utility class that helps to create, add or convert time in different formats, 
 * like Timestamp, seconds (long) and seconds in hex (String)
 * @author jtrujillo-brenes
 */
public class TimeUtils {
    
    private static final Logger logger = Logger.getLogger(TimeUtils.class);
    
    /**
     * Private constructor to avoid the creation of instances
     */    
    private TimeUtils() {
    }
    
    public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_ZONE_UTC = "UTC";
    
    public static String currentDateInFormat(String format, String timeZone) {
        SimpleDateFormat simpleDateFormat;
        if (format != null) {
            simpleDateFormat = new SimpleDateFormat(format);
        } else {
            throw new NullPointerException("Can't obtain the current date without specifying a format");
        } 
        if (timeZone != null) {
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        } else {
            throw new NullPointerException("Can't obtain the current date without specifying a time zone");
        } 
        return simpleDateFormat.format(new Date(TimeUnit.SECONDS.toMillis(currentSeconds())));  
    }
       
    public static String currentDateInFormatAdding(String format, String timeZone, int seconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(TimeUnit.SECONDS.toMillis(currentSeconds())));
        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        calendar.add(Calendar.SECOND, seconds);
        return simpleDateFormat.format(calendar.getTime());  
    }    
       
    public static long currentSeconds() {
        long curretSeconds = secondsOf(Calendar.getInstance().getTimeInMillis());
        return curretSeconds;  
    }
    
    public static long secondsOf(Timestamp timestamp) {  
        if(timestamp == null) {
            throw new NullPointerException("Can't convert null timestamp into seconds");
        }
        long milliseconds = timestamp.getTime();
        long seconds = secondsOf(milliseconds);
        return seconds;
    }
    
    public static long secondsOf(String secondsInHex) {
        if(secondsInHex != null) {
            if(!secondsInHex.startsWith("0x")) {
                secondsInHex = "0x"+secondsInHex;
            }
            return Long.decode(secondsInHex);
        } else {
            throw new NullPointerException("Unable to convert a null String. It should contain a Hexadecimal number");
        }        
    }  
    
    public static long secondsOf(long milliseconds) {
        return TimeUnit.MILLISECONDS.toSeconds(milliseconds);     
    }
    
    public static long secondsSince(long seconds) {
        long curretSeconds = currentSeconds();
        return curretSeconds - seconds;
    }
    
    public static long secondsSince(Timestamp timestamp) { 
        long secondsSince = secondsSince(secondsOf(timestamp));       
        return secondsSince;
    }
     
    public static String currentSecondsInHex() {
        return secondsInHexOf(currentTimestamp());
    }
    
    public static String secondsInHexOf(long seconds) {        
        return Long.toHexString(seconds).toUpperCase();
    }
    
    public static String secondsInHexOf(Timestamp timestamp) { 
        if(timestamp != null) {            
            return (Long.toHexString(secondsOf(timestamp)).toUpperCase());
        } else {
            return null;
        }        
    }

    public static String secondsInHexOfAdding(String secondsInHex1, String secondsInHex2) {
        return secondsInHexOf(timestampOfAdding(secondsInHex1, secondsInHex2));
    }
    
    public static String secondsInHexOfSubtracting(String secondsInHex1, String secondsInHex2) {
        return secondsInHexOf(timestampOfSubtracting(secondsInHex1, secondsInHex2));
    }
    
    public static String secondsInHexOfSubtracting(Timestamp timestamp1, Timestamp timestamp2) {
        if(timestamp1.after(timestamp2)) {
            return secondsInHexOf(timestampOfSubtracting(timestamp1, timestamp2));
        } else {
            return "0";
        }        
    }
    
    public static String secondsInHexOfSubtracting(long seconds1, long seconds2) {
        return secondsInHexOf(timestampOfSubtracting(seconds1, seconds2));
    }
    
     public static String halfSecondsInHexOfSubtracting(String secondsInHex1, String secondsInHex2) {
        return secondsInHexOf(halfTimestampOfSubtracting(secondsInHex1, secondsInHex2));
    }
     
    public static Timestamp currentTimestamp() {
        return timestampOf(currentSeconds());
    }
    
     public static Timestamp currentTimestampAdding(long seconds) {
        return timestampOf(currentSeconds()+seconds);
    }
    
    public static Timestamp timestampOf(long seconds) {
        return new Timestamp(TimeUnit.SECONDS.toMillis(seconds));
    }
    
    public static Timestamp timestampOf(String secondsInHex) {        
        return timestampOf(secondsOf(secondsInHex));        
    }
    
    public static Timestamp timestampOf(String date, String format, String timeZone) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return new Timestamp(dateFormat.parse(date).getTime());
    }
    
    public static Timestamp timestampOfAdding(long seconds1, long seconds2) {
        return timestampOf(seconds1 + seconds2);
    }
    
    public static Timestamp timestampOfAdding(Timestamp timestamp, long seconds) {
        return timestampOfAdding(secondsOf(timestamp), seconds);
    }
    
    public static Timestamp timestampOfAdding(Timestamp timestamp, String secondsInHex) {
        return timestampOfAdding(secondsOf(timestamp), secondsOf(secondsInHex));
    }
    
    public static Timestamp timestampOfAdding(String secondsInHex1, String secondsInHex2) {
        return timestampOfAdding(secondsOf(secondsInHex1), secondsOf(secondsInHex2));
    }
    
    public static Timestamp timestampOfSubtracting(long seconds1, long seconds2) {
        return timestampOf(seconds1 - seconds2);
    }
    
    public static Timestamp timestampOfSubtracting(Timestamp timestamp, long seconds) {
        return timestampOfSubtracting(secondsOf(timestamp), seconds);
    }
    
    public static Timestamp timestampOfSubtracting(Timestamp timestamp, String secondsInHex) {
        return timestampOfSubtracting(secondsOf(timestamp), secondsOf(secondsInHex));
    }
    
    public static Timestamp timestampOfSubtracting(Timestamp timestamp1, Timestamp timestamp2) {
        return timestampOfSubtracting(secondsOf(timestamp1), secondsOf(timestamp2));
    }
    
    public static Timestamp timestampOfSubtracting(String secondsInHex1, String secondsInHex2) {
        return timestampOfSubtracting(secondsOf(secondsInHex1), secondsOf(secondsInHex2));
    }
    
    public static Timestamp halfTimestampOfSubtracting(long seconds1, long seconds2) {
        return timestampOf((seconds1 - seconds2)/2);
    }
    
    public static Timestamp halfTimestampOfSubtracting(Timestamp timestamp, long seconds) {
        return halfTimestampOfSubtracting(secondsOf(timestamp), seconds);
    }
    
    public static Timestamp halfTimestampOfSubtracting(Timestamp timestamp, String secondsInHex) {
        return halfTimestampOfSubtracting(secondsOf(timestamp), secondsOf(secondsInHex));
    }
    
    public static Timestamp halfTimestampOfSubtracting(String secondsInHex1, String secondsInHex2) {
        return halfTimestampOfSubtracting(secondsOf(secondsInHex1), secondsOf(secondsInHex2));
    }
    
    public static long minutesOf(long seconds) {
        return TimeUnit.SECONDS.toMinutes(seconds);
    }
    
}
