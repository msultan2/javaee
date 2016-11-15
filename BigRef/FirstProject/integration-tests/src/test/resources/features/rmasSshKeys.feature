Feature: SSH Keys
As an RMAS User
I want to manage RMAS SSH keys
In order to maintain the security of the RMAS link to roadside devices

@integration @RMAS-770 @RMAS
Scenario: Generate key pairs for non-helpdesk user
    Given I have logged into the RMAS system as a non helpdesk user
    When I navigate to the SSH keys page
    Then I should not see the option to generate a new key pair

@integration @RMAS-770 @RMAS
Scenario: View current public key for any user
    Given I have logged into the RMAS system
    When I navigate to the SSH keys page
    Then I should see the current public key with date of key creation

@integration @RMAS-770 @RMAS @wip
Scenario: Generate new key pairs for helpdesk user
    Given I have logged into the RMAS system as a helpdesk user
    And I have navigated to the SSH keys page
    When I generate a new key pair
    Then I should be notified of success
    And I should see the new public key on the SSH keys page with the new creation date
