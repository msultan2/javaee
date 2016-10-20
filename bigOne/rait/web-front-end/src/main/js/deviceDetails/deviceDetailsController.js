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

var deviceDetailsControllerModule = angular.module('rmasApp.deviceDetails.controller', ['rmasApp.module.fileaccess']);

deviceDetailsControllerModule.controller('deviceDetailsController', function ($scope, $log, $location, $rootScope) {
    $scope.clear = function () {
        $scope.deviceDetailsForm.$setPristine();
        $scope.initDeviceDetailsFormData();
    };

    $scope.next = function () {
        $log.debug("Redirecting to device operation page");
        $location.path('/deviceOperation/'+ $scope.deviceDetailsFormData.ipAddress);
        $rootScope.deviceDetails = $scope.deviceDetailsFormData;
    };

    $scope.loadContent = function ($fileContent) {
        $scope.deviceDetailsFormData.privateKey = ($fileContent.length>0)?$fileContent:null;
    };

    $scope.isInvalidUserInput = function(element){
        return angular.isDefined(element) && element.$invalid && !element.$pristine;
    };
    $scope.numbersPattern = /^\d{1,4}$/;
    $scope.ipPattern = /^([0-9]{1,3})[.]([0-9]{1,3})[.]([0-9]{1,3})[.]([0-9]{1,3})$/;
    
    $scope.initDeviceDetailsFormData = function(){
        $scope.deviceDetailsFormData = {
            'ipAddress': '',
            'bandwidthLimit': '',
            'privateKey': ''
        };
        $log.debug("deviceDetails initialised");
    };
    
    $scope.initDeviceDetailsFormData();
});
