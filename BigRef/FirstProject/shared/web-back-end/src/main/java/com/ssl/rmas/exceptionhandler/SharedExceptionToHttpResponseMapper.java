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
 */
package com.ssl.rmas.exceptionhandler;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.NoHandlerFoundException;

public class SharedExceptionToHttpResponseMapper implements ExceptionToHttpResponseMapper {

    ExceptionToMessagesMapper exceptionToMessagesMapper;
    
    @Autowired 
    public void setExceptionToMessagesMapper(ExceptionToMessagesMapper exceptionToMessagesMapper){
        this.exceptionToMessagesMapper =  exceptionToMessagesMapper;
    }
    
    @Override
    public int getStatusCode(Throwable throwable) {
        int statusCode;
        if (throwable instanceof AccessDeniedException || throwable instanceof AuthenticationException || throwable instanceof JwtException) {
            statusCode = HttpStatus.FORBIDDEN.value();
        } else if (throwable instanceof IllegalArgumentException) {
            statusCode = HttpStatus.BAD_REQUEST.value();
        } else if (throwable instanceof NoHandlerFoundException) {
            statusCode = HttpStatus.NOT_FOUND.value();
        } else {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return statusCode;
    }

    @Override
    public String getBody(Throwable throwable) {
        return "[\"" + exceptionToMessagesMapper.getErrorMessage(throwable) + "\"]";
    }
}
