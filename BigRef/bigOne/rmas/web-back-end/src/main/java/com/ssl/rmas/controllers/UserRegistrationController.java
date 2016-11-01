/*
 *   THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 *   LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 *   EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 *   BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 *   INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 *   OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 *   Copyright 2016 © Costain Integrated Technology Solutions Limited.
 *   All Rights Reserved.
 */
package com.ssl.rmas.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ssl.rmas.endpoints.EmailServiceGateway;
import com.ssl.rmas.entities.RequestStatus;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.entities.UserGroup;
import com.ssl.rmas.entities.UserRegistration;
import com.ssl.rmas.entities.validation.UserRegistrationCreateValidator;
import com.ssl.rmas.entities.validation.UserRegistrationUpdateValidator;
import com.ssl.rmas.repositories.UserGroupRepository;
import com.ssl.rmas.repositories.UserRegistrationRepository;
import com.ssl.rmas.repositories.UserRepository;
import com.ssl.rmas.security.PasswordChangeTokenManager;
import com.ssl.rmas.utils.ErrorMessage;

@RestController
public class UserRegistrationController {

    private UserRepository userRepository;
    private UserRegistrationRepository userRegistrationRepository;
    private PasswordChangeTokenManager passwordChangeTokenManager;
    private EmailServiceGateway emailServiceGateway;
    private final Logger logger = LoggerFactory.getLogger(UserRegistrationController.class);
    private UserRegistrationCreateValidator createValidator;
    private UserRegistrationUpdateValidator updateValidator;

    private UserGroupRepository userGroupRepository;
    
    @Autowired
    public void setUserGroupRepository(UserGroupRepository userGroupRepository){
        this.userGroupRepository = userGroupRepository;
    }    

    @Autowired
    public void setUserRegistrationRepository(UserRegistrationRepository userRegistrationRepository) {
        this.userRegistrationRepository = userRegistrationRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordChangeTokenManager(PasswordChangeTokenManager passwordChangeTokenManager) {
        this.passwordChangeTokenManager = passwordChangeTokenManager;
    }

    @Autowired
    public void setEmailServiceGateway(EmailServiceGateway emailServiceGateway) {
        this.emailServiceGateway = emailServiceGateway;
    }

    @Autowired
    public void setUserRegistrationCreateValidator(UserRegistrationCreateValidator createValidator) {
        this.createValidator = createValidator;
    }

    @Autowired
    public void setUserRegistrationUpdateValidator(UserRegistrationUpdateValidator updateValidator) {
        this.updateValidator = updateValidator;
    }

    @RequestMapping(value = "/userRegistrations", method = RequestMethod.POST)
    public ResponseEntity<?> insert(@RequestBody UserRegistration userReg, BindingResult bindingResult) {
        if (isValidInsertRequest(userReg, bindingResult)) {
            userRegistrationRepository.save(userReg);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('HEAPPROVER') and hasRole('LOGGED_IN_WITH_2FA')")
    @RequestMapping(value = "/userRegistrations/{userId}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable(value = "userId") String userId, @RequestBody UserRegistration userReg, BindingResult bindingResult) {
        if (isValidUpdateRequest(userId, userReg, bindingResult)) {
            if (RequestStatus.APPROVED.equals(userReg.getRequestStatus())) {
                return approveUser(userReg);
            } else {
                return rejectUser(userReg);
            }
        } else {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('HEAPPROVER') and hasRole('LOGGED_IN_WITH_2FA')")
    @RequestMapping(value = "/userRegistrations/{userId}", method = RequestMethod.GET)
    public ResponseEntity<?> get(@PathVariable(value = "userId") String userId) {
            UserRegistration userRegistration = userRegistrationRepository.findOne(userId);
            return new ResponseEntity<>(userRegistration, HttpStatus.OK);
    }

    private ResponseEntity<?> approveUser(UserRegistration userReg) {
        try {
            User user = new User(userReg);
            user.setUserGroup(getUserGroup(userReg));
            userRepository.save(user);
            userRegistrationRepository.save(userReg);
            emailServiceGateway.sendRmasWelcomeEmail(passwordChangeTokenManager.createToken(user));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DuplicateKeyException dkex) {
            logger.debug(ErrorMessage.DUPLICATE_USER.toString(), dkex);
            userReg.setRequestStatus(RequestStatus.REJECTED);
            userReg.setRejectApproveReason(ErrorMessage.DUPLICATE_USER.toString());
            return rejectUser(userReg);
        } catch (MessageDeliveryException mde) {
            logger.warn(ErrorMessage.FAILED_TO_SEND_EMAIL.toString(), mde);
            return new ResponseEntity<>(ErrorMessage.FAILED_TO_SEND_EMAIL, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private UserGroup getUserGroup(UserRegistration userReg) {
        return userGroupRepository.findByGroupName(userReg.getUserGroup());
    }

    private ResponseEntity<?> rejectUser(UserRegistration userReg) {
        try {
            userRegistrationRepository.save(userReg);
            if (ErrorMessage.DUPLICATE_USER.toString().equals(userReg.getRejectApproveReason())) {
                return new ResponseEntity<>(ErrorMessage.DUPLICATE_USER, HttpStatus.BAD_REQUEST);
            } else {
                emailServiceGateway.sendUserRejectedEmail(userReg);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (MessageDeliveryException mde) {
            logger.warn(ErrorMessage.FAILED_TO_SEND_EMAIL.toString(), mde);
            return new ResponseEntity<>(ErrorMessage.FAILED_TO_SEND_EMAIL, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidUpdateRequest(String userId, UserRegistration userReg, BindingResult bindingResult) {
        if (userId.equals(userReg.getId())){
            updateValidator.validate(userReg, bindingResult);
        } else {
            bindingResult.rejectValue("id", "id.invalid.value", "The Id in the body needs to match the Id in the url");
        }
        return !bindingResult.hasErrors();
    }

    private boolean isValidInsertRequest(UserRegistration userReg, BindingResult bindingResult) {
        createValidator.validate(userReg, bindingResult);
        return !bindingResult.hasErrors();
    }
}