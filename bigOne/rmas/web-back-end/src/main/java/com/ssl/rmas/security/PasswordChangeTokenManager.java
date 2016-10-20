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

import com.ssl.rmas.entities.PasswordChangeToken;
import com.ssl.rmas.entities.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ssl.rmas.repositories.PasswordChangeTokenRepository;

@Component
public class PasswordChangeTokenManager {

    private final Logger auditLogger = LoggerFactory.getLogger("Audit");
    private PasswordChangeTokenRepository passwordChangeTokenRepository;

    @Autowired
    public void setPasswordResetTokenRepository(PasswordChangeTokenRepository passwordChangeTokenRepository) {
        this.passwordChangeTokenRepository = passwordChangeTokenRepository;
    }

    public PasswordChangeToken createToken(User user) {
        PasswordChangeToken newToken = new PasswordChangeToken();
        newToken.setUser(user);
        PasswordChangeToken persistedToken = passwordChangeTokenRepository.save(newToken);
        auditLogger.info("Password change requested for user {}, new token {}", user.getName(), persistedToken.getToken());
        return persistedToken;
    }

    public void deleteToken(User user) {
        List<PasswordChangeToken> existingTokens = passwordChangeTokenRepository.findAllByUser(user.getId());
        passwordChangeTokenRepository.delete(existingTokens);
    }
}
