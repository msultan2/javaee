Feature: Reset Device
As a RAIT user
I want to reset a device
So that I can restart the device

Background:
    Given I have started the RAIT application

@integration @RMAS-476 @RAIT
Scenario: Successful device reset
    Given I have entered connections parameters
    When I initiate the reset command
    Then I should be notified of the successful result

