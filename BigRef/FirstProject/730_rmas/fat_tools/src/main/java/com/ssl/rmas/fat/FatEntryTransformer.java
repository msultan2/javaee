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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.ssl.rmas.fat.jiradata.FatEntry;

public class FatEntryTransformer {

    private String testType;

    public FatEntryTransformer(String testType) {
        this.testType = testType;
    }

    public Message<String> transform(Message<List<FatEntry>> message) {
        String newPayload = "Requirements,Test,Description,Details\n"
            + message.getPayload().stream().filter(fatEntry -> testType.equals(fatEntry.getTestType())).map(fatEntry ->
                "\"" + fatEntry.getRequirements().stream().collect(Collectors.joining(", ")) + "\","
                + fatEntry.getTestKey() + ","
                + "\"" + (fatEntry.getDescription()==null?"":fatEntry.getDescription().trim()) + "\","
                + "\"" + (fatEntry.getTestDescription()==null?"":fatEntry.getTestDescription().trim()) + "\""
            ).collect(Collectors.joining("\n"));

        Map<String, Object> messageHeaders = new HashMap<>();
        messageHeaders.putAll(message.getHeaders());
        messageHeaders.put(FileHeaders.FILENAME, "data" + testType + ".csv");
        return MessageBuilder.withPayload(newPayload).copyHeaders(messageHeaders).build();
    }
}
