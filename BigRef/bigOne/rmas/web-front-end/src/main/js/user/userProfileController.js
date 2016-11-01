/*
 *   THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 *   LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 *   EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 *   BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 *   INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 *   OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 *   Copyright 2016 © Costain Integrated Technology Solutions Limited.
 *   All Rights Reserved.
 */

'use strict';

angular.module('rmasApp.user.profile.controller', ['ngRoute', 'rmasApp.user.profile.service', 'rmasApp.alert.service', 'constants', 'rmasApp.module.userDetails'])
        .controller('profileController', function ($scope, $log, userProfileService, alertService, rccService) {

    var originalUser = {};
    $scope.user = {};
    $scope.config = {};
    $scope.rccs = [];
    $scope.showEditbutton = true;

    function gotoEditState(){
        $scope.showEditbutton = false;
        $scope.config.userDetailsDirective.purpose = "edit";
        $scope.config.userDetailsDirective.parameters.default = 'editable';
    }

    function gotoViewState(){
        $scope.showEditbutton = true;
        $scope.config.userDetailsDirective.purpose = "view";
        $scope.config.userDetailsDirective.parameters.default = 'readOnly';
    }

    userProfileService.getUserDetails().then(function (response) {
        $log.debug("The response received the backend", response);
        $scope.user.details = response.data;
        angular.copy($scope.user, originalUser);
    }, function () {
        alertService.addAlert('error', 'Failed to get user details');
    });

    rccService.getRccs().then(function (response) {
        angular.forEach(response.data._embedded.rccs, function(value) {
            $scope.rccs.push(value);
        });
    }, function () {
        alertService.addAlert('error', 'Failed to get RCCs');
    });

    function acceptEdit() {
        return userProfileService.updateUserDetails($scope.user.details).then(function () {
            var successMessage = "User details updated successfully";
            if(!angular.equals(originalUser.details.name, $scope.user.details.name)){
                successMessage = "User name updated";
            }
            alertService.addAlert('success', successMessage);
            angular.copy($scope.user, originalUser);
            gotoViewState();
        }, function () {
           alertService.addAlert('error', 'Failed to update user details');
    });

    }

    function cancelEdit() {
        angular.copy(originalUser, $scope.user);
        gotoViewState();
    }

    function edit() {
        gotoEditState();
    }

    $scope.config.userDetailsDirective = {
        parameters: {
            default: 'readOnly',
            email: 'readOnly',
            projectSponsor: 'hidden',
            accessRequestReason: 'hidden',
            accessRequired: 'hidden',
            tandcAccepted: 'hidden'
        },
        showPlaceHolders: false,
        rccs: $scope.rccs,
        purpose: "view",
        acceptEdit: acceptEdit,
        cancelEdit: cancelEdit
    };

    $scope.edit=edit;

});