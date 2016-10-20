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

import com.ssl.rmas.utils.ErrorMessage;
import io.jsonwebtoken.JwtException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.NoHandlerFoundException;

public class SharedExceptionToMessagesMapper implements ExceptionToMessagesMapper {
    
    @Override
    public ErrorMessage getErrorMessage(Throwable throwable) {
        
        ErrorMessage errorMessage;
        if (throwable instanceof AccessDeniedException || throwable instanceof AuthenticationException || throwable instanceof JwtException) {
            errorMessage = ErrorMessage.FORBIDDEN;
        } else if (throwable instanceof IllegalArgumentException) {
            errorMessage = ErrorMessage.BAD_REQUEST;
        } else if (throwable instanceof IllegalStateException || throwable instanceof NoHandlerFoundException) {
            errorMessage = ErrorMessage.GENERIC_ERROR;
        } else {
            errorMessage = ErrorMessage.UNKNOWN;
        }
        return errorMessage;
    }

    @Override
    public Set<ErrorMessage> getErrorMessages(Throwable throwable) {
        Set<ErrorMessage> errorMessageSet = new HashSet<>();
        errorMessageSet.add(getErrorMessage(throwable));
        return errorMessageSet;
    }
}
