/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 * 
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.exceptionhandler.ExceptionToMessagesMapper;
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
import org.springframework.security.core.Authentication;

public class ExceptionHandlingTransformer {
    private final Logger logger = LoggerFactory.getLogger(ExceptionHandlingTransformer.class);
    
    private ResultsManager resultsManager;
    private ExceptionToMessagesMapper exceptionToMessagesMapper;
    
    @Autowired
    public void setExceptionToMessagesMapper(ExceptionToMessagesMapper exceptionToMessagesMapper) {
        this.exceptionToMessagesMapper = exceptionToMessagesMapper;
    }
    
    @Autowired
    public void setResultsManager(ResultsManager resultsManager) {
       this.resultsManager = resultsManager;
    }
    
    @Transformer
    public boolean storeOperationResult(Message<Throwable> message){
        Throwable throwable = message.getPayload();
        Set<ErrorMessage> errorMessages = exceptionToMessagesMapper.getErrorMessages(throwable);        
        return storeFailureIfOperationResultPresent(message.getHeaders(), errorMessages);
    }
    
    private boolean storeFailureIfOperationResultPresent(MessageHeaders messageHeaders, Set<ErrorMessage> errorMessages) {
        boolean result = false;
        Authentication currentUser = messageHeaders.get(HeaderKeys.CURRENT_USER.toString(), Authentication.class);
        UUID uuid = messageHeaders.get(HeaderKeys.ACTIVITY_ID.toString(), UUID.class);
        if (uuid != null) {
            logger.debug("Updating the operation result with error message {} for current user {} and activity id {}", errorMessages, currentUser, uuid);
            resultsManager.updateResult(currentUser, uuid, errorMessages);
            result = true;
        }
        return result;
    }
    
}