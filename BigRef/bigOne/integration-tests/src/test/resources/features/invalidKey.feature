Feature: Invalid SSH private key
As a RAIT User
I want to try and connect to a device using an invalid SSH private key
In order to check that the device handles the error scenario appropriately

Background:
    Given I have started the RAIT application

@integration @RMAS-400 @RAIT
Scenario: Connect using invalid SSH private key
    When I select an invalid SSH private key file
    And I initiate a command on the device
    Then I should be notified of failure