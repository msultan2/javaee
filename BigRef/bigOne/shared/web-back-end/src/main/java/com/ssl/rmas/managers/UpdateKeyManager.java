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

import static com.ssl.rmas.entities.OperationResult.Status.*;
import static com.ssl.rmas.utils.ErrorMessage.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import com.ssl.rmas.utils.ssh.SshManager;

@Component
public class UpdateKeyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateKeyManager.class);
    private static final String COMMAND_RESULT_SUCCESS = "ok";
    private static final String COMMAND_RESULT_FAILURE = "no";
    private static final String COMMAND = "updatekey";

    @Autowired
    private SshManager sshManager;

    /**
     * updatekey <key> - append a new SSH public key for rmas_user
     *
     * @param newPublicKey a text string of the key in base64 coding as described in section 3.4 of SSH Public Key File
     * Format RFC 4716. The roadside device must respond "ok" to confirm successful key replacement, and "no" if the
     * process failed.
     *
     * More info in Generic Roadside Device Requirements for Remote Access.
     */
    OperationResult updateKey(final Authentication currentUser, OperationResult opResult, ConnectionParams connectionParams, String newPublicKey) {
        String command = COMMAND + " " + newPublicKey;
        try {
            opResult = sshManager.executeCommand(currentUser, opResult, command, connectionParams);
            if (!FAILURE.equals(opResult.getStatus())) {
                opResult = updateResultFromDeviceResponse(opResult);
            }
        } catch (IOException ex) {
            LOGGER.debug("{} command failed; activity id {}", COMMAND, opResult.getActivityId(), ex);
            opResult.setStatus(FAILURE);
            opResult.addErrorMessage(FAILED_TO_UPDATE_KEY_ON_DEVICE);
        }
        return opResult;
    }

    private OperationResult updateResultFromDeviceResponse(OperationResult opResult) {
        switch (opResult.getResult()) {
            case COMMAND_RESULT_SUCCESS:
                opResult.setStatus(SUCCESS);
                break;
            case COMMAND_RESULT_FAILURE:
                opResult.setStatus(FAILURE);
                opResult.addErrorMessage(FAILED_TO_UPDATE_KEY_ON_DEVICE);
                break;
            default:
                opResult.setStatus(FAILURE);
                opResult.addErrorMessage(UNEXPECTED_RESPONSE);
                LOGGER.info("{} command failed for activity id {} because of a invalid response {}", COMMAND, opResult.getActivityId(), opResult.getResult());
                break;
        }
        opResult.setResult("");
        return opResult;
    }

}
