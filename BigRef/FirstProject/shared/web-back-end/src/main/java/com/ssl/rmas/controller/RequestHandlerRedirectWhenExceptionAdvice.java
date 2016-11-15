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
package com.ssl.rmas.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.advice.AbstractRequestHandlerAdvice;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.MessageBuilder;

public class RequestHandlerRedirectWhenExceptionAdvice extends AbstractRequestHandlerAdvice {

    private final MessageChannel redirectChannel;
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandlerRedirectWhenExceptionAdvice.class);

    public RequestHandlerRedirectWhenExceptionAdvice(MessageChannel redirectChannel) {
        this.redirectChannel = redirectChannel;
    }

    @Override
    protected Object doInvoke(final ExecutionCallback callback, Object target, final Message<?> inputMessage) throws Exception {
        try {
            return callback.execute();
        } catch (MessageHandlingException ex) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                handleException(target, inputMessage, ex);
            } else {
                handleException(target, inputMessage, ex.getCause());
            }
        } catch (Exception ex) {
            handleException(target, inputMessage, ex);
        }
        return null;
    }

    private void handleException(Object target, final Message<?> inputMessage, Throwable ex) {
        LOGGER.warn("Exception '{}' has occurred in method '{}' with message: '{}'", ex.getLocalizedMessage(), target.toString(), inputMessage.toString());
        Message<Throwable> outputMessage = MessageBuilder.withPayload(ex).copyHeaders(inputMessage.getHeaders()).build();
        redirectChannel.send(outputMessage);
    }

}
