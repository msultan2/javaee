Feature: User access request
As a prospective RMAS user
I want to apply for an RMAS account
In order to use the RMAS service

Background:
    Given I am on the user registration page

@integration @RMAS @RMAS-421 @RMAS-547
Scenario: Submit user registration form with valid entries
    When I submit a completed user registration form
    Then I will be returned to the login page
    And I will be notified that my registration form has been successfully submitted

@integration @RMAS @RMAS-581
Scenario: User registration page contains T&C
    When I click on T&C link
    Then I will get a OK response 

@integration @RMAS @RMAS-581
Scenario: Invalid user registration rejected by core
    When I submit an invalid user registration request
    Then I get a bad request response
