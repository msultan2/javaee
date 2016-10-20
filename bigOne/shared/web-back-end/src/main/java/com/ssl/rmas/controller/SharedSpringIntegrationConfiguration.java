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
 */
package com.ssl.rmas.controller;

import static com.ssl.rmas.utils.ActivityEnum.DOWNLOAD_LOGS;
import static com.ssl.rmas.utils.ActivityEnum.UPDATE_KEY;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.integration.router.RecipientListRouter;
import org.springframework.messaging.MessageChannel;
import org.springframework.oxm.Unmarshaller;

import com.ssl.rmas.endpoints.AddConnectionParamsToHeaderTransformer;
import com.ssl.rmas.endpoints.AddSecurityContextHeaderTransformer;
import com.ssl.rmas.endpoints.DowngradeFirmwareEndpoint;
import com.ssl.rmas.endpoints.ExceptionHandlingTransformer;
import com.ssl.rmas.endpoints.ExceptionToHttpResponseTransformer;
import com.ssl.rmas.endpoints.ExtractActivityIdTransformer;
import com.ssl.rmas.endpoints.IdGeneratorTransformer;
import com.ssl.rmas.endpoints.LowercaseHeaderTransformer;
import com.ssl.rmas.endpoints.PersistingOperationResultEndpoint;
import com.ssl.rmas.endpoints.RemoveOldSshPublicKeysEndpoint;
import com.ssl.rmas.endpoints.ResetDeviceEndpoint;
import com.ssl.rmas.endpoints.StaticDataEndpoint;
import com.ssl.rmas.endpoints.TemporaryFileStorageTransformer;
import com.ssl.rmas.endpoints.UpdateKeyEndpoint;
import com.ssl.rmas.endpoints.UpgradeFirmwareEndpoint;
import com.ssl.rmas.endpoints.UploadFirmwareEnpoint;
import com.ssl.rmas.endpoints.ValidationFilter;
import com.ssl.rmas.endpoints.VerifyEndpoint;
import com.ssl.rmas.entities.DownloadLogDates;
import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.PublicKey;
import com.ssl.rmas.utils.ActivityEnum;
import com.ssl.rmas.utils.AdviceConfiguration;
import com.ssl.rmas.utils.FileActivityEnum;
import com.ssl.rmas.utils.LoggingChannelConfiguration;
import com.ssl.rmas.utils.StaticDeviceDataXMLToDeviceUnmarshaller;
import java.util.concurrent.Executor;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableIntegration
@PropertySource("classpath:config/application.properties")
@Import(AdviceConfiguration.class)
public class SharedSpringIntegrationConfiguration {

    private final String activityIdChannelName = "activityIdChannel";
    private final String connectionParamsChannelName  = "connectionParamsChannel";
    private final String duplicatorChannelName = "duplicatorChannel";
    private final String uploadingFirmwareFileChannelName = "uploadingFirmwareFileChannel";
    private final String httpResponseDataChannelName = "httpResponseDataChannel";
    private final String connectionParamsInHeaderChannelName = "connectionParamsInHeaderChannel";
    private final String staticDataChannelName = "staticDataChannel";
    private final String updateKeyChannelName = "updateKeyChannel";
    private final String uploadFirmwareChannelName = "uploadFirmwareChannel";
    private final String downloadLogsChannelName = "downloadLogsChannel";
    private final String upgradeFirmwareChannelName = "upgradeFirmwareChannel";
    private final String downgradeFirmwareChannelName = "downgradeFirmwareChannel";
    private final String connectionParamsValidationChannelName = "connectionParamsValidationChannel";
    private final String validationFilterChannelName = "validationFilterChannel";
    private final String resetDeviceChannelName = "resetDeviceChannel";
    private final String verifyChannelName = "verifyChannel";
    private final String lowercaseHeadersChannelName = "lowerCaseHeadersChannel";
    private final String addSecurityContextHeaderChannelName = "addSecurityContextHeaderChannel";
    private final String connectionDetailsChannelName = "connectionDetailsChannel";
    private final String removeOldSshPublicKeysChannelName = "removeOldSshPublicKeysChannel";
    private final String persistingOperationResultChannelName = "persistingOperationResultChannel";
    private final String executorName = "executor";
    private final SpelExpressionParser spelParser = new SpelExpressionParser();
    
    @Value("${spring.integration.exec-channel.core-pool-size: 5}")
    private int execChannelCorePoolSize;
    
    @Value("${spring.integration.exec-channel.max-pool-size: 10}")
    private int execChannelMaxPoolSize;
    
    @Value("${spring.integration.exec-channel.queue-capacity: 10}")
    private int execChannelQueueCapacity;    

    @Bean(name = activityIdChannelName)
    MessageChannel activityIdChannel() {
        return new DirectChannel();
    }

