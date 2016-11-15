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

package com.ssl.rmas.test.rmas.steps;


import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.LoginUtils;
import com.ssl.rmas.test.rmas.utils.UserUtils;
import com.ssl.rmas.test.shared.utils.HtmlHeaderUtils;
import com.ssl.rmas.test.shared.utils.HtmlTableUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class PendingUserRegistrationRequestsSteps {

    private static final String PAGE_TITLE = "Pending User Registration Requests";
    private static final String LOGIN_PAGE_TITLE = "Login";
    private static final String TITLE_SIZE_1 = "1";
    private static final List<String> EXPECTED_HEADERS = Arrays.asList("Name","Employer","RCC", "Maintenance region");
    private static final List<String> EXPECTED_REQUESTERS = Arrays.asList("Merce", "Rocio");
    private static final String PENDING_USER_REGISTRATION_REQUESTS_URL = "#/pendingUserRegistrationRequests";
    private static final String AUTHENTICATION_ERROR_MESSAGE = "Please log in to use the RMAS service";
    private static final String USER_NAME = "Jose Trujillo Brenes";
    private static final String USER_EMAIL = "jose@ssl.com";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlTableUtils htmlTableUtils;
    @Autowired
    private UserUtils userUtils;
    @Autowired
    private LoginUtils loginUtils;

    @When("^I view pending user registration requests$")
    @Given("I have navigated to the pending access requests page")
    public void goToPendingRegistrationPage() {
        userUtils.goToPendingRegistrationPage();
    }

    @Then("^I should see the list of pending user registration requests assigned to me$")
    public void checkPendingRequestsList() throws TestTimeoutException{
        htmlUtils.assertTitleText(PAGE_TITLE, TITLE_SIZE_1);
        htmlTableUtils.assertHeadersPresent(EXPECTED_HEADERS);
        htmlTableUtils.waitforNumberOfRows(EXPECTED_REQUESTERS.size(), 5);
        htmlTableUtils.assertRowsPresent(EXPECTED_REQUESTERS);
    }

    @When("^I try to navigate to the pending user registration requests page$")
    public void navigateToPendingUserRegistrationRequests() throws TestTimeoutException{
        loginUtils.waitForWelcomeToRMAS();
    }

    @Then("^I should not have access to it$")
    public void checkThatNoLinkIsAvailable(){
        htmlUtils.assertLinkNotPresent(HtmlHeaderUtils.ADMINISTRATION_LINK);
    }

    @When("^I go directly to the pending user access requests page$")
    public void navigateToPendingUserRegistrationRequestsDirectly(){
        htmlUtils.goToRmasUrl(PENDING_USER_REGISTRATION_REQUESTS_URL);
    }

    @Then("^I should see the login page with an error message$")
    public void checkErrorMessage(){
        htmlUtils.assertTitleText(LOGIN_PAGE_TITLE, TITLE_SIZE_1);
        htmlUtils.assertAlertText(AUTHENTICATION_ERROR_MESSAGE);
    }

    @Given("I have submitted a user registration request")
    public void submitUserRegistrationRequest(){
        userUtils.goToRegistrationPage();
        userUtils.register(USER_NAME,USER_EMAIL);
    }

    @When("^I view the request$")
    public void viewPendingRequest(){
        htmlUtils.clickOnLink(USER_NAME);
    }

    @Then("^I will be able to see the details of the access request$")
    public void detailsOfAccessRequest() {
        userUtils.assertUserDetails(USER_NAME, USER_EMAIL);
    }
}
