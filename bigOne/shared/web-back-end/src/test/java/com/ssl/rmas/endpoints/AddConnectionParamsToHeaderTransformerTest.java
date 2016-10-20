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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 */
package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.HeaderKeys;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class AddConnectionParamsToHeaderTransformerTest {

    private static final String IP_ADDRESS_VALUE = "192.168.0.33";
    private static final String BANDWIDTH_LIMIT_VALUE = "256";
    private static final String PRIVATE_KEY_VALUE = "privateKey";

    public AddConnectionParamsToHeaderTransformerTest() {
    }

    @Test
    public void transform_always_StrippedConnectionParamsAreDeletedFromTheHeader() {
        Message<?> originMessage = createMessage(IP_ADDRESS_VALUE, BANDWIDTH_LIMIT_VALUE, PRIVATE_KEY_VALUE);
        Message<?> resultMessage = new AddConnectionParamsToHeaderTransformer().transform(originMessage);
        Assert.assertNull(resultMessage.getHeaders().get(HeaderKeys.IP_ADDRESS.toString()));
        Assert.assertNull(resultMessage.getHeaders().get(HeaderKeys.BANDWIDTH_LIMIT.toString()));
        Assert.assertNull(resultMessage.getHeaders().get(HeaderKeys.PRIVATE_KEY.toString()));
    }

    private Message<?> createMessage(String ipAddress, String bandwidthLimit, String privateKey) {
        return new Message<String>() {
            @Override
            public String getPayload() {
                return "{}";
            }

            @Override
            public MessageHeaders getHeaders() {
                Map<String, Object> map = new HashMap<>();
                map.put(HeaderKeys.IP_ADDRESS.toString(), ipAddress);
                map.put(HeaderKeys.BANDWIDTH_LIMIT.toString(), bandwidthLimit);
                map.put(HeaderKeys.PRIVATE_KEY.toString(), privateKey);
                return new MessageHeaders(map);
            }
        };
    }

}
