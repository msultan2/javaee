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
 *  Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 */

'use strict';

var resetPasswordModule = angular.module('rmasApp.user.requestResetPassword.controller', ['ngRoute', 'rmasApp.user.resetPassword.service', 'rmasApp.alert.service']);

resetPasswordModule.controller('requestResetPasswordController', function ($scope, resetPasswordService, alertService, $location) {

    $scope.submit = function () {
        resetPasswordService.requestResetPasswordEmail($scope.user).then(function () {
            alertService.addAlert('success', 'Password reset request successful', 1);
            $location.path('/login');
        }, function () {
            alertService.addAlert('error', 'Password reset request failed', 0);
        });
    };

    $scope.clear = function () {
    	$scope.user = undefined;
        $scope.resetPasswordForm.$setPristine();
    };

    $scope.isInvalidUserInput = function (element) {
        return angular.isDefined(element) && element.$invalid && !element.$pristine;
    };
});
