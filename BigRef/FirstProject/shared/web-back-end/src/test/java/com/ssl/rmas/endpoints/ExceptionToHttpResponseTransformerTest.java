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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved. *
 */
package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.utils.ErrorMessage;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import com.ssl.rmas.exceptionhandler.ExceptionToHttpResponseMapper;
import com.ssl.rmas.exceptionhandler.ExceptionToMessagesMapper;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionToHttpResponseTransformerTest {

    private static final String PREVIOUS_HEADER = "previousHeader";
    private static final String PREVIOUS_HEADER_VALUE = "previousValue";
    private static final int INTERNAL_SERVER_ERROR_500 = 500;

    @Mock
    private ExceptionToHttpResponseMapper httpResponseError;
    
    @Mock
    private ExceptionToMessagesMapper exceptionToMessages;

    @InjectMocks
    private ExceptionToHttpResponseTransformer exceptionToHttpResponseTransformer;

    @Test
    public void updateMessage_always_KeepsPreviousHeaders() {
        Exception ex = new Exception("test");
        Message<Throwable> messageIn = createMessage(ex);
        Message<Set<ErrorMessage>> messageOut = exceptionToHttpResponseTransformer.updateMessage(messageIn);
        assertThat(messageOut.getHeaders().get(PREVIOUS_HEADER), is(equalTo(PREVIOUS_HEADER_VALUE)));
    }

    @Test
    public void updateMessage_always_SetStatusCodeFromHttpResponseError() {
        Exception ex = new Exception("test");
        Mockito.when(httpResponseError.getStatusCode(ex)).thenReturn(INTERNAL_SERVER_ERROR_500);
        Message<Throwable> messageIn = createMessage(ex);
        Message<Set<ErrorMessage>> messageOut = exceptionToHttpResponseTransformer.updateMessage(messageIn);
        assertThat(messageOut.getHeaders().get(HeaderKeys.HTTP_STATUS_CODE.toString()), is(equalTo(INTERNAL_SERVER_ERROR_500)));
    }

    @Test
    public void updateMessage_always_SetErrorMessageFromHttpResponseError() {
        Exception ex = new Exception("test");
        Set<ErrorMessage> errorMessageSet = new HashSet<>();
        errorMessageSet.add(ErrorMessage.UNKNOWN);
        Mockito.when(exceptionToMessages.getErrorMessages(ex)).thenReturn(errorMessageSet);
        Message<Throwable> messageIn = createMessage(ex);
        Message<Set<ErrorMessage>> messageOut = exceptionToHttpResponseTransformer.updateMessage(messageIn);
        assertTrue(messageOut.getPayload().contains(ErrorMessage.UNKNOWN));
    }

    private Message<Throwable> createMessage(Throwable throwable) {
        return MessageBuilder
                .withPayload(throwable)
                .setHeader(PREVIOUS_HEADER, PREVIOUS_HEADER_VALUE)
                .build();
    }
}