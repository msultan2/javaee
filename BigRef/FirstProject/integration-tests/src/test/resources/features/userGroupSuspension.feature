Feature: User group suspension
As an HE approver
I want to suspend an existing user group
In order to prevent unauthorised access to the RMAS System

@integration @RMAS-1210 @RMAS
Scenario: Suspend user group
    Given I have logged in as an HE approver
    And I have navigated to the user groups page
    When I suspend a user group
    Then I should be notified that the user group has been suspended

@integration @RMAS-1210 @RMAS
Scenario: Re-enable a previously suspended user group
    Given I have logged in as an HE approver
    And I have navigated to the user groups page
    When I re-enable a user group
    Then I should be notified that the user group has been re-enabled

@integration @RMAS-1210 @RMAS
Scenario: Login as a user of suspended group
    Given I have logged in as an HE approver
    And I have suspended a user group
    When I try to log in with a user of the suspended group
    Then I should not be able to log in
    And I should see a message telling me that my account has been suspended

@integration @RMAS-1210 @RMAS
Scenario: Login as a user of a previously suspended group that has been re-enabled
    Given I have logged in as an HE approver
    And I have re-enabled a user group
    When I try to log in with a user of the re-enabled group
    Then I should be able to log in
    