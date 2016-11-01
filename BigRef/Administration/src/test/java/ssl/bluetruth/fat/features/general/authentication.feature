Feature: General Functionality - Authentication

  Background: The user is on the test bluetruth website
    Given I am on the bluetruth test website

  # 4.1.1
  Scenario: The user can log into the BlueTruth Cloud Instation
    When I have logged in as "cucumber" with password "12"
    Then the page title is "Cloud Instation - Home"

  # 4.1.2
  Scenario: The user can log out of the BlueTruth Cloud Instation
    Given I have logged in as "cucumber" with password "12"
    When I select the "sign out" link
    Then the page title is "Cloud Instation - Sign In"

  # 4.1.3
  Scenario: The user can recover from forgotten password
    Given I have logged in as "cucumber" with password "12"

#  Scenario: Logging in with the wrong password restricts access
#    Given I am on the bluetruth website
#    When I have logged in as "cucumber" with password "wrong"
#    Then the page title is "Cloud Instation - Sign In"

 