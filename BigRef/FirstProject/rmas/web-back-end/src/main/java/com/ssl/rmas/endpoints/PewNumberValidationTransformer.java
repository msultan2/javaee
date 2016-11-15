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
import com.ssl.rmas.utils.ActivityEnum;

import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;


public class PewNumberValidationTransformer extends ValidationTransformer{

    private final Logger logger = LoggerFactory.getLogger(PewNumberValidationTransformer.class);

    @Transformer
    @Override
    public Message<?> createValidationErrors(Message<?> message) {
        MessageHeaders headers = message.getHeaders();
        Set<String> errors = getErrors(message);
        String pewNumber = headers.get(HeaderKeys.PEW_NUMBER.toString().toLowerCase(Locale.UK), String.class);
        String currentactivity = message.getHeaders().get(HeaderKeys.CURRENT_ACTIVITY.toString(), String.class);
        Message<?> retval;
        if(StringUtils.isEmpty(pewNumber) && doesActivityRequirePewNumber(currentactivity)){
            errors.add("PEW number is required for " + currentactivity);
            retval = getErrorMessage(errors, HttpStatus.BAD_REQUEST, headers);
        }else{
            retval = MessageBuilder.fromMessage(message)
                    .build();
        }

        logger.debug("message returned after validation is: {}", retval);
        return retval;
    }
    
    private boolean doesActivityRequirePewNumber(String currentactivity){
        return currentactivity.equals(ActivityEnum.RESET_DEVICE.toString());
    }
}
