Feature: Download static data
As a RAIT User
I want to download the static data files
In order to test the device compatibility with RMAS

@integration @RMAS-325 @RAIT
Scenario: download static data
    Given I have started the RAIT application
    Given I have entered connections parameters
    When I initiate the static data download
    Then the location of the downloaded file should be shown in results pop up
