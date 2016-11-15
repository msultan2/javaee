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
 * Copyright 2016 (C) Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
'use strict';

var deviceEnrolmentModule = angular.module('rmasApp.deviceEnrolment.controller', []);

deviceEnrolmentModule.controller('deviceEnrolmentController', function ($scope, rccService, alertService, devicesService, $location) {

    $scope.device={};
    $scope.rccs=[];
    $scope.numbersPattern = /^\d{1,4}$/;
    $scope.ipPattern = /^([0-9]{1,3})[.]([0-9]{1,3})[.]([0-9]{1,3})[.]([0-9]{1,3})$/;

    function clear() {
        $scope.device={};
        $scope.deviceEnrolmentForm.$setPristine();
        $scope.device.enrolmentDate=new Date();
    }
    function errorHandler (error) {
        if (error.data[0].defaultMessage === "Duplicate IP address") {
            alertService.addAlert('warning', 'Duplicate IP address');
        } else {
            alertService.addAlert('error', 'Failed to save the device');
        }
    }

    function submit() {
        devicesService.insert($scope.device).then(function() {
            alertService.addAlert('success', 'Device saved successfully', 1);
            devicesService.deleteCache();
            $location.path('/devices');
        }, function (error) {
            errorHandler(error);
        });
    }
    
    function isInvalidUserInput(element) {
        return angular.isDefined(element) && element.$invalid && !element.$pristine;
    }
    
    rccService.getRccs().then(function (response) {
        angular.forEach(response.data._embedded.rccs, function(value) {
            $scope.rccs.push(value);
        });
    }, function () {
        alertService.addAlert('error', 'Failed to get RCCs');
    });

    $scope.clear=clear;
    $scope.submit=submit;
    $scope.device.enrolmentDate=new Date();
    $scope.isInvalidUserInput=isInvalidUserInput;
});

