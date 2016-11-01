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
package com.ssl.rmas.endpoints;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.security.core.Authentication;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.DeviceManager;
import com.ssl.rmas.managers.ResultsManager;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import org.springframework.security.access.prepost.PreAuthorize;

public class UploadFirmwareEnpoint {

    @Autowired
    private ResultsManager resultsManager;
    @Autowired
    private DeviceManager deviceManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFirmwareEnpoint.class);

    @PreAuthorize("hasPermission(#message.headers['connectionParams'].ipAddress, 'uploadFirmwareFile')")
    public void uploadFirmwareFile(@Headers MessageHeaders headers) throws IOException {
        UUID activityId = headers.get(HeaderKeys.ACTIVITY_ID.toString(), UUID.class);
        Authentication currentUser = headers.get(HeaderKeys.CURRENT_USER.toString(), Authentication.class);
        ConnectionParams connectionParams = headers.get(HeaderKeys.CONNECTION_PARAMS.toString(), ConnectionParams.class);
        List<Path> filePathList = getFilePaths(headers);
        LOGGER.debug("filePaths obtained: {}", filePathList);
        OperationResult operationResult = deviceManager.lockDeviceAndExecute(deviceManager.uploadFirmware(filePathList), currentUser, activityId, connectionParams);
        resultsManager.storeResult(currentUser, operationResult);
    }

    @SuppressWarnings("unchecked")
    private List<Path> getFilePaths(MessageHeaders headers) {
        return headers.get(HeaderKeys.FILE_PATHS.toString(), List.class);
    }
}
