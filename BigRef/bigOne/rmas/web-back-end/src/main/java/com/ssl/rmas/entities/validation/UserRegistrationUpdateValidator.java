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
import com.ssl.rmas.repositories.UserRegistrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserRegistrationUpdateValidator implements Validator{

    private final static Logger LOGGER = LoggerFactory.getLogger(UserRegistrationUpdateValidator.class);
    private UserRegistrationRepository userRegistrationRepository;

    @Autowired
    public void setUserRegistrationRepository(UserRegistrationRepository userRegistrationRepository) {
        this.userRegistrationRepository = userRegistrationRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return UserRegistration.class.equals(type);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserRegistration userRegistrationFromRequest = (UserRegistration) target;
        UserRegistration userRegistrationFromDB = userRegistrationRepository.findOne(userRegistrationFromRequest.getId());
        if (userRegistrationFromDB == null) {
            errors.reject("entity.not.present", "There is no entity to update");
        } else {
            if (!userRegistrationFromDB.getRequestStatus().equals(RequestStatus.PENDING)) {
                errors.reject("entity.already.updated", "Entity has already been approved or rejected");
            }
            if (userRegistrationFromRequest.getRequestStatus().equals(RequestStatus.PENDING)) {
                errors.rejectValue("requestStatus", "requestStatus.invalid.value", "The requestStatus must not be PENDING");
            }
            validateNotEditableValues(userRegistrationFromDB, userRegistrationFromRequest, errors);
        }
        if (errors.hasErrors()) {
            LOGGER.debug("Validation failure {}", errors);
        }
    }

    private void validateNotEditableValues(UserRegistration userRegistrationFromDB, UserRegistration userRegistrationFromRequest, Errors errors) {
        if (!userRegistrationFromDB.getEmail().equals(userRegistrationFromRequest.getEmail())) {
            errors.rejectValue("email", "email.not.editable", "Email can not be updated");
        }
        if (!userRegistrationFromDB.getEmployer().equals(userRegistrationFromRequest.getEmployer())) {
            errors.rejectValue("employer", "employer.not.editable", "Employer can not be updated");
        }
        if (!userRegistrationFromDB.getMcr().equals(userRegistrationFromRequest.getMcr())) {
            errors.rejectValue("mcr", "mcr.not.editable", "Maintenance region can not be updated");
        }
        if (!userRegistrationFromDB.getMobile().equals(userRegistrationFromRequest.getMobile())) {
            errors.rejectValue("mobile", "mobile.not.editable", "Mobile phone number can not be updated");
        }
        if (!userRegistrationFromDB.getName().equals(userRegistrationFromRequest.getName())) {
            errors.rejectValue("name", "name.not.editable", "Name can not be updated");
        }
        if (!userRegistrationFromDB.getPrimaryPhone().equals(userRegistrationFromRequest.getPrimaryPhone())) {
            errors.rejectValue("primaryPhone", "primaryPhone.not.editable", "Primary phone number can not be updated");
        }
        if (!userRegistrationFromDB.getRcc().equals(userRegistrationFromRequest.getRcc())) {
            errors.rejectValue("rcc", "rcc.not.editable", "RCC can not be updated");
        }
        if (!userRegistrationFromDB.getAddress().equals(userRegistrationFromRequest.getAddress())) {
            errors.rejectValue("address", "address.not.editable", "Address can not be updated");
        }
        if (!userRegistrationFromDB.getTandcAccepted().equals(userRegistrationFromRequest.getTandcAccepted())) {
            errors.rejectValue("tandcAccepted", "tandcAccepted.not.editable", "Accepted terms and conditions can not be updated");
        }
        if (!userRegistrationFromDB.getProjectSponsor().equals(userRegistrationFromRequest.getProjectSponsor())) {
            errors.rejectValue("projectSponsor", "projectSponsor.not.editable", "Project sponsor can not be updated");
        }
    }
}
