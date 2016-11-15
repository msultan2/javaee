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

import com.ssl.rmas.test.shared.utils.HtmlUtils;
import com.ssl.rmas.test.shared.utils.HtmlUserGroupsTableUtils;
import com.ssl.rmas.test.shared.utils.HtmlAlertsUtils;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlHeaderUtils;
import com.ssl.rmas.test.shared.utils.HtmlModalUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.LoginUtils;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.collections4.ListUtils;

public class UserGroupsSteps {

    private final String userGroupsLink = "User Groups";
    private final String administrationLink = "Administration";

    private final String addNewUserGroupButton = "+";
    private final String saveUserGroupButton = "Save";
    private final String suspendUserGroupButton = "Suspend";
    private final String reEnabledUserGroupButton = "Re-enable";
    private final String acceptButton = "Accept";
    private final String deleteUserGroupButton = "Delete";

    private final String userGroupNameField = "Name";
    private final String accountClassificationField = "Account classification";
    private final String ipAddressField = "IP address";
    private final String maintenanceRegionField = "Maintenance region";
    private final String manufacturerField = "Manufacturer";
    private final String rccRegionField = "RCC";

    private final String newUserGroupNameValue = "Nailsea Users";
    private final String newUserGroupClassificationValue = "Medium";
    private final String modifyUserGroupNameValue = "Bristol Users";
    private final String modifyUserGroupClassificationValue = "Low";
    private final String modifyIpAddressValue = "1.1.1.1";
    private final String modifyMaintenanceRegionValue = "South";
    private final String modifyManufacturerValue = "SSL";
    private final String modifyRccRegionValue = "South East";
    private final String toModifyUserGroupNameValue = "Kent Users";
    private final String otherUserGroupNameValue = "Yatton Users";
    private final String enabledToSuspendedUserGroup = "Enabled group to be suspended";
    private final String enabledToSuspendedUserGroupWithUserToLogIn = "Enabled group to be suspended with user to login";
    private final String suspendedToEnabledUserGroup = "Suspended group to be enabled";
    private final String suspendedToEnabledUserGroupWithUserToLogIn = "Suspended group to be enabled with user to login";
    private final String userGroupWithUsersToDelete = "User group with users to delete";
    private final String userGroupWithoutUsersToDelete = "User group without users to delete";

    private final String userToTestSuspendedGroup = "user@toTest.suspendedGroup";
    private final String userToTestReEnabledGroup = "user@toTest.reEnabledGroup";

    private final List<String> groups = Arrays.asList(otherUserGroupNameValue, enabledToSuspendedUserGroup, enabledToSuspendedUserGroupWithUserToLogIn, suspendedToEnabledUserGroup, suspendedToEnabledUserGroupWithUserToLogIn);
    private final List<String> expectedGroups = ListUtils.union(groups, Arrays.asList(toModifyUserGroupNameValue));
    private final List<String> expectedGroupsWithNewGroup = ListUtils.union(groups, Arrays.asList(toModifyUserGroupNameValue, newUserGroupNameValue));
    private final List<String> expectedGroupsWithModifiedGroup = ListUtils.union(groups, Arrays.asList(modifyUserGroupNameValue, newUserGroupNameValue));

    private static final String SUCCESS_USER_GROUP_SAVED = "User group save successful";
    private static final String SUCCESS_USER_GROUP_SUSPENDED = "User group has been suspended";
    private static final String SUCCESS_USER_GROUP_RE_ENABLED = "User group has been re-enabled";
    private static final String SUSPENDED_ACCOUNT_ERROR_MESSAGE = "Your RMAS account has been suspended, please contact the RMAS service desk";
    private static final String SUCCESS_USER_GROUP_DELETE = "User group has been deleted";
    private static final String FAILURE_USER_GROUP_DELETE = "You are not allowed to delete a user group that contains users";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlUserGroupsTableUtils htmlUserGroupsTableUtils;
    @Autowired
    private HtmlFormUtils htmlFormUtils;
    @Autowired
    private HtmlModalUtils htmlModalUtils;
    @Autowired
    private LoginUtils loginUtils;
    @Autowired
    private HtmlHeaderUtils htmlHeaderUtils;
    @Autowired
    private HtmlAlertsUtils htmlAlertsUtils;

    @Given("^I have navigated to the user groups page$")
    @When("^I navigate to the user groups page$")
    public void goToUserGroupsPage() {
        htmlHeaderUtils.clickInSubMenu(administrationLink, userGroupsLink);
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
        htmlUserGroupsTableUtils.setInTable(userGroupNameField, modifyUserGroupNameValue);
        htmlUserGroupsTableUtils.setSelectInTable(accountClassificationField, modifyUserGroupClassificationValue);
        htmlUserGroupsTableUtils.setInTable(ipAddressField, modifyIpAddressValue);
        htmlUserGroupsTableUtils.setInTable(maintenanceRegionField, modifyMaintenanceRegionValue);
        htmlUserGroupsTableUtils.setInTable(manufacturerField, modifyManufacturerValue);
        htmlUserGroupsTableUtils.setSelectInTable(rccRegionField, modifyRccRegionValue);
        htmlUserGroupsTableUtils.clickOnButtonInTable(saveUserGroupButton);
        htmlUtils.assertAlertText(SUCCESS_USER_GROUP_SAVED);
    }

    @Then("^I should see a list of the user groups$")
    public void checkUserGroupsList() throws TestTimeoutException {
        htmlUserGroupsTableUtils.assertRowsPresent(expectedGroups);
    }

    @Then("^I should see a list of the user groups including the new group$")
    public void checkUserGroupsListWithNewGroup() throws TestTimeoutException {
        htmlUserGroupsTableUtils.assertRowsPresent(expectedGroupsWithNewGroup);
    }

