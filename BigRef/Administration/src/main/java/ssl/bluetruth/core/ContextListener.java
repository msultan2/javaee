/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import ssl.bluetruth.chart.actions.ActionsThreadPool;
import ssl.bluetruth.core.interfaces.Core;

/**
 *
 * @author edt
 */
public class ContextListener implements ServletContextListener {

    private static Logger LOGGER = null;

    public void contextInitialized(ServletContextEvent sce) {
        configureLogger(sce.getServletContext());
        initialiseCore(sce.getServletContext());
    }

    public void contextDestroyed(ServletContextEvent sce) {
        shutdownCore(sce.getServletContext());
        ActionsThreadPool.shutdown();   
    }

    private void initialiseCore(ServletContext context) {
        CoreImpl core = new CoreImpl(context);
        context.setAttribute("Core", core);

        core.initialise();
    }

    private void shutdownCore(ServletContext context) {
        Core core = getCore(context);
        if (core != null && core instanceof CoreImpl) {
            CoreImpl coreImpl = (CoreImpl)core;
            coreImpl.shutdown();
        }
        context.removeAttribute("Core");
    }

    private Core getCore(ServletContext context) {
        Core retVal = null;

        Object coreObj = context.getAttribute("Core");
        if (coreObj != null && coreObj instanceof Core) {
            retVal = (Core) coreObj;
        }

        return retVal;
    }

     /**
     * sets up the logger from log4j
     */
    private static void configureLogger(final ServletContext ctx) {

        try {
            //Asynchronous logging
            DOMConfigurator.configureAndWatch(ctx.getRealPath("/WEB-INF/log4j.xml"));
        } catch (Exception ex) {
            BasicConfigurator.configure();
        }
        LOGGER = Logger.getLogger(ContextListener.class);
        LOGGER.info("logger initialised");
    }
}
