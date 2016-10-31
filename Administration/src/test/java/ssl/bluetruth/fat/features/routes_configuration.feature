Feature: BlueTruth Routes Configuration
   As a registered user of Bluetruth
   I want to be able to login
   In order to add, edit and delete routes

   Background: User is logged into BlueTruth Cloud Instation
      Given I am on the bluetruth website
      And I have logged in as "cucumber" with password "12"
      And I am viewing routes configuration

   @wip
   Scenario: Demonstrate that new routes can be created
      When I create a new route with name "Santhosh Test route" and description "test"
      Then I can see a route in the routes table with name "Santhosh Test route" and description "test"

   @wip
   Scenario: Demonstrate that routes are editable
      When I edit route with name "Santhosh Test route" to "Test route"
      Then I can see a route in the routes table with name "Test route" and description "test"

   @wip
   Scenario: Demonstrate that routes can be deleted
      When I delete route with name "Test route"
      Then I can see route with name "Test route" does not exist in the routes table 

   @wip
   Scenario: Demonstrate that spans can be added to a route
      Given I have a span with name "Span A"
      And I have a route with name "Route A"
      When I add span "Span A" to route "Route A"
      Then I can see span "Span A" added to route "Route A"

   @wip
   Scenario: Demonstrate that spans can be removed from a route
      Given I have a span with name "Span A"
      And I have a route with name "Route A"
      When I remove span "Span A" from route "Route A"
      Then I can see span "Span A" removed from route "Route A"