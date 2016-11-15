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

var changePasswordModule = angular.module('rmasApp.user.changePassword.controller', ['ngRoute', 'rmasApp.user.password.service', 'rmasApp.alert.service']);

changePasswordModule.controller('changePasswordController', function ($scope, $log, passwordService, alertService, $location) {

    $scope.config = {};

    $scope.submit = function (currentPassword, newPassword) {
        return passwordService.changePassword(currentPassword, newPassword).then(function () {
            alertService.addAlert('success', 'Password changed successfully', 1);
            $location.path('/home');
        }, function (error) {
            if (error.status === 400) {
                $log.error("Invalid password", error);
                alertService.addAlert('warning', 'Invalid password', 0);
            } else {
                $log.error("Password change failed", error);
                alertService.addAlert('warning', 'Password change failed', 0);
            }
        });
    };

    $scope.config.setPasswordDirective = {
        currentPasswordVisible: true,
        submit: $scope.submit
    };
});