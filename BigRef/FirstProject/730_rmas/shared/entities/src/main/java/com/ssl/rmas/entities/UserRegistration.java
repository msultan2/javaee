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

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserRegistration extends BaseUser{

    private RequestStatus requestStatus;    
    private String accessRequestReason;    
    private String rejectApproveReason;
    private String accessRequired;
    private Boolean tandcAccepted;
    private String email;   
    private String userGroup;

    public UserRegistration() {
        requestStatus = RequestStatus.PENDING;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Boolean getTandcAccepted() {
        return tandcAccepted;
    }

    public void setTandcAccepted(Boolean tandcAccepted) {
        this.tandcAccepted = tandcAccepted;
    }

    public String getAccessRequestReason() {
        return accessRequestReason;
    }

    public void setAccessRequestReason(String accessRequestReason) {
        this.accessRequestReason = accessRequestReason;
    }

    public String getAccessRequired() {
        return accessRequired;
    }

    public void setAccessRequired(String accessRequired) {
        this.accessRequired = accessRequired;
    }

    public String getRejectApproveReason() {
        return rejectApproveReason;
    }

    public void setRejectApproveReason(String rejectApproveReason) {
        this.rejectApproveReason = rejectApproveReason;
    }
    
    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 29 * hash + Objects.hashCode(this.requestStatus);
        hash = 29 * hash + Objects.hashCode(this.accessRequestReason);
        hash = 29 * hash + Objects.hashCode(this.rejectApproveReason);
        hash = 29 * hash + Objects.hashCode(this.accessRequired);
        hash = 29 * hash + Objects.hashCode(this.tandcAccepted);
        hash = 29 * hash + Objects.hashCode(this.email);
        hash = 29 * hash + Objects.hashCode(this.userGroup);
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
        final UserRegistration other = (UserRegistration) obj;
        if (!Objects.equals(this.accessRequestReason, other.accessRequestReason)) {
            return false;
        }
        if (!Objects.equals(this.rejectApproveReason, other.rejectApproveReason)) {
            return false;
        }
        if (!Objects.equals(this.accessRequired, other.accessRequired)) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        if (this.requestStatus != other.requestStatus) {
            return false;
        }
        if (!Objects.equals(this.tandcAccepted, other.tandcAccepted)) {
            return false;
        }
        if (!Objects.equals(this.userGroup, other.userGroup)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserRegistration{" + "requestStatus=" + requestStatus + ", accessRequestReason=" + accessRequestReason + ", rejectApproveReason=" + 
                rejectApproveReason + ", accessRequired=" + accessRequired + ", tandcAccepted=" + tandcAccepted + ", email=" + email + ", userGroup=" + userGroup + '}';
    }
}
