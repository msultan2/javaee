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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
'use strict';

var pendingUserRegistrationModule = angular.module('rmasApp.pending.user.registration.controller', ['rmasApp.user.registration.service', 'constants']);

pendingUserRegistrationModule.controller('pendingUserRegistrationController', function ($scope, $log, alertService, userRegistrationService, paths) {
    function getUserRegistrationRequests() {
        userRegistrationService.getUserRegistrationRequests().then(function (response) {
            $scope.userRegistrationRequests.list = response.data._embedded.userRegistrations;
            $log.debug("User access requests list:", $scope.userRegistrationRequests.list);
        }, function (error) {
             $log.error("Error trying to get the user access requests: ", error);
             alertService.addAlert('error', 'Unknown');
        });
    }

    $scope.userRegistrationRequests={list: []};
    $scope.pendingUserRegistrationPath = paths.frontEnd.userRegistrationRequestDetail;
    getUserRegistrationRequests();
});
