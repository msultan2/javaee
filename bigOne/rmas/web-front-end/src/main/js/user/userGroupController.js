/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
'use strict';
var userGroupModule = angular.module('rmasApp.user.userGroup.controller', ['rmasApp.user.userGroup.service', 'constants', 'ngInputModified']);
userGroupModule.controller('userGroupController', function ($scope, alertService, userGroupService) {

    var selectedIndex;
    var tableRowExpanded;
    var expandedTableRowIndex;

    function collapseExistingUserGroupEditPanels() {
        $scope.groupDataCollapse = $scope.userGroups.list.map(function () {
            return false;
        });
        tableRowExpanded = false;
        expandedTableRowIndex = "";
    }

    function collapseAllUserGroupEditPanels() {
        collapseExistingUserGroupEditPanels();
        $scope.isNewUserGroupPanelCollapsed = true;
    }

    function getUserGroupList() {
        userGroupService.getUserGroups().then(function (response) {
            $scope.userGroups.list = response.data._embedded.userGroups;
            $scope.userGroups.list.sort(function (grp1, grp2) {
                return grp1.groupName.localeCompare(grp2.groupName);
            });
        }, function () {
            alertService.addAlert('error', 'Failed to get user groups', 0);
        });
    }

    function saveUserGroup() {
        userGroupService.submit($scope.userGroup).then(function () {
            alertService.addAlert('success', 'User group save successful', 0);
            collapseAllUserGroupEditPanels();
            getUserGroupList();
        }, function (error) {
            alertService.addAlert('error', error.data, 0);
            getUserGroupList();
        });
    }

    function getAccountClassifications() {
        userGroupService.getAccountClassifications().then(function (response) {
            $scope.accountClassifications = {
                list: response.data._embedded.accountClassifications.map(function (accountClassification) {
                    return accountClassification.name;
                })
            };
        }, function () {
            alertService.addAlert('error', 'Failed to get account classifications', 0);
        });
    }

    function createNewDeviceFilterList() {
        $scope.deviceFilterList = [];
    }

    function addNewGroupToScope() {
        createNewDeviceFilterList();
        $scope.userGroup = {"deviceFilter": {}};
        $scope.deviceFilterList.push($scope.userGroup.deviceFilter);
    }

    function setupUserGroupForEditing(index) {
        $scope.userGroup = angular.copy($scope.userGroups.list[index]);
        $scope.deviceFilterList = [];
        $scope.deviceFilterList.push($scope.userGroup.deviceFilter);
    }

    $scope.isInvalidUserInput = function (element) {
        return angular.isDefined(element) && element.$invalid && !element.$pristine;
    };

    $scope.expandNewUserGroupPanel = function () {
        if (angular.isDefined($scope.userGroup)) {
            $scope.cancel();
        }
        collapseExistingUserGroupEditPanels();
        addNewGroupToScope();
        $scope.isNewUserGroupPanelCollapsed = false;
        selectedIndex = -1;
    };

    $scope.save = saveUserGroup;

    $scope.cancel = function () {
        collapseAllUserGroupEditPanels();
        $scope.userGroup = {};
        $scope.userGroupDetailForm.$setPristine();
    };

    $scope.selectTableRow = function (index) {

        $scope.isNewUserGroupPanelCollapsed = true;

        if (tableRowExpanded === true) {
            if (expandedTableRowIndex === index) {
                $scope.cancel();
            } else {
                $scope.groupDataCollapse[expandedTableRowIndex] = false;
                $scope.cancel();
                expandedTableRowIndex = index;
                tableRowExpanded = true;
                $scope.groupDataCollapse[expandedTableRowIndex] = true;
                setupUserGroupForEditing(index);
            }
        } else {
            tableRowExpanded = true;
            expandedTableRowIndex = index;
            $scope.groupDataCollapse[index] = true;
            setupUserGroupForEditing(index);
        }
        selectedIndex = index;
    };

    $scope.userGroup = {};
    $scope.userGroups = {list: []};
    getAccountClassifications();
    getUserGroupList();
    collapseAllUserGroupEditPanels();
});


