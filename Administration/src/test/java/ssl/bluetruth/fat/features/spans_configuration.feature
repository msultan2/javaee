Feature: BlueTruth Spans Configuration
   As a registered user of Bluetruth
   I want to be able to login
   In order to add, edit and delete spans

   Background: User is logged into BlueTruth Cloud Instation
      Given I am on the bluetruth website
      And I have logged in as "cucumber" with password "12"
      And I am viewing spans configuration

   @wip
   Scenario: Demonstrate that new spans can be created
      When I create a new span with span name "Span 1", Start Detector "A (Detector A)", End Detector "C (Detector C)", Stationary "10", very slow "20", slow "30", moderate "40"
      Then I observe the new span with span name "Span 1" is added to the spans table

   @wip
   Scenario: Demonstrate that span configurations are editable
      When I edit span with name "Span 1" to "Span 12"
      Then I observe that the span name is updated to "Span 12"

   @wip
   Scenario: Demonstrate that spans can be deleted
      When I delete span with name "Span 12"
      Then I can see span with name "Span 12" does not exist in the spans table 
