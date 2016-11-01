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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.security.core.Authentication;
import com.ssl.rmas.entities.DownloadLogDates;
import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.DeviceManager;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.prepost.PreAuthorize;

public class DownloadLogsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadLogsEndpoint.class);

    @Autowired
    private DeviceManager deviceManager;

    private String downloadDataDir;

    public DownloadLogsEndpoint(String downloadDataDir) {
        this.downloadDataDir = downloadDataDir;
    }

    @PreAuthorize("hasPermission(#message.headers['connectionParams'].ipAddress, 'downloadLogs')")
    public Message<DownloadLogDates> downloadLogs(Message<DownloadLogDates> message) {
        ConnectionParams connectionParams = message.getHeaders().get(HeaderKeys.CONNECTION_PARAMS.toString(), ConnectionParams.class);
        Authentication currentUser = message.getHeaders().get(HeaderKeys.CURRENT_USER.toString(), Authentication.class);
        UUID activityId = message.getHeaders().get(HeaderKeys.ACTIVITY_ID.toString(), UUID.class);
        OperationResult operationResult = deviceManager.lockDeviceAndExecute(deviceManager.downloadLogs(downloadDataDir, message.getPayload().getStartDate(), message.getPayload().getEndDate()), currentUser, activityId, connectionParams);
        LOGGER.debug("Downloaded log files, operation result: {}", operationResult);
        return MessageBuilder.withPayload(message.getPayload())
                .copyHeaders(message.getHeaders())                
                .setHeader(HeaderKeys.OPERATION_RESULT.toString(), operationResult)
                .build();
    }
}
