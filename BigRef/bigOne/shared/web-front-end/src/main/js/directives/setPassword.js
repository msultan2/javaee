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
angular.module('rmasApp.module.setPassword', ['ui.bootstrap','rmasApp.alert.service'])
        .directive("setPassword", function () {
    return {
        restrict: "E",
        scope: {
            config:'='
        },
        templateUrl: 'directives/setPasswordView.html',
        replace: true,
        controller: function ($scope) {
            function clear() {
                $scope.setPasswordForm.$setPristine();
            }
            function isInvalidUserInput(element) {
                return angular.isDefined(element) && element.$invalid && !element.$pristine;
            }
            function submit() {
                $scope.resetPassword.submittedPassword = true;
                if ($scope.config.currentPasswordVisible) {
                    $scope.config.submit($scope.currentPassword, $scope.newPassword).finally(function (){
                        $scope.resetPassword.submittedPassword = false;
                    });
                } else {
                    $scope.config.submit($scope.newPassword).finally(function (){
                        $scope.resetPassword.submittedPassword = false;
                    });
                }
            }
            if (angular.isUndefined($scope.resetPassword)) {
                $scope.resetPassword = {};
            }
            $scope.clear = clear;
            $scope.submit = submit;
            $scope.isInvalidUserInput = isInvalidUserInput;
        }
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
                return $scope.setPasswordForm.password.$viewValue === modelValue;
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
                $scope.setPasswordForm.passwordCheck.$validate();
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