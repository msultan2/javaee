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

import static com.ssl.rmas.test.rmas.steps.DeviceListSteps.TITLE_SIZE_1;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlHeaderUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class EditUserDetailsSteps {

    private static final String USER_DETAILS_TITLE = "User Details";
    private static final String USER_DETAILS_NAME = "Merce";
    private static final String USER_DETAILS_NAME_INPUT = "Arnold";
    private static final String USER_DETAILS_ORGANISATION_INPUT = "Costain";
    private static final String USER_DETAILS_MAINTENANCE_INPUT = "XZY";
    private static final String USER_DETAILS_RCC_INPUT = "South West";
    private static final String USER_DETAILS_ADDRESS_INPUT = "Riogordo";
    private static final String USER_DETAILS_EMAIL_INPUT = "merce@ssl.com";
    private static final String USER_DETAILS_TELEPHONE_INPUT = "123456789";
    private static final String USER_DETAILS_MOBILE_INPUT = "07777777777";
    private static final String UPDATED_MOBILE_NUMBER = "076645678";
    private static final String UPDATED_PHONE_NUMBER = "987654321";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlHeaderUtils htmlHeaderUtils;
    @Autowired
    private HtmlFormUtils htmlFormUtils;

    @Given("^I have navigated to the user details page$")
    public void navigateToUserDetailsPage() {
        htmlHeaderUtils.clickInsideUserMenu(HtmlUtils.VIEW_PROFILE_LINK);
    }

    @When("^I edit my user details$")
    public void editUserDetails() throws Throwable {
        htmlUtils.clickOnButton("Edit");
        htmlFormUtils.clear(HtmlFormUtils.EMPLOYMENT_ORGANIZATION);
        htmlFormUtils.set(HtmlFormUtils.EMPLOYMENT_ORGANIZATION, USER_DETAILS_ORGANISATION_INPUT);
        htmlFormUtils.set(HtmlFormUtils.MAINTENANCE_CONTRACT, USER_DETAILS_MAINTENANCE_INPUT);
        htmlFormUtils.set(HtmlFormUtils.CONTACT_ADDRESS, HtmlFormUtils.TEXTAREA_TYPE, USER_DETAILS_ADDRESS_INPUT);
        htmlUtils.clickOnButton("Save");
    }

    @Then("^I can view that my user details are updated successfully$")
    public void checkUpdatedUserDetails() throws Throwable {
        htmlUtils.assertAlertText("User details updated successfully");
        htmlUtils.refreshPage();
        htmlUtils.assertTitleText(USER_DETAILS_TITLE, TITLE_SIZE_1);
        htmlFormUtils.assertInput(HtmlFormUtils.NAME, USER_DETAILS_NAME);
        htmlFormUtils.assertInput(HtmlFormUtils.EMPLOYMENT_ORGANIZATION, USER_DETAILS_ORGANISATION_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.MAINTENANCE_CONTRACT, USER_DETAILS_MAINTENANCE_INPUT);
        htmlFormUtils.assertSelect(HtmlFormUtils.RCC, USER_DETAILS_RCC_INPUT);
        htmlFormUtils.assertTextArea(HtmlFormUtils.CONTACT_ADDRESS, USER_DETAILS_ADDRESS_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.EMAIL, USER_DETAILS_EMAIL_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.PHONE, USER_DETAILS_TELEPHONE_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.MOBILE, USER_DETAILS_MOBILE_INPUT);
    }

    @When("^I cancel editing my user details$")
    public void cancelUserDetailsEdit() throws Throwable {
        htmlUtils.clickOnButton("Edit");
        htmlFormUtils.set(HtmlFormUtils.PHONE, UPDATED_PHONE_NUMBER);
        htmlFormUtils.set(HtmlFormUtils.MOBILE, UPDATED_MOBILE_NUMBER);
        htmlUtils.clickOnButton("Cancel");
    }

    @Then("^I can view that my user details remain the same$")
    public void checkUserDetailsAreUnchanged() throws Throwable {
        htmlUtils.refreshPage();
        htmlUtils.assertTitleText(USER_DETAILS_TITLE, TITLE_SIZE_1);
        htmlFormUtils.assertInput(HtmlFormUtils.NAME, USER_DETAILS_NAME);
        htmlFormUtils.assertSelect(HtmlFormUtils.RCC, USER_DETAILS_RCC_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.EMAIL, USER_DETAILS_EMAIL_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.PHONE, USER_DETAILS_TELEPHONE_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.MOBILE, USER_DETAILS_MOBILE_INPUT);
    }

    @When("^I edit my name in my user details$")
    public void editUserName() throws Throwable {
        htmlUtils.clickOnButton("Edit");
        htmlFormUtils.clear(HtmlFormUtils.NAME);
        htmlFormUtils.set(HtmlFormUtils.NAME, USER_DETAILS_NAME_INPUT);
        htmlUtils.clickOnButton("Save");
    }

    @Then("^I can view that my name is updated successfully$")
    public void checkUpdatedName() throws Throwable {
        htmlUtils.assertAlertText("User details updated successfully, user name change will show the next time you log in");
        htmlUtils.refreshPage();
        htmlUtils.assertTitleText(USER_DETAILS_TITLE, TITLE_SIZE_1);
        htmlFormUtils.assertInput(HtmlFormUtils.NAME, USER_DETAILS_NAME_INPUT);
    }
}
