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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.ResultsManager;

@RunWith(MockitoJUnitRunner.class)
public class IdGeneratorTransformerTest {

    @Mock
    private ResultsManager resultsManager;
    @Mock
    private OperationResult operationResult;

    @InjectMocks
    private IdGeneratorTransformer idGeneratorTransformer;

    private static final String PUBLIC_KEY = "publicKey";
    private final Authentication testAuth = new TestingAuthenticationToken("bob", "something");

    @Test
    public void generateUuid_always_createsActivityIdInTheHeader() {
        Message<?> newMessage = idGeneratorTransformer.generateUuid(createMessage(PUBLIC_KEY));
        UUID generatedActivityId = newMessage.getHeaders().get(HeaderKeys.ACTIVITY_ID.toString(), UUID.class);
        assertNotNull(generatedActivityId);
    }

    @Test
    public void generateUuid_always_storesOperationResultInResultManager() {
        Mockito.when(resultsManager.buildNewResult(Mockito.any(), Mockito.isA(UUID.class))).thenReturn(operationResult);

        Message<?> newMessage = idGeneratorTransformer.generateUuid(createMessage(PUBLIC_KEY));
        newMessage.getHeaders().get(HeaderKeys.ACTIVITY_ID.toString(), UUID.class);
        verify(resultsManager).storeResult(testAuth, operationResult);
    }

    private Message<String> createMessage(String publicKey) {
        return new Message<String>() {
            @Override
            public String getPayload() {
                return "{publicKey: \""+publicKey+"\"}";
            }

            @Override
            public MessageHeaders getHeaders() {
                Map<String, Object> map = new HashMap<>();
                map.put(HeaderKeys.CURRENT_USER.toString(), testAuth);
                return new MessageHeaders(map);
            }
        };
    }
}
