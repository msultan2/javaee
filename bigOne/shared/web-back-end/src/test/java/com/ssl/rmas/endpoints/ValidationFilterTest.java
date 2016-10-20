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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class ValidationFilterTest {

    Map<String, String> actualCurrentActivity = new HashMap<>();

    @Test
    public void testEmptyValidation() {
        ValidationFilter validationFilter = new ValidationFilter();
        boolean filterResult = validationFilter.accept(getMessageWithStatus(null));

        assertTrue(filterResult);
    }

    @Test
    public void testValidationErrors() {
        ValidationFilter validationFilter = new ValidationFilter();
        boolean filterResult = validationFilter.accept(getMessageWithStatus(HttpStatus.BAD_REQUEST));

        assertFalse(filterResult);
    }

    private Message<?> getMessageWithStatus(HttpStatus status) {
        return new Message<String>() {

            @Override
            public MessageHeaders getHeaders() {
                Map<String, Object> map = new HashMap<>();
                map.put("ipAddress", "192.168.0.33");
                map.put("bandwidthLimit", "256");
                map.put("privateKey", "-----BEGIN RSA PRIVATE KEY-----\\r\\nMIIEpAIBAAKCAQEAw2rqE9oiD6V2PxST5+QFM7Y2epTiB+duymBcCzVPNLRX8f66\\r\\n/FsDwzA8L8Am1XY33vfMxLi8JABX+SxpQtwykf6229eDE1cJu6LD+0Nl+IDLUVI4\\r\\nTH8A/A8jOD42EvMxlS5puMqxaRqheR4rDCT7b6SFM4SSLLvR7LKgFr3cKrGQ0l0k\\r\\nF48DzS8Qzsqn8Y7GXnoQ+rKn25eIvfnm7md8W5oqjG4T20CcgL0WZmAkemMFDcmi\\r\\naM2DkxcEVRNNWTpYqxY2bcmIEH77uPo0ek36VmXfh4rspO7Vks/4/MtGrF/x5OPt\\r\\n72NdNsCzxy9keGAryALfYoqoKqWySTpFRL9EgQIDAQABAoIBAD8xbrq2uncG0cM6\\r\\nW04MzaXYUau4Q5O8wnYNTRaOyatIenwWHM+IeyH+kif64lP7I7wltAEVoDmgJtK8\\r\\nCtoFLRFLJkTXX1q8tbgIvYmeVPZUZ+tHRE9wbpgUKRaB/6iHHkyYsrWATninlPn+\\r\\nyHrn5qtGr5BmWYK/xfXeGajhAraHHpdq+s4WdSMvUMZ1VZDWAjka5CyW+onsT7ZI\\r\\nigmxLDPB/p9YcI+WUcmMmLKujydAwuJN9nXHM4OS38qYmmzSX3Qx1g3iHIH32CF+\\r\\nRNffljSh1J/ApV0g07BF9od7+Y1Pd45TbmkkG4KQ4vuV0gPEF30f+gfi1mraLiOG\\r\\nHhLvjAECgYEA5Ao2qr3cYnNSo1kAdzSsdi3oI0w7RDbZVwlDNeOCLqnlPB90d988\\r\\nmvptcnlXnflFeLOdzko/s+yI7PxpGOtiljflxtSJSexqND1P6zJFhwrj7H2bNHgE\\r\\nXtEYyM7/Ow9jNiVAk5clHbKysqq/Rn0Br0OXh9hZ6ehm9QjI6v7+NaECgYEA22C+\\r\\nQwc6ZpOhsp4lXajG4UyFQPOVS+JpEAJVTZbuK8zPia3K7F93oEMuT2P27XjRX58G\\r\\npp6A8WdBBROW+Vr6JX6jFD680EDNR5sXUs9zXV4696Voan7DlRMvBNH/yABEoce+\\r\\nWosLCRDJpvG9TDDbC/VO7d7PAS7HIOkte6W64uECgYBzzLv1+HPxip5sVMXnwtfR\\r\\nK362bf+qpC+6AvoeY2m4SI4f+dkrBjHAgWsStovHrM2afZulJg8zTuo5bfPws5dc\\r\\nsQX98e/FmkhUPs5WoTCtYtpbPW2TN18Axy3Oy07Qrv+yfzhLunUen6AfGpYHNXOl\\r\\njK1mTndaUKn3xH5C1zpAAQKBgQCmszTgqjVujPBqUlICgaXevc7kNDRWnY1IZP19\\r\\nIBuLnO53qGByjXjRhztBiKyydwRirOzY38kLp3J48RHgGYnLxBcX4zSG6zcSGn/2\\r\\npTz8zgXw+PgHNJxaQ2GPQCQlTk7YwHedRdr04JTbDwbRRq6sv5NindSfCXS3RXkS\\r\\nZDVHAQKBgQDSsUlkOmT7OKFmPNq4hnItSXfBsS+eYstdLNC6bH870QlfyEyxkJZp\\r\\n0cSF5Ciq8RWcLJJXngrUGiWuPjur7T3XPEFeAHMKgsw1A4GcapuY9wSMfhxVox9c\\r\\nWbrm2bXxNZsO9B6hBfd6Xv2OAv07mPDHdrzi1GKZQ2QF8A2iyqVunA==\\r\\n-----END RSA PRIVATE KEY-----\\r\\n");
                map.put("http_requestMethod", "POST");
                map.put("http_requestUrl", "http://localhost:8084/rait-core/activity/staticData");
                map.put("http_statusCode", status);
                return new MessageHeaders(map);
            }

            @Override
            public String getPayload() {
                return "";
            }
        };
    }
}
