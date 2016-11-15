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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import com.ssl.rmas.managers.DeviceManager;
import com.ssl.rmas.managers.ResultsManager;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

public class StaticDataEndpoint {

    private ResultsManager resultsManager;
    private DeviceManager deviceManager;

    private String staticDataRelativeOutputDir;

    public StaticDataEndpoint(String staticDataRelativeOutputDir) {
        this.staticDataRelativeOutputDir = staticDataRelativeOutputDir;
    }

    @Autowired
    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Autowired
    public void setResultsManager(ResultsManager resultsManager) {
        this.resultsManager = resultsManager;
    }

    public void setStaticDataRelativeOutputDir(String staticDataRelativeOutputDir) {
        this.staticDataRelativeOutputDir = staticDataRelativeOutputDir;
    }

    @PreAuthorize("hasPermission(#message.headers['connectionParams'].ipAddress, 'getStaticDataFile')")
    public void getStaticDataFile(Message<?> message) {
        ConnectionParams connectionParams = message.getHeaders().get(HeaderKeys.CONNECTION_PARAMS.toString(), ConnectionParams.class);
        Authentication currentUser = message.getHeaders().get(HeaderKeys.CURRENT_USER.toString(), Authentication.class);
        UUID activityId = message.getHeaders().get(HeaderKeys.ACTIVITY_ID.toString(), UUID.class);
        OperationResult operationResult = deviceManager.lockDeviceAndExecute(deviceManager.getStaticData(staticDataRelativeOutputDir), currentUser, activityId, connectionParams);
        resultsManager.storeResult(currentUser, operationResult);
    }
}
