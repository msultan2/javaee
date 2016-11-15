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
 * 
 */
package com.ssl.rmas.jwt;

import com.ssl.rmas.utils.ErrorMessage;
import java.util.LinkedHashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import java.util.Set;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JSONExceptionHandlerUtilsTest {

 
    @InjectMocks
    private  JSONExceptionHandlerUtils  jSONExceptionHandlerUtils;

    @Test
    public void errorMessageSetToJsonStringConvertsSetOfErrorMessageToStringTest() throws Exception {
        Set<ErrorMessage> errorMessages = new LinkedHashSet<>();
        errorMessages.add(ErrorMessage.USER_ACCOUNT_LOCKED);
        errorMessages.add(ErrorMessage.DUPLICATE_VALUE);
        errorMessages.add(ErrorMessage.FAILED_TO_GET_USER_GROUPS);
        
        String messages = jSONExceptionHandlerUtils.errorMessageSetToJsonString(errorMessages);
        
        assertThat(messages, equalTo("[\"" + ErrorMessage.USER_ACCOUNT_LOCKED.toString() + "\", \""
                + ErrorMessage.DUPLICATE_VALUE.toString() + "\", \"" + ErrorMessage.FAILED_TO_GET_USER_GROUPS.toString() + "\"]"));
    }
}
