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

var deviceOperationControllerModule = angular.module('rmasApp.deviceOperation.controller', ['rmasApp.deviceOperation.service', 'constants', 'ngRoute', 'rmasApp.alert.service', 'rmasApp.devices.service', 'ui.bootstrap']);

deviceOperationControllerModule.controller('deviceOperationController', function ($routeParams, $log, $scope, alertService, devicesService, enabledActivities, application, modalDialogService) {

    $log.debug("deviceOperationController is created");

    function fetchDeviceDetails() {        
        var bandwidthLimit;
        if(angular.isDefined($scope.deviceDetails)){
             bandwidthLimit = $scope.deviceDetails.bandwidthLimit;
        }
        $log.debug("Received ipAddress:", $routeParams.ipAddress);

        devicesService.getDeviceDetails($routeParams.ipAddress).then(function (data) {
            $log.debug("Got device details", data);
            $scope.deviceDetails = data.data;
            if($scope.deviceDetails.bandwidthLimit === 0){
                $scope.deviceDetails.bandwidthLimit = bandwidthLimit;
            }
            $scope.isDeviceNotRegisteredInRmas = false;
        }, function (error) {
            $scope.deviceDetails = undefined;
            if (error.status === 404) {
                $log.error("Got error", error);
                $scope.isDeviceNotRegisteredInRmas = true;
                alertService.addAlert('warning', 'Device not registered', 0);
            } else {
                $log.error("Got error", error);
                alertService.addAlert('error', 'Unknown', 0);
            }
        });
    }

    var pollingPromise;
    
    function setPromise(promiseIn) {
        pollingPromise = promiseIn;
    }

    $scope.$on('$locationChangeStart', function () {
        if (angular.isDefined($scope.modalInstance)) {
            $scope.modalInstance.dismiss();
        }
    });

    function openDialog(view, controller, scope, setPromise, successHandler) {
        var partialCloseDialog = angular.bind(this, modalDialogService.closeDialog, pollingPromise);
        modalDialogService.openModalInstance(view, controller, scope, setPromise)
                .then(successHandler, partialCloseDialog);
    }

    $scope.openDownloadStaticDataDialog = function () {
        openDialog('deviceOperation/downloadStaticDataView.html', 'downloadStaticDataController', $scope, setPromise, fetchDeviceDetails);
    };

    $scope.openUpdateKeyDialog = function () {
        openDialog('deviceOperation/updateKeyDialogView.html', 'updateKeyDialogController', $scope, setPromise, angular.noop);
    };

    $scope.openUploadFirmwareDialog = function () {
        openDialog( 'deviceOperation/uploadFirmwareDialogView.html', 'uploadFirmwareDialogController', $scope, setPromise, angular.noop);
    };

    $scope.openDownloadLogsDialog = function () {
        openDialog('deviceOperation/downloadLogsDialogView.html', 'downloadLogsDialogController', $scope, setPromise, angular.noop);
    };

    $scope.openUpgradeFirmwareDialog = function () {
        openDialog('deviceOperation/upgradeFirmwareDialogView.html', 'upgradeFirmwareDialogController', $scope, setPromise, angular.noop);
    };
    
    $scope.openDowngradeFirmwareDialog = function() {
        openDialog('deviceOperation/downgradeFirmwareDialogView.html', 'downgradeFirmwareDialogController', $scope, setPromise, angular.noop);
    };
    
    $scope.openResetDeviceDialog = function () {
        openDialog('deviceOperation/resetDeviceDialogView.html', 'resetDeviceDialogController', $scope, setPromise, angular.noop);
    };
    
    $scope.openPewNumberDialog = function () {
        openDialog('deviceOperation/pewNumberForResetDeviceDialogView.html', 'pewNumberForResetDeviceDialogController', $scope, setPromise, function(result){
            $scope.pewNumber=result;
            $scope.openResetDeviceDialog();
        });
    };

    $scope.openVerifyDialog = function () {
        openDialog('deviceOperation/verifyDialogView.html', 'verifyDialogController', $scope, setPromise, angular.noop);
    };

    $scope.openRemoveOldSshPublicKeysDialog = function () {
        openDialog('deviceOperation/removeOldSshPublicKeysDialogView.html', 'removeOldSshPublicKeysDialogController', $scope, setPromise, angular.noop);
    };

    fetchDeviceDetails();
    $scope.enabledActivities = enabledActivities;
    $scope.pewNumber = {
       'number':''
    } ;
    $scope.application = {
            brand: application.brand
        };
});