    @Bean(name = uploadingFirmwareFileChannelName)
    MessageChannel uploadingFirmwareFileChannel() {
        return new DirectChannel();
    }

    @Bean(name = httpResponseDataChannelName)
    MessageChannel httpResponseDataChannel() {
        return new DirectChannel();
    }
    
    @Bean(name = executorName)
    Executor getExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(execChannelCorePoolSize);
        executor.setMaxPoolSize(execChannelMaxPoolSize);
        executor.setQueueCapacity(execChannelQueueCapacity);
        executor.initialize();
        return executor;
    }
    
    @Bean(name = connectionParamsChannelName )
    MessageChannel connectionParamsChannel(@Qualifier(value = executorName) Executor executor) {        
        return new ExecutorChannel(executor);
    }

    @Bean(name = duplicatorChannelName)
    MessageChannel duplicatorChannel() {
        return new DirectChannel();
    }

    @Bean(name = staticDataChannelName)
    MessageChannel staticDataCahnnel() {
        return new DirectChannel();
    }

    @Bean(name = updateKeyChannelName)
    MessageChannel updateKeyChannel() {
        return new DirectChannel();
    }

    @Bean(name = downloadLogsChannelName)
    MessageChannel downloadLogsChannel() {
        return new DirectChannel();
    }

    @Bean(name = uploadFirmwareChannelName)
    MessageChannel uploadFirmwareChannel() {
        return new DirectChannel();
    }

    @Bean(name = downgradeFirmwareChannelName)
    MessageChannel downgradeFirmwareChannel() {
        return new DirectChannel();
    }

    @Bean(name = upgradeFirmwareChannelName)
    MessageChannel upgradeFirmwareChannel() {
        return new DirectChannel();
    }

    @Bean(name = resetDeviceChannelName)
    MessageChannel resetDeviceChannel() {
        return new DirectChannel();
    }

    @Bean(name = verifyChannelName)
    MessageChannel verifyChannel() {
        return new DirectChannel();
    }

    @Bean(name = removeOldSshPublicKeysChannelName)
    MessageChannel removeOldSshPublicKeysChannel() {
        return new DirectChannel();
    }

    @Bean(name = connectionParamsValidationChannelName)
    MessageChannel connectionParamsValidationChannel() {
        return new DirectChannel();
    }

    @Bean(name = validationFilterChannelName)
    MessageChannel validationFilterChannel() {
        return new DirectChannel();
    }

    @Bean(name = connectionParamsInHeaderChannelName)
    MessageChannel connectionParamsInHeaderChannel() {
        return new DirectChannel();
    }

    @Bean(name = lowercaseHeadersChannelName)
    MessageChannel lowercaseHeadersChannel() {
        return new DirectChannel();
    }

    @Bean(name = addSecurityContextHeaderChannelName)
    MessageChannel addSecurityContextHeaderChannel() {
        return new DirectChannel();
    }

    @Bean(name = connectionDetailsChannelName)
    MessageChannel connectionDetailsChannel() {
        return new DirectChannel();
    }

    @Bean(name = persistingOperationResultChannelName)
    MessageChannel persistingOperationResultChannel() {
        return new DirectChannel();
    }

    @Bean
    @Autowired
    public HttpRequestHandlingMessagingGateway updateKeyGateway(@Qualifier(value = lowercaseHeadersChannelName) MessageChannel requestChannel,
            @Qualifier(value = httpResponseDataChannelName) MessageChannel responseChannel,
            DefaultHttpHeaderMapper defaultHeaderMapper) {
        HttpRequestHandlingMessagingGateway gateway = getDefaultGateway(requestChannel, responseChannel, "/activity/" + UPDATE_KEY, defaultHeaderMapper);
        gateway.setRequestPayloadType(PublicKey.class);
        Map<String, Expression> headerExpressions = new HashMap<>();
        headerExpressions.put("currentActivity", spelParser.parseExpression("'" + UPDATE_KEY.toString() + "'"));
        gateway.setHeaderExpressions(headerExpressions );
        return gateway;
    }

    @Bean
    @Autowired
    public HttpRequestHandlingMessagingGateway downloadLogsGateway(@Qualifier(value = lowercaseHeadersChannelName) MessageChannel requestChannel,
            @Qualifier(value = httpResponseDataChannelName) MessageChannel responseChannel,
            DefaultHttpHeaderMapper defaultHeaderMapper) {
        HttpRequestHandlingMessagingGateway gateway = getDefaultGateway(requestChannel, responseChannel, "/activity/" + DOWNLOAD_LOGS, defaultHeaderMapper);
        gateway.setRequestPayloadType(DownloadLogDates.class);
        Map<String, Expression> headerExpressions = new HashMap<>();
        headerExpressions.put("currentActivity", spelParser.parseExpression("'" + DOWNLOAD_LOGS.toString() + "'"));
        gateway.setHeaderExpressions(headerExpressions );
        return gateway;
    }

    @Bean
    @Autowired
    public HttpRequestHandlingMessagingGateway defaultActivityGateway(@Qualifier(value = lowercaseHeadersChannelName) MessageChannel requestChannel,
            @Qualifier(value = httpResponseDataChannelName) MessageChannel responseChannel,
            DefaultHttpHeaderMapper defaultHeaderMapper) {
        HttpRequestHandlingMessagingGateway gateway = getDefaultGateway(requestChannel, responseChannel, "/activity/{activityName}", defaultHeaderMapper);
        Map<String, Expression> headerExpressions = new HashMap<>();
        headerExpressions.put("currentActivity", spelParser.parseExpression("#pathVariables.activityName"));
        gateway.setHeaderExpressions(headerExpressions );
        return gateway;
    }

    @Bean
    @Autowired
    public HttpRequestHandlingMessagingGateway defaultFileActivityGateway(
            @Qualifier(uploadFirmwareChannelName) MessageChannel fileRequestChannel,
            @Qualifier(httpResponseDataChannelName) MessageChannel responseChannel,
            DefaultHttpHeaderMapper defaultHeaderMapper) {
        HttpRequestHandlingMessagingGateway gateway = getDefaultGateway(fileRequestChannel, responseChannel, "/fileActivity/{activityName}", defaultHeaderMapper);
        Map<String, Expression> headerExpressions = new HashMap<>();
        headerExpressions.put("currentActivity", spelParser.parseExpression("#pathVariables.activityName"));
        gateway.setHeaderExpressions(headerExpressions );
        return gateway;
    }

    public HttpRequestHandlingMessagingGateway getDefaultGateway(
            MessageChannel requestChannel, MessageChannel responseChannel, String pathPattern, DefaultHttpHeaderMapper defaultHeaderMapper) {
        HttpRequestHandlingMessagingGateway httpGateway = new HttpRequestHandlingMessagingGateway(true);
        RequestMapping requestMapping = new RequestMapping();
        requestMapping.setMethods(HttpMethod.POST);
        requestMapping.setPathPatterns(pathPattern);
        httpGateway.setRequestMapping(requestMapping);
        httpGateway.setRequestChannel(requestChannel);
        httpGateway.setReplyChannel(responseChannel);
        httpGateway.setHeaderMapper(defaultHeaderMapper);
        return httpGateway;
    }

    @Bean(name = "defaultHeaderMapper")
    DefaultHttpHeaderMapper getDefaultHeaderMapper() {
        DefaultHttpHeaderMapper instance = new DefaultHttpHeaderMapper();
        String[] arr = new String[4];
        arr[0] = HeaderKeys.PRIVATE_KEY.toString();
        arr[1] = HeaderKeys.IP_ADDRESS.toString();
        arr[2] = HeaderKeys.BANDWIDTH_LIMIT.toString();
        arr[3] = HeaderKeys.PEW_NUMBER.toString();
        
        instance.setInboundHeaderNames(arr);
        return instance;
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = uploadFirmwareChannelName,
            outputChannel = lowercaseHeadersChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    TemporaryFileStorageTransformer uploadFirmware(@Value("${rmas.staticData.localPath}") String localPath,
            @Value("${rmas.uploadFileStorageSubdirectory:temp}") String tempDir) {
        return new TemporaryFileStorageTransformer(localPath,tempDir);
    }

    @Bean
    @ServiceActivator(inputChannel = lowercaseHeadersChannelName,
            outputChannel = addSecurityContextHeaderChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    LowercaseHeaderTransformer lowercaseHeaderTransformer() {
        return new LowercaseHeaderTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel = addSecurityContextHeaderChannelName,
            outputChannel = connectionDetailsChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    AddSecurityContextHeaderTransformer addSecurityHeaderTransformer() {
        return new AddSecurityContextHeaderTransformer();
    }

    @Bean
    @Filter(inputChannel = connectionParamsValidationChannelName,
            outputChannel = validationFilterChannelName,
            discardChannel = httpResponseDataChannelName,
            adviceChain = AdviceConfiguration.LOG_AND_IGNORE_ADVICE_NAME)
    ValidationFilter validationFilter() {
        return new ValidationFilter();
    }

    @Bean
    @ServiceActivator(inputChannel = validationFilterChannelName,
            outputChannel = connectionParamsInHeaderChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    AddConnectionParamsToHeaderTransformer addConnectionParamsToHeaderTransformer() {
        return new AddConnectionParamsToHeaderTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel = connectionParamsInHeaderChannelName,
            outputChannel = duplicatorChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    IdGeneratorTransformer activityIdGenerator() {
        return new IdGeneratorTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel = duplicatorChannelName, adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    RecipientListRouter activityMessageDuplicator(@Qualifier(value = connectionParamsChannelName ) MessageChannel connectionParamsChannel,
            @Qualifier(value = activityIdChannelName) MessageChannel activityIdChannel) {
        RecipientListRouter router = new RecipientListRouter();
        router.setChannels(Arrays.asList(connectionParamsChannel, activityIdChannel));
        return router;
    }

    @Bean
    @ServiceActivator(inputChannel = activityIdChannelName,
            outputChannel = httpResponseDataChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    ExtractActivityIdTransformer extractActivityId() {
        return new ExtractActivityIdTransformer();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = connectionParamsChannelName ,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    HeaderValueRouter activityMessageChannelRouter() {
        HeaderValueRouter router = new HeaderValueRouter(HeaderKeys.CURRENT_ACTIVITY.toString());
        router.setChannelMapping(ActivityEnum.UPDATE_KEY.toString(), updateKeyChannelName);
        router.setChannelMapping(ActivityEnum.STATIC_DATA.toString(), staticDataChannelName);
        router.setChannelMapping(FileActivityEnum.UPLOAD_FIRMWARE.toString(), uploadingFirmwareFileChannelName);
        router.setChannelMapping(ActivityEnum.DOWNLOAD_LOGS.toString(), downloadLogsChannelName);
        router.setChannelMapping(ActivityEnum.UPGRADE_FIRMWARE.toString(), upgradeFirmwareChannelName);
        router.setChannelMapping(ActivityEnum.DOWNGRADE_FIRMWARE.toString(), downgradeFirmwareChannelName);
        router.setChannelMapping(ActivityEnum.RESET_DEVICE.toString(), resetDeviceChannelName);
        router.setChannelMapping(ActivityEnum.VERIFY.toString(), verifyChannelName);
        router.setChannelMapping(ActivityEnum.REMOVE_OLD_SSH_PUBLIC_KEYS.toString(), removeOldSshPublicKeysChannelName);
        return router;
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = staticDataChannelName,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    StaticDataEndpoint getStaticData(@Value("${rmas.staticData.localPath}") String staticDataRelativeOutputDir) {
        return new StaticDataEndpoint(staticDataRelativeOutputDir);
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = updateKeyChannelName,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    UpdateKeyEndpoint updateKey() {
        return new UpdateKeyEndpoint();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = uploadingFirmwareFileChannelName,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    UploadFirmwareEnpoint uploadFirmwareEnpoint() {
        return new UploadFirmwareEnpoint();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = upgradeFirmwareChannelName,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    UpgradeFirmwareEndpoint upgradeFirmware() {
        return new UpgradeFirmwareEndpoint();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = downgradeFirmwareChannelName,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    DowngradeFirmwareEndpoint downgradeFirmware() {
        return new DowngradeFirmwareEndpoint();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = resetDeviceChannelName,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    ResetDeviceEndpoint resetDeviceEndpoint() {
        return new ResetDeviceEndpoint();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = removeOldSshPublicKeysChannelName,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    RemoveOldSshPublicKeysEndpoint removeOldSshPublicKeysEndpoint() {
        return new RemoveOldSshPublicKeysEndpoint();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = verifyChannelName,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    VerifyEndpoint verifyEndpoint() {
        return new VerifyEndpoint();
    }

    @Bean
    @ServiceActivator(inputChannel = AdviceConfiguration.ERROR_CHANNEL_NAME,
            outputChannel = httpResponseDataChannelName,
            adviceChain = AdviceConfiguration.LOG_AND_IGNORE_ADVICE_NAME)
    ExceptionToHttpResponseTransformer exceptionToHttpResponseTransformer() {
        return new ExceptionToHttpResponseTransformer();
    }
    
    @Bean
    @ServiceActivator(inputChannel = AdviceConfiguration.EXCEPTION_HANDLING_CHANNEL_NAME,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.LOG_AND_IGNORE_ADVICE_NAME)
    ExceptionHandlingTransformer exceptionTransformer() {
        return new ExceptionHandlingTransformer();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = persistingOperationResultChannelName,
            outputChannel = LoggingChannelConfiguration.DEBUG_LOGGING_CHANNEL_NAME,
            adviceChain = AdviceConfiguration.UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    PersistingOperationResultEndpoint persistingOperationResult() {
        return new PersistingOperationResultEndpoint();
    }

    @Bean
    public Unmarshaller unmarshaller() {
        Unmarshaller unmarshaller = new StaticDeviceDataXMLToDeviceUnmarshaller();
        return unmarshaller;
    }
}
