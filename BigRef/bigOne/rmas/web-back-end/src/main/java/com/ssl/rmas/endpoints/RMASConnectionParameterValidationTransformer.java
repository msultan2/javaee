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

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import com.ssl.rmas.entities.Device;
import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.RMASKey;
import com.ssl.rmas.repositories.DeviceRepository;
import com.ssl.rmas.repositories.RMASKeyRepository;

public class RMASConnectionParameterValidationTransformer extends ValidationTransformer{

    private static final Logger LOGGER = LoggerFactory.getLogger(RMASConnectionParameterValidationTransformer.class);
    private DeviceRepository deviceRepository;
    private RMASKeyRepository keyRepository;

    @Autowired
    @Required
    public void setDeviceRepository(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Autowired
    @Required
    public void setKeyRepository(RMASKeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    @Transformer
    @Override
    public Message<?> createValidationErrors(Message<?> message) {
        MessageHeaders headers = message.getHeaders();
        Set<String> errors = getErrors(message);
        String ipAddress = getMessageHeader(headers, HeaderKeys.IP_ADDRESS);        
        Device device = deviceRepository.findOne(ipAddress);
        Message<?> retval;
        
        if(device==null){
            errors.add("Device not found");
            retval = getErrorMessage(errors, HttpStatus.BAD_REQUEST, headers);
        }else{
            retval = MessageBuilder.fromMessage(message)
                    .setHeader(HeaderKeys.BANDWIDTH_LIMIT.toString(), device.getBandwidthLimit())
                    .setHeader(HeaderKeys.PRIVATE_KEY.toString(), keyRepository.findCurrentKey(RMASKey.KeyType.PRIVATE).getContent())
                    .build();
        }

        LOGGER.debug("message returned after validation is: {}", retval);
        return retval;
    }

    private String getMessageHeader(MessageHeaders headers, HeaderKeys headerKey) {
        return headers.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(headerKey.toString())).map(entry -> entry.getValue().toString()).findFirst().orElseGet(() -> null);
    }
}
