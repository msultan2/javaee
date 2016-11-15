/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */

'use strict';

var userRegistrationModule = angular.module('rmasApp.user.registration.controller', ['ngRoute', 'rmasApp.user.registration.service', 'rmasApp.alert.service', 'constants']);

userRegistrationModule.controller('userRegistrationController', function ($scope, userRegistrationService, alertService, $location, rccService) {

    $scope.user = {};
    $scope.config = {};

    $scope.submit = function () {
        userRegistrationService.submit($scope.user.details).then(function () {
            alertService.addAlert('success', 'User registration successful', Infinity);
            $location.path('/login');
        }, function () {
            alertService.addAlert('error', 'User registration failed', Infinity);
        });
    };

    $scope.projectSponsors = [];
    $scope.rccs = [];

    userRegistrationService.getProjectSponsors().then(function (response) {
        angular.forEach(response.data._embedded.users, function(value) {
            $scope.projectSponsors.push(value);
        });
    }, function () {
        alertService.addAlert('error', 'Failed to get project sponsors');
    });

    rccService.getRccs().then(function (response) {
        angular.forEach(response.data._embedded.rccs, function(value) {
            $scope.rccs.push(value);
        });
    }, function () {
        alertService.addAlert('error', 'Failed to get RCCs');
    });

    $scope.config.userDetailsDirective = {
        parameters: {
            default: 'editable'
        },
        submit: $scope.submit,
        projectSponsors: $scope.projectSponsors,
        rccs: $scope.rccs,
        showPlaceHolders: true,
        purpose: 'create'
    };
});
