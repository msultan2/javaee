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

angular.module('rmasApp.module.userDetails', ['ui.bootstrap','rmasApp.user.registration.service','rmasApp.alert.service'])
        .directive("userDetails", function () {
    return {
        restrict: "E",
        scope: {
            config:'=',
            user: '='
        },
        templateUrl: 'directives/userDetailsView.html',
        replace: true,
        controller: function ($scope) {
            
            function showParameter(element) {
                if(!angular.isUndefined($scope.config.parameters[element])){
                    return $scope.config.parameters[element] !== "hidden";
                }else if(!angular.isUndefined($scope.config.parameters.default)){
                    return $scope.config.parameters.default !== "hidden";
                }
                return true;
            }

            function disabledParameter(element) {
                if(!angular.isUndefined($scope.config.parameters[element])){
                    return $scope.config.parameters[element] !== "editable";
                }else if(!angular.isUndefined($scope.config.parameters.default)){
                    return $scope.config.parameters.default !== "editable";
                }
                return false;
            }

            function showSaveButton(){
                return $scope.config.purpose === "edit";
            }
            
            function showCancelButton(){
                return showSaveButton() && !$scope.state.saving;
            }

            function showSubmitClearButtons() {
                return $scope.config.purpose === "create";
            }
            
            function clear() {
                $scope.user = angular.copy($scope.originalUser);
                $scope.userDetailsForm.$setPristine();
            }

            function isInvalidUserInput(element) {
                return angular.isDefined(element) && element.$invalid && !element.$pristine;
            }
            
            function cancelEdit(){
                $scope.config.cancelEdit();
                $scope.userDetailsForm.$setPristine();
            }
            
            function acceptEdit(){
                $scope.state.saving=true;
                $scope.config.acceptEdit().finally(function() {
                    $scope.state.saving = false;                                       
                });
            }

            $scope.state = {
                saving: false
            };

            $scope.showParameter = showParameter;
            $scope.disabledParameter = disabledParameter;
            $scope.showSubmitClearButtons = showSubmitClearButtons;
            $scope.showSaveButton = showSaveButton;
            $scope.showCancelButton = showCancelButton;
            $scope.acceptEdit = acceptEdit;
            $scope.cancelEdit = cancelEdit;
            $scope.clear = clear;
            $scope.isInvalidUserInput = isInvalidUserInput;
            $scope.originalUser = angular.copy($scope.user);
        }
    };
});