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
package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.DeviceManager;
import com.ssl.rmas.managers.ResultsManager;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

public class RemoveOldSshPublicKeysEndpoint {

    private ResultsManager resultsManager;
    private DeviceManager deviceManager;

    @Autowired
    public void setResultsManager(ResultsManager resultsManager) {
        this.resultsManager = resultsManager;
    }

    @Autowired
    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    private final Logger LOGGER = LoggerFactory.getLogger(RemoveOldSshPublicKeysEndpoint.class);

    @PreAuthorize("hasPermission(#message.headers['connectionParams'].ipAddress, 'removeOldSshPublicKeys')")
    public void removeOldSshPublicKeys(Message<?> message) {
        ConnectionParams connectionParams = message.getHeaders().get(HeaderKeys.CONNECTION_PARAMS.toString(), ConnectionParams.class);
        Authentication currentUser = message.getHeaders().get(HeaderKeys.CURRENT_USER.toString(), Authentication.class);
        UUID activityId = message.getHeaders().get(HeaderKeys.ACTIVITY_ID.toString(), UUID.class);
        OperationResult operationResult = deviceManager.lockDeviceAndExecute(deviceManager.removeOldSshPublicKeys(), currentUser, activityId, connectionParams);
        LOGGER.debug("Remove old SSH public keys, operation result: {}", operationResult);
        resultsManager.storeResult(currentUser, operationResult);
    }

}
