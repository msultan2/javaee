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

import org.junit.Assert;
import org.junit.Test;

public class DateTimeHelperTest {

    @Test
    public void testGetCurrentWinterTimestampAtEuropeLondon() {
        System.setProperty("user.timezone", "Europe/London");
        TimeSource winterMock = new TimeSourceWinterMockEuropeLondon();
        DateTimeHelper tzHelper = new DateTimeHelper(winterMock);

        String now = tzHelper.getCurrentLocalTimestamp();
        Assert.assertTrue(now.equalsIgnoreCase("2016-03-01 12:30:00"));
    }

    @Test
    public void testGetCurrentWinterTimestampAtUTC() {
        System.setProperty("user.timezone", "ETC/UTC");
        TimeSource winterMock = new TimeSourceWinterMockUTC();
        DateTimeHelper tzHelper = new DateTimeHelper(winterMock);

        String now = tzHelper.getCurrentLocalTimestamp();
        Assert.assertTrue(now.equalsIgnoreCase("2016-03-01 12:30:00"));
    }

    @Test
    public void testGetCurrentSummerTimestampAtEuropeLondon() {
        System.setProperty("user.timezone", "Europe/London");
        TimeSource summerMock = new TimeSourceSummerMockEuropeLondon();
        DateTimeHelper tzHelper = new DateTimeHelper(summerMock);

        String now = tzHelper.getCurrentLocalTimestamp();
        Assert.assertTrue(now.equalsIgnoreCase("2016-05-01 12:30:00"));
    }

    @Test
    public void testGetCurrentSummerTimestampAtUTC() {
        System.setProperty("user.timezone", "ETC/UTC");
        TimeSource summerMock = new TimeSourceSummerMockUTC();
        DateTimeHelper tzHelper = new DateTimeHelper(summerMock);

        String now = tzHelper.getCurrentLocalTimestamp();
        Assert.assertTrue(now.equalsIgnoreCase("2016-05-01 13:30:00"));
    }

    @Test
    public void testGetYesterdayWinterTimestampAtEuropeLondon() {
        System.setProperty("user.timezone", "Europe/London");
        TimeSource winterMock = new TimeSourceWinterMockEuropeLondon();
        DateTimeHelper tzHelper = new DateTimeHelper(winterMock);

        String yesterday = tzHelper.getYesterdayTimestamp();
        Assert.assertTrue(yesterday.equalsIgnoreCase("2016-02-29 12:30:00"));
    }

    @Test
    public void testGetYesterdayWinterTimestampAtUTC() {
        System.setProperty("user.timezone", "ETC/UTC");
        TimeSource winterMock = new TimeSourceWinterMockUTC();
        DateTimeHelper tzHelper = new DateTimeHelper(winterMock);

        String yesterday = tzHelper.getYesterdayTimestamp();
        Assert.assertTrue(yesterday.equalsIgnoreCase("2016-02-29 12:30:00"));
    }

    @Test
    public void testGetYesterdaySummerTimestampAtEuropeLondon() {
        System.setProperty("user.timezone", "Europe/London");
        TimeSource summerMock = new TimeSourceSummerMockEuropeLondon();
        DateTimeHelper tzHelper = new DateTimeHelper(summerMock);

        String yesterday = tzHelper.getYesterdayTimestamp();
        Assert.assertTrue(yesterday.equalsIgnoreCase("2016-04-30 12:30:00"));
    }

    @Test
    public void testGetYesterdaySummerTimestampAtUTC() {
        System.setProperty("user.timezone", "ETC/UTC");
        TimeSource summerMock = new TimeSourceSummerMockUTC();
        DateTimeHelper tzHelper = new DateTimeHelper(summerMock);

        String yesterday = tzHelper.getYesterdayTimestamp();
        Assert.assertTrue(yesterday.equalsIgnoreCase("2016-04-30 13:30:00"));
    }

}