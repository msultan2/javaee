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
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
@EnableAutoConfiguration
public class RAITFATApplication implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(RAITFATApplication.class);

    @Autowired
    private MessageChannel queryChannel;

    private String raitTests = "{"+
        "\"jql\": \"project = rmas and type=\\\"Acceptance criterion\\\" and status=Approved and issueFunction in linkedIssuesOf(\\\"project=rmas and type=\\\\\\\"Acceptance Criterion\\\\\\\" and labels=\\\\\\\"RAIT\\\\\\\" and labels=\\\\\\\"Phase1\\\\\\\"\\\")\","+
        "\"startAt\": 0,"+
        "\"maxResults\": 150,"+
        "\"fields\": ["+
            "\"key\","+
            "\"summary\","+
            "\"description\","+
            "\"customfield_10604\","+
            "\"test description\""+
        "]"+
    "}";

    private String raitRequirements = "{"+
        "\"jql\": \"project = RMAS AND issuetype=\\\"Acceptance criterion\\\" AND issueFunction in subtasksOf(\\\"key=RMAS-293\\\") and labels=RAIT and labels=Phase1\","+
        "\"startAt\": 0,"+
        "\"maxResults\": 150,"+
        "\"fields\": ["+
            "\"key\","+
            "\"summary\","+
            "\"requirement\","+
            "\"customfield_10400\","+
            "\"issuelinks\""+
        "]"+
    "}";

    @Override
    public void run(String... args) throws Exception {
        Map<String, Object> messageHeaders = new HashMap<>();
        messageHeaders.put("Content-Type", "application/json");
        messageHeaders.put(IntegrationMessageHeaderAccessor.CORRELATION_ID, UUID.randomUUID());
        messageHeaders.put(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE, 2);
        messageHeaders.put("QueryName", "raitTests");
        Message<?> message = MessageBuilder.createMessage(raitTests, new MessageHeaders(messageHeaders));
        queryChannel.send(message);
        logger.info("Sent request 1");

        messageHeaders.put("QueryName", "raitRequirements");
        message = MessageBuilder.createMessage(raitRequirements, new MessageHeaders(messageHeaders));
        queryChannel.send(message);
        logger.info("Sent request 2");

        logger.info("Query1: {}", raitTests);
        logger.info("Query2: {}", raitRequirements);
    }

    public static final void main(String args[]) {
        SpringApplication.run(RAITFATApplication.class, args);
    }

}
