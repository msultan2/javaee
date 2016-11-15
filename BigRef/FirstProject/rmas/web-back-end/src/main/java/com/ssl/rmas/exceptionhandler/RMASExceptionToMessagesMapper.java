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
package com.ssl.rmas.exceptionhandler;

import com.ssl.rmas.utils.ErrorMessage;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.toList;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;

@Component("ExceptionToMessagesMapper")
public class RMASExceptionToMessagesMapper extends SharedExceptionToMessagesMapper {

    @Override
    public ErrorMessage getErrorMessage(Throwable throwable) {
        ErrorMessage errorMessage;
        if (throwable instanceof RepositoryConstraintViolationException) {
            errorMessage = getMessage((RepositoryConstraintViolationException) throwable);
        } else if (throwable instanceof DuplicateKeyException) {
            errorMessage = ErrorMessage.DUPLICATE_VALUE;
        } else if (throwable instanceof LockedException) {
            errorMessage = getMessage((LockedException) throwable);
        } else if (throwable instanceof AccountExpiredException) {
            errorMessage = ErrorMessage.USER_ACCOUNT_LOCKED;
        } else {
            errorMessage = super.getErrorMessage(throwable);
        }
        return errorMessage;
    }

    private ErrorMessage getMessage(final LockedException ex) {
        if (ex.getMessage().equals(ErrorMessage.USER_ACCOUNT_SUSPENDED.toString())) {
            return ErrorMessage.USER_ACCOUNT_SUSPENDED;
        }
        if (ex.getMessage().equals(ErrorMessage.USER_ACCOUNT_LOCKED.toString())) {
            return ErrorMessage.USER_ACCOUNT_LOCKED;
        }
        return ErrorMessage.UNKNOWN;
    }

    private ErrorMessage getMessage(final RepositoryConstraintViolationException ex) {
        List<String> defaultMessages = ex.getErrors().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(toList());
        List<ErrorMessage> errorMessages = Arrays.asList(ErrorMessage.NOT_ALLOWED_TO_SUSPEND_OWN_GROUP,
            ErrorMessage.NOT_ALLOWED_TO_DELETE_GROUP_WITH_USERS);
        return errorMessages.stream()
                .filter(errorMessage -> defaultMessages.contains(errorMessage.toString()))
                .findAny()
                .orElse(ErrorMessage.BAD_REQUEST);
    }
}
