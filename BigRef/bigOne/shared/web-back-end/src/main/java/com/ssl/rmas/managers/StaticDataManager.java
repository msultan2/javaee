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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import com.ssl.rmas.utils.ErrorMessage;
import com.ssl.rmas.utils.ssh.SshManager;
import java.io.IOException;

@Component
public class StaticDataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticDataManager.class);
    private static final Path STATICDATA_REMOTE_PATH = Paths.get("logs", "staticdata.log");

    private SshManager sshManager;
    private StaticDataProcessor staticDataProcessor;

    OperationResult getStaticData(final Authentication currentUser, OperationResult opResult, final ConnectionParams connectionParams,
            final String relativeOutputDir) {
        LOGGER.debug("Getting staticdata.log...");
        final Path absoluteOutputDir = Paths.get(System.getProperty("user.home"), relativeOutputDir, connectionParams.getIpAddress());
        final Path absoluteOutputFile = absoluteOutputDir.resolve(STATICDATA_REMOTE_PATH.getFileName());
        boolean createdOutputDir = absoluteOutputDir.toFile().mkdirs();
        if(createdOutputDir) {
            LOGGER.debug("Created output directory {}", absoluteOutputDir);
        }
        try {
            opResult = sshManager.downloadFiles(currentUser, opResult, Collections.singletonList(STATICDATA_REMOTE_PATH), connectionParams, absoluteOutputDir);
            staticDataProcessor.processStaticData(connectionParams.getIpAddress(), opResult, absoluteOutputFile);
            LOGGER.debug("Got staticdata.log, operation result: {}", opResult);
        } catch (IOException ex) {
            LOGGER.debug("Get static data failed for activity id {}", opResult.getActivityId(), ex);
            opResult.setStatus(OperationResult.Status.FAILURE);
            opResult.addErrorMessage(ErrorMessage.FAILED_TO_DOWNLOAD_STATIC_DATA);
        }
        return opResult;
    }

    @Autowired
    @Required
    public void setSshManager(SshManager sshManager) {
        this.sshManager = sshManager;
    }

    @Autowired
    @Required
    public void setStaticDataProcessor(StaticDataProcessor staticDataProcessor) {
        this.staticDataProcessor = staticDataProcessor;
    }
}
