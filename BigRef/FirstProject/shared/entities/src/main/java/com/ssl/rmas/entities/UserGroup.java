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
package com.ssl.rmas.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserGroup {

    @Id
    private String id;

    @Indexed(unique = true)
    private String groupName;
    private DeviceFilter deviceFilter;
    @DBRef
    private AccountClassification accountClassification;
    private UserGroupStatus status = UserGroupStatus.ENABLED;

    public UserGroup() {
    }

    @JsonCreator
    public UserGroup(@JsonProperty("id") String id, @JsonProperty("groupName") String groupName, @JsonProperty("deviceFilter") DeviceFilter deviceFilter,
            @JsonProperty("accountClassification") AccountClassification accountClassification) {
        this.id = id;
        this.groupName = groupName;
        this.deviceFilter = deviceFilter;
        this.accountClassification = accountClassification;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public DeviceFilter getDeviceFilter() {
        return deviceFilter;
    }

    public void setDeviceFilter(DeviceFilter deviceFilter) {
        this.deviceFilter = deviceFilter;
    }

    public AccountClassification getAccountClassification() {
        return accountClassification;
    }

    public void setAccountClassification(AccountClassification accountClassification) {
        this.accountClassification = accountClassification;
    }

    public UserGroupStatus getStatus() {
        return status;
    }

    public void setStatus(UserGroupStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserGroup{" + "id=" + id + ", groupName=" + groupName + ", deviceFilter=" + deviceFilter
            + ", accountClassification=" + accountClassification + ", status=" + status + '}';
    }

}
