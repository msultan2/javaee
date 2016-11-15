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
  * Copyright 2015 © Simulation Systems Ltd. All Rights Reserved.
  *
 */
package com.ssl.rmas.utils;

import com.ssl.rmas.controller.RequestHandlerRedirectWhenExceptionAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.messaging.MessageChannel;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableIntegration
@PropertySource("classpath:config/application.properties")
public class AdviceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AdviceConfiguration.class);
    public static final String RETRY_ADVICE_NAME = "retryAdvice";
    public static final String LOG_AND_IGNORE_ADVICE_NAME = "logAndIgnoreAdvice";
    public static final String REDIRECT_ERROR_CHANNEL_ADVICE_NAME = "redirectErrorChannelAdvice";
    public static final String REDIRECT_ERROR_CHANNEL_RESULT_MANAGER_ADVICE_NAME = "redirectErrorChannelForResultManagerAdvice";
    public static final String ERROR_CHANNEL_NAME = "errorChannel";
    public static final String EXCEPTION_HANDLING_CHANNEL_NAME = "exceptionHandlingChannel";
    public static final String UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME = "updateOperationResultLogAndIgnoreAdvice";

    @Value("${spring.integration.retryTemplateMaxAttempts}")
    private int retryTemplateMaxAttempts;

    @Value("${spring.integration.retryTemplateMillisecondsToWaitBeforeRetry}")
    private int retryTemplateMillisecondsToWaitBeforeRetry;

    @Bean(name = ERROR_CHANNEL_NAME)
    MessageChannel errorChannel() {
        return new DirectChannel();
    }
    
    @Bean(name = EXCEPTION_HANDLING_CHANNEL_NAME)
    MessageChannel exceptionHandlingChannel() {
        return new DirectChannel();
    }

    @Bean
    RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        simpleRetryPolicy.setMaxAttempts(retryTemplateMaxAttempts);
        fixedBackOffPolicy.setBackOffPeriod(retryTemplateMillisecondsToWaitBeforeRetry);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        return retryTemplate;
    }

    @Bean(name = RETRY_ADVICE_NAME)
    @Autowired
    RequestHandlerRetryAdvice retryAdvice(RetryTemplate retryTemplate) {
        RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();
        requestHandlerRetryAdvice.setRetryTemplate(retryTemplate);
        logger.debug("{} created with a Threshold of {} and HalfOpenAfterMilliseconds of {}",
                RETRY_ADVICE_NAME, retryTemplateMaxAttempts, retryTemplateMillisecondsToWaitBeforeRetry);
        return requestHandlerRetryAdvice;
    }

    @Bean(name = LOG_AND_IGNORE_ADVICE_NAME)
    RequestHandlerRetryAdvice ignoreAdvice() {
        RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new NeverRetryPolicy());
        requestHandlerRetryAdvice.setRetryTemplate(retryTemplate);
        requestHandlerRetryAdvice.setRecoveryCallback(retryContext -> {
            logger.warn("Got exception from message handler, ignoring:", retryContext.getLastThrowable());
            return null;
        });
        logger.debug("{} created with a NeverRetryPolicy", LOG_AND_IGNORE_ADVICE_NAME);
        return requestHandlerRetryAdvice;
    }

    @Bean(name = REDIRECT_ERROR_CHANNEL_ADVICE_NAME)
    @Autowired
    RequestHandlerRedirectWhenExceptionAdvice redirectAdvice(@Qualifier(ERROR_CHANNEL_NAME) MessageChannel responseChannel) {
        RequestHandlerRedirectWhenExceptionAdvice advice = new RequestHandlerRedirectWhenExceptionAdvice(responseChannel);
        logger.debug("{} created to redirect when exception to {}", REDIRECT_ERROR_CHANNEL_ADVICE_NAME, responseChannel.toString());
        return advice;
    }
    
    @Bean(name = UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME)
    @Autowired
    RequestHandlerRedirectWhenExceptionAdvice updateOperationResultAndIgnoreAdvice(@Qualifier(EXCEPTION_HANDLING_CHANNEL_NAME) MessageChannel exceptionHandlingChannel) {
        RequestHandlerRedirectWhenExceptionAdvice advice = new RequestHandlerRedirectWhenExceptionAdvice(exceptionHandlingChannel);
        logger.debug("{} created to redirect when exception to {}", UPDATE_OPERATION_RESULT_LOG_AND_IGNORE_ADVICE_NAME, exceptionHandlingChannel.toString());
        return advice;
    }
}
