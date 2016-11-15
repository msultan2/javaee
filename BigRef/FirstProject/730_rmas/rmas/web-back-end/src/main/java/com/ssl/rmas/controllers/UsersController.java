/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 */
package com.ssl.rmas.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ssl.rmas.endpoints.EmailServiceGateway;
import com.ssl.rmas.entities.PasswordPair;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.entities.validation.UserValidator;
import com.ssl.rmas.repositories.UserRepository;
import com.ssl.rmas.security.PasswordChangeTokenManager;
import com.ssl.rmas.utils.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import com.ssl.rmas.utils.PasswordUtils;
import org.springframework.messaging.MessageDeliveryException;

@Controller
public class UsersController {

    private final Logger logger = LoggerFactory.getLogger(UsersController.class);
    private final Logger audit = LoggerFactory.getLogger("Audit");

    private UserRepository userRepository;
    private PasswordChangeTokenManager passwordChangeTokenManager;
    private EmailServiceGateway emailServiceGateway;
    private UserValidator userValidator;
    private PasswordUtils passwordUtils;
    private AuthenticationManager authenticationManager;

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
    public void setUserValidator(UserValidator userValidator) {
        this.userValidator = userValidator;
    }
    
    @Autowired
    public void setPasswordUtils(PasswordUtils passwordUtils) {
        this.passwordUtils = passwordUtils;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @RequestMapping(value = "users/search/findOwn2faStatus")
    public ResponseEntity<Boolean> getUsers2faDetails() {
        return ResponseEntity.ok(userRepository.findOwnUserProfile().get2faSecret().isPresent());
    }

    @RequestMapping(value = "users/{userId}/resetPassword")
    public ResponseEntity<ErrorMessage> generateResetToken(@PathVariable(value = "userId") String userId) {
        User user = userRepository.findOneByEmail(userId);
        if (user != null) {
            passwordChangeTokenManager.deleteToken(user);
            emailServiceGateway.sendPasswordResetEmail(passwordChangeTokenManager.createToken(user));
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(value = "users/updateUserDetails")
    public ResponseEntity<?> updateUserDetails(@RequestBody User user, BindingResult bindingResult) {
        if (!isValidUpdateUserDetailsRequest(user, bindingResult)) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        User currentUser = userRepository.findOwnUser();
        if (!currentUser.getEmail().equals(user.getEmail())) {
            bindingResult.rejectValue("email", "email.not.editable", "Email should not be updated");
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        currentUser.setName(user.getName());
        currentUser.setEmployer(user.getEmployer());
        currentUser.setMcr(user.getMcr());
        currentUser.setRcc(user.getRcc());
        currentUser.setAddress(user.getAddress());            
        currentUser.setPrimaryPhone(user.getPrimaryPhone());
        currentUser.setMobile(user.getMobile());
        userRepository.save(currentUser);
        try {
            emailServiceGateway.sendProfileChangedEmail(currentUser);
        } catch (MessageDeliveryException mde) {
            logger.warn(ErrorMessage.FAILED_TO_SEND_EMAIL.toString(), mde);
        }
        return ResponseEntity.ok(null);
    }

    private boolean isValidUpdateUserDetailsRequest(User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);
        return !bindingResult.hasErrors();
    }

    @RequestMapping(value = "users/changePassword", method = RequestMethod.POST)
    ResponseEntity<?> changePassword(@RequestBody PasswordPair passwordPair) {
        if (checkCurrentPassword(passwordPair.getCurrentPassword()) && passwordUtils.isPasswordValid(passwordPair.getNewPassword())) {
            User currentUser = userRepository.findOwnUserProfile();
            passwordUtils.changePassword(currentUser, passwordPair.getNewPassword());
            audit.info("Password changed for user {} from RMAS Web Page", currentUser.getName());
            try {
                emailServiceGateway.sendPasswordChangedEmail(currentUser);
            } catch (MessageDeliveryException mde) {
                logger.warn(ErrorMessage.FAILED_TO_SEND_EMAIL.toString(), mde);
            }
            return ResponseEntity.ok(null);
        }
        logger.warn(ErrorMessage.INVALID_PASSWORD.toString());
        return new ResponseEntity<>(ErrorMessage.INVALID_PASSWORD, HttpStatus.BAD_REQUEST);
    }

    private boolean checkCurrentPassword(String password) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), password);
            newAuth.setDetails(auth.getDetails());
            authenticationManager.authenticate(newAuth);
            return true;
        } catch (AuthenticationException ae) {
            logger.info("Authentication exception for old password {}", ae.getLocalizedMessage());
            return false;
        }
    }
}
