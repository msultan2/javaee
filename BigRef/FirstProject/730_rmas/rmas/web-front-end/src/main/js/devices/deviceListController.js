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

var deviceListModule = angular.module('rmasApp.devices.deviceList.controller', ['rmasApp.devices.service','rmasApp.module.devicefilter','constants']);

deviceListModule.controller('deviceListController', function ($scope, alertService, $log, devicesService, paths, $rootScope) {

    var defaultFilter = {};
    var filterConfigParams = {};
    $scope.config = {};

    var roles = $rootScope.session.roles;
    for (var index in roles) {
        var value = roles[index];
        if (value.startsWith("DEVICE_FILTER_CONDITION_")) {
            var filterCondition = value.substring("DEVICE_FILTER_CONDITION_".length);
            var filterConditionParts = filterCondition.split("=");
            if (filterConditionParts.length === 2 && filterConditionParts[1].length > 0) {
                var filterConditionKey = filterConditionParts[0];
                var filterConditionValue = filterConditionParts[1];
                defaultFilter[filterConditionKey] = filterConditionValue;
                filterConfigParams[filterConditionKey] = 'readOnly';
                $log.debug(index + " = " + filterConditionKey + ":" + filterConditionValue);
            }
        }
    }
    $log.debug("defaultFilter: ", defaultFilter);

    function setMessageIfLimited(headers) {
        if (headers('maxDevicesReached') === 'true') {
            $scope.devices.maxReached = true;
            $scope.devices.maxReachedMessage = "Reached maximum devices";
        } else {
            $scope.devices.maxReached = false;
        }
    }
    
    function removeEmptyCriterias() {       
        $scope.devices.filters.forEach(function(row){
            for(var filter in row){
                if (angular.equals(row[filter].trim(), "")) {
                    delete row[filter];
                }
            }
        });
    }

    $scope.addFilterRow = function () {
        $scope.devices.filters.push(angular.copy(defaultFilter));
        $log.debug("Added row to filters", $scope.devices.filters);
    };

    $scope.getDeviceList = function () {
        removeEmptyCriterias();
        devicesService.getDeviceList($scope.devices.filters).then(function (response) {
            $scope.devices.list = response.data.deviceList;
            $log.debug("Device list:", $scope.devices.list);
            setMessageIfLimited(response.headers);
        }, function (error) {
            if(angular.equals(error.status,-1)){
                $log.debug("The previous promise was cancelled.");
            }else{
                $log.error("Error trying to get the device list: ", error);
                alertService.addAlert('error', 'Unknown');
            }
        });
    };

    $scope.devices = {list: []};

    $scope.reset = function () {
        $log.debug("Default filter", defaultFilter);
        $scope.devices.filters = [angular.copy(defaultFilter)];
        $scope.getDeviceList();
    };

    $scope.config.deviceFilterDirective = {
        parameters: filterConfigParams,
        showPlaceHolders: false
    };

    $scope.reset();
    $scope.deviceOperationPath = paths.frontEnd.deviceOperation;
    $log.debug("$scope.config.deviceFilterDirective: ", $scope.config.deviceFilterDirective);
    $log.debug("Devices: ", $scope.devices);
    $scope.deviceEnrolmentPath = paths.frontEnd.deviceEnrolment;
});

