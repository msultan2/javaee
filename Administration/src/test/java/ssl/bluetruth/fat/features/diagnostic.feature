Feature: BlueTruth Diagnostic
   As a registered BlueTruth user 
   I want to be able to login
   In order to view diagnostic routes, spans and detectors

   Background: User is logged into BlueTruth Cloud Instation
      Given I am on the bluetruth website
      And I have logged in as "cucumber" with password "12"
      
@wip2
   Scenario: Demonstrate that the routes diagnostic information is shown
      Given I am on the bluetruth Diagnostic page
      When I select the "Routes" link
      Then a table has columns:
         | Route Name |
         | Description  |
         | Status *|
      And the table has 1 row:
#     | Route A | Route A Description | Silent |
      | Route 78535 to 178537 | Route around SSL premises | Silent |

@wip2
   Scenario: Demonstrate that the spans diagnostic information is shown
      Given I am on the bluetruth Diagnostic page
      When I select the "Spans" link
      Then a table has columns:
         | Span Name * |
         | Start * |
         | End * |
         | Status * |
#       And the table has 1 row:
#     | Span A | Detector A | Detector B | Silent |

@wip2
   Scenario: Demonstrate that the detectors diagnostic information is shown
      Given I am on the bluetruth Diagnostic page
      When I select the "Detectors" link
      Then a table has columns: 
         | Detector * |
         | ID * |
         | Location * |
         | Status * |
         | View Data |
#       And the table has 10 rows:
#             |178534                             | 178534    |  default | Silent | view data|
#             |178535                             | 178535    |  default | Silent | view data|
#             |178537                             | 178537    |  default | Silent | view data|
#             |Detector A                         | A         |  default | Silent | view data|
#             |Detector B                         | B         |  default | Silent | view data|
#             |Detector C                         | C         |  default | Silent | view data|
#             |LowPowerBT                         | 7567      |  default | Silent | view data|
#             |Radek's Detector                   | 1234      |  default | Silent | view data|
#             |Raspberry PI Radek's test detector | 1236      |  default | Silent | view data|
#             |Test Detector runing WindRiver     | 1235      |  default | Silent | view data|

# @wip2
#    Scenario: Demonstrate that the detector diagnostic information is shown
#       Given I am on the bluetruth Diagnostic page
#       When I select the "Detectors" link
#       And I select the "view data" button for Detector A
#       Then I should see the following detector information:
#          | Last Detection        |
#          | Last Occupancy Report |
#       And I should see the following detector tables: 
#          | Detector Messages |
#          | Detector Logs     |   

