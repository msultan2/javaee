/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
package com.ssl.rmas.init;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.security.channel.SecurityContextPropagationChannelInterceptor;
import org.springframework.messaging.MessageChannel;

import com.ssl.rmas.controller.SharedSpringIntegrationConfiguration;
import com.ssl.rmas.endpoints.ArchiveLogsEndpoint;
import com.ssl.rmas.endpoints.DownloadLogsEndpoint;
import com.ssl.rmas.endpoints.PreparePasswordChangedEmailTransformer;
import com.ssl.rmas.endpoints.PreparePasswordResetTokenEmailTransformer;
import com.ssl.rmas.endpoints.PrepareProfileChangedEmailTransformer;
import com.ssl.rmas.endpoints.PrepareRmasWelcomeEmailTransformer;
import com.ssl.rmas.endpoints.PrepareTemplatedEmailTransformer;
import com.ssl.rmas.endpoints.PrepareUserRejectedEmailTransformer;
import com.ssl.rmas.endpoints.PewNumberValidationTransformer;
import com.ssl.rmas.endpoints.RMASConnectionParameterValidationTransformer;
import com.ssl.rmas.endpoints.ResetPasswordEndpoint;
import com.ssl.rmas.endpoints.SendEmailEndpoint;
import com.ssl.rmas.utils.AdviceConfiguration;

@Configuration
@IntegrationComponentScan(basePackages={"com.ssl.rmas.endpoints"})
public class SpringIntegrationConfiguration {

    private final String connectionParamsValidationChannelName = "connectionParamsValidationChannel";
    private final String connectionDetailsChannelName  = "connectionDetailsChannel";
    private final String pewNumberValidationChannelName  = "connectionParamsValidationChannelName";
    private final String httpResponseDataChannelName = "httpResponseDataChannel";
    private final String resetPasswordChannelName = "resetPasswordChannel";
    private final SpelExpressionParser spelParser = new SpelExpressionParser();
    private final String sendPasswordResetTokenEmailChannelName = "sendPasswordResetTokenEmailChannel";
    private final String sendRmasWelcomeTokenEmailChannelName = "sendRmasWelcomeTokenEmailChannel";
    private final String sendUserRejectedEmailChannelName = "sendUserRejectedEmailChannel";
    private final String sendPasswordChangedEmailChannelName = "sendPasswordChangedEmailChannel";
    private final String sendProfileChangedEmailChannelName = "sendProfileChangedEmailChannel";
    private final String sendTemplatedEmailChannelName = "sendTemplatedEmail";
    private final String sendEmailChannelName = "sendEmailChannel";
    private final String archiveLogsChannelName = "archiveLogsChannel";
    private final String downloadLogsChannelName = "downloadLogsChannel";
    private final String persistingOperationResultChannelName = "persistingOperationResultChannel";

    private SharedSpringIntegrationConfiguration sharedSIConfig;

    @Autowired
    public void setSharedSpringIntegrationConfiguration(SharedSpringIntegrationConfiguration sharedSIConfig) {
        this.sharedSIConfig = sharedSIConfig;
    }

    @Bean(name = resetPasswordChannelName)
    MessageChannel resetPasswordChannel() {
        return new DirectChannel();
    }

    @Bean(name = sendPasswordResetTokenEmailChannelName)
    MessageChannel sendPasswordResetTokenEmailChannel() {
        return new DirectChannel();
    }

    @Bean(name = sendRmasWelcomeTokenEmailChannelName)
    MessageChannel sendRmasWelcomeTokenEmailChannel() {
        return new DirectChannel();
    }

    @Bean(name = sendPasswordChangedEmailChannelName)
    MessageChannel sendPasswordChangedEmailChannel() {
        return new DirectChannel();
    }

    @Bean(name = sendProfileChangedEmailChannelName)
    MessageChannel sendProfileChangedEmailChannel() {
        return new DirectChannel();
    }

    @Bean(name = sendUserRejectedEmailChannelName)
    MessageChannel sendUserRejectedEmailChannel() {
        return new DirectChannel();
    }

    @Bean(name = sendEmailChannelName)
    MessageChannel sendEmailChannel() {
        return new DirectChannel();
    }

