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
package com.ssl.rmas.ssh.manager;

import java.util.Objects;

public class ConnectionParams {

    private String ipAddress;
    private int bandwidthLimit;
    private String privateKey;
    private int port;
    private String userName;

    public ConnectionParams(String ipAddress, int bandwidthLimit, String privateKey) {
        this.ipAddress = ipAddress;
        this.bandwidthLimit = bandwidthLimit;
        this.privateKey = privateKey;
        this.port = 22;
        this.userName = "rmas_user";
    }

    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setBandwidthLimit(int bandwidthLimit) {
        this.bandwidthLimit = bandwidthLimit;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getBandwidthLimit() {
        return bandwidthLimit;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "ConnectionParams{" + "ipAddress=" + ipAddress + ", bandwidthLimit=" + bandwidthLimit + ", privateKey="
                + privateKey + ", port=" + port + ", userName=" + userName + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.ipAddress);
        hash = 59 * hash + this.bandwidthLimit;
        hash = 59 * hash + Objects.hashCode(this.privateKey);
        hash = 59 * hash + this.port;
        hash = 59 * hash + Objects.hashCode(this.userName);
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
        final ConnectionParams other = (ConnectionParams) obj;
        if (this.bandwidthLimit != other.bandwidthLimit) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (!Objects.equals(this.ipAddress, other.ipAddress)) {
            return false;
        }
        if (!Objects.equals(this.privateKey, other.privateKey)) {
            return false;
        }
        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }
        return true;
    }
}
