# 4.1.4
Feature: General Functionality - Navigation

  Background: The user is logged into the test bluetruth website
    Given the test data is deployed and unmodified
    And the user has all user roles
    And the user is logged into the Instation

  Scenario Outline: The user can navigate the Home page
    Given I am on the bluetruth Home page
    Then the page title is "Cloud Instation - Home"
    And the page has a link to <Page>
    Examples:
    | Page           |
    | Report         |
    | Map            |
    | Analysis       |
    | Diagnostic     |
    | Configuration  |
    | Administration |
    | Wiki           |
    | Support        |
        

  Scenario Outline: The user can navigate reports
    Given I am on the bluetruth Report page
    Then the page title is "Cloud Instation - Report"
    And the page has a link to <Page>
    When I select the "<Page>" link
    Then the page title is "<Title>"
    Examples:
    | Page          | Title                                      |
    | Journey Times | Cloud Instation - Live Journey Time Report |
    | Traffic Flow  | Cloud Instation - Live Traffic Flow Report |

  Scenario: The user can navigate the map
    Given I am on the bluetruth Map page
    Then the page title is "Cloud Instation - Map"

  Scenario Outline: The user can navigate analysis
    Given I am on the bluetruth Analysis page
    Then the page title is "Cloud Instation - Analysis"
    And the page has a link to <Analysis-Link>
    When I select the "<Analysis-Link>" link
    Then the page title is "<Title>"
    Examples:
    | Analysis-Link | Title                               |
    | Route         | Cloud Instation - Route Analysis    |
    | Span          | Cloud Instation - Span Analysis     |
    | Detector      | Cloud Instation - Detector Analysis |

# TODO check each route, span, and detector have analysis links

  Scenario Outline: The user can further navigate analysis
    Given I am on the bluetruth Analysis page
    And I select the "<Analysis>" link
    Then the page has a link to <Analysis-Link>
    When I select the "<Analysis-Link>" link
    Then the page title matches "<Title>"
    Examples:
    | Analysis | Analysis-Link   | Title                                                  |
    | Route    | View Duration   | Cloud Instation - Analysis / Routes / .* Duration      |
    | Route    | View Speed      | Analysis / Routes / .* Speed                           |
    | Span     | View Duration   | Cloud Instation - Analysis / Spans / .* Duration       |
    | Span     | View Speed      | Cloud Instation - Analysis / Spans / .* Speed          |
#   | Detector | View Detections | Cloud Instation - Analysis / Detectors / .* Detections |

  Scenario Outline: The user can navigate diagnostics
    Given I am on the bluetruth Diagnostic page
    Then the page title is "Cloud Instation - Diagnostic"
    And the page has a link to <Diagnostic-Link>
    When I select the "<Diagnostic-Link>" link
    Then the page title is "<Title>"
    Examples:
    | Diagnostic-Link | Title                                  |
    | Routes          | Cloud Instation - Diagnostic Routes    |
    | Spans           | Cloud Instation - Span Diagnostic      |
    | Detectors       | Cloud Instation - Detector Diagnostic  |

  Scenario: The user can further navigate diagnostics
    Given I am on the bluetruth Diagnostic page
    And I select the "Detectors" link
    Then the page has a link to view data
    When I select the "view data" link
    Then the page title is "Cloud Instation - Detector Diagnostic"

  Scenario Outline: The user can navigate configuration
    Given I am on the bluetruth Configuration page
    Then the page title is "Cloud Instation - Configuration"
    And the page has a link to <Component>
    When I select the "<Component>" link
    Then the page title is "<Title>"
    Examples:
    | Component          | Title                       |
    | Routes             | Cloud Instation - Routes    |
    | Spans              | Cloud Instation - Spans     |
    | Detectors          | Cloud Instation - Detectors |
    | Detectors          | Cloud Instation - Detectors |

  Scenario Outline: The user can further navigate configuration
    Given I am on the bluetruth Configuration page
    And I select the "<Component>" link
    Then the page has a link to <Component-Configuration>
    When I select the "<Component-Configuration>" link
    Then the page title matches "<Config-Title>"
    Examples:
    | Component          | Component-Configuration | Config-Title                             |
    | Routes             | view                    | Cloud Instation - Route - .*             |
    | Spans              | view data               | Cloud Instation - Span                   |
    | Detectors          | configuration           | Cloud Instation - Detector Configuration |
    | Detectors          | view data               | Cloud Instation - Detector               |

  Scenario Outline: The user can navigate administration
    Given I am on the bluetruth Administration page
    Then the page title is "Cloud Instation - Administration"
    And the page has a link to <Administrate>
    When I select the "<Administrate>" link
    Then the page title is "<Title>"
    Examples:
    | Administrate       | Title                                  |
    | Users              | Cloud Instation - Instation Users      |
    | User Roles         | Cloud Instation - Instation User Roles |
    | Logical Groups     | Cloud Instation - Logical Groups       |
    | Brands             | Cloud Instation - Brands               |
    | Broadcast Messages | Cloud Instation - Broadcast Messages   |
    | Support            | Cloud Instation - Support              |
    | Audit Trail        | Audit Trail                            |

  Scenario Outline: The user can further navigate administration
    Given I am on the bluetruth Administration page
    And I select the "<Administrate>" link
    Then the page has a link to <Component-Administration>
    When I select the "<Component-Administration>" link
    Then the page title matches "<Title>"
    Examples:
    | Administrate       | Component-Administration | Title                                 |
    | Users              | view                     | Cloud Instation - Instation User .*   |
    | Logical Groups     | view                     | Cloud Instation - Logical Group       |
    | Brands             | configuration            | Cloud Instation - Brand Configuration |

# TODO There is no variation between page titles
# We need to use text from page e.g:
#     User Administration / Logical Groups / haip-datex2 / Users
  Scenario Outline: The user can further navigate logical groups administration
    Given I am on the bluetruth Administration page
    And I select the "Logical Groups" link
    And I select the "view" link
    Then the page has a link to <Administrate>
    When I select the "<Administrate>" link
    Then the page title matches "<Title>"
    Examples:
    | Administrate | Title                           |
    | Users        | Cloud Instation - Logical Group |
    | Routes       | Cloud Instation - Logical Group |
    | Spans        | Cloud Instation - Logical Group |
    | Detectors    | Cloud Instation - Logical Group |

  Scenario Outline: The user can navigate the wiki
    Given I am on the bluetruth Wiki page
    Then the page title is "Wiki: Bluetooth Detector Cloud Instation Wiki"
    And the page has a link to <Page>
    Examples:
    | Page           |
    | Report         |
    | Map            |
    | Analysis       |
    | Diagnostic     |
    | Configuration  |
    | Administration |
    | Wiki           |
    | Support        |

  Scenario: The user can navigate support
    Given I am on the bluetruth Support page
    Then the page title is "Cloud Instation - Support"
