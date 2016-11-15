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
var accountClassificationModule = angular.module('rmasApp.user.accountClassification.controller', ['rmasApp.user.accountClassification.service','rmasApp.user.permission.service','constants', 'ngInputModified']);
accountClassificationModule.controller('accountClassificationController', function ($scope, $log, alertService, accountClassificationService, permissionService) {

    var tableRowExpanded;
    var expandedTableRowIndex;

    function collapseExistingAccountClassificationEditPanels() {
        $scope.accountClassificationDataCollapse = $scope.accountClassifications.list.map(function () {
            return false;
        });
        tableRowExpanded = false;
        expandedTableRowIndex = "";
    }

    function collapseAllAccountClassificationEditPanels() {
        collapseExistingAccountClassificationEditPanels();
        $scope.isNewAccountClassificationPanelCollapsed = true;
    }

    function getAccountClassifications() {
        accountClassificationService.getAccountClassifications().then(function (response) {
            $scope.accountClassifications = {
                list: response.data._embedded.accountClassifications.map(function (accountClassification) {
                    return accountClassification;
                })
            };
        }, function () {
            alertService.addAlert('error', 'Failed to get account classifications', 0);
        });
    }

    function getPermissions() {
        permissionService.getPermissions().then(function (response) {
            $scope.permissions = {
                list: response.data._embedded.permissions.map(function (permission) {
                    return permission;
                })
            };
        }, function () {
            alertService.addAlert('error', 'Failed to get permissions', 0);
        });
    }

    function addNewAccountClassificationToScope() {
        $scope.notAllowedModified = false;
        $scope.accountClassification = {
            "permissionSelected": {
                list: $scope.permissions.list.map(function (permission) {
                    permission.value = false;
                    return permission;
                })
            }
        };
    }

    function columnSizeCalculation(){
        if($scope.permissions.list.length > 6){
            return 2;
        }
        return Math.round(12/($scope.permissions.list.length));
    }

    $scope.save = function () {
        $scope.accountClassification.permissions =
            $scope.accountClassification.permissionSelected.list.filter(function (permission) {
                return permission.value===true;
            }).map(function (permission) {
                return permission.name;
            });
        accountClassificationService.submit($scope.accountClassification).then(function () {            
            alertService.addAlert('success', 'Account classification save successful', 0);
            collapseAllAccountClassificationEditPanels();
            getAccountClassifications();
        }, function (error) {
            if (error.data[0] === "Duplicate value") {
                alertService.addAlert('warning', "Duplicate value", 0);
            } else {
                alertService.addAlert('error', error.data, 0);
            }            
            getAccountClassifications();
        });
    };

    function setupUAccountClassificationForEditing(index) {
        $scope.accountClassification = angular.copy($scope.accountClassifications.list[index]);
        $scope.accountClassification.permissionSelected = {
            list: $scope.permissions.list.map(function (permission) {
                if($scope.accountClassification.permissions.indexOf(permission.name) !== -1){
                    permission.value = true;
                }else{
                    permission.value = false;
                }
                return permission;
            })
        };
        $scope.accountClassification.colSize = columnSizeCalculation();
        $scope.checkFor2fa();
    }

    $scope.isInvalidUserInput = function (element) {
        return angular.isDefined(element) && element.$invalid && !element.$pristine;
    };

    $scope.expandNewAccountClassificationPanel = function () {
        if (angular.isDefined($scope.accountClassification)) {
            $scope.cancel();
        }
        collapseExistingAccountClassificationEditPanels();
        addNewAccountClassificationToScope();
        $scope.accountClassification.colSize = columnSizeCalculation();
        $scope.isNewAccountClassificationPanelCollapsed = false;
    };

    $scope.cancel = function () {
        collapseAllAccountClassificationEditPanels();
        $scope.accountClassification = {};
        $scope.accountClassificationDetailForm.$setPristine();
    };

    $scope.selectTableRow = function (index) {
        $scope.notAllowedModified = true;
        $scope.isNewAccountClassificationPanelCollapsed = true;
        $log.debug("$scope.accountClassification.permissionSelected ", $scope.accountClassification.permissionSelected);

        if (tableRowExpanded === true) {
            if (expandedTableRowIndex === index) {
                $scope.cancel();
            } else {
                $scope.accountClassificationDataCollapse[expandedTableRowIndex] = false;
                $scope.cancel();
                expandedTableRowIndex = index;
                tableRowExpanded = true;
                $scope.accountClassificationDataCollapse[expandedTableRowIndex] = true;
                setupUAccountClassificationForEditing(index);
            }
        } else {
            tableRowExpanded = true;
            expandedTableRowIndex = index;
            $scope.accountClassificationDataCollapse[index] = true;
            setupUAccountClassificationForEditing(index);
        }
    };
    
    $scope.checkFor2fa = function () {
        $scope.accountClassification.permissionSelected.list.map(function(permission){
            if("Roadside device firmware change" === permission.name){
                $scope.accountClassification.display2fa = permission.value;
            }
        });
       
    };

    $scope.accountClassification = {
        display2fa: false
    };
    $scope.permissionSelected = [];
    $scope.accountClassifications = {list: []};
    getAccountClassifications();
    getPermissions();
    collapseAllAccountClassificationEditPanels();
});


