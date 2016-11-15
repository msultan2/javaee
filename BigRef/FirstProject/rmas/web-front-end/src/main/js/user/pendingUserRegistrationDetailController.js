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
 * 
 */
'use strict';

var pendingUserRegistrationDetailModule = angular.module('rmasApp.pending.user.registration.detail.controller', ['rmasApp.user.registration.service','rmasApp.user.userGroup.service']);

pendingUserRegistrationDetailModule.controller('pendingUserRegistrationDetailController', function($routeParams, $scope, $log, $location, userRegistrationService, alertService, paths, rccService, userGroupService) {

    $scope.user = {};
    $scope.config = {};
    $scope.rccs = [];
    $scope.userGroups = [];

    function getUserRegistrationDetails() {
        userRegistrationService.getPendingUserRegistrationRequest($routeParams.id).then(function(response) {
            $scope.user.details = response.data;
        }, function(error) {
            $log.error("Error trying to get the user access requests: ", error);
            alertService.addAlert('error', 'Failed to find pending request details');
        });
    }

    function update(requestStatus) {
        $scope.user.details.requestStatus = requestStatus;
        userRegistrationService.update($scope.user.details).then(function() {
            alertService.addAlert('success', 'Pending request updated successfully', 1);
        }, function(error) {
            $log.error("Error trying to update the pending request: ", error);
            if (error.data === 'The user is already present in the system') {
                alertService.addAlert('warning', error.data, 1);
            } else {
                alertService.addAlert('error', error.data, 1);
            }
        }).finally(function() {
            $location.path(paths.frontEnd.userRegistrationRequests.substr(1));
        });
    }

    function approve() {
        update("APPROVED");
    }

    function reject() {
        update("REJECTED");
    }

    rccService.getRccs().then(function (response) {
        angular.forEach(response.data._embedded.rccs, function(value) {
            $scope.rccs.push(value);
        });
    }, function () {
        alertService.addAlert('error', 'Failed to get RCCs');
    });

    function getUserGroups(){
        userGroupService.getUserGroups().then(function(response){
            $log.debug("response :", response.data);
            angular.forEach(response.data._embedded.userGroups, function(value) {
                $scope.userGroups.push(value);
            });
        });
    }

    $scope.config.userDetailsDirective = {
        parameters: {
            default: 'readOnly',
            projectSponsor: 'hidden',
            tandcAccepted: 'hidden',
            purpose: 'view'
        },
        showPlaceHolders: false,
        rccs: $scope.rccs
    };

    $scope.approve = approve;
    $scope.reject = reject;
    getUserRegistrationDetails();
    getUserGroups();
});
