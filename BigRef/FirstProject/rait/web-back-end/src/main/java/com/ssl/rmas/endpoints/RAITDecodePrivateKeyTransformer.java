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
 * 
 */
package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.HeaderKeys;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Locale;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MutableMessageHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Base64Utils;

public class RAITDecodePrivateKeyTransformer {

    @Transformer
    public Message<?> decodePrivateKey(Message<?> message) {
        String encodedPrivateKey = message.getHeaders().get(HeaderKeys.PRIVATE_KEY.toString()).toString();
        String decodedPrivateKey = new String(Base64Utils.decodeFromString(encodedPrivateKey), UTF_8);
        MessageHeaders newHeaders = createHeaders(message.getHeaders(), decodedPrivateKey);
        return MessageBuilder.createMessage(message.getPayload(), newHeaders);
    }

    private MessageHeaders createHeaders(MessageHeaders headers, String decodedPrivateKey) {
        MutableMessageHeaders newHeaders = new MutableMessageHeaders(headers);
        newHeaders.remove(HeaderKeys.PRIVATE_KEY.toString().toLowerCase(Locale.UK));
        newHeaders.put(HeaderKeys.PRIVATE_KEY.toString(), decodedPrivateKey);
        return newHeaders;
    }
}
