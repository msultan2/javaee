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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Issue {
    private String key;
    private Fields fields = new Fields();
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public Fields getFields() {
        return fields;
    }
    public void setFields(Fields fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Issue [key=" + key + ", fields=" + fields + "]";
    }

    public class Fields {
        private String summary;
        private String description;
        @JsonProperty("customfield_10605")
        private String testDescription;
        @JsonProperty("customfield_10400")
        private String requirement;
        private List<IssueLink> issuelinks = new ArrayList<>();
        private List<Issue> subtasks = new ArrayList<>();
        @JsonProperty("customfield_10604")
        private TestType testType;

        public TestType getTestType() {
            return testType;
        }
        public void setTestType(TestType testType) {
            this.testType = testType;
        }
        public List<Issue> getSubtasks() {
            return subtasks;
        }
        public void setSubtasks(List<Issue> subtasks) {
            this.subtasks = subtasks;
        }
        public void setIssuelinks(List<IssueLink> issuelinks) {
            this.issuelinks = issuelinks;
        }
        public String getSummary() {
            return summary;
        }
        public void setSummary(String summary) {
            this.summary = summary;
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
        public List<IssueLink> getIssuelinks() {
            return issuelinks;
        }
        public void setIssueLinks(List<IssueLink> issuelinks) {
            this.issuelinks = issuelinks;
        }
        @Override
        public String toString() {
            return "Fields [summary=" + summary + ", description=" + description + ", testDescription="
                    + testDescription + ", testType=" + testType + ", requirement=" + requirement + ", issuelinks=" + issuelinks.stream().map(IssueLink::toString).collect(Collectors.joining(", ")) + ", subtasks="
                    + subtasks.stream().map(Issue::toString).collect(Collectors.joining(", ")) + "]";
        }
        public String getRequirement() {
            return requirement;
        }
        public void setRequirement(String requirement) {
            this.requirement = requirement;
        }
    }

    public static class IssueLink {
        private int id;
        private Issue inwardIssue;
        private Issue outwardIssue;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Issue getInwardIssue() {
            return inwardIssue;
        }

        public void setInwardIssue(Issue inwardIssue) {
            this.inwardIssue = inwardIssue;
        }

        public Issue getOutwardIssue() {
            return outwardIssue;
        }

        public void setOutwardIssue(Issue outwardIssue) {
            this.outwardIssue = outwardIssue;
        }

        @Override
        public String toString() {
            return "IssueLink [id=" + id + ", inwardIssue=" + inwardIssue + ", outwardIssue=" + outwardIssue + "]";
        }
    }

    public static class TestType {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "TestType [value=" + value + "]";
        }
    }
}
