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

public class SearchResult {
    private int startAt;
    private int maxResults;
    private int total;
    private List<Issue> issues = new ArrayList<>();

    public int getStartAt() {
        return startAt;
    }
    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }
    public int getMaxResults() {
        return maxResults;
    }
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public List<Issue> getIssues() {
        return issues;
    }
    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    @Override
    public String toString() {
        return "SearchResult [startAt=" + startAt + ", maxResults=" + maxResults + ", total=" + total + ", issues={"
                + issues.stream().map(Issue::toString).collect(Collectors.joining(", ")) + "}]";
    }
}
