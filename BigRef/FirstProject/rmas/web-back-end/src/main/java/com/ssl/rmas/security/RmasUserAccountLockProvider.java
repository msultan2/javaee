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
package com.ssl.rmas.security;

import com.ssl.rmas.endpoints.EmailServiceGateway;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.entities.UserGroupStatus;
import java.time.Duration;
import java.time.Instant;

import com.ssl.rmas.repositories.UserRepository;
import com.ssl.rmas.utils.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

@PropertySource("classpath:config/application.properties")
public class RmasUserAccountLockProvider extends DaoAuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(RmasUserAccountLockProvider.class);
    private final Logger auditLogger = LoggerFactory.getLogger("Audit");
    private EmailServiceGateway emailServiceGateway;
    private UserRepository userRepository;

    @Value("${rmas.userAccount.timeToExpireInDays:90}")
    private long timeToExpireInDays;

    @Value("${rmas.userAccount.loginMaxAttempts:5}")
    private int loginMaxAttempts;

    @Autowired
    public void setEmailServiceGateway(EmailServiceGateway emailServiceGateway) {
        this.emailServiceGateway = emailServiceGateway;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (userDetails instanceof RMASUserDetails) {
            RMASUserDetails rmasUserDetails = (RMASUserDetails) userDetails;
            credentialsChecks(rmasUserDetails, authentication);
            accountChecks(rmasUserDetails.getUser());
        } else {
            logger.warn("User details not an RMAS user details bean, instead found a {}", userDetails.getClass().getName());
            super.additionalAuthenticationChecks(userDetails, authentication);
        }
    }

    private void credentialsChecks(RMASUserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
        try {
            super.additionalAuthenticationChecks(userDetails, authentication);
        } catch (BadCredentialsException ex) {
            handleBadCredentials(userDetails.getUser());
            throw ex;
        }
    }

    private void handleBadCredentials(final User user) throws LockedException {
        user.increaseLoginAttempts();
        userRepository.save(user);
        if (user.isAccountNonLocked() && isTooManyLoginAttempts(user)) {
            lockAccountAndSendEmail(user);
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", ErrorMessage.USER_ACCOUNT_LOCKED.toString()));
        }
    }

    private boolean isTooManyLoginAttempts(final User user) {
        return user.getLoginAttempts() > loginMaxAttempts;
    }

    private void accountChecks(final User user) {
        if (isAccountLocked(user)) {
            throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", ErrorMessage.USER_ACCOUNT_LOCKED.toString()));
        }
        if (user.isAccountNonLocked() && isUserAccountExpired(user)) {
            lockAccountAndSendEmail(user);
            throw new AccountExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"));
        }
        userGroupSuspendedCheck(user);
    }

    private void userGroupSuspendedCheck(final User user) {
        UserGroupStatus userGroupStatus = user.getUserGroup().getStatus();
        if (UserGroupStatus.SUSPENDED == userGroupStatus) {
            throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", ErrorMessage.USER_ACCOUNT_SUSPENDED.toString()));
        }
    }

    private boolean isAccountLocked(final User user) {
        return !user.isAccountNonLocked();
    }

    private boolean isUserAccountExpired(final User user) {
        if(user.getLastSuccessfulLogin() == null){
            return false;
        }
        Instant lastSuccessfulLogin = user.getLastSuccessfulLogin();
        long numberOfDays = Duration.between(lastSuccessfulLogin, Instant.now()).toDays();
        return numberOfDays > timeToExpireInDays;
    }

    private void lockAccountAndSendEmail(User user) {
        user.setAccountNonLocked(false);
        userRepository.save(user);
        auditLogger.info("{} user account is locked", user.getEmail());
        sendLockAccountEmail(user);
    }

    private void sendLockAccountEmail(User user) {
        try {
            emailServiceGateway.sendAccountLockedEmail(user);
        } catch (MessageDeliveryException mde) {
            logger.warn(ErrorMessage.FAILED_TO_SEND_EMAIL.toString(), mde);
        }
    }

}
