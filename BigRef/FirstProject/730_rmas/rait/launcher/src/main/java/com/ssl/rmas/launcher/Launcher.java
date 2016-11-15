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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 */
package com.ssl.rmas.launcher;

import com.ssl.rmas.launcher.windows.FailedToStartError;
import com.ssl.rmas.launcher.windows.SplashScreen;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    private final LauncherProperties prop = new LauncherProperties();
    private final ProcessUtils processUtils = new ProcessUtils();

    public void stop() throws InterruptedException {
        processUtils.stopWebBrowser();
        processUtils.stopRait();
    }

    public void start() {
        try {
            loadProperties();
            processUtils.setProperties(prop);
            if (raitIsRunning()) {
                processUtils.startWebBrowser();
                return;
            }

            displaySplashScreen();
            processUtils.startRait();
            if (raitHasStarted(prop.raitApplicationTimeout())) {
                LOGGER.info("Started RAIT");
                processUtils.startWebBrowser();
                stopSplashWindow();
                processUtils.monitorProcesses();
                stop();
            } else {
                LOGGER.error("RAIT start timed out");
                displayFailedToStartError();
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("Failed to start RAIT: {}", ex);
            displayFailedToStartError();
        }
    }

    private void displaySplashScreen() {
        Thread fxApplicationThread = new Thread(() -> Application.launch(SplashScreen.class));
        fxApplicationThread.start();
        Platform.setImplicitExit(false);
    }

    private void stopSplashWindow() {
        Platform.exit();
    }

    private boolean raitHasStarted(final Duration timeOut) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeOut.toMillis();
        while (notTimeOut(endTime)) {
            if (raitIsRunning()) {
                return true;
            }
            TimeUnit.SECONDS.sleep(1);
        }
        return false;
    }

    private boolean raitIsRunning() throws IOException {
        return isHostAvailable(prop.raitUrl().getHost(), prop.raitUrl().getPort());
    }

    private boolean isHostAvailable(final String host, final int port) {
        try (Socket socket = new Socket(host, port)) {
            return socket.isBound();
        } catch (IOException ex) {
            LOGGER.debug("Failed to check if host is available {}:{}", host, port, ex);
        }
        return false;
    }

    private boolean notTimeOut(final long endTime) {
        return System.currentTimeMillis() < endTime;
    }

    private void displayFailedToStartError() {
        new JFXPanel();
        Platform.runLater(() -> {
            new FailedToStartError().start(new Stage());
            Platform.exit();
        });
    }

    private void loadProperties() throws IOException {
        prop.loadProperties();
    }

}
