/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
package com.ssl.rmas.test.rmas.steps;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.LoginUtils;
import com.ssl.rmas.test.shared.utils.HtmlAlertsUtils;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlHeaderUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import static org.junit.Assert.assertTrue;

public class TwoFactorAuthenticationSteps {

    private final String password = "ssl1324";
    private final String heApproverUserEmail = "roberto@ssl.com";
    private final String existing2faUserEmail = "another.user@costain.com";
    private final String secret = "LZEX2HR3IXYCB3HPVLQSB4QB5Q";

    private LoginUtils loginUtils;
    private HtmlHeaderUtils htmlHeaderUtils;
    private HtmlButtonUtils htmlButtonUtils;
    private HtmlFormUtils htmlFormUtils;
    private HtmlUtils htmlUtils;
    private HtmlAlertsUtils htmlAlertUtils;

    @Autowired
    public void setLoginUtils(LoginUtils loginUtils) {
        this.loginUtils = loginUtils;
    }

    @Autowired
    public void setHtmlHeaderUtils(HtmlHeaderUtils htmlHeaderUtils) {
        this.htmlHeaderUtils = htmlHeaderUtils;
    }

    @Autowired
    public void setHtmlFormUtils(HtmlFormUtils htmlFormUtils) {
        this.htmlFormUtils = htmlFormUtils;
    }

    @Autowired
    public void setHtmlButtonUtils(HtmlButtonUtils htmlButtonUtils) {
        this.htmlButtonUtils = htmlButtonUtils;
    }

    @Autowired
    public void setHtmlUtils(HtmlUtils htmlUtils) {
        this.htmlUtils = htmlUtils;
    }

    @Autowired
    public void setHtmlAlertUtils(HtmlAlertsUtils htmlAlertUtils) {
        this.htmlAlertUtils = htmlAlertUtils;
    }

    @Given("^I have logged in as a non 2FA user$")
    public void i_have_logged_in_as_a_non_2FA_user() throws TestTimeoutException {
        loginUtils.logIn("some.user@costain.com", password);
    }

    @When("^I elect to enable 2FA on my account$")
    public void i_elect_to_enable_2FA_on_my_account() throws TestTimeoutException, IOException {
        htmlHeaderUtils.clickInsideUserMenu("Security token");
        htmlButtonUtils.waitAndClick("Generate new token", 1);
    }

    @When("^I confirm the 2FA details$")
    public void i_confirm_the_2FA_details() throws TestTimeoutException, InvalidKeyException, NoSuchAlgorithmException, IOException {
        htmlFormUtils.waitForFormElement("Verification code", 30);
        Pattern pattern = Pattern.compile("(?:^|\\s)'([^']*?)'(?:$|\\s)");
        Matcher matcher = pattern.matcher(htmlUtils.findParagraphContent("QR Code or enter the code"));
        assertTrue("", matcher.find());
        String secret = matcher.group().replace("'", "").trim();
        String token = loginUtils.generateCodeFromSecret(secret);
        htmlFormUtils.set("Verification code", token);
        htmlButtonUtils.waitAndClick("Verify new token", 1);
    }

    @Then("^I will be presented with a success message$")
    public void i_will_be_presented_with_a_success_message() {
        htmlAlertUtils.assertAlertSuccess();
    }

    @Then("^I will be told to log out and log in again$")
    public void i_will_be_told_to_log_out_and_log_in_again() throws InterruptedException {
        htmlAlertUtils.waitForAlertMessage("Two factor authentication is now active on this account. Please log out and in again.", HtmlAlertsUtils.ALERT_SUCCESS);
    }

    @When("^I have entered the username and password for a 2FA user on the login page$")
    public void i_have_entered_the_username_and_password_for_a_2FA_user_on_the_login_page() throws TestTimeoutException {
        loginUtils.enterLoginDetailsAndClickLogin(existing2faUserEmail, password);
    }

    @Then("^I am required to enter my 2FA code$")
    public void i_am_required_to_enter_my_FA_code() throws TestTimeoutException {
        loginUtils.waitFor2faField();
    }

    @When("^I enter an incorrect 2FA code$")
    public void i_enter_an_incorrect_FA_code() {
        loginUtils.enter2faCodeAndClickLogin("010101");
    }

    @Then("^I am asked to try again$")
    public void i_am_asked_to_try_again() throws InterruptedException {
        htmlAlertUtils.waitForAlertMessage("Login details are incorrect, please check your username, password and security token and try again.", HtmlAlertsUtils.ALERT_WARNING);
    }

    @Then("^the message says my password or code were incorrect$")
    public void the_message_says_my_password_or_code_were_incorrect() throws InterruptedException {
        htmlAlertUtils.waitForAlertMessage("Login details are incorrect, please check your username, password and security token and try again.", HtmlAlertsUtils.ALERT_WARNING);
    }

    @When("^I enter the correct 2FA code$")
    public void i_enter_the_correct_FA_code() throws InvalidKeyException, NoSuchAlgorithmException {
        loginUtils.generateAndenter2faCodeAndClickLogin(secret);
    }

    @Then("^I am shown the RMAS home page$")
    public void i_am_shown_the_RMAS_home_page() throws TestTimeoutException {
        loginUtils.waitForWelcomeToRMAS();
    }

    @Given("^I have entered the username and password for an HE approver on the login page$")
    @When("^I enter the username and password for an HE approver on the login page$")
    public void i_have_entered_the_username_and_password_for_an_HE_approver_on_the_login_page() throws TestTimeoutException {
        loginUtils.enterLoginDetailsAndClickLogin(heApproverUserEmail, password);
    }

    @Given("^I have entered the username and an incorrect password for a 2FA user on the login page$")
    @When("^I enter the username and an incorrect password for a 2FA user on the login page$")
    public void i_have_entered_the_username_and_an_incorrect_password_for_a_2FA_user_on_the_login_page() throws TestTimeoutException {
        loginUtils.enterLoginDetailsAndClickLogin(heApproverUserEmail, "fred");
    }
}
