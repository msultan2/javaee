Feature: Update SSH key on a device
As a RAIT User
I want to update SSH key on a local device
In order to test the device compatibility with RMAS

Background:
Given I have started the RAIT application

@integration @RMAS-474 @RAIT
Scenario: update public SSH key
Given I have entered connections parameters
When I upload a new valid SSH key
Then I should be notified of the successful result