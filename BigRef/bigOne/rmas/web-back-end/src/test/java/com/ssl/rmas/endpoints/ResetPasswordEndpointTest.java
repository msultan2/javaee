/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
package com.ssl.rmas.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.PasswordChangeToken;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.utils.ErrorMessage;
import com.ssl.rmas.repositories.PasswordChangeTokenRepository;
import com.ssl.rmas.utils.PasswordUtils;

@RunWith(MockitoJUnitRunner.class)
public class ResetPasswordEndpointTest {

    @Mock
    private PasswordChangeTokenRepository tokenRepo;
    @Mock
    private PasswordUtils passwordUtils;
    @Mock
    private Clock clock;

    @InjectMocks
    private ResetPasswordEndpoint endpoint;

    @Before
    public void setup() {
        Instant testInstant = Instant.parse("2016-03-01T09:00:00Z");
        PasswordChangeToken testToken = new PasswordChangeToken();
        testToken.setCreatedTimestamp(testInstant.minus(10, ChronoUnit.MINUTES));
        testToken.setToken("testToken");
        User testUser = new User();
        testUser.setEmail("sergio@ssl.com");
        testUser.setName("Sergio");
        testToken.setUser(testUser);
        Mockito.when(tokenRepo.findOne("testToken")).thenReturn(testToken);
        Mockito.when(clock.instant()).thenReturn(testInstant);

        endpoint.setOnetimeTokenExpiry(604800);
    }

    @Test
    public void allValid() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("tokenId", "testToken");
        Message<String> message = MessageBuilder.createMessage("12345eE^", new MessageHeaders(headers));
        Mockito.when(passwordUtils.isPasswordValid("12345eE^")).thenReturn(true);

        Message<ErrorMessage[]> result = endpoint.resetPassword(message);
        assertNull("Response code not set", result.getHeaders().get(HeaderKeys.HTTP_STATUS_CODE.toString(), Integer.class));
        assertEquals("No errors", 0, result.getPayload().length);
    }

    @Test
    public void missingToken() {
        Map<String, Object> headers = new HashMap<>();
        Message<String> message = MessageBuilder.createMessage("12345eE^", new MessageHeaders(headers));

        Message<ErrorMessage[]> result = endpoint.resetPassword(message);
        assertEquals("Response code set", Integer.valueOf(HttpStatus.BAD_REQUEST.value()), result.getHeaders().get(HeaderKeys.HTTP_STATUS_CODE.toString(), Integer.class));
        assertEquals("Errors present", 1, result.getPayload().length);
    }

    @Test
    public void invalidPassword() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("tokenId", "testToken");
        Message<String> message = MessageBuilder.createMessage("", new MessageHeaders(headers));

        Message<ErrorMessage[]> result = endpoint.resetPassword(message);
        assertEquals("Response code set", Integer.valueOf(HttpStatus.BAD_REQUEST.value()), result.getHeaders().get(HeaderKeys.HTTP_STATUS_CODE.toString(), Integer.class));
        assertEquals("Errors present", 1, result.getPayload().length);
    }

}
