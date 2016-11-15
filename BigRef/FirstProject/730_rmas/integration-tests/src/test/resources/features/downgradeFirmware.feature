Feature: Downgrade firmware
As a RAIT user
I want to downgrade firmware on a device
In order to test device compatibility with RMAS

Background:
    Given I have started the RAIT application

@integration @RMAS-690 @RAIT
Scenario: Downgrade firmware
    Given I have entered connections parameters
    When I initiate the downgrade command
    Then I should be notified of the successful result

