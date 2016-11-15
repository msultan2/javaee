Feature: Lock unused user account
As the HE
I want the RMAS system to automatically lock an unused RMAS User Account
In order to prevent unauthorised access to the RMAS System
    
@integration @RMAS-1200 @RMAS
Scenario: Login with an unused account and valid credentials
    When I enter details of an account that has not been used for a long time
    Then I should see a message telling me that my account has been locked
    And I should not be able to log in to the RMAS system any more

@integration @RMAS-1200 @RMAS
Scenario: Login with an unused account and invalid credentials
    When I enter invalid login details
    Then I should see the login page with an error message
