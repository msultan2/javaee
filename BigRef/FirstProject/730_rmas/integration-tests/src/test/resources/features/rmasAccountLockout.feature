Feature: Account lockout on multiple incorrect login

As the HE
I want an RMAS user account to be locked if incorrect password is entered more than five times
In order to prevent unauthorised access to the RMAS System

@integration @RMAS-1194 @RMAS
Scenario: Invalid password entered a few times
    Given I enter an invalid password 5 times
    When I enter a valid password
    Then I should be able to log in the RMAS system

@integration @RMAS-1194 @RMAS
Scenario: Invalid password entered multiple times
    Given I enter an invalid password 6 times
    When I enter a valid password
    Then I should see a message telling me that my account has been locked
    And I should not be able to log in to the RMAS system

@integration @RMAS-1194 @RMAS
Scenario: Not revealing account lockout information to unauthenticated users
    When I enter an invalid password 6 times
    Then I should see a message telling me that login details are incorrect
