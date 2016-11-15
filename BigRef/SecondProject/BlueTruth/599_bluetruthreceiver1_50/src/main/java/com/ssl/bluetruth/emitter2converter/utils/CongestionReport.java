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

package com.ssl.bluetruth.emitter2converter.utils;

import java.sql.Timestamp;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.utils.TimeUtils;

/**
 * Class used to create or translate a congestion report using de setters and getters
 * @author jtrujillo-brenes
 */
public class CongestionReport {
    
    public static final String QUEUE_PRESENT_VALUE_QUEUE_PRESENT = "9";
    public static final String QUEUE_PRESENT_VALUE_NO_QUEUE = "0";
    public static final String QUEUE_PRESENT_VALUE_NOT_READY = "FE";
    public static final String QUEUE_PRESENT_VALUE_FAULTY = "FF";
    
    public static final String RND_VALUE_NOT_USED = "0";
    
    private final Logger logger = LogManager.getLogger(getClass());
    
    private String detectorId;
    private Timestamp timeReport;
    private Integer freeBin;
    private Integer moderateBin;
    private Integer slowBin;
    private Integer verySlowBin;
    private Integer stationaryBin;
    private Timestamp queueStart;
    private Timestamp queueEnd;
    private String queuePresent;
    
    public CongestionReport(String detectorId) {
        this.detectorId = detectorId;
        freeBin = 0;
        moderateBin = 0;
        slowBin = 0;
        verySlowBin = 0;
        stationaryBin = 0;
    }
    
    public String getV3String() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=<").append(getDetectorId()).append(">");
        sb.append("&t=<").append(getTimeReport()).append(">");
        sb.append("&f=<").append(getFreeBin()).append(">");
        sb.append("&m=<").append(getModerateBin()).append(">");
        sb.append("&s=<").append(getSlowBin()).append(">");
        sb.append("&vs=<").append(getVerySlowBin()).append(">");
        sb.append("&st=<").append(getStationaryBin()).append(">");
        if(getQueueStart() != null) {
            sb.append("&qs=<").append(getQueueStart()).append(">");
        } else {            
            if(getQueueEnd() != null) {
                sb.append("&qe=<").append(getQueueEnd()).append(">"); 
            } 
        }
        return sb.toString();
    }
    
    public String getV4String() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDetectorId()).append(",");
        sb.append(getTimeReportInHex()).append(",");
        sb.append(getFreeBin()).append(":");
        sb.append(getModerateBin()).append(":");
        sb.append(getSlowBin()).append(":");
        sb.append(getVerySlowBin()).append(":");
        sb.append(getStationaryBin()).append(",");
        sb.append(getQueuePresent()).append(",");
        sb.append(getRND());
        return sb.toString();
    } 
    
    private String getTimeReportInHex() {        
        return TimeUtils.secondsInHexOf(getTimeReport());    
    } 
    
    private String getRND() {
        return RND_VALUE_NOT_USED; 
    }
    
    public String getDetectorId() {
        return detectorId;
    }

    public void setDetectorId(String detectorId) {
        this.detectorId = detectorId;
    }

    public Timestamp getTimeReport() {
        return timeReport;
    }    

    public void setTimeReport(Timestamp timeReport) {
        this.timeReport = timeReport;
    }

    public Integer getFreeBin() {
        return freeBin;
    }
    
    public Integer getFromFreeToStationaryBin() {
        return getFreeBin() + getFromModerateToStationaryBin();
    }
    
    public void increaseFreeBin() {
        freeBin++;
    }

    public void setFreeBin(Integer freeBin) {
        this.freeBin = freeBin;
    }

    public Integer getModerateBin() {
        return moderateBin;
    }
    
    public Integer getFromModerateToStationaryBin() {
        return getModerateBin() + getFromSlowToStationaryBin();
    }
    
    public void increaseModerateBin() {
        moderateBin++;
    }

    public void setModerateBin(Integer moderateBin) {
        this.moderateBin = moderateBin;
    }

    public Integer getSlowBin() {
        return slowBin;
    }
    
    public Integer getFromSlowToStationaryBin() {
        return getSlowBin() + getFromVerySlowToStationaryBin();
    }
    
    public void increaseSlowBin() {
        slowBin++;
    }

    public void setSlowBin(Integer slowBin) {
        this.slowBin = slowBin;
    }    

    public Integer getVerySlowBin() {
        return verySlowBin;
    }
    
    public Integer getFromVerySlowToStationaryBin() {
        return getVerySlowBin() + getStationaryBin();
    }
    
    public void increaseVerySlowBin() {
        verySlowBin++;
    }

    public void setVerySlowBin(Integer verySlowBin) {
        this.verySlowBin = verySlowBin;
    }

    public Integer getStationaryBin() {
        return stationaryBin;
    }
    
    public void increaseStationaryBin() {
        stationaryBin++;
    }

    public void setStationaryBin(Integer stationaryBin) {
        this.stationaryBin = stationaryBin;
    }

    public Timestamp getQueueStart() {
        return queueStart;
    }

    public void setQueueStartAndQueuePresent(Timestamp queueStart) {
        if (queueStart != null) {
            setQueuePresent(QUEUE_PRESENT_VALUE_QUEUE_PRESENT);
            this.queueStart = queueStart;
        }
    }

    public Timestamp getQueueEnd() {
        return queueEnd;
    }

    public void setQueueEndAndQueuePresent(Timestamp queueEnd) {
        if (queueEnd != null) {
            setQueuePresent(QUEUE_PRESENT_VALUE_NO_QUEUE);
            this.queueEnd = queueEnd;
        }        
    }
    
    public String getQueuePresent() {
        if (queuePresent != null) {
            return queuePresent;
        } else {
            return QUEUE_PRESENT_VALUE_NO_QUEUE;
        }
    }

    public void setQueuePresent(String queuePresent) {
        this.queuePresent = queuePresent;
    }
    
    public boolean isEmpty() {
        return (freeBin==0 && moderateBin==0 && slowBin==0 && verySlowBin==0 && stationaryBin==0);
    }
}
