Feature: General Functionality - Table Contents

  Background: The user is logged into the diagnostics page
    Given the test data is deployed and unmodified
    And the user has all user roles
    And the user is logged into the Instation
    And I am on the bluetruth Diagnostic page

  # 4.1.5
  Scenario: The user can download table contents as a CSV file
    When I select the "Detectors" link
    Then the page has a link to Download CSV
    When I download the file at link "Download CSV"
    Then the first line of the file is:
      """
      Detector,ID,Location,Longitude,Latitude,MODE,Carriageway,Logical Groups,Status
      """

  # 4.1.6
  Scenario Outline: The user can show/hide colums in tables
    When I select the "Detectors" link
    Then a table has columns:
      | Detector * |
      | ID *       |
      | Location * |
      | Status *   |
      | View Data  |
    And the page has a button to Show/Hide Columns
    When I select the "Show/Hide Columns" link
    Then the dialog "Detector Show/Hide Columns" is visible
    And the check box <Checkbox> exists
    When I select checkbox <Checkbox>
    Then the table column <Column> is visible
    When I uncheck checkbox <Checkbox>
    Then the table column <Column> is not visible
    Examples:
      |      Column      |     Checkbox     |
      | Detector *       | Detector *       |
      | ID *             | ID *             | 
      | Location *       | Location *       |
      | Latitude *       | Latitude *       |
      | Longitude *      | Longitude *      |
      | MODE *           | MODE *           |
      | Carriageway *    | Carriageway *    |
      | Logical Groups * | Logical Groups * |
      | Status *         | Status *         |
      | View Data        | View Data        |

  # 4.1.7
  Scenario: The user can filter table contents
    When I select the "Detectors" link
    When I select the "Filter" link
    Then the dialog "Detector Filter" is visible
    When I enter "Detector*" into the "Detector *:" field
    Then the table has 3 rows
    And column "Detector *" is:
      |Detector A|
      |Detector B|
      |Detector C|
    When I enter "Detector A" into the "Detector" field
    Then the table has 1 row
    And column "Detector *" is:
      |Detector A|

 