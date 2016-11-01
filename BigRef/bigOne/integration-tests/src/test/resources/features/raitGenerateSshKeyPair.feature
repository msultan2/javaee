Feature: Generation of SSH key pair

   As a RAIT User
   I want to generate public/private SSH key pair
   In order to test the device compatibility with RMAS

   Background:
      Given I have started the RAIT application

   @integration @RMAS-791 @RAIT
   Scenario: Generate SSH key pair
        When I initiate SSH key pair generation
        Then the location of the generated SSH keys should be shown in the results message
        And the new SSH key pair should be present in this location
