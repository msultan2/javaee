Feature: Verify firmware
As an RMAS User
I want the to run the verify command on a device
In order to test device compatibility with RMAS

@integration @RMAS-933 @RMAS
Scenario: Successful verify
    Given I have navigated to the device landing page
    When I initiate the verify command
    Then I should be notified of the successful result


