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

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlHeaderUtils;
import com.ssl.rmas.test.shared.utils.HtmlUserGroupsTableUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class AccountClassificationsSteps {

    private final String administrationLink = "Administration";
    private final String addNewAccountClassificationButton = "+";
    private final String accountClassficationNameField = "Name";
    private final String roadSideDeviceLogCheckBox = "Roadside device log retrieval";
    private final String roadSideDeviceFirmwareChangeCheckBox = "Roadside device firmware change";
    private final String twoFactorAuthenticationLabel = "Two Factor Authentication is required";
    private final String saveAccountClassificationButton = "Save";
    private final String accountClassificationSaveMessage = "Account classification save successful";
    private final String accountClassificationAlreadyExistsMessage = "The entry already exists. Please change the name and try again";
    
    private final String accountClassification1 = "Low";
    private final String accountClassification2 = "Medium";
    private final String newAccountClassifcationNameValue = "Hiper High";
    private final List<String> accountClassificationsList = Arrays.asList(accountClassification1, accountClassification2);
    private final List<String> accountClassificationsListWithNewOne = ListUtils.union(accountClassificationsList, Arrays.asList(newAccountClassifcationNameValue));

    @Autowired
    private HtmlHeaderUtils htmlHeaderUtils;
    @Autowired
    private HtmlUserGroupsTableUtils htmlUserGroupsTableUtils;
    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlFormUtils htmlFormUtils;

    @Given("^I have navigated to the account classifications page$")
    @When("^I navigate to the account classifications page$")
    public void goToAccountClassficationPage() {
        htmlHeaderUtils.clickInSubMenu(administrationLink, HtmlHeaderUtils.ACCOUNT_CLASSIFICATIONS_LINK);
    }

    @Then("^I should see a list of account classifications$")
    public void checkAccountClassificationList() throws TestTimeoutException {
        htmlUserGroupsTableUtils.waitforNumberOfRows(accountClassificationsList.size(), 2);
        htmlUserGroupsTableUtils.assertRowsPresent(accountClassificationsList);
    }

    @When("^I create a new account classification$")
    public void createNewAccountClassifcation() {
        htmlUtils.clickOnButton(addNewAccountClassificationButton);
        htmlFormUtils.set(accountClassficationNameField, newAccountClassifcationNameValue);
        htmlFormUtils.clickOnCheckbox(roadSideDeviceLogCheckBox);
        htmlFormUtils.clickOnCheckbox(roadSideDeviceFirmwareChangeCheckBox);
        htmlFormUtils.assertLabel(twoFactorAuthenticationLabel);
        htmlUtils.clickOnButton(saveAccountClassificationButton);
    }

    @When("^I create an account classification which already exists$")
    public void createNewAccountClassifcationThatExists() {
        htmlUtils.clickOnButton(addNewAccountClassificationButton);
        htmlFormUtils.set(accountClassficationNameField, accountClassification2);
        htmlUtils.clickOnButton(saveAccountClassificationButton);
    }

    @Then("^I should be notified that the account classification has been created$")
    public void assertAlertOfAccountClassificationCreated() {
        htmlUtils.assertAlertText(accountClassificationSaveMessage);
    }

    @Then("^I should see a list of account classifications including the new account classification$")
    public void checkAccountClassificationListWithAccountClassification() throws TestTimeoutException {
        htmlUserGroupsTableUtils.waitforNumberOfRows(accountClassificationsListWithNewOne.size(), 3);
        htmlUserGroupsTableUtils.assertRowsPresent(accountClassificationsListWithNewOne);
    }

    @Then("^I should be notified of failure to create the account classification$")
    public void assertAccountClassificationAlreadyExists() {
        htmlUtils.assertAlertText(accountClassificationAlreadyExistsMessage);
    }
}
