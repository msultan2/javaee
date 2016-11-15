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

import static com.ssl.rmas.utils.ErrorMessage.DEVICE_ALREADY_IN_USE;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.entities.OperationResult.Status;
import com.ssl.rmas.ssh.manager.ConnectionParams;

/**
 * Entry point for RMAS device.
 */
@Component
public class DeviceManager {

    private DeviceUsageRegistry usageRegistry;
    private StaticDataManager staticDataManager;
    private UpdateKeyManager updateKeyManager;
    private DownloadLogsManager downloadLogsManager;
    private UploadFirmwareManager uploadFirmwareManager;
    private UpgradeFirmwareManager upgradeFirmwareManager;
    private DowngradeFirmwareManager downgradeFirmwareManager;
    private ResetDeviceManager resetDeviceManager;
    private VerifyManager verifyManager;
    private ResultsManager resultManager;
    private RemoveOldSshPublicKeysManager removeOldSshPublicKeysManager;

    @Autowired
    public void setDeviceUsageRegistry(DeviceUsageRegistry usageRegistry) {
        this.usageRegistry = usageRegistry;
    }

    @Autowired
    public void setStaticDataManager(StaticDataManager staticDataManager) {
        this.staticDataManager = staticDataManager;
    }

    @Autowired
    public void setUpdateKeyManager(UpdateKeyManager updateKeyManager) {
        this.updateKeyManager = updateKeyManager;
    }

    @Autowired
    public void setDownloadLogsManager(DownloadLogsManager downloadLogsManager) {
        this.downloadLogsManager = downloadLogsManager;
    }

    @Autowired
    public void setUploadFirmwareManager(UploadFirmwareManager uploadFirmwareManager) {
        this.uploadFirmwareManager = uploadFirmwareManager;
    }

    @Autowired
    public void setUpgradeFirmwareManager(UpgradeFirmwareManager upgradeFirmwareManager) {
        this.upgradeFirmwareManager = upgradeFirmwareManager;
    }

    @Autowired
    public void setDowngradeFirmwareManager(DowngradeFirmwareManager downgradeFirmwareManager) {
        this.downgradeFirmwareManager = downgradeFirmwareManager;
    }

    @Autowired
    public void setResetDeviceManager(ResetDeviceManager resetDeviceManager) {
        this.resetDeviceManager = resetDeviceManager;
    }
    
    @Autowired
    public void setVerifyManager(VerifyManager verifyManager) {
        this.verifyManager = verifyManager;
    }

    @Autowired
    public void setResultsManager(ResultsManager resultManager) {
        this.resultManager = resultManager;
    }

    @Autowired
    public void setRemoveOldSshPublicKeysManager(RemoveOldSshPublicKeysManager removeOldSshPublicKeysManager) {
        this.removeOldSshPublicKeysManager = removeOldSshPublicKeysManager;
    }

    @FunctionalInterface
    public interface Operation {
        OperationResult execute(final Authentication currentUser, final OperationResult opResult, final ConnectionParams connectionParams);
    }
    
    public OperationResult lockDeviceAndExecute(final Operation operation, final Authentication currentUser, final UUID activityId, final ConnectionParams connectionParams) {
        String ipAddress = connectionParams.getIpAddress();
        OperationResult opResult = resultManager.getData(currentUser, activityId).get();
        if (usageRegistry.setInUse(ipAddress)) {
            try {
                return operation.execute(currentUser, opResult, connectionParams);
            } finally {
                usageRegistry.setNotInUse(ipAddress);
            }
        } else {
            return deviceIsInUseResult(currentUser, activityId);
        }
    }
    
    public Operation getStaticData(final String relativeOutputDir) {
        return (currentUser, opResult, connectionParams) -> {
            return staticDataManager.getStaticData(currentUser, opResult, connectionParams, relativeOutputDir);
        };
    }
    
    public Operation updateKey(final String newPublicKey) {
        return (currentUser, opResult, connectionParams) -> {
            return updateKeyManager.updateKey(currentUser, opResult, connectionParams, newPublicKey);
        };
    }
    
    public Operation downloadLogs(final String outputDir, final LocalDate fromDate, final LocalDate toDate) {
        return (currentUser, opResult, connectionParams) -> {
            return downloadLogsManager.downloadLogFiles(outputDir, currentUser, opResult, connectionParams, fromDate, toDate);
        };
    }
    
    public Operation uploadFirmware(final List<Path> filePathList) {
        return (currentUser, opResult, connectionParams) -> {
            return uploadFirmwareManager.uploadFirmwareFile(currentUser, opResult, connectionParams, filePathList);
        };
    }
    
    public Operation upgradeFirmware() {
        return (currentUser, opResult, connectionParams) -> {
            return upgradeFirmwareManager.perform(currentUser, opResult, connectionParams);
        };
    }
    
    public Operation downgradeFirmware() {
        return (currentUser, opResult, connectionParams) -> {
            return downgradeFirmwareManager.perform(currentUser, opResult, connectionParams);
        };
    }
    
    public Operation resetDevice(String pewNumber) {
        return (currentUser, opResult, connectionParams) -> {
            return resetDeviceManager.resetDevice(currentUser, opResult, connectionParams, pewNumber);
        };
    }
    
    public Operation verify() {
        return (currentUser, opResult, connectionParams) -> {
            return verifyManager.verify(currentUser, opResult, connectionParams);
        };
    }

    public Operation removeOldSshPublicKeys() {
        return (currentUser, opResult, connectionParams) -> {
            return removeOldSshPublicKeysManager.removeOldSshPublicKeys(currentUser, opResult, connectionParams);
        };
    }

    private OperationResult deviceIsInUseResult(Authentication currentUser, final UUID activityId) {
        OperationResult result = resultManager.buildNewResult(currentUser, activityId);
        result.setStatus(Status.FAILURE);
        result.addErrorMessage(DEVICE_ALREADY_IN_USE);
        result.setResult("");
        return result;
    }
}
