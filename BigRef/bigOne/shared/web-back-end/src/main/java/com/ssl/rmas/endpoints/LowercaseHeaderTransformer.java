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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import com.ssl.rmas.entities.HeaderKeys;
import java.util.Locale;

public class LowercaseHeaderTransformer {
    @Transformer
    public Message<?> transformHeaders(Message<?> original) {
        MessageHeaders originalHeaders = original.getHeaders();
        MessageBuilder<?> messageBuilder = MessageBuilder.fromMessage(original);
        List<String> ourHeaderKeys = Arrays.stream(HeaderKeys.values()).map(key -> key.toString()).collect(Collectors.toList());

        originalHeaders.entrySet().stream().filter(entry -> ourHeaderKeys.contains(entry.getKey())).forEach(entry -> {
            messageBuilder.setHeader(entry.getKey().toLowerCase(Locale.UK), entry.getValue());
            messageBuilder.removeHeader(entry.getKey());
        });

        return messageBuilder.build();
    }
}
