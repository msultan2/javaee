/**
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

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.LoginUtils;
import com.ssl.rmas.test.shared.utils.HtmlAlertsUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class AccountLockoutSteps {

    public static final String INCORRECT_LOGIN_MESSAGE = "Login details are incorrect, please check your username and password.";
    private static final String TITLE_SIZE_1 = "1";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private LoginUtils loginUtils;
    @Autowired
    private HtmlAlertsUtils htmlAlertsUtils;

    @Given("^I enter an invalid password (\\d+) times$")
    public void enterInvaildCredentials(int n) throws TestTimeoutException, InterruptedException {
        for (int i = 0; i < n; i++) {
            loginUtils.enterLoginDetailsAndClickLogin("jim.manico@ssl.com", "ssl");
            htmlAlertsUtils.waitForAlertMessage(INCORRECT_LOGIN_MESSAGE,HtmlAlertsUtils.ALERT_WARNING);
        }
    }

    @When("^I enter a valid password$")
    public void enterValidCredentials() throws TestTimeoutException {
        loginUtils.enterLoginDetailsAndClickLogin("jim.manico@ssl.com", "ssl1324");
    }

    @Then("^I should be able to log in the RMAS system$")
    public void checkUserLogedInRmas() throws TestTimeoutException {
        loginUtils.waitForWelcomeToRMAS();
    }

    @Then("^I should see a message telling me that login details are incorrect$")
    public void assertIncorrectLoginDetailsMessage() throws InterruptedException {
        htmlUtils.assertAlertText(INCORRECT_LOGIN_MESSAGE);
    }

    @And("^I should not be able to log in to the RMAS system$")
    public void checkUserCannotLogin() throws TestTimeoutException, InterruptedException {
        enterValidCredentials();
        htmlUtils.assertTitleText(HtmlUtils.LOGIN_PAGE_TITLE, TITLE_SIZE_1);
    }

}
