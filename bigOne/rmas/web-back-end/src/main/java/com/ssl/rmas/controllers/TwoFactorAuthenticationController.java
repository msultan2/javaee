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
package com.ssl.rmas.controllers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.io.BaseEncoding;
import com.ssl.rmas.entities.TwoFactorAuthenticationTokenVerification;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.repositories.UserRepository;
import com.ssl.rmas.security.TotpAuthenticator;

@Controller
@RequestMapping(value = "twoFactorAuthentication/")
public class TwoFactorAuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(TwoFactorAuthenticationController.class);
    private final Logger auditLogger = LoggerFactory.getLogger("Audit");
    private TotpAuthenticator totpAuthenticator;
    private UserRepository userRepo;
    private Clock clock;

    @Autowired
    public void setUserRepo(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Autowired
    public void setTotpAuthenticator(TotpAuthenticator totpAuthenticator) {
        this.totpAuthenticator = totpAuthenticator;
    }

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @RequestMapping(value = "generateSecret", produces="text/plain")
    @ResponseBody
    public String generateSecret() throws NoSuchAlgorithmException {
        logger.debug("Generating new secret");
        long startTime = clock.millis();
        byte[] bytes = new byte[16];
        SecureRandom.getInstanceStrong().nextBytes(bytes);
        String secret = BaseEncoding.base32().omitPadding().encode(bytes);
        logger.debug("Generated new secret in {}ms", (clock.millis() - startTime));
        return secret;
    }

    @RequestMapping(value = "verifySecret", method = RequestMethod.POST)
    public ResponseEntity<?> verifySecret(@RequestBody TwoFactorAuthenticationTokenVerification verification) throws InvalidKeyException, NoSuchAlgorithmException {
        logger.debug("Got 2FA verification {} for user {}", verification, SecurityContextHolder.getContext().getAuthentication().getName());
        boolean verified = totpAuthenticator.verifyCode(verification.getSecret(), verification.getVerificationCode());
        if(verified) {
            logger.debug("2FA token verified for user {}", SecurityContextHolder.getContext().getAuthentication().getName());
            User user = userRepo.findOwnUser();
            user.set2faSecret(Optional.ofNullable(verification.getSecret()));
            auditLogger.info("Set two factor authentication token for user {}", SecurityContextHolder.getContext().getAuthentication().getName());
            userRepo.save(user);
            return ResponseEntity.ok(null);
        } else {
            logger.debug("Verification of 2FA token failed for user {}", SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
