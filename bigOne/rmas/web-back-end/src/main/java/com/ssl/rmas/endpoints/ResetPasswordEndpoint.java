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

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.PasswordChangeToken;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.utils.ErrorMessage;
import com.ssl.rmas.repositories.PasswordChangeTokenRepository;
import com.ssl.rmas.utils.PasswordUtils;

@PropertySource("classpath:config/application.properties")
public class ResetPasswordEndpoint {

    private final Logger logger = LoggerFactory.getLogger(ResetPasswordEndpoint.class);
    private final Logger audit = LoggerFactory.getLogger("Audit");

    @Value("${security.onetimetoken.expiry:604800}")
    private long onetimetokenExpiry;
    private PasswordChangeTokenRepository passwordChangeTokenRepository;
    private Clock clock;
    private PasswordUtils passwordUtils;

    @Autowired
    public void setPasswordResetTokenRepository(PasswordChangeTokenRepository passwordChangeTokenRepository) {
        this.passwordChangeTokenRepository = passwordChangeTokenRepository;
    }

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }
    
    @Autowired
    public void setPasswordUtils(PasswordUtils passwordUtils) {
        this.passwordUtils = passwordUtils;
    }

    public void setOnetimeTokenExpiry(long onetimetokenExpiry) {
        this.onetimetokenExpiry = onetimetokenExpiry;
    }

    public Message<ErrorMessage[]> resetPassword(Message<String> message) {
        String code = message.getHeaders().get("tokenId", String.class);
        PasswordChangeToken token = passwordChangeTokenRepository.findOne(code);
        String newPassword = message.getPayload();

        logger.debug("Password change requested from token {}", code);

        Message<ErrorMessage[]> retval = MessageBuilder.createMessage(new ErrorMessage[]{}, message.getHeaders());
        if (passwordUtils.isPasswordValid(newPassword)) {
            if (token!=null && token.getCreatedTimestamp().plusSeconds(onetimetokenExpiry).isAfter(Instant.now(clock))) {
                logger.debug("Got token {}", token);
                User user = token.getUser();
                passwordUtils.changePassword(user, newPassword);
                passwordChangeTokenRepository.delete(token);

                audit.info("Password changed for user {} from one time link", user.getName());
            } else {
                logger.debug("Token is not valid");
                retval = MessageBuilder.withPayload(new ErrorMessage[]{ErrorMessage.INVALID_PASSWORD_RESET_TOKEN})
                        .copyHeaders(message.getHeaders())
                        .setHeader(HeaderKeys.HTTP_STATUS_CODE.toString(), HttpStatus.BAD_REQUEST.value())
                        .setHeader(HeaderKeys.CONTENT_TYPE.toString(), "application/json")
                        .build();
            }
        } else {
            logger.debug("Password is not valid");
            retval = MessageBuilder.withPayload(new ErrorMessage[]{ErrorMessage.INVALID_PASSWORD})
                .copyHeaders(message.getHeaders())
                .setHeader(HeaderKeys.HTTP_STATUS_CODE.toString(), HttpStatus.BAD_REQUEST.value())
                .setHeader(HeaderKeys.CONTENT_TYPE.toString(), "application/json")
                .build();
        }

        return retval;
    }
}
