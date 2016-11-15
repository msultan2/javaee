Feature: Displaying Device List
As an RMAS user
I want to to be able to select a Roadside device
So that I can see its details

@integration @RMAS-593 @RMAS
Scenario: Displaying Device List
    When I go to the device list page
    Then I see the page displaying the list of devices available to me

@integration @RMAS-593 @RMAS
Scenario: Displaying Device
    When I go to the device list page
    And I select a device
    Then I see the page displaying the device
    