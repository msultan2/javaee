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
package com.ssl.rmas.managers;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.utils.ErrorMessage;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;

public abstract class AbstractResultManagerImpl implements ResultsManager {

    @Override
    public void updateResult(Authentication currentUser, UUID activityId, Set<ErrorMessage> errorMessages) {
        Optional<OperationResult> optionalOperationResult = getData(currentUser, activityId);
        if (optionalOperationResult.isPresent()) {
            OperationResult operationResult = optionalOperationResult.get();
            operationResult.setStatus(OperationResult.Status.FAILURE);
            operationResult.addErrorMessages(errorMessages);
            operationResult.setResult("");
            storeResult(currentUser, operationResult);
        }
    }
}
