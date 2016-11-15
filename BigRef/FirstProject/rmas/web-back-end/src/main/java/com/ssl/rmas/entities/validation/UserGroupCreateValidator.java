/*
 * 
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

package com.ssl.rmas.entities.validation;

import com.ssl.rmas.entities.DeviceFilter;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.entities.UserGroup;
import com.ssl.rmas.entities.UserGroupStatus;
import static com.ssl.rmas.entities.UserGroupStatus.SUSPENDED;
import com.ssl.rmas.security.RMASUserDetails;
import com.ssl.rmas.utils.ErrorMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component("beforeCreateUserGroupValidator")
public class UserGroupCreateValidator implements Validator {

    private final Logger logger = LoggerFactory.getLogger(UserGroupCreateValidator.class);

    private DeviceFilterValidator deviceFilterValidator;

    @Autowired
    public void setDeviceFilterValidator(DeviceFilterValidator deviceFilterValidator) {
        this.deviceFilterValidator = deviceFilterValidator;
    }

    @Override
    public boolean supports(Class<?> type) {
        return UserGroup.class.equals(type);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserGroup userGroup = (UserGroup) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "groupName", "groupName.empty");
        ValidationUtils.rejectIfEmpty(errors, "accountClassification", "accountClassification.empty");
        if (StringUtils.isBlank(userGroup.getId()) && !UserGroupStatus.ENABLED.equals(userGroup.getStatus())) {
            errors.rejectValue("status", "status.invalid.value", "User Group Status should be enabled when creating");
        }
        DeviceFilter deviceFilter = userGroup.getDeviceFilter();
        deviceFilterValidator.validate(deviceFilter, errors);

        if (isSuspendingOwnUserGroup(userGroup)) {
            errors.rejectValue("status", "status.invalid.value", ErrorMessage.NOT_ALLOWED_TO_SUSPEND_OWN_GROUP.toString());
        }

        if(errors.hasErrors()) {
            logger.debug("Validation failure {}", errors);
        }
    }

    private boolean isSuspendingOwnUserGroup(final UserGroup userGroup) {
        return isSuspending(userGroup) && isOwn(userGroup);
    }

    private boolean isSuspending(final UserGroup userGroup) {
        return SUSPENDED.equals(userGroup.getStatus());
    }

    private boolean isOwn(final UserGroup userGroup) {
        return getUserGroup().getId().equals(userGroup.getId());
    }

    private UserGroup getUserGroup() {
        User user = getUser();
        return user.getUserGroup();
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RMASUserDetails rmasUserDetails = getRMASUserDetails(authentication);
        return rmasUserDetails.getUser();
    }

    private RMASUserDetails getRMASUserDetails(final Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null
            && RMASUserDetails.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
            return ((RMASUserDetails) authentication.getPrincipal());
        } else {
            throw new IllegalStateException("Authentication is null or has a invalid principal");
        }
    }

}
