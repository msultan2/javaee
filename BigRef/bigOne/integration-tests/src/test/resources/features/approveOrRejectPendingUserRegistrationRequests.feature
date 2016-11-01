Feature: Approve or reject pending user registration requests
As an HE approver
I want to be able to approve or reject an access request
In order to appropriately authorise access to RMAS

@integration @RMAS-1188 @RMAS
Scenario: Approve pending access requests 
    Given I have created a user registration request to approve
    And I have logged in as an HE approver
    And I have navigated to the pending access requests page
    When I select a pending user registration request to approve
    And I assign user group to the user
    And I approve the request
    Then I should see the list of pending user registration requests without the approved request
    And I should be notified that I failed to send email to the user

@integration @RMAS-1188 @RMAS
Scenario: Reject pending access requests 
    Given I have created a user registration request to reject
    And I have logged in as an HE approver
    And I have navigated to the pending access requests page
    And I select a pending user registration request to reject
    When I reject the request adding a reason
    Then I should see the list of pending user registration requests without the rejected request
    And I should be notified that I failed to send email to the user

