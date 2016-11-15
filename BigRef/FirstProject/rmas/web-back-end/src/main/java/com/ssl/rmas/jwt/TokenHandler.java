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

import com.ssl.rmas.entities.DeviceFilter;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.entities.UserGroup;
import com.ssl.rmas.security.RMASUserDetails;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.ssl.rmas.security.TotpPreAuthenticatedDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Clock;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@PropertySource("classpath:config/application.properties")
public class TokenHandler {

	@Value("${security.jwt.secretKey:}")
	private String jwtSecretKey;
	@Value("${security.jwt.timeout:600}")
	private Integer jwtTimeout;

    private final String rolesField = "roles";
    private final String nameField = "name";
    private final String emailField = "email";
    private final String userGroupIdField = "userGroupField";
    private Clock clock;
    
    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Authentication parseUserFromToken(String token) {
        Jws<Claims> jwsClaims = Jwts.parser()
            .setSigningKey(jwtSecretKey)
            .parseClaimsJws(token);
        Claims body = jwsClaims.getBody();
        String id = body.getSubject();
        @SuppressWarnings("unchecked")
        List<String> roleStrings = (List<String>) body.get(rolesField);
        if (roleStrings == null) {
            throw new IllegalArgumentException("Token for " + id + " should have at least one role");
        }
        List<GrantedAuthority> roles = roleStrings.stream()
            .filter(Objects::nonNull)
            .filter(role -> role.startsWith("ROLE_"))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        User user = new User();
        user.setRoles(roleStrings);
        user.setId(id);
        Object nameClaim = body.get(nameField);
        Object emailClaim = body.get(emailField);
        Object userGroupIdClaim = body.get(userGroupIdField);
        if(nameClaim==null || emailClaim==null) {
            throw new AuthenticationCredentialsNotFoundException("Credentials are missing");
        }
        user.setName(nameClaim.toString());
        user.setEmail(emailClaim.toString());
        user.setUserGroup(createUserGroup(roleStrings, userGroupIdClaim.toString()));
        RMASUserDetails rmasUser = new RMASUserDetails(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(rmasUser, null, roles);
        auth.setDetails(new TotpPreAuthenticatedDetails());
        return auth;
    }

    public String createTokenForUser(Authentication authentication) {
        return createTokenForUser(authentication, Instant.now(clock));
    }

    public String createTokenForUser(Authentication authentication, Instant instant) {
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        RMASUserDetails userDetails = (RMASUserDetails) authentication.getPrincipal();
        roles.addAll(userDetails.getUser().getUserGroup().getDeviceFilter().conditions());
        return createTokenForUser(userDetails.getUserId(), userDetails.getUsername(), userDetails.getUsersName(), roles,
            userDetails.getUser().getUserGroup().getId(), instant);
    }

    private String createTokenForUser(String id, String email, String usersName, List<String> roles, String userGroupId, Instant instant) {
        return Jwts.builder()
            .setSubject(id)
            .claim(rolesField, roles)
            .claim(nameField, usersName)
            .claim(emailField, email)
            .claim(userGroupIdField, userGroupId)
            .setIssuedAt(new Date(instant.toEpochMilli()))
            .setExpiration(new Date(instant.plusSeconds(jwtTimeout).toEpochMilli()))
            .signWith(SignatureAlgorithm.HS512, jwtSecretKey)
            .compact();
    }

	public void setJwtSecretKey(String jwtSecretKey) {
		this.jwtSecretKey = jwtSecretKey;
	}

	public void setJwtTimeout(int jwtTimeout) {
		this.jwtTimeout = jwtTimeout;
	}

    private UserGroup createUserGroup(final List<String> roleStrings, final String userGroupId) {
        List<String> conditions = extractConditions(roleStrings);
        DeviceFilter filter = createDeviceFilter(conditions);
        UserGroup group = new UserGroup();
        group.setDeviceFilter(filter);
        group.setId(userGroupId);
        return group;
    }

    private DeviceFilter createDeviceFilter(final List<String> conditions) {
        DeviceFilter filter = new DeviceFilter();
        filter.setConditions(conditions);
        return filter;
    }

    private List<String> extractConditions(final List<String> roleStrings) {
        return roleStrings.stream()
            .filter(Objects::nonNull)
            .filter(roleString -> roleString.startsWith("DEVICE_FILTER_CONDITION_"))
            .collect(Collectors.toList());
    }

}
