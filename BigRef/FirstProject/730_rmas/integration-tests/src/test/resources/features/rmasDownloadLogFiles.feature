Feature: Download log files from device

   As an RMAS User
   I want to download log files
   In order to investigate and diagnose issues with the device

   @integration @RMAS-890 @RMAS
   Scenario: Successful download
      Given I have navigated to the device landing page
      When I select the option to download logs
      And I select a valid date range of log files to download
      And I initiate log file download
      Then I should be provided with a link to access the zipped up log files     
      And I should be notified of the successful result
