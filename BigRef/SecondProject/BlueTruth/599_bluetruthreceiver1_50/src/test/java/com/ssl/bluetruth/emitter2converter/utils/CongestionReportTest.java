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
 * Created on 09-Jul-2015 04:11 PM
 */
package com.ssl.bluetruth.emitter2converter.utils;

import com.ssl.bluetruth.emitter2converter.utils.CongestionReport;
import java.text.ParseException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;

/**
 *
 * @author josetrujillo-brenes
 */
public class CongestionReportTest {

    @Test
    public void getV4String_occupancyReportWithOutQueueStartOrQueueEnd_noQueue() throws ParseException {      
        CongestionReport congestionReport = new CongestionReport("detectorId");
        congestionReport.setFreeBin(1);
        congestionReport.setModerateBin(2);
        congestionReport.setSlowBin(3);
        congestionReport.setVerySlowBin(4);
        congestionReport.setStationaryBin(5);
        congestionReport.setTimeReport(TimeUtils.timestampOf("1982-08-19 22:30:00", TimeUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS, TimeUtils.TIME_ZONE_UTC));
        assertEquals("detectorId,17C2D3E8,1:2:3:4:5,0,0", congestionReport.getV4String());
    }
}
