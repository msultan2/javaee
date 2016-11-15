/*
 * 
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ''AS IS'' AND
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
 * Copyright 2015(C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Created By: Estelle Edwards, Simulation Systems Ltd
 *
 * Product:
 */
package com.ssl.bluetruth.receiver.v2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManagerImpl;
import ssl.bluetruth.emitter2converter.configuration.DefaultConfiguration;
import ssl.bluetruth.emitter2converter.configuration.DefaultConfigurationFromDataBase;
import ssl.bluetruth.emitter2converter.configuration.OutStationsConfiguration;
import ssl.bluetruth.emitter2converter.configuration.OutStationsConfigurationFromDataBase;

@EnableWebMvc
@ComponentScan(basePackages = "com.ssl")
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Bean(name = "defaultConfiguration")
    public DefaultConfiguration getDefaultConfiguration() {
        return new DefaultConfigurationFromDataBase();
    }
    
    @Bean(name="outstationConfiguration")
    public OutStationsConfiguration getOutstationConfiguration() {
        return new OutStationsConfigurationFromDataBase();
    }
    
    @Bean
    public ConfigurationManagerImpl getConfigurationManager() {
        return new ConfigurationManagerImpl(getDefaultConfiguration(), getOutstationConfiguration());
    }
}
