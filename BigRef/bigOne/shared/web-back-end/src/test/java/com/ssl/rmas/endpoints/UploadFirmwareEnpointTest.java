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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.integration.support.MutableMessageHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.DeviceManager;
import com.ssl.rmas.managers.ResultsManager;
import com.ssl.rmas.ssh.manager.ConnectionParams;

@RunWith(MockitoJUnitRunner.class)
public class UploadFirmwareEnpointTest {

    private UUID activityId;
    private ConnectionParams connectionParams;
    private final List<Path> tempFirmwareFileList = new ArrayList<>();
    private final Authentication testAuth = new TestingAuthenticationToken("bob", "something");

    @Mock
    private DeviceManager deviceManager;
    @Mock
    private ResultsManager resultsManager;
    @Mock
    private OperationResult expResult;
    @InjectMocks
    private UploadFirmwareEnpoint uploadFirmwareEnpoint;

    @Before
    public void setUp() throws IOException {
        activityId = UUID.fromString("11b81f31-f2a6-4295-9ca5-1a3d23c9f49e");
        tempFirmwareFileList.add(Paths.get(System.getProperty("user.home"), "rait", "temp", "testFile1.txt"));
    }

    @Test
    public void uploadFirmwareFile_StoresOperationResultInResultsManager() throws Exception {
        when(deviceManager.lockDeviceAndExecute(deviceManager.uploadFirmware(tempFirmwareFileList), testAuth, activityId, connectionParams)).thenReturn(expResult);

        uploadFirmwareEnpoint.uploadFirmwareFile(getHeader());

        verify(resultsManager).storeResult(testAuth, expResult);
    }

    @Test
    public void uploadFirmwareFile_multipleFiles() throws Exception {
        tempFirmwareFileList.add(Paths.get(System.getProperty("user.home"), "rait", "temp", "testFile2.txt"));
        when(deviceManager.lockDeviceAndExecute(deviceManager.uploadFirmware(tempFirmwareFileList), testAuth, activityId, connectionParams)).thenReturn(expResult);

        uploadFirmwareEnpoint.uploadFirmwareFile(getHeader());

        verify(resultsManager, times(1)).storeResult(testAuth, expResult);
    }

    private MessageHeaders getHeader() {
        Map<String, Object> mapHeader = new HashMap<>();
        mapHeader.put(HeaderKeys.CONNECTION_PARAMS.toString(), connectionParams);
        mapHeader.put(HeaderKeys.ACTIVITY_ID.toString(), activityId);
        mapHeader.put(HeaderKeys.FILE_PATHS.toString(), tempFirmwareFileList);
        mapHeader.put(HeaderKeys.CURRENT_USER.toString(), testAuth);
        return new MutableMessageHeaders(mapHeader);
    }
}
