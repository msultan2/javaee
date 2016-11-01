Feature: Analysis

  Background: User is logged into BlueTruth Cloud Instation
    Given the test data is deployed and unmodified
    And the user has all user roles
    And the user is logged into the Instation

  Scenario: Route Journey Time Analysis graphs
    Given I want to see a graph like A.11a
    And I am on the bluetruth Analysis page
    When I select the "Route" link
    And I select the "View Duration" link
    And I configure the graph start and end dates:
      | start | 2012-01-01 13:15:05 |
      |  end  | 2012-01-02 13:15:05 |
    Then the page has a graph like A.11a

  Scenario: Route Speed Analysis graphs
    Given I want to see a graph like A.11b
    And I am on the bluetruth Analysis page
    When I select the "Route" link
    And I select the "View Speed" link
    And I configure the graph start and end dates:
      | start | 2012-01-01 13:15:05 |
      |  end  | 2012-01-02 13:15:05 |
    Then the page has a graph like A.11b

  Scenario: Span Journey Time Analysis graphs
    Given I want to see a graph like A.12a
    And I am on the bluetruth Analysis page
    When I select the "Span" link
    And I select the "View Duration" link
    And I configure the graph start and end dates:
      | start | 2012-01-01 13:15:05 |
      |  end  | 2012-01-02 13:15:05 |
    Then the page has a graph like A.12a

  Scenario: Span Speed Analysis graphs
    Given I want to see a graph like A.12b
    And I am on the bluetruth Analysis page
    When I select the "Span" link
    And I select the "View Speed" link
    And I configure the graph start and end dates:
      | start | 2012-01-01 13:15:05 |
      |  end  | 2012-01-02 13:15:05 |
    Then the page has a graph like A.12b

  Scenario: Detector Analysis graphs
    Given I want to see a graph like A.13
    And I am on the bluetruth Analysis page
    When I select the "Detector" link
    And I select the "View Detections" link
    And I configure the graph start and end dates:
      | start | 2012-01-01 13:15:05 |
      |  end  | 2012-01-02 13:15:05 |
    Then the page has a graph like A.13
