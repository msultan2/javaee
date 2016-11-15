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
package com.ssl.rmas.controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ssl.rmas.entities.Device;

@RestController
@RequestMapping(method = RequestMethod.GET)
public class RAITDevicesController {

    private final Logger logger = LoggerFactory.getLogger(RAITDevicesController.class);

    private Map<String,Device> deviceMap = new ConcurrentHashMap<>();

    public void updateDeviceDetails(Device newDevice) {
        deviceMap.put(newDevice.getIpAddress(), newDevice);
        logger.debug("Device details set {}", newDevice);
    }

    @RequestMapping(value = "devices/{deviceIp:.+}")
    public Device getDevice(@PathVariable(value = "deviceIp") String deviceIp) {
        Device currentDevice = deviceMap.get(deviceIp);
        if(currentDevice==null) {
            currentDevice = new Device();
            currentDevice.setIpAddress(deviceIp);
            updateDeviceDetails(currentDevice);
        }

        logger.debug("Returning device: {}", currentDevice);
        return currentDevice;
    }
}
