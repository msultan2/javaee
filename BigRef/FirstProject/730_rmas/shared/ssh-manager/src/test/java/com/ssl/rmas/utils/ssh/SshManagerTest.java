/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SIMULATION SYSTEMS LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 */
package com.ssl.rmas.utils.ssh;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import java.io.IOException;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class SshManagerTest {

    @Mock private Authentication currentUser;
    @Mock private OperationResult mockResult;
    @Mock private ExpiredKeyProvider mockInterface;
    @InjectMocks private SshManager sshManager;

    @Test
    public void downloadFiles_wrongPrivateKey_operationResultWithFailureStatus() throws IOException {
        ConnectionParams connectionParams = new ConnectionParams("ip", 32, "wrongPrivateKey");
        Mockito.when(mockInterface.getExpiredPrivateKeys()).thenReturn(Collections.<String>emptyList());
        OperationResult operationResult = sshManager.downloadFiles(currentUser, mockResult, null, connectionParams, null);
        Mockito.verify(operationResult).setStatus(OperationResult.Status.FAILURE);
    }
}
