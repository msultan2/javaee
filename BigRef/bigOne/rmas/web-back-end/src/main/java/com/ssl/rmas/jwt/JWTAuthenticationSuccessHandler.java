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
 * Copyright 2015 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.jwt;

import com.ssl.rmas.entities.User;
import com.ssl.rmas.repositories.UserRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import static java.time.temporal.ChronoUnit.DAYS;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:config/application.properties")
public class JWTAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationSuccessHandler.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("Audit");
    @Value("${password.expiry.days: 90}")
    private int passwordExpiryDays;  

    private TokenHandler tokenHandler;
    private UserRepository userRepository;
    private Clock clock;

    @Autowired
    public void setTokenHandler(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        logger.debug("Authentication Success");
        AUDIT_LOGGER.info("User {} logged in", authentication.getName());
        User user = userRepository.findOneByEmail(authentication.getName());
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Authorization", "Bearer " + tokenHandler.createTokenForUser(authentication));
        response.addHeader("PasswordExpired", isPasswordExpired(user));
        response.setStatus(HttpStatus.OK.value());
    }


    private String isPasswordExpired(User user) {
        Instant lastChangeDatePlusExpiryDays = user.getPasswordChangedDate().plus(passwordExpiryDays,DAYS);
        if (user.getPasswordChangedDate()== null||Instant.now(clock).isAfter(lastChangeDatePlusExpiryDays)) {
            return "true";
        }
        return "false";
    }
}
