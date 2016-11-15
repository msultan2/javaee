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
 * Created on 06-May-2015 09:32 AM
 */
package com.ssl.bluetruth.emitter2converter.utils;

import ssl.bluetruth.emitter2converter.configuration.ThreadLocalConfigurationManager;
import java.io.IOException;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;
import com.ssl.bluetruth.emitter2converter.exceptions.UnableToSendReportException;
import com.ssl.bluetruth.emitter2converter.schedules.ReportEnum;

/**
 * Utility class that encapsulates Apache HttpComponents third party library
 * This class allows to send a report using POST
 * Despite its a utility class, it doesn't have static methods. This means in creates a new instance each time it's use.
 * @author josetrujillo-brenes
 */
public class HttpReport {
    
    private final Logger logger = LogManager.getLogger(getClass());
    
    private final String idOutStation;
    private final ReportEnum report;
    
    public HttpReport(String idOutStation, ReportEnum report) {
        this.idOutStation = idOutStation;
        this.report = report;        
    }
    
    public void sendReportUsingPOST(String body) throws InvalidConfigurationException, UnableToSendReportException {   
        String url = null;        
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {  
            url = ThreadLocalConfigurationManager.get().get(idOutStation, report.getPropertyNameForUrl());
            if(logger.isDebugEnabled()) {
                logger.debug(report.getName()+": '"+body+"' from "+idOutStation+" is going to be sent to '"+url+"'");
            }
            HttpPost httppost = new HttpPost(url);            
            httppost.setEntity(new StringEntity(body, ContentType.create("plain/text", Consts.UTF_8)));            
            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                StatusLine responseStatus = response.getStatusLine();
                if (HttpStatus.SC_OK == responseStatus.getStatusCode()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Response from "+report.getName()+": '"+body+"' from "+idOutStation+" sent to '"+url+"': "+responseStatus);
                    }
                } else {
                    logger.warn("Response from "+report.getName()+": '"+body+"' from "+idOutStation+" sent to '"+url+"': "+responseStatus);
                }
            }
        }
        catch(IOException ioex) {
            String message = null;
            if(url == null || url.equals("")){
                message = "Unable to send "+report.getName()+": '"+body+"' from "+idOutStation+" to '"+url+"'. Cause: URL is NULL/Empty";            
            }else{
                message = "Unable to send "+report.getName()+": '"+body+"' from "+idOutStation+" to '"+url+"'. Cause: "+ioex.getLocalizedMessage();            
            }
            throw new UnableToSendReportException(message, ioex);
        }
    }
    
}
