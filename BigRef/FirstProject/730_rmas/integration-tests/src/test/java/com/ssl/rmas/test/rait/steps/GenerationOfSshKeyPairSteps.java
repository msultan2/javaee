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
package com.ssl.rmas.test.rait.steps;

import com.ssl.rmas.test.shared.utils.HtmlAlertsUtils;
import com.ssl.rmas.test.shared.utils.HtmlHeaderUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GenerationOfSshKeyPairSteps {

    private final Path expectedSshDirectory = Paths.get(System.getProperty("user.home"), "rait", "sshKeys");
    private final String generateSshKeyPair = "Generate new SSH key pair";
    private final String sshKeysMenu = "SSH Key";
    private final String privateKeyLabel = "Private key:";
    private final String publicKeyLabel = "Public key:";
    private final DateTimeFormatter fileTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlAlertsUtils htmlAlertsUtils;
    @Autowired
    private HtmlHeaderUtils htmlHeaderUtils;
    @Autowired 
    private Clock clock;
    
    @Before("@RMAS-791")
    public void deleteSshFilesIfExists() throws IOException {
        if (expectedSshDirectory.toFile().isDirectory()) {
            FileUtils.cleanDirectory(expectedSshDirectory.toFile());
        }
    }
    
    @When("^I initiate SSH key pair generation$")
    public void initiateSshKeyPairGeneration() {    
        htmlHeaderUtils.click(sshKeysMenu);
        htmlUtils.clickOnButton(generateSshKeyPair);
    }

    @Then("^the location of the generated SSH keys should be shown in the results message$")
    public void checkSuccessfullyGeneratedSshKeyPair() throws InterruptedException {
         htmlAlertsUtils.waitForAlertMessage("New SSH key pair generated successfully", HtmlAlertsUtils.ALERT_SUCCESS);
         assertThat(Paths.get(htmlUtils.getSpanNextToLabel(privateKeyLabel)).getParent(), is(equalTo(expectedSshDirectory)));
         assertThat(Paths.get(htmlUtils.getSpanNextToLabel(publicKeyLabel)).getParent(), is(equalTo(expectedSshDirectory)));
    }

    @Then("^the new SSH key pair should be present in this location$")
    public void checkNewSshKeyPairIsPresent() {
        File[] listOfFiles = expectedSshDirectory.toFile().listFiles();
        validateKeyFiles(listOfFiles);
    }

    private void validateKeyFiles(File[] listOfFiles) {
        assertTrue("There should be two key files", listOfFiles.length == 2);
        for (File file : listOfFiles) {
            validateFile(file);
        }
    }

    private void validateFile(File file) {
        LocalDateTime now = LocalDateTime.now(clock);
        String fileTime = file.getName().substring(7,22);
        LocalDateTime fileDateTime = LocalDateTime.parse(fileTime, fileTimeFormatter);

        checkFileName(file);
        assertFalse("File timestamp should not be in the future",  fileDateTime.isAfter(now));
        assertTrue("File should have been created in the last minute", fileDateTime.until(now, ChronoUnit.SECONDS) < 60L);
    }

    private void checkFileName(File file) {
        String fileName = file.getName();
        assertTrue(fileName.startsWith("id_rsa_"));
        if (fileName.indexOf('.') != -1) {
            assertTrue("File extension of public key should be .pub", fileName.endsWith(".pub"));
        }
    }
}
