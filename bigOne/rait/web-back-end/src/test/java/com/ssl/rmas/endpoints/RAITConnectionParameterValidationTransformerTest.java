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
  *
 */
package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.HeaderKeys;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

@SuppressWarnings("unchecked")
public class RAITConnectionParameterValidationTransformerTest {

    private final String VALID_IP_ADDRESS_VALUE = "192.168.0.33";
    private final String VALID_BANDWIDTH_LIMIT_VALUE = "256";
    private final String VALID_PRIVATE_KEY_VALUE = createValidPrivateKey();
    private final String INVALID_IP_ADDRESS_VALUE = "257.257.257.257";
    private final String INVALID_BANDWIDTH_LIMIT_VALUE = "-1";
    private final String INVALID_PRIVATE_KEY_VALUE = "";

    @Test
    public void createValidationErrors_EmptyPrivateKey_BadRequestWithErrorInPayload() {
        Message<String> message = createMessage(VALID_IP_ADDRESS_VALUE, VALID_BANDWIDTH_LIMIT_VALUE ,INVALID_PRIVATE_KEY_VALUE);
        RAITConnectionParameterValidationTransformer validationTransformer = new RAITConnectionParameterValidationTransformer();
        Message<?> validationErrors = validationTransformer.createValidationErrors(message);
        String httpStatusCode = validationErrors.getHeaders().get(HeaderKeys.HTTP_STATUS_CODE.toString()).toString();
        assertThat(httpStatusCode, is(equalTo(HttpStatus.BAD_REQUEST.toString())));
        assertTrue(validationErrors.getPayload() instanceof Set);
        ((Set<String>) validationErrors.getPayload()).contains("Private key is empty");
    }

    @Test
    public void createValidationErrors_InvalidIpAddress_BadRequestWithErrorInPayload() {
        Message<String> message = createMessage(INVALID_IP_ADDRESS_VALUE, VALID_BANDWIDTH_LIMIT_VALUE, VALID_PRIVATE_KEY_VALUE);
        RAITConnectionParameterValidationTransformer validationTransformer = new RAITConnectionParameterValidationTransformer();
        Message<?> validationErrors = validationTransformer.createValidationErrors(message);
        String httpStatusCode = validationErrors.getHeaders().get(HeaderKeys.HTTP_STATUS_CODE.toString()).toString();
        assertThat(httpStatusCode, is(equalTo(HttpStatus.BAD_REQUEST.toString())));
        assertTrue(validationErrors.getPayload() instanceof Set);
        ((Set<String>) validationErrors.getPayload()).contains("IP address not valid");
    }

    @Test
    public void createValidationErrors_InvalidBandwidthLimit_BadRequestWithErrorInPayload() {
        Message<String> message = createMessage(VALID_IP_ADDRESS_VALUE, INVALID_BANDWIDTH_LIMIT_VALUE, VALID_PRIVATE_KEY_VALUE);
        RAITConnectionParameterValidationTransformer validationTransformer = new RAITConnectionParameterValidationTransformer();
        Message<?> validationErrors = validationTransformer.createValidationErrors(message);
        String httpStatusCode = validationErrors.getHeaders().get(HeaderKeys.HTTP_STATUS_CODE.toString()).toString();
        assertThat(httpStatusCode, is(equalTo(HttpStatus.BAD_REQUEST.toString())));
        assertTrue(validationErrors.getPayload() instanceof Set);
        ((Set<String>) validationErrors.getPayload()).contains("Bandwidth limit not valid");
    }

    @Test
    public void createValidationErrors_EveryThingValid_DoesNotSetStatusCodeAndDoesNotChangeThePayload() {
        Message<String> message = createMessage(VALID_IP_ADDRESS_VALUE, VALID_BANDWIDTH_LIMIT_VALUE, VALID_PRIVATE_KEY_VALUE);
        RAITConnectionParameterValidationTransformer validationTransformer = new RAITConnectionParameterValidationTransformer();
        Message<?> validationErrors = validationTransformer.createValidationErrors(message);
        assertThat(validationErrors.getHeaders().get(HeaderKeys.HTTP_STATUS_CODE.toString()), is(equalTo(null)));
        assertThat(validationErrors.getPayload(), is(equalTo("{}")));
    }

    @Test
    public void createValidationErrors_EveryThingInvalid_BadRequestWithErrorsInPayload() {
        Message<String> message = createMessage(INVALID_IP_ADDRESS_VALUE, INVALID_BANDWIDTH_LIMIT_VALUE, INVALID_PRIVATE_KEY_VALUE);
        RAITConnectionParameterValidationTransformer validationTransformer = new RAITConnectionParameterValidationTransformer();
        Message<?> validationErrors = validationTransformer.createValidationErrors(message);
        String httpStatusCode = validationErrors.getHeaders().get(HeaderKeys.HTTP_STATUS_CODE.toString()).toString();
        assertThat(httpStatusCode, is(equalTo(HttpStatus.BAD_REQUEST.toString())));
        assertTrue(validationErrors.getPayload() instanceof Set);
        ((Set<String>) validationErrors.getPayload()).contains("Private key is empty");
        ((Set<String>) validationErrors.getPayload()).contains("IP address not valid");
        ((Set<String>) validationErrors.getPayload()).contains("Bandwidth limit not valid");
    }

