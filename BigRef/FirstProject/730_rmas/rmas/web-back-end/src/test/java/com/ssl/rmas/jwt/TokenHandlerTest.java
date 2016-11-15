/*
 *
 * TokenHandlerTest.java
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

import com.ssl.rmas.entities.DeviceFilter;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.entities.UserGroup;
import com.ssl.rmas.security.RMASUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RunWith(MockitoJUnitRunner.class)
public class TokenHandlerTest {

    private final Instant FUTURE_TIME = Instant.parse("2982-08-19T10:00:00.00Z");
    private final String USER_Id = "userId";
    private final String USER_NAME = "userName";
    private final String USER_PASSWORD = "userPassword";
    private final String USER_EMAIL = "userEmail";
    private final String ROLE_1 = "ROLE_1";
    private final String ROLE_2 = "ROLE_2";
    private final GrantedAuthority GRANTED_AUTHORITY_1 = new SimpleGrantedAuthority(ROLE_1);
    private final GrantedAuthority GRANTED_AUTHORITY_2 = new SimpleGrantedAuthority(ROLE_2);
    private final List<GrantedAuthority> GRANTED_AUTHORITIES = Arrays.asList(GRANTED_AUTHORITY_1, GRANTED_AUTHORITY_2);

    private final String VALID_TOKEN_AT_FUTURE_TIME = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VySWQiLCJyb2xlcyI6WyJST0xFXzEiLCJST0xFXzIiLCJERVZJQ0VfRklMVEVSX0NPTkRJVElPTl9pcEFkZHJlc3M9MTAuMTYzLjQ5LjY4IiwiREVWSUNFX0ZJTFRFUl9DT05ESVRJT05fbWFpbnRlbmFuY2VDb250cmFjdFJlZ2lvbj1Tb3V0aCBXZXN0IiwiREVWSUNFX0ZJTFRFUl9DT05ESVRJT05fcmNjUmVnaW9uPVNvdXRoIFdlc3QiLCJERVZJQ0VfRklMVEVSX0NPTkRJVElPTl9tYW51ZmFjdHVyZXI9U1NMIl0sIm5hbWUiOiJ1c2VyTmFtZSIsImVtYWlsIjoidXNlckVtYWlsIiwidXNlckdyb3VwRmllbGQiOiI1NzcyODU2ZDY4NDgxMjQ5ZmJmNjYwMDAiLCJpYXQiOjMxOTU1NTk0NDAwLCJleHAiOjMxOTU1NTk1MDAwfQ.2WlMf_xAE6C5Ijbn1UTfKGqRPEwqTQ5eDFs9vmoB8HpXMH-I6Nvn-UKMZphFuC3ucFUjEQ7pClSHHsa-UIi4iQ";
    private final String EXPIERED_TOKEN_AT_PAST_TIME = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyTmFtZSIsInJvbGVzIjpbInJvbGUxIiwicm9sZTIiXSwiaWF0IjozOTg1OTkyMDAsImV4cCI6Mzk4NTk5ODAwfQ.EQVUhUF7vzZkNZlsXWIdODRbEycyPxPiGIwO_WDj118FVizquM6RSu2n3nfd0bU87KDbAXB6OcPBfCJX7ZV31g";
    private final String TOKEN_WITHOUT_ROLES_AT_FUTURE_TIME = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyTmFtZSIsImlhdCI6MzE5NTU1OTQ0MDAsImV4cCI6MzE5NTU1OTUwMDB9.aIr8r4Ed2bIz4c9E6OBpBMPBLCHJz9RDu1t5MuTTKP8Ry32g3TU128elLphitNf2yuCaAs3eC6X644PKGpnk-Q";

    private User user;
    private RMASUserDetails rmasUser;
    private Authentication authentication;
    @Mock
    private Clock clock;
    @InjectMocks
    private TokenHandler tokenHandler;

    public TokenHandlerTest() {
        user = new User();
        user.setId(USER_Id);
        user.setName(USER_NAME);
        user.setPasswordHash(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setRoles(Arrays.asList(ROLE_1, ROLE_2));
        user.setUserGroup(userGroup());
        rmasUser = new RMASUserDetails(user);
        authentication = new UsernamePasswordAuthenticationToken(rmasUser, null, GRANTED_AUTHORITIES);
    }

    @Before
    public void setupTokenHandler() {
    	tokenHandler.setJwtSecretKey("secret");
    	tokenHandler.setJwtTimeout(600);
        Mockito.when(clock.instant()).thenReturn(Instant.now(Clock.systemUTC()));
    }

    @Test
    public void parseUserFromToken_validToken_userHasCorrectName() {
        Assert.assertThat(((RMASUserDetails)tokenHandler.parseUserFromToken(VALID_TOKEN_AT_FUTURE_TIME).getPrincipal()).getUsersName(), is(equalTo(USER_NAME)));
    }

    @Test
    public void parseUserFromToken_validToken_userHasCorrectEmail() {
        Assert.assertThat(tokenHandler.parseUserFromToken(VALID_TOKEN_AT_FUTURE_TIME).getName(), is(equalTo(USER_EMAIL)));
    }

    @Test
    public void parseUserFromToken_validToken_userHasCorrectRoles() {
        Assert.assertThat(tokenHandler.parseUserFromToken(VALID_TOKEN_AT_FUTURE_TIME).getAuthorities(), is(equalTo(GRANTED_AUTHORITIES)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseUserFromToken_tokenWithoutRoles_throwsIllegalArgumentException() {
        tokenHandler.parseUserFromToken(TOKEN_WITHOUT_ROLES_AT_FUTURE_TIME);
    }

    @Test(expected = ExpiredJwtException.class)
    public void parseUserFromToken_expiredToken_throwsExpiredJwtException() {
        tokenHandler.parseUserFromToken(EXPIERED_TOKEN_AT_PAST_TIME);
    }

    @Test
    public void createTokenForUser_validAuthenticationInSpecificTime_specificValidToken() {
        Assert.assertThat(tokenHandler.createTokenForUser(authentication, FUTURE_TIME), is(equalTo(VALID_TOKEN_AT_FUTURE_TIME)));
    }

    @Test
    public void createTokenForUser_validAuthenticationNow_aToken() {
        Assert.assertThat(tokenHandler.createTokenForUser(authentication), is(notNullValue()));
    }

    @Test
    public void parseUserFromToken_withNoRoles_emptyAuthorities() {
        String token = tokenHandler.createTokenForUser(new UsernamePasswordAuthenticationToken(rmasUser, null, null));
    	Assert.assertThat(tokenHandler.parseUserFromToken(token).getAuthorities(), is(equalTo(Collections.emptyList())));
    }

    @Test
    public void parseUserFromToken_withTwoRoles_TwoAuthorities() {
        String token = tokenHandler.createTokenForUser(authentication);
    	Assert.assertThat(tokenHandler.parseUserFromToken(token).getAuthorities(), is(equalTo(Arrays.asList(new SimpleGrantedAuthority(ROLE_1), new SimpleGrantedAuthority(ROLE_2)))));
    }

    private UserGroup userGroup() {
        UserGroup userGroup = new UserGroup();
        userGroup.setId("5772856d68481249fbf66000");
        userGroup.setDeviceFilter(deviceFilter());
        return userGroup;
    }

    private DeviceFilter deviceFilter() {
        DeviceFilter deviceFilter = new DeviceFilter();
        deviceFilter.setIpAddress("10.163.49.68");
        deviceFilter.setMaintenanceContractRegion("South West");
        deviceFilter.setRccRegion("South West");
        deviceFilter.setManufacturer("SSL");
        return deviceFilter;
    }

}
