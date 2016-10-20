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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private TokenHandler tokenHandler;
    private JSONExceptionHandlerUtils exceptionUtils;

    @Autowired
    public void setTokenHandler(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    @Autowired
    public void setExceptionUtils(JSONExceptionHandlerUtils exceptionUtils) {
        this.exceptionUtils = exceptionUtils;
    }

    @Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = request;
        String authorizationHeader = httpRequest.getHeader("Authorization");
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
                String token = authorizationHeader.substring(7);
                Authentication authentication = tokenHandler.parseUserFromToken(token);
                LOGGER.debug("User \"{}\" token accepted", authentication.getName());
                LOGGER.trace("Setting authentication \"{}\" built from authorization header", authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                LOGGER.debug("null or not valid Authorization header: {}", authorizationHeader);
            }
            filterChain.doFilter(request, response);
        } catch (JwtException | AuthenticationException e) {
            writeExceptionResponse(response, e);
            LOGGER.debug("invalid Authorization header: {}, {}", e.getLocalizedMessage(), authorizationHeader);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    private void writeExceptionResponse(ServletResponse response, Exception e) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        try (PrintWriter writer = response.getWriter()) {
            writer.println(exceptionUtils.exceptionToJsonString(e));
        } catch (IOException ex) {
            LOGGER.warn("Response body could not be written", ex);
        }
    }
}
