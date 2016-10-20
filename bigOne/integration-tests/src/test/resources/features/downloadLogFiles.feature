Feature: Download log files from device
As a RAIT User
I want to download log files
In order to test device compatibility with RMAS

Background:
    Given I have started the RAIT application

@integration @RMAS-477 @RAIT
Scenario: Valid input fields
    Given I have entered connections parameters
    When I select a valid date range of log files to download
    And I initiate log file download
    Then I should be able to view the downloaded log files

@integration @RMAS-477 @RAIT
Scenario: Invalid input fields
    Given I have entered connections parameters
    When I select a invalid date range of log files to download
    And I initiate log file download
    Then I should not be able to view the downloaded log files

@integration @RMAS-477 @RAIT
Scenario: Partial valid input fields
    Given I have entered connections parameters
    When I select a partial valid date range of log files to download
    And I initiate log file download
    Then I should be able to view one or more the downloaded log files
