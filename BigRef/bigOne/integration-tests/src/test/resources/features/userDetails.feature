Feature: View User details
As an RMAS user
I want to see my user details
So that I can ensure they are correct and up-to-date

Background:
Given I have logged into the RMAS system

@integration @RMAS @RMAS-766
Scenario: View details
    When I navigate to the user details page
    Then I can see my user details
