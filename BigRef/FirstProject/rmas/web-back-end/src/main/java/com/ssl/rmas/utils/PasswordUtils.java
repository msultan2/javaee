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
package com.ssl.rmas.utils;

import com.ssl.rmas.entities.User;
import com.ssl.rmas.repositories.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {

    private final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);    
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private Clock clock;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isPasswordValid(String password) {
        boolean valid = false;
        if(!StringUtils.isEmpty(password)) {
            int rulesBroken = 0;
            valid = true;

            if (password.toUpperCase(Locale.UK).equals(password)) {
                logger.debug("No lowercase characters");
                rulesBroken++;
            }
            if (password.toLowerCase(Locale.UK).equals(password)) {
                logger.debug("No uppercase characters");
                rulesBroken++;
            }
            if(!password.matches(".*\\d.*")) {
                logger.debug("No digits");
                rulesBroken++;
            }
            if(!password.matches(".*\\W.*")) {
                logger.debug("No non alphanumeric characters");
                rulesBroken++;
            }

            if(rulesBroken > 1) {
                logger.debug("Too many broken rules");
                valid = false;
            }
            if(password.matches(".*(.)\\1.*")) {
                //Contains repeats
                logger.debug("Contains repeats");
                valid = false;
            }
            if(password.length() < 8) {
                logger.debug("Password too short");
                valid = false;
            }
        }
        return valid;
    }

    public void changePassword(User currentUser,String newPassword) {
        Instant startInstance = Instant.now(clock);
        String newPasswordHash = passwordEncoder.encode(newPassword);
        logger.debug("Generated new password has for user {} in {} seconds", currentUser.getName(), (Instant.now(clock).toEpochMilli() - startInstance.toEpochMilli()) / 1000f);
        currentUser.setPasswordHash(newPasswordHash);
        currentUser.setPasswordChangedDate(startInstance);
        userRepository.save(currentUser);
    }

}
