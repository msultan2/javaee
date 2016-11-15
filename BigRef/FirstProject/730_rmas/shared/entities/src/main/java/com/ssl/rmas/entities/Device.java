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
package com.ssl.rmas.entities;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Device {

    @Id
    private String ipAddress;
    private String maintenanceContractRegion;
    private String rccRegion;
    private String manufacturer;
    private Instant enrolmentDate;
    private int bandwidthLimit;
    private String manufacturerType;
    private String serialNumber;
    private String hardwareVersion;
    private String firmwareVersion;
    private URI hostname;
    private String manufacturerSpecificData;
    private String haGeographicAddress;
    private Double latitude;
    private Double longitude;
    private Map<String,List<String>> deviceList;
    private DeviceStatus status;

    public Map<String, List<String>> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(Map<String, List<String>> deviceList) {
        this.deviceList = deviceList;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getHaGeographicAddress() {
        return haGeographicAddress;
    }

    public void setHaGeographicAddress(String haGeographicAddress) {
        this.haGeographicAddress = haGeographicAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMaintenanceContractRegion() {
        return maintenanceContractRegion;
    }

    public void setMaintenanceContractRegion(String maintenanceContractRegion) {
        this.maintenanceContractRegion = maintenanceContractRegion;
    }

    public String getRccRegion() {
        return rccRegion;
    }

    public void setRccRegion(String rccRegion) {
        this.rccRegion = rccRegion;
    }

    public Instant getEnrolmentDate() {
        return enrolmentDate;
    }

    public void setEnrolmentDate(Instant requiredEnrolmentDate) {
        this.enrolmentDate = requiredEnrolmentDate;
    }

    public int getBandwidthLimit() {
        return bandwidthLimit;
    }

    public void setBandwidthLimit(int bandwidthLimit) {
        this.bandwidthLimit = bandwidthLimit;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturerType() {
        return manufacturerType;
    }

    public void setManufacturerType(String manufacturerType) {
        this.manufacturerType = manufacturerType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public URI getHostname() {
        return hostname;
    }

    public void setHostname(URI hostname) {
        this.hostname = hostname;
    }

    public String getManufacturerSpecificData() {
        return manufacturerSpecificData;
    }

    public void setManufacturerSpecificData(String manufacturerSpecificData) {
        this.manufacturerSpecificData = manufacturerSpecificData;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.ipAddress);
        hash = 89 * hash + Objects.hashCode(this.maintenanceContractRegion);
        hash = 89 * hash + Objects.hashCode(this.rccRegion);
        hash = 89 * hash + Objects.hashCode(this.enrolmentDate);
        hash = 89 * hash + Objects.hashCode(this.manufacturer);
        hash = 89 * hash + this.bandwidthLimit;
        hash = 89 * hash + Objects.hashCode(this.manufacturerType);
        hash = 89 * hash + Objects.hashCode(this.serialNumber);
        hash = 89 * hash + Objects.hashCode(this.hardwareVersion);
        hash = 89 * hash + Objects.hashCode(this.firmwareVersion);
        hash = 89 * hash + Objects.hashCode(this.hostname);
        hash = 89 * hash + Objects.hashCode(this.manufacturerSpecificData);
        hash = 89 * hash + Objects.hashCode(this.haGeographicAddress);
        hash = 89 * hash + Objects.hashCode(this.latitude);
        hash = 89 * hash + Objects.hashCode(this.longitude);
        hash = 89 * hash + Objects.hashCode(this.deviceList);
        hash = 89 * hash + Objects.hashCode(this.status);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Device other = (Device) obj;
        if (this.bandwidthLimit != other.bandwidthLimit) {
            return false;
        }
        if (!Objects.equals(this.ipAddress, other.ipAddress)) {
            return false;
        }
        if (!Objects.equals(this.maintenanceContractRegion, other.maintenanceContractRegion)) {
            return false;
        }
        if (!Objects.equals(this.rccRegion, other.rccRegion)) {
            return false;
        }
        if (!Objects.equals(this.enrolmentDate, other.enrolmentDate)) {
            return false;
        }
        if (!Objects.equals(this.manufacturer, other.manufacturer)) {
            return false;
        }
        if (!Objects.equals(this.manufacturerType, other.manufacturerType)) {
            return false;
        }
        if (!Objects.equals(this.serialNumber, other.serialNumber)) {
            return false;
        }
        if (!Objects.equals(this.hardwareVersion, other.hardwareVersion)) {
            return false;
        }
        if (!Objects.equals(this.firmwareVersion, other.firmwareVersion)) {
            return false;
        }
        if (!Objects.equals(this.hostname, other.hostname)) {
            return false;
        }
        if (!Objects.equals(this.manufacturerSpecificData, other.manufacturerSpecificData)) {
            return false;
        }
        if (!Objects.equals(this.haGeographicAddress, other.haGeographicAddress)) {
            return false;
        }
        if (!Objects.equals(this.latitude, other.latitude)) {
            return false;
        }
        if (!Objects.equals(this.longitude, other.longitude)) {
            return false;
        }
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!CollectionUtils.containsAll(this.deviceList.keySet(), other.deviceList.keySet())) {
            return false;
        }
        if (!CollectionUtils.containsAll(other.deviceList.keySet(), this.deviceList.keySet())) {
            return false;
        }
        for(Entry<String, List<String>> entry: this.deviceList.entrySet()) {
            List<String> otherValue = other.deviceList.get(entry.getKey());
            if(entry.getValue().size()!=otherValue.size()) {
                return false;
            }
            if(!CollectionUtils.containsAll(entry.getValue(), otherValue)) {
                return false;
            }
            if(!CollectionUtils.containsAll(otherValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Device{");
        builder
            .append("ipAddress=").append(ipAddress)
            .append(", maintenanceContractRegion=").append(maintenanceContractRegion)
            .append(", rccRegion=").append(rccRegion)
            .append(", enrolmentDate=").append(enrolmentDate)
            .append(", bandwidthLimit=").append(bandwidthLimit)
            .append(", manufacturer=").append(manufacturer)
            .append(", manufacturerType=").append(manufacturerType)
            .append(", serialNumber=").append(serialNumber)
            .append(", hardwareVersion=").append(hardwareVersion)
            .append(", firmwareVersion=").append(firmwareVersion)
            .append(", hostname=").append(hostname)
            .append(", manufacturerSpecificData=").append(manufacturerSpecificData)
            .append(", haGeographicAddress=").append(haGeographicAddress)
            .append(", latitude=").append(latitude)
            .append(", longitude=").append(longitude)
            .append(", status=").append(status)
            .append(", deviceList={");

        if(deviceList!=null) {
            builder.append(deviceList.entrySet().stream().map(entry -> entry.getKey() + "{"
                    + entry.getValue().stream().collect(Collectors.joining(", "))
                    + "}").collect(Collectors.joining(", ")));
        }

        builder.append("}}");

        return builder.toString();
    }
}
