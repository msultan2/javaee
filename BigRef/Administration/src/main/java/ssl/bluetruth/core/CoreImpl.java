/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.core;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.servlet.ServletContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.core.interfaces.Core;

/**
 *
 * @author etorbett
 */
public final class CoreImpl implements Core {

    private static Logger LOGGER = LogManager.getLogger(CoreImpl.class);
    private final ServletContext servletContext;
    private final Map<String, String> initialisationParameters;
    private final ExecutorService coreInitialisationExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean initialised;
    private volatile boolean shutdown;

    public CoreImpl(ServletContext servletContext) {
        this.servletContext = servletContext;

        //Read all initialisation parameters
        Map<String, String> initParams = new HashMap<String, String>();
        Enumeration initParameterNames = servletContext.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String initParameterName = (String) initParameterNames.nextElement();
            String initParameter = servletContext.getInitParameter(initParameterName);
            initParams.put(initParameterName, initParameter);
        }

        this.initialisationParameters = Collections.unmodifiableMap(initParams);

        initialised = false;
        shutdown = false;

    }

    public void initialise() {
        createAndScheduleInitialisationTask();
    }

    public void shutdown() {
        shutdown = true;

        while (!coreInitialisationExecutor.isTerminated()) {
            //We are actually still starting up!
            //Wait for the Initialisation task to finish first.
            try {
                coreInitialisationExecutor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException interruptedException) {
                LOGGER.warn("Core.shutdown_0" + interruptedException.getMessage());
            }
        }

        DatabaseManager.destroy();
    }

    private void createAndScheduleInitialisationTask() {
        Runnable initialisationTask = new InitialisationTask(this);

        coreInitialisationExecutor.submit(initialisationTask);
        coreInitialisationExecutor.shutdown();
    }

    public boolean isInitialised() {
        return initialised;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    protected void notifyInitialisationComplete() {
        //At this point all subsystems are initialised
        //start automated tasks:

        LOGGER.info("Core.notifyInitialisationComplete_0");

        initialised = true;
    }

    public Map<String, String> getInitialisationParameters() {
        return initialisationParameters;
    }

    protected boolean initialiseClientBackend() {
        // TODO
        return true;
    }

    protected boolean initialiseAuthenticationClient() {
        // TODO
        return true;
    }

    protected boolean initialiseDataStore() {
        boolean retVal = false;
        try {
            DatabaseManager.getInstance();
            retVal = true;
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal("Database cannot be initialised.");
        }
        return retVal;
    }
}
