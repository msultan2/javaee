/**
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
 */

'use strict';

var deviceFilterModule = angular.module('rmasApp.module.devicefilter', ['ui.bootstrap']);

deviceFilterModule.directive("deviceFilter", function () {
    return {
        restrict: "E",
        scope: {
            config:'=',
            filters:'='
        },
        templateUrl: 'directives/deviceFilterView.html',
        replace: true,
        controller: function ($scope, $log, alertService, rccService) {
            $scope.rccs = [];

            $scope.deleteRow = function (index) {
                $scope.filters.splice(index, 1);
                $log.debug("Deleted row from filters", $scope.filters);
            };

            $scope.showDeleteRow = function () {
                return $scope.filters.length > 1;
            };

            function disabledParameter(element) {
                if(!angular.isUndefined($scope.config.parameters[element])){
                    return $scope.config.parameters[element] !== "editable";
                }else if(!angular.isUndefined($scope.config.parameters.default)){
                    return $scope.config.parameters.default !== "editable";
                }
                return false;
            }

            rccService.getRccs().then(function (response) {
                angular.forEach(response.data._embedded.rccs, function (value) {
                    $scope.rccs.push(value);
                });
            }, function () {
                alertService.addAlert('error', 'Failed to get RCCs');
            });
            $scope.disabledParameter = disabledParameter;
        }
    };
});