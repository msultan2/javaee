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
 * Created on 14-May-2015 01:43 PM
 */
package com.ssl.bluetruth.emitter2converter.utilsfortesting;

import java.util.Arrays;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import com.ssl.bluetruth.emitter2converter.exceptions.DeadOutStationWithNothingElseToReportException;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;
import com.ssl.bluetruth.emitter2converter.raw.RawDeviceData;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;

/**
 *
 * @author josetrujillo-brenes
 */
public class AssertUtils {
    
    private final static Logger logger = Logger.getLogger(AssertUtils.class);
    
    public static void assertCongestionReport(RawDeviceData rawDeviceData, String idOutStation, String bins, String queuePresent)
            throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        assertCongestionReport(null, rawDeviceData, idOutStation, bins, queuePresent);
    }
    
    public static void assertCongestionReport(String assertMesage, RawDeviceData rawDeviceData, String idOutStation, String bins, String queuePresent)
            throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {
        String expectedCongestionReport = idOutStation+","+TimeUtils.currentSecondsInHex()+","+bins+","+queuePresent+",0";
        String actualCongestionReport = rawDeviceData.getReportOfOutStation(ReportEnum.CONGESTION, idOutStation, TimeUtils.currentTimestamp());        
        assertEquals(assertMesage, expectedCongestionReport, actualCongestionReport);
    }
    
    public static void assertStatisticsBriefReport(String assertMessage, RawDeviceData rawDeviceData, String idOutStation, String partOfExpectedStatisticsReports) throws DeadOutStationWithNothingElseToReportException, InvalidConfigurationException {        
        String expectedStatisticsReports = idOutStation+","+partOfExpectedStatisticsReports+",0";
        String actualStatisticsReports = rawDeviceData.getReportOfOutStation(ReportEnum.STATISTICS_BRIEF, idOutStation, TimeUtils.currentTimestamp());
        String sortedStatisticsReports = sortDevicesInStatisticsReport(actualStatisticsReports);
        assertEquals(assertMessage, expectedStatisticsReports, sortedStatisticsReports); 
    }
    
    public static String sortDevicesInStatisticsReport(String statisticsReports) {
        String[] elementsStatisticsReports = statisticsReports.split(",");
        int positionFirstDevice = 3;
        int positionLastDevice = elementsStatisticsReports.length-1;
        Arrays.sort(elementsStatisticsReports, positionFirstDevice, positionLastDevice);
        StringBuilder sb = new StringBuilder();
        for(String element: elementsStatisticsReports) {
            sb.append(element).append(",");
        }
        String sortedStatisticsReportOneExtraComa = sb.toString();
        return sortedStatisticsReportOneExtraComa.substring(0, sortedStatisticsReportOneExtraComa.length()-1);
    }
    
}
