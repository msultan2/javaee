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
 *
 */
package com.ssl.rmas.test.rmas.steps;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.UserUtils;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlTableUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ApproveOrRejectUserRegistrationRequestSteps {

    private final static String USER_NAME = "Jonas";
    private final static String USER_EMAIL = USER_NAME + "@nowhere.com";
    private final static String USER_NAME2 = "Thomas";
    private final static String USER_EMAIL2 = USER_NAME2 + "@nowhere.com";
    private final static String APPROVE_BUTTON = "Approve";
    private final static String REJECT_BUTTON = "Reject";
    private final static String REJECT_REASON = "Rejected for no good reason";
    private final static String FAILURE_MESSAGE = "Unable to send the E-mail to the user";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlButtonUtils htmlButtonUtils;
    @Autowired
    private HtmlFormUtils htmlFormUtils;
    @Autowired
    private HtmlTableUtils htmlTableUtils;
    @Autowired
    private UserUtils userUtils;

    private int startRowCount;

    @Given("I have created a user registration request to approve")
    public void createUserRegistrationRequestToApprove() {
        userUtils.goToRegistrationPage();
        userUtils.register(USER_NAME, USER_EMAIL);
    }

    @Given("I have created a user registration request to reject$")
    public void createUserRegistrationRequestToReject() {
        userUtils.goToRegistrationPage();
        userUtils.register(USER_NAME2, USER_EMAIL2);
    }

    @When("^I select a pending user registration request to approve$")
    public void selectRequestToApprove() {
        startRowCount = htmlTableUtils.getNumberOfRows();
        htmlUtils.closeAllAlerts();
        htmlUtils.clickOnLink(USER_NAME);
    }

    @When("^I select a pending user registration request to reject$")
    public void selectRequestToReject() {
        startRowCount = htmlTableUtils.getNumberOfRows();
        htmlUtils.closeAllAlerts();
        htmlUtils.clickOnLink(USER_NAME2);
    }

    @When("^I assign user group to the user$")
    public void selectUserGroup(){
        htmlFormUtils.setSelect(HtmlFormUtils.USER_GROUP, "Kent Users");
    }

    @When("^I approve the request$")
    public void approveRequest() {
        htmlButtonUtils.click(APPROVE_BUTTON);
    }

    @When("^I reject the request adding a reason$")
    public void rejectRequestWithReason() {
        htmlFormUtils.set(HtmlFormUtils.REASON_ACCESS_REJECTED_APPROVED, HtmlFormUtils.TEXTAREA_TYPE, REJECT_REASON);
        htmlButtonUtils.click(REJECT_BUTTON);
    }

    @Then("^I should see the list of pending user registration requests without the approved request$")
    public void shouldSeeListOfUsersWithoutTheAcceptedUser() throws TestTimeoutException {
        htmlTableUtils.waitforNumberOfRows(startRowCount - 1, 5);
        htmlTableUtils.assertNumberOfRows(startRowCount - 1);
        htmlTableUtils.assertRowsNotPresent(Collections.singletonList(USER_NAME));
    }

    @Then("^I should see the list of pending user registration requests without the rejected request$")
    public void shouldSeeListOfUsersWithoutTheRejectedUser() throws TestTimeoutException {
        htmlTableUtils.waitforNumberOfRows(startRowCount - 1, 5);
        htmlTableUtils.assertNumberOfRows(startRowCount - 1);
        htmlTableUtils.assertRowsNotPresent(Collections.singletonList(USER_NAME2));
    }

    @Then("^I should be notified that I failed to send email to the user$")
    public void shouldSeeFailedToSendEmailAlert() {
        htmlUtils.assertAlertText(FAILURE_MESSAGE);
        htmlUtils.closeAllAlerts();
    }
}
