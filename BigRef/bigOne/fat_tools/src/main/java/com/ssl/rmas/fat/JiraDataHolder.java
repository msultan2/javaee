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
package com.ssl.rmas.fat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ssl.rmas.fat.jiradata.Issue;

@Component
public class JiraDataHolder {

    private final Logger logger = LoggerFactory.getLogger(JiraDataHolder.class);

    private List<Issue> raitTests = new ArrayList<>();
    private List<Issue> raitRequirements = new ArrayList<>();

    public List<Issue> getRaitTests() {
        return raitTests;
    }
    public void setRaitTests(List<Issue> raitTests) {
        this.raitTests.clear();
        this.raitTests.addAll(raitTests);
        logger.info("Got tests: {}, {}", hashCode(), this.raitTests.stream().map(Issue::getKey).collect(Collectors.joining(", ")));
    }
    public List<Issue> getRaitRequirements() {
        return raitRequirements;
    }
    public void setRaitRequirements(List<Issue> raitRequirements) {
        this.raitRequirements.clear();
        this.raitRequirements.addAll(raitRequirements);
        logger.info("Got requirements: {}, {}", hashCode(), this.raitRequirements.stream().map(Issue::getKey).collect(Collectors.joining(", ")));
    }
}
