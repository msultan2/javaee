Feature: SSH Keys
As an RMAS User
I want to manage RMAS SSH keys
In order to maintain the security of the RMAS link to roadside devices

@integration @RMAS-904 @RMAS @wip
Scenario: Able to connect to a device after generating a new key pair 
Given I have Generated a new key pair
When I refresh the device details
Then I will receive notification that the device details have been updated