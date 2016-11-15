Feature: BlueTruth Configuration
   As a registered user of Bluetruth
   I want to be able to login
   In order to add, edit and delete routes, spans and detectors

   Background: User is logged into BlueTruth Cloud Instation
      Given I am on the bluetruth test website
      And I have logged in as "cucumber" with password "12"

   @myTests
   @broken
   Scenario: Demonstrate that new routes can be created
      Given I am viewing routes configuration
      When I create a new route with name "Test route" and description "test"
      Then I can see a route in the routes table with name "Test route" and description "test"


   