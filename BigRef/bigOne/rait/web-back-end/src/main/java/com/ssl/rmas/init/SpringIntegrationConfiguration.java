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

import com.ssl.rmas.endpoints.DownloadLogsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;

import com.ssl.rmas.endpoints.RAITConnectionParameterValidationTransformer;
import com.ssl.rmas.endpoints.RAITDecodePrivateKeyTransformer;
import com.ssl.rmas.utils.AdviceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class SpringIntegrationConfiguration {

    private final String connectionParamsValidationChannelName = "connectionParamsValidationChannel";
    private final String connectionDetailsChannelName = "connectionDetailsChannel";
    private final String downloadLogsChannelName = "downloadLogsChannel";
    private final String persistingOperationResultChannelName = "persistingOperationResultChannel";
    private final String decodePrivateKeyChannelName = "decodePrivateKeyChannel";

    @Bean(name = downloadLogsChannelName)
    MessageChannel downloadLogsChannel() {
        return new DirectChannel();
    }
    
    @Bean(name = decodePrivateKeyChannelName)
    MessageChannel decodePrivateKeyChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = connectionDetailsChannelName,
            outputChannel = decodePrivateKeyChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    RAITConnectionParameterValidationTransformer parametersValidationFilter() {
        return new RAITConnectionParameterValidationTransformer();
    }
    
    @Bean
    @ServiceActivator(inputChannel = decodePrivateKeyChannelName,
            outputChannel = connectionParamsValidationChannelName,
            adviceChain = AdviceConfiguration.REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    RAITDecodePrivateKeyTransformer decodePrivateKey() {
        return new RAITDecodePrivateKeyTransformer();
    }

    @Bean
    @Autowired
    @ServiceActivator(inputChannel = downloadLogsChannelName,
            outputChannel = persistingOperationResultChannelName)
    DownloadLogsEndpoint downloadLogs(@Value("${rmas.downloadLogs.localPath}") String downloadDataDir) {
        return new DownloadLogsEndpoint(downloadDataDir);
    }
}
