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

import java.util.Objects;
import org.springframework.data.annotation.Id;

public abstract class BaseUser {
    
    @Id
    private String id;
    
    private String name;
    private String address;
    private String primaryPhone;
    private String mobile;
    private String employer;
    private String mcr;
    private String rcc;
    private String projectSponsor;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmployer() {
        return employer;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

    public String getMcr() {
        return mcr;
    }

    public void setMcr(String mcr) {
        this.mcr = mcr;
    }

    public String getRcc() {
        return rcc;
    }

    public void setRcc(String rcc) {
        this.rcc = rcc;
    }

    public String getProjectSponsor() {
        return projectSponsor;
    }

    public void setProjectSponsor(String projectSponsor) {
        this.projectSponsor = projectSponsor;
    } 

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
        hash = 59 * hash + Objects.hashCode(this.name);
        hash = 59 * hash + Objects.hashCode(this.address);
        hash = 59 * hash + Objects.hashCode(this.primaryPhone);
        hash = 59 * hash + Objects.hashCode(this.mobile);
        hash = 59 * hash + Objects.hashCode(this.employer);
        hash = 59 * hash + Objects.hashCode(this.mcr);
        hash = 59 * hash + Objects.hashCode(this.rcc);
        hash = 59 * hash + Objects.hashCode(this.projectSponsor);
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
        final BaseUser other = (BaseUser) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.address, other.address)) {
            return false;
        }
        if (!Objects.equals(this.primaryPhone, other.primaryPhone)) {
            return false;
        }
        if (!Objects.equals(this.mobile, other.mobile)) {
            return false;
        }
        if (!Objects.equals(this.employer, other.employer)) {
            return false;
        }
        if (!Objects.equals(this.mcr, other.mcr)) {
            return false;
        }
        if (!Objects.equals(this.rcc, other.rcc)) {
            return false;
        }
        if (!Objects.equals(this.projectSponsor, other.projectSponsor)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "id=" + id + ", name=" + name + ", address=" + address + ", primaryPhone=" + primaryPhone + ", mobile=" + mobile + ", employer=" + employer + ", mcr=" + mcr + ", rcc=" + rcc + ", projectSponsor=" + projectSponsor;
    }
}
