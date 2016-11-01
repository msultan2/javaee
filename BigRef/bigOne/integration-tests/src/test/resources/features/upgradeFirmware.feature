Feature: Upgrade firmware
As a RAIT User
I want to upgrade firmware on a device
In order to test device compatibility with RMAS

Background:
Given I have started the RAIT application

@integration @RMAS-579 @RAIT
Scenario: Upgrade firmware
Given I have entered connections parameters
When I initiate upgrade firmware
Then I should be notified of the successful result

