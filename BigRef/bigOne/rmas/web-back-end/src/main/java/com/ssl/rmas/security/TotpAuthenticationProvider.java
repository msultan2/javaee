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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TotpAuthenticationProvider extends DaoAuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(TotpAuthenticationProvider.class);

    private TotpAuthenticator totpAuthenticator;

    @Autowired
    public void setTotpAuthenticator(TotpAuthenticator authenticator) {
        this.totpAuthenticator = authenticator;
    }

    @Override
    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        super.setPasswordEncoder(passwordEncoder);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        logger.debug("additionalAuthenticationChecks called with {} and {}", userDetails, authentication);

        if (userDetails instanceof RMASUserDetails) {
            RMASUserDetails rmasUserDetails = (RMASUserDetails) userDetails;
            if (!rmasUserDetails.get2faSecret().isPresent()) {
                logger.debug("User {} does not have 2FA enabled", rmasUserDetails.getUsername());
                super.additionalAuthenticationChecks(userDetails, authentication);
                return;
            } else {
                String secret = rmasUserDetails.get2faSecret().get();
                if(authentication.getDetails() instanceof TotpWebAuthenticationDetails) {
                    Integer totpToken = ((TotpWebAuthenticationDetails)authentication.getDetails()).getTotpToken();
                    logger.debug("User {} totpToken {}", rmasUserDetails.getUsername(), totpToken);
                    if (totpToken != null) {
                        try {
                            super.additionalAuthenticationChecks(userDetails, authentication);
                            if (!totpAuthenticator.verifyCode(secret, totpToken)) {
                                throw new BadCredentialsException("Invalid TOTP code");
                            }
                            rmasUserDetails.getAuthorities().add(new SimpleGrantedAuthority("ROLE_LOGGED_IN_WITH_2FA"));
                        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                            throw new InternalAuthenticationServiceException("TOTP code verification failed", e);
                        }
                    } else {
                        throw new MissingTotpKeyAuthenticatorException("TOTP code is mandatory");
                    }
                } else if(authentication.getDetails() instanceof TotpPreAuthenticatedDetails) {
                    logger.debug("User pre authenticated");
                    super.additionalAuthenticationChecks(userDetails, authentication);
                } else {
                    logger.warn("Authentication details is not an instace of TotpWebAuthenticationDetails, instead found a {}", authentication.getDetails().getClass().getName());
                }
            }
        } else {
            logger.warn("User details not an RMAS user details bean, instead found a {}", userDetails.getClass().getName());
            super.additionalAuthenticationChecks(userDetails, authentication);
        }
    }
}
