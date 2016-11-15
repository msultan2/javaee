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

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssl.cukes.WebserviceConversationHelper;
import com.ssl.rmas.test.rmas.utils.UserUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import static org.junit.Assert.assertEquals;

public class UserRegistrationSteps {

    private static final String NAME = "Jane";
    private static final String EMAIL = "jane@london.co.uk";
    private static final String TITLE_SIZE_1 = "1";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private UserUtils userUtils;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebserviceConversationHelper webserviceConvoHelper;

    @Given("^I am on the user registration page$")
    public void navigateToRegistrationPage() {
        userUtils.goToRegistrationPage();
    }

    @When("^I submit a completed user registration form$")
    public void fillFormOnRegistrationPage() {
        userUtils.register(NAME, EMAIL);
    }

    @Then("^I will be returned to the login page$")
    public void returnTologinPage() {
        htmlUtils.assertTitleText(HtmlUtils.LOGIN_PAGE_TITLE, TITLE_SIZE_1);
    }

    @Then("^I will be notified that my registration form has been successfully submitted$")
    public void successUserRegistration() {
        htmlUtils.assertAlertText(HtmlUtils.SUCCESS_USER_REGISTRATION_MESSAGE);
    }

    @When("^I click on T&C link$")
    public void clickToTAndCPdf() {
        htmlUtils.clickOnLink(HtmlUtils.USER_REGISTRATION_TERMS_AND_CONDITIONS_LINK);
    }

    @Then("^I will get a OK response$")
    public void tAndCPdfIsReadableInOtherTab() throws URISyntaxException, IOException {
        htmlUtils.goToTab(1);
        htmlUtils.assertOkResponseFromCurrentUrl();
        htmlUtils.closeCurrentTab();
        htmlUtils.goToTab(0);
    }

    @When("^I submit an invalid user registration request$")
    public void submitInvalidUserRegistration() throws IOException {
        JsonNode jsonData = objectMapper.readTree("{\"name\":\"4576\",\"address\":\"4576\", \"email\": \"someone@somewhere.com\",\"phone\":\"3456\",\"employer\":\"h\",\"mcr\":\"dfhg\",\"rcc\":\"dfgh\",\"projectSponsor\":\"57319e066ea9bf6e47ce544a\",\"accessRequestReason\":\"dfhg\",\"accessRequired\":\"dfgh\",\"tandcAccepted\":false}");
        webserviceConvoHelper.doPostJSONService(System.getProperty("cucumber.rmas.url", "http://localhost:8080/") + "rmas-core/userRegistrations", jsonData);
    }

    @Then("^I get a bad request response$")
    public void checkForBadRequest() {
        assertEquals("Check for bad request", HttpStatus.BAD_REQUEST, webserviceConvoHelper.getResultsStatusCode());
    }

}
