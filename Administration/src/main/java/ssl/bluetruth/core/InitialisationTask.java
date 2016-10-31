/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.core;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;



/**
 *
 * @author etorbett
 */
public class InitialisationTask implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(InitialisationTask.class);

    private final CoreImpl core;
    
    public InitialisationTask(CoreImpl core) {
        this.core = core;
    }

    public void run() {
        final InitialisationStage[] INITIALISATION_STAGES = InitialisationStage.values();

        final int NUMBER_OF_INITIALISATION_STAGES = INITIALISATION_STAGES.length;

        int initialisationStage = 0;

        while (initialisationStage < NUMBER_OF_INITIALISATION_STAGES && !core.isShutdown()) {

            boolean initialisationStageCompleted = false;

            switch (INITIALISATION_STAGES[initialisationStage]) {
                case DATA_STORE:
                {
                    initialisationStageCompleted = initialiseDataStore();
                    break;
                }
                case CLIENT_BACKEND:
                {
                    initialisationStageCompleted = initialiseClientBackend();
                    break;
                }
                case AUTHENTICATION_CLIENT:
                {
                    initialisationStageCompleted = initialiseAuthenticationClient();
                    break;
                }
                default:
                {
                    initialisationStageCompleted = false;
                    break;
                }
            }

            if (initialisationStageCompleted) {
                initialisationStage += 1;
            } else {
                LOGGER.warn("PDATInitialisationTask.run_0:" + INITIALISATION_STAGES[initialisationStage]);
                try {
                    final long timeToFinishWaiting = System.currentTimeMillis() + 60000;
                    while (!core.isShutdown() && (System.currentTimeMillis() < timeToFinishWaiting)) {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException interruptedException) {
                    //Do not handle interrupted exception
                }
            }
        }

        if (initialisationStage == NUMBER_OF_INITIALISATION_STAGES) {
            notifyInitialisationComplete();
        }
    }

    private boolean initialiseResourceManagement() {
        return true; //Not yet implemented
    }

    private boolean initialiseClientBackend() {
        return core.initialiseClientBackend();
    }

    private void notifyInitialisationComplete() {
        core.notifyInitialisationComplete();
    }

    private boolean initialiseAuthenticationClient() {
        return core.initialiseAuthenticationClient();
    }

    private boolean initialiseDataStore() {
        return core.initialiseDataStore();
    }
}
