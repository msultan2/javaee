/*
 *   THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 *   LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 *   EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 *   BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 *   INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 *   OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 *   Copyright 2016 © Costain Integrated Technology Solutions Limited.
 *   All Rights Reserved.
 */
package com.ssl.rmas.test.rmas.steps;

import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import org.springframework.beans.factory.annotation.Autowired;
import static com.ssl.rmas.test.rmas.steps.DeviceListSteps.TITLE_SIZE_1;
import com.ssl.rmas.test.rmas.utils.LoginUtils;
import com.ssl.rmas.test.shared.utils.HtmlHeaderUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class UserDetailsSteps {

    private static final String USER_DETAILS_TITLE = "User Details";
    private static final String USER_DETAILS_NAME_INPUT = "Sergio";
    private static final String USER_DETAILS_ORGANISATION_INPUT = "SSL";
    private static final String USER_DETAILS_MAINTENANCE_INPUT = "ABC";
    private static final String USER_DETAILS_RCC_INPUT = "South West";
    private static final String USER_DETAILS_ADDRESS_INPUT = "Calle Rio Ebro";
    private static final String USER_DETAILS_EMAIL_INPUT = "sergio@ssl.com";
    private static final String USER_DETAILS_TELEPHONE_INPUT = "123456789";
    private static final String USER_DETAILS_MOBILE_INPUT = "07777777777";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlHeaderUtils htmlHeaderUtils;
    @Autowired
    private HtmlFormUtils htmlFormUtils;

    @When("^I navigate to the user details page$")
    public void navigateToUserDetailsPage() {
        htmlHeaderUtils.clickInSubMenu(LoginUtils.NORMAL_USER_NAME, HtmlUtils.VIEW_PROFILE_LINK);
    }

    @Then("^I can see my user details$")
    public void checkUserDetails() {
        htmlUtils.assertTitleText(USER_DETAILS_TITLE, TITLE_SIZE_1);
        htmlFormUtils.assertInput(HtmlFormUtils.NAME, USER_DETAILS_NAME_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.EMPLOYMENT_ORGANIZATION, USER_DETAILS_ORGANISATION_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.MAINTENANCE_CONTRACT, USER_DETAILS_MAINTENANCE_INPUT);
        htmlFormUtils.assertSelect(HtmlFormUtils.RCC, USER_DETAILS_RCC_INPUT);
        htmlFormUtils.assertTextArea(HtmlFormUtils.CONTACT_ADDRESS, USER_DETAILS_ADDRESS_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.EMAIL, USER_DETAILS_EMAIL_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.PHONE, USER_DETAILS_TELEPHONE_INPUT);
        htmlFormUtils.assertInput(HtmlFormUtils.MOBILE, USER_DETAILS_MOBILE_INPUT);
    }
}
