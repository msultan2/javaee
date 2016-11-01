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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

@RunWith(MockitoJUnitRunner.class)
public class JWTAuthenticationErrorHandlerTest {

    @Mock
    private JSONExceptionHandlerUtils exceptionUtils;
    @InjectMocks private JWTAuthenticationErrorHandler jWTAuthenticationErrorHandler;
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    public void onAuthenticationFailure_contentContainsExceptionMessage() throws IOException, ServletException {
        AuthenticationException exception = new BadCredentialsException("A \"funny\" message with {} odd characters");
        when(exceptionUtils.exceptionToJsonString(exception)).thenReturn("{}");
        jWTAuthenticationErrorHandler.onAuthenticationFailure(null, response, exception);
        assertThat("Response content was not an empty JSON object.", response.getContentAsString().trim(), is(equalTo("{}")));
    }

    @Test
    public void onAuthenticationFailure_responseStatus202() throws IOException, ServletException {
        jWTAuthenticationErrorHandler.onAuthenticationFailure(null, response, new BadCredentialsException(null));
        assertThat(response.getStatus(), is(equalTo(HttpServletResponse.SC_UNAUTHORIZED)));
    }

    @Test
    public void onAuthenticationFailure_responseWithEncodingUtf8() throws IOException, ServletException {
        jWTAuthenticationErrorHandler.onAuthenticationFailure(null, response, new BadCredentialsException(null));
        assertThat(response.getCharacterEncoding(), is(equalTo("UTF-8")));
    }

    @Test
    public void onAuthenticationFailure_responseWithContentTypeJson() throws IOException, ServletException {
        jWTAuthenticationErrorHandler.onAuthenticationFailure(null, response, new BadCredentialsException(null));
        assertThat(response.getContentType(), is(equalTo("application/json")));
    }
}
