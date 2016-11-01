Feature: Two factor authentication
As an RMAS administrator
I want 2FA accounts and HE approvers to be required to use it
So that I can be confident that authorised users are who they claim to be.

@wip @RMAS-1098 @RMAS
Scenario: Allow a logged in user to enable 2FA
    Given I have logged in as a non 2FA user
    When I elect to enable 2FA on my account
    And I confirm the 2FA details
    Then I will be presented with a success message
    And I will be told to log out and log in again

@integration @RMAS-1098 @RMAS
Scenario: Enforce 2FA user to log in with 2FA
    When I have entered the username and password for a 2FA user on the login page
    Then I am required to enter my 2FA code

@integration @RMAS-1098 @RMAS
Scenario: Enter wrong 2FA code
    Given I have entered the username and password for a 2FA user on the login page
    When I enter an incorrect 2FA code
    Then I am asked to try again
    And the message says my password or code were incorrect

@integration @RMAS-1098 @RMAS
Scenario: Enter correct 2FA code
    Given I have entered the username and password for a 2FA user on the login page
    When I enter the correct 2FA code
    Then I am shown the RMAS home page

@integration @RMAS-1098 @RMAS
Scenario: HE approver requires 2FA
    When I enter the username and password for an HE approver on the login page
    Then I am required to enter my 2FA code

@integration @RMAS-1098 @RMAS
Scenario: Incorrect password still shows 2FA
    When I enter the username and an incorrect password for a 2FA user on the login page
    And I enter an incorrect 2FA code
    Then I am asked to try again
    And the message says my password or code were incorrect
