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
import java.util.stream.Collectors;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.ssl.rmas.fat.jiradata.FatEntry;
import com.ssl.rmas.fat.jiradata.Issue;

public class RAITFatEntryEndpoint {
    public Message<List<FatEntry>> produceFatEntries(Message<Map<Issue, List<String>>> message) {
        Map<Issue, List<String>> testToRequirementMap = message.getPayload();

        List<FatEntry> fatEntries = testToRequirementMap.entrySet().stream().map(entry -> {
           FatEntry fatEntry = new FatEntry();
           fatEntry.setDescription(entry.getKey().getFields().getDescription());
           fatEntry.setRequirements(entry.getValue());
           fatEntry.setTestDescription(entry.getKey().getFields().getTestDescription());
           fatEntry.setTestKey(entry.getKey().getKey());
           fatEntry.setTestType(entry.getKey().getFields().getTestType().getValue());
           return fatEntry;
        }).collect(Collectors.toList());

        Collections.sort(fatEntries, new Comparator<FatEntry>() {
            @Override
            public int compare(FatEntry o1, FatEntry o2) {
                return o1.getTestKey().compareTo(o2.getTestKey());
            }
        });

        return MessageBuilder.withPayload(fatEntries).copyHeaders(message.getHeaders()).build();
    }
}
