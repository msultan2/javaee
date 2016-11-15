Feature: Account classification creation
As an HE approver
I want to create a new account classification
In order to secure roadside devices and operations to authorised users

Background:
    Given I have logged in as an HE approver

@integration @RMAS-1215 @RMAS
Scenario: View account classification list
    When I navigate to the account classifications page
    Then I should see a list of account classifications

@integration @RMAS-1215 @RMAS
Scenario: Create a new account classification
    Given I have navigated to the account classifications page
    When I create a new account classification
    Then I should be notified that the account classification has been created
    And I should see a list of account classifications including the new account classification

@integration @RMAS-1215 @RMAS
Scenario: Create an account classification which already exists
    Given I have navigated to the account classifications page
    When I create an account classification which already exists
    Then I should be notified of failure to create the account classification
