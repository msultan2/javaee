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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;

import com.ssl.rmas.entities.UserRegistration;

public class PrepareUserRejectedEmailTransformer extends PrepareEmailHeadersTransformer {

    private final Logger logger = LoggerFactory.getLogger(PrepareUserRejectedEmailTransformer.class);

    @Transformer
    public Message<Map<String, Object>> prepareEmail(UserRegistration userRegistration) {
        Map<String, Object> templateContext = new HashMap<>();
        templateContext.put("userName", userRegistration.getName());
        templateContext.put("rejectReason", userRegistration.getRejectApproveReason());
        logger.debug("Preparing user rejected email for user {}", userRegistration.getEmail());
        return prepareEmail(
                userRegistration.getName(),
                userRegistration.getEmail(),
                "templates/emails/userRejectedText.vm",
                "templates/emails/userRejectedHTML.vm",
                "RMAS User Rejected",
                templateContext);
    }
}
