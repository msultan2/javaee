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
package com.ssl.rmas.managers;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.utils.ErrorMessage;

@Component("ResultsManager")
public class InMemoryResultsManager extends AbstractResultManagerImpl {

    Map<UUID, OperationResult> results = new ConcurrentHashMap<>();

    @Override
    public Optional<OperationResult> getData(Authentication currentUser, UUID activityID) {
        OperationResult originalResult = results.get(activityID);
        Optional<OperationResult> retval;
        if(originalResult!=null) {
            retval = Optional.of(new RAITOperationResult(originalResult));
        } else {
            retval = Optional.empty();
        }
        return retval;
    }

    @Override
    public void storeResult(Authentication currentUser, OperationResult value) {
        results.put(value.getActivityId(), value);
    }

    @Override
    public OperationResult buildNewResult(Authentication currentUser, UUID activityId) {
        return new RAITOperationResult(activityId);
    }

    private class RAITOperationResult implements OperationResult {
        private final UUID activityId;
        private Status status = Status.PENDING;
        private final Set<ErrorMessage> errorMessages = new HashSet<>();
        private String result = "";

        public RAITOperationResult(UUID activityId) {
            this.activityId = activityId;
        }

        public RAITOperationResult(OperationResult original) {
            this.status = original.getStatus();
            this.errorMessages.addAll(original.getErrorMessages());
            this.result = original.getResult();
            this.activityId = original.getActivityId();
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
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            OperationResult other = (OperationResult) obj;
            if (activityId == null) {
                if (other.getActivityId() != null)
                    return false;
            } else if (!activityId.equals(other.getActivityId()))
                return false;
            if (errorMessages == null) {
                if (other.getErrorMessages() != null)
                    return false;
            } else if (!errorMessages.equals(other.getErrorMessages()))
                return false;
            if (result == null) {
                if (other.getResult() != null)
                    return false;
            } else if (!result.equals(other.getResult()))
                return false;
            if (status != other.getStatus())
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "RAITOperationResult [activityId=" + activityId + ", status=" + status + ", errorMessages="
                    + errorMessages + ", result=" + result + "]";
        }
    }
}
