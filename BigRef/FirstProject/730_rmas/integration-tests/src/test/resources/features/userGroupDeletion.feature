Feature: User group deletion
As an HE approver
I want to delete an existing user group
In order to clean up unused groups

Background:
    Given I have logged in as an HE approver
    And I have navigated to the user groups page

@integration @RMAS @RMAS-1214
Scenario: Delete user group without users
    When I delete a user group without users
    Then I should be notified of user group deletion success

@integration @RMAS @RMAS-1214
Scenario: Delete user group with users
    When I try to delete a user group with users
    Then I should be notified of user group deletion failure
