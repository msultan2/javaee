Feature: BlueTruth Detectors Configuration
   As a rgistered BlueTruth user
   I want to be able to login
   In order to add, edit and delete detectors

   Background: User is logged into BlueTruth Cloud Instation
      Given I am on the bluetruth website
      And I have logged in as "cucumber" with password "12"
      And I am viewing detectors configuration
   
   @wip
   Scenario: Demonstrate that new detectors can be created
      When I create a new detector with name "Detector 1", Detector ID "12345", Latitude "0", Longitude "0", Mode "MODE 0 - Idle", Carriageway "North" and logical group "Test Group"
      Then I can see a detector in the detectors table with name "Detector 1"

   @wip
   Scenario: Demonstrate that detector configurations are editable
      When I edit detector with name "Detector 1" to "Detector 12"
      Then I can see a detector in the detectors table with name "Detector 12"

   @wip
   Scenario: Demonstrate that detector configuration settings are configurable
      Given I have a detector with name "Detector 12"
      When I edit the detector configuration "Alert Target 1" to "SSL" for detector with name "Detector 12"
      Then I can see the detector with name "Detector 12" the configuration "Alert Target 1" set to "SSL"

   @wip
   Scenario: Demonstrate that detectors can be deleted
      When I delete detector with name "Detector 12"
      Then I can see detector with name "Detector 12" does not exist in the detectors table   


      