    private Message<String> createMessage(String ipAddress, String bandwidthLimit, String privateKey) {
        return new Message<String>() {
            @Override
            public String getPayload() {
                return "{}";
            }

            @Override
            public MessageHeaders getHeaders() {
                Map<String, Object> map = new HashMap<>();
                map.put(HeaderKeys.HTTP_REQUEST_MODE.toString(), "POST");
                map.put(HeaderKeys.HTTP_REQUEST_URL.toString(), "http://localhost:8084/rait-core/activity/staticData");
                map.put(HeaderKeys.IP_ADDRESS.toString(), ipAddress);
                map.put(HeaderKeys.BANDWIDTH_LIMIT.toString(), bandwidthLimit);
                map.put(HeaderKeys.PRIVATE_KEY.toString(), privateKey);
                return new MessageHeaders(map);
            }
        };
    }

    private static String createValidPrivateKey() {
        return ""
                + "-----BEGIN RSA PRIVATE KEY-----\\\\r\\\\n"
                + "MIIEpAIBAAKCAQEAw2rqE9oiD6V2PxST5+QFM7Y2epTiB+duymBcCzVPNLRX8f66\\\\r\\\\n"
                + "/FsDwzA8L8Am1XY33vfMxLi8JABX+SxpQtwykf6229eDE1cJu6LD+0Nl+IDLUVI4\\\\r\\\\n"
                + "TH8A/A8jOD42EvMxlS5puMqxaRqheR4rDCT7b6SFM4SSLLvR7LKgFr3cKrGQ0l0k\\\\r\\\\n"
                + "F48DzS8Qzsqn8Y7GXnoQ+rKn25eIvfnm7md8W5oqjG4T20CcgL0WZmAkemMFDcmi\\\\r\\\\n"
                + "aM2DkxcEVRNNWTpYqxY2bcmIEH77uPo0ek36VmXfh4rspO7Vks/4/MtGrF/x5OPt\\\\r\\\\n"
                + "72NdNsCzxy9keGAryALfYoqoKqWySTpFRL9EgQIDAQABAoIBAD8xbrq2uncG0cM6\\\\r\\\\n"
                + "W04MzaXYUau4Q5O8wnYNTRaOyatIenwWHM+IeyH+kif64lP7I7wltAEVoDmgJtK8\\\\r\\\\n"
                + "CtoFLRFLJkTXX1q8tbgIvYmeVPZUZ+tHRE9wbpgUKRaB/6iHHkyYsrWATninlPn+\\\\r\\\\n"
                + "yHrn5qtGr5BmWYK/xfXeGajhAraHHpdq+s4WdSMvUMZ1VZDWAjka5CyW+onsT7ZI\\\\r\\\\n"
                + "igmxLDPB/p9YcI+WUcmMmLKujydAwuJN9nXHM4OS38qYmmzSX3Qx1g3iHIH32CF+\\\\r\\\\n"
                + "RNffljSh1J/ApV0g07BF9od7+Y1Pd45TbmkkG4KQ4vuV0gPEF30f+gfi1mraLiOG\\\\r\\\\n"
                + "HhLvjAECgYEA5Ao2qr3cYnNSo1kAdzSsdi3oI0w7RDbZVwlDNeOCLqnlPB90d988\\\\r\\\\n"
                + "mvptcnlXnflFeLOdzko/s+yI7PxpGOtiljflxtSJSexqND1P6zJFhwrj7H2bNHgE\\\\r\\\\n"
                + "XtEYyM7/Ow9jNiVAk5clHbKysqq/Rn0Br0OXh9hZ6ehm9QjI6v7+NaECgYEA22C+\\\\r\\\\n"
                + "Qwc6ZpOhsp4lXajG4UyFQPOVS+JpEAJVTZbuK8zPia3K7F93oEMuT2P27XjRX58G\\\\r\\\\n"
                + "pp6A8WdBBROW+Vr6JX6jFD680EDNR5sXUs9zXV4696Voan7DlRMvBNH/yABEoce+\\\\r\\\\n"
                + "WosLCRDJpvG9TDDbC/VO7d7PAS7HIOkte6W64uECgYBzzLv1+HPxip5sVMXnwtfR\\\\r\\\\n"
                + "K362bf+qpC+6AvoeY2m4SI4f+dkrBjHAgWsStovHrM2afZulJg8zTuo5bfPws5dc\\\\r\\\\n"
                + "sQX98e/FmkhUPs5WoTCtYtpbPW2TN18Axy3Oy07Qrv+yfzhLunUen6AfGpYHNXOl\\\\r\\\\n"
                + "jK1mTndaUKn3xH5C1zpAAQKBgQCmszTgqjVujPBqUlICgaXevc7kNDRWnY1IZP19\\\\r\\\\n"
                + "IBuLnO53qGByjXjRhztBiKyydwRirOzY38kLp3J48RHgGYnLxBcX4zSG6zcSGn/2\\\\r\\\\n"
                + "pTz8zgXw+PgHNJxaQ2GPQCQlTk7YwHedRdr04JTbDwbRRq6sv5NindSfCXS3RXkS\\\\r\\\\n"
                + "ZDVHAQKBgQDSsUlkOmT7OKFmPNq4hnItSXfBsS+eYstdLNC6bH870QlfyEyxkJZp\\\\r\\\\n"
                + "0cSF5Ciq8RWcLJJXngrUGiWuPjur7T3XPEFeAHMKgsw1A4GcapuY9wSMfhxVox9c\\\\r\\\\n"
                + "Wbrm2bXxNZsO9B6hBfd6Xv2OAv07mPDHdrzi1GKZQ2QF8A2iyqVunA==\\\\r\\\\n"
                + "-----END RSA PRIVATE KEY-----\\\\r\\\\n";
    }
}
