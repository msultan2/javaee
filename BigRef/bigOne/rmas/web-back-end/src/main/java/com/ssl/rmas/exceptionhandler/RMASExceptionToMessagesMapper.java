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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.stereotype.Component;

@Component("ExceptionToMessagesMapper")
public class RMASExceptionToMessagesMapper extends SharedExceptionToMessagesMapper {

    @Override
    public ErrorMessage getErrorMessage(Throwable throwable) {
        ErrorMessage errorMessage;
        if (throwable instanceof RepositoryConstraintViolationException) {
            errorMessage = ErrorMessage.BAD_REQUEST;
        } else if (throwable instanceof DuplicateKeyException) {
            errorMessage = ErrorMessage.DUPLICATE_VALUE;
        }  else {
            errorMessage = super.getErrorMessage(throwable);
        }
        return errorMessage;
    }
}
