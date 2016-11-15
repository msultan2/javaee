Feature: Verify firmware
As a RAIT user
I want the to run the verify command on a device
In order to test device compatibility with RMAS

Background:
Given I have started the RAIT application

@integration @RMAS-595 @RAIT
Scenario: Successful verify
    Given I have entered connections parameters
    When I initiate the verify command
    Then I should be notified of the successful result


