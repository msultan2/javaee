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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.ssl.rmas.fat.jiradata.Issue;

public class RAITJiraDataProcessor {

    private final Logger logger = LoggerFactory.getLogger(RAITJiraDataProcessor.class);

    @Autowired
    private JiraDataHolder jiraDataHolder;

    public Message<Map<Issue, List<String>>> handleRaitTestQueryResult(Message<List<Boolean>> message) {

        Map<Issue, List<String>> testToRequirementMap = jiraDataHolder.getRaitTests().stream().collect(Collectors.toMap(test -> test, test -> {
            List<String> requirements = jiraDataHolder.getRaitRequirements().stream()
                    .filter(requirement ->
                        requirement.getFields().getIssuelinks().stream()
                            .anyMatch(
                                    issueLink -> {
                                        return (issueLink.getInwardIssue()!=null && issueLink.getInwardIssue().getKey().equals(test.getKey())) || (issueLink.getOutwardIssue()!=null && issueLink.getOutwardIssue().getKey().equals(test.getKey()));
                                    }
                                 )
                            )
                    .map(requirement -> requirement.getFields().getRequirement())
                    .collect(Collectors.toList());
            Collections.sort(requirements, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    if(o1==null || o2 == null) {
                        logger.warn("Failed to sort requirements list for {}, {}", test.getKey(), requirements.stream().collect(Collectors.joining(", ")));
                    }
                    //Requirements all start M: and then a number
                    try {
                        Integer req1 = new Integer(o1.substring(2));
                        Integer req2 = new Integer(o2.substring(2));
                        return req1.compareTo(req2);
                    } catch (NumberFormatException e) {
                        logger.warn("Failed to parse requirements {}, {}", o1, o2);
                        return 0;
                    }
                }
            });
            return requirements;
        })).entrySet().stream().filter(testEntry -> !testEntry.getValue().isEmpty()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return MessageBuilder.withPayload(testToRequirementMap).copyHeaders(message.getHeaders()).build();
    }

}
