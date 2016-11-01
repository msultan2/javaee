Feature: View pending user registration requests
As an HE approver
I want to view pending user registration requests
So that I can take action(s) to accept/reject requests

@integration @RMAS-826 @RMAS
Scenario: HE Approver user
Given I have logged in as an HE approver
When I view pending user registration requests
Then I should see the list of pending user registration requests assigned to me

@integration @RMAS-826 @RMAS
Scenario: Non HE Approver user
Given I am logged in as user other than an HE Approver user
When I try to navigate to the pending user registration requests page
Then I should not have access to it

@integration @RMAS-826 @RMAS
Scenario: Non HE Approver user using bookmark
Given I am logged in as user other than an HE Approver user
When I go directly to the pending user access requests page
Then I should see the login page with an error message

@integration @RMAS-782 @RMAS
Scenario: View details of pending access requests
Given I have submitted a user registration request
And I have logged in as an HE approver
And I have navigated to the pending access requests page
When I view the request
Then I will be able to see the details of the access request
