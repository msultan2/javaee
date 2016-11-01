Feature: BlueTruth Map

  Background: User is logged into BlueTruth Cloud Instation
    Given I am on the bluetruth test website
    And I have logged in as "cucumber" with password "12"
    And I am on the bluetruth Map page

  # 4.3.1  
  Scenario Outline: Panning the map
    Given the map is centred on Preston
    When I press the <arrow> key
    Then the map is centred <relative-to> Preston
    Examples:
      | arrow | relative-to |
      |  up   |   north of  |
      | down  |   south of  |
      | left  |   west of   |
      | right |   east of   |

  # 4.3.2
  Scenario Outline:  Zooming the map
    Given the map includes:
      | Leeds |
      | York  |
    Then the map excludes:
      |    Bradford     |
      | Market Weighton |
    When I press the <zoom> key
    Then the map will <inclusion> <place>
    Examples:
      | zoom | inclusion |     place       |
      |  +   |  exclude  |     Leeds       |
      |  +   |  exclude  |     York        |
      |  -   |  include  |   Bradford      |
      |  -   |  include  | Market Weighton |

  # 4.3.2
  Scenario: Detector information
    Given the "Detector A" detector is visible
    When I select the "Detector A" detector
    Then a popup for "Detector A" appears
    And the detector popup contains fields:
      | ID:          | A                 |
      | location:    | default           |
      | carriageway: | North             |
      | latitude:    | 53.73571574532637 |
      | longitude:   | -2.63671875       |
      | mode:        | MODE 0 - Idle     |
      | status:      | Silent            |
      | groups:      | [Test Group]      |
    And the detector popup contains links to:
      | Additional Information |
      | Configuration          |
      | Diagnostic             |
      | Analysis               |

  # 4.3.2
  Scenario: Span information
    Given the "Span A" span is visible
    When I select the "Span A" span
    Then a popup for "Span A" appears
    And the span popup contains fields:
      | distance:       | 343151m      |
      | start detector: | A            |
      | end detector:   | B            |
      | status:         | Silent       |
      | average speed:  | -            |
      | groups:         | [Test Group] |
      | routes:         | -            |
    And the span popup contains links to:
      | Additional Information |
      | Duration Analysis      |
      | Speed Analysis         |

  # 4.3.4
  Scenario: Change detector location
    Given the "Detector A" detector is visible
    And I select the "Detector A" detector

    When I select the "Edit Detector Location" link

    Then the dialog "Edit detector location" is visible

    When I click the map north-east of "Detector A"
    And I select "Confirm Detector Location" on the dialog
    
    Then the detector latitude > 53.73571574532637
    And the detector longitude > -2.63671875
