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

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import com.ssl.rmas.utils.ErrorMessage;
import com.ssl.rmas.utils.ssh.SshManager;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

@RunWith(MockitoJUnitRunner.class)
public class DowngradeFirmwareManagerTest {
    
    @InjectMocks
    DowngradeFirmwareManager classUnderTest;
    @Mock
    SshManager sshManager;
    @Mock
    private OperationResult operationResult;
    
    private final String command = "firmwaredowngrade";
    private final String commandResultSuccess = "yes";
    private final String commandResultNotFound = "notfound";
    private final String commandError = "error";
    private final ConnectionParams connectionParams = new ConnectionParams("ipAddress", 256, "privateKey");
    private final Authentication testAuth = new TestingAuthenticationToken("bob", "something");
    
    @Test
    public void downgradeFirmware_catchsIOException_failedToDowngrade() throws IOException {
        Mockito.when(sshManager.executeCommand(testAuth, operationResult, command, connectionParams)).thenThrow(new IOException());
        classUnderTest.perform(testAuth, operationResult, connectionParams);
        Mockito.verify(operationResult).setStatus(OperationResult.Status.FAILURE);
        Mockito.verify(operationResult).addErrorMessage(ErrorMessage.FAILED_TO_DOWNGRADE_FIRMWARE_ON_DEVICE);
    }
    
    @Test
    public void downgradeFirmware_successResponse_success() throws IOException {
        Mockito.when(sshManager.executeCommand(testAuth, operationResult, command, connectionParams)).thenReturn(operationResult);
        Mockito.when(operationResult.getResult()).thenReturn(commandResultSuccess);
        classUnderTest.perform(testAuth, operationResult, connectionParams);
        Mockito.verify(operationResult).setStatus(OperationResult.Status.SUCCESS);
    }   
    
    @Test
    public void downgradeFirmware_notFoundResponse_FailedWithNotFoundMessage() throws IOException {
        Mockito.when(sshManager.executeCommand(testAuth, operationResult, command, connectionParams)).thenReturn(operationResult);
        Mockito.when(operationResult.getResult()).thenReturn(commandResultNotFound);
        classUnderTest.perform(testAuth, operationResult, connectionParams);
        Mockito.verify(operationResult).setStatus(OperationResult.Status.FAILURE);
        Mockito.verify(operationResult).addErrorMessage(ErrorMessage.FIRMWARE_NOT_FOUND_ON_ROADSIDE_DEVICE);
    }

    @Test
    public void downgradeFirmware_otherResponse_FailedWithUnexpectedResponseMessage() throws IOException {
        Mockito.when(sshManager.executeCommand(testAuth, operationResult, command, connectionParams)).thenReturn(operationResult);
        Mockito.when(operationResult.getResult()).thenReturn(commandError);
        classUnderTest.perform(testAuth, operationResult, connectionParams);
        Mockito.verify(operationResult).setStatus(OperationResult.Status.FAILURE);
        Mockito.verify(operationResult).addErrorMessage(ErrorMessage.UNEXPECTED_RESPONSE);
    }
}
