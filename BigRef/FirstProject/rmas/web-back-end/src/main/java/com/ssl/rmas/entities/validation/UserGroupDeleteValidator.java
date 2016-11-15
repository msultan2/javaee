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

import com.ssl.rmas.entities.UserGroup;
import com.ssl.rmas.repositories.UserRepository;
import com.ssl.rmas.utils.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserGroupDeleteValidator implements Validator {

    private final Logger logger = LoggerFactory.getLogger(UserGroupDeleteValidator.class);
    private final Logger auditLogger = LoggerFactory.getLogger("Audit");
    private UserRepository userRepository;

    @Autowired
    public void setUserRegistrationRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return UserGroup.class.equals(type);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserGroup userGroup = (UserGroup) target;        
        long usersCount = userRepository.countByUserGroup(userGroup);
        if (usersCount > 0) {
            errors.rejectValue("id", "id.invalid.value", ErrorMessage.NOT_ALLOWED_TO_DELETE_GROUP_WITH_USERS.toString());
        }
        if(!errors.hasErrors()) {
            auditLogger.info("User group deletion requested for {}", userGroup);
        } else {
            logger.debug("User group deletion validation failed for {}. Errors: {}", userGroup, errors);
        }
    }
}
