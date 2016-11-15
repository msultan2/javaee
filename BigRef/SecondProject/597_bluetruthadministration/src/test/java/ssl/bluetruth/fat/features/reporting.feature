# TODO: Set up database with data from A.1.
Feature: Reporting

  Background: User is logged into BlueTruth Cloud Instation
    Given the test data is deployed and unmodified
    And the user has all user roles
    And the user is logged into the Instation

  Scenario: BlueTruth reports journey information
    Given I am on the bluetruth Report page
    When I select the "Journey Times" link
    Then a table has columns:
      | Route * |
      | Journey Time *       |
      | Average Speed * |
      | Strength *   |
      | Status * |
      | Calculated Timestamp * |
    And the table has 1 row:
#     | Route A | 2 Hours 35 Minutes | 83 MPH | 2 | Route status | Last journey complete timestamp |
      | Route 78535 to 178537 | - | - | - | Silent | - |

  Scenario: BlueTruth reports occupancy information
    Given I am on the bluetruth Report page
    When I select the "Traffic Flow" link
    Then a table has columns:
      | Detector Name |
      | Reported Timestamp |
      | Stationary |
      | Very Slow |
      | Slow |
      | Moderate |
      | Free |
      | Queue Status |
      | Select |
    And the table has 1 row:
      | No data available in table |
