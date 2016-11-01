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

import static com.ssl.rmas.utils.ErrorMessage.FAILED_TO_UPLOAD_ONE_OR_MORE_FILES;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ssl.rmas.entities.OperationResult;
import static com.ssl.rmas.entities.OperationResult.Status.*;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import com.ssl.rmas.utils.ssh.SshManager;
import java.io.IOException;

@Component
public class UploadFirmwareManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFirmwareManager.class);
    private static final Path UPLOAD_FIRMWARE_REMOTE_PATH = Paths.get("software update");

    @Autowired
    private SshManager sshManager;

    @PreAuthorize("hasRole('LOGGED_IN_WITH_2FA')")
    OperationResult uploadFirmwareFile(final Authentication currentUser, OperationResult operationResult, ConnectionParams connectionParams, List<Path> filePathList) {
        try {
            LOGGER.debug("Received connectionParams {} and firmware file {} for the activity id: {}", connectionParams, filePathList, operationResult.getActivityId());
            operationResult = sshManager.uploadFiles(currentUser, operationResult, UPLOAD_FIRMWARE_REMOTE_PATH, filePathList, connectionParams);
            LOGGER.debug("Uploaded firmware file result: {} for the activity id: {}", operationResult, operationResult.getActivityId());
            if (operationResult.getStatus().equals(PARTIAL_SUCCESS)) {
                operationResult.setStatus(FAILURE);
                operationResult.addErrorMessage(FAILED_TO_UPLOAD_ONE_OR_MORE_FILES);
                LOGGER.debug("Failed to upload one or more files");
            }
            for (Path tempPath : filePathList) {
                if (!FileUtils.deleteQuietly(tempPath.toFile())) {
                    LOGGER.info("Unable to delete {} for the activity id: {}", tempPath.toFile(), operationResult.getActivityId());
                }
            }
        } catch (IOException ex) {
            LOGGER.debug("Failed to upload one or more files for activity id {}", operationResult.getActivityId(), ex);
            operationResult.setStatus(FAILURE);
            operationResult.addErrorMessage(FAILED_TO_UPLOAD_ONE_OR_MORE_FILES);
        }
        return operationResult;
    }
}
