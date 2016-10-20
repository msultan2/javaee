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
import java.util.Objects;
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
    private UserGroup userGroup;

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

    public Optional<String> get2faSecret() {
        if(twoFactorAuthenticationSecret==null) {
            twoFactorAuthenticationSecret = Optional.empty();
        }
        return twoFactorAuthenticationSecret;
    }

    public void set2faSecret(Optional<String> twoFASecret) {
        this.twoFactorAuthenticationSecret = twoFASecret;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 29 * hash + Objects.hashCode(this.roles);
        hash = 29 * hash + Objects.hashCode(this.passwordHash);
        hash = 29 * hash + (this.enabled ? 1 : 0);
        hash = 29 * hash + Objects.hashCode(this.userGroup);
        hash = 29 * hash + Objects.hashCode(twoFactorAuthenticationSecret);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)){
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (this.enabled != other.enabled) {
            return false;
        }
        if (!Objects.equals(this.roles, other.roles)) {
            return false;
        }
        if (!Objects.equals(this.passwordHash, other.passwordHash)) {
            return false;
        }
        if (!Objects.equals(this.userGroup, other.userGroup)) {
            return false;
        }
        if (!Objects.equals(this.twoFactorAuthenticationSecret, other.twoFactorAuthenticationSecret)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "User{"+ super.toString() + " roles=" + roles + ", passwordHash=" + passwordHash + ", enabled=" + enabled + ", userGroup=" + userGroup + ", twoFactorAuthenticationSecret.isPresent=" + get2faSecret().isPresent() + "}";
    }

}
