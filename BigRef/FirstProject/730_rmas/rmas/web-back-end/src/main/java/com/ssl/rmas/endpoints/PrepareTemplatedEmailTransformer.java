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
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.Transformer;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.ui.velocity.VelocityEngineUtils;

@PropertySource("classpath:config/application.properties")
public class PrepareTemplatedEmailTransformer {

    private VelocityEngine velocityEngine;

    @Autowired
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    @Transformer
    public Message<MimeMessagePreparator> prepareTemplatedEmail(Message<Map<String,Object>> message) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws MessagingException {
                String textEmailTemplate = message.getHeaders().get(HeaderKeys.EMAIL_TEXT_TEMPLATE.toString(), String.class);
                String htmlEmailTemplate = message.getHeaders().get(HeaderKeys.EMAIL_HTML_TEMPLATE.toString(), String.class);
                Map<String, Object> model = message.getPayload();
                @SuppressWarnings("unchecked")
                Map<String, Resource> inlineResources = message.getHeaders().get(HeaderKeys.EMAIL_INLINE_RESOURCES.toString(), Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Resource> attachmentsResources = message.getHeaders().get(HeaderKeys.EMAIL_ATTACHMENT_RESOURCES.toString(), Map.class);

                int type = MimeMessageHelper.MULTIPART_MODE_NO;
                if(htmlEmailTemplate!=null) {
                    type = MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED;
                } else if(!attachmentsResources.isEmpty()) {
                    type = MimeMessageHelper.MULTIPART_MODE_MIXED;
                }

                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, type);
                messageHelper.setFrom(message.getHeaders().get(HeaderKeys.EMAIL_FROM_ADDRESS.toString(), String.class));
                messageHelper.setTo(message.getHeaders().get(HeaderKeys.EMAIL_TO_ADDRESS.toString(), String.class));

                String textEmailText = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, textEmailTemplate, "UTF-8", model);
                if(type==MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED) {
                    String htmlEmailText = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, htmlEmailTemplate, "UTF-8", model);
                    messageHelper.setText(textEmailText, htmlEmailText);
                    if(inlineResources!=null) {
                        for(Map.Entry<String, Resource> entry : inlineResources.entrySet()) {
                            messageHelper.addInline(entry.getKey(), entry.getValue());
                        }
                    }
                } else {
                    messageHelper.setText(textEmailText);
                }
                if ((type == MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED || type == MimeMessageHelper.MULTIPART_MODE_MIXED)
                    && attachmentsResources != null) {
                        for(Map.Entry<String, Resource> entry : attachmentsResources.entrySet()) {
                            messageHelper.addAttachment(entry.getKey(), entry.getValue());
                    }
                }
                messageHelper.setSubject(message.getHeaders().get(HeaderKeys.EMAIL_SUBJECT.toString(), String.class));
            }
        };

        return MessageBuilder.withPayload(preparator).build();
    }
}
