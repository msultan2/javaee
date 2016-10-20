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
package com.ssl.rmas.utils;

import com.ssl.rmas.entities.User;
import com.ssl.rmas.repositories.UserRepository;
import java.time.Clock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@RunWith(MockitoJUnitRunner.class)
public class PasswordUtilsTest {
    
    @Mock
    private PasswordEncoder mockPasswordEncoder;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private User mockUser;    
    
    @InjectMocks
    private PasswordUtils passwordUtils;
    
    @Test
    public void passwordTooShort() {
        assertFalse(passwordUtils.isPasswordValid("123eE^"));
    }
    
    @Test
    public void passwordHasRepeats() {
        assertFalse(passwordUtils.isPasswordValid("1234eeE^"));
    }
    
    @Test
    public void onlyUpperAndLowercasePassword() {
        assertFalse(passwordUtils.isPasswordValid("eEeEeEeE"));
    }
    
    @Test
    public void onlyNumbersAndNonAlpha() {
        assertFalse(passwordUtils.isPasswordValid("1234!\"£$"));
    }

    @Test
    public void validPasswordMissingLowercase() {        
        assertTrue(passwordUtils.isPasswordValid("ER1234^&"));
    }

    @Test
    public void validPasswordMissingUppercase() {
        assertTrue(passwordUtils.isPasswordValid("er1234^&"));        
    }

    @Test
    public void validPasswordMissingNumber() {
        assertTrue(passwordUtils.isPasswordValid("er£%RG^&"));
    }

    @Test
    public void validPasswordMissingNonAlpha() {       
        assertTrue(passwordUtils.isPasswordValid("s1S1s1S1"));
    }
    
    @Test
    public void changePasswordTest(){
        final String password = "ER1234^&";
        final String hashedPassword = "hashedPassword";
        Mockito.when(mockPasswordEncoder.encode(password)).thenReturn(hashedPassword);
        passwordUtils.setClock(Clock.systemUTC());
        passwordUtils.changePassword(mockUser, password);
        Mockito.verify(mockPasswordEncoder).encode(password);
        Mockito.verify(mockUser).setPasswordHash(hashedPassword);
        Mockito.verify(mockUserRepository).save(mockUser);
    }

}
