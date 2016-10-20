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
import com.ssl.rmas.ssh.manager.ConnectionParams;
import com.ssl.rmas.utils.ErrorMessage;
import com.ssl.rmas.utils.ssh.SshManager;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class RemoveOldSshPublicKeysManager {

    private SshManager sshManager;
    private final Logger LOGGER = LoggerFactory.getLogger(RemoveOldSshPublicKeysManager.class);
    private final String COMMAND = "deleteoldkey";
    private final String COMMAND_RESULT_SUCCESS = "";

    @Autowired
    public void setSshManager(SshManager sshManager) {
        this.sshManager = sshManager;
    }

    OperationResult removeOldSshPublicKeys(final Authentication currentUser, OperationResult opResult, ConnectionParams connectionParams) {
        try {
            opResult = sshManager.executeCommand(currentUser, opResult, COMMAND, connectionParams);
            if (!OperationResult.Status.FAILURE.equals(opResult.getStatus())) {
                opResult = updateResultFromDeviceResponse(opResult);
            }
        } catch (IOException ex) {
            LOGGER.debug(COMMAND, " command failed for activity id {}", opResult.getActivityId(), ex);
            opResult.setStatus(OperationResult.Status.FAILURE);
            opResult.addErrorMessage(ErrorMessage.FAILED_TO_REMOVE_OLD_SSH_PUBLIC_KEYS);
        }
        return opResult;
    }

    private OperationResult updateResultFromDeviceResponse(OperationResult opResult) {
        switch (opResult.getResult()) {
            case COMMAND_RESULT_SUCCESS:
                opResult.setStatus(OperationResult.Status.SUCCESS);
                break;
            default:
                opResult.setStatus(OperationResult.Status.FAILURE);
                opResult.addErrorMessage(ErrorMessage.UNEXPECTED_RESPONSE);
                LOGGER.info("{} command failed for activity id {} because of a invalid response {}", COMMAND, opResult.getActivityId(), opResult.getResult());
                break;
        }
        opResult.setResult("");
        return opResult;
    }
}
