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
package com.ssl.rmas.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.ssl.rmas.utils.ErrorMessage;

@Document(collection="operationResults")
public class RMASOperationResult implements OperationResult {

    @Id
    private final UUID activityId;
    @Indexed
    private final String userId;
    private Status status = Status.PENDING;
    private Set<ErrorMessage> errorMessages = new HashSet<>();
    private String result = "";

    @PersistenceConstructor
    public RMASOperationResult(String userId, UUID activityId, Status status, Set<ErrorMessage> errorMessages, String result) {
        this.userId = userId;
        this.activityId = activityId;
        this.status = status;
        this.errorMessages = errorMessages;
        this.result = result;
    }

    public RMASOperationResult(String userId, UUID activityId) {
        this.userId = userId;
        this.activityId = activityId;
    }

    public RMASOperationResult(OperationResult result, String userId) {
        this.userId = userId;
        this.activityId = result.getActivityId();
        this.status = result.getStatus();
        this.errorMessages = result.getErrorMessages();
        this.result = result.getResult();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public UUID getActivityId() {
        return activityId;
    }

    @Override
    public Set<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    @Override
    public void addErrorMessage(ErrorMessage errorMessage) {
        errorMessages.add(errorMessage);
    }

    @Override
    public void addErrorMessages(Set<ErrorMessage> errorMessageSet) {
        for (ErrorMessage errorMessage : errorMessageSet) {
            addErrorMessage(errorMessage);
        }
    }

    public String getUserId() {
        return userId;
    }
    
    @Override
    public void setErrorMessage(ErrorMessage errorMessage){
        errorMessages.clear();
        errorMessages.add(errorMessage);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((activityId == null) ? 0 : activityId.hashCode());
        result = prime * result + ((errorMessages == null) ? 0 : errorMessages.hashCode());
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof RMASOperationResult))
            return false;
        RMASOperationResult other = (RMASOperationResult) obj;
        if (activityId == null) {
            if (other.activityId != null)
                return false;
        } else if (!activityId.equals(other.activityId))
            return false;
        if (errorMessages == null) {
            if (other.errorMessages != null)
                return false;
        } else if (!errorMessages.equals(other.errorMessages))
            return false;
        if (result == null) {
            if (other.result != null)
                return false;
        } else if (!result.equals(other.result))
            return false;
        if (status != other.status)
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RMASOperationResult [userId=" + userId + ", activityId=" + activityId + ", status=" + status
                + ", errorMessages=" + errorMessages + ", result=" + result + "]";
    }

}