    @Then("^I should see a list of the user groups including the modified group$")
    public void checkUserGroupsListWithModifiedGroup() throws TestTimeoutException {
        htmlUserGroupsTableUtils.assertRowsPresent(expectedGroupsWithModifiedGroup);
    }

    @Then("^all fields should be updated$")
    public void checkModifiedUserGroupsValues() throws TestTimeoutException {
        htmlUserGroupsTableUtils.clickOnRow(modifyUserGroupNameValue);
        htmlUserGroupsTableUtils.assertInputInTable(userGroupNameField, modifyUserGroupNameValue);
        htmlUserGroupsTableUtils.assertSelectInTableByLabel(accountClassificationField, modifyUserGroupClassificationValue);
        htmlUserGroupsTableUtils.assertInputInTable(ipAddressField, modifyIpAddressValue);
        htmlUserGroupsTableUtils.assertInputInTable(maintenanceRegionField, modifyMaintenanceRegionValue);
        htmlUserGroupsTableUtils.assertInputInTable(manufacturerField, modifyManufacturerValue);
        htmlUserGroupsTableUtils.assertSelectInTable(rccRegionField, modifyRccRegionValue);
    }

    @When("^I suspend a user group$")
    public void suspend() {
        htmlUserGroupsTableUtils.clickOnRow(enabledToSuspendedUserGroup);
        htmlUserGroupsTableUtils.clickOnButtonInTable(suspendUserGroupButton);
        htmlModalUtils.clickOnButton(acceptButton);
    }

    @Given("^I have suspended a user group$")
    public void suspendToLogIn() {
        htmlHeaderUtils.clickInSubMenu(administrationLink, userGroupsLink);
        htmlUserGroupsTableUtils.clickOnRow(enabledToSuspendedUserGroupWithUserToLogIn);
        htmlUserGroupsTableUtils.clickOnButtonInTable(suspendUserGroupButton);
        htmlModalUtils.clickOnButton(acceptButton);
    }

    @When("^I try to log in with a user of the suspended group$")
    public void logInSuspendedUser() throws TestTimeoutException {
        loginUtils.logOut();
        loginUtils.enterLoginDetailsAndClickLogin(userToTestSuspendedGroup, LoginUtils.PASSWORD);
    }

    @Then("^I should not be able to log in$")
    public void checkNotAbleToLogIn() {
        htmlUtils.assertTitleText(HtmlUtils.LOGIN_PAGE_TITLE, HtmlUtils.TITLE_SIZE_1);
    }

    @Then("^I should be notified that the user group has been suspended$")
    public void checkSuspendedNotification() {
        htmlAlertsUtils.assertAndCloseSuccess(SUCCESS_USER_GROUP_SUSPENDED);
    }

    @When("^I re-enable a user group$")
    public void reEnable() {
        htmlUserGroupsTableUtils.clickOnRow(suspendedToEnabledUserGroup);
        htmlUserGroupsTableUtils.clickOnButtonInTable(reEnabledUserGroupButton);
        htmlModalUtils.clickOnButton(acceptButton);
    }

    @Given("^I have re-enabled a user group$")
    public void reEnableToLogIn() {
        htmlHeaderUtils.clickInSubMenu(administrationLink, userGroupsLink);
        htmlUserGroupsTableUtils.clickOnRow(suspendedToEnabledUserGroupWithUserToLogIn);
        htmlUserGroupsTableUtils.clickOnButtonInTable(reEnabledUserGroupButton);
        htmlModalUtils.clickOnButton(acceptButton);
    }

    @When("^I try to log in with a user of the re-enabled group$")
    public void logInReEnableUser() throws TestTimeoutException {
        loginUtils.logOut();
        loginUtils.enterLoginDetailsAndClickLogin(userToTestReEnabledGroup, LoginUtils.PASSWORD);
    }

    @Then("^I should be able to log in$")
    public void checkAbleToLogIn() throws TestTimeoutException {
        loginUtils.waitForWelcomeToRMAS();
    }

    @Then("^I should be notified that the user group has been re-enabled$")
    public void checkReEnabledNotification() {
        htmlAlertsUtils.assertAndCloseSuccess(SUCCESS_USER_GROUP_RE_ENABLED);
    }

    @Then("^I should see a message telling me that my account has been suspended$")
    public void assertAccountLockedMessage(){
        htmlUtils.assertAlertText(SUSPENDED_ACCOUNT_ERROR_MESSAGE);
    }

    @When("^I delete a user group without users$")
    public void deleleWithoutUsers() {
        htmlUserGroupsTableUtils.clickOnRow(userGroupWithoutUsersToDelete);
        htmlUserGroupsTableUtils.clickOnButtonInTable(deleteUserGroupButton);
        htmlModalUtils.clickOnButton(acceptButton);
    }

    @When("^I try to delete a user group with users$")
    public void deleleWithUsers() {
        htmlUserGroupsTableUtils.clickOnRow(userGroupWithUsersToDelete);
        htmlUserGroupsTableUtils.clickOnButtonInTable(deleteUserGroupButton);
        htmlModalUtils.clickOnButton(acceptButton);
    }

    @Then("^I should be notified of user group deletion success$")
    public void checkSuccessfulDeletionNotification() {
        htmlAlertsUtils.assertAndCloseSuccess(SUCCESS_USER_GROUP_DELETE);
    }

    @Then("^I should be notified of user group deletion failure$")
    public void checkFailureDeletionNotification() {
        htmlAlertsUtils.assertAndCloseFailure(FAILURE_USER_GROUP_DELETE);
    }
}
