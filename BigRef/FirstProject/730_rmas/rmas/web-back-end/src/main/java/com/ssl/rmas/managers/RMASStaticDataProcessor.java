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
 */
package com.ssl.rmas.managers;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;

import com.ssl.rmas.entities.Device;
import com.ssl.rmas.entities.DeviceStatus;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.repositories.DeviceRepository;
import com.ssl.rmas.utils.ErrorMessage;

@Component(value = "staticDataProcessor")
public class RMASStaticDataProcessor implements StaticDataProcessor {

    private final Logger logger = LoggerFactory.getLogger(RMASStaticDataProcessor.class);
    private final Logger auditLogger = LoggerFactory.getLogger("Audit");

    private DeviceRepository deviceRepository;
    private Unmarshaller unmarshaller;

    @Autowired
    @Required
    public void setDeviceRepository(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Autowired
    @Required
    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller= unmarshaller;
    }

    @Override
    public void processStaticData(String deviceIp, OperationResult opResult, Path staticDataFile) {
        if(OperationResult.Status.SUCCESS.equals(opResult.getStatus())) {
            Device originalDevice = deviceRepository.findOne(deviceIp);
            Device newDevice;
            try {
                newDevice = (Device) unmarshaller.unmarshal(new StreamSource(new FileSystemResource(staticDataFile.toFile()).getInputStream()));
                if(originalDevice.getIpAddress().equals(newDevice.getIpAddress())) {
                    newDevice.setBandwidthLimit(originalDevice.getBandwidthLimit());
                    newDevice.setEnrolmentDate(originalDevice.getEnrolmentDate());
                    newDevice.setMaintenanceContractRegion(originalDevice.getMaintenanceContractRegion());
                    newDevice.setRccRegion(originalDevice.getRccRegion());
                    newDevice.setStatus(DeviceStatus.OK);
                    deviceRepository.save(newDevice);

                    if(!originalDevice.equals(newDevice)) {
                        opResult.setStatus(OperationResult.Status.UPDATED);
                        opResult.addErrorMessage(ErrorMessage.DEVICE_DETAILS_UPDATED);
                        logger.info("Updated device details old:{}, new:{}", originalDevice, newDevice);
                    }

                    logger.debug("Saved device after update {}", newDevice);
                } else {
                    logger.info("Downloaded static data file IP Address does not match this device ({})", originalDevice.getIpAddress());
                    auditLogger.info("Downloaded static data file IP Address does not match this device got {}", newDevice.getIpAddress());
                    opResult.setStatus(OperationResult.Status.FAILURE);
                    opResult.addErrorMessage(ErrorMessage.FAILED_TO_PROCESS_STATIC_DATA_FILES);
                }
            } catch (XmlMappingException | IOException e) {
                logger.info("Failed to parse data file", e);
                opResult.setStatus(OperationResult.Status.FAILURE);
                opResult.addErrorMessage(ErrorMessage.FAILED_TO_PROCESS_FILES);
            }
        }
    }

}
