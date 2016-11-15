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

var userLoginModule = angular.module('rmasApp.user.login.controller', ['ngRoute', 'rmasApp.user.login.service', 'rmasApp.alert.service', 'constants']);

userLoginModule.controller('userLoginController', function ($scope, userLoginService, $log, $location, $filter, alertService, paths) {
    
    var isLoggingIn=false;

    function showLastLoggedMessage(loginInformation) {
        if ((loginInformation.lastLogin !== null) && (loginInformation.lastLogin !== "")) {
            var messageArray = {
                "message" :"Last logged in",
                "args" : [$filter('date')(loginInformation.lastLogin, 'dd-MM-yyyy HH:mm:ss')]
            } ;
            alertService.addAlert('success', messageArray, 1);
        }
    }

    $scope.login = function () {
        isLoggingIn=true;
        alertService.expireAlerts();
        userLoginService.loginUser($scope.user).then(function (loginInformation) {
            var successRedirect = "/home";
            
            if (loginInformation.passwordExpired) {
                alertService.addAlert('success', "Password expired", 1);
                successRedirect = "/changePassword";
                $log.debug("Expired password, redirecting to", successRedirect);
            } else {
                if(angular.isDefined($scope.loginProcess) && angular.isDefined($scope.loginProcess.successRedirect) && $scope.loginProcess.successRedirect!=='/login') {
                    successRedirect = $scope.loginProcess.successRedirect;
                    $scope.loginProcess.successRedirect = undefined;
                    $log.debug("Using saved original destination prior to login of", successRedirect);
                }
            }
            $log.debug("Logged in, redirecting to", successRedirect);
            showLastLoggedMessage(loginInformation);
            $location.path(successRedirect);
        }, function (error) {
            $log.debug("Login error: ", error);
       	    if(error.status===401) {
       	        if($scope.show2faInput) {
                    alertService.addAlert('warning', '2FA User credentials invalid');
       	        } else {
                    alertService.addAlert('warning', 'User credentials invalid');
       	        }
                $scope.show2faInput = false;
                delete $scope.user.twoFactorAuthenticationCode;
                delete $scope.user.password;
       	    } else if(error.status===400) {
       	    	$log.debug("2FA credentials required");
       	    	$scope.show2faInput = true;
            } else if(error.status===403){
                $log.debug("User account is locked or suspended");
                var errorMessageKey;
                if (error.data.length === 1) {
                    errorMessageKey = error.data[0];
                } else {
                    errorMessageKey = "Unknown";
                }
                alertService.addAlert('error', errorMessageKey);
                delete $scope.user.twoFactorAuthenticationCode;
                delete $scope.user.password;
                $scope.show2faInput = false;
            } else {
                $scope.show2faInput = false;
                delete $scope.user.twoFactorAuthenticationCode;
                delete $scope.user.password;
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

