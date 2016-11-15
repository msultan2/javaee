/*
 *
 * JWTAuthenticationFilterTest.java
 *
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
 *  Copyright 2015 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.jwt;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import io.jsonwebtoken.JwtException;

@RunWith(MockitoJUnitRunner.class)
public class JWTAuthenticationFilterTest {

    @Mock private TokenHandler tokenHandler;
    @Mock private JSONExceptionHandlerUtils exceptionUtils;

    @InjectMocks
    private JWTAuthenticationFilter jWTAuthenticationFilter;

    private static final String VALID_TOKEN_AT_FUTURE_TIME = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyTmFtZSIsInJvbGVzIjpbInJvbGUxIiwicm9sZTIiXSwiaWF0IjozMTk1NTU5NDQwMCwiZXhwIjozMTk1NTU5NTAwMH0.iS0wpzRDO3zVL6ScgS42NN4j2fEXEAH3jG7MzW4i-d4_XZWUMi6cB5k6Mxxph4BY4tj8ripVUuFkAihP04LUtg";
    private static final String EXPIERED_TOKEN_AT_PAST_TIME = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyTmFtZSIsInJvbGVzIjpbInJvbGUxIiwicm9sZTIiXSwiaWF0IjozOTg1OTkyMDAsImV4cCI6Mzk4NTk5ODAwfQ.EQVUhUF7vzZkNZlsXWIdODRbEycyPxPiGIwO_WDj118FVizquM6RSu2n3nfd0bU87KDbAXB6OcPBfCJX7ZV31g";

    @Test
    public void doFilter_requestWithNullAuthorization_shouldCallNextChainFilter() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        jWTAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilter_requestWithAuthorization_shouldCallNextChainFilter() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer "+VALID_TOKEN_AT_FUTURE_TIME);
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Mockito.when(tokenHandler.parseUserFromToken(VALID_TOKEN_AT_FUTURE_TIME)).thenReturn(new UsernamePasswordAuthenticationToken("Bob", null));

        jWTAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilter_requestWithNotVaildAuthorization_shouldNotCallNextChainFilter() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer "+EXPIERED_TOKEN_AT_PAST_TIME);
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Mockito.when(tokenHandler.parseUserFromToken(EXPIERED_TOKEN_AT_PAST_TIME)).thenThrow(new JwtException("ExpiredToken"));

        jWTAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
    }
}
