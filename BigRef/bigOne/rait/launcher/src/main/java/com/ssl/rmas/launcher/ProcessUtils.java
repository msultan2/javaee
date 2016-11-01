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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import static java.util.concurrent.CompletableFuture.runAsync;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessUtils.class);

    private Process raitProcess;
    private Process webBrowserProcess;
    private LauncherProperties prop;

    void monitorProcesses() {
        try {
            CompletableFuture.anyOf(runAsync(() -> waitFor(raitProcess)), runAsync(() -> waitFor(webBrowserProcess))).get();
        } catch (ExecutionException ex) {
            LOGGER.debug("Stopped monitoring RAIT and web browser processes", ex);
        } catch (InterruptedException ex) {
            LOGGER.debug("Stopped monitoring RAIT and web browser processes", ex);
            Thread.currentThread().interrupt();
        }
    }

    private void waitFor(final Process process) {
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            LOGGER.debug("Failed to wait for process: {}", process);
            Thread.currentThread().interrupt();
        }
    }

    void startRait() throws IOException {
        raitProcess = startProcess(raitCommand());
        LOGGER.info("Starting RAIT...");
    }

    void startWebBrowser() throws IOException {
        webBrowserProcess = startProcess(webBrowserCommand());
        LOGGER.info("Starting web browser...");
    }

    private Process startProcess(final String[] command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(prop.raitHome().toFile());
        redirectErrorAndOutput(pb);
        return pb.start();
    }

    private void redirectErrorAndOutput(final ProcessBuilder pb) {
        pb.redirectError(ProcessBuilder.Redirect.to(prop.raitErrorFile().toFile()));
        pb.redirectOutput(ProcessBuilder.Redirect.to(prop.raitOutputFile().toFile()));
    }

    private String[] raitCommand() throws MalformedURLException {
        String heapArgument = "-Xmx" + prop.javaHeapSize();
        String mainClass = "com.ssl.rmas.RAITApplication";
        String portArgument = "--server.port=" + prop.raitUrl().getPort();
        return new String[]{prop.javaApplicationLauncher(), heapArgument, "-jar", prop.raitJar(), mainClass, portArgument};
    }

    private String[] webBrowserCommand() throws MalformedURLException {
        return new String[]{prop.webBrowserLauncher(), "--user-data-dir=" + prop.webBrowserUserDataDir(),
            "--new-window", prop.raitUrl().toString(), "--no-default-browser-check"};
    }

    void stopWebBrowser() throws InterruptedException {
        if (webBrowserIsRunning()) {
            webBrowserProcess.destroy();
        }
        int exitValue = webBrowserProcess.waitFor();
        LOGGER.info("Stopped web browser, exit value {}", exitValue);
    }

    void stopRait() throws InterruptedException {
        if (isRaitStarting()) {
            tryToSendHttpPostRequest("shutdown");
        }
        int exitValue = raitProcess.waitFor();
        LOGGER.info("Stopped RAIT, exit value {}", exitValue);
    }

    private void tryToSendHttpPostRequest(final String endpoint) {
        try {
            LOGGER.debug("Sending HTTP POST request: {}", endpoint);
            sendHttpPostRequest(endpoint);
        } catch (IOException ex) {
            LOGGER.error("Failed to send HTTP POST request {}", endpoint, ex);
        }
    }

    private void sendHttpPostRequest(final String endpoint) throws IOException {
        HttpURLConnection httpCon = getHttpConnection(endpoint);
        httpCon.connect();
        LOGGER.debug("HTTP response message: {}, status code: {}", httpCon.getResponseMessage(), httpCon.getResponseCode());
    }

    private HttpURLConnection getHttpConnection(final String spec) throws IOException {
        URL url = new URL(prop.raitUrl(), "/" + spec);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("POST");
        return httpCon;
    }

    private boolean webBrowserIsRunning() {
        return webBrowserProcess.isAlive();
    }

    private boolean isRaitStarting() {
        return raitProcess != null && raitProcess.isAlive();
    }

    void setProperties(final LauncherProperties prop) {
        this.prop = prop;
    }

}
