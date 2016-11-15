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

var resetPasswordModule = angular.module('rmasApp.user.resetPassword.controller', ['ngRoute', 'rmasApp.user.resetPassword.service', 'rmasApp.alert.service']);

resetPasswordModule.controller('resetPasswordController', function ($routeParams, $scope, resetPasswordService, alertService, $location) {

    if(angular.isUndefined($scope.resetPassword)) {
        $scope.resetPassword = {};
    }

    $scope.submit = function () {
        $scope.resetPassword.submittedPassword = true;
        resetPasswordService.resetPassword($routeParams.tokenId, $scope.newPassword).then(function () {
            alertService.addAlert('success', 'Password reset successful', 1);
            $location.path('/login');
        }, function () {
            alertService.addAlert('error', 'Password reset failed', 0);
        }).finally(function(){
            $scope.resetPassword.submittedPassword = false;
        });
    };

    $scope.clear = function () {
        $scope.resetPasswordForm.$setPristine();
    };

    $scope.isInvalidUserInput = function (element) {
        return angular.isDefined(element) && element.$invalid && !element.$pristine;
    };
}).directive('passwordCheck', function() {
    return {
        require : 'ngModel',
        link : function($scope, elm, attrs, ctrl) {
            void elm;
            void attrs;           
            ctrl.$validators.passwordCheck = function(modelValue) {
                if (ctrl.$isEmpty(modelValue)) {
                    // consider empty models to be valid
                    return true;
                }
                return $scope.resetPasswordForm.password.$viewValue === modelValue;
            };
        }
    };
}).directive('password', function($log) {
    return {
        require : 'ngModel',
        link : function($scope, elm, attrs, ctrl) {
            void $scope;
            void elm;
            void attrs;           
            ctrl.$validators.password = function(modelValue) {
                $scope.resetPasswordForm.passwordCheck.$validate();

                var valid = true;
                
                if (!ctrl.$isEmpty(modelValue)) {
	                var rulesBroken = 0;
	                if(modelValue.toUpperCase()===modelValue) {
	                	//No lower case characters
	                	$log.debug("No lower case characters");
	                	rulesBroken++;
	                }
	                if(modelValue.toLowerCase()===modelValue) {
	                	//No upper case characters
	                	$log.debug("No upper case characters");
	                	rulesBroken++;
	                }
	                if(modelValue.match(/\d/g) === null) {
	                	//No digits
	                	$log.debug("No digits characters");
	                	rulesBroken++;
	                }
	                if(modelValue.match(/(\W)/g) === null) {
	                	//No non alphanumeric characters
	                	$log.debug("No non alpha numeric characters");
	                	rulesBroken++;
	                }
	                
	                if(rulesBroken > 1) {
	                    $log.debug("Too many broken rules");
	                	valid = false;
	                }	                
	                if(modelValue.match(/(.)\1/g) !== null) {
	                	//Contains repeats
	                	$log.debug("Contains repeats");
	                	valid = false;
	                }
	                if(modelValue.length < 8) {
	                	$log.debug("Password too short");
	                	valid = false;
	                }
                }
                return valid;
            };
        }
    };
});
