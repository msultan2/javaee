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
  *
 */
package com.ssl.rmas.test.rait.steps;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.rmas.test.shared.utils.HtmlModalUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class DownloadStaticDataSteps {

    private final Path baseLogDir = Paths.get(System.getProperty("user.home"), "rait", DeviceConnectionSteps.DEVICE_IP);
    private final Path staticDataLog = baseLogDir.resolve("staticdata.log");

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlModalUtils htmlModalUtils;

    @BeforeClass
    public void deleteStaticDataIfExists() throws IOException{
        Files.deleteIfExists(staticDataLog);
    }

    @When("^I initiate the static data download$")
    public void initiateGetStaticDataDownload() {
        htmlUtils.clickOnButton(HtmlUtils.DOWNLOAD_STATIC_DATA_BUTTON);
    }

    @Then("^the location of the downloaded file should be shown in results pop up$")
    public void checkFileDownload() throws InterruptedException {
        htmlModalUtils.waitForStatus(HtmlModalUtils.STATUS_SUCCESS);
        final File staticDataLogFile = staticDataLog.toFile();
        assertTrue("Log file exists", staticDataLogFile.exists());
        assertFalse("Log file is not a directory", staticDataLogFile.isDirectory());
        assertTrue("Log file is less than one minute old", htmlModalUtils.isFileLessThanOneMinOld(staticDataLogFile));
    }
}
