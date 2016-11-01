Feature: Reporting
  As a registered user of BlueTruth
  I want to view journey time information
  So that I can understand how it changes through the day

  Scenario: BlueTruth Cloud Instation reports journey information
    Given I am on http://www.bluetruth.co.uk
    And I am logged in
    Then there is a link to Report
    When I select the Report link
    Then there is a link to Journey Times
    When I select the Journey Times link
    Then there is a Journey Time table
    And the table has at least 10 rows

  Scenario: BlueTruth Cloud Instation reports occupancy
    Given I am on http://www.bluetruth.co.uk
    And I am logged in
    Then there is a link to Report
    When I select the Report link
    Then there is a link to Traffic Flow
    When I select the Traffic Flow link
    Then the table headers are:
         |Detector Name|
         |Reported Timestamp|
         |Stationary|
         |Very Slow|
         |Slow|
         |Moderate|
         |Free|
         |Queue Status|
         |Select|
         