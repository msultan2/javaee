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
 * Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 * 
 */
'use strict';

var userLoginModule = angular.module('rmasApp.user.login.controller', ['ngRoute', 'rmasApp.user.login.service', 'rmasApp.alert.service', 'constants']);

userLoginModule.controller('userLoginController', function ($scope,userLoginService,$log,$location, alertService, paths) {
    
    var isLoggingIn=false;

    $scope.login = function () {
        isLoggingIn=true;
        alertService.expireAlerts();
        userLoginService.loginUser($scope.user).then(function () {
            var successRedirect = "/home";
            
            if(angular.isDefined($scope.loginProcess) && angular.isDefined($scope.loginProcess.successRedirect) && $scope.loginProcess.successRedirect!=='/login') {
                successRedirect = $scope.loginProcess.successRedirect;
                $scope.loginProcess.successRedirect = undefined;
                $log.debug("Using saved original destination prior to login of", successRedirect);
            }            
            $log.debug("Logged in, redirecting to", successRedirect);
            $location.path(successRedirect);
        }, function (error) {
            $log.debug("Login error: ", error);
       	    if(error.status===401) {
                alertService.addAlert('error', 'User credentials invalid');
            } else {
                alertService.addAlert('error', 'Unknown');
            }
        }).finally(function() {
            isLoggingIn=false;
        });
    };

    $scope.isLoggingIn = function() {
        return isLoggingIn;
    };

    $scope.isInvalidUserInput = function(element){
        return element.$invalid && !element.$pristine;
    };
    
    $scope.userRegistrationPath = paths.frontEnd.registration;
    $scope.requestResetPasswordPath = paths.frontEnd.requestResetPassword;
});

