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
package com.ssl.bluetruth.receiver.v2.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import ssl.bluetruth.emitter2converter.configuration.ConfigurationManagerImpl;
import ssl.bluetruth.emitter2converter.configuration.DefaultConfiguration;
import ssl.bluetruth.emitter2converter.configuration.DefaultConfigurationFromDataBase;
import ssl.bluetruth.emitter2converter.configuration.OutStationsConfiguration;
import ssl.bluetruth.emitter2converter.configuration.OutStationsConfigurationFromDataBase;

import com.ssl.bluetruth.receiver.v2.test.mocks.MockStatisticsReport;

@Configuration
@ComponentScan(basePackages={"com.ssl.bluetruth.receiver.v2.misc", "com.ssl.bluetruth.receiver.v2.entities"})
public class SpringTestConfig {

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

    @Bean(name="testing")
    public Testing getTesting() {
    	return new Testing();
    }

    @Bean(name="mockStatisticsReport")
    public MockStatisticsReport getMockStatisticsReport() {
    	return new MockStatisticsReport();
    }
}
