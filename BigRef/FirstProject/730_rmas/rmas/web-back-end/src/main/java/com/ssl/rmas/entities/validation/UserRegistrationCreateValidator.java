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

import com.ssl.rmas.entities.RequestStatus;
import com.ssl.rmas.entities.UserRegistration;
import com.ssl.rmas.repositories.RccRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserRegistrationCreateValidator implements Validator{

    private final static Logger LOGGER = LoggerFactory.getLogger(UserRegistrationCreateValidator.class);

    @Override
    public boolean supports(Class<?> type) {
        return UserRegistration.class.equals(type);
    }

    @Autowired
    private RccRepository rccRepository;

    @Override
    public void validate(Object target, Errors errors) {
        UserRegistration userRegistration = (UserRegistration) target;
        if (!StringUtils.isBlank(userRegistration.getId())) {
            errors.rejectValue("id", "id.invalid.value", "The id must be empty");
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "email.empty", "The Email must be present");
        if (!userRegistration.getRequestStatus().equals(RequestStatus.PENDING)) {
            errors.rejectValue("requestStatus", "requestStatus.invalid.value", "The requestStatus must be PENDING");
        }
        if (!Boolean.TRUE.equals(userRegistration.getTandcAccepted())) {
            errors.rejectValue("tandcAccepted", "tandcAccepted.invalid.value", "Accepted terms and conditions must be true");
        }
        if (isInvalidRcc(userRegistration.getRcc())) {
            errors.rejectValue("rcc", "rcc.invalid.value", "Rcc must have a valid value");
        }
        if(errors.hasErrors()) {
            LOGGER.debug("Validation failure {}", errors);
        }
    }

    private boolean isInvalidRcc(String rcc) {
        return !rccRepository.exists(rcc);
    }
}
