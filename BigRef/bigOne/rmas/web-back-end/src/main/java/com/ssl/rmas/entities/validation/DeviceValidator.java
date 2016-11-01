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
 * Copyright 2016 (C) Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */


package com.ssl.rmas.entities.validation;

import com.ssl.rmas.entities.Device;
import com.ssl.rmas.repositories.RccRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

@Component
public class DeviceValidator implements Validator{

    private final Logger logger = LoggerFactory.getLogger(DeviceValidator.class);
    private final int BANDWIDTH_MAX_VALUE = 9999;
    
    @Autowired
    private RccRepository rccRepository;
    
    @Override
    public boolean supports(Class<?> type) {
        return DeviceValidator.class.equals(type);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Device device = (Device) target;

        if (isInvalidIpv4Address(device.getIpAddress())) {
            errors.rejectValue("ipAddress", "ipAddress.invalid.value", "The ipAddress must be valid");
        }
        if (isInvalidRcc(device.getRccRegion())) {
            errors.rejectValue("rccRegion", "rccRegion.invalid.value", "The rcc must have a valid value");
        }
        if (isInvalidBandwidthLimit(device.getBandwidthLimit())) {
            errors.rejectValue("bandwidthLimit", "bandwidthLimit.invalid.value", "The bandwidth limit must be valid");
        }
        if(errors.hasErrors()) {
            logger.debug("Validation failure {}", errors);
        }
    }

    private boolean isInvalidIpv4Address(String ipAddress){
        return !InetAddressValidator.getInstance().isValidInet4Address(ipAddress);
    }
    
    private boolean isInvalidRcc(String rcc) {
        return rcc != null && !rccRepository.exists(rcc);
    }
    
    private Boolean isInvalidBandwidthLimit(int bandwidthLimit) {
        return !(bandwidthLimit >= 1 && bandwidthLimit <= BANDWIDTH_MAX_VALUE);
    }
}