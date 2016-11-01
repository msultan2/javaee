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
package com.ssl.rmas.endpoints;

import org.springframework.integration.support.MutableMessageHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import java.util.Locale;

public class AddConnectionParamsToHeaderTransformer {

    public Message<?> transform(Message<?> message) {
        ConnectionParams connectionParams = getConnectionParams(message.getHeaders());
        MessageHeaders newHeaders = createHeaders(message.getHeaders(), connectionParams);
        return MessageBuilder.createMessage(message.getPayload(), newHeaders);
    }

     private ConnectionParams getConnectionParams(final MessageHeaders headers) {
        String ipAddress = getMessageHeader(headers, HeaderKeys.IP_ADDRESS);
        int bandwidthLimit = Integer.parseInt(getMessageHeader(headers, HeaderKeys.BANDWIDTH_LIMIT));
        String privateKey = getMessageHeader(headers, HeaderKeys.PRIVATE_KEY);
        return new ConnectionParams(ipAddress, bandwidthLimit, privateKey);
    }

     private String getMessageHeader(MessageHeaders headers, HeaderKeys headerKey) {
         return headers.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(headerKey.toString())).map(entry -> entry.getValue().toString()).findFirst().orElseGet(() -> null);
     }

    private MessageHeaders createHeaders(MessageHeaders headers, ConnectionParams connectionParams) {
        MessageHeaders newHeaders = new MutableMessageHeaders(headers);
        newHeaders.put(HeaderKeys.CONNECTION_PARAMS.toString(), connectionParams);
        newHeaders.remove(HeaderKeys.IP_ADDRESS.toString());
        newHeaders.remove(HeaderKeys.BANDWIDTH_LIMIT.toString());
        newHeaders.remove(HeaderKeys.PRIVATE_KEY.toString());
        newHeaders.remove(HeaderKeys.IP_ADDRESS.toString().toLowerCase(Locale.UK));
        newHeaders.remove(HeaderKeys.BANDWIDTH_LIMIT.toString().toLowerCase(Locale.UK));
        newHeaders.remove(HeaderKeys.PRIVATE_KEY.toString().toLowerCase(Locale.UK));
        return newHeaders;
    }
}
