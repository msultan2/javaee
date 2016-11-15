Feature: Filter devices
As an RMAS User
I want to filter the list of devices
In order to narrow my search results

@integration @RMAS-734 @RMAS
Scenario: filter devices
    Given I go to the device list page
    When I filter the device list
    Then I should only see the filtered devices
