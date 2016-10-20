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

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlUserGroupsTableUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class UserGroupsSteps {

    private final String userGroupsLink = "User Groups";
    private final String administrationLink = "Administration";

    private final String addNewUserGroupButton = "+";
    private final String saveUserGroupButton = "Save";

    private final String userGroupNameField = "Name";
    private final String accountClassificationField = "Account classification";
    private final String ipAddressField = "IP address";
    private final String maintenanceRegionField = "Maintenance region";
    private final String manufacturerField = "Manufacturer";
    private final String rccRegionField = "RCC";

    private final String newUserGroupNameValue = "Nailsea Users";
    private final String newUserGroupClassificationValue = "High";
    private final String modifyUserGroupNameValue = "Bristol Users";
    private final String modifyUserGroupClassificationValue = "Low";
    private final String modifyIpAddressValue = "1.1.1.1";
    private final String modifyMaintenanceRegionValue = "South";
    private final String modifyManufacturerValue = "SSL";
    private final String modifyRccRegionValue = "South East";
    private final String toModifyUserGroupNameValue = "Kent Users";
    private final String otherUserGroupNameValue = "Yatton Users";

    private final List<String> expectedGroups = Arrays.asList(toModifyUserGroupNameValue, otherUserGroupNameValue);
    private final List<String> expectedGroupsWithNewGroup = Arrays.asList(toModifyUserGroupNameValue, otherUserGroupNameValue, newUserGroupNameValue);
    private final List<String> expectedGroupsWithModifiedGroup = Arrays.asList(modifyUserGroupNameValue, otherUserGroupNameValue, newUserGroupNameValue);

    private static final String SUCCESS_USER_GROUP_SAVED = "User group save successful";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlUserGroupsTableUtils htmlUserGroupsTableUtils;
    @Autowired
    private HtmlFormUtils htmlFormUtils;
    @Autowired
    private HtmlUserGroupsTableUtils HtmlUserGroupsTableUtils;

    @Given("^I have navigated to the user groups page$")
    @When("^I navigate to the user groups page$")
    public void goToUserGroupsPage() {
        htmlUtils.clickOnLink(administrationLink);
        htmlUtils.clickOnLink(userGroupsLink);
    }

    @When("^I create a new user group specifying all required fields$")
    public void createNewUserGroup() {
        htmlUtils.clickOnButton(addNewUserGroupButton);
        htmlFormUtils.set(userGroupNameField, newUserGroupNameValue);
        htmlFormUtils.setSelect(accountClassificationField, newUserGroupClassificationValue);
        htmlUtils.clickOnButton(saveUserGroupButton);
        htmlUtils.assertAlertText(SUCCESS_USER_GROUP_SAVED);
    }

    @When("^I modify all the fields of a user group$")
    public void modifyAllValues() {
        htmlUserGroupsTableUtils.clickOnRow(toModifyUserGroupNameValue);
        HtmlUserGroupsTableUtils.setInTable(userGroupNameField, modifyUserGroupNameValue);
        HtmlUserGroupsTableUtils.setSelectInTable(accountClassificationField, modifyUserGroupClassificationValue);
        HtmlUserGroupsTableUtils.setInTable(ipAddressField, modifyIpAddressValue);
        HtmlUserGroupsTableUtils.setInTable(maintenanceRegionField, modifyMaintenanceRegionValue);
        HtmlUserGroupsTableUtils.setInTable(manufacturerField, modifyManufacturerValue);
        HtmlUserGroupsTableUtils.setSelectInTable(rccRegionField, modifyRccRegionValue);
        HtmlUserGroupsTableUtils.clickOnButtonInTable(saveUserGroupButton);
        htmlUtils.assertAlertText(SUCCESS_USER_GROUP_SAVED);
    }

    @Then("^I should see a list of the user groups$")
    public void checkUserGroupsList() throws TestTimeoutException {
        htmlUserGroupsTableUtils.waitforNumberOfRows(expectedGroups.size(), 5);
        htmlUserGroupsTableUtils.assertRowsPresent(expectedGroups);
    }

    @Then("^I should see a list of the user groups including the new group$")
    public void checkUserGroupsListWithNewGroup() throws TestTimeoutException {
        htmlUserGroupsTableUtils.waitforNumberOfRows(expectedGroupsWithNewGroup.size(), 5);
        htmlUserGroupsTableUtils.assertRowsPresent(expectedGroupsWithNewGroup);
    }

    @Then("^I should see a list of the user groups including the modified group$")
    public void checkUserGroupsListWithModifiedGroup() throws TestTimeoutException {
        htmlUserGroupsTableUtils.waitforNumberOfRows(expectedGroupsWithModifiedGroup.size(), 5);
        htmlUserGroupsTableUtils.assertRowsPresent(expectedGroupsWithModifiedGroup);
    }

    @Then("^all fields should be updated$")
    public void checkModifiedUserGroupsValues() throws TestTimeoutException {
        htmlUserGroupsTableUtils.clickOnRow(modifyUserGroupNameValue);
        HtmlUserGroupsTableUtils.assertInputInTable(userGroupNameField, modifyUserGroupNameValue);
        HtmlUserGroupsTableUtils.assertSelectInTable(accountClassificationField, modifyUserGroupClassificationValue);
        HtmlUserGroupsTableUtils.assertInputInTable(ipAddressField, modifyIpAddressValue);
        HtmlUserGroupsTableUtils.assertInputInTable(maintenanceRegionField, modifyMaintenanceRegionValue);
        HtmlUserGroupsTableUtils.assertInputInTable(manufacturerField, modifyManufacturerValue);
        HtmlUserGroupsTableUtils.assertSelectInTable(rccRegionField, modifyRccRegionValue);
    }
}
