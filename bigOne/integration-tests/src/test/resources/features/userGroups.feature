Feature: View, create and modify User Groups
As an RMAS HE Approver
I want to view, create and modify user groups
In order to secure roadside devices and operations to authorised groups of users

Background:
    Given I have logged in as an HE approver

@integration @RMAS-984 @RMAS
Scenario: View existing user groups
    When I navigate to the user groups page
    Then I should see a list of the user groups

@integration @RMAS-984 @RMAS
Scenario: Create new user group
    Given I have navigated to the user groups page
    When I create a new user group specifying all required fields
    Then I should see a list of the user groups including the new group

@integration @RMAS-984 @RMAS
Scenario: Modify existing user group
    Given I have navigated to the user groups page
    When I modify all the fields of a user group
    Then I should see a list of the user groups including the modified group
    And all fields should be updated