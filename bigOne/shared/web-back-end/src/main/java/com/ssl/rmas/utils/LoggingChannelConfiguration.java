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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableIntegration
public class LoggingChannelConfiguration {

    public static final String DEBUG_LOGGING_CHANNEL_NAME = "debugLoggingChannel";
    public static final String INFO_LOGGING_CHANNEL_NAME = "infoLoggingChannel";

    @Bean(name = DEBUG_LOGGING_CHANNEL_NAME)
    MessageChannel debugLoggingChannel() {
        return new DirectChannel();
    }

    @Bean(name = INFO_LOGGING_CHANNEL_NAME)
    MessageChannel infoLoggingChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = DEBUG_LOGGING_CHANNEL_NAME)
    LoggingHandler debugLoggingHandler() {
        LoggingHandler handler = new LoggingHandler("DEBUG");
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = INFO_LOGGING_CHANNEL_NAME)
    LoggingHandler infoLoggingHandler() {
        LoggingHandler handler = new LoggingHandler("INFO");
        return handler;
    }
}
