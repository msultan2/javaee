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
 * Copyright 2015 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.jwt;

import com.ssl.rmas.entities.User;
import com.ssl.rmas.repositories.UserRepository;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

@RunWith(MockitoJUnitRunner.class)
public class JWTAuthenticationSuccessHandlerTest {

    @Mock
    private TokenHandler tokenHandler;
    @Mock
    private UserRepository userRepository;
    @Mock 
    private User mockUser;    
    @Mock
    private Clock clock;
    
    @InjectMocks
    private JWTAuthenticationSuccessHandler jWTAuthenticationSuccessHandler;

    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final Authentication auth = new TestingAuthenticationToken("Bob", null);
    
    @Before
    public void beforeEachTest() {
        Instant testInstant = Instant.parse("2016-03-01T09:00:00Z");
        when(clock.instant()).thenReturn(testInstant);
        when(mockUser.getEmail()).thenReturn("Bob");
        when(mockUser.getLastSuccessfulLogin()).thenReturn(testInstant);
        when(mockUser.getPasswordChangedDate()).thenReturn(testInstant);
        when(userRepository.findOneByEmail("Bob")).thenReturn(mockUser);
    }

    @Test
    public void onAuthenticationSuccess_responseStatus200() throws IOException, ServletException {
        jWTAuthenticationSuccessHandler.onAuthenticationSuccess(null, response, auth);
        Assert.assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_OK));
    }

    @Test
    public void onAuthenticationSuccess_responseWithEncodingUtf8() throws IOException, ServletException {
        jWTAuthenticationSuccessHandler.onAuthenticationSuccess(null, response, auth);
        Assert.assertThat(response.getCharacterEncoding(), equalTo("UTF-8"));
    }

    @Test
    public void onAuthenticationSuccess_responseWithTokenInHeader() throws IOException, ServletException {
        when(tokenHandler.createTokenForUser((Authentication)notNull())).thenReturn("token");
        jWTAuthenticationSuccessHandler.onAuthenticationSuccess(null, response, auth);
        Assert.assertThat(response.getHeader("Authorization"), equalTo("Bearer token"));
    }
}
