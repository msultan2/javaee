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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved. *
 */
package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.managers.ResultsManager;
import com.ssl.rmas.utils.ErrorMessage;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import com.ssl.rmas.exceptionhandler.ExceptionToHttpResponseMapper;
import com.ssl.rmas.exceptionhandler.ExceptionToMessagesMapper;

public class ExceptionToHttpResponseTransformer {

    private final Logger logger = LoggerFactory.getLogger(ExceptionToHttpResponseTransformer.class);

    private final String contentTypeValue = "application/json";

    private ExceptionToHttpResponseMapper exceptionToHttpResponseMapper;
    private ExceptionToMessagesMapper exceptionToMessagesMapper;
    private ResultsManager resultsManager;

    @Autowired
    public void setExceptionToHttpResponseMapper(ExceptionToHttpResponseMapper exceptionToHttpResponseMapper) {
        this.exceptionToHttpResponseMapper = exceptionToHttpResponseMapper;
    }
    
    @Autowired
    public void setExceptionToMessagesMapper(ExceptionToMessagesMapper exceptionToMessagesMapper) {
        this.exceptionToMessagesMapper = exceptionToMessagesMapper;
    }

   @Autowired
   public void setResultsManager(ResultsManager resultsManager) {
       this.resultsManager = resultsManager;
    }

    @Transformer
    public Message<Set<ErrorMessage>> updateMessage(Message<Throwable> throwableMessage) {
        Throwable throwable = throwableMessage.getPayload();
        Set<ErrorMessage> errorMessages = exceptionToMessagesMapper.getErrorMessages(throwable);
        storeFailureIfOperationResultPresent(throwableMessage.getHeaders(), errorMessages);
        int statusCode = exceptionToHttpResponseMapper.getStatusCode(throwable);
        logger.warn("Unhandled exception '{}' caught. Response returned with status code: '{}' and body: '{}'",
                throwable.getLocalizedMessage(), statusCode, errorMessages, throwable);
        return MessageBuilder
                .withPayload(errorMessages)
                .copyHeaders(throwableMessage.getHeaders())
                .setHeader(HeaderKeys.CONTENT_TYPE.toString(), contentTypeValue)
                .setHeader(HeaderKeys.HTTP_STATUS_CODE.toString(), statusCode)
                .build();
    }

    private void storeFailureIfOperationResultPresent(MessageHeaders messageHeaders, Set<ErrorMessage> errorMessages) {
        Authentication currentUser = messageHeaders.get(HeaderKeys.CURRENT_USER.toString(), Authentication.class);
        UUID uuid = messageHeaders.get(HeaderKeys.ACTIVITY_ID.toString(), UUID.class);
        if (currentUser != null && uuid != null) {
            resultsManager.updateResult(currentUser, uuid, errorMessages);
        }
    }
}
