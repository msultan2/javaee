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
package com.ssl.rmas.entities.validation;

import com.ssl.rmas.entities.AccountClassification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AccountClassificationValidator implements Validator {

    private final Logger auditLogger = LoggerFactory.getLogger("Audit");
    private final Logger logger = LoggerFactory.getLogger(AccountClassificationValidator.class);

    @Override
    public boolean supports(Class<?> type) {
        return AccountClassification.class.equals(type);
    }

    @Override
    public void validate(Object target, Errors errors) {

        AccountClassification accountClassificationFromRequest = (AccountClassification) target;
        if (!StringUtils.isBlank(accountClassificationFromRequest.getId())) {
            errors.rejectValue("id", "id.invalid.value", "The id must be empty");
        }

        if(!errors.hasErrors()){
            auditLogger.info("Creating a new Account Classification - {} requested",accountClassificationFromRequest.getName());
        } else{
            logger.debug("Account classification creation failed for {} due to {}", accountClassificationFromRequest.getName(), errors);
        }
    }
}
