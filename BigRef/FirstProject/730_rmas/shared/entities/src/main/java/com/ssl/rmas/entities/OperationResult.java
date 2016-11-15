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
 */
package com.ssl.rmas.entities;

import java.util.Set;
import java.util.UUID;

import com.ssl.rmas.utils.ErrorMessage;

public interface OperationResult {

    public enum Status {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILURE,
        PENDING,
        UPDATED;

        public static Status getStatus(Boolean atLeastOneSuccess, Boolean atLeastOneFailure) {
            if (atLeastOneSuccess) {
                if (atLeastOneFailure) {
                    return PARTIAL_SUCCESS;
                } else {
                    return SUCCESS;
                }
            } else {
                return FAILURE;
            }
        }
    }

    Set<ErrorMessage> getErrorMessages();

    Status getStatus();

    void setStatus(final Status status);

    String getResult();

    UUID getActivityId();

    void setResult(final String result);

    void addErrorMessage(final ErrorMessage errorMessage);

    void addErrorMessages(final Set<ErrorMessage> errorMessageSet);

    void setErrorMessage(final ErrorMessage errorMessage);
}
