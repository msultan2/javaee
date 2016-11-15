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
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;


@PropertySource("classpath:config/application.properties")
public class PrepareEmailHeadersTransformer {

    @Value("${security.email.fromAddress:No Reply <no-reply@he-rmas.org.uk>}")
    private String emailFromAddress;

    @Value("${rmas.web.url:'https://www.he-rmas.org.uk/#/'}")
    private String webUrl;

    protected Message<Map<String, Object>> prepareEmail(String name, String email, String emailTextTemplate, String emailHTMLTemplate,String emailSubject, Map<String, Object> templateContext) {
        Map<String, Object> messageHeaders = new HashMap<>();
        templateContext.put("rmasWebUrl", webUrl);
        messageHeaders.put(HeaderKeys.EMAIL_TEXT_TEMPLATE.toString(), emailTextTemplate);
        messageHeaders.put(HeaderKeys.EMAIL_HTML_TEMPLATE.toString(), emailHTMLTemplate);
        messageHeaders.put(HeaderKeys.EMAIL_FROM_ADDRESS.toString(), emailFromAddress);
        messageHeaders.put(HeaderKeys.EMAIL_TO_ADDRESS.toString(), name + " <"+email+">");
        messageHeaders.put(HeaderKeys.EMAIL_SUBJECT.toString(), emailSubject);
        return MessageBuilder.createMessage(templateContext, new MessageHeaders(messageHeaders));
    }
}
