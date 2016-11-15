Feature: Download static data
As an RMAS User
I want to download the static data files
In order to keep RMAS up to date with any device changes

@integration @RMAS-605 @1045 @RMAS
Scenario: download static data
    Given I have navigated to the device landing page
    When I refresh the device details
    Then I will receive notification that the device details have been updated
    And I will be able to see the updated device details
