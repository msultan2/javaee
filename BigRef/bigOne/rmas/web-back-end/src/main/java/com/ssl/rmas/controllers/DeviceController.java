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
package com.ssl.rmas.controllers;

import com.ssl.rmas.entities.Device;
import com.ssl.rmas.entities.DeviceFilter;
import com.ssl.rmas.entities.DeviceStatus;
import com.ssl.rmas.entities.FilteredDeviceListContainer;
import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.validation.DeviceValidator;
import com.ssl.rmas.repositories.DeviceRepository;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PropertySource("classpath:config/application.properties")
public class DeviceController {

    private final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Value("${rmas.device.filter.maxResults}")
    private Integer maxNoDevices;
    private DeviceValidator deviceValidator;
    private DeviceRepository deviceRepository;

    @Autowired
    public void setDeviceValidator(DeviceValidator deviceValidator) {
        this.deviceValidator = deviceValidator;
    }

    @Autowired
    public void setDeviceRepository(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @RequestMapping(value = "/devices/search/findFilteredDevices", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FilteredDeviceListContainer> findByFilter(@RequestBody Optional<List<DeviceFilter>> filters) {
        ResponseEntity<FilteredDeviceListContainer> response;
        try {
            List<Device> devicesList = deviceRepository.findDevicesForCurrentUser(filters);
            FilteredDeviceListContainer filteredDeviceListContainer = new FilteredDeviceListContainer(devicesList, filters);
            response = getResponseWithLimitedToMaxDevices(filteredDeviceListContainer);
        } catch (IllegalArgumentException ex) {
            logger.debug("IllegalArgumentException occured: {}", ex.getLocalizedMessage());
            response = new ResponseEntity<FilteredDeviceListContainer>(HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException ex) {
            logger.debug("IllegalAccessException occured: {}", ex.getLocalizedMessage());
            response = new ResponseEntity<FilteredDeviceListContainer>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @RequestMapping(value = "/devices/insert/{ipAddress}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> insert(@PathVariable(value = "ipAddress") String ipAddress, @RequestBody Device device, BindingResult bindingResult) {
        if (isValidInsertRequest(ipAddress, device, bindingResult)) {
            setDefaultValuesOnDevice(device);
            deviceRepository.save(device);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidInsertRequest(String ipAddress, Device device, BindingResult bindingResult) {
        deviceValidator.validate(device, bindingResult);
        if (!ipAddress.equals(device.getIpAddress())) {
            bindingResult.rejectValue("ipAddress", "ipAddress.mismatched.values", "The ipAddress in device and URL must be equal");
        }
        if (!bindingResult.hasErrors() && deviceRepository.exists(ipAddress)) {
            bindingResult.rejectValue("ipAddress", "ipAddress.duplicate.value", "Duplicate IP address");
        }
        return !bindingResult.hasErrors();
    }

    private void setDefaultValuesOnDevice(Device device) {
        device.setStatus(DeviceStatus.ENROLLED);
        if (StringUtils.isBlank(device.getMaintenanceContractRegion())) {
            device.setMaintenanceContractRegion(device.getRccRegion());
        }
    }

    private ResponseEntity<FilteredDeviceListContainer> getResponseWithLimitedToMaxDevices(FilteredDeviceListContainer filteredDeviceListContainer) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        List<Device> devicesList = filteredDeviceListContainer.getDeviceList();
        if (devicesList.size() > maxNoDevices) {
            devicesList = devicesList.subList(0, maxNoDevices);
            filteredDeviceListContainer.setDeviceList(devicesList);
            headers.add(HeaderKeys.MAX_DEVICES_REACHED.toString(), Boolean.TRUE.toString());
        } else {
            headers.add(HeaderKeys.MAX_DEVICES_REACHED.toString(), Boolean.FALSE.toString());
        }
        return new ResponseEntity<>(filteredDeviceListContainer, headers, HttpStatus.OK);
    }
}
