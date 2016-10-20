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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FilteredDeviceListContainer {

    private List<Device> deviceList;
    private Optional<List<DeviceFilter>> filterList;

    public FilteredDeviceListContainer(List<Device> deviceList, Optional<List<DeviceFilter>> filterList) {
        this.deviceList = deviceList;
        this.filterList = filterList;
    }

    public List<Device> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    public Optional<List<DeviceFilter>> getFilterList() {
        return filterList;
    }

    public void setFilterList(Optional<List<DeviceFilter>> filterList) {
        this.filterList = filterList;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.deviceList);
        hash = 89 * hash + Objects.hashCode(this.filterList);
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
        final FilteredDeviceListContainer other = (FilteredDeviceListContainer) obj;
        if (!Objects.equals(this.deviceList, other.deviceList)) {
            return false;
        }
        if (!Objects.equals(this.filterList, other.filterList)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "HttpBodyContainer{" + "deviceList=" + deviceList + ", filterList=" + filterList + '}';
    }
}
