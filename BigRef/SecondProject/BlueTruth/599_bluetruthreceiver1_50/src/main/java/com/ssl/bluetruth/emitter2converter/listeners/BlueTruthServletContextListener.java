/**
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SIMULATION SYSTEMS LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF TH
 * IS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Created on 06-May-2015 02:44 PM
 */
package com.ssl.bluetruth.emitter2converter.listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import com.ssl.bluetruth.emitter2converter.schedules.SchedulerUtils;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;
import ssl.bluetruth.emitter2converter.configuration.ConfigurationManagerImpl;
import ssl.bluetruth.emitter2converter.configuration.DefaultConfiguration;
import ssl.bluetruth.emitter2converter.configuration.DefaultConfigurationFromDataBase;
import ssl.bluetruth.emitter2converter.configuration.OutStationsConfiguration;
import ssl.bluetruth.emitter2converter.configuration.OutStationsConfigurationFromDataBase;

/**
 * This ServletContextListener creates a new ConfigurationManager and saves it
 * in the ServletContext when the server starts up, and deletes all the
 * schedules when its shut down.
 *
 * @author josetrujillo-brenes
 */
public class BlueTruthServletContextListener implements ServletContextListener {

    private final Logger logger = Logger.getLogger(getClass());

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("contextInitialized");
            }
            ServletContext servletContext = contextEvent.getServletContext();
            DefaultConfiguration defaultConfiguration = new DefaultConfigurationFromDataBase();
            OutStationsConfiguration outStationsConfiguration = new OutStationsConfigurationFromDataBase();            
            ConfigurationManager configurationManager = new ConfigurationManagerImpl(defaultConfiguration, outStationsConfiguration);
            servletContext.setAttribute(ConfigurationManager.CONFIGURATION_MANAGER_IN_SERVLETCONTEXT, configurationManager);
        } catch (RuntimeException rex) {
            String message = "Unable to create new ConfigurationManager and save it in the servletContext. Cause: " + rex.getLocalizedMessage();
            logger.error(message, rex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        if (logger.isInfoEnabled()) {
            logger.info("contextDestroyed");
        }
        SchedulerUtils reportsScheduler = new SchedulerUtils();
        reportsScheduler.logAllSchedules();
        reportsScheduler.deleteAllSchedules();
    }

}
