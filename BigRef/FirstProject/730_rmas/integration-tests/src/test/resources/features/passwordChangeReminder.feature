Feature: Automatically prompted to change the password periodically 
As an RMAS User
I want to automatically be prompted to change my password periodically 
In order to follow HE Password Policy

@integration @RMAS-1156 @RMAS
Scenario: Automatically prompted to change the expired password
    When I log in into the RMAS system with a user with an expired password
    Then I should be asked to change my password
