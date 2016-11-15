Feature: Display last logged in date and time
As an RMAS User
I want to see the date and time of my last successful login
In order to know when I last accessed the RMAS System

@integration @RMAS-1205 @RMAS
Scenario: View last login date and time
    When I log into the RMAS system
    Then I should see the date and time of my last login

@integration @RMAS-1205 @RMAS
Scenario: Last login date and time when logging in for the first time
    When I log into the RMAS system as a new user
    Then I should not see a last login message