Feature: Remove old public keys from device
As an RMAS User
I want to remove old public keys from the device.
@integration @RMAS-974 @RMAS
Scenario: Successful removal old public keys
Given I have navigated to the device landing page
When I initiate the delete old key command
Then I should be notified of the successful result