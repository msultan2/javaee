Feature: General Functionality
  As a registered user of BlueTruth
  I want to be able to login
  In order to view reports

  Scenario: The user can log into the BlueTruth Cloud Instation
    Given I am on http://www.bluetruth.co.uk
    When I enter my username and password
    Then the login is successful
    And the homepage is displayed

  Scenario: The user can log out of the BlueTruth Cloud Instation
    Given I am on http://www.bluetruth.co.uk
    And I am logged in
    When I sign out
    Then the login page is displayed
