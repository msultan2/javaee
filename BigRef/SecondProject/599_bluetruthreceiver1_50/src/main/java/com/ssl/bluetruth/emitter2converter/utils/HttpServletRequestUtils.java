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
 * Created on 14-May-2015 12:08 PM
 */
package com.ssl.bluetruth.emitter2converter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import com.ssl.bluetruth.emitter2converter.exceptions.RequestParametersException;

/**
 * Utility class that provides methods to work with a HttpServletRequest object.
 * This methods help to get the parameters and other information from the HttpServletRequest
 * @author josetrujillo-brenes
 */
public class HttpServletRequestUtils {
    
    public String getInfo(HttpServletRequest request) {
        return "Request from "+request.getRemoteAddr()
                +" to "+request.getRequestURL()
                +" with parameters {"+getAllParameters(request)
                +"} and body {"+getBody(request)+"}";
    }
    
    public int getInt(HttpServletRequest request, String parameterName) throws RequestParametersException {
        String stringValue = getString(request, parameterName);
        try {            
            return Integer.parseInt(stringValue);
        } catch(NumberFormatException nfex) {
            throw new RequestParametersException("The expected parameter '"+parameterName+"' should be int but is '"+stringValue+"' in the request: "+getInfo(request), nfex);
        }
    }
    
    public String getString(HttpServletRequest request, String parameterName) throws RequestParametersException {
        String stringValue = request.getParameter(parameterName);
        if(stringValue == null) {
            throw new RequestParametersException("The expected parameter '"+parameterName+"' is missing in the request: "+getInfo(request));
        }
        return stringValue;
    }
    
    public String getStringIfPossible(HttpServletRequest request, String parameterName) {
        return request.getParameter(parameterName);
    }
    
    public String getAllParameters(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        StringBuilder allParameters = new StringBuilder();
        while(parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String parameterValue = request.getParameter(parameterName);
            allParameters.append(parameterName).append(": '").append(parameterValue).append("' ");
        }        
        return allParameters.toString();
    }
    
    public String getBody(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder body = new StringBuilder();
            String line;
            if (reader != null) {
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
            return body.toString();
        } catch (IOException ioex) {
            return "";
        }
    }
    
}
