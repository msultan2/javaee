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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.init;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.ssl.rmas.exceptionhandler.ExceptionToHttpResponseMapper;

@ControllerAdvice
public class GlobalExceptionHandlerControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandlerControllerAdvice.class);

    private ExceptionToHttpResponseMapper exceptionToHttpResponseMapper;

    @Autowired
    public void setExceptionToHttpResponseMapper(ExceptionToHttpResponseMapper exceptionToHttpResponseMapper) {
        this.exceptionToHttpResponseMapper = exceptionToHttpResponseMapper;
    }
 
    @ExceptionHandler(Exception.class)
    public void handleUnhandledException(Exception e, HttpServletResponse response) {
        try {
            int httpStatusCode = exceptionToHttpResponseMapper.getStatusCode(e);
            String httpBody = exceptionToHttpResponseMapper.getBody(e);
            LOGGER.warn("Unhandled exception '{}' caught. Response returned with status code: '{}' and body: '{}'", e.getLocalizedMessage(), httpStatusCode, httpBody, e);
            response.setStatus(httpStatusCode);
            response.setContentType("application/json");
            response.getWriter().write(httpBody);
        } catch (IOException ioEx) {
            LOGGER.warn("IOException while outputting exception response", ioEx);
        }
    }
}
