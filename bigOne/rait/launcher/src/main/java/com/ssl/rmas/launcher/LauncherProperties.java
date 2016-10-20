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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

public class LauncherProperties {

    private static final Path PROPERTIES_FILE = Paths.get("launcher.properties");
    private final Properties prop = new Properties();

    void loadProperties() throws IOException {
        try (InputStream inStream = Files.newInputStream(PROPERTIES_FILE)) {
            prop.load(inStream);
        }
    }

    Path raitHome() {
        return Paths.get(String.valueOf(prop.get("rait_home")));
    }

    String javaApplicationLauncher() {
        return String.valueOf(prop.get("java_application_launcher"));
    }

    String webBrowserLauncher() {
        return String.valueOf(prop.get("web_browser_launcher"));
    }

    String raitJar() {
        return String.valueOf(prop.get("rait_jar"));
    }

    Duration raitApplicationTimeout() {
        return Duration.ofSeconds(Long.parseLong(String.valueOf(prop.get("rait_application_timeout"))));
    }

    URL raitUrl() throws MalformedURLException {
        return new URL(String.valueOf(prop.get("rait_url")));
    }

    String webBrowserUserDataDir() {
        return String.valueOf(prop.get("web_browser_user_data_dir"));
    }

    String javaHeapSize() {
        return prop.getProperty("java_heap_size");
    }

    Path raitOutputFile() {
        return Paths.get(System.getProperty("user.home"), "rait", prop.getProperty("rait_output_file"));
    }

    Path raitErrorFile() {
        return Paths.get(System.getProperty("user.home"), "rait", prop.getProperty("rait_error_file"));
    }
}
