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
 * Copyright 2016 (C) Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */

package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.HeaderKeys;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;


public abstract class ValidationTransformer {

    public abstract Message<?> createValidationErrors(Message<?> message);

    @SuppressWarnings("unchecked")
    protected Set<String> getErrors(Message<?> message){
        Object payload = message.getPayload();
        if(payload instanceof Set){
            return (Set<String>) payload;
        }
        return new HashSet<>();
    }

    protected Message<?> getErrorMessage(Set<String> errors, HttpStatus httpStatus, MessageHeaders headers) {
        return MessageBuilder
            .withPayload(errors)
            .copyHeaders(headers)
            .setHeader(HeaderKeys.CONTENT_TYPE.toString(), "application/json")
            .setHeader(HeaderKeys.HTTP_STATUS_CODE.toString(), httpStatus.value())
            .build();
    }
}
