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
package com.ssl.rmas.test.rmas.steps;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.shared.utils.DateTimeUtils;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class SshKeysSteps {

    private static final String SSH_KEYS_LINK = "SSH Key";
    private static final String GENERATE_BUTTON = "Generate new SSH key pair";
    private static final int PUBLIC_KEY_LENGTH = 372;
    private static final String CURRENT_PUBLIC_KEY_TITLE = "Current Public Key ";
    private static final String SUCCESS_GENERATE_NEW_SSH_KEY_PAIR_MESSAGE = "New SSH key pair generated successfully";
    private String PUBLIC_KEY = "";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlButtonUtils htmlButtonUtils;
    @Autowired
    private DateTimeUtils dateTimeUtils;

    @Before
    public void resetPublicKey() {
        this.PUBLIC_KEY = "";
    }

    @When("^I navigate to the SSH keys page$")
    @Given("^I have navigated to the SSH keys page$")
    public void goToSshKeysPage() {
        htmlUtils.clickOnLink(SSH_KEYS_LINK);
        PUBLIC_KEY = htmlUtils.getParagraphContent();
    }

    @When("^I generate a new key pair$")
    public void clickGenerateKeysButton() throws TestTimeoutException {
        htmlButtonUtils.waitAndClick(GENERATE_BUTTON, 1);
    }

    @Then("^I should(| not) see the option to generate a new key pair$")
    public void checkButtonNotPresent(String shouldOrShouldNot) {
        if ("".equalsIgnoreCase(shouldOrShouldNot)) {
            htmlButtonUtils.assertDisplayed(GENERATE_BUTTON);
        } else {
            htmlButtonUtils.assertNotDisplayed(GENERATE_BUTTON);
        }
    }

    @Then("^I should see the current public key with date of key creation$")
    public void checkPublicKeyDisplayed() {
        htmlUtils.assertParagraphLength(PUBLIC_KEY_LENGTH);
        dateTimeUtils.assertDateIsTodayOrExceptionallyYesterday(htmlUtils.getPanelHeadingLocalDate(CURRENT_PUBLIC_KEY_TITLE));
    }
    
    @Given("^I have been notified of success$")
    @Then("^I should be notified of success$")
    public void i_should_be_notified_of_success() throws TestTimeoutException {
        htmlUtils.assertAlertText(SUCCESS_GENERATE_NEW_SSH_KEY_PAIR_MESSAGE, 3);
    }

    @Then("^I should see the new public key on the SSH keys page with the new creation date$")
    public void checkPublicKeyChanged() throws TestTimeoutException {
        htmlUtils.assertParagraphNotContent(PUBLIC_KEY);
        dateTimeUtils.assertDateIsTodayOrExceptionallyYesterday(htmlUtils.getPanelHeadingLocalDate(CURRENT_PUBLIC_KEY_TITLE));
    }
}
