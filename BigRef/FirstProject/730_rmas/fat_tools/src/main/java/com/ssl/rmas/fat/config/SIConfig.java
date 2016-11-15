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
package com.ssl.rmas.fat.config;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.integration.router.RecipientListRouter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import com.ssl.rmas.fat.FatEntryTransformer;
import com.ssl.rmas.fat.RAITFatEntryEndpoint;
import com.ssl.rmas.fat.RAITJiraDataProcessor;
import com.ssl.rmas.fat.RAITRequirementsPersistorEndpoint;
import com.ssl.rmas.fat.RAITTestResultsPersistorEndpoint;
import com.ssl.rmas.fat.jiradata.SearchResult;

@Configuration
public class SIConfig {

    @Autowired
    private WireTap wireTap;

    @Bean
    public WireTap wireTap() {
        return new WireTap(logChannel());
    }

    private MessageChannel getStandardWireTappedDirectChannel() {
        DirectChannel channel = new DirectChannel();
        channel.addInterceptor(wireTap);
        return channel;
    }

    @Bean
    public MessageChannel queryChannel() {
        return getStandardWireTappedDirectChannel();
    }

    @Bean
    public MessageChannel combineDataChannel() {
        return getStandardWireTappedDirectChannel();
    }
    @Bean
    public MessageChannel processQueryResultsChannel() {
        return getStandardWireTappedDirectChannel();
    }
    @Bean
    public MessageChannel processRaitTestsChannel() {
        return getStandardWireTappedDirectChannel();
    }
    @Bean
    public MessageChannel processRaitRequirementsChannel() {
        return getStandardWireTappedDirectChannel();
    }
    @Bean
    public MessageChannel processJiraDataChannel() {
        return getStandardWireTappedDirectChannel();
    }
    @Bean
    public MessageChannel produceFatEntriesChannel() {
        return getStandardWireTappedDirectChannel();
    }
    @Bean
    public MessageChannel duplicateFatEntryChannel() {
        return getStandardWireTappedDirectChannel();
    }
    @Bean
    public MessageChannel manualFatEntryTransformerChannel() {
        return getStandardWireTappedDirectChannel();
    }
    @Bean
    public MessageChannel automatedFatEntryTransformerChannel() {
        return getStandardWireTappedDirectChannel();
    }
    @Bean
    public MessageChannel logChannel() {
        DirectChannel channel = new DirectChannel();
        return channel;
    }

    @Bean
    @ServiceActivator(inputChannel = "queryChannel")
    HttpRequestExecutingMessageHandler httpRequestExecutingMessageHandler() throws URISyntaxException {
        HttpRequestExecutingMessageHandler messageHandler = new HttpRequestExecutingMessageHandler(new URI("http://jira.ssl.local/rest/api/2/search"));
        messageHandler.setHttpMethod(HttpMethod.POST);
        messageHandler.setExpectReply(true);
        messageHandler.setExpectedResponseType(SearchResult.class);
        messageHandler.setOutputChannelName("processQueryResultsChannel");
        return messageHandler;
    }

    @Bean
    @ServiceActivator(inputChannel = "combineDataChannel")
    public MessageHandler aggregator() {
         AggregatingMessageHandler aggregator = new AggregatingMessageHandler(new DefaultAggregatingMessageGroupProcessor());
         aggregator.setOutputChannelName("processJiraDataChannel");
         return aggregator;
    }

    @Bean
    @ServiceActivator(inputChannel = "logChannel")
    LoggingHandler debugLoggingHandler() {
        LoggingHandler handler = new LoggingHandler("INFO");
        handler.setShouldLogFullMessage(true);
        return handler;
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = "processQueryResultsChannel")
    HeaderValueRouter activityMessageChannelRouter() {
        HeaderValueRouter router = new HeaderValueRouter("QueryName");
        router.setChannelMapping("raitTests", "processRaitTestsChannel");
        router.setChannelMapping("raitRequirements", "processRaitRequirementsChannel");
        return router;
    }

    @Bean
    @ServiceActivator(inputChannel="processRaitTestsChannel", outputChannel="combineDataChannel")
    RAITTestResultsPersistorEndpoint getRAITTestResultsPersistor() {
        return new RAITTestResultsPersistorEndpoint();
    }

    @Bean
    @ServiceActivator(inputChannel="processRaitRequirementsChannel", outputChannel="combineDataChannel")
    RAITRequirementsPersistorEndpoint getRAITRequirmentsResultsPersistor() {
        return new RAITRequirementsPersistorEndpoint();
    }

    @Bean
    @ServiceActivator(inputChannel="processJiraDataChannel", outputChannel="produceFatEntriesChannel")
    RAITJiraDataProcessor getRAITJiraDataProcessor() {
        return new RAITJiraDataProcessor();
    }

    @Bean
    @ServiceActivator(inputChannel="produceFatEntriesChannel", outputChannel="duplicateFatEntryChannel")
    RAITFatEntryEndpoint getFatEntriesEndpoint() {
        return new RAITFatEntryEndpoint();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = "duplicateFatEntryChannel")
    RecipientListRouter fatEntryRouter(MessageChannel manualFatEntryTransformerChannel, MessageChannel automatedFatEntryTransformerChannel) {
        RecipientListRouter router = new RecipientListRouter();
        router.setChannels(Arrays.asList(manualFatEntryTransformerChannel, automatedFatEntryTransformerChannel));
        return router;
    }

    @Bean
    @ServiceActivator(inputChannel="manualFatEntryTransformerChannel", outputChannel="writeToFileChannel")
    FatEntryTransformer manualFatEntryTransformer() {
        return new FatEntryTransformer("Manual");
    }

    @Bean
    @ServiceActivator(inputChannel="automatedFatEntryTransformerChannel", outputChannel="writeToFileChannel")
    FatEntryTransformer automatedFatEntryTransformer() {
        return new FatEntryTransformer("Automated");
    }

    @Bean
    @ServiceActivator(inputChannel = "writeToFileChannel")
    public MessageHandler fileWritingMessageHandler() {
         FileWritingMessageHandler handler = new FileWritingMessageHandler(new File("data"));
         handler.setFileExistsMode(FileExistsMode.REPLACE);
         handler.setExpectReply(false);
         return handler;
    }
}
