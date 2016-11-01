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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import com.ssl.rmas.utils.ErrorMessage;
import com.ssl.rmas.utils.ssh.SshManager;

@Component
public abstract class FirmwareManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirmwareManager.class);
    private static final String COMMAND_RESULT_SUCCESS = "yes";
    private static final String COMMAND_RESULT_NOT_FOUND = "notfound";

    @Autowired
    private SshManager sshManager;

    @PreAuthorize("hasRole('LOGGED_IN_WITH_2FA')")
    OperationResult perform(final Authentication currentUser, OperationResult opResult, ConnectionParams connectionParams) {
        try {
            opResult = sshManager.executeCommand(currentUser, opResult, command(), connectionParams);
            if (!OperationResult.Status.FAILURE.equals(opResult.getStatus())) {
                opResult = updateResultFromDeviceResponse(opResult);
            }
        } catch (IOException ex) {
            LOGGER.debug("{} command failed for activity id {}", command(), opResult.getActivityId(), ex);
            opResult.setStatus(OperationResult.Status.FAILURE);
            opResult.addErrorMessage(getErrorMessage());
        }
        return opResult;
    }

    private OperationResult updateResultFromDeviceResponse(OperationResult opResult) {
        switch (opResult.getResult()) {
            case COMMAND_RESULT_SUCCESS:
                opResult.setStatus(OperationResult.Status.SUCCESS);
                break;
            case COMMAND_RESULT_NOT_FOUND:
                opResult.setStatus(OperationResult.Status.FAILURE);
                opResult.addErrorMessage(ErrorMessage.FIRMWARE_NOT_FOUND_ON_ROADSIDE_DEVICE);
                break;
            default:
                opResult.setStatus(OperationResult.Status.FAILURE);
                opResult.addErrorMessage(ErrorMessage.UNEXPECTED_RESPONSE);
                LOGGER.info("{} command failed for activity id {} because of a invalid response {}", command(), opResult.getActivityId(), opResult.getResult());
                break;
        }
        opResult.setResult("");
        return opResult;
    }

    abstract String command();

    abstract ErrorMessage getErrorMessage();

}
