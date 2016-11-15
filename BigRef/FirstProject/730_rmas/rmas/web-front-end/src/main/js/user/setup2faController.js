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

var setup2faModule = angular.module('rmasApp.user.setup2fa.controller', ['ngRoute', 'rmasApp.alert.service', 'rmasApp.user.profile.service', 'rmasApp.twoFactorAuthentication.service', 'monospaced.qrcode', 'constants']);

setup2faModule.controller('setup2faController', function ($scope, $route, $rootScope, $window, $log, twoFactorAuthenticationService, userProfileService, alertService) {

    $scope.generateNewToken = function() {
        $log.debug("New 2FA token requested");
        $scope.generatingToken = true;
        alertService.expireAlerts();
        twoFactorAuthenticationService.getNew2faSecret().then(function(response){
            $log.debug("Got new secret");
            $scope.user.newTwoFactorAuthentication = {
                secret: response.data,
                url: 'otpauth://totp/' + $window.encodeURIComponent($rootScope.session.name) + '?secret=' + response.data + '&issuer=RMAS'
            };
            
            $log.debug("New totp uri", $scope.user.newTwoFactorAuthentication.url);
        }, function(error){
            $log.error("Failed to get new secret", error);
            alertService.addAlert('error', 'Failed to get new 2FA code', 0);
        }).finally(function() {
            $scope.generatingToken = false;
        });
    };
    
    $scope.verifyTokenAndCode = function() {
        var verifyData = {secret: $scope.user.newTwoFactorAuthentication.secret, verificationCode: $scope.user.newTwoFactorAuthentication.verificationCode};
        twoFactorAuthenticationService.verifyNewToken(verifyData).then(function(response){
            $log.debug("Verified new 2FA token", response);
            alertService.addAlert('success', '2FA set up', 1);
            $route.reload();
        }, function(error) {
            $log.debug("Failed to verified new 2FA token", error);
            alertService.addAlert('warning', '2FA verification failed', 0);
            $scope.user.newTwoFactorAuthentication = {};
        });
    };

    userProfileService.getUser2faEnabled().then(function(response){
        $log.debug("Got 2FA status", response.data);
        $scope.user = {twoFactorAuthentication: {enabled: response.data}};
    },function(error){
        $log.error("Failed to get 2FA status", error);
        alertService.addAlert('error', 'Failed to get 2FA details', 0);
    });
});

