/*
 *
 * WebSecurityConfig.java
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
 *  Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import com.ssl.rmas.jwt.JSON403AuthenticationEntryPoint;
import com.ssl.rmas.jwt.JWTAuthenticationErrorHandler;
import com.ssl.rmas.jwt.JWTAuthenticationFilter;
import com.ssl.rmas.jwt.JWTAuthenticationSuccessHandler;
import com.ssl.rmas.security.TotpAuthenticationProvider;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER-10)
@EnableWebSecurity
@EnableGlobalAuthentication
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    private JWTAuthenticationSuccessHandler jwtAuthenticationSuccessHandler;
    private JWTAuthenticationErrorHandler jwtAuthenticationErrorHandler;
    private JWTAuthenticationFilter authenticationFilter;
    private JSON403AuthenticationEntryPoint json403AuthenticationEntryPoint;
    private WebAuthenticationDetailsSource webAuthenticationDetailsSource;
    @Autowired
    public void setJwtAuthenticationSuccessHandler(JWTAuthenticationSuccessHandler jwtAuthenticationSuccessHandler) {
        this.jwtAuthenticationSuccessHandler = jwtAuthenticationSuccessHandler;
    }

    @Autowired
    public void setJwtAuthenticationErrorHandler(JWTAuthenticationErrorHandler jwtAuthenticationErrorHandler) {
        this.jwtAuthenticationErrorHandler = jwtAuthenticationErrorHandler;
    }

    @Autowired
    public void setAuthenticationFilter(JWTAuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Autowired
    public void setJson403AuthenticationEntryPoint(JSON403AuthenticationEntryPoint json403AuthenticationEntryPoint) {
        this.json403AuthenticationEntryPoint = json403AuthenticationEntryPoint;
    }

    @Autowired
    public void setWebAuthenticationDetailsSource(WebAuthenticationDetailsSource webAuthenticationDetailsSource) {
        this.webAuthenticationDetailsSource = webAuthenticationDetailsSource;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.debug("Setting the spring security configuration");
        // @formatter:off
        http
            .formLogin().loginProcessingUrl("/login")
                    .authenticationDetailsSource(webAuthenticationDetailsSource)
                    .failureHandler(jwtAuthenticationErrorHandler)
                    .successHandler(jwtAuthenticationSuccessHandler)
                    .and()
            .csrf().disable()
            .exceptionHandling().authenticationEntryPoint(json403AuthenticationEntryPoint).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and()
            .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/login").permitAll()
                    .antMatchers(HttpMethod.GET, "/users/search/findAllHEApprovers").permitAll()
                    .antMatchers(HttpMethod.POST, "/userRegistrations").permitAll()
                    .antMatchers(HttpMethod.POST, "/users/*/resetPassword").permitAll()
                    .antMatchers(HttpMethod.POST, "/resetPassword/*").permitAll()
                    .antMatchers(HttpMethod.GET, "/rccs").permitAll()
                    .anyRequest().authenticated()
                    .and()
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // @formatter:on
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, TotpAuthenticationProvider totpAuthenticationProvider) throws Exception {
        logger.debug("Setting the spring security global configuration");
        logger.debug("authentication provider {}", totpAuthenticationProvider);

        if(totpAuthenticationProvider==null) {
            throw new IllegalStateException("totpAuthenticationProvider was null");
        }

        // @formatter:off
        auth
            .authenticationProvider(totpAuthenticationProvider);
        // @formatter:on
    }
}
