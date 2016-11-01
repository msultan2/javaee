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

deviceOperationControllerModule.controller('deviceOperationController', function ($routeParams, $log, $scope, $uibModal, deviceOperationService, alertService, devicesService, enabledActivities, application) {

    $log.debug("deviceOperationController is created");

    function fetchDeviceDetails() {
        $scope.deviceDetails = {ipAddress: $routeParams.ipAddress};
        $log.debug("Received ipAddress:", $routeParams.ipAddress);
        devicesService.getDeviceDetails($routeParams.ipAddress).then(function (data) {
            $log.debug("Got device details", data);
            $scope.deviceDetails = data.data;
            $scope.isDeviceNotRegisteredInRmas = false;
        }, function (error) {
            $scope.deviceDetails = undefined;
            if (error.status === 404) {
                $log.error("Got error", error);
                $scope.isDeviceNotRegisteredInRmas = true;
                alertService.addAlert('error', 'Device not registered', 0);
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

    function closeDialog() {
        if (angular.isDefined(pollingPromise)) {
            deviceOperationService.stopPolling(pollingPromise);
        }
        $log.debug("Modal dialog closed", new Date());
    }

    function openModalInstance(templateUrl, controller) {
        $scope.modalInstance = $uibModal.open({
            templateUrl: templateUrl,
            controller: controller,
            scope: $scope,
            resolve: {
                promise: function () {
                    return setPromise;
                }
            }
        });

        return $scope.modalInstance.result;
    }

    $scope.openDownloadStaticDataDialog = function () {
        openModalInstance('deviceOperation/downloadStaticDataView.html','downloadStaticDataController').then(fetchDeviceDetails, closeDialog);
    };

    $scope.openUpdateKeyDialog = function () {
        openModalInstance('deviceOperation/updateKeyDialogView.html','updateKeyDialogController').then(angular.noop, closeDialog);
    };

    $scope.openUploadFirmwareDialog = function () {
        openModalInstance('deviceOperation/uploadFirmwareDialogView.html', 'uploadFirmwareDialogController').then(angular.noop, closeDialog);
    };

    $scope.openDownloadLogsDialog = function () {
        openModalInstance('deviceOperation/downloadLogsDialogView.html', 'downloadLogsDialogController').then(angular.noop, closeDialog);
    };

    $scope.openUpgradeFirmwareDialog = function () {
        openModalInstance('deviceOperation/upgradeFirmwareDialogView.html', 'upgradeFirmwareDialogController').then(angular.noop, closeDialog);
    };
    
    $scope.openDowngradeFirmwareDialog = function() {
        openModalInstance('deviceOperation/downgradeFirmwareDialogView.html', 'downgradeFirmwareDialogController').then(angular.noop, closeDialog);
    };
    
    $scope.openResetDeviceDialog = function () {
        openModalInstance('deviceOperation/resetDeviceDialogView.html', 'resetDeviceDialogController').then(angular.noop, closeDialog);
    };
    
    $scope.openPewNumberDialog = function () {
        openModalInstance('deviceOperation/pewNumberForResetDeviceDialogView.html', 'pewNumberForResetDeviceDialogController').then(function(result){
            $scope.pewNumber=result;
            $scope.openResetDeviceDialog();
        }, closeDialog);
    };

    $scope.openVerifyDialog = function () {
        openModalInstance('deviceOperation/verifyDialogView.html', 'verifyDialogController').then(angular.noop, closeDialog);
    };

    $scope.openRemoveOldSshPublicKeysDialog = function () {
        openModalInstance('deviceOperation/removeOldSshPublicKeysDialogView.html', 'removeOldSshPublicKeysDialogController').then(angular.noop, closeDialog);
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
