Feature: Delete old key
As a RAIT user
I want to run the delete old key command on a device
In order to test device compatibility with RMAS

@integration @RMAS-698 @RAIT
Scenario: Successful delete old key command
    Given I have started the RAIT application
    And I have entered connections parameters
    When I initiate the delete old key command
    Then I should be notified of the successful result
