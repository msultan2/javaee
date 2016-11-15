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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.ssh.manager.ConnectionParams;

public class RAITConnectionParameterValidationTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RAITConnectionParameterValidationTransformer.class);
    private static final int BANDWIDTH_MAX_VALUE = 9999;

    @Transformer
    public Message<?> createValidationErrors(Message<?> message) {

        MessageHeaders headers = message.getHeaders();
        Set<String> errors = new HashSet<>();

        ConnectionParams connectionParams = getConnectionParams(headers, errors);
        validateConnectionParams(connectionParams.getIpAddress(), connectionParams.getBandwidthLimit(), connectionParams.getPrivateKey(), errors);

        if (!errors.isEmpty()) {
            LOGGER.debug("Validation failed due to these issues: {}", errors);
            return MessageBuilder
                    .withPayload(errors)
                    .copyHeaders(headers)
                    .setHeader(HeaderKeys.CONTENT_TYPE.toString(), "application/json")
                    .setHeader(HeaderKeys.HTTP_STATUS_CODE.toString(), HttpStatus.BAD_REQUEST.value())
                    .build();
        }
        LOGGER.debug("message returned after validation is: {}", message);
        return message;
    }

    private Boolean isBandwidthCorrect(int bandwidthLimit) {
        return bandwidthLimit >= 1 && bandwidthLimit <= BANDWIDTH_MAX_VALUE;
    }

    private boolean isIPv4Address(String address) {
        boolean result;
        if (!StringUtils.hasText(address)) {
            result = false;
        } else {
            try {
                Object res = InetAddress.getByName(address);
                result = res instanceof Inet4Address;
            } catch (final UnknownHostException ex) {
                result = false;
            }
        }
        return result;
    }

    private ConnectionParams getConnectionParams(MessageHeaders headers, Set<String> errors) {
        int bandwidthLimit = 0;
        String ipAddress = getMessageHeader(headers, HeaderKeys.IP_ADDRESS);
        try {
            bandwidthLimit = Integer.parseInt(getMessageHeader(headers, HeaderKeys.BANDWIDTH_LIMIT));
        } catch (NumberFormatException ex) {
            errors.add("Bandwidth limit not valid");
        }
        String privateKey = getMessageHeader(headers, HeaderKeys.PRIVATE_KEY);

        return new ConnectionParams(ipAddress, bandwidthLimit, privateKey);
    }

    private String getMessageHeader(MessageHeaders headers, HeaderKeys headerKey) {
        return headers.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(headerKey.toString())).map(entry -> entry.getValue().toString()).findFirst().orElseGet(() -> null);
    }

    private void validateConnectionParams(String ipAddress, int bandwidthLimit, String privateKey, Set<String> errors) {
        if (!isIPv4Address(ipAddress)) {
            errors.add("IP address not valid");
        }
        if (!isBandwidthCorrect(bandwidthLimit)) {
            errors.add("Bandwidth limit not valid");
        }
        if (!StringUtils.hasText(privateKey)) {
            errors.add("Private key is empty");
        }
    }
}
