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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.LoginUtils;
import com.ssl.rmas.test.shared.utils.DateTimeUtils;
import com.ssl.rmas.test.shared.utils.HtmlAlertsUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class UserLoginSteps {

    private final String userNameValue = "sergio@ssl.com";
    private final String userToEditUserDetails = "merce@ssl.com";
    private final String userToEditNameValue = "victor@ssl.com";
    private final String heApproverUserNameValue = "roberto@ssl.com";
    private final String helpdeskUserNameValue = "sara@ssl.com";
    private final String userWithExpiredPassword = "johnny@ssl.com";
    private final String userToCheckLoginDateAndTime = "james@ssl.com";
    private final String passwordValue = "ssl1324";
    private final String heApprover2FASecret = "LZEX2HR3IXYCB3HPVLQSB4QB5Q";
    private final String changePasswordTitle = "Change Password";
    private final String lastLoggedMessage = "Last logged in: ";

    private LoginUtils loginUtils;
    private HtmlUtils htmlUtils;
    private HtmlAlertsUtils htmlAlertsUtils;
    private DateTimeUtils dateTimeUtils;
    
    @Autowired
    public void setLoginUtils(LoginUtils loginUtils) {
        this.loginUtils = loginUtils;
    }
    
    @Autowired
    public void setHtmlUtils(HtmlUtils htmlUtils) {
        this.htmlUtils = htmlUtils;
    }
    
    @Autowired
    public void setHtmlAlertsUtils(HtmlAlertsUtils htmlAlertsUtils) {
        this.htmlAlertsUtils = htmlAlertsUtils;
    }

    @Autowired
    public void setDateTimeUtils(DateTimeUtils dateTimeUtils) {
        this.dateTimeUtils = dateTimeUtils;
    }

    @Given("^I have logged into the RMAS system$")
    @When("^I log into the RMAS system$")
    public void logInWithRoleUser() throws TestTimeoutException {
        loginUtils.logIn(userNameValue, passwordValue);
    }

    @Given("^I have logged into RMAS to edit my user details$")
    public void logInUser() throws TestTimeoutException {
        loginUtils.logIn(userToEditUserDetails, passwordValue);
    }

    @Given("^I have logged into the RMAS system as a (|non )helpdesk user$")
    public void logIn(String helpdeskUser) throws TestTimeoutException {
        if ("".equalsIgnoreCase(helpdeskUser)) {
            loginUtils.logIn(helpdeskUserNameValue, passwordValue);
        } else {
            loginUtils.logIn(userNameValue, passwordValue);
        }
    }

    @Given("^I have logged in as an HE approver$")
    public void loginAsHeApprover() throws TestTimeoutException, InvalidKeyException, NoSuchAlgorithmException{
        loginUtils.logIn(heApproverUserNameValue, passwordValue, heApprover2FASecret);
    }

    @Given("^I am logged in as user other than an HE Approver user$")
    public void loginAsNonHeApprover() throws TestTimeoutException{
        loginUtils.logIn(userNameValue, passwordValue);
    }
    
    @Given("^I have logged into RMAS to edit my name$")
    public void loginToEditNameEdit() throws Throwable {
        loginUtils.logIn(userToEditNameValue, passwordValue);
    }
    
    @When("^I log in into the RMAS system with a user with an expired password$")
    public void loginWithExpiredPassword() throws Throwable {
        loginUtils.logInAndRedirectedTo(userWithExpiredPassword, passwordValue, changePasswordTitle);
    }

    @When("^I log into the RMAS system as a new user$")
    public void logInUserWithLastLoginDateAndTime() throws TestTimeoutException {
        loginUtils.logIn(userToCheckLoginDateAndTime, passwordValue);
    }

    @Then("^I should not see a last login message$")
    public void dontSeeLastLoginMessage() throws TestTimeoutException {
       loginUtils.waitForWelcomeToRMAS();
       htmlUtils.waitForJStoLoad();
       htmlAlertsUtils.assertAlertNotPresent(lastLoggedMessage, HtmlAlertsUtils.ALERT_SUCCESS);
    }

    @Then("^I should see the date and time of my last login$")
    public void seeDateAndTimeOfLastLogin() throws InterruptedException, TestTimeoutException{
        loginUtils.waitForWelcomeToRMAS();
        htmlUtils.assertAlertText(lastLoggedMessage);
        String date = htmlAlertsUtils.getAlertMessage(lastLoggedMessage, HtmlAlertsUtils.ALERT_SUCCESS).substring(lastLoggedMessage.length());
        dateTimeUtils.assertDateTimeIsValid(date, "dd-MM-yyyy HH:mm:ss");
    }
}
 