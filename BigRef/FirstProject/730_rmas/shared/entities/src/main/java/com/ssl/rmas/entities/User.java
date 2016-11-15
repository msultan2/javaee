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
 *
 */
package com.ssl.rmas.entities;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document
public class User extends BaseUser {

    private final String defaultRole = "USER";
    private List<String> roles;
    private String passwordHash;
    private boolean enabled;
    private Instant passwordChangedDate;
    @DBRef
    @Indexed
    private UserGroup userGroup;
    private Instant lastSuccessfulLogin;
    private boolean accountNonLocked = true;
    private int loginAttempts;

    @JsonIgnore
    private Optional<String> twoFactorAuthenticationSecret = Optional.empty();

    @Indexed(unique = true)
    private String email;

    public User() {
    }

    public User(UserRegistration userRegistration) {
        setAddress(userRegistration.getAddress());
        setEmployer(userRegistration.getEmployer());
        setMcr(userRegistration.getMcr());
        setMobile(userRegistration.getMobile());
        setName(userRegistration.getName());
        setProjectSponsor(userRegistration.getProjectSponsor());
        setRcc(userRegistration.getRcc());
        setPrimaryPhone(userRegistration.getPrimaryPhone());
        setEmail(userRegistration.getEmail());
        setEnabled(true);
        setRoles(Arrays.asList(defaultRole));
    }

    public Instant getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(Instant lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getPasswordChangedDate() {
        return passwordChangedDate;
    }

    public void setPasswordChangedDate(Instant passwordChangedDate) {
        this.passwordChangedDate = passwordChangedDate;
    }
    
    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public Optional<String> get2faSecret() {
        if(twoFactorAuthenticationSecret==null) {
            twoFactorAuthenticationSecret = Optional.empty();
        }
        return twoFactorAuthenticationSecret;
    }

    public void set2faSecret(Optional<String> twoFASecret) {
        this.twoFactorAuthenticationSecret = twoFASecret;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void increaseLoginAttempts() {
        this.loginAttempts++;
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        User user = (User) o;

        if (enabled != user.enabled) return false;
        if (accountNonLocked != user.accountNonLocked) return false;
        if (!defaultRole.equals(user.defaultRole)) return false;
        if (!roles.equals(user.roles)) return false;
        if (!passwordHash.equals(user.passwordHash)) return false;
        if (!passwordChangedDate.equals(user.passwordChangedDate)) return false;
        if (!userGroup.equals(user.userGroup)) return false;
        if (!lastSuccessfulLogin.equals(user.lastSuccessfulLogin)) return false;
        if (!twoFactorAuthenticationSecret.equals(user.twoFactorAuthenticationSecret)) return false;
        return email.equals(user.email);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + defaultRole.hashCode();
        result = 31 * result + roles.hashCode();
        result = 31 * result + passwordHash.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + passwordChangedDate.hashCode();
        result = 31 * result + userGroup.hashCode();
        result = 31 * result + lastSuccessfulLogin.hashCode();
        result = 31 * result + (accountNonLocked ? 1 : 0);
        result = 31 * result + twoFactorAuthenticationSecret.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "defaultRole='" + defaultRole + '\'' +
                ", roles=" + roles +
                ", enabled=" + enabled +
                ", passwordChangedDate=" + passwordChangedDate +
                ", userGroup=" + userGroup +
                ", lastSuccessfulLogin=" + lastSuccessfulLogin +
                ", accountNonLocked=" + accountNonLocked +
                ", twoFactorAuthenticationSecret=" + twoFactorAuthenticationSecret +
                ", email='" + email + '\'' +
                '}';
    }
}
