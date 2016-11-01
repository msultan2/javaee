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
package com.ssl.rmas.entities;

import java.util.ArrayList;
import java.util.List;

public class DeviceFilter {

    private String ipAddress;
    private String maintenanceContractRegion;
    private String rccRegion;
    private String manufacturer;
    private String firmwareVersion;
    private String haGeographicAddress;

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
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

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public List<String> conditions() {
        List<String> conditions = new ArrayList<>();
        if (ipAddress != null) {
            conditions.add("DEVICE_FILTER_CONDITION_" + "ipAddress" + "=" + ipAddress);
        }
        if (maintenanceContractRegion != null) {
            conditions.add("DEVICE_FILTER_CONDITION_" + "maintenanceContractRegion" + "=" + maintenanceContractRegion);
        }
        if (rccRegion != null) {
            conditions.add("DEVICE_FILTER_CONDITION_" + "rccRegion" + "=" + rccRegion);
        }
        if (manufacturer != null) {
            conditions.add("DEVICE_FILTER_CONDITION_" + "manufacturer" + "=" + manufacturer);
        }
        if (firmwareVersion != null) {
            conditions.add("DEVICE_FILTER_CONDITION_" + "firmwareVersion" + "=" + firmwareVersion);
        }
        if (haGeographicAddress != null) {
            conditions.add("DEVICE_FILTER_CONDITION_" + "haGeographicAddress" + "=" + haGeographicAddress);
        }
        return conditions;
    }

    public void setConditions(final List<String> conditions) {
        conditions.stream()
            .filter(condition -> !condition.isEmpty())
            .forEach(this::setField);
    }

    private void setField(final String condition) {
        String[] conditionParts = extractConditionParts(condition);
        String key = conditionParts[0];
        if (conditionParts.length == 2 && !key.isEmpty()) {
            String value = conditionParts[1];
            switch (key) {
                case "ipAddress":
                    ipAddress = value;
                    break;
                case "maintenanceContractRegion":
                    maintenanceContractRegion = value;
                    break;
                case "rccRegion":
                    rccRegion = value;
                    break;
                case "manufacturer":
                    manufacturer = value;
                    break;
                case "firmwareVersion":
                    firmwareVersion = value;
                    break;
                case "haGeographicAddress":
                    haGeographicAddress = value;
                    break;
                default:
                    throw new IllegalArgumentException("Can not set invalid field: " + condition);
            }
        } else {
            throw new IllegalArgumentException("Can not set invalid field: " + condition);
        }
    }

    private String[] extractConditionParts(final String condition) {
        return condition.substring("DEVICE_FILTER_CONDITION_".length(),condition.length() ).split("=");
    }

    @Override
    public String toString() {
        return "DeviceFilter{" + "ipAddress=" + ipAddress + ", maintenanceContractRegion=" + maintenanceContractRegion
            + ", rccRegion=" + rccRegion + ", manufacturer=" + manufacturer + ", firmwareVersion=" + firmwareVersion
            + ", haGeographicAddress=" + haGeographicAddress + '}';
    }

}
