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

import com.ssl.rmas.entities.User;
import com.ssl.rmas.repositories.RccRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator{

    private final Logger logger = LoggerFactory.getLogger(UserValidator.class);
    private RccRepository rccRepository;

    @Autowired
    public void setRccRepository(RccRepository rccRepository) {
        this.rccRepository = rccRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return User.class.equals(type);
    }

    @Override
    public void validate(Object target, Errors errors) {        
        User userFromRequest = (User) target;
        validateRequiredValues(userFromRequest, errors);       
        if (errors.hasErrors()) {
            logger.debug("Validation failure {}", errors);
        }        
    }

    private void validateRequiredValues(User userFromRequest, Errors errors){
        if(StringUtils.isBlank(userFromRequest.getEmail())){
            errors.rejectValue("email", "email.is.required", "Email is required");
        }
        if(StringUtils.isBlank(userFromRequest.getName())){
            errors.rejectValue("name", "name.is.required", "Name is required");
        }
        if(StringUtils.isBlank(userFromRequest.getEmployer())){
            errors.rejectValue("employer", "employer.is.required", "Organisation is required");
        }
        if(StringUtils.isBlank(userFromRequest.getMcr())){
            errors.rejectValue("mcr", "mcr.is.required", "Maintenance region is required");
        }
        if(StringUtils.isBlank(userFromRequest.getRcc())){
            errors.rejectValue("rcc", "rcc.is.required", "RCC is required");
        }else if (!rccRepository.exists(userFromRequest.getRcc())){            
            errors.rejectValue("rcc", "rcc.invalid.value", "The rcc must have a valid value");            
        }
        if(StringUtils.isBlank(userFromRequest.getAddress())){
            errors.rejectValue("address", "address.is.required", "Address is required");
        }
        if(StringUtils.isBlank(userFromRequest.getPrimaryPhone())){
            errors.rejectValue("primaryPhone", "primaryPhone.is.required", "Telephone number is required");
        }
        if(StringUtils.isBlank(userFromRequest.getMobile())){
            errors.rejectValue("mobile", "mobile.is.required", "Mobile number is required");
        }
    }
}
