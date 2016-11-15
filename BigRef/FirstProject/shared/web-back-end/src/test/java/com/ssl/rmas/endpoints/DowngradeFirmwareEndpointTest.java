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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.jcraft.jsch.JSchException;
import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.DeviceManager;
import com.ssl.rmas.managers.ResultsManager;
import com.ssl.rmas.ssh.manager.ConnectionParams;

@RunWith(MockitoJUnitRunner.class)
public class DowngradeFirmwareEndpointTest {

    @InjectMocks
    UpgradeFirmwareEndpoint classUnderTest;

    @Mock
    DeviceManager deviceManager;
    @Mock
    ResultsManager resultsManager;
    @Mock
    private OperationResult expResult;

    private static final UUID ACTIVITY_ID = UUID.fromString("11b81f31-f2a6-4295-9ca5-1a3d23c9f49e");
    private static final ConnectionParams CONNECTION_PARAMS = new ConnectionParams("ipAddress", 256, "privateKey");
    private final Authentication testAuth = new TestingAuthenticationToken("bob", "something");

    @Test
    public void downgradeFirmware_always_storesOperationResultInResultsManager() throws JSchException, IOException, InterruptedException {
        when(deviceManager.lockDeviceAndExecute(deviceManager.downgradeFirmware(), testAuth, ACTIVITY_ID, CONNECTION_PARAMS)).thenReturn(expResult);
        classUnderTest.upgradeFirmware(createMessage(ACTIVITY_ID, CONNECTION_PARAMS));
        verify(resultsManager).storeResult(testAuth, expResult);
    }

    private Message<?> createMessage(UUID activityId, ConnectionParams connectionParams) {
        return new Message<String>() {
            @Override
            public String getPayload() {
                return "";
            }

            @Override
            public MessageHeaders getHeaders() {
                Map<String, Object> map = new HashMap<>();
                map.put(HeaderKeys.CONNECTION_PARAMS.toString(), connectionParams);
                map.put(HeaderKeys.ACTIVITY_ID.toString(), activityId);
                map.put(HeaderKeys.CURRENT_USER.toString(), testAuth);
                return new MessageHeaders(map);
            }
        };
    }
}
