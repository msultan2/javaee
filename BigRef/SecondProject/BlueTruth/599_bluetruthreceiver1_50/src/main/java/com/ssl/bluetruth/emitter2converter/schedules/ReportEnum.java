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

package com.ssl.bluetruth.emitter2converter.schedules;

import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;

/**
 * Enum used to identify which report should be created, 
 * with  which interval in seconds should be schedule 
 * and to which url should be send 
 * @author jtrujillo-brenes
 */
public enum ReportEnum {
    STATISTICS_FULL("FullStatisticsReport", ConfigurationManager.STATISTICS_REPORT_PERIOD_IN_SECONDS, ConfigurationManager.URL_STATISTICS_REPORTS), 
    STATISTICS_BRIEF("BriefStatisticsReport", ConfigurationManager.STATISTICS_REPORT_PERIOD_IN_SECONDS, ConfigurationManager.URL_STATISTICS_REPORTS), 
    CONGESTION("CongestionReport", ConfigurationManager.CONGESTION_REPORT_PERIOD_IN_SECONDS, ConfigurationManager.URL_CONGESTION_REPORTS);
    
    private final String name;
    private final String propertyNameForReportPeriod;
    private final String propertyNameForUrl;

    private ReportEnum(String name, String propertyNameForPeriod, String propertyNameForUrl) {
        this.name = name;
        this.propertyNameForReportPeriod = propertyNameForPeriod;
        this.propertyNameForUrl = propertyNameForUrl;
    }

    public String getName() {return name;}
    public String getPropertyNameForPeriod() {return propertyNameForReportPeriod;}
    public String getPropertyNameForUrl() {return propertyNameForUrl;}
    
}
