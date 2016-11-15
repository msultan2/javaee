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
package com.ssl.rmas.fat.jiradata;

import java.util.List;

public class FatEntry {
    private String testKey;
    private String description;
    private String testDescription;
    private String testType;

    public String getTestType() {
        return testType;
    }
    public void setTestType(String testType) {
        this.testType = testType;
    }
    private List<String> requirements;

    public String getTestKey() {
        return testKey;
    }
    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getTestDescription() {
        return testDescription;
    }
    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }
    public List<String> getRequirements() {
        return requirements;
    }
    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }
    @Override
    public String toString() {
        return "FatEntry [testKey=" + testKey + ", testType=" + testType + ", description=" + description + ", testDescription=" + testDescription
                + ", requirements=" + requirements + "]";
    }
}
