Feature: Individual Device Enrolment
As an RMAS User
I want to enrol a TR2597A compliant roadside device in the RMAS System
So that I can carry out maintenance activities remotely and securely on the device using the RMAS System

Background:
Given I have logged into the RMAS system
And I am on the device enrolment page

@integration @RMAS-1146 @RMAS
Scenario: Device Enrolment where device matches my filter settings
    When I submit the details of the new device
    Then I am informed that the enrolment was successful
    And I see the list of devices including the new device

@integration @RMAS-1146 @RMAS
Scenario: Device Enrolment where new device IP already exists in RMAS
    When I enter the details of a device with an IP address which already exists in RMAS
    Then I am informed that the enrolment was unsuccessful