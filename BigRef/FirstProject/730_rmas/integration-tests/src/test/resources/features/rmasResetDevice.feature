Feature: Reset Device
As an RMAS User
I want to reset a device
Because sometimes turning it off and on again works

@integration @RMAS-732 @RMAS
Scenario: Successful device reset
    Given I have navigated to the device landing page
    When I select the option to reset device
    And I have entered a correct PEW number
    And I have confirmed that PEW number and my user details are correct
    And I initiate the reset command
    Then I should be notified of the successful result

