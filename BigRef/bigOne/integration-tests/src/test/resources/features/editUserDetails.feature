Feature: As an RMAS User
I should be able to edit my user details
So that my user details are correct

@integration @RMAS @RMAS-1133
Scenario: Edited user details successfully
Given I have logged into RMAS to edit my user details
And I have navigated to the user details page
When I edit my user details
Then I can view that my user details are updated successfully

@integration @RMAS @RMAS-1133
Scenario: Edited name in user details
Given I have logged into RMAS to edit my name
And I have navigated to the user details page
When I edit my name in my user details
Then I can view that my name is updated successfully

@integration @RMAS @RMAS-1133
Scenario: Cancel editing the user details
Given I have logged into RMAS to edit my user details
And I have navigated to the user details page
When I cancel editing my user details
Then I can view that my user details remain the same