    @Bean(name = downloadLogsChannelName)
    MessageChannel downloadLogsChannel() {
        return new DirectChannel();
    }

    @Bean(name = archiveLogsChannelName)
    MessageChannel archiveLogsChannel() {
        return new DirectChannel();
    }

    @Bean(name = connectionParamsValidationChannelName)
    MessageChannel connectionParamsValidationChannel() {
        return new DirectChannel();
    }

    @Bean
    @GlobalChannelInterceptor
    public SecurityContextPropagationChannelInterceptor globalSecurityChannelInterceptor() {
        return new SecurityContextPropagationChannelInterceptor();
    }

    @Bean
    @ServiceActivator(inputChannel = connectionDetailsChannelName ,
            outputChannel =  pewNumberValidationChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    PewNumberValidationTransformer pewNumberValidationFilter() {
        return new PewNumberValidationTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel = pewNumberValidationChannelName,
            outputChannel = connectionParamsValidationChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    RMASConnectionParameterValidationTransformer parametersValidationFilter() {
        return new RMASConnectionParameterValidationTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel = resetPasswordChannelName)
    ResetPasswordEndpoint resetPasswordEndpoint() {
        return new ResetPasswordEndpoint();
    }

    @Bean
    @Autowired
    HttpRequestHandlingMessagingGateway resetPassword(@Qualifier(value = resetPasswordChannelName) MessageChannel requestChannel,
            @Qualifier(value = httpResponseDataChannelName) MessageChannel responseChannel,DefaultHttpHeaderMapper defaultHeaderMapper) {
        HttpRequestHandlingMessagingGateway gateway = sharedSIConfig.getDefaultGateway(requestChannel, responseChannel, "resetPassword/{tokenId}", defaultHeaderMapper);
        Map<String, Expression> headerExpressions = new HashMap<>();
        headerExpressions.put("tokenId", spelParser.parseExpression("#pathVariables.tokenId"));
        gateway.setHeaderExpressions(headerExpressions );
        gateway.setRequestPayloadType(String.class);
        return gateway;
    }

    @Bean
    @ServiceActivator(inputChannel=sendPasswordResetTokenEmailChannelName, outputChannel=sendTemplatedEmailChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    public PreparePasswordResetTokenEmailTransformer sendPasswordResetTokenEmailService() {
        return new PreparePasswordResetTokenEmailTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel=sendRmasWelcomeTokenEmailChannelName, outputChannel=sendTemplatedEmailChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    public PrepareRmasWelcomeEmailTransformer prepareRmasWelcomeEmailTransformer() {
        return new PrepareRmasWelcomeEmailTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel=sendUserRejectedEmailChannelName, outputChannel=sendTemplatedEmailChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    public PrepareUserRejectedEmailTransformer prepareUserRejectedEmailTransformer() {
        return new PrepareUserRejectedEmailTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel=sendPasswordChangedEmailChannelName, outputChannel=sendTemplatedEmailChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    public PreparePasswordChangedEmailTransformer preparePasswordChangedEmailTransformer() {
        return new PreparePasswordChangedEmailTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel=sendProfileChangedEmailChannelName, outputChannel=sendTemplatedEmailChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    public PrepareProfileChangedEmailTransformer prepareProfileChangedEmailTransformer() {
        return new PrepareProfileChangedEmailTransformer();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel=sendTemplatedEmailChannelName, outputChannel=sendEmailChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    public PrepareTemplatedEmailTransformer sendTemplatedEmailEndpoint() {
        return new PrepareTemplatedEmailTransformer();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel=sendEmailChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    public SendEmailEndpoint sendEmailEndpoint() {
        return new SendEmailEndpoint();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = downloadLogsChannelName,
            outputChannel = archiveLogsChannelName)
    DownloadLogsEndpoint downloadLogs(@Value("${rmas.downloadLogs.localPath}") String downloadDataDir) {
        return new DownloadLogsEndpoint(downloadDataDir);
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = archiveLogsChannelName,
            outputChannel = persistingOperationResultChannelName)
    ArchiveLogsEndpoint archiveLogs() {
        return new ArchiveLogsEndpoint();
    }
}
