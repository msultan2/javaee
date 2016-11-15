/*
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
 * Copyright 2015 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.jwt;

import com.ssl.rmas.exceptionhandler.ExceptionToMessagesMapper;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ssl.rmas.security.MissingTotpKeyAuthenticatorException;
import com.ssl.rmas.utils.ErrorMessage;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.authentication.BadCredentialsException;

@Component
public class JWTAuthenticationErrorHandler implements AuthenticationFailureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthenticationErrorHandler.class);
    ExceptionToMessagesMapper exceptionToMessagesMapper;
    
    @Autowired 
    public void setExceptionToMessagesMapper(ExceptionToMessagesMapper exceptionToMessagesMapper){
        this.exceptionToMessagesMapper =  exceptionToMessagesMapper;
    }
    
    @Autowired
    private JSONExceptionHandlerUtils exceptionUtils;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) {
        LOGGER.debug("Authentication failed", exception);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        Set<ErrorMessage> errorMessages = null;
        if(exception instanceof MissingTotpKeyAuthenticatorException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else if (exception instanceof BadCredentialsException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else if(exception instanceof AccountExpiredException || exception instanceof LockedException) {
            errorMessages = exceptionToMessagesMapper.getErrorMessages(exception);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        try (PrintWriter writer = response.getWriter()) {
            if (CollectionUtils.isEmpty(errorMessages)) {
                writer.println(exceptionUtils.exceptionToJsonString(exception)); 
            } else {
                writer.println(exceptionUtils.errorMessageSetToJsonString(errorMessages));
            }
        } catch (IOException ex) {
            LOGGER.warn("Response body could not be written", ex);
        }
    }
}
