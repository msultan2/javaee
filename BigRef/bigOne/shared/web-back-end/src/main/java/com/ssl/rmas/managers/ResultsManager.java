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
package com.ssl.rmas.managers;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.utils.ErrorMessage;
import java.util.Set;

public interface ResultsManager {

    /**
     * Returns a copy of the operation result. No changes will be persisted until storeResults is called.
     * If no activity exists with the provided activity ID a new result object will be created in the pending state.
     *
     * @param activityID the activity ID of the operation to get the results for
     * @return the copy of the operation results for the requested activity.
     */
    Optional<OperationResult> getData(Authentication currentUser, UUID activityID);

    void storeResult(Authentication currentUser, OperationResult result);

    void updateResult(Authentication currentUser, UUID activityId, Set<ErrorMessage> errorMessages);

    OperationResult buildNewResult(Authentication currentUser, UUID activityId);

